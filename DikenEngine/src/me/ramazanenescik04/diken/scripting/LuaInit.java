package me.ramazanenescik04.diken.scripting;

import java.awt.Point;
import java.awt.event.*;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaTable;

import me.ramazanenescik04.diken.game.nodes.Camera;
import me.ramazanenescik04.diken.game.nodes.Light;
import me.ramazanenescik04.diken.game.nodes.Part;
import me.ramazanenescik04.diken.game.nodes.SpriteSheet;
import me.ramazanenescik04.diken.gui.UDim2;
import me.ramazanenescik04.diken.gui.component.Panel;

import static org.luaj.vm2.lib.jse.CoerceJavaToLua.*;

public class LuaInit {

	public static void initClasses(Globals globals) {
		globals.set("UDim2", coerce(UDim2.class));
		globals.set("KeyEvent", coerce(KeyEvent.class));
		globals.set("MouseEvent", coerce(MouseEvent.class));
		globals.set("Point", coerce(Point.class));
	}

	public static void initEnums(Globals globals) {
		var spriteEnum = coerce(SpriteSheet.ImageType.class);
		var cameraEnum = coerce(Camera.CameraType.class);
		var panelEnum = coerce(Panel.BorderStyle.class);
		var lightEnum = coerce(Light.LightType.class);
		var partEnum = coerce(Part.Surface.class);
		
		LuaTable enumTable = new LuaTable();
		enumTable.set("CameraType", cameraEnum);
		enumTable.set("LightType", lightEnum);
		enumTable.set("Surface", partEnum);
		enumTable.set("BorderStyle", panelEnum);
		enumTable.set("ImageType", spriteEnum);
		
		globals.set("Enum", enumTable);
	}
}
