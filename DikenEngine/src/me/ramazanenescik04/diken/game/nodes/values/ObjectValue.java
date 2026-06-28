package me.ramazanenescik04.diken.game.nodes.values;

import java.io.DataInputStream;
import java.io.IOException;

import me.ramazanenescik04.diken.game.EnumSettingType;
import me.ramazanenescik04.diken.game.Node;

public class ObjectValue extends AbstractValue<Node> {
	public ObjectValue() {
		super("ObjectValue", null, Node.class, EnumSettingType.OBJECT_SELECT);
	}

	public ObjectValue(DataInputStream in) throws IOException {
		super(in);
	}

}
