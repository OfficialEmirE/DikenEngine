package me.ramazanenescik04.diken.game.nodes;

import java.util.List;

import me.ramazanenescik04.diken.game.Instance;
import me.ramazanenescik04.diken.game.SettingCategory;
import me.ramazanenescik04.diken.gui.hitbox.Hitbox;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

/**
 * Represents the `Part` type within the DikenEngine `game.nodes` package.
 */
public class Part extends Instance {
	private static final long serialVersionUID = 4072578864221886901L;
	private transient Bitmap cachedBitmap;
	private transient int cachedWidth = -1;
	private transient int cachedHeight = -1;
	private transient int cachedColor = 0;
	
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
		int width = Math.max(1, this.aabb.getWidth());
		int height = Math.max(1, this.aabb.getHeight());
		
		if (cachedBitmap == null || cachedWidth != width || cachedHeight != height || cachedColor != color) {
			cachedBitmap = new Bitmap(width, height);
			cachedBitmap.clear(color);
			cachedWidth = width;
			cachedHeight = height;
			cachedColor = color;
		}
		
		return cachedBitmap;
	}

	@Override
	protected void reloadNode() {
		cachedBitmap = null;
		cachedWidth = -1;
		cachedHeight = -1;
	}
	
	@Override
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("part", "Part", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(0, 1));
		
		var settingCategory = SettingCategory
				.createSettingCategory(key);
		
		var list = super.getNodeSettings();
		list.add(settingCategory);
		return list;
	}
}

