package me.ramazanenescik04.diken.gui.component;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.NodeResource;
import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.game.io.Tag.Compound;
import me.ramazanenescik04.diken.game.nodes.ImageType;
import me.ramazanenescik04.diken.game.setting.EnumSettingType;
import me.ramazanenescik04.diken.game.setting.Setting;
import me.ramazanenescik04.diken.game.setting.SettingCategory;
import me.ramazanenescik04.diken.gui.UDim2;
import me.ramazanenescik04.diken.gui.hitbox.Hitbox;
import me.ramazanenescik04.diken.renderer.FrameBitmapPool;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.EnumResource;
import me.ramazanenescik04.diken.resource.ResourceLocator;

/**
 * Represents the `RenderImage` type within the DikenEngine `gui.compoment` package.
 */
public class RenderImage extends GuiComponent {
	protected NodeResource<Bitmap> resource = new NodeResource<>("empty", EnumResource.IMAGE);
	
	private ImageType imageType = ImageType.Texture;
	private boolean xFlip = false;

	public RenderImage(String btpID, UDim2 position, UDim2 size) {
		super("RenderImage", position, size);
		
		setTexture("empty");
	}
	
	public String getTexture() {
		return resource.getKey();
	}

	public void setTexture(String texture) {
		resource.setKey(texture);
	}
	
	public ImageType getImageType() {
		return imageType;
	}

	public void setImageType(ImageType imageType) {
		this.imageType = imageType;
	}

	public boolean isxFlip() {
		return xFlip;
	}

	public void setxFlip(boolean xFlip) {
		this.xFlip = xFlip;
	}
	
	public Bitmap render() {
		var texture = this.resource.getResource();

		if (texture == null) {
			return null;
		}
		
		Hitbox realSize = this.getLocalAbsoluteBounds();
		
		if (this.imageType == ImageType.Texture) {
			Bitmap thisBitmap = FrameBitmapPool.newBitmap(realSize.getWidth(), realSize.getHeight());
			for (var y = 0; y < (realSize.getHeight() / texture.h) + 1; y++) {
				for (var x = 0; x < (realSize.getWidth() / texture.w) + 1; x++) {
					thisBitmap.draw(texture, x * texture.w, y * texture.h);
				}
			}
			return thisBitmap;
		} else if (this.imageType == ImageType.Decal) {
			Bitmap scaledTexture = FrameBitmapPool.newBitmap(realSize.getWidth(), realSize.getHeight());
			texture.scaleInto(scaledTexture);
			return scaledTexture;
		}
		
		return null;
	}
	
	@Override
	public void update(World world, DikenEngine engine) {
		super.update(world, engine);
		
		resource.update(world);
	}

	@Override
	protected void reloadNode() {
		resource.reloadResource();
	}
	
	@Override
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("renderImage", "RenderImage", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(12, 2));
		
		var settingCategory = SettingCategory
				.createSettingCategory(key)
				.addSetting(new Setting<String>("Texture", this.resource.getKey(), String.class, EnumSettingType.RESOURCE_SELECT).addChangeListener(this::setTexture));
		
		var list = super.getNodeSettings();
		list.add(settingCategory);
		return list;
	}

	@Override
	public void saveNodeData(DataOutputStream out) throws IOException {
		super.saveNodeData(out);
		out.writeUTF(resource.getKey());
	}

	@Override
	public void loadNodeData(DataInputStream in) throws IOException {
		super.loadNodeData(in);
		this.resource.setKey(in.readUTF());
	}

	@Override
	public void saveNodeData(Compound tag) {
		super.saveNodeData(tag);
		tag.putBoolean("xFlip", xFlip);
		tag.putString("imageType", imageType.name());
	}

	@Override
	public void loadNodeData(Compound tag) {
		super.loadNodeData(tag);
		this.xFlip = tag.getBoolean("xFlip", false);
		this.imageType = ImageType.valueOf(tag.getString("imageType", "Texture"));
	}
}
