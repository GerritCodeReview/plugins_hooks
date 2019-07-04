// Copyright (C) 2019 The Android Open Source Project
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

import static com.google.common.truth.Truth.assertThat;

import com.google.gerrit.acceptance.LightweightPluginDaemonTest;
import com.google.gerrit.acceptance.NoHttpd;
import com.google.gerrit.acceptance.TestPlugin;
import com.google.gerrit.server.config.SitePaths;
import com.google.inject.Inject;
import org.junit.Test;

@NoHttpd
@TestPlugin(name = "hooks", sysModule = "com.googlesource.gerrit.plugins.hooks.Module")
public class HooksIT extends LightweightPluginDaemonTest {
  @Inject HookFactory factory;
  @Inject SitePaths sitePaths;

  @Test
  public void defaultHooksPathWithDefaultHookName() throws Exception {
    Hook hook = factory.createAsync("test-hook", "test-hook");
    assertThat(hook.path.toString()).isEqualTo(sitePaths.hooks_dir.resolve("test-hook"));
  }
}
