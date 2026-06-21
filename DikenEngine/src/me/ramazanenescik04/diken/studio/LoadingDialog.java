package me.ramazanenescik04.diken.studio;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.resource.IOResource;

public class LoadingDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private static final int WIDTH = 430;
	private static final int HEIGHT = 277;

	private final JLabel statusLabel;
	private final JProgressBar progressBar;

	public LoadingDialog() {
		setTitle("DikenEngine Studio Yükleniyor...");
		setUndecorated(true);
		setSize(WIDTH, HEIGHT);
		setLocationRelativeTo(null);
		setAlwaysOnTop(true);

		BackgroundPanel content = new BackgroundPanel(loadBackground());
		content.setLayout(new BorderLayout());
		setContentPane(content);

		JPanel headerPanel = new JPanel();
		headerPanel.setOpaque(false);
		headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.X_AXIS));
		headerPanel.setBorder(BorderFactory.createEmptyBorder(22, 22, 0, 22));

		ImageIcon logoIcon = loadLogoIcon();
		if (logoIcon != null) {
			headerPanel.add(new JLabel(logoIcon));
			headerPanel.add(Box.createHorizontalStrut(14));
		}

		JPanel namePanel = new JPanel();
		namePanel.setOpaque(false);
		namePanel.setLayout(new BoxLayout(namePanel, BoxLayout.Y_AXIS));

		JLabel nameLabel = new JLabel("DikenEngine");
		nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
		nameLabel.setForeground(Color.WHITE);
		nameLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);

		JLabel versionLabel = new JLabel(DikenEngine.VERSION);
		versionLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
		versionLabel.setForeground(new Color(225, 225, 225));
		versionLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);

		namePanel.add(nameLabel);
		namePanel.add(versionLabel);
		headerPanel.add(namePanel);

		content.add(headerPanel, BorderLayout.NORTH);

		JPanel footerPanel = new JPanel();
		footerPanel.setOpaque(false);
		footerPanel.setLayout(new BoxLayout(footerPanel, BoxLayout.Y_AXIS));
		footerPanel.setBorder(BorderFactory.createEmptyBorder(0, 16, 12, 16));

		statusLabel = new JLabel("Yükleniyor...");
		statusLabel.setForeground(Color.WHITE);
		statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		statusLabel.setAlignmentX(JLabel.LEFT_ALIGNMENT);
		statusLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));

		progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		progressBar.setPreferredSize(new Dimension(10, 14));
		progressBar.setMaximumSize(new Dimension(Short.MAX_VALUE, 14));
		progressBar.setAlignmentX(JLabel.LEFT_ALIGNMENT);

		footerPanel.add(statusLabel);
		footerPanel.add(progressBar);

		content.add(footerPanel, BorderLayout.SOUTH);
	}

	private BufferedImage loadBackground() {
		try {
			return ImageIO.read(DikenEngine.class.getResource("/background.png"));
		} catch (IOException | IllegalArgumentException e) {
			return IOResource.missingTexture.toImage();
		}
	}

	private ImageIcon loadLogoIcon() {
		try {
			Image img = ImageIO.read(DikenEngine.class.getResource("/icon.png"));
			Image scaled = img.getScaledInstance(64, 64, Image.SCALE_SMOOTH);
			return new ImageIcon(scaled);
		} catch (Exception e) {
			return null;
		}
	}

	public void setStatus(String text) {
		SwingUtilities.invokeLater(() -> statusLabel.setText(text));
	}

	/**
	 * Determinate moda gecer ve yuzdeyi gunceller (0-100).
	 */
	public void setProgress(int percent) {
		SwingUtilities.invokeLater(() -> {
			progressBar.setIndeterminate(false);
			progressBar.setValue(Math.max(0, Math.min(100, percent)));
		});
	}

	/**
	 * Indeterminate (belirsiz sure) moduna geri doner.
	 */
	public void setIndeterminate() {
		SwingUtilities.invokeLater(() -> progressBar.setIndeterminate(true));
	}

	private static class BackgroundPanel extends JPanel {

		private static final long serialVersionUID = 1L;

		private final BufferedImage background;

		BackgroundPanel(BufferedImage background) {
			this.background = background;
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (background != null) {
				g.drawImage(background, 0, 0, getWidth(), getHeight(), null);
			}
		}
	}
}
