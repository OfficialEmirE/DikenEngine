package me.ramazanenescik04.diken.game.nodes;

import java.util.List;

import me.ramazanenescik04.diken.game.Instance;
import me.ramazanenescik04.diken.game.SettingCategory;
import me.ramazanenescik04.diken.renderer.FrameBitmapPool;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

/**
 * Represents the `Texture` type within the DikenEngine `game.nodes` package.
 */
public class Texture extends ImageNode {
	private static final long serialVersionUID = 1L;

	public Texture() {
		super("Texture");
	}
	
	public Texture(String name) {
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
		
		Bitmap thisBitmap = FrameBitmapPool.newBitmap(parentBitmap.w, parentBitmap.h);
		for (var y = 0; y < (parentBitmap.h / texture.h) + 1; y++) {
			for (var x = 0; x < (parentBitmap.w / texture.w) + 1; x++) {
				thisBitmap.blendDraw(texture, x * texture.w, y * texture.h, this.color);
			}
		}
		return thisBitmap;
	}
	
	@Override
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("texture", "Texture", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(1, 1));
		
		var settingCategory = SettingCategory
				.createSettingCategory(key);
		
		var list = super.getNodeSettings();
		list.add(settingCategory);
		return list;
	}

}

