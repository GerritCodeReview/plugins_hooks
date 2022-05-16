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

import com.google.common.collect.ImmutableListMultimap;
import java.nio.file.Path;
import java.util.Optional;

class SynchronousHook extends Hook {
  private final HookExecutor executor;

  SynchronousHook(HookExecutor executor, Path path) {
    super(path);
    this.executor = executor;
  }

  @Override
  HookResult execute(HookArgs args) {
    return executor.submit(path, args);
  }

  @Override
  HookResult execute(String projectName, HookArgs args) {
    return executor.submit(projectName, path, args);
  }

  @Override
  HookResult execute(
      String projectName, HookArgs args, Optional<ImmutableListMultimap> pushOptions) {
    return executor.submit(projectName, path, args, pushOptions);
  }
}
