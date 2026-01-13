package me.ramazanenescik04.diken.game.nodes;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.resource.Bitmap;

public class Sky extends Node {
	private static final long serialVersionUID = 9068692202217556542L;
	public transient Bitmap skyBitmap;
	public transient int width = 1, height = 1;

	public Sky() {
		super("Sky");
	}
	
	public Sky(int color) {
		super("Sky");
		this.color = color;
	}
	
	public Sky(Bitmap skyBitmap) {
		super("Sky");
		this.skyBitmap = skyBitmap;
	}

	@Override
	public Bitmap render() {
		Bitmap bitmap = new Bitmap(width, height);
		bitmap.clear(color);
		if (this.skyBitmap != null) {
			for (int y = 0; y < (bitmap.h / skyBitmap.h) + 1; y++) {
				for (int x = 0; x < (bitmap.w / skyBitmap.w) + 1; x++) {
					bitmap.blendDraw(skyBitmap, x * skyBitmap.w, y * skyBitmap.h, this.color);
				}
			}
		}
		return bitmap;
	}

	@Override
	public void update(World world, DikenEngine engine) {
		this.width = engine.getWidth() + 20;
		this.height = engine.getHeight() + 20;
		
		this.x = (int) world.camera.x() - 10;
		this.y = (int) world.camera.y() - 10;
		
		super.update(world, engine);
	}
}
