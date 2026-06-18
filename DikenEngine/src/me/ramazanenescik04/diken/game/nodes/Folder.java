package me.ramazanenescik04.diken.game.nodes;

import java.util.List;

import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.game.SettingCategory;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

/**
 * Represents the `Folder` type within the DikenEngine `game.nodes` package.
 */
public class Folder extends Node {
	private static final long serialVersionUID = -1974610025825096210L;
	
	public Folder() {
		this("Folder");
	}

	public Folder(String name) {
		super(name);
	}
	
	@Override
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("folder", "Folder", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(6, 1));
		
		var settingCategory = SettingCategory
				.createSettingCategory(key);
		
		var list = super.getNodeSettings();
		list.add(settingCategory);
		return list;
	}

}
