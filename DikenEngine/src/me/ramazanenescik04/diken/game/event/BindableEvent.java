package me.ramazanenescik04.diken.game.event;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;

import org.luaj.vm2.LuaValue;

import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.game.event.Event.LuaEventListenerJava;
import me.ramazanenescik04.diken.game.setting.SettingCategory;
/**
 * Represents the `Tool` type within the DikenEngine `game.nodes` package.
 */
public class BindableEvent extends Node {
	private final Event event = new Event();

	public BindableEvent() {
		this("BindableEvent");
	}

	public BindableEvent(String name) {
		super(name);
	}

	public BindableEvent(DataInputStream in) throws IOException {
		super(in);
		loadNodeData(in);
	}

	public void FireEvent(Object... args) {
		event.FireEvent(args);
	}

	public void Connect(LuaValue luaFunction) {
		event.Connect(luaFunction);
	}

	public void Connect(LuaEventListenerJava listener) {
		event.Connect(listener);
	}

	@Override
	public List<SettingCategory> getNodeSettings() {
		var settingCategory = SettingCategory
				.createSettingCategory("bindableNode", "BindableNode", 8, 2);
		
		var list = super.getNodeSettings();
		list.add(settingCategory);
		return list;
	}
}

