How To Release
==============

Due to Maven Central's very particular requirements, the release process is a bit
elaborate and requires a good deal of local configuration.  This guide should walk
you through it.  It won't do anyone outside of KeepSafe any good, but the workflow
is representative of just about any project deploying via Sonatype.

We currently deploy to both Maven Central (via Sonatype's OSS Nexus instance) and to
plugins.gradle.org.

## Prerequisites

1. A *published* GPG code-signing key
1. A Sonatype Nexus OSS account with permission to publish in ru.cian
1. A plugins.gradle.org account with permission to publish in ru.cian
1. Permission to push directly to https://github.com/cianru/huawei-publish-gradle-plugin

## Contents page

1. [Setup](docs/releasing/01-setup.md)
2. [Pushing a SNAPSHOT build to local repository](docs/releasing/02-publish-a-snapshot-to-local-repository.md)
3. [Pushing a SNAPSHOT build to Sonatype](docs/releasing/03-publish-a-snapshot-to-sonatype.md)
4. [Pushing a release build to Sonatype](docs/releasing/04-publish-a-release-build-to-sonatype.md)
5. [Pushing a release build to Bintray (DEPRECATED)](docs/releasing/05-publish-a-release-build-to-bintray.md)
6. [Pushing a release build to Gradle Plugin Portal](docs/releasing/06-publish-a-release-build-to-gradle-plugin-portal.md)
7. [Prepare Release Commit](docs/releasing/07-prepare-release-commit.md)
8. [Prepare Next Snapshot Version Commit](docs/releasing/08-prepare-next-snapshot-version-commit.md)
