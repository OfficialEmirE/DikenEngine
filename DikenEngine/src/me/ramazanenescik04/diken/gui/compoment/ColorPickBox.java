package me.ramazanenescik04.diken.gui.compoment;

import java.util.function.Consumer;

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
		return this;
	}
	
	public ColorPickBox setHueColor(int color) {
		this.hueColor = PixelToColor.rgbToHsv(color)[0];
		return this;
	}
	
	public int getSelectedColor() {
		return selectedColor;
	}
	
	public float getHueColor() {
		return hueColor;
	}

	@Override
	public Bitmap render() {
		Bitmap bitmap = super.render();
		Bitmap colorMap = PixelToColor.createHSVRect(bitmap.w - 2, bitmap.h - 2, hueColor);
		bitmap.draw(colorMap, 1, 1);
		bitmap.box(0, 0, bitmap.w - 2, bitmap.h - 2, 0xff000000);
		return bitmap;
	}

	@Override
	public void mouseClicked(int x, int y, int button, boolean isTouch) {
		if (isTouch && button == 0) {
			this.selectedColor = this.render().getPixel(x, y);
			
			if (this.consumer != null) this.consumer.accept(this.selectedColor);
		}
	}
}
