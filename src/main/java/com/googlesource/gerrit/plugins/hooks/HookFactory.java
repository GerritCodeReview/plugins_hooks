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

import com.google.gerrit.common.Nullable;
import com.google.gerrit.server.IdentifiedUser;
import com.google.gerrit.server.config.CanonicalWebUrl;
import com.google.gerrit.server.config.GerritServerConfig;
import com.google.gerrit.server.config.SitePaths;
import com.google.gerrit.server.git.GitRepositoryManager;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.eclipse.jgit.lib.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class HookFactory {
  private static final Logger log = LoggerFactory.getLogger(HookFactory.class);

  private final HookQueue queue;
  private final HookExecutor syncHookExecutor;
  private final Config config;
  private final IdentifiedUser.GenericFactory identifiedUserFactory;
  private final HookMetrics metrics;
  private final Provider<String> urlProvider;
  private final Path hooksPath;
  private final GitRepositoryManager gitManager;
  private final SitePaths sitePaths;

  @Inject
  HookFactory(
      HookQueue queue,
      HookExecutor syncHookExecutor,
      @GerritServerConfig Config config,
      IdentifiedUser.GenericFactory identifiedUserFactory,
      @CanonicalWebUrl @Nullable Provider<String> urlProvider,
      HookMetrics metrics,
      SitePaths sitePaths,
      GitRepositoryManager gitManager) {
    this.queue = queue;
    this.syncHookExecutor = syncHookExecutor;
    this.config = config;
    this.identifiedUserFactory = identifiedUserFactory;
    this.metrics = metrics;
    this.urlProvider = urlProvider;
    this.gitManager = gitManager;
    this.sitePaths = sitePaths;

    String v = config.getString("hooks", null, "path");
    if (v != null) {
      this.hooksPath = Paths.get(v);
    } else {
      this.hooksPath = sitePaths.hooks_dir;
    }
    log.info("hooks.path: {}", this.hooksPath);
  }

  private Path getHookPath(String configName, String defaultName) {
    String v = config.getString("hooks", null, configName);
    Path hookPath = hooksPath.resolve(firstNonNull(v, defaultName));
    log.info("hooks.{} resolved to {}", configName, hookPath);
    return hookPath;
  }

  public AsynchronousHook createAsync(String configName, String defaultName) {
    return new AsynchronousHook(queue, getHookPath(configName, defaultName));
  }

  public SynchronousHook createSync(String configName, String defaultName) {
    return new SynchronousHook(syncHookExecutor, getHookPath(configName, defaultName));
  }

  public HookArgs createArgs() {
    return new HookArgs(identifiedUserFactory, urlProvider, metrics, gitManager, sitePaths);
  }
}
