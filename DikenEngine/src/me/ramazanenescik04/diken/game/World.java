package me.ramazanenescik04.diken.game;

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
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.nodes.Folder;
import me.ramazanenescik04.diken.game.services.UIService;
import me.ramazanenescik04.diken.game.services.InputService;
import me.ramazanenescik04.diken.game.services.Lighting;
import me.ramazanenescik04.diken.game.services.Service;
import me.ramazanenescik04.diken.game.services.Workspace;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.EnumResource;
import me.ramazanenescik04.diken.resource.IResource;
import me.ramazanenescik04.diken.scripting.Script;
import me.ramazanenescik04.diken.gui.hitbox.Hitbox;
import me.ramazanenescik04.diken.gui.hitbox.IHitbox;
import me.ramazanenescik04.diken.renderer.FrameBitmapPool;

/**
 * Represents the `World` type within the DikenEngine `game.world` package.
 */
public class World implements Cloneable {
    private static final int MAX_COLLISION_PASSES = 6;
    public static final int WORLD_IO_VERSION = 1;

    public transient DikenEngine engine;
    
    private final Node root;
    public String gameName = "Game";
    public long lastUpdateTime = System.currentTimeMillis();
    
    public transient Map<String, IResource> resources;
    
    public transient Point camera = new Point(0, 0);
    private float zoom = 1.0f;

    public World(String gameName, Node rootNode) {
    	this.gameName = gameName;
    	this.resources = new ConcurrentHashMap<>();
    	this.resources.put("empty", Bitmap.empty);
        // Root isimsiz ve render edilmeyen bir container'dır
    	if (rootNode == null) {
    		this.root = new Folder(gameName);
    		initServices(this.root);
    	} else {
    		this.root = rootNode;
    	}
    }
    
    public World(String gameName) {
    	this(gameName, null);
    }
    
    private void initServices(Node root) {
    	root.addChild(new Workspace());
    	root.addChild(new Lighting());
    	root.addChild(new UIService());
    	root.addChild(new InputService());
    }

    // --- Node Yönetimi ---
    
    public void addOptionalService(Class<? extends Service> serviceClass) {
		if (getService(serviceClass) == null) {
			try {
				root.addChild(serviceClass.getDeclaredConstructor().newInstance());
				DikenEngine.log("Service successfully added: " + serviceClass.getName());
			} catch (Exception e) {
				DikenEngine.errorLog("Service addition failed: " + serviceClass.getName(), e);
			}
		} else {
			DikenEngine.log("Service already exists, skipping: " + serviceClass.getName());
		}
	}
    
    public <T extends Service> T getService(Class<T> serviceClass) {
		return root.findFirstChildOfClass(serviceClass);
	}
    
    @SuppressWarnings("unchecked")
	public <T extends Service> T getService(String serviceName) {
    	var node = root.findFirstChild(serviceName);
    	
    	if (node instanceof Service) {
    		return (T) node;
    	}
		return null;
	}

    public List<Service> getServices() {
		return root.findByClass(Service.class);
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

    public void render(Bitmap mainBitmap) {
    	var width = this.engine.getScaledWidth();
    	var height = this.engine.getScaledHeight();
    	
        float activeZoom = Math.max(0.1f, this.zoom);
        int sceneWidth = Math.max(1, Math.round(width / activeZoom));
        int sceneHeight = Math.max(1, Math.round(height / activeZoom));
        Bitmap sceneBitmap = FrameBitmapPool.newBitmap(sceneWidth, sceneHeight);
        Hitbox viewport = new Hitbox(camera.x, camera.y, sceneWidth, sceneHeight);
        
        var workspace = this.getService(Workspace.class);
        var lighting = this.getService(Lighting.class);
        var ui = this.getService(UIService.class);
        
        lighting.drawSky(sceneBitmap, viewport);
        workspace.draw(sceneBitmap, viewport);
        lighting.applyLightOverlay(sceneBitmap, viewport, workspace);
        
        Bitmap worldBitmap;
        if (sceneWidth == width && sceneHeight == height) {
            worldBitmap = sceneBitmap;
        } else {
            worldBitmap = sceneBitmap.resize(width, height);
        }
        
        mainBitmap.draw(worldBitmap, 0, 0);
        ui.draw(mainBitmap, new Hitbox(0, 0, width, height));
    }

    // --- Update & Collision ---

    public void tick(DikenEngine engine) {
    	if (this.engine == null)
    		this.engine = engine;
    	
        root.update(this, engine);

    	checkCollisions(engine);
    }

    private void checkCollisions(DikenEngine engine) {
        List<Instance> collidables = new ArrayList<>();
        collectCollidableNodes(this.getWorkspace(), collidables);
        
        for (int pass = 0; pass < MAX_COLLISION_PASSES; pass++) {
        	boolean hadIntersection = false;
        	
	        for (int i = 0; i < collidables.size(); i++) {
            Instance a = collidables.get(i);
            
            for (int j = i + 1; j < collidables.size(); j++) {
                Instance b = collidables.get(j);
	                IHitbox boxA = a.getGlobalAABB();
	                Hitbox boxB = b.getGlobalAABB();

	                if (boxA != null && boxB != null && boxA.intersects(boxB)) {
	                	hadIntersection = true;
	                    
	                    a.onCollision(b);
	                    b.onCollision(a);
	                    
	                    a.triggerEvent("OnCollision", b);
	                    b.triggerEvent("OnCollision", a);

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
    private void collectCollidableNodes(Node current, List<Instance> list) {
        if (current instanceof Instance inst && inst.getGlobalAABB() != null) {
            list.add(inst);
        }
        for (Node child : current.getChildren()) {
            collectCollidableNodes(child, list);
        }
    }
    
    public void startScripts() {
    	List<Script> scripts = this.root.findByClass(Script.class);
    	for (Script script : scripts) {
    		script.initialize(this);
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
    
    public Workspace getWorkspace() {
		return this.root.findFirstChildOfClass(Workspace.class);
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
            
            World world = new World(gameName, rootNode);
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
        IResource res = resources.remove(key);
        
        if (res != null) {
        	res.disponse();
            System.out.println(key + " başarıyla kaldırıldı.");
        }
        return this;
    }

    public void clearAllResources() {
        resources.values().forEach(IResource::disponse);
        
        resources.clear();
    }
    
    public Node getRoot() {
    	return this.root;
    }
    
    public World copy() {
    	Node copyRoot;
    	if (root == null) {
    		copyRoot = new Folder(new String(this.gameName));
    		this.initServices(copyRoot);
    	} else
    		copyRoot = this.root.copy();
    		
    	copyRoot.sendReloadAllNodes(copyRoot);
    	World copyWorld = new World(new String(this.gameName), copyRoot);
		copyWorld.resources = new ConcurrentHashMap<String, IResource>(this.resources);
		copyWorld.resources.values().forEach(IResource::reload);
		copyWorld.lastUpdateTime = System.currentTimeMillis();
		copyWorld.zoom = this.zoom;
		return copyWorld;
    }
}
