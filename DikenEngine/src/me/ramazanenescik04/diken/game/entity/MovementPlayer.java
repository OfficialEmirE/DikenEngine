package me.ramazanenescik04.diken.game.entity;

import java.util.*;

import org.lwjgl.input.Keyboard;

public class MovementPlayer {
	private Player player;
	
	private Map<String, Integer> keyMap = new HashMap<>();
	
	public int viewType = 0; //0 = left, 1 = right
	public boolean isMoving = false;
	
	public MovementPlayer(Player player) {
		keyMap.put("moveUpKey", Keyboard.KEY_W);
		keyMap.put("moveLeftKey", Keyboard.KEY_A);
		keyMap.put("moveDownKey", Keyboard.KEY_S);
		keyMap.put("moveRightKey", Keyboard.KEY_D);
		
		keyMap.put("moveUpArrowKey", Keyboard.KEY_UP);
		keyMap.put("moveLeftArrowKey", Keyboard.KEY_LEFT);
		keyMap.put("moveDownArrowKey", Keyboard.KEY_DOWN);
		keyMap.put("moveRightArrowKey", Keyboard.KEY_RIGHT);
		
		keyMap.put("invertoryKey", Keyboard.KEY_B);
		
		keyMap.put("invertoryShortcut1", Keyboard.KEY_1);
		keyMap.put("invertoryShortcut2", Keyboard.KEY_2);
		keyMap.put("invertoryShortcut3", Keyboard.KEY_3);
		keyMap.put("invertoryShortcut4", Keyboard.KEY_4);
		keyMap.put("invertoryShortcut5", Keyboard.KEY_5);
		keyMap.put("invertoryShortcut6", Keyboard.KEY_6);
		keyMap.put("invertoryShortcut7", Keyboard.KEY_7);
		keyMap.put("invertoryShortcut8", Keyboard.KEY_8);
		keyMap.put("invertoryShortcut9", Keyboard.KEY_9);
		keyMap.put("invertoryShortcut0", Keyboard.KEY_0);
		
		this.player = player;
	}
	
	public void tick() {
		this.isMoving = false;
		if (player.canMove) {
			if (Keyboard.isKeyDown(this.keyMap.get("moveUpKey")) || Keyboard.isKeyDown(this.keyMap.get("moveUpArrowKey"))) {
				player.y -= player.speed;
				this.isMoving = true;
			}
			if (Keyboard.isKeyDown(this.keyMap.get("moveDownKey")) || Keyboard.isKeyDown(this.keyMap.get("moveDownArrowKey"))) {
				player.y += player.speed;
				this.isMoving = true;
			}
			if (Keyboard.isKeyDown(this.keyMap.get("moveLeftKey")) || Keyboard.isKeyDown(this.keyMap.get("moveLeftArrowKey"))) {
				player.x -= player.speed;
				viewType = 0;
				this.isMoving = true;
			}
			if (Keyboard.isKeyDown(this.keyMap.get("moveRightKey")) || Keyboard.isKeyDown(this.keyMap.get("moveRightArrowKey"))) {
				player.x += player.speed;
				viewType = 1;
				this.isMoving = true;
			}
		}
	}
	
	public Map<String, Integer> getKeyMap() {
		return new HashMap<>(this.keyMap);
	}
	
	public void setKeyBindings(Map<String, Integer> newKeyMap) {
		this.keyMap = new HashMap<>(newKeyMap);
	}

}
