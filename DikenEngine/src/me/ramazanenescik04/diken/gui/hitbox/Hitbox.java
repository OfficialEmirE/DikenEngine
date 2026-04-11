package me.ramazanenescik04.diken.gui.hitbox;

/**
 * Represents the `Hitbox` type within the DikenEngine `gui` package.
 */
public class Hitbox implements java.io.Serializable, IHitbox {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7326677608083408652L;
	public int x, y, width, height;
	public boolean active = true;

	public Hitbox(Hitbox hitbox) {
		java.util.Objects.requireNonNull(hitbox);

		this.x = hitbox.x;
		this.y = hitbox.y;
		this.width = hitbox.width;
		this.height = hitbox.height;
	}

	public Hitbox(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;

		if (width < 1)
			width = 1;
		if (height < 1)
			height = 1;

		this.width = width;
		this.height = height;
	}

	public Hitbox(int x, int y) {
		this(x, y, 1, 1);
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		} else if (obj instanceof Hitbox h) {
			return (h.x == x && h.y == y &&  h.width == width && h.height == height);
		} else if (obj instanceof java.awt.Point p) {
			return this.contains(p.x, p.y);
		}
		return false;
	}

	@Override
	public boolean intersects(Hitbox r) {
		if (!active || !r.active)
			return false;

		// AABB collision
		return r.x < this.x + this.width && r.x + r.width > this.x && r.y < this.y + this.height
				&& r.y + r.height > this.y;
	}

	@Override
	public boolean contains(int px, int py) {
		if (!active)
			return false;
		return px >= x && px < x + width && py >= y && py < y + height;
	}

	@Override
	public boolean contains(Hitbox r) {
		if (!active || !r.active)
			return false;

		return r.x >= this.x && r.y >= this.y && r.x + r.width <= this.x + this.width
				&& r.y + r.height <= this.y + this.height;
	}

	@Override
	public IHitbox setActive(boolean active) {
        this.active = active;
        return this;
    }

	@Override
	public boolean isActive() {
		return active;
	}

	@Override
	public void setLocation(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public int getX() {
		return x;
	}

	@Override
	public void setX(int x) {
		this.x = x;
	}

	@Override
	public int getY() {
		return y;
	}

	@Override
	public void setY(int y) {
		this.y = y;
	};

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public void setWidth(int width) {
		this.width = width;
	}

	@Override
	public void setHeight(int height) {
		this.height = height;
	}

	@Override
	public void setBounds(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}

	@Override
	public void setSize(int width, int height) {
		this.width = width;
		this.height = height;
	}

	@Override
	public IHitbox getBounds() {
		return new Hitbox(this.x, this.y, this.width, this.height);
	}

	public String toString() {
		return this.getClass().getName() + "-["
				+ String.format("%h - %h, %h x %h", this.x, this.y, this.width, this.height) + "]";
	}
}
