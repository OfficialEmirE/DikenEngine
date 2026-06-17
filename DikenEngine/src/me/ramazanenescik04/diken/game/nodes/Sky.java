package me.ramazanenescik04.diken.game.nodes;

import java.util.List;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.EnumSettingType;
import me.ramazanenescik04.diken.game.Instance;
import me.ramazanenescik04.diken.game.Setting;
import me.ramazanenescik04.diken.game.SettingCategory;
import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.renderer.FrameBitmapPool;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.EnumResource;
import me.ramazanenescik04.diken.resource.ResourceLocator;

/**
 * Represents the `Sky` type within the DikenEngine `game.nodes` package.
 */
public class Sky extends Instance {
	private static final long serialVersionUID = 9068692202217556542L;
	private transient Bitmap skyBitmap;
	private transient int width = 1, height = 1;
	
	public String resourceID = "empty";
	private boolean resourceLoaded = false;

	public Sky() {
		super("Sky");
	}
	
	public Sky(int color) {
		super("Sky");
		this.color = color;
	}
	
	public Sky(String skyBitmap) {
		super("Sky");
		this.resourceID = skyBitmap;
	}
	
	public String getTexture() {
		return resourceID;
	}
	
	public void setTexture(String resourceID) {
		this.resourceID = resourceID;
		this.reloadNode();
	}

	@Override
	public Bitmap render() {
		if (width < 1) {
			this.width = 1;
		}
		
		if (height < 1) {
			this.height = 1;
		}
		
		Bitmap bitmap = FrameBitmapPool.newBitmap(width, height);
		bitmap.clear(color);
		if (this.skyBitmap != null) {
			for (int y = 0; y < (bitmap.h / skyBitmap.h) + 1; y++) {
				for (int x = 0; x < (bitmap.w / skyBitmap.w) + 1; x++) {
					bitmap.blendDraw(skyBitmap, x * skyBitmap.w, y * skyBitmap.h, this.color);
				}
			}
		}
		return bitmap;
	}

	@Override
	public void update(World world, DikenEngine engine) {
		syncToCamera(world, engine);
		
		super.update(world, engine);
		
		if (!resourceLoaded ) {
			skyBitmap = world.getResource(resourceID, EnumResource.IMAGE);
			resourceLoaded = true;
		}
	}

	public void syncToCamera(World world, DikenEngine engine) {
		float activeZoom = Math.max(0.1f, world.getZoom());
        int sceneWidth = Math.max(1, Math.round(engine.getScaledWidth() / activeZoom));
        int sceneHeight = Math.max(1, Math.round(engine.getScaledHeight() / activeZoom));
		this.width = sceneWidth + 20;
		this.height = sceneHeight + 20;
	}

	@Override
	public int getRenderX() {
		return -10;
	}
	
	@Override
	public int getRenderY() {
		return -10;
	}
	
	@Override
	protected boolean isCameraIndependent() {
		return true;
	}

	@Override
	protected void reloadNode() {
		this.resourceLoaded = false;
	}

	@Override
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("sky", "Sky", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(3, 1));
		
		var settingCategory = SettingCategory
				.createSettingCategory(key)
				.addSetting(new Setting<String>("Texture ID", resourceID, String.class, EnumSettingType.RESOURCE_SELECT).addChangeListener(this::setTexture));
		
		var list = super.getNodeSettings();
		list.add(settingCategory);
		return list;
	}
}
