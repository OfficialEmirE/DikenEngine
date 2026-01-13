package me.ramazanenescik04.diken.gui.window;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.gui.compoment.*;
import me.ramazanenescik04.diken.gui.screen.StaticBackground;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

public class SettingsWindow extends Window {
	private static final long serialVersionUID = 1L;

	public SettingsWindow() {
		super(2, 2, 200, 200);
		this.setTitle("Settings");
		ArrayBitmap icons = (ArrayBitmap) ResourceLocator.getResource("win-icons");
		this.setIcon(icons.getBitmap(10, 0));
	}
	
	public void open() {
		DikenEngine engine = DikenEngine.getEngine();
		this.setLocation(engine.getWidth() / 2 - this.width / 2, engine.getHeight() / 2 - this.height / 2);
		this.getContentPane().setBackground(new StaticBackground(Bitmap.createClearedBitmap(16, 16, 0xffa0a0a0)));
		Panel panel = this.getContentPane();
		panel.add(new Text("Üzgünüm. burda şu an birşey yok.", 2, 2));
	}

}
