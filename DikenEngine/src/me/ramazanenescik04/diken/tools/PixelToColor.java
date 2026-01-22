package me.ramazanenescik04.diken.tools;

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
