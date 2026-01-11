package me.ramazanenescik04.diken.game;

import java.util.ArrayList;
import java.util.List;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.Vec2D;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.gui.Hitbox;
import me.ramazanenescik04.diken.gui.compoment.GuiCompoment;
import me.ramazanenescik04.diken.gui.compoment.Panel;

public class World extends Panel {
    private static final long serialVersionUID = 1L;

    // Her şeyin bağlı olduğu ana düğüm (Sahne)
    public final Node root;
    public String gameName = "Game";
    
    public Vec2D camera = new Vec2D(0, 0);

    public World(String gameName, int width, int height) {
    	super(0, 0, width, height);
    	this.gameName = gameName;
        // Root isimsiz ve render edilmeyen bir container'dır
        this.root = new Node(gameName) {
            @Override
            public Bitmap render() { return null; } // Root'un kendisi görünmez
            @Override
            protected void onAdded() {}
            @Override
            protected void onRemoved() {}
        };
    }

    // --- Node Yönetimi ---

    public void addNode(Node node) {
        root.addChild(node);
    }

    public void removeNode(Node node) {
        root.removeChild(node);
    }

    public Node getNodeByName(String name) {
        return findNodeRecursive(root, name);
    }
    
    // Ağaç içinde isme göre arama yapar
    private Node findNodeRecursive(Node current, String name) {
        if (current.name.equals(name)) return current;
        for (Node child : current.getChildren()) {
            Node found = findNodeRecursive(child, name);
            if (found != null) return found;
        }
        return null;
    }

    // --- Render ---

    @Override
    public Bitmap render() {
        Bitmap worldBitmap = new Bitmap(width, height);

        // Arkaplan rengi ve çizgileri
        if (this.drawX) {
            worldBitmap.box(0, 0, width - 1, height - 1, 0xffffffff);
            worldBitmap.drawLine(0, 0, this.width, this.height, 0xffffffff, 1);
            worldBitmap.drawLine(this.width, 0, 0, this.height, 0xffffffff, 1);
        }

        // --- KAMERA MANTIĞI ---
        // Root'u kameranın tersine çekerek her şeyi kaydırıyoruz
        int oldX = root.x;
        int oldY = root.y;
        
        root.x = -(int)camera.x();
        root.y = -(int)camera.y();

        // Tüm sahneyi (Entityler, Objectler) çiz
        root.draw(worldBitmap);

        // Root'u eski yerine koy (ki fizik hesaplamaları bozulmasın)
        root.x = oldX;
        root.y = oldY;

        // GUI Bileşenlerini çiz (Kameradan etkilenmezler, sabit kalırlar)
        List<GuiCompoment> compoments = this.getCompoments();
        for (GuiCompoment compoment : compoments) {
            worldBitmap.draw(compoment.render(), compoment.x, compoment.y);
        }

        return worldBitmap;
    }

    // --- Update & Collision ---

    public void tick(DikenEngine engine) {
        // 1. Tüm objelerin mantığını çalıştır (Recursive)
        root.update(this, engine);

        // 2. Çarpışmaları kontrol et
        checkCollisions(engine);

        super.tick(engine);
    }

    private void checkCollisions(DikenEngine engine) {
        List<Node> collidables = new ArrayList<>();
        collectCollidableNodes(root, collidables);

        for (int i = 0; i < collidables.size(); i++) {
            Node a = collidables.get(i);
            
            // j = i + 1 değil, 0'dan başlatıp kendisiyle kontrolü engelliyoruz.
            // Neden? Çünkü dinamik nesnelerin her turda taranması gerekebilir.
            // Ama performans için i+1 daha iyidir, sadece itme mantığını iyi kurmalıyız.
            for (int j = i + 1; j < collidables.size(); j++) {
                Node b = collidables.get(j);

                Hitbox boxA = a.getGlobalAABB();
                Hitbox boxB = b.getGlobalAABB();

                if (boxA != null && boxB != null && boxA.intersects(boxB)) {
                    
                    // 1. Önce mantıksal çarpışma olaylarını tetikle
                    a.onCollision(b);
                    b.onCollision(a);

                    // 2. Eğer ikisi de KATI (Solid) ise fiziksel itme uygula
                    if (a.solid && b.solid) {
                    	if (a.solid && b.solid) {
                    	    if (!a.isStatic && b.isStatic) {
                    	        a.separate(b); // A hareketli, B duvar -> A'yı it
                    	    } 
                    	    else if (a.isStatic && !b.isStatic) {
                    	        b.separate(a); // A duvar, B hareketli -> B'yi it
                    	    }
                    	    else if (!a.isStatic && !b.isStatic) {
                    	        // İkisi de hareketli (Örn: İki oyuncu)
                    	        // Birbirlerini yarı yarıya itebilirler veya sadece biri itilir.
                    	        a.separate(b);
                    	    }
                    	}
                    }
                }
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
    
    public void setCamera(Vec2D camera) {
        this.camera = camera;
    }

    public Vec2D getCamera() {
        return camera;
    }
}