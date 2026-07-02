package me.ramazanenescik04.diken.game.nodes;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.List;

import me.ramazanenescik04.diken.game.Instance;
import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.game.setting.SettingCategory;
import me.ramazanenescik04.diken.gui.hitbox.Hitbox;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

/**
 * Represents the `Model` type within the DikenEngine `game.nodes` package.
 */
public class Model extends Instance {
	public Model() {
		this("Model", 0, 0);
	}

	public Model(String name) {
		this(name, 0, 0);
	}

	public Model(String name, int x, int y) {
		super(name, x, y);
		init();
	}

	public Model(DataInputStream in) throws IOException {
		super(in);
		init();
		loadNodeData(in);
	}

	private void init() {
		
		// Child'e birşeyler olmuşsa, aabb'yi yeniden hesapla
		OnAddDescendant.Connect((_) -> {
			this.recalculateAABB();
		});
		OnInsertDescendant.Connect((_) -> {
			this.recalculateAABB();
		});
		OnRemoveDescendant.Connect((_) -> {
			this.recalculateAABB();
		});
		OnReplaceDescendant.Connect((_) -> {
			this.recalculateAABB();
		});
		
		this.setSolid(false);
	}
	
	private void recalculateAABB() {
	    List<Node> descendants = this.getDescendants();
	    
	    // Listede hiç Instance yoksa kutuyu sıfırla veya işlemi iptal et
	    boolean hasInstance = false;
	    
	    // Başlangıç değerlerini olabilecek en uç değerler yapıyoruz
	    int minInstanceX = Integer.MAX_VALUE;
	    int minInstanceY = Integer.MAX_VALUE;
	    int maxInstanceX = Integer.MIN_VALUE;
	    int maxInstanceY = Integer.MIN_VALUE;
	    
	    for (Node descendant : descendants) {
	        if (descendant instanceof Instance instance) {
	            hasInstance = true;
	            
	            if (instance.getX() < minInstanceX) {
	                minInstanceX = instance.getX();
	            }
	            if (instance.getY() < minInstanceY) {
	                minInstanceY = instance.getY();
	            }
	            
	            if (instance.getX() + instance.getAABBWidth() > maxInstanceX) {
	                maxInstanceX = instance.getX() + instance.getAABBWidth();
	            }
	            if (instance.getY() + instance.getAABBHeight() > maxInstanceY) {
	                maxInstanceY = instance.getY() + instance.getAABBHeight();
	            }
	        }
	    }
	    
	    // Eğer en az bir tane Instance bulunduysa Hitbox oluştur, yoksa sıfır kutu yap
	    if (hasInstance) {
	        this.aabb = new Hitbox(minInstanceX, minInstanceY, maxInstanceX, maxInstanceY);
	    } else {
	        this.aabb = new Hitbox(0, 0, 0, 0);
	    }
	}

	@Override
	public Bitmap render() {
		return null;
	}
	
	@Override
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("model", "Model", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(7, 1));
		
		var settingCategory = SettingCategory
				.createSettingCategory(key);
		
		var list = super.getNodeSettings();
		list.add(settingCategory);
		return list;
	}

}
