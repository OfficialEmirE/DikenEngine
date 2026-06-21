package me.ramazanenescik04.diken.game.services;

import java.util.List;

import me.ramazanenescik04.diken.game.SettingCategory;
import me.ramazanenescik04.diken.game.event.Event;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

public class Service extends AbstractService {
	private static final long serialVersionUID = 1L;
	
	public final Event OnPreRender = new Event();
    public final Event OnPostRender = new Event();
	
	public Service() {
		this("Service");
	}
	
	public Service(String name) {
		super(name);
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
