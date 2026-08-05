package me.ramazanenescik04.diken.game.setting;

import java.util.Arrays;

import me.ramazanenescik04.diken.game.Node;

// --- Enum ---
public enum EnumSettingType {
    CHECK_BOX(Boolean.class),
    TEXT_FIELD(String.class, Integer.class,
    	 Short.class, Byte.class, Long.class,
    	 Float.class, Double.class),
    SLIDER(Float.class, Double.class, Integer.class), 
    COLOR_PICKER(Integer.class),
    KEY_BIND(Character.class, Integer.class),
	RESOURCE_SELECT(String.class),
	LIST_SELECT(Enum.class, String.class, Integer.class),
	OBJECT_SELECT(Node.class),
	/**
	 * @apiNote Sadece SettingDialog'da Help gibi yerlerde kullanılması için eklendi.
	 * ilerde silinilebilir!
	 */
	TEXT(Object.class),
	UNKNOWN(Object.class);
	
	public Class<?>[] allowedClasses;
	
	EnumSettingType(Class<?>...classes) {
		this.allowedClasses = classes;
	}

	public boolean isAllowedClasses(Class<?> type) {
		return Arrays.stream(allowedClasses).anyMatch(clazz -> clazz.isAssignableFrom(type));
	}

	static EnumSettingType foundSettingType(Class<?> typeClass) {
		var settingTypes = values();
		
		for (var settingType : settingTypes) {
			if (settingType.isAllowedClasses(typeClass))
				return settingType;
		}
		
		return UNKNOWN;
	}
}