# Hooks plugin

Gerrit does not support the [standard server-side git hooks][1] in the
repositories it manages.

This plugin adds support for custom hooks that can be run instead. Refer
to the [configuration documentation][2] and [list of supported hooks][3]
for details.

[1]: https://git-scm.com/book/gr/v2/Customizing-Git-Git-Hooks
[2]: src/main/resources/Documentation/config.md
[3]: src/main/resources/Documentation/hooks.md
