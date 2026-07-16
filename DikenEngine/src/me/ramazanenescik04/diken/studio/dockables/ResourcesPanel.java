package me.ramazanenescik04.diken.studio.dockables;

import me.ramazanenescik04.diken.CrashDialog;
import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.gui.UniFont;
import me.ramazanenescik04.diken.language.Lang;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.EnumResource;
import me.ramazanenescik04.diken.resource.IOResource;
import me.ramazanenescik04.diken.resource.IResource;
import me.ramazanenescik04.diken.resource.ResourceLocator;
import me.ramazanenescik04.diken.studio.builders.Menubar;
import me.ramazanenescik04.diken.studio.builders.Toolbar;
import me.ramazanenescik04.diken.studio.dialog.CropDialog;
import me.ramazanenescik04.diken.studio.editors.AnimationEditor;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;

public class ResourcesPanel extends DockablePanel {

    private static final long serialVersionUID = 1L;

    private World theWorld;
    private JPanel listPanel;
    private JFrame parentFrame;
    private EditorTabPanel editor;

    private String selectedKey = null;
    private final Set<String> expandedNodes = new HashSet<>();

    public ResourcesPanel(World world, EditorTabPanel editor, JFrame parentFrame) {
    	super("resources_panel", "studio.windows.resources");
    	
        this.theWorld = world;
        this.editor = editor;
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
        
        var defaultToolbar = builder.newToolbar("default");
        builder.addButton(defaultToolbar, "new", 10, 0, Lang.get("studio.menubar.new"), this::newResourceDialog);
        builder.addButton(defaultToolbar, "add", 9, 0, Lang.get("resources.add"), this::addResourceDialog);
        builder.addButton(defaultToolbar, "remove", 0, 0, Lang.get("resources.remove"), this::removeSelectedResource);
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

	    Map<String, List<Map.Entry<String, IResource>>> children = new HashMap<>();

	    for (var entry : theWorld.resources.entrySet()) {
	        String key = entry.getKey();

	        if (key.equals("empty") || key.equals("default_font"))
	            continue;

	        int i = key.indexOf('&');
	        if (i != -1) {
	            children.computeIfAbsent(key.substring(0, i), _ -> new ArrayList<>()).add(entry);
	        }
	    }

	    for (var entry : theWorld.resources.entrySet()) {
	        String key = entry.getKey();
	        IResource resource = entry.getValue();

	        if (key.equals("empty") || key.equals("default_font") || resource == null)
	            continue;

	        // Child'ları burada göstermeyeceğiz.
	        if (key.contains("&"))
	            continue;

	        listPanel.add(createItem(key, resource, false));

	        List<Map.Entry<String, IResource>> list = children.get(key);
	        if (list != null && expandedNodes.contains(key)) {
	            for (var child : list) {
	                listPanel.add(createItem(child.getKey(), child.getValue(), true));
	            }
	        }
	    }

	    listPanel.revalidate();
	    listPanel.repaint();
	}

	private JPanel createItem(String key, IResource resource, boolean child) {
        JPanel item = new JPanel(new BorderLayout(8, 0));
        item.setBackground(getItemBackground(key));
        item.setBorder(BorderFactory.createCompoundBorder(
        	BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(35, 35, 35)),
        	new EmptyBorder(4, child ? 28 : 8, 4, 8)
        ));
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        
        JLabel arrow = new JLabel(" ");
        arrow.setPreferredSize(new Dimension(16, 16));
        
        boolean hasChildren = theWorld.resources.keySet().stream()
                .anyMatch(e -> e.startsWith(key + "&"));

