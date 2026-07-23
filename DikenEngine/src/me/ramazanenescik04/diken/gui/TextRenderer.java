package me.ramazanenescik04.diken.gui;

import java.util.List;
import java.util.stream.Stream;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.resource.Bitmap;

/**
 * Represents the `Text` type within the DikenEngine `gui.compoment` package.
 */
public class TextRenderer {
	private static final char COLOR_CODE_MARKER = '\u00A7';
	private static final int RGB_HEX_LENGTH = 6;
	private static final int ARGB_HEX_LENGTH = 8;
	
	public static void render(String text, Bitmap bitmap, int x, int y, int color, UniFont font) {
		if (font == null) {
			font = DikenEngine.getEngine().defaultFont;
		}
		
		List<String> lines = text.lines().toList();
	    
	    // Height of a single line (approximate)
	    int lineHeight = stringBitmapAverageHeight(new String[] {text}, font) + 2; // Add a small padding
	    
	    int currentColor = color;
	    for (int i = 0; i < lines.size(); i++) {
	        String lineText = lines.get(i);
	        int w = 0;
	        
	        for (int j = 0; j < lineText.length(); j++) {
	        	ColorCode colorCode = readColorCode(lineText, j);
	        	if (colorCode != null) {
	        		currentColor = colorCode.color;
	        		j = colorCode.endIndex;
	        		continue;
	        	}
	        	
	            Bitmap btp = UniFont.getBitmapChar(lineText.charAt(j), font);
	            
	            // Render character
	            bitmap.blendDraw(btp, x + w, y + (i * lineHeight), currentColor);
	            
	            w += ((btp.w));
	        }
	    }
	}
	
	public static void render(String text, Bitmap bitmap, int x, int y, int color) {
		render(text, bitmap, x, y, color, DikenEngine.getEngine().defaultFont);
	}
	
	public static void render(String text, Bitmap bitmap, int x, int y, UniFont font) {
		render(text, bitmap, x, y, 0xffffffff, font);
	}
	
	public static void render(String text, Bitmap bitmap, int x, int y) {
		render(text, bitmap, x, y, 0xffffffff, DikenEngine.getEngine().defaultFont);
	}
	
	public static void renderCenter(String string, Bitmap bitmap, int x, int y) {
		renderCenter(string, bitmap, x, y, 0xffffffff, DikenEngine.getEngine().defaultFont);
	}
	
	public static void renderCenter(String string, Bitmap bitmap, int x, int y, int color) {
		renderCenter(string, bitmap, x, y, color, DikenEngine.getEngine().defaultFont);
	}
	
	public static void renderCenter(String string, Bitmap bitmap, int x, int y, UniFont font) {
		renderCenter(string, bitmap, x, y, 0xffffffff, font);
	}
	
	public static void renderCenter(String string, Bitmap bitmap, int x, int y, int color, UniFont font) {
		int x1 = x - (stringBitmapWidth(string, font) / 2);
		
		render(string, bitmap,x1, y, color, font);
	}
	
	public static int stringBitmapWidth(String text, UniFont font) {
		int w = 0;
		for (int i = 0; i < text.length(); i++) {
			ColorCode colorCode = readColorCode(text, i);
			if (colorCode != null) {
				i = colorCode.endIndex;
				continue;
			}
			
			Bitmap btp = UniFont.getBitmapChar(text.charAt(i), font);
			w += ((btp.w));
		}
		
		return w;
	}
	
	public static int stringBitmapAverageWidth(String[] texts, UniFont font) {
		int maxLength = 0;
	    for (String str : texts) {
	    	 if (str != null) {
	             int length = 0;
	             for (int i = 0; i < str.length(); i++) {
	            	ColorCode colorCode = readColorCode(str, i);
	     			if (colorCode != null) {
	     				i = colorCode.endIndex;
	     				continue;
	     			}
	     			
	                 Bitmap btp = UniFont.getBitmapChar(str.charAt(i), font);
	                 length += ((btp.w));
	             }
	             if (length > maxLength) {
	                 maxLength = length;
	             }
	         }
	    }
	    
	    return maxLength;
	}
	
	public static int stringBitmapAverageWidth(String text, UniFont font) {
		Stream<String> lines = text.lines();
		String[] texts = lines.toArray(String[]::new);
		
		return stringBitmapAverageWidth(texts, font);
	}
	
	public static int stringBitmapAverageHeight(String text, UniFont font) {
		Stream<String> lines = text.lines();
		String[] texts = lines.toArray(String[]::new);
		
		return stringBitmapAverageHeight(texts, font);
	}
	
	public static int stringBitmapAverageHeight(String[] texts, UniFont font) {
		int maxHeight = 0;
	    for (String str : texts) {
	    	 if (str != null) {
	             for (int i = 0; i < str.length(); i++) {
	            	ColorCode colorCode = readColorCode(str, i);
	     			if (colorCode != null) {
	     				i = colorCode.endIndex;
	     				continue;
	     			}
	     			
	                 Bitmap btp = UniFont.getBitmapChar(str.charAt(i), font);
	                 if (btp.h > maxHeight) {
	                     maxHeight = btp.h;
	                 }
	             }
	         }
	    }
	    
		return maxHeight;
	}

	private static ColorCode readColorCode(String text, int markerIndex) {
		if (text.charAt(markerIndex) != COLOR_CODE_MARKER) {
			return null;
		}
		
		ColorCode argbColor = readColorCode(text, markerIndex, ARGB_HEX_LENGTH);
		if (argbColor != null) {
			return argbColor;
		}
		
		ColorCode rgbColor = readColorCode(text, markerIndex, RGB_HEX_LENGTH);
		if (rgbColor != null) {
			return rgbColor;
		}
		
		return null;
	}

	private static ColorCode readColorCode(String text, int markerIndex, int hexLength) {
		int hexStartIndex = markerIndex + 1;
		int hexEndIndex = hexStartIndex + hexLength;
		if (hexEndIndex > text.length()) {
			return null;
		}
		
		for (int i = hexStartIndex; i < hexEndIndex; i++) {
			if (!isHexChar(text.charAt(i))) {
				return null;
			}
		}
		
		long parsedColor = Long.parseLong(text.substring(hexStartIndex, hexEndIndex), 16);
		if (hexLength == RGB_HEX_LENGTH) {
			parsedColor |= 0xff000000L;
		}
		
		return new ColorCode((int) parsedColor, hexEndIndex - 1);
	}

	private static boolean isHexChar(char c) {
		return (c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F');
	}

	private static class ColorCode {
		private final int color;
		private final int endIndex;

		private ColorCode(int color, int endIndex) {
			this.color = color;
			this.endIndex = endIndex;
		}
	}

	public static String wordWrapString(String message, int i, UniFont defaultFont) {
		StringBuilder wrappedText = new StringBuilder();
		String[] words = message.split(" ");
		int lineLength = 0;
		
		for (String word : words) {
			int wordLength = stringBitmapWidth(word + " ", defaultFont);
			
			if (lineLength + wordLength > i) {
				wrappedText.append("\n");
				lineLength = 0;
			}
			
			wrappedText.append(word).append(" ");
			lineLength += wordLength;
		}
		
		return wrappedText.toString().trim();
	}
}
