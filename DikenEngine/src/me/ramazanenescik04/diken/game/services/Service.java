package me.ramazanenescik04.diken.game.services;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.game.event.Event;
import me.ramazanenescik04.diken.game.setting.SettingCategory;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

public abstract class Service extends AbstractService {
	public final Event OnPreRender = new Event();
    public final Event OnPostRender = new Event();
    
    protected World world;
    protected DikenEngine engine;
	
	public Service() {
		this("Service");
	}
	
	public Service(String name) {
		super(name);
	}

	public Service(DataInputStream in) throws IOException {
		super(in);
		if (getClass() == Service.class) {
			loadNodeData(in);
		}
	}
	
	@Override
	public void update(World world, DikenEngine engine) {
		super.update(world, engine);
		
		if (this.world == null || this.world != world) {
			this.world = world;
		}
		
		if (this.engine == null || this.engine != engine) {
			this.engine = engine;
		}
	}

	@Override
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("service", "Service", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(1, 3));
		
		var settingCategory = SettingCategory
				.createSettingCategory(key);
		
		var list = super.getNodeSettings();
		list.add(settingCategory);
		return list;
	}
}
