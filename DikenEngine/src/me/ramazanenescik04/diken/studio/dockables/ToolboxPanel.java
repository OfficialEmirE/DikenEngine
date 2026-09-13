package me.ramazanenescik04.diken.studio.dockables;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.json.JSONArray;

import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.language.Lang;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.EnumResource;
import me.ramazanenescik04.diken.resource.IResource;
import me.ramazanenescik04.diken.resource.ResourceLocator;
import me.ramazanenescik04.diken.studio.builders.Toolbar;
import me.ramazanenescik04.diken.tools.Utils;

public class ToolboxPanel extends DockablePanel {
    private static final long serialVersionUID = 1L;
    private static final String REPOSITORY_URL =
            "https://raw.githubusercontent.com/OfficialEmirE/DikenEngine-Toolbox/main/";

    private World theWorld;
    private JPanel listPanel;
    private JFrame parentFrame;

    private String selectedKey = null;

    public ToolboxPanel(World world, JFrame parentFrame) {
    	super("toolbox_panel", "studio.windows.toolbox");
    	
        this.theWorld = world;
        this.parentFrame = parentFrame;

        setLayout(new BorderLayout());
        setBackground(new Color(45, 45, 45));

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(new Color(45, 45, 45));

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(new Color(45, 45, 45));
        add(scrollPane, BorderLayout.CENTER);

        // Toolbar
        Toolbar.Builder builder = new Toolbar.Builder();
        
        var defaultToolbar = builder.create("default");
        builder.addButton(defaultToolbar, "refresh", 2, 15, Lang.get("resources.refresh"), this::rebuildList);
        
        builder.convertCButton(dock);

        rebuildList();
    }
    
	public void reloadWorld(World newWorld) {
		this.theWorld = newWorld;
		
		this.rebuildList();
	}

	private void rebuildList() {
	    listPanel.removeAll();

	    var itemList = new JSONArray(Utils.getWebData(REPOSITORY_URL + "index.json"));

	    listPanel.revalidate();
	    listPanel.repaint();
	}

	private JPanel createItem(String key, IResource resource) {
        JPanel item = new JPanel(new BorderLayout(8, 0));
        item.setBackground(getItemBackground(key));
        item.setBorder(BorderFactory.createCompoundBorder(
        	BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(35, 35, 35)),
        	new EmptyBorder(4, 8, 4, 8)
        ));
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        
        JLabel arrow = new JLabel(" ");
        arrow.setPreferredSize(new Dimension(16, 16));

        // Önizleme / ikon
        JLabel iconLabel = new JLabel();
        iconLabel.setPreferredSize(new Dimension(28, 28));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        var resourceType = resource.getResourceType();

        if (resourceType == EnumResource.IMAGE || resourceType == EnumResource.CURSOR) {
        	if (resource instanceof Bitmap bitmap) {
                var icon = getIcon(bitmap);
                
                if (icon == null) {
                	icon = getIcon(((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(1, 1));
                }
                
                if (icon == null) {
                	iconLabel.setText("?");
                    iconLabel.setForeground(Color.GRAY);
                } else {
                	iconLabel.setIcon(icon);
                }
            }
        } else if (resourceType == EnumResource.SOUND) {
        	var icon = getIcon(((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(11, 1));
            
            if (icon == null) {
            	iconLabel.setText("?");
                iconLabel.setForeground(Color.GRAY);
            } else {
            	iconLabel.setIcon(icon);
            }
        } else {
            // Diğer tipler için tip baş harfi göster
            iconLabel.setText(resource.getResourceType().name().substring(0, 1));
            iconLabel.setForeground(new Color(180, 180, 180));
            iconLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
        }
        
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        leftPanel.setOpaque(false);
        
        leftPanel.add(iconLabel);

        item.add(leftPanel, BorderLayout.WEST);

        // İsim + tip
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(key);
        nameLabel.setForeground(new Color(220, 220, 220));
        nameLabel.setFont(new Font("Tahoma", Font.PLAIN, 13));

        JLabel typeLabel = new JLabel(resource.getResourceType().name());
        typeLabel.setForeground(new Color(140, 140, 140));
        typeLabel.setFont(new Font("Tahoma", Font.PLAIN, 11));

        textPanel.add(nameLabel);
        textPanel.add(typeLabel);

        item.add(textPanel, BorderLayout.CENTER);

        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedKey = key;
                
                if (e.getClickCount() == 2) {
                	//openEditor(resourceType, key);
                }           
                
                rebuildList(); // seçim rengini güncelle
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                if (!key.equals(selectedKey)) {
                    item.setBackground(new Color(65, 65, 65));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                item.setBackground(getItemBackground(key));
            }
        });
        
        return item;
    }

    private Color getItemBackground(String key) {
        return key.equals(selectedKey) ? new Color(75, 90, 110) : new Color(45, 45, 45);
    }

    private ImageIcon getIcon(Bitmap icon) {
    	try {
            Image img = icon.toImage();
            Image scaled = img.getScaledInstance(28, 28, Image.SCALE_FAST);
            return new ImageIcon(scaled);
        } catch (Exception e) {
            return null;
        }
    }

    public String getSelectedResourceKey() {
        return selectedKey;
    }
}