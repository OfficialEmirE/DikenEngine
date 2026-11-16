package me.ramazanenescik04.diken.scripting.security;

import java.io.IOException;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LoadState;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.jse.*;
import org.luaj.vm2.lib.*;

public class LuaStandardSecurity extends LuaSecurity {
	
	public LuaStandardSecurity() {
		super();
	}
	
	@Override
	public Globals generateGlobals() {
		Globals var0 = new Globals();
		var0.load(new JseBaseLib());
		var0.load(new PackageLib());
		var0.load(new Bit32Lib());
		var0.load(new TableLib());
		var0.load(new JseStringLib());
		var0.load(new CoroutineLib());
		var0.load(new JseMathLib());
		var0.load(new SecurityIoLib());
		var0.load(new SecurityOsLib());
		// var0.load(new LuajavaLib()); Security Leak!
		LoadState.install(var0);
		LuaC.install(var0);
		return var0;
	}
	
	protected static class SecurityOsLib extends OsLib {}
	
	protected static class SecurityIoLib extends IoLib {

		@Override
		protected File wrapStdin() throws IOException {
			throw new UnsupportedOperationException("wrapStdin() not supported!");
		}

		@Override
		protected File wrapStdout() throws IOException {
			throw new UnsupportedOperationException("wrapStdout() not supported!");
		}

		@Override
		protected File wrapStderr() throws IOException {
			throw new UnsupportedOperationException("wrapStderr() not supported!");
		}

		@Override
		protected File openFile(String var1, boolean var2, boolean var3, boolean var4, boolean var5)
				throws IOException {
			throw new UnsupportedOperationException("openFile() not supported!");
		}

		@Override
		protected File tmpFile() throws IOException {
			throw new UnsupportedOperationException("tmpFile() not supported!");
		}

		@Override
		protected File openProgram(String var1, String var2) throws IOException {
			throw new UnsupportedOperationException("openProgram() not supported!");
		}
		
	}
}
