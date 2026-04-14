package me.ramazanenescik04.diken.game;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.Setting.EnumSettingType;
import me.ramazanenescik04.diken.game.SettingCategory.SettingCategoryHelper;
import me.ramazanenescik04.diken.game.world.World;
import me.ramazanenescik04.diken.gui.hitbox.Hitbox;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

// Java 25 ile gelen özellikleri kullanabiliriz ama temel yapı sağlam olmalı.
/**
 * Represents the `Node` type within the DikenEngine `game` package.
 */
public abstract class Node implements java.io.Serializable, Cloneable {
	private static final long serialVersionUID = -4123363831057244200L;
	
	// Hiyerarşi
    protected Node parent;
    protected List<Node> children = new ArrayList<>();
    protected UUID netId = UUID.randomUUID();
    
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
    protected boolean removed = false;

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
    
    public void insertChild(int index, Node child) {
    	if (child == null || child == this || this.isDescendantOf(child)) {
    		return;
    	}
    	
    	index = Math.max(0, Math.min(index, children.size()));
    	
    	if (child.parent == this) {
    		int currentIndex = children.indexOf(child);
    		if (currentIndex < 0) {
    			return;
    		}
    		if (currentIndex < index) {
    			index--;
    		}
    		children.remove(currentIndex);
    		children.add(Math.max(0, Math.min(index, children.size())), child);
    		return;
    	}
    	
    	if (child.parent != null) {
    		child.parent.children.remove(child);
    		child.onRemoved();
    		child.parent = null;
    	}
    	
    	child.parent = this;
    	children.add(index, child);
    	child.onAdded();
    }
    
    public int getChildIndex(Node child) {
    	return children.indexOf(child);
    }
    
    public boolean isDescendantOf(Node other) {
    	if (other == null) {
    		return false;
    	}
    	
    	Node current = this.parent;
    	while (current != null) {
    		if (current == other) {
    			return true;
    		}
    		current = current.parent;
    	}
    	
    	return false;
    }

    public void removeChild(Node child) {
        if (children.remove(child)) {
            child.onRemoved(); // 3. Lifecycle Event
            child.parent = null;
        }
    }
    
