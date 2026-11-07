package me.ramazanenescik04.diken.game.event;

import java.util.Objects;

import org.luaj.vm2.LuaValue;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.scripting.Script;

public class LuaSignal extends Signal {
	public LuaValue signalFunc;
	private final Script owner;

	public LuaSignal(LuaValue luaFunction) {
		this(luaFunction, null);
	}

	public LuaSignal(LuaValue luaFunction, Script owner) {
		Objects.requireNonNull(luaFunction);
		this.owner = owner;
		this.signalFunc = luaFunction;
	}

	@Override
	public void invoke(Object... args) {
		LuaValue[] luaArgs = new LuaValue[args.length];
		for (int i = 0; i < args.length; i++) {
			luaArgs[i] = org.luaj.vm2.lib.jse.CoerceJavaToLua.coerce(args[i]);
		}

		Script previous = Script.setCurrentScript(owner);
		try {
			signalFunc.invoke(LuaValue.varargsOf(luaArgs));
		} catch (Exception e) {
			DikenEngine.errorLog("LuaSignal Error [???]: " + e.getMessage());
		} finally {
			Script.setCurrentScript(previous);
		}
	}

	@Override
	public String toString() {
		return "[Signal]-" + signalFunc.checkfunction().name();
	}
}
