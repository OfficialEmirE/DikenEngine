package me.ramazanenescik04.diken.game.nodes;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;

import me.ramazanenescik04.diken.game.Instance;
import me.ramazanenescik04.diken.game.setting.SettingCategory;
import me.ramazanenescik04.diken.renderer.FrameBitmapPool;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

/**
 * Represents the `Decal` type within the DikenEngine `game.nodes` package.
 */
public class Decal extends ImageNode {
	public Decal() {
		super("Decal");
	}
	
	public Decal(String name) {
		super(name);
	}

	public Decal(DataInputStream in) throws IOException {
		super(in);
		loadNodeData(in);
	}

	@Override
	public Bitmap render() {
		if (this.parent == null) {
			return null;
		}
		
		Bitmap parentBitmap = null;
		if (this.parent instanceof Instance i) {
			parentBitmap = i.render();
		}
		
		if (parentBitmap == null || this.texture == null) {
			return null;
		}
		
		Bitmap scaledTexture = FrameBitmapPool.newBitmap(parentBitmap.w, parentBitmap.h);
		texture.scaleInto(scaledTexture);
		return scaledTexture;
	}
	
	@Override
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("decal", "Decal", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(1, 1));
		
		var settingCategory = SettingCategory
				.createSettingCategory(key);
		
		var list = super.getNodeSettings();
		list.add(settingCategory);
		return list;
	}

}
