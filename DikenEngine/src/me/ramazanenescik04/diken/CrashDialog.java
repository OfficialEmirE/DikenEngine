package me.ramazanenescik04.diken;

import java.awt.*;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.TitledBorder;

import me.ramazanenescik04.diken.tools.Utils;

import javax.swing.JLabel;

import javax.swing.SwingConstants;
import javax.swing.UIManager;

import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;

public class CrashDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	public static void crash(JFrame window, Throwable error) {
		new CrashDialog(window, error,
				"Oyun motoru sebebsiz yere çöktü. Lütfen \"Hatayı Kopyala\" ya basın ve sorunu bildirin!")
				.setVisible(true);
	}
	
	public static void crash(JFrame window, Throwable error, String optionalMessages) {
		new CrashDialog(window, error, optionalMessages).setVisible(true);
	}
	
	private CrashDialog(JFrame window, Throwable error, String msg) {
		super(window, true);
		
		var errorIcon = UIManager.getIcon("OptionPane.errorIcon");
		
		setResizable(false);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setIconImage(iconToImage(errorIcon));
		setTitle("DikenEngine Crashed (T_T)");
		setBounds(100, 100, 529, 653);
		setLocationRelativeTo(null);
		
		JPanel buttonsPanel = new JPanel();
		buttonsPanel.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		buttonsPanel.setPreferredSize(new Dimension(10, 44));
		getContentPane().add(buttonsPanel, BorderLayout.SOUTH);
		
		JCheckBox openIssuePageCBox = new JCheckBox("DikenEngine'nin Sorunlar Sayfasını Aç");
		openIssuePageCBox.setToolTipText("Sorunlar Sayfası (Issue), Oyun motorunun hatalarını bildirmek için bulunan sayfa.");
		
		JButton closeButton = new JButton("Kapat");
		closeButton.addActionListener(_ -> closeWindow(error, openIssuePageCBox.isSelected(), false));
		
		JButton copyErrorButtom = new JButton("Hatayı Kopyala");
		copyErrorButtom.addActionListener(_ -> closeWindow(error, openIssuePageCBox.isSelected(), true));
		
		GroupLayout gl_buttonsPanel = new GroupLayout(buttonsPanel);
		gl_buttonsPanel.setHorizontalGroup(
			gl_buttonsPanel.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_buttonsPanel.createSequentialGroup()
					.addGap(2)
					.addComponent(openIssuePageCBox, GroupLayout.DEFAULT_SIZE, 209, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(copyErrorButtom, GroupLayout.PREFERRED_SIZE, 122, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(closeButton, GroupLayout.PREFERRED_SIZE, 122, GroupLayout.PREFERRED_SIZE)
					.addContainerGap())
		);
		gl_buttonsPanel.setVerticalGroup(
			gl_buttonsPanel.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, gl_buttonsPanel.createSequentialGroup()
					.addGroup(gl_buttonsPanel.createParallelGroup(Alignment.TRAILING)
						.addComponent(copyErrorButtom, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 38, Short.MAX_VALUE)
						.addComponent(closeButton, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 38, Short.MAX_VALUE)
						.addComponent(openIssuePageCBox, Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 38, GroupLayout.PREFERRED_SIZE))
					.addContainerGap())
		);
		buttonsPanel.setLayout(gl_buttonsPanel);
		
		JPanel infoPanel = new JPanel();
		infoPanel.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		infoPanel.setPreferredSize(new Dimension(10, 50));
		getContentPane().add(infoPanel, BorderLayout.NORTH);
		infoPanel.setLayout(new BorderLayout(0, 0));
		
		JLabel errorLogo = new JLabel();
		errorLogo.setOpaque(false);
		errorLogo.setPreferredSize(new Dimension(50, 50));
		errorLogo.setHorizontalAlignment(SwingConstants.CENTER);
		errorLogo.setFont(new Font("Arial", Font.PLAIN, 33));
		errorLogo.setIcon(errorIcon);
		infoPanel.add(errorLogo, BorderLayout.WEST);
		
		JLabel infoText = new JLabel(msg);
		infoPanel.add(infoText, BorderLayout.CENTER);
		
		JScrollPane scrollPane = new JScrollPane();
		getContentPane().add(scrollPane, BorderLayout.CENTER);
		
		JTextArea throwTextArea = new JTextArea();
		throwTextArea.setEditable(false);
		scrollPane.setViewportView(throwTextArea);
		
		throwTextArea.setText(Utils.getStackTraceString(error));
	}
	
	private void closeWindow(Throwable _throw, boolean openIssue, boolean copyThrow) {
		if (copyThrow) {
			StringSelection selection = new StringSelection(Utils.getStackTraceString(_throw));
			Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);
		}
		
		if (openIssue && Desktop.isDesktopSupported()) {
			try {
				Desktop.getDesktop().browse(URI.create("https://github.com/OfficialEmirE/DikenEngine/issues"));
			} catch (IOException ignore) {}
		}
		
		dispose();
	}

	private static Image iconToImage(Icon icon) {
		if (icon instanceof ImageIcon) {
			return ((ImageIcon) icon).getImage();
		} else {
			int w = icon.getIconWidth();
			int h = icon.getIconHeight();
			
			BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
			Graphics2D g = image.createGraphics();
			icon.paintIcon(null, g, 0, 0);
			g.dispose();
			return image;
		}
	}
}
