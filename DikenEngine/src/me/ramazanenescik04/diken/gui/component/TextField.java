package me.ramazanenescik04.diken.gui.component;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.KeyEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.EnumSettingType;
import me.ramazanenescik04.diken.game.setting.Setting;
import me.ramazanenescik04.diken.game.setting.SettingCategory;
import me.ramazanenescik04.diken.gui.TextRenderer;
import me.ramazanenescik04.diken.gui.UDim2;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

/**
 * TextField class for creating a text input field in the GUI.
 * This class extends the GuiCompoment class and provides functionality for rendering,
 * handling keyboard input, and managing focus state.
 * 
 * @author Ramazanenescik04
 */
public class TextField extends GuiComponent {
	
	private String text = "";
	
	private int counter;
	private boolean isFocused;
	private boolean isPressingControl;
	private boolean isNumberField = false;
	private Consumer<String> textChanced;

	public TextField(UDim2 position, UDim2 size) {
		this("", position, size);
	}
	
	public TextField(String text, UDim2 position, UDim2 size) {
		super("TextField", position, size);
		this.text = text;
	}

	public TextField(DataInputStream in) throws IOException {
		super(in);
		if (getClass() == TextField.class) {
			loadNodeData(in);
		}
	}
	
	public TextField setTextChanged(Consumer<String> consumer) {
		this.textChanced = consumer;
		return this;
	}

	public Bitmap render() {
		var width = this.getWidth();
		var height = this.getHeight();
		
		Bitmap bitmap = super.render();
		bitmap.fill(0, 0, width, height, 0xff484848);
		bitmap.box(0, 0, width - 1, height - 1, isFocused() ? 0xffffff00 : 0xffffffff);
		TextRenderer.render(getRenderedText(), bitmap, 2, 2);
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
		isPressingControl = engine.input.isKeyDown(KeyEvent.VK_CONTROL);
	}

	public void keyPressed(char var1, int var2) {
		if (isFocused) {
			if (isPressingControl && var2 == KeyEvent.VK_V) {
				String clipboardText = "";
				try {
					clipboardText = Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor).toString();
				} catch (Exception e) {
					DikenEngine.errorLog("Paste Clipboard Failed: ", e);
				}
				if (clipboardText != null) {
					text += clipboardText;
					if (textChanced != null) {
						textChanced.accept(text);
					}
				}
			} else if (isPressingControl && var2 == KeyEvent.VK_C) {
				try {
					Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new java.awt.datatransfer.StringSelection(text), null);
				} catch (Exception e) {
					DikenEngine.errorLog("Copy Clipboard Failed: ", e);
				}
		    } else if (var2 == KeyEvent.VK_BACK_SPACE) {
				if (text.length() > 0) {
					text = text.substring(0, text.length() - 1);
				}
				
				if (textChanced != null) {
					textChanced.accept(text);
				}
			} else if (var2 == KeyEvent.VK_ENTER) {
				isFocused = false;
			} else if (var2 == KeyEvent.VK_ESCAPE) {
				isFocused = false;
			} else if (var2 == KeyEvent.VK_DELETE) {
				text = "";
				if (textChanced != null) {
					textChanced.accept(text);
				}
			} else if (isNumberField && (Character.isDigit(var1) || var1 == '.' || var1 == '-')) {
				text += var1;
				if (textChanced != null) {
					textChanced.accept(text);
				}
			} else if (!isNumberField) {
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
	
	private void setNumberic(boolean b) {
		this.isNumberField = b;
	}

	public boolean isNumberField() {
		return isNumberField;
	}

	public void setNumberField(boolean isNumberField) {
		this.isNumberField = isNumberField;
	}

	public Consumer<String> getTextChanced() {
		return textChanced;
	}

	public void setTextChanced(Consumer<String> textChanced) {
		this.textChanced = textChanced;
	}

	@Override
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("textField", "TextField", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(3, 2));
		
		var settingCategory = SettingCategory
				.createSettingCategory(key)
				.addSetting(new Setting<Boolean>("Focused", this.isFocused, Boolean.class, EnumSettingType.CHECK_BOX).addChangeListener(this::setFocused))
				.addSetting(new Setting<Boolean>("Numberic", this.isNumberField, Boolean.class, EnumSettingType.CHECK_BOX).addChangeListener(this::setNumberic))
				.addSetting(new Setting<String>("Text", this.text, String.class, EnumSettingType.TEXT_FIELD).addChangeListener(this::setText));
		
		var list = super.getNodeSettings();
		list.add(settingCategory);
		return list;
	}

	@Override
	public void saveNodeData(DataOutputStream out) throws IOException {
		super.saveNodeData(out);
		out.writeUTF(text);
		out.writeInt(counter);
		out.writeBoolean(isFocused);
		out.writeBoolean(isNumberField);
	}

	@Override
	public void loadNodeData(DataInputStream in) throws IOException {
		super.loadNodeData(in);
		this.text = in.readUTF();
		this.counter = in.readInt();
		this.isFocused = in.readBoolean();
		this.isNumberField = in.readBoolean();
		this.isPressingControl = false;
	}
}
