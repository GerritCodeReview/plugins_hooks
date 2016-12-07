load("//tools/bzl:plugin.bzl", "gerrit_plugin")

gerrit_plugin(
    name = "hooks",
    srcs = glob(["src/main/java/**/*.java"]),
    manifest_entries = [
        "Gerrit-PluginName: hooks",
        "Gerrit-Module: com.googlesource.gerrit.plugins.hooks.Module",
    ],
    resources = glob(["src/main/resources/**/*"]),
)
