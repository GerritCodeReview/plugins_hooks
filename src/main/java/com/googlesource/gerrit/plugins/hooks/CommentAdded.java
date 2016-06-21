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
import com.google.gerrit.extensions.events.CommentAddedListener;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
class CommentAdded implements CommentAddedListener {
  private final Hook hook;
  private final HookArgs.Factory hookArgsFactory;

  @Inject
  CommentAdded(Hook.Factory hookFactory,
      HookArgs.Factory hookArgsFactory) {
    this.hook = hookFactory.create("commentAddedHook", "comment-added");
    this.hookArgsFactory = hookArgsFactory;
  }

  @Override
  public void onCommentAdded(CommentAddedListener.Event event) {
    HookArgs args = hookArgsFactory.create();

    ChangeInfo c = event.getChange();
    args.add("--change", c.id);
    args.add("--is-draft", event.getRevision().draft);
    args.addUrl(c);
    args.add("--change-owner", c.owner);
    args.add("--project", c.project);
    args.add("--branch", c.branch);
    args.add("--topic", c.topic);
    args.add("--author", event.getAuthor());
    args.add("--commit", event.getRevision().ref);
    args.add("--comment", event.getComment());
    args.addApprovals(event.getApprovals(), event.getOldApprovals());

    hook.submit(c.project, args);
  }
}
