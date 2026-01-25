package me.ramazanenescik04.diken.game;

import java.awt.Color;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;

import me.ramazanenescik04.diken.game.Setting.EnumSettingType;

public class Config {
	
	private volatile Map<String, Setting<?>> config = new HashMap<>();
	public static final Map<String, Setting<?>> defaultConfig = new HashMap<>();
	public static File defaultConfigFile = new File("./config.dat");

	public Config() {		
		defaultConfig.put("sync", new Setting<Boolean>("V-Sync", false, Boolean.class, EnumSettingType.CHECK_BOX));
		defaultConfig.put("debug", new Setting<Boolean>("Hata Ayıklama", false, Boolean.class, EnumSettingType.CHECK_BOX));
		defaultConfig.put("useOldScaleCode", new Setting<Boolean>("Eski Boyutlandırma Sistemi Kullan", false, Boolean.class, EnumSettingType.CHECK_BOX));
		
		defaultConfig.put("lang", new Setting<String>("Dil", "tr-TR", String.class, EnumSettingType.TEXT_FIELD));
		
		defaultConfig.put("activeWindowColor", new Setting<Integer>("Aktif Pencere Rengi", 0xff000080, Integer.class, EnumSettingType.COLOR_PICKER));
		defaultConfig.put("windowColor", new Setting<Integer>("Pencere Rengi", Color.GRAY.getRGB(), Integer.class, EnumSettingType.COLOR_PICKER));
		
		defaultConfig.put("guiScale", new Setting<Double>("GUI Ölceği", 1.0d, Double.class, EnumSettingType.SLIDER));
		
		
		this.config.putAll(defaultConfig);
		this.loadConfig(defaultConfigFile);
	}

	public boolean loadConfig(File configFile) {
		if(configFile == null) {
			System.err.println("File is Null!");
			return false;
		}
		try (DataInputStream stream = new DataInputStream(new FileInputStream(configFile))) {
			Map<String, Setting<?>> properties = new HashMap<>();
			int size = stream.readInt();
			for(int i = 0; i < size; i++) {
				var key = stream.readUTF();
				properties.put(key, Setting.readSetting(stream));
			}
			stream.close();
			this.config.putAll(properties);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public void saveConfig(File configFile) {
	    if(configFile == null) return;
	    
	    // GZIPOutputStream'i ayrı bir değişkende tutarak kontrolü garantileyelim
	    try (FileOutputStream fos = new FileOutputStream(configFile);
	         DataOutputStream stream = new DataOutputStream(fos)) {
	        
	        stream.writeInt(config.size());
	        for(Map.Entry<String, Setting<?>> entry : this.config.entrySet()) {
	            stream.writeUTF(entry.getKey());
	            entry.getValue().writeSetting(stream);
	        }
	        
	        stream.flush();
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}

	public boolean loadConfig() {
		return this.loadConfig(defaultConfigFile);
	}
	
	public void saveConfig() {
		this.saveConfig(defaultConfigFile);
	}
	
	public <T> Setting<T> getSetting(String key, Class<T> type) {
	    Setting<?> setting = config.get(key);
	    if (setting == null)
	        throw new IllegalArgumentException("Setting not found: " + key);

	    if (setting.getTypeClass() != type)
	        throw new IllegalArgumentException("Type mismatch");

	    @SuppressWarnings("unchecked")
	    Setting<T> s = (Setting<T>) setting;
	    return s;
	}
	
	@SuppressWarnings("unchecked")
	public <T> Setting<T> getOrDefaultSetting(
	        String key,
	        Class<T> type,
	        T defaultValue
	) {
	    return (Setting<T>) config.computeIfAbsent(
	        key,
	        k -> new Setting<>(key, defaultValue, type, null)
	    );
	}
	
	public <T> void setSetting(String key, T value) {
	    Setting<?> setting = config.get(key);
	    if (setting == null)
	        throw new IllegalArgumentException("Setting not found: " + key);

	    if (value == null) {
	        setting.setValue(null);
	        return;
	    }

	    if (!setting.getTypeClass().isInstance(value)) {
	        throw new IllegalArgumentException(
	            "Type mismatch. Expected " + setting.getTypeClass().getName()
	            + " but got " + value.getClass().getName()
	        );
	    }

	    @SuppressWarnings("unchecked")
	    Setting<T> s = (Setting<T>) setting;
	    s.setValue(value);
	}
	
	public Map<String, Setting<?>> getConfig() {
		return this.config;
	}
}
