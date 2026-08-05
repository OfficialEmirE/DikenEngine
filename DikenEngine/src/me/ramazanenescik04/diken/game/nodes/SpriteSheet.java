package me.ramazanenescik04.diken.game.nodes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.Animation;
import me.ramazanenescik04.diken.game.Instance;
import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.game.setting.EnumSettingType;
import me.ramazanenescik04.diken.game.setting.Setting;
import me.ramazanenescik04.diken.game.setting.SettingCategory;
import me.ramazanenescik04.diken.renderer.FrameBitmapPool;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.EnumResource;
import me.ramazanenescik04.diken.resource.ResourceLocator;

public class SpriteSheet extends Instance {	
	protected transient Animation texture = new Animation(8);
	private transient boolean textureLoaded = false;
	private String resourceID = "empty";
	
	private boolean playing = false;
	private ImageType imageType = ImageType.Texture;
	
	public enum ImageType {
		Texture,
		Decal,
		None
	}

	public SpriteSheet() {
		this("SpriteSheet");
	}

	public SpriteSheet(String name) {
		super(name);
		this.setSolid(false);
	}

	public SpriteSheet(DataInputStream in) throws IOException {
		super(in);
		loadNodeData(in);
	}
	
	public String getAnimationID() {
		return resourceID;
	}
	
	public Animation getAnimation() {
		return texture;
	}

	public void setAnimationID(String texture) {
		if (texture == null || texture.isBlank())
			this.resourceID = "empty";
		else
			this.resourceID = texture;
		
		this.textureLoaded = false;
	}
	
	@Override
	public Bitmap render() {
		if (this.texture == null)
			return null;
		
		var texture = this.texture.getCurrentFrame();
		
		if (texture == null)
			return null;
		
		if (this.imageType == ImageType.Texture) {
			Bitmap parentBitmap = null;
			if (this.parent != null && this.parent instanceof Instance i) {
				parentBitmap = i.render();
			}
			
			if (parentBitmap == null)
				return null;
			
			Bitmap thisBitmap = FrameBitmapPool.newBitmap(parentBitmap.w, parentBitmap.h);
			for (var y = 0; y < (parentBitmap.h / texture.h) + 1; y++) {
				for (var x = 0; x < (parentBitmap.w / texture.w) + 1; x++) {
					thisBitmap.blendDraw(texture, x * texture.w, y * texture.h, this.getColor());
				}
			}
			return thisBitmap;
		} else if (this.imageType == ImageType.Decal) {
			Bitmap parentBitmap = null;
			if (this.parent != null && this.parent instanceof Instance i) {
				parentBitmap = i.render();
			}
			
			if (parentBitmap == null)
				return null;
			
			Bitmap scaledTexture = FrameBitmapPool.newBitmap(parentBitmap.w, parentBitmap.h);
			texture.scaleInto(scaledTexture);
			return scaledTexture;
		}
		return null;
	}

	@Override
	public void update(World world, DikenEngine engine) {
		super.update(world, engine);
		
		if (!textureLoaded) {
			this.texture = world.getResource(resourceID, EnumResource.ANIMATION);
			this.textureLoaded = true;
		}
		
		if (playing && this.texture != null) {
			this.texture.update(System.currentTimeMillis());
		}
	}

	public boolean isPlaying() {
		return playing;
	}

	public void setPlaying(boolean playing) {
		this.playing = playing;
	}

	public ImageType getImageType() {
		return imageType;
	}

	public void setImageType(ImageType imageType) {
		this.imageType = imageType;
	}

	@Override
	protected void reloadNode() {
		textureLoaded = false;
	}
	
	@Override
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("spriteSheet", "SpriteSheet", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(2, 3));
		var settingCategory = SettingCategory
				.createSettingCategory(key)
				.addSetting(new Setting<String>("Animation ID", resourceID, String.class, EnumSettingType.RESOURCE_SELECT).addChangeListener(this::setAnimationID))
				.addSetting(new Setting<ImageType>("Image Type", imageType, ImageType.values(), ImageType.class, EnumSettingType.LIST_SELECT).addChangeListener(this::setImageType))
				.addSetting(new Setting<Boolean>("Playing", playing, Boolean.class, EnumSettingType.CHECK_BOX).addChangeListener(this::setPlaying));
		
		var list = super.getNodeSettings();
		list.add(settingCategory);
		return list;
	}

	@Override
	public void saveNodeData(DataOutputStream out) throws IOException {
		super.saveNodeData(out);
		
		out.writeUTF(resourceID);
		out.writeUTF(imageType.name());
		out.writeBoolean(playing);
	}

	@Override
	public void loadNodeData(DataInputStream in) throws IOException {
		super.loadNodeData(in);
		
		this.resourceID = in.readUTF();
		this.imageType = ImageType.valueOf(in.readUTF());
		this.playing = in.readBoolean();
		
		this.textureLoaded = false;
	}
}
