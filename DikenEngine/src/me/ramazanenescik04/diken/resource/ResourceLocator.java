package me.ramazanenescik04.diken.resource;

import java.util.concurrent.ConcurrentHashMap;
import java.io.InputStream;
import java.util.Map;
import java.util.Objects;

public class ResourceLocator {
    private static final Map<ResourceKey, IResource> resMap = new ConcurrentHashMap<>();
    private static final ResourceKey lookupKey = new ResourceKey("diken", "dummy");
    
    public static void addResource(String resourceName, InputStream in, EnumResource resType) {
        addResource(new ResourceKey(resourceName), in, resType);
    }
    
    public static void addResource(ResourceKey key, InputStream in, EnumResource resType) {
        var res = IOResource.loadResource(in, resType);
        
        if (res == null) {
            res = IOResource.missingTexture;
        }
        resMap.put(key, res);
    }
    
    public static void addResource(String resourceName, IResource res) {
        addResource(new ResourceKey(resourceName), res);
    }
    
    public static void addResource(ResourceKey key, IResource res) {
        if (res == null) {
            res = IOResource.missingTexture;
        }
        resMap.put(key, res);
    }
    
    public static IResource getResource(String resName) {
        synchronized (lookupKey) {
            lookupKey.setResourceName(resName);
            return resMap.getOrDefault(lookupKey, IOResource.missingTexture);
        }
    }
    
    public static IResource getResource(ResourceKey key) {
        return resMap.getOrDefault(key, IOResource.missingTexture);
    }
    
    public static class ResourceKey {
        private final String gameID;
        private String resourceName;
        
        public ResourceKey(String resourceName) {
        	Objects.requireNonNull(resourceName);
        	
        	this.gameID = "diken";
            this.resourceName = resourceName;
        }

        public ResourceKey(String _gameID, String _resourceName) {
            this.gameID = Objects.requireNonNull(_gameID);
            this.resourceName = Objects.requireNonNull(_resourceName);
        }
        
        // Geçici arama nesnesi için değiştirici (setter) metot
        public void setResourceName(String resourceName) {
            this.resourceName = resourceName;
        }

        public String getGameID() { return gameID; }
        public String getResourceName() { return resourceName; }

        // Map içinde String'e dönüştürmeden direkt objeleri karşılaştırmak için ŞART:
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ResourceKey that = (ResourceKey) o;
            return gameID.equals(that.gameID) && resourceName.equals(that.resourceName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(gameID, resourceName);
        }

        public final String toString() {
            return gameID + ":" + resourceName;
        }
    }
}