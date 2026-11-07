package me.ramazanenescik04.diken.studio.dialog;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.studio.builders.Buttonbar;

public class NodeEIDialog extends JDialog {
	private static final long serialVersionUID = 1L;

	private static final int PADDING = 12;

	private Consumer<NodeEIDialog> consumer;
	private JTable table; // field olarak tut, sonra seçilenleri okumak için lazım
	private boolean isCancelled;

	public NodeEIDialog(Frame owner, Consumer<NodeEIDialog> consumer, boolean importMode) {
		super(owner, "Please Select Optional " + (importMode ? "Import" : "Export") + " Options", true);
		this.consumer = consumer;
		setSize(640, 480);
		setLocationRelativeTo(owner);

		Buttonbar.Builder builder = new Buttonbar.Builder();
		var mainButtons = builder.create("testing");

		builder.addButton(mainButtons, "ImportButton", importMode ? "Import" : "Export", () -> pressButton(false));
		builder.addButton(mainButtons, "CancelButton", "Cancel", () -> pressButton(true));

		// Ana içerik paneli: kenarlarda boşluk
		JPanel content = new JPanel(new BorderLayout(0, PADDING));
		content.setBorder(BorderFactory.createEmptyBorder(PADDING, PADDING, PADDING, PADDING));
		content.add(createTable(), BorderLayout.CENTER);

		getContentPane().add(content, BorderLayout.CENTER);
		getContentPane().add(builder.convert(), BorderLayout.SOUTH);
	}

	private void pressButton(boolean cancelled) {
		this.isCancelled = cancelled;
		
		if (consumer != null)
			consumer.accept(this);
		
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
}