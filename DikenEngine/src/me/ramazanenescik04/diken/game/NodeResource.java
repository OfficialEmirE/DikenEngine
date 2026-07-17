package me.ramazanenescik04.diken.game;

import me.ramazanenescik04.diken.resource.EnumResource;
import me.ramazanenescik04.diken.resource.IResource;

public class NodeResource<T extends IResource> {
	private String key = "empty";
	private volatile boolean keyLoaded = false;
	private EnumResource resourceType;
	private volatile T value;
	
	public NodeResource(String defaultKey, EnumResource resource) {
		this.resourceType = resource;
		this.key = defaultKey;
		this.keyLoaded = false;
	}
	
	public void update(World theWorld) {
		if (keyLoaded == false) {
			this.value = theWorld.getResource(key, resourceType);
			this.keyLoaded = true;
		}
	}
	
	public EnumResource getResourceType() {
		return resourceType;
	}
	
	public void setResourceType(EnumResource resourceType) {
		this.resourceType = resourceType;
		this.keyLoaded = false;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
		this.keyLoaded = false;
	}

	public T getResource() {
		return value;
	}

	public boolean isLoaded() {
		return keyLoaded;
	}
	
	public void reloadResource() {
		this.keyLoaded = false;
	}
}
