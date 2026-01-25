package me.ramazanenescik04.diken.tools;

import me.ramazanenescik04.diken.resource.Bitmap;

public class PixelToColor {
	
	public static int toRedColor(int color) {
		return (color >> 16) & 0xff;
	}
	
	public static int toGreenColor(int color) {
		return (color >> 8) & 0xff;
	}

	public static int toBlueColor(int color) {
		return (color >> 0) & 0xff;
	}
	
	public static int toAlphaColor(int color) {
		return (color >> 24) & 0xff;
	}
	
	public static int toColor(int alpha, int red, int green, int blue) {
		return (alpha << 24) | (red << 16) | (green << 8) | blue;
	}

	public static int toColor(float alpha, float red, float green, float blue) {
		return toColor(
			(int) (alpha * 255),
			(int) (red * 255),
			(int) (green * 255),
			(int) (blue * 255)
		);
	}
	
	public static int colorAddAlpha(int rgb, int alpha) {
		int red = (rgb >> 16) & 0xff;
		int green = (rgb >> 8) & 0xff;
		int blue = (rgb >> 0) & 0xff;
		
		return toColor(alpha, red, green, blue);
	}
	
	public static int hsvToRgb(float h, float s, float v) {
	    float r = 0, g = 0, b = 0;
	    
	    // H (Hue) değerini 0-6 arasına ölçekliyoruz (360 derece / 60)
	    int i = (int) (h * 6);
	    float f = h * 6 - i;
	    float p = v * (1 - s);
	    float q = v * (1 - f * s);
	    float t = v * (1 - (1 - f) * s);

	    // Hue sektörüne göre renk ataması
	    switch (i % 6) {
	        case 0: r = v; g = t; b = p; break;
	        case 1: r = q; g = v; b = p; break;
	        case 2: r = p; g = v; b = t; break;
	        case 3: r = p; g = q; b = v; break;
	        case 4: r = t; g = p; b = v; break;
	        case 5: r = v; g = p; b = q; break;
	    }

	    // 0-1 aralığını 0-255 aralığına çeviriyoruz
	    return toColor(1.0f, r, g, b);
	}
	
	public static float[] rgbToHsv(int color) {
	    // 0-255 aralığını 0.0-1.0 arasına çekiyoruz
	    float rf = toRedColor(color) / 255.0f;
	    float gf = toGreenColor(color) / 255.0f;
	    float bf = toBlueColor(color) / 255.0f;

	    float max = Math.max(rf, Math.max(gf, bf));
	    float min = Math.min(rf, Math.min(gf, bf));
	    float delta = max - min;

	    float h = 0, s, v;

	    // 1. Value (Parlaklık) hesaplama
	    v = max;

	    // 2. Saturation (Doygunluk) hesaplama
	    if (max != 0) {
	        s = delta / max;
	    } else {
	        // Renk tamamen siyahsa
	        return new float[]{0, 0, 0};
	    }

	    // 3. Hue (Renk Özü) hesaplama
	    if (delta == 0) {
	        h = 0; // Renk gri tonlarındaysa açı yoktur
	    } else {
	        if (max == rf) {
	            h = (gf - bf) / delta + (gf < bf ? 6 : 0);
	        } else if (max == gf) {
	            h = (bf - rf) / delta + 2;
	        } else if (max == bf) {
	            h = (rf - gf) / delta + 4;
	        }
	        h /= 6; // 0-1 aralığına normalize et
	    }

	    return new float[]{h, s, v};
	}
	
	public static Bitmap createHSVRect(int width, int height, float h) {
		Bitmap bitmap = new Bitmap(width, height);
		for (int y = 0; y < height; y++) {
	        for (int x = 0; x < width; x++) {
	            // X ekseni S (Saturation) değerini belirler: 0'dan 1.0'a
	            float s = (float) x / (width - 1);
	            
	            // Y ekseni V (Value) değerini belirler: 1.0'dan 0'a (Üst taraf parlak olsun diye)
	            float v = 1.0f - ((float) y / (height - 1));
	            
	            // HSV'yi RGB'ye çevirip rengi alıyoruz
	            int color = hsvToRgb(h, s, v);
	            
	            // Piksele rengi basıyoruz
	            bitmap.setPixel(x, y, color);
	        }
	    }
		return bitmap;
	}
	
	public static Bitmap createHColorRect(int width, int height) {
		Bitmap bitmap = new Bitmap(width, height);
		for (int y = 0; y < height; y++) {
	        for (int x = 0; x < width; x++) {
	            // X ekseni S (Saturation) değerini belirler: 0'dan 1.0'a
	            float s = (float) x / (width - 1);
	            
	            // HSV'yi RGB'ye çevirip rengi alıyoruz
	            int color = hsvToRgb(s, 1.0f, 1.0f);
	            
	            // Piksele rengi basıyoruz
	            bitmap.setPixel(x, y, color);
	        }
	    }
		return bitmap;
	}
	
	@Deprecated(since = "1.2.0", forRemoval = true)
	public static int blendColor(int oldColor, int blendColor) {
		int bgR = (blendColor >> 16) & 0xff;
        int bgG = (blendColor >> 8) & 0xff;
        int bgB = blendColor & 0xff;
        
        int a = (oldColor >> 24) & 0xff;
	    int srcR = (oldColor >> 16) & 0xff;
	    int srcG = (oldColor >> 8) & 0xff;
	    int srcB = oldColor & 0xff;
		
		int newR = (srcR * a + bgR * (255 - a)) / 255;
        int newG = (srcG * a + bgG * (255 - a)) / 255;
        int newB = (srcB * a + bgB * (255 - a)) / 255;
        
        return  0xff000000 | (newR << 16) | (newG << 8) | newB;
	 }

}
