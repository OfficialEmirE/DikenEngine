package me.ramazanenescik04.diken.game.nodes;

import java.util.List;

import me.ramazanenescik04.diken.game.SettingCategory;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

/**
 * Represents the `SpawnLocation` type within the DikenEngine `game.nodes` package.
 */
public class SpawnLocation extends Part {
	private static final long serialVersionUID = -6112488161375121027L;
	
	public SpawnLocation() {
		super();
		this.name = "SpawnLocation";
		this.color = 0xfff0f0f0;
		this.setSolid(false);
		this.setAnchored(true);
	}

	public SpawnLocation(int x, int y, int width, int height) {
		super(x, y, width, height);
		this.name = "SpawnLocation";
		this.color = 0xfff0f0f0;
		this.setSolid(false);
		this.setAnchored(true);
	}
	
	@Override
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("spawnLocation", "SpawnLocation", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(5, 1));
		
		var settingCategory = SettingCategory
				.createSettingCategory(key);
		
		var list = super.getNodeSettings();
		list.add(settingCategory);
		return list;
	}
}
