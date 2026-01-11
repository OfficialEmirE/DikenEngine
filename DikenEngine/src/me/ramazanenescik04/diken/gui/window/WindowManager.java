package me.ramazanenescik04.diken.gui.window;

import java.awt.Point;
import java.util.*;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.InputHandler;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.CursorResource;
import me.ramazanenescik04.diken.resource.ResourceLocator;

public class WindowManager {
    private ArrayList<Window> windows;
    public Window activeWindow;

    private Point dragStartPoint;
    private boolean dragMode = false;
    private boolean scaleMode = false;
    private int boyutlandirmaBolgesi = 0; // 0:hiçbiri, 1:sağ, 2:alt, 3:sağ-alt köşe
    
    // Window position offset for dragging
    private int dragOffsetX;
    private int dragOffsetY;

    public WindowManager() {
        windows = new ArrayList<Window>();
    }

    public void addWindow(Window window) {   	
    	window.open();
    	window.moved();
    	window.resized();
        windows.add(window);
        activeWindow = window;
    }

    public void render(Bitmap screen) {
        for (Window window : windows) {
            if(window.active) {
                screen.blendFill(window.x + 5, window.y + 5, window.x + window.width + 5, window.y + window.height + 5, 0x64000000);
            }
            
            screen.draw(window.render(), window.x, window.y);
        }
    }

    // Önceki mouse durumunu saklamak için değişkenler
    private boolean prevMouseDown = false;
    @SuppressWarnings("unused")
	private Point lastMousePos = new Point(0, 0);
    
    // Pencere boyutlandırma için minimum boyutlar
    private final int MIN_WINDOW_WIDTH = 100;
    private final int MIN_WINDOW_HEIGHT = 80;
    
    // Kenarlardaki resize bölgesi genişliği
    private final int RESIZE_BORDER_SIZE = 8;
    
