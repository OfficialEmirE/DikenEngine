package me.ramazanenescik04.diken.game.services;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.game.setting.SettingCategory;
import me.ramazanenescik04.diken.gui.component.ScreenGui;
import me.ramazanenescik04.diken.gui.hitbox.Hitbox;
import me.ramazanenescik04.diken.resource.Bitmap;

public class CoreUIService extends Service {
	
	public CoreUIService() {
		this("CoreUI");
	}

	public CoreUIService(String name) {
		super(name);
	}

	public CoreUIService(DataInputStream in) throws IOException {
		super(in);
		loadNodeData(in);
	}
	
	@Override
	public boolean showStudio() {
		return false;
	}

	@Override
	public List<SettingCategory> getNodeSettings() {
		var settingCategory = SettingCategory
				.createSettingCategory("coreUiService", "CoreUI", 3, 3);
		
		var list = super.getNodeSettings();
		list.add(settingCategory);
		return list;
	}

	@Override
	public void draw(Bitmap sceneBitmap, Hitbox viewport) {
		OnPreRender.FireEvent();
		
		List<Node> sortedChildren = new ArrayList<>(children);
    	sortedChildren.sort(Comparator.comparingInt(Node::getZIndex));

        for (int i = 0; i < sortedChildren.size(); i++) {
            Node child = sortedChildren.get(i);
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
