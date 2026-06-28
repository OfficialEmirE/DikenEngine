package me.ramazanenescik04.diken.game.services;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.EnumSettingType;
import me.ramazanenescik04.diken.game.Setting;
import me.ramazanenescik04.diken.game.SettingCategory;
import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;
import me.ramazanenescik04.diken.scripting.Script;

public class RunService extends Service {
	private transient World world;
	private boolean running;

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
	
	public boolean isRunning() {
		return running;
	}

	public void run() {
		this.running = true;
		
		this.world.startScripts();
	}
	
	public void stop() {
		this.running = false;
		
		List<Script> scripts = this.world.getRoot().findByClass(Script.class);
    	for (Script script : scripts) {
    		script.stop();
    	}
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

	@Override
	public void saveNodeData(DataOutputStream out) throws IOException {
		super.saveNodeData(out);
		out.writeBoolean(running);
	}

	@Override
	public void loadNodeData(DataInputStream in) throws IOException {
		super.loadNodeData(in);
		this.running = in.readBoolean();
		this.OnReload.Connect(_ -> {
			World currentWorld = DikenEngine.getEngine() != null ? DikenEngine.getEngine().getWorld() : null;
			this.world = currentWorld;
		});
	}
}
