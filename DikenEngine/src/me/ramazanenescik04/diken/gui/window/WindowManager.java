package me.ramazanenescik04.diken.gui.window;

import java.awt.Point;
import java.util.*;

import org.lwjgl.glfw.GLFW;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.Setting;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.CursorResource;
import me.ramazanenescik04.diken.resource.ResourceLocator;

public class WindowManager {
    // List interface'i kullanmak daha esnektir
    private List<Window> windows;
    public Window activeWindow;

    private Point dragStartPoint;
    private boolean dragMode = false;
    private boolean scaleMode = false;
    private int boyutlandirmaBolgesi = 0; // 0:hiçbiri, 1:sağ, 2:alt, 3:sağ-alt köşe
    
    // Pencere sürükleme offset'i
    private int dragOffsetX;
    private int dragOffsetY;

    // Önceki mouse durumunu saklamak için değişkenler
    private boolean prevMouseDown = false;
	private Point lastMousePos = new Point(0, 0);
    
    // Sabitler
    private final int MIN_WINDOW_WIDTH = 100;
    private final int MIN_WINDOW_HEIGHT = 80;
    private final int RESIZE_BORDER_SIZE = 8;
    
    private int startWindowWidth;  // Tıklama anındaki genişlik
    private int startWindowHeight; // Tıklama anındaki yükseklik

    public WindowManager() {
        windows = new ArrayList<>();
    }

    public void addWindow(Window window) {        
        windows.add(window); // Listeye ekle
        activeWindow = window; // Yeni açılan pencereyi aktif yap
        
        window.open();
        window.moved();
        window.resized();
    }

    public void render(Bitmap screen) {
        // Listeyi güvenli bir şekilde dolaş
    	Window[] windowArray = windows.toArray(new Window[0]);
        for (int i = 0; i < windowArray.length; i++) {
        	Window window = windowArray[i];
            if(window.active) {
                // Gölge efekti vs.
                screen.blendFill(window.x + 5, window.y + 5, window.x + window.width + 5, window.y + window.height + 5, 0x64000000);
            }
            screen.draw(window.render(), window.x, window.y);
        }
    }
    
