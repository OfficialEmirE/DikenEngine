package me.ramazanenescik04.diken.game.nodes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.Instance;
import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.game.NodeResource;
import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.game.entity.Humanoid;
import me.ramazanenescik04.diken.game.setting.EnumSettingType;
import me.ramazanenescik04.diken.game.setting.Setting;
import me.ramazanenescik04.diken.game.setting.SettingCategory;
import me.ramazanenescik04.diken.gui.hitbox.Hitbox;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

/**
 * Represents the `Tool` type within the DikenEngine `game.nodes` package.
 */
public class Tool extends Instance {	
	private NodeResource<Bitmap> resource;
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
	
	public Bitmap getIconBitmap() {
		return resource.getResource();
	}
	
	public String getIcon() {
		return resource.getKey();
	}
	
	public void setIcon(String icon) {
		this.resource.setKey(icon);
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
			this.setRenderType(RenderType.InVisible);
			copyTool.resource = this.resource.copy();
			copyTool.isEquipped = true;
			player.findFirstChild("Tools").addChild(copyTool);
			
			if (this.parent != null)
				this.parent.removeChild(this);
		}
	}

	@Override
	public void update(World world, DikenEngine engine) {
		super.update(world, engine);
		
		resource.update(world);
	}

	@Override
	public Bitmap render() {
		return resource.getResource();
	}
	
	@Override
	protected void reloadNode() {
		this.resource.reloadResource();
	}
	
	@Override
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("tool", "Tool", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(4, 1));
		var settingCategory = SettingCategory
				.createSettingCategory(key)
				.addSetting(new Setting<String>("Icon ID", resource.getKey(), String.class, EnumSettingType.RESOURCE_SELECT).addChangeListener(this::setIcon));
		
		var list = super.getNodeSettings();
		list.add(settingCategory);
		return list;
	}

	@Override
	public void saveNodeData(DataOutputStream out) throws IOException {
		super.saveNodeData(out);
		out.writeUTF(resource.getKey());
		out.writeBoolean(isEquipped);
	}

	@Override
	public void loadNodeData(DataInputStream in) throws IOException {
		super.loadNodeData(in);
		this.resource.setKey(in.readUTF());
		this.isEquipped = in.readBoolean();
	}
}

