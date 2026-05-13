package me.ramazanenescik04.diken.game.world;

import java.awt.Point;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.EnumResource;
import me.ramazanenescik04.diken.resource.FrameBitmapPool;
import me.ramazanenescik04.diken.resource.IResource;
import me.ramazanenescik04.diken.gui.component.GuiComponent;
import me.ramazanenescik04.diken.gui.component.Panel;
import me.ramazanenescik04.diken.gui.hitbox.Hitbox;
import me.ramazanenescik04.diken.gui.hitbox.IHitbox;

/**
 * Represents the `World` type within the DikenEngine `game.world` package.
 */
public class World extends Panel implements Cloneable {
    private static final long serialVersionUID = 1L;
    private static final int MAX_COLLISION_PASSES = 6;
    public static final int WORLD_IO_VERSION = 1;

    // Her şeyin bağlı olduğu ana düğüm (Sahne)
    public final Node root;
    public String gameName = "Game";
    public long lastUpdateTime = System.currentTimeMillis();
    
    public transient Map<String, IResource> resources;
    
    public transient Point camera = new Point(0, 0);
    private float zoom = 1.0f;

    public World(String gameName, Node rootNode, int width, int height) {
    	super(0, 0, width, height);
    	this.gameName = gameName;
    	this.resources = new ConcurrentHashMap<>();
    	this.resources.put("empty", Bitmap.empty);
        // Root isimsiz ve render edilmeyen bir container'dır
    	if (rootNode == null) {
    		this.root = new Workspace(gameName);
    	} else {
    		this.root = rootNode;
    	}
    }
    
    public World(String gameName, int width, int height) {
    	this(gameName, null, width, height);
    }

    // --- Node Yönetimi ---

    public void addNode(Node node) {
        root.addChild(node);
    }

    public void removeNode(Node node) {
        root.removeChild(node);
    }

    public Node getNodeByName(String name) {
        return root.findFirstChild(name);
    }
    
    public Node getNodeByNetId(UUID netId) {
        return root.findFirstChildByNetId(netId);
    }
    
    public List<Node> getAllNodes() {
        List<Node> nodes = new ArrayList<>();
        collectNodes(root, nodes);
        return nodes;
    }
    
    private void collectNodes(Node node, List<Node> nodes) {
        nodes.add(node);
        for (Node child : node.getChildren()) {
            collectNodes(child, nodes);
        }
    }

    // --- Render ---

    @Override
    public Bitmap render() {
        float activeZoom = Math.max(0.1f, this.zoom);
        int sceneWidth = Math.max(1, Math.round(width / activeZoom));
        int sceneHeight = Math.max(1, Math.round(height / activeZoom));
        Bitmap sceneBitmap = FrameBitmapPool.newBitmap(sceneWidth, sceneHeight);
        Hitbox viewport = new Hitbox(0, 0, sceneWidth, sceneHeight);

        if (this.drawX) {
            sceneBitmap.box(0, 0, sceneWidth - 1, sceneHeight - 1, 0xffffffff);
            sceneBitmap.drawLine(0, 0, sceneWidth, sceneHeight, 0xffffffff, 1);
            sceneBitmap.drawLine(sceneWidth, 0, 0, sceneHeight, 0xffffffff, 1);
        }

        int oldX = root.x;
        int oldY = root.y;
        
        root.x = -(int)camera.x;
        root.y = -(int)camera.y;

        root.draw(sceneBitmap, viewport);

        root.x = oldX;
        root.y = oldY;
        
        Bitmap worldBitmap;
        if (sceneWidth == width && sceneHeight == height) {
            worldBitmap = sceneBitmap;
        } else {
            worldBitmap = sceneBitmap.resize(width, height);
        }

        List<GuiComponent> compoments = this.getCompoments();
        for (GuiComponent compoment : compoments) {
            worldBitmap.draw(compoment.render(), compoment.x, compoment.y);
        }

        return worldBitmap;
    }

    // --- Update & Collision ---

    public void tick(DikenEngine engine) {
    	super.tick(engine);

        // Hareket/kuvvet uygulandıktan sonra çarpışma çözmek, frame sonunda
        // nesnenin duvarın içinde kalmasını engeller.
        root.update(this, engine);

    	checkCollisions(engine);
    }

    private void checkCollisions(DikenEngine engine) {
        List<Node> collidables = new ArrayList<>();
        collectCollidableNodes(root, collidables);
        
        for (int pass = 0; pass < MAX_COLLISION_PASSES; pass++) {
        	boolean hadIntersection = false;
        	
	        for (int i = 0; i < collidables.size(); i++) {
	            Node a = collidables.get(i);
	            
	            for (int j = i + 1; j < collidables.size(); j++) {
	                Node b = collidables.get(j);

	                IHitbox boxA = a.getGlobalAABB();
	                Hitbox boxB = b.getGlobalAABB();

	                if (boxA != null && boxB != null && boxA.intersects(boxB)) {
	                	hadIntersection = true;
	                    
	                    a.onCollision(b);
	                    b.onCollision(a);

	                    if (a.isSolid() && b.isSolid()) {
	                    	Node.resolveCollision(a, b);
	                    }
	                }
	            }
	        }
	        
	        if (!hadIntersection) {
	        	break;
	        }
        }
    }

    // Yardımcı metod: Ağacı gezip hitbox'ı olanları bulur
    private void collectCollidableNodes(Node current, List<Node> list) {
        if (current.getGlobalAABB() != null) {
            list.add(current);
        }
        for (Node child : current.getChildren()) {
            collectCollidableNodes(child, list);
        }
    }

    // --- Setter/Getter ---
    
    public void setCamera(Point camera) {
        this.camera = camera;
    }

