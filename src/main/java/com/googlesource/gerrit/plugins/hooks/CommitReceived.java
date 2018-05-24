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
import com.google.gerrit.server.events.CommitReceivedEvent;
import com.google.gerrit.server.git.validators.CommitValidationException;
import com.google.gerrit.server.git.validators.CommitValidationListener;
import com.google.gerrit.server.git.validators.CommitValidationMessage;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collections;
import java.util.List;
import org.eclipse.jgit.lib.ObjectId;

@Singleton
public class CommitReceived implements CommitValidationListener {
  private final Hook hook;
  private final HookFactory hookFactory;

  @Inject
  CommitReceived(HookFactory hookFactory) {
    this.hook = hookFactory.createSync("commitReceivedHook", "commit-received");
    this.hookFactory = hookFactory;
  }

  @Override
  public List<CommitValidationMessage> onCommitReceived(CommitReceivedEvent receiveEvent)
      throws CommitValidationException {
    String refname = receiveEvent.refName;
    String commandRef = receiveEvent.command.getRefName();
    ObjectId old = ObjectId.zeroId();
    if (receiveEvent.commit.getParentCount() > 0) {
      old = receiveEvent.commit.getParent(0);
    }

    HookArgs args = hookFactory.createArgs();
    String projectName = receiveEvent.project.getName();
    args.add("--project", projectName);
    args.add("--refname", refname);
    args.add("--uploader", receiveEvent.user.getNameEmail());
    args.add("--uploader-username", receiveEvent.user.getUserName().orElse(null));
    args.add("--oldrev", old.name());
    args.add("--newrev", receiveEvent.commit.name());
    args.add("--cmdref", commandRef);

    HookResult result = hook.execute(projectName, args);
    if (result != null) {
      String output = result.toString();
      if (result.getExitValue() != 0) {
        throw new CommitValidationException(output);
      }
      if (!output.isEmpty()) {
        return ImmutableList.of(new CommitValidationMessage(output, false));
      }
    }

    return Collections.emptyList();
  }
}
