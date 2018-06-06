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

import static com.google.common.base.MoreObjects.firstNonNull;

import com.google.common.flogger.FluentLogger;
import com.google.gerrit.server.config.GerritServerConfig;
import com.google.gerrit.server.config.SitePaths;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.jgit.lib.Config;

@Singleton
public class HookFactory {
  private static final FluentLogger logger = FluentLogger.forEnclosingClass();

  private final HookQueue queue;
  private final HookExecutor syncHookExecutor;
  private final Config config;
  private final Path hooksPath;
  private final HookArgs.Factory argsFactory;

  @Inject
  HookFactory(
      HookQueue queue,
      HookExecutor syncHookExecutor,
      @GerritServerConfig Config config,
      SitePaths sitePaths,
      HookArgs.Factory argsFactory) {
    this.queue = queue;
    this.syncHookExecutor = syncHookExecutor;
    this.config = config;
    this.argsFactory = argsFactory;

    String v = config.getString("hooks", null, "path");
    if (v != null) {
      this.hooksPath = Paths.get(v);
    } else {
      this.hooksPath = sitePaths.hooks_dir;
    }
    logger.atInfo().log("hooks.path: %s", this.hooksPath);
  }

  private Path getHookPath(String configName, String defaultName) {
    String v = config.getString("hooks", null, configName);
    Path hookPath = hooksPath.resolve(firstNonNull(v, defaultName));
    logger.atInfo().log("hooks.%s resolved to %s", configName, hookPath);
    return hookPath;
  }

  public Hook createAsync(String configName, String defaultName) {
    return new AsynchronousHook(queue, getHookPath(configName, defaultName));
  }

  public Hook createSync(String configName, String defaultName) {
    return new SynchronousHook(syncHookExecutor, getHookPath(configName, defaultName));
  }

  public HookArgs createArgs() {
    return argsFactory.create();
  }
}
