package me.ramazanenescik04.diken.game.nodes;

import me.ramazanenescik04.diken.resource.Bitmap;

public class Decal extends ImageNode {
	private static final long serialVersionUID = 1L;

	public Decal() {
		super("Decal");
	}
	
	public Decal(String name) {
		super(name);
	}

	@Override
	public Bitmap render() {
		Bitmap parentBitmap = this.parent.render();
		return texture.resize(parentBitmap.w, parentBitmap.h);
	}

}
