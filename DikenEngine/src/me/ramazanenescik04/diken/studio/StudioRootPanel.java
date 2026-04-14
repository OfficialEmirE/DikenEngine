package me.ramazanenescik04.diken.studio;

import me.ramazanenescik04.diken.gui.compoment.GuiComponent;
import me.ramazanenescik04.diken.gui.compoment.Panel;
import me.ramazanenescik04.diken.resource.Bitmap;

public class StudioRootPanel extends Panel {
	private static final long serialVersionUID = 1L;

	@Override
	public Bitmap render() {
		Bitmap bitmap = new Bitmap(width, height);
		bitmap.clear(0xff1b1f26);

		// Keep index-based loop to avoid concurrent modification during UI updates.
		for (int i = 0; i < count(); i++) {
			GuiComponent component = getCompoments().get(i);
			if (component != null && component.isVisible()) {
				bitmap.draw(component.render(), component.x, component.y);
			}
		}
		return bitmap;
	}
}
