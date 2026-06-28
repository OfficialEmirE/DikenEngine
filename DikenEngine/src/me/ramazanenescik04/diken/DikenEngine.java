package me.ramazanenescik04.diken;

import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.UIManager;

import org.lwjgl.LWJGLException;
import com.formdev.flatlaf.*;

import me.ramazanenescik04.diken.game.Config;
import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.game.services.InputService;
import me.ramazanenescik04.diken.gui.UniFont;
import me.ramazanenescik04.diken.input.IInputListener;
import me.ramazanenescik04.diken.input.InputHandler;
import me.ramazanenescik04.diken.language.Language;
import me.ramazanenescik04.diken.log.ConsoleLog;
import me.ramazanenescik04.diken.log.ConsoleLog.LogType;
import me.ramazanenescik04.diken.renderer.FrameBitmapPool;
import me.ramazanenescik04.diken.renderer.RendererPanel;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.CursorResource;
import me.ramazanenescik04.diken.resource.EnumResource;
import me.ramazanenescik04.diken.resource.IOResource;
import me.ramazanenescik04.diken.resource.IResource;
import me.ramazanenescik04.diken.resource.ResourceLocator;
import me.ramazanenescik04.diken.studio.LoadingDialog;
import me.ramazanenescik04.diken.studio.StudioPanel;
import me.ramazanenescik04.diken.tools.PixelToColor;
import me.ramazanenescik04.diken.tools.Utils;

/**
 * Represents the `DikenEngine` type within the DikenEngine `core` package.
 */
public class DikenEngine implements Runnable, IInputListener {
	public static final String VERSION = "3.0.0";
	public static final int protocolVersion = VERSION.hashCode();
	private static final boolean IS_DEV_MODE = 
	        "development".equals(System.getProperty("dikeneditor.mode"));

	private static DikenEngine instance;

	private JFrame engineWindow;
	private LoadingDialog loadingDialog;
	private StudioPanel studioPanel;
	private boolean fullscreen;

	private boolean running = false;
	private RendererPanel rendererPanel = null;

	private int width;
	private int height;
	private int scale = 1;
	private final int baseWidth;
	private final int baseHeight;
	public int currentFPS = -1;
	private boolean studioMode = false;

	public UniFont defaultFont;
	private World theWorld;

	public InputHandler input;
	public CursorResource cursorResource;
	public Config config = new Config();
	public Language defaultLanguage = Language.TURKISH;
	
	private List<IEngineListener> listeners = new ArrayList<>();
	
	private int tmpW;
	private int tmpH;

	public DikenEngine(int width, int height, int scale) {
		this(width, height, scale, false);
	}
	
	public DikenEngine(int width, int height, int scale, boolean openStudio) {
		NativeManager.loadNatives();
		instance = this;

		this.config.setSetting("guiScale", scale);
		this.config.loadConfig();

		this.width = tmpW = width;
		this.height = tmpH = height;
		this.baseWidth = width;
		this.baseHeight = height;
		this.scale = scale;
		this.studioMode = openStudio;
		
		initWindow(openStudio);
	}

