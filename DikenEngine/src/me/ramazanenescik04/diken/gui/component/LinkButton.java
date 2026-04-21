package me.ramazanenescik04.diken.gui.component;

import java.net.URI;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

/**
 * Represents the `LinkButton` type within the DikenEngine `gui.compoment` package.
 */
public class LinkButton extends GuiLink {
	private static final long serialVersionUID = 1L;
	private Button button;

	public LinkButton(String text, int x, int y, int width, int height, int color) {
		this(text, x, y, width, height);
		button = button.setTextColor(color);
	}
	
	public LinkButton(String text, int x, int y, int width, int height) {
		super( x, y, width, height);
		button = new Button(text, x, y, width, height);
	}
	
	public LinkButton setURI(URI uri) {
		_setURI(uri);
		return this;
	}
	
	public Bitmap render() {
		Bitmap bitmap = button.render();
		ArrayBitmap button = (ArrayBitmap) ResourceLocator.getResource("button-array");
		bitmap.draw(button.bitmap[3][0].replaceColor(0xffffffff, 0xff000000), width - 9, height / 2 - 4);
		return bitmap;
	}

	public void tick(DikenEngine engine) {
		button.tick(engine);
	}

	public void mouseGetInfo(int x, int y, boolean isTouch) {
		super.mouseGetInfo(x, y, isTouch);
		
		button.mouseGetInfo(x, y, isTouch);
	}
}
