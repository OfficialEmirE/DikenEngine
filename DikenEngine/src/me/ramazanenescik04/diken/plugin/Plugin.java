package me.ramazanenescik04.diken.plugin;

import java.util.List;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.setting.Setting;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.studio.StudioPanel;
import me.ramazanenescik04.diken.studio.builders.Menubar;
import me.ramazanenescik04.diken.studio.builders.Toolbar;

public abstract class Plugin {
	protected DikenEngine engine;
    protected StudioPanel studio;
    private PluginInfo info;
    private boolean enabled;

    public abstract String getName();
    public abstract String getVersion();
    public abstract String getAuthor();
    public abstract String getDescription();
    public abstract Bitmap getIcon();

    protected abstract void onEnable();
    protected abstract void onDisable();

    public final void enable(DikenEngine engine, StudioPanel studio) {
    	this.engine = engine;
    	this.studio = studio;
    	
        if (!enabled) {
            enabled = true;
            onEnable();
        }
    }

    public final void disable() {
        if (enabled) {
            enabled = false;
            onDisable();
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public PluginInfo info() {
        return info;
    }

    public void setInfo(PluginInfo info) {
        this.info = info;
    }
    
    public void generateToolbar(Toolbar.Builder builder) {};
    
    public void generateMenubar(Menubar.Builder builder) {};
    
    public List<Setting<?>> getPluginSettings() {
        return List.of();
    }
}