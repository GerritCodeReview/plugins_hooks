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

import com.google.common.collect.Lists;
import com.google.gerrit.extensions.annotations.Listen;
import com.google.gerrit.extensions.events.GitReferenceUpdatedListener;
import com.google.inject.Inject;

import java.util.List;

@Listen
class RefUpdated implements GitReferenceUpdatedListener {
  private final Hook hook;

  @Inject
  RefUpdated(HookQueue queue) {
    this.hook = queue.resolve("refUpdatedHook", "ref-updated");
  }

  @Override
  public void onGitReferenceUpdated(Event event) {
    for (Update u : event.getUpdates()) {
      List<String> args = Lists.newArrayList();

      args.add("--project");
      args.add(event.getProjectName());

      args.add("--refname");
      args.add(u.getRefName());

      if (u.getOldObjectId() != null) {
        args.add("--oldrev");
        args.add(u.getOldObjectId());
      }

      if (u.getNewObjectId() != null) {
        args.add("--newrev");
        args.add(u.getNewObjectId());
      }

      args.add("--submitter");
      args.add(u.getSubmitter());

      hook.submit(args);
    }
  }
}
