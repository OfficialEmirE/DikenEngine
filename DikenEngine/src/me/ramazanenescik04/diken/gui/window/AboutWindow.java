/**
 * 
 */
package me.ramazanenescik04.diken.gui.window;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.gui.component.Button;
import me.ramazanenescik04.diken.gui.component.Panel;
import me.ramazanenescik04.diken.gui.component.RenderImage;
import me.ramazanenescik04.diken.gui.component.Text;
import me.ramazanenescik04.diken.gui.screen.StaticBackground;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.Language;

/**
 * 
 */
public class AboutWindow extends Window {

	private static final long serialVersionUID = 1L;
	
	/**
	 * 
	 */
	public AboutWindow() {
		super(0, 0, 150, 100);
		
		int w = 150;
		int h = 100;
		int x = (DikenEngine.getEngine().getScaledWidth() - w) / 2;
		int y = (DikenEngine.getEngine().getScaledHeight() - h) / 2;
		
		this.setBounds(x, y, w, h);
		this.setTitle("DikenEngine Hakkında");
	}

	protected void open() {
		Panel contentPane = this.getContentPane();
		Language lang = Language.i;
		
		contentPane.setBackground(new StaticBackground(Bitmap.createClearedBitmap(32, 32, 0xffaaaaaa)));
		contentPane.add(new Button(lang.languageValue("gui.done"), contentPane.width - 76, contentPane.height - 26, 70, 20).setRunnable(() -> {
			this.close();
		}));
		
		Bitmap textBitmap = new Bitmap(150, 100);
		
		Text.render("DikenEngine \n\rYapan: Ramazanenescik04", textBitmap, 2, 2);
		
		contentPane.add(new RenderImage(textBitmap, 2, 2));
		
		
	}

}
