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

import com.google.gerrit.common.Nullable;
import com.google.gerrit.server.config.AnonymousCowardName;
import com.google.gerrit.server.config.CanonicalWebUrl;
import com.google.gerrit.server.config.GerritServerConfig;
import com.google.gerrit.server.config.SitePaths;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import org.eclipse.jgit.lib.Config;

import java.nio.file.Path;
import java.nio.file.Paths;

@Singleton
public class HookFactory {
  private final HookQueue queue;
  private final Config config;
  private final String anonymousCowardName;
  private final Provider<String> urlProvider;
  private final Path hooksPath;

  @Inject
  HookFactory(HookQueue queue,
      @GerritServerConfig Config config,
      @AnonymousCowardName String anonymousCowardName,
      @CanonicalWebUrl @Nullable Provider<String> urlProvider,
      SitePaths sitePaths) {
    this.queue = queue;
    this.config = config;
    this.anonymousCowardName = anonymousCowardName;
    this.urlProvider = urlProvider;

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

  public HookArgs create() {
    return new HookArgs(anonymousCowardName, urlProvider);
  }
}
