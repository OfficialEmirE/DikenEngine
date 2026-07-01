package me.ramazanenescik04.diken.studio;

import javax.swing.JDialog;
import javax.swing.JFrame;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import java.awt.Image;
import java.io.IOException;
import java.net.URI;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.resource.IOResource;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.Font;
import javax.swing.JTabbedPane;
import javax.swing.JScrollPane;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import java.awt.Component;
import java.awt.Desktop;

public class AboutWindow extends JDialog {

	private static final long serialVersionUID = 1L;
	
	private static JLabel emirElogo;
	private static ImageIcon logo;
	
	static {loadJLabel();};

	/**
	 * Create the dialog.
	 * @param engineWindow 
	 */
	public AboutWindow(JFrame engineWindow) {
		super(engineWindow);
		
		setTitle(engineWindow.getTitle() + " Hakkında");
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 508, 597);
		setMaximumSize(new Dimension(508, 597));
		getContentPane().setLayout(new BorderLayout(0, 0));
		setResizable(false);
		setLocationRelativeTo(null);
		
		JPanel buttons = new JPanel();
		getContentPane().add(buttons, BorderLayout.SOUTH);
		
		JButton okButton = new JButton("Tamam");
		okButton.addActionListener(_ -> {
			dispose();
		});
		
		JButton btnNewButton = new JButton("GitHub Sayfası");
		btnNewButton.addActionListener(_ -> {
			if (Desktop.isDesktopSupported()) {
				try {
					Desktop.getDesktop().browse(URI.create("https://github.com/OfficialEmirE/DikenEngine"));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		buttons.add(btnNewButton);
		buttons.add(okButton);
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		getContentPane().add(tabbedPane, BorderLayout.CENTER);
		
		JPanel aboutPage = new JPanel();
		tabbedPane.addTab("Yapımcılar", null, new JScrollPane(aboutPage), null);
		aboutPage.setLayout(new BoxLayout(aboutPage, BoxLayout.Y_AXIS));
		
		Component verticalStrut_1 = Box.createVerticalStrut(5);
		aboutPage.add(verticalStrut_1);
		
		JLabel lblDikenengineInfo = new JLabel("DikenEngine, Oyun/Harita yapmanızı sağlayan 2B bir oyun motorudur");
		lblDikenengineInfo.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblDikenengineInfo.setAlignmentX(0.5f);
		aboutPage.add(lblDikenengineInfo);
		
		JLabel lblLicenseInfo = new JLabel("GNU GPL V2 Lisansını kullanmaktadır.");
		lblLicenseInfo.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblLicenseInfo.setAlignmentX(0.5f);
		aboutPage.add(lblLicenseInfo);
		
		Component verticalStrut = Box.createVerticalStrut(25);
		aboutPage.add(verticalStrut);
		
		JLabel lblGelitiriciler = new JLabel("Geliştiriciler");
		lblGelitiriciler.setFont(new Font("Arial", Font.PLAIN, 18));
		lblGelitiriciler.setAlignmentX(0.5f);
		aboutPage.add(lblGelitiriciler);
		
		aboutPage.add(Box.createVerticalStrut(5));
		
		JLabel text1 = new JLabel("Ana Geliştirici: Ramazanenescik04");
		text1.setFont(new Font("Arial", Font.PLAIN, 12));
		text1.setAlignmentX(Component.CENTER_ALIGNMENT);
		aboutPage.add(text1);
		
		aboutPage.add(Box.createVerticalStrut(25));
		
		JLabel lblKullanlanKtphaneler = new JLabel("Kullanılan Kütüphaneler");
		lblKullanlanKtphaneler.setFont(new Font("Arial", Font.PLAIN, 18));
		lblKullanlanKtphaneler.setAlignmentX(0.5f);
		aboutPage.add(lblKullanlanKtphaneler);
		
		aboutPage.add(Box.createVerticalStrut(5));
		
		JLabel lblLuaj = new JLabel("LuaJ - https://luaj.sourceforge.net");
		lblLuaj.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblLuaj.setAlignmentX(0.5f);
		aboutPage.add(lblLuaj);
		
		JLabel lblOrgjson = new JLabel("org.json - https://github.com/stleary/JSON-java");
		lblOrgjson.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblOrgjson.setAlignmentX(0.5f);
		aboutPage.add(lblOrgjson);
		
		JLabel lblLwjgl = new JLabel("LWJGL 2 - https://legacy.lwjgl.org/");
		lblLwjgl.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblLwjgl.setAlignmentX(0.5f);
		aboutPage.add(lblLwjgl);
		
		JLabel lblDockingFrames = new JLabel("Docking Frames - https://docking-frames.org/");
		lblDockingFrames.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblDockingFrames.setAlignmentX(0.5f);
		aboutPage.add(lblDockingFrames);
		
		JLabel lblRTextArea = new JLabel("RSyntaxTextArea - https://github.com/bobbylight/RSyntaxTextArea");
		lblRTextArea.setFont(new Font("Tahoma", Font.PLAIN, 12));
		lblRTextArea.setAlignmentX(0.5f);
		aboutPage.add(lblRTextArea);
		
		JPanel headerPanel = new JPanel();
		headerPanel.setOpaque(false);
		headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.X_AXIS));
		headerPanel.setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));

		if (logo != null) {
			headerPanel.add(new JLabel(logo));
			headerPanel.add(Box.createHorizontalStrut(14));
		}

		JPanel namePanel = new JPanel();
		namePanel.setOpaque(false);
		namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));

		JLabel nameLabel = new JLabel("DikenEngine");
		nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
		nameLabel.setForeground(Color.WHITE);

		JLabel versionLabel = new JLabel(DikenEngine.VERSION);
		versionLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
		versionLabel.setForeground(new Color(225, 225, 225));
		versionLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		
		emirElogo.setFont(new Font("Tahoma", Font.PLAIN, 12));
		emirElogo.setAlignmentX(0.5f);

		namePanel.add(nameLabel);
		namePanel.add(versionLabel);
		headerPanel.add(namePanel);
		headerPanel.add(emirElogo);
		
		getContentPane().add(headerPanel, BorderLayout.NORTH);
	}
	
	private static void loadJLabel() {	
		if (emirElogo == null) {
			Image img;
			try {
				img = ImageIO.read(AboutWindow.class.getResource("/emire.png"));
			} catch (Exception e1) {
				e1.printStackTrace();
				img = IOResource.missingTexture.toImage();
			}
			
			emirElogo = new JLabel(new ImageIcon(img));
		}
		
		if (logo == null) {
			Image img;
			try {
				img = ImageIO.read(AboutWindow.class.getResource("/icon.png")).getScaledInstance(64, 64, Image.SCALE_FAST);
			} catch (Exception e1) {
				e1.printStackTrace();
				img = IOResource.missingTexture.toImage();
			}
			
			logo = new ImageIcon(img);
		}
	}
}
