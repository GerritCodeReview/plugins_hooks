Configuration
=============

Environment
-----------

When a project is given to a hook, the environment will have `GIT_DIR` set to
the full path of the affected git repository so that git commands can be easily
run.

Make sure your hook scripts are executable if running on *nix.

With the exception of the `commit-receive` and `ref-operate` hooks, hooks are
run in the background after the relevant change has taken place so are unable
to affect the outcome of any given change. Because of the fact the hooks are
run in the background after the activity, a hook might not be notified about an
event if the server is shutdown before the hook can be invoked.

Configuration
-------------

It is possible to change where this plugin looks for hooks, and what
filenames it looks for by adding a `[hooks]` section to `gerrit.config`.

hooks.path
:	Location of hook executables. If not set, defaults to `$site_path/hooks`

hooks.syncHookTimeout
:	Timeout value in seconds for synchronous hooks. If not set, defaults
to 30 seconds.

hooks.changeAbandonedHook
:	Filename for the change abandoned hook. If not set, defaults to `change-abandoned`.

hooks.changeMergedHook
:	Filename for the change merged hook. If not set, defaults to `change-merged`.

hooks.changeRestoredHook
:	Filename for the change restored hook. If not set, defaults to `change-restored`.

hooks.claSignedHook
:	Filename for the CLA signed hook. If not set, defaults to `cla-signed`.

hooks.commentAddedHook
:	Filename for the comment added hook. If not set, defaults to `comment-added`.

hooks.commitReceiveHook
:	Filename for the commit receive hook. If not set, defaults to `commit-receive`.

hooks.draftPublishedHook
:	Filename for the draft published hook. If not set, defaults to `draft-published`.

hooks.hashtagsChangedHook
:	Filename for the hashtags changed hook. If not set, defaults to `hashtags-changed`.

hooks.patchsetCreatedHook
:	Filename for the patchset created hook. If not set, defaults to `patchset-created`.

hooks.projectCreatedHook
:	Filename for the project created hook. If not set, defaults to `project-created`.

hooks.refOperateHook
:	Filename for the ref operate hook. If not set, defaults to `ref-operate`.

hooks.refUpdatedHook
:	Filename for the ref updated hook. If not set, defaults to `ref-updated`.

hooks.reviewerAddedHook
:	Filename for the reviewer added hook. If not set, defaults to `reviewer-added`.

hooks.reviewerDeletedHook
:	Filename for the reviewer update hook. If not set, defaults to `reviewer-deleted`.

hooks.topicChangedHook
:	Filename for the topic changed hook. If not set, defaults to `topic-changed`.


Missing Change URLs
-------------------

If [gerrit.canonicalWebUrl][1] is not set in `gerrit.config` the
`--change-url` flag may not be passed to all hooks.  Hooks started out
of an SSH context (for example the patchset-created hook) don't know
the server's web URL, unless this variable is configured.

[1]: ../../../Documentation/config-gerrit.html#gerrit.canonicalWebUrl
