package me.ramazanenescik04.diken.studio.dockables;

import javax.swing.JMenuItem;
import java.awt.BorderLayout;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.game.services.AbstractService;
import me.ramazanenescik04.diken.game.setting.EnumSettingType;
import me.ramazanenescik04.diken.game.setting.Setting;
import me.ramazanenescik04.diken.language.Lang;
import me.ramazanenescik04.diken.studio.StudioTreeRenderer;
import me.ramazanenescik04.diken.studio.StudioUtils;
import me.ramazanenescik04.diken.studio.builders.Toolbar;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.JPopupMenu;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JScrollPane;
import java.awt.Color;
import java.awt.Cursor;

import javax.swing.DropMode;
import javax.swing.JFileChooser;

import java.awt.Font;

public class ExplorerPanel extends DockablePanel {

	private static final long serialVersionUID = 1L;
	
	private JTree tree;
    private DefaultMutableTreeNode rootTreeNode;
    private List<SelectedNodeListener> selectedNodeListeners = new ArrayList<>();
    public World theWorld;
    
    private List<Node> clipboard = new ArrayList<>();
    public boolean suppressRebuild = false;
    private boolean isCut = false;
    
    private PickCallback pickCallback = null;
    private boolean ignoreNextSelectionEvent = false;

	private boolean showHideServices = false;

	/**
	 * Create the panel.
	 */
	public ExplorerPanel(World world) {
		super("explorer_id", "studio.windows.explorer");
		
		this.theWorld = world;
		var root = world.getRoot();
		
		setLayout(new BorderLayout(0, 0));
		
		this.rootTreeNode = new DefaultMutableTreeNode(root);
		var treeModel = new DefaultTreeModel(rootTreeNode);
		
		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);
		
		tree = new JTree(treeModel);
		tree.setFont(new Font("Tahoma", Font.PLAIN, 15));
		tree.setBackground(new Color(63, 63, 63));
		scrollPane.setViewportView(tree);
		tree.setShowsRootHandles(true);
		tree.setRootVisible(false);
		tree.setCellRenderer(new StudioTreeRenderer());
		tree.getSelectionModel().setSelectionMode(
                TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);
		setupTreeContextMenu(tree);
		
		rebuildExplorer();
		
		root.OnAddDescendant.Connect(_ -> {
			rebuildExplorer();
		});
		
		root.OnInsertDescendant.Connect(_ -> {
			rebuildExplorer();
		});
		
		root.OnRemoveDescendant.Connect(_ -> {
			rebuildExplorer();
		});
		
		root.OnReplaceDescendant.Connect(_ -> {
			rebuildExplorer();
		});
		
		tree.setDragEnabled(true);
		tree.setDropMode(DropMode.ON_OR_INSERT);
		tree.setTransferHandler(new NodeTransferHandler(this));

		tree.addTreeSelectionListener(_ -> {
		    if (pickCallback != null) {
		        Node selected = getSelectedNode();
		        if (selected != null) {
		            PickCallback callback = pickCallback;
		            pickCallback = null;
		            tree.setCursor(Cursor.getDefaultCursor());
		            ignoreNextSelectionEvent = true;
		            callback.onPicked(selected);
		        }
		        return;
		    }
		    
		    if (ignoreNextSelectionEvent) {
		        ignoreNextSelectionEvent = false;
		        return;
		    }

		    Node selected = getSelectedNode();
		    fireSelectedNodeChanged(selected, getSelectedNodes());
		});
		
		tree.addKeyListener(new java.awt.event.KeyAdapter() {
		    @Override
		    public void keyPressed(java.awt.event.KeyEvent e) {
		    	boolean ctrl = e.isControlDown();
		    	
		    	ExplorerPanel.this.keyPressed(ctrl, e.getKeyCode(), e.getKeyChar());
		    }
		});
		
		Toolbar.Builder builder = new Toolbar.Builder();
        
        var defaultToolbar = builder.newToolbar("default");
        builder.addButton(defaultToolbar, "refresh", 2, 15, Lang.get("resources.refresh"), this::rebuildExplorer);
		
