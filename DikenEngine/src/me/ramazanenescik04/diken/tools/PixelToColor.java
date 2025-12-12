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

}
