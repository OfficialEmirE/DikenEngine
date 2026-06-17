package me.ramazanenescik04.diken.game.services;

import java.util.List;

import me.ramazanenescik04.diken.game.Instance;
import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.game.SettingCategory;
import me.ramazanenescik04.diken.gui.hitbox.Hitbox;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

public class Workspace extends Service {
	private static final long serialVersionUID = 1L;

	public Workspace() {
		this("Workspace");
	}

	public Workspace(String name) {
		super(name);
	}
	
	public void draw(Bitmap sceneBitmap, Hitbox viewport) {
		triggerEvent("OnPreRender");

        for (int i = 0; i < children.size(); i++) {
            Node child = children.get(i);
            if (child instanceof Instance childInstance && childInstance.isVisible()) {
				childInstance.draw(sceneBitmap, viewport);
			}
        }
        
        triggerEvent("OnPostRender");
	}

	@Override
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("workspace", "Workspace", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(9, 1));
		
		var settingCategory = SettingCategory
				.createSettingCategory(key);
		
		var list = super.getNodeSettings();
		list.add(settingCategory);
		return list;
	}
}
