package me.ramazanenescik04.diken.game.nodes;

import java.util.List;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.Instance;
import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.game.event.Event;
import me.ramazanenescik04.diken.game.io.Tag.Compound;
import me.ramazanenescik04.diken.game.services.InputService;
import me.ramazanenescik04.diken.game.setting.EnumSettingType;
import me.ramazanenescik04.diken.game.setting.Setting;
import me.ramazanenescik04.diken.game.setting.SettingCategory;
import me.ramazanenescik04.diken.gui.hitbox.Hitbox;
import me.ramazanenescik04.diken.tools.Utils;

public class ClickDetector extends Node {	
	public final Event OnClicked = new Event();
	private String previousCursor = "diken:cursor-3";
	private Hitbox hitbox = null;
	private boolean active = true;

	public ClickDetector() {
		this("ClickDetector");
	}
	
	public ClickDetector(String name) {
		super(name);
	}

	@Override
	public void update(World world, DikenEngine engine) {
		super.update(world, engine);
		
		if (this.parent != null && this.parent instanceof Instance insParent) {
			this.hitbox = insParent.getGlobalAABB();
		}
		
		var inputService = world.getService(InputService.class);
		var mousePosition = inputService.getMousePosition();
		if (isTouching(world, mousePosition.x, mousePosition.y)) {
			var currentCursor = inputService.getCursor();

			if (previousCursor != null && !currentCursor.equals("diken:cursor-4")) {
				previousCursor = currentCursor;
			}
			inputService.setCursor("diken:cursor-4");
		} else {
			if (previousCursor == null)
				previousCursor = "diken:cursor-3";
				
			inputService.setCursor(previousCursor);
		}
	}
	
	public boolean isTouching(World world, int x, int y) {
		var worldPos = world.screenToWorld(x, y);
		var hitbox = new Hitbox((int)worldPos.x, (int)worldPos.y, 1, 1);
		
		return this.hitbox != null && this.hitbox.intersects(hitbox) && active;
	}
	
	public void isClicked(World world, int mouseX, int mouseY) {
		if (isTouching(world, mouseX, mouseY)) {
			OnClicked.FireEvent();
		}
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	@Override
	public List<SettingCategory> getNodeSettings() {
		return Utils.listAdd(super.getNodeSettings(), SettingCategory.createSettingCategory("clickDetector", "ClickDetector", 7, 2)
				.addSetting(new Setting<>("Enable", active, Boolean.class, EnumSettingType.CHECK_BOX)
						.addChangeListener(this::setActive)));
	}

	@Override
	public void saveNodeData(Compound tag) {
		super.saveNodeData(tag);
		tag.putBoolean("enabled", this.hitbox.isActive());
	}

	@Override
	public void loadNodeData(Compound tag) {
		super.loadNodeData(tag);
		this.hitbox.setActive(tag.getBoolean("enabled", true));
	}
}
