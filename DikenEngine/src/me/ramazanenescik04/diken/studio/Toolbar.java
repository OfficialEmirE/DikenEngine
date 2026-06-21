package me.ramazanenescik04.diken.studio;

import java.util.*;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;	
import javax.swing.JToolBar;

import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.IOResource;
import me.ramazanenescik04.diken.resource.ResourceLocator;

public class Toolbar {
	private final String toolbarID;
	private final Map<String, AbstractButton> buttons = new LinkedHashMap<>();
	
	public Toolbar(String toolbarID) {
		this.toolbarID = toolbarID;
	}
	
	public void addButton(String key, AbstractButton button) {
		this.buttons.put(key, button);
	}
	
	public void removeButton(String key) {
		this.buttons.remove(key);
	}
	
	public String getToolbarID() {
		return new String(toolbarID);
	}
	
	public List<AbstractButton> getButtons() {
		return new ArrayList<>(buttons.values());
	}
	
	public static final class Builder {
		private Map<String, Toolbar> toolbars = new HashMap<>();
		
		public Toolbar newToolbar(String id) {
			var toolbar = new Toolbar(id);
			toolbars.put(id, toolbar);
			return toolbar;
		}
		
		public Toolbar getToolbar(String id) {
			return toolbars.getOrDefault(id, newToolbar(id));
		}

		public void addButton(Toolbar toolbar, String key, int x, int y, String toolTip, Runnable r) {
			// check args require non null
			Objects.requireNonNull(toolbar);
			Objects.requireNonNull(r);
			
			// load icon
			ImageIcon imageIcon;
			try {
				var image = ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(x, y);
				imageIcon = new ImageIcon(image.toImage());
			} catch (Exception ignore) {
				imageIcon = new ImageIcon(IOResource.missingTexture.toImage());
			} 
			
			// init and add button
			var button = new JButton();
			button.setToolTipText(toolTip);
			button.setIcon(imageIcon);
			button.addActionListener(_ -> r.run());
			
			toolbar.addButton(key, button);
		}
		
		public void setButtonChecked(Toolbar toolbar, String key, boolean b) {
			var button = toolbar.buttons.get(key);
			if (button != null) {
				button.setSelected(b);
			}
		}
		
		public boolean getButtonChecked(Toolbar toolbar, String key) {
			var button = toolbar.buttons.get(key);
			if (button != null) {
				return button.isSelected();
			}
			return false;
		}

		public JToolBar getJToolBar() {
			var jToolBar = new JToolBar();
			
			for (var toolbar : toolbars.values()) {
				toolbar.getButtons().forEach(button -> jToolBar.add(button));
				
				jToolBar.addSeparator();
			}
			
			return jToolBar;
		}
	}
}
