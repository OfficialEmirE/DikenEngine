package me.ramazanenescik04.diken.scripting;

import java.util.List;

import org.luaj.vm2.Globals;

import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.game.setting.EnumSettingType;
import me.ramazanenescik04.diken.game.setting.Setting;
import me.ramazanenescik04.diken.game.setting.SettingCategory;
import me.ramazanenescik04.diken.scripting.security.LuaSecurity;

public abstract class BaseScript extends Node {
	protected transient Globals globals;
	
	public BaseScript() {
		super("BaseScript");
	}

	public BaseScript(String name) {
		super(name);
	}
	
	public abstract LuaSecurity getScriptSecurity();
	
	protected Globals getGlobals() {
		return globals = getScriptSecurity().generateGlobals();
	}
	
	@Override
	public List<SettingCategory> getNodeSettings() {
		var settingCategory = SettingCategory
				.createSettingCategory("baseScript", "Base Script", 12, 1)
				.addSetting(new Setting<>("SecurityType", getScriptSecurity().getName(), String.class, EnumSettingType.TEXT));
		
		var list = super.getNodeSettings();
		list.add(settingCategory);
		return list;
	}

}