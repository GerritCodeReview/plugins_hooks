package com.googlesource.gerrit.plugins.hooks;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.gerrit.extensions.events.LifecycleListener;
import com.google.gerrit.server.config.GerritServerConfig;
import com.google.gerrit.server.config.SitePaths;
import com.google.gerrit.server.git.GitRepositoryManager;
import com.google.inject.Inject;

import org.eclipse.jgit.lib.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.Thread.UncaughtExceptionHandler;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class HookExecutor implements LifecycleListener {
  private static final Logger log =
      LoggerFactory.getLogger(HookExecutor.class);
  private static final UncaughtExceptionHandler LOG_UNCAUGHT_EXCEPTION =
      new UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
          log.error("HookExecutor thread " + t.getName()
            + " threw exception", e);
        }
      };

  private final SitePaths sitePaths;
  private final GitRepositoryManager gitManager;
  private final ExecutorService threadPool;
  private final int timeout;

  @Inject
  HookExecutor(@GerritServerConfig Config config,
      SitePaths sitePaths,
      GitRepositoryManager gitManager) {
    this.sitePaths = sitePaths;
    this.gitManager = gitManager;

    this.timeout = config.getInt("hooks", "syncHookTimeout", 30);
    this.threadPool = Executors.newCachedThreadPool(
        new ThreadFactoryBuilder()
          .setNameFormat("SyncHook-%d")
          .setUncaughtExceptionHandler(LOG_UNCAUGHT_EXCEPTION)
          .build());
  }

  HookResult submit(Path hook, HookArgs args) {
    return submit(null, hook, args);
  }

  HookResult submit(String projectName, Path hook, HookArgs args) {
    if (!Files.exists(hook)) {
      return null;
    }
    HookTask.Sync hookTask = new HookTask.Sync(
        gitManager, sitePaths.site_path, projectName, hook, args);
    FutureTask<HookResult> task = new FutureTask<>(hookTask);
    threadPool.execute(task);
    String message;

    try {
      return task.get(timeout, TimeUnit.SECONDS);
    } catch (TimeoutException e) {
      message = "Synchronous hook timed out "  + hook.toAbsolutePath();
      log.error(message);
    } catch (Exception e) {
      message = "Error running hook " + hook.toAbsolutePath();
      log.error(message, e);
    }
    task.cancel(true);
    hookTask.cancel();

    return new HookResult(hookTask.getOutput(), message);
  }

  @Override
  public void start() {
  }

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
