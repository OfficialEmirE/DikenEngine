# DikenEngine Documentation

This document describes the current DikenEngine 3.x source code. DikenEngine is a 2D game engine and Studio editor written in Java. A game scene is a `Node` tree; Lua scripts control the tree at runtime, while Java plugins extend Studio.

## Contents

- [Installation and execution](#installation-and-execution)
- [Project and runtime model](#project-and-runtime-model)
- [Lua API](#lua-api)
- [Java plugin API](#java-plugin-api)
- [World and file operations](#world-and-file-operations)
- [Resources and notes](#resources-and-notes)

## Installation and execution

### Requirements

- Java 25 or newer is recommended. The source uses virtual threads and current Java syntax.
- On Windows, `run.bat` automatically adds the LWJGL native files and JAR files under `DikenEngine/libs` to the classpath.
- On Linux and macOS, provide the equivalent Java classpath manually. Native files are under `DikenEngine/res/natives/<OS>`.

### Windows

```bat
run.bat
run.bat --studio
```

Running without arguments starts the game. `--studio` opens the Studio interface. Build output is written to `DikenEngine/bin`. Each run compiles the Java files under `src`.

### Manual compilation

From the project root:

```powershell
$cp = "DikenEngine\res;" + ((Get-ChildItem DikenEngine\libs\*.jar).FullName -join ";")
New-Item -ItemType Directory -Force DikenEngine\bin
javac -d DikenEngine\bin -cp $cp (Get-ChildItem DikenEngine\src -Recurse -Filter *.java).FullName
java -cp "$cp;DikenEngine\bin" me.ramazanenescik04.diken.DikenEngine --studio
```

### `.dwf` worlds

Version 3.0.0 can open and play `.dwf` files. Create the scene and resources in Studio, then save the world. At runtime, use `World.loadWorld(...)` to load a world.

## Project and runtime model

The source root is `DikenEngine/src/me/ramazanenescik04/diken`.

| Package | Responsibility |
|---|---|
| `game` | `World`, `Node`, `Instance`, events, settings, and the game model |
| `game.nodes` | Camera, part, sprite, audio, light, and other scene nodes |
| `game.services` | `Workspace`, runtime, input, UI, player, and lighting services |
| `gui` | Runtime GUI components and `UDim2` |
| `resource` | Bitmap, audio, cursor, and resource serialization |
| `scripting` | LuaJ integration, script lifecycle, and the Lua bridge |
| `plugin` | Studio plugin loading and lifecycle |
| `studio` | Editor panels, menus, toolbars, and settings |

Every node has a parent and a child list. Typical access from the root:

```lua
local workspace = game:GetService("Workspace")
local run = game:GetService("RunService")
local part = workspace:FindFirstChild("Part")
```

The Lua proxy exposes Java getters, setters, fields, and methods. Java method names are case-sensitive. `game.Workspace.Part` searches for children by name.

## Lua API

### Script lifecycle

The `source` field of a `Script` node is executed. If the script defines a global `update` function, it is called on every engine update:

```lua
function update()
    -- Called every frame.
end
```

Script source API:

| API | Description |
|---|---|
| `script.source` | Lua source text |
| `script.enabled` | Whether the script can run |
| `script:getSource()` | Returns the source |
| `script:setSource(source)` | Changes the source |
| `script:isEnabled()` | Returns the enabled state |
| `script:setEnabled(enabled)` | Changes the enabled state |

### Global Lua values

The following globals are installed by `res/scripts/init.lua`:

| Global | Description |
|---|---|
| `game` | Proxy for the world root node |
| `script` | Proxy for the running `Script` node |
| `Node` | Table containing `new` and `clone` helpers |
| `Enum` | Table containing engine enum proxies |
| `UDim2` | The `UDim2` Java class |
| `KeyEvent`, `MouseEvent` | AWT input classes |
| `NodeResource`, `Point`, `Event`, `Signal` | Java classes exposed to Lua |
| `hex(value)` | Converts a hexadecimal string, with or without a `0x` prefix, to a number |
| `print(...)` | Writes to the engine console |

`print` joins multiple arguments with spaces and forwards the result to the Java engine log.

### Creating and cloning nodes

```lua
local part = Node.new("Part")
part.Name = "PlayerBody"
part.Parent = game:GetService("Workspace")

local copy = Node.clone(part)
copy.Parent = part.Parent
```

- `Node.new(className)` searches `InstanceList` by simple class name. Examples include `Part`, `Texture`, `SpriteSheet`, `Folder`, and `Script`.
- `Node.clone(object)` works only for cloneable/archivable nodes.
- An unknown class or a `nil` object can produce `nil` and writes an error to the Java console.
- Add a child with `node:addChild(child)` or `child.Parent = parent`.

### Base Node API

The following methods are inherited by all `Node` types. The Java proxy exposes public Java methods and fields to Lua.

| API | Description |
|---|---|
| `getName()`, `setName(name)` | Node name |
| `getParent()`, `setParent(parent)` | Parent access |
| `getRootNode()` | Root node of the tree |
| `getChildren()` | Copy of the direct child list |
| `addChild(child)` | Adds a child at the end |
| `insertChild(index, child)` | Adds a child at an index |
| `removeChild(child)` | Detaches a child |
| `replaceChild(old, new)` | Replaces a child |
| `getChildIndex(child)` | Returns a child index |
| `isDescendantOf(other)` | Checks the hierarchy |
| `getDescendants()` | Returns all descendants |
| `findByName(name)` | Finds nodes by name |
| `findByClass(clazz)` | Finds nodes by Java class |
| `findByNetId(uuid)` | Finds nodes by UUID |
| `findFirstChild(name)` | Finds the first child by name |
| `findFirstChildByNetId(uuid)` | Finds the first UUID match |
| `findFirstChildOfClass(clazz)` | Finds the first class match |
| `getFullName()` | Dot-separated full name from the root |
| `getGlobalX()`, `getGlobalY()` | Global coordinates |
| `toPoint()` | Global `Point` |
| `getNetId()` | UUID identity |
| `isRemoved()` | Removal state |
| `removeNode()` | Marks the node for removal and fires `OnDestroy` |
| `getZIndex()`, `setZIndex(value)` | Render order |
| `isDebugRenderer()`, `setDebugRenderer(value)` | Debug hitbox rendering |
| `isArchivable()`, `setArchivable(value)` | Whether the node can be cloned/saved |

Node event fields are `OnAddChild`, `OnRemoveChild`, `OnInsertChild`, `OnReplaceChild`, `OnAddDescendant`, `OnRemoveDescendant`, `OnInsertDescendant`, `OnReplaceDescendant`, `OnParentChangedDescendant`, `OnUpdate`, `OnDispose`, `OnReload`, `OnDestroy`, `OnParentChanged`, `OnPropertyChanged`, `OnPreRender`, and `OnPostRender`.

### Event API

```lua
local signal = part.OnCollision:Connect(function(other)
    print("Collision", other)
end)

part.OnCollision:FireEvent(other)
part.OnCollision:Disconnect(signal)
```

| API | Description |
|---|---|
| `event:Connect(function(...))` | Connects a Lua callback and returns a `Signal` |
| `event:Disconnect(signal)` | Removes a connection |
| `event:FireEvent(...)` | Calls all listeners |
| `BindableEvent:Connect(function(...))` | Connects to a scene event node |
| `BindableEvent:FireEvent(...)` | Calls the BindableEvent listeners |

### Instance API

`Instance` is the base class for renderable nodes:

| API | Description |
|---|---|
| `getX()`, `setX(x)`, `getY()`, `setY(y)` | Local position |
| `getGlobalX()`, `getGlobalY()` | Position including parents |
| `setLocation(x, y)` | Sets the local position |
| `getScaleX()`, `setScaleX(v)`, `getScaleY()`, `setScaleY(v)` | Scale |
| `getRotation()`, `setRotation(degrees)` | Rotation in degrees |
| `getColor()`, `setColor(argb)` | ARGB tint |
| `isSolid()`, `setSolid(value)` | Whether collision is enabled |
| `isAnchored()`, `setAnchored(value)` | Whether physics keeps the instance fixed |
| `getRenderType()`, `setRenderType(type)` | Render scope |
| `getGlobalAABB()`, `hasAABB()` | Hitbox access |
| `setAABB(width, height)` | Creates a local AABB |
| `getAABBWidth()`, `getAABBHeight()` | AABB dimensions |
| `setAABBSize(width, height)` | Updates AABB dimensions |
| `findInArea(area)` | Finds instances in an area |

`Enum.RenderType` values are `InVisible`, `OnlyRenderThis`, `OnlyRenderChildrens`, and `RenderAll`.

### Game nodes

| Class | Public API / main fields |
|---|---|
| `Part` | `getSurface`, `setSurface`; `Surface` enum |
| `Texture` | `getTexture`, `setTexture`, `getTextureBitmap` |
| `ImageNode` | `getTexture`, `setTexture`, `getTextureBitmap` |
| `Decal` | ImageNode API; renders a texture on a parent instance surface |
| `SpriteSheet` | `getAnimationID`, `setAnimationID`, `getAnimation`, `isPlaying`, `setPlaying`, `getImageType`, `setImageType` |
| `Audio` | `getSound`, `setSound`, `playAudio`, `isPlaying`, `setLoop`, `isLoop`, `setVolume`, `getVolume`, `setPosition`, `getPosition`, `setPitch`, `getPitch` |
| `Light` | `getLightColor`, `setLightColor`, `getRadius`, `setRadius`, `getIntensity`, `setIntensity`, `getType`, `setType`, `getDirection`, `setDirection`, `getConeAngle`, `setConeAngle`, `isShadows`, `setShadows` |
| `Camera` | `getCameraType`, `setCameraType`, `getFollowingInstance`, `setFollowingInstance`, `getPosition`, `setPosition`, `getX`, `setX`, `getY`, `setY`, `addX`, `addY`, `getZoom`, `setZoom`, `reset` |
| `Sky` | `getTexture`, `setTexture`, `syncToCamera` |
| `Tool` | `getIcon`, `setIcon`, `getIconBitmap` |
| `Folder` | Node hierarchy API |
| `Model` | Instance API and child render grouping |
| `SpawnLocation` | Part API; player spawn location |
| `BooleanValue`, `FloatValue`, `IntegerValue`, `StringValue`, `ObjectValue` | `getValue`, `setValue`, `getTypeClass` |

### Services

Get services with `game:GetService("ServiceName")`. Default world services include `Workspace`, `PlayerService`, `UIService`, `InputService`, `RunService`, `Lighting`, and `Game`.

| Service | API |
|---|---|
| `Workspace` | `findInArea(area)` and the Node/Instance API |
| `RunService` | `isRunning`, `run`, `stop`, `restart`; `OnUpdate` event |
| `InputService` | `isKeyDown(key)`, `isKeyPressed(key)`, `isKeyReleased(key)`, `setCursor(resource)` |
| `PlayerService` | `getUsername`, `setUsername`, `getCharacter`, `setCharacter` |
| `Lighting` | `getSky`, `setSky`, `getAmbientColor`, `setAmbientColor`, `isLightingEnabled`, `setLightingEnabled` |
| `UIService` | Updates and renders the UI node tree |
| `Game` | `HttpSend(url, method, data)`, `HttpGet(url)`, `HttpPost(url, data)` |

`InputService` events are `OnKeyHandled`, `OnKeyDown`, `OnMouseHandled`, and `OnMouseClicked`. Callback arguments contain the engine input mode, key/mouse code, character, and button values.

### GUI API

GUI nodes should be placed under `ScreenGui`. Common `GuiComponent` API:

| API | Description |
|---|---|
| `getPosition`, `setPosition(UDim2 or x,y)` | UI position |
| `getSize`, `setSize(UDim2 or w,h)` | UI size |
| `getGlobalX`, `getGlobalY`, `getLocalX`, `getLocalY` | Pixel positions |
| `getWidth`, `getHeight`, `getAbsoluteBounds` | Calculated dimensions and bounds |
| `setVisible`, `isVisible` | Visibility |
| `setActive`, `isActive` | Input activation |
| `addGuiListener`, `removeGuiListener` | GUI listener management |

GUI-specific APIs:

| Class | API |
|---|---|
| `ScreenGui` | `isEnabled`, `setEnabled`, `create`, `createFramePool`, `drawBitmap`, `keyHandled`, `mouseHandled` |
| `Panel` | `isClipsDescendants`, `setClipsDescendants`, `get/setBorderStyle`, `get/setBackgroundColor`, `get/setBorderColor`, `get/setBorderSize` |
| `Text` | `get/setText`, `get/setColor`, `get/setTextPosition`, `get/setFont`, `calculateTextCoordinates` |
| `TextField` | `get/setText`, `setTextChanged`, `setPressedEnter`, `setFocused`, `isFocused`, `setNumberic`, `isNumberField`, `setNumberField` |
| `PasswordField` | TextField API |
| `TextLine` | `get/setTextLines`, `add`, `remove`, `clear`, `get/setText`, `get/setFont`, `get/setColor`, `get/setBgColor`, `autoSetSize`, `get/setEditable`, `get/setFocused` |
| `Button` | `get/setText`, `setTextColor`, `setButtonColor`, `setButtonIcon`, `getButtonIcon`, `setButtonIconLeft`, `isButtonIconLeft`, `setRunnable`, `isTouchingMouse` |
| `ImageButton` | `getIcon`, `setIcon` |
| `CheckBox` | `isChecked`, `setChecked`, `get/setText`, `setConsumer` |
| `ProgressBar` | `get/setValue`, `get/setMaxValue`, `get/setColor`, `get/setColor2`, `get/setBackgroundColor`, `get/setText` |
| `RenderImage` | `getTexture`, `setTexture` |
| `ScrollBar` | `addDraggedListener`, `get/setScrollValue`, `updateHandleSize` |
| `ColorPickBox` | `setConsumer`, `get/setSelectedColor`, `get/setHueColor`, `getSelectedPosColor` |
| `ColorPickBar` | `setConsumer`, `get/setSelectedColor` |
| `AlphaPickBar` | `setConsumer`, `setSelectedAlpha`, `setBaseColor` |

### `UDim2` and enums

```lua
local size = UDim2.of(0, 320, 0, 180)
local panel = Node.new("Panel")
panel.Position = UDim2.zero
panel.Size = size
panel.BorderStyle = Enum.BorderStyle.Line
```

`UDim2` constructor: `UDim2(scaleX, offsetX, scaleY, offsetY)`. Constants are `UDim2.zero`, `UDim2.defaultV`, and `UDim2.fullscreen`. Use `UDim2:clone()` and `UDim2:getGlobalPosition(width, height)` as needed.

Lua enum tables:

| Enum table | Source enum |
|---|---|
| `Enum.CameraType` | `Camera.CameraType` |
| `Enum.LightType` | `Light.LightType` |
| `Enum.Surface` | `Part.Surface` |
| `Enum.BorderStyle` | `Panel.BorderStyle` |
| `Enum.ImageType` | `SpriteSheet.ImageType` |
| `Enum.ResourceType` | `EnumResource` |
| `Enum.TextPosition` | `Text.TextPosition` |
| `Enum.RenderType` | `Instance.RenderType` |

## Java plugin API

Plugins are loaded in the Studio process. The main plugin class must extend `me.ramazanenescik04.diken.plugin.Plugin` and provide a public no-argument constructor.

### JAR layout

Place the JAR in the `plugins/` directory at the project root:

```text
plugins/
  ExamplePlugin.jar
```

The JAR root must contain `plugin.json`:

```json
{
  "mainClass": "com.example.MyPlugin"
}
```

`PluginManager` scans every `.jar`, loads `mainClass`, verifies that it extends `Plugin`, and creates it with a public no-argument constructor.

### Minimal plugin

```java
package com.example;

import me.ramazanenescik04.diken.plugin.Plugin;
import me.ramazanenescik04.diken.resource.Bitmap;

public final class MyPlugin extends Plugin {
    @Override public String getName() { return "My Plugin"; }
    @Override public String getVersion() { return "1.0.0"; }
    @Override public String getAuthor() { return "Author"; }
    @Override public String getDescription() { return "Studio extension."; }
    @Override public Bitmap getIcon() { return null; }

    @Override protected void onEnable() {
        System.out.println("Plugin enabled");
    }

    @Override protected void onDisable() {
        System.out.println("Plugin disabled");
    }
}
```

### `Plugin` class

Required abstract methods:

| Method | Description |
|---|---|
| `getName()` | Display name |
| `getVersion()` | Plugin version |
| `getAuthor()` | Author |
| `getDescription()` | Description |
| `getIcon()` | `Bitmap` icon, or `null` |
| `onEnable()` | Enable callback; `protected` in subclasses |
| `onDisable()` | Disable callback; `protected` in subclasses |

Public lifecycle and access methods:

| Method | Description |
|---|---|
| `final enable(DikenEngine, StudioPanel)` | Sets engine/studio references and calls `onEnable` once |
| `final disable()` | Calls `onDisable` when enabled |
| `isEnabled()` | Returns the state |
| `info()` | Returns `PluginInfo` metadata |
| `setInfo(PluginInfo)` | Assigned by the manager |
| `generateToolbar(Toolbar.Builder)` | Toolbar extension point |
| `generateMenubar(Menubar.Builder)` | Menu extension point |
| `playTestMode(boolean)` | Play/test mode callback |
| `getPluginSettings()` | Studio settings; defaults to `List.of()` |

Because `enable` and `disable` are final, a plugin cannot override its lifecycle. The subclass can use the protected `engine` and `studio` fields.

### Toolbar builder

```java
@Override
public void generateToolbar(Toolbar.Builder builder) {
    Toolbar toolbar = builder.getToolbar("my-tools");
    builder.addButton(toolbar, "hello", 0, 0, "plugin.hello", () -> {
        System.out.println("Hello");
    });
}
```

`Toolbar` methods: `addButton(key, AbstractButton)`, `removeButton(key)`, `getToolbarID()`, `getButton(key)`, and `getButtons()`.

`Toolbar.Builder` methods: `newToolbar(id)`, `getToolbar(id)`, `addButton(toolbar, key, iconX, iconY, tooltip, Runnable, Object...)`, `setButtonChecked`, `getButtonChecked`, `getJToolBar()`, and `convertCButton(DefaultSingleCDockable)`.

Toolbar icons are read from the `editor_icons` sprite resource. Tooltip text is localized through `Lang.get`.

### Menubar builder

```java
@Override
public void generateMenubar(Menubar.Builder builder) {
    Menubar menu = builder.newMenu("my-menu", "plugin.menu");
    String id = builder.addMenuItem(menu, "plugin.open", 0, 1,
        () -> System.out.println("Open"));
    builder.setButtonEnabled(menu, id, true);
}
```

`Menubar` methods: `addSeperator()`, `addButton(key, JMenuItem)`, `removeButton(key)`, `getName()`, `setName(name)`, `getMenubarID()`, `getButton(key)`, and `getButtons()`.

`Menubar.Builder` methods: `newMenu`, `getMenu`, `addMenuItem`, `addMenuItemCheckBox`, `addMenuAccelerator`, `addMenuSeparator`, `setButtonChecked`, `getButtonChecked`, `setButtonEnabled`, `isButtonEnabled`, `getJMenuBar`, and `getJPopupMenu`.

Negative icon coordinates disable icons. The `KeyStroke` overload adds a menu accelerator.

### Plugin settings

Return a `List<Setting<?>>` from `getPluginSettings()` to add settings to the Studio settings panel:

```java
Setting<Boolean> setting = new Setting<>(
    "Enabled", true, Boolean.class, EnumSettingType.CHECK_BOX
).addChangeListener(value -> {
    // New value
});
```

Important `Setting` methods include `getName`, `setName`, `getValue`, `setValue`, `getTypeClass`, `getSettingType`, `addChangeListener`, `getChangeListeners`, `isSaveable`, and `setSaveable`.

### `PluginInfo` and manager

`PluginInfo` is a Java record:

```java
Class<? extends Plugin> pluginClass();
File pluginFile();
URLClassLoader classLoader();
```

Public API of the `PluginManager.instance` singleton:

| Method/field | Description |
|---|---|
| `loadPlugins(engine, studio)` | Loads `./plugins/*.jar` files |
| `loadLocalPlugin(pluginClass, engine, studio)` | Loads a plugin already on the classpath |
| `enableAll(engine, studio)` | Enables all plugins, then fires `allPluginsLoaded` |
| `disableAll()` | Disables plugins, closes URL classloaders, and clears the list |
| `getPlugins()` | Unmodifiable plugin list |
| `allPluginsLoaded` | `Event` fired after all plugins are enabled |

Plugins are loaded in filesystem order without dependency resolution. If one plugin fails, the error is logged and loading continues for the others.

## World and file operations

Java world serialization API:

```java
World world = World.loadWorld(file);
World.saveWorld(world, file);
byte[] bytes = World.saveWorldToBytes(world);
World copy = World.loadWorldFromBytes(bytes);
```

Overloads accept `File`, `InputStream`, and `OutputStream`. `.dwf` files contain GZIP-compressed binary node data; do not edit them as JSON.

Resources are managed through `ResourceLocator`:

```java
IResource resource = ResourceLocator.getResource("background");
ResourceLocator.addResource("my-resource", resource);
```

Supported resource implementations include `Bitmap`, `ArrayBitmap`, `SoundResource`, `CursorResource`, `UniFont`, and `EnumResource`. Lua normally uses resource ID strings through node properties.

## Resources and notes

- Lua integration uses LuaJ 3.0.2.
- Scripts start on separate virtual threads. `Script.stop()` interrupts the thread and clears the Lua update callback.
- Long blocking operations inside Lua `update` can affect runtime performance.
- Event callbacks run in connection order. Connecting the same callback multiple times creates multiple calls.
- The current `setParent` implementation has a special null-parent path. When detaching a node, prefer `parent:removeChild(node)`.
- DikenEngine is intended to be compiled with Java 25 or newer. Dependency JARs are in `DikenEngine/libs`.
- This document covers public APIs found in the source. Update it when adding a new public method.
