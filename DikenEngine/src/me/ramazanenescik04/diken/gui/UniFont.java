package me.ramazanenescik04.diken.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.json.*;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.EnumResource;
import me.ramazanenescik04.diken.resource.IOResource;

/**
 * Represents the `UniFont` type within the DikenEngine `gui` package.
 */
public class UniFont {
	
	private static List<UniFont> unifonts = new ArrayList<>();
	
	public String charTypes = "";
	
	public String font_name;
	
	public Map<Character, Bitmap> charBitmaps = new HashMap<>();
	
	public String name;
	
	private static Bitmap missingChar;
	
	public static void createFont(String fontName) {
		UniFont font = new UniFont();
		
		Bitmap bitmap = (Bitmap) IOResource.loadResource(UniFont.class.getResourceAsStream("/fonts/" + fontName + "/font_bitmap.png"), EnumResource.IMAGE);
		font.name = fontName;
		BufferedReader reader = new BufferedReader(new InputStreamReader(UniFont.class.getResourceAsStream("/fonts/" + fontName + "/font_data.json")));
		String data = "",data2 = "";
		
		try {
			while((data = reader.readLine()) != null) {
				data2 = data2 + data;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		JSONObject obj = new JSONObject(data2);
		font.font_name = obj.getString("font_name") + "-" + obj.getString("author");
		
		JSONArray array = obj.getJSONArray("chars");
		
		for (int i = 0; i < array.length(); i++) {
			JSONObject obj1 = array.getJSONObject(i);
			String chara = obj1.getString("char");
			font.charTypes = font.charTypes + chara;
			
			Bitmap charBitmap = new Bitmap(obj1.getInt("width"), obj1.getInt("height"));
		    
		    if (obj1.getInt("x") < 0 || obj1.getInt("y") < 0) {
		    	font.charBitmaps.put(chara.charAt(0), charBitmap);
    			continue;
    		}
		    
		    for(int x = 0; x < obj1.getInt("width"); x++) {
		    	for(int y = 0; y < obj1.getInt("height"); y++) {
		    		
		    		int color = bitmap.pixels.get((x + obj1.getInt("x")) + (y + obj1.getInt("y")) * bitmap.w);
		    		charBitmap.setPixel(x, y, color);
		    		
		    		font.charBitmaps.put(chara.charAt(0), charBitmap);
			    }
		    }
		    
		    
		}
		
		DikenEngine.log("Loaded Font: " + fontName);
		
		unifonts.add(font);
	}
	
	public static UniFont createFont(Font font) {
		var unifont = new UniFont();
		var scratchImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

		var g2d = scratchImage.createGraphics();
		g2d.setFont(font);
		var fontMetrics = g2d.getFontMetrics();

		// Modern stream API ile glyph'leri işle
		var charBitmaps = IntStream.range(0, font.getNumGlyphs()).filter(font::canDisplay).mapToObj(codePoint -> {
			int width = fontMetrics.charWidth(codePoint) + 1;
			int height = fontMetrics.getHeight();

			if (width <= 0 || height <= 0) {
				return null;
			}

			char character = (char) codePoint;
			unifont.charTypes += character;
			var bitmap = renderCharacter(character, font, fontMetrics, width, height);

			return Map.entry(character, bitmap);
		}).filter(Objects::nonNull).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

		unifont.charBitmaps = charBitmaps;

		// Modern string formatting
		var locale = Locale.getDefault();
		unifont.name = "%s.%d".formatted(font.getFontName(), font.getSize()).replace(" ", "_");
		unifont.font_name = "%s_%s-%s".formatted(font.getFontName(), locale.getLanguage(), locale.getCountry());

		g2d.dispose();

		unifonts.add(unifont);
		DikenEngine.log("Loaded Font: " + unifont.name);

		return unifont;
	}

	private static Bitmap renderCharacter(char character, Font font, FontMetrics fontMetrics, int width, int height) {
		var fontImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		var g2d = fontImage.createGraphics();
		
		g2d.setFont(font);
		g2d.setColor(Color.WHITE);
		g2d.drawString(String.valueOf(character), 0, fontMetrics.getAscent());

		return Bitmap.toBitmap(fontImage);
	}
	
	public static boolean removeFont(String s) {
        UniFont font = getFont(s);
        if(font == null) {
        	return false;
        }
		unifonts.remove(font);
		return true;
	}
	
	public static UniFont getFont(String name) {
		UniFont font = null;
		
		for (int i = 0; i < unifonts.size(); i++) {
			UniFont tmpFont = unifonts.get(i);
			
			if(tmpFont.name.equals(name)) {
				font = tmpFont;
			}
		}
		
		return font;
	}
	
	public static int size() {
		return unifonts.size();
	}
	
	public static Bitmap[] getBitmapChars(String text, UniFont font) {
		List<Bitmap> list = new ArrayList<Bitmap>();
		for (int i = 0; i < text.length(); i++) {
			if(font == null) {
				list.add(getMissingChar());
			} else {
				char ch = text.charAt(i);
				Bitmap charBitmap = font.charBitmaps.getOrDefault(ch, getMissingChar());
				list.add(charBitmap);
			}
		}
		return list.toArray(new Bitmap[] {});
	}
	
	public static Bitmap getBitmapChar(char chara, UniFont font) {
		if(font == null) {
			return getMissingChar();
		}
		
		return font.charBitmaps.getOrDefault(chara, getMissingChar());
	}
	
	public static UniFont getFont(int id) {
		UniFont font = unifonts.get(id);
		return font;
	}
	
	public static Bitmap getMissingChar() {
		if (missingChar == null) {
			missingChar = generateMissingChar();
		}
		return missingChar;
	}
	
	private static Bitmap generateMissingChar() {
		Bitmap bitmap = new Bitmap(6, 8);
		bitmap.box(1, 0, 6 - 2, 8 - 1, 0xffffffff);
		return bitmap;
	}

}
