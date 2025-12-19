package me.ramazanenescik04.diken;

import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Cursor;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.openal.AL;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;

import me.ramazanenescik04.diken.game.Config;
import me.ramazanenescik04.diken.gui.UniFont;
import me.ramazanenescik04.diken.gui.screen.*;
import me.ramazanenescik04.diken.gui.window.ConsoleWindow;
import me.ramazanenescik04.diken.gui.window.WindowManager;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.CursorResource;
import me.ramazanenescik04.diken.resource.EnumResource;
import me.ramazanenescik04.diken.resource.IOResource;
import me.ramazanenescik04.diken.resource.IResource;
import me.ramazanenescik04.diken.resource.Language;
import me.ramazanenescik04.diken.resource.ResourceLocator;
import me.ramazanenescik04.diken.tools.*;
import me.ramazanenescik04.reportbugs.Issue;
import me.ramazanenescik04.reportbugs.gui.ReportBugGUI;

/**
 * Bu sınıf Diken Engine'in ana sınıfıdır. Bu sınıf, LWJGL kütüphanesini
 * kullanarak oyun motorunu başlatır ve çalıştırır.
 * 
 * @author Ramazanenescik04
 */
public class DikenEngine implements Runnable {
	public static final String VERSION = "1.0.0";
	public static final int protocolVersion = 10;

	public Canvas canvas;
	public int width, tmpwidth;
	public int height, tmpheight;
	private boolean fullscreen;
	private boolean tmpFullscreen;
	private boolean isResizable = true;
	public float scale = 1.0f; // Ölçeklendirme faktörü
	private String title;

	private long lastFPSTime = System.currentTimeMillis(); // Son FPS hesaplama zamanı
	public int currentFPS = -1; // Gösterilecek FPS

	public static double TARGET_FPS = 320;
	public static boolean V_SYNC = false;
	public boolean running = true;

	private int screenTextureID;
	private ByteBuffer screenBuffer;
	public Bitmap screenBitmap;

	public UniFont defaultFont;
	public WindowManager wManager;

	public Config config;

	/** Şu Anki Ekran */
	private Screen currentScreen;
	// null = Varsayılan Fare İmleci
	private CursorResource cursorResource = null;

	private static DikenEngine instance;

	public DikenEngine(/* Nullable */Canvas canvas, int width, int height) {
		this(canvas, width, height, 2.0f);
	}

	public DikenEngine(/* Nullable */Canvas canvas, int width, int height, float scale) {
		this.scale = scale;

		int scaleWidth = (int) (width * scale);
		int scaleHeight = (int) (height * scale);

		this.canvas = canvas;
		this.width = scaleWidth;
		this.height = scaleHeight;
		this.tmpwidth = scaleWidth;
		this.tmpheight = scaleHeight;
		this.fullscreen = false;

		config = new Config();
		title = "DikenEngine " + VERSION;
		instance = this;
	}

	/** Bu kod, Diken Engine'de bir ekranı ayarlar. */
	public void setCurrentScreen(Screen screen) {
		if (currentScreen != null) {
			currentScreen.closeScreen();
		}
		log("Opening screen: " + (screen != null ? screen.getClass().getSimpleName() : "null"));
		this.currentScreen = screen;
		System.gc();
		if (screen != null) {
			Keyboard.enableRepeatEvents(true);
			screen.engine = this;
			screen.openScreen();
		} else {
			Keyboard.enableRepeatEvents(false);
		}
	}

	public void setCursor(CursorResource cursor) {
		this.cursorResource = cursor;
	}

	/** Bu kod, Diken Engine'i başlatır. */
	public void start() {
		new Thread(this, "Engine Thread").start();
	}

	public void close() {
		running = false;
	}

