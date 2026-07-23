# Release 3.1.0

## What's New

A complete plugin system has been implemented for DikenEngine, allowing developers to extend the engine functionality with custom plugins.

### Major Features Added

- **Plugin System Architecture**: Abstract `Plugin` base class with lifecycle management (onEnable/onDisable)
- **PluginManager**: Manages loading, enabling, and disabling plugins from the `./plugins/` directory
- **JAR Plugin Support**: Load plugins from JAR files with `plugin.json` configuration files
- **Example Plugin**: Demonstration plugin showing how to create custom plugins
- **Plugin Manager UI**: New Plugin Manager panel accessible from Tools menu for managing active plugins
- **Studio Integration**: Plugins can contribute custom toolbar buttons and menu items
- **Plugin Settings**: Support for plugins to register custom settings
- **Proper Cleanup**: Automatic plugin cleanup and resource management on engine shutdown

### Technical Details

#### Plugin System Components

1. **Plugin Base Class** (`Plugin.java`)
   - Abstract methods: `getName()`, `getVersion()`, `getAuthor()`, `getDescription()`, `getIcon()`
   - Lifecycle methods: `onEnable()`, `onDisable()`
   - Integration hooks: `generateToolbar()`, `generateMenubar()`, `getPluginSettings()`

2. **PluginManager** (`PluginManager.java`)
   - Singleton pattern for centralized plugin management
   - `loadPlugins()` - Load JAR plugins from ./plugins/ directory
   - `loadLocalPlugin()` - Load locally compiled plugins
   - `enableAll()` / `disableAll()` - Manage plugin lifecycle
   - `getPlugins()` - Query loaded plugins

3. **Plugin Configuration** (`plugin.json`)
   - Required in JAR root: specify main plugin class

4. **UI Components**
   - `PluginManagerPanel` - Visual interface for plugin management
   - Display plugin metadata (name, version, author, description, icon)
   - Enable/Disable buttons for runtime plugin management

### Changes in Version 3.1.0

- Engine version bumped from 3.0.0 to 3.1.0
- Protocol version updated from 300 to 310
- Added plugin system initialization on engine startup
- Added plugin cleanup on engine shutdown
- StudioPanel updated to support dynamic UI generation from plugins
- Language files updated with plugin manager menu item translations

### How to Create a Plugin

```java
public class MyPlugin extends Plugin {
    @Override
    public String getName() { return "My Plugin"; }
    
    @Override
    public String getVersion() { return "1.0"; }
    
    @Override
    public String getAuthor() { return "Your Name"; }
    
    @Override
    public String getDescription() { return "My awesome plugin"; }
    
    @Override
    public Bitmap getIcon() { return null; }
    
    @Override
    protected void onEnable() {
        // Plugin initialization code
    }
    
    @Override
    protected void onDisable() {
        // Plugin cleanup code
    }
}
```

For JAR plugins, create a `plugin.json` in the JAR root:
```json
{
    "mainClass": "com.example.MyPlugin"
}
```

### Installation

Place plugin JAR files in the `./plugins/` directory and they will be automatically loaded on engine startup.

---

**Full Changelog**: [Compare with 3.0.0](https://github.com/OfficialEmirE/DikenEngine/compare/3.0.0...3.1.0)
