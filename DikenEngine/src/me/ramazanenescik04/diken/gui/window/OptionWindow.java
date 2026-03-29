package me.ramazanenescik04.diken.gui.window;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.function.Consumer;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.gui.UniFont;
import me.ramazanenescik04.diken.gui.compoment.Button;
import me.ramazanenescik04.diken.gui.compoment.GuiComponent;
import me.ramazanenescik04.diken.gui.compoment.Panel;
import me.ramazanenescik04.diken.gui.compoment.Text;
import me.ramazanenescik04.diken.gui.screen.StaticBackground;
import me.ramazanenescik04.diken.resource.*;

/**
 * Represents the `OptionWindow` type within the DikenEngine `gui.window` package.
 */
public class OptionWindow extends Window {
	private static final long serialVersionUID = 2373782406506701304L;
	
	public static final int INFO_MESSAGE         = 0;
	public static final int ERROR_MESSAGE        = 1;
	public static final int WARNING_MESSAGE      = 2;
	public static final int PLAIN_MESSAGE        = 3;
	
	public static final int YES_NO_CANCEL_OPTION = 1;
	public static final int OK_CANCEL_OPTION     = 2;
	public static final int YES_NO_OPTION        = 3;
	public static final int OK_OPTION            = 0;
	
	public static final int OK_BUTTON            = 0;
	public static final int CANCEL_BUTTON        = 1;
	public static final int YES_BUTTON           = 2;
	public static final int NO_BUTTON            = 3;
	
	
	private int maxWidth = 200;
	private int clickedOption = -1;
	private int optionType = OK_OPTION;
	private FutureTask<Integer> onCloseFutureRunnable = null;

	public OptionWindow(String message, String title, Bitmap icon, int optionType) {
		super(2, 2, 100, 50);
		if (icon == null)
			icon = IOResource.missingTexture;

		this.setTitle(title);
		this.setIcon(icon);
		this.optionType = optionType;
		this.setContentPane(new OptionPanel(icon, message, this));
		this.onCloseFutureRunnable = new java.util.concurrent.FutureTask<Integer>(() -> {
			this.close();
			return clickedOption;
		});
	}
	
	public OptionWindow(String message, String title, int messageType, int optionType) {
		this(message, title, null, optionType);
		switch (messageType) {
			case INFO_MESSAGE:
				this.setIcon(((ArrayBitmap)ResourceLocator.getResource("win-icons")).getBitmap(5, 0));
				break;
				
			case ERROR_MESSAGE:
				this.setIcon(((ArrayBitmap)ResourceLocator.getResource("win-icons")).getBitmap(2, 0));
				break;
				
			case WARNING_MESSAGE:
				this.setIcon(((ArrayBitmap)ResourceLocator.getResource("win-icons")).getBitmap(4, 0));
				break;
				
			case PLAIN_MESSAGE:
				this.setIcon(((ArrayBitmap)ResourceLocator.getResource("win-icons")).getBitmap(3, 0));
				break;
		}
		((OptionPanel)this.getContentPane()).icon = (this.getIcon());
	}

	protected void open() {
		resized();
	}
	
	private static class OptionPanel extends Panel {

		private static final long serialVersionUID = 1L;

		public String message;
		public Bitmap icon;
		private OptionWindow parent;
		
		public OptionPanel(Bitmap icon, String message, OptionWindow parent) {
			this.message = message;
			this.icon = icon;
			this.parent = parent;
		}

		public void init(DikenEngine engine) {
			setBackground(new StaticBackground(Bitmap.createClearedBitmap(16, 16, 0xffa0a0a0)));
			
			switch (parent.optionType) {
				case YES_NO_OPTION:
					this.add(new Button("Yes", 0, 0, 60, 20).setRunnable(() -> {
						((OptionWindow)parent).clickedOption = OptionWindow.YES_BUTTON;
						((OptionWindow)parent).onCloseFutureRunnable.run();
					}));
					this.add(new Button("No", 70, 0, 60, 20).setRunnable(() -> {
						((OptionWindow)parent).clickedOption = OptionWindow.NO_BUTTON;
						((OptionWindow)parent).onCloseFutureRunnable.run();
					}));
					break;
					
				case YES_NO_CANCEL_OPTION:
					this.add(new Button("Yes", 0, 0, 60, 20).setRunnable(() -> {
						((OptionWindow)parent).clickedOption = OptionWindow.YES_BUTTON;
						((OptionWindow)parent).onCloseFutureRunnable.run();
					}));
					this.add(new Button("No", 70, 0, 60, 20).setRunnable(() -> {
						((OptionWindow)parent).clickedOption = OptionWindow.NO_BUTTON;
						((OptionWindow)parent).onCloseFutureRunnable.run();
					}));
					this.add(new Button("Cancel", 140, 0, 60, 20).setRunnable(() -> {
						((OptionWindow)parent).clickedOption = OptionWindow.CANCEL_BUTTON;
						((OptionWindow)parent).onCloseFutureRunnable.run();
					}));
					break;
					
				case OK_CANCEL_OPTION:
					this.add(new Button("OK", 0, 0, 60, 20).setRunnable(() -> {
						((OptionWindow)parent).clickedOption = OptionWindow.OK_BUTTON;
						((OptionWindow)parent).onCloseFutureRunnable.run();
					}));
					this.add(new Button("Cancel", 70, 0, 60, 20).setRunnable(() -> {
						((OptionWindow)parent).clickedOption = OptionWindow.CANCEL_BUTTON;
						((OptionWindow)parent).onCloseFutureRunnable.run();
					}));
					break;
					
				case OK_OPTION:
					this.add(new Button("OK", 0, 0, 60, 20).setRunnable(() -> {
						((OptionWindow)parent).onCloseFutureRunnable.run();
					}));
					break;
			}
			
		}
		
