package me.ramazanenescik04.diken.studio;

import javax.swing.JFrame;
import javax.swing.UIManager;

import bibliothek.gui.dock.common.CControl;
import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.game.setting.Setting;
import me.ramazanenescik04.diken.studio.dockables.DockablePanel;

import static me.ramazanenescik04.diken.game.EnumSettingType.*;

final class StudioUtils {
	static void openSettingsDialog(CControl control, DikenEngine engine, JFrame engineWindow) {
		SettingsDialog dialog = new SettingsDialog(engineWindow, "Motor Ayarları");

		dialog.addSection(1, 3, "DikenEngine Ayarları");
		var engineSettings = engine.config.getConfig();
		dialog.addSettings(engineSettings.values().toArray(new Setting<?>[engineSettings.size()]));
		
		String[] keyThemes = new String[control.getThemes().size()];	
		for (int i = 0; i < keyThemes.length; i++) {
			var key = control.getThemes().getKey(i);
			var firstChar = String.valueOf(key.charAt(0));
			keyThemes[i] = key.replaceFirst(firstChar, firstChar.toUpperCase());
		}
		
		var currentDockableTheme = control.getThemes().getSelectedKey();
		var firstChar = String.valueOf(currentDockableTheme.charAt(0));
		currentDockableTheme = currentDockableTheme.replaceFirst(firstChar, firstChar.toUpperCase());
		
		var installedLAFs = UIManager.getInstalledLookAndFeels();
		String[] lookAndFeelNames = new String[installedLAFs.length];
		for (int i = 0; i < installedLAFs.length; i++) {
		    lookAndFeelNames[i] = installedLAFs[i].getName(); 
		}
		String currentLAFName = UIManager.getLookAndFeel().getName();

		dialog.addSection(15, 2, "Tema");
		dialog.addSetting(new Setting<>("Dockable Teması", currentDockableTheme, keyThemes, String.class,
				LIST_SELECT).addChangeListener(e -> control.setTheme(e.toLowerCase())));
		dialog.addSetting(new Setting<>("Look And Feel", currentLAFName, lookAndFeelNames, String.class,
				LIST_SELECT).addChangeListener(selectedName -> {
					try {
						String targetClassName = null;
						for (var laf : UIManager.getInstalledLookAndFeels()) {
							if (laf.getName().equals(selectedName)) {
								targetClassName = laf.getClassName();
								break;
							}
						}

						if (targetClassName != null) {
							UIManager.setLookAndFeel(targetClassName);

							for (java.awt.Window window : java.awt.Window.getWindows()) {
								javax.swing.SwingUtilities.updateComponentTreeUI(window);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}));
		
		for (var entry : DockablePanel.panels.entrySet()) {
			var window = entry.getValue();
			dialog.addSection(15, 1, window.getTitle());
			
			dialog.addSettings(window.getDockableSettings().toArray(new Setting<?>[0]));
		}
		
		dialog.setVisible(true);
	}

	static void openGameSettingsDialog(World editWorld, JFrame engineWindow) {
		SettingsDialog dialog = new SettingsDialog(engineWindow, "Oyun Ayarları");
		dialog.addSection("Genel Bilgiler");
		dialog.addSetting(new Setting<>("İsim", editWorld.gameName, String.class, TEXT_FIELD).addChangeListener(e ->  editWorld.gameName = e));
		
		dialog.setVisible(true);
	}
}
