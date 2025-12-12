package me.ramazanenescik04.diken.gui.compoment;

import java.awt.Point;
import java.util.List;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.InputHandler;
import me.ramazanenescik04.diken.gui.Hitbox;
import me.ramazanenescik04.diken.gui.screen.IBackground;
import me.ramazanenescik04.diken.resource.Bitmap;

public class Panel extends GuiCompoment {
	private static final long serialVersionUID = 1L;
	private List<GuiCompoment> compoments;
	protected IBackground background;
	
	public boolean drawX = false;

	public Panel(int x, int y, int width, int height) {
		super(x, y, width, height);
		compoments = new java.util.ArrayList<GuiCompoment>();
	}
	
	public Panel() {
		this(0, 0, 100, 100);
	}

	public void add(GuiCompoment compoment) {
		if (compoment instanceof Panel) {
			Panel panel = (Panel) compoment;
			panel.init(DikenEngine.getEngine());
		}
		this.compoments.add(compoment);
	}
	
	public void remove(GuiCompoment compoment) {
		this.compoments.remove(compoment);
	}
	
	public void clear() {
		this.compoments.clear();
	}
	
	public void remove(int index) {
		this.compoments.remove(index);
	}
	
	public GuiCompoment get(int index) {
		return this.compoments.get(index);
	}
	
	public GuiCompoment get(Point point) {
		for (GuiCompoment compoment : this.compoments) {
			if (compoment.intersects(new Hitbox(point.x - this.x, point.y - this.y, 1, 1))) {
				return compoment;
			}
		}
		return null;
	}
	
	public int count() {
		return this.compoments.size();
	}

	public Bitmap render() {
		Bitmap bitmap = super.render();
		if (this.drawX) {
			bitmap.box(0, 0, width - 1, height - 1, 0xffffffff);
			bitmap.drawLine(0, 0, this.width, this.height, 0xffffffff, 1);
			bitmap.drawLine(this.width, 0, 0, this.height, 0xffffffff ,1);
		}
		
		if (this.background != null) {
			this.background.render(bitmap);
		}
		for (GuiCompoment compoment : this.compoments) {
			bitmap.draw(compoment.render(), compoment.x, compoment.y);
		}
		return bitmap;
	}

	public void tick(DikenEngine engine) {
		if (this.background != null) {
			this.background.tick();
		}
		for (GuiCompoment compoment : this.compoments) {
			compoment.tick(engine);
		}
	}

	public void keyPressed(char var1, int var2) {
		for (GuiCompoment compoment : this.compoments) {
			if (active)
				compoment.keyPressed(var1, var2);
		}
	}

	public void mouseClicked(int x, int y, int button, boolean isTouch2) {
		for (GuiCompoment compoment : this.compoments) {
			boolean isTouch = false;
			Hitbox rect = InputHandler.getMouseHitbox();
			Hitbox aarect = new Hitbox(rect.x - this.x, rect.y - this.y, rect.width, rect.height);
			if (compoment.intersects(aarect) && isTouch2) {
				isTouch = true;
			}
			
			if (active)
				compoment.mouseClicked(this.x - x, this.y - y, button, isTouch);
		}
	}

	public void setBackground(IBackground downBackground) {
		this.background = downBackground;
	}
	
	public IBackground getBackground() {
		return this.background;
	}
	
	public void init(DikenEngine engine) {
	}
	
	/**
	 * Returns the list of components in this panel.
	 * <br>
	 * This method provides access to the components contained within the panel.
	 * 
	 * @return List of GuiCompoment objects.
	 */
	public final List<GuiCompoment> getCompoments() {
		return compoments;
	}

	public void mouseGetInfo(int x, int y, boolean isTouch2) {
		for (GuiCompoment compoment : this.compoments) {
			Hitbox rect = InputHandler.getMouseHitbox();
			boolean isTouch = false;
			Hitbox aarect = new Hitbox(rect.x - this.x, rect.y - this.y, rect.width, rect.height);
			if (compoment.intersects(aarect) && isTouch2) {
				isTouch = true;
			}
			
			compoment.mouseGetInfo(this.x - x, this.y - y, isTouch);
		}
	}

	public boolean isVaild(GuiCompoment compoment) {
		for (int i = 0; i < compoments.size(); i++) {
			GuiCompoment custom = compoments.get(i);
			
			if (custom == compoment) {
				return true;
			}
		}
		return false;
	}
}
