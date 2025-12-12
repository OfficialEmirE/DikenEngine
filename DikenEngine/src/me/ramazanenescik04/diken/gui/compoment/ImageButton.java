package me.ramazanenescik04.diken.gui.compoment;

import me.ramazanenescik04.diken.resource.Bitmap;

public class ImageButton extends Button {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Bitmap bitmap;

	public ImageButton(Bitmap btp, int x, int y, int width, int height) {
		super("", x, y, width, height);
		this.bitmap = btp;
	}
	
	public Bitmap render() {
		Bitmap bitmap = super.render();
		bitmap.draw(this.bitmap, (width + 4) / 2 - (this.bitmap.w / 2), (height + 4) / 2 - (this.bitmap.h / 2));
		return bitmap;
	}
	
}
