package me.ramazanenescik04.diken.gui.component;

import java.util.List;

import me.ramazanenescik04.diken.game.SettingCategory;
import me.ramazanenescik04.diken.gui.UDim2;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

/**
 * Represents the `PasswordField` type within the DikenEngine `gui.compoment` package.
 */
public class PasswordField extends TextField {
	private static final long serialVersionUID = 1L;

	public PasswordField(UDim2 position, UDim2 size) {
		this("", position, size);
	}

	public PasswordField(String text, UDim2 position, UDim2 size) {
		super(text, position, size);
		
		this.setName("PasswordField");
	}

	@Override
	protected String getRenderedText() {
		StringBuilder masked = new StringBuilder();
		for (int i = 0; i < getText().length(); i++) {
			masked.append('*');
		}
		return masked.toString();
	}
	
	@Override
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("passwordField", "PasswordField", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(10, 2));
		
		var settingCategory = SettingCategory
				.createSettingCategory(key);
		
		var list = super.getNodeSettings();
		list.add(settingCategory);
		return list;
	}
}
