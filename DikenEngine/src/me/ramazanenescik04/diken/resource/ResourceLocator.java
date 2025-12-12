package me.ramazanenescik04.diken.resource;

import java.util.concurrent.ConcurrentHashMap;

import java.util.Map;
import java.util.Objects;

public class ResourceLocator {
	
	private static Map<String, IResource> resMap = new ConcurrentHashMap<String, IResource>();
	
	public static void addResource(String resourceName, IResource res) {
		addResource(new ResourceKey(resourceName), res);
	}
	
	public static void addResource(ResourceKey key, IResource res) {
		if (res == null) {
			res = IOResource.missingTexture;
		}
		
		resMap.put(key.toString(), res);
	}
	
	public static IResource getResource(String resName) {
		return resMap.getOrDefault(new ResourceKey(resName).toString(), IOResource.missingTexture);
	}
	
	public static IResource getResource(ResourceKey key) {
		return resMap.getOrDefault(key.toString(), IOResource.missingTexture);
	}
	
	public static final class ResourceKey {
		private final String gameID;
		private final String resourceName;
		
		public ResourceKey() {
			throw new IllegalArgumentException("please add resourceName");
		}
		
		public ResourceKey(String _resourceName) {
			Objects.requireNonNull(_resourceName);
			
			this.gameID = "diken";
			this.resourceName = _resourceName;
		}

		public ResourceKey(String _gameID, String _resourceName) {
			Objects.requireNonNull(_gameID);
			Objects.requireNonNull(_resourceName);
			
			this.gameID = _gameID;
			this.resourceName = _resourceName;
		}
		
		public String getGameID() {
			return gameID;
		}

		public String getResourceName() {
			return resourceName;
		}

		public final String toString() {
			return gameID+":"+resourceName;
		}

		protected Object clone() throws CloneNotSupportedException {
			throw new CloneNotSupportedException("not cloneable");
		}
		
		
	}
}
