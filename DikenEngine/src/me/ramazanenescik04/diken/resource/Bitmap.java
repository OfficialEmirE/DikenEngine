package me.ramazanenescik04.diken.resource;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.image.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.Cleaner.Cleanable;
import java.util.*;

import javax.imageio.ImageIO;

import me.ramazanenescik04.diken.gui.UniFont;
import me.ramazanenescik04.diken.gui.component.Text;
import me.ramazanenescik04.diken.renderer.ArrayBuffer;

/**
 * Represents the `Bitmap` type within the DikenEngine `resource` package.
 */
public class Bitmap implements IResource, Cleanable {
	private static final long serialVersionUID = 1L;
	
	private static final ArrayBuffer empty_pixels = new ArrayBuffer(1);
	public static final Bitmap empty = new Bitmap(1, 1);
	
	public transient ArrayBuffer pixels = empty_pixels;
	public int w;
	public int h;
	public int xOffs;
	public int yOffs;
	public boolean xFlip = false;
	
	public Bitmap() {
		this.w = 1;
		this.h = 1;
		this.pixels = empty_pixels;
	}

	public Bitmap(int w, int h) {
		w = (w <= 0) ? 1 : w;
		h = (h <= 0) ? 1 : h;
		
		this.w = w;
	    this.h = h;
		this.pixels = new ArrayBuffer(w * h);
	}
	
	public Bitmap(int w, int h, ArrayBuffer copy) {
		w = (w <= 0) ? 1 : w;
		h = (h <= 0) ? 1 : h;
		
		this.w = w;
	    this.h = h; 
	    this.pixels = new ArrayBuffer(copy.size());
	    this.pixels.put(copy);
	}
	
	public Bitmap(int w, int h, int[] copy) {
		w = (w <= 0) ? 1 : w;
		h = (h <= 0) ? 1 : h;
		
		this.w = w;
	    this.h = h;
	    this.pixels = new ArrayBuffer(copy);
	}

	public static Bitmap wrap(int w, int h, int[] pixels) {
		w = (w <= 0) ? 1 : w;
		h = (h <= 0) ? 1 : h;
		
		if (pixels == null || pixels.length < w * h) {
			throw new IllegalArgumentException("Pixel array is too small for the requested bitmap size.");
		}
		
		Bitmap bitmap = new Bitmap();
		bitmap.w = w;
		bitmap.h = h;
		bitmap.pixels = ArrayBuffer.wrap(pixels);
		return bitmap;
	}
	
	@Deprecated
	public void rewind() {}

	public BufferedImage toImage() {
		var localArray = this.pixels.array();

	    DataBufferInt buffer = new DataBufferInt(localArray, localArray.length);
	    SampleModel sm = new SinglePixelPackedSampleModel(DataBuffer.TYPE_INT, w, h, new int[]{0x00FF0000, 0x0000FF00, 0x000000FF, 0xFF000000});
	    WritableRaster raster = Raster.createWritableRaster(sm, buffer, null);
	    
	    return new BufferedImage(new DirectColorModel(32, 0x00FF0000, 0x0000FF00, 0x000000FF, 0xFF000000), raster, false, null);
	}
	
	public Bitmap resize(int newWidth, int newHeight) {
		Bitmap resizedBitmap;
		BufferedImage bitmapImg, resizedBitmapImg;
		bitmapImg = toImage();
		resizedBitmapImg = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D g = resizedBitmapImg.createGraphics();
		g.drawImage(bitmapImg.getScaledInstance(newWidth, newHeight, Image.SCALE_FAST), 0, 0, null);
		g.dispose();
		
		resizedBitmap = toBitmap(resizedBitmapImg);
		
		return resizedBitmap;
	}
	
	public Bitmap resize(int scale) {
		return resize(w * scale, h * scale);
	}
	
	public Bitmap rotate(float degress) {
		return rotate(degress, 0x00000000);
	}
	
