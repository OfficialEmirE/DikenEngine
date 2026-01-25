package me.ramazanenescik04.diken.gui.compoment;

import java.awt.Point;
import java.util.List;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.gui.Hitbox;
import me.ramazanenescik04.diken.gui.screen.IBackground;
import me.ramazanenescik04.diken.resource.Bitmap;

public class Panel extends GuiComponent {
	private static final long serialVersionUID = 1L;
	private List<GuiComponent> compoments;
	
	protected IBackground background;
	
	public boolean drawX = false;

	public Panel(int x, int y, int width, int height) {
		super(x, y, width, height);
		compoments = new java.util.ArrayList<GuiComponent>();
	}
	
	public Panel() {
		this(0, 0, 100, 100);
	}

	public void add(GuiComponent compoment) {
		if (compoment == null) return;
		
		compoment.parent = this;
		if (compoment instanceof Panel panel) {
			panel.init(DikenEngine.getEngine());
		}
		this.compoments.add(compoment);
	}
	
	public void remove(GuiComponent compoment) {
		if (compoment == null) return;
		
		compoment.parent = null;
		this.compoments.remove(compoment);
	}
	
	public void clear() {
		this.compoments.forEach(e -> {
			if (e instanceof Panel panel) {
				panel.parent = null;
			}
		});
		
		this.compoments.clear();
	}
	
	public void remove(int index) {
		GuiComponent compoment = this.compoments.remove(index);
		
		if (compoment != null) {
			parent = null;
		}
	}
	
	public GuiComponent get(int index) {
		return this.compoments.get(index);
	}
	
	public GuiComponent get(Point point) {
		for (int i = 0; i < this.compoments.size(); i++) {
			GuiComponent compoment = this.compoments.get(i);
			if (compoment == null) {
				continue;
			}
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
		for (int i = 0; i < this.compoments.size(); i++) {
			GuiComponent compoment = this.compoments.get(i);
			if (compoment != null && compoment.visible) {
				bitmap.draw(compoment.render(), compoment.x, compoment.y);
			}
		}
		return bitmap;
	}

	public void tick(DikenEngine engine) {
		if (this.background != null) {
			this.background.tick();
		}
		
		for (int i = 0; i < this.compoments.size(); i++) {
			GuiComponent compoment = this.compoments.get(i);
			if (engine != null && compoment != null) {
				compoment.tick(engine);
			}
		}
	}

	public void keyPressed(char var1, int var2) {
		for (int i = 0; i < this.compoments.size(); i++) {
			GuiComponent compoment = this.compoments.get(i);
			if (active && compoment != null)
				compoment.keyPressed(var1, var2);
		}
	}

	public void mouseClicked(int relMouseX, int relMouseY, int button, boolean isTouch2) {
	    boolean claimed = false;
	    
	    // HATAYI ÖNLEYEN KRİTİK NOKTA: Listenin o anki elemanlarını yeni bir diziye alıyoruz.
	    // Böylece döngü sırasında compoments listesine ekleme/çıkarma yapılsa bile döngü patlamaz.
	    GuiComponent[] tempElements = this.compoments.toArray(new GuiComponent[0]);

	    // Tersten dönmeye devam (En üstteki bileşen için)
	    for (int i = tempElements.length - 1; i >= 0; i--) {
	        GuiComponent compoment = tempElements[i];
	        if (compoment == null) {
				continue;
			}

	        boolean isHovered = (relMouseX >= compoment.x && relMouseX <= compoment.x + compoment.width) &&
	                            (relMouseY >= compoment.y && relMouseY <= compoment.y + compoment.height);

	        if (!claimed && isHovered) {
	            if (active) {
	                compoment.mouseClicked(relMouseX - compoment.x, relMouseY - compoment.y, button, isTouch2);
	            }
	            claimed = true; 
	        } else {
	            // Focus kaybetme mantığı burada çalışmaya devam eder
	            compoment.mouseClicked(-1, -1, button, false);
	        }
	    }
	}

	public void mouseGetInfo(int relMouseX, int relMouseY, boolean isTouch2) {
		for (int i = 0; i < this.compoments.size(); i++) {
			GuiComponent compoment = compoments.get(i);
			if (compoment == null) {
				continue;
			}
	        // Mantık burada da aynı: InputHandler yok, bağıl matematik var.
	        
	        boolean isHovered = (relMouseX >= compoment.x && relMouseX <= compoment.x + compoment.width) &&
	                            (relMouseY >= compoment.y && relMouseY <= compoment.y + compoment.height);

	        boolean isTouch = isHovered && isTouch2;
	        
	        compoment.mouseGetInfo(relMouseX - compoment.x, relMouseY - compoment.y, isTouch);
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
	public List<GuiComponent> getCompoments() {
		return compoments;
	}

	public boolean isVaild(GuiComponent compoment) {
		for (int i = 0; i < compoments.size(); i++) {
			GuiComponent custom = compoments.get(i);
			
			if (custom == compoment) {
				return true;
			}
		}
		return false;
	}
}
