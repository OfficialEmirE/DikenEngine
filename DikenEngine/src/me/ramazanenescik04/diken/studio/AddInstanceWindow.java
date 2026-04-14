package me.ramazanenescik04.diken.studio;

import java.util.List;

import me.ramazanenescik04.diken.game.InstanceList;
import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.game.SettingCategory;
import me.ramazanenescik04.diken.gui.compoment.Button;
import me.ramazanenescik04.diken.gui.compoment.Panel;
import me.ramazanenescik04.diken.gui.compoment.ScrollPanel;
import me.ramazanenescik04.diken.gui.screen.StaticBackground;
import me.ramazanenescik04.diken.gui.window.Window;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

public class AddInstanceWindow extends Window {
	private static final long serialVersionUID = 1L;
	
	private ScrollPanel instancesScrollPanel;
	private Panel instances;
	private AddInstanceFuture future;

	public AddInstanceWindow(int x, int y, AddInstanceFuture future) {
		super(x, y, 200, 200);
		setTitle("Add Instance");
		ArrayBitmap icons = (ArrayBitmap) ResourceLocator.getResource("editor_icons");
		setIcon(icons.getBitmap(2, 0));
		resizable = true;
		
		this.future = future;
	}

	@Override
	protected void open() {
		Panel panel = getContentPane();
		
		instancesScrollPanel = new ScrollPanel(0, 0, panel.getWidth(), panel.getHeight());
		panel.add(instancesScrollPanel);
		panel.setBackground(new StaticBackground(Bitmap.createClearedBitmap(16, 16, 0xffa0a0a0)));
		
		instances = new Panel(0, 0, panel.getWidth() - 18, 1);
		
		List<Node> node_list = InstanceList.getNodeList();
		
		int index = 0;
		for (Node node : node_list) {
			SettingCategory sc = node.getNodeSettings().getLast();
			instances.add(new Button(sc.getKey().getCategory(), 0, index, instances.getWidth(), 18).setButtonIcon(sc.getKey().getImage()).setRunnable(() -> {
				if (future != null) {
					future.success(node.copy());
					this.closed = true;
				}
			}));
			index += 19;
		}
		instances.setHeight(index);
		
		instancesScrollPanel.updateBars();
		instancesScrollPanel.setScrollComponent(instances);
	}

	@Override
	public void close() {
		super.close();
		
		if (future != null)
			future.cancelled();
	}

	@Override
	public void resized() {
		super.resized();
		
		Panel panel = getContentPane();
		instancesScrollPanel.setBounds(0, 0, panel.getWidth(), panel.getHeight());
	}
	
	public static abstract class AddInstanceFuture {
		public abstract void cancelled();
		public abstract void success(Node node);
	}

}
