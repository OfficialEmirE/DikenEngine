package me.ramazanenescik04.diken.gui.compoment;

import java.awt.Point;
import java.util.List;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.gui.Hitbox;
import me.ramazanenescik04.diken.gui.screen.IBackground;
import me.ramazanenescik04.diken.resource.Bitmap;

public class Panel extends GuiCompoment {
	private static final long serialVersionUID = 1L;
	private List<GuiCompoment> compoments;
	protected Panel parent = null;
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
		if (compoment instanceof Panel panel) {
			panel.parent = this;
			panel.init(DikenEngine.getEngine());
		}
		this.compoments.add(compoment);
	}
	
	public void remove(GuiCompoment compoment) {
		if (compoment instanceof Panel panel) {
			panel.parent = null;
		}
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
		GuiCompoment compoment = this.compoments.remove(index);
		if (compoment != null && compoment instanceof Panel panel) {
			panel.parent = null;
		}
	}
	
	public GuiCompoment get(int index) {
		return this.compoments.get(index);
	}
	
	public GuiCompoment get(Point point) {
		for (int i = 0; i < this.compoments.size(); i++) {
			GuiCompoment compoment = this.compoments.get(i);
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
			GuiCompoment compoment = this.compoments.get(i);
			bitmap.draw(compoment.render(), compoment.x, compoment.y);
		}
		return bitmap;
	}

	public void tick(DikenEngine engine) {
		if (this.background != null) {
			this.background.tick();
		}
		
		for (int i = 0; i < this.compoments.size(); i++) {
			GuiCompoment compoment = this.compoments.get(i);
			compoment.tick(engine);
		}
	}

	public void keyPressed(char var1, int var2) {
		for (int i = 0; i < this.compoments.size(); i++) {
			GuiCompoment compoment = this.compoments.get(i);
			if (active)
				compoment.keyPressed(var1, var2);
		}
	}

	// Not: x ve y parametreleri, 'this' (şu anki panel) sol üst köşesine göre
	// farenin nerede olduğunu temsil etmeli.

	public void mouseClicked(int relMouseX, int relMouseY, int button, boolean isTouch2) {
	    // 1. DEĞİŞİKLİK: Listeyi TERSTEN dönüyoruz.
	    // Listenin sonundaki eleman ekranda en üsttedir. Önce onu kontrol etmeliyiz.
	    for (int i = this.compoments.size() - 1; i >= 0; i--) {
	        GuiCompoment compoment = compoments.get(i);
	        
	        // Eğer bileşen görünür değilse (visible) veya aktif değilse bu turu geçebilirsin.
	        // if (!compoment.isVisible) continue; // (Opsiyonel: Eğer böyle bir değişkenin varsa ekle)

	        // Farenin bu component üzerinde olup olmadığını kontrol et.
	        boolean isHovered = (relMouseX >= compoment.x && relMouseX <= compoment.x + compoment.width) &&
	                            (relMouseY >= compoment.y && relMouseY <= compoment.y + compoment.height);

	        boolean isTouch = isHovered && isTouch2;

	        // Eğer fare bu bileşenin üzerindeyse:
	        if (isHovered) {
	            if (active) {
	                // Tıklama olayını çocuğa gönder
	                compoment.mouseClicked(relMouseX - compoment.x, relMouseY - compoment.y, button, isTouch);
	            }

	            // 2. DEĞİŞİKLİK: KRİTİK NOKTA!
	            // En üstteki bileşeni bulduk ve tıkladık. Artık arkadakilere bakmaya gerek yok.
	            // Döngüyü kırıyoruz.
	            break; 
	        }
	    }
	}

	public void mouseGetInfo(int relMouseX, int relMouseY, boolean isTouch2) {
		for (int i = 0; i < this.compoments.size(); i++) {
			GuiCompoment compoment = compoments.get(i);
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
	public List<GuiCompoment> getCompoments() {
		return compoments;
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