    public Point getCamera() {
        return camera;
    }
    
    public float getZoom() {
    	return zoom;
    }
    
    public void setZoom(float zoom) {
    	this.zoom = Math.max(0.1f, zoom);
    }
    
    public static void saveWorld(World theWorld, File outputFile) throws IOException {
    	try (ObjectOutputStream outStream = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(outputFile)))) {
    		writeWorld(theWorld, outStream);
		}
    }
    
    public static byte[] saveWorldToBytes(World theWorld) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        try (ObjectOutputStream outStream = new ObjectOutputStream(new GZIPOutputStream(stream))) {
            writeWorld(theWorld, outStream);
        }
        return stream.toByteArray();
    }
    
    public static World loadWorld(File outputFile) throws IOException, ReflectiveOperationException {
    	try (ObjectInputStream outStream = new ObjectInputStream(new GZIPInputStream(new FileInputStream(outputFile)))) {
    		return readWorld(outStream);
		}
    }
    
    public static World loadWorldFromBytes(byte[] data) throws IOException, ReflectiveOperationException {
        try (ObjectInputStream outStream = new ObjectInputStream(new GZIPInputStream(new ByteArrayInputStream(data)))) {
            return readWorld(outStream);
        }
    }
    
    private static void writeWorld(World theWorld, ObjectOutputStream outStream) throws IOException {
        outStream.writeUTF("DikenEngine-WorldFile");
        outStream.writeInt(WORLD_IO_VERSION);
        
        outStream.writeUTF(theWorld.gameName);
        outStream.writeLong(System.currentTimeMillis());
        
        outStream.writeInt(theWorld.resources.size());
        for (Map.Entry<String, IResource> entry : theWorld.resources.entrySet()) {
            outStream.writeUTF(entry.getKey());
            IResource resource = entry.getValue();
            outStream.writeUTF(resource.getClass().getName());
            
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            resource.saveResource(new DataOutputStream(stream));
            outStream.writeInt(stream.size());
            outStream.write(stream.toByteArray());
        }
        
        theWorld.root.sendDisposeAllNodes(theWorld.root);
        outStream.writeObject(theWorld.root);
    }
    
    private static World readWorld(ObjectInputStream outStream) throws IOException, ReflectiveOperationException {
        String signature = outStream.readUTF();
        if (!signature.equals("DikenEngine-WorldFile")) {
            throw new IOException("DikenENngine World Dosyası Değil!");
        }
        
        int worldVersion = outStream.readInt();
        if (worldVersion < WORLD_IO_VERSION) {
        	throw new IOException("Dünya yükleme sistemi, eski dünya dosyaları yüklemeyi desteklemiyor!");
        }
    
        String gameName = outStream.readUTF();
        long lastUpdateTime = outStream.readLong();
        int resourceLenght = outStream.readInt();
        
        Map<String, IResource> resources = new ConcurrentHashMap<>();
        
        for (int i = 0; i < resourceLenght; i++) {
            String key = outStream.readUTF();
            String className = outStream.readUTF();
            int lenght = outStream.readInt();
            byte[] data = new byte[lenght];
            outStream.readFully(data);
            
            var in = new ByteArrayInputStream(data);
            var resource = IResource.loadResource(new DataInputStream(in), className);
            
            resources.put(key, resource);
        }
        
        try {
            Node rootNode = (Node) outStream.readObject();
            rootNode.sendReloadAllNodes(rootNode);
            
            World world = new World(gameName, rootNode, 1, 1);
            world.lastUpdateTime = lastUpdateTime;
            
            resources.values().forEach(IResource::reload);
            
            world.resources = resources;
            return world;
        } catch (ClassNotFoundException e) {
            throw new ReflectiveOperationException(e);
        }
    }
    
	@SuppressWarnings("unchecked")
	public <T extends IResource> T getResource(String key, EnumResource expectedType) {
        IResource res = resources.get(key);

        // Kaynak var mı ve tipi bizim beklediğimiz tip mi?
        if (res != null && res.resourceIs(expectedType)) {
            return (T) res;
        }
        
        return null;
    }
    
    public World addResource(String key, IResource resource) {
        if (key == null || resource == null) {
            throw new IllegalArgumentException("Key veya Resource null olamaz!");
        }

        // Eğer aynı isimde başka bir şey varsa uyarabilir veya üzerine yazabilirsin
        if (resources.containsKey(key)) {
            System.out.println("Uyarı: " + key + " zaten kayıtlı, üzerine yazılıyor...");
        }

        resources.put(key, resource);
        return this;
    }
    
    public World removeResource(String key) {
        IResource res = resources.remove(key); // remove() sildiği objeyi geri döndürür
        
        if (res != null) {
        	res.disponse();
            System.out.println(key + " başarıyla kaldırıldı.");
        }
        return this;
    }

    public void clearAllResources() {
        // Önce hepsini bellekten boşalt (varsa unload metodun)
        resources.values().forEach(IResource::disponse);
        
        resources.clear();
    }
    
    public World copy() {
    	Node copyRoot;
    	if (root == null)
    		copyRoot = new Workspace(new String(this.gameName));
    	else
    		copyRoot = this.root.copy();
    		
    	copyRoot.sendReloadAllNodes(copyRoot);
    	World copyWorld = new World(new String(this.gameName), copyRoot, this.getWidth(), this.getHeight());
		copyWorld.resources = new ConcurrentHashMap<String, IResource>(this.resources);
		copyWorld.resources.values().forEach(IResource::reload);
		copyWorld.lastUpdateTime = System.currentTimeMillis();
		copyWorld.zoom = this.zoom;
		return copyWorld;
    }
}
