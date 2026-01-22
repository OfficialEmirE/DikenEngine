package me.ramazanenescik04.diken.game.nodes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.game.entity.Humanoid;
import me.ramazanenescik04.diken.gui.Hitbox;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.IResource;

public class Tool extends Node {
	private static final long serialVersionUID = -3315727912522278757L;
	
	private transient Bitmap icon = new Bitmap(16, 16);
	private byte[] bitmapData = null;
	
	private boolean isEquipped = false;
	
	public Bitmap getIcon() {
		return icon;
	}
	
	public void setIcon(Bitmap icon) {
		this.icon = icon;
	}

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
	
	private void init() {
		this.aabb = new Hitbox(0, 0, 16, 16);
		this.setSolid(false);
		this.setAnchored(false);
	}
	
	public void onCollision(Node other) {
		if (other instanceof Humanoid player && !isEquipped) {
			Tool copyTool = (Tool) this.copy();
			copyTool.visible = false;
			copyTool.icon = this.icon.clone();
			copyTool.isEquipped = true;
			player.findFirstChild("Tools").addChild(copyTool);
			this.parent.removeChild(this);
		}
	}

	@Override
	public Bitmap render() {
		return icon;
	}
	
	@Override
	protected void reloadNode() {
		try {			
			var in = new ByteArrayInputStream(bitmapData);
			icon = (Bitmap) IResource.loadResource(new DataInputStream(in), Bitmap.class.getName());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ReflectiveOperationException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void dispose() {
		try {
			ByteArrayOutputStream stream = new ByteArrayOutputStream();
			icon.saveResource(new DataOutputStream(stream));
			
			this.bitmapData = stream.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
