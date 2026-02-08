package me.ramazanenescik04.diken.resource;

import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.concurrent.ThreadLocalRandom;

public class CursorResource implements IResource {
	private static final long serialVersionUID = 1L;
	public Bitmap cursorBitmap;
	private transient Cursor cursor;
	
	public CursorResource() {
		cursorBitmap = IOResource.missingTexture;
	}
	
	private Cursor generateCursor() {
		if (cursorBitmap == null)
			cursorBitmap = IOResource.missingTexture;
		
		// Source - https://stackoverflow.com/a/1984117
		// Posted by coobird, modified by community. See post 'Timeline' for change history
		// Retrieved 2026-02-05, License - CC BY-SA 2.5

		// Transparent 16 x 16 pixel cursor image.
		BufferedImage cursorImg = cursorBitmap.toImage();

		// Create a new blank cursor.
		Cursor cursor = Toolkit.getDefaultToolkit().createCustomCursor(
		    cursorImg, new Point(0, 0), "cursor-" + ThreadLocalRandom.current().nextLong());

		// Set the blank cursor to the JFrame.
		return cursor;
	}
	
	public Cursor getCursor() {
		if (cursor == null) {
			cursor = generateCursor();
		}
		
		return cursor;
	}

	@Override
	public void saveResource(DataOutputStream out) throws IOException {
		byte[] array = cursorBitmap.toBytes("png");
		out.writeInt(array.length);
		out.write(array);
	}

	@Override
	public void loadResource(DataInputStream in) throws IOException {
		int lenght = in.readInt();
		byte[] image = new byte[lenght];
		in.readFully(image);
		cursorBitmap = Bitmap.fromBytes(image);
	}

	public EnumResource getResourceType() {
		return EnumResource.CURSOR;
	}
	
	public IResource clone() {
		CursorResource clonedCursor = new CursorResource();
		clonedCursor.cursorBitmap = this.cursorBitmap;
		clonedCursor.generateCursor();
		return clonedCursor;
	}
}
