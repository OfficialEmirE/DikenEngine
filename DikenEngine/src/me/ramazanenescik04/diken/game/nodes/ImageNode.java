package me.ramazanenescik04.diken.game.nodes;

import java.util.List;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.EnumSettingType;
import me.ramazanenescik04.diken.game.Instance;
import me.ramazanenescik04.diken.game.Setting;
import me.ramazanenescik04.diken.game.SettingCategory;
import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.EnumResource;
import me.ramazanenescik04.diken.resource.ResourceLocator;

/**
 * Represents the `ImageNode` type within the DikenEngine `game.nodes` package.
 */
public abstract class ImageNode extends Instance {
	private static final long serialVersionUID = -5489915245652040387L;
	
	protected transient Bitmap texture = new Bitmap(16, 16);
	private transient boolean textureLoaded = false;
	private String resourceID = "empty";

	public ImageNode() {
		this("DONT-USE->ImageNode");
	}

	public ImageNode(String name) {
		super(name);
		this.setSolid(false);
	}
	
	public String getTexture() {
		return resourceID;
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
		}
	}

	@Override
	protected void reloadNode() {
		textureLoaded = false;
	}
	
	@Override
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("imageNode", "ImageNode", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(1, 1));
		var settingCategory = SettingCategory
				.createSettingCategory(key)
				.addSetting(new Setting<String>("Texture ID", resourceID, String.class, EnumSettingType.RESOURCE_SELECT).addChangeListener(this::setTexture));
		
		var list = super.getNodeSettings();
		list.add(settingCategory);
		return list;
	}
}

