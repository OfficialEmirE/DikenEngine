package me.ramazanenescik04.diken.game.nodes;

import java.util.List;

import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.game.SettingCategory;
import me.ramazanenescik04.diken.game.SettingCategory.SettingCategoryHelper;
import me.ramazanenescik04.diken.gui.hitbox.Hitbox;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

/**
 * Represents the `Part` type within the DikenEngine `game.nodes` package.
 */
public class Part extends Node {	
	private static final long serialVersionUID = 4072578864221886901L;
	
	public Part() {
		super();
		this.name = "Part";
		this.aabb = new Hitbox(0, 0, 16, 16);
		this.setAnchored(true);
	}

	public Part(int x, int y, int width, int height) {
		super("Part", x, y);
		this.aabb = new Hitbox(0, 0, width, height);
		this.setAnchored(true);
	}
	
	public Part(int x, int y) {
		super("Part", x, y);
		this.aabb = new Hitbox(0, 0, 16, 16);
		this.setAnchored(true);
	}
	
	public Bitmap render() {
		Bitmap bitmap = new Bitmap(this.aabb.width, this.aabb.height);
		bitmap.clear(color);
		return bitmap;
	}
	
	@Override
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("part", "Part", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(0, 1));
		
		var settingCategory = SettingCategoryHelper.getOrCreateCategory(key, () -> SettingCategory
				.createSettingCategory(key));
		
		var list = super.getNodeSettings();
		list.add(settingCategory);
		return list;
	}
}
