package me.ramazanenescik04.diken.studio.dockables;

import me.ramazanenescik04.diken.game.InstanceList;
import me.ramazanenescik04.diken.game.Node;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Sol taraftaki obje paleti. Kategorilere ayrılmış node listesini JTree
 * olarak gösterir, çift tıklayınca sahneye yeni node eklenir.
 */
public class BasicObjectsPanel extends DockablePanel {

    private static final long serialVersionUID = 1L;

    private static final Color BG_DARK = new Color(45, 45, 45);
    private static final Color BG_TREE = new Color(63, 63, 63);

    // -------------------------------------------------------------------------
    //  Fields
    // -------------------------------------------------------------------------
    private final JTree tree;
    private final DefaultMutableTreeNode rootNode;
    private final DefaultTreeModel treeModel;

    // -------------------------------------------------------------------------
    //  Constructor
    // -------------------------------------------------------------------------
    public BasicObjectsPanel(Consumer<Node> onDoubleClick) {
        super("basic_objects_id", "studio.windows.basicObjects");

        setLayout(new BorderLayout());
        setBackground(BG_DARK);

        rootNode = new DefaultMutableTreeNode("Objects");
        treeModel = new DefaultTreeModel(rootNode);
        tree = new JTree(treeModel);

        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        tree.setRowHeight(40);
        tree.setFont(new Font("Tahoma", Font.PLAIN, 15));
        tree.setBackground(BG_TREE);
        tree.getSelectionModel().setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION);

        // Özel renderer: kategori başlıkları ve node satırları
        tree.setCellRenderer(new ObjectTreeRenderer());

        // Çift tıklama → node ekle
        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                    if (path == null) return;
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
                    if (node.getUserObject() instanceof Node gameNode) {
                        onDoubleClick.accept(gameNode.copy());
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tree);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(BG_DARK);
        add(scrollPane, BorderLayout.CENTER);

        // Veriyi yükle
        rebuild();
    }

    // -------------------------------------------------------------------------
    //  Ağaç yapısını oluştur
    // -------------------------------------------------------------------------
    public final void rebuild() {
        rootNode.removeAllChildren();

        Map<InstanceList.CategoryKey, List<Node>> categories = InstanceList.getTypedNodes();

        for (Map.Entry<InstanceList.CategoryKey, List<Node>> entry : categories.entrySet()) {
            InstanceList.CategoryKey key = entry.getKey();
            List<Node> nodes = entry.getValue();

            // Kategori başlık düğümü (içinde CategoryKey tutar)
            DefaultMutableTreeNode categoryNode = new DefaultMutableTreeNode(key);
            rootNode.add(categoryNode);

            nodes.sort(Comparator.comparing(n -> n.getClass().getSimpleName()));

            for (Node node : nodes) {
                categoryNode.add(new DefaultMutableTreeNode(node));
            }
        }

        treeModel.reload();
        expandAll();
    }

    private void expandAll() {
        for (int i = 0; i < rootNode.getChildCount(); i++) {
            tree.expandPath(new TreePath(
                    ((DefaultMutableTreeNode) rootNode.getChildAt(i)).getPath()));
        }
    }
}
