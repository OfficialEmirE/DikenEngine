package me.ramazanenescik04.diken.resource;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.Animation;
import me.ramazanenescik04.diken.gui.UniFont;

/**
 * Represents the `IOResource` type within the DikenEngine `resource` package.
 */
public class IOResource {
	public static final Bitmap missingTexture = generateMissingTexture();
	
	public static IResource loadResource(InputStream stream, EnumResource _enum) {
		try {
			if (_enum == EnumResource.IMAGE) {
				BufferedImage img = ImageIO.read(stream);

				return Bitmap.toBitmap(img);
			} else if (_enum == EnumResource.SOUND) {
				return SoundResource.fromWav(stream, java.util.UUID.randomUUID().toString());
			} else if (_enum == EnumResource.CURSOR) {
				Bitmap cursorBitmap = (Bitmap) loadResource(stream, EnumResource.IMAGE);
				CursorResource res = new CursorResource();
				res.cursorBitmap = cursorBitmap;

				return res;
			} else if (_enum == EnumResource.ANIMATION) {
				return Animation.load(stream);
			} else if (_enum == EnumResource.FONT) {
				return IResource.loadResource(new DataInputStream(stream), UniFont.class);
			}
		} catch (Exception e) {
			DikenEngine.errorLog("Error loading " + _enum.name() + " resource.", e);
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
	    		int[] pixels = new int[sw * sh];
	        	img.getRGB(x * sw, y * sh, sw, sh, pixels, 0, sw);
	    		
	        	result[x][y] = new Bitmap(sw, sh, pixels);
	        }
	    }

	    return result;
	}
	
	public static InputStream createClassResource(String path) {
		InputStream stream = IOResource.class.getResourceAsStream(path);
		if (stream == null) {
			System.err.println("Error: Resource not found: " + path);
		}
		return stream;
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
