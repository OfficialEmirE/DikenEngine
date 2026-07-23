package me.ramazanenescik04.diken.game;

import java.awt.Point;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.event.Event;
import me.ramazanenescik04.diken.game.setting.Setting;
import me.ramazanenescik04.diken.game.setting.SettingCategory;
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
public abstract class Node implements Cloneable {
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
	public final Event OnParentChangedDescendant = new Event();
	
	// Default listeners
	public final Event OnUpdate = new Event();
	public final Event OnDispose = new Event();
	public final Event OnReload = new Event();
	public final Event OnDestroy = new Event();
	public final Event OnParentChanged = new Event();
	public final Event OnPropertyChanged = new Event();
	
	// Render listeners
	public final Event OnPreRender = new Event();
    public final Event OnPostRender = new Event();
	
	// Hiyerarşi
    protected Node parent;
    protected List<Node> children = new ObservableList<>();
    protected final UUID netId;
    
    // Temel Özellikler
    protected String name;
    
    protected boolean debug = false;
    protected boolean archiveable = true; // Kaydedilebilir mi?
    protected boolean removed = false;
    protected int zIndex = 0;

    // Constructor
    public Node() {
    	this("BaseNode");
    }
    
    public Node(String name) {
        this.name = name;
        this.netId = UUID.randomUUID();
    }
    
