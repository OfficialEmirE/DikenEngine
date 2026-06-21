package me.ramazanenescik04.diken.game.services;

import java.util.ArrayList;
import java.util.List;

import me.ramazanenescik04.diken.game.Instance;
import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.game.SettingCategory;
import me.ramazanenescik04.diken.gui.hitbox.Hitbox;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

public class Workspace extends Service {
	private static final long serialVersionUID = 1L;

	public Workspace() {
		this("Workspace");
	}

	public Workspace(String name) {
		super(name);
	}
	
	public void draw(Bitmap sceneBitmap, Hitbox viewport) {
		OnPreRender.FireEvent();

        for (int i = 0; i < children.size(); i++) {
            Node child = children.get(i);
			child.draw(sceneBitmap, viewport);
        }
        
        OnPostRender.FireEvent();
	}

	@Override
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("workspace", "Workspace", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(9, 1));
		
		var settingCategory = SettingCategory
				.createSettingCategory(key);
		
		var list = super.getNodeSettings();
		list.add(settingCategory);
		return list;
	}

	public List<Instance> findInArea(Hitbox area) {
		List<Instance> result = new ArrayList<>();
		
		for (Node child : getChildren()) {
			if (child instanceof Instance instance && instance.getGlobalAABB() != null && instance.getGlobalAABB().intersects(area)) {
				result.add(instance);
			}
	    }
	    
	    for (Node child : getChildren()) {
	    	if (child instanceof Instance instance) {
	    		List<Instance> childResult = instance.findInArea(area);
		        result.addAll(childResult);
	    	}
	    }
	    
	    return result;
	}
}
