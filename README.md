<div align="center">
<h2>Interface Editor</h2>
<h3>Source: <a href="https://github.com/PaoloKa/Interface-tool">https://github.com/PaoloKa/Interface-tool</a></h3>
<h4>Icons used in this project are provided by <a href="https://www.luckyicon.com">LuckyIcon Art</a> and are used according to their license.</h4>

<a href="#startup">Startup</a> •
<a href="#tool-properties">Configuration</a> •
<a href="#editing-interfaces">Editing</a> •
<a href="#scripts">Scripts overview</a> •
[Scripts references](data/REFERENCES.md) •
<a href="https://github.com/szumaster3/Interface-tool/wiki">Documentation</a>

</div>

## Startup

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
-  Import the project as a Gradle project and run the `run` task.

## Tool Properties

```properties
cache_path=data/cache
dump_path=dump/
sprite_path=dump/
```

## Editing Interfaces
- Select an interface from the list on the **left**.
- Once selected, its components will appear on the **right**.
- Choose a component to edit its properties.

## Font IDs

Below is a list of font IDs currently identified:

```
305, 307, 468, 473, 494, 495, 496, 497, 584, 591,
645, 646, 647, 648, 764, 776, 819, 1591, 2244,
2710, 3237, 3793, 3794, 3795, 4040, 5419, 5631,
13120, 13121
```

## Scripts

**How do scripts work?**

Example – transparency script:

```
1357;-2147483645;100;
```

The script logic (not editable in this editor) looks like this:
```java
void script_1357(Widget widget0, int arg1) {
    widget0.setTrans(arg1);
}
```

## What do these values mean?

- **1357** – Script ID  
  The first value is always the script identifier.
- **-2147483645** – Widget reference  
  Refers to the widget itself (its own hash).  
  To target a different component, replace this with its hash.
- **100** – Script parameter  
  In this case, sets the transparency to 100% when the component is hovered.
