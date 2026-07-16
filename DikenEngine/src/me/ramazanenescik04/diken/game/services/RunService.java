package me.ramazanenescik04.diken.game.services;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.EnumSettingType;
import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.game.setting.Setting;
import me.ramazanenescik04.diken.game.setting.SettingCategory;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;
import me.ramazanenescik04.diken.scripting.Script;

public class RunService extends Service {
	private boolean running, runScriptEvent, stopScriptEvent;

	public RunService(World world) {
		this(world, "RunService");
	}

	public RunService(World world, String name) {
		super(name);
		this.world = world;
	}

	public RunService(DataInputStream in) throws IOException {
		super(in);
		loadNodeData(in);
	}

	@Override
	public void update(World world, DikenEngine engine) {
		if (stopScriptEvent) {
			world.disposeNodes();
			List<Script> scripts = this.world.getRoot().findByClass(Script.class);
	    	for (Script script : scripts) {
	    		script.stop();
	    	}
	    	System.gc();
	    	
			this.stopScriptEvent = true;
		}
		
		super.update(world, engine);
		
		if (runScriptEvent) {
			world.reloadNodes();
			world.startScripts();
			this.runScriptEvent = false;
		}
	}

	public boolean isRunning() {
		return running;
	}

	public void run() {
		this.running = true;
		this.runScriptEvent = true;
	}
	
	public void stop() {
		this.running = false;
		this.stopScriptEvent = true;
	}
	
	public void restart() {
		stop();
		run();
	}
	
	@Override
	public boolean showStudio() {
		return false;
	}

	@Override
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("runService", "RunService", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(1, 3));
		
		var settingCategory = SettingCategory
				.createSettingCategory(key)
				.addSetting(new Setting<>("Running", this.running, Boolean.class, EnumSettingType.CHECK_BOX).setChangeable(false));
		
		var list = super.getNodeSettings();
		list.add(settingCategory);
		return list;
	}
}
