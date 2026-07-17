package me.ramazanenescik04.diken.studio.dockables;

import javax.swing.*;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.EnumSettingType;
import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.game.services.AbstractService;
import me.ramazanenescik04.diken.game.setting.Setting;
import me.ramazanenescik04.diken.language.Lang;
import me.ramazanenescik04.diken.studio.StudioTreeRenderer;
import me.ramazanenescik04.diken.studio.StudioUtils;

import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import javax.swing.DropMode;
import javax.swing.JFileChooser;

public class ExplorerPanel extends DockablePanel {

    private static final long serialVersionUID = 1L;

    // -------------------------------------------------------------------------
    //  Fields
    // -------------------------------------------------------------------------

    private JTree tree;
    private DefaultMutableTreeNode rootTreeNode;
    private final List<SelectedNodeListener> selectedNodeListeners = new ArrayList<>();
    public World theWorld;

    private final List<Node> clipboard = new ArrayList<>();
    public boolean suppressRebuild = false;
    private boolean isCut = false;

    private PickCallback pickCallback = null;
    private boolean ignoreNextSelectionEvent = false;
    private boolean showHideServices;

    private final List<NodeOpenListener> scriptOpenListeners = new ArrayList<>();

    /** rebuildExplorer sonrası çalıştırılacak callback (StudioPanel tarafından set edilir). */
    public Runnable postRebuildCallback;
    
    /** Sadece script isimleri değiştiğinde çalıştırılacak hafif callback (tam rebuild gerektirmez). */
    public Runnable scriptRenameCallback;

    // -------------------------------------------------------------------------
    //  Constructor
    // -------------------------------------------------------------------------

    public ExplorerPanel(World world) {
        super("explorer_id", "studio.windows.explorer");
        this.theWorld = world;
        var root = world.getRoot();

        setLayout(new BorderLayout(0, 0));
        this.rootTreeNode = new DefaultMutableTreeNode(root);

        // Create tree and assign to field before any rebuild call
        this.tree = new JTree(new DefaultTreeModel(rootTreeNode));
        initTree();
        add(new JScrollPane(this.tree), BorderLayout.CENTER);
    }

    // -------------------------------------------------------------------------
    //  Tree initialization (tree field must be set before calling this)
    // -------------------------------------------------------------------------

    private void initTree() {
        tree.setFont(new Font("Tahoma", Font.PLAIN, 15));
        tree.setBackground(new Color(63, 63, 63));
        tree.setShowsRootHandles(true);
        tree.setRootVisible(false);
        tree.setCellRenderer(new StudioTreeRenderer());
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);

        tree.setDragEnabled(true);
        tree.setDropMode(DropMode.ON_OR_INSERT);
        tree.setTransferHandler(new NodeTransferHandler(this));

        // Tree selection
        tree.addTreeSelectionListener(_ -> {
            if (pickCallback != null) {
                Node selected = getSelectedNode();
                if (selected != null) {
                    PickCallback cb = pickCallback;
                    pickCallback = null;
                    tree.setCursor(Cursor.getDefaultCursor());
                    ignoreNextSelectionEvent = true;
                    cb.onPicked(selected);
                }
                return;
            }
            if (ignoreNextSelectionEvent) {
                ignoreNextSelectionEvent = false;
                return;
            }
            fireSelectedNodeChanged(getSelectedNode(), getSelectedNodes());
        });

