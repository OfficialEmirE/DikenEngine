package me.ramazanenescik04.diken.gui.component;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.EnumSettingType;
import me.ramazanenescik04.diken.game.Setting;
import me.ramazanenescik04.diken.game.SettingCategory;
import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.gui.UDim2;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.EnumResource;
import me.ramazanenescik04.diken.resource.ResourceLocator;

/**
 * Represents the `RenderImage` type within the DikenEngine `gui.compoment` package.
 */
public class RenderImage extends GuiComponent {
	protected transient Bitmap bitmap = new Bitmap(16, 16);
	private transient boolean textureLoaded = false;
	private String resourceID = "empty";

	public RenderImage(String btpID, UDim2 position, UDim2 size) {
		super("RenderImage", position, size);
		
		setTexture(resourceID);
	}

	public RenderImage(DataInputStream in) throws IOException {
		super(in);
		loadNodeData(in);
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
	
	public Bitmap render() {
		return bitmap;
	}
	
	@Override
	public void update(World world, DikenEngine engine) {
		super.update(world, engine);
		
		if (!textureLoaded) {
			this.bitmap = world.getResource(resourceID, EnumResource.IMAGE);
			this.textureLoaded = true;
		}
	}

	@Override
	protected void reloadNode() {
		textureLoaded = false;
	}
	
	@Override
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("renderImage", "RenderImage", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(12, 2));
		
		var settingCategory = SettingCategory
				.createSettingCategory(key)
				.addSetting(new Setting<String>("Texture", this.resourceID, String.class, EnumSettingType.TEXT_FIELD).addChangeListener(this::setTexture));
		
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
		this.bitmap = new Bitmap(16, 16);
	}
}
