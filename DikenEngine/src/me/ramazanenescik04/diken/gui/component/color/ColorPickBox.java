package me.ramazanenescik04.diken.gui.component.color;

import java.util.List;
import java.util.function.Consumer;

import me.ramazanenescik04.diken.game.EnumSettingType;
import me.ramazanenescik04.diken.game.Setting;
import me.ramazanenescik04.diken.game.SettingCategory;
import me.ramazanenescik04.diken.gui.UDim2;
import me.ramazanenescik04.diken.gui.component.GuiComponent;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;
import me.ramazanenescik04.diken.tools.PixelToColor;

/**
 * Represents the `ColorPickBox` type within the DikenEngine `gui.compoment` package.
 */
public class ColorPickBox extends GuiComponent {
	private static final long serialVersionUID = 1L;

	private int selectedColor;
	private float hueColor = PixelToColor.rgbToHsv(0xffff0000)[0];
	private Consumer<Integer> consumer;

	private Bitmap cachedMap;
	private int selectedX = -1;
	private int selectedY = -1;

	public ColorPickBox(UDim2 position, UDim2 size) {
		super(position, size);
	}

	public ColorPickBox setConsumer(Consumer<Integer> consumer) {
		this.consumer = consumer;
		return this;
	}

	public ColorPickBox setSelectedColor(int color) {
		this.selectedColor = color;
		return this;
	}

	public ColorPickBox setHueColor(float color) {
		var bounds = this.getAbsoluteBounds();
		this.hueColor = color;
		this.cachedMap = PixelToColor.createHSVRect(bounds.getWidth() - 2, bounds.getHeight() - 2, hueColor);
		var point = this.cachedMap.findColorPos(PixelToColor.hsvToRgb(color, 0, 0));
		if (point != null) {
			this.selectedX = point.x + 1;
			this.selectedY = point.y + 1;
		}
		return this;
	}

	public ColorPickBox setHueColor(int color) {
		var bounds = this.getAbsoluteBounds();
		this.hueColor = PixelToColor.rgbToHsv(color)[0];
		this.cachedMap = PixelToColor.createHSVRect(bounds.getWidth() - 2, bounds.getHeight() - 2, hueColor);
		
		var point = this.cachedMap.findColorPos(color);
		if (point != null) {
			this.selectedX = point.x + 1;
			this.selectedY = point.y + 1;
		}
		return this;
	}

	public int getSelectedColor() {
		return selectedColor;
	}

	public float getHueColor() {
		return hueColor;
	}
	
	public int getSelectedPosColor() {
		if (selectedX == -1 || selectedY == -1)
			return 0xffff0000;
		
		return cachedMap.getPixel(selectedX - 1, selectedY - 1);
	}

	@Override
	public Bitmap render() {
		Bitmap bitmap = super.render();

		bitmap.draw(cachedMap, 1, 1);

		// Çerçeve
		bitmap.box(0, 0, bitmap.w - 1, bitmap.h - 1, 0xff000000);

		// Seçili noktayı göster
		if (selectedX >= 0 && selectedY >= 0) {
			bitmap.box(selectedX - 1, selectedY - 1, selectedX + 1, selectedY + 1, 0xffcfcfcf);
		}

		return bitmap;
	}

	@Override
	public void mouseClicked(int x, int y, int button, boolean isTouch) {
		if (isTouch && button == 0) {
			var bounds = this.getAbsoluteBounds();
			
			// sınır kontrolü
			if (x <= 0 || y <= 0 || x >= bounds.getWidth() - 1 || y >= bounds.getHeight() - 1 || cachedMap == null) return;

			// cached map üzerinden al
			selectedColor = cachedMap.getPixel(x - 1, y - 1);

			selectedX = x;
			selectedY = y;

			if (consumer != null) {
				consumer.accept(selectedColor);
			}
		}
	}
	
	@Override
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("colorPickBox", "ColorPickBox", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(15, 2));
		var settingCategory = 
				SettingCategory.createSettingCategory(key)
				.addSetting(new Setting<Integer>("Selected X", this.selectedX, Integer.class, EnumSettingType.TEXT_FIELD).addChangeListener(e -> this.selectedX = e))
				.addSetting(new Setting<Integer>("Selected Y", this.selectedY, Integer.class, EnumSettingType.TEXT_FIELD).addChangeListener(e -> this.selectedY = e))
				.addSetting(new Setting<Integer>("Selected Color", this.selectedColor, Integer.class, EnumSettingType.COLOR_PICKER).addChangeListener(this::setSelectedColor))
				.addSetting(new Setting<Integer>("Hue Color", PixelToColor.hsvToRgb(hueColor, 0, 0), Integer.class, EnumSettingType.COLOR_PICKER).addChangeListener(this::setHueColor));
		
		var list = super.getNodeSettings();
		list.add(settingCategory);
		return list;
	}
}