        // Keyboard
        tree.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                ExplorerPanel.this.keyPressed(e.isControlDown(), e.getKeyCode(), e.getKeyChar());
            }
        });

        // Event listeners (descendant changes → rebuild)
        var root = theWorld.getRoot();
        root.OnAddDescendant.Connect(_    -> rebuildExplorer());
        root.OnInsertDescendant.Connect(_ -> rebuildExplorer());
        root.OnRemoveDescendant.Connect(_ -> rebuildExplorer());
        root.OnReplaceDescendant.Connect(_ -> rebuildExplorer());

        // Set up context menu (also uses tree which is now set)
        setupTreeContextMenu(tree);

        rebuildExplorer();
    }

    // -------------------------------------------------------------------------
    //  Klavye kısayolları
    // -------------------------------------------------------------------------

    public void keyPressed(boolean ctrl, int key, char character) {
        if (key == StudioUtils.keyMapList.get("rename")) {
            DefaultMutableTreeNode n = getLastSelectedTreeNode();
            if (n == null || isServiceOrRoot(n)) return;
            if (n.getUserObject() instanceof Node gameNode) startRename(n, gameNode);

        } else if (ctrl && key == StudioUtils.keyMapList.get("copy")) {
            handleCopy();
        } else if (ctrl && key == StudioUtils.keyMapList.get("cut")) {
            handleCut();
        } else if (ctrl && key == StudioUtils.keyMapList.get("paste")) {
            handlePaste();
        } else if (ctrl && key == StudioUtils.keyMapList.get("duplicate")) {
            handleDuplicate();
        } else if (key == StudioUtils.keyMapList.get("delete")) {
            for (var n : getSelectedTreeNodes()) {
                if (n == null || isServiceOrRoot(n)) return;
                handleDelete(n);
            }
        } else if (key == StudioUtils.keyMapList.get("escape") && pickCallback != null) {
            pickCallback.onPicked(null);
            pickCallback = null;
            tree.setCursor(Cursor.getDefaultCursor());
        }
    }

    // -------------------------------------------------------------------------
    //  Tree yenileme
    // -------------------------------------------------------------------------

    public void rebuildExplorer() {
        if (suppressRebuild) return;

        var model = (DefaultTreeModel) tree.getModel();
        List<String> expandedPaths = saveExpandedPaths();

        ((DefaultMutableTreeNode) model.getRoot()).removeAllChildren();

        for (var service : theWorld.getServices()) {
            if (service.showStudio() || showHideServices) {
                addExplorerNode(rootTreeNode, service);
            }
        }

        model.reload();
        restoreExpandedPaths(expandedPaths);
        
        if (postRebuildCallback != null) {
            SwingUtilities.invokeLater(postRebuildCallback);
        }
    }

    private List<String> saveExpandedPaths() {
        List<String> paths = new ArrayList<>();
        var descendants = tree.getExpandedDescendants(new TreePath(rootTreeNode));
        if (descendants == null) return paths;

        for (TreePath path : Collections.list(descendants)) {
            StringBuilder sb = new StringBuilder();
            for (Object comp : path.getPath()) sb.append(comp.toString()).append("/");
            paths.add(sb.toString());
        }
        return paths;
    }

    private void restoreExpandedPaths(List<String> paths) {
        for (String p : paths) expandPathByString(rootTreeNode, p);
    }

    private void expandPathByString(DefaultMutableTreeNode node, String target) {
        TreeNode[] pathNodes = node.getPath();
        StringBuilder cur = new StringBuilder();
        for (TreeNode n : pathNodes) cur.append(n.toString()).append("/");

        String current = cur.toString();
        if (current.equals(target)) {
            tree.expandPath(new TreePath(pathNodes));
            return;
        }
        if (target.startsWith(current)) {
            for (int i = 0; i < node.getChildCount(); i++) {
                expandPathByString((DefaultMutableTreeNode) node.getChildAt(i), target);
            }
        }
    }

    // -------------------------------------------------------------------------
    //  Ağaç düğümü ekleme
    // -------------------------------------------------------------------------

    public DefaultMutableTreeNode createStudioObject(DefaultMutableTreeNode parent, Node node) {
        DefaultMutableTreeNode child = new DefaultMutableTreeNode(node);
        if (parent == null) parent = rootTreeNode;
        ((DefaultTreeModel) tree.getModel()).insertNodeInto(child, parent, parent.getChildCount());
        tree.scrollPathToVisible(new TreePath(child.getPath()));
        return child;
    }

    private void addExplorerNode(DefaultMutableTreeNode parent, Node node) {
        if (node instanceof AbstractService s && !(s.showStudio() || showHideServices)) return;
        var treeNode = createStudioObject(parent, node);
        for (Node child : node.getChildren()) addExplorerNode(treeNode, child);
    }

    // -------------------------------------------------------------------------
    //  Context menu
    // -------------------------------------------------------------------------

    private void setupTreeContextMenu(JTree tree) {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem miRename  = new JMenuItem(Lang.get("explorer.rename"));
        JMenuItem miExport  = new JMenuItem(Lang.get("studio.menubar.export"));
        JMenuItem miImport  = new JMenuItem(Lang.get("explorer.import"));
        JMenuItem miCopy    = new JMenuItem(Lang.get("explorer.copy"));
        JMenuItem miCut     = new JMenuItem(Lang.get("explorer.cut"));
        JMenuItem miPaste   = new JMenuItem(Lang.get("explorer.paste"));
        JMenuItem miDelete  = new JMenuItem(Lang.get("explorer.delete"));

        menu.add(miRename);
        menu.add(miExport);
        menu.add(miImport);
        menu.addSeparator();
        menu.add(miCopy);
        menu.add(miCut);
        menu.add(miPaste);
        menu.addSeparator();
        menu.add(miDelete);

        // Popup açılmadan önce aktiflik
        menu.addPopupMenuListener(new javax.swing.event.PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(javax.swing.event.PopupMenuEvent e) {
                DefaultMutableTreeNode sel = getLastSelectedTreeNode();
                boolean isSvcOrRoot = isServiceOrRoot(sel);
                boolean hasSel = sel != null && !sel.isRoot();
                boolean hasClip = !clipboard.isEmpty();

                miRename.setEnabled(hasSel && !isSvcOrRoot);
                miExport.setEnabled(hasSel && !isSvcOrRoot);
                miImport.setEnabled(hasSel);
                miCopy.setEnabled(hasSel && !isSvcOrRoot);
                miCut.setEnabled(hasSel && !isSvcOrRoot);
                miPaste.setEnabled(hasClip && hasSel);
                miDelete.setEnabled(hasSel && !isSvcOrRoot);
            }
            @Override public void popupMenuWillBecomeInvisible(javax.swing.event.PopupMenuEvent e) {}
            @Override public void popupMenuCanceled(javax.swing.event.PopupMenuEvent e) {}
        });

        // Aksiyonlar
        miRename.addActionListener(_ -> {
            DefaultMutableTreeNode n = getLastSelectedTreeNode();
            if (n == null || isServiceOrRoot(n)) return;
            if (n.getUserObject() instanceof Node gn) startRename(n, gn);
        });
        miExport.addActionListener(_ -> handleExport());
        miImport.addActionListener(_ -> handleImport());
        miCopy.addActionListener(_ -> handleCopy());
        miCut.addActionListener(_ -> handleCut());
        miPaste.addActionListener(_ -> handlePaste());
        miDelete.addActionListener(_ -> {
            for (var n : getSelectedTreeNodes()) {
                if (n == null || isServiceOrRoot(n)) return;
                handleDelete(n);
            }
        });

        // Mouse listener
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (pickCallback != null) return;
                if (e.getClickCount() == 2) {
                    TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                    if (path == null) return;
                    DefaultMutableTreeNode tn = (DefaultMutableTreeNode) path.getLastPathComponent();
                    if (!tn.isRoot() && tn.getUserObject() instanceof Node node) {
                        fireNodeOpenRequested(node);
                    }
                }
            }
            @Override
            public void mousePressed(MouseEvent e) { checkPopup(e); }
            @Override
            public void mouseReleased(MouseEvent e) { checkPopup(e); }
            private void checkPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                    if (path != null) {
                        tree.setSelectionPath(path);
                        menu.show(tree, e.getX(), e.getY());
                    }
                }
            }
        });
    }

    // -------------------------------------------------------------------------
    //  Context menu aksiyonları
    // -------------------------------------------------------------------------

    private void handleExport() {
        DefaultMutableTreeNode n = getLastSelectedTreeNode();
        if (n == null || isServiceOrRoot(n)) return;
        if (!(n.getUserObject() instanceof Node gn)) return;

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle(Lang.get("explorer.exportNodeTitle"));
        fc.setFileFilter(new FileNameExtensionFilter("DikenEngine Node File", "dnf"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        try {
            Node.exportNode(fc.getSelectedFile(), gn);
        } catch (IOException ex) {
            DikenEngine.errorLog("Node export failed", ex);
        }
    }

    private void handleImport() {
        DefaultMutableTreeNode n = getLastSelectedTreeNode();
        if (n == null || !(n.getUserObject() instanceof Node gn)) return;

        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle(Lang.get("explorer.importNodeTitle"));
        fc.setFileFilter(new FileNameExtensionFilter("DikenEngine Node File", "dnf"));
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        try {
            Node loaded = Node.importNode(fc.getSelectedFile());
            if (loaded != null) gn.addChild(loaded);
            rebuildExplorer();
        } catch (IOException ex) {
            DikenEngine.errorLog("Node import failed", ex);
        }
    }

    private void handleDelete(DefaultMutableTreeNode node) {
        if (node == null || isServiceOrRoot(node)) return;
        if (!(node.getUserObject() instanceof Node gn)) return;

        // Gerçek parent'tan kaldır (removeNode() sadece removed=true yapar)
        Node parentNode = gn.getParent();
        if (parentNode != null) {
            parentNode.removeChild(gn);
        }
        
        gn.removeNode();
        var model = (DefaultTreeModel) tree.getModel();
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
        model.removeNodeFromParent(node);
        if (parent != null) tree.setSelectionPath(new TreePath(parent.getPath()));
        
        // Script ağacını ve tab'ları güncelle (rebuildExplorer → postRebuildCallback → reloadScripts)
        rebuildExplorer();
    }

    // -------------------------------------------------------------------------
    //  Clipboard işlemleri: Copy / Cut / Paste / Duplicate
    // -------------------------------------------------------------------------

    private void handleCopy() {
        List<Node> selected = getSelectedGameNodes();
        if (selected.isEmpty()) return;
        clipboard.clear();
        isCut = false;
        for (Node node : selected) {
            if (isService(node)) continue;
            Node copy = node.copy();
            if (copy != null) clipboard.add(copy);
        }
    }

    private void handleCut() {
        List<Node> selected = getSelectedGameNodes();
        if (selected.isEmpty()) return;
        clipboard.clear();
        isCut = true;
        clipboard.addAll(selected);

        suppressRebuild = true;
        try {
            for (Node node : selected) {
                if (isService(node)) continue;
                Node parent = node.getParent();
                if (parent != null) parent.removeChild(node);
            }
        } finally {
            suppressRebuild = false;
        }
        SwingUtilities.invokeLater(this::rebuildExplorer);
    }

    private void handlePaste() {
        if (clipboard.isEmpty()) return;
        DefaultMutableTreeNode sel = getLastSelectedTreeNode();
        if (sel == null || sel.isRoot()) return;
        if (!(sel.getUserObject() instanceof Node target)) return;

        suppressRebuild = true;
        try {
            if (isCut) {
                for (Node node : clipboard) target.addChild(node);
                clipboard.clear();
                isCut = false;
            } else {
                for (Node node : clipboard) {
                    Node copy = node.copy();
                    if (copy != null) target.addChild(copy);
                }
            }
        } finally {
            suppressRebuild = false;
        }
        SwingUtilities.invokeLater(() -> { rebuildExplorer(); sendReloadAll(); });
    }

    private void handleDuplicate() {
        List<Node> selected = getSelectedGameNodes();
        if (selected.isEmpty()) return;

        suppressRebuild = true;
        try {
            for (Node node : selected) {
                if (isService(node)) continue;
                Node parent = node.getParent();
                if (parent == null) continue;
                Node copy = node.copy();
                if (copy != null) parent.addChild(copy);
            }
        } finally {
            suppressRebuild = false;
        }
        SwingUtilities.invokeLater(() -> { rebuildExplorer(); sendReloadAll(); });
    }

    // -------------------------------------------------------------------------
    //  Yeniden adlandırma (inline text field)
    // -------------------------------------------------------------------------

    private void startRename(DefaultMutableTreeNode treeNode, Node gameNode) {
        TreePath path = new TreePath(treeNode.getPath());
        java.awt.Rectangle bounds = tree.getPathBounds(path);
        if (bounds == null) return;

        JTextField tf = new JTextField(gameNode.getName());
        tf.setBounds(bounds);
        tf.setFont(tree.getFont());
        tf.selectAll();
        tree.add(tf);
        tf.requestFocusInWindow();
        tree.repaint();

        // Yazı genişledikçe text field'ı büyüt
        tf.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void resize() {
                java.awt.FontMetrics fm = tf.getFontMetrics(tf.getFont());
                int w = Math.max(bounds.width, fm.stringWidth(tf.getText()) + 20);
                w = Math.min(w, tree.getWidth() - bounds.x - 2);
                tf.setBounds(bounds.x, bounds.y, w, bounds.height);
                tree.repaint();
            }
            @Override public void insertUpdate(javax.swing.event.DocumentEvent e) { resize(); }
            @Override public void removeUpdate(javax.swing.event.DocumentEvent e) { resize(); }
            @Override public void changedUpdate(javax.swing.event.DocumentEvent e) { resize(); }
        });

        Runnable commit = () -> {
            String name = tf.getText().trim();
            if (!name.isEmpty()) {
                gameNode.setName(name);
                // Lightweight tree update: just refresh display of this node
                var model = (DefaultTreeModel) tree.getModel();
                model.nodeChanged(treeNode);
            }
            tree.remove(tf);
            // Script rename callback for updating tab titles (lightweight, no full rebuild)
            if (scriptRenameCallback != null) {
                SwingUtilities.invokeLater(scriptRenameCallback);
            }
        };

        tf.addActionListener(_ -> commit.run());
        tf.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) { commit.run(); }
        });
    }

    // -------------------------------------------------------------------------
    //  Diğer
    // -------------------------------------------------------------------------

    public void moveNode(Node node, Node newParent) {
        suppressRebuild = true;
        try {
            newParent.addChild(node);
        } finally {
            suppressRebuild = false;
        }
        SwingUtilities.invokeLater(this::rebuildExplorer);
    }

    private void sendReloadAll() {
        theWorld.getWorkspace().sendReloadAllNodes(theWorld.getWorkspace());
    }

    public void reloadWorld(World newWorld) {
        this.theWorld = newWorld;
        rebuildExplorer();
    }

    // -------------------------------------------------------------------------
    //  Pick mode
    // -------------------------------------------------------------------------

    public interface PickCallback {
        void onPicked(Node node);
    }

    public void startPickMode(PickCallback callback) {
        this.pickCallback = callback;
        tree.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    }

    // -------------------------------------------------------------------------
    //  Node ekleme (BasicObjectsPanel'den çağrılır)
    // -------------------------------------------------------------------------

    public void addNodeToSelected(Node newNode) {
        Node target = getSelectedNode();
        DefaultMutableTreeNode tn = getLastSelectedTreeNode();
        if (target == null || isServiceOrRoot(tn)) {
            if (target == null) target = theWorld.getWorkspace();
        }

        suppressRebuild = true;
        try { target.addChild(newNode); }
        finally { suppressRebuild = false; }

        SwingUtilities.invokeLater(() -> { rebuildExplorer(); selectNode(newNode); });
    }

    // -------------------------------------------------------------------------
    //  Selection listener'lar
    // -------------------------------------------------------------------------

    public interface SelectedNodeListener {
        void onSelectedNode(Node node);
        void onSelectedNodes(List<Node> nodes);
    }

    public void addSelectedNodeListener(SelectedNodeListener listener) {
        selectedNodeListeners.add(listener);
    }

    private void fireSelectedNodeChanged(Node node, List<Node> nodes) {
        for (var l : selectedNodeListeners) {
            l.onSelectedNode(node);
            l.onSelectedNodes(nodes);
        }
    }

    public void refreshSelection() {
        fireSelectedNodeChanged(getSelectedNode(), getSelectedNodes());
    }

    public Node getSelectedNode() {
        DefaultMutableTreeNode tn = getLastSelectedTreeNode();
        if (tn == null || tn.isRoot()) return null;
        return tn.getUserObject() instanceof Node n ? n : null;
    }

    public List<Node> getSelectedNodes() {
        List<Node> result = new ArrayList<>();
        TreePath[] paths = tree.getSelectionPaths();
        if (paths == null) return result;
        for (TreePath p : paths) {
            DefaultMutableTreeNode tn = (DefaultMutableTreeNode) p.getLastPathComponent();
            if (!tn.isRoot() && tn.getUserObject() instanceof Node n) result.add(n);
        }
        return result;
    }

    private List<DefaultMutableTreeNode> getSelectedTreeNodes() {
        List<DefaultMutableTreeNode> result = new ArrayList<>();
        TreePath[] paths = tree.getSelectionPaths();
        if (paths == null) return result;
        for (TreePath p : paths) {
            DefaultMutableTreeNode tn = (DefaultMutableTreeNode) p.getLastPathComponent();
            if (!tn.isRoot() && tn.getUserObject() instanceof Node) result.add(tn);
        }
        return result;
    }

    private List<Node> getSelectedGameNodes() {
        List<Node> result = new ArrayList<>();
        for (var tn : getSelectedTreeNodes()) {
            if (tn.getUserObject() instanceof Node n && !isService(n)) result.add(n);
        }
        return result;
    }

    private DefaultMutableTreeNode getLastSelectedTreeNode() {
        return (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
    }

    // -------------------------------------------------------------------------
    //  Node seçme
    // -------------------------------------------------------------------------

    public void selectNode(Node node) {
        if (node == null) return;
        TreePath path = findTreePath(rootTreeNode, node);
        if (path == null) return;
        tree.expandPath(path.getParentPath());
        tree.setSelectionPath(path);
        tree.scrollPathToVisible(path);
    }

    private TreePath findTreePath(DefaultMutableTreeNode current, Node node) {
        if (current.getUserObject() == node) return new TreePath(current.getPath());
        for (int i = 0; i < current.getChildCount(); i++) {
            TreePath p = findTreePath((DefaultMutableTreeNode) current.getChildAt(i), node);
            if (p != null) return p;
        }
        return null;
    }

    // -------------------------------------------------------------------------
    //  Node açma (double click)
    // -------------------------------------------------------------------------

    public interface NodeOpenListener {
        void onNodeOpenRequested(Node node);
    }

    public void addNodeOpenListener(NodeOpenListener listener) {
        scriptOpenListeners.add(listener);
    }

    private void fireNodeOpenRequested(Node node) {
        for (var l : scriptOpenListeners) l.onNodeOpenRequested(node);
    }

    // -------------------------------------------------------------------------
    //  Yardımcılar
    // -------------------------------------------------------------------------

    boolean isService(Node node) {
        return node instanceof AbstractService;
    }

    private boolean isServiceOrRoot(DefaultMutableTreeNode tn) {
        if (tn == null || tn.isRoot()) return true;
        return tn.getUserObject() instanceof Node n && isService(n);
    }

    // -------------------------------------------------------------------------
    //  Dockable ayarları
    // -------------------------------------------------------------------------

    @Override
    public List<Setting<?>> getDockableSettings() {
        List<Setting<?>> list = super.getDockableSettings();
        list.add(new Setting<>("Show Root Node", tree.isRootVisible(), Boolean.class, EnumSettingType.CHECK_BOX)
                .addChangeListener(tree::setRootVisible));
        list.add(new Setting<>("Show Hidden Services", showHideServices, Boolean.class, EnumSettingType.CHECK_BOX)
                .addChangeListener(v -> { showHideServices = v; rebuildExplorer(); }));
        return list;
    }
}
