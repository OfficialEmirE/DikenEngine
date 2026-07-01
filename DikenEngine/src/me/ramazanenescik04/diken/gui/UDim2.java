package me.ramazanenescik04.diken.gui;

import me.ramazanenescik04.diken.gui.hitbox.Hitbox;
import me.ramazanenescik04.diken.gui.hitbox.IHitbox;

/**
 * Represents the `GPos2` type within the DikenEngine `gui` package.
 */
public class UDim2 implements java.io.Serializable, Cloneable {
	private static final long serialVersionUID = 7092023545513922157L;
	
	// Hızlı kullanmak için :D
	public static final UDim2 defaultV = new UDim2(0, 32, 0, 16);
	public static final UDim2 fullscreen = new UDim2(1, 0, 1, 0);
	public static final UDim2 zero = new UDim2(0, 0, 0, 0);
	
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
	
	@Override
	public String toString() {
		return x.toString() + ", " + y.toString();
	}

	@Override
	public UDim2 clone() throws CloneNotSupportedException {
		UDim2 cloned = (UDim2) super.clone();
		
		cloned.x = this.x.clone();
		cloned.y = this.y.clone();
		
		return cloned;
	}

	public static class UDim implements java.io.Serializable, Cloneable {
		private static final long serialVersionUID = 4832816613298833197L;
		
		public double scale;
		public int offset;
		
		public UDim(double scale, int offset) {
			this.scale = scale;
			this.offset = offset;
		}
		
		public int getGlobalPosition(int length) {
			return (int) Math.round((length * scale) + offset);
		}
		
		@Override
		public String toString() {
			return "{" + scale + ", " + offset + "}";
		}
		
		@Override
		public UDim clone() {
			try {
				return (UDim) super.clone();
			} catch (CloneNotSupportedException e) {
				throw new AssertionError();
			}
		}
	}
}
