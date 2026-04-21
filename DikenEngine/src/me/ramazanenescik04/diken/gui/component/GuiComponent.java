package me.ramazanenescik04.diken.gui.component;

import java.util.*;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.gui.hitbox.Hitbox;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.FrameBitmapPool;

/**
 * Represents the `GuiComponent` type within the DikenEngine `gui.compoment` package.
 */
public abstract class GuiComponent extends Hitbox {
	private static final long serialVersionUID = 1L;
	
	protected GuiComponent parent = null; //default
	protected List<IGuiListener> listeners = new ArrayList<>();
	private Hitbox prevBounds;
	protected boolean visible = true;
	
	public int getGlobalX() {
	    return (parent != null) ? parent.getGlobalX() + this.x : this.x;
	}

	public int getGlobalY() {
	    return (parent != null) ? parent.getGlobalY() + this.y : this.y;
	}

	public GuiComponent(int x, int y, int width, int height) {
		super(x, y, width, height);
		
		prevBounds = (Hitbox) this.getBounds();
	}
	
	public Bitmap render() {
		return FrameBitmapPool.newBitmap(width, height);
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
	
	public void addGuiListener(IGuiListener listener) {
		listeners.add(listener);
	}
	
	public void removeGuiListener(IGuiListener listener) {
		if (listeners.contains(listener))
			listeners.remove(listener);
	}

	@Override
	public boolean equals(Object obj) {
		return this == obj;
	}
	
	public void checkListener() {
		if (!(prevBounds.equals(this))) {
			prevBounds = (Hitbox) this.getBounds();
			listeners.forEach(l -> {
				l.changedBounds(prevBounds);
				l.changedSize(prevBounds, prevBounds.width, prevBounds.height);
				l.changedLocation(prevBounds, prevBounds.x, prevBounds.y);
			});	
		}
	}
}
