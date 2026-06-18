package me.ramazanenescik04.diken.game.nodes.values;

import me.ramazanenescik04.diken.game.EnumSettingType;

public class BooleanValue extends AbstractValue<Boolean> {
	private static final long serialVersionUID = 8184140639827444368L;

	public BooleanValue() {
		super("BooleanValue", false, Boolean.class, EnumSettingType.CHECK_BOX);
	}

}
