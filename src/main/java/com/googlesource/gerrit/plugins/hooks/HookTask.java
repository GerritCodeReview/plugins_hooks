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

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.flogger.FluentLogger;
import com.google.common.io.ByteStreams;
import com.google.gerrit.metrics.Timer1;
import com.google.gerrit.reviewdb.client.Project;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import org.eclipse.jgit.lib.Repository;

class HookTask {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private final Path sitePath;
  private final String projectName;
  private final Path hook;
  private final HookArgs args;
  private StringWriter output;
  private Process ps;

  public static class Async extends HookTask implements Runnable {
    Async(String projectName, Path hook, HookArgs args) {
      super(projectName, hook, args);
    }

    @Override
    public void run() {
      super.runHook();
    }
  }

  public static class Sync extends HookTask implements Callable<HookResult> {
    Sync(String projectName, Path hook, HookArgs args) {
      super(projectName, hook, args);
    }

    @Override
    public HookResult call() throws Exception {
      return super.runHook();
    }
  }

  HookTask(String projectName, Path hook, HookArgs args) {
    this.projectName = projectName;
    this.hook = hook;
    this.args = args;
    this.sitePath = args.sitePaths.site_path;
  }

  public void cancel() {
    ps.destroy();
  }

  protected String getName() {
    return hook.getFileName().toString();
  }

  public String getOutput() {
    return output != null ? output.toString() : null;
  }

  public HookResult runHook() {
    HookResult result = null;
    String name = getName();
    try (Timer1.Context timer = args.metrics.start(name)) {
      args.metrics.count(name);
      List<String> argv = new ArrayList<>(1 + args.get().size());
      argv.add(hook.toAbsolutePath().toString());
      argv.addAll(args.get());

      ProcessBuilder pb = new ProcessBuilder(argv);
      pb.redirectErrorStream(true);

      Map<String, String> env = pb.environment();
      env.put("GERRIT_SITE", sitePath.toAbsolutePath().toString());

      if (projectName != null) {
        try (Repository git = args.gitManager.openRepository(new Project.NameKey(projectName))) {
          pb.directory(git.getDirectory());
          env.put("GIT_DIR", git.getDirectory().getAbsolutePath());
        }
      }

      ps = pb.start();
      ps.getOutputStream().close();
      String out = new String(ByteStreams.toByteArray(ps.getInputStream()), UTF_8);
      ps.waitFor();
      result = new HookResult(ps.exitValue(), out);
    } catch (InterruptedException iex) {
      // InterruptedException - timeout or cancel
      args.metrics.timeout(name);
      logger.atSevere().log("hook[%s] timed out: %s", name, iex.getMessage());
    } catch (Throwable err) {
      args.metrics.error(name);
      logger.atSevere().withCause(err).log("Error running hook %s", hook.toAbsolutePath());
    }

    if (result != null) {
      int exitValue = result.getExitValue();
      if (exitValue != 0) {
        logger.atSevere().log("hook[%s] exited with error status: %d", name, exitValue);
      }

      if (logger.atFine().isEnabled()) {
        try (BufferedReader br = new BufferedReader(new StringReader(result.getOutput()))) {
          br.lines()
              .filter(s -> !s.isEmpty())
              .forEach(line -> logger.atFine().log("hook[%s] output: %s", name, line));
        } catch (IOException iox) {
          logger.atSevere().withCause(iox).log("Error writing hook [%s] output", name);
        }
      }
    }

    return result;
  }

  @Override
  public String toString() {
    return "hook " + hook.getFileName();
  }
}
