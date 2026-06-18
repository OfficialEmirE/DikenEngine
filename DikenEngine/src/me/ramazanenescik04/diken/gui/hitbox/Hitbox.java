package me.ramazanenescik04.diken.gui.hitbox;

/**
 * Represents the `Hitbox` type within the DikenEngine `gui` package.
 */
public class Hitbox implements java.io.Serializable, IHitbox {
	private static final long serialVersionUID = 7326677608083408652L;
	protected int x, y, width, height;
	protected float rotation;
	protected boolean active = true;

	public Hitbox(Hitbox hitbox) {
		java.util.Objects.requireNonNull(hitbox);

		this.x = hitbox.x;
		this.y = hitbox.y;
		this.width = hitbox.width;
		this.height = hitbox.height;
		this.rotation = hitbox.rotation;
	}

	public Hitbox(int x, int y, int width, int height) {
		this.x = x;  this.y = y;
	    this.width  = Math.max(1, width);
	    this.height = Math.max(1, height);
	}

	public Hitbox(int x, int y) { this(x, y, 1, 1); }
	
	public double[][] getCorners() {
        double cx = x + width  / 2.0;
        double cy = y + height / 2.0;
        double rad = Math.toRadians(rotation);
        double cos = Math.cos(rad), sin = Math.sin(rad);

        double hw = width  / 2.0;
        double hh = height / 2.0;

        // Yerel köşeler → döndür → dünya koordinatlarına taşı
        double[][] local = { {-hw,-hh}, {hw,-hh}, {hw,hh}, {-hw,hh} };
        double[][] world = new double[4][2];
        for (int i = 0; i < 4; i++) {
            world[i][0] = cx + local[i][0] * cos - local[i][1] * sin;
            world[i][1] = cy + local[i][0] * sin + local[i][1] * cos;
        }
        return world;
    }
	
	private static double[] project(double[][] corners, double[] axis) {
        double min = Double.MAX_VALUE, max = -Double.MAX_VALUE;
        for (double[] c : corners) {
            double dot = c[0] * axis[0] + c[1] * axis[1];
            if (dot < min) min = dot;
            if (dot > max) max = dot;
        }
        return new double[]{ min, max };
    }
	
	private static boolean overlaps(double[] a, double[] b) {
        return a[0] <= b[1] && b[0] <= a[1];
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
        if (!active || !r.active) return false;

        // Rotasyon yoksa eski AABB hızlı yolunu kullan
        if (rotation == 0f && r.rotation == 0f) {
            return r.x < x + width  && r.x + r.width  > x
                && r.y < y + height && r.y + r.height > y;
        }

        double[][] cornersA = this.getCorners();
        double[][] cornersB = r.getCorners();

        // SAT: her iki dikdörtgenin 2'şer ekseni = 4 eksen toplam
        double[][] axes = getSATAxes(cornersA, cornersB);
        for (double[] axis : axes) {
            double len = Math.sqrt(axis[0]*axis[0] + axis[1]*axis[1]);
            if (len < 1e-9) continue;
            axis[0] /= len;  axis[1] /= len; // normalize

            double[] pA = project(cornersA, axis);
            double[] pB = project(cornersB, axis);
            if (!overlaps(pA, pB)) return false; // ayrılık ekseni bulundu
        }
        return true; // hiç ayrılık ekseni yok → çarpışma var
    }

    /** OBB köşelerinden 4 kenar normalini döner */
    private static double[][] getSATAxes(double[][] cA, double[][] cB) {
        double[][] axes = new double[4][2];
        // A'nın iki ekseni
        axes[0] = new double[]{ cA[1][0]-cA[0][0], cA[1][1]-cA[0][1] };
        axes[1] = new double[]{ cA[3][0]-cA[0][0], cA[3][1]-cA[0][1] };
        // B'nin iki ekseni
        axes[2] = new double[]{ cB[1][0]-cB[0][0], cB[1][1]-cB[0][1] };
        axes[3] = new double[]{ cB[3][0]-cB[0][0], cB[3][1]-cB[0][1] };
        return axes;
    }

	@Override
	public boolean contains(int px, int py) {
        if (!active) return false;
        if (rotation == 0f)
            return px >= x && px < x+width && py >= y && py < y+height;

        double cx = x + width  / 2.0;
        double cy = y + height / 2.0;
        double rad = Math.toRadians(-rotation); // ters döndür
        double cos = Math.cos(rad), sin = Math.sin(rad);

        double lx = (px - cx) * cos - (py - cy) * sin;
        double ly = (px - cx) * sin + (py - cy) * cos;

        return lx >= -width/2.0 && lx < width/2.0
            && ly >= -height/2.0 && ly < height/2.0;
    }

	@Override
	public boolean contains(Hitbox r) {
        if (!active || !r.active) return false;
        // r'nin 4 köşesinin hepsi this içinde mi?
        for (double[] corner : r.getCorners()) {
            if (!contains((int) Math.round(corner[0]), (int) Math.round(corner[1])))
                return false;
        }
        return true;
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
	
	public float getRotation() { return rotation; }

    public Hitbox setRotation(float degrees) {
        this.rotation = degrees % 360f;
        return this;
    }

    public Hitbox rotate(float deltaDegrees) {
        return setRotation(this.rotation + deltaDegrees);
    }

	public String toString() {
		return this.getClass().getName() + "-["
				+ String.format("%h - %h, %h x %h", this.x, this.y, this.width, this.height) + "]";
	}
}
