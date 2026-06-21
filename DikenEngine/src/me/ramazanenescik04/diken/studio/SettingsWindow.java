package me.ramazanenescik04.diken.studio;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

public class SettingsWindow extends JDialog {

	private static final long serialVersionUID = 1L;

	public SettingsWindow(JFrame window) {
		super(window);
		setTitle("Ayarlar");
		setBounds(100, 100, 607, 471);
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.X_AXIS));
		
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		panel.add(new JButton("Button 1"));
		panel.add(new JSeparator(SwingConstants.HORIZONTAL));
		panel.add(new JButton("Button 2"));

		this.setContentPane(panel);
	}

}
