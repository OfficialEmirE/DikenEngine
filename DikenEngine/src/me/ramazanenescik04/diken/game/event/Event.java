package me.ramazanenescik04.diken.game.event;

import java.util.ArrayList;
import java.util.List;

import org.luaj.vm2.LuaValue;

public class Event {
	private List<Signal> listeners = new ArrayList<>();
	
	public void FireEvent(Object... args) {
        if (listeners == null || listeners.isEmpty()) return;
        
		for (Signal listener : new ArrayList<>(listeners)) {
            listener.invoke(args);
        }
	}
	
	public void Disconnect(Signal signal) {
		listeners.remove(signal);
	}

	public void Clear() {
		listeners.clear();
	}
	
	public void DisconnectLuaSignals() {
		listeners.removeIf(signal -> signal instanceof LuaSignal);
	}
	
	public Signal Connect(Signal signal) {
		if (signal == null) return null;

        listeners.add(signal);
        
        return signal;
	}
	
	public Signal Connect(LuaValue luaFunction) {
		if (luaFunction == null || !luaFunction.isfunction()) return null;

		var owner = me.ramazanenescik04.diken.scripting.Script.getCurrentScript();
		var signal = Connect(new LuaSignal(luaFunction, owner));
		if (owner != null) owner.trackConnection(this, signal);
		return signal;
	}
	
	public Signal Connect(LuaEventListenerJava listener) {
	    if (listener == null) return null;

	    return Connect(new JavaSignal(listener));
	}

	public static interface LuaEventListenerJava {
		void onEvent(Object... args);
	}
}
