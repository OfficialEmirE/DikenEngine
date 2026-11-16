package me.ramazanenescik04.diken.game.nodes;

import java.util.List;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.Instance;
import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.game.event.Event;
import me.ramazanenescik04.diken.game.setting.SettingCategory;
import me.ramazanenescik04.diken.gui.hitbox.Hitbox;

public class ClickDetector extends Node {	
	public final Event OnClicked = new Event();
	private Hitbox hitbox = new Hitbox(0, 0, 16, 16);

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
			hitbox.setBounds(this.getGlobalX(), this.getGlobalY(), insParent.getAABBWidth(), insParent.getAABBHeight());
		}		
	}
	
	public Hitbox getCollision() {
		return hitbox;
	}

	@Override
	public List<SettingCategory> getNodeSettings() {
		return super.getNodeSettings();
	}
}