		public Bitmap render() {
			Bitmap bitmap = super.render();
			bitmap.draw(icon, 2, 2);
			Text.render(message, bitmap, 25, 2);
			return bitmap;
		}
	}
	
	private void setSizeAuto() {
	    UniFont defaultFont = DikenEngine.getEngine().defaultFont;
	    OptionPanel contentPane = (OptionPanel) this.getContentPane();
	    String message = contentPane.message;

	    // 1. Mesaj boyutlarını hesapla
	    int messageWidth = Text.stringBitmapAverageWidth(message, defaultFont);
	    int messageHeight = (int) message.lines().count() * 2 * Text.stringBitmapAverageHeight(message, defaultFont);

	    // 2. Butonların toplam genişliğini hesapla
	    int totalComponentsWidth = 0;
	    int maxComponentHeight = 0;
	    int padding = 10;
	    int margin = 6;

	    for (int i = 0; i < contentPane.count(); i++) {
	        GuiComponent comp = contentPane.get(i);
	        totalComponentsWidth += comp.width + (i > 0 ? padding : 0);
	        maxComponentHeight = Math.max(maxComponentHeight, comp.height);
	    }
	    totalComponentsWidth += (margin * 2); // Kenar boşlukları

	    // 3. Genişliği hem mesaj hem butonlara göre belirle
	    int width = Math.max(messageWidth, totalComponentsWidth);
	    int height = messageHeight + maxComponentHeight + 20; // Mesaj + Butonlar + Boşluk

	    // Minimum sınırlar
	    width = Math.max(width, 100);
	    height = Math.max(height, 50);

	    // MaxWidth kontrolü ve WordWrap
	    if (width > maxWidth) {
	        width = maxWidth;
	        message = Text.wordWrapString(message, maxWidth - 35, defaultFont);
	        contentPane.message = message;
	        // Wrap sonrası yüksekliği tekrar güncellemek gerekebilir
	        height = ((int) message.lines().count() * 2 * Text.stringBitmapAverageHeight(message, defaultFont)) + maxComponentHeight + 20;
	    }

	    // 4. Boyutu set et (Önce setSize yapmalısın ki buton konumları panel boyutuna göre düzgün hesaplansın)
	    this.setSize(width + 35, height + 35);

	    // 5. Bileşenleri (Butonları) yatayda ortalayarak veya hizalı diz
	    int currentX = margin;
	    for (int i = 0; i < contentPane.count(); i++) {
	        GuiComponent comp = contentPane.get(i);
	        comp.x = currentX;
	        // Y pozisyonunu güncellenmiş panel boyuna göre ayarla
	        comp.y = (this.getContentPane().getHeight() - comp.height - margin);
	        currentX += comp.width + padding;
	    }
	}
	
	public void resized() {
		super.resized();
		setSizeAuto();
	}

	/**
	 * 
	 * @param message
	 * @param title
	 * @param messageType
	 * @return clicked button index
	 */
	public static int showMessage(String message, String title, int messageType, int optionType) {
		if (title == null)
			title = "Option Window";
		
		if (message == null)
			message = "No message";
		
		OptionWindow window = new OptionWindow(message, title, messageType, optionType);
		window.setSizeAuto();
		
		window.setLocation(
			(DikenEngine.getEngine().getScaledWidth() / 2 - window.getWidth() / 2) ,
			(DikenEngine.getEngine().getScaledHeight()  / 2 - window.getHeight()  / 2)
		);
		
		DikenEngine.getEngine().wManager.addWindow(window);
		
		Future<Integer> future = window.onCloseFutureRunnable;
		while (!future.isDone()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		try {
			return future.get();
		} catch (InterruptedException | ExecutionException e) {
			return -1;
		}
	}
	
	public static void showMessageNoWait(String message, String title, int messageType, int optionType, Consumer<Integer> consumer) {		
		Thread thread = new Thread(() -> {
			int clicked = showMessage(message, title, messageType, optionType);
			if (consumer != null) {
				consumer.accept(clicked);
			}
		}, "OptionWindow-Wait-Thread-" + Math.random());
		thread.setDaemon(true);
		thread.start();
	}
}
