package me.ramazanenescik04.diken.game.entity;

import java.awt.event.KeyEvent;
import java.util.*;

import me.ramazanenescik04.diken.DikenEngine;

public class MovementPlayer {
	private Humanoid player;
	
	private Map<String, Integer> keyMap = new HashMap<>();
	
	public int viewType = 0; //0 = left, 1 = right
	public boolean isMoving = false;
	
	public MovementPlayer(Humanoid player) {
		keyMap.put("moveUpKey", KeyEvent.VK_W);
		keyMap.put("moveLeftKey", KeyEvent.VK_A);
		keyMap.put("moveDownKey", KeyEvent.VK_S);
		keyMap.put("moveRightKey", KeyEvent.VK_D);
		
		keyMap.put("moveUpArrowKey", KeyEvent.VK_UP);
		keyMap.put("moveLeftArrowKey", KeyEvent.VK_LEFT);
		keyMap.put("moveDownArrowKey", KeyEvent.VK_DOWN);
		keyMap.put("moveRightArrowKey", KeyEvent.VK_RIGHT);
		
		keyMap.put("invertoryKey", KeyEvent.VK_B);
		
		keyMap.put("invertoryShortcut1", KeyEvent.VK_1);
		keyMap.put("invertoryShortcut2", KeyEvent.VK_2);
		keyMap.put("invertoryShortcut3", KeyEvent.VK_3);
		keyMap.put("invertoryShortcut4", KeyEvent.VK_4);
		keyMap.put("invertoryShortcut5", KeyEvent.VK_5);
		keyMap.put("invertoryShortcut6", KeyEvent.VK_6);
		keyMap.put("invertoryShortcut7", KeyEvent.VK_7);
		keyMap.put("invertoryShortcut8", KeyEvent.VK_8);
		keyMap.put("invertoryShortcut9", KeyEvent.VK_9);
		keyMap.put("invertoryShortcut0", KeyEvent.VK_0);
		
		this.player = player;
	}
	
	public void tick(DikenEngine engine) {
		this.isMoving = false;
		if (player.canMove) {
			if (engine.input.isKeyDown(this.keyMap.get("moveUpKey")) || engine.input.isKeyDown(this.keyMap.get("moveUpArrowKey"))) {
				player.y -= player.speed;
				this.isMoving = true;
			}
			if (engine.input.isKeyDown(this.keyMap.get("moveDownKey")) || engine.input.isKeyDown(this.keyMap.get("moveDownArrowKey"))) {
				player.y += player.speed;
				this.isMoving = true;
			}
			if (engine.input.isKeyDown(this.keyMap.get("moveLeftKey")) || engine.input.isKeyDown(this.keyMap.get("moveLeftArrowKey"))) {
				player.x -= player.speed;
				viewType = 0;
				this.isMoving = true;
			}
			if (engine.input.isKeyDown(this.keyMap.get("moveRightKey")) || engine.input.isKeyDown(this.keyMap.get("moveRightArrowKey"))) {
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
