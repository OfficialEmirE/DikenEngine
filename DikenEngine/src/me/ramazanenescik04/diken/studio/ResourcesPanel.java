package me.ramazanenescik04.diken.studio;

import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.EnumResource;
import me.ramazanenescik04.diken.resource.IOResource;
import me.ramazanenescik04.diken.resource.IResource;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.util.Map;

public class ResourcesPanel extends DockablePanel {

    private static final long serialVersionUID = 1L;

    private World theWorld;
    private JPanel listPanel;
    private JFrame parentFrame;

    private String selectedKey = null;

    public ResourcesPanel(World world, JFrame parentFrame) {
    	super("resources_panel", "Kaynaklar");
    	
        this.theWorld = world;
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
        JToolBar toolBar = new JToolBar();
        toolBar.setBackground(new Color(60, 60, 60));

        JButton addButton = new JButton("Ekle");
        addButton.addActionListener(_ -> addResourceDialog());
        toolBar.add(addButton);

        JButton removeButton = new JButton("Kaldır");
        removeButton.addActionListener(_ -> removeSelectedResource());
        toolBar.add(removeButton);

        add(toolBar, BorderLayout.NORTH);

        rebuildList();
    }

    private void rebuildList() {
        listPanel.removeAll();

        for (Map.Entry<String, IResource> entry : theWorld.resources.entrySet()) {
            String key = entry.getKey();
            IResource resource = entry.getValue();

            if (key.equals("empty")) continue; // varsayılan boş resource'u gösterme

            JPanel item = createItem(key, resource);
            listPanel.add(item);
        }

        listPanel.revalidate();
        listPanel.repaint();
    }

    private JPanel createItem(String key, IResource resource) {
        JPanel item = new JPanel(new BorderLayout(8, 0));
        item.setBackground(getItemBackground(key));
        item.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(35, 35, 35)),
            new EmptyBorder(4, 8, 4, 8)
        ));
        item.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        item.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        // Önizleme / ikon
        JLabel iconLabel = new JLabel();
        iconLabel.setPreferredSize(new Dimension(28, 28));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);

        if (resource instanceof Bitmap bitmap) {
            try {
                Image img = bitmap.toImage();
                Image scaled = img.getScaledInstance(28, 28, Image.SCALE_SMOOTH);
                iconLabel.setIcon(new ImageIcon(scaled));
            } catch (Exception e) {
                iconLabel.setText("?");
                iconLabel.setForeground(Color.GRAY);
            }
        } else {
            // Diğer tipler için tip baş harfi göster
            iconLabel.setText(resource.getResourceType().name().substring(0, 1));
            iconLabel.setForeground(new Color(180, 180, 180));
            iconLabel.setFont(new Font("Tahoma", Font.BOLD, 14));
        }
        item.add(iconLabel, BorderLayout.WEST);

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

    private Color getItemBackground(String key) {
        return key.equals(selectedKey) ? new Color(75, 90, 110) : new Color(45, 45, 45);
    }

    private void addResourceDialog() {
        // Resource tipi seçimi
        EnumResource[] types = EnumResource.values();
        EnumResource selectedType = (EnumResource) JOptionPane.showInputDialog(
            parentFrame,
            "Kaynak tipini seçin:",
            "Kaynak Ekle",
            JOptionPane.PLAIN_MESSAGE,
            null,
            types,
            types[0]
        );

        if (selectedType == null) return;

        // Dosya seçimi
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Kaynak Dosyası Seç");
        
        if (selectedType == EnumResource.IMAGE || selectedType == EnumResource.CURSOR) {
        	fileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", "png", "jpg", "jpeg", "bmp", "gif"));
		} else if (selectedType == EnumResource.SOUND) {
			fileChooser.setFileFilter(new FileNameExtensionFilter("Wave Files", "wav"));
		} else if (selectedType == EnumResource.ANIMATION) {
			fileChooser.setFileFilter(new FileNameExtensionFilter("Animation Files", "bin", "anim"));
		}

        int result = fileChooser.showOpenDialog(parentFrame);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File selectedFile = fileChooser.getSelectedFile();

        // Resource ismi
        String defaultName = selectedFile.getName();
        int dotIndex = defaultName.lastIndexOf('.');
        if (dotIndex > 0) defaultName = defaultName.substring(0, dotIndex);

        String resourceName = JOptionPane.showInputDialog(
            parentFrame,
            "Kaynak adı:",
            defaultName
        );

        if (resourceName == null || resourceName.trim().isEmpty()) return;
        resourceName = resourceName.trim();

        if (theWorld.resources.containsKey(resourceName)) {
            int overwrite = JOptionPane.showConfirmDialog(
                parentFrame,
                "\"" + resourceName + "\" zaten mevcut. Üzerine yazılsın mı?",
                "Uyarı",
                JOptionPane.YES_NO_OPTION
            );
            if (overwrite != JOptionPane.YES_OPTION) return;
        }

        try (FileInputStream fis = new FileInputStream(selectedFile)) {
            IResource resource = IOResource.loadResource(fis, selectedType);
            theWorld.addResource(resourceName, resource);
            rebuildList();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(
                parentFrame,
                "Kaynak yüklenemedi: " + e.getMessage(),
                "Hata",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void removeSelectedResource() {
        if (selectedKey == null) return;

        int confirm = JOptionPane.showConfirmDialog(
            parentFrame,
            "\"" + selectedKey + "\" kaynağını kaldırmak istediğine emin misin?",
            "Onay",
            JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        theWorld.removeResource(selectedKey);
        selectedKey = null;
        rebuildList();
    }

    public String getSelectedResourceKey() {
        return selectedKey;
    }
}