	public Bitmap rotate(float degrees, int fillColor) {
	    if (degrees % 360 == 0) return clone();

	    double rad = Math.toRadians(degrees);
	    double cos = Math.cos(rad);
	    double sin = Math.sin(rad);

	    // --- 1. Yeni canvas boyutunu hesapla ---
	    // Orijinal köşelerin döndürülmüş hallerinin kapsadığı alanı bul
	    double hw = w / 2.0, hh = h / 2.0;
	    double[][] corners = {
	        rotatePoint(-hw, -hh, cos, sin),
	        rotatePoint( hw, -hh, cos, sin),
	        rotatePoint( hw,  hh, cos, sin),
	        rotatePoint(-hw,  hh, cos, sin)
	    };

	    double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE;
	    double minY = Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
	    for (double[] c : corners) {
	        if (c[0] < minX) minX = c[0];
	        if (c[0] > maxX) maxX = c[0];
	        if (c[1] < minY) minY = c[1];
	        if (c[1] > maxY) maxY = c[1];
	    }

	    int newW = (int) Math.ceil(maxX - minX);
	    int newH = (int) Math.ceil(maxY - minY);

	    // --- 2. Yeni resmi doldur ---
	    int[] newPixels = new int[newW * newH];

	    // Yeni canvas'ın merkezi
	    double newCX = newW / 2.0;
	    double newCY = newH / 2.0;

	    // Ters dönüşüm için -θ kullan
	    double cosR = Math.cos(-rad);
	    double sinR = Math.sin(-rad);

	    for (int dy = 0; dy < newH; dy++) {
	        for (int dx = 0; dx < newW; dx++) {

	            // Hedef pikseli merkeze göre konumlandır
	            double ox = dx - newCX;
	            double oy = dy - newCY;

	            // Ters döndür → kaynaktaki konuma ulaş
	            double srcXd = cosR * ox - sinR * oy + hw;
	            double srcYd = sinR * ox + cosR * oy + hh;

	            int srcX = (int) Math.round(srcXd);
	            int srcY = (int) Math.round(srcYd);

	            if (srcX >= 0 && srcX < w && srcY >= 0 && srcY < h) {
	                newPixels[dx + dy * newW] = pixels.localArray[srcX + srcY * w];
	            } else {
	                newPixels[dx + dy * newW] = fillColor; // kenar rengi
	            }
	        }
	    }

	    Bitmap result = new Bitmap(newW, newH, newPixels);

	    return result;
	}

	/** Noktayı orijin etrafında döndürür */
	private static double[] rotatePoint(double x, double y, double cos, double sin) {
	    return new double[]{ x * cos - y * sin, x * sin + y * cos };
	}
	
	public void drawGradient(int color1, int color2) {
		rewind();
		int r1 = (color1 >> 16) & 0xFF;
	    int g1 = (color1 >> 8) & 0xFF;
	    int b1 = color1 & 0xFF;

	    // Renk bileşenlerini (RGB) ayır (Color2 - Alt Renk)
	    int r2 = (color2 >> 16) & 0xFF;
	    int g2 = (color2 >> 8) & 0xFF;
	    int b2 = color2 & 0xFF;

	    // Bu sefer dış döngü y (yükseklik) üzerinden dönüyor
	    for (int y = 0; y < h; y++) {
	        // İlerleme oranı artık yüksekliğe göre hesaplanıyor
	        float t = (float) y / (h - 1);

	        // Her kanal için dikey ara rengi hesapla
	        int r = (int) (r1 + (r2 - r1) * t);
	        int g = (int) (g1 + (g2 - g1) * t);
	        int b = (int) (b1 + (b2 - b1) * t);

	        // Yeni rengi birleştir
	        int finalColor = (255 << 24) | (r << 16) | (g << 8) | b;

	        // Hesaplanan bu rengi tüm satır (x) boyunca boya
	        for (int x = 0; x < w; x++) {
	            this.pixels.put(x + y * w, finalColor);
	        }
	    }
	}

	public void drawLine(int x1, int y1, int x2, int y2, int color, int width) {
	    x1 += this.xOffs;
	    y1 += this.yOffs;
	    x2 += this.xOffs;
	    y2 += this.yOffs;
	    int dx = Math.abs(x2 - x1);
	    int dy = Math.abs(y2 - y1);
	    
	    int sx = x1 < x2 ? 1 : -1;
	    int sy = y1 < y2 ? 1 : -1;
	    
	    int err = dx - dy;
	    
	    // Genişlik için başlangıç ve bitiş ofsetleri
	    int startX, endX, startY, endY;
	    if (width % 2 == 0) {
	        startX = -width / 2;
	        endX = width / 2 - 1;
	        startY = startX;
	        endY = endX;
	    } else {
	        startX = -(width - 1) / 2;
	        endX = (width - 1) / 2;
	        startY = startX;
	        endY = endX;
	    }
	    
	    while (true) {
	        // Mevcut nokta etrafında genişlik karesi çiz
	        for (int ox = startX; ox <= endX; ox++) {
	            for (int oy = startY; oy <= endY; oy++) {
	                int px = x1 + ox;
	                int py = y1 + oy;
	                if (px >= 0 && px < w && py >= 0 && py < h) {
	                    this.pixels.put(py * w + px, color);
	                }
	            }
	        }
	        
	        if (x1 == x2 && y1 == y2) break;
	        
	        int e2 = 2 * err;
	        
	        if (e2 > -dy) {
	            err -= dy;
	            x1 += sx;
	        }
	        
	        if (e2 < dx) {
	            err += dx;
	            y1 += sy;
	        }
	    }
	    
	    rewind();
	}

