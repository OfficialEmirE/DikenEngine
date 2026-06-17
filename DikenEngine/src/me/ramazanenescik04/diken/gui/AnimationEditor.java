package me.ramazanenescik04.diken.gui;

import java.awt.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.IOException;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.Animation;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.EnumResource;
import me.ramazanenescik04.diken.resource.IOResource;

/**
 * Represents the `AnimationEditor` type within the DikenEngine `game` package.
 */
public class AnimationEditor extends JFrame {
    private static final long serialVersionUID = 1L;
	private final Animation animation;
    private final DefaultListModel<String> frameListModel;
    private final JList<String> frameList;
    private final JLabel previewLabel;
    private final JButton playBtn;
    private Timer playTimer;
    private boolean playing = false;

    public AnimationEditor(Animation animation) {
        super("PoAnimation");
        this.animation = animation;

        // --- Liste ---
        frameListModel = new DefaultListModel<>();
        frameList = new JList<>(frameListModel);
        JScrollPane scrollPane = new JScrollPane(frameList);

        // --- Önizleme ---
        previewLabel = new JLabel("Önizleme Yok", SwingConstants.CENTER);
        previewLabel.setPreferredSize(new Dimension(256, 256));
        previewLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        // --- Butonlar ---
        JButton addBtn = new JButton("Frame Ekle");
        JButton removeBtn = new JButton("Frame Sil");
        playBtn = new JButton("Oynat");
        JButton fpsBtn = new JButton("FPS Ayarla");

        addBtn.addActionListener(_ -> addFrames());
        removeBtn.addActionListener(_ -> removeSelectedFrame());
        playBtn.addActionListener(_ -> togglePlay());
        fpsBtn.addActionListener(_ -> setFPS());

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addBtn);
        buttonPanel.add(removeBtn);
        buttonPanel.add(playBtn);
        buttonPanel.add(fpsBtn);

        // --- Düzen ---
        getContentPane().setLayout(new BorderLayout(8, 8));
        getContentPane().add(scrollPane, BorderLayout.WEST);
        getContentPane().add(previewLabel, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        // --- Timer ---
        playTimer = new Timer(1000 / animation.getFPS(), _ -> updatePreview());

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(640, 400);
        setLocationRelativeTo(null);
        
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        
        JMenu mnDosya = new JMenu("Dosya");
        menuBar.add(mnDosya);
        
        JMenuItem mnıtmAnimasyonDosyasYkle = new JMenuItem("Aç");
        mnıtmAnimasyonDosyasYkle.addActionListener(_ -> loadAnimationFile());
        mnDosya.add(mnıtmAnimasyonDosyasYkle);
        
        JMenuItem mnıtmAnimasyonDosyasnKaydet = new JMenuItem("Kaydet");
        mnıtmAnimasyonDosyasnKaydet.addActionListener(_ -> saveAnimationFile());
        mnDosya.add(mnıtmAnimasyonDosyasnKaydet);
        
        JMenuItem mnıtmResimleriDaryaAktar = new JMenuItem("Kareleri Dışarıya Aktar");
        mnıtmResimleriDaryaAktar.addActionListener(_ -> saveAnimationFrames());
        mnDosya.add(mnıtmResimleriDaryaAktar);
        
        JMenu mnYardm = new JMenu("Yardım");
        menuBar.add(mnYardm);
        
        JMenuItem mnıtmHakknda = new JMenuItem("Hakkında");
        mnıtmHakknda.addActionListener(_ -> JOptionPane.showMessageDialog(this, "PoAnimation \nYapan: Ramazanenescik04\n\nSürüm: " + DikenEngine.VERSION));
        mnYardm.add(mnıtmHakknda);
        setVisible(true);
    }

    private void saveAnimationFrames() {
    	JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
        	try {
				for (int i = 0; i < this.animation.getFrameCount(); i++) {
					ImageIO.write(this.animation.getFrame(i).toImage(), "png", new File(chooser.getSelectedFile(), "Frame" + i + ".png"));
				}
				JOptionPane.showMessageDialog(this, "Başarıyla Frameler Dışarıya Aktarıldı!");
			} catch (Exception e) {
				JOptionPane.showMessageDialog(this, "Kaydedilemedi: " + e.getMessage());
			}
        }
	}

	private void addFrames() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Resim Dosyaları", "png", "jpg", "bmp"));
        chooser.setMultiSelectionEnabled(true); // ÇOKLU SEÇİM!
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File[] files = chooser.getSelectedFiles();
            for (File file : files) {
                Bitmap bmp = (Bitmap) IOResource.loadResource(IOResource.createFileStream(file), EnumResource.IMAGE);
                animation.addFrame(bmp);
                frameListModel.addElement(file.getName());
            }
            if (animation.getFrameCount() > 0) {
                Bitmap first = animation.getFrame(0);
                previewLabel.setText("");
                previewLabel.setIcon(new ImageIcon(first.toImage()));
            }
        }
    }
    
    private void loadAnimationFile() {
    	JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Animasyon Dosyası", "bin"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
        	try {
                File file = chooser.getSelectedFile();
                Animation loaded = (Animation) IOResource.loadResource(IOResource.createFileStream(file), EnumResource.ANIMATION);

                // mevcut animasyonu değiştir
                animation.setFPS(loaded.getFPS());
                animation.clearFrames();
                for (int i = 0; i < loaded.getFrameCount(); i++)
                    animation.addFrame(loaded.getFrame(i));

                // listeyi yenile
                frameListModel.clear();
                for (int i = 0; i < loaded.getFrameCount(); i++)
                    frameListModel.addElement("Frame " + i);

                previewLabel.setIcon(new ImageIcon(animation.getFrame(0).toImage()));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Yüklenemedi: " + ex.getMessage());
            }
        }
    }
    
    private void saveAnimationFile() {
    	JFileChooser chooser = new JFileChooser();
        chooser.setFileFilter(new FileNameExtensionFilter("Animasyon Dosyası", "bin"));
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
        	try {
				this.animation.save(chooser.getSelectedFile());
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
    }

    private void removeSelectedFrame() {
        int index = frameList.getSelectedIndex();
        if (index >= 0) {
            animation.removeFrame(index);
            frameListModel.remove(index);
            if (frameListModel.isEmpty()) {
                previewLabel.setText("Önizleme Yok");
                previewLabel.setIcon(null);
            }
        }
    }

    private void togglePlay() {
        if (playing) {
            playTimer.stop();
            playBtn.setText("Oynat");
        } else {
            playTimer.start();
            playBtn.setText("Durdur");
        }
        playing = !playing;
    }

    private void setFPS() {
        String input = JOptionPane.showInputDialog(this, "Yeni FPS:", animation.getFPS());
        try {
            int fps = Integer.parseInt(input);
            animation.setFPS(fps);
            playTimer.setDelay(1000 / fps);
        } catch (NumberFormatException ignored) {}
    }

    private void updatePreview() {
        animation.update(System.currentTimeMillis());
        Bitmap frame = animation.getCurrentFrame();
        if (frame != null) {
            previewLabel.setText("");
            previewLabel.setIcon(new ImageIcon(frame.toImage()));
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AnimationEditor(new Animation(8)));
    }
}
