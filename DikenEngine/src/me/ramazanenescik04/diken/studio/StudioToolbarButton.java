package me.ramazanenescik04.diken.studio;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.gui.compoment.Button;
import me.ramazanenescik04.diken.gui.compoment.Text;
import me.ramazanenescik04.diken.resource.Bitmap;

public class StudioToolbarButton extends Button {
	private static final long serialVersionUID = 1L;
	
	private String toolInfo;

	public StudioToolbarButton(Bitmap icon, int x, int y, int width, int height, String toolInfo) {
		super("", x, y, width, height);
		setButtonIcon(icon);
		
		this.toolInfo = toolInfo;
	}

	@Override
	public Bitmap render() {
		Bitmap bitmap = new Bitmap(width, height);
		bitmap.blendDraw(createButtonTexture(), 0, 0, bColor);
		
		Bitmap icon = getButtonIcon();
		bitmap.draw(icon, (width / 2) - (icon.w / 2), (height / 2) - (icon.h / 2));
		
		if (this.isTouching) {
			bitmap.box(0, 0, width - 1, height - 1, 0xffFFDF00);
		}
		
		return bitmap;
	}
	
	public Bitmap renderToolInfo() {
		if (toolInfo == null && toolInfo.isBlank()) {
			return null;
		}
		
		var font = DikenEngine.getEngine().defaultFont;
		Bitmap bitmap = new Bitmap(Text.stringBitmapWidth(toolInfo, font), Text.stringBitmapAverageHeight(toolInfo, font));
		bitmap.drawText(toolInfo, 0, 0, false);
		return bitmap;
	}
}
