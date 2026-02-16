## Interface Editor

https://github.com/PaoloKa/Interface-tool

---

### Usage

#### Startup
1. Open the **`config.properties`** file.
2. Update the **`cache_path`** to point to your cache directory. 
   - Use `\\` or `/` as path separators.
   - Make sure the path ends with a trailing slash.
3. Launch the application either by:
   - Starting **`IfaceUI.kt`** directly from your IDE.
___
#### Tool Properties

```properties
cache_path=D:\\server\\data\\cache\\
dump_path=dump/
sprite_path=dump/
version=1
```

___

### Editing Interfaces
- Select an interface from the list on the **left**.
- Once selected, its components will appear on the **right**.
- Choose a component to edit its properties.

---

## Additional Information

### Font IDs

Below is a list of font IDs currently identified in the cache:

```
305, 307, 468, 473, 494, 495, 496, 497, 584, 591,
645, 646, 647, 648, 764, 776, 819, 1591, 2244,
2710, 3237, 3793, 3794, 3795, 4040, 5419, 5631,
13120, 13121
```

---

### Script IDs

A full list of discovered script IDs will be added in the future.

---

### Script System Overview

**How do scripts work in the Script tab?**

Example – transparency script:
```
1357;-2147483645;100;
```
___
The script logic (not editable in this editor) looks like this:
```java
void script_1357(Widget widget0, int arg1) {
    widget0.setTrans(arg1);
}
```
___
#### What do these values mean?

- **1357** – Script ID  
  The first value is always the script identifier.
- **-2147483645** – Widget reference  
  Refers to the widget itself (its own hash).  
  To target a different component, replace this with its hash.
- **100** – Script parameter  
  In this case, sets the transparency to 100% when the component is hovered.
