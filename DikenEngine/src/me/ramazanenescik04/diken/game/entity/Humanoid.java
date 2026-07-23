package me.ramazanenescik04.diken.game.entity;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.EnumSettingType;
import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.game.Animation;
import me.ramazanenescik04.diken.game.nodes.Folder;
import me.ramazanenescik04.diken.game.nodes.Part;
import me.ramazanenescik04.diken.game.nodes.SpawnLocation;
import me.ramazanenescik04.diken.game.nodes.Tool;
import me.ramazanenescik04.diken.game.setting.Setting;
import me.ramazanenescik04.diken.game.setting.SettingCategory;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

/**
 * Represents the `Humanoid` type within the DikenEngine `game.entity` package.
 */
public class Humanoid extends Part {
	public boolean canMove = true;
	public float speed = 4.0f;
	
	public int health = 100;
	public int maxHealth = 100;
	
	transient int killTime = 0;
	private transient Tool selectedTool;
	
	public Humanoid() {
		super(0, 0, 16, 16);
		this.init();
	}
	
	public Humanoid(int x, int y, int width, int height) {
		super(x, y, width, height);
		this.init();
	}
	
	public Humanoid(int width, int height) {
		super(0, 0, width, height);
		this.init();
	}

	public Humanoid(DataInputStream in) throws IOException {
		super(in);
		loadNodeData(in);
	}
	
	private void init() {
		this.name = "Humanoid";
		this.setAnchored(false);
		this.addChild(new Folder("Tools"));
	}

	@Override
	public Bitmap render() {
		return super.render();
	}

	@Override
	public void update(World world, DikenEngine engine) {
		if (!isAlive()) {
			// Oyuncu öldüyse hareket edemez
			killTime++;
			this.canMove = false;
			
			if (killTime > 120) { // 2 saniye sonra yeniden doğma
				this.health = (this.maxHealth);
				teleportSpawnLocation(world);
				this.canMove = true;
				killTime = 0;
			}
		} else {
			killTime = 0;
		}
		
		super.update(world, engine);
	}
	
	public void teleportSpawnLocation(World world) {
		List<SpawnLocation> spawnLocations = world.getWorkspace().findByClass(SpawnLocation.class);
		
		if (spawnLocations.isEmpty()) {
			this.setLocation(0, 0);
		} else {
			SpawnLocation spawnLocation = spawnLocations.get(ThreadLocalRandom.current().nextInt(spawnLocations.size()));
			this.setX((spawnLocation.getGlobalAABB().getWidth() / 2 - render().w / 2) + spawnLocation.getGlobalX());
			this.setY((spawnLocation.getGlobalAABB().getHeight() / 2 - render().h / 2) + spawnLocation.getGlobalY());
		}
	}
	
	public void move(int deltaX, int deltaY) {
	    // Gelen değişim miktarını hız ile çarpıyoruz
	    int moveX = (int) (deltaX * speed);
	    int moveY = (int) (deltaY * speed);
	    
	    // Mevcut konumun üzerine ekliyoruz
	    this.setX(this.getX() + moveX);
	    this.setY(this.getY() + moveY);
	}
	
	public Tool getSelectedTool() {
		return this.selectedTool;
	}
	
	public Tool setSelectedTool(Tool tool) {
		if (tool != null) {
			this.setRenderType(RenderType.RenderAll);
		}
		Tool oldTool = this.selectedTool;
		if (oldTool != null) {
			this.setRenderType(RenderType.InVisible);
		}
		return this.selectedTool = tool;
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
		return this.health >= 1;
	}
	
	@Override
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("humanoid", "Humanoid", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(2, 1));
		var settingCategory = SettingCategory
				.createSettingCategory(key)
				.addSetting(new Setting<Boolean>("Can Move", canMove, Boolean.class, EnumSettingType.CHECK_BOX).addChangeListener(this::setCanMove))
				.addSetting(new Setting<Float>("Speed", speed, Float.class, EnumSettingType.TEXT_FIELD).addChangeListener(this::setSpeed))
				.addSetting(new Setting<Integer>("Health", health, Integer.class, EnumSettingType.TEXT_FIELD).addChangeListener(this::setHealth))
				.addSetting(new Setting<Integer>("Max Health", maxHealth, Integer.class, EnumSettingType.TEXT_FIELD).addChangeListener(this::setMaxHealth));
		
		var list = super.getNodeSettings();
		list.add(settingCategory);
		return list;
	}

	@Override
	public void saveNodeData(DataOutputStream out) throws IOException {
		super.saveNodeData(out);
		out.writeBoolean(canMove);
		out.writeFloat(speed);
		out.writeInt(health);
		out.writeInt(maxHealth);
	}

	@Override
	public void loadNodeData(DataInputStream in) throws IOException {
		super.loadNodeData(in);
		this.canMove = in.readBoolean();
		this.speed = in.readFloat();
		this.health = in.readInt();
		this.maxHealth = in.readInt();
		this.killTime = 0;
		this.selectedTool = null;
	}
}
