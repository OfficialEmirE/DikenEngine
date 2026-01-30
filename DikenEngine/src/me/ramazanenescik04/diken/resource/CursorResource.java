package me.ramazanenescik04.diken.resource;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Cursor;

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
		
		IntBuffer buffer = BufferUtils.createIntBuffer(cursorBitmap.pixels.capacity());
		
		// Buffer'ı hazırla
		buffer.clear();
        
		int width = cursorBitmap.w;
		int height = cursorBitmap.h;
		IntBuffer pixels = cursorBitmap.pixels;
		for (int y = 0; y < height; y++) {
		    for (int x = 0; x < width; x++) {
		        int mirroredX = width - 1 - x;
		        int color = pixels.get(y * width + mirroredX);
		        buffer.put(color);
		    }
		}

        
        // Buffer'ı okuma için hazırla
        buffer.flip();
		
        Cursor cursor = null;
		try {
			cursor = new Cursor(cursorBitmap.w, cursorBitmap.h, 2, cursorBitmap.h - 2, 1, buffer, null);
		} catch (LWJGLException e) {
			e.printStackTrace();
		}
		
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
