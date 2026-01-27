package me.ramazanenescik04.diken.game.entity;

import java.util.*;

import org.lwjgl.glfw.GLFW;

import me.ramazanenescik04.diken.DikenEngine;

public class MovementPlayer {
	private Humanoid player;
	
	private Map<String, Integer> keyMap = new HashMap<>();
	
	public int viewType = 0; //0 = left, 1 = right
	public boolean isMoving = false;
	
	public MovementPlayer(Humanoid player) {
		keyMap.put("moveUpKey", GLFW.GLFW_KEY_W);
		keyMap.put("moveLeftKey", GLFW.GLFW_KEY_A);
		keyMap.put("moveDownKey", GLFW.GLFW_KEY_S);
		keyMap.put("moveRightKey", GLFW.GLFW_KEY_D);
		
		keyMap.put("moveUpArrowKey", GLFW.GLFW_KEY_UP);
		keyMap.put("moveLeftArrowKey", GLFW.GLFW_KEY_LEFT);
		keyMap.put("moveDownArrowKey", GLFW.GLFW_KEY_DOWN);
		keyMap.put("moveRightArrowKey", GLFW.GLFW_KEY_RIGHT);
		
		keyMap.put("invertoryKey", GLFW.GLFW_KEY_B);
		
		keyMap.put("invertoryShortcut1", GLFW.GLFW_KEY_1);
		keyMap.put("invertoryShortcut2", GLFW.GLFW_KEY_2);
		keyMap.put("invertoryShortcut3", GLFW.GLFW_KEY_3);
		keyMap.put("invertoryShortcut4", GLFW.GLFW_KEY_4);
		keyMap.put("invertoryShortcut5", GLFW.GLFW_KEY_5);
		keyMap.put("invertoryShortcut6", GLFW.GLFW_KEY_6);
		keyMap.put("invertoryShortcut7", GLFW.GLFW_KEY_7);
		keyMap.put("invertoryShortcut8", GLFW.GLFW_KEY_8);
		keyMap.put("invertoryShortcut9", GLFW.GLFW_KEY_9);
		keyMap.put("invertoryShortcut0", GLFW.GLFW_KEY_0);
		
		this.player = player;
	}
	
	public void tick() {
		DikenEngine engine = DikenEngine.getEngine();
		this.isMoving = false;
		if (player.canMove) {
			if (engine.isKeyPressed(this.keyMap.get("moveUpKey")) || engine.isKeyPressed(this.keyMap.get("moveUpArrowKey"))) {
				player.y -= player.speed;
				this.isMoving = true;
			}
			if (engine.isKeyPressed(this.keyMap.get("moveDownKey")) || engine.isKeyPressed(this.keyMap.get("moveDownArrowKey"))) {
				player.y += player.speed;
				this.isMoving = true;
			}
			if (engine.isKeyPressed(this.keyMap.get("moveLeftKey")) || engine.isKeyPressed(this.keyMap.get("moveLeftArrowKey"))) {
				player.x -= player.speed;
				viewType = 0;
				this.isMoving = true;
			}
			if (engine.isKeyPressed(this.keyMap.get("moveRightKey")) || engine.isKeyPressed(this.keyMap.get("moveRightArrowKey"))) {
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
