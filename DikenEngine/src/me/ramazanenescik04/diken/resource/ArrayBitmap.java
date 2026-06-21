package me.ramazanenescik04.diken.resource;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Represents the `ArrayBitmap` type within the DikenEngine `resource` package.
 */
public class ArrayBitmap implements IResource {
	private static final long serialVersionUID = 1L;
	
	public Bitmap[][] bitmap;
	
	public ArrayBitmap() {
		this.bitmap = new Bitmap[][] {};
	}
	
	public ArrayBitmap(Bitmap[][] bitmaps) {
		this.bitmap = bitmaps;
	}
	
	public ArrayBitmap(ArrayBitmap array) {
		this.bitmap = array.bitmap.clone();
	}

	public void setArray(Bitmap[][] array) {
		this.bitmap = array;
	}
	
	public Bitmap getBitmap(int x, int y) {
		return bitmap[x][y];
	}
	
	@Override
	public EnumResource getResourceType() {
		return EnumResource.IMAGE;
	}
	
	@Override
	public void saveResource(DataOutputStream out) throws IOException {
		out.writeInt(bitmap.length);

		for (int i = 0; i < bitmap.length; i++) {
		    if (bitmap[i] == null) {
		        out.writeInt(-1);
		        continue;
		    }

		    out.writeInt(bitmap[i].length);

		    for (int j = 0; j < bitmap[i].length; j++) {
		        if (bitmap[i][j] == null) {
		            out.writeInt(-1);
		            continue;
		        }

		        byte[] data = bitmap[i][j].toBytes("png");
		        out.writeInt(data.length);
		        out.write(data);
		    }
		}

	}

	@Override
	public void loadResource(DataInputStream in) throws IOException {
	    int rows = in.readInt();
	    bitmap = new Bitmap[rows][];

	    for (int i = 0; i < rows; i++) {
	        int cols = in.readInt();

	        if (cols == -1) {
	            bitmap[i] = null;
	            continue;
	        }

	        bitmap[i] = new Bitmap[cols];

	        for (int j = 0; j < cols; j++) {
	            int len = in.readInt();

	            if (len == -1) {
	                bitmap[i][j] = null;
	                continue;
	            }

	            byte[] data = new byte[len];
	            in.readFully(data);

	            bitmap[i][j] = Bitmap.fromBytes(data);
	        }
	    }
	}

	public IResource clone() {
		return new ArrayBitmap(bitmap.clone());
	}
}
