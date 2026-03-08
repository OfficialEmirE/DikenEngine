package me.ramazanenescik04.diken.game.nodes;

import java.util.List;

import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.game.SettingCategory;
import me.ramazanenescik04.diken.game.SettingCategory.SettingCategoryHelper;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

public class Folder extends Node {
	private static final long serialVersionUID = -1974610025825096210L;

	// Folder klasör gibidir. x ve y si daima 0 dır!
	
	public Folder() {
		this.x = 0;
		this.y = 0;
	}

	public Folder(String name) {
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
		var key = new SettingCategory.SettingKey("folder", "Folder", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(6, 1));
		
		var settingCategory = SettingCategoryHelper.getOrCreateCategory(key, () -> SettingCategory
				.createSettingCategory(key));
		
		var list = super.getNodeSettings();
		list.add(settingCategory);
		return list;
	}

}
