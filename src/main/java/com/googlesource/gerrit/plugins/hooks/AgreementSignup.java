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
import com.google.gerrit.extensions.events.AgreementSignupListener;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
class AgreementSignup implements AgreementSignupListener {
  private final Hook hook;
  private final HookFactory hookFactory;

  @Inject
  AgreementSignup(HookFactory hookFactory) {
    this.hook = hookFactory.createAsync("claSignedHook", "cla-signed");
    this.hookFactory = hookFactory;
  }

  @Override
  public void onAgreementSignup(AgreementSignupListener.Event event) {
    AccountInfo submitter = event.getAccount();
    if (submitter != null) {
      HookArgs args = hookFactory.createArgs();

      args.add("--submitter", submitter);
      args.add("--user-id", submitter._accountId);
      args.add("--cla-name", event.getAgreementName());

      hook.execute(args);
    }
  }
}
