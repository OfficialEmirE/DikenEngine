package me.ramazanenescik04.diken.game.services;

import java.util.List;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.SettingCategory;
import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.game.event.Event;
import me.ramazanenescik04.diken.input.InputHandler;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

public class InputService extends Service {
	private static final long serialVersionUID = 6035548334767273241L;
	
	// Keyboard listeners
	public final Event OnKeyHandled = new Event();
	public final Event OnKeyDown = new Event();
		
	// Mouse listeners
	public final Event OnMouseHandled = new Event();
	public final Event OnMouseClicked = new Event();
	
	private transient UIService uiService;
	private transient DikenEngine engine;
	
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
		
		if (this.engine == null)
			this.engine = engine;
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
	
	public boolean isKeyDown(int key) {
		return engine.input.isKeyDown(key);
	}
	
	public boolean isKeyPressed(int key) {
		return engine.input.isKeyPressed(key);
	}
	
	public boolean isKeyReleased(int key) {
		return engine.input.isKeyReleased(key);
	}

	public void keyHandled(int inputMode, int key, char character) {
		if (uiService != null) {
			uiService.keyHandled(inputMode, key, character);
		}
		
		OnKeyHandled.FireEvent(inputMode, key, character);
		
		if (inputMode == InputHandler.INPUT_PRESSED || inputMode == InputHandler.INPUT_REPEATED) {
			OnKeyDown.FireEvent(key, character);
		}
	}
	
	public void mouseHandled(int inputMode, int x, int y, int clicked) {
		if (uiService != null) {
			uiService.mouseHandled(inputMode, x, y, clicked);
		}
		
		OnMouseHandled.FireEvent(inputMode, x, y, clicked);
		
		if (inputMode == InputHandler.INPUT_PRESSED) {
			OnMouseClicked.FireEvent(x, y, clicked);
		}
	}
}
