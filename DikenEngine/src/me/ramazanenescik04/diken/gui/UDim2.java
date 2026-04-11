package me.ramazanenescik04.diken.gui;

import me.ramazanenescik04.diken.gui.hitbox.Hitbox;
import me.ramazanenescik04.diken.gui.hitbox.IHitbox;

/**
 * Represents the `GPos2` type within the DikenEngine `gui` package.
 */
public class UDim2 {
	public UDim x;
	public UDim y;
	
	public UDim2(double scaleX, int offsetX, double scaleY, int offsetY) {
		this.x = new UDim(scaleX, offsetX);
		this.y = new UDim(scaleY, offsetY);
	}
	
	public UDim2(UDim x, UDim y) {
		this.x = x;
		this.y = y;
	}
	
	public IHitbox getGlobalPosition(int width, int height) {
		return new Hitbox(x.getGlobalPosition(width), y.getGlobalPosition(height), width, height);
	}

	public static class UDim {
		public double scale;
		public int offset;
		
		public UDim(double scale, int offset) {
			this.scale = scale;
			this.offset = offset;
		}
		
		public int getGlobalPosition(int length) {
			return (int) Math.round((length * scale) + offset);
		}
	}
}
