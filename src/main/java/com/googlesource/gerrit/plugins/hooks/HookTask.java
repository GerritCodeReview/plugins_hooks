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

import com.google.gerrit.reviewdb.client.Project;
import com.google.gerrit.server.git.GitRepositoryManager;

import org.eclipse.jgit.lib.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

class HookTask {
  private static final Logger log = LoggerFactory.getLogger(HookTask.class);

  private final GitRepositoryManager gitManager;
  private final Path sitePath;
  private final String projectName;
  private final Path hook;
  private final List<String> args;
  private StringWriter output;
  private Process ps;

  public static class Async extends HookTask implements Runnable {
    Async(GitRepositoryManager gitManager, Path sitePath, String projectName,
        Path hook, HookArgs args) {
      super(gitManager, sitePath, projectName, hook, args);
    }

    @Override
    public void run() {
      super.runHook();
    }
  }

  public static class Sync extends HookTask implements Callable<HookResult> {
    Sync(GitRepositoryManager gitManager, Path sitePath, String projectName,
        Path hook, HookArgs args) {
      super(gitManager, sitePath, projectName, hook, args);
    }

    @Override
    public HookResult call() throws Exception {
      return super.runHook();
    }
  }

  HookTask(GitRepositoryManager gitManager,
      Path sitePath,
      String projectName,
      Path hook,
      HookArgs args) {
    this.gitManager = gitManager;
    this.sitePath = sitePath;
    this.projectName = projectName;
    this.hook = hook;
    this.args = args.get();
  }

  private String readOutput(InputStream is) throws IOException {
    output = new StringWriter();
    InputStreamReader input = new InputStreamReader(is);
    char[] buffer = new char[4096];
    int n;
    while ((n = input.read(buffer)) != -1) {
      output.write(buffer, 0, n);
    }

    return output.toString();
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
    try {
      List<String> argv = new ArrayList<>(1 + args.size());
      argv.add(hook.toAbsolutePath().toString());
      argv.addAll(args);

      ProcessBuilder pb = new ProcessBuilder(argv);
      pb.redirectErrorStream(true);

      Map<String, String> env = pb.environment();
      env.put("GERRIT_SITE", sitePath.toAbsolutePath().toString());

      if (projectName != null) {
        try (Repository git = gitManager.openRepository(
              new Project.NameKey(projectName))) {
          pb.directory(git.getDirectory());
          env.put("GIT_DIR", git.getDirectory().getAbsolutePath());
        }
      }

      ps = pb.start();
      ps.getOutputStream().close();
      String output = null;
      try (InputStream is = ps.getInputStream()) {
        output = readOutput(is);
      } finally {
        ps.waitFor();
        result = new HookResult(ps.exitValue(), output);
      }
    } catch (InterruptedException iex) {
      // InterruptedExeception - timeout or cancel
    } catch (Throwable err) {
      log.error("Error running hook " + hook.toAbsolutePath(), err);
    }

    if (result != null) {
      int exitValue = result.getExitValue();
      if (exitValue == 0) {
        log.debug("hook[" + getName() + "] exitValue:" + exitValue);
      } else {
        log.info("hook[" + getName() + "] exitValue:" + exitValue);
      }

      BufferedReader br =
          new BufferedReader(new StringReader(result.getOutput()));
      try {
        String line;
        while ((line = br.readLine()) != null) {
          log.info("hook[" + getName() + "] output: " + line);
        }
      } catch (IOException iox) {
        log.error("Error writing hook output", iox);
      }
    }

    return result;
  }

  @Override
  public String toString() {
    return "hook " + hook.getFileName();
  }
}

