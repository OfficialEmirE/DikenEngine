package me.ramazanenescik04.diken.game;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.gui.Hitbox;
import me.ramazanenescik04.diken.resource.Bitmap;

// Java 25 ile gelen özellikleri kullanabiliriz ama temel yapı sağlam olmalı.
public abstract class Node implements java.io.Serializable, Cloneable {
	private static final long serialVersionUID = -4123363831057244200L;
	
	// Hiyerarşi
    protected Node parent;
    protected List<Node> children = new ArrayList<>();
    
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
    
    protected boolean debug = false;
    protected boolean solid = true;
    protected boolean anchored = false;

    // Constructor
    public Node() {
    	this.name = "BaseNode";
    }
    
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
    
    public Node getParent() {
    	return parent;
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
				btp.box(globalBox.x, globalBox.y, globalBox.x + globalBox.width, globalBox.y + globalBox.height, 0xffff0000); // Kırmızı kutu
				
				int renderX = (int)getGlobalX();
				int renderY = (int)getGlobalY();
				
				btp.box(renderX, renderY, renderX + myTexture.w, renderY + myTexture.h, 0xff00ff00); // Yeşil kutu
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
    
    public StringBuilder printTree(boolean printConsole) {
        StringBuilder builder = new StringBuilder();
        generateTreeString(this, "", true, builder);
        
        if (printConsole) {
            System.out.println(builder.toString());
        }
        return builder;
    }

    private void generateTreeString(Node node, String prefix, boolean isLast, StringBuilder builder) {
        // Mevcut düğümü ekle
        builder.append(prefix)
               .append(isLast ? "└── " : "├── ")
               .append(node.name) // Düğüm ismine göre uyarla
               .append("\n");

        // Çocuk düğümleri gez
        List<Node> children = node.getChildren();
        for (int i = 0; i < children.size(); i++) {
            String newPrefix = prefix + (isLast ? "    " : "│   ");
            boolean lastChild = (i == children.size() - 1);
            generateTreeString(children.get(i), newPrefix, lastChild, builder);
        }
    }
    
    public List<Node> find(Predicate<Node> condition) {
        List<Node> results = new ArrayList<>();
        searchInternal(this, condition, results);
        return results;
    }

    private void searchInternal(Node node, Predicate<Node> condition, List<Node> results) {
        if (condition.test(node)) {
            results.add(node);
        }
        
        // Çocukları doğrudan döngüyle gezmek Stream'den daha hızlıdır
        List<Node> children = node.getChildren();
        for (int i = 0; i < children.size(); i++) {
            searchInternal(children.get(i), condition, results);
        }
    }

    // Kullanım kolaylığı sağlayan "Wrapper" metotlar
    public List<Node> findByName(String name) {
        return find(n -> n.getName().equalsIgnoreCase(name));
    }

	public <T> List<T> findByClass(Class<T> clazz) {
        @SuppressWarnings("unchecked")
		List<T> found = (List<T>) find(clazz::isInstance);
        return found;
    }
	
	public List<Node> findInArea(Hitbox area) {
		List<Node> result = new ArrayList<>();
		
		for (Node child : getChildren()) {
			if (child.getGlobalAABB() != null && child.getGlobalAABB().intersects(area)) {
				result.add(child);
			}
	    }
	    
	    // Eğer çocuklarda yoksa, derinlemesine (recursive) ara
	    for (Node child : getChildren()) {
	        List<Node> childResult = child.findInArea(area);
	        result.addAll(childResult);
	    }
	    
	    return result;
	}
	
	/**
	 * İsme göre ilk bulduğu düğümü döndürür. Hiç bulamazsa null döner.
	 */
	public Node findFirstChild(String name) {
	    // Önce doğrudan çocuklarına bak (Hız için)
	    for (Node child : getChildren()) {
	        if (child.getName().equalsIgnoreCase(name)) {
	            return child;
	        }
	    }
	    
	    // Eğer çocuklarda yoksa, derinlemesine (recursive) ara
	    for (Node child : getChildren()) {
	        Node found = child.findFirstChild(name);
	        if (found != null) return found;
	    }
	    
	    return null;
	}

	/**
	 * Belirli bir Class tipindeki ilk düğümü döndürür.
	 */
	@SuppressWarnings("unchecked")
	public <T extends Node> T findFirstChildOfClass(Class<T> clazz) {
	    // Önce doğrudan çocuklara bak
	    for (Node child : getChildren()) {
	        if (clazz.isInstance(child)) {
	            return (T) child;
	        }
	    }
	    
	    // Derinlemesine ara
	    for (Node child : getChildren()) {
	        T found = child.findFirstChildOfClass(clazz);
	        if (found != null) return found;
	    }
	    
	    return null;
	}
	
	public void sendDisposeAllNodes(Node parent) {
	    for (Node child : parent.children) {
	        sendDisposeAllNodes(child);
	    }
	    parent.dispose();
	}
	
	public void sendReloadAllNodes(Node parent) {
	    for (Node child : parent.children) {
	    	sendReloadAllNodes(child);
	    }
	    parent.reloadNode();
	}
	
	public String getName() {
		return new String(name);
	}
	
	public void setName(String name) {
		this.name = name;
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

	public boolean isAnchoed() {
		return this.anchored;
	}

	public void setAnchored(boolean isStatic) {
		this.anchored = isStatic;
	}

	// --- Lifecycle Hooks (3. Madde) ---
    // Alt sınıflar isterse bunları override edebilir
    protected void onAdded() {} 
    protected void onRemoved() {}
    protected void reloadNode() {}
    protected void dispose() {}
    
    public Node copy() {
        try {
            // 1. Yüzeysel kopya
            Node newNode = (Node) super.clone();

            // 2. Parent bağını kopar
            newNode.parent = null;

            // 3. Hitbox'ı kopyala
            if (this.aabb != null) {
                newNode.aabb = new Hitbox(
                    this.aabb.x, 
                    this.aabb.y, 
                    this.aabb.width, 
                    this.aabb.height
                );
            }

            // --- KRİTİK NOKTA ---
            // clear() yerine yeni bir liste örneği oluşturuyoruz!
            // ArrayList kullandığını varsayıyorum, kendi liste tipine göre değiştir:
            newNode.children = new ArrayList<>(); 
            
            // Şimdi orijinal liste (this.children) hala dolu, güvenle dönebiliriz:
            for (Node child : this.children) {
                newNode.addChild(child.copy()); 
            }

            return newNode;

        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
            return null;
        }
    }

	@Override
	public String toString() {
		return "[" + this.getName() + "-X=" + this.x + "-Y=" + this.y + "]";
	}
}