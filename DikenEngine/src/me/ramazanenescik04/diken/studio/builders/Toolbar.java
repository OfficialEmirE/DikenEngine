package me.ramazanenescik04.diken.studio.builders;

import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;	
import javax.swing.JToolBar;

import bibliothek.gui.dock.common.DefaultSingleCDockable;
import bibliothek.gui.dock.common.action.CButton;
import me.ramazanenescik04.diken.language.Lang;
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
		private Map<String, Toolbar> toolbars = new LinkedHashMap<>();
		
		public Toolbar newToolbar(String id) {
			var toolbar = new Toolbar(id);
			toolbars.put(id, toolbar);
			return toolbar;
		}
		
		public Toolbar getToolbar(String id) {
			return toolbars.getOrDefault(id, newToolbar(id));
		}

		public void addButton(Toolbar toolbar, String key, int x, int y, String toolTip, Runnable r, Object...args) {
			Objects.requireNonNull(toolbar);
			Runnable runnable = Objects.requireNonNullElse(r, () -> {});
			
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
			button.setToolTipText(Lang.get(toolTip, args));
			button.setIcon(imageIcon);
			button.addActionListener(_ -> runnable.run());
			
			toolbar.addButton(key, button);
		}
		
		public void setButtonChecked(Toolbar toolbar, String key, boolean check) {
			var button = toolbar.buttons.get(key);
			if (button != null) {
				button.setSelected(check);
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

		public void convertCButton(DefaultSingleCDockable dock) {
			for (var toolbar : toolbars.values()) {
				toolbar.getButtons().forEach(jButton -> {
					CButton button = new CButton(jButton.getText(), jButton.getIcon());
					
					ActionListener[] list = jButton.getActionListeners();
					
					button.addActionListener(list[list.length - 1]);
					button.setTooltip(jButton.getToolTipText());
					
					dock.addAction(button);
				});
				
				dock.addSeparator();
			}
		}
	}
}
