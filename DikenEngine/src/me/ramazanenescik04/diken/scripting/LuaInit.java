package me.ramazanenescik04.diken.scripting;

import java.awt.event.*;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import me.ramazanenescik04.diken.game.nodes.Camera;
import me.ramazanenescik04.diken.game.nodes.Light;
import me.ramazanenescik04.diken.game.nodes.Part;
import me.ramazanenescik04.diken.gui.UDim2;
import me.ramazanenescik04.diken.gui.component.Panel;

public class LuaInit {

	public static void initClasses(Globals globals) {
		globals.set("UDim2", CoerceJavaToLua.coerce(UDim2.class));
		globals.set("KeyEvent", CoerceJavaToLua.coerce(KeyEvent.class));
		globals.set("MouseEvent", CoerceJavaToLua.coerce(MouseEvent.class));
	}

	public static void initEnums(Globals globals) {
		var cameraEnum = CoerceJavaToLua.coerce(Camera.CameraType.class);
		var lightEnum = CoerceJavaToLua.coerce(Light.LightType.class);
		var panelEnum = CoerceJavaToLua.coerce(Panel.BorderStyle.class);
		var partEnum = CoerceJavaToLua.coerce(Part.Surface.class);
		
		LuaTable enumTable = new LuaTable();
		enumTable.set("CameraType", cameraEnum);
		enumTable.set("LightType", lightEnum);
		enumTable.set("Surface", partEnum);
		enumTable.set("BorderStyle", panelEnum);
		
		globals.set("Enum", enumTable);
	}
}
