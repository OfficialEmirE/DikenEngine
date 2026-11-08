package me.ramazanenescik04.diken.game.nodes.values;

import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.game.setting.EnumSettingType;

public class ObjectValue extends AbstractValue<Node> {
	public ObjectValue() {
		super("ObjectValue", null, Node.class, EnumSettingType.OBJECT_SELECT);
	}
}
