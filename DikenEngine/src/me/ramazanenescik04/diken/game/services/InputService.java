package me.ramazanenescik04.diken.game.services;

import java.util.List;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.SettingCategory;
import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.input.InputHandler;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

public class InputService extends Service {
	private static final long serialVersionUID = 6035548334767273241L;
	
	private UIService uiService;
	
	public InputService() {
		this("InputService");
	}
	
	public InputService(String name) {
		super(name);
	}

	@Override
	public void update(World world, DikenEngine engine) {
		super.update(world, engine);
		
		if (uiService == null)
			uiService = world.getService(UIService.class);
	}

	@Override
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("inputService", "InputService", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(0, 3));
		
		var settingCategory = SettingCategory
				.createSettingCategory(key);
		
		var list = super.getNodeSettings();
		list.add(settingCategory);
		return list;
	}

	public void keyHandled(int inputMode, int key, char character) {
		if (uiService != null) {
			uiService.keyHandled(inputMode, key, character);
		}
		
		triggerEvent("OnKeyHandled", inputMode, key, character);
		
		if (inputMode == InputHandler.INPUT_PRESSED || inputMode == InputHandler.INPUT_REPEATED) {
			triggerEvent("OnKeyDown", key, character);
		}
	}
	
	public void mouseHandled(int inputMode, int x, int y, int clicked) {
		if (uiService != null) {
			uiService.mouseHandled(inputMode, x, y, clicked);
		}
		
		triggerEvent("OnMouseHandled", inputMode, x, y, clicked);
		
		if (inputMode == InputHandler.INPUT_PRESSED) {
			triggerEvent("OnMouseCkicked", x, y, clicked);
		}
	}
}
