package me.ramazanenescik04.diken.game.nodes;

import java.util.List;

import me.ramazanenescik04.diken.game.Instance;
import me.ramazanenescik04.diken.game.SettingCategory;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

/**
 * Represents the `Decal` type within the DikenEngine `game.nodes` package.
 */
public class Decal extends ImageNode {
	private static final long serialVersionUID = 1L;

	public Decal() {
		super("Decal");
	}
	
	public Decal(String name) {
		super(name);
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
		
		return texture.resize(parentBitmap.w, parentBitmap.h);
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
