package me.ramazanenescik04.diken.studio;

import java.io.*;
import java.util.*;

import me.ramazanenescik04.diken.Config;
import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.setting.Setting;
import me.ramazanenescik04.diken.game.setting.SettingCategory;

public final class SettingsManager {

    private static final List<SettingCategory> categories = new ArrayList<>();
    private static final Map<String, Setting<?>> byName = new LinkedHashMap<>();
    private static File saveFile = new File(Config.defaultConfigFile.getParentFile(), "studio.dat");

    private SettingsManager() {}

    public static void setSaveFile(File file) {
        saveFile = file;
    }

    public static SettingCategory registerCategory(SettingCategory category, boolean persist) {
        categories.add(category);
        if (persist) {
            for (Setting<?> setting : category.getSettings()) {
                byName.put(setting.getName(), setting);
            }
        }
        return category;
    }

    public static List<SettingCategory> getCategories() {
        return new ArrayList<>(categories);
    }
    
    public static SettingCategory unregisterCategory(String id) {
        SettingCategory removed = categories.stream()
                .filter(c -> c.getKey().getId().equals(id))
                .findFirst()
                .orElse(null);

        if (removed != null) {
            categories.remove(removed);
            for (Setting<?> setting : removed.getSettings()) {
                byName.remove(setting.getName());
            }
        }
        return removed;
    }

    public static void save() {
        File parent = saveFile.getParentFile();
        if (parent != null) parent.mkdirs();

        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(saveFile)))) {
            out.writeInt(byName.size());
            for (Setting<?> setting : byName.values()) {
                setting.writeSetting(out);
            }
        } catch (IOException e) {
            DikenEngine.errorLog("[SettingsManager] Kaydedilemedi. ", e);
        }
    }

    public static void load() {
        if (!saveFile.exists()) return;

        try (DataInputStream in = new DataInputStream(new BufferedInputStream(new FileInputStream(saveFile)))) {
            int count = in.readInt();
            for (int i = 0; i < count; i++) {
                Setting<?> loaded = Setting.readSetting(in);
                applyLoadedValue(loaded);
            }
        } catch (IOException e) {
        	DikenEngine.errorLog("[SettingsManager] Yüklenemedi. ", e);
        }
    }
    
    public static void removeConfig() {
    	var configFolder = Config.defaultConfigFile.getParentFile();
    	
    	if (configFolder.exists()) {
    		try {
    			delete(configFolder);
			} catch (IOException e) {
				DikenEngine.errorLog("[SettingsManager] Config Silinilemedi. ", e);
			}
    	}
    }
    
    private static void delete(File file) throws IOException {
        if (file.isDirectory()) {
            File[] files = file.listFiles();

            if (files != null) {
                for (File child : files) {
                    delete(child);
                }
            }
        }

        file.deleteOnExit();
    }

    @SuppressWarnings("unchecked")
    private static <T> void applyLoadedValue(Setting<T> loaded) {
        Setting<?> existing = byName.get(loaded.getName());
        if (existing == null) return; // artık kullanılmayan eski ayar, yok say
        try {
            ((Setting<T>) existing).setValue(loaded.getValue());
        } catch (Exception e) {
        	DikenEngine.errorLog("[SettingsManager] '" + loaded.getName() + "' uygulanamadı. ", e);
        }
    }
}