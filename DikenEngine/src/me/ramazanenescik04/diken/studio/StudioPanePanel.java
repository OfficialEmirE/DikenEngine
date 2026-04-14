package me.ramazanenescik04.diken.studio;

import me.ramazanenescik04.diken.gui.compoment.GuiComponent;
import me.ramazanenescik04.diken.gui.compoment.Panel;
import me.ramazanenescik04.diken.gui.compoment.Text;
import me.ramazanenescik04.diken.resource.Bitmap;

public class StudioPanePanel extends Panel {
	private static final long serialVersionUID = 1L;
	protected static final int HEADER_HEIGHT = 18;

	private final String title;

	public StudioPanePanel(String title) {
		this.title = title;
	}

	@Override
	public Bitmap render() {
		Bitmap bitmap = new Bitmap(width, height);
		bitmap.blendFill(0, 0, width, height, 0xff232934);
		bitmap.box(0, 0, width - 1, height - 1, 0xff5e6a7d);
		bitmap.fill(0, 0, width - 1, HEADER_HEIGHT, 0xff2e3644);
		for (int i = 0; i < count(); i++) {
			GuiComponent component = get(i);
			if (component != null && component.isVisible()) {
				bitmap.draw(component.render(), component.x, component.y);
			}
		}
		Text.render(title, bitmap, 6, 5, 0xffffffff);
		return bitmap;
	}
}
