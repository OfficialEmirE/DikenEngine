package me.ramazanenescik04.diken.game.nodes.values;

import me.ramazanenescik04.diken.game.EnumSettingType;
import me.ramazanenescik04.diken.game.Node;

public class ObjectValue extends AbstractValue<Node> {
	private static final long serialVersionUID = 8184140639827444368L;

	public ObjectValue() {
		super("ObjectValue", null, Node.class, EnumSettingType.OBJECT_SELECT);
	}

}
