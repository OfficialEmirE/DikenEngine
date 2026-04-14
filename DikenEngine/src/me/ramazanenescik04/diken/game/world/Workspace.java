package me.ramazanenescik04.diken.game.world;

import java.util.List;

import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.game.SettingCategory;
import me.ramazanenescik04.diken.game.SettingCategory.SettingCategoryHelper;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

public class Workspace extends Node {
	private static final long serialVersionUID = 1L;

	public Workspace() {
		this("Workspace");
	}

	public Workspace(String name) {
		super(name);
		this.x = 0;
		this.y = 0;
	}

	@Override
	public Bitmap render() {
		return null;
	}
	
	@Override
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("workspace", "Workspace", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(9, 1));
		
		var settingCategory = SettingCategoryHelper.getOrCreateCategory(key, () -> SettingCategory
				.createSettingCategory(key));
		
		var list = super.getNodeSettings();
		list.add(settingCategory);
		return list;
	}

}