	public void draw(Bitmap b, int xp, int yp) {
		if (b == null) return;
		b.ensurePixels();

		if (b.w <= 0 || b.h <= 0) return;
		
		xp += xOffs;
		yp += yOffs;
		int x0 = xp;
		int x1 = xp + b.w;
		int y0 = yp;
		int y1 = yp + b.h;
		if (x0 < 0) x0 = 0;
		if (y0 < 0) y0 = 0;
		if (x1 > w) x1 = w;
		if (y1 > h) y1 = h;

		if (xFlip) {
			for (int y = y0; y < y1; y++) {
				int sp = (y - yp) * b.w + xp + b.w - 1;
				int dp = (y) * w;

				for (int x = x0; x < x1; x++) {
					int c = b.pixels.get(sp - x);
					if (c < 0) pixels.put(dp + x, b.pixels.get(sp - x));
				}
			}
		} else {
			for (int y = y0; y < y1; y++) {
				int sp = (y - yp) * b.w - xp;
				int dp = (y) * w;

				for (int x = x0; x < x1; x++) {
					int c = b.pixels.get(sp + x);
					if (c < 0) pixels.put(dp + x, b.pixels.get(sp + x));
				}
			}
		}
		
		b.rewind();
		rewind();
	}
	
	// Düzeltilmiş blend metodu
	public void blend(Bitmap b, int xp, int yp) {
		b.ensurePixels();
		
	    xp += xOffs;
	    yp += yOffs;
	    int x0 = xp;
	    int x1 = xp + b.w;
	    int y0 = yp;
	    int y1 = yp + b.h;
	    if (x0 < 0) x0 = 0;
	    if (y0 < 0) y0 = 0;
	    if (x1 > w) x1 = w;
	    if (y1 > h) y1 = h;

	    for (int y = y0; y < y1; y++) {
	        int sp = (y - yp) * b.w - xp;
	        int dp = y * w;

	        for (int x = x0; x < x1; x++) {
	            int c = b.pixels.get(sp + x);
	            int a = (c >> 24) & 0xff;
	            
	            // Sadece alfa değeri > 0 olan pikselleri işle
	            if (a > 0) {
	                int bgColor = pixels.get(dp + x);
	                
	                // Arka plan ve kaynak pikselin renk bileşenlerini çıkar
	                int bgR = (bgColor >> 16) & 0xff;
	                int bgG = (bgColor >> 8) & 0xff;
	                int bgB = bgColor & 0xff;
	                
	                int srcR = (c >> 16) & 0xff;
	                int srcG = (c >> 8) & 0xff;
	                int srcB = c & 0xff;
	                
	                // Alfa değerine göre renk karışımını hesapla
	                int newR = (srcR * a + bgR * (255 - a)) / 255;
	                int newG = (srcG * a + bgG * (255 - a)) / 255;
	                int newB = (srcB * a + bgB * (255 - a)) / 255;
	                
	                // Yeni pikseli ayarla (alfa kanalı tamamen opak)
	                pixels.put(dp + x, 0xff000000 | (newR << 16) | (newG << 8) | newB);
	            }
	        }
	    }
	    b.rewind();
	    rewind();
	}

