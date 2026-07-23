package me.ramazanenescik04.diken.plugin;

import java.io.File;
import java.net.URLClassLoader;

public record PluginInfo(
        Class<? extends Plugin> pluginClass,
        File pluginFile,
        URLClassLoader classLoader
) {}