package me.ramazanenescik04.diken.gui.compoment;

import me.ramazanenescik04.diken.resource.Bitmap;

/**
 * Represents the `RenderImage` type within the DikenEngine `gui.compoment` package.
 */
public class RenderImage extends GuiComponent {
	private static final long serialVersionUID = 1L;
	protected Bitmap bitmap;

	public RenderImage(Bitmap btp, int x, int y) {
		super(x, y, btp.w, btp.h);
		this.bitmap = btp;
	}
	
	public void setBitmap(Bitmap b) {
		this.bitmap = b;
	}
	
	public Bitmap render() {
		return bitmap;
	}

}