	// Düzeltilmiş blendDraw metodu
	// Düzeltilmiş blendDraw metodu
	public void blendDraw(Bitmap b, int xp, int yp, int col) {
		b.ensurePixels();
		
	    xp += xOffs;
	    yp += yOffs;
	    int x0 = xp;
	    int x1 = xp + b.w;
	    int y0 = yp;
	    int y1 = yp + b.h;
	    if (x0 < 0) x0 = 0;
	    if (y0 < 0) y0 = 0;
	    if (x1 > w) x1 = w;
	    if (y1 > h) y1 = h;

	    // col renginin bileşenlerini çıkar
	    int colR = (col >> 16) & 0xff;
	    int colG = (col >> 8) & 0xff;
	    int colB = col & 0xff;

	    if (xFlip) {
	        for (int y = y0; y < y1; y++) {
	            int sp = (y - yp) * b.w + b.w - 1;
	            int dp = y * w;

	            for (int x = x0; x < x1; x++) {
	                int c = b.pixels.get(sp - (x - xp));
	                int a = (c >> 24) & 0xff;
	                
	                if (a > 0) {
	                    int srcR = (c >> 16) & 0xff;
	                    int srcG = (c >> 8) & 0xff;
	                    int srcB = c & 0xff;
	                    
	                    // Kaynak ve col değerini karıştır
	                    // Burada düzeltme - renkleri doğru şekilde karıştırma
	                    int blendR = (srcR * colR) / 255;
	                    int blendG = (srcG * colG) / 255;
	                    int blendB = (srcB * colB) / 255;
	                    
	                    // Mevcut piksel ile alfa değerine göre karıştır
	                    int bgColor = pixels.get(dp + x);
	                    int bgR = (bgColor >> 16) & 0xff;
	                    int bgG = (bgColor >> 8) & 0xff;
	                    int bgB = bgColor & 0xff;
	                    
	                    int newR = (blendR * a + bgR * (255 - a)) / 255;
	                    int newG = (blendG * a + bgG * (255 - a)) / 255;
	                    int newB = (blendB * a + bgB * (255 - a)) / 255;
	                    
	                    pixels.put(dp + x, 0xff000000 | (newR << 16) | (newG << 8) | newB);
	                }
	            }
	        }
	    } else {
	        for (int y = y0; y < y1; y++) {
	            int sp = (y - yp) * b.w;
	            int dp = y * w;

	            for (int x = x0; x < x1; x++) {
	                int c = b.pixels.get(sp + (x - xp));
	                int a = (c >> 24) & 0xff;
	                
	                if (a > 0) {
	                    int srcR = (c >> 16) & 0xff;
	                    int srcG = (c >> 8) & 0xff;
	                    int srcB = c & 0xff;
	                    
	                    // Kaynak ve col değerini karıştır
	                    // Burada düzeltme - renkleri doğru şekilde karıştırma
	                    int blendR = (srcR * colR) / 255;
	                    int blendG = (srcG * colG) / 255;
	                    int blendB = (srcB * colB) / 255;
	                    
	                    // Mevcut piksel ile alfa değerine göre karıştır
	                    int bgColor = pixels.get(dp + x);
	                    int bgR = (bgColor >> 16) & 0xff;
	                    int bgG = (bgColor >> 8) & 0xff;
	                    int bgB = bgColor & 0xff;
	                    
	                    int newR = (blendR * a + bgR * (255 - a)) / 255;
	                    int newG = (blendG * a + bgG * (255 - a)) / 255;
	                    int newB = (blendB * a + bgB * (255 - a)) / 255;
	                    
	                    pixels.put(dp + x, 0xff000000 | (newR << 16) | (newG << 8) | newB);
	                }
	            }
	        }
	    }
	    b.rewind();
	    rewind();
	}

	// Düzeltilmiş blendFill metodu
	public void blendFill(int x0, int y0, int x1, int y1, int color) {
	    x0 += xOffs;
	    y0 += yOffs;
	    x1 += xOffs;
	    y1 += yOffs;
	    if (x0 < 0) x0 = 0;
	    if (y0 < 0) y0 = 0;
	    if (x1 >= w) x1 = w - 1;
	    if (y1 >= h) y1 = h - 1;
	    
	    // color renginin bileşenlerini ve alfa değerini çıkar
	    int a = (color >> 24) & 0xff;
	    int srcR = (color >> 16) & 0xff;
	    int srcG = (color >> 8) & 0xff;
	    int srcB = color & 0xff;
	    
	    // Alfa değeri belirtilmemişse (0 ise), tamamen opak kabul et
	    //if (a == 0) a = 255; // WTF
	    
	    for (int y = y0; y <= y1; y++) {
	        for (int x = x0; x <= x1; x++) {
	            int bgColor = pixels.get(x + y * w);
	            int bgR = (bgColor >> 16) & 0xff;
	            int bgG = (bgColor >> 8) & 0xff;
	            int bgB = bgColor & 0xff;
	            
	            int newR = (srcR * a + bgR * (255 - a)) / 255;
	            int newG = (srcG * a + bgG * (255 - a)) / 255;
	            int newB = (srcB * a + bgB * (255 - a)) / 255;
	            
	            pixels.put(x + y * w, 0xff000000 | (newR << 16) | (newG << 8) | newB);
	        }
	    }
	    
	    rewind();
	}

