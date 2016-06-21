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

import com.google.gerrit.extensions.events.LifecycleListener;
import com.google.gerrit.server.config.SitePaths;
import com.google.gerrit.server.git.GitRepositoryManager;
import com.google.gerrit.server.git.WorkQueue;
import com.google.inject.Inject;

import java.nio.file.Path;

class HookQueue implements LifecycleListener {
  private final GitRepositoryManager gitManager;
  private final WorkQueue workQueue;
  private final SitePaths sitePaths;

  private WorkQueue.Executor queue;

  @Inject
  HookQueue(GitRepositoryManager m,
      WorkQueue q,
      SitePaths s) {
    gitManager = m;
    workQueue = q;
    sitePaths = s;
  }

  void submit(Path hook, HookArgs args) {
    submit(null, hook, args);
  }

  void submit(String projectName, Path hook, HookArgs args) {
    if (hook.toFile().exists()) {
      queue.submit(new HookTask.Async(
          gitManager,
          sitePaths.site_path,
          projectName,
          hook,
          args));
    }
  }

  @Override
  public void start() {
    queue = workQueue.createQueue(1, "HookQueue");
  }

  @Override
  public void stop() {
    if (queue != null) {
      queue.unregisterWorkQueue();
      queue.shutdownNow();
      queue = null;
    }
  }
}
