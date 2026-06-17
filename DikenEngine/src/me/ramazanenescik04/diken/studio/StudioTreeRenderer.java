package me.ramazanenescik04.diken.studio;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import me.ramazanenescik04.diken.game.Node;

import java.awt.Component;

public class StudioTreeRenderer extends DefaultTreeCellRenderer {
    private static final long serialVersionUID = -1014350127719842383L;

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                  boolean sel, boolean expanded,
                                                  boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

        if (value instanceof DefaultMutableTreeNode node) {
            Object userObject = node.getUserObject();
            if (userObject instanceof Node data) {
                setText(data.getName());
                var settingCategory = data.getNodeSettings().getLast();
                setIcon(new ImageIcon(settingCategory.getKey().getImage().toImage()));
            }
        }
        return this;
    }
}