    public void tick() {
    	DikenEngine engine = DikenEngine.getEngine();
        Point currentMousePoint = InputHandler.getMousePosition();
        boolean button0 = Mouse.isButtonDown(0);
        
        if(activeWindow != null) {
            if(!isWindowVaild(activeWindow)) {
                activeWindow.close();
                activeWindow = null;
            }
        }

        for (Window window : windows) {
            if(window.closed) {
                windows.remove(window);
                break;
            }

            if (window != activeWindow) {
                window.active = false;
            } else {
                window.active = true;
            }
            
            if(window.closeButtonClicked(currentMousePoint) && button0 && !dragMode && !scaleMode) {
    			window.close();
    		}

            window.tick(engine);
        }
        
        if (activeWindow != null && !dragMode && !scaleMode) {
            boyutlandirmaBolgesi = checkResizeArea(activeWindow, currentMousePoint);
            setResizeCursor(boyutlandirmaBolgesi);
        }
        
        // Mouse butonuna ilk tıkladığımızda (önceki durum false, şimdiki durum true)
        if(!prevMouseDown && button0) {
            Window eskiAktivPencere = activeWindow;
            findActiveWindow(currentMousePoint);

            // Return if no window is found
            if (activeWindow == null) {
                // Diğer modları sıfırla
                dragMode = false;
                scaleMode = false;
                boyutlandirmaBolgesi = 0;
            } else {
                // Reset operation modes if active window changed
                if (eskiAktivPencere != activeWindow) {
                    dragMode = false;
                    scaleMode = false;
                    boyutlandirmaBolgesi = 0;
                }
                
                // Önce boyutlandırma bölgesini kontrol et
                boyutlandirmaBolgesi = checkResizeArea(activeWindow, currentMousePoint);
                
                if (boyutlandirmaBolgesi != 0 && activeWindow.resizable) {
                    // Boyutlandırma modunu başlat
                    scaleMode = true;
                    dragStartPoint = new Point(currentMousePoint);
                    setResizeCursor(boyutlandirmaBolgesi);
                }
                // Boyutlandırma bölgesi değilse title bar'ı kontrol et
                else if (!dragMode && isInTitleBar(currentMousePoint)) {
                    dragMode = true;
                    dragStartPoint = new Point(currentMousePoint);
                    
                    // Calculate offset between mouse position and window position
                    dragOffsetX = activeWindow.x - currentMousePoint.x;
                    dragOffsetY = activeWindow.y - currentMousePoint.y;
                }
            }
        }
        
        // Mouse butonunu bıraktığımızda (önceki durum true, şimdiki durum false)
        if (prevMouseDown && !button0) {
            // Mouse released, end all modes
            dragMode = false;
            scaleMode = false;
            // İmleç tipini resetlemiyoruz - fare konumuna göre otomatik ayarlanacak
        }
        
        // Ensure window stays within screen bounds
        int screenWidth = engine.getWidth();
        int screenHeight = engine.getHeight();
        
        // Handle dragging when in drag mode
        if (dragMode && activeWindow != null && button0) {
            // Update window position based on mouse movement and initial offset
            activeWindow.x = currentMousePoint.x + dragOffsetX;
            activeWindow.y = currentMousePoint.y + dragOffsetY;
            
            if (activeWindow.x < 0) activeWindow.x = 0;
            if (activeWindow.y < 0) activeWindow.y = 0;
            if (activeWindow.x + activeWindow.width > screenWidth) activeWindow.x = screenWidth - activeWindow.width;
            if (activeWindow.y + activeWindow.height > screenHeight) activeWindow.y = screenHeight - activeWindow.height;
            
            activeWindow.moved();
        }
        
        // Handle resizing when in scale mode
        if (scaleMode && activeWindow != null && button0) {
            int deltaX = currentMousePoint.x - dragStartPoint.x;
            int deltaY = currentMousePoint.y - dragStartPoint.y;
            
            // Başlangıç boyutlarını kaydet
            int originalWidth = activeWindow.width;
            int originalHeight = activeWindow.height;
            
            // Resize moduna göre boyutlandır
            switch (boyutlandirmaBolgesi) {
                case 1: // Sağ kenar - sadece genişlik değişir
                    activeWindow.width = Math.max(originalWidth + deltaX, MIN_WINDOW_WIDTH);
                    break;
                    
                case 2: // Alt kenar - sadece yükseklik değişir
                    activeWindow.height = Math.max(originalHeight + deltaY, MIN_WINDOW_HEIGHT);
                    break;
                    
                case 3: // Sağ-alt köşe - hem genişlik hem yükseklik değişir
                    activeWindow.width = Math.max(originalWidth + deltaX, MIN_WINDOW_WIDTH);
                    activeWindow.height = Math.max(originalHeight + deltaY, MIN_WINDOW_HEIGHT);
                    break;
            }
            
            /*if (!activeWindow.fullscreen) {
            	activeWindow.tmpWidth = activeWindow.width;
            	activeWindow.tmpHeight = activeWindow.height;
            }*/
            
            if (activeWindow.x + activeWindow.width > screenWidth) {
                activeWindow.width = screenWidth - activeWindow.x;
            }
            
            if (activeWindow.y + activeWindow.height > screenHeight) {
                activeWindow.height = screenHeight - activeWindow.y;
            }
            
            activeWindow.resized();
            
            // Başlangıç noktasını güncelle
            dragStartPoint = new Point(currentMousePoint);
        }
        
        // Güncellenen mouse durumunu ve konumunu sakla
        prevMouseDown = button0;
        lastMousePos = new Point(currentMousePoint);
    }
    
    // Helper method to determine if a point is in the title bar area of the active window
    private boolean isInTitleBar(Point p) {
        if (activeWindow == null) return false;
        
        // Assuming the title bar is the top 20 pixels of the window
        int titleBarHeight = 20;
        
        return p.x >= activeWindow.x && 
               p.x <= activeWindow.x + activeWindow.width && 
               p.y >= activeWindow.y && 
               p.y <= activeWindow.y + titleBarHeight;
    }
    
