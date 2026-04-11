package me.ramazanenescik04.diken.game.entity;

import java.util.UUID;

public class ServerPlayer extends Player {
	private static final long serialVersionUID = 1368068969206735451L;
	
	private final UUID connectionId;
	private String username;
	
	public ServerPlayer(String username) {
		this(username, UUID.randomUUID(), 0, 0);
	}
	
	public ServerPlayer(String username, UUID connectionId, int x, int y) {
		super(x, y);
		this.username = username != null ? username : "Player";
		this.connectionId = connectionId != null ? connectionId : UUID.randomUUID();
		this.setName("ServerPlayer-" + this.username);
		this.canMove = false;
	}
	
	public UUID getConnectionId() {
		return connectionId;
	}
	
	public String getUsername() {
		return username;
	}
	
	public void setUsername(String username) {
		this.username = username != null ? username : this.username;
	}
}
