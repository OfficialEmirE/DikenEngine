package me.ramazanenescik04.diken.game.nodes;

import java.util.List;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.EnumSettingType;
import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.game.Setting;
import me.ramazanenescik04.diken.game.SettingCategory;
import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.EnumResource;
import me.ramazanenescik04.diken.resource.ResourceLocator;
import me.ramazanenescik04.diken.resource.SoundResource;

/**
 * Represents the `ImageNode` type within the DikenEngine `game.nodes` package.
 */
public class Audio extends Node {
	private static final long serialVersionUID = -5489915245652040387L;
	
	protected transient SoundResource texture = new SoundResource();
	private transient boolean textureLoaded = false;
	private String resourceID = "empty";

	public Audio() {
		this("Audio");
	}

	public Audio(String name) {
		super(name);
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
	
	private void playAudio(boolean play) {
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
	
	@Override
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("audio", "Audio", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(11, 1));
		var settingCategory = SettingCategory
				.createSettingCategory(key)
				.addSetting(new Setting<String>("Sound ID", resourceID, String.class, EnumSettingType.RESOURCE_SELECT).addChangeListener(this::setSound))
				.addSetting(new Setting<Boolean>("Playing", texture != null ? texture.isPlaying() : false, Boolean.class, EnumSettingType.CHECK_BOX).addChangeListener(this::playAudio))
				.addSetting(new Setting<Boolean>("Loop", texture != null ? texture.isLoop() : false, Boolean.class, EnumSettingType.CHECK_BOX).addChangeListener(texture::setLoop))
				.addSetting(new Setting<Float>("Volume", texture != null ? texture.getVolume() : 0.0f, Float.class, EnumSettingType.TEXT_FIELD).addChangeListener(texture::setVolume));
		
		var list = super.getNodeSettings();
		list.add(settingCategory);
		return list;
	}
}
