load("//tools/bzl:junit.bzl", "junit_tests")
load("//tools/bzl:plugin.bzl", "PLUGIN_DEPS", "PLUGIN_TEST_DEPS", "gerrit_plugin")

gerrit_plugin(
    name = "hooks",
    srcs = glob(["src/main/java/**/*.java"]),
    manifest_entries = [
        "Gerrit-PluginName: hooks",
        "Gerrit-Module: com.googlesource.gerrit.plugins.hooks.Module",
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
