package me.ramazanenescik04.diken.game.nodes;

import java.util.List;

import me.ramazanenescik04.diken.game.Instance;
import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.game.SettingCategory;
import me.ramazanenescik04.diken.gui.hitbox.Hitbox;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

/**
 * Represents the `Model` type within the DikenEngine `game.nodes` package.
 */
public class Model extends Instance {
	private static final long serialVersionUID = 1L;

	public Model() {
		this("Model", 0, 0);
	}

	public Model(String name) {
		this(name, 0, 0);
	}

	public Model(String name, int x, int y) {
		super(name, x, y);
		
		// Child'e birşeyler olmuşsa, aabb'yi yeniden hesapla
		this.addLuaEventListener("OnAddDescendant", (_) -> {
			this.recalculateAABB();
		});
		this.addLuaEventListener("OnInsertDescendant", (_) -> {
			this.recalculateAABB();
		});
		this.addLuaEventListener("OnRemoveDescendant", (_) -> {
			this.recalculateAABB();
		});
		this.addLuaEventListener("OnReplaceDescendant", (_) -> {
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
	            
	            if (instance.x < minInstanceX) {
	                minInstanceX = instance.x;
	            }
	            if (instance.y < minInstanceY) {
	                minInstanceY = instance.y;
	            }
	            
	            if (instance.x + instance.getAABBWidth() > maxInstanceX) {
	                maxInstanceX = instance.x + instance.getAABBWidth();
	            }
	            if (instance.y + instance.getAABBHeight() > maxInstanceY) {
	                maxInstanceY = instance.y + instance.getAABBHeight();
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
