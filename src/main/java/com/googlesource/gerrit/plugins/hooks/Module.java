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

import com.google.gerrit.extensions.config.FactoryModule;
import com.google.gerrit.extensions.events.AgreementSignupListener;
import com.google.gerrit.extensions.events.ChangeAbandonedListener;
import com.google.gerrit.extensions.events.ChangeDeletedListener;
import com.google.gerrit.extensions.events.ChangeMergedListener;
import com.google.gerrit.extensions.events.ChangeRestoredListener;
import com.google.gerrit.extensions.events.CommentAddedListener;
import com.google.gerrit.extensions.events.GitReferenceUpdatedListener;
import com.google.gerrit.extensions.events.HashtagsEditedListener;
import com.google.gerrit.extensions.events.LifecycleListener;
import com.google.gerrit.extensions.events.NewProjectCreatedListener;
import com.google.gerrit.extensions.events.ReviewerAddedListener;
import com.google.gerrit.extensions.events.ReviewerDeletedListener;
import com.google.gerrit.extensions.events.RevisionCreatedListener;
import com.google.gerrit.extensions.events.TopicEditedListener;
import com.google.gerrit.extensions.registration.DynamicSet;
import com.google.gerrit.server.git.validators.CommitValidationListener;
import com.google.gerrit.server.git.validators.MergeValidationListener;
import com.google.gerrit.server.git.validators.RefOperationValidationListener;
import com.google.inject.Scopes;
import com.google.inject.internal.UniqueAnnotations;

class Module extends FactoryModule {
  @Override
  protected void configure() {
    bind(HookQueue.class).in(Scopes.SINGLETON);
    bind(LifecycleListener.class).annotatedWith(UniqueAnnotations.create()).to(HookQueue.class);
    bind(HookExecutor.class).in(Scopes.SINGLETON);
    bind(LifecycleListener.class).annotatedWith(UniqueAnnotations.create()).to(HookExecutor.class);

    factory(HookArgs.Factory.class);

    DynamicSet.bind(binder(), AgreementSignupListener.class).to(AgreementSignup.class);
    DynamicSet.bind(binder(), ChangeAbandonedListener.class).to(ChangeAbandoned.class);
    DynamicSet.bind(binder(), ChangeDeletedListener.class).to(ChangeDeleted.class);
    DynamicSet.bind(binder(), ChangeMergedListener.class).to(ChangeMerged.class);
    DynamicSet.bind(binder(), ChangeRestoredListener.class).to(ChangeRestored.class);
    DynamicSet.bind(binder(), CommentAddedListener.class).to(CommentAdded.class);
    DynamicSet.bind(binder(), CommitValidationListener.class).to(CommitReceived.class);
    DynamicSet.bind(binder(), GitReferenceUpdatedListener.class).to(GitReferenceUpdated.class);
    DynamicSet.bind(binder(), HashtagsEditedListener.class).to(HashtagsEdited.class);
    DynamicSet.bind(binder(), MergeValidationListener.class).to(Submit.class);
    DynamicSet.bind(binder(), NewProjectCreatedListener.class).to(NewProjectCreated.class);
    DynamicSet.bind(binder(), RefOperationValidationListener.class).to(RefUpdate.class);
    DynamicSet.bind(binder(), ReviewerAddedListener.class).to(ReviewerAdded.class);
    DynamicSet.bind(binder(), ReviewerDeletedListener.class).to(ReviewerDeleted.class);
    DynamicSet.bind(binder(), RevisionCreatedListener.class).to(RevisionCreated.class);
    DynamicSet.bind(binder(), TopicEditedListener.class).to(TopicEdited.class);
  }
}
