package me.ramazanenescik04.diken.game.services;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;

import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.game.setting.SettingCategory;

public abstract class AbstractService extends Node {
	public AbstractService() {
		this("NoName-Service");
	}
	
	public AbstractService(String name) {
		super(name);
	}

	public AbstractService(DataInputStream in) throws IOException {
		super(in);
		if (getClass() == AbstractService.class) {
			loadNodeData(in);
		}
	}
	
	@Override
	public void setName(String name) {}

	@Override
	public void removeNode() {}

	@Override
	public void setParent(Node newParent) {}
	
	public abstract boolean showStudio();
	
	public List<SettingCategory> getNodeSettings() {
		var list = super.getNodeSettings();
		list.getFirst().getSettings().stream()
			.filter(n -> n.getName().equals("Name") || n.getName().equals("Parent"))
			.forEach(t -> t.setChangeable(false));
		
		return list;
	}
}
