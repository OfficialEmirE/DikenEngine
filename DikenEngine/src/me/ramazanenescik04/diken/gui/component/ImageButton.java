package me.ramazanenescik04.diken.gui.component;

import me.ramazanenescik04.diken.resource.Bitmap;

/**
 * Represents the `ImageButton` type within the DikenEngine `gui.compoment` package.
 */
public class ImageButton extends Button {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public Bitmap bitmap;

	public ImageButton(Bitmap btp, int x, int y, int width, int height) {
		super("", x, y, width, height);
		this.bitmap = btp;
	}
	
	public Bitmap getIcon() {
		return bitmap;
	}

	public void setIcon(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

	public Bitmap render() {
		Bitmap bitmap = super.render();
		bitmap.draw(this.bitmap, (width) / 2 - (this.bitmap.w / 2), (height) / 2 - (this.bitmap.h / 2));
		return bitmap;
	}
	
}
