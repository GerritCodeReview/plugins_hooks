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

import com.google.gerrit.extensions.events.GitReferencesUpdatedListener;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
class GitReferencesUpdated implements GitReferencesUpdatedListener {
  private final Hook hook;
  private final HookFactory hookFactory;

  @Inject
  GitReferencesUpdated(HookFactory hookFactory) {
    this.hook = hookFactory.createAsync("refUpdatedHook", "ref-updated");
    this.hookFactory = hookFactory;
  }

  @Override
  public void onGitReferencesUpdated(GitReferencesUpdatedListener.Event event) {
    for (UpdatedRef updatedRef : event.getUpdatedRefs()) {
      HookArgs args = hookFactory.createArgs();

      args.add("--oldrev", updatedRef.getOldObjectId());
      args.add("--newrev", updatedRef.getNewObjectId());
      args.add("--refname", updatedRef.getRefName());
      args.add("--project", event.getProjectName());
      args.add("--submitter", event.getUpdater());

      hook.execute(event.getProjectName(), args);
    }
  }
}
