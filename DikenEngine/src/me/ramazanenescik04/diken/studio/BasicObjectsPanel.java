package me.ramazanenescik04.diken.studio;

import me.ramazanenescik04.diken.game.InstanceList;
import me.ramazanenescik04.diken.game.Node;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.function.Consumer;

public class BasicObjectsPanel extends DockablePanel {

    private static final long serialVersionUID = 1L;

    public BasicObjectsPanel(Consumer<Node> onDoubleClick) {
    	super("basic_objects_id", "Basit Objeler");
    	
        setLayout(new BorderLayout());
        setBackground(new Color(45, 45, 45));

        JPanel listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBackground(new Color(45, 45, 45));

        List<Node> nodeList = InstanceList.getNodeList();

        for (Node node : nodeList) {
            JPanel item = createItem(node, onDoubleClick);
            listPanel.add(item);
        }
        
        listPanel.add(Box.createVerticalGlue());

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(new Color(45, 45, 45));
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createItem(Node node, Consumer<Node> onDoubleClick) {
        JPanel item = new JPanel(new BorderLayout(8, 0));
        item.setBackground(new Color(45, 45, 45));
        item.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(35, 35, 35)),
            new EmptyBorder(6, 10, 6, 10)
        ));
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        try {
            Image icon = node.getNodeSettings().getLast().getKey().getImage().toImage();
            Image scaled = icon.getScaledInstance(20, 20, Image.SCALE_SMOOTH);
            JLabel iconLabel = new JLabel(new ImageIcon(scaled));
            item.add(iconLabel, BorderLayout.WEST);
        } catch (Exception e) {}
        
        var className = node.getClass().getSimpleName();

        JLabel nameLabel = new JLabel(className);
        nameLabel.setForeground(new Color(220, 220, 220));
        nameLabel.setFont(new Font("Tahoma", Font.PLAIN, 13));
        item.add(nameLabel, BorderLayout.CENTER);

        item.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    onDoubleClick.accept(node.copy());
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                item.setBackground(new Color(65, 65, 65));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                item.setBackground(new Color(45, 45, 45));
            }
        });
        
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        return item;
    }
}