<div align="center">
<h2>Interface Editor</h2>
<h3>RuneScape Interface Editor for Revision 530 Caches</h3>

Prebuild release: https://github.com/szumaster3/Interface-tool/releases

<a href="#features">Features</a> •
<a href="#setup">Setup</a> •
<a href="https://github.com/szumaster3/Interface-tool/wiki">Docs</a> •
<a href="#screenshots">Screenshots</a> •
<a href="#credits">Credits</a>

</div>

> [!NOTE]
>
> This tool requires a valid RuneScape cache to function.
>
> The cache is **not included** with this project and **will not be provided**.
> Users are responsible for obtaining and configuring their own cache.
>
> Historical RuneScape caches are available through the OpenRS2 Archive:
> https://archive.openrs2.org/

***

## Features

- Open interfaces straight from the cache and preview them in the editor.
- Add and edit components: layer, sprite, text, model, line and figure.
- Ready-made templates.
- Tree view and list view of the interface, find, copy & paste, undo & redo.
- Import and export of interfaces.
- Sprite manager, color picker.
- Model library and model viewer.
- Cache backup and update check.

***

## Requirements

- Java 8 or newer
- A revision 530 cache

## Setup

1. Open the **`config.properties`** file.
2. Update the **`cache_path`** to point to your `cache` directory.
    - Use `\\` or `/` as path separators.
    - Make sure the path ends with a trailing slash.
3. Launch the application via Gradle:

**From command line:**

```bash
./gradlew run
```
or on Windows:

```bat
gradlew.bat run
```

**From IDE:**
- Import the project as a Gradle project and run the `run` task.

### Configuration

| Key               | Description                          | Default        |
|-------------------|--------------------------------------|----------------|
| `cache_path`      | Path to the cache directory          | `data/cache/`  |
| `dump_path`       | Directory used for dumps and exports | `dump/`        |
| `model_dump_path` | Directory used for model dumps       | `dump/models/` |
| `sprite_path`     | Optional directory with sprites      | empty          |

## Building

Build a standalone jar (written to `build/libs`):

```bash
./gradlew shadowJar
```

Package a distributable zip (written to `build/distro`):

```bash
./gradlew packageZip
```

## Project structure

| Path                              | Content                                              |
|-----------------------------------|------------------------------------------------------|
| `src/com/editor`                  | Editor GUI, interface drawing, toolbar and utilities |
| `src/com/editor/util/model`       | Model viewer                                         |
| `src/com/editor/util/sprites`     | Sprite manager                                       |
| `src/com/alex/loaders/interfaces` | Interface component definitions (decode / encode)    |
| `src/com/alex/loaders/mesh`       | Model decoder, loader and renderer                   |
| `src/com/alex/store`              | Cache store                                          |

## Screenshots

<details>
<summary>View Screenshots</summary>

<br>

<p align="center">
    <img src="https://imgur.com/Anc7REA.png" alt="Interface Editor Screenshot"/>
</p>

<p align="center">
    <img src="https://imgur.com/EkNXBws.png" alt="Interface Editor Screenshot"/>
</p>

<p align="center">
    <img src="https://imgur.com/S9UVpYz.png" alt="Interface Editor Screenshot"/>
</p>

<p align="center">
    <img src="https://imgur.com/oA9qx9O.png" alt="Interface Editor Screenshot"/>
</p>

<p align="center">
    <img src="https://imgur.com/FekpayZ.png" alt="Interface Editor Screenshot"/>
</p>

<p align="center">
    <img src="https://imgur.com/Au59175.png" alt="Interface Editor Screenshot"/>
</p>

</details>

***

## Credits

- Original Interface Tool source by PaoloKa:
    - https://github.com/PaoloKa/Interface-tool

- Model decoding based on Simple-JFX-Model-Editor.
    - https://github.com/Suicolen/Simple-JFX-Model-Editor

- Icons used in this project are provided by LuckyIcon Art and are used in accordance with their license:
    - https://www.luckyicon.com