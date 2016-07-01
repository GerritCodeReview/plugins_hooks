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

import com.google.gerrit.extensions.events.GitReferenceUpdatedListener;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
class GitReferenceUpdated implements GitReferenceUpdatedListener {
  private final AsynchronousHook hook;
  private final HookFactory hookFactory;

  @Inject
  GitReferenceUpdated(HookFactory hookFactory) {
    this.hook = hookFactory.createAsync("refUpdatedHook", "ref-updated");
    this.hookFactory = hookFactory;
  }

  @Override
  public void onGitReferenceUpdated(GitReferenceUpdatedListener.Event event) {
    HookArgs args = hookFactory.createArgs();

    args.add("--oldrev", event.getOldObjectId());
    args.add("--newrev", event.getNewObjectId());
    args.add("--refname", event.getRefName());
    args.add("--project", event.getProjectName());
    args.add("--submitter", event.getUpdater());

    hook.submit(event.getProjectName(), args);
  }
}
