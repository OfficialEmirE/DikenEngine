package me.ramazanenescik04.diken.game.services;

import java.util.List;

import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.game.SettingCategory;
import me.ramazanenescik04.diken.gui.hitbox.Hitbox;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

public class Service extends Node {
	private static final long serialVersionUID = 1L;
	
	public Service() {
		this("Service");
	}
	
	public Service(String name) {
		super(name);
	}
	
	@Override
	public void setName(String name) {
		throw new IllegalAccessError("Servislere isim koyulamaz");
	}
	
	public void draw(Bitmap sceneBitmap, Hitbox viewport) {}

	@Override
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("service", "Service", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(8, 1));
		
		var settingCategory = SettingCategory
				.createSettingCategory(key);
		
		var list = super.getNodeSettings();
		list.add(settingCategory);
		return list;
	}
}
