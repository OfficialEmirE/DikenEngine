package me.ramazanenescik04.diken.game.nodes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import me.ramazanenescik04.diken.game.Instance;
import me.ramazanenescik04.diken.game.setting.EnumSettingType;
import me.ramazanenescik04.diken.game.setting.Setting;
import me.ramazanenescik04.diken.game.setting.SettingCategory;
import me.ramazanenescik04.diken.gui.hitbox.Hitbox;
import me.ramazanenescik04.diken.renderer.FrameBitmapPool;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

/**
 * Represents the `Part` type within the DikenEngine `game.nodes` package.
 */
public class Part extends Instance {
	private Surface surface = Surface.Stud;
	
	public enum Surface {
		Stud(0, 0),
		InStud(1, 0),
		Smooth(2, 0),
		Universal(-1, -1),
		Weld(3, 0),
		Motor(4, 0);
		
		public final int x, y;
		
		Surface(int x, int y) {
			this.x = x; this.y = y;
		}
	}
	
	public Part() {
		super();
		this.name = "Part";
		this.aabb = new Hitbox(0, 0, 16, 16);
		this.setAnchored(true);
	}

	public Part(int x, int y, int width, int height) {
		super("Part", x, y);
		this.aabb = new Hitbox(0, 0, width, height);
		this.setAnchored(true);
	}
	
	public Part(int x, int y) {
		super("Part", x, y);
		this.aabb = new Hitbox(0, 0, 16, 16);
		this.setAnchored(true);
	}

	public Part(DataInputStream in) throws IOException {
		super(in);
		if (getClass() == Part.class) {
			loadNodeData(in);
		}
	}
	
	public Bitmap render() {
		int width = this.aabb.getWidth();
		int height = this.aabb.getHeight();
	
		if (width <= 0 || height <= 0)
			return null;
		
		var bitmap = FrameBitmapPool.newBitmap(width, height);
		
		if (surface == Surface.Universal) {
		    // 1. Kullanılacak iki farklı dokuyu önden bir kez çekiyoruz (Döngü içinde sürekli getResource çağırmak performansı düşürür)
		    var surfaceAtlas = (ArrayBitmap) ResourceLocator.getResource("surface");
		    var texture0 = surfaceAtlas.getBitmap(0, 0);
		    var texture1 = surfaceAtlas.getBitmap(1, 0);
		    
		    int texW = texture0.w; // Genellikle 16 piksel
		    int texH = texture0.h; // Genellikle 16 piksel

		    // 2. Ekranı veya hedef bitmap'i kaplayacak kadar döngü oluşturuyoruz
		    for (var y = 0; y < (bitmap.h / texH) + 1; y++) {
		        for (var x = 0; x < (bitmap.w / texW) + 1; x++) {
		            
		            // Satır ve sütun toplamına göre 0 veya 1 seçerek damalı/ızgara deseni oluşturuyoruz
		            var surfaceTexture = ((x + y) % 2 == 0) ? texture0 : texture1;
		            
		            // Dokuyu doğru x ve y piksel koordinatlarına çiziyoruz
		            bitmap.draw(surfaceTexture, x * texW, y * texH);
		        }
		    }
		} else {
			var surfaceTexture = ((ArrayBitmap) ResourceLocator.getResource("surface")).getBitmap(surface.x, surface.y);
			
			for (var y = 0; y < (bitmap.h / surfaceTexture.h) + 1; y++) {
				for (var x = 0; x < (bitmap.w / surfaceTexture.w) + 1; x++) {
					bitmap.draw(surfaceTexture, x * surfaceTexture.w, y * surfaceTexture.h);
				}
			}
		}
		
		return bitmap;
	}
	
	public Surface getSurface() {
		return surface;
	}

	public void setSurface(Surface surface) {
		this.surface = surface;
	}

	@Override
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("part", "Part", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(0, 1));
		
		String[] typeOptions = new String[Surface.values().length];
        for (int i = 0; i < typeOptions.length; i++) {
            typeOptions[i] = Surface.values()[i].name();
        }
		
		var settingCategory = SettingCategory
				.createSettingCategory(key)
				.addSetting(new Setting<String>("Surface", surface.name(), typeOptions, String.class, EnumSettingType.LIST_SELECT)
						.addChangeListener(value -> this.setSurface(Surface.valueOf(value))));
		
		var list = super.getNodeSettings();
		list.add(settingCategory);
		return list;
	}

	@Override
	public void saveNodeData(DataOutputStream out) throws IOException {
		super.saveNodeData(out);
		out.writeUTF(surface.name());
	}

	@Override
	public void loadNodeData(DataInputStream in) throws IOException {
		super.loadNodeData(in);
		this.surface = Surface.valueOf(in.readUTF());
	}
}

