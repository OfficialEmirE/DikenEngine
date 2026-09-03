package me.ramazanenescik04.diken;

import java.awt.Cursor;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.lwjgl.LWJGLException;
import com.formdev.flatlaf.*;

import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.game.services.InputService;
import me.ramazanenescik04.diken.gui.DebugScreen;
import me.ramazanenescik04.diken.gui.UniFont;
import me.ramazanenescik04.diken.input.IInputListener;
import me.ramazanenescik04.diken.input.InputHandler;
import me.ramazanenescik04.diken.language.Lang;
import me.ramazanenescik04.diken.log.ConsoleLog;
import me.ramazanenescik04.diken.log.ConsoleLog.LogType;
import me.ramazanenescik04.diken.plugin.PluginManager;
import me.ramazanenescik04.diken.renderer.RenderWorker;
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
import me.ramazanenescik04.diken.tools.Utils;

/**
 * Represents the `DikenEngine` type within the DikenEngine `core` package.
 */
public class DikenEngine implements Runnable, IInputListener {
	public static final String VERSION = "3.2.1";
	public static final int protocolVersion = 321;

	private static DikenEngine instance;

	private JFrame engineWindow;
	private LoadingDialog loadingDialog;
	private StudioPanel studioPanel;
	private boolean fullscreen;

	private volatile boolean running = false;
	private RendererPanel rendererPanel = null;
	private RenderWorker renderWorker;

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
	
	private List<IEngineListener> listeners = new ArrayList<>();
	
	private int tmpW;
	private int tmpH;
	private String appliedLanguageDisplayName;

	public DikenEngine(int width, int height, int scale) {
		this(width, height, scale, false);
	}
	
	public DikenEngine(int width, int height, int scale, boolean openStudio) {
		NativeManager.loadNatives();
		instance = this;

		this.config.setSetting("guiScale", scale);
		this.config.loadConfig();
		
		Map<String, String> available = Lang.getAvailableLanguages(); // kod -> görünen isim
        Map<String, String> nameToCode = new LinkedHashMap<>();
        for (var entry : available.entrySet()) {
            nameToCode.put(entry.getValue(), entry.getKey());
        }
		
		String code = nameToCode.get(this.config.getSettingValue("lang", String.class));
		if (code != null && Lang.isLanguageAvailable(code)) {
			Lang.setLanguage(code);
		}
		this.appliedLanguageDisplayName = this.config.getSettingValue("lang", String.class);
		
		this.width = tmpW = width;
		this.height = tmpH = height;
		this.baseWidth = width;
		this.baseHeight = height;
		this.scale = scale;
		this.studioMode = openStudio;
		
		initWindow(openStudio);
	}

