package me.ramazanenescik04.diken.game.nodes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.game.setting.EnumSettingType;
import me.ramazanenescik04.diken.game.setting.Setting;
import me.ramazanenescik04.diken.game.setting.SettingCategory;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.EnumResource;
import me.ramazanenescik04.diken.resource.ResourceLocator;
import me.ramazanenescik04.diken.resource.SoundResource;

/**
 * Represents the `ImageNode` type within the DikenEngine `game.nodes` package.
 */
public class Audio extends Node {
	protected transient SoundResource texture = new SoundResource();
	private transient boolean textureLoaded = false;
	private String resourceID = "empty";

	public Audio() {
		this("Audio");
	}

	public Audio(String name) {
		super(name);
	}

	public Audio(DataInputStream in) throws IOException {
		super(in);
		loadNodeData(in);
	}
	
	public String getSound() {
		return resourceID;
	}

	public void setSound(String soundID) {
		if (soundID == null || soundID.isBlank())
			this.resourceID = "empty";
		else
			this.resourceID = soundID;
		
		this.textureLoaded = false;
	}

	@Override
	public void update(World world, DikenEngine engine) {
		super.update(world, engine);
		
		if (!textureLoaded) {
			this.texture = world.getResource(resourceID, EnumResource.SOUND);
			this.textureLoaded = true;
			
			if (texture == null)
				texture = new SoundResource();
		}
	}

	@Override
	protected void reloadNode() {
		textureLoaded = false;
	}
	
	public void playAudio(boolean play) {
		if (texture == null)
			return;
		
		if (play) {
			try {
				texture.play();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			texture.stop();
		}
	}
	
	public boolean isPlaying() {
		if (texture == null) return false;
		return texture.isPlaying();
	}
	
	public void setLoop(boolean loop) {
		if (texture == null) return ;
		texture.setLoop(loop);
	}
	
	public boolean isLoop() {
		if (texture == null) return false;
		return texture.isLoop();
	}
	
	public void setVolume(float volume) {
		if (texture == null) return;
		texture.setVolume(volume);
	}
	
	public float getVolume() {
		if (texture == null) return 0f;
		return texture.getVolume();
	}
	
	public void setPosition(long position) {
		if (texture == null) return;
		texture.setPosition(position);
	}
	
	public long getPosition() {
		if (texture == null) return 0;
		return texture.getPosition();
	}
	
	public void setPitch(float pitch) {
		if (texture == null) return;
		texture.setPitch(pitch);
	}
	
	public float getPitch() {
		if (texture == null) return 0f;
		return texture.getPitch();
	}
	
	@Override
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("audio", "Audio", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(11, 1));
		var settingCategory = SettingCategory
				.createSettingCategory(key)
				.addSetting(new Setting<String>("Sound ID", resourceID, String.class, EnumSettingType.RESOURCE_SELECT).addChangeListener(this::setSound))
				.addSetting(new Setting<Boolean>("Playing", isPlaying(), Boolean.class, EnumSettingType.CHECK_BOX).addChangeListener(this::playAudio))
				.addSetting(new Setting<Boolean>("Loop", isLoop(), Boolean.class, EnumSettingType.CHECK_BOX).addChangeListener(this::setLoop))
				.addSetting(new Setting<Float>("Volume", getVolume(), Float.class, EnumSettingType.TEXT_FIELD).addChangeListener(this::setVolume))
				.addSetting(new Setting<Long>("Position", getPosition(), Long.class, EnumSettingType.TEXT_FIELD).addChangeListener(this::setPosition))
				.addSetting(new Setting<Float>("Pitch", getPitch(), Float.class, EnumSettingType.TEXT_FIELD).addChangeListener(this::setPitch));
		
		var list = super.getNodeSettings();
		list.add(settingCategory);
		return list;
	}
	
	@Override
	public void saveNodeData(DataOutputStream out) throws IOException {
		super.saveNodeData(out);
		
		out.writeUTF(resourceID);
	}

	@Override
	public void loadNodeData(DataInputStream in) throws IOException {
		super.loadNodeData(in);
		
		this.resourceID = in.readUTF();
		this.textureLoaded = false;
	}
}
