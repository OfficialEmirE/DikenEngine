package me.ramazanenescik04.diken.studio.dockables;

import me.ramazanenescik04.diken.game.InstanceList;
import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.language.Lang;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.IOResource;
import me.ramazanenescik04.diken.resource.ResourceLocator;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

/**
 * JTree renderer for BasicObjectsPanel.
 * Kategori başlıklarını (CategoryKey) ve node satırlarını farklı
 * renk/ikonlarla gösterir.
 */
public class ObjectTreeRenderer extends DefaultTreeCellRenderer {

    private static final long serialVersionUID = 1L;

    // -------------------------------------------------------------------------
    //  Renk sabitleri
    // -------------------------------------------------------------------------
    private static final Color CATEGORY_BG = new Color(70, 70, 70);
    private static final Color NODE_EVEN   = new Color(45, 45, 45);
    private static final Color NODE_ODD    = new Color(50, 50, 50);
    private static final Color BG_TREE     = new Color(63, 63, 63);
    private static final Color TEXT_COLOR  = new Color(220, 220, 220);
    private static final Color TEXT_SELECTED = Color.WHITE;

    private static final int ICON_SIZE = 24;
    private static final Font ROW_FONT = new Font("Tahoma", Font.PLAIN, 15);

    // -------------------------------------------------------------------------
    //  Yardımcı etiket (her hücre yeniden kullanılır)
    // -------------------------------------------------------------------------
    private final JLabel label = new JLabel();

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean selected, boolean expanded, boolean leaf, int row,
            boolean hasFocus) {

        DefaultMutableTreeNode dmtNode = (DefaultMutableTreeNode) value;
        Object userObject = dmtNode.getUserObject();

        label.setOpaque(true);
        label.setFont(ROW_FONT);
        label.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));

        if (userObject instanceof InstanceList.CategoryKey key) {
            renderCategory(key);
        } else if (userObject instanceof Node node) {
            renderNode(node, dmtNode, selected);
        } else {
            renderDefault(value);
        }

        return label;
    }

    // -------------------------------------------------------------------------
    //  Render tipleri
    // -------------------------------------------------------------------------

    private void renderCategory(InstanceList.CategoryKey key) {
        label.setBackground(CATEGORY_BG);
        label.setForeground(TEXT_COLOR);

        try {
            Image img = ((ArrayBitmap) ResourceLocator.getResource("editor_icons"))
                    .getBitmap(key.iconX(), key.iconY()).toImage();
            Image scaled = img.getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_SMOOTH);
            label.setIcon(new ImageIcon(scaled));
        } catch (Exception e) {
            label.setIcon(null);
        }

        label.setText(Lang.get(key.displayName()));
    }

    private void renderNode(Node node, DefaultMutableTreeNode dmtNode, boolean selected) {
        boolean isEven = (dmtNode.getParent().getIndex(dmtNode) % 2 == 0);
        label.setBackground(isEven ? NODE_EVEN : NODE_ODD);
        label.setForeground(selected ? TEXT_SELECTED : TEXT_COLOR);

        try {
            Image img = node.getNodeSettings()
                    .getLast().getKey().getImage().toImage();
            Image scaled = img.getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_SMOOTH);
            label.setIcon(new ImageIcon(scaled));
        } catch (Exception e) {
            label.setIcon(new ImageIcon(IOResource.missingTexture.toImage()));
        }

        label.setText(node.getClass().getSimpleName());
    }

    private void renderDefault(Object value) {
        label.setBackground(BG_TREE);
        label.setForeground(TEXT_COLOR);
        label.setIcon(null);
        label.setText(value.toString());
    }
}