	private void initWindow(boolean openStudio) {
		FlatDarkLaf.setup();
		UIManager.installLookAndFeel(new UIManager.LookAndFeelInfo("FlatLaf Light", FlatLightLaf.class.getName()));
		UIManager.installLookAndFeel(new UIManager.LookAndFeelInfo("FlatLaf Dark", FlatDarkLaf.class.getName()));
		UIManager.installLookAndFeel(new UIManager.LookAndFeelInfo("FlatLaf IntelliJ", FlatIntelliJLaf.class.getName()));
		
		try {
			this.rendererPanel = new RendererPanel(this.width, this.height);
		} catch (LWJGLException e) {
			crash(e);
			System.exit(1);
		}

		this.engineWindow = new JFrame("DikenEngine");
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
		
		if (this.studioMode && this.studioPanel != null)
			this.studioPanel.newWorld();
		
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
	
	/**
	 * @see DikenEngine#isStudioMode()
	 * @apiNote eğerki studio modunda açılmamışsa null olarak döner.
	 * @return Studio'yu dödürür
	 */
	public StudioPanel getStudio() {
		return studioPanel;
	}
	
	/**
	 * @return DikenEngine'nin Penceresini Döndürür.
	 */
	public JFrame getWindow() {
		return engineWindow;
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
	
	/**
	 * Oyun motorunu tam ekran yapar
	 * @param bool (bende bilmiyom :p)
	 */
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
	
	/**
	 * @return Studio Modunda başlatılıp başlatılmadığını bildirir
	 */
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
			log("Starting DikenEngine " + VERSION + " (Protocol: " + protocolVersion + ")");

			defaultFont = UniFont.getFont("default_font");

			input = new InputHandler(rendererPanel);
			input.addListener(this);
			
			renderWorker = new RenderWorker(this, rendererPanel, scale);
	        renderWorker.start();

			rendererPanel.acquireFrameBuffer(getScaledWidth(), getScaledHeight());
			
			if (this.loadingDialog != null) {
				this.loadingDialog.setStatus("Loading Plugins");
				this.loadingDialog.setProgress(50);
			}
			
			PluginManager.instance.loadPlugins(this, studioPanel);
			
			PluginManager.instance.enableAll(this, studioPanel);
			
			if (this.loadingDialog != null)
				this.loadingDialog.dispose();
			
			this.listeners.forEach(l -> l.engineStarted());
			
			long fixedUpdateTime = 1000000000L / 60;
			long lastUpdateTime = System.nanoTime();
			long lastFrameTime = lastUpdateTime;
			long lastFPSTime = System.currentTimeMillis();
			double accumulator = 0;
			int frames = 0;
			while (running) {
				long currentTime = System.nanoTime();
				long updateDelta = currentTime - lastUpdateTime;
				accumulator += updateDelta;
				lastUpdateTime = currentTime;

				// Uzun bir duraklamadan sonra sonsuz catch-up döngüsüne girme.
				accumulator = Math.min(accumulator, fixedUpdateTime * 5);
				int updates = 0;
				while (accumulator >= fixedUpdateTime && updates < 5) {
					tick();
					accumulator -= fixedUpdateTime;
					updates++;
				}

				if (currentTime - lastFrameTime >= fixedUpdateTime) {
					Bitmap frameBitmap = rendererPanel.acquireFrameBuffer(getScaledWidth(), getScaledHeight());
					renderWorker.queueFrame(frameBitmap);
					lastFrameTime = currentTime;
					frames++;
				}

				if (System.currentTimeMillis() - lastFPSTime >= 1000) {
					currentFPS = frames;
					frames = 0;
					lastFPSTime = System.currentTimeMillis();
				}

				try {
				    Thread.sleep(1);
				} catch (InterruptedException e) {
				    this.running = false;
				    Thread.currentThread().interrupt();
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
			crash(e);
		} finally {
			log("Closing DikenEngine...");

			config.saveConfig();
			engineWindow.dispose();
			
			PluginManager.instance.disableAll();

			ConsoleLog.saveLogs();
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
				this.config.setSetting("debug", !this.config.getSettingValue("debug", Boolean.class));
			}
			
			if (key == KeyEvent.VK_F9 && this.theWorld != null && !studioMode) {
				if (DebugScreen.instance.isOpen())
					DebugScreen.instance.closeDebugScreen();
				else
					DebugScreen.instance.openDebugScreen(theWorld);
			}
			
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

	public void render(Bitmap bitmap) {
		if (theWorld != null) {
			theWorld.render(bitmap);
			
			if (studioMode && studioPanel != null) {
				studioPanel.renderOverlay(bitmap);
			}
		}
		
		if (this.config.getSettingValue("debug", Boolean.class)) {
			bitmap.drawText("DikenEngine " + VERSION, 2, 2, false);
			bitmap.drawText("FPS: " + currentFPS, 2, 12, false);
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
		
		String configuredLanguage = this.config.getSettingValue("lang", String.class);
		if (!configuredLanguage.equals(appliedLanguageDisplayName)) {
			Map<String, String> available = Lang.getAvailableLanguages();
		        Map<String, String> nameToCode = new LinkedHashMap<>();
		        for (var entry : available.entrySet()) {
		            nameToCode.put(entry.getValue(), entry.getKey());
		        }
			
			String code = nameToCode.get(configuredLanguage);
			if (code != null && Lang.isLanguageAvailable(code)) {
				Lang.setLanguage(code);
				
				JOptionPane.showMessageDialog(
						engineWindow, 
						Lang.get("studio.restartNote"), 
						Lang.get("studio.restartRequired"), 
						JOptionPane.WARNING_MESSAGE);
			}
			appliedLanguageDisplayName = configuredLanguage;
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
		DikenEngine engine;
		if (args.length > 0 && args[0].equals("--studio")) {
			engine = new DikenEngine(800, 600, 2, true);
		} else {
			engine = new DikenEngine(800, 600, 2, false);
			
			JFileChooser fileChooser = new JFileChooser();
	        fileChooser.setDialogTitle("Yükleyeceğin Dünyayı Seç");
			fileChooser.setFileFilter(new FileNameExtensionFilter("DikenEngine World File", "dwf"));

	        int result = fileChooser.showOpenDialog(engine.engineWindow);
	        if (result != JFileChooser.APPROVE_OPTION) {System.exit(1); return;}

	        File selectedFile = fileChooser.getSelectedFile();
	        
	        try {
	        	var loadedWorld = World.loadWorld(selectedFile);
	        	loadedWorld.getRunService().run();
	        	engine.engineWindow.setTitle(engine.engineWindow.getTitle() + " - " + loadedWorld.getRoot().getName());
				engine.setWorld(loadedWorld);
			} catch (IOException | ReflectiveOperationException e) {
				CrashDialog.crash(engine.engineWindow, e, "Dünya Yükleme Başarısızlıkla Sonuçlandı!");
				System.exit(67);
			}
		}
		engine.start();
	}

	static {
		loadLocalImages();
		
		UniFont.loadFromResources("default_font");
	}

	private void crash(Throwable e) {
		ConsoleLog.sendLog(LogType.C_ERR, Utils.getStackTraceString(e));
		ConsoleLog.saveLogs();

		CrashDialog.crash(engineWindow, e);
	}
}