    Point currentMousePoint = new Point();
    public void tick() {
        DikenEngine engine = DikenEngine.getEngine();
        currentMousePoint.setLocation(engine.getMouseX(), engine.getMouseY());
        
        boolean button0 = engine.isMouseButtonPressed(0);
        // Mouse sadece bu "tick" anında mı basıldı? (Basılı tutma değil, ilk tıklama anı)
        boolean mouseJustClicked = button0 && !prevMouseDown;
        
        // --- 1. Temizlik Aşaması ---
        // Kapanmış pencereleri listeden güvenli bir şekilde temizle (Java 8+ removeIf)
        windows.removeIf(w -> w.closed);

        // Aktif pencere listeden silindiyse veya geçersizse referansı temizle
        if(activeWindow != null && (!windows.contains(activeWindow) || activeWindow.closed)) {
            activeWindow = null;
            // Eğer pencere kapandıysa sürükleme modlarını da iptal et
            dragMode = false;
            scaleMode = false;
        }

        // --- 2. Aktiflik Durumu Güncellemesi ---
        for (Window window : windows) {
            window.active = (window == activeWindow);
            window.tick(engine);
        }

        // --- 3. Kapatma İşlemi (Öncelikli) ---
        // Sadece AKTİF pencere kapatılabilir ve sadece tıklama anında (mouseJustClicked) işlem yapılır.
        // Bu sayede arkadaki pencerelerin yanlışlıkla kapanması engellenir.
        if (activeWindow != null && mouseJustClicked && !dragMode && !scaleMode) {
            if (activeWindow.closeButtonClicked(currentMousePoint)) {
                activeWindow.close();
                // Pencere kapandığı an işlemi bitir, başka tıklama algılama
                prevMouseDown = button0;
                lastMousePos = new Point(currentMousePoint);
                return; 
            }
        }
        
        // --- 4. İmleç ve Boyutlandırma Alanı Kontrolü ---
        if (activeWindow != null && !dragMode && !scaleMode) {
            boyutlandirmaBolgesi = checkResizeArea(activeWindow, currentMousePoint);
            setResizeCursor(boyutlandirmaBolgesi);
        }
        
        // --- 5. Pencere Seçimi (Focus) ---
        // Eğer bir yere tıklandıysa ve şu an sürükleme/boyutlandırma yapılmıyorsa
        if (mouseJustClicked && !dragMode && !scaleMode) {
            Window eskiAktivPencere = activeWindow;
            findActiveWindow(currentMousePoint); // Tıklanan pencereyi bul ve activeWindow yap

            if (activeWindow == null) {
                // Boşluğa tıklandı
                boyutlandirmaBolgesi = 0;
            } else {
                // Başka bir pencereye geçildiyse modları sıfırla
                if (eskiAktivPencere != activeWindow) {
                    boyutlandirmaBolgesi = 0;
                }
                
                // Tıklama anında boyutlandırma bölgesinde miyiz?
                boyutlandirmaBolgesi = checkResizeArea(activeWindow, currentMousePoint);
                
                if (boyutlandirmaBolgesi != 0 && activeWindow.resizable) {
                    scaleMode = true;
                    dragStartPoint = new Point(currentMousePoint);
                    // TIKLAMA ANINDAKİ BOYUTLARI KAYDET (Referans noktası)
                    startWindowWidth = activeWindow.width;
                    startWindowHeight = activeWindow.height;
                    setResizeCursor(boyutlandirmaBolgesi);
                }
                // Başlık çubuğunda mıyız? (Sürükleme başlat)
                else if (isInTitleBar(currentMousePoint)) {
                    dragMode = true;
                    dragStartPoint = new Point(currentMousePoint);
                    dragOffsetX = activeWindow.x - currentMousePoint.x;
                    dragOffsetY = activeWindow.y - currentMousePoint.y;
                }
            }
        }
        
        // --- 6. Mouse Bırakma (Reset) ---
        if (prevMouseDown && !button0) {
            dragMode = false;
            scaleMode = false;
        }
        
        // --- 7. Sürükleme ve Boyutlandırma Mantığı ---
        int screenWidth = engine.getWidth();
        int screenHeight = engine.getHeight();
        
        // Sürükleme
        if (dragMode && activeWindow != null && button0) {
            activeWindow.x = currentMousePoint.x + dragOffsetX;
            activeWindow.y = currentMousePoint.y + dragOffsetY;
            
            // Ekran sınırları kontrolü
            if (activeWindow.x < 0) activeWindow.x = 0;
            if (activeWindow.y < 0) activeWindow.y = 0;
            // Pencerenin tamamen kaybolmasını engelle
            if (activeWindow.x > screenWidth - 20) activeWindow.x = screenWidth - 20; 
            if (activeWindow.y > screenHeight - 20) activeWindow.y = screenHeight - 20;
            
            activeWindow.moved();
        }
        
        Setting<Boolean> useOldScaleSystem = engine.config.getOrDefaultSetting("useOldScaleCode", Boolean.class, false);
        
        if (useOldScaleSystem.getValue()) {
        	// Boyutlandırma
            if (scaleMode && activeWindow != null && button0) {
                int newWidth = activeWindow.width;
                int newHeight = activeWindow.height;

                // Mouse'un son kareden bu kareye ne kadar oynadığı (delta değil movement)
                int moveX = currentMousePoint.x - lastMousePos.x;
                int moveY = currentMousePoint.y - lastMousePos.y;

                switch (boyutlandirmaBolgesi) {
                    case 1: // Sağ
                        newWidth += moveX;
                        break;
                    case 2: // Alt
                        newHeight += moveY;
                        break;
                    case 3: // Sağ-Alt
                        newWidth += moveX;
                        newHeight += moveY;
                        break;
                }

                activeWindow.width = Math.max(newWidth, MIN_WINDOW_WIDTH);
                activeWindow.height = Math.max(newHeight, MIN_WINDOW_HEIGHT);
                
                // Sınırlara çarpma kontrolü
                if (activeWindow.x + activeWindow.width > screenWidth) {
                    activeWindow.width = screenWidth - activeWindow.x;
                }
                if (activeWindow.y + activeWindow.height > screenHeight) {
                    activeWindow.height = screenHeight - activeWindow.y;
                }
                
                activeWindow.resized();
                
                // dragStartPoint güncellemesi resize mantığında kafa karıştırabilir, 
                // burada gerek yok çünkü moveX/Y kullanıyoruz.
            }
        } else {
        	// --- tick metodu içindeki scaleMode bloğu ---

            if (scaleMode && activeWindow != null && button0) {
                // 1. Toplam farkı hesapla (Tıklanan ilk noktadan şu anki mouse konumuna)
                int deltaX = currentMousePoint.x - dragStartPoint.x;
                int deltaY = currentMousePoint.y - dragStartPoint.y;
                
                // 2. Boyutlandırma bölgesine göre yeni boyutları hesapla
                // NOT: Burada 'originalWidth' ve 'originalHeight' değerlerinin 
                // scaleMode başladığı AN (yani mouseJustClicked içinde) kaydedilmesi gerekir.
                
                switch (boyutlandirmaBolgesi) {
                    case 1: // Sağ kenar
                        activeWindow.width = Math.max(startWindowWidth + deltaX, MIN_WINDOW_WIDTH);
                        break;
                        
                    case 2: // Alt kenar
                        activeWindow.height = Math.max(startWindowHeight + deltaY, MIN_WINDOW_HEIGHT);
                        break;
                        
                    case 3: // Sağ-alt köşe
                        activeWindow.width = Math.max(startWindowWidth + deltaX, MIN_WINDOW_WIDTH);
                        activeWindow.height = Math.max(startWindowHeight + deltaY, MIN_WINDOW_HEIGHT);
                        break;
                }
                
                // Ekran sınırlarını kontrol et
                if (activeWindow.x + activeWindow.width > screenWidth) {
                    activeWindow.width = screenWidth - activeWindow.x;
                }
                if (activeWindow.y + activeWindow.height > screenHeight) {
                    activeWindow.height = screenHeight - activeWindow.y;
                }
                
                activeWindow.resized();
                
                // DİKKAT: Burada dragStartPoint'i GÜNCELLEMİYORUZ. 
                // Çünkü delta'yı her zaman en baştaki tıklama noktasına göre alıyoruz.
            }
        }

        // Mouse durumunu güncelle
        prevMouseDown = button0;
        lastMousePos = new Point(currentMousePoint);
    }
    