	private void initWindow(boolean openStudio) {
		try {
			this.rendererPanel = new RendererPanel(this.width, this.height);
		} catch (LWJGLException e) {
			crash(e);
			System.exit(1);
		}

		this.engineWindow = new JFrame("DikenEngine" + (openStudio ? " Studio" : "") + (IS_DEV_MODE ? " [Development Version]" : ""));
		this.engineWindow.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.engineWindow.setIconImage(new javax.swing.ImageIcon(DikenEngine.class.getResource("/icon-x16.png")).getImage());
		
		if (openStudio) {
			loadingDialog = new LoadingDialog();
			loadingDialog.setVisible(true);
			
			studioPanel = new StudioPanel(this.engineWindow, this.rendererPanel, this);
			
			this.engineWindow.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					studioPanel.stop();
				}
			});
			this.engineWindow.setContentPane(studioPanel);
		} else {
			this.engineWindow.addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					stop();
				}
			});
			this.engineWindow.add(this.rendererPanel);
		}
		this.engineWindow.pack();
		this.engineWindow.setLocationRelativeTo(null);
	}

	public void start() {
		running = true;
		this.engineWindow.setVisible(true);
		this.rendererPanel.requestFocusInWindow();
		if (this.loadingDialog != null)
			this.loadingDialog.dispose();
		
		new Thread(this, "DikenEngine Thread").start();
	}

	public void stop() {
		running = false;
	}

	public void setWorld(World world) {
		if (world != null) {
			world.engine = this;
		}
		
		this.listeners.forEach(l -> l.worldChanged(theWorld, world));
		this.theWorld = world;
		
		System.gc();
	}
	
	public void addEngineListener(IEngineListener listener) {
		this.listeners.add(listener);
	}
	
	public void removeEngineListener(IEngineListener listener) {
		this.listeners.remove(listener);
	}

	public World getWorld() {
		return this.theWorld;
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
	
	public String getTitle() {
		return this.engineWindow.getTitle();
	}
	
	public void setTitle(String title) {
		this.engineWindow.setTitle(title);
	}
	
	public boolean isStudioMode() {
		return this.studioMode;
	}
	
	public static void log(ConsoleLog.LogType logType, String message) {
		ConsoleLog.sendLog(logType, message);
	}

	public static void log(String message) {
		log(LogType.C_LOG, message);
	}

	public static void errorLog(String message) {
		log(LogType.C_ERR, message);
	}
	
	public static void errorLog(String message, Throwable e) {
		log(LogType.C_ERR, message + "\n" + Utils.getStackTraceString(e));
	}

	@Override
	public void run() {
		try {			
			log("DikenEngine " + VERSION + " (Protocol: " + protocolVersion + ") Başlatılıyor");
			log("Başlatma Türü: " + (IS_DEV_MODE ? "Development Sürümü, Hatalar Olabilir!" : "Release Sürümü"));

			defaultFont = UniFont.getFont("default_font");

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

			config.saveConfig();
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

			/*if (key == KeyEvent.VK_F9 && !studioMode) {
				if (wManager.isWindowVaild(ConsoleWindow.class)) {
					return;
				}
				wManager.addWindow(new ConsoleWindow(2, 2, 200, 200));
			}*/
			
			if (key == KeyEvent.VK_F11) {
				toggleFullscreen();
			}
		}

		if (theWorld != null) {
			theWorld.getService(InputService.class).keyHandled(inputMode, key, character);
		}

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
		if (theWorld != null) {
			theWorld.getService(InputService.class).mouseHandled(inputMode, x, y, clicked);
		}
	}

	private void render(Bitmap bitmap) {
		if (theWorld != null) {
			theWorld.render(bitmap);
		}

		if (this.config.getSettingValue("debug", Boolean.class)) {
			bitmap.blendFill(0, 0, 120, 62, 0x2f000000);
			bitmap.drawText("DikenEngine " + VERSION, 2, 2, false);
			bitmap.drawText("FPS: " + currentFPS, 2, 12, false);
			bitmap.drawText("Screen: null (No Screen)", 2, 22, false);
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
					PixelToColor.blend(0xff00ff00, 0xffff0000, (percentageUse * 255) / 100));
			bitmap.drawText("%" + usedMemory * 100L / maxMemory, getScaledWidth() - 26, 13, false);
			bitmap.drawText("Max Memory: " + (maxMemory / 1024 / 1024) + " MB", getScaledWidth() - 118, 22, false);
		}
	}

	private void tick() {
		input.update();

		checkResized();

		if (theWorld != null) {
			theWorld.tick(this);
		}
		
		if (studioMode && studioPanel != null) {
			studioPanel.tick();
		}

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
				IOResource.loadResourceAndCut(IOResource.createClassResource("/win_icons.png"), 16, 16));
		ResourceLocator.addResource("win-icons", win_icons);

		ResourceLocator.addResource("editor_icons", new ArrayBitmap(
				IOResource.loadResourceAndCut(IOResource.createClassResource("/editor_icons.png"), 16, 16)));
		
		ResourceLocator.addResource("surface", new ArrayBitmap(
				IOResource.loadResourceAndCut(IOResource.createClassResource("/surface.png"), 16, 16)));

		ArrayBitmap win_cursors = new ArrayBitmap();
		win_cursors
				.setArray(IOResource.loadResourceAndCut(IOResource.createClassResource("/cursors.png"), 32, 32));

		for (int j = 0; j < 3; j++) {
			CursorResource cursor = new CursorResource();
			cursor.cursorBitmap = win_cursors.bitmap[0][j];
			ResourceLocator.addResource("cursor-" + j, cursor);
		}
	}

	public static DikenEngine getEngine() {		
		return instance;
	}

	public static void main(String[] args) {
		FlatDarkLaf.setup();
		UIManager.installLookAndFeel(new UIManager.LookAndFeelInfo("FlatLaf Light", FlatLightLaf.class.getName()));
		UIManager.installLookAndFeel(new UIManager.LookAndFeelInfo("FlatLaf Dark", FlatDarkLaf.class.getName()));
		UIManager.installLookAndFeel(new UIManager.LookAndFeelInfo("FlatLaf IntelliJ", FlatIntelliJLaf.class.getName()));
		
		DikenEngine engine = new DikenEngine(800, 600, 2, true);
		engine.start();
	}

	static {
		loadLocalImages();
		UniFont.createFont("default_font");
	}

	private void crash(Throwable e) {
		ConsoleLog.sendLog(LogType.C_ERR, Utils.getStackTraceString(e));
		ConsoleLog.saveLogs();

		CrashDialog.crash(engineWindow, e);
	}
}
