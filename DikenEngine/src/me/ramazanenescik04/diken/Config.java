package me.ramazanenescik04.diken;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;

import me.ramazanenescik04.diken.game.setting.EnumSettingType;
import me.ramazanenescik04.diken.game.setting.Setting;
import me.ramazanenescik04.diken.language.Lang;

/**
 * Represents the `Config` type within the DikenEngine `game` package.
 */
public class Config {
	
	private volatile Map<String, Setting<?>> config = new HashMap<>();
	public static final Map<String, Setting<?>> defaultConfig = new HashMap<>();
	public static File defaultConfigFile = new File("./config/engine.dat");

	public Config() {		
		defaultConfig.put("sync", new Setting<Boolean>("V-Sync", false, Boolean.class, EnumSettingType.CHECK_BOX));
		defaultConfig.put("debug", new Setting<Boolean>("Debug Mode", false, Boolean.class, EnumSettingType.CHECK_BOX));
		defaultConfig.put("maxFPS", new Setting<>("Max Fps", 120, Integer.class, EnumSettingType.TEXT_FIELD));
		defaultConfig.put("fixedInternalResolution", new Setting<Boolean>("Fixed Internal Resolution", false, Boolean.class, EnumSettingType.CHECK_BOX));
		
		Map<String, String> available = Lang.getAvailableLanguages();
        String[] displayNames = available.values().toArray(new String[0]);
        String currentDisplayName = available.getOrDefault(Lang.getCurrentLanguage(), "English");
		
		defaultConfig.put("lang",
				new Setting<>("Language", currentDisplayName, displayNames, String.class, EnumSettingType.LIST_SELECT));
		
		defaultConfig.put("guiScale", new Setting<Integer>("GUI Scale", 1, 1, 3, Integer.class, EnumSettingType.SLIDER));
		defaultConfig.put("screenshotPath", new Setting<String>("Screenshot Path", "./", String.class, EnumSettingType.TEXT_FIELD));
		
		defaultConfig.put("saveLog", new Setting<Boolean>("Save Logs", false, Boolean.class, EnumSettingType.CHECK_BOX));
		
		this.config.putAll(defaultConfig);
	}

	public boolean loadConfig(File configFile) {
		if(configFile == null || !configFile.exists()) {
			DikenEngine.errorLog("file is not exist or null: " + configFile);
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
	    
	    if(configFile.getParentFile() != null) {
	        configFile.getParentFile().mkdirs();
	    }
	    
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
	
	public <T> T getSettingValue(String key, Class<T> type) {
	    return getSetting(key, type).getValue();
	}
	
	/**
	 * @deprecated Bunu kullanmak yerine LÜTFEN {@link Config#getSetting(String, Class)} kullanın!
	 * @return Ayarlar
	 */
	@SuppressWarnings("unchecked")
	@Deprecated
	public <T> Setting<T> getOrDefaultSetting(
	        String key,
	        Class<T> type,
	        T defaultValue
	) {
	    return (Setting<T>) config.computeIfAbsent(
	        key,
	        _ -> new Setting<>(key, defaultValue, type, null)
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
