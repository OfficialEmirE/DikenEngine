package me.ramazanenescik04.diken.game.event;

import java.util.Objects;

import me.ramazanenescik04.diken.DikenEngine;

public class JavaSignal extends Signal {
	private Event.LuaEventListenerJava listener;
	
	public JavaSignal(Event.LuaEventListenerJava listener) {
		Objects.requireNonNull(listener);
		
		this.listener = listener;
	}

	@Override
	public void invoke(Object... args) {
		try {
			this.listener.onEvent(args);
        } catch (Exception e) {
            DikenEngine.errorLog("JavaSignal Error [???]: " + e.getMessage());
        }
	}
	
	@Override
	public String toString() {
		return "[Java-Signal]-" + this.listener.hashCode();
	}
	
}
