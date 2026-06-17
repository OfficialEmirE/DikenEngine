package me.ramazanenescik04.diken.game.nodes.values;

import me.ramazanenescik04.diken.game.EnumSettingType;

public class IntegerValue extends AbstractValue<Integer> {
	private static final long serialVersionUID = 8184140639827444368L;

	public IntegerValue() {
		super("IntegerValue", 0, Integer.class, EnumSettingType.TEXT_FIELD);
	}

}
