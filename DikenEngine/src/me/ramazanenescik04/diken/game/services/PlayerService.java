package me.ramazanenescik04.diken.game.services;

import java.awt.event.KeyEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.EnumSettingType;
import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.game.entity.Humanoid;
import me.ramazanenescik04.diken.game.setting.Setting;
import me.ramazanenescik04.diken.game.setting.SettingCategory;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

public class PlayerService extends Service {
	private Humanoid character;
	private String username = "Player";
	private String nodeObjectID = "";

	public PlayerService() {
		this("PlayerService");
	}

	public PlayerService(String name) {
		super(name);
	}

	public PlayerService(DataInputStream in) throws IOException {
		super(in);
		loadNodeData(in);
	}
	
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Humanoid getCharacter() {
		return character;
	}

	public void setCharacter(Humanoid character) {
		this.character = character;
		if (this.character != null) {
			this.nodeObjectID = (character != null ? character.getNetId().toString() : ""); 
		}
	}
	
	@Override
	public boolean showStudio() {
		return true;
	}
	
	@Override
	public void update(World world, DikenEngine engine) {
		super.update(world, engine);
		
		if (this.character != null && world.getRunService().isRunning()) {
			if (engine.input.isKeyDown(KeyEvent.VK_W)) {
				this.character.move(0, -1);
			}
			
			if (engine.input.isKeyDown(KeyEvent.VK_A)) {
				this.character.move(-1, 0);
			}
			
			if (engine.input.isKeyDown(KeyEvent.VK_S)) {
				this.character.move(0, 1);
			}
			
			if (engine.input.isKeyDown(KeyEvent.VK_D)) {
				this.character.move(1, 0);
			}
		}
	}

	@Override
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("playerService", "PlayerService", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(2, 1));
		
		var settingCategory = SettingCategory
				.createSettingCategory(key)
				.addSetting(new Setting<>("Username", username, String.class, EnumSettingType.TEXT_FIELD)
						.addChangeListener(this::setUsername))
				.addSetting(new Setting<>("Character", character, Humanoid.class, EnumSettingType.OBJECT_SELECT)
						.addChangeListener(this::setCharacter));
		
		var list = super.getNodeSettings();
		list.add(settingCategory);
		return list;
	}
	
	protected void reloadNode() {
		if (nodeObjectID.isEmpty())
			return;
		
		UUID target = UUID.fromString(nodeObjectID);
		List<Node> results = getRootNode().findByNetId(target);
		this.character = (Humanoid) (results.isEmpty() ? null : results.get(0));
	}

	@Override
	public void saveNodeData(DataOutputStream out) throws IOException {
		super.saveNodeData(out);
		out.writeBoolean(username != null);
		if (username != null) {
			out.writeUTF(username);
		}
		out.writeUTF(character != null ? character.getNetId().toString() : "");
	}

	@Override
	public void loadNodeData(DataInputStream in) throws IOException {
		super.loadNodeData(in);
		this.username = in.readBoolean() ? in.readUTF() : "Player";
		this.nodeObjectID = in.readUTF();
	}
}
