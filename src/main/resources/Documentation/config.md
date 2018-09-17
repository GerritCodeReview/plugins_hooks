Configuration
=============

Environment
-----------

When a project is given to a hook, the environment will have `GIT_DIR` set to
the full path of the affected git repository so that git commands can be easily
run.

Make sure your hook scripts are executable if running on *nix.

With the exception of the `commit-received` and `ref-update` hooks, hooks are
run in the background after the relevant change has taken place so are unable
to affect the outcome of any given change. Because of the fact the hooks are
run in the background after the activity, a hook might not be notified about
an event if the server is shutdown before the hook can be invoked.

Configuration
-------------

The plugin is configured in a `[hooks]` section in the `gerrit.config` file
in the site's `etc` folder.

The configuration values are evaluated when the plugin is loaded. If the values
are changed, the plugin must be reloaded for them to take effect.

A hook may be temporarily disabled by either removing it or renaming it. Its
behavior may be changed by replacing its content. Such changes will take
immediate effect without having to reload the plugin.

hooks.path
:	Location of hook executables. If not set, defaults to `$site_path/hooks`

hooks.syncHookTimeout
:	Timeout value in seconds for synchronous hooks. If not set, defaults
to 30 seconds.

hooks.changeAbandonedHook
:	Filename for the change abandoned hook. If not set, defaults to `change-abandoned`.

hooks.changeDeletedHook
:	Filename for the change deleted hook. If not set, defaults to `change-deleted`.

hooks.changeMergedHook
:	Filename for the change merged hook. If not set, defaults to `change-merged`.

hooks.changeRestoredHook
:	Filename for the change restored hook. If not set, defaults to `change-restored`.

hooks.claSignedHook
:	Filename for the CLA signed hook. If not set, defaults to `cla-signed`.

hooks.commentAddedHook
:	Filename for the comment added hook. If not set, defaults to `comment-added`.

hooks.commitReceivedHook
:	Filename for the commit received hook. If not set, defaults to `commit-received`.

hooks.draftPublishedHook
:	Filename for the draft published hook. If not set, defaults to `draft-published`.

hooks.hashtagsChangedHook
:	Filename for the hashtags changed hook. If not set, defaults to `hashtags-changed`.

hooks.patchsetCreatedHook
:	Filename for the patchset created hook. If not set, defaults to `patchset-created`.

hooks.projectCreatedHook
:	Filename for the project created hook. If not set, defaults to `project-created`.

hooks.refUpdateHook
:	Filename for the ref update hook. If not set, defaults to `ref-update`.

hooks.refUpdatedHook
:	Filename for the ref updated hook. If not set, defaults to `ref-updated`.

hooks.reviewerAddedHook
:	Filename for the reviewer added hook. If not set, defaults to `reviewer-added`.

hooks.reviewerDeletedHook
:	Filename for the reviewer update hook. If not set, defaults to `reviewer-deleted`.

hooks.submitHook
:	Filename for the submit hook. If not set, defaults to `submit`.

hooks.topicChangedHook
:	Filename for the topic changed hook. If not set, defaults to `topic-changed`.


Missing Change URLs
-------------------

If [gerrit.canonicalWebUrl][1] is not set in `gerrit.config` the
`--change-url` flag may not be passed to all hooks.  Hooks started out
of an SSH context (for example the `patchset-created` hook) don't know
the server's web URL, unless this variable is configured.


Debugging Hooks
---------------

If execution of a hook failed (i.e. it returned a non-zero exit code) the
exit code is logged at error level. Likewise, if a hook timed out or was
cancelled, this is logged at error level.

Any output (including both stdout and stderr) from the hook is logged at
debug level.

To make debug logs visible in Gerrit's log file, debug logging must be
enabled for the `com.googlesource.gerrit.plugins.hooks` package. This can be
done by setting the log level at runtime with the ssh command:

```
  ssh -p 29418 user@gerrit gerrit logging set-level DEBUG com.googlesource.gerrit.plugins.hooks
```

Note that setting the log level at runtime only works for loggers that
have already been created. Loggers that get created after the level was
set will still be created with the default level.

To set the level for all loggers, it is necessary to do it by editing the
`log4j.properties` file. This requires the Gerrit server to be restarted.

[1]: ../../../Documentation/config-gerrit.html#gerrit.canonicalWebUrl
