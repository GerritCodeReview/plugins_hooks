package com.googlesource.gerrit.plugins.hooks;

import com.google.common.flogger.FluentLogger;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gerrit.extensions.events.LifecycleListener;
import com.google.gerrit.server.config.GerritServerConfig;
import com.google.gerrit.server.logging.LoggingContextAwareExecutorService;
import com.google.inject.Inject;
import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.eclipse.jgit.lib.Config;

public class HookExecutor implements LifecycleListener {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private static final UncaughtExceptionHandler LOG_UNCAUGHT_EXCEPTION =
      (t, e) ->
          logger.atSevere().withCause(e).log("HookExecutor thread %s threw exception", t.getName());

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
                    .setUncaughtExceptionHandler(LOG_UNCAUGHT_EXCEPTION)
                    .build()));
  }

  HookResult submit(Path hook, HookArgs args) {
    return submit(null, hook, args);
  }

  HookResult submit(String projectName, Path hook, HookArgs args) {
    if (!Files.exists(hook)) {
      logger.atFine().log("Hook file not found: %s", hook);
      return null;
    }
    HookTask.Sync hookTask = new HookTask.Sync(projectName, hook, args);
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
}
