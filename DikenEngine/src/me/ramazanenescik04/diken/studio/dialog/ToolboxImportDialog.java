package me.ramazanenescik04.diken.studio.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;
import me.ramazanenescik04.diken.studio.builders.Buttonbar;
import me.ramazanenescik04.diken.studio.dockables.ToolboxPanel.ToolboxItem;

public class ToolboxImportDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private static final int ICON_SIZE = 64;
	private static final int PADDING = 12;

	private ToolboxItem item;
	private Consumer<ToolboxImportDialog> consumer;
	private JTable table; // field olarak tut, sonra seçilenleri okumak için lazım
	private boolean isCancelled;

	public ToolboxImportDialog(Frame owner, ToolboxItem item, Consumer<ToolboxImportDialog> consumer) {
		super(owner, "Import Toolbox", true);
		this.item = item;
		this.consumer = consumer;
		setSize(640, 480);
		setLocationRelativeTo(owner);

		Buttonbar.Builder builder = new Buttonbar.Builder();
		var mainButtons = builder.create("testing");

		builder.addButton(mainButtons, "ImportButton", "Import", () -> pressButton(false));
		builder.addButton(mainButtons, "CancelButton", "Cancel", () -> pressButton(true));

		// Ana içerik paneli: kenarlarda boşluk
		JPanel content = new JPanel(new BorderLayout(0, PADDING));
		content.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));
		content.add(createItem(), BorderLayout.NORTH);
		content.add(createTable(), BorderLayout.CENTER);

		getContentPane().add(content, BorderLayout.CENTER);
		getContentPane().add(builder.convert(), BorderLayout.SOUTH);
	}

	private void pressButton(boolean cancelled) {
		this.isCancelled = cancelled;

		if (consumer != null) {
			consumer.accept(this);
		}
		
		this.dispose();
	}

	public boolean isCancelled() {
		return isCancelled;
	}
	
	public List<Node.EIOptions> getEnabledImportOptions() {
		List<Node.EIOptions> result = new ArrayList<>();
		for (int row = 0; row < table.getRowCount(); row++) {
			if ((Boolean) table.getValueAt(row, 0)) {
				var name = (String) table.getValueAt(row, 1);
				result.add(Node.EIOptions.valueOf(name));
			}
		}
		return result;
	}

	private JPanel createItem() {
		JPanel panel = new JPanel(new BorderLayout(PADDING, 0));

		JLabel iconLabel = new JLabel();
		iconLabel.setPreferredSize(new Dimension(ICON_SIZE, ICON_SIZE));
		iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
		iconLabel.setVerticalAlignment(SwingConstants.CENTER);
		iconLabel.setIcon(getIcon(item.icon()));
		iconLabel.setBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)));

		panel.add(iconLabel, BorderLayout.WEST);

		// İsim + tip
		JPanel textPanel = new JPanel();
		textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
		textPanel.setOpaque(false);

		JLabel nameLabel = new JLabel(item.name());
		nameLabel.setForeground(new Color(220, 220, 220));
		nameLabel.setFont(new Font("Tahoma", Font.BOLD, 16));

		JLabel typeLabel = new JLabel("By: " + item.author() + " | Type: " + item.type());
		typeLabel.setForeground(new Color(140, 140, 140));
		typeLabel.setFont(new Font("Tahoma", Font.PLAIN, 12));

		textPanel.add(Box.createVerticalGlue());
		textPanel.add(nameLabel);
		textPanel.add(Box.createVerticalStrut(4));
		textPanel.add(typeLabel);
		textPanel.add(Box.createVerticalGlue());

		panel.add(textPanel, BorderLayout.CENTER);

		return panel;
	}

	private JScrollPane createTable() {
		String[] columns = { "Enabled", "Import Options" };
		DefaultTableModel model = new DefaultTableModel(columns, 0) {
			private static final long serialVersionUID = 1L;

			@Override
			public Class<?> getColumnClass(int columnIndex) {
				// 0. kolon Boolean olunca checkbox olarak gösterilir
				return columnIndex == 0 ? Boolean.class : String.class;
			}

			@Override
			public boolean isCellEditable(int row, int column) {
				// sadece checkbox kolonu tıklanabilir
				return column == 0;
			}
		};
		
		for (Node.EIOptions option : Node.EIOptions.values()) {
			model.addRow(new Object[] {false, option.name()});
		}

		table = new JTable(model);
		table.setRowHeight(24);
		table.setFillsViewportHeight(true);

		// Checkbox kolonunu dar tut
		table.getColumnModel().getColumn(0).setMaxWidth(60);

		return new JScrollPane(table);
	}

	private ImageIcon getIcon(Bitmap icon) {
		icon = Objects.requireNonNullElse(icon, ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(1, 1));
		try {
			Image img = icon.toImage();
			Image scaled = img.getScaledInstance(ICON_SIZE, ICON_SIZE, Image.SCALE_FAST);
			return new ImageIcon(scaled);
		} catch (Exception e) {
			return null;
		}
	}
}