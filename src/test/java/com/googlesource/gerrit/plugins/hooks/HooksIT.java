// Copyright (C) 2019 The Android Open Source Project
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

import com.google.gerrit.acceptance.LightweightPluginDaemonTest;
import com.google.gerrit.acceptance.NoHttpd;
import com.google.gerrit.acceptance.TestPlugin;
import com.google.gerrit.entities.Project;
import com.google.gerrit.extensions.events.GitReferencesUpdatedListener;
import com.google.gerrit.server.extensions.events.GitReferenceUpdated;
import java.util.Set;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.transport.ReceiveCommand;
import org.junit.Test;
import org.mockito.Mockito;

@NoHttpd
@TestPlugin(name = "hooks", sysModule = "com.googlesource.gerrit.plugins.hooks.PluginModule")
public class HooksIT extends LightweightPluginDaemonTest {

  @Test
  public void hookIsTriggeredOnEachRefInBatchRefUpdate() {
    Hook hook = Mockito.mock(Hook.class);
    HookArgs hookArgs = Mockito.mock(HookArgs.class);
    HookFactory hookFactory = Mockito.mock(HookFactory.class);

    Mockito.when(hookFactory.createAsync("refUpdatedHook", "ref-updated")).thenReturn(hook);
    Mockito.when(hookFactory.createArgs()).thenReturn(hookArgs);
    Mockito.when(hook.execute("project", hookArgs)).thenReturn(null);

    Set<GitReferencesUpdatedListener.UpdatedRef> updatedRefs =
        Set.of(
            new GitReferenceUpdated.UpdatedRef(
                "refs/changes/01/1/1",
                ObjectId.zeroId(),
                ObjectId.fromString("0000000000000000000000000000000000000001"),
                ReceiveCommand.Type.CREATE),
            new GitReferenceUpdated.UpdatedRef(
                "refs/changes/01/1/meta",
                ObjectId.zeroId(),
                ObjectId.fromString("0000000000000000000000000000000000000001"),
                ReceiveCommand.Type.CREATE));
    GitReferencesUpdatedListener.Event event =
        new GitReferenceUpdated.Event(Project.NameKey.parse("project"), updatedRefs, null);
    new GitReferencesUpdated(hookFactory).onGitReferencesUpdated(event);
    Mockito.verify(hook, Mockito.times(2)).execute("project", hookArgs);
  }
}
