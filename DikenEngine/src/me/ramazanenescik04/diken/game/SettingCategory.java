package me.ramazanenescik04.diken.game;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

public class SettingCategory {
	private final List<Setting<?>> settings;
	private final SettingKey key;
	
	private SettingCategory(SettingKey key) {
		this.settings = new java.util.ArrayList<>();
		this.key = key;
	}
	
	public SettingCategory addSetting(Setting<?> setting) {
		settings.add(setting);
		return this;
	}

	public static SettingCategory createSettingCategory(SettingKey key) {
		var category = new SettingCategory(key);
		return category;
	}
	
	public static SettingCategory createSettingCategory(String id, String category, Bitmap image) {
		if (image == null) {
			image = ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(0, 1);
		}
		
		return createSettingCategory(new SettingKey(id, category, image));
	}
	
	public List<Setting<?>> getSettings() {
		return new java.util.ArrayList<>(settings); // Kopya liste döndürerek dış müdahaleyi engelle
	}
	
	public SettingKey getKey() {
		return key;
	}
	
	public static class SettingKey {
		private String id;
		private Bitmap image;
		private String category;
		
		public SettingKey(String id, String category, Bitmap image) {
			this.id = id;
			this.image = image;
			this.category = category;
		}
		
		public String getId() {
			return id;
		}
		
		public Bitmap getImage() {
			return image;
		}
		
		public String getCategory() {
			return category;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null || getClass() != obj.getClass()) return false;
			SettingKey other = (SettingKey) obj;
			return id.equals(other.id);
		}
		
		@Override
		public int hashCode() {
			return java.util.Objects.hash(id, image, category);
		}
	}
	
	public static class SettingCategoryHelper {
		private static final Map<SettingCategory.SettingKey, SettingCategory> settingsCache = new HashMap<>();

		public static SettingCategory getOrCreateCategory(SettingCategory.SettingKey key,
		                                             Supplier<SettingCategory> factory) {
		    return settingsCache.computeIfAbsent(key, _ -> factory.get());
		}
	}
}
