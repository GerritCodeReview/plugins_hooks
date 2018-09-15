# Supported Hooks

[TOC]

## Synchronous Hooks

These hooks are invoked synchronously so it is recommended that they not block.

A default timeout on the hook is set to 30 seconds to avoid "runaway" hooks using
up server threads.  The timeout can be changed by setting [`hooks.syncHookTimeout`][1].

### ref-update

This is called when a ref update request (direct push, non-fastforward update, or
ref deletion) is received by Gerrit. It allows a request to be rejected before it
is committed to the Gerrit repository.

If the hook exits with non-zero return code the update will be rejected. Any output
from the hook will be returned to the user, regardless of the return code.

```
  ref-update --project <project name> --refname <refname> --uploader <uploader> --uploader-username <username> --oldrev <sha1> --newrev <sha1>
```

### commit-received

This is called when a commit is received for review by Gerrit. It allows a push to
be rejected before the review is created.

If the hook exits with non-zero return code the push will be rejected. Any output
from the hook will be returned to the user, regardless of the return code.

```
  commit-received --project <project name> --refname <refname> --uploader <uploader> --uploader-username <username> --oldrev <sha1> --newrev <sha1> --cmdref <refname>
```

### submit

This is called when a user attempts to submit a change. It allows the submit to
be rejected.

If the hook exits with non-zero return code the submit will be rejected and the
ouput from the hook will be returned to the user.

```
  submit --project <project name> --branch <branch> --submitter <submitter> --patchset <patchset id> --commit <sha1>
```

## Asynchronous Hooks

These hooks are invoked asynchronously on a background thread.

### patchset-created

Called whenever a patchset is created (this includes new changes).

```
  patchset-created --change <change id> --kind <change kind> --change-url <change url> --change-owner <change owner> --change-owner-username <username> --project <project name> --branch <branch> --topic <topic> --uploader <uploader> --uploader-username <username> --commit <sha1> --patchset <patchset id>
```

The `--kind` parameter represents the kind of change uploaded. See documentation
of [`patchSet`][2] for details.

### comment-added

Called whenever a comment is added to a change.

```
  comment-added --change <change id> --change-url <change url> --change-owner <change owner> --change-owner-username <username> --project <project name> --branch <branch> --topic <topic> --author <comment author> --author-username <username> --commit <commit> --comment <comment> [--<approval category id> <score> --<approval category id> <score> --<approval category id>-oldValue <score> ...]
```

### change-merged

Called whenever a change has been merged.

```
  change-merged --change <change id> --change-url <change url> --change-owner <change owner> --change-owner-username <username> --project <project name> --branch <branch> --topic <topic> --submitter <submitter> --submitter-username <username> --commit <sha1> --newrev <sha1>
```

### change-abandoned

Called whenever a change has been abandoned.

```
  change-abandoned --change <change id> --change-url <change url> --change-owner <change owner> --change-owner-username <username> --project <project name> --branch <branch> --topic <topic> --abandoner <abandoner> --abandoner-username <username> --commit <sha1> --reason <reason>
```

### change-deleted

Called whenever a change has been deleted.

```
  change-deleted --change <change id> --change-url <change url> --change-owner <change owner> --project <project name> --branch <branch> --topic <topic> --deleter <deleter>
```

### change-restored

Called whenever a change has been restored.

```
  change-restored --change <change id> --change-url <change url> --change-owner <change owner> --change-owner-username <username> --project <project name> --branch <branch> --topic <topic> --restorer <restorer> --restorer-username <username> --commit <sha1> --reason <reason>
```

### ref-updated

Called whenever a ref has been updated.

```
  ref-updated --oldrev <old rev> --newrev <new rev> --refname <ref name> --project <project name> --submitter <submitter> --submitter-username <username>
```

### project-created

Called whenever a project has been created.

```
  project-created --project <project name> --head <head name>
```

### reviewer-added

Called whenever a reviewer is added to a change.

```
  reviewer-added --change <change id> --change-url <change url> --change-owner <change owner> --change-owner-username <username> --project <project name> --branch <branch> --reviewer <reviewer> --reviewer-username <username>
```

### reviewer-deleted

Called whenever a reviewer (with a vote) is removed from a change.

```
  reviewer-deleted --change <change id> --change-url <change url> --change-owner <change owner> --change-owner-username <username> --project <project name> --branch <branch> --reviewer <reviewer> [--<approval category id> <score> --<approval category id> <score> ...]
```

### topic-changed

Called whenever a change's topic is changed from the Web UI or via the REST API.

```
  topic-changed --change <change id> --change-owner <change owner> --change-owner-username <username> --project <project name> --branch <branch> --changer <changer> --changer-username <username> --old-topic <old topic> --new-topic <new topic>
```

### hashtags-changed

Called whenever hashtags are added to or removed from a change from the Web UI
or via the REST API.

```
  hashtags-changed --change <change id>  --change-owner <change owner> --change-owner-username <username> --project <project name> --branch <branch> --editor <editor> --editor-username <username> --added <hashtag> --removed <hashtag> --hashtag <hashtag>
```

The `--added` parameter may be passed multiple times, once for each
hashtag that was added to the change.

The `--removed` parameter may be passed multiple times, once for each
hashtag that was removed from the change.

The `--hashtag` parameter may be passed multiple times, once for each
hashtag remaining on the change after the add or remove operation has
been performed.

### cla-signed

Called whenever a user signs a contributor license agreement.

```
  cla-signed --submitter <submitter> --user-id <user_id> --cla-id <cla_id>
```

[1]: config.md#hooks.syncHookTimeout
[2]: ../../../Documentation/json.html#patchSet
