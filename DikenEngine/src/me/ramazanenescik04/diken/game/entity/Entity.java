package me.ramazanenescik04.diken.game.entity;

import java.util.Properties;

import me.ramazanenescik04.diken.game.GameObject;

public abstract class Entity extends GameObject {
	public boolean removed = false;
	public int health = 100, maxHealth = 100; // Default health value
	public int z;
	
	public Properties prop;

	public Entity(int x, int y, int width, int height) {
		super(x, y, width, height);
		this.name = "Entity";
	}
	
	public Entity(int x, int y) {
		super(x, y);
		this.name = "Entity";
	}
	
	//API START
	
	public void remove() {
		this.removed = true;
	}
	
	public void kill() {
		this.health = -1; // Set health to -1 to indicate death
	}
	
	public boolean isDead() {
		return this.health <= 0;
	}

	public int getHealth() {
		return this.health;
	}
	
	public void setHealth(int health) {
		this.health = Math.min(health, this.maxHealth);
	}
	
	public void move(float dx, float dy) {
		this.x += dx;
		this.y += dy;
	}
	
    //API END
}