    public Node(DataInputStream in) throws IOException {
        this.netId = UUID.fromString(in.readUTF());
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
    
    public void setParent(Node newParent) {
    	OnPropertyChanged.FireEvent("Parent", this.parent, newParent);
    	OnParentChanged.FireEvent(this.parent, newParent);
    	notifyAncestors(OnParentChangedDescendant, this.parent, newParent);
    	if (newParent == null) {
    		this.parent.removeChild(this);
    	} else {
    		newParent.addChild(this);
    	}
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
    	List<Node> sortedChildren = new ArrayList<>(children);
    	sortedChildren.sort(Comparator.comparingInt(Node::getZIndex));
    	
		for (int i = 0; i < sortedChildren.size(); i++) {
            Node child = sortedChildren.get(i);
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
    
    public String getFullName() {
    	String fullName = "";
    	var current = this;
        while (current != null) {
        	if (fullName.isEmpty()) {
        		fullName = current.getName();
        	} else {
        		fullName = current.getName() + "." + fullName;
        	}
        	current = current.parent;
        }
        
        return fullName;
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
	
	public List<Node> findByNetId(UUID target) {
		return find(n -> n.getNetId().equals(target));
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
		return (name);
	}
	
	public UUID getNetId() {
		return this.netId;
	}
	
	public void setName(String name) {
		OnPropertyChanged.FireEvent("name", this.name, name);
		this.name = name;
	}
    
    public void onCollision(Node other) {
    }
    
    public Node getRootNode() {
        Node current = this;
        while (current.getParent() != null) {
            current = current.getParent();
        }
        return current;
    }
    
    public static void resolveCollision(Instance a, Instance b) {
        Hitbox boxA = a.getGlobalAABB();
        Hitbox boxB = b.getGlobalAABB();
        if (boxA == null || boxB == null) return;

        // Overlap hesapla
        int aL = boxA.getX(), aR = aL + boxA.getWidth();
        int aT = boxA.getY(), aB = aT + boxA.getHeight();
        int bL = boxB.getX(), bR = bL + boxB.getWidth();
        int bT = boxB.getY(), bB = bT + boxB.getHeight();

        int overlapX = Math.min(aR, bR) - Math.max(aL, bL);
        int overlapY = Math.min(aB, bB) - Math.max(aT, bT);

        if (overlapX <= 0 || overlapY <= 0) return;

        boolean aAnchored = a.isAnchored();
        boolean bAnchored = b.isAnchored();
        if (aAnchored && bAnchored) return;

        // SAT: en küçük overlap ekseninde resolve et
        // Eşit durumda X tercih edilir (arbitrary ama tutarlı)
        if (overlapX <= overlapY) {
            // X ekseninde resolve
            // A'nın sağ kenarı mı B'nin sol kenarına daha yakın?
            int fromLeft  = aR - bL; // A sağdan B'ye girmiş
            int fromRight = bR - aL; // A soldan B'ye girmiş

            int pushX;
            int dirA, dirB; // +1 veya -1

            if (fromLeft < fromRight) {
                // A sağdan çarpmış → A'yı sola, B'yi sağa
                pushX = fromLeft;
                dirA = -1;
                dirB = +1;
            } else {
                // A soldan çarpmış → A'yı sağa, B'yi sola
                pushX = fromRight;
                dirA = +1;
                dirB = -1;
            }

            if (!aAnchored && !bAnchored) {
                int halfA = pushX / 2;
                int halfB = pushX - halfA;
                a.setX(a.getX() + dirA * halfA);
                b.setX(b.getX() + dirB * halfB);
            } else if (!aAnchored) {
                a.setX(a.getX() + dirA * pushX);
            } else {
                b.setX(b.getX() + dirB * pushX);
            }

        } else {
            // Y ekseninde resolve
            int fromTop    = aB - bT; // A yukarıdan girmiş
            int fromBottom = bB - aT; // A aşağıdan girmiş

            int pushY;
            int dirA, dirB;

            if (fromTop < fromBottom) {
                // A yukarıdan çarpmış → A'yı yukarı, B'yi aşağı
                pushY = fromTop;
                dirA = -1;
                dirB = +1;
            } else {
                // A aşağıdan çarpmış → A'yı aşağı, B'yi yukarı
                pushY = fromBottom;
                dirA = +1;
                dirB = -1;
            }

            if (!aAnchored && !bAnchored) {
                int halfA = pushY / 2;
                int halfB = pushY - halfA;
                a.setY(a.getY() + dirA * halfA);
                b.setY(b.getY() + dirB * halfB);
            } else if (!aAnchored) {
                a.setY(a.getY() + dirA * pushY);
            } else {
                b.setY(b.getY() + dirB * pushY);
            }
        }
    }

    public boolean isDebugRenderer() {
		return debug;
	}

	public void setDebugRenderer(boolean debug) {
		OnPropertyChanged.FireEvent("debug", this.debug, debug);
		this.debug = debug;
	}
	
	public boolean isArchiveable() {
		return archiveable;
	}
	
	public void setArchiveable(boolean archiveable) {
		OnPropertyChanged.FireEvent("archiveable", this.archiveable, archiveable);
		this.archiveable = archiveable;
	}
	
	public void removeNode() {
		OnPropertyChanged.FireEvent("removed", this.removed, true);
		this.removed = true;
		OnDestroy.FireEvent();
	}
	
	public boolean isRemoved() {
		return this.removed;
	}
	
    public void setZIndex(int zIndex) {
    	OnPropertyChanged.FireEvent("zIndex", this.zIndex, zIndex);
        this.zIndex = zIndex;
    }
	
	public int getZIndex() {
        return this.zIndex;
    }
	
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("default", "Node", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(8, 1));
		
		var list = new ArrayList<SettingCategory>();
		list.add(this.generateDefaultSettings(key));
		return list;
	}
	
	private SettingCategory generateDefaultSettings(SettingCategory.SettingKey key) {
		var s = SettingCategory.createSettingCategory(key)
				.addSetting(new Setting<>("ClassName", getClass().getSimpleName(), String.class, EnumSettingType.UNKNOWN).setChangeable(false))
				.addSetting(new Setting<>("Name", name, String.class, EnumSettingType.TEXT_FIELD).addChangeListener(this::setName))
				.addSetting(new Setting<>("Parent", parent, Node.class, EnumSettingType.OBJECT_SELECT).addChangeListener(this::setParent))
				.addSetting(new Setting<>("Debug Renderer", debug, Boolean.class, EnumSettingType.CHECK_BOX).addChangeListener(this::setDebugRenderer))
				.addSetting(new Setting<>("Archiveable", archiveable, Boolean.class, EnumSettingType.CHECK_BOX).addChangeListener(this::setArchiveable))
				.addSetting(new Setting<>("Z Index", zIndex, Integer.class, EnumSettingType.TEXT_FIELD).addChangeListener(this::setZIndex));
		return s;
	}
	
	public void saveNodeData(DataOutputStream out) throws IOException {
		out.writeUTF(netId.toString());
		out.writeUTF(name);
		out.writeBoolean(debug);
		out.writeBoolean(archiveable);
		out.writeBoolean(removed);
		out.writeInt(zIndex);
	}
	
	public void loadNodeData(DataInputStream in) throws IOException {
		this.name = in.readUTF();
		this.debug = in.readBoolean();
		this.archiveable = in.readBoolean();
		this.removed = in.readBoolean();
		this.zIndex = in.readInt();
	}
	
	public static void exportNode(File file, Node node) throws IOException {
		try (var out = new DataOutputStream(new java.io.FileOutputStream(file))) {
			out.writeUTF("DikenEngine-NodeFile");
			
			saveNode(node, out);
		}
	}
	
	public static Node importNode(File file) throws IOException {
		try (var in = new DataInputStream(new java.io.FileInputStream(file))) {
			String signature = in.readUTF();
	        if (!signature.equals("DikenEngine-NodeFile")) {
	            throw new IOException("DikenEngine NodeFile Dosyası Değil!");
	        }
	        
	        return loadNode(in);
		}
	}
	
	public static Node loadNode(DataInputStream inStream) throws IOException {
        String className = inStream.readUTF();
        int childCount = inStream.readInt();
        
        Node node;
        try {
            Class<?> clazz = Class.forName(className);
            node = (Node) clazz.getConstructor(DataInputStream.class).newInstance(inStream);
        } catch (Exception e) {
            throw new IOException("Node sınıfı yüklenemedi: " + className, e);
        }
        
        // Çocukları recursive yükle
        for (int i = 0; i < childCount; i++) {
            Node child = loadNode(inStream);
            node.addChild(child); // ya da node.children.add(child)
        }
        
        return node;
    }
	
	public static void saveNode(Node current, DataOutputStream outStream) throws IOException {
		List<Node> filteredChildren = current.children.stream()
	            .filter(child -> child.isArchiveable())
	            .toList();
		
    	outStream.writeUTF(current.getClass().getName());
    	outStream.writeInt(filteredChildren.size());
    	current.saveNodeData(outStream);
    	
    	for (Node node : filteredChildren) {
    		saveNode(node, outStream);
    	}
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
            	if (!child.isArchiveable())
            		continue;
            	
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
