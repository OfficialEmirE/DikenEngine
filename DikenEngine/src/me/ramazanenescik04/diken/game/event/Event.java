package me.ramazanenescik04.diken.game.event;

import java.util.ArrayList;
import java.util.List;

import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;

import me.ramazanenescik04.diken.DikenEngine;

public class Event {
	private List<Signal> listeners = new ArrayList<>();
	
	public void FireEvent(Object... args) {
        if (listeners == null || listeners.isEmpty()) return;

        LuaValue[] luaArgs = new LuaValue[args.length];
        for (int i = 0; i < args.length; i++) {
            luaArgs[i] = org.luaj.vm2.lib.jse.CoerceJavaToLua.coerce(args[i]);
        }

        for (Signal listener : listeners) {
            try {
                listener.signalFunc.invoke(LuaValue.varargsOf(luaArgs));
            } catch (Exception e) {
                DikenEngine.errorLog("Lua Event Error [???]: " + e.getMessage());
            }
        }
	}
	
	public void Disconnect(Signal signal) {
		listeners.remove(signal);
	}
	
	public Signal Connect(LuaValue luaFunction) {
		if (luaFunction == null || !luaFunction.isfunction()) return null;

		var signal = new Signal(luaFunction);
        listeners.add(signal);
        
        return signal;
	}
	
	public Signal Connect(LuaEventListenerJava listener) {
		if (listener == null) return null;

	    LuaValue luaFunctionWrapper = new VarArgFunction() {
	        @Override
	        public Varargs invoke(Varargs luaArgs) {
	            int count = luaArgs.narg();
	            Object[] javaArgs = new Object[count];
	            
	            for (int i = 1; i <= count; i++) {
	                LuaValue arg = luaArgs.arg(i);
	                
	                if (arg.isuserdata()) {
	                    javaArgs[i - 1] = arg.checkuserdata();
	                } else {
	                    javaArgs[i - 1] = arg.isnil() ? null : arg.tojstring();
	                }
	            }
	            
	            listener.onEvent(javaArgs);
	            
	            return LuaValue.NIL; 
	        }
	    };

	    return Connect(luaFunctionWrapper);
	}

	public static interface LuaEventListenerJava {
		void onEvent(Object... args);
	}
}
