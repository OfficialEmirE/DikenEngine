package me.ramazanenescik04.diken.gui;

public class Hitbox implements java.io.Serializable {
    /**
	 * 
	 */
	private static final long serialVersionUID = 7326677608083408652L;
	public int x, y, width, height;
    public boolean active = true;

    public Hitbox(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;

        if (width < 1) width = 1;
        if (height < 1) height = 1;

        this.width = width;
        this.height = height;
    }

    public Hitbox(int x, int y) {
        this(x, y, 1, 1);
    }

    public boolean intersects(Hitbox r) {
        if (!active || !r.active) return false;

        // AABB collision
        return r.x < this.x + this.width &&
               r.x + r.width > this.x &&
               r.y < this.y + this.height &&
               r.y + r.height > this.y;
    }

    public boolean contains(int px, int py) {
        if (!active) return false;
        return px >= x && px < x + width &&
               py >= y && py < y + height;
    }

    public boolean contains(Hitbox r) {
        if (!active || !r.active) return false;

        return r.x >= this.x &&
               r.y >= this.y &&
               r.x + r.width <= this.x + this.width &&
               r.y + r.height <= this.y + this.height;
    }

    public Hitbox setActive(boolean active) {
        this.active = active;
        return this;
    }

    public boolean isActive() {
        return active;
    }
    
    public void setLocation(int x, int y) {
    	this.x = x;
    	this.y = y;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	};
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public void setWidth(int width) {
		this.width = width;
	}
	
	public void setHeight(int height) {
		this.height = height;
	}
	
	public void setBounds(int x, int y, int width, int height) {
		this.x = x;
    	this.y = y;
    	this.width = width;
    	this.height = height;
	}
	
	public void setSize(int width, int height) {
		this.width = width;
		this.height = height;
	}
}
