package me.ramazanenescik04.diken.renderer;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

public class ArrayBuffer {
	private final IntBuffer nativeBuffer;
    public final int[] localArray;
    public final int size;

    public ArrayBuffer(int size) {
        this.size = size;
        this.localArray = new int[size];
        this.nativeBuffer = ByteBuffer.allocateDirect(size * 4)
                                      .order(ByteOrder.nativeOrder())
                                      .asIntBuffer();
    }

    public ArrayBuffer(int[] copy) {
        if (copy == null) {
        	throw new NullPointerException();
        }

        this.size = copy.length;
        this.localArray = new int[copy.length];
        System.arraycopy(copy, 0, this.localArray, 0, copy.length);
        this.nativeBuffer = ByteBuffer.allocateDirect(copy.length * 4)
        		.order(ByteOrder.nativeOrder())
        		.asIntBuffer();
    }

	public void put(ArrayBuffer copy) {
        if (copy == null || copy.localArray == null) {
            return;
        }

        int copySize = Math.min(this.localArray.length, copy.localArray.length);
        System.arraycopy(copy.localArray, 0, this.localArray, 0, copySize);
    }
    
    public void put(int index, int color) {
        set(index, color);
    }
    
    public void set(int index, int color) {
        localArray[index] = color;
    }

    public int get(int index) {
        return localArray[index];
    }

    public IntBuffer convertDirectIntBuffer() {
        nativeBuffer.clear();
        nativeBuffer.put(localArray);
        nativeBuffer.flip();
        return nativeBuffer;
    }

	public int size() {
		return size;
	}

	public static ArrayBuffer wrap(int[] pixels) {
		return new ArrayBuffer(pixels);
	}

	public int[] array() {
		return localArray;
	}
}
