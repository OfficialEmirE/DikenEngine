package me.ramazanenescik04.diken.game.nodes;

import me.ramazanenescik04.diken.resource.Bitmap;

public class Texture extends ImageNode {
	private static final long serialVersionUID = 1L;

	public Texture() {
		super("Texture");
	}
	
	public Texture(String name) {
		super(name);
	}
	
	@Override
	public Bitmap render() {
		Bitmap parentBitmap = this.parent.render();
		Bitmap thisBitmap = new Bitmap(parentBitmap.w, parentBitmap.h);
		for (var y = 0; y < (parentBitmap.h / texture.h) + 1; y++) {
			for (var x = 0; x < (parentBitmap.w / texture.w) + 1; x++) {
				thisBitmap.blendDraw(texture, x * texture.w, y * texture.h, this.color);
			}
		}
		return thisBitmap;
	}

}
