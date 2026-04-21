package me.ramazanenescik04.diken.gui.component.color;

import java.util.function.Consumer;

import me.ramazanenescik04.diken.gui.component.GuiComponent;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.tools.PixelToColor;

/**
 * Represents the `ColorPickBox` type within the DikenEngine `gui.compoment` package.
 */
public class ColorPickBox extends GuiComponent {
	private static final long serialVersionUID = 1L;

	private int selectedColor;
	private float hueColor = PixelToColor.rgbToHsv(0xffff0000)[0];
	private Consumer<Integer> consumer;

	private Bitmap cachedMap;
	private int selectedX = -1;
	private int selectedY = -1;

	public ColorPickBox(int x, int y, int width, int height) {
		super(x, y, width, height);
	}

	public ColorPickBox setConsumer(Consumer<Integer> consumer) {
		this.consumer = consumer;
		return this;
	}

	public ColorPickBox setSelectedColor(int color) {
		this.selectedColor = color;
		return this;
	}

	public ColorPickBox setHueColor(float color) {
		this.hueColor = color;
		this.cachedMap = PixelToColor.createHSVRect(this.width - 2, this.height - 2, hueColor);
		var point = this.cachedMap.findColorPos(PixelToColor.hsvToRgb(color, 0, 0));
		if (point != null) {
			this.selectedX = point.x + 1;
			this.selectedY = point.y + 1;
		}
		return this;
	}

	public ColorPickBox setHueColor(int color) {
		this.hueColor = PixelToColor.rgbToHsv(color)[0];
		this.cachedMap = PixelToColor.createHSVRect(this.width - 2, this.height - 2, hueColor);
		
		var point = this.cachedMap.findColorPos(color);
		if (point != null) {
			this.selectedX = point.x + 1;
			this.selectedY = point.y + 1;
		}
		return this;
	}

	public int getSelectedColor() {
		return selectedColor;
	}

	public float getHueColor() {
		return hueColor;
	}
	
	public int getSelectedPosColor() {
		if (selectedX == -1 || selectedY == -1)
			return 0xffff0000;
		
		return cachedMap.getPixel(selectedX - 1, selectedY - 1);
	}

	@Override
	public Bitmap render() {
		Bitmap bitmap = super.render();

		bitmap.draw(cachedMap, 1, 1);

		// Çerçeve
		bitmap.box(0, 0, bitmap.w - 1, bitmap.h - 1, 0xff000000);

		// Seçili noktayı göster
		if (selectedX >= 0 && selectedY >= 0) {
			bitmap.box(selectedX - 1, selectedY - 1, selectedX + 1, selectedY + 1, 0xffcfcfcf);
		}

		return bitmap;
	}

	@Override
	public void mouseClicked(int x, int y, int button, boolean isTouch) {
		if (isTouch && button == 0) {

			// sınır kontrolü
			if (x <= 0 || y <= 0 || x >= width - 1 || y >= height - 1 || cachedMap == null) return;

			// cached map üzerinden al
			selectedColor = cachedMap.getPixel(x - 1, y - 1);

			selectedX = x;
			selectedY = y;

			if (consumer != null) {
				consumer.accept(selectedColor);
			}
		}
	}
}