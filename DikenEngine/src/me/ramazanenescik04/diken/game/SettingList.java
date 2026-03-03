package me.ramazanenescik04.diken.game;

import java.util.List;

public class SettingList {
	private final List<Setting<?>> settings;
	private final SettingKey key;
	
	private SettingList(SettingKey key) {
		this.settings = new java.util.ArrayList<>();
		this.key = key;
	}
	
	public SettingList addSetting(Setting<?> setting) {
		settings.add(setting);
		return this;
	}

	public static SettingList createSetting(SettingKey key) {
		return new SettingList(key);
	}
	
	public static SettingList createSetting(String id, String category) {
		return new SettingList(new SettingKey(id, category));
	}
	
	public SettingList addAll(SettingList... lists) {
		if (lists.length == 0) throw new IllegalArgumentException("En az bir SettingList sağlanmalıdır!");
		
		for (SettingList list : lists) {
			settings.addAll(list.settings);
		}
		return this;
	}
	
	public List<Setting<?>> getSettings() {
		return new java.util.ArrayList<>(settings); // Kopya liste döndürerek dış müdahaleyi engelle
	}
	
	public SettingKey getKey() {
		return key;
	}
	
	public static class SettingKey {
		private String id;
		private String category;
		
		public SettingKey(String id, String category) {
			this.id = id;
			this.category = category;
		}
		
		public String getId() {
			return id;
		}
		
		public String getCategory() {
			return category;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null || getClass() != obj.getClass()) return false;
			SettingKey other = (SettingKey) obj;
			return id == other.id && category.equals(other.category);
		}
	}
}
