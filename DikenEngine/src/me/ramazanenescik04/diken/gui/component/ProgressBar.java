package me.ramazanenescik04.diken.gui.component;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.FrameBitmapPool;

/**
 * Represents the `ProgressBar` type within the DikenEngine `gui.compoment` package.
 */
public class ProgressBar extends GuiComponent {
	private static final long serialVersionUID = 1L;

	public int value = 100, maxValue = 100;
	public int color = 0xff00ff00, color2 = 0xff00ff00, bgColor = 0xff000000;
	public String text = "";
	
	public ProgressBar(int x, int y, int width, int height) {
		super(x, y, width, height);
	}

	@Override
	public Bitmap render() {
		Bitmap bitmap = super.render();
		bitmap.clear(bgColor);
		bitmap.box(0, 0, bitmap.w - 1, bitmap.h - 1, 0xffffffff);
		
		double progressRatio = (double) value / maxValue;
        int progressWidth = (int) ((width - 2) * progressRatio);
        if (progressWidth <= 1) {
        	progressWidth = 1;
        }
        
        Bitmap progressBar = FrameBitmapPool.newBitmap(progressWidth, height - 2);
        if (!(progressWidth == 1)) {
        	progressBar.drawGradient(color, color2);
        }
        
        bitmap.draw(progressBar, 1, 1);
        
        bitmap.drawText(text.isEmpty() ? value + "%" : text, 4, height / 2 - Text.stringBitmapAverageHeight(text, DikenEngine.getEngine().defaultFont) / 2, false);
		
		return bitmap;
	}

}
