package me.ramazanenescik04.diken.studio.builders;

import java.util.*;

import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

import me.ramazanenescik04.diken.language.Lang;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.IOResource;
import me.ramazanenescik04.diken.resource.ResourceLocator;

public class Menubar {
	private String name;
	private final String toolbarID;
	private final Map<String, JMenuItem> buttons = new LinkedHashMap<>();
	
	public Menubar(String toolbarID, String name) {
		this.toolbarID = toolbarID;
		this.name = name;
	}
	
	public void addSeperator() {
		this.buttons.put("Seperator-" + this.buttons.size(), new JMenu());
	}
	
	public void addButton(String key, JMenuItem button) {
		this.buttons.put(key, button);
	}
	
	public void removeButton(String key) {
		this.buttons.remove(key);
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMenubarID() {
		return new String(toolbarID);
	}
	
	public List<JMenuItem> getButtons() {
		return new ArrayList<>(buttons.values());
	}
	
	public static final class Builder {
		private Map<String, Menubar> menubars = new LinkedHashMap<>();
		
		public Menubar newMenu(String id, String name) {
			var toolbar = new Menubar(id, name);
			menubars.put(id, toolbar);
			return toolbar;
		}
		
		public Menubar getMenu(String id) {
			return menubars.getOrDefault(id, newMenu(id, "Untitled Menu"));
		}
		
		private String addMenuItem(Menubar toolbar, JMenuItem menu) {
			Objects.requireNonNull(toolbar);
			Objects.requireNonNull(menu);
			
			toolbar.addButton(menu.getName(), menu);
			
			return menu.getName();
		}
		
		public String addMenuItemCheckBox(Menubar toolbar, String name, int x, int y, Runnable r, Object...args) {
			Objects.requireNonNull(r);

			var button = new JCheckBoxMenuItem(Lang.get(name, args));
			button.setName(name.trim() + "_ID");
			button.addActionListener(_ -> r.run());

			var icon = getIcon(x, y);
			if (icon != null)
				button.setIcon(icon);

			return addMenuItem(toolbar, button);
		}

		public String addMenuItem(Menubar toolbar, String name, int x, int y, Runnable r, Object...args) {
			Objects.requireNonNull(r);
			
			var button = new JMenuItem(Lang.get(name, args));
			button.setName(name.trim() + "_ID");
			button.addActionListener(_ -> r.run());
			
			var icon = getIcon(x, y);
			if (icon != null)
				button.setIcon(icon);
			
			return addMenuItem(toolbar, button);
		}
		
		public void addMenuItem(Menubar menubar, String name, int x, int y, Runnable r, KeyStroke keyStroke) {
			addMenuAccelerator(menubar, addMenuItem(menubar, name, x, y, r), keyStroke);
		}
		
		public void addMenuAccelerator(Menubar menubar, String key, KeyStroke text) {
			menubar.buttons.get(key).setAccelerator(text);
		}
		
		public void addMenuSeparator(Menubar menubar) {
			menubar.addSeperator();
		}
		
		public void setButtonChecked(Menubar toolbar, String key, boolean b) {
			JMenuItem button = toolbar.buttons.get(key);
			if (button != null) {
				button.setSelected(b);
			}
		}
		
		public boolean getButtonChecked(Menubar toolbar, String key) {
			var button = toolbar.buttons.get(key);
			if (button != null) {
				return button.isSelected();
			}
			return false;
		}
		
		public void setButtonEnabled(Menubar menu, String key, boolean b) {
			JMenuItem button = menu.buttons.get(key);
			if (button != null) {
				button.setEnabled(b);
			}
		}
		
		public boolean isButtonEnabled(Menubar menu, String key) {
			var button = menu.buttons.get(key);
			if (button != null) {
				return button.isEnabled();
			}
			return false;
		}

		public JMenuBar getJMenuBar() {
			var jToolBar = new JMenuBar();
			
			for (var toolbar : menubars.values()) {
				var menu = new JMenu(Lang.get(toolbar.getName()));
				toolbar.getButtons().forEach(button -> {
					if (button instanceof JMenu) {
						menu.addSeparator();
					} else {
						menu.add(button);
					}
				});		
				jToolBar.add(menu);
			}
			
			return jToolBar;
		}
		

		public JPopupMenu getJPopupMenu(Menubar menubar) {
			var jPopupMenu = new JPopupMenu();
			
			menubar.getButtons().forEach(button -> {
				if (button instanceof JMenu) {
					jPopupMenu.addSeparator();
				} else {
					jPopupMenu.add(button);
				}
			});

			return jPopupMenu;
		}
		
		private static ImageIcon getIcon(int x, int y) {
			if (x >= 0 && y >= 0) {
				// load icon
				ImageIcon imageIcon;
				try {
					var image = ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(x, y);
					imageIcon = new ImageIcon(image.toImage());
				} catch (Exception ignore) {
					imageIcon = new ImageIcon(IOResource.missingTexture.toImage());
				} 
				
				return imageIcon;
			}
			
			return null;
		}
	}
}
