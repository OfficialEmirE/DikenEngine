package me.ramazanenescik04.diken.studio;

import javax.swing.JDialog;
import javax.swing.JFrame;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import java.awt.Image;
import java.io.IOException;
import java.net.URI;

import javax.swing.SwingConstants;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.resource.IOResource;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import java.awt.Font;
import javax.swing.JTabbedPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.JScrollPane;
import javax.swing.Box;
import javax.swing.BoxLayout;
import java.awt.Component;
import java.awt.Desktop;

public class AboutWindow extends JDialog {

	private static final long serialVersionUID = 1L;
	
	private static JLabel licenseText;
	private static String license;
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
		
		JScrollPane scrollPane = new JScrollPane(licenseText);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		tabbedPane.addTab("GNU GPL Lisansı", null, scrollPane, null);
		
		JLabel engineText = new JLabel("<html><h2>DikenEngine</h2><h4>" + DikenEngine.VERSION + "</h4></html>");
		engineText.setIcon(logo);
		engineText.setHorizontalAlignment(SwingConstants.CENTER);
		engineText.setFont(new Font("Arial", Font.PLAIN, 20));
		getContentPane().add(engineText, BorderLayout.NORTH);
	}
	
	private static void loadJLabel() {
		var loadLicense = license;
		if (loadLicense == null) {
			try {
				var text = new String(AboutWindow.class.getResourceAsStream("/about/LICENSE").readAllBytes());
				loadLicense = "<html><center>" + text.replaceAll("\n", "<br>") + "</center></html>";
			} catch (IOException e) {
				e.printStackTrace();
				loadLicense = "Lisans Yükleme Başarısızlıkla Sonuçlandı.";
			}
			
			license = loadLicense;
		}
		
		if (licenseText == null) {
			licenseText = new JLabel(loadLicense);
			licenseText.setHorizontalAlignment(SwingConstants.CENTER);
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
