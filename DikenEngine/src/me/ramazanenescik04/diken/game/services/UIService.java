package me.ramazanenescik04.diken.game.services;

import java.util.List;

import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.game.SettingCategory;
import me.ramazanenescik04.diken.gui.component.ScreenGui;
import me.ramazanenescik04.diken.gui.hitbox.Hitbox;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

public class UIService extends Service {
	private static final long serialVersionUID = 1L;

	public UIService() {
		this("UI");
	}

	public UIService(String name) {
		super(name);
	}

	@Override
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("uiService", "UI", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(15, 1));
		
		var settingCategory = SettingCategory
				.createSettingCategory(key);
		
		var list = super.getNodeSettings();
		list.add(settingCategory);
		return list;
	}

	@Override
	public void draw(Bitmap sceneBitmap, Hitbox viewport) {
		OnPreRender.FireEvent();

        for (int i = 0; i < children.size(); i++) {
            Node child = children.get(i);
            if (child instanceof ScreenGui childInstance) {
				childInstance.drawScreen(sceneBitmap, viewport);
			}
        }
        
        OnPostRender.FireEvent();
	}

	public void keyHandled(int inputMode, int key, char character) {
		for (int i = 0; i < children.size(); i++) {
            Node child = children.get(i);
            if (child instanceof ScreenGui childInstance) {
				childInstance.keyHandled(inputMode, key, character);
			}
        }
	}

	public void mouseHandled(int inputMode, int x, int y, int clicked) {
		for (int i = 0; i < children.size(); i++) {
            Node child = children.get(i);
            if (child instanceof ScreenGui childInstance) {
				childInstance.mouseHandled(inputMode, x, y, clicked);
			}
        }
	}
}
