package me.ramazanenescik04.diken;

import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import me.ramazanenescik04.diken.game.Animation;
import me.ramazanenescik04.diken.game.Config;
import me.ramazanenescik04.diken.gui.UniFont;
import me.ramazanenescik04.diken.gui.screen.DefaultMainMenuScreen;
import me.ramazanenescik04.diken.gui.screen.Screen;
import me.ramazanenescik04.diken.gui.window.ConsoleWindow;
import me.ramazanenescik04.diken.gui.window.WindowManager;
import me.ramazanenescik04.diken.input.IInputListener;
import me.ramazanenescik04.diken.input.InputHandler;
import me.ramazanenescik04.diken.log.ConsoleLog;
import me.ramazanenescik04.diken.log.ConsoleLog.LogType;
import me.ramazanenescik04.diken.renderer.RendererPanel;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.CursorResource;
import me.ramazanenescik04.diken.resource.EnumResource;
import me.ramazanenescik04.diken.resource.FrameBitmapPool;
import me.ramazanenescik04.diken.resource.IOResource;
import me.ramazanenescik04.diken.resource.IResource;
import me.ramazanenescik04.diken.resource.Language;
import me.ramazanenescik04.diken.resource.ResourceLocator;
import me.ramazanenescik04.diken.tools.Utils;

/**
 * Represents the `DikenEngine` type within the DikenEngine `core` package.
 */
public class DikenEngine implements Runnable, IInputListener {
	public static final String VERSION = "2.2.0";
	public static final int protocolVersion = 21;

	private static DikenEngine instance;

	private JFrame engineWindow;
	private boolean fullscreen;

	private boolean running = false;
	private final RendererPanel rendererPanel;

	private int width;
	private int height;
	private int scale = 1;
	private final int baseWidth;
	private final int baseHeight;
	public int currentFPS = -1;

	public UniFont defaultFont;
	public WindowManager wManager;
	private Screen currentScreen;

	public InputHandler input;
	public CursorResource cursorResource;
	public Config config = new Config();
	
	private int tmpW;
	private int tmpH;

