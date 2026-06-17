package me.ramazanenescik04.diken.gui.component;

import java.util.List;
import java.util.function.Consumer;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.EnumSettingType;
import me.ramazanenescik04.diken.game.Setting;
import me.ramazanenescik04.diken.game.SettingCategory;
import me.ramazanenescik04.diken.gui.UDim2;
import me.ramazanenescik04.diken.gui.hitbox.Hitbox;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

/**
 * Represents the `ScrollBar` type within the DikenEngine `gui.component` package.
 */
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

	public ScrollBar(UDim2 position, UDim2 size, int type) {
		super("ScrollBar", position, size);
		this.type = type;

		regenerateButtons();
	}

	public ScrollBar(UDim2 position, UDim2 size) {
		this(position, size, VERTICAL);
	}
	
	private void regenerateButtons() {
		int width = getWidth();
		int height = getHeight();

		if (type == VERTICAL) {
			upButton = new Button("/\\", new UDim2(0, 1, 0, 1), new UDim2(0, width - 2, 0, 20)).setRunnable(() -> {
				setScrollValue(scrollValue - 0.05f);
			});

			downButton = new Button("\\/", new UDim2(0, 1, 0, height - 21), new UDim2(0, width - 2, 0, 20)).setRunnable(() -> {
				setScrollValue(scrollValue + 0.05f);
			});

			sliderButton = new Button("=", new UDim2(0, 1, 0, 21), new UDim2(0, width - 2, 0, handleHeight));
		} else {
			upButton = new Button("<", new UDim2(0, 1, 0, 1), new UDim2(0, 20, 0, height - 2)).setRunnable(() -> {
				setScrollValue(scrollValue - 0.05f);
			});

			downButton = new Button(">", new UDim2(0, width - 21, 0, 1), new UDim2(0, 20, 0, height - 2)).setRunnable(() -> {
				setScrollValue(scrollValue + 0.05f);
			});

			sliderButton = new Button("||", new UDim2(0, 21, 0, 1), new UDim2(0, handleHeight, 0, height - 2));
		}

		buttons = new Button[] { upButton, downButton, sliderButton };
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
		int trackSize = (type == VERTICAL) ? getHeight() - 40 : getWidth() - 40;

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

		int width = getWidth();
		int height = getHeight();

		btp.clear(0xffa0a0a0);
		btp.draw(upButton.render(), upButton.getLocalX(), upButton.getLocalY());
		btp.draw(downButton.render(), downButton.getLocalX(), downButton.getLocalY());
		btp.draw(sliderButton.render(), sliderButton.getLocalX(), sliderButton.getLocalY());
		btp.box(0, 0, width - 1, height - 1, 0xffffffff); // Kenarlık

		return btp;
	}

	private void recalculateLayout() {
		int width = getWidth();
		int height = getHeight();

		if (type == VERTICAL) {
			// 1. Yukarı Butonu: En üstte, bar genişliğinde kare
			upButton.setPosition(1, 1);
			upButton.setSize(width - 2, width); // Kare olması için height = width

			// 2. Aşağı Butonu: En altta
			downButton.setPosition(1, height - width - 1);
			downButton.setSize(width - 2, width);

			// 3. Sürükleme Butonu (Slider): Genişlik bar kadar, y konumu değere göre
			sliderButton.setSize(width - 2, sliderButton.getHeight());
			// sliderButton.height ve sliderButton.y daha önce konuştuğumuz
			// dinamik hesaplama ile burada güncellenmeli.

		} else {
			// YATAY MANTIĞI (Horizontal)
			upButton.setPosition(1, 1);
			upButton.setSize(height, height - 2); // Yatayda yükseklik kadar genişlik (kare)

			downButton.setPosition(width - height - 1, 1);
			downButton.setSize(height, height - 2);

			sliderButton.setSize(sliderButton.getWidth(), height - 2);
			// sliderButton.width ve sliderButton.x burada güncellenmeli.
		}
	}

	@Override
	public void tick(DikenEngine engine) {
		upButton.tick(engine);
		downButton.tick(engine);

		recalculateLayout();

		int width = getWidth();
		int height = getHeight();

		if (type == VERTICAL) {
			if (sliderButton.getHeight() != handleHeight) {
				sliderButton.setSize(sliderButton.getWidth(), handleHeight);
			}
		} else {
			if (sliderButton.getWidth() != handleHeight) {
				sliderButton.setSize(handleHeight, sliderButton.getHeight());
			}
		}

		// Tutamağın pozisyonunu güncelle
		int sliderAreaLength;
		if (type == VERTICAL) {
			sliderAreaLength = height - 40 - handleHeight; // Yukarı ve aşağı butonları hariç
			int sliderY = 20 + (int) (scrollValue * sliderAreaLength);
			sliderButton.setPosition(sliderButton.getLocalX(), sliderY);
			sliderButton.tick(engine);

			// Sürükleme işlemi
			boolean mouseDown = engine.input.isMouseDown(0);
			int mouseX = engine.input.getMousePosition().x - this.getGlobalX();
			int mouseY = engine.input.getMousePosition().y - this.getGlobalY();

			if (mouseDown && !prevMouseDown) {
				// Mouse tıklandığında
				Hitbox sliderBounds = new Hitbox(sliderButton.getLocalX(), sliderButton.getLocalY(),
						sliderButton.getWidth(), sliderButton.getHeight());
				if (sliderBounds.contains(mouseX, mouseY)) {
					isDragging = true;
					dragOffsetY = mouseY - sliderButton.getLocalY();
				}
			} else if (!mouseDown) {
				// Mouse bırakıldığında
				isDragging = false;
			}

			if (isDragging) {
				if (listener != null && sliderButton.getLocalY() != mouseY - dragOffsetY) {
					listener.accept(scrollValue);
				}

				int minY = upButton.getHeight();
				int maxY = height - downButton.getHeight() - sliderButton.getHeight();

				// Çökme koruması: Max, Min'den küçük olamaz
				if (maxY < minY) maxY = minY;

				int newSliderY = mouseY - dragOffsetY;
				int clampedY = Math.clamp(newSliderY, minY, maxY);
				sliderButton.setPosition(sliderButton.getLocalX(), clampedY);

				// scrollValue'yu güncelle
				int sliderAreaHeightDrag = height - 40 - handleHeight;
				scrollValue = (float) (clampedY - 20) / (float) sliderAreaHeightDrag;
			}

			prevMouseDown = mouseDown;
		} else {
			sliderAreaLength = width - 40 - handleHeight; // Sol ve sağ butonları hariç
			int sliderX = 20 + (int) (scrollValue * sliderAreaLength);
			sliderButton.setPosition(sliderX, sliderButton.getLocalY());
			sliderButton.tick(engine);

			// Sürükleme işlemi
			boolean mouseDown = engine.input.isMouseDown(0);
			int mouseX = engine.input.getMousePosition().x - this.getGlobalX();
			int mouseY = engine.input.getMousePosition().y - this.getGlobalY();

			if (mouseDown && !prevMouseDown) {
				// Mouse tıklandığında
				Hitbox sliderBounds = new Hitbox(sliderButton.getLocalX(), sliderButton.getLocalY(),
						sliderButton.getWidth(), sliderButton.getHeight());
				if (sliderBounds.contains(mouseX, mouseY)) {
					isDragging = true;
					dragOffsetX = mouseX - sliderButton.getLocalX();
				}
			} else if (!mouseDown) {
				// Mouse bırakıldığında
				isDragging = false;
			}

			if (isDragging) {
				if (listener != null && sliderButton.getLocalX() != mouseX - dragOffsetX) {
					listener.accept(scrollValue);
				}

				int minX = upButton.getWidth();
				int maxX = width - downButton.getWidth() - sliderButton.getWidth();

				// Çökme koruması
				if (maxX < minX) maxX = minX;

				int newSliderX = mouseX - dragOffsetX;
				int clampedX = Math.clamp(newSliderX, minX, maxX);
				sliderButton.setPosition(clampedX, sliderButton.getLocalY());

				// scrollValue'yu güncelle
				int sliderAreaWidthDrag = width - 40 - handleHeight;
				scrollValue = (float) (clampedX - 20) / (float) sliderAreaWidthDrag;
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

			int cx = compoment.getLocalX();
			int cy = compoment.getLocalY();
			int cw = compoment.getWidth();
			int ch = compoment.getHeight();

			boolean isHovered = (relMouseX >= cx && relMouseX <= cx + cw) &&
					(relMouseY >= cy && relMouseY <= cy + ch);

			if (!claimed && isHovered) {
				if (isActive()) {
					compoment.mouseClicked(relMouseX - cx, relMouseY - cy, button, isTouch2);
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

			int cx = compoment.getLocalX();
			int cy = compoment.getLocalY();
			int cw = compoment.getWidth();
			int ch = compoment.getHeight();

			boolean isHovered = (relMouseX >= cx && relMouseX <= cx + cw) &&
					(relMouseY >= cy && relMouseY <= cy + ch);

			boolean isTouch = isHovered && isTouch2;

			compoment.mouseGetInfo(relMouseX - cx, relMouseY - cy, isTouch);
		}
	}
	
	@Override
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("scrollBar", "ScrollBar", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(13, 2));
		
		var settingCategory = SettingCategory
				.createSettingCategory(key)
				.addSetting(new Setting<Integer>("Scroll Value", (int) (this.scrollValue * 100), 0, 100, Integer.class, EnumSettingType.SLIDER)
						.addChangeListener(e -> setScrollValue(e / 100.0f))
				)
				.addSetting(new Setting<Boolean>("Vertical Scroll", type == HORIZONTAL, Boolean.class, EnumSettingType.CHECK_BOX)
						.addChangeListener(e -> {
							this.type = e ? HORIZONTAL : VERTICAL;
							
							regenerateButtons();
						})
				);
		
		var list = super.getNodeSettings();
		list.add(settingCategory);
		return list;
	}
}