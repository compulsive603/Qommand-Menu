# Command List (Forge 1.20.1)

Press **K** in a world to open a menu with one button: **Show All Commands**.

## Command browser
- Lists every command the server sent you (vanilla + all mods)
- Shows the **mod** each command belongs to and a **description**
- Hover a row's text for full details
- Search by name, mod or description
- Sort: A-Z / Z-A / Newest / Oldest / By Mod (grouped under headers)
- **Use**: runs the command now, or opens chat with it pre-typed if it needs arguments
  (stop/kill/reload/save-off ask for confirmation first)
- **Copy**: copies `/command` to the clipboard
- **Key**: bind the command (with any arguments you add) to a keyboard key, optionally with Ctrl/Alt/Shift
- **My Keybinds**: list, edit or remove your command keybinds

## Config files (in `config/`)
- `commandlist_binds.json`: your command keybinds
- `commandlist_seen.json`: when each command was first seen (used by "Newest/Oldest")
- `commandlist_info.json`: set the mod name / description for any command yourself

## How mod detection works
Minecraft does not store which mod registered a command, so it is a best guess:
1. `modid:command` namespace
2. Known vanilla / Forge commands
3. Singleplayer only: the package of the code that runs the command
4. Command name matching a mod id
Anything else shows as "Unknown mod". Fix it via `commandlist_info.json`.

## Build
Requires JDK 17.

    ./gradlew build        (Windows: gradlew.bat build)

The jar ends up in `build/libs/commandlist-1.20.1-1.1.0.jar`. Put it in `mods/`.

## GitHub Actions
`.github/workflows/build.yml` builds on every push/PR and uploads the jar as an artifact.
Push a tag like `v1.1.0` to also publish a GitHub Release with the jar attached.

    git tag v1.1.0 && git push origin v1.1.0
