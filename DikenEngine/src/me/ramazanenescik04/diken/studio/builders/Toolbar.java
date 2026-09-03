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

public class Toolbar extends AbstractBuilder<AbstractButton> {	
	public Toolbar(String toolbarID) {
		super(toolbarID);
	}
	
	public static final class Builder extends AbstractBuilder.Builder<Toolbar, JToolBar> {
		public Toolbar createT(String id) {
			return new Toolbar(id);
		}
		
		public Toolbar getToolbar(String id) {
			return abstractBuilders.get(id);
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
			
			toolbar.add(key, button);
		}
		
		public void setButtonChecked(Toolbar toolbar, String key, boolean check) {
			var button = toolbar.builders.get(key);
			if (button != null) {
				button.setSelected(check);
			}
		}
		
		public boolean getButtonChecked(Toolbar toolbar, String key) {
			var button = toolbar.builders.get(key);
			if (button != null) {
				return button.isSelected();
			}
			return false;
		}
		
		@Override
		public JToolBar convert() {
			var jToolBar = new JToolBar();
			
			for (var toolbar : abstractBuilders.values()) {
				toolbar.getButtons().forEach(button -> jToolBar.add(button));
				
				jToolBar.addSeparator();
			}
			
			return jToolBar;
		}

		public void convertCButton(DefaultSingleCDockable dock) {
			for (var toolbar : abstractBuilders.values()) {
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
