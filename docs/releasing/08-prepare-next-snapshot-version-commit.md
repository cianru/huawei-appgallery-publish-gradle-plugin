## Prepare Next Snapshot Version Commit

1. Create new `snapshot-<version>` Git branch
2. Open the plugin directory:
    ```
    cd ./plugin
    ```
3. Edit the `gradle.properties` file to set new `VERSION_NAME` version.
4. Edit the `gradle.properties` file:
```bash
IS_SNAPSHOT=true
```
5. Make a *signed* commit:
   ```bash
   git commit -m "Prepare next development version"
   ```
