package me.ramazanenescik04.diken.renderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.ramazanenescik04.diken.resource.Bitmap;

/**
 * Frame-local bitmap pool for temporary render surfaces.
 * Use this only for bitmaps produced and consumed within the same frame.
 */
public final class FrameBitmapPool {
	private static final Map<Long, List<Bitmap>> BITMAPS = new HashMap<>();
	private static final Map<Long, Integer> CURSORS = new HashMap<>();
	
	private FrameBitmapPool() {
	}
	
	public static void beginFrame() {
		CURSORS.clear();
	}
	
	public static Bitmap newBitmap(int width, int height) {
		width = Math.max(1, width);
		height = Math.max(1, height);
		
		long key = (((long) width) << 32) | (height & 0xffffffffL);
		List<Bitmap> pool = BITMAPS.computeIfAbsent(key, _ -> new ArrayList<>());
		int index = CURSORS.getOrDefault(key, 0);
		Bitmap bitmap;
		
		if (index < pool.size()) {
			bitmap = pool.get(index);
		} else {
			bitmap = new Bitmap(width, height);
			pool.add(bitmap);
		}
		
		CURSORS.put(key, index + 1);
		bitmap.clear(0x00000000);
		return bitmap;
	}
}
