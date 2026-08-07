package me.ramazanenescik04.diken.gui.component;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.services.InputService;
import me.ramazanenescik04.diken.game.setting.EnumSettingType;
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
	
	private Consumer<String> textChanged;
	private Runnable pressedEnter;

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
		this.textChanged = consumer;
		return this;
	}
	
	public Consumer<String> getTextChanged() {
		return textChanged;
	}
	
	public TextField setPressedEnter(Runnable consumer) {
		this.pressedEnter = consumer;
		return this;
	}
	
	public Runnable getPressedEnter() {
		return pressedEnter;
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
		if (textChanged != null) {
			textChanged.accept(text);
		}
	}
	
	public void setFocused(boolean focused) {
		this.isFocused = focused;
		InputService.setTextTypeMode(focused);
	}

	public boolean isFocused() {
		return isFocused;
	}

	public void tick(DikenEngine engine) {
		counter++;
		isPressingControl = engine.input.isKeyDown(KeyEvent.VK_CONTROL);
	}

	public void keyPressed(char character, int keyCode) {
		if (!isFocused) {
			return;
		}

		if (isPressingControl) {
			if (handleClipboardShortcut(keyCode)) {
				return;
			}
		}

		switch (keyCode) {
			case KeyEvent.VK_BACK_SPACE -> removeLastCharacter();
			case KeyEvent.VK_ENTER -> confirmInput();
			case KeyEvent.VK_ESCAPE -> cancelInput();
			case KeyEvent.VK_DELETE -> clearText();
			default -> addCharacter(character);
		}
	}

	private boolean handleClipboardShortcut(int keyCode) {
		return switch (keyCode) {
			case KeyEvent.VK_V -> {
				pasteClipboard();
				yield true;
			}
			case KeyEvent.VK_C -> {
				copyClipboard();
				yield true;
			}
			default -> false;
		};
	}

	private void pasteClipboard() {
		try {
			var clipboard = Toolkit.getDefaultToolkit()
					.getSystemClipboard();

			var data = clipboard.getData(DataFlavor.stringFlavor);

			if (data != null) {
				text += data.toString();
				notifyTextChanged();
			}

		} catch (Exception e) {
			DikenEngine.errorLog("Paste Clipboard Failed: ", e);
		}
	}

	private void copyClipboard() {
		try {
			var clipboard = Toolkit.getDefaultToolkit()
					.getSystemClipboard();

			clipboard.setContents(
					new StringSelection(text),
					null
			);

		} catch (Exception e) {
			DikenEngine.errorLog("Copy Clipboard Failed: ", e);
		}
	}

	private void removeLastCharacter() {
		if (!text.isEmpty()) {
			text = text.substring(0, text.length() - 1);
			notifyTextChanged();
		}
	}

	private void clearText() {
		text = "";
		notifyTextChanged();
	}

	private void confirmInput() {
		isFocused = false;
		InputService.setTextTypeMode(false);

		if (pressedEnter != null) {
			pressedEnter.run();
		}
	}

	private void cancelInput() {
		isFocused = false;
		InputService.setTextTypeMode(false);
	}

	private void addCharacter(char character) {
		if (isNumberField) {
			if (!Character.isDigit(character) 
					&& character != '.' 
					&& character != '-') {
				return;
			}
		} else {
			if (!DikenEngine.getEngine()
					.defaultFont
					.hasChar(character)) {
				return;
			}
		}

		text += character;
		notifyTextChanged();
	}

	private void notifyTextChanged() {
		if (textChanged != null) {
			textChanged.accept(text);
		}
	}

	public void mouseClicked(int x, int y, int button, boolean isTouch) {
		if (button == 0) {
			if (isTouch) {
				this.isFocused = !this.isFocused;
				InputService.setTextTypeMode(this.isFocused);
			} else {
				this.isFocused = false;
				InputService.setTextTypeMode(false);
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