	public DikenEngine(int width, int height, int scale) {
		this.config.setSetting("guiScale", scale);
		this.config.loadConfig();

		this.width = tmpW = width;
		this.height = tmpH = height;
		this.baseWidth = width;
		this.baseHeight = height;
		this.scale = scale;
		this.rendererPanel = new RendererPanel(width, height);

		this.engineWindow = new JFrame("DikenEngine");
		this.engineWindow.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				stop();
			}
		});
		this.engineWindow.add(this.rendererPanel);
		this.engineWindow.pack();
		this.engineWindow.setLocationRelativeTo(null);

		instance = this;
	}

	public void start() {
		running = true;
		this.engineWindow.setVisible(true);
		this.rendererPanel.requestFocusInWindow();
		new Thread(this, "DikenEngine Thread").start();
	}

	public void stop() {
		running = false;
	}

	public void setCurrentScreen(Screen screen) {
		if (currentScreen != null) {
			currentScreen.closeScreen();
		}
		
		log("Opening screen: " + (screen != null ? screen.getClass().getSimpleName() : "null"));
		
		if (screen != null) {
			screen.engine = this;
			screen.openScreen();
		}

		this.currentScreen = screen;
		
		System.gc();
	}

	public Screen getCurrentScreen() {
		return this.currentScreen;
	}

	public void setCursor(CursorResource cursor) {
		this.cursorResource = cursor;
	}

	public int getScaledWidth() {
		return getInternalRenderWidth() / scale;
	}

	public int getScaledHeight() {
		return getInternalRenderHeight() / scale;
	}

	public int getScale() {
		return scale;
	}

	private int getInternalRenderWidth() {
		return isFixedInternalResolutionEnabled() ? baseWidth : width;
	}

	private int getInternalRenderHeight() {
		return isFixedInternalResolutionEnabled() ? baseHeight : height;
	}

	private boolean isFixedInternalResolutionEnabled() {
		return this.config.getSetting("fixedInternalResolution", Boolean.class).getValue();
	}
	
	public void setFullscreen(boolean bool) {
		this.fullscreen = !bool;
		this.toggleFullscreen();
	}
	
	public boolean getFullscreen() {
		return this.fullscreen;
	}
	
	public static void log(ConsoleLog.LogType logType, String message) {
		if (logType == LogType.CLIENT_ERROR || logType == LogType.SERVER_ERROR) {
			System.err.println("[" + logType.toString() + "] " + message);
		} else {
			System.out.println("[" + logType.toString() + "] " + message);
		}
		ConsoleLog.sendLog(logType, message);
	}

	public static void log(String message) {
		log(LogType.CLIENT_DEFAULT, message);
	}

	public static void errorLog(String message) {
		log(LogType.CLIENT_ERROR, message);
	}

	@Override
	public void run() {
		try {
			log("Starting DikenEngine " + VERSION + " (Protocol: " + protocolVersion + ")");

			defaultFont = UniFont.getFont("default_font");
			wManager = new WindowManager();

			input = new InputHandler(rendererPanel);
			input.addListener(this);

			rendererPanel.acquireFrameBuffer(getScaledWidth(), getScaledHeight());

			long fixedUpdateTime = 1000000000L / 60;
			long lastUpdateTime = System.nanoTime();
			long lastFPSTime = System.currentTimeMillis();
			double accumulator = 0;
			int frames = 0;
			while (running) {
				long currentTime = System.nanoTime();
				long updateDelta = currentTime - lastUpdateTime;
				accumulator += updateDelta;
				lastUpdateTime = currentTime;

				while (accumulator >= fixedUpdateTime) {
					tick();
					accumulator -= fixedUpdateTime;
				}

				Bitmap frameBitmap = rendererPanel.acquireFrameBuffer(getScaledWidth(), getScaledHeight());
				FrameBitmapPool.beginFrame();
				render(frameBitmap);
				frames++;

				if (System.currentTimeMillis() - lastFPSTime >= 1000) {
					currentFPS = frames;
					frames = 0;
					lastFPSTime = System.currentTimeMillis();
				}

				rendererPanel.present(scale);
			}
		} catch (Throwable e) {
			e.printStackTrace();
			crash(e);
		} finally {
			log("Closing DikenEngine...");

			if (currentScreen != null) {
				currentScreen.closeScreen();
			}

			config.saveConfig();
			wManager.closeAll();
			engineWindow.dispose();

			ConsoleLog.saveLogs();
			System.gc();
		}
	}

	@Override
	public void keyHandled(int inputMode, int key, char character) {
		if (inputMode == InputHandler.INPUT_PRESSED) {
			if (key == KeyEvent.VK_F12) {
				try {
					String fileName = "screenshot-"
							+ new Date().toString().replaceAll(" ", "_").replaceAll(":", "-") + ".png";

					File pathFile = new File(this.config.getSetting("screenshotPath", String.class).getValue());
					pathFile.mkdirs();

					File file = new File(pathFile, fileName);
					ImageIO.write(rendererPanel.getFrameSnapshot().toImage(), "png", file);

					log("Screenshot saved as " + fileName);
				} catch (IOException e) {
					e.printStackTrace();
					log("Screenshot failed to save.");
				}
			}

			if (key == KeyEvent.VK_F3) {
				this.config.setSetting("debug", !this.config.getSetting("debug", Boolean.class).getValue());
			}

			if (key == KeyEvent.VK_F9) {
				if (wManager.isWindowVaild(ConsoleWindow.class)) {
					return;
				}
				wManager.addWindow(new ConsoleWindow(2, 2, 200, 200));
			}
			
			if (key == KeyEvent.VK_F11) {
				toggleFullscreen();
			}
		}

		if (currentScreen != null) {
			currentScreen.keyboardEvent(inputMode, key, character);
		}

		wManager.keyboardEvent(inputMode, key, character);
	}

	private void toggleFullscreen() {
		this.fullscreen = !this.fullscreen;
		
		if (this.fullscreen) {
			this.tmpW = width;
			this.tmpH = height;
			engineWindow.dispose();

			engineWindow.setExtendedState(JFrame.MAXIMIZED_BOTH);
			if (!engineWindow.isUndecorated()) {
				engineWindow.setUndecorated(true);
			}
			engineWindow.setVisible(true);
			rendererPanel.requestFocus();
			engineWindow.toFront();
			// engineWindow.requestFocus();
		} else {
			rendererPanel.setSize(tmpW, tmpH);
			engineWindow.pack();
			engineWindow.setLocationRelativeTo(null);

			engineWindow.dispose();

			engineWindow.setExtendedState(JFrame.NORMAL);
			if (engineWindow.isUndecorated()) {
				engineWindow.setUndecorated(false);
			}
			engineWindow.setVisible(true);
			rendererPanel.requestFocus();
			engineWindow.toFront();
			// engineWindow.requestFocus();
		}
	}

	@Override
	public void mouseHandled(int inputMode, int x, int y, int clicked) {
		if (currentScreen != null) {
			currentScreen.mouseEvent(inputMode, x, y, clicked);
		}

		wManager.mouseEvent(inputMode, x, y, clicked);
	}

	private void render(Bitmap bitmap) {
		if (currentScreen != null) {
			currentScreen.render(bitmap);
		}

		wManager.render(bitmap);

		if (this.config.getSetting("debug", Boolean.class).getValue()) {
			bitmap.blendFill(0, 0, 120, 62, 0x2f000000);
			bitmap.drawText("DikenEngine " + VERSION, 2, 2, false);
			bitmap.drawText("FPS: " + currentFPS, 2, 12, false);
			bitmap.drawText("Screen: " + (currentScreen != null ? currentScreen.getClass().getSimpleName() : "null (No Screen)"), 2,
					22, false);
			bitmap.drawText("Width: " + getScaledWidth() + " Height: " + getScaledHeight(), 2, 32, false);
			bitmap.drawText("Scale: " + this.getScale(), 2, 42, false);
			java.awt.Point point = input.getMousePosition();
			bitmap.drawText("Mouse: " + point.x + ", " + point.y, 2, 52, false);

			Runtime runtime = Runtime.getRuntime();
			long totalMemory = runtime.totalMemory();
			long freeMemory = runtime.freeMemory();
			long usedMemory = totalMemory - freeMemory;
			long maxMemory = runtime.maxMemory();

			int percentageUse = Utils.toProccesBarValue(usedMemory, maxMemory, 94);
			bitmap.blendFill(getScaledWidth() - 120, 0, getScaledWidth(), 52, 0x2f000000);
			bitmap.drawText("Used Memory: " + usedMemory / 1024 / 1024 + " MB", getScaledWidth() - 118, 2, 0xffffffff, false);
			bitmap.box(getScaledWidth() - 118, 12, getScaledWidth() - 30, 20, 0xffffffff);
			bitmap.fill(getScaledWidth() - 117, 13, (getScaledWidth() - 117) + percentageUse, 19,
					!(percentageUse > 85) ? 0xff00ff00 : 0xffff0000);
			bitmap.drawText("%" + usedMemory * 100L / maxMemory, getScaledWidth() - 26, 13, false);
			bitmap.drawText("Max Memory: " + (maxMemory / 1024 / 1024) + " MB", getScaledWidth() - 118, 22, false);
		} else {
			/*bitmap.drawText("DikenEngine " + VERSION, 2, 2, false);
			bitmap.drawText("Bu sürüm deneysel sürümdür! Hatalar olabilir!", 2, 12, false);
			bitmap.blendFill(0, 0, 300, 22, 0x2f000000);*/
		}
		
		//System.gc();
	}

	private void tick() {
		input.update();

		checkResized();

		if (currentScreen != null) {
			currentScreen.tick();
		}
		wManager.tick();

		if (this.cursorResource == null && rendererPanel.getCursor() != Cursor.getDefaultCursor()) {
			rendererPanel.setCursor(Cursor.getDefaultCursor());
		} else if (this.cursorResource != null) {
			Cursor cursor = this.cursorResource.getCursor();

			if (cursor != rendererPanel.getCursor()) {
				rendererPanel.setCursor(cursor);
			}
		}
	}

	private void checkResized() {
		boolean needsResize = false;

		if (rendererPanel.getWidth() != this.width || rendererPanel.getHeight() != this.height) {
			needsResize = true;
		} else if (this.getScale() != this.config.getSetting("guiScale", Integer.class).getValue()) {
			this.scale = this.config.getSetting("guiScale", Integer.class).getValue();
			needsResize = true;
		}

		if (needsResize) {
			this.resize();
		}
	}

	private void resize() {
		this.width = rendererPanel.getWidth();
		this.height = rendererPanel.getHeight();

		if (this.width <= 0) {
			this.width = 1;
		}

		if (this.height <= 0) {
			this.height = 1;
		}

		rendererPanel.acquireFrameBuffer(getScaledWidth(), getScaledHeight());

		if (this.currentScreen != null) {
			currentScreen.resized();
		}
	}

	private static void loadLocalImages() {
		IResource icon_x16 = IOResource.loadResource(DikenEngine.class.getResourceAsStream("/icon-x16.png"),
				EnumResource.IMAGE);
		ResourceLocator.addResource("icon-x16", icon_x16);

		ArrayBitmap button = new ArrayBitmap();
		button.bitmap = IOResource.loadResourceAndCut(DikenEngine.class.getResourceAsStream("/button.png"), 16, 16);
		ResourceLocator.addResource("button-array", button);

		ArrayBitmap checkBox = new ArrayBitmap();
		checkBox.bitmap = IOResource.loadResourceAndCut(DikenEngine.class.getResourceAsStream("/check_box.png"), 16, 16);
		ResourceLocator.addResource("checkbox-array", checkBox);

		ArrayBitmap bg_tiles = new ArrayBitmap();
		bg_tiles.bitmap = IOResource.loadResourceAndCut(DikenEngine.class.getResourceAsStream("/background_tiles.png"),
				32, 32);
		ResourceLocator.addResource("bgd-tiles", bg_tiles);

		ArrayBitmap batteryImage = new ArrayBitmap();
		batteryImage.bitmap = IOResource.loadResourceAndCut(DikenEngine.class.getResourceAsStream("/battery.png"), 16,
				8);
		ResourceLocator.addResource("battery-image", batteryImage);

		ArrayBitmap win_icons = new ArrayBitmap();
		win_icons.setArray(
				IOResource.loadResourceAndCut(IOResource.createClassResourceStream("/win_icons.png"), 16, 16));
		ResourceLocator.addResource("win-icons", win_icons);

		ResourceLocator.addResource("editor_icons", new ArrayBitmap(IOResource.loadResourceAndCut(IOResource.createClassResourceStream("/editor_icons.png"), 16, 16)));

		ArrayBitmap win_cursors = new ArrayBitmap();
		win_cursors
				.setArray(IOResource.loadResourceAndCut(IOResource.createClassResourceStream("/scl_cur.png"), 32, 32));

		for (int j = 0; j < 3; j++) {
			CursorResource cursor = new CursorResource();
			cursor.cursorBitmap = win_cursors.bitmap[0][j];
			ResourceLocator.addResource("cursor-" + j, cursor);
		}

		Language lang = Language.i;
		lang.addLangValue("tr-TR", "dmainmenu.reportbug=Hata Bildir");
		lang.addLangValue("en-US", "dmainmenu.reportbug=Report Bug");
		
		Bitmap def_body = (Bitmap)IOResource.loadResource(DikenEngine.class.getResourceAsStream("/default_c3/body.png"), EnumResource.IMAGE);
		Bitmap def_hand = (Bitmap)IOResource.loadResource(DikenEngine.class.getResourceAsStream("/default_c3/hand.png"), EnumResource.IMAGE);
		Bitmap def_face = (Bitmap)IOResource.loadResource(DikenEngine.class.getResourceAsStream("/default_c3/face.png"), EnumResource.IMAGE);
		
		ArrayBitmap def_avatar = new ArrayBitmap(new Bitmap[][] { { def_body, def_hand, def_face } });
		ResourceLocator.addResource(new ResourceLocator.ResourceKey("capsule", "default_avatar"), (IResource)def_avatar);
		
		Animation leftWalkAnim = (Animation)IOResource.loadResource(DikenEngine.class.getResourceAsStream("/default_c3/animation/walkanim-left.bin"), 
		    EnumResource.ANIMATION);
		Animation rightWalkAnim = (Animation)IOResource.loadResource(DikenEngine.class.getResourceAsStream("/default_c3/animation/walkanim-right.bin"), 
		    EnumResource.ANIMATION);
		
		Animation idleAnim = (Animation)IOResource.loadResource(DikenEngine.class.getResourceAsStream("/default_c3/animation/idleanim.bin"), 
			EnumResource.ANIMATION);
		
		ResourceLocator.addResource(new ResourceLocator.ResourceKey("capsule", "leftWalkAnim"), (IResource)leftWalkAnim);
		ResourceLocator.addResource(new ResourceLocator.ResourceKey("capsule", "rightWalkAnim"), (IResource)rightWalkAnim);
		ResourceLocator.addResource(new ResourceLocator.ResourceKey("capsule", "idleAnim"), (IResource)idleAnim);
		
		ArrayBitmap menu_buttons = new ArrayBitmap(IOResource.loadResourceAndCut(DikenEngine.class.getResourceAsStream("/menubuttons.png"), 16, 16));
		ResourceLocator.addResource(new ResourceLocator.ResourceKey("capsule", "menu_buttons"), (IResource)menu_buttons);
	}

	public static DikenEngine getEngine() {
		return instance;
	}

	public static void main(String[] args) {
		DikenEngine engine = new DikenEngine(800, 600, 2);
		engine.setCurrentScreen(new DefaultMainMenuScreen());
		engine.start();
	}

	static {
		loadLocalImages();
		UniFont.createFont("default_font");
	}

	private void crash(Throwable e) {
		ConsoleLog.sendLog(LogType.CLIENT_ERROR, Utils.getStackTraceString(e));
		ConsoleLog.saveLogs();

		String title = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage().trim();
		StringBuilder desc = new StringBuilder();
		desc.append("DikenEngine Version: ").append(VERSION).append("\n");
		desc.append("Protocol Version: ").append(protocolVersion).append("\n");
		desc.append("Java Version: ").append(System.getProperty("java.version")).append("\n");
		desc.append("OS: ").append(System.getProperty("os.name")).append(" (").append(System.getProperty("os.arch"))
				.append(")\n\n");
		desc.append("Date: ").append(new Date()).append("\n");

		String[] errorLines = Utils.getStackTraceStringArray(e);
		if (errorLines.length > 0) {
			desc.append("Error: ").append(errorLines[0]).append("\n");
		}

		if (errorLines.length > 1) {
			desc.append("Stack Trace:\n");
			for (String line : errorLines) {
				desc.append(line).append("\n");
			}
		} else {
			desc.append("No stack trace available.\n");
		}

		JOptionPane.showMessageDialog(null, desc.toString().trim(), title, JOptionPane.ERROR_MESSAGE);
	}
}
