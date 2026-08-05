package me.ramazanenescik04.diken.studio;

import java.awt.Window;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.game.setting.EnumSettingType;
import me.ramazanenescik04.diken.game.setting.Setting;
import me.ramazanenescik04.diken.game.setting.SettingCategory;
import me.ramazanenescik04.diken.language.Lang;
import me.ramazanenescik04.diken.plugin.Plugin;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;
import me.ramazanenescik04.diken.studio.dockables.DockablePanel;

public final class StudioUtils {
	public static Setting<String> LOOK_AND_FEEL;
    public static Setting<String> DOCKING_THEME;
    
    public static final Map<String, Integer> keyMapList = new LinkedHashMap<>();
    
    public static void registerPluginSettings(Plugin plugin) {
    	var settingList = plugin.getPluginSettings();
    	Bitmap pluginIcon = plugin.getIcon();
    	
    	if (settingList.isEmpty())
    		return;
    	
    	if (pluginIcon == null)
    		pluginIcon = ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(6, 3);
    	
    	var pluginSettings = SettingCategory.createSettingCategory(
    			plugin.info().pluginClass().getName(),
    			plugin.getName(),
    			pluginIcon
    	);
    	pluginSettings.addSettings(settingList);
		
		SettingsManager.registerCategory(pluginSettings, true);
	}
	
	static void init(StudioPanel studioPanel, DikenEngine engine, JFrame engineWindow) {
		var engineSettings = SettingCategory.createSettingCategory("engineSettings", "Engine Settings", 1, 3);
		engineSettings.addSettings(engine.config.getConfig().values());
		
		SettingsManager.registerCategory(engineSettings, false);
		
		String[] lafNames = Arrays.stream(UIManager.getInstalledLookAndFeels())
                .map(UIManager.LookAndFeelInfo::getName)
                .toArray(String[]::new);

        LOOK_AND_FEEL = new Setting<>("Look And Feel", UIManager.getLookAndFeel().getName(),
                lafNames, String.class, EnumSettingType.LIST_SELECT)
            .setDescription("Studio IDE arayüz temasını değiştirir.");
        LOOK_AND_FEEL.addChangeListener(StudioUtils::applyLookAndFeel);

        String[] dockingThemes = { "Eclipse", "Flat", "Bubble", "Basic", "Smooth" };
        var currentDockableTheme = studioPanel.control.getThemes().getSelectedKey();
        if (currentDockableTheme != null) {
        	var firstChar = String.valueOf(currentDockableTheme.charAt(0));
    		currentDockableTheme = currentDockableTheme.replaceFirst(firstChar, firstChar.toUpperCase());
        }

        DOCKING_THEME = new Setting<>("Docking Theme", currentDockableTheme, dockingThemes, String.class, EnumSettingType.LIST_SELECT)
            .setDescription("Panel (docking) arayüz temasını değiştirir.");
        DOCKING_THEME.addChangeListener(e -> studioPanel.control.setTheme(e.toLowerCase()));

        SettingCategory appearance = SettingCategory
                .createSettingCategory("appearance", "Appearence", 15, 2)
                .addSetting(LOOK_AND_FEEL)
                .addSetting(DOCKING_THEME)
                .addSetting(new Setting<>("Draw Grid", studioPanel.drawGrid, Boolean.class, EnumSettingType.CHECK_BOX)
                		.addChangeListener(e -> studioPanel.drawGrid = e))
                .addSetting(new Setting<>("Selection Color", studioPanel.selectionColor, Integer.class, EnumSettingType.COLOR_PICKER)
                		.addChangeListener(e -> studioPanel.selectionColor = e))
                .addSetting(new Setting<>("Handle Color", studioPanel.handleColor, Integer.class, EnumSettingType.COLOR_PICKER)
                		.addChangeListener(e -> studioPanel.handleColor = e))
                .addSetting(new Setting<>("Handle Size", studioPanel.handleSize, Integer.class, EnumSettingType.TEXT_FIELD)
                		.addChangeListener(e -> studioPanel.handleSize = e))
                .addSetting(new Setting<>("Grid Color", studioPanel.gridColor, Integer.class, EnumSettingType.COLOR_PICKER)
                		.addChangeListener(e -> studioPanel.gridColor = e));

        SettingsManager.registerCategory(appearance, true);
        
        SettingCategory keyMap = SettingCategory
                .createSettingCategory("keyMap", "Key Map", 0, 3);
        for (var entry : keyMapList.entrySet()) {
        	String langKey = "studio.keymap." + entry.getKey();
        	
        	keyMap.addSetting(new Setting<>(Lang.get(langKey), entry.getValue(), Integer.class, EnumSettingType.KEY_BIND)
        			.addChangeListener(entry::setValue));
        }
        
        SettingsManager.registerCategory(keyMap, true);
        
        for (var entry : DockablePanel.panels.entrySet()) {
        	var dockable = entry.getValue();
        	var settingList = dockable.getDockableSettings();
        	
        	if (settingList.isEmpty())
        		continue;
        	
        	SettingCategory dockableSettings = SettingCategory
                    .createSettingCategory(entry.getKey(), dockable.getTitle(), 15, 1);
        	
        	dockableSettings.addSettings(settingList);
            SettingsManager.registerCategory(dockableSettings, true);
        }
	}
	
	static void reloadGameSettings(World newWorld) {
		SettingsManager.unregisterCategory("gameSettings");
		
		var gameSettings = SettingCategory.createSettingCategory("gameSettings", "Game Settings", 9, 1)
				.addSetting(new Setting<>("Game Name", newWorld.getRoot().getName(), String.class, EnumSettingType.TEXT_FIELD)
				.addChangeListener(newWorld.getRoot()::setName))
				.addSetting(new Setting<>("Allow Third Party Resources", newWorld.getRoot().allowThirdPartyResources,
						Boolean.class, EnumSettingType.CHECK_BOX)
						.addChangeListener(e -> newWorld.getRoot().allowThirdPartyResources = e));
		
		SettingsManager.registerCategory(gameSettings, false);
	}
	
	private static void applyLookAndFeel(String lafName) {
        SwingUtilities.invokeLater(() -> {
            try {
                for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                    if (info.getName().equals(lafName)) {
                        UIManager.setLookAndFeel(info.getClassName());
                        break;
                    }
                }
                for (Window window : Window.getWindows()) {
                    SwingUtilities.updateComponentTreeUI(window);
                }
            } catch (Exception e) {
                DikenEngine.errorLog("[EngineSettings] LookAndFeel uygulanamadı!", e);
            }
        });
    }
	
	static {
		keyMapList.put("rename", KeyEvent.VK_F2);
		keyMapList.put("copy", KeyEvent.VK_C);
		keyMapList.put("cut", KeyEvent.VK_X);
		keyMapList.put("paste", KeyEvent.VK_V);
		keyMapList.put("duplicate", KeyEvent.VK_D);
		keyMapList.put("delete", KeyEvent.VK_DELETE);
		keyMapList.put("escape", KeyEvent.VK_ESCAPE);
		
		keyMapList.put("goInstance", KeyEvent.VK_F);
		keyMapList.put("goForward", KeyEvent.VK_W);
		keyMapList.put("goLeft", KeyEvent.VK_A);
		keyMapList.put("goBack", KeyEvent.VK_S);
		keyMapList.put("goRight", KeyEvent.VK_D);
	}
}
