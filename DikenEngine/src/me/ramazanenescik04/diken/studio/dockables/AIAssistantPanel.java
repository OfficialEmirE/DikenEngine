package me.ramazanenescik04.diken.studio.dockables;
import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class AIAssistantPanel extends DockablePanel {

	private static final long serialVersionUID = 1L;

	/**
	 * Create the panel.
	 */
	public AIAssistantPanel() {
		super("ai_assistant", "Assistant");
		setLayout(new BorderLayout(0, 0));
		
		JPanel panel = new JPanel();
		add(panel, BorderLayout.SOUTH);
		panel.setLayout(new BorderLayout(0, 0));
		panel.setPreferredSize(new Dimension(10, 70));
		
		JPanel panel_1 = new JPanel();
		panel.add(panel_1, BorderLayout.SOUTH);
		
		JButton btnNewButton = new JButton("Gönder");
		panel_1.add(btnNewButton);
		
		JTextArea textArea = new JTextArea();
		panel.add(textArea, BorderLayout.CENTER);
		
		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);
	}

}
