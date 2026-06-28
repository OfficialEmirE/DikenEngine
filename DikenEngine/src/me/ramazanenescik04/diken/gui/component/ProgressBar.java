package me.ramazanenescik04.diken.gui.component;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.EnumSettingType;
import me.ramazanenescik04.diken.game.Setting;
import me.ramazanenescik04.diken.game.SettingCategory;
import me.ramazanenescik04.diken.gui.UDim2;
import me.ramazanenescik04.diken.renderer.FrameBitmapPool;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

/**
 * Represents the `ProgressBar` type within the DikenEngine `gui.compoment` package.
 */
public class ProgressBar extends GuiComponent {
	private int value = 100, maxValue = 100;
	private int color = 0xff00ff00, color2 = 0xff00ff00, bgColor = 0xff000000;
	private String text = "";
	
	public ProgressBar(UDim2 position, UDim2 size) {
		super("ProgressBar", position, size);
	}

	public ProgressBar(DataInputStream in) throws IOException {
		super(in);
		loadNodeData(in);
	}

	@Override
	public Bitmap render() {
		var width = this.getAbsoluteBounds().getWidth();
		var height = this.getAbsoluteBounds().getHeight();
		
		Bitmap bitmap = super.render();
		bitmap.clear(bgColor);
		bitmap.box(0, 0, bitmap.w - 1, bitmap.h - 1, 0xffffffff);
		
		double progressRatio = (double) value / maxValue;
        int progressWidth = (int) ((width - 2) * progressRatio);
        if (progressWidth <= 1) {
        	progressWidth = 1;
        }
        
        Bitmap progressBar = FrameBitmapPool.newBitmap(progressWidth, height - 2);
        if (!(progressWidth == 1)) {
        	progressBar.drawGradient(color, color2);
        }
        
        bitmap.draw(progressBar, 1, 1);
        
        bitmap.drawText(text.isEmpty() ? value + "%" : text, 4, height / 2 - Text.stringBitmapAverageHeight(text, DikenEngine.getEngine().defaultFont) / 2, false);
		
		return bitmap;
	}
	
	
	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	public int getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(int maxValue) {
		this.maxValue = maxValue;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public int getColor2() {
		return color2;
	}

	public void setColor2(int color2) {
		this.color2 = color2;
	}

	public int getBackgroundColor() {
		return bgColor;
	}

	public void setBackgroundColor(int bgColor) {
		this.bgColor = bgColor;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("progressBar", "ProgressBar", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(11, 2));
		
		var settingCategory = SettingCategory
				.createSettingCategory(key)
				.addSetting(new Setting<Integer>("Value", this.value, Integer.class, EnumSettingType.TEXT_FIELD).addChangeListener(this::setValue))
				.addSetting(new Setting<Integer>("Max Value", this.maxValue, Integer.class, EnumSettingType.TEXT_FIELD).addChangeListener(this::setMaxValue))
				.addSetting(new Setting<Integer>("First Color", this.color, Integer.class, EnumSettingType.COLOR_PICKER).addChangeListener(this::setColor))
				.addSetting(new Setting<Integer>("Second Color", this.color2, Integer.class, EnumSettingType.COLOR_PICKER).addChangeListener(this::setColor2))
				.addSetting(new Setting<Integer>("Background Color", this.bgColor, Integer.class, EnumSettingType.COLOR_PICKER).addChangeListener(this::setBackgroundColor))
				.addSetting(new Setting<String>("Text", this.text, String.class, EnumSettingType.TEXT_FIELD).addChangeListener(this::setText));
		
		var list = super.getNodeSettings();
		list.add(settingCategory);
		return list;
	}

	@Override
	public void saveNodeData(DataOutputStream out) throws IOException {
		super.saveNodeData(out);
		out.writeInt(value);
		out.writeInt(maxValue);
		out.writeInt(color);
		out.writeInt(color2);
		out.writeInt(bgColor);
		out.writeUTF(text);
	}

	@Override
	public void loadNodeData(DataInputStream in) throws IOException {
		super.loadNodeData(in);
		this.value = in.readInt();
		this.maxValue = in.readInt();
		this.color = in.readInt();
		this.color2 = in.readInt();
		this.bgColor = in.readInt();
		this.text = in.readUTF();
	}

}
