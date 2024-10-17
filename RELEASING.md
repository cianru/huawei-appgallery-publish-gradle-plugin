How To Release
==============

Due to Maven Central's very particular requirements, the release process is a bit
elaborate and requires a good deal of local configuration.  This guide should walk
you through it.  It won't do anyone outside of KeepSafe any good, but the workflow
is representative of just about any project deploying via Sonatype.

We currently deploy to plugins.gradle.org.

## Prerequisites

1. A *published* GPG code-signing key
1. A plugins.gradle.org account with permission to publish in ru.cian
1. Permission to push directly to https://github.com/cianru/huawei-publish-gradle-plugin

## Contents page

1. [Setup](docs/releasing/01-setup.md)
1. [Prepare Release Commit](docs/releasing/02-prepare-release-commit.md)
1. [Pushing a build to local repository](docs/releasing/03-publish-to-local-repository)
1. [Pushing a release build to Gradle Plugin Portal](docs/releasing/07-publish-a-release-build-to-gradle-plugin-portal.md)
1. [Prepare Next Alpha Version Commit](docs/releasing/08-prepare-alpha-version-commit)
