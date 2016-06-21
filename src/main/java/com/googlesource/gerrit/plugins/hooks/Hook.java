// Copyright (C) 2012 The Android Open Source Project
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

import com.google.gerrit.server.config.GerritServerConfig;
import com.google.gerrit.server.config.SitePaths;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.eclipse.jgit.lib.Config;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Hook {
  @Singleton
  public static class Factory {
    private final HookQueue queue;
    private final Config config;
    private final Path hooksPath;

    @Inject
    Factory(HookQueue queue,
        @GerritServerConfig Config config,
        SitePaths sitePaths) {
      this.queue = queue;
      this.config = config;

      String v = config.getString("hooks", null, "path");
      if (v != null) {
        this.hooksPath = Paths.get(v);
      } else {
        this.hooksPath = sitePaths.hooks_dir;
      }
    }

    public Hook create(String configName, String defaultName) {
      String v = config.getString("hooks", null, configName);
      Path path = hooksPath.resolve(v != null ? v : defaultName);
      return new Hook(queue, path);
    }
  }

  private final HookQueue queue;
  private final Path path;

  Hook(HookQueue queue, Path path) {
    this.queue = queue;
    this.path = path;
  }

  void submit(HookArgs args) {
    queue.submit(path, args);
  }

  void submit(String projectName, HookArgs args) {
    queue.submit(projectName, path, args);
  }
}
