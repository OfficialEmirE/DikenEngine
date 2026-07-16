package me.ramazanenescik04.diken.game.event;

import org.luaj.vm2.LuaValue;

public class Signal {
	public LuaValue signalFunc;
	
	public Signal(LuaValue luaFunction) {
		this.signalFunc = luaFunction;
	}

	@Override
	public String toString() {
		return "[Signal]-" + signalFunc.checkfunction().name();
	}
}