    // --- Helper Methods ---

    private boolean isInTitleBar(Point p) {
        if (activeWindow == null) return false;
        int titleBarHeight = 20;
        return p.x >= activeWindow.x && 
               p.x <= activeWindow.x + activeWindow.width && 
               p.y >= activeWindow.y && 
               p.y <= activeWindow.y + titleBarHeight;
    }
    
    private int checkResizeArea(Window window, Point p) {
        if (window == null) return 0;
        
        // Kenar toleransını biraz artırdım, tutması daha kolay olsun
        int border = RESIZE_BORDER_SIZE;
        
        boolean inXRange = p.x >= window.x && p.x <= window.x + window.width + border;
        boolean inYRange = p.y >= window.y && p.y <= window.y + window.height + border;
        
        boolean onRightEdge = Math.abs(p.x - (window.x + window.width)) <= border && inYRange;
        boolean onBottomEdge = Math.abs(p.y - (window.y + window.height)) <= border && inXRange;
        
        if (onRightEdge && onBottomEdge) return 3; // Sağ-alt köşe (öncelikli)
        if (onRightEdge) return 1;
        if (onBottomEdge) return 2;
        
        return 0;
    }
    
    private void setResizeCursor(int resizeArea) {      
        DikenEngine engine = DikenEngine.getEngine();
        // ResourceLocator null dönebilir, güvenli cast yapalım
        try {
            switch (resizeArea) {
                case 1: engine.setCursor((CursorResource) ResourceLocator.getResource("cursor-0")); break;
                case 2: engine.setCursor((CursorResource) ResourceLocator.getResource("cursor-2")); break;
                case 3: engine.setCursor((CursorResource) ResourceLocator.getResource("cursor-1")); break;
                default: if (!dragMode) engine.setCursor(null);
            }
        } catch (Exception e) {
            // Resource bulunamazsa varsayılan cursor kalsın, oyun çökmesin
            if (!dragMode) engine.setCursor(null);
        }
    }

    public boolean isWindowVaild(Window window) {
        return windows.contains(window);
    }

    public void closeAll() {
        // removeIf kullanmadan önce pencerelerin kapatma metodunu çağırıyoruz
        for (Window window : windows) {
            window.close();
        }
        windows.clear();
        activeWindow = null;
    }

    public int size() {
        return windows.size();
    }

    public Window get(int index) {
        return windows.get(index);
    }

    private boolean findActiveWindow(Point p) {
        Window oldActiveWindow = activeWindow;
        activeWindow = null;
        
        // Tersten döngü (Z-order): En üstteki pencere listenin sonundadır.
        for (int i = windows.size() - 1; i >= 0; i--) {
            Window pencere = windows.get(i);
            if (pencere.isTouching(p)) {
                activeWindow = pencere;
                
                // Aktif pencereyi listenin sonuna (en üste) taşı
                windows.remove(i);
                windows.add(activeWindow);
                break;
            }
        }
        
        if (oldActiveWindow != activeWindow) {
            DikenEngine.getEngine().setCursor(null);
        }

        return activeWindow != null;
    }
    
    // Yardımcı metod: Sadece tıklanan yerin altında pencere var mı diye bakar (sıralamayı bozmaz)
    public boolean findActiveWindow2(Point point) {
        for (int i = windows.size() - 1; i >= 0; i--) {
            if (windows.get(i).isTouching(point)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean screenActionMode(Point point) {
        return !findActiveWindow2(point) && !scaleMode;
    }
    
    public void keyboardEvent(char character, int key, int action) {
        if (activeWindow != null && (action == GLFW.GLFW_PRESS || action == GLFW.GLFW_REPEAT || action == -2)) {
            activeWindow.keyPressed(character, key);
        }
    }
    
    public void mouseEvent(int action, int clickedButton, int x, int y) {
        if (activeWindow != null) {
            Point mousePos = new Point(x, y);
            boolean isTouch = activeWindow.isTouching(mousePos);
            
            // Mouse.getEventButtonState() sadece event loop içinde anlamlıdır
            if (action == GLFW.GLFW_PRESS) {
                activeWindow.mouseClicked(mousePos.x, mousePos.y, clickedButton, isTouch);
            }
            
            activeWindow.mouseGetInfo(mousePos.x, mousePos.y, isTouch);
        }
    }

    public boolean isWindowActive(Class<?> class1) {
        for (Window window : windows) {
            if (window.getClass() == class1) {
                // Burada activeWindow'u değiştirmiyoruz, sadece kontrol ediyoruz.
                // İstersen: activeWindow = window; yapabilirsin ama findActiveWindow mantığıyla çakışabilir.
                return activeWindow == window;
            }
        }
        return false;
    }
}