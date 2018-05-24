// Copyright (C) 2017 The Android Open Source Project
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
import com.google.gerrit.common.Nullable;
import com.google.gerrit.server.events.RefReceivedEvent;
import com.google.gerrit.server.git.validators.RefOperationValidationListener;
import com.google.gerrit.server.git.validators.ValidationMessage;
import com.google.gerrit.server.validators.ValidationException;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collections;
import java.util.List;
import org.eclipse.jgit.lib.ObjectId;

@Singleton
public class RefUpdate implements RefOperationValidationListener {
  private final Hook hook;
  private final HookFactory hookFactory;

  @Inject
  RefUpdate(HookFactory hookFactory) {
    this.hook = hookFactory.createSync("refUpdateHook", "ref-update");
    this.hookFactory = hookFactory;
  }

  private ObjectId getObjectId(@Nullable ObjectId object) {
    return object == null ? ObjectId.zeroId() : object;
  }

  @Override
  public List<ValidationMessage> onRefOperation(RefReceivedEvent refEvent)
      throws ValidationException {
    String projectName = refEvent.project.getName();

    HookArgs args = hookFactory.createArgs();
    args.add("--project", projectName);
    args.add("--uploader", refEvent.user.getNameEmail());
    args.add("--uploader-username", refEvent.user.getUserName().orElse(null));
    args.add("--oldrev", getObjectId(refEvent.command.getOldId()).getName());
    args.add("--newrev", getObjectId(refEvent.command.getNewId()).getName());
    args.add("--refname", refEvent.command.getRefName());

    HookResult result = hook.execute(projectName, args);
    if (result != null) {
      String output = result.toString();
      if (result.getExitValue() != 0) {
        throw new ValidationException(output);
      }
      if (!output.isEmpty()) {
        return ImmutableList.of(new ValidationMessage(output, false));
      }
    }

    return Collections.emptyList();
  }
}
