package me.ramazanenescik04.diken.gui.compoment;

import java.util.stream.Stream;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.gui.UniFont;
import me.ramazanenescik04.diken.resource.Bitmap;

/**
 * Represents the `Text` type within the DikenEngine `gui.compoment` package.
 */
public class Text extends GuiComponent {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public String text;
	public int color;
	public UniFont font;
	
	public int offsetX, offsetY;

	public Text(String text, int x, int y) {
		this(text, x, y, 0xFFFFFFFF, 0, 0, DikenEngine.getEngine().defaultFont);
	}
	
	public Text(String text, int x, int y, UniFont font) {
		this(text, x, y, 0, 0, 0xFFFFFFFF, font);
	}
	
	public Text(String text, int x, int y, int offsetX, int offsetY, int color, UniFont font) {
		super(x, y, Text.stringBitmapAverageWidth(text, font) + offsetX, Text.stringBitmapAverageHeight(text, font) + offsetY);
		this.text = text;
		this.color = color;
		this.font = font;
		this.offsetX = offsetX;
		this.offsetY = offsetY;
	}
	
	public Text(String text, int x, int y, int color) {
		this(text, x, y, 0, 0, color, DikenEngine.getEngine().defaultFont);
	}
	
	public Bitmap render() {
		Bitmap bitmap = super.render();
		Text.render(text, bitmap, offsetX, offsetY, color, font);
		return bitmap;
	}
	
	@Override
	public void tick(DikenEngine engine) {
		if((this.width != Text.stringBitmapWidth(text, font) + offsetX)) {
			this.width = Text.stringBitmapWidth(text, font) + offsetX;
		}
		
		if((this.height != Text.stringBitmapAverageHeight(text, font) + offsetY)) {
			this.height = Text.stringBitmapAverageHeight(text, font) + offsetY;
		}
	}
	
	public Text setOffsetLocation(int x, int y) {
		this.offsetX = x;
		this.offsetY = y;
		return this;
	}

	public static void render(String text, Bitmap bitmap, int x, int y, int color, UniFont font) {
		if (font == null) {
			font = DikenEngine.getEngine().defaultFont;
		}
		
		Stream<String> lines = text.lines();
		String[] texts = lines.toArray(String[]::new);
	    
	    // Height of a single line (approximate)
	    int lineHeight = stringBitmapAverageHeight(new String[] {text}, font) + 2; // Add a small padding
	    
	    for (int i = 0; i < texts.length; i++) {
	        String lineText = texts[i];
	        Bitmap[] chars = UniFont.getBitmapChars(lineText, font);
	        int w = 0;
	        
	        for (int j = 0; j < chars.length; j++) {
	            Bitmap btp = chars[j];
	            
	            // Render character
	            bitmap.blendDraw(btp, x + w, y + (i * lineHeight), color);
	            
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
		int x1 = x - (string.length() * 6 / 2);
		
		render(string, bitmap,x1, y, color, font);
	}
	
	public static int stringBitmapWidth(String text, UniFont font) {
		Bitmap[] chars = UniFont.getBitmapChars(text, font);
		int w = 0;
		for (int i = 0; i < chars.length; i++) {
			Bitmap btp = chars[i];
			w += ((btp.w));
		}
		
		return w;
	}
	
	public static int stringBitmapAverageWidth(String[] texts, UniFont font) {
		int maxLength = 0;
	    for (String str : texts) {
	    	 if (str != null) {
	    		 Bitmap[] chars = UniFont.getBitmapChars(str, font);
	             int length = 0;
	             for (int i = 0; i < chars.length; i++) {
	                 Bitmap btp = chars[i];
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
	    		 Bitmap[] chars = UniFont.getBitmapChars(str, font);
	             for (int i = 0; i < chars.length; i++) {
	                 Bitmap btp = chars[i];
	                 if (btp.h > maxHeight) {
	                     maxHeight = btp.h;
	                 }
	             }
	         }
	    }
	    
	    return maxHeight;
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
