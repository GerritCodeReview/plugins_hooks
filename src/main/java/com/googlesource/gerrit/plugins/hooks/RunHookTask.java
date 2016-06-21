// Copyright (C) 2010 The Android Open Source Project
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
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class RunHookTask implements Runnable {
  private static final Logger log = LoggerFactory.getLogger(RunHookTask.class);

  private final GitRepositoryManager gitManager;
  private final Path sitePath;
  private final String projectName;
  private final Path hook;
  private final List<String> args;

  RunHookTask(GitRepositoryManager gitManager,
      Path site_path,
      String projectName,
      Path hook,
      HookArgs args) {
    this.gitManager = gitManager;
    this.sitePath = site_path;
    this.projectName = projectName;
    this.hook = hook;
    this.args = args.get();
  }

  @Override
  public void run() {
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

      Process ps = pb.start();
      ps.getOutputStream().close();

      try (BufferedReader br = new BufferedReader(
          new InputStreamReader(ps.getInputStream()))) {
        String line;
        while ((line = br.readLine()) != null) {
          log.info("hook[" + hook.getFileName() + "] output: " + line);
        }
      } finally {
        ps.waitFor();
      }
    } catch (Throwable err) {
      log.error("Error running hook " + hook.toAbsolutePath(), err);
    }
  }

  @Override
  public String toString() {
    return "hook " + hook.getFileName();
  }
}

