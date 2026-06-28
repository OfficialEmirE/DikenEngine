package me.ramazanenescik04.diken.studio.dockables;

import me.ramazanenescik04.diken.game.InstanceList;
import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
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

        Map<InstanceList.CategoryKey, List<Node>> nodeList = InstanceList.getTypedNodes();

        for (Map.Entry<InstanceList.CategoryKey, List<Node>> category : nodeList.entrySet()) {
        	listPanel.add(createBorder(category.getKey()));
        	var list = category.getValue();
        	
        	list.sort(Comparator.comparing(node -> node.getClass().getSimpleName()));
        	
        	int i = 0;
            for (Node obj : list) {
                JPanel item = createItem(obj, onDoubleClick, i);
                listPanel.add(item);
                
                i++;
            }
        }
        
        listPanel.add(Box.createVerticalGlue());

        JScrollPane scrollPane = new JScrollPane(listPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBackground(new Color(45, 45, 45));
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createBorder(InstanceList.CategoryKey key) {
    	JPanel item = new JPanel(new BorderLayout(8, 0));
        item.setBackground(new Color(70, 70, 70));
        item.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(35, 35, 35)),
            new EmptyBorder(6, 10, 6, 10)
        ));
        
        try {
            Image icon = ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(key.iconX(), key.iconY()).toImage();
            Image scaled = icon.getScaledInstance(20, 20, Image.SCALE_SMOOTH);
            JLabel iconLabel = new JLabel(new ImageIcon(scaled));
            item.add(iconLabel, BorderLayout.WEST);
        } catch (Exception e) {}
        
        JLabel nameLabel = new JLabel(key.displayName());
        nameLabel.setForeground(new Color(220, 220, 220));
        nameLabel.setFont(new Font("Tahoma", Font.PLAIN, 13));
        item.add(nameLabel, BorderLayout.CENTER);
        
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        return item;
	}

	private JPanel createItem(Node node, Consumer<Node> onDoubleClick, int i) {
        JPanel item = new JPanel(new BorderLayout(8, 0));
        if (i % 2 == 0) {
        	item.setBackground(new Color(45, 45, 45));
        } else {
        	item.setBackground(new Color(50, 50, 50));
        }
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
            	if (i % 2 == 0) {
                	item.setBackground(new Color(45, 45, 45));
                } else {
                	item.setBackground(new Color(50, 50, 50));
                }
            }
        });
        
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        return item;
    }
}