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

import com.google.common.collect.ImmutableList;
import com.google.gerrit.extensions.common.AccountInfo;
import com.google.gerrit.extensions.common.ApprovalInfo;
import com.google.gerrit.extensions.common.ChangeInfo;
import com.google.inject.Provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class HookArgs {
  private final String anonymousCowardName;
  private final Provider<String> urlProvider;
  private final List<String> args;

  HookArgs(String anonymousCowardName, Provider<String> urlProvider) {
    this.anonymousCowardName = anonymousCowardName;
    this.urlProvider = urlProvider;
    this.args = new ArrayList<>();
  }

  public ImmutableList<String> get() {
    return ImmutableList.copyOf(args);
  }

  public void add(String name, String value) {
    args.add(name);
    args.add(value != null ? value : "");
  }

  public void add(String name, int value) {
    args.add(name);
    args.add(String.valueOf(value));
  }

  public void add(String name, Integer value) {
    args.add(name);
    args.add(value.toString());
  }

  public void add(String name, Boolean value) {
    args.add(name);
    args.add(value ? "true" : "false");
  }

  public void add(String name, AccountInfo account) {
    if (account != null) {
      args.add(name);
      args.add(format(account));
    }
  }

  public void addUrl(ChangeInfo change) {
    args.add("--change-url");
    if (change != null && urlProvider.get() != null) {
      StringBuilder r = new StringBuilder();
      r.append(urlProvider.get());
      r.append(change.changeId);
      args.add(r.toString());
    }
    args.add("");
  }

  public void addApprovals(Map<String, ApprovalInfo> approvals,
      Map<String, ApprovalInfo> oldApprovals) {
    for (Map.Entry<String, ApprovalInfo> approval : approvals.entrySet()) {
      if (approval.getValue() != null) {
        args.add("--" + approval.getKey());
        args.add(approval.getValue().value.toString());
        if (oldApprovals != null && !oldApprovals.isEmpty()) {
          ApprovalInfo oldValue = oldApprovals.get(approval.getKey());
          if (oldValue != null && oldValue.value != null) {
            args.add("--" + approval.getKey() + "-oldValue");
            args.add(oldValue.value.toString());
          }
        }
      }
    }
  }

  private String format(AccountInfo account) {
    String who = (account.name == null) ? anonymousCowardName : account.name;
    if (account.email != null) {
      who += " (" + account.email + ")";
    }
    return who;
  }
}
