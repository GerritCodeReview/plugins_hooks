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
import com.google.gerrit.server.IdentifiedUser;
import com.google.gerrit.server.events.CommitReceivedEvent;
import com.google.gerrit.server.git.validators.CommitValidationException;
import com.google.gerrit.server.git.validators.CommitValidationListener;
import com.google.gerrit.server.git.validators.CommitValidationMessage;
import com.google.inject.Inject;

import org.eclipse.jgit.lib.ObjectId;

import java.util.Collections;
import java.util.List;

public class CommitReceived implements CommitValidationListener {
  private final SynchronousHook hook;
  private final HookFactory hookFactory;

  @Inject
  CommitReceived(HookFactory hookFactory) {
    this.hook = hookFactory.createSync("refUpdateHook", "ref-update");
    this.hookFactory = hookFactory;
  }

  @Override
  public List<CommitValidationMessage> onCommitReceived(
      CommitReceivedEvent receiveEvent) throws CommitValidationException {
    IdentifiedUser user = receiveEvent.user;
    String refname = receiveEvent.refName;
    ObjectId old = ObjectId.zeroId();
    if (receiveEvent.commit.getParentCount() > 0) {
      old = receiveEvent.commit.getParent(0);
    }

    if (receiveEvent.command.getRefName().startsWith("refs/for/")) {
       // In case the ref-update hook tries to distinguish behavior between pushes
       // to refs/heads/... and refs/for/..., we need to make sure we send it the
       // correct refname. Also, if this is targetting refs/for/, make sure we behave
       // the same as a push to refs/for/, by setting the old revision to
       // 0000000000000000000000000000000000000000.
      refname = receiveEvent.command.getRefName();
      old = ObjectId.zeroId();
    }

    HookArgs args = hookFactory.createArgs();
    String projectName = receiveEvent.project.getName();
    args.add("--project", projectName);
    args.add("--refname", refname);
    args.add("--uploader", user.getNameEmail());
    args.add("--oldrev", old.name());
    args.add("--newrev", receiveEvent.commit.name());

    HookResult result = hook.run(projectName, args);
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
