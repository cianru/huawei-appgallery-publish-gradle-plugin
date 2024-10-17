## Prepare Next Alpha Version Commit

1. Create new `snapshot-<version>` Git branch
1. Open the plugin directory:
    ```
    cd ./plugin
    ```
1. Edit the `gradle.properties` file to set new `VERSION_NAME`+ `-alpha<number>` version. For example: `1.0.0-alpha01`.
1. Make a *signed* commit:
   ```bash
   git commit -m "Prepare next development version"
   ```
