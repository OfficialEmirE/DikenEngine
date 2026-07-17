package me.ramazanenescik04.diken.game.services;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import me.ramazanenescik04.diken.game.EnumSettingType;
import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.game.setting.Setting;
import me.ramazanenescik04.diken.game.setting.SettingCategory;
import me.ramazanenescik04.diken.gui.component.GuiComponent;
import me.ramazanenescik04.diken.gui.component.ScreenGui;
import me.ramazanenescik04.diken.gui.hitbox.Hitbox;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

public class UIService extends Service {
	private boolean allowDrawBitmap;

	public UIService() {
		this("UI");
	}

	public UIService(String name) {
		super(name);
	}

	public UIService(DataInputStream in) throws IOException {
		super(in);
		loadNodeData(in);
	}
	
	@Override
	public boolean showStudio() {
		return true;
	}

	@Override
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("uiService", "UI", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(15, 1));
		
		var settingCategory = SettingCategory
				.createSettingCategory(key)
				.addSetting(new Setting<>("Allow Draw Bitmap", this.allowDrawBitmap, Boolean.class, EnumSettingType.CHECK_BOX));
		
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
            if (child instanceof ScreenGui screenGui) {
				screenGui.drawScreen(sceneBitmap, viewport, allowDrawBitmap);
			} else if (child instanceof GuiComponent guiComponent) {
				guiComponent.drawComponent(sceneBitmap, viewport);
			}
        }
        
        OnPostRender.FireEvent();
	}

	public void keyHandled(int inputMode, int key, char character) {
		for (int i = 0; i < children.size(); i++) {
            Node child = children.get(i);
            if (child instanceof ScreenGui screenGui) {
				screenGui.keyHandled(inputMode, key, character);
			}
        }
	}

	public void mouseHandled(int inputMode, int x, int y, int clicked) {
		for (int i = 0; i < children.size(); i++) {
            Node child = children.get(i);
            if (child instanceof ScreenGui screenGui) {
				screenGui.mouseHandled(inputMode, x, y, clicked);
			}
        }
	}

	@Override
	public void saveNodeData(DataOutputStream out) throws IOException {
		super.saveNodeData(out);
		
		out.writeBoolean(allowDrawBitmap);
	}

	@Override
	public void loadNodeData(DataInputStream in) throws IOException {
		super.loadNodeData(in);
		
		this.allowDrawBitmap = in.readBoolean();
	}
}
