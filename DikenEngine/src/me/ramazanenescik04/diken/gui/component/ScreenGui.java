package me.ramazanenescik04.diken.gui.component;

import java.util.List;

import me.ramazanenescik04.diken.game.EnumSettingType;
import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.game.Setting;
import me.ramazanenescik04.diken.game.SettingCategory;
import me.ramazanenescik04.diken.gui.hitbox.Hitbox;
import me.ramazanenescik04.diken.input.InputHandler;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

public class ScreenGui extends Node {
	private static final long serialVersionUID = -2143812617894766479L;
	
	private boolean enabled = true;
	
	public ScreenGui() {
		this("ScreenGui");
	}
	
	public ScreenGui(String name) {
		super(name);
	}
	
	public void drawScreen(Bitmap sceneBitmap, Hitbox viewport) {
		triggerEvent("OnPreRender");
        if (!enabled) return;
        
        for (int i = 0; i < children.size(); i++) {
            Node child = children.get(i);
            if (child instanceof GuiComponent childInstance) {
				childInstance.drawComponent(sceneBitmap, viewport);
			}
        }
        
        triggerEvent("OnPostRender");
	}
	
	public void keyHandled(int inputMode, int key, char character) {
		if (!enabled) return;
		
		if (inputMode == InputHandler.INPUT_PRESSED || inputMode == InputHandler.INPUT_REPEATED) {
			for (var descendant : this.getDescendants()) {
				if (descendant instanceof GuiComponent component) {
					component.keyPressed(character, key);
				}
			}
		}
	}
	
	public void mouseHandled(int inputMode, int x, int y, int clicked) {
		if (!enabled) return;
		
		sendMousePosition(x, y, clicked);
		
		if (inputMode == InputHandler.INPUT_PRESSED) {
			sendMouseClicked(x, y, clicked);
		}
	}
	
	private void sendMousePosition(int x, int y, int clicked) {
	}
	
	private void sendMouseClicked(int x, int y, int clicked) {
	}
	
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	@Override
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("ScreenGui", "ScreenGui", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(15, 1));
		
		var settingCategory = SettingCategory
				.createSettingCategory(key)
				.addSetting(new Setting<Boolean>("Enabled", this.enabled, Boolean.class,
						EnumSettingType.CHECK_BOX).addChangeListener(this::setEnabled));
		
		var list = super.getNodeSettings();
		list.add(settingCategory);
		return list;
	}
}
