package me.ramazanenescik04.diken.scripting;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import org.luaj.vm2.*;
import org.luaj.vm2.lib.jse.*;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.EnumSettingType;
import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.game.setting.Setting;
import me.ramazanenescik04.diken.game.setting.SettingCategory;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

public class Script extends Node {
	public String source = "";
	public boolean enabled = true;
	
	private transient volatile boolean stopRequested = false;
	private transient Thread virtualThread = null;
	private transient Globals globals;
	private transient volatile boolean isInitialized = false;
	private transient volatile LuaValue luaUpdateFunction = null;
	
	public Script() {
		this("Script");
	}
	
	public Script(String name) {
		super(name);
	}

	public Script(DataInputStream in) throws IOException {
		super(in);
		loadNodeData(in);
	}
	
	public void initialize(World theWorld) {
	    stop();

	    if (!enabled || theWorld == null || source == null || source.isEmpty()) return;

	    stopRequested = false;
	    this.globals = JsePlatform.standardGlobals();

	    globals.load(new org.luaj.vm2.lib.DebugLib() {
            private int counter = 0;

            @Override
            public void onInstruction(int pc, Varargs v, int top) {
                if (stopRequested || Thread.currentThread().isInterrupted()) {
                    throw new LuaError("Script stopped: " + getName());
                }
                if ((++counter & 511) == 0) Thread.yield();
                super.onInstruction(pc, v, top);
            }
        });

	    // Bunları da virtual thread'e al
	    virtualThread = Thread.ofVirtual()
	        .name("lua-script-" + getName())
	        .start(() -> {
	            try {
	                LuaValue rootNode = CoerceJavaToLua.coerce(theWorld);
	                LuaValue bridge = CoerceJavaToLua.coerce(new LuaBridge(this));

	                globals.load(new String(
	                    (Script.class.getResourceAsStream("/scripts/init.lua").readAllBytes())
	                )).call(rootNode, bridge);

	                LuaInit.initClasses(globals);
	                LuaInit.initEnums(globals);

	                globals.load(source).call();

	                LuaValue fn = globals.get("update");
	                luaUpdateFunction = fn.isnil() ? null : fn;
	                isInitialized = true;

	            } catch (LuaError e) {
	                if (!stopRequested) {
	                    DikenEngine.errorLog("Lua Error [" + getName() + "]: ", e);
	                }
	            } catch (Exception e) {
	                DikenEngine.errorLog("Script Error [" + getName() + "]: ", e);
	            }
	        });
	}

    public void stop() {
        stopRequested = true;
        isInitialized = false;
        luaUpdateFunction = null;

        if (virtualThread != null) {
            virtualThread.interrupt();
            virtualThread = null;
        }

        globals = null;
    }

    @Override
    public void update(World world, DikenEngine engine) {
        if (luaUpdateFunction != null && isInitialized) {
            try {
                luaUpdateFunction.call();
            } catch (LuaError e) {
                if (!stopRequested) {
                    DikenEngine.errorLog("Lua Update Error [" + getName() + "]: " + e.getMessage());
                }
            } catch (Exception e) {
                DikenEngine.errorLog("Script Update Error [" + getName() + "]: " + e.getMessage());
            }
        }

        super.update(world, engine);
    }

    @LuaDoc
	public String getSource() {
		return source;
	}

	@LuaDoc
	public void setSource(String source) {
		this.source = source;
	}

	@LuaDoc
	public boolean isEnabled() {
		return enabled;
	}
	
	@LuaDoc
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	/**
	 * Bu Method SADECE Kod tamamlama gibi yerlerde kullanılmalıdır!
	 * @return
	 */
	@Deprecated
	public Globals getGlobals() {
		return globals;
	}

	@Override
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("script", "Script", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(12, 1));
		var settingCategory = SettingCategory
				.createSettingCategory(key)
				.addSetting(new Setting<Boolean>("Enabled", enabled, Boolean.class, EnumSettingType.CHECK_BOX).addChangeListener(this::setEnabled));
		
		var list = super.getNodeSettings();
		list.add(settingCategory);
		return list;
	}
	
	@Override
	public void saveNodeData(DataOutputStream out) throws IOException {
		super.saveNodeData(out);
		
		out.writeUTF(source);
		out.writeBoolean(enabled);
	}

	@Override
	public void loadNodeData(DataInputStream in) throws IOException {
		super.loadNodeData(in);
		
		this.source = in.readUTF();
		this.enabled = in.readBoolean();
	}

	@Override
    public Node copy() {
        Script cloned = (Script) super.copy();
        cloned.setSource(this.source);
        cloned.setEnabled(this.enabled);
        return cloned;
    }
}
