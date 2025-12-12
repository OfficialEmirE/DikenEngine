package me.ramazanenescik04.diken.game;

import java.util.HashMap;
import java.util.Map;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.gui.Hitbox;
import me.ramazanenescik04.diken.resource.Bitmap;

public class GameObject extends Hitbox {
	protected String name; // Object name, used for identification
	private final Map<String, Object> properties = new HashMap<>();
	private boolean isVisible = true; // Object visibility status

	public GameObject(int x, int y, int width, int height) {
		super(x, y, width, height);
	}
	
	public GameObject(int x, int y) {
		super(x, y);
	}
	
	public Bitmap render() {
		return new Bitmap(16, 16);
	}
	
	public void tick(World world, DikenEngine engine) {
	}
	
	public void objectCollided(World world, DikenEngine engine, GameObject other) {
		// Default implementation does nothing
	}
	
	// API START
	
	public GameObject setName(String name) {
		this.name = name;
		return this;
	}
	
	public String getName() {
		return name;
	}
	
	public GameObject setVisible(boolean visible) {
		this.isVisible = visible;
		return this;
	}
	
	public boolean isVisible() {
		return isVisible;
	}
	
	// ---------------- Property API ----------------

    /**
     * Bir property ekler veya günceller.
     */
    public void setProperty(String key, Object value) {
        properties.put(key, value);
    }

    /**
     * Belirli bir property değerini alır.
     * @param key property adı
     * @return değeri veya null
     */
    public Object getProperty(String key) {
        return properties.get(key);
    }

    /**
     * Property var mı diye kontrol eder
     */
    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }

    /**
     * Property siler
     */
    public void removeProperty(String key) {
        properties.remove(key);
    }

    /**
     * Tüm property’leri döndürür (okuma amaçlı)
     */
    public Map<String, Object> getAllProperties() {
        return new HashMap<>(properties);
    }
	
	// API END
}
