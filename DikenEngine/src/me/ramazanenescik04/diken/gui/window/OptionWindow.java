package me.ramazanenescik04.diken.gui.window;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.gui.UniFont;
import me.ramazanenescik04.diken.gui.compoment.Button;
import me.ramazanenescik04.diken.gui.compoment.Panel;
import me.ramazanenescik04.diken.gui.compoment.Text;
import me.ramazanenescik04.diken.gui.screen.StaticBackground;
import me.ramazanenescik04.diken.resource.*;

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
	
	public static final int DEFAULT_OPTION       = 0;
	public static final int CANCEL_OPTION        = -1;
	public static final int CLOSED_OPTION        = -2;
	public static final int YES_OPTION           = 1;
	public static final int NO_OPTION            = 2;
	
	
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
						((OptionWindow)parent).clickedOption = OptionWindow.YES_OPTION;
						((OptionWindow)parent).onCloseFutureRunnable.run();
					}));
					this.add(new Button("No", 70, 0, 60, 20).setRunnable(() -> {
						((OptionWindow)parent).clickedOption = OptionWindow.NO_OPTION;
						((OptionWindow)parent).onCloseFutureRunnable.run();
					}));
					break;
					
				case YES_NO_CANCEL_OPTION:
					this.add(new Button("Yes", 0, 0, 60, 20).setRunnable(() -> {
						((OptionWindow)parent).clickedOption = OptionWindow.YES_OPTION;
						((OptionWindow)parent).onCloseFutureRunnable.run();
					}));
					this.add(new Button("No", 70, 0, 60, 20).setRunnable(() -> {
						((OptionWindow)parent).clickedOption = OptionWindow.NO_OPTION;
						((OptionWindow)parent).onCloseFutureRunnable.run();
					}));
					this.add(new Button("Cancel", 140, 0, 60, 20).setRunnable(() -> {
						((OptionWindow)parent).clickedOption = OptionWindow.CANCEL_OPTION;
						((OptionWindow)parent).onCloseFutureRunnable.run();
					}));
					break;
					
				case OK_CANCEL_OPTION:
					this.add(new Button("OK", 0, 0, 60, 20).setRunnable(() -> {
						((OptionWindow)parent).clickedOption = OptionWindow.OK_OPTION;
						((OptionWindow)parent).onCloseFutureRunnable.run();
					}));
					this.add(new Button("Cancel", 70, 0, 60, 20).setRunnable(() -> {
						((OptionWindow)parent).clickedOption = OptionWindow.CANCEL_OPTION;
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
		String message = ((OptionPanel)this.getContentPane()).message;
		int w = Text.stringBitmapAverageWidth(message, defaultFont);
		int h = Text.stringBitmapAverageHeight(message, defaultFont);
		int width = w;
		int height = (int)message.lines().count() * 2 * h;
		
		if (width < 100)
			width = 100;
		
		if (height < 50)
			height = 50;
		
		if (width > maxWidth) {
			width = maxWidth;
			
			message = Text.wordWrapString(message, maxWidth - 25, defaultFont);
		}
		
		((OptionPanel)this.getContentPane()).message = message;
		
		this.setSize(width + 35, height + 35);
	}
	
	public void resized() {
		setSizeAuto();
		super.resized();
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
			(DikenEngine.getEngine().getWidth() / 2 - window.getWidth() / 2) ,
			(DikenEngine.getEngine().getHeight()  / 2 - window.getHeight()  / 2)
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
}
