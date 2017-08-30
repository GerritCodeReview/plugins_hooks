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
import com.google.gerrit.extensions.events.TopicEditedListener;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
class TopicEdited implements TopicEditedListener {
  private final Hook hook;
  private final HookFactory hookFactory;

  @Inject
  TopicEdited(HookFactory hookFactory) {
    this.hook = hookFactory.createAsync("topicChangedHook", "topic-changed");
    this.hookFactory = hookFactory;
  }

  @Override
  public void onTopicEdited(TopicEditedListener.Event event) {
    HookArgs args = hookFactory.createArgs();

    ChangeInfo c = event.getChange();
    args.add("--change", c.id);
    args.add("--change-owner", c.owner);
    args.add("--project", c.project);
    args.add("--branch", c.branch);
    args.add("--changer", event.getWho());
    args.add("--old-topic", event.getOldTopic());
    args.add("--new-topic", event.getChange().topic);

    hook.execute(c.project, args);
  }
}
