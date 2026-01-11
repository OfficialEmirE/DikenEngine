package me.ramazanenescik04.diken.game;

import java.util.ArrayList;
import java.util.List;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.gui.Hitbox;
import me.ramazanenescik04.diken.resource.Bitmap;

// Java 25 ile gelen özellikleri kullanabiliriz ama temel yapı sağlam olmalı.
public abstract class Node {

    // Hiyerarşi
    private Node parent;
    protected final List<Node> children = new ArrayList<>();
    
    // Temel Özellikler
    public String name;
    public boolean visible = true;
    
    // Transform (Basit tuttum, Vector2f veya Matrix kullanıyorsan değiştirebilirsin)
    public int x, y; 
    public float scaleX = 1.0f, scaleY = 1.0f;
    public float rotation = 0.0f;

    // Renk (0xAARRGGBB formatında int veya senin Color sınıfın)
    public int color = 0xFFFFFFFF; // Varsayılan Beyaz

    // 1. AABB Sistemi (Null ise collision yok/hayalet)
    protected Hitbox aabb = null;
    
    private boolean debug = false;
    public boolean solid = true;
    public boolean isStatic = false;

    // Constructor
    public Node(String name) {
        this.name = name;
    }
    
    public Node(String name, int x, int y) {
        this.name = name;
        this.x = x;
        this.y = y;
    }


    // --- Hiyerarşi Yönetimi ---

    public void addChild(Node child) {
        if (child.parent != null) {
            child.parent.removeChild(child); // Eski ailesinden kopar
        }
        child.parent = this;
        children.add(child);
        child.onAdded(); // 3. Lifecycle Event
    }

    public void removeChild(Node child) {
        if (children.remove(child)) {
            child.onRemoved(); // 3. Lifecycle Event
            child.parent = null;
        }
    }
    
    // --- Render Sistemi ---
    
    /**
     * Bu metod motor tarafından otomatik çağrılır.
     * 1. Node'un resmini (render() metodundan) alır.
     * 2. Bu resmi ana ekrana (btp) yapıştırır.
     * 3. Çocukları için aynı işlemi yapar.
     */
    public final void draw(Bitmap btp) {
        if (!visible) return;

        // 1. Bu objenin görüntüsünü iste
        Bitmap myTexture = render();

        // 2. Eğer görüntüsü varsa (görünmez bir container değilse) çiz
        if (myTexture != null) {
            // Not: Bitmap sınıfında 'drawBitmap' veya benzeri bir metodun olduğunu varsayıyorum.
            // Global koordinatlara çizim yapıyoruz.
            btp.draw(myTexture, (int)getGlobalX(), (int)getGlobalY());
            
            if (debug && aabb != null) {
				Hitbox globalBox = getGlobalAABB();
				btp.box(globalBox.x, globalBox.y, globalBox.width, globalBox.height, 0xffff0000); // Kırmızı kutu
				
				int renderX = (int)getGlobalX();
				int renderY = (int)getGlobalY();
				
				btp.box(renderX, renderY, myTexture.w, myTexture.h, 0xff00ff00); // Yeşil kutu
			}
        }

        // 3. Çocukları çiz (Recursive)
        for (Node child : children) {
            child.draw(btp);
        }
    }

    /**
     * Alt sınıflar artık çizim kodu yazmayacak, 
     * sadece kendilerini temsil eden Bitmap'i döndürecek.
     * * @return Çizilecek resim (veya null, eğer sadece container ise)
     */
    public abstract Bitmap render();

    // --- Collision & Logic ---

    public void update(World world, DikenEngine engine) {
        // Override edilebilir logic
        
        // Çocukları güncelle
        for (Node child : children) {
            child.update(world, engine);
        }
    }

    // AABB'yi ayarlamak için
    public void setAABB(int width, int height) {
        this.aabb = new Hitbox(0, 0, width, height); // Local coordinates
    }
    
    public Hitbox getGlobalAABB() {
        if (aabb == null) return null; // İçinden geçilebilir (Ghost)
        
        // Local AABB'yi Dünya koordinatlarına taşı
        int globalX = getGlobalX() + aabb.x;
        int globalY = getGlobalY() + aabb.y;
        return new Hitbox(globalX, globalY, aabb.width, aabb.height);
    }
    
    // Global pozisyonu bulmak için parent zincirini takip et
    public int getGlobalX() {
        if (parent == null) return x;
        return parent.getGlobalX() + x;
    }

    public int getGlobalY() {
        if (parent == null) return y;
        return parent.getGlobalY() + y;
    }
    
    public List<Node> getChildren() {
		return new ArrayList<>(children); // Kopya döndür
	}
    
    public void onCollision(Node other) {
    }
    
    public void separate(Node other) {
        Hitbox box1 = this.getGlobalAABB();
        Hitbox box2 = other.getGlobalAABB();

        if (box1 == null || box2 == null) return;

        // Merkez noktaları bul
        float c1x = box1.x + box1.width / 2.0f;
        float c1y = box1.y + box1.height / 2.0f;
        float c2x = box2.x + box2.width / 2.0f;
        float c2y = box2.y + box2.height / 2.0f;

        // Mesafeler
        float dx = c1x - c2x;
        float dy = c1y - c2y;

        // Çarpışma olmaması için gereken minimum mesafeler
        float minDistanceX = (box1.width / 2.0f) + (box2.width / 2.0f);
        float minDistanceY = (box1.height / 2.0f) + (box2.height / 2.0f);

        // Ne kadar iç içe girmişler?
        float overlapX = minDistanceX - Math.abs(dx);
        float overlapY = minDistanceY - Math.abs(dy);

        // Çarpışma var mı kontrolü (Garanti olsun)
        if (overlapX > 0 && overlapY > 0) {
            // Hangi taraftan itmek daha kolaysa oradan it (En kısa yol)
            if (overlapX < overlapY) {
                // X ekseninde it
                if (dx > 0) {
                    this.x += overlapX; // Sağa it
                } else {
                    this.x -= overlapX; // Sola it
                }
            } else {
                // Y ekseninde it
                if (dy > 0) {
                    this.y += overlapY; // Aşağı it
                } else {
                    this.y -= overlapY; // Yukarı it
                }
            }
        }
    }

    public boolean isDebugRenderer() {
		return debug;
	}

	public void setDebugRenderer(boolean debug) {
		this.debug = debug;
	}

	public boolean isSolid() {
		return solid;
	}

	public void setSolid(boolean isSolid) {
		this.solid = isSolid;
	}

	public boolean isStatic() {
		return isStatic;
	}

	public void setStatic(boolean isStatic) {
		this.isStatic = isStatic;
	}

	// --- Lifecycle Hooks (3. Madde) ---
    // Alt sınıflar isterse bunları override edebilir
    protected void onAdded() {} 
    protected void onRemoved() {}
}