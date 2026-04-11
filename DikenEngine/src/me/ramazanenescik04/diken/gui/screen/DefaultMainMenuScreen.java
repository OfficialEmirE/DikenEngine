package me.ramazanenescik04.diken.gui.screen;

import java.net.URI;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.Timer;
import me.ramazanenescik04.diken.gui.compoment.Button;
import me.ramazanenescik04.diken.gui.compoment.LinkButton;
import me.ramazanenescik04.diken.gui.compoment.Panel;
import me.ramazanenescik04.diken.gui.compoment.Text;
import me.ramazanenescik04.diken.gui.window.AboutWindow;
import me.ramazanenescik04.diken.gui.window.SettingsWindow;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.Language;
import me.ramazanenescik04.diken.resource.ResourceLocator;
import me.ramazanenescik04.diken.tools.PixelToColor;

/**
 * Represents the `DefaultMainMenuScreen` type within the DikenEngine `gui.screen` package.
 */
public class DefaultMainMenuScreen extends Screen {
	
	private Timer darknessTimer; // Zamanla ekran karanlıklaşacak
	private int alpha = 1; // Ekranın başlangıçta görünürlüğü

	public void openScreen() {		
		darknessTimer = new Timer((ct, mt, _, _, isEnd) -> {
			if (isEnd) {
				engine.setCurrentScreen(new WhereAmIScreen());
			} else {
				float t = (float) ct / mt; // 0.0 → 1.0
		        int value = (int) (t * 255);
				alpha = value;
			}
		}, 0, 1200000); // 20 dakika sonra karanlıklaşacak
		ArrayBitmap icon = (ArrayBitmap) ResourceLocator.getResource("bgd-tiles");
		DMMPanel panel = new DMMPanel(engine.getScaledWidth(), engine.getScaledHeight());
		panel.setBackground(new DownBackground(icon.bitmap[0][0]));		
		this.setContentPane(panel);
		darknessTimer.start();
	}
	
	public void render(Bitmap bitmap) {
		super.render(bitmap);
		
		var color = PixelToColor.toColor(alpha, 0, 0, 0);
		
		bitmap.blendFill(0, 0, engine.getScaledWidth(), engine.getScaledHeight(), color);
	}
	
	public void closeScreen() {
		darknessTimer.stop();
	}
	
	public void resized() {
		this.getContentPane().get(4).setLocation(10, engine.getScaledHeight() - (1 * 20));
		this.getContentPane().get(5).setLocation(10, engine.getScaledHeight() - (2 * 20));
		this.getContentPane().get(6).setLocation(110, engine.getScaledHeight() - (2 * 20));
	}
	
	private class DMMPanel extends Panel {
		private static final long serialVersionUID = 1L;

		public DMMPanel(int width, int height) {
			super(0, 0, width, height);
		}
		
		public Bitmap render() {
			Bitmap bitmap = super.render();
			bitmap.draw((Bitmap) ResourceLocator.getResource("icon-x16"), 10, (100 - 32) - 10);
			Text.render("DikenEngine", bitmap, (10 + 32) + 2, (100 - 32) - 10);
			Text.render(DikenEngine.VERSION, bitmap, (10 + 32) + 2, ((100 - 32) - 10) + 9);
			return bitmap;
		}

		public void init(DikenEngine engine) {
			Language lang = Language.i;
			
			clear();
			add(new Button(lang.languageValue("Demo Ekranını Aç"), 10, 100, 200, 20).setRunnable(() -> {
				engine.setCurrentScreen(new GameScreen(DefaultMainMenuScreen.this));
			}));
			add(new Button("Multiplayer Test", 10, 120, 200, 20).setRunnable(() -> {
			}));
			add(new Button(lang.languageValue("dmainmenu.setting"), 10, 140, 200, 20).setRunnable(() -> {
				engine.wManager.addWindow(new SettingsWindow()); 
			}));
			add(new Button(lang.languageValue("dmainmenu.exit"), 10, 160, 200, 20).setRunnable(() -> {
				engine.stop();
			}));
			add(new LinkButton("Github", 10, engine.getScaledHeight() - (1 * 20), 200, 20).setURI(URI.create("https://github.com/Ramazanenesisik010/DikenEngine")));
			add(new Button(lang.languageValue("dmainmenu.about"), 10, engine.getScaledHeight() - (2 * 20), 100, 20).setRunnable(() -> {
				engine.wManager.addWindow(new AboutWindow());
			}));
			add(new Button(lang.languageValue("dmainmenu.reportbug"), 110, engine.getScaledHeight() - (2 * 20), 100, 20).setRunnable(() -> {
			}));
		}
		
	}
	
}