    public void replaceChild(Node oldChild, Node newChild) {
        int index = children.indexOf(oldChild);
        if (index < 0) {
            return;
        }
        
        oldChild.onRemoved();
        oldChild.parent = null;
        newChild.parent = this;
        children.set(index, newChild);
        newChild.onAdded();
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
        for (int i = 0; i < children.size(); i++) {
        	Node child = children.get(i);
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
    	var clonedList = new ArrayList<>(children);
        for (Node child : clonedList) {
        	if (child.isRemoved()) {
        		children.remove(child);
        		continue;
        	}
        	
            child.update(world, engine);
        }
    }

    // AABB'yi ayarlamak için
    public void setAABB(int width, int height) {
        this.aabb = new Hitbox(0, 0, width, height); // Local coordinates
    }
    
    public boolean hasAABB() {
    	return this.aabb != null;
    }
    
    public int getAABBWidth() {
    	return this.aabb != null ? this.aabb.width : 0;
    }
    
    public int getAABBHeight() {
    	return this.aabb != null ? this.aabb.height : 0;
    }
    
    public void setAABBSize(int width, int height) {
    	if (this.aabb == null) {
    		return;
    	}
    	
    	this.aabb.width = Math.max(1, width);
    	this.aabb.height = Math.max(1, height);
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
	
	public Node findFirstChildByNetId(UUID netId) {
	    if (netId == null) {
	        return null;
	    }
	    
	    if (netId.equals(this.netId)) {
	        return this;
	    }
	    
	    for (Node child : getChildren()) {
	        Node found = child.findFirstChildByNetId(netId);
	        if (found != null) {
	            return found;
	        }
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
	
	public UUID getNetId() {
		return this.netId;
	}
	
	public void setNetId(UUID netId) {
		this.netId = netId != null ? netId : UUID.randomUUID();
	}
	
	public void setName(String name) {
		this.name = name;
	}
    
    public void onCollision(Node other) {
    }
    
    // TODO: BURASI BOZUK!
    public static void resolveCollision(Node a, Node b) {
        Hitbox boxA = a.getGlobalAABB();
        Hitbox boxB = b.getGlobalAABB();
        if (boxA == null || boxB == null) return;

        // Çakışma miktarını hesapla
        float overlapX = Math.min(boxA.x + boxA.width, boxB.x + boxB.width) - Math.max(boxA.x, boxB.x);
        float overlapY = Math.min(boxA.y + boxA.height, boxB.y + boxB.height) - Math.max(boxA.y, boxB.y);

        if (overlapX <= 0 || overlapY <= 0) return; // Çarpışma yok

        boolean aAnchored = a.isAnchored();
        boolean bAnchored = b.isAnchored();

        if (aAnchored && bAnchored) return; // İkisi de sabit, hiçbir şey yapma

        // En küçük çakışma eksenini seç ve ayır
        if (overlapX < overlapY) {
            // X ekseninde ayır
            if (!aAnchored && !bAnchored) {
                // İkisi de hareketli, yarı yarıya ayır
                float half = overlapX / 2.0f;
                if (boxA.x < boxB.x) {
                    a.x -= half;
                    b.x += half;
                } else {
                    a.x += half;
                    b.x -= half;
                }
            } else if (!aAnchored) {
                // Sadece A hareketli, B sabit
                if (boxA.x < boxB.x) {
                    a.x -= overlapX;
                } else {
                    a.x += overlapX;
                }
            } else if (!bAnchored) {
                // Sadece B hareketli, A sabit
                if (boxB.x < boxA.x) {
                    b.x -= overlapX;
                } else {
                    b.x += overlapX;
                }
            }
        } else {
            // Y ekseninde ayır
            if (!aAnchored && !bAnchored) {
                // İkisi de hareketli, yarı yarıya ayır
                float half = overlapY / 2.0f;
                if (boxA.y < boxB.y) {
                    a.y -= half;
                    b.y += half;
                } else {
                    a.y += half;
                    b.y -= half;
                }
            } else if (!aAnchored) {
                // Sadece A hareketli, B sabit
                if (boxA.y < boxB.y) {
                    a.y -= overlapY;
                } else {
                    a.y += overlapY;
                }
            } else if (!bAnchored) {
                // Sadece B hareketli, A sabit
                if (boxB.y < boxA.y) {
                    b.y -= overlapY;
                } else {
                    b.y += overlapY;
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

	public boolean isAnchored() {
		return this.anchored;
	}

	public void setAnchored(boolean isStatic) {
		this.anchored = isStatic;
	}
	
	public void removeNode() {
		this.removed = true;
	}
	
	public boolean isRemoved() {
		return this.removed;
	}
	
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("default", "Node", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(8, 1));
		var cat = SettingCategoryHelper.getOrCreateCategory(key, () -> this.generateDefaultSettings(key));
		
		var list = new ArrayList<SettingCategory>();
		list.add(cat);
		return list;
	}
	
	private SettingCategory generateDefaultSettings(SettingCategory.SettingKey key) {
		var s = SettingCategory.createSettingCategory(key)
				.addSetting(new Setting<String>("Name", name, String.class, EnumSettingType.TEXT_FIELD).addChangeListener(this::setName))
				.addSetting(new Setting<Integer>("X", x, Integer.class, EnumSettingType.TEXT_FIELD).addChangeListener(val -> this.x = val))
				.addSetting(new Setting<Integer>("Y", y, Integer.class, EnumSettingType.TEXT_FIELD).addChangeListener(val -> this.y = val))
				.addSetting(new Setting<Boolean>("Visible", visible, Boolean.class, EnumSettingType.CHECK_BOX).addChangeListener(val -> this.visible = val))
				.addSetting(new Setting<Boolean>("Debug Renderer", debug, Boolean.class, EnumSettingType.CHECK_BOX).addChangeListener(this::setDebugRenderer))
				.addSetting(new Setting<Boolean>("Solid", solid, Boolean.class, EnumSettingType.CHECK_BOX).addChangeListener(this::setSolid))
				.addSetting(new Setting<Boolean>("Anchored", anchored, Boolean.class, EnumSettingType.CHECK_BOX).addChangeListener(this::setAnchored))
				.addSetting(new Setting<Integer>("Color", color, Integer.class, EnumSettingType.COLOR_PICKER).addChangeListener(val -> this.color = val));
		
		if (this.aabb != null) {
			s.addSetting(new Setting<Integer>("Hitbox Width", aabb.width, Integer.class, EnumSettingType.TEXT_FIELD).addChangeListener(val -> {
				if (aabb != null) aabb.width = val;
			}))
			.addSetting(new Setting<Integer>("Hitbox Height", aabb.height, Integer.class, EnumSettingType.TEXT_FIELD).addChangeListener(val -> {
				if (aabb != null) aabb.height = val;
			}))
			.addSetting(new Setting<Integer>("Hitbox Offset X", aabb.x, Integer.class, EnumSettingType.TEXT_FIELD).addChangeListener(val -> {
				if (aabb != null) aabb.x = val;
			}))
			.addSetting(new Setting<Integer>("Hitbox Offset Y", aabb.y, Integer.class, EnumSettingType.TEXT_FIELD).addChangeListener(val -> {
				if (aabb != null) aabb.y = val;
			}));
		}
		
		return s;
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
