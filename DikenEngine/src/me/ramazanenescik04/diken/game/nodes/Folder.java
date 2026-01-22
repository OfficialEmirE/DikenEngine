package me.ramazanenescik04.diken.game.nodes;

import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.resource.Bitmap;

public class Folder extends Node {
	private static final long serialVersionUID = -1974610025825096210L;

	// Folder klasör gibidir. x ve y si daima 0 dır!
	
	public Folder() {
		this.x = 0;
		this.y = 0;
	}

	public Folder(String name) {
		super(name);
		this.x = 0;
		this.y = 0;
	}

	@Override
	public Bitmap render() {
		return null;
	}

}
