package me.ramazanenescik04.diken.game.nodes;

import java.util.List;

import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.game.SettingCategory;
import me.ramazanenescik04.diken.game.SettingCategory.SettingCategoryHelper;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

/**
 * Represents the `Model` type within the DikenEngine `game.nodes` package.
 */
public class Model extends Node {
	private static final long serialVersionUID = 1L;

	public Model() {
		this("Model", 0, 0);
	}

	public Model(String name) {
		this(name, 0, 0);
	}

	public Model(String name, int x, int y) {
		super(name, x, y);
	}

	@Override
	public Bitmap render() {
		return null;
	}
	
	@Override
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("model", "Model", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(7, 1));
		
		var settingCategory = SettingCategoryHelper.getOrCreateCategory(key, () -> SettingCategory
				.createSettingCategory(key));
		
		var list = super.getNodeSettings();
		list.add(settingCategory);
		return list;
	}

}
