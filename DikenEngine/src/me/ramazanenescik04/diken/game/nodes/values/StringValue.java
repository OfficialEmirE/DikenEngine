package me.ramazanenescik04.diken.game.nodes.values;

import me.ramazanenescik04.diken.game.setting.EnumSettingType;

public class StringValue extends AbstractValue<String> {
	public StringValue() {
		super("StringValue", "", String.class, EnumSettingType.TEXT_FIELD);
	}
}
