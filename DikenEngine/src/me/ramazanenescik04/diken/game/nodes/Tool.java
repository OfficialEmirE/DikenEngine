package me.ramazanenescik04.diken.game.nodes;

import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.game.entity.Player;
import me.ramazanenescik04.diken.gui.Hitbox;
import me.ramazanenescik04.diken.resource.Bitmap;

public class Tool extends Node {
	private static final long serialVersionUID = -3315727912522278757L;
	
	private transient Bitmap icon;
	
	public Bitmap getIcon() {
		return icon;
	}
	
	public void setIcon(Bitmap icon) {
		this.icon = icon;
	}

	public Tool() {
		super("Tool");
		init();
	}

	public Tool(String name) {
		super(name);
		init();
	}

	public Tool(String name, int x, int y) {
		super(name, x, y);
		init();
	}
	
	private void init() {
		this.aabb = new Hitbox(0, 0, 16, 16);
		this.setSolid(false);
		this.setAnchored(false);
	}
	
	public void onCollision(Node other) {
		if (other instanceof Player player) {
			player.addTool(this);
			this.parent.removeChild(this);
		}
	}

	@Override
	public Bitmap render() {
		return null;
	}

}
