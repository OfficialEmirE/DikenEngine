package me.ramazanenescik04.diken.gui.compoment;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.gui.Hitbox;
import me.ramazanenescik04.diken.resource.Bitmap;

public abstract class GuiComponent extends Hitbox implements java.io.Serializable {
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

}
