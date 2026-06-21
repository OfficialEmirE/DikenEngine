package me.ramazanenescik04.diken.game.services;

import me.ramazanenescik04.diken.game.Node;

public class AbstractService extends Node {
	private static final long serialVersionUID = 3053175280241192963L;
	
	public AbstractService() {
		this("NoName-Service");
	}
	
	public AbstractService(String name) {
		super(name);
	}
	
	@Override
	public void setName(String name) {}

	@Override
	public void removeNode() {}
}
