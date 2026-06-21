package me.ramazanenescik04.diken.game;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.event.Event;
import me.ramazanenescik04.diken.gui.hitbox.Hitbox;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;
import me.ramazanenescik04.diken.tools.ListAdapter;
import me.ramazanenescik04.diken.tools.ObservableList;

// Java 25 ile gelen özellikleri kullanabiliriz ama temel yapı sağlam olmalı.
/**
 * Represents the `Node` type within the DikenEngine `game` package.
 */
public abstract class Node implements java.io.Serializable, Cloneable {
	private static final long serialVersionUID = -4123363831057244200L;
	
	// Child listeners
	public final Event OnAddChild = new Event();
	public final Event OnRemoveChild = new Event();
	public final Event OnInsertChild = new Event();
	public final Event OnReplaceChild = new Event();
	
	// Descendant listeners
	public final Event OnAddDescendant = new Event();
	public final Event OnRemoveDescendant = new Event();
	public final Event OnInsertDescendant = new Event();
	public final Event OnReplaceDescendant = new Event();
	
	// Default listeners
	public final Event OnUpdate = new Event();
	public final Event OnDispose = new Event();
	public final Event OnReload = new Event();
	public final Event OnDestroy = new Event();
	
	// Render listeners
	public final Event OnPreRender = new Event();
    public final Event OnPostRender = new Event();
	
	// Hiyerarşi
    protected Node parent;
    protected List<Node> children = new ObservableList<>();
    protected UUID netId = UUID.randomUUID();
    
    // Temel Özellikler
    protected String name;
    
    protected boolean debug = false;
    protected boolean archiveable = true; // Kaydedilebilir mi?
    protected boolean removed = false;

    // Constructor
    public Node() {
    	this.name = "BaseNode";
    }
    
    public Node(String name) {
        this.name = name;
    }

    // --- Hiyerarşi Yönetimi ---
    
    public void setListAdapter(ListAdapter<Node> l) {
    	((ObservableList<Node>) children).setListAdapter(l);
    }

    public void addChild(Node child) {    	
    	OnAddChild.FireEvent(child);
    	
        if (child.parent != null) {
            child.parent.removeChild(child); // Eski ailesinden kopar
        }
        child.parent = this;
        children.add(child);
        child.onAdded(); // 3. Lifecycle Event
        
        notifyAncestors(OnAddDescendant, child);
    }
    
    public void insertChild(int index, Node child) {
    	OnInsertChild.FireEvent(child, index); // 3. Lifecycle Event
    	notifyAncestors(OnInsertDescendant, child, index);
    	
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
    	OnRemoveChild.FireEvent(child); // 3. Lifecycle Event
    	
        if (children.remove(child)) {
            child.onRemoved(); // 3. Lifecycle Event
            child.parent = null;
        }
        
        notifyAncestors(OnRemoveDescendant, child);
    }
    
    private void notifyAncestors(Event eventName, Object... datas) {
        var current = this;
        while (current != null) {
        	eventName.FireEvent(datas);
        	current = current.parent;
        }
    }
    