        if (!child) {
            if (hasChildren) {
                arrow.setText(expandedNodes.contains(key) ? "▼" : "▶");

                arrow.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (expandedNodes.contains(key))
                            expandedNodes.remove(key);
                        else
                            expandedNodes.add(key);

                        rebuildList();
                        e.consume();
                    }
                });
            }
        }

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

        if (hasChildren)
        	leftPanel.add(arrow);
        
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
                	openEditor(resourceType, key);
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
        
        addPopup(item, buildMenubar(key, resource));
        return item;
    }

    private Color getItemBackground(String key) {
        return key.equals(selectedKey) ? new Color(75, 90, 110) : new Color(45, 45, 45);
    }

    private void addResourceDialog() {
        EnumResource[] types = EnumResource.values();
        EnumResource selectedType = (EnumResource) JOptionPane.showInputDialog(
            parentFrame,
            Lang.get("resources.selectResourceType"),
            Lang.get("resources.addResource"),
            JOptionPane.PLAIN_MESSAGE,
            null,
            types,
            types[0]
        );

        if (selectedType == null) return;

        // Dosya seçimi
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(Lang.get("resources.selectResourceName"));
        
        if (selectedType == EnumResource.IMAGE || selectedType == EnumResource.CURSOR) {
        	fileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", "png", "jpg", "jpeg", "bmp", "gif"));
		} else if (selectedType == EnumResource.SOUND) {
			fileChooser.setFileFilter(new FileNameExtensionFilter("Wave Files", "wav"));
		} else if (selectedType == EnumResource.ANIMATION) {
			fileChooser.setFileFilter(new FileNameExtensionFilter("Animation Files", "bin", "anim"));
		} else if (selectedType == EnumResource.FONT) {
			fileChooser.setFileFilter(new FileNameExtensionFilter("Fonts", "otf", "otc", "ttf", "ttc"));
		}

        int result = fileChooser.showOpenDialog(parentFrame);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File selectedFile = fileChooser.getSelectedFile();

        // Resource ismi
        String defaultName = selectedFile.getName();
        int dotIndex = defaultName.lastIndexOf('.');
        if (dotIndex > 0) defaultName = defaultName.substring(0, dotIndex);

        String resourceName = defaultName;
        if (selectedType != EnumResource.FONT) {
        	resourceName = JOptionPane.showInputDialog(
            	parentFrame,
            	Lang.get("resources.setResourceName"),
                defaultName
            );
        }
        
        if (resourceName == null || resourceName.trim().isEmpty()) return;
        resourceName = resourceName.trim();

        if (theWorld.resources.containsKey(resourceName)) {
            int overwrite = JOptionPane.showConfirmDialog(
                parentFrame,
                Lang.get("resources.overwriteWarning", resourceName),
                Lang.get("message.warning"),
                JOptionPane.YES_NO_OPTION
            );
            if (overwrite != JOptionPane.YES_OPTION) return;
        }

        try (FileInputStream fis = new FileInputStream(selectedFile)) {
        	if (selectedType == EnumResource.FONT) {
        		Font mainFont = Font.createFont(Font.TRUETYPE_FONT, fis);
        		Object[] stilSecenekleri = {
        				Lang.get("font.plain"),
        				Lang.get("font.bold"),
        				Lang.get("font.italic"),
        				Lang.get("font.bold.italic")};
                
                int styleSelect = JOptionPane.showOptionDialog(
                		parentFrame,
                		Lang.get("resources.selectFontStyle"),
                		Lang.get("resources.selectFontStyle.title"),
                        JOptionPane.DEFAULT_OPTION,
                        JOptionPane.QUESTION_MESSAGE,
                        null,
                        stilSecenekleri,
                        stilSecenekleri[0]
                );
                
                if (styleSelect == JOptionPane.CLOSED_OPTION) {
                	rebuildList();
                	return;
                }
                
                int selectedStyle = Font.PLAIN;
                if (styleSelect == 1) selectedStyle = Font.BOLD;
                else if (styleSelect == 2) selectedStyle = Font.ITALIC;
                else if (styleSelect == 3) selectedStyle = Font.BOLD | Font.ITALIC;
                
                String sizeInput = JOptionPane.showInputDialog(parentFrame, Lang.get("resources.fontSizeInput"));
                
                if (sizeInput != null) {
					float selectedSize = Float.parseFloat(sizeInput);

					Font finalFont = mainFont.deriveFont(selectedStyle, selectedSize);

					resourceName = JOptionPane.showInputDialog(parentFrame,
							Lang.get("resources.setResourceName"),
							finalFont.getFontName() + "-" + finalFont.getSize());

					theWorld.addResource(resourceName, UniFont.fromAwtFont(finalFont));
                }
        	} else {
        		var resource = IOResource.loadResource(fis, selectedType);
        		theWorld.addResource(resourceName, resource);
        	}
            
            rebuildList();
        } catch (Exception e) {
            e.printStackTrace();
            
            CrashDialog.crash(parentFrame, e, Lang.get("resources.importError"));
        }
    }
    
    private void newResourceDialog() {
    	try {
    		EnumResource[] types = EnumResource.values();
            EnumResource selectedType = (EnumResource) JOptionPane.showInputDialog(
                parentFrame,
                Lang.get("resources.selectResourceType"),
                Lang.get("resources.newResource"),
                JOptionPane.PLAIN_MESSAGE,
                null,
                types,
                types[3]
            );

            if (selectedType == null) return;
            
            var defaultName = selectedType.name() + "_" + (int) (Math.random() * 9999); //Random Name
            var resourceName = JOptionPane.showInputDialog(
            	parentFrame,
                Lang.get("resources.setResourceName"),
                defaultName
            );
            
            openEditor(selectedType, resourceName);
		} catch (Exception e) {
			e.printStackTrace();

			CrashDialog.crash(parentFrame, e, Lang.get("resources.importError"));
		}
    }

    private void removeSelectedResource() {
        if (selectedKey == null) return;

        int confirm = JOptionPane.showConfirmDialog(
            parentFrame,
            Lang.get("resources.removeWarning", selectedKey),
            Lang.get("message.confirm"),
            JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        theWorld.removeResource(selectedKey);
        selectedKey = null;
        rebuildList();
    }
    
    private void cloneSelectedResource() {
        if (selectedKey == null) return;

        IResource old = theWorld.resources.get(selectedKey);
        theWorld.addResource(selectedKey += "_1", old.clone());
        selectedKey = null;
        rebuildList();
    }
    
    private void cutImage() {
    	if (selectedKey == null) return;

        IResource resource = theWorld.resources.get(selectedKey);
        
        if (resource instanceof Bitmap bitmap) {
        	var dialog = new CropDialog(parentFrame, bitmap);
        	dialog.setVisible(true);
        	
        	try {
        		int[] index = {0};
				var bitmaps = dialog.get();
				
				if (bitmaps == null) {
					return;
				}
				
				bitmaps.forEach(e -> {
					theWorld.addResource(selectedKey + "&" + index[0], e);
					index[0]++;
				});
				
				rebuildList();
			} catch (Exception e) {
				e.printStackTrace();
			}
        }
    }
    
    private void renameResource() {
    	String oldResourceKey = selectedKey;
    	String newResourceKey = JOptionPane.showInputDialog(parentFrame, Lang.get("resources.setResourceName"), oldResourceKey);
    	
    	IResource res = theWorld.resources.get(oldResourceKey);
    	theWorld.removeResource(oldResourceKey);
    	theWorld.addResource(newResourceKey, res);
    	
    	selectedKey = newResourceKey;
    	
    	rebuildList();
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
    
    public void openEditor(EnumResource selectedType, String key) {
    	switch(selectedType) {
    		case ANIMATION:
    			editor.openEditor(new AnimationEditor(this.theWorld, key));
    			break;
    		default:
    			break;
    	}
    	
    	rebuildList();
    }

    public String getSelectedResourceKey() {
        return selectedKey;
    }
    
    private JPopupMenu buildMenubar(String key, IResource resource) {
    	Menubar.Builder builder = new Menubar.Builder();
    	
    	var menubar = builder.newMenu("default", "none");
    	builder.addMenuItem(menubar, "resources.remove", 0, 0, this::removeSelectedResource);
    	builder.addMenuItem(menubar, "resources.clone", 7, 0, this::cloneSelectedResource);
    	var id = builder.addMenuItem(menubar, "resources.cutImage", 3, 15, this::cutImage);
    	
    	if (!(resource instanceof Bitmap)) {
    		builder.setButtonEnabled(menubar, id, false);
    	}
    	builder.addMenuItem(menubar, "explorer.rename", 1, 0, this::renameResource);
    	
		return builder.getJPopupMenu(menubar);
	}
    
	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}
	
	class ResourceNode {
	    String key;
	    IResource resource;

	    boolean expanded;
	    List<ResourceNode> children = new ArrayList<>();
	}
}