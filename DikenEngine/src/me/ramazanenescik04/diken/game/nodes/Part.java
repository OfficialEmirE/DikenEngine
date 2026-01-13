package me.ramazanenescik04.diken.game.nodes;

import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.gui.Hitbox;
import me.ramazanenescik04.diken.resource.Bitmap;

public class Part extends Node {	
	private static final long serialVersionUID = 4072578864221886901L;
	
	public Part() {
		super();
		this.name = "Part";
		this.aabb = new Hitbox(0, 0, 16, 16);
		this.setAnchored(true);
	}

	public Part(int x, int y, int width, int height) {
		super("Part", x, y);
		this.aabb = new Hitbox(0, 0, width, height);
		this.setAnchored(true);
	}
	
	public Part(int x, int y) {
		super("Part", x, y);
		this.aabb = new Hitbox(0, 0, 16, 16);
		this.setAnchored(true);
	}
	
	public Bitmap render() {
		Bitmap bitmap = new Bitmap(this.aabb.width, this.aabb.height);
		bitmap.clear(color);
		return bitmap;
	}
}
