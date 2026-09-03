package me.ramazanenescik04.diken.studio.builders;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractBuilder<T> {
	protected final String id;
	protected final Map<String, T> builders = new LinkedHashMap<>();
	
	public AbstractBuilder(String id) {
		this.id = id;
	}
	
	public void add(String key, T button) {
		this.builders.put(key, button);
	}
	
	public void remove(String key) {
		this.builders.remove(key);
	}
	
	public String getID() {
		return new String(id);
	}
	
	public T get(String key) {
		return builders.get(key);
	}
	
	public List<T> getButtons() {
		return new ArrayList<>(builders.values());
	}
	
	public static abstract class Builder<T, R> {
		protected Map<String, T> abstractBuilders = new LinkedHashMap<>();
		
		public abstract T createT(String key);
		public T create(String key) {
			var toolbar = createT(key);
			abstractBuilders.put(key, toolbar);
			return toolbar;
		}
		public T get(String key) {
			return abstractBuilders.get(key);
		}
		
		public abstract R convert();
	}
}
