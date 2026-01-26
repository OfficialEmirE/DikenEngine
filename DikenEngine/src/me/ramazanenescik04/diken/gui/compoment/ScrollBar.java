package me.ramazanenescik04.diken.gui.compoment;

import java.util.function.Consumer;

import org.lwjgl.input.Mouse;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.InputHandler;
import me.ramazanenescik04.diken.gui.Hitbox;
import me.ramazanenescik04.diken.resource.Bitmap;

public class ScrollBar extends GuiComponent {

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
			upButton = new Button("/\\", 1, 1, width - 2, 20).setRunnable(() -> {
				setScrollValue(scrollValue - 0.05f);
			});
			upButton.parent = this;
			
			downButton = new Button("\\/", 1, height - 21, width - 2, 20).setRunnable(() -> {
				setScrollValue(scrollValue + 0.05f);
			});
			downButton.parent = this;
			
			sliderButton = new Button("=", 1, 21, width - 2, handleHeight);
			sliderButton.parent = this;
		} else {
			upButton = new Button("<", 1, 1, 20, height - 2).setRunnable(() -> {
				setScrollValue(scrollValue - 0.05f);
			});
			upButton.parent = this;
			
			downButton = new Button(">", width - 21, 1, 20, height - 2).setRunnable(() -> {
				setScrollValue(scrollValue + 0.05f);
			});
			downButton.parent = this;
			
			sliderButton = new Button("||", 21, 1, handleHeight, height - 2);
			sliderButton.parent = this;
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
    
    public void updateHandleSize(int viewportSize, int totalContentSize) {
        // trackSize: Barın toplam hareket yolu (üst/alt butonlar arası mesafe)
        int trackSize = (type == VERTICAL) ? this.height - 40 : this.width - 40;

        if (totalContentSize <= viewportSize) {
            // İçerik ekrana sığıyor, düğme tam boy (veya gizli)
            this.handleHeight = trackSize;
        } else {
            // ALTIN KURAL: Düğme Boyu = (Görünür Alan / Toplam İçerik) * Bar Yolu
            float ratio = (float) viewportSize / totalContentSize;
            this.handleHeight = (int) (trackSize * ratio);

            // Düğme çok küçülüp kaybolmasın (min 15px)
            if (this.handleHeight < 5) this.handleHeight = 5;
        }
    }

	@Override
	public Bitmap render() {
		Bitmap btp = super.render();
		
		btp.clear(0xffa0a0a0);
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
            upButton.width = this.width - 2;
            upButton.height = this.width - 0; // Kare olması için height = width

            // 2. Aşağı Butonu: En altta
            downButton.x = 1;
            downButton.y = this.height - this.width - 1;
            downButton.width = this.width - 2;
            downButton.height = this.width - 0;

            // 3. Sürükleme Butonu (Slider): Genişlik bar kadar, y konumu değere göre
            sliderButton.width = this.width - 2;
            // sliderButton.height ve sliderButton.y daha önce konuştuğumuz 
            // dinamik hesaplama ile burada güncellenmeli.
            
        } else {
            // YATAY MANTIĞI (Horizontal)
        	upButton.x = 1;
        	upButton.y = 1;
        	upButton.width = this.height; // Yatayda yükseklik kadar genişlik (kare)
        	upButton.height = this.height - 2;

        	downButton.x = this.width - this.height - 1;
            downButton.y = 1;
            downButton.width = this.height;
            downButton.height = this.height - 2;

            sliderButton.height = this.height - 2;
            // sliderButton.width ve sliderButton.x burada güncellenmeli.
        }
    }

	@Override
	public void tick(DikenEngine engine) {
		upButton.tick(engine);
		downButton.tick(engine);
		
		recalculateLayout();
		
		if (type == VERTICAL) {
			if (sliderButton.height != handleHeight) {
				sliderButton.setSize(sliderButton.width, handleHeight);
			}
		} else {
			if (sliderButton.width != handleHeight) {
				sliderButton.setSize(handleHeight, sliderButton.height);
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
			int mouseX = InputHandler.getMousePosition().x - this.getGlobalX();
			int mouseY = InputHandler.getMousePosition().y - this.getGlobalY();
			
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
				
				int minY = upButton.height; 
		        int maxY = height - (downButton.height) - (sliderButton.height);

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
			int mouseX = InputHandler.getMousePosition().x - this.getGlobalX();
			int mouseY = InputHandler.getMousePosition().y - this.getGlobalY();
			
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
				
				int minX = upButton.width;
		        int maxX = width - (downButton.width) - (sliderButton.width);

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
	    boolean claimed = false;
	    
	    // HATAYI ÖNLEYEN KRİTİK NOKTA: Listenin o anki elemanlarını yeni bir diziye alıyoruz.
	    // Böylece döngü sırasında compoments listesine ekleme/çıkarma yapılsa bile döngü patlamaz.
	    GuiComponent[] tempElements = this.buttons;

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
	    for (GuiComponent compoment : this.buttons) {
	        // Mantık burada da aynı: InputHandler yok, bağıl matematik var.
	        
	        boolean isHovered = (relMouseX >= compoment.x && relMouseX <= compoment.x + compoment.width) &&
	                            (relMouseY >= compoment.y && relMouseY <= compoment.y + compoment.height);

	        boolean isTouch = isHovered && isTouch2;
	        
	        compoment.mouseGetInfo(relMouseX - compoment.x, relMouseY - compoment.y, isTouch);
	    }
	}
}
