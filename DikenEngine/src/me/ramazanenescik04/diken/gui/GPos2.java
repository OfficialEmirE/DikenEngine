package me.ramazanenescik04.diken.gui;

import me.ramazanenescik04.diken.gui.hitbox.Hitbox;
import me.ramazanenescik04.diken.gui.hitbox.IHitbox;

/**
 * Represents the `GPos2` type within the DikenEngine `gui` package.
 */
public class GPos2 {
	public GPos x;
	public GPos y;
	
	public GPos2(double scaleX, int offsetX, double scaleY, int offsetY) {
		this.x = new GPos(scaleX, offsetX);
		this.y = new GPos(scaleY, offsetY);;
	}
	
	public GPos2(GPos x, GPos y) {
		this.x = x;
		this.y = y;
	}
	
	public IHitbox getGlobalPosition(int width, int height) {
		return new Hitbox(x.getGlobalPosition(width), y.getGlobalPosition(height), width, height);
	}

	public static class GPos {
		public double scale;
		public int offset;
		
		public GPos(double scale, int offset) {
			this.scale = scale;
			this.offset = offset;
		}
		
		public int getGlobalPosition(int length) {
			return (int) Math.round((length * scale) + offset);
		}
	}
}
