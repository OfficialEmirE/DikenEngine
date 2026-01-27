package me.ramazanenescik04.diken.game.entity;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.Vec2D;
import me.ramazanenescik04.diken.game.Animation;
import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.game.nodes.Part;
import me.ramazanenescik04.diken.game.nodes.SpawnLocation;
import me.ramazanenescik04.diken.game.nodes.Tool;
import me.ramazanenescik04.diken.resource.Bitmap;

public class Humanoid extends Part {
	private static final long serialVersionUID = 2113495473844070076L;
	
	public transient boolean followCamera = true;
	public boolean canMove = true;
	public float speed = 4.0f;
	
	public int health = 100;
	public int maxHealth = 100;
	
	transient int killTime = 0;
	private transient Tool selectedTool;
	
	public Humanoid(int x, int y, int width, int height) {
		super(x, y, width, height);
		this.init();
	}
	
	public Humanoid(int width, int height) {
		super(0, 0, width, height);
		this.init();
	}
	
	private void init() {
		this.name = "Humanoid";
		this.setAnchored(false);
	}

	@Override
	public Bitmap render() {
		Bitmap bitmap = new Bitmap(this.aabb.width, this.aabb.height);
		bitmap.clear(0xffff00ff);
		bitmap.drawText("Player", 2, 2, false);
		return bitmap;
	}

	@Override
	public void update(World world, DikenEngine engine) {
		if (!isAlive()) {
			// Oyuncu öldüyse hareket edemez
			killTime++;
			this.canMove = false;
			
			if (killTime > 120) { // 2 saniye sonra yeniden doğma
				this.health = (this.maxHealth);
				List<SpawnLocation> spawnLocations = world.root.findByClass(SpawnLocation.class);
				
				if (spawnLocations.isEmpty()) {
					this.x = 0; // Yeniden doğma pozisyonu
					this.y = 0;
				} else {
					SpawnLocation spawnLocation = spawnLocations.get(ThreadLocalRandom.current().nextInt(spawnLocations.size()));
					this.x = (spawnLocation.getGlobalAABB().width / 2 - render().w / 2) + spawnLocation.getGlobalX();
					this.y = (spawnLocation.getGlobalAABB().height / 2 - render().h / 2) + spawnLocation.getGlobalY();
				}
				this.canMove = true;
				killTime = 0;
			}
		} else {
			killTime = 0;
		}
		super.update(world, engine);
	}
	
	public Tool getSelectedTool() {
		return this.selectedTool;
	}
	
	public Tool setSelectedTool(Tool tool) {
		if (tool != null) {
			tool.visible = true;
		}
		Tool oldTool = this.selectedTool;
		if (oldTool != null) {
			oldTool.visible = false;
		}
		return this.selectedTool = tool;
	}
	
	public void centerCamera(World world, DikenEngine engine, int width, int height) {
		int centerX = -(engine.getWidth() / 2 - width / 2);
	    int centerY = -(engine.getHeight() / 2 - height / 2);
		world.camera = new Vec2D(this.x + centerX, this.y + centerY);
	}
	
	public void centerCamera(World world, DikenEngine engine) {
		this.centerCamera(world, engine, this.aabb.width, this.aabb.height);
	}
	
	public Animation getIdleAnimation() {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	public Animation getWalkAnimation() {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	public void setIdleAnimation(Animation animation) {
		throw new UnsupportedOperationException("Not supported yet.");
	}
	
	public void setWalkAnimation(Animation animation) {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	public boolean isFollowCamera() {
		return followCamera;
	}

	public void setFollowCamera(boolean followCamera) {
		this.followCamera = followCamera;
	}

	public boolean isCanMove() {
		return canMove;
	}

	public void setCanMove(boolean canMove) {
		this.canMove = canMove;
	}

	public float getSpeed() {
		return speed;
	}

	public void setSpeed(float speed) {
		this.speed = speed;
	}
	
	public int getHealth() {
		return health;
	}
	
	public void setHealth(int health) {
		this.health = health;
	}
	
	public int getMaxHealth() {
		return maxHealth;
	}
	
	public void setMaxHealth(int maxHealth) {
		this.maxHealth = maxHealth;
	}
	
	public void heal(int amount) {
		this.health += amount;
		if (this.health > this.maxHealth) {
			this.health = this.maxHealth;
		}
	}
	
	public void damage(int amount) {
		this.health -= amount;
		if (this.health < 0) {
			this.health = 0;
		}
	}
	
	public boolean isAlive() {
		return this.health > 1;
	}
}
