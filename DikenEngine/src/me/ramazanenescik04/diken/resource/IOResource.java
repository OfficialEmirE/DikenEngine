package me.ramazanenescik04.diken.resource;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import me.ramazanenescik04.diken.game.Animation;

public class IOResource {
	
	public static final Bitmap missingTexture = generateMissingTexture();
	
	public static IResource loadResource(InputStream stream, EnumResource _enum) {
		if (_enum == EnumResource.IMAGE) {
			BufferedImage img = null;
			try {
				img = ImageIO.read(stream);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return Bitmap.toBitmap(img);
		} else if (_enum == EnumResource.SOUND) {
			return SoundResource.loadSound(stream);
		} else if (_enum == EnumResource.CURSOR) {
			Bitmap cursorBitmap = (Bitmap) loadResource(stream, EnumResource.IMAGE);
			CursorResource res = new CursorResource();
			res.cursorBitmap = cursorBitmap;
			
			return res;
		} else if (_enum == EnumResource.ANIMATION) {
			try {
				return Animation.load(stream);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static Bitmap[][] loadResourceAndCut(InputStream stream, int sw, int sh) {
		BufferedImage img = ((Bitmap)loadResource(stream, EnumResource.IMAGE)).toImage();
		int xSlices = img.getWidth() / sw;
	    int ySlices = img.getHeight() / sh;
	    Bitmap[][] result = new Bitmap[xSlices][ySlices];

	    for(int x = 0; x < xSlices; ++x) {
	    	for(int y = 0; y < ySlices; ++y) {
	        	result[x][y] = new Bitmap(sw, sh);
	        	int[] pixels = new int[sw * sh];
	        	img.getRGB(x * sw, y * sh, sw, sh, pixels, 0, sw);
	        	result[x][y].pixels.clear();
	        	result[x][y].pixels.rewind().put(pixels);
	        }
	    }

	    return result;
	}
	
	public static InputStream createClassResourceStream(String path) {
		InputStream stream = IOResource.class.getResourceAsStream(path);
		if (stream == null) {
			System.err.println("Error: Resource not found: " + path);
		}
		return stream;
	}
	
	@Deprecated(since="1.0.0", forRemoval=true)
	public static InputStream createFileStream(String path) {
		try {
			return new FileInputStream(path);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static InputStream createFileStream(File path) {
		try {
			return new FileInputStream(path);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private static Bitmap generateMissingTexture() {
		Bitmap bitmap = new Bitmap(16, 16);
		bitmap.fill(0, 0, 15, 15, 0xff000000);
		bitmap.fill(0, 8, 7, 15, 0xff76428a);
		bitmap.fill(8, 0, 15, 7, 0xff76428a);
		return bitmap;
	}
}
