package me.ramazanenescik04.diken.gui.component;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.EnumSettingType;
import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.game.setting.Setting;
import me.ramazanenescik04.diken.game.setting.SettingCategory;
import me.ramazanenescik04.diken.gui.UDim2;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.EnumResource;
import me.ramazanenescik04.diken.resource.ResourceLocator;

/**
 * Represents the `ImageButton` type within the DikenEngine `gui.compoment` package.
 */
public class ImageButton extends Button {
	private transient Bitmap bitmap;
	private transient boolean textureLoaded = false;
	private String textureID = "empty";

	public ImageButton(String btpTexture, UDim2 position, UDim2 size) {
		super("", position, size);
		this.setName("ImageButton");
		
		this.setIcon(btpTexture);
	}

	public ImageButton(DataInputStream in) throws IOException {
		super(in);
		loadNodeData(in);
	}
	
	public String getIcon() {
		return textureID;
	}

	public void setIcon(String texture) {
		if (texture == null || texture.isBlank())
			this.textureID = "empty";
		else
			this.textureID = texture;
		
		this.textureLoaded = false;
	}
	
	@Override
	public void update(World world, DikenEngine engine) {
		super.update(world, engine);
		
		if (!textureLoaded) {
			this.bitmap = world.getResource(textureID, EnumResource.IMAGE);
			this.textureLoaded = true;
		}
	}

	public Bitmap render() {
		Bitmap bitmap = super.render();
		
		if (this.bitmap != null)
			bitmap.draw(this.bitmap, (getWidth()) / 2 - (this.bitmap.w / 2), (getHeight()) / 2 - (this.bitmap.h / 2));
		
		return bitmap;
	}
	
	@Override
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("imageButton", "ImageButton", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(9, 2));
		
		var settingCategory = SettingCategory.createSettingCategory(key).addSetting(new Setting<String>("Icon ID",
				this.textureID, String.class, EnumSettingType.RESOURCE_SELECT)
				.setDescription(
						"This \"Icon ID\" should not be confused with the Icon ID on the Button. They are two different things.")
				.addChangeListener(this::setIcon));
		
		var list = super.getNodeSettings();
		list.add(settingCategory);
		return list;
	}
	
	@Override
	protected void reloadNode() {
		textureLoaded = false;
	}

	@Override
	public void saveNodeData(DataOutputStream out) throws IOException {
		super.saveNodeData(out);
		out.writeUTF(textureID);
	}

	@Override
	public void loadNodeData(DataInputStream in) throws IOException {
		super.loadNodeData(in);
		this.textureID = in.readUTF();
		this.textureLoaded = false;
	}
	
}
