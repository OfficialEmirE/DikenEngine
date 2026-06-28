package me.ramazanenescik04.diken.game.nodes.values;

import java.io.DataInputStream;
import java.io.IOException;

import me.ramazanenescik04.diken.game.EnumSettingType;

public class FloatValue extends AbstractValue<Float> {
	public FloatValue() {
		super("FloatValue", 0.0f, Float.class, EnumSettingType.TEXT_FIELD);
	}

	public FloatValue(DataInputStream in) throws IOException {
		super(in);
	}

}
