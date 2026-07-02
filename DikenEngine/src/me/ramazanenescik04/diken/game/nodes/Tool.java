package me.ramazanenescik04.diken.game.nodes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.EnumSettingType;
import me.ramazanenescik04.diken.game.Instance;
import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.game.entity.Humanoid;
import me.ramazanenescik04.diken.game.setting.Setting;
import me.ramazanenescik04.diken.game.setting.SettingCategory;
import me.ramazanenescik04.diken.gui.hitbox.Hitbox;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.EnumResource;
import me.ramazanenescik04.diken.resource.IResource;
import me.ramazanenescik04.diken.resource.ResourceLocator;

/**
 * Represents the `Tool` type within the DikenEngine `game.nodes` package.
 */
public class Tool extends Instance {
	private transient Bitmap icon = new Bitmap(16, 16);
	private String resourceID = "empty";
	private boolean resourceLoaded = false;
	
	private boolean isEquipped = false;
	
	public Tool() {
		super("Tool");
		init();
	}

	public Tool(String name) {
		super(name);
		init();
	}

	public Tool(String name, int x, int y) {
		super(name, x, y);
		init();
	}

	public Tool(DataInputStream in) throws IOException {
		super(in);
		loadNodeData(in);
	}
	
	public Bitmap getIconBitmap() {
		return icon;
	}
	
	public String getIcon() {
		return resourceID;
	}
	
	public void setIcon(String icon) {
		this.resourceID = icon;
		this.reloadNode();
	}
	
	private void init() {
		this.aabb = new Hitbox(0, 0, 16, 16);
		this.setSolid(false);
		this.setAnchored(false);
	}
	
	public void onCollision(Node other) {
		if (other instanceof Humanoid player && !isEquipped) {
			Tool copyTool = (Tool) this.copy();
			copyTool.setVisible(false);
			copyTool.icon = this.icon.clone();
			copyTool.isEquipped = true;
			player.findFirstChild("Tools").addChild(copyTool);
			
			if (this.parent != null)
				this.parent.removeChild(this);
		}
	}

	@Override
	public void update(World world, DikenEngine engine) {
		super.update(world, engine);
		
		if (!resourceLoaded) {
			IResource resource = world.getResource(resourceID, EnumResource.IMAGE);
			if (resource != null && resource instanceof Bitmap bitmap) {
				this.icon = bitmap.clone();
				this.resourceLoaded = true;
			}
		}
	}

	@Override
	public Bitmap render() {
		return icon;
	}
	
	@Override
	protected void reloadNode() {
		this.resourceLoaded = false;
	}
	
	@Override
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("tool", "Tool", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(4, 1));
		var settingCategory = SettingCategory
				.createSettingCategory(key)
				.addSetting(new Setting<String>("Icon ID", resourceID, String.class, EnumSettingType.RESOURCE_SELECT).addChangeListener(this::setIcon));
		
		var list = super.getNodeSettings();
		list.add(settingCategory);
		return list;
	}

	@Override
	public void saveNodeData(DataOutputStream out) throws IOException {
		super.saveNodeData(out);
		out.writeUTF(resourceID);
		out.writeBoolean(isEquipped);
	}

	@Override
	public void loadNodeData(DataInputStream in) throws IOException {
		super.loadNodeData(in);
		this.resourceID = in.readUTF();
		this.isEquipped = in.readBoolean();
		this.resourceLoaded = false;
		this.icon = new Bitmap(16, 16);
	}
}

