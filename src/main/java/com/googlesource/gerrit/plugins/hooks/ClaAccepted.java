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
import com.google.gerrit.extensions.events.PostContributorAgreementAcceptedListener;
import com.google.gerrit.server.config.AnonymousCowardName;
import com.google.gerrit.server.config.GerritServerConfig;
import com.google.inject.Inject;

import org.eclipse.jgit.lib.Config;

import java.util.List;

@Listen
class ClaAccepted implements PostContributorAgreementAcceptedListener {
  private final Hook hook;
  private final String anonymousCowardName;

  @Inject
  ClaAccepted(HookQueue queue, @GerritServerConfig Config c) {
    this.hook = queue.resolve("claSignedHook", "cla-signed");
    anonymousCowardName = getAnonymousCowardName(c);
  }

  private String getAnonymousCowardName(Config c) {
    String anonymousCowardName = c.getString("user", null, "anonymousCoward");
    if (anonymousCowardName == null) {
      anonymousCowardName = "Anonymous Coward";
    }

    return anonymousCowardName;
  }

  @Override
  public void onPostContributorAgreementAccepted(Event event) {
    List<String> args = Lists.newArrayList();

    args.add("--cla-name");
    args.add(event.getAgreementName());

    args.add("--user-id");
    args.add(Integer.toString(event.getAccountId()));

    args.add("--submitter");
    args.add(format(event.getAccountFullName(), event.getAccountEmail()));

    hook.submit(args);
  }

  private String format(String name, String email) {
    String who = (name == null) ? anonymousCowardName : name;
    if (email != null) {
      who += " (" + email + ")";
    }
    return who;
  }
}
