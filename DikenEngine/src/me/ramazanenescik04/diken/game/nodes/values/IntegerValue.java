package me.ramazanenescik04.diken.game.nodes.values;

import me.ramazanenescik04.diken.game.setting.EnumSettingType;

public class IntegerValue extends AbstractValue<Integer> {
	public IntegerValue() {
		super("IntegerValue", 0, Integer.class, EnumSettingType.TEXT_FIELD);
	}
}