	public static void main(String[] args) {
		// Teşekküller Claude.ai

		// Argümanlardan anahtar-değer çiftlerini tutacak Map
		Map<String, String> argMap = new HashMap<>();

		// Argümanları işleme
		for (int i = 0; i < args.length; i++) {
			// Eğer argüman "-" ile başlıyorsa, bu bir anahtar olabilir
			if (args[i].startsWith("-")) {
				// Mevcut argüman anahtar, sonraki argüman değer olacak
				String key = args[i];

				// Sonraki argümanın var olduğunu ve "-" ile başlamadığını kontrol et
				if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
					String value = args[i + 1];
					argMap.put(key, value);
					// Değere işaret eden indeksi atla
					i++;
				} else {
					// Değer yoksa, anahtarın değerini boş veya null olarak ayarla
					argMap.put(key, "");
				}
			}
		}

		DikenEngine engine = useMap(argMap);

		/*
		 * IGame capsuleGame = new CapsuleGame();
		 * capsuleGame.loadResources();
		 * capsuleGame.loadAdvancedNative();
		 * capsuleGame.loadNatives();
		 * capsuleGame.startGame(engine);
		 */ // TODO: Capsule Game

		engine.start();
	}

	private static DikenEngine useMap(Map<String, String> argMap) {
		DikenEngine object = null;
		// Varsayılan Değerler
		int w = 320;
		int h = 240;
		int s = 2;
		boolean fullscreen = false;

		if (argMap.containsKey("-w")) {
			String wStr = (String) argMap.get("-w");

			if (wStr != null && isNumeric(wStr)) {
				w = Integer.parseInt(wStr);
			}
		}

		if (argMap.containsKey("-h")) {
			String wStr = (String) argMap.get("-h");

			if (wStr != null && isNumeric(wStr)) {
				h = Integer.parseInt(wStr);
			}
		}

		if (argMap.containsKey("-s")) {
			String wStr = (String) argMap.get("-s");

			if (wStr != null && isNumeric(wStr)) {
				s = Integer.parseInt(wStr);
			}
		}

		if (argMap.containsKey("-fullscreen")) {
			fullscreen = true;
		}

		object = new DikenEngine(null, w, h, s);
		object.setFullscreen(fullscreen);

		return object;
	}

	public static boolean isNumeric(String s) {
		if (s == null || s.isEmpty())
			return false;
		for (int i = 0; i < s.length(); i++) {
			if (!Character.isDigit(s.charAt(i)))
				return false;
		}
		return true;
	}

	public void setFullscreen(boolean fullscreen2) {
		this.fullscreen = fullscreen2;
	}

	public void setResizable(boolean resizable) {
		this.isResizable = resizable;
	}

	public void setTitle(String var1) {
		this.title = var1;
	}

	public String getTitle() {
		return title;
	}

	private static void loadLocalNatives() {
		try {
			SystemInfo.Architecture arch = SystemInfo.instance.getArch();

			if (arch == SystemInfo.Architecture.X86) {
				// Linux için
				NativeManager.loadLibraryFromOSPathFromJar("/libjinput-linux.so");
				NativeManager.loadLibraryFromOSPathFromJar("/liblwjgl.so");
				NativeManager.loadLibraryFromOSPathFromJar("/libopenal.so");

				// MacOS için
				NativeManager.loadLibraryFromOSPathFromJar("/libjinput-osx.dylib");
				NativeManager.loadLibraryFromOSPathFromJar("/liblwjgl.dylib");
				NativeManager.loadLibraryFromOSPathFromJar("/openal.dylib");

				// Windows için
				NativeManager.loadLibraryFromOSPathFromJar("/jinput-dx8.dll");
				NativeManager.loadLibraryFromOSPathFromJar("/jinput-raw.dll");
				NativeManager.loadLibraryFromOSPathFromJar("/lwjgl.dll");
				NativeManager.loadLibraryFromOSPathFromJar("/OpenAL32.dll");
			} else if (arch == SystemInfo.Architecture.X86_64) {
				// Linux için
				NativeManager.loadLibraryFromOSPathFromJar("/libjinput-linux64.so");
				NativeManager.loadLibraryFromOSPathFromJar("/liblwjgl64.so");
				NativeManager.loadLibraryFromOSPathFromJar("/libopenal64.so");

				// MacOS için
				// MacOS için 64 bit mimari desteği yok

				// Windows için
				NativeManager.loadLibraryFromOSPathFromJar("/jinput-dx8_64.dll");
				NativeManager.loadLibraryFromOSPathFromJar("/jinput-raw_64.dll");
				NativeManager.loadLibraryFromOSPathFromJar("/lwjgl64.dll");
				NativeManager.loadLibraryFromOSPathFromJar("/OpenAL64.dll");

			} else {
				System.err.println("HATA: Bilgisayarınızın mimarisi desteklenmiyor. Mimari: " + arch.name());
				System.exit(0);
			}

			NativeManager.loadedNativeSetProperty("org.lwjgl.librarypath");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	static {
		loadLocalNatives();
		loadLocalImages();
		UniFont.createFont("default_font");
	}

	public static DikenEngine getEngine() {
		return DikenEngine.instance;
	}

	public void getGLError() {
		int error = GL11.glGetError();
		if (error != 0) {
			log("------------------");
			log("GL Error: " + GL11.glGetString(error));
			log("-------------------");
		}
	}

	/** Bu kod yerel resimleri ve sesleri yükler. */
	private static void loadLocalImages() {
		IResource icon_x16 = IOResource.loadResource(DikenEngine.class.getResourceAsStream("/icon-x16.png"),
				EnumResource.IMAGE);
		ResourceLocator.addResource("icon-x16", icon_x16);

		ArrayBitmap button = new ArrayBitmap();
		button.bitmap = IOResource.loadResourceAndCut(DikenEngine.class.getResourceAsStream("/button.png"), 16, 16);
		ResourceLocator.addResource("button-array", button);

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
	}

	/** Bu Kodu Şöyle Başlat: new Thread(dikenengine).start(); */
	public void run() {
		try {
			DikenEngine.log("DikenEngine " + VERSION + " Starting...");

			defaultFont = UniFont.getFont("default_font");
			wManager = new WindowManager();

			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				running = false;
			}));

			if (this.canvas != null) {
				Graphics var1 = this.canvas.getGraphics();
				if (var1 != null) {
					var1.setColor(Color.BLACK);
					var1.fillRect(0, 0, this.width, this.height);
					var1.dispose();
				}

				Display.setParent(this.canvas);
			} else if (this.fullscreen) {
				Display.setFullscreen(true);
				this.width = Display.getDisplayMode().getWidth();
				this.height = Display.getDisplayMode().getHeight();
				if (this.width <= 0) {
					this.width = 1;
				}

				if (this.height <= 0) {
					this.height = 1;
				}
			} else {
				Display.setDisplayMode(new DisplayMode(this.width, this.height));
			}
			Display.setTitle(title);
			Display.setResizable(isResizable);
			Display.create();

			Mouse.create();
			Keyboard.create();

			if (!AL.isCreated()) {
				SoundManager.init();
			}

			screenBitmap = new Bitmap(getWidth(), getHeight());
			screenBuffer = BufferUtils.createByteBuffer(screenBitmap.pixels.capacity() * 4);

			GL11.glMatrixMode(GL11.GL_PROJECTION);
			GL11.glLoadIdentity();
			GL11.glOrtho(0, width, height, 0, 1, -1);
			GL11.glMatrixMode(GL11.GL_MODELVIEW);

			// Dokulamaları etkinleştir
			GL11.glEnable(GL11.GL_TEXTURE_2D);

			// Doku oluştur
			screenTextureID = GL11.glGenTextures();
			// run() metodunda doku oluşturma kısmındaki parametreleri şu şekilde
			// değiştirin:
			GL11.glBindTexture(GL11.GL_TEXTURE_2D, screenTextureID);
			// Doku parametrelerini NEAREST olarak ayarla
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
			GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);

			// Piksel paketleme hizalaması
			GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 1);

			GL11.glViewport(0, 0, width, height);

			long fixedUpdateTime = 1000000000L / 60; // Fixed update rate at 60Hz
			long lastUpdateTime = System.nanoTime();
			double accumulator = 0;

			while (running) {
				long currentTime = System.nanoTime();

				if (this.canvas == null && Display.isCloseRequested()) {
					running = false;
				}

				long updateDelta = currentTime - lastUpdateTime;
				accumulator += updateDelta;
				lastUpdateTime = currentTime;

				while (accumulator >= fixedUpdateTime) {
					tick();
					accumulator -= fixedUpdateTime;
				}

				render(screenBitmap);

				// Buffer'ı hazırla
				screenBuffer.clear();
				screenBitmap.pixels.rewind();
				for (int i = 0; i < screenBitmap.pixels.capacity(); i++) {
					// Değerleri 0-255 aralığına dönüştür
					int color = screenBitmap.pixels.get(i);
					byte bgR = (byte) ((color >> 16) & 0xff);
					byte bgG = (byte) ((color >> 8) & 0xff);
					byte bgB = (byte) (color & 0xff);
					byte bgA = (byte) ((color >> 24) & 0xff);

					// Pikseli buffer'a ekle
					screenBuffer.put(bgR).put(bgG).put(bgB).put(bgA);
				}

				// Buffer'ı okuma için hazırla
				screenBuffer.flip();

				// Dokuyu güncelle
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, screenTextureID);
				GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, getWidth(), getHeight(), 0,
						GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, screenBuffer);

				GL11.glClear(GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_COLOR_BUFFER_BIT);

				GL11.glBindTexture(GL11.GL_TEXTURE_2D, screenTextureID);

				GL11.glBegin(GL11.GL_QUADS);
				GL11.glTexCoord2f(0, 0);
				GL11.glVertex2f(0, 0);

				GL11.glTexCoord2f(1, 0);
				GL11.glVertex2f(width, 0);

				GL11.glTexCoord2f(1, 1);
				GL11.glVertex2f(width, height);

				GL11.glTexCoord2f(0, 1);
				GL11.glVertex2f(0, height);
				GL11.glEnd();

				updateFPS();

				Display.update();
				if (V_SYNC) {
					Display.sync(60);
				} else {
					Display.sync((int) TARGET_FPS);
				}

				tickDimension();

				IntBuffer viewport = BufferUtils.createIntBuffer(16);
				GL11.glGetInteger(GL11.GL_VIEWPORT, viewport);

				int localWidth = viewport.get(2);
				int localHeight = viewport.get(3);

				if (localWidth != width || localHeight != height) {
					GL11.glViewport(0, 0, width, height);

					GL11.glMatrixMode(GL11.GL_PROJECTION);
					GL11.glLoadIdentity();
					GL11.glOrtho(0, width, height, 0, 1, -1);
					GL11.glMatrixMode(GL11.GL_MODELVIEW);

					this.refreshScreenBuffer();
				}
			}
		} catch (Throwable e) {
			e.printStackTrace();
			crash(e);
		} finally {
			if (currentScreen != null) {
				currentScreen.closeScreen();
			}

			config.saveConfig();

			wManager.closeAll();	
			
			try {
				System.out.println("Stopping!");
				SoundManager.destroy();

				try {
					GL11.glDisable(GL11.GL_TEXTURE_2D);
					GL11.glDeleteTextures(screenTextureID);
				} catch (Exception var6) {
				}

				Mouse.destroy();
				Keyboard.destroy();
			} finally {
				Display.destroy();
			}
			
			System.gc();
		}	
	}

	/**
	 * Bu kod her 1 saniyede 60 kez çalışır.
	 * 
	 * @throws LWJGLException
	 */
	private void tick() throws LWJGLException {
		getGLError();

		if (currentScreen != null) {
			currentScreen.tick();
		}
		wManager.tick();

		if (this.cursorResource == null && Mouse.getNativeCursor() != null) {
			Mouse.setNativeCursor(null);
		} else if (this.cursorResource != null) {
			Cursor cursor = this.cursorResource.getCursor();

			if (cursor != Mouse.getNativeCursor()) {
				try {
					Mouse.setNativeCursor(cursor);
				} catch (LWJGLException e) {
					e.printStackTrace();
				}
			}
		}

		if (Display.getTitle() != title) {
			Display.setTitle(title);
		}

		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState()) {
				if (Keyboard.getEventKey() == Keyboard.KEY_F2) {
					Bitmap screenshotBitmap = new Bitmap(getWidth(), getHeight());
					screenshotBitmap.clear(0xFF000000);
					screenshotBitmap.draw(screenBitmap, 0, 0);
					try {
						Date date = new Date();
						@SuppressWarnings("deprecation")
						String fileName = "screenshot-"
								+ date.toLocaleString().replaceAll(" ", "_").replaceAll(":", "-") + ".png";
						ImageIO.write(screenshotBitmap.toImage(), "png", new File(fileName));
						log("Screenshot saved as " + fileName);
					} catch (IOException e) {
						e.printStackTrace();
						log("Screenshot failed to save.");
					}
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_F11) {
					this.setFullscreen(!fullscreen);
				}

				if (Keyboard.getEventKey() == Keyboard.KEY_F12) {
					ReportBugGUI.showErrorReport(new DEngineBugHandler());
				}

				if (Keyboard.getEventKey() == Keyboard.KEY_F3) {
					this.config.setProperty("debug",
							this.config.getProperty("debug").equals("true") ? "false" : "true");
				}

				char ch = Keyboard.getEventCharacter();
				if (Keyboard.getEventKey() == Keyboard.KEY_GRAVE || ch == '"') {
					if (wManager.isWindowActive(ConsoleWindow.class)) {
						continue; // Konsol penceresi zaten açıksa, yeni bir tane açma
					}
					wManager.addWindow(new ConsoleWindow(2, 2, 200, 200));
				}

				if (currentScreen != null) {
					currentScreen.keyboardEvent();
				}
				wManager.keyboardEvent();
			}
		}
		while (Mouse.next()) {
			InputHandler.updateMousePosition();

			if (currentScreen != null) {
				currentScreen.mouseEvent();
			}
			wManager.mouseEvent();
		}
	}

	/** FPS'i günceller. */
	private volatile int frame;

	private void updateFPS() {
		if (System.currentTimeMillis() - lastFPSTime > 1000) {
			currentFPS = frame;
			frame = 0;
			lastFPSTime = System.currentTimeMillis();
		}
		frame++;
	}

	private void render(Bitmap bitmap) {
		bitmap.clear(0xFF000000);

		if (currentScreen != null) {
			currentScreen.render(bitmap);
		}

		wManager.render(bitmap);

		if (this.config.getProperty("debug").equals("true")) {
			bitmap.blendFill(0, 0, 120, 52, 0x2f000000);
			bitmap.drawText("FPS: " + currentFPS, 2, 2, false);
			bitmap.drawText("Screen: " + (currentScreen != null ? currentScreen.getClass().getSimpleName() : "null"), 2,
					12, false);
			bitmap.drawText("Width: " + getWidth() + " Height: " + getHeight(), 2, 22, false);
			bitmap.drawText("Scale: " + scale, 2, 32, false);
			java.awt.Point point = InputHandler.getMousePosition();
			bitmap.drawText("Mouse: " + point.x + ", " + point.y, 2, 42, false);

			Runtime runtime = Runtime.getRuntime();

			// Byte cinsinden bilgileri al
			long totalMemory = runtime.totalMemory(); // JVM tarafından tahsis edilen toplam hafıza
			long freeMemory = runtime.freeMemory(); // Kullanılabilir boş hafıza
			long usedMemory = totalMemory - freeMemory; // Kullanılmış hafıza
			long maxMemory = runtime.maxMemory();

			int percentageUse = Utils.toProccesBarValue(usedMemory, maxMemory, 94);
			bitmap.blendFill(getWidth() - 120, 0, getWidth(), 52, 0x2f000000);
			bitmap.drawText("Used Memory: " + usedMemory / 1024 / 1024 + " MB", getWidth() - 118, 2, 0xffffffff, false);
			bitmap.box(getWidth() - 118, 12, getWidth() - 30, 20, 0xffffffff);
			bitmap.fill(getWidth() - 117, 13, (getWidth() - 117) + percentageUse, 19,
					!(percentageUse > 85) ? 0xff00ff00 : 0xffff0000);
			bitmap.drawText("%" + usedMemory * 100L / maxMemory, getWidth() - 26, 13, false);
			bitmap.drawText("Max Memory: " + (maxMemory / 1024 / 1024) + " MB", getWidth() - 118, 22, false);
		}
	}

	/**
	 * Bu kod, Diken Engine'de hata ayıklama amacıyla kullanılır.
	 * 
	 * @param message mesajı yazdırılacak olan mesajdır.
	 */
	public static void log(String message) {
		System.out.println("[DikenEngine] " + message);
	}

	public static void errorLog(String message) {
		System.err.println("[DikenEngine] " + message);
	}

	public int getHeight() {
		return (int) (height / scale);
	}

	public int getWidth() {
		return (int) (width / scale);
	}

	public final Screen getCurrentScreen() {
		return this.currentScreen;
	}

	/**
	 * Bu Kod, Ekranı Resize Edildiğinde Boyutunu Değiştirilir.
	 * 
	 * @throws LWJGLException
	 */
	private void tickDimension() throws LWJGLException {
		if (this.canvas != null) {
			if (this.canvas.getWidth() != this.width || this.canvas.getHeight() != this.height) {
				resize();
			}
		} else if (Display.wasResized()) {
			resize();
		} else if (this.tmpFullscreen != this.fullscreen) {
			this.tmpFullscreen = this.fullscreen;
			if (this.fullscreen) {
				Display.setDisplayMode(Display.getDesktopDisplayMode());
				this.width = Display.getDisplayMode().getWidth();
				this.height = Display.getDisplayMode().getHeight();

				if (this.width <= 0) {
					this.width = 1;
				}

				if (this.height <= 0) {
					this.height = 1;
				}
			} else {
				if (this.canvas != null) {
					this.width = this.canvas.getWidth();
					this.height = this.canvas.getHeight();
				} else {
					this.width = this.tmpwidth;
					this.height = this.tmpheight;
				}

				Display.setDisplayMode(new DisplayMode(width, height));

				if (this.width <= 0) {
					this.width = 1;
				}

				if (this.height <= 0) {
					this.height = 1;
				}

			}
			Display.setFullscreen(this.fullscreen);
			Display.setVSyncEnabled(this.config.getProperty("sync").equals("true"));
			Display.setResizable(this.isResizable);
			Display.update();

			refreshScreenBuffer();
			sendScreenResized();
		}
	}

	private void sendScreenResized() {
		if (this.currentScreen != null) {
			currentScreen.resized();
		}
	}

	private void refreshScreenBuffer() {
		screenBitmap.clean();
		;
		screenBitmap = new Bitmap(getWidth(), getHeight());
		screenBuffer = BufferUtils.createByteBuffer(screenBitmap.pixels.capacity() * 4);

		System.gc();
	}

	private void resize() throws LWJGLException {
		if (this.canvas != null) {
			this.width = this.canvas.getWidth();
			this.height = this.canvas.getHeight();
		} else {
			this.width = Display.getWidth();
			this.height = Display.getHeight();
		}

		if (this.width <= 0) {
			this.width = 1;
		}

		if (this.height <= 0) {
			this.height = 1;
		}

		refreshScreenBuffer();
		sendScreenResized();
	}

	public void setSize(int i, int j) {
		this.width = i;
		this.height = j;

		if (this.canvas != null) {
			this.canvas.setSize(i, j);
		}

		this.tmpwidth = i;
		this.tmpheight = j;

		refreshScreenBuffer();
		sendScreenResized();
	}

	private void crash(Throwable e) {
		String title = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage().trim();
		ReportBugGUI.showError(title, Utils.getStackTraceString(e).trim());

		int status = ReportBugGUI.showSendErrorPopup();

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

		Issue issue = new Issue(title, desc.toString().trim(), Issue.Severity.CRITICAL);

		if (status == 0) {
			new DEngineBugHandler().handle(issue);
		} else if (status == 1) {
			ReportBugGUI.showErrorReportWait(issue.getTitle(), issue.getDesc(), new DEngineBugHandler());
		}
	}
}
