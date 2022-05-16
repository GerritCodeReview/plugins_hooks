// Copyright (C) 2016 The Android Open Source Project
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.googlesource.gerrit.plugins.hooks;

import com.google.common.collect.ImmutableListMultimap;
import com.google.common.flogger.FluentLogger;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gerrit.extensions.events.LifecycleListener;
import com.google.gerrit.server.config.GerritServerConfig;
import com.google.gerrit.server.logging.LoggingContextAwareExecutorService;
import com.google.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.Optional;
import org.eclipse.jgit.lib.Config;

public class HookExecutor implements LifecycleListener {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private final ExecutorService threadPool;
  private final int timeout;

  @Inject
  HookExecutor(@GerritServerConfig Config config) {
    this.timeout = config.getInt("hooks", "syncHookTimeout", 30);
    this.threadPool =
        new LoggingContextAwareExecutorService(
            Executors.newCachedThreadPool(
                new ThreadFactoryBuilder()
                    .setNameFormat("SyncHook-%d")
                    .setUncaughtExceptionHandler(HookExecutor::logUncaughtException)
                    .build()));
  }

  HookResult submit(Path hook, HookArgs args) {
    return submit(null, hook, args);
  }

  HookResult submit(String projectName, Path hook, HookArgs args) {
    return submit(null, hook, args, Optional.empty());
  }

  HookResult submit(
      String projectName, Path hook, HookArgs args, Optional<ImmutableListMultimap> pushOptions) {
    if (!Files.exists(hook)) {
      logger.atFine().log("Hook file not found: %s", hook);
      return null;
    }
    HookTask.Sync hookTask = new HookTask.Sync(projectName, hook, args, pushOptions);
    FutureTask<HookResult> task = new FutureTask<>(hookTask);
    threadPool.execute(task);
    String message;

    try {
      return task.get(timeout, TimeUnit.SECONDS);
    } catch (TimeoutException e) {
      message = "Synchronous hook timed out " + hook;
      logger.atSevere().log(message);
    } catch (Exception e) {
      message = "Error running hook " + hook;
      logger.atSevere().withCause(e).log(message);
    }
    task.cancel(true);
    hookTask.cancel();

    return new HookResult(hookTask.getOutput(), message);
  }

  @Override
  public void start() {}

  @Override
  public void stop() {
    threadPool.shutdown();
    boolean isTerminated;
    do {
      try {
        isTerminated = threadPool.awaitTermination(10, TimeUnit.SECONDS);
      } catch (InterruptedException ie) {
        isTerminated = false;
      }
    } while (!isTerminated);
  }

  private static void logUncaughtException(Thread t, Throwable e) {
    logger.atSevere().withCause(e).log("HookExecutor thread %s threw exception", t.getName());
  }
}
