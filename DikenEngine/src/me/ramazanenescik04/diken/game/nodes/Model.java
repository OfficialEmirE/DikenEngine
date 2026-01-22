package me.ramazanenescik04.diken.game.nodes;

import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.resource.Bitmap;

public class Model extends Node {
	private static final long serialVersionUID = 1L;

	public Model() {
		this("Model", 0, 0);
	}

	public Model(String name) {
		this(name, 0, 0);
	}

	public Model(String name, int x, int y) {
		super(name, x, y);
	}

	@Override
	public Bitmap render() {
		return null;
	}

}
