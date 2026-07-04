load("@com_googlesource_gerrit_bazlets//:gerrit_plugin.bzl", "gerrit_plugin", "gerrit_plugin_tests")

gerrit_plugin(
    name = "hooks",
    srcs = glob(["src/main/java/**/*.java"]),
    flavour = "any",
    manifest_entries = [
        "Gerrit-PluginName: hooks",
        "Gerrit-Module: com.googlesource.gerrit.plugins.hooks.PluginModule",
    ],
    resources = glob(["src/main/resources/**/*"]),
)

gerrit_plugin_tests(
    name = "hooks_tests",
    srcs = glob(["src/test/java/**/*.java"]),
    tags = ["hooks"],
    visibility = ["//visibility:public"],
    runtime_deps = [":hooks__plugin"],
)
