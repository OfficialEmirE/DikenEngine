package me.ramazanenescik04.diken.game.services;

import java.util.List;

import me.ramazanenescik04.diken.game.EnumSettingType;
import me.ramazanenescik04.diken.game.Setting;
import me.ramazanenescik04.diken.game.SettingCategory;
import me.ramazanenescik04.diken.game.nodes.Sky;
import me.ramazanenescik04.diken.gui.hitbox.Hitbox;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

public class Lighting extends Service {
	private static final long serialVersionUID = 1L;
	
	private Sky sky;

	public Lighting() {
		this("Lighting");
	}

	public Lighting(String name) {
		super(name);
	}
	
	public void draw(Bitmap sceneBitmap, Hitbox viewport) {
		triggerEvent("OnPreRender");

		if (sky != null) {
			sky.draw(sceneBitmap, viewport);
		}
        
        triggerEvent("OnPostRender");
	}
	
	public void setSky(Sky sky) {
		this.sky = sky;
	}
	
	public Sky getSky() {
		return sky;
	}

	@Override
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("lighting", "Lighting", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(10, 1));
		
		var settingCategory = SettingCategory
				.createSettingCategory(key)
				.addSetting(new Setting<Sky>("Sky", sky, Sky.class, EnumSettingType.OBJECT_SELECT).addChangeListener(this::setSky));
		
		var list = super.getNodeSettings();
		list.add(settingCategory);
		return list;
	}

}
