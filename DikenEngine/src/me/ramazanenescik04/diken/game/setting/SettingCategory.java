package me.ramazanenescik04.diken.game.setting;

import java.util.Collection;
import java.util.List;

import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

/**
 * Represents the `SettingCategory` type within the DikenEngine `game` package.
 */
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
	
	public SettingCategory addSettings(Collection<Setting<?>> values) {
		settings.addAll(values);
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
	
	public static SettingCategory createSettingCategory(String id, String category, int x, int y) {
		return createSettingCategory(id, category, ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(x, y));
	}
	
	public static List<SettingCategory> addList(List<SettingCategory> list, SettingCategory category) {
		list.add(category);
		return list;
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
}
