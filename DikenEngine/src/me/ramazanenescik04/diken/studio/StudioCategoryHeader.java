package me.ramazanenescik04.diken.studio;

import me.ramazanenescik04.diken.game.SettingCategory;
import me.ramazanenescik04.diken.gui.compoment.GuiComponent;
import me.ramazanenescik04.diken.gui.compoment.Text;
import me.ramazanenescik04.diken.resource.Bitmap;

public class StudioCategoryHeader extends GuiComponent {
	private static final long serialVersionUID = 1L;
	private final SettingCategory.SettingKey key;

	public StudioCategoryHeader(SettingCategory.SettingKey key, int x, int y, int width, int height) {
		super(x, y, width, height);
		this.key = key;
	}

	@Override
	public Bitmap render() {
		Bitmap bitmap = new Bitmap(width, height);
		bitmap.fill(0, 0, width, height, 0xff2e3644);
		bitmap.box(0, 0, width - 1, height - 1, 0xff5e6a7d);
		if (key.getImage() != null) {
			bitmap.draw(key.getImage(), 4, 1);
		}
		Text.render(key.getCategory(), bitmap, 22, 5, 0xffffffff);
		return bitmap;
	}
}