    // Fare koordinatının pencerenin hangi boyutlandırma bölgesinde olduğunu kontrol eder
    private int checkResizeArea(Window window, Point p) {
        if (window == null) return 0;
        
        boolean onRightEdge = Math.abs(p.x - (window.x + window.width)) <= RESIZE_BORDER_SIZE && 
                              p.y >= window.y && 
                              p.y <= window.y + window.height;
                              
        boolean onBottomEdge = Math.abs(p.y - (window.y + window.height)) <= RESIZE_BORDER_SIZE && 
                               p.x >= window.x && 
                               p.x <= window.x + window.width;
                               
        boolean onBottomRightCorner = Math.abs(p.x - (window.x + window.width)) <= RESIZE_BORDER_SIZE && 
                                     Math.abs(p.y - (window.y + window.height)) <= RESIZE_BORDER_SIZE;
        
        if (onBottomRightCorner) return 3; // Sağ-alt köşe
        if (onRightEdge) return 1; // Sağ kenar
        if (onBottomEdge) return 2; // Alt kenar
        
        return 0; // Hiçbir resize bölgesinde değil
    }
    
    // Resize modu için imleç tipini ayarlar
    private void setResizeCursor(int resizeArea) {    
    	DikenEngine engine = DikenEngine.getEngine();
    	CursorResource cursorResource = null;
        switch (resizeArea) {
            case 1: // Sağ kenar - horizontal resize
            	cursorResource = (CursorResource) ResourceLocator.getResource("cursor-0");
            	engine.setCursor(cursorResource); // Köşe resize imleci
                break;
            case 2: // Alt kenar - vertical resize
            	cursorResource = (CursorResource) ResourceLocator.getResource("cursor-2");
            	engine.setCursor(cursorResource); // Köşe resize imleci
                break;
            case 3: // Sağ-alt köşe - diagonal resize
            	cursorResource = (CursorResource) ResourceLocator.getResource("cursor-1");
            	engine.setCursor(cursorResource); // Köşe resize imleci
                break;
            default:
                if (!dragMode) {
                    engine.setCursor(null); // Varsayılan imleç
                }
        }
    }

    public boolean isWindowVaild(Window window) {
        return windows.contains(window);
    }

    public void closeAll() {
        for (Window window : windows) {
            window.close();
        }
        if(activeWindow != null) {
		    activeWindow.close();
		}
    }

    public int size() {
        return windows.size();
    }

    public Window get(int index) {
        return windows.get(index);
    }

    private boolean findActiveWindow(Point p) {
        Window oldActiceWindow = activeWindow;
        activeWindow = null;
        // Check from end to start (top windows first)
        for (int i = windows.size() - 1; i >= 0; i--) {
            Window pencere = windows.get(i);
            if (pencere.isTouching(p)) {
                activeWindow = pencere;
                
                // Move the active window to the end of the list (top of the z-order)
                if (i < windows.size() - 1) {
                    windows.remove(i);
                    windows.add(activeWindow);
                }
                
                break;
            }
        }
        // Reset cursor if active window changed
        if (oldActiceWindow != activeWindow) {
            DikenEngine.getEngine().setCursor(null); // Varsayılan imleç
        }

        if(activeWindow != null) {
            return true;
        } else {
            return false;
        }
    }
    
    public boolean findActiveWindow2(Point point) {
        // Check from end to start (top windows first)
        for (int i = windows.size() - 1; i >= 0; i--) {
            Window pencere = windows.get(i);
            if (pencere.isTouching(point)) {
                return true;
            }
        }
        return false;
	}
    
    public boolean screenActionMode(Point point) {
    	if(!findActiveWindow2(point) && !scaleMode) {
			return true;
		}
    	return false;
    }
    
    public void keyboardEvent() {
    	if (activeWindow != null) {
    		activeWindow.keyPressed(Keyboard.getEventCharacter(), Keyboard.getEventKey());
    	}
    }
    
    public void mouseEvent() {
    	if (activeWindow != null) {
    		Point mousePos = InputHandler.getMousePosition();
    		int mouseButton = Mouse.getEventButton();
    		boolean isTouch = activeWindow.isTouching(mousePos);
    		
    		if (Mouse.getEventButtonState()) {
    			activeWindow.mouseClicked(mousePos.x, mousePos.y, mouseButton, isTouch);
    		}
    		
    		activeWindow.mouseGetInfo(mousePos.x, mousePos.y, isTouch);
    	}
    }

	public boolean isWindowActive(Class<?> class1) {
		for (Window window : windows) {
			if (window.getClass() == class1) {
				activeWindow = window;
				return true;
			}
		}
		return false;
	}
}