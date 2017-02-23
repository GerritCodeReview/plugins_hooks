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

import com.google.gerrit.extensions.common.ChangeInfo;
import com.google.gerrit.extensions.events.RevisionCreatedListener;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class RevisionCreated implements RevisionCreatedListener {
  private final Hook hook;
  private final HookFactory hookFactory;

  @Inject
  RevisionCreated(HookFactory hookFactory) {
    this.hook = hookFactory.createAsync("patchsetCreatedHook", "patchset-created");
    this.hookFactory = hookFactory;
  }

  @Override
  public void onRevisionCreated(Event event) {
    HookArgs args = hookFactory.createArgs();

    ChangeInfo c = event.getChange();
    args.add("--change", c.id);
    args.add("--kind", String.valueOf(event.getRevision().kind));
    args.addUrl(c);
    args.add("--change-owner", c.owner);
    args.add("--project", c.project);
    args.add("--branch", c.branch);
    args.add("--topic", c.topic);
    args.add("--uploader", event.getWho());
    args.add("--commit", event.getRevision().commit.commit);
    args.add("--patchset", event.getRevision()._number);

    hook.execute(c.project, args);
  }
}
