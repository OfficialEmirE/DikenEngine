package me.ramazanenescik04.diken.scripting;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.luaj.vm2.*;
import org.luaj.vm2.lib.jse.*;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.EnumSettingType;
import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.game.Setting;
import me.ramazanenescik04.diken.game.SettingCategory;
import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

public class Script extends Node {
	private static final long serialVersionUID = -7142348037284380694L;
	
	public String source = "";
	public boolean enabled = true;
	
	private transient Globals globals;
	private transient LuaValue luaUpdateFunction = null;
	private transient boolean isInitialized = false;
	
	public Script() {
		this("Script");
	}
	
	public Script(String name) {
		super(name);
	}

    public void initialize(World theWorld) {
        if (!enabled || theWorld == null || source == null || source.isEmpty()) return;

        this.luaUpdateFunction = null;
        this.globals = JsePlatform.standardGlobals();
        
        LuaValue rootNode = CoerceJavaToLua.coerce(theWorld);
        LuaValue brige = CoerceJavaToLua.coerce(new LuaBridge(this));

        try {
			globals.load(Files.readString(Paths.get(Script.class.getResource("/scripts/init.lua").toURI()))).call(rootNode, brige);
		} catch (IOException | URISyntaxException e) {
			DikenEngine.errorLog("Script init.lua Error: " + e.getMessage());
		}
        
        LuaInit.initClasses(globals);
        LuaInit.initEnums(globals);

        try {
            LuaValue chunk = globals.load(source);
            chunk.call(); 
            
            luaUpdateFunction = globals.get("update");
            if (luaUpdateFunction.isnil()) {
                luaUpdateFunction = null; 
            }
        } catch (Exception e) {
        	DikenEngine.errorLog("Script Initialiaze Error: " + e.getMessage());
        }
        
        this.isInitialized = true;
    }

	@Override
	public void update(World world, DikenEngine engine) {		
		if (luaUpdateFunction != null && isInitialized) {
            try {
                luaUpdateFunction.call();
            } catch (LuaError e) {
                DikenEngine.errorLog("Lua Update Error [" + getName() + "]: " + e.getMessage());
            } catch (Exception e) {
                DikenEngine.errorLog("Script Update Error [" + getName() + "]: " + e.getMessage());
            }
        }
		
		super.update(world, engine);
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("server_script", "Script", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(12, 1));
		var settingCategory = SettingCategory
				.createSettingCategory(key)
				.addSetting(new Setting<Boolean>("Enabled", enabled, Boolean.class, EnumSettingType.CHECK_BOX).addChangeListener(this::setEnabled));
		
		var list = super.getNodeSettings();
		list.add(settingCategory);
		return list;
	}
	
	@Override
    public Node copy() {
        Script cloned = (Script) super.copy();
        cloned.setSource(this.source);
        cloned.setEnabled(this.enabled);
        return cloned;
    }
}
