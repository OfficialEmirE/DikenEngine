package me.ramazanenescik04.diken.game.nodes;

public class SpawnLocation extends Part {
	private static final long serialVersionUID = -6112488161375121027L;
	
	public SpawnLocation() {
		super();
		this.name = "SpawnLocation";
		this.color = 0xfff0f0f0;
		this.setSolid(false);
		this.setAnchored(true);
	}

	public SpawnLocation(int x, int y, int width, int height) {
		super(x, y, width, height);
		this.name = "SpawnLocation";
		this.color = 0xfff0f0f0;
		this.setSolid(false);
		this.setAnchored(true);
	}
}
