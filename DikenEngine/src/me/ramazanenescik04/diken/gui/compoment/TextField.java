package me.ramazanenescik04.diken.gui.compoment;

import java.util.function.Consumer;

import org.lwjgl.input.Keyboard;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.gui.UniFont;
import me.ramazanenescik04.diken.resource.Bitmap;

/**
 * TextField class for creating a text input field in the GUI.
 * This class extends the GuiCompoment class and provides functionality for rendering,
 * handling keyboard input, and managing focus state.
 * 
 * @author Ramazanenescik04
 */
public class TextField extends GuiComponent {
	
	public static final long serialVersionUID = 1L;	
	public String text = "";
	
	private int counter;
	private boolean isFocused;
	public boolean isNumberField = false;
	private Consumer<String> textChanced;

	public TextField(int x, int y, int width, int height) {
		super(x, y, width, height);
	}
	
	public TextField(String text, int x, int y, int width, int height) {
		super(x, y, width, height);
		this.text = text;
	}
	
	public TextField setTextChanced(Consumer<String> consumer) {
		this.textChanced = consumer;
		return this;
	}

	public Bitmap render() {
		Bitmap bitmap = super.render();
		bitmap.fill(0, 0, width, height, 0xff484848);
		bitmap.box(0, 0, width - 1, height - 1, isFocused() ? 0xffffff00 : 0xffffffff);
		Text.render(getRenderedText(), bitmap, 2, 2);
		return bitmap;
	}
	
	protected String getRenderedText() {
		String text = new String(this.text);
		
		if(isFocused) {
			text = text + (this.counter / 6 % 12 > 6?"_":"");
		}
		return text;
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
		if (textChanced != null) {
			textChanced.accept(text);
		}
	}
	
	public void setFocused(boolean focused) {
		this.isFocused = focused;
	}

	public boolean isFocused() {
		return isFocused;
	}

	public void tick(DikenEngine engine) {
		counter++;
	}

	public void keyPressed(char var1, int var2) {
		UniFont defaultFont = DikenEngine.getEngine().defaultFont;
		if (isFocused) {
			if (var2 == Keyboard.KEY_BACK) {
				if (text.length() > 0) {
					text = text.substring(0, text.length() - 1);
				}
			} else if (var2 == Keyboard.KEY_RETURN) {
				isFocused = false;
			} else if (var2 == Keyboard.KEY_ESCAPE) {
				isFocused = false;
			} else if (var2 == Keyboard.KEY_DELETE) {
				text = "";
				if (textChanced != null) {
					textChanced.accept(text);
				}
			} else if (isNumberField && Character.isDigit(var1)) {
				text += var1;
				if (textChanced != null) {
					textChanced.accept(text);
				}
			} else if (!isNumberField && defaultFont.charTypes.indexOf(var1) >= 0) {
				text += var1;
				if (textChanced != null) {
					textChanced.accept(text);
				}
			}
		}
	}

	public void mouseClicked(int x, int y, int button, boolean isTouch) {
		if (button == 0) {
			if (isTouch) {
				this.isFocused = !this.isFocused;
			} else {
				this.isFocused = false;
			}
		}
	}
	
	public TextField setNumberic() {
		this.isNumberField = true;
		return this;
	}
}