    public void replaceChild(Node oldChild, Node newChild) {
    	OnReplaceChild.FireEvent(oldChild, newChild); // 3. Lifecycle Event
    	notifyAncestors(OnReplaceDescendant, oldChild, newChild);
    	
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

    protected boolean shouldRenderSelf(Hitbox viewport) {
    	if (isCameraIndependent() || viewport == null) {
    		return true;
    	}
    	
    	if (this instanceof Instance instance) {
    	    Hitbox globalBox = instance.getGlobalAABB();
    	    if (globalBox == null) {
    	        return true;
    	    }
    	    return globalBox.intersects(viewport);
    	}
    	
    	return true;
    }
    
    public void draw(Bitmap btp, Hitbox viewport) {
		for (int i = 0; i < children.size(); i++) {
            Node child = children.get(i);
            child.draw(btp, viewport);
        }
		
		OnPostRender.FireEvent();
	}
    
    protected int getRenderX() {
    	return getGlobalX();
    }
    
    protected int getRenderY() {
    	return getGlobalY();
    }
    
    protected boolean isCameraIndependent() {
    	return false;
    }

    // --- Collision & Logic ---

    public void update(World world, DikenEngine engine) {
        // Override edilebilir logic
    	OnUpdate.FireEvent();
        
    	// Çocukları güncelle
        for (int i = 0; i < this.children.size(); i++) {
        	Node child = this.children.get(i);
        	if (child.isRemoved()) {
        		children.remove(child);
        		continue;
        	}
        	
            child.update(world, engine);
        }
    }

    // Global pozisyonu bulmak için parent zincirini takip et
    public int getGlobalX() {
        if (parent instanceof Instance parentInstance) {
            return parentInstance.getGlobalX();
        }
        return 0;
    }

    public int getGlobalY() {
        if (parent instanceof Instance parentInstance) {
            return parentInstance.getGlobalY();
        }
        return 0;
    }
    
    public Point toPoint() {
		return new Point(getGlobalX(), getGlobalY());
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
	    
	    return null;
	}
	
	public List<Node> getDescendants() {
	    List<Node> descendants = new ArrayList<>();
	    for (Node child : getChildren()) {
	        descendants.add(child);
	        descendants.addAll(child.getDescendants());
	    }
	    return descendants;
	}
	
	public void sendDisposeAllNodes(Node parent) {
	    for (Node child : parent.children) {
	        sendDisposeAllNodes(child);
	    }
	    parent.OnDispose.FireEvent();
	    parent.dispose();
	}
	
	public void sendReloadAllNodes(Node parent) {
	    for (Node child : parent.children) {
	    	sendReloadAllNodes(child);
	    }
	    parent.OnReload.FireEvent();
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
    
    public static void resolveCollision(Instance a, Instance b) {
        Hitbox boxA = a.getGlobalAABB();
        Hitbox boxB = b.getGlobalAABB();
        if (boxA == null || boxB == null) return;

        int overlapX = Math.min(boxA.getX() + boxA.getX(), boxB.getX() + boxB.getWidth()) - Math.max(boxA.getX(), boxB.getX());
        int overlapY = Math.min(boxA.getY() + boxA.getY(), boxB.getY() + boxB.getHeight()) - Math.max(boxA.getY(), boxB.getY());

        if (overlapX <= 0 || overlapY <= 0) return;

        boolean aAnchored = a.isAnchored();
        boolean bAnchored = b.isAnchored();

        if (aAnchored && bAnchored) return;

        float centerAx = boxA.getX() + (boxA.getWidth() / 2.0f);
        float centerBx = boxB.getX() + (boxB.getWidth() / 2.0f);
        float centerAy = boxA.getY() + (boxA.getHeight() / 2.0f);
        float centerBy = boxB.getY() + (boxB.getHeight() / 2.0f);

        if (overlapX < overlapY) {
            if (!aAnchored && !bAnchored) {
                int moveA = overlapX / 2;
                int moveB = overlapX - moveA;
                if (centerAx < centerBx) {
                    a.x -= moveA;
                    b.x += moveB;
                } else {
                    a.x += moveB;
                    b.x -= moveA;
                }
            } else if (!aAnchored) {
                if (centerAx < centerBx) a.x -= overlapX;
                else a.x += overlapX;
            } else if (!bAnchored) {
                if (centerBx < centerAx) b.x -= overlapX;
                else b.x += overlapX;
            }
        } else {
            if (!aAnchored && !bAnchored) {
                int moveA = overlapY / 2;
                int moveB = overlapY - moveA;
                if (centerAy < centerBy) {
                    a.y -= moveA;
                    b.y += moveB;
                } else {
                    a.y += moveB;
                    b.y -= moveA;
                }
            } else if (!aAnchored) {
                if (centerAy < centerBy) a.y -= overlapY;
                else a.y += overlapY;
            } else if (!bAnchored) {
                if (centerBy < centerAy) b.y -= overlapY;
                else b.y += overlapY;
            }
        }
    }

    public boolean isDebugRenderer() {
		return debug;
	}

	public void setDebugRenderer(boolean debug) {
		this.debug = debug;
	}
	
	public boolean isArchiveable() {
		return archiveable;
	}
	
	public void setArchiveable(boolean archiveable) {
		this.archiveable = archiveable;
	}
	
	public void removeNode() {
		this.removed = true;
		OnDestroy.FireEvent();
	}
	
	public boolean isRemoved() {
		return this.removed;
	}
	
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("default", "Node", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(8, 1));
		
		var list = new ArrayList<SettingCategory>();
		list.add(this.generateDefaultSettings(key));
		return list;
	}
	
	private SettingCategory generateDefaultSettings(SettingCategory.SettingKey key) {
		var s = SettingCategory.createSettingCategory(key)
				.addSetting(new Setting<String>("Name", name, String.class, EnumSettingType.TEXT_FIELD).addChangeListener(this::setName))
				.addSetting(new Setting<Boolean>("Debug Renderer", debug, Boolean.class, EnumSettingType.CHECK_BOX).addChangeListener(this::setDebugRenderer))
				.addSetting(new Setting<Boolean>("Archiveable", archiveable, Boolean.class, EnumSettingType.CHECK_BOX).addChangeListener(val -> this.archiveable = val));
		return s;
	}

	// --- Lifecycle Hooks (3. Madde) ---
    // Alt sınıflar isterse bunları override edebilir
    protected void onAdded() {} 
    protected void onRemoved() {}
    protected void reloadNode() {}
    protected void dispose() {}
    
    public Node copy() {
    	if (!isArchiveable())
    		return null;
    	
        try {
            // 1. Yüzeysel kopya
            Node newNode = (Node) super.clone();

            // 2. Parent bağını kopar
            newNode.parent = null;
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
		return "[" + this.getName() + "]";
	}
}
