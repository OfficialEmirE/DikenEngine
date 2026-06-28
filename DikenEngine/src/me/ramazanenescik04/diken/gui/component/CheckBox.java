package me.ramazanenescik04.diken.gui.component;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.EnumSettingType;
import me.ramazanenescik04.diken.game.Setting;
import me.ramazanenescik04.diken.game.SettingCategory;
import me.ramazanenescik04.diken.gui.UDim2;
import me.ramazanenescik04.diken.gui.UniFont;
import me.ramazanenescik04.diken.renderer.FrameBitmapPool;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

/**
 * Represents the `CheckBox` type within the DikenEngine `gui.compoment` package.
 */
public class CheckBox extends GuiComponent {	
	private Consumer<CheckBox> checkBoxClicked;
	private boolean touching;
	private boolean checked = false;
	
	public Text text;

	public CheckBox(String text, UDim2 position, UDim2 size) {
		super("CheckBox", position, size);
		
		// TODO test edilmeli!
		this.text = new Text(text, UDim2.zero, UDim2.zero, 0xFFFFFFFF, UniFont.getFont("default_font"));
	}

	public CheckBox(DataInputStream in) throws IOException {
		super(in);
		loadNodeData(in);
	}
	
	public CheckBox setConsumer(Consumer<CheckBox> r) {
		this.checkBoxClicked = r;
		return this;
	}

	public boolean isChecked() {
		return checked;
	}

	public CheckBox setChecked(boolean checked) {
		this.checked = checked;
		return this;
	}

	public String getText() {
		return text.text;
	}
	
	public Text getTextNode() {
		return text;
	}

	public CheckBox setText(String text) {
		this.text.text = text;
		return this;
	}

	@Override
	public Bitmap render() {
		Bitmap bitmap = FrameBitmapPool.newBitmap(20 + text.getAbsoluteBounds().getWidth() + 2, 20);
		
		ArrayBitmap array = (ArrayBitmap) ResourceLocator.getResource("checkbox-array");
		bitmap.draw(checked ? array.getBitmap(0, 0) : array.getBitmap(1, 0), 2, 2);
		if (touching) {
			bitmap.box(2, 2, 17, 17, 0xffffffff);
		}
		
		bitmap.draw(text.render(), 20, 6);
		
		return bitmap;
	}

	@Override
	public void tick(DikenEngine engine) {
		text.tick(engine);
	}

	@Override
	public void mouseClicked(int x, int y, int button, boolean isTouch) {
		if (this.active && (isTouch || touching)) {
			checked = !checked;
			if (checkBoxClicked != null) {
				checkBoxClicked.accept(this);
			}
		}
	}

	@Override
	public void mouseGetInfo(int x, int y, boolean isTouch) {
		this.touching = isTouch;
	}
	
	@Override
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("checkBox", "CheckBox", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(2, 2));
		
		var settingCategory = SettingCategory
				.createSettingCategory(key)
				.addSetting(new Setting<String>("Text", this.getText(), String.class, EnumSettingType.TEXT_FIELD).addChangeListener(this::setText))
				.addSetting(new Setting<Boolean>("Checked", this.checked, Boolean.class, EnumSettingType.CHECK_BOX).addChangeListener(this::setChecked));
		
		var list = super.getNodeSettings();
		list.add(settingCategory);
		return list;
	}

	@Override
	public void saveNodeData(DataOutputStream out) throws IOException {
		super.saveNodeData(out);
		out.writeBoolean(touching);
		out.writeBoolean(checked);
		out.writeUTF(text != null ? text.text : "");
	}

	@Override
	public void loadNodeData(DataInputStream in) throws IOException {
		super.loadNodeData(in);
		this.touching = in.readBoolean();
		this.checked = in.readBoolean();
		this.text = new Text(in.readUTF(), UDim2.zero, UDim2.zero, 0xFFFFFFFF, UniFont.getFont("default_font"));
	}
}
