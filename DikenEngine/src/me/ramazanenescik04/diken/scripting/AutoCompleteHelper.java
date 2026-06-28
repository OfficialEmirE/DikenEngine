package me.ramazanenescik04.diken.scripting;

import org.fife.ui.autocomplete.*;
import org.luaj.vm2.*;

public final class AutoCompleteHelper {
	public static CompletionProvider createLuaProvider(Globals globals) {
		DefaultCompletionProvider provider = new DefaultCompletionProvider() {
	        @Override
	        public boolean isValidChar(char ch) {
	            return super.isValidChar(ch) || ch == '.';
	        }
	    };
	    
		LuaValue[] keys = globals.keys();
		
		for (LuaValue key : keys) {
			String name = key.tojstring();
            LuaValue value = globals.get(key);
            
            if (value.isfunction()) {
                FunctionCompletion func = new FunctionCompletion(provider, name, "function");
                func.setShortDescription("Lua Global Fonksiyonu: " + name);
                provider.addCompletion(func);
            } 
            else if (value.istable()) {
                VariableCompletion tableComp = new VariableCompletion(provider, name, "table");
                tableComp.setShortDescription("Lua Kütüphanesi/Tablosu");
                provider.addCompletion(tableComp);
                
                addSubMethods(provider, name, value.checktable());
            } 
            else {
                VariableCompletion varComp = new VariableCompletion(provider, name, "variable");
                varComp.setShortDescription("Global Değişken (Tip: " + value.typename() + ")");
                provider.addCompletion(varComp);
            }
		}
		
		return provider;
	}
	
	private static void addSubMethods(DefaultCompletionProvider provider, String tableName, LuaTable table) {
	    LuaValue[] subKeys = table.keys();
	    for (LuaValue subKey : subKeys) {
	        String subName = subKey.tojstring();
	        LuaValue subValue = table.get(subKey);
	        
	        String fullName = tableName + "." + subName;
	        
	        if (subValue.isfunction()) {
	            FunctionCompletion func = new FunctionCompletion(provider, fullName, "function");
	            provider.addCompletion(func);
	        } else {
	            VariableCompletion varComp = new VariableCompletion(provider, fullName, "field");
	            provider.addCompletion(varComp);
	        }
	    }
	}
}
