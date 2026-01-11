package me.ramazanenescik04.diken.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

import org.json.*;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.EnumResource;
import me.ramazanenescik04.diken.resource.IOResource;

public class UniFont {
	
	private static List<UniFont> unifonts = new ArrayList<UniFont>();
	
	public String charTypes = "";
	
	public String font_name;
	
	public Map<String, Bitmap> charBitmaps = new HashMap<String, Bitmap>();
	
	public String name;
	
	private static Bitmap missingChar;
	
	public static void createFont(String fontName) {
		UniFont font = new UniFont();
		
		Bitmap bitmap = (Bitmap) IOResource.loadResource(IOResource.createClassResourceStream("/fonts/" + fontName + "/font_bitmap.png"), EnumResource.IMAGE);
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
		    	font.charBitmaps.put(chara, charBitmap);
    			continue;
    		}
		    
		    for(int x = 0; x < obj1.getInt("width"); x++) {
		    	for(int y = 0; y < obj1.getInt("height"); y++) {
		    		
		    		int color = bitmap.pixels.get((x + obj1.getInt("x")) + (y + obj1.getInt("y")) * bitmap.w);
		    		charBitmap.setPixel(x, y, color);
		    		
		    		font.charBitmaps.put(chara, charBitmap);
			    }
		    }
		    
		    
		}
		
		DikenEngine.log("Loaded Font: " + fontName);
		
		unifonts.add(font);
	}
	
	public static void createFont(Font font) {
		UniFont de_font = new UniFont();
				
		BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2d = img.createGraphics();
		g2d.setFont(font);
		FontMetrics fontMetrics = g2d.getFontMetrics();
			        
		Map<String, Bitmap> charBitmaps = new HashMap<String, Bitmap>();

		for (int i=0; i < font.getNumGlyphs(); i++) {
			if (font.canDisplay(i)) {
				int w = fontMetrics.charWidth(i);
			    int h = fontMetrics.getHeight();
			    if(w <= 0 || h <= 0) {
			    	continue;
			    }
			    de_font.charTypes += "" + (char)i;
			    BufferedImage font_img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			    Graphics2D font_g2d = font_img.createGraphics();
			    font_g2d.setFont(font);
			    font_g2d.setColor(Color.WHITE);
			    font_g2d.drawString("" + (char)i, 0, fontMetrics.getAscent());
			    font_g2d.dispose();
			    charBitmaps.put("" + (char)i, Bitmap.toBitmap(font_img));
			}
		}
			    
		Locale locale = Locale.getDefault();
			    
		de_font.name = font.getFontName() + "." + font.getSize();
		de_font.font_name = font.getFontName() + "_" + locale.getLanguage()  + "-" + locale.getCountry();
		de_font.charBitmaps = charBitmaps;
		
		de_font.name = de_font.name.replaceAll(" ", "_");
			        
		g2d.dispose();
			    
		unifonts.add(de_font);
			    
		DikenEngine.log("Loaded Font: " + de_font.name);
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
				Bitmap charBitmap = font.charBitmaps.getOrDefault(ch + "", getMissingChar());
				list.add(charBitmap);
			}
		}
		return list.toArray(new Bitmap[] {});
	}
	
	public static Bitmap getBitmapChar(char chara, UniFont font) {
		if(font == null) {
			return getMissingChar();
		}
		
		return font.charBitmaps.getOrDefault(chara + "", getMissingChar());
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