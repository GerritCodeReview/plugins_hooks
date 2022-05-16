// Copyright (C) 2018 The Android Open Source Project
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

import com.google.gerrit.entities.BranchNameKey;
import com.google.gerrit.entities.PatchSet;
import com.google.gerrit.server.IdentifiedUser;
import com.google.gerrit.server.git.CodeReviewCommit;
import com.google.gerrit.server.git.CodeReviewCommit.CodeReviewRevWalk;
import com.google.gerrit.server.git.validators.MergeValidationException;
import com.google.gerrit.server.git.validators.MergeValidationListener;
import com.google.gerrit.server.project.ProjectState;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.eclipse.jgit.lib.Repository;
import java.util.Optional;

@Singleton
public class Submit implements MergeValidationListener {
  private final Hook hook;
  private final HookFactory hookFactory;

  @Inject
  Submit(HookFactory hookFactory) {
    this.hook = hookFactory.createSync("submitHook", "submit");
    this.hookFactory = hookFactory;
  }

  @Override
  public void onPreMerge(
      Repository repo,
      CodeReviewRevWalk revWalk,
      CodeReviewCommit commit,
      ProjectState destProject,
      BranchNameKey destBranch,
      PatchSet.Id patchSetId,
      IdentifiedUser caller)
      throws MergeValidationException {
    String projectName = destProject.getProject().getName();

    HookArgs args = hookFactory.createArgs();
    args.add("--change", patchSetId.changeId().get());
    args.add("--project", projectName);
    args.add("--branch", destBranch.branch());
    args.add("--submitter", caller.getNameEmail());
    args.add("--submitter-username", caller.getUserName().orElse(null));
    args.add("--patchset", patchSetId.get());
    args.add("--commit", commit.getId().name());

    HookResult result = hook.execute(projectName, args, Optional.empty());
    if (result != null && result.getExitValue() != 0) {
      throw new MergeValidationException(result.toString());
    }
  }
}
