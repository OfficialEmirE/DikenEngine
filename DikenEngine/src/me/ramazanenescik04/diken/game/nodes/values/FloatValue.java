package me.ramazanenescik04.diken.game.nodes.values;

import me.ramazanenescik04.diken.game.setting.EnumSettingType;

public class FloatValue extends AbstractValue<Float> {
	public FloatValue() {
		super("FloatValue", 0.0f, Float.class, EnumSettingType.TEXT_FIELD);
	}
}