	// Düzeltilmiş fogBlend metodu
	public void fogBlend(Bitmap b, int xp, int yp) {
		b.ensurePixels();
		
	    xp += xOffs;
	    yp += yOffs;
	    int x0 = xp;
	    int x1 = xp + b.w;
	    int y0 = yp;
	    int y1 = yp + b.h;
	    if (x0 < 0) x0 = 0;
	    if (y0 < 0) y0 = 0;
	    if (x1 > w) x1 = w;
	    if (y1 > h) y1 = h;

	    for (int y = y0; y < y1; y++) {
	        int sp = (y - yp) * b.w - xp;
	        int dp = y * w;

	        for (int x = x0; x < x1; x++) {
	            int c = b.pixels.get(sp + x);
	            // Eğer piksel şeffaf değilse işleme devam et
	            if (c != 0) {
	                // Alfa değerini al (0-255 arası)
	                int fogIntensity = c & 0xff;
	                // Sis yoğunluğu değeri olmadığında işlem yapma
	                if (fogIntensity > 0) {
	                    int bgColor = pixels.get(dp + x);
	                    int bgR = (bgColor >> 16) & 0xff;
	                    int bgG = (bgColor >> 8) & 0xff;
	                    int bgB = bgColor & 0xff;
	                    
	                    // Gri tonu hesapla
	                    int gray = (bgR * 30 + bgG * 59 + bgB * 11) / 100;
	                    
	                    // Sis yoğunluğuna göre karışım hesapla
	                    // fogIntensity ne kadar yüksekse o kadar sis efekti olacak
	                    int ic = 255 - fogIntensity;
	                    
	                    int newR = (bgR * ic + gray * fogIntensity) / 255;
	                    int newG = (bgG * ic + gray * fogIntensity) / 255;
	                    int newB = (bgB * ic + gray * fogIntensity) / 255;
	                    
	                    pixels.put(dp + x, 0xff000000 | (newR << 16) | (newG << 8) | newB);
	                }
	            }
	        }
	    }
	    
	    b.rewind();
	    rewind();
	}

	public void clear(int color) {
		for (int i = 0; i < pixels.size(); i++) {
		    pixels.put(i, color);
		}
	}

	public void setPixel(int xp, int yp, int color) {
		xp += xOffs;
		yp += yOffs;
		if (xp >= 0 && yp >= 0 && xp < w && yp < h) {
			pixels.put(xp + yp * w, color);
		}
	}

	public void shade(Bitmap shadows) {
		shadows.ensurePixels();
		
		for (int i = 0; i < pixels.size(); i++) {
			if (shadows.pixels.get(i) > 0) {
				int r = ((pixels.get(i) & 0xff0000) * 200) >> 8 & 0xff0000;
				int g = ((pixels.get(i) & 0xff00) * 200) >> 8 & 0xff00;
				int b = ((pixels.get(i) & 0xff) * 200) >> 8 & 0xff;
				pixels.put(i, 0xff000000 | r | g | b);
			}
		}
		shadows.rewind();
		rewind();
	}

	public void fill(int x0, int y0, int x1, int y1, int color) {
		x0 += xOffs;
		y0 += yOffs;
		x1 += xOffs;
		y1 += yOffs;
		if (x0 < 0) x0 = 0;
		if (y0 < 0) y0 = 0;
		if (x1 >= w) x1 = w - 1;
		if (y1 >= h) y1 = h - 1;
		for (int y = y0; y <= y1; y++) {
			for (int x = x0; x <= x1; x++) {
				pixels.put(x + y * w, color);
			}
		}
		rewind();
	}

