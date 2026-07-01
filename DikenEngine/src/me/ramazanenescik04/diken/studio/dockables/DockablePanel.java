package me.ramazanenescik04.diken.studio.dockables;

import java.awt.Component;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Icon;
import javax.swing.JPanel;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import me.ramazanenescik04.diken.game.setting.Setting;

public class DockablePanel extends JPanel {
	private static final long serialVersionUID = -5075070012793078559L;
	
	public static final Map<String, DockablePanel> panels = new HashMap<>();
	protected DefaultSingleCDockable dock;
	
	private static final Icon EMPTY_ICON = new Icon() {
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {}
        @Override public int getIconWidth() { return 0; }
        @Override public int getIconHeight() { return 0; }
    };
	
	public DockablePanel(String id, String title) {
		this.dock = new DefaultSingleCDockable(id, title, this);
		this.dock.setTitleIcon(EMPTY_ICON);
		this.dock.setExternalizable(false);
		this.dock.setCloseable(true);
		
		panels.put(id, this);
	}
	
	public String getId() {
		return this.dock.getUniqueId();
	}

	public String getTitle() {
		return this.dock.getTitleText();
	}

	public void setTitle(String title) {
		this.dock.setTitleText(title);
	}
	
	public List<Setting<?>> getDockableSettings() {
		return new ArrayList<>();
	}

	public DefaultSingleCDockable getDockable() {
		return dock;
	}
}
