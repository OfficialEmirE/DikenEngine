package me.ramazanenescik04.diken.game.entity;

import org.lwjgl.input.Keyboard;

public class MovementPlayer {
	private Player player;
	
	public int moveUpKey = Keyboard.KEY_W;
	public int moveDownKey = Keyboard.KEY_S;
	public int moveLeftKey = Keyboard.KEY_A;
	public int moveRightKey = Keyboard.KEY_D;
	
	public int viewType = 0; //0 = left, 1 = right
	public boolean isMoving = false;
	
	public MovementPlayer(Player player) {
		this.player = player;
	}
	
	public void tick() {
		this.isMoving = false;
		if (player.canMove) {
			if (Keyboard.isKeyDown(moveUpKey)) {
				player.y -= player.speed;
				this.isMoving = true;
			}
			if (Keyboard.isKeyDown(moveDownKey)) {
				player.y += player.speed;
				this.isMoving = true;
			}
			if (Keyboard.isKeyDown(moveLeftKey)) {
				player.x -= player.speed;
				viewType = 0;
				this.isMoving = true;
			}
			if (Keyboard.isKeyDown(moveRightKey)) {
				player.x += player.speed;
				viewType = 1;
				this.isMoving = true;
			}
		}
	}
	
	public void setKeyBindings(int up, int down, int left, int right) {
		this.moveUpKey = up;
		this.moveDownKey = down;
		this.moveLeftKey = left;
		this.moveRightKey = right;
	}

}
