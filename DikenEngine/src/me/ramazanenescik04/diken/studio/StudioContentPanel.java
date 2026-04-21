package me.ramazanenescik04.diken.studio;

import me.ramazanenescik04.diken.gui.component.GuiComponent;
import me.ramazanenescik04.diken.gui.component.Panel;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.FrameBitmapPool;

public class StudioContentPanel extends Panel {
	private static final long serialVersionUID = 1L;

	@Override
	public Bitmap render() {
		Bitmap bitmap = FrameBitmapPool.newBitmap(width, height);
		bitmap.blendFill(0, 0, width, height, 0xff232934);
		for (int i = 0; i < count(); i++) {
			GuiComponent component = get(i);
			if (component != null && component.isVisible()) {
				bitmap.draw(component.render(), component.x, component.y);
			}
		}
		return bitmap;
	}
}

