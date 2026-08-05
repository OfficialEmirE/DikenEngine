package me.ramazanenescik04.diken.game.nodes.values;

import java.io.DataInputStream;
import java.io.IOException;

import me.ramazanenescik04.diken.game.setting.EnumSettingType;

public class BooleanValue extends AbstractValue<Boolean> {
	public BooleanValue() {
		super("BooleanValue", false, Boolean.class, EnumSettingType.CHECK_BOX);
	}

	public BooleanValue(DataInputStream in) throws IOException {
		super(in);
	}

}
