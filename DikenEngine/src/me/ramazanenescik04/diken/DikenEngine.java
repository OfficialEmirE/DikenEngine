package me.ramazanenescik04.diken;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import me.ramazanenescik04.diken.game.Config;
import me.ramazanenescik04.diken.gui.UniFont;
import me.ramazanenescik04.diken.gui.screen.DefaultMainMenuScreen;
import me.ramazanenescik04.diken.gui.screen.Screen;
import me.ramazanenescik04.diken.gui.window.ConsoleWindow;
import me.ramazanenescik04.diken.gui.window.WindowManager;
import me.ramazanenescik04.diken.log.ConsoleLog;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.CursorResource;
import me.ramazanenescik04.diken.resource.EnumResource;
import me.ramazanenescik04.diken.resource.IOResource;
import me.ramazanenescik04.diken.resource.IResource;
import me.ramazanenescik04.diken.resource.Language;
import me.ramazanenescik04.diken.resource.ResourceLocator;
import me.ramazanenescik04.diken.tools.Utils;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

public class DikenEngine implements Runnable {
	public static final String VERSION = "1.3.0";
	public static final int protocolVersion = -1;
    private static DikenEngine defaultEngine;

	public UniFont defaultFont;
	
	public WindowManager wManager;
	private Screen currentScreen;
	public Config config;
	private List<Callable<Boolean>> onCloseRunnables = new ArrayList<>();
	
	// Engine durumu
    private boolean running = false;
    
    // Pencere boyutları
    private static final int DEFAULT_WIDTH = 800;
    private static final int DEFAULT_HEIGHT = 600;

    private int width = DEFAULT_WIDTH;
    private int height = DEFAULT_HEIGHT;
    
    private float scale = 1.0f;
    
    // Ekran bufferleri
    private Bitmap screenBitmap;
    private ByteBuffer screenBuffer;
    
    // OpenGL kaynakları
    private long window;
    private int textureID;
    
    // GLFW
    private CursorResource theCursor;
    private String title = "DikenEngine " + VERSION, newTitle;
	private boolean isResizable = true;
	private GLFWImage.Buffer icons;
	
	int windowedX, windowedY;
	int windowedW, windowedH;
	
	private int currentFPS = -1;
	private long lastFPSTime = System.currentTimeMillis();
    
    // Input durumları
    private final boolean[] keys = new boolean[GLFW_KEY_LAST];
    private final boolean[] mouseButtons = new boolean[GLFW_MOUSE_BUTTON_LAST];
    private boolean isMouseOnScreen = false;
    private double mouseX = 0;
    private double mouseY = 0;
    private double scrollX = 0;
    private double scrollY = 0;
    
    public DikenEngine(int width, int height, float scale) {
    	this.scale = scale;
    	this.width = width;
    	this.height = height;
    	
    	this.config = new Config();
    	this.newTitle = title;
    	defaultEngine = this;
    }

    /**
     * Engine'i başlatır
     */
    public void start() {
        if (running) {
            System.out.println("Engine zaten çalışıyor!");
            return;
        }
        
        this.running = true;
        new Thread(this, "Engine Thread").start();
    }
    
    @Deprecated
    public void close() {
		this.running = false;
	}
    
    /**
     * Engine'i durdurur
     */
    public void stop() {
        if (!running) {
            System.out.println("Engine zaten durmuş!");
            return;
        }
        
        this.running = false;
    }
    
    public int getHeight() {
		return (int) (height / scale);
	}

