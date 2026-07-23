package me.ramazanenescik04.diken.plugin;

import me.ramazanenescik04.diken.resource.Bitmap;

public class ExamplePlugin extends Plugin {

    @Override
    public String getName() {
        return "Example Plugin";
    }

    @Override
    public String getVersion() {
        return "0.1";
    }

    @Override
    public String getAuthor() {
        return "Ramazanenescik04";
    }

    @Override
    public String getDescription() {
        return "An example plugin.";
    }

    @Override
    public Bitmap getIcon() {
        return null;
    }

    @Override
    protected void onEnable() {
    }

    @Override
    protected void onDisable() {}
}