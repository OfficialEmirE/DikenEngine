package me.ramazanenescik04.diken.game.nodes.values;

import java.io.DataInputStream;
import java.io.IOException;

import me.ramazanenescik04.diken.game.EnumSettingType;

public class IntegerValue extends AbstractValue<Integer> {
	public IntegerValue() {
		super("IntegerValue", 0, Integer.class, EnumSettingType.TEXT_FIELD);
	}

	public IntegerValue(DataInputStream in) throws IOException {
		super(in);
	}

}
