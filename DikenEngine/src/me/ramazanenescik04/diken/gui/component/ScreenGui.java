package me.ramazanenescik04.diken.gui.component;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.game.setting.EnumSettingType;
import me.ramazanenescik04.diken.game.setting.Setting;
import me.ramazanenescik04.diken.game.setting.SettingCategory;
import me.ramazanenescik04.diken.gui.hitbox.Hitbox;
import me.ramazanenescik04.diken.input.InputHandler;
import me.ramazanenescik04.diken.renderer.FrameBitmapPool;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;
import me.ramazanenescik04.diken.scripting.LuaDoc;

public class ScreenGui extends Node {
	private boolean enabled = true;
	private transient final List<DrawList> drawList = new ArrayList<>();
	
	public ScreenGui() {
		this("ScreenGui");
	}
	
	public ScreenGui(String name) {
		super(name);
	}

	public ScreenGui(DataInputStream in) throws IOException {
		super(in);
		loadNodeData(in);
	}
	
	public void drawScreen(Bitmap sceneBitmap, Hitbox viewport, boolean allowDrawBitmap) {
		OnPreRender.FireEvent();
        if (!enabled) return;
        
        List<Node> sortedChildren = new ArrayList<>(children);
    	sortedChildren.sort(Comparator.comparingInt(Node::getZIndex));
    	
    	if (allowDrawBitmap) {
    		drawList.forEach(e -> {
        		sceneBitmap.draw(e.bitmap, e.x, e.y);
        	});
    	}   	
    	drawList.clear();
        
        for (int i = 0; i < sortedChildren.size(); i++) {
            Node child = sortedChildren.get(i);
            if (child instanceof GuiComponent childInstance) {
				childInstance.drawComponent(sceneBitmap, viewport);
			}
        }
        
        OnPostRender.FireEvent();
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
		for (var descendant : this.getDescendants()) {
			if (descendant instanceof GuiComponent component) {
				if (!(component.isActive() || component.isVisible())) continue;
				var hitbox = component.getAbsoluteBounds();
				
				component.mouseGetInfo(x - hitbox.getX(), y - hitbox.getY(), hitbox.intersects(new Hitbox(x, y)));
			}
		}
	}
	
	private void sendMouseClicked(int x, int y, int clicked) {
		for (var descendant : this.getDescendants()) {
			if (descendant instanceof GuiComponent component) {
				if (!(component.isActive() || component.isVisible())) continue;
				var hitbox = component.getAbsoluteBounds();
				
				component.mouseClicked(x - hitbox.getX(), y - hitbox.getY(), clicked, hitbox.intersects(new Hitbox(x, y)));
			}
		}
	}
	
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	@LuaDoc
	public Bitmap create(int width, int height) {
		return new Bitmap(width, height);
	}
	
	@LuaDoc(description = "! ONLY USE YOUR RENDER SYSTEMS !")
	public Bitmap createFramePool(int width, int height) {
		return FrameBitmapPool.newBitmap(width, height);
	}
	
	public void drawBitmap(Bitmap btp, int x, int y) {
		this.drawList.add(new DrawList(btp, x, y));
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

	@Override
	public void saveNodeData(DataOutputStream out) throws IOException {
		super.saveNodeData(out);
		out.writeBoolean(enabled);
	}

	@Override
	public void loadNodeData(DataInputStream in) throws IOException {
		super.loadNodeData(in);
		this.enabled = in.readBoolean();
	}
	
	private record DrawList(Bitmap bitmap, int x, int y) {}
}
