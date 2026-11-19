package me.ramazanenescik04.diken.game.services;

import java.util.List;

import me.ramazanenescik04.diken.Timer;
import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.game.setting.SettingCategory;
import me.ramazanenescik04.diken.tools.Utils;

public class Debris extends Service {
	
	public Debris() {
		this("Debris");
	}
	
	public Debris(String name) {
		super(name);
	}
	
	public void addItem(Node instance, long lifetime) {
        Timer.delay(instance::removeNode, lifetime);
    }
	
	@Override
	public boolean showStudio() {
		return true;
	}

	@Override
	public List<SettingCategory> getNodeSettings() {
		return Utils.listAdd(super.getNodeSettings(), SettingCategory.createSettingCategory("debris", "Debris", 7, 3));
	}
}
