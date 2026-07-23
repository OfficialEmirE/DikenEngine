package me.ramazanenescik04.diken.plugin;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.json.JSONObject;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.event.Event;
import me.ramazanenescik04.diken.log.ConsoleLog;
import me.ramazanenescik04.diken.studio.StudioPanel;
import me.ramazanenescik04.diken.studio.StudioUtils;

public class PluginManager {
    public static final PluginManager instance = new PluginManager();

    private final List<Plugin> plugins = new ArrayList<>();
    private final File pluginsFolder = new File("./plugins/");
    
    public final Event allPluginsLoaded = new Event();

    private PluginManager() {}

    public void loadPlugins(DikenEngine engine, StudioPanel studio) {
        if (!pluginsFolder.exists()) {
            pluginsFolder.mkdirs();
            return;
        }

        File[] files = pluginsFolder.listFiles(
                (_, name) -> name.endsWith(".jar")
        );

        if (files == null) return;

        for (File file : files) {
            loadJarPlugin(file, engine, studio);
        }
    }

    @SuppressWarnings("unchecked")
	private void loadJarPlugin(File file, DikenEngine engine, StudioPanel studio) {
        try {
            URLClassLoader loader = new URLClassLoader(
                    new URL[]{file.toURI().toURL()},
                    getClass().getClassLoader()
            );

            // plugin.json oku
            InputStream in = loader.getResourceAsStream("plugin.json");

            if (in == null) {
                ConsoleLog.sendLog("plugin.json bulunamadı: " + file.getName());
                loader.close();
                return;
            }

            String json = new String(
                    in.readAllBytes(),
                    StandardCharsets.UTF_8
            );

            JSONObject obj = new JSONObject(json);

            String mainClass = obj.getString("mainClass");

            // Ana sınıfı yükle
            Class<?> clazz = loader.loadClass(mainClass);

            if (!Plugin.class.isAssignableFrom(clazz)) {
                ConsoleLog.sendLog("Ana sınıf Plugin değil: " + mainClass);
                loader.close();
                return;
            }

            Plugin plugin = (Plugin) clazz
                    .getDeclaredConstructor()
                    .newInstance();

            plugin.setInfo(new PluginInfo(
                    (Class<? extends Plugin>) clazz,
                    file,
                    loader
            ));

            plugins.add(plugin);

            ConsoleLog.sendLog("Loaded plugin: " + plugin.getName());

            if (engine.isStudioMode()) {
                StudioUtils.registerPluginSettings(plugin);
            }
        } catch (Exception e) {
            ConsoleLog.sendLog("Plugin yüklenemedi: " + file.getName());
            e.printStackTrace();
        }
    }

    public void loadLocalPlugin(Class<? extends Plugin> pluginClass,
                                DikenEngine engine,
                                StudioPanel studio) {
        try {
            Plugin plugin = pluginClass
                    .getDeclaredConstructor()
                    .newInstance();

            plugin.setInfo(new PluginInfo(
                    pluginClass,
                    null,
                    null
            ));

            plugins.add(plugin);

            ConsoleLog.sendLog("Loaded local plugin: " + plugin.getName());

            if (engine.isStudioMode()) {
                StudioUtils.registerPluginSettings(plugin);
            }

        } catch (Exception e) {
            ConsoleLog.sendLog(
                    "Local plugin yüklenemedi: " + pluginClass.getName()
            );
            e.printStackTrace();
        }
    }

    public void disableAll() {
        for (Plugin plugin : plugins) {
            try {
                plugin.disable();

                URLClassLoader loader = plugin.info().classLoader();

                if (loader != null) {
                    loader.close();
                }

            } catch (Exception e) {
            	DikenEngine.errorLog("An issue was encountered disabling " + plugin.getName() + ":", e);
            }
        }

        plugins.clear();
    }

    public List<Plugin> getPlugins() {
        return Collections.unmodifiableList(plugins);
    }

	public void enableAll(DikenEngine engine, StudioPanel studio) {
		for (Plugin plugin : plugins) {
			try {
				plugin.enable(engine, studio);
			} catch (Exception e) {
				DikenEngine.errorLog("An error occurred while activating " + plugin.getName() + ":", e);
			}
		}
		
		allPluginsLoaded.FireEvent();
	}
}