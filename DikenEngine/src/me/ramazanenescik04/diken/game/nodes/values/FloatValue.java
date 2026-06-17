package me.ramazanenescik04.diken.game.nodes.values;

import me.ramazanenescik04.diken.game.EnumSettingType;

public class FloatValue extends AbstractValue<Float> {
	private static final long serialVersionUID = 8184140639827444368L;

	public FloatValue() {
		super("FloatValue", 0.0f, Float.class, EnumSettingType.TEXT_FIELD);
	}

}
