package me.ramazanenescik04.diken.gui.compoment;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.gui.hitbox.Hitbox;
import me.ramazanenescik04.diken.resource.Bitmap;

/**
 * Represents the `GuiComponent` type within the DikenEngine `gui.compoment` package.
 */
public abstract class GuiComponent extends Hitbox {
	private static final long serialVersionUID = 1L;
	
	protected GuiComponent parent = null; //default
	protected boolean visible = true;
	
	public int getGlobalX() {
	    return (parent != null) ? parent.getGlobalX() + this.x : this.x;
	}

	public int getGlobalY() {
	    return (parent != null) ? parent.getGlobalY() + this.y : this.y;
	}

	public GuiComponent(int x, int y, int width, int height) {
		super(x, y, width, height);
	}
	
	public Bitmap render() {
		return new Bitmap(width, height);
	}
	
	public void tick(DikenEngine engine) {
	}
	
	public void keyPressed(char var1, int var2) {
	}
	
	public void mouseClicked(int x, int y, int button, boolean isTouch) {
	}
	
	public void mouseGetInfo(int x, int y, boolean isTouch) {
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}
	
	public boolean isVisible() {
		return visible;
	}

	@Override
	public boolean equals(Object obj) {
		return this == obj;
	}
}
