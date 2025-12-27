package me.ramazanenescik04.diken.gui.compoment;

import java.util.function.Consumer;

import org.lwjgl.input.Mouse;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.InputHandler;
import me.ramazanenescik04.diken.gui.Hitbox;
import me.ramazanenescik04.diken.resource.Bitmap;

public class ScrollBar extends GuiCompoment {

	private static final long serialVersionUID = 9174661067316376835L;

	public static final int VERTICAL = 0;
	public static final int HORIZONTAL = 1;
	
	private Button upButton;
	private Button downButton;
	private Button sliderButton;
	
	private Button[] buttons;
	
	private Consumer<Float> listener;
	
	private float scrollValue = 0.0f; //0.0 - 1.0 arası değer
	private int handleHeight = 30;    // Tutamağın boyu (İçeriğe göre dinamik de yapılabilir)
    private boolean isDragging = false;
    
    private int dragOffsetY = 0;
    private int dragOffsetX = 0;
    
    private int type;
    
    private boolean prevMouseDown = false;

	public ScrollBar(int x, int y, int width, int height, int type) {
		super(x, y, width, height);
		this.type = type;
		
		if (type == VERTICAL) {
			upButton = new Button("/\\", 1, 1, width - 6, 20 - 4).setRunnable(() -> {
				setScrollValue(scrollValue - 0.05f);
			});
			
			downButton = new Button("\\/", 1, height - 21, width - 6, 20 - 4).setRunnable(() -> {
				setScrollValue(scrollValue + 0.05f);
			});
			
			sliderButton = new Button("", 1, 21, width - 6, handleHeight - 4);
		} else {
			upButton = new Button("<", 1, 1, 20 - 4, height - 6).setRunnable(() -> {
				setScrollValue(scrollValue - 0.05f);
			});
			
			downButton = new Button(">", width - 21, 1, 20 - 4, height - 6).setRunnable(() -> {
				setScrollValue(scrollValue + 0.05f);
			});
			
			sliderButton = new Button("=", 21, 1, handleHeight - 4, height - 6);
		}
		buttons = new Button[] { upButton, downButton, sliderButton };
	}
	
	public ScrollBar(int x, int y, int width, int height) {
		this(x, y, width, height, VERTICAL);
	}
	
	// get ile bizi sayı versin
	public ScrollBar addDraggedListener(Consumer<Float> listener) {
		this.listener = listener;
	    return this;
	}
	
	// Değeri dışarıdan almak için (ScrollPanel bunu kullanacak)
	public float getScrollValue() {
        return scrollValue;
    }

    // Değeri dışarıdan değiştirmek için (Örn: Mouse Tekerleği ile)
    public void setScrollValue(float value) {
        this.scrollValue = Math.clamp(value, 0.0f, 1.0f); // Java 21 Math.clamp
        
        if (listener != null) {
			listener.accept(this.scrollValue);
		}
    }
    
    public void updateHandleSize(int panelHeight, int contentHeight) {
        if (contentHeight <= panelHeight) {
            this.handleHeight = this.height; // Her şey görünüyor, handle tam boy
            //this.visible = false; // İstersen scrollbar'ı gizleyebilirsin
        } else {
            float ratio = (float) panelHeight / contentHeight;
            this.handleHeight = (int) (this.height * ratio);
            
            // Minimum boy sınırı (Çok küçülüp kaybolmasın)
            if (this.handleHeight < 20) this.handleHeight = 20;
            
            //this.visible = true;
        }
    }

	@Override
	public Bitmap render() {
		Bitmap btp = super.render();
		
		btp.draw(upButton.render(), upButton.x, upButton.y);
		btp.draw(downButton.render(), downButton.x, downButton.y);
		btp.draw(sliderButton.render(), sliderButton.x, sliderButton.y);
		btp.box(0, 0, width - 1, height - 1, 0xffffffff); // Kenarlık
		
		return btp;
	}
	
	private void recalculateLayout() {
        if (type == VERTICAL) {
            // 1. Yukarı Butonu: En üstte, bar genişliğinde kare
            upButton.x = 1; // Bu x, ScrollBar'ın kendi (0,0)'ına göredir
            upButton.y = 1;
            upButton.width = this.width - 6;
            upButton.height = this.width - 4; // Kare olması için height = width

            // 2. Aşağı Butonu: En altta
            downButton.x = 1;
            downButton.y = this.height - this.width - 1;
            downButton.width = this.width - 6;
            downButton.height = this.width - 4;

            // 3. Sürükleme Butonu (Slider): Genişlik bar kadar, y konumu değere göre
            sliderButton.width = this.width - 6;
            // sliderButton.height ve sliderButton.y daha önce konuştuğumuz 
            // dinamik hesaplama ile burada güncellenmeli.
            
        } else {
            // YATAY MANTIĞI (Horizontal)
        	upButton.x = 1;
        	upButton.y = 1;
        	upButton.width = this.height - 4; // Yatayda yükseklik kadar genişlik (kare)
        	upButton.height = this.height - 6;

        	downButton.x = this.width - this.height - 1;
            downButton.y = 1;
            downButton.width = this.height - 4;
            downButton.height = this.height - 6;

            sliderButton.height = this.height - 6;
            // sliderButton.width ve sliderButton.x burada güncellenmeli.
        }
    }

