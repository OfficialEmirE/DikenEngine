package me.ramazanenescik04.diken.studio.editors;

import java.awt.FlowLayout;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.plugin.Plugin;
import me.ramazanenescik04.diken.plugin.PluginManager;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

public class PluginManagerPanel extends BaseEditor {
	private static final long serialVersionUID = 1L;
	
	private final DefaultListModel<Plugin> model = new DefaultListModel<>();
    private final JList<Plugin> list = new JList<>(model);
    
    private final JButton enableButton = new JButton("Enable");
    private final JButton disableButton = new JButton("Disable");

	public PluginManagerPanel() {
		super("studio.menubar.pluginManager");
		
		setLayout(new BorderLayout());

        list.setCellRenderer(new PluginRenderer());
        add(new JScrollPane(list), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(enableButton);
        buttonPanel.add(disableButton);
        add(buttonPanel, BorderLayout.SOUTH);

        enableButton.addActionListener(_ -> {
            Plugin plugin = list.getSelectedValue();
            if (plugin != null) {
            	var engine = DikenEngine.getEngine();
                plugin.enable(engine, engine.getStudio());
                list.repaint();
            }
        });

        disableButton.addActionListener(_ -> {
            Plugin plugin = list.getSelectedValue();
            if (plugin != null) {
                plugin.disable();
                list.repaint();
            }
        });

        refresh();
	}
	
	public void refresh() {
        model.clear();

        for (Plugin plugin : PluginManager.instance.getPlugins()) {
            model.addElement(plugin);
        }
    }
	
	class PluginRenderer extends JPanel implements ListCellRenderer<Plugin> {
	    private static final long serialVersionUID = 1L;
		private final JLabel iconLabel = new JLabel();
	    private final JLabel nameLabel = new JLabel();
	    private final JLabel versionLabel = new JLabel();
	    private final JLabel authorLabel = new JLabel();
	    private final JLabel descLabel = new JLabel();

	    public PluginRenderer() {
	        setLayout(new BorderLayout(10, 5));
	        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

	        // Sol icon
	        iconLabel.setPreferredSize(new Dimension(48, 48));

	        // Sağ bilgi paneli
	        JPanel infoPanel = new JPanel();
	        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
	        infoPanel.setOpaque(false);

	        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
	        descLabel.setFont(descLabel.getFont().deriveFont(Font.ITALIC, 11f));
	        
	        infoPanel.add(nameLabel);
	        infoPanel.add(versionLabel);
	        infoPanel.add(authorLabel);
	        infoPanel.add(descLabel);

	        add(iconLabel, BorderLayout.WEST);
	        add(infoPanel, BorderLayout.CENTER);
	    }

	    @Override
	    public Component getListCellRendererComponent(
	            JList<? extends Plugin> list,
	            Plugin plugin,
	            int index,
	            boolean isSelected,
	            boolean cellHasFocus) {

	        // Icon
	        iconLabel.setIcon(scaleIcon(plugin.getIcon(), 48));

	        // Metinler
	        nameLabel.setText(plugin.getName() +
	                (plugin.isEnabled() ? " [Enabled]" : " [Disabled]"));
	        versionLabel.setText("Version: " + plugin.getVersion());
	        authorLabel.setText("By: " + plugin.getAuthor());
	        descLabel.setText(plugin.getDescription());

	        // Seçili renkler
	        if (isSelected) {
	            setBackground(list.getSelectionBackground());
	            setForeground(list.getSelectionForeground());
	        } else {
	            setBackground(list.getBackground());
	            setForeground(list.getForeground());
	        }

	        return this;
	    }
	    
	    private ImageIcon scaleIcon(Bitmap icon, int size) {
	        if (icon == null) {
	        	icon = ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(6, 3);
	        }

	        Image scaled = icon.toImage()
	                .getScaledInstance(size, size, Image.SCALE_SMOOTH);

	        return new ImageIcon(scaled);
	    }
	}
}
