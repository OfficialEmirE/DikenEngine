package me.ramazanenescik04.diken.scripting;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.luaj.vm2.*;
import org.luaj.vm2.lib.jse.*;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.EnumSettingType;
import me.ramazanenescik04.diken.game.InstanceList;
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
        LuaValue brige = CoerceJavaToLua.coerce(bridge);
        
        globals.set("script", CoerceJavaToLua.coerce(this));

        try {
			globals.load(Files.readString(Paths.get(Script.class.getResource("/scripts/init.lua").toURI()))).call(rootNode, brige);
		} catch (IOException | URISyntaxException e) {
			DikenEngine.errorLog("Script init.lua Error: " + e.getMessage());
		}

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
                luaUpdateFunction.call(
                    CoerceJavaToLua.coerce(engine), 
                    CoerceJavaToLua.coerce(world)
                );
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
				.addSetting(new Setting<String>("Source", source, String.class, EnumSettingType.TEXT_FIELD).addChangeListener(this::setSource))
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
	
	private Object bridge = new Object() {
        @SuppressWarnings("unused")
		public Object create(String className) {
            for (Node node : InstanceList.getNodeList()) {
                if (node.getClass().getSimpleName().equalsIgnoreCase(className)) {
                    return node.copy();
                }
            }
            DikenEngine.errorLog("Instance.new Error: '" + className + "' adında bir Node bulunamadı!");
            return null;
        }
        
        @SuppressWarnings("unused")
		public Object clone(Object object) {
        	if (object == null) {
        		DikenEngine.errorLog("Instance.clone Error: Object Null Olamaz!");
        		return null;
        	}
        	
        	if (object instanceof Node node) {
        		var copyNode = node.copy();
        		if (copyNode != null) {
        			return copyNode;
        		}
        		
        		DikenEngine.errorLog("Instance.clone Error: '" + object.getClass().getSimpleName() + ", Archiveable true değil.");
        		return null;
        	} else if (object instanceof Cloneable c) {
        		return c;
        	}
        	
        	DikenEngine.errorLog("Instance.clone Error: '" + object.getClass().getSimpleName() + ", Klonlamayı desteklemiyor.");
            return null;
        }
        
        @SuppressWarnings("unused")
		public Object httpGet(String url) {
			try {
				var httpClient = HttpClient.newHttpClient();
            	var httpRequest = HttpRequest.newBuilder(URI.create(url))
            			.GET()
            			.header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:151.0) Gecko/20100101 Firefox/151.0")
            			.build();
            	
				var response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
				
				return response.body();
			} catch (Exception e) {
				DikenEngine.errorLog("HttpGet Error: " + e.getMessage());
			}
			
			return null;
        }
        
        @SuppressWarnings("unused")
		public Object getCurrentScript() {
        	return Script.this;
        }
        
        @SuppressWarnings("unused")
		public void log(String message) {
            DikenEngine.log(message);
        }
    };

}
