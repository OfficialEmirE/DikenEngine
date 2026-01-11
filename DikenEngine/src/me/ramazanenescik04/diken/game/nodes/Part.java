package me.ramazanenescik04.diken.game.nodes;

import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.gui.Hitbox;
import me.ramazanenescik04.diken.resource.Bitmap;

public abstract class Part extends Node {
	public int color = 0xFFFFFFFF;
	
	public Part(int x, int y, int width, int height) {
		super("Part", x, y);
		this.aabb = new Hitbox(0, 0, width, height);
		this.isStatic = true;
	}
	
	public Part(int x, int y) {
		super("Part", x, y);
		this.aabb = new Hitbox(0, 0, 16, 16);
	}
	
	public Bitmap render() {
		Bitmap bitmap = new Bitmap(this.aabb.width, this.aabb.height);
		bitmap.clear(color);
		return bitmap;
	}
}
