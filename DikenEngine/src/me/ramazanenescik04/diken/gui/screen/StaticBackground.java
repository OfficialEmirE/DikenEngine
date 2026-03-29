package me.ramazanenescik04.diken.gui.screen;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.resource.Bitmap;

/**
 * Represents the `StaticBackground` type within the DikenEngine `gui.screen` package.
 */
public class StaticBackground implements IBackground {
	
	private Bitmap bitmap;
	private DikenEngine engine;
	
	public StaticBackground(Bitmap bitmap) {
		this.bitmap = bitmap;
		engine = DikenEngine.getEngine();
	}
	
	public void render(Bitmap bitmap) {
		Bitmap bg = new Bitmap(bitmap.w, bitmap.h);
		for(int x = 0; x < (engine.getScaledWidth() / this.bitmap.w) + 1; x++) {
			for(int y = 0; y < (engine.getScaledHeight() / this.bitmap.h) + 1; y++) {
				bg.blendDraw(this.bitmap, x * this.bitmap.w, y * this.bitmap.h, 0xff7d7d7d);
			}
		}
		bitmap.draw(bg, 0, 0);
	}

	public void tick() {
	}

}
