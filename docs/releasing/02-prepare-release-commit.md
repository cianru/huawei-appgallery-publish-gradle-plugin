## Prepare Release Commit

1. Edit the `gradle.properties` file:
   Remove `-alpha<number>` from the `VERSION_NAME` and set the version to the release version. For example: `1.0.0`.
1. Make a *signed* commit:
   ```bash
   git commit -m "Release X.Y.Z"
   ```
1. Make a *signed* tag (check existing tags for message format):
   ```bash
   git tag -a "X.Y.Z" -m "X.Y.Z" 
   ```
1. Push all of our work to Github to make it official:
   ```bash
   git push --tags origin master
