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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import org.json.JSONArray;
import org.json.JSONObject;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.language.Lang;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.EnumResource;
import me.ramazanenescik04.diken.resource.IOResource;
import me.ramazanenescik04.diken.resource.ResourceLocator;
import me.ramazanenescik04.diken.studio.builders.Toolbar;
import me.ramazanenescik04.diken.studio.dialog.ToolboxImportDialog;
import me.ramazanenescik04.diken.tools.Utils;
import javax.swing.border.EtchedBorder;

public class ToolboxPanel extends DockablePanel {
    private static final long serialVersionUID = 1L;
    private static final String REPOSITORY_URL =
            "https://raw.githubusercontent.com/OfficialEmirE/DikenEngine-Toolbox/refs/heads/main/";

    private List<ToolboxItem> toolboxItems = new ArrayList<>();
    private World theWorld;
    private JPanel listPanel;
    private JFrame parentFrame;
    private ExplorerPanel explorerPanel;
    private JTextField searchBar;
    
    private final Map<String, Filter> filters = Map.of(
    	    "by", (item, value) ->
    	        item.author().equalsIgnoreCase(value),

    	    "type", (item, value) ->
    	        item.type().equalsIgnoreCase(value)
    	);

    private String selectedKey = null;

    public ToolboxPanel(World world, JFrame parentFrame, ExplorerPanel explorerPanel) {
    	super("toolbox_panel", "studio.windows.toolbox");
    	
        this.theWorld = world;
        this.parentFrame = parentFrame;
        this.explorerPanel = explorerPanel;

        setLayout(new BorderLayout());
        setBackground(new Color(45, 45, 45));

        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(new Color(45, 45, 45));
        
        searchBar = new JTextField();
        searchBar.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        searchBar.addCaretListener(_ -> {
        	rebuildList();
        });

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(new Color(45, 45, 45));
        add(scrollPane, BorderLayout.CENTER);
        add(searchBar, BorderLayout.NORTH);

        // Toolbar
        Toolbar.Builder builder = new Toolbar.Builder();
        
        var defaultToolbar = builder.create("default");
        builder.addButton(defaultToolbar, "refresh", 2, 15, Lang.get("resources.refresh"), this::refreshItems);
        
        builder.convertCButton(dock);

        refreshItems();
    }
    
	public void reloadWorld(World newWorld) {
		this.theWorld = newWorld;
		
		this.refreshItems();
	}
	
	private void refreshItems() {
		this.toolboxItems.clear();
		
		var itemList = new JSONArray(Utils.getWebData(REPOSITORY_URL + "index.json"));
	    for (int i = 0; i < itemList.length(); i++) {
	    	JSONObject item = (JSONObject) itemList.get(i);
	    	
	    	toolboxItems.add(toToolboxItem(item));
	    }
	    
	    this.rebuildList();
	}
	
	private boolean matches(ToolboxItem item, String text) {
	    for (String part : text.split("\\s+")) {

	        int colon = part.indexOf(':');

	        if (colon == -1) {
	            // Normal arama
	            if (!item.name().toLowerCase().contains(part.toLowerCase()))
	                return false;

	            continue;
	        }

	        String key = part.substring(0, colon).toLowerCase();
	        String value = part.substring(colon + 1);

	        Filter filter = filters.get(key);

	        if (filter == null)
	            continue; // veya false, tercihe bağlı

	        if (!filter.matches(item, value))
	            return false;
	    }

	    return true;
	}

	private void rebuildList() {
	    listPanel.removeAll();
	    selectedKey = null;

	    String search = searchBar.getText().trim();

	    for (var item : toolboxItems) {
	        if (matches(item, search)) {
	            listPanel.add(createItem(item));
	        }
	    }

	    listPanel.revalidate();
	    listPanel.repaint();
	}

	private JPanel createItem(ToolboxItem resource) {
		var key = resource.id;
		
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
        
        var icon = getIcon(resource.icon);
        
        if (icon == null) {
        	icon = getIcon(((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(1, 1));
        }
        
        if (icon == null) {
        	iconLabel.setText("?");
            iconLabel.setForeground(Color.GRAY);
        } else {
        	iconLabel.setIcon(icon);
        }
        
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
        leftPanel.setOpaque(false);
        
        leftPanel.add(iconLabel);

        item.add(leftPanel, BorderLayout.WEST);

        // İsim + tip
        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(resource.name);
        nameLabel.setForeground(new Color(220, 220, 220));
        nameLabel.setFont(new Font("Tahoma", Font.PLAIN, 13));

        JLabel typeLabel = new JLabel("By: " + resource.author + " | Type: " + resource.type);
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
                	selectedKey = null;
                	addToWorld(resource);
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

	private void addToWorld(ToolboxItem item) {
		new ToolboxImportDialog(this.parentFrame, item, e -> {
			if (e.isCancelled())
				return;
			
			try {
				Node importedNode = item.getNode(e.getEnabledImportOptions().toArray(Node.EIOptions[]::new));
				if (item.place_location() == null) {
					importedNode.setParent(this.explorerPanel.getSelectedNode());
				} else {
					String[] splitted = item.place_location().split("\\.");
					
					if (!splitted[0].startsWith("game")) {
						throw new IOException(item.place_location() + " başı \"game\" ile eşit değil!");
					}
					
					Node placeLoc = theWorld.getRoot();
					for (int i = 1; i < splitted.length; i++) {
						String name = splitted[i];
						placeLoc = placeLoc.findFirstChild(name);
					}
					
					importedNode.setParent(placeLoc);
				}
				
				this.explorerPanel.rebuildExplorer();
			} catch (IOException e1) {
				DikenEngine.errorLog("Toolbox Import Error! ", e1);
			}
		}).setVisible(true);
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
    
    private ToolboxItem toToolboxItem(JSONObject item) {
    	var baseAssetUrl = REPOSITORY_URL + "assets/"; 
    	var image = IOResource.loadResource(Utils.getWebDataToIS(baseAssetUrl + item.getString("thumbnail")), EnumResource.IMAGE);
    	
    	return new ToolboxItem(
    			item.getString("id"),
    			(Bitmap) image,
    			item.getString("name"),
    			item.getString("type"),
    			item.getString("author"),
    			baseAssetUrl + item.getString("file"),
    			item.optString("place-location", null)
    	);
    }
    
    public record ToolboxItem(
    		String id,
    		Bitmap icon,
    		String name,
    		String type,
    		String author,
    		String file,
    		String place_location) {
    	
    	public Node getNode(Node.EIOptions...options) throws IOException {
    		var tempFile = File.createTempFile(id + "-", ".dnf");
    		tempFile.deleteOnExit();
    		
    		try (InputStream stream = Utils.getWebDataToIS(file)) {
    			Files.copy(stream, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    		}
    		
    		return Node.importNode(tempFile, options);
    	}
    }
    
    private interface Filter {
        boolean matches(ToolboxItem item, String value);
    }
}