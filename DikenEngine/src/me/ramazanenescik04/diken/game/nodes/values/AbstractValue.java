package me.ramazanenescik04.diken.game.nodes.values;

import java.util.List;

import me.ramazanenescik04.diken.game.EnumSettingType;
import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.game.Setting;
import me.ramazanenescik04.diken.game.SettingCategory;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

public abstract class AbstractValue<T> extends Node {
	private static final long serialVersionUID = -2728299564091357317L;
	
	private T value;
	private final Class<T> typeClass;
	private final EnumSettingType enumSettingType;
	
	public AbstractValue(T value, Class<T> typeClass, EnumSettingType enumSettingType) {
		this("Value", value, typeClass, enumSettingType);
	}
	
	public AbstractValue(String name, T value, Class<T> typeClass, EnumSettingType enumSettingType) {
		super(name);
		
		this.typeClass = typeClass;
		this.value = value;
		if (enumSettingType.isAllowedClasses(typeClass)) {
			this.enumSettingType = enumSettingType;
        } else {
        	this.enumSettingType = EnumSettingType.UNKNOWN;
        }
	}
	
	public T getValue() {
		return value;
	}
	
	public void setValue(T value) {
		this.value = value;
	}
	
	public Class<T> getTypeClass() {
		return typeClass;
	}
	
	@Override
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("value", "Value", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(0, 2));
		
		var settingCategory = SettingCategory
				.createSettingCategory(key)
				.addSetting(new Setting<T>("Value", value, typeClass, enumSettingType).addChangeListener(this::setValue));
		
		var list = super.getNodeSettings();
		list.add(settingCategory);
		return list;
	}
}
