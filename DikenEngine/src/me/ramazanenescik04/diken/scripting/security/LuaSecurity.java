package me.ramazanenescik04.diken.scripting.security;

import java.util.*;

import org.luaj.vm2.Globals;

public abstract class LuaSecurity {
	private static final List<LuaSecurity> SECURITY_TYPES = new ArrayList<>();
	public static final LuaSecurity STANDARD = new LuaStandardSecurity();;
	
	public LuaSecurity() {
		SECURITY_TYPES.add(this);
	}
	
	public abstract Globals generateGlobals();
	
	public String getName() {
		return this.getClass().getSimpleName();
	}
	
	public static List<LuaSecurity> getSecurityList() {
		return new ArrayList<>(SECURITY_TYPES);
	}
}
