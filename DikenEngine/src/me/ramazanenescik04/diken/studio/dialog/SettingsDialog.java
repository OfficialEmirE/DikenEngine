package me.ramazanenescik04.diken.studio.dialog;

import java.awt.*;
import java.util.List;

import javax.swing.*;

import me.ramazanenescik04.diken.game.setting.Setting;
import me.ramazanenescik04.diken.game.setting.SettingCategory;
import me.ramazanenescik04.diken.language.Lang;
import me.ramazanenescik04.diken.studio.SettingsManager;
import me.ramazanenescik04.diken.studio.StudioPanel;
import me.ramazanenescik04.diken.studio.builders.SettingEditorFactory;

public class SettingsDialog extends JDialog {
	private static final long serialVersionUID = -346711513953658371L;
	private final CardLayout cardLayout = new CardLayout();
    private final JPanel cardsPanel = new JPanel(cardLayout);
    private final DefaultListModel<SettingCategory> categoryModel = new DefaultListModel<>();

    public SettingsDialog(Frame owner, StudioPanel panel) {
        super(owner, Lang.get("studio.windows.settings"), true);
        setSize(760, 480);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        JList<SettingCategory> categoryList = new JList<>(categoryModel);
        categoryList.setFixedCellHeight(25);
        categoryList.setCellRenderer(new CategoryCellRenderer());
        categoryList.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        for (SettingCategory category : SettingsManager.getCategories()) {
            categoryModel.addElement(category);
            cardsPanel.add(buildCategoryPanel(category.getSettings()), category.getKey().getId());
        }

        categoryList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && categoryList.getSelectedValue() != null) {
                cardLayout.show(cardsPanel, categoryList.getSelectedValue().getKey().getId());
            }
        });
        if (!categoryModel.isEmpty()) categoryList.setSelectedIndex(0);

        JScrollPane categoryScroll = new JScrollPane(categoryList);
        categoryScroll.setPreferredSize(new Dimension(190, 0));

        JButton closeButton = new JButton(Lang.get("close"));
        closeButton.addActionListener(_ -> {
            SettingsManager.save();
            dispose();
        });
        
        JButton resetButton = new JButton(Lang.get("reset"));
        resetButton.addActionListener(_ -> {
			int option = JOptionPane.showConfirmDialog(this, Lang.get("studio.windows.settings.resetSettingWarning"),
					"Confirm Reset Config", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (option == JOptionPane.YES_OPTION) {
				panel.stop();
				
				SettingsManager.removeConfig();
	            dispose();
			}
        });
        
        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        southPanel.add(resetButton);
        southPanel.add(closeButton);

        add(categoryScroll, BorderLayout.WEST);
        add(cardsPanel, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);
    }

    private JPanel buildCategoryPanel(List<Setting<?>> settings) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        for (Setting<?> setting : settings) {
            panel.add(buildSettingRow(setting));
            panel.add(Box.createVerticalStrut(10));
        }
        panel.add(Box.createVerticalGlue());

        JScrollPane scroll = new JScrollPane(panel);
        scroll.getVerticalScrollBar().setUnitIncrement(16);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    private JPanel buildSettingRow(Setting<?> setting) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));

        JLabel label = new JLabel(setting.getName());
        if (!setting.getDescription().isEmpty()) label.setToolTipText(setting.getDescription());
        label.setPreferredSize(new Dimension(180, 24));

        JComponent editor = SettingEditorFactory.create(setting);
        editor.setEnabled(setting.isChangeable());

        row.add(label, BorderLayout.WEST);
        row.add(editor, BorderLayout.CENTER);
        return row;
    }

    private static class CategoryCellRenderer extends DefaultListCellRenderer {
		private static final long serialVersionUID = 1L;

		@Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof SettingCategory category) {
                label.setText(category.getKey().getCategory());
                label.setIcon(new ImageIcon(category.getKey().getImage().toImage()));
                label.setIconTextGap(10);
                label.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
            }
            return label;
        }
    }
}