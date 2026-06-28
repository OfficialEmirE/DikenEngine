package me.ramazanenescik04.diken.game.nodes.values;

import java.io.DataInputStream;
import java.io.IOException;

import me.ramazanenescik04.diken.game.EnumSettingType;

public class StringValue extends AbstractValue<String> {
	public StringValue() {
		super("StringValue", "", String.class, EnumSettingType.TEXT_FIELD);
	}

	public StringValue(DataInputStream in) throws IOException {
		super(in);
	}

}