		builder.convertCButton(dock);
	}
	
	public void keyPressed(boolean ctrl, int key, char character) {
		if (key == StudioUtils.keyMapList.get("rename")) {
            DefaultMutableTreeNode selectedNode = 
                (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
            if (selectedNode == null || isServiceOrRoot(selectedNode)) return;
            if (!(selectedNode.getUserObject() instanceof Node gameNode)) return;
            
            startRename(selectedNode, gameNode);
        } else if (ctrl && key == StudioUtils.keyMapList.get("copy")) {
            handleCopy();
            
        } else if (ctrl && key == StudioUtils.keyMapList.get("cut")) {
            handleCut();
            
        } else if (ctrl && key == StudioUtils.keyMapList.get("paste")) {
            handlePaste();
        } else if (ctrl && key == StudioUtils.keyMapList.get("duplicate")) {
            handleDuplicate();
        } else if (key == StudioUtils.keyMapList.get("delete")) {
        	var selectedNodes = getSelectedDMTNodes();
	    	for (var selectedNode : selectedNodes) {
	    		if (selectedNode == null || isServiceOrRoot(selectedNode)) return;
		        handleDelete(selectedNode);
	    	}  
        } else if (key == StudioUtils.keyMapList.get("escape") && pickCallback != null) {
            pickCallback.onPicked(null);
            pickCallback = null;
            tree.setCursor(Cursor.getDefaultCursor());
        }
	}
	
	public void reloadWorld(World newWorld) {
		this.theWorld = newWorld;
		
		this.rebuildExplorer();
	}
	
	public DefaultMutableTreeNode createStudioObject(DefaultMutableTreeNode parentNode, Node node) {
        DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(node);

        if (parentNode == null) {
            parentNode = rootTreeNode;
        }

        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        model.insertNodeInto(newNode, parentNode, parentNode.getChildCount());

        tree.scrollPathToVisible(new javax.swing.tree.TreePath(newNode.getPath()));

        return newNode;
    }
	
	public void setupTreeContextMenu(JTree tree) {
	    JPopupMenu contextMenu = new JPopupMenu();
	    
	    JMenuItem itemYeniden = new JMenuItem(Lang.get("explorer.rename"));
	    JMenuItem itemDisariyaAktar = new JMenuItem(Lang.get("studio.menubar.export"));
	    JMenuItem itemIceriyeAktar = new JMenuItem(Lang.get("explorer.import"));
	    JMenuItem itemKopyala = new JMenuItem(Lang.get("explorer.copy"));
	    JMenuItem itemKes = new JMenuItem(Lang.get("explorer.cut"));
	    JMenuItem itemYapistir = new JMenuItem(Lang.get("explorer.paste"));
	    JMenuItem itemSil = new JMenuItem(Lang.get("explorer.delete"));
	    
	    contextMenu.add(itemYeniden);
	    contextMenu.add(itemDisariyaAktar);
	    contextMenu.add(itemIceriyeAktar);
	    contextMenu.addSeparator();
	    contextMenu.add(itemKopyala);
	    contextMenu.add(itemKes);
	    contextMenu.add(itemYapistir);
	    contextMenu.addSeparator();
	    contextMenu.add(itemSil);
	    
	    // Popup açılmadan önce hangi itemların aktif olacağını ayarla
	    contextMenu.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
	        @Override
	        public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
	            DefaultMutableTreeNode selectedNode = 
	                (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
	            
	            boolean isServiceOrRoot = isServiceOrRoot(selectedNode);
	            boolean hasClipboard = !clipboard.isEmpty();
	            boolean hasSelection = selectedNode != null && !selectedNode.isRoot();
	            
	            itemYeniden.setEnabled(hasSelection && !isServiceOrRoot);
	            itemDisariyaAktar.setEnabled(hasSelection && !isServiceOrRoot);
	            itemIceriyeAktar.setEnabled(hasSelection);
	            itemKopyala.setEnabled(hasSelection && !isServiceOrRoot);
	            itemKes.setEnabled(hasSelection && !isServiceOrRoot);
	            itemYapistir.setEnabled(hasClipboard && hasSelection);
	            itemSil.setEnabled(hasSelection && !isServiceOrRoot);
	        }
	        
	        @Override public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) {}
	        @Override public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {}
	    });
	    
	    itemYeniden.addActionListener(_ -> {
	        DefaultMutableTreeNode selectedNode = 
	            (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
	        if (selectedNode == null || isServiceOrRoot(selectedNode)) return;
	        if (!(selectedNode.getUserObject() instanceof Node gameNode)) return;
	        startRename(selectedNode, gameNode);
	    });
	    
	    itemDisariyaAktar.addActionListener(_ -> {
			DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
			if (selectedNode == null || isServiceOrRoot(selectedNode))
				return;
			if (!(selectedNode.getUserObject() instanceof Node gameNode))
				return;
			
			JFileChooser fileChooser = new JFileChooser();
	        fileChooser.setDialogTitle(Lang.get("explorer.exportNodeTitle"));
			fileChooser.setFileFilter(new FileNameExtensionFilter("DikenEngine Node File", "dnf"));

	        int result = fileChooser.showSaveDialog(this);
	        if (result != JFileChooser.APPROVE_OPTION) return;

	        File selectedFile = fileChooser.getSelectedFile();
	        if (selectedFile != null && gameNode != null) {
				try {
					Node.exportNode(selectedFile, gameNode);
				} catch (IOException e1) {
					DikenEngine.errorLog("Node dışarıya aktarmada bir sorun çıktı!", e1);
				}
			}
	    });
	    
	    itemIceriyeAktar.addActionListener(_ -> {
			DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
			if (selectedNode == null)
				return;
			if (!(selectedNode.getUserObject() instanceof Node gameNode))
				return;
			
			JFileChooser fileChooser = new JFileChooser();
	        fileChooser.setDialogTitle(Lang.get("explorer.importNodeTitle"));
			fileChooser.setFileFilter(new FileNameExtensionFilter("DikenEngine Node File", "dnf"));

	        int result = fileChooser.showOpenDialog(this);
	        if (result != JFileChooser.APPROVE_OPTION) return;

	        File selectedFile = fileChooser.getSelectedFile();
	        if (selectedFile != null && gameNode != null) {
				try {
					var loadedNode = Node.importNode(selectedFile);
					
					if (loadedNode != null)
						gameNode.addChild(loadedNode);
				} catch (IOException e1) {
					DikenEngine.errorLog("Node içeriye aktarmada bir sorun çıktı!", e1);
				}
				
				rebuildExplorer();
			}
	    });
	    
	    itemKopyala.addActionListener(_ -> handleCopy());
	    itemKes.addActionListener(_ -> handleCut());
	    itemYapistir.addActionListener(_ -> handlePaste());
	    
	    itemSil.addActionListener(_ -> {
	    	var selectedNodes = this.getSelectedDMTNodes();
	    	for (var selectedNode : selectedNodes) {
	    		if (selectedNode == null || isServiceOrRoot(selectedNode)) return;
		        handleDelete(selectedNode);
	    	}
	    });
	    
	    tree.addMouseListener(new MouseAdapter() { 
	    	@Override
	        public void mouseClicked(MouseEvent e) {
	            if (pickCallback != null) return; // pick modundaysa atla

	            if (e.getClickCount() == 2) {
	                TreePath path = tree.getPathForLocation(e.getX(), e.getY());
	                if (path == null) return;

	                DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) path.getLastPathComponent();
	                if (treeNode.isRoot()) return;
	                
	                if (treeNode.getUserObject() instanceof Node node) {
	                	fireNodeOpenRequested(node);
	                }
	            }
	        }
	    	
	        @Override
	        public void mousePressed(MouseEvent e) { checkForTrigger(e); }

	        @Override
	        public void mouseReleased(MouseEvent e) { checkForTrigger(e); }

	        private void checkForTrigger(MouseEvent e) {
	            if (e.isPopupTrigger()) {
	            	
	                TreePath path = tree.getPathForLocation(e.getX(), e.getY());
	                if (path != null) {
	                    tree.setSelectionPath(path);
	                    contextMenu.show(tree, e.getX(), e.getY());
	                }
	            }
	        }
	    });
	}
	
	private void handleDelete(DefaultMutableTreeNode selectedNode) {
	    if (selectedNode == null || isServiceOrRoot(selectedNode)) return;
	    if (!(selectedNode.getUserObject() instanceof Node gameNode)) return;
	    
	    gameNode.removeNode();
	    
	    DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
	    DefaultMutableTreeNode parent = (DefaultMutableTreeNode) selectedNode.getParent();
	    model.removeNodeFromParent(selectedNode);
	    
	    if (parent != null) {
	        tree.setSelectionPath(new TreePath(parent.getPath()));
	    }
	}

	public void rebuildExplorer() {
		if (suppressRebuild) return;
		
	    var model = tree.getModel();
	    
	    // 1. ADIM: Açık olan düğümlerin isim yollarını (String listesi olarak) hafızaya al
	    List<String> expandedStrPaths = new ArrayList<>();
	    var descendants = tree.getExpandedDescendants(new TreePath(rootTreeNode));
	    
	    if (descendants != null) {
	        for (TreePath path : Collections.list(descendants)) {
	            StringBuilder sb = new StringBuilder();
	            for (Object component : path.getPath()) {
	                sb.append(component.toString()).append("/");
	            }
	            expandedStrPaths.add(sb.toString());
	        }
	    }

	    // 2. ADIM: Ağacı tamamen temizle ve yeniden doldur (Senin orijinal kodun)
	    ((DefaultMutableTreeNode) model.getRoot()).removeAllChildren();
	    
	    var services = theWorld.getServices().iterator();
	    while (services.hasNext()) {
	        var service = services.next();
	        if (service.showStudio() || showHideServices) {
	        	addExplorerNode(this.rootTreeNode, service);
	        }
	    }
	    
	    // Ağacı grafik olarak yenile
	    ((DefaultTreeModel) model).reload();
	    
	    // 3. ADIM: Yeni oluşan ağaçta, eski açık string yollarını bul ve tekrar aç
	    for (String strPath : expandedStrPaths) {
	        expandPathByString(rootTreeNode, strPath);
	    }
	}

	// Rekürsif (özyinelemeli) olarak string yolunu yeni ağaçta arayan yardımcı metot
	private void expandPathByString(DefaultMutableTreeNode currentNode, String targetStrPath) {
	    // Mevcut düğümün ağaçtaki tam yolunu oluştur
	    TreeNode[] pathNodes = currentNode.getPath();
	    StringBuilder currentSb = new StringBuilder();
	    for (TreeNode n : pathNodes) {
	        currentSb.append(n.toString()).append("/");
	    }
	    
	    // Eğer hedef yola ulaştıysak bu düğümü genişlet
	    if (currentSb.toString().equals(targetStrPath)) {
	        tree.expandPath(new TreePath(pathNodes));
	        return;
	    }
	    
	    // Hedef yol mevcut yolun devamıysa çocuklarında aramaya devam et
	    if (targetStrPath.startsWith(currentSb.toString())) {
	        for (int i = 0; i < currentNode.getChildCount(); i++) {
	            DefaultMutableTreeNode child = (DefaultMutableTreeNode) currentNode.getChildAt(i);
	            expandPathByString(child, targetStrPath);
	        }
	    }
	}

	private void addExplorerNode(DefaultMutableTreeNode parentNode, Node node) {
		if (node instanceof AbstractService service && !(service.showStudio() || showHideServices)) return;
		
		var mutableNode = createStudioObject(parentNode, node);

		for (Node child : node.getChildren()) {
			addExplorerNode(mutableNode, child);
		}
	}

	public void moveNode(Node oyunNode, Node yeniParent) {
	    suppressRebuild = true;
	    try {
	        yeniParent.addChild(oyunNode);
	    } finally {
	        suppressRebuild = false;
	    }
	    javax.swing.SwingUtilities.invokeLater(() -> rebuildExplorer());
	}
	
	private void startRename(DefaultMutableTreeNode treeNode, Node gameNode) {
	    TreePath path = new TreePath(treeNode.getPath());
	    java.awt.Rectangle bounds = tree.getPathBounds(path);
	    if (bounds == null) return;

	    javax.swing.JTextField textField = new javax.swing.JTextField(gameNode.getName());
	    textField.setBounds(bounds);
	    textField.setFont(tree.getFont());
	    textField.selectAll();
	    
	    tree.add(textField);
	    textField.requestFocusInWindow();
	    tree.repaint();

	    // Yazıldıkça genişlet
	    textField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
	        private void resize() {
	            java.awt.FontMetrics fm = textField.getFontMetrics(textField.getFont());
	            int textWidth = fm.stringWidth(textField.getText()) + 20; // padding
	            int newWidth = Math.max(bounds.width, textWidth);
	            int treeWidth = tree.getWidth();
	            
	            // Tree sınırını geçmesin
	            int maxWidth = treeWidth - bounds.x - 2;
	            newWidth = Math.min(newWidth, maxWidth);
	            
	            textField.setBounds(bounds.x, bounds.y, newWidth, bounds.height);
	            tree.repaint();
	        }

	        @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { resize(); }
	        @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { resize(); }
	        @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { resize(); }
	    });

	    Runnable commit = () -> {
	        String newName = textField.getText().trim();
	        if (!newName.isEmpty()) {
	            gameNode.setName(newName);
	        }
	        tree.remove(textField);
	        javax.swing.SwingUtilities.invokeLater(() -> rebuildExplorer());
	    };

	    textField.addActionListener(_ -> commit.run());

	    textField.addFocusListener(new java.awt.event.FocusAdapter() {
	        @Override
	        public void focusLost(java.awt.event.FocusEvent e) {
	            commit.run();
	        }
	    });
	}
	
	private List<Node> getSelectedGameNodes() {
	    List<Node> result = new ArrayList<>();
	    TreePath[] paths = tree.getSelectionPaths();
	    if (paths == null) return result;
	    
	    for (TreePath path : paths) {
	        DefaultMutableTreeNode treeNode = 
	            (DefaultMutableTreeNode) path.getLastPathComponent();
	        if (treeNode.isRoot()) continue;
	        if (treeNode.getUserObject() instanceof Node gameNode) {
	            result.add(gameNode);
	        }
	    }
	    return result;
	}

	private void handleCopy() {
	    List<Node> selected = getSelectedGameNodes();
	    if (selected.isEmpty()) return;
	    
	    clipboard.clear();
	    isCut = false;
	    
	    for (Node node : selected) {
	        if (isService(node)) continue; // servisleri kopyalama
	        Node copied = node.copy();
	        if (copied != null) clipboard.add(copied);
	    }
	}

	private void handleCut() {
	    List<Node> selected = getSelectedGameNodes();
	    if (selected.isEmpty()) return;
	    
	    clipboard.clear();
	    isCut = true;
	    clipboard.addAll(selected);
	    
	    // Anında parent'tan kopar ama world'de tutmaya devam et
	    suppressRebuild = true;
	    try {
	        for (Node node : selected) {
	            if (isService(node)) continue;
	            Node parent = node.getParent();
	            if (parent != null) {
	                parent.removeChild(node); // parent'tan kopar
	            }
	        }
	    } finally {
	        suppressRebuild = false;
	    }
	    
	    javax.swing.SwingUtilities.invokeLater(() -> rebuildExplorer());
	}

	private void handlePaste() {
	    if (clipboard.isEmpty()) return;
	    
	    DefaultMutableTreeNode selectedTreeNode = 
	        (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
	    
	    // Root'a veya servis listesinin olduğu yere yapıştırmayı engelle
	    if (selectedTreeNode == null || selectedTreeNode.isRoot()) return;
	    
	    Node targetParent;
	    if (selectedTreeNode.getUserObject() instanceof Node gameNode) {
	        // Servise yapıştırmaya izin ver ama root'a değil
	        targetParent = gameNode;
	    } else {
	        return;
	    }
	    
	    suppressRebuild = true;
	    try {
	        if (isCut) {
	            for (Node node : clipboard) {
	                targetParent.addChild(node);
	            }
	            clipboard.clear();
	            isCut = false;
	        } else {
	            for (Node node : clipboard) {
	                Node copied = node.copy();
	                if (copied != null) targetParent.addChild(copied);
	            }
	        }
	    } finally {
	        suppressRebuild = false;
	    }
	    
	    javax.swing.SwingUtilities.invokeLater(() -> {
	        rebuildExplorer();
	        sendReloadAll();
	    });
	}

	private void handleDuplicate() {
	    List<Node> selected = getSelectedGameNodes();
	    if (selected.isEmpty()) return;
	    
	    suppressRebuild = true;
	    try {
	        for (Node node : selected) {
	            if (isService(node)) continue; // servisleri duplicate etme
	            
	            Node parent = node.getParent();
	            if (parent == null) continue;
	            
	            Node copied = node.copy();
	            if (copied != null) {
	                parent.addChild(copied);
	            }
	        }
	    } finally {
	        suppressRebuild = false;
	    }
	    
	    javax.swing.SwingUtilities.invokeLater(() -> {
	        rebuildExplorer();
	        sendReloadAll();
	    });
	}

	private void sendReloadAll() {
	    theWorld.getWorkspace().sendReloadAllNodes(theWorld.getWorkspace());
	}
	
	boolean isService(Node node) {
	    return node instanceof AbstractService;
	}

	private boolean isServiceOrRoot(DefaultMutableTreeNode treeNode) {
	    if (treeNode == null || treeNode.isRoot()) return true;
	    if (treeNode.getUserObject() instanceof Node gameNode) {
	        return isService(gameNode);
	    }
	    return false;
	}
	
	public interface SelectedNodeListener {
	    void onSelectedNode(Node node);
	    void onSelectedNodes(List<Node> nodes); // çoklu
	}

	public List<Node> getSelectedNodes() {
	    TreePath[] paths = tree.getSelectionPaths();
	    if (paths == null) return new ArrayList<>();
	    
	    List<Node> result = new ArrayList<>();
	    for (TreePath path : paths) {
	        DefaultMutableTreeNode treeNode = 
	            (DefaultMutableTreeNode) path.getLastPathComponent();
	        if (treeNode.isRoot()) continue;
	        if (treeNode.getUserObject() instanceof Node gameNode) {
	            result.add(gameNode);
	        }
	    }
	    return result;
	}
	
	private List<DefaultMutableTreeNode> getSelectedDMTNodes() {
	    TreePath[] paths = tree.getSelectionPaths();
	    if (paths == null) return new ArrayList<>();
	    
	    List<DefaultMutableTreeNode> result = new ArrayList<>();
	    for (TreePath path : paths) {
	        DefaultMutableTreeNode treeNode = 
	            (DefaultMutableTreeNode) path.getLastPathComponent();
	        if (treeNode.isRoot()) continue;
	        if (treeNode.getUserObject() instanceof Node) {
	            result.add(treeNode);
	        }
	    }
	    return result;
	}


	private void fireSelectedNodeChanged(Node node, List<Node> nodes) {
	    for (SelectedNodeListener listener : selectedNodeListeners) {
	        listener.onSelectedNode(node);
	        listener.onSelectedNodes(nodes);
	    }
	}
	
	public Node getSelectedNode() {
	    DefaultMutableTreeNode selectedTreeNode = 
	        (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
	    if (selectedTreeNode == null || selectedTreeNode.isRoot()) return null;
	    if (selectedTreeNode.getUserObject() instanceof Node gameNode) return gameNode;
	    return null;
	}
	
	public void addSelectedNodeListener(SelectedNodeListener listener) {
	    selectedNodeListeners.add(listener);
	}
	
	public void selectNode(Node node) {
	    if (node == null) return;
	    
	    TreePath path = findTreePath(rootTreeNode, node);
	    if (path == null) return;
	    
	    tree.expandPath(path.getParentPath());
	    tree.setSelectionPath(path);
	    tree.scrollPathToVisible(path);
	}
	
	public void refreshSelection() {
	    fireSelectedNodeChanged(getSelectedNode(), getSelectedNodes());
	}
	
	private TreePath findTreePath(DefaultMutableTreeNode currentNode, Node node) {
	    if (currentNode.getUserObject() == node) {
	        return new TreePath(currentNode.getPath());
	    }
	    
	    for (int i = 0; i < currentNode.getChildCount(); i++) {
	        DefaultMutableTreeNode child = (DefaultMutableTreeNode) currentNode.getChildAt(i);
	        TreePath path = findTreePath(child, node);
	        if (path != null) {
	            return path;
	        }
	    }
	    
	    return null;
	}
	
	public interface PickCallback {
	    void onPicked(Node node);
	}

	public void startPickMode(PickCallback callback) {
	    this.pickCallback = callback;
	    tree.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
	}
	
	public void addNodeToSelected(Node newNode) {
	    Node target = getSelectedNode();
	    
	    DefaultMutableTreeNode selectedTreeNode = 
	        (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
	    
	    if (target == null || isServiceOrRoot(selectedTreeNode)) {
	        if (target == null) {
	            target = theWorld.getWorkspace();
	        }
	    }
	    
	    suppressRebuild = true;
	    try {
	        target.addChild(newNode);
	    } finally {
	        suppressRebuild = false;
	    }
	    
	    SwingUtilities.invokeLater(() -> {
	        rebuildExplorer();
	        selectNode(newNode);
	    });
	}
	
	public interface NodeOpenListener {
	    void onNodeOpenRequested(Node script);
	}

	private List<NodeOpenListener> scriptOpenListeners = new ArrayList<>();

	public void addNodeOpenListener(NodeOpenListener listener) {
	    scriptOpenListeners.add(listener);
	}

	private void fireNodeOpenRequested(Node script) {
	    for (NodeOpenListener listener : scriptOpenListeners) {
	        listener.onNodeOpenRequested(script);
	    }
	}

	@Override
	public List<Setting<?>> getDockableSettings() {
		List<Setting<?>> list = super.getDockableSettings();
		list.add(new Setting<>("Show Root Node", this.tree.isRootVisible(), Boolean.class, EnumSettingType.CHECK_BOX)
				.addChangeListener(this.tree::setRootVisible));
		list.add(new Setting<>("Show Hidded Services", this.showHideServices, Boolean.class,
				EnumSettingType.CHECK_BOX).addChangeListener(e -> {
					showHideServices = e;
					rebuildExplorer();
				}));
		return list;
	}
}
