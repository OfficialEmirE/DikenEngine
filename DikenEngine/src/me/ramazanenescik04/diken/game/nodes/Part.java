package me.ramazanenescik04.diken.game.nodes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import me.ramazanenescik04.diken.game.EnumSettingType;
import me.ramazanenescik04.diken.game.Instance;
import me.ramazanenescik04.diken.game.setting.Setting;
import me.ramazanenescik04.diken.game.setting.SettingCategory;
import me.ramazanenescik04.diken.gui.hitbox.Hitbox;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

/**
 * Represents the `Part` type within the DikenEngine `game.nodes` package.
 */
public class Part extends Instance {
	private transient Bitmap cachedBitmap;
	private transient int cachedWidth = -1;
	private transient int cachedHeight = -1;
	private transient int cachedColor = 0;
	private transient Surface cSurface = Surface.Stud;
	
	private Surface surface = Surface.Stud;
	
	public enum Surface {
		Stud(0, 0),
		InStud(1, 0),
		Smooth(2, 0),
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
		int width = Math.max(1, this.aabb.getWidth());
		int height = Math.max(1, this.aabb.getHeight());
		
		if (cachedBitmap == null || cachedWidth != width || cachedHeight != height || cachedColor != color || cSurface != surface) {
			cachedBitmap = new Bitmap(width, height);
			cachedBitmap.clear(color);
			
			var surfaceTexture = ((ArrayBitmap) ResourceLocator.getResource("surface")).getBitmap(surface.x, surface.y);
			
			for (var y = 0; y < (cachedBitmap.h / surfaceTexture.h) + 1; y++) {
				for (var x = 0; x < (cachedBitmap.w / surfaceTexture.w) + 1; x++) {
					cachedBitmap.blendDraw(surfaceTexture, x * surfaceTexture.w, y * surfaceTexture.h, this.color);
				}
			}
			
			// Later
			cachedWidth = width;
			cachedHeight = height;
			cachedColor = color;
			cSurface = surface;
		}
		
		return cachedBitmap;
	}

	@Override
	protected void reloadNode() {
		cachedBitmap = null;
		cachedWidth = -1;
		cachedHeight = -1;
		cSurface = Surface.Stud;
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
		this.cachedBitmap = null;
		this.cachedWidth = -1;
		this.cachedHeight = -1;
	}
}

