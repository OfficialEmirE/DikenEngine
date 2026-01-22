package me.ramazanenescik04.diken.game.nodes;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.IResource;

public abstract class ImageNode extends Node {
	private static final long serialVersionUID = -5489915245652040387L;
	
	protected transient Bitmap texture = new Bitmap(16, 16);
	private byte[] bitmapData = null;

	public ImageNode() {
		this("DONT-USE->ImageNode");
	}

	public ImageNode(String name) {
		super(name);
		this.setSolid(false);
	}
	
	public Bitmap getTexture() {
		return texture;
	}

	public void setTexture(Bitmap texture) {
		this.texture = texture;
	}
	
	@Override
	public Bitmap render() {
		return null;
	}

	@Override
	protected void reloadNode() {
		try {			
			var in = new ByteArrayInputStream(bitmapData);
			texture = (Bitmap) IResource.loadResource(new DataInputStream(in), Bitmap.class.getName());
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
			texture.saveResource(new DataOutputStream(stream));
			
			this.bitmapData = stream.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
