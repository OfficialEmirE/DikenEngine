package me.ramazanenescik04.diken.gui.compoment;

import java.util.function.Consumer;

import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.tools.PixelToColor;

/**
 * Represents the `ColorPickBar` type within the DikenEngine `gui.compoment` package.
 */
public class ColorPickBar extends GuiComponent {
	private static final long serialVersionUID = 1L;
	
	private int selectedColor;
	private Consumer<Integer> consumer;

	public ColorPickBar(int x, int y, int width, int height) {
		super(x, y, width, height);
	}
	
	public ColorPickBar setConsumer(Consumer<Integer> consumer) {
		this.consumer = consumer;
		return this;
	}
	
	public ColorPickBar setSelectedColor(int color) {
		this.selectedColor = color;
		return this;
	}
	
	public int getSelectedColor() {
		return selectedColor;
	}

	@Override
	public Bitmap render() {
		Bitmap bitmap = super.render();
		Bitmap colorMap = PixelToColor.createHColorRect(bitmap.w - 2, bitmap.h - 2);
		bitmap.draw(colorMap, 1, 1);
		bitmap.box(0, 0, bitmap.w - 2, bitmap.h - 2, 0xff000000);
		return bitmap;
	}

	@Override
	public void mouseClicked(int x, int y, int button, boolean isTouch) {
		if (isTouch && this.active && button == 0) {
			this.selectedColor = this.render().getPixel(x, y);
			
			if (this.consumer != null) this.consumer.accept(this.selectedColor);
		}
	}
}
