 <div align="center">
<h2>Interface Editor</h2>
<h3>RuneScape Interface Editor for Revision 530 Caches</h3>

Prebuilt releases: [GitHub Releases](https://github.com/szumaster3/Interface-tool/releases)

<a href="#features">Features</a> •
<a href="#requirements">Requirements</a> •
<a href="#setup">Setup</a> •
<a href="https://github.com/szumaster3/Interface-tool/wiki">Documentation</a> •
<a href="#screenshots">Screenshots</a> •
<a href="#credits">Credits</a>

</div>

> [!NOTE]
>
> This tool requires a valid RuneScape cache to function.
>
> The cache is **not included** with this project and **will not be provided**. Users are responsible for obtaining and configuring their own cache.
>
> Historical RuneScape caches are available through the [OpenRS2 Archive](https://archive.openrs2.org/).

***

## Features

### Interface Editing
- Load, create, edit and preview IF1 and IF3 interfaces.
- Full support for IF1 and IF3 decoding and encoding.
- Create and manage components: layers, sprites, text, models, lines, figures, inventories and more.
- Edit component properties, events, scripts and CS1 instructions.
- Switch between Tab, Fixed and Full Screen interface layouts.
- Select, drag, resize and reposition components using the mouse or keyboard.
- Copy, paste, duplicate, reorder and delete components.
- Duplicate interfaces or copy them between caches.
- Import and export interfaces.
- Search component text and navigate interfaces using tree and list views.
- Undo and redo support.
- Ready-made component templates and custom component names.

### Rendering and Previews
- Client-accurate model rendering with support for new and 317 model formats.
- Cache-based font decoding and rendering, including kerning, wrapping, alignment and formatting effects.
- Preview cached fonts with custom text and formatting tags.
- Sprite and model previews.
- Inventory previews with item definitions and item icons.
- Sprite and model pickers.

### Cache Tools
- File Manager for browsing, searching, importing and exporting cache files.
- Sprite Manager with bulk import and export.
- Model library and model viewer.
- Cache backup and update checking.
- Custom cache paths and configurable dump directories.

### Additional Tools
- Color picker.
- IF3 key code editor.
- IF1 component naming.
- Event ID insertion.
- Interface and component templates.
- Custom interface indexes.
- Configurable editor layouts and zoom controls.

***

## Requirements

- Java 8 or newer
- A valid revision 530 cache

## Setup

1. Open the `config.properties` file.
2. Set `cache_path` to your cache directory.
    - Use `\` or `/` as path separators.
    - Make sure the path ends with a trailing slash.
3. Launch the application using Gradle.

**From command line:**

```bash
./gradlew run
```

**On Windows:**

```bat
gradlew.bat run
```

**From IDE:**

Import the project as a Gradle project and run the `run` task.

### Configuration

| Key | Description | Default |
|---|---|---|
| `cache_path` | Path to the cache directory | `data/cache/` |
| `dump_path` | Directory used for dumps and exports | `dump/` |
| `model_dump_path` | Directory used for model dumps | `dump/models/` |
| `sprite_path` | Optional directory with sprites | Empty |

## Building

Build a standalone JAR (written to `build/libs`):

```bash
./gradlew shadowJar
```

Package a distributable ZIP (written to `build/distro`):

```bash
./gradlew packageZip
```

## Project Structure

| Path | Content |
|---|---|
| `src/com/editor` | Editor GUI, interface drawing, toolbar and utilities |
| `src/com/editor/util/model` | Model viewer |
| `src/com/editor/util/sprites` | Sprite manager |
| `src/com/alex/loaders/interfaces` | Interface component definitions, decoding and encoding |
| `src/com/alex/loaders/mesh` | Model decoder, loader and renderer |
| `src/com/alex/store` | Cache store |

## Screenshots

<p align="center">
  <img src="https://imgur.com/GVlI2kF.gif" alt="Interface Editor Screenshot"/>
</p>

<details>
<summary>More...</summary>


<p align="center">
  <img src="https://imgur.com/Dv7KuQq.png" alt="Interface Editor Screenshot"/>
</p>

<p align="center">
  <img src="https://imgur.com/Anc7REA.png" alt="Interface Editor Screenshot"/>
</p>

<p align="center">
  <img src="https://imgur.com/EkNXBws.png" alt="Interface Editor Screenshot"/>
</p>

<p align="center">
  <img src="https://imgur.com/jspWwwg.png" alt="Interface Editor Screenshot"/>
</p>
<p align="center">
  <img src="https://imgur.com/KGNDiMR.png" alt="Interface Editor Screenshot"/>
</p>

<p align="center">
  <img src="https://imgur.com/2pDUduK.png" alt="Interface Editor Screenshot"/>
</p>

<p align="center">
  <img src="https://imgur.com/oA9qx9O.png" alt="Interface Editor Screenshot"/>
</p>

<p align="center">
  <img src="https://imgur.com/FekpayZ.png" alt="Interface Editor Screenshot"/>
</p>

</details>

***

## Credits

- Original Interface Tool source by PaoloKa:
    - [PaoloKa/Interface-tool](https://github.com/PaoloKa/Interface-tool)

- Model decoding based on Simple-JFX-Model-Editor:
    - [Suicolen/Simple-JFX-Model-Editor](https://github.com/Suicolen/Simple-JFX-Model-Editor)

- Icons provided by LuckyIcon Art and used in accordance with their license:
    - [LuckyIcon](https://www.luckyicon.com)