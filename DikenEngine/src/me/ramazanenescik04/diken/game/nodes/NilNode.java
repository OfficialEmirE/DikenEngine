package me.ramazanenescik04.diken.game.nodes;

import java.util.List;

import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.game.setting.SettingCategory;

public class NilNode extends Node {
	
	public NilNode() {
		this("Node Could Not Be Loaded!");
	}
	
	public NilNode(String text) {
		super(text);
		this.setArchivable(false);
	}
	
	@Override
	public void setName(String name) {}
	
	@Override
	public void setArchivable(boolean archivable) {}

	public List<SettingCategory> getNodeSettings() {
		var list = super.getNodeSettings();
		list.add(SettingCategory.createSettingCategory("nilNode", "NilNode", 12, 3));
		return list;
	}
}