	public int getWidth() {
		return (int) (width / scale);
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
			screen.engine = this;
			screen.openScreen();
		}
	}
	
	public Screen getCurrentScreen() {
		return this.currentScreen;
	}
	
	public void addOnCloseRunnable(Runnable runnable) {
		this.onCloseRunnables.add(() -> {
			runnable.run();
			return true;
		});
	}
	
	public void addOnCloseRunnable(Callable<Boolean> runnable) {
		this.onCloseRunnables.add(runnable);
	}
	
	public List<Callable<Boolean>> getOnCloseRunnables() {
		return new ArrayList<>(this.onCloseRunnables);
	}
	
	public void setCursor(CursorResource resource) {
		this.theCursor = resource;
	}
	
	public CursorResource getCursor() {
		return this.theCursor;
	}
	
	public void setTitle(String title) {
		this.newTitle = title;
	}
	
	public void appendTitle(String var1) {
		this.newTitle = this.title + var1;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setFullscreen(boolean fullscreen2) {
		this.config.setSetting("fullscreen", fullscreen2);
	}
	   
    private boolean isFullscreen() {
		return this.config.getSetting("fullscreen", Boolean.class).getValue();
	}

	public void setResizable(boolean resizable) {
		this.isResizable = resizable;
	}
	
	private void toggleFullscreen() {
	    if (this.config.getSetting("fullscreen", Boolean.class).getValue()) {
	        long monitor = GLFW.glfwGetPrimaryMonitor();
	        GLFWVidMode mode = GLFW.glfwGetVideoMode(monitor);

	        // pencere bilgilerini kaydet
	        saveWindowedState();

	        GLFW.glfwSetWindowMonitor(
	            window,
	            monitor,
	            0, 0,
	            mode.width(),
	            mode.height(),
	            mode.refreshRate()
	        );
	    } else {
	        GLFW.glfwSetWindowMonitor(
	            window,
	            0,
	            windowedX,
	            windowedY,
	            windowedW,
	            windowedH,
	            0
	        );
	    }
	}
	
    private void saveWindowedState() {
    	IntBuffer xb = BufferUtils.createIntBuffer(1);
    	IntBuffer yb = BufferUtils.createIntBuffer(1);
    	IntBuffer wb = BufferUtils.createIntBuffer(1);
    	IntBuffer hb = BufferUtils.createIntBuffer(1);

    	GLFW.glfwGetWindowPos(window, xb, yb);
    	GLFW.glfwGetWindowSize(window, wb, hb);

    	windowedX = xb.get(0);
    	windowedY = yb.get(0);
    	windowedW = wb.get(0);
    	windowedH = hb.get(0);
	}
    
    public void setIcon(Bitmap...icons) {
    	this.icons = GLFWImage.malloc(icons.length);
    	
    	for (int i = 0; i < icons.length; i++) {
    		Bitmap icon = icons[i];
    		ByteBuffer buffer = BufferUtils.createByteBuffer(icon.w * icon.h * 4);

    	    for (int y = 0; y < icon.h; y++) {
    	        for (int x = 0; x < icon.w; x++) {
    	            int pixel = icon.pixels.get(y * icon.w + x);

    	            buffer.put((byte) ((pixel >> 16) & 0xFF)); // R
    	            buffer.put((byte) ((pixel >> 8) & 0xFF));  // G
    	            buffer.put((byte) (pixel & 0xFF));         // B
    	            buffer.put((byte) ((pixel >> 24) & 0xFF)); // A
    	        }
    	    }

    	    buffer.flip();
    	    GLFWImage img = GLFWImage.malloc();
    	    img.set(icon.w, icon.h, buffer);
    		
    		this.icons.put(i, img);
    	}
    }

	@Override
    public void run() {
        try {
            init();
            loop();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "");
        } finally {
            cleanup();
        }
    }
    
    static {
    	loadLocalImages();
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
    
    /**
     * GLFW ve OpenGL'i başlatır
     */
    private void init() {
    	
    	GLFWErrorCallback.createPrint(System.err).set();
        // GLFW'yi başlat
        if (!glfwInit()) {
            throw new IllegalStateException("GLFW başlatılamadı!");
        }
        
        this.defaultFont = UniFont.createFont("default_font");;
        
        this.wManager = new WindowManager();
        
        // Ekran bitmap ve buffer'ını oluştur
        this.screenBitmap = new Bitmap(getWidth(), getHeight());
        this.screenBuffer = ByteBuffer.allocateDirect(screenBitmap.pixels.capacity() * 4);

        // Pencere ayarları
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, this.isResizable ? GLFW_TRUE : GLFW_FALSE);

        // Pencereyi oluştur
        window = glfwCreateWindow(width, height, title, NULL, NULL);
        if (window == NULL) {
            throw new RuntimeException("Pencere oluşturulamadı!");
        }
        
        if (this.icons != null) {
        	glfwSetWindowIcon(window, icons);
        	this.icons.free();
        }

        // Input callback'lerini ayarla
        setupInputCallbacks();

        // OpenGL context'ini ayarla
        glfwMakeContextCurrent(window);
        glfwSwapInterval(1); // V-Sync aktif
        glfwShowWindow(window);

        // LWJGL OpenGL yeteneklerini aktif et
        GL.createCapabilities();
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1); // Her byte'ı tek tek oku, hizalamaya zorlama
        
        // OpenGL ayarları
        setupOpenGL();
        
        // Texture oluştur
        createTexture();
        
        if (this.config.getSetting("fullscreen", Boolean.class).getValue()) toggleFullscreen();
    }
    
    @SuppressWarnings("unused")
	private void setupInputCallbacks() {
        // Klavye callback
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key >= 0 && key < keys.length) {
                if (action == GLFW_PRESS) {
                    keys[key] = true;
                } else if (action == GLFW_RELEASE) {
                    keys[key] = false;
                }
            }
            
            // Input event'ini işle
            onInput(key, scancode, (char) 0, action, mods, -1, mouseX, mouseY, 0, 0);
        });
        
        // Mouse tuş callback
        glfwSetMouseButtonCallback(window, (window, button, action, mods) -> {
            if (button >= 0 && button < mouseButtons.length) {
                if (action == GLFW_PRESS) {
                    mouseButtons[button] = true;
                } else if (action == GLFW_RELEASE) {
                    mouseButtons[button] = false;
                }
            }
            
            // Input event'ini işle
            onInput(-1, -1, (char) 0, action, mods, button, mouseX, mouseY, 0, 0);
        });
        
        // Mouse pozisyon callback
        glfwSetCursorPosCallback(window, (window, xpos, ypos) -> {
            mouseX = xpos / this.scale;
            mouseY = ypos / this.scale;
            
            // Input event'ini işle
            onInput(-1, -1, (char) 0, -1, -1, -1, mouseX, mouseY, 0, 0);
        });
        
        // Mouse scroll callback
        glfwSetScrollCallback(window, (window, xoffset, yoffset) -> {
            scrollX += xoffset;
            scrollY += yoffset;
            
            // Input event'ini işle
            onInput(-1, -1, (char) 0, -1, -1, -1, mouseX, mouseY, xoffset, yoffset);
        });
        
        glfwSetCharCallback(window, (win, codepoint) -> {
            onInput(
                -1, -1,  (char) codepoint, -2, -1,
                -1,
                mouseX, mouseY,
                0, 0
            );
        });

        
        // Pencere boyutu değişimi callback
        glfwSetFramebufferSizeCallback(window, (window, w, h) -> {
            width = w;
            height = h;
            glViewport(0, 0, w, h);
            
            // Projeksiyon matrisini güncelle
            glMatrixMode(GL_PROJECTION);
            glLoadIdentity();
            glOrtho(0, w, h, 0, -1, 1);
            glMatrixMode(GL_MODELVIEW);
            
            screenBitmap = new Bitmap(getWidth(), getHeight());
            screenBuffer = ByteBuffer.allocateDirect(screenBitmap.pixels.capacity() * 4);
            
            if (this.currentScreen != null) this.currentScreen.resized();
            
            // Callback içinde bitmap oluşturduktan sonra:
            glBindTexture(GL_TEXTURE_2D, textureID);
            // Texture alanını yeni boyutlara göre GPU'da yeniden rezerve et
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, w, h, 0, GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer)null);
        });

        glfwSetCursorEnterCallback(window,
            (win, entered) -> {
                isMouseOnScreen = entered;
            }
        );

    }
    
    /**
     * Tüm input eventlerini işleyen tek metod
     * 
     * @param key Klavye tuşu (yoksa -1)
     * @param scancode Tuş scancode (yoksa -1)
     * @param action Aksiyon tipi (GLFW_PRESS, GLFW_RELEASE, GLFW_REPEAT veya -1)
     * @param mods Modifier tuşlar (SHIFT, CTRL, ALT vb. veya -1)
     * @param mouseButton Mouse tuşu (yoksa -1)
     * @param mouseX Mouse X pozisyonu
     * @param mouseY Mouse Y pozisyonu
     * @param scrollX Scroll X offset (yoksa 0)
     * @param scrollY Scroll Y offset (yoksa 0)
     */
    protected void onInput(int key, int scancode, char character, int action, int mods, 
                          int mouseButton, double mouseX, double mouseY, 
                          double scrollX, double scrollY) {
    	if (key != 1 && action == GLFW_PRESS) {
    		if (key == GLFW_KEY_F2) {
				Bitmap screenshotBitmap = new Bitmap(getWidth(), getHeight());
				screenshotBitmap.clear(0xFF000000);
				screenshotBitmap.draw(screenBitmap, 0, 0);
				try {
					String fileName = "screenshot-"
							+ new Date().toString().replaceAll(" ", "_").replaceAll(":", "-") + ".png";
					ImageIO.write(screenshotBitmap.toImage(), "png", new File(fileName));
					log("Screenshot saved as " + fileName);
				} catch (IOException e) {
					e.printStackTrace();
					log("Screenshot failed to save.");
				}
			}
			if (key == GLFW_KEY_F11) {
				this.setFullscreen(!isFullscreen());
			}

			if (key == GLFW_KEY_F3) {
				this.config.setSetting("debug", !this.config.getSetting("debug", Boolean.class).getValue());
			}
			
			if (key == GLFW_KEY_F9) {
				if (!this.wManager.isWindowActive(ConsoleWindow.class)) {
					this.wManager.addWindow(new ConsoleWindow(10, 10, 200, 250));
				}
			}
    	}
    	
    	if (this.currentScreen != null) {
    		this.currentScreen.keyboardEvent(character, key, action);
    		if (mouseButton != -1) this.currentScreen.mouseEvent(action, isMouseOnScreen, mouseButton);
    	}
    	
    	this.wManager.keyboardEvent(character, key, action);
    	if (mouseButton != -1) this.wManager.mouseEvent(action, mouseButton, (int)mouseX, (int)mouseY);
    }

	/**
     * OpenGL render ayarlarını yapar
     */
    private void setupOpenGL() {
        glDisable(GL_DEPTH_TEST);
        
        glViewport(0, 0, width, height);
        // Projeksiyon matrisini ayarla
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        glOrtho(0, width, height, 0, -1, 1);
        glMatrixMode(GL_MODELVIEW);
        
        // Renk ayarları
        glClearColor(0.2f, 0.4f, 0.6f, 0.0f);
        glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
    }
    
    /**
     * Ekran texture'ını oluşturur
     */
    private void createTexture() {
        textureID = glGenTextures();
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, textureID);

        // Texture filtreleme ayarları
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        // İlk texture verisini gönder
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, getWidth(), getHeight(), 
                     0, GL_RGBA, GL_UNSIGNED_BYTE, screenBuffer);
        
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer)null);
    }
    
    /**
     * Ana render döngüsü
     */
    private void loop() {
        while (running && !glfwWindowShouldClose(window)) {
            // Input'ları işle
            processInput();
            
            // Güncelleme
            update();
            
            // Ekranı temizle
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            
            // Render işlemleri
            render();
            
            updateFPS();
            
            // Pencereyi güncelle
            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }
    
    /**
     * Input durumlarını kontrol eder
     */
    private void processInput() {
    	onInput(-1, -1, (char) 0, -1, -1, -2, this.mouseX, this.mouseY, 0, 0);
    }
    
    /**
     * Oyun mantığını günceller
     */
    private void update() {
    	if (title != newTitle) {
    		newTitle = title;
    		glfwSetWindowTitle(window, newTitle);
    	}
    	
    	if (this.config.getSetting("fullscreen", Boolean.class).getValue() != (glfwGetWindowMonitor(window) != 0)) {
    		toggleFullscreen();
    	}
    	
    	if (!this.config.getSetting("fullscreen", Boolean.class).getValue() && (isResizable != (glfwGetWindowAttrib(window, GLFW_RESIZABLE) == GLFW_TRUE))) {
    		glfwSetWindowAttrib(window, GLFW_RESIZABLE, isResizable ? GLFW_TRUE : GLFW_FALSE);
    	}
    	
        // Burada oyun mantığınız güncellenecek
    	if (this.currentScreen != null) {
    		this.currentScreen.tick();
    	}
    	
    	this.wManager.tick();
    	
    	if (this.theCursor != null) {
    		glfwSetCursor(window, theCursor.getCursor());
    	} else {
    		glfwSetCursor(window, NULL);
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
    
    /**
     * Render işlemlerini yapar
     */
    private void render() {
        if (this.currentScreen != null) {
        	this.currentScreen.render(screenBitmap);
        }
        
        this.wManager.render(screenBitmap);
        
        if (this.config.getSetting("debug", Boolean.class).getValue()) {
        	screenBitmap.blendFill(0, 0, 120, 52, 0x2f000000);
        	screenBitmap.drawText("FPS: " + currentFPS, 2, 2, false);
        	screenBitmap.drawText("Screen: " + (currentScreen != null ? currentScreen.getClass().getSimpleName() : "null"), 2,
					12, false);
        	screenBitmap.drawText("Width: " + getWidth() + " Height: " + getHeight(), 2, 22, false);
        	screenBitmap.drawText("Scale: " + scale, 2, 32, false);

			screenBitmap.drawText("Mouse: " + this.mouseX + ", " + this.mouseY, 2, 42, false);

			Runtime runtime = Runtime.getRuntime();

			// Byte cinsinden bilgileri al
			long totalMemory = runtime.totalMemory(); // JVM tarafından tahsis edilen toplam hafıza
			long freeMemory = runtime.freeMemory(); // Kullanılabilir boş hafıza
			long usedMemory = totalMemory - freeMemory; // Kullanılmış hafıza
			long maxMemory = runtime.maxMemory();

			int percentageUse = Utils.toProccesBarValue(usedMemory, maxMemory, 94);
			screenBitmap.blendFill(getWidth() - 120, 0, getWidth(), 52, 0x2f000000);
			screenBitmap.drawText("Used Memory: " + usedMemory / 1024 / 1024 + " MB", getWidth() - 118, 2, 0xffffffff, false);
			screenBitmap.box(getWidth() - 118, 12, getWidth() - 30, 20, 0xffffffff);
			screenBitmap.fill(getWidth() - 117, 13, (getWidth() - 117) + percentageUse, 19,
					!(percentageUse > 85) ? 0xff00ff00 : 0xffff0000);
			screenBitmap.drawText("%" + usedMemory * 100L / maxMemory, getWidth() - 26, 13, false);
			screenBitmap.drawText("Max Memory: " + (maxMemory / 1024 / 1024) + " MB", getWidth() - 118, 22, false);
		}
        
        // Bitmap'i buffer'a aktar
        updateScreenBuffer();
        
        // Texture'ı güncelle ve çiz
        drawTexture();
    }
    
    /**
     * Bitmap piksellerini ByteBuffer'a aktarır
     */
    private void updateScreenBuffer() {
        screenBuffer.clear();
        
        for (int i = 0; i < screenBitmap.pixels.capacity(); i++) {
            int pixel = screenBitmap.pixels.get(i);
            screenBuffer.put((byte) ((pixel >> 16) & 0xFF)); // R
            screenBuffer.put((byte) ((pixel >> 8) & 0xFF));  // G
            screenBuffer.put((byte) (pixel & 0xFF));         // B
            screenBuffer.put((byte) ((pixel >> 24) & 0xFF)); // A
        }
        
        screenBuffer.flip();
    }
    
    /**
     * Texture'ı ekrana çizer
     */
    private void drawTexture() {
    	glPushMatrix(); // Mevcut matrisi koru
    	glScalef(this.scale, this.scale, 1.0f); // X ekseninde 2 kat büyüt (width/2'yi width yapar)
    
        glEnable(GL_TEXTURE_2D);
        glBindTexture(GL_TEXTURE_2D, textureID);			
        glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, getWidth(), getHeight(), 
                        GL_RGBA, GL_UNSIGNED_BYTE, screenBuffer);

        // Tam ekran quad çiz
        glBegin(GL_QUADS);
            glTexCoord2f(0, 0); glVertex2f(0, 0);
            glTexCoord2f(1, 0); glVertex2f(width, 0);
            glTexCoord2f(1, 1); glVertex2f(width, height);
            glTexCoord2f(0, 1); glVertex2f(0, height);
        glEnd();

        glDisable(GL_TEXTURE_2D);
        glPopMatrix();
    }
    
    // ==================== INPUT QUERY METODları ====================
    
    public boolean isMouseOnScreen() {
    	return isMouseOnScreen;
    }
    
    /**
     * Belirtilen tuşun basılı olup olmadığını kontrol eder
     */
    public boolean isKeyPressed(int key) {
        return key >= 0 && key < keys.length && keys[key];
    }
    
    /**
     * Belirtilen mouse tuşunun basılı olup olmadığını kontrol eder
     */
    public boolean isMouseButtonPressed(int button) {
        return button >= 0 && button < mouseButtons.length && mouseButtons[button];
    }
    
    /**
     * Mouse X pozisyonunu döndürür
     */
    public double getMouseX() {
        return mouseX;
    }
    
    /**
     * Mouse Y pozisyonunu döndürür
     */
    public double getMouseY() {
        return mouseY;
    }
    
    /**
     * Mouse scroll X değerini döndürür
     */
    public double getScrollX() {
        return scrollX;
    }
    
    /**
     * Mouse scroll Y değerini döndürür
     */
    public double getScrollY() {
        return scrollY;
    }
    
    /**
     * Kaynakları temizler
     */
    private void cleanup() {
    	for (Callable<Boolean> runnable : onCloseRunnables) {
			try {
				runnable.call();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
    	
    	if (this.currentScreen != null) this.currentScreen.closeScreen();
    
    	this.wManager.closeAll();
    	this.config.saveConfig();
    	
    	System.out.println("Stopping!");
        if (textureID != 0) {
            glDeleteTextures(textureID);
        }
        
        if (window != NULL) {
            glfwDestroyWindow(window);
        }
        
        glfwTerminate();
        
        ConsoleLog.saveLogs();
    }
    
    public static void main(String[] args) {
        DikenEngine engine = new DikenEngine(1280, 720, 2.0f);
        engine.setCurrentScreen(new DefaultMainMenuScreen());
        engine.setFullscreen(true);
        engine.start();
    }

	public static DikenEngine getEngine() {
		return defaultEngine;
	}

	public static void log(String string) {
		System.out.println("[DikenEngine] " + string);
		ConsoleLog.sendLog(string);
	}
	
	public static void errorLog(String string) {
		System.err.println("[DikenEngine] " + string);
		ConsoleLog.sendLog("Error: " + string);
	}
}