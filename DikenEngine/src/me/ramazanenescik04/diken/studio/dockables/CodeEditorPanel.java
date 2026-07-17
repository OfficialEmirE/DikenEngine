package me.ramazanenescik04.diken.studio.dockables;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.scripting.Script;
import me.ramazanenescik04.diken.studio.editors.BaseEditor;
import me.ramazanenescik04.diken.studio.editors.ScriptEditor;

/**
 * Script Editor paneli — Godot benzeri: sol tarafta script ağacı,
 * sağ tarafta JTabbedPane ile açık script sekmeleri.
 * 
 * Sol: dünyadaki tüm Script node'larını gösteren JTree
 * Sağ: JTabbedPane içinde ScriptEditor'lar
 */
public class CodeEditorPanel extends DockablePanel {

    private static final long serialVersionUID = 1L;

    private final JTabbedPane tabbedPane;
    private final JTree scriptTree;
    private final DefaultTreeModel treeModel;
    private final DefaultMutableTreeNode rootNode;
    private final List<BaseEditor> openEditors = new ArrayList<>();

    private World currentWorld;

    public CodeEditorPanel() {
        super("code_editor_id", "Script Editor");

        setLayout(new BorderLayout());
        setBackground(new Color(45, 45, 45));

        // --- Sol: Script ağacı ---
        rootNode = new DefaultMutableTreeNode("Scripts");
        treeModel = new DefaultTreeModel(rootNode);
        scriptTree = new JTree(treeModel);
        scriptTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        scriptTree.setRootVisible(true);
        scriptTree.setShowsRootHandles(true);
        scriptTree.setBackground(new Color(50, 50, 50));
        scriptTree.setForeground(new Color(220, 220, 220));
        scriptTree.setRowHeight(22);
        scriptTree.setFont(new JLabel().getFont().deriveFont(12f));

        // Renderer — Script adını göster
        scriptTree.setCellRenderer(new DefaultTreeCellRenderer() {
            private static final long serialVersionUID = 1L;
            {
                setBackgroundNonSelectionColor(new Color(50, 50, 50));
                setBackgroundSelectionColor(new Color(70, 70, 70));
                setTextNonSelectionColor(new Color(220, 220, 220));
                setTextSelectionColor(Color.WHITE);
            }
            @Override
            public java.awt.Component getTreeCellRendererComponent(JTree tree, Object value,
                    boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                if (value instanceof DefaultMutableTreeNode node) {
                    if (node.getUserObject() instanceof Script script) {
                        setText(script.getName());
                    }
                }
                return this;
            }
        });

        // Çift tıklama → tab aç
        scriptTree.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    TreePath path = scriptTree.getSelectionPath();
                    if (path == null) return;
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                    if (node.getUserObject() instanceof Script script) {
                        openScript(script);
                    }
                }
            }
        });

        JScrollPane treeScroll = new JScrollPane(scriptTree);
        treeScroll.setPreferredSize(new Dimension(180, 0));
        treeScroll.setMinimumSize(new Dimension(120, 0));
        treeScroll.setBorder(null);

        // --- Sağ: TabbedPane ---
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setBackground(new Color(45, 45, 45));

        // --- Ayırıcı ---
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScroll, tabbedPane);
        splitPane.setDividerLocation(180);
        splitPane.setDividerSize(4);
        splitPane.setResizeWeight(0.0);
        splitPane.setBorder(null);
        splitPane.setBackground(new Color(45, 45, 45));
        add(splitPane, BorderLayout.CENTER);
    }

    // =========================================================================
    //  Script ağacını dünyadaki Script node'ları ile doldur
    // =========================================================================

    /** Dünyadaki tüm Script'leri tarayıp ağaca ekler. */
    public void reloadScripts(World world) {
        this.currentWorld = world;
        rootNode.removeAllChildren();

        // Silinmiş script'lerin tab'larını kapat
        if (world != null) {
            List<Node> allNodes = world.getAllNodes();
            for (Node node : allNodes) {
                if (node instanceof Script script) {
                    DefaultMutableTreeNode scriptNode = new DefaultMutableTreeNode(script);
                    rootNode.add(scriptNode);
                }
            }
            
            // Artık dünyada olmayan script'lerin tab'larını kapat
            closeRemovedScriptTabs(allNodes);
        }

        treeModel.reload();
        expandAllNodes();
        
        // Update tab titles after tree refresh — this ensures script name
        // changes made via Properties or Explorer are reflected in tab headers.
        refreshTitles();
    }
    
    /** Dünyada artık var olmayan Script'lerin açık tab'larını kapatır. */
    private void closeRemovedScriptTabs(List<Node> currentNodes) {
        for (BaseEditor editor : new ArrayList<>(openEditors)) {
            if (editor instanceof ScriptEditor se) {
                Script script = se.getScript();
                boolean stillExists = false;
                for (Node node : currentNodes) {
                    if (node == script) {
                        stillExists = true;
                        break;
                    }
                }
                if (!stillExists) {
                    removeEditor(editor);
                }
            }
        }
    }

    private void expandAllNodes() {
        for (int i = 0; i < scriptTree.getRowCount(); i++) {
            scriptTree.expandRow(i);
        }
    }

    // =========================================================================
    //  Script aç / kapat
    // =========================================================================

    /** Bir Script'i tab olarak açar (zaten açıksa o tab'a geçer). */
    public void openScript(Script script) {
        // Zaten açık mı?
        for (BaseEditor editor : openEditors) {
            if (editor instanceof ScriptEditor se && se.getScript() == script) {
                tabbedPane.setSelectedComponent(editor);
                selectInTree(script);
                return;
            }
        }

        ScriptEditor editor = new ScriptEditor(script);
        addEditor(editor);
        selectInTree(script);
    }

    /** Yeni boş script oluşturup açar ve world'un Workspace'ine ekler. */
    public void newScript() {
        Script s = new Script();
        s.setName("New Script");
        if (currentWorld != null) {
            var workspace = currentWorld.getWorkspace();
            if (workspace != null) workspace.addChild(s);
        }
        openScript(s);
        reloadScripts(currentWorld);
    }

    public void addEditor(BaseEditor editor) {
        editor.init(new EditorTabPanel() {
            private static final long serialVersionUID = 1L;
            @Override
            public void removeEditor(BaseEditor e) {
                CodeEditorPanel.this.removeEditor(e);
            }
            @Override
            public void updateEditor(BaseEditor e) {
                CodeEditorPanel.this.updateEditor(e);
            }
        });
        openEditors.add(editor);

        tabbedPane.addTab(null, editor);
        int index = tabbedPane.indexOfComponent(editor);
        tabbedPane.setTabComponentAt(index, editor.getTabHeader());
        tabbedPane.setSelectedComponent(editor);
    }

    public void removeEditor(BaseEditor editor) {
        editor.closing();
        openEditors.remove(editor);

        int index = tabbedPane.indexOfComponent(editor);
        if (index >= 0) tabbedPane.removeTabAt(index);
    }

    public void removeScript(Script script) {
        for (BaseEditor editor : new ArrayList<>(openEditors)) {
            if (editor instanceof ScriptEditor se && se.getScript() == script) {
                removeEditor(editor);
                break;
            }
        }
    }

    /** Tüm açık script editor'lerin başlıklarını günceller (hafif - ağaç yeniden yüklenmez). */
    public void refreshTitles() {
        for (BaseEditor editor : openEditors) {
            if (editor instanceof ScriptEditor se) {
                se.updateTitle();
            }
        }
        // Only reload tree if it's visible — treeModel.reload() is expensive, 
        // use nodeChanged for the root to refresh all labels instead.
        treeModel.reload();
    }
    
    /** 
     * Sadece script tab başlıklarını günceller, ağacı yeniden yüklemez.
     * Explorer'da inline rename yapıldığında kullanılır (hızlı, lightweight).
     */
    public void refreshScriptTitles() {
        for (BaseEditor editor : openEditors) {
            if (editor instanceof ScriptEditor se) {
                se.updateTitle();
            }
        }
    }

    private void selectInTree(Script script) {
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) rootNode.getChildAt(i);
            if (child.getUserObject() == script) {
                TreePath path = new TreePath(child.getPath());
                scriptTree.setSelectionPath(path);
                scriptTree.scrollPathToVisible(path);
                return;
            }
        }
    }

    // =========================================================================
    //  Delegates
    // =========================================================================

    public void reloadWorld(World world, boolean playtest) {
        openEditors.forEach(e -> e.refreshWorld(world, playtest));
    }

    public void updateEditor(BaseEditor editor) {
        int index = tabbedPane.indexOfComponent(editor);
        if (index >= 0) {
            tabbedPane.setTabComponentAt(index, editor.getTabHeader());
            tabbedPane.setSelectedComponent(editor);
        }
    }

    public BaseEditor getCurrentEditor() {
        if (tabbedPane.getSelectedComponent() instanceof BaseEditor be) {
            return be;
        }
        return null;
    }

    public boolean hasEditor(BaseEditor editor) {
        return openEditors.contains(editor);
    }
}
