package me.ramazanenescik04.diken.gui.compoment;

import java.net.URI;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.gui.UniFont;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

/**
 * Represents the `LinkText` type within the DikenEngine `gui.compoment` package.
 */
public class LinkText extends GuiLink {
	private static final long serialVersionUID = 1L;
	public Text text;
	
	public LinkText(String _text, int x, int y) {
		super(x, y, 2, 2);
		this.text = new Text(_text, x, y, 0, 0, 0xFFFFFFFF, DikenEngine.getEngine().defaultFont);
	}
	
	public LinkText(String text, int x, int y, UniFont font) {
		super(x, y, 2, 2);
		this.text = new Text(text, x, y, 0, 0, 0xFFFFFFFF, font);
	}
	
	public LinkText setURI(URI uri) {
		_setURI(uri);
		return this;
	}

	public Bitmap render() {
		Bitmap bitmap = new Bitmap(width + 9, height);
		bitmap.draw(text.render(), 0, 0);
		ArrayBitmap button = (ArrayBitmap) ResourceLocator.getResource("button-array");
		bitmap.draw(button.bitmap[3][0], width + 2, height / 2 - 4);
		return bitmap;
	}

	public void tick(DikenEngine engine) {
		text.tick(engine);
		
		this.width = text.width;
		this.height = text.height;
	}
	
	
}
