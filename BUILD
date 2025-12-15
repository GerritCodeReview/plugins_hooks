load("@gerrit_api_version//:version.bzl", "GERRIT_API_VERSION")
load("@com_googlesource_gerrit_bazlets//tools:junit.bzl", "junit_tests")
load("@com_googlesource_gerrit_bazlets//:gerrit_plugin.bzl", "PLUGIN_DEPS", "PLUGIN_TEST_DEPS", "gerrit_plugin")

gerrit_plugin(
    name = "hooks",
    srcs = glob(["src/main/java/**/*.java"]),
    gerrit_api_version = GERRIT_API_VERSION,
    manifest_entries = [
        "Gerrit-PluginName: hooks",
        "Gerrit-Module: com.googlesource.gerrit.plugins.hooks.PluginModule",
    ],
    resources = glob(["src/main/resources/**/*"]),
)

junit_tests(
    name = "hooks_tests",
    srcs = glob(["src/test/java/**/*.java"]),
    tags = ["hooks"],
    visibility = ["//visibility:public"],
    runtime_deps = [":hooks__plugin"],
    deps = PLUGIN_TEST_DEPS + PLUGIN_DEPS,
)