	@Override
	public void tick(DikenEngine engine) {
		upButton.tick(engine);
		downButton.tick(engine);
		
		recalculateLayout();
		
		if (type == VERTICAL) {
			if (sliderButton.height != handleHeight - 4) {
				sliderButton.setSize(sliderButton.width, handleHeight - 4);
			}
		} else {
			if (sliderButton.width != handleHeight - 4) {
				sliderButton.setSize(handleHeight - 4, sliderButton.height);
			}
		}
		
		// Tutamağın pozisyonunu güncelle
		int sliderAreaLength;
		if (type == VERTICAL) {
			sliderAreaLength = height - 40 - handleHeight; // Yukarı ve aşağı butonları hariç
			int sliderY = 20 + (int) (scrollValue * sliderAreaLength);
			sliderButton.y = sliderY;
			sliderButton.tick(engine);
			
			// Sürükleme işlemi
			boolean mouseDown = Mouse.isButtonDown(0);
			int mouseX = InputHandler.getMousePosition().x - this.x;
			int mouseY = InputHandler.getMousePosition().y - this.y;
			
			if (mouseDown && !prevMouseDown) {
				// Mouse tıklandığında
				if (sliderButton.intersects(new Hitbox(mouseX, mouseY))) {
					isDragging = true;
					dragOffsetY = mouseY - sliderButton.y;
				}
			} else if (!mouseDown) {
				// Mouse bırakıldığında
				isDragging = false;
			}
			
			if (isDragging) {
				if (listener != null && sliderButton.y != mouseY - dragOffsetY) {
					listener.accept(scrollValue);
				}
				
				int minY = upButton.height + 4; 
		        int maxY = height - (downButton.height + 4) - (sliderButton.height + 4);

		        // Çökme koruması: Max, Min'den küçük olamaz
		        if (maxY < minY) maxY = minY;

		        int newSliderY = mouseY - dragOffsetY;
		        sliderButton.y = Math.clamp(newSliderY, minY, maxY);
				
				// scrollValue'yu güncelle
				int sliderAreaHeightDrag = height - 40 - handleHeight;
				scrollValue = (float)(sliderButton.y - 20) / (float)sliderAreaHeightDrag;
			}
			
			prevMouseDown = mouseDown;
		} else {
			sliderAreaLength = width - 40 - handleHeight; // Sol ve sağ butonları hariç
			int sliderX = 20 + (int) (scrollValue * sliderAreaLength);
			sliderButton.x = sliderX;
			sliderButton.tick(engine);
			
			// Sürükleme işlemi
			boolean mouseDown = Mouse.isButtonDown(0);
			int mouseX = InputHandler.getMousePosition().x - this.x;
			int mouseY = InputHandler.getMousePosition().y - this.y;
			
			if (mouseDown && !prevMouseDown) {
				// Mouse tıklandığında
				if (sliderButton.intersects(new Hitbox(mouseX, mouseY))) {
					isDragging = true;
					dragOffsetX = mouseX - sliderButton.x;
				}
			} else if (!mouseDown) {
				// Mouse bırakıldığında
				isDragging = false;
			}
			
			if (isDragging) {
				if (listener != null && sliderButton.x != mouseX - dragOffsetX) {
					listener.accept(scrollValue);
				}
				
				int minX = upButton.width + 4;
		        int maxX = width - (downButton.width + 4) - (sliderButton.width + 4);

		        // Çökme koruması
		        if (maxX < minX) maxX = minX;

		        int newSliderX = mouseX - dragOffsetX;
		        sliderButton.x = Math.clamp(newSliderX, minX, maxX);
				
				// scrollValue'yu güncelle
				int sliderAreaWidthDrag = width - 40 - handleHeight;
				scrollValue = (float)(sliderButton.x - 20) / (float)sliderAreaWidthDrag;
			}
			
			prevMouseDown = mouseDown;
		}
	}

	public void mouseClicked(int relMouseX, int relMouseY, int button, boolean isTouch2) {
	    for (GuiCompoment compoment : this.buttons) {
	        
	        // --- DÜZELTME BAŞLANGICI ---
	        
	        // Farenin bu component (çocuk) üzerinde olup olmadığını kontrol et.
	        // relMouseX/Y zaten 'this' paneline göre olduğu için, sadece çocuğun sınırlarına bakıyoruz.
	        // InputHandler kullanmıyoruz, yukarıdan gelen koordinata güveniyoruz.
	        
	        boolean isHovered = (relMouseX >= compoment.x && relMouseX <= compoment.x + compoment.width) &&
	                            (relMouseY >= compoment.y && relMouseY <= compoment.y + compoment.height);

	        // Eğer senin 'intersects' metodun özel bir hitbox şekli (daire vs.) kullanıyorsa:
	        // Hitbox childHitbox = new Hitbox(compoment.x, compoment.y, compoment.width, compoment.height);
	        // Hitbox mousePoint = new Hitbox(relMouseX, relMouseY, 1, 1);
	        // boolean isHovered = childHitbox.intersects(mousePoint);

	        boolean isTouch = isHovered && isTouch2;

	        if (active) {
	            // Çocuğa koordinat gönderirken, ÇOCUĞUN konumunu çıkarıyoruz.
	            // Böylece çocuk da kendi içindeki (0,0) noktasına göre fareyi alıyor.
	            compoment.mouseClicked(relMouseX - compoment.x, relMouseY - compoment.y, button, isTouch);
	        }
	        
	        // --- DÜZELTME BİTİŞİ ---
	    }
	}

	public void mouseGetInfo(int relMouseX, int relMouseY, boolean isTouch2) {
	    for (GuiCompoment compoment : this.buttons) {
	        // Mantık burada da aynı: InputHandler yok, bağıl matematik var.
	        
	        boolean isHovered = (relMouseX >= compoment.x && relMouseX <= compoment.x + compoment.width) &&
	                            (relMouseY >= compoment.y && relMouseY <= compoment.y + compoment.height);

	        boolean isTouch = isHovered && isTouch2;
	        
	        compoment.mouseGetInfo(relMouseX - compoment.x, relMouseY - compoment.y, isTouch);
	    }
	}
}
