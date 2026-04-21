package me.ramazanenescik04.diken.studio;

import java.util.ArrayList;
import java.util.List;

import me.ramazanenescik04.diken.game.world.World;
import me.ramazanenescik04.diken.gui.component.Button;
import me.ramazanenescik04.diken.gui.component.Panel;
import me.ramazanenescik04.diken.gui.component.ScrollPanel;
import me.ramazanenescik04.diken.gui.component.Text;
import me.ramazanenescik04.diken.gui.screen.StaticBackground;
import me.ramazanenescik04.diken.gui.window.Window;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.IResource;
import me.ramazanenescik04.diken.resource.ResourceLocator;

public class StudioResourceSelectWindow extends Window {
	private static final long serialVersionUID = 1L;
	
	private final World world;
	private final ResourceSelectFuture future;
	
	private ScrollPanel listScrollPanel;
	private Panel listPanel;

	public StudioResourceSelectWindow(World world, ResourceSelectFuture future) {
		super(0, 0, 260, 260);
		this.world = world;
		this.future = future;
		this.resizable = true;
		setTitle("Select Resource");
		ArrayBitmap icons = (ArrayBitmap) ResourceLocator.getResource("win-icons");
		setIcon(icons.getBitmap(8, 0));
	}
	
	@Override
	protected void open() {
		Panel panel = getContentPane();
		panel.setBackground(new StaticBackground(Bitmap.createClearedBitmap(16, 16, 0xffa0a0a0)));
		
		listScrollPanel = new ScrollPanel(6, 6, Math.max(1, panel.getWidth() - 12), Math.max(1, panel.getHeight() - 12));
		listPanel = new Panel(0, 0, Math.max(1, listScrollPanel.getWidth() - 18), 1);
		
		panel.add(listScrollPanel);
		listScrollPanel.setScrollComponent(listPanel);
		
		rebuildList();
	}
	
	@Override
	public void resized() {
		super.resized();
		Panel panel = getContentPane();
		if (panel == null || listScrollPanel == null) {
			return;
		}
		
		listScrollPanel.setBounds(6, 6, Math.max(1, panel.getWidth() - 12), Math.max(1, panel.getHeight() - 12));
		rebuildList();
	}
	
	private void rebuildList() {
		if (listPanel == null || listScrollPanel == null) {
			return;
		}
		
		listPanel.clear();
		int width = Math.max(48, listScrollPanel.getWidth() - 22);
		int y = 0;
		
		if (world == null || world.resources == null || world.resources.isEmpty()) {
			listPanel.add(new Text("No resources", 4, 4, 0xff333333));
			listPanel.setSize(width, 20);
			listScrollPanel.updateBars();
			return;
		}
		
		List<String> keys = new ArrayList<>(world.resources.keySet());
		keys.sort(String.CASE_INSENSITIVE_ORDER);
		
		for (String key : keys) {
			IResource resource = world.resources.get(key);
			String typeText = resource != null ? resource.getResourceType().name() : "UNKNOWN";
			
			Button selectButton = new Button(key + " (" + typeText + ")", 0, y, width, 18).setRunnable(() -> {
				if (future != null) {
					future.selected(key);
				}
				closed = true;
			});
			listPanel.add(selectButton);
			y += 20;
		}
		
		listPanel.setSize(width, Math.max(1, y));
		listScrollPanel.updateBars();
	}
	
	public static interface ResourceSelectFuture {
		void selected(String key);
	}
}
