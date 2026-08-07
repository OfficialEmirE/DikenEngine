package me.ramazanenescik04.diken.game.nodes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.Instance;
import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.game.io.Tag.Compound;
import me.ramazanenescik04.diken.game.setting.EnumSettingType;
import me.ramazanenescik04.diken.game.setting.Setting;
import me.ramazanenescik04.diken.game.setting.SettingCategory;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.EnumResource;
import me.ramazanenescik04.diken.resource.ResourceLocator;

/**
 * Represents the `ImageNode` type within the DikenEngine `game.nodes` package.
 */
public abstract class ImageNode extends Instance {
	protected transient Bitmap texture = new Bitmap(16, 16);
	private transient boolean textureLoaded = false;
	private String resourceID = "empty";
	
	private boolean xFlip = false;

	public ImageNode() {
		this("DONT-USE->ImageNode");
	}

	public ImageNode(String name) {
		super(name);
		this.setSolid(false);
	}

	public ImageNode(DataInputStream in) throws IOException {
		super(in);
	}
	
	public String getTexture() {
		return resourceID;
	}
	
	public Bitmap getTextureBitmap() {
		return texture;
	}

	public void setTexture(String texture) {
		if (texture == null || texture.isBlank())
			this.resourceID = "empty";
		else
			this.resourceID = texture;
		
		this.textureLoaded = false;
	}
	
	@Override
	public Bitmap render() {
		return null;
	}

	@Override
	public void update(World world, DikenEngine engine) {
		super.update(world, engine);
		
		if (!textureLoaded) {
			this.texture = world.getResource(resourceID, EnumResource.IMAGE);
			this.textureLoaded = true;
			
			if (this.xFlip)
				this.texture = this.texture.opposite(false);
		}
	}

	@Override
	protected void reloadNode() {
		textureLoaded = false;
	}
	
	public boolean isxFlip() {
		return xFlip;
	}

	public void setxFlip(boolean xFlip) {
		this.xFlip = xFlip;
		this.textureLoaded = false;
	}

	@Override
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("imageNode", "ImageNode", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(1, 1));
		var settingCategory = SettingCategory
				.createSettingCategory(key)
				.addSetting(new Setting<>("Texture ID", resourceID, String.class, EnumSettingType.RESOURCE_SELECT)
						.addChangeListener(this::setTexture))
				.addSetting(new Setting<>("X Flip", xFlip, Boolean.class, EnumSettingType.CHECK_BOX)
						.addChangeListener(this::setxFlip));
		
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

	@Override
	public void saveNodeData(Compound tag) {
		tag.putBoolean("xFlip", xFlip);
	}

	@Override
	public void loadNodeData(Compound tag) {
		this.xFlip = tag.getBoolean("xFlip", false);
	}
}

