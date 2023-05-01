## Prepare Release Commit

1. Edit ./plugin/gradle.properties, remove '-SNAPSHOT' from the VERSION property
2. Make a *signed* commit:
   ```bash
   git commit -m "Release vX.Y.Z"
   ```
3. Make a *signed* tag (check existing tags for message format):
   ```bash
   git tag -a "vX.Y.Z" -m "vX.Y.Z" 
   ```
4. Push all of our work to Github to make it official:
   ```bash
   git push --tags origin master
