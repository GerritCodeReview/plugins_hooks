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
import com.google.gerrit.extensions.events.HashtagsEditedListener;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import java.util.Collection;

@Singleton
class HashtagsEdited implements HashtagsEditedListener {
  private final AsynchronousHook hook;
  private final HookFactory hookFactory;

  @Inject
  HashtagsEdited(HookFactory hookFactory) {
    this.hook = hookFactory.createAsync("hashtagsChanged", "hashtags-changed");
    this.hookFactory = hookFactory;
  }

  @Override
  public void onHashtagsEdited(HashtagsEditedListener.Event event) {
    HookArgs args = hookFactory.createArgs();

    ChangeInfo c = event.getChange();
    args.add("--change", c.id);
    args.add("--change-owner", c.owner);
    args.add("--project", c.project);
    args.add("--branch", c.branch);
    args.add("--editor", event.getEditor());
    add(args, "--hashtag", event.getHashtags());
    add(args, "--added", event.getAddedHashtags());
    add(args, "--removed", event.getRemovedHashtags());

    hook.submit(c.project, args);
  }

  private void add(HookArgs args, String name, Collection<String> hashtags) {
    for (String hashtag : hashtags) {
      args.add(name, hashtag);
    }
  }
}