	public void box(int x0, int y0, int x1, int y1, int color) {
		x0 += xOffs;
		y0 += yOffs;
		x1 += xOffs;
		y1 += yOffs;
		int xx0 = x0;
		int yy0 = y0;
		int xx1 = x1;
		int yy1 = y1;

		if (x0 < 0) x0 = 0;
		if (y0 < 0) y0 = 0;
		if (x1 >= w) x1 = w - 1;
		if (y1 >= h) y1 = h - 1;

		for (int y = y0; y <= y1; y++) {
			for (int x = x0; x <= x1; x++) {
				if (x == xx0 || y == yy0 || x == xx1 || y == yy1) pixels.put(x + y * w, color);
				if (y > yy0 && y < yy1 && x < xx1 - 1) {
					x = xx1 - 1;
				}
			}
		}
		rewind();
	}
	
	public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints, int color) {
	    // Apply offsets
	    int[] xp = new int[nPoints];
	    int[] yp = new int[nPoints];
	    for (int i = 0; i < nPoints; i++) {
	        xp[i] = xPoints[i] + xOffs;
	        yp[i] = yPoints[i] + yOffs;
	    }
	    
	    // Find the bounding box of the polygon
	    int minX = Integer.MAX_VALUE;
	    int maxX = Integer.MIN_VALUE;
	    int minY = Integer.MAX_VALUE;
	    int maxY = Integer.MIN_VALUE;
	    
	    for (int i = 0; i < nPoints; i++) {
	        minX = Math.min(minX, xp[i]);
	        maxX = Math.max(maxX, xp[i]);
	        minY = Math.min(minY, yp[i]);
	        maxY = Math.max(maxY, yp[i]);
	    }
	    
	    // Clip to bitmap boundaries
	    minX = Math.max(0, minX);
	    minY = Math.max(0, minY);
	    maxX = Math.min(w - 1, maxX);
	    maxY = Math.min(h - 1, maxY);
	    
	    // Scan each row within bounding box
	    for (int y = minY; y <= maxY; y++) {
	        // Find intersections with polygon edges for this scanline
	        List<Integer> intersections = new ArrayList<>();
	        
	        for (int i = 0; i < nPoints; i++) {
	            int j = (i + 1) % nPoints; // Next vertex
	            
	            // Skip horizontal edges
	            if (yp[i] == yp[j]) continue;
	            
	            // Check if the edge crosses this scanline
	            if ((yp[i] <= y && y < yp[j]) || (yp[j] <= y && y < yp[i])) {
	                // Calculate x-coordinate of intersection
	                int x = xp[i] + (y - yp[i]) * (xp[j] - xp[i]) / (yp[j] - yp[i]);
	                intersections.add(x);
	            }
	        }
	        
	        // Sort intersections by x-coordinate
	        Collections.sort(intersections);
	        
	        // Fill pixels between intersection pairs
	        for (int i = 0; i < intersections.size(); i += 2) {
	            if (i + 1 < intersections.size()) {
	                int startX = Math.max(minX, intersections.get(i));
	                int endX = Math.min(maxX, intersections.get(i + 1));
	                
	                // Fill the span
	                for (int x = startX; x <= endX; x++) {
	                    pixels.put(y * w + x, color);
	                }
	            }
	        }
	    }
	    rewind();
	}

	// Convenience overload that takes exactly 3 points (for triangles)
	public void fillPolygon(int[] xPoints, int[] yPoints, int color) {
	    fillPolygon(xPoints, yPoints, xPoints.length, color);
	}
	
	public Bitmap clone() {
		var localArray = this.pixels.array();
		var copyArray = new int[localArray.length];
		System.arraycopy(localArray, 0, copyArray, 0, localArray.length); 
		
		var cloned = new Bitmap(w, h, copyArray);
		cloned.xFlip = this.xFlip;
		cloned.xOffs = this.xOffs;
		cloned.yOffs = this.yOffs;
		
		return cloned;
	}
	
	public Bitmap clone(int w, int h) {
		Bitmap newBitmap = clone();
		Bitmap resizedBitmap = newBitmap.resize( w, h);
		return resizedBitmap;
	}

	@Override
	public EnumResource getResourceType() {
		return EnumResource.IMAGE;
	}

	public int getPixel(int srcX, int srcY) {
		return pixels.get(srcX + srcY * w);
	}

	public static Bitmap createClearedBitmap(int w, int h, int color) {
		Bitmap bitmap = new Bitmap(w, h);
		bitmap.clear(color);
		return bitmap;
	}

	public void saveResource(OutputStream stream) {
		try {
			ImageIO.write(toImage(), "png", stream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void drawText(String text, int x, int y, int color, boolean center) {
		x += xOffs;
		y += yOffs;
		if (center) {
			Text.renderCenter(text, this, x, y, color);
		} else {
			Text.render(text, this, x, y, color);
		}
	}
	
	public void drawText(String text, int x, int y, boolean center) {
		x += xOffs;
		y += yOffs;
		if (center) {
			Text.renderCenter(text, this, x, y);
		} else {
			Text.render(text, this, x, y);
		}
	}
	
	public void drawText(String text, int x, int y, UniFont font, boolean center) {
		x += xOffs;
		y += yOffs;
		if (center) {
			Text.renderCenter(text, this, x, y, font);
		} else {
			Text.render(text, this, x, y, font);
		}
	}
	
	public void drawText(String text, int x, int y, int color, UniFont font, boolean center) {
		x += xOffs;
		y += yOffs;
		if (center) {
			Text.renderCenter(text, this, x, y, color, font);
		} else {
			Text.render(text, this, x, y, color, font);
		}
	}
	
	public static Bitmap toBitmap(BufferedImage img) {
		if (img == null) {
			img = IOResource.missingTexture.toImage();
		}
		   
		int sw = img.getWidth();
		int sh = img.getHeight();

		int[] pixels = new int[sw * sh];
		img.getRGB(0, 0, sw, sh, pixels, 0, sw);

		return new Bitmap(sw, sh, pixels);
	}

    @Deprecated
	public Bitmap opposite(boolean upMode) {
		Bitmap bitmap = new Bitmap(w, h);
    	for (int y = 0; y < h; y++) {
        	for (int x = 0; x < w; x++) {
            	int srcIndex = x + y * w;
            	int destX, destY;

            	if (upMode) {
                	// Dikey ters çevirme (yukarı <-> aşağı)
                	destX = x;
                	destY = h - 1 - y;
            	} else {
                	// Yatay ters çevirme (sol <-> sağ)
                	destX = w - 1 - x;
                	destY = y;
            	}

            	int destIndex = destX + destY * w;
            	bitmap.pixels.put(destIndex, pixels.get(srcIndex));
        	}
    	}
    	rewind();
    	bitmap.rewind();
    	return bitmap;
	}
	
	// Bitmap.java içine:
	public byte[] toBytes(String format) {
	    try (java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()) {
	        javax.imageio.ImageIO.write(toImage(), format, out);
	        return out.toByteArray();
	    } catch (Exception e) {
	        e.printStackTrace();
	        return new byte[0];
	    }
	}

	public static Bitmap fromBytes(byte[] bytes) {
	    try {
	        java.awt.image.BufferedImage img =
	            javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(bytes));
	        return Bitmap.toBitmap(img);
	    } catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }
	}

	@Override
	@Deprecated
	public void clean() {}
	
	public Bitmap replaceColor(int targetColor, int replaceColor) {
		Bitmap cloned = this.clone();
		int size = cloned.w * cloned.h;
		cloned.rewind();

		for (int i = 0; i < size; i++) {
			if (cloned.pixels.get(i) == targetColor) {
				cloned.pixels.put(i, replaceColor);
			}
		}
		cloned.rewind();
		
		return cloned;
	}

	@Override
	public void saveResource(DataOutputStream out) throws IOException {
		byte[] data = this.toBytes("png");
		out.writeInt(data.length);
		out.write(data);
		
		out.writeBoolean(xFlip);
		out.writeInt(this.xOffs);
		out.writeInt(this.yOffs);
	}

	@Override
	public void loadResource(DataInputStream in) throws IOException {
		int length = in.readInt();
		byte[] data = new byte[length];
		in.readFully(data);
		Bitmap newBitmap = Bitmap.fromBytes(data);
		
		this.xFlip = in.readBoolean();
		this.xOffs = in.readInt();
		this.yOffs = in.readInt();
		
		this.pixels = newBitmap.pixels;
		this.w = newBitmap.w;
		this.h = newBitmap.h;
	}
	
	protected void ensurePixels() {
	    if (pixels == null) {
	        throw new IllegalStateException(
	            "Bitmap pixels NULL: constructor bypass or shadowed field"
	        );
	    }
	}

	public Point findColorPos(int color) {
		this.rewind();
		
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				if (getPixel(x, y) == color)
					return new Point(x, y);
			}
		}
		return null;
	}
}
