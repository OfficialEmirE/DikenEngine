package me.ramazanenescik04.diken.scripting;

import java.awt.Point;
import java.awt.event.*;
import java.util.*;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaTable;

import me.ramazanenescik04.diken.game.NodeResource;
import me.ramazanenescik04.diken.game.event.Event;
import me.ramazanenescik04.diken.game.event.Signal;
import me.ramazanenescik04.diken.game.nodes.Camera;
import me.ramazanenescik04.diken.game.nodes.Light;
import me.ramazanenescik04.diken.game.nodes.Part;
import me.ramazanenescik04.diken.game.nodes.SpriteSheet;
import me.ramazanenescik04.diken.gui.UDim2;
import me.ramazanenescik04.diken.gui.component.Panel;
import me.ramazanenescik04.diken.gui.component.Text;
import me.ramazanenescik04.diken.resource.EnumResource;

import static org.luaj.vm2.lib.jse.CoerceJavaToLua.*;

public class LuaInit {
	
	public static Map<String, Class<?>> initClasses() {
		Map<String, Class<?>> enumList = new HashMap<>();
		enumList.put("UDim2", UDim2.class);
		enumList.put("KeyEvent", KeyEvent.class);
		enumList.put("MouseEvent", MouseEvent.class);
		enumList.put("NodeResource", NodeResource.class);
		enumList.put("Point", Point.class);
		enumList.put("Event", Event.class);
		enumList.put("Signal", Signal.class);
		
		return enumList;
	}
	
	public static Map<String, Class<? extends Enum<?>>> initEnums() {
		var spriteEnum = (SpriteSheet.ImageType.class);
		var cameraEnum = (Camera.CameraType.class);
		var panelEnum = (Panel.BorderStyle.class);
		var resourceEnum = (EnumResource.class);
		var lightEnum = (Light.LightType.class);
		var partEnum = (Part.Surface.class);
		
		Map<String, Class<? extends Enum<?>>> enumList = new HashMap<>();
		enumList.put("CameraType", cameraEnum);
		enumList.put("LightType", lightEnum);
		enumList.put("Surface", partEnum);
		enumList.put("BorderStyle", panelEnum);
		enumList.put("ImageType", spriteEnum);
		enumList.put("ResourceType", resourceEnum);
		enumList.put("TextPosition", Text.TextPosition.class);
		
		return enumList;
	}
	
	public static void initClasses(Globals globals) {
		initClasses().entrySet().forEach(e -> {
			globals.set(e.getKey(), coerce(e.getValue()));
		});
	}

	public static void initEnums(Globals globals) {
		LuaTable enumTable = new LuaTable();
		
		initEnums().entrySet().forEach(e -> {
			enumTable.set(e.getKey(), coerce(e.getValue()));
		});
		
		globals.set("Enum", enumTable);
	}
}
