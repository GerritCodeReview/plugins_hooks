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

import com.google.gerrit.extensions.common.AccountInfo;
import com.google.gerrit.extensions.common.ChangeInfo;
import com.google.gerrit.extensions.events.ReviewerAddedListener;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
class ReviewerAdded implements ReviewerAddedListener {
  private final AsynchronousHook hook;
  private final HookFactory hookFactory;

  @Inject
  ReviewerAdded(HookFactory hookFactory) {
    this.hook = hookFactory.createAsync("reviewerAdded", "reviewer-added");
    this.hookFactory = hookFactory;
  }

  @Override
  public void onReviewersAdded(ReviewerAddedListener.Event event) {
    ChangeInfo c = event.getChange();
    for (AccountInfo reviewer : event.getReviewers()) {
      HookArgs args = hookFactory.createArgs();

      args.add("--change", c.id);
      args.addUrl(c);
      args.add("--change-owner", c.owner);
      args.add("--project", c.project);
      args.add("--branch", c.branch);
      args.add("--reviewer", reviewer);

      hook.submit(c.project, args);
    }
  }
}
