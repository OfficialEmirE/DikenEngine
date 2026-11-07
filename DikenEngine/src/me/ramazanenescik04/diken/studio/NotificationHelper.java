package me.ramazanenescik04.diken.studio;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.util.ArrayList;
import java.util.List;

public class NotificationHelper {
    private final JLayeredPane pane;
    private final List<JPanel> activeNotifications;

    // Kompakt & Modern Ölçüler
    private final int notificationWidth = 320;
    private final int notificationHeight = 68;
    private final int padding = 16;
    private final int spacing = 10;

    public NotificationHelper(JLayeredPane pane) {
        this.pane = pane;
        this.activeNotifications = new ArrayList<>();

        this.pane.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                repositionNotifications();
            }
        });
    }

    /**
     * Gelişmiş Bildirim Gösterici
     * 
     * @param title Başlık (Örn: "Başarılı", "Hata")
     * @param message Açıklama metni
     * @param messageType JOptionPane ikon tipi (JOptionPane.INFORMATION_MESSAGE, JOptionPane.ERROR_MESSAGE vb.)
     * @param timeoutMs Kaç ms sonra kapanacağı (0 verilirse manuel kapanır)
     */
    public void showNotification(String title, String message, int messageType, int timeoutMs) {
        if (pane.getWidth() == 0 || pane.getHeight() == 0) {
            SwingUtilities.invokeLater(() -> showNotification(title, message, messageType, timeoutMs));
            return;
        }

        JPanel notificationCard = createNotificationCard(title, message, messageType);

        pane.add(notificationCard, JLayeredPane.POPUP_LAYER);
        pane.moveToFront(notificationCard);
        activeNotifications.add(notificationCard);

        repositionNotifications();

        if (timeoutMs > 0) {
            Timer timer = new Timer(timeoutMs, _ -> dismissNotification(notificationCard));
            timer.setRepeats(false);
            timer.start();
        }
    }

    // Aşırı yüklenmiş (Overloaded) kolay kullanım metodları
    public void showNotification(String title, String message, int messageType) {
        showNotification(title, message, messageType, 4000);
    }

    public void showNotification(String title, String message) {
        showNotification(title, message, JOptionPane.INFORMATION_MESSAGE, 4000);
    }

    private JPanel createNotificationCard(String title, String message, int messageType) {
        // Kart Ana Gövdesi (Yuvarlatılmış Kenarlı Panel)
        JPanel card = new JPanel(new BorderLayout(12, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Arka Plan
                g2.setColor(new Color(38, 42, 51));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                
                // İnce Çerçeve
                g2.setColor(new Color(60, 66, 78));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
                g2.dispose();
            }
        };
        
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 10));

        // Tıklamaların arkadaki JDesktopPane / Butonlara geçmesini engelle
        card.addMouseListener(new MouseAdapter() {});

        // --- 1. SOL İKON (JOptionPane'dan Çalma) ---
        Icon optionPaneIcon = getIconForType(messageType);
        if (optionPaneIcon != null) {
            // İkonu orantılı bir şekilde ölçeklendir
            Icon scaledIcon = scaleIcon(optionPaneIcon, 24, 24);
            JLabel lblIcon = new JLabel(scaledIcon);
            lblIcon.setVerticalAlignment(SwingConstants.TOP);
            card.add(lblIcon, BorderLayout.WEST);
        }

        // --- 2. ORTA METİN ALANI (Başlık + Açıklama) ---
        JPanel textContainer = new JPanel();
        textContainer.setLayout(new BoxLayout(textContainer, BoxLayout.Y_AXIS));
        textContainer.setOpaque(false);

        // Başlık Label'ı
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(new Color(240, 240, 240));

        // Açıklama Label'ı (Uzunsa kesilip ... konur)
        JLabel lblMessage = new JLabel("" + message + "");
        lblMessage.setFont(new Font("Segoe UI", Font.PLAIN, 11));

        textContainer.add(lblTitle);
        textContainer.add(Box.createVerticalStrut(2)); // İki metin arası boşluk
        textContainer.add(lblMessage);

        card.add(textContainer, BorderLayout.CENTER);

        // --- 3. SAĞ KAPATMA DÜĞMESİ (✕) ---
        JButton btnClose = new JButton("✕") {
            @Override
            protected void paintComponent(Graphics g) {
                // Hover efekti
                if (getModel().isRollover()) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(255, 255, 255, 30));
                    g2.fillRect(0, 0, getWidth(), getHeight());
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        
        btnClose.setFont(new Font("SansSerif", Font.BOLD, 11));
        btnClose.setForeground(new Color(140, 145, 155));
        btnClose.setFocusPainted(false);
        btnClose.setBorderPainted(false);
        btnClose.setContentAreaFilled(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.setPreferredSize(new Dimension(20, 20));
        btnClose.setVerticalAlignment(SwingConstants.TOP);

        btnClose.addActionListener(_ -> dismissNotification(card));

        card.add(btnClose, BorderLayout.EAST);

        return card;
    }

    /**
     * JOptionPane varsayılan ikonlarını çeker.
     */
    private Icon getIconForType(int messageType) {
        return switch (messageType) {
            case JOptionPane.ERROR_MESSAGE -> UIManager.getIcon("OptionPane.errorIcon");
            case JOptionPane.WARNING_MESSAGE -> UIManager.getIcon("OptionPane.warningIcon");
            case JOptionPane.QUESTION_MESSAGE -> UIManager.getIcon("OptionPane.questionIcon");
            case JOptionPane.PLAIN_MESSAGE -> null;
            default -> UIManager.getIcon("OptionPane.informationIcon");
        };
    }

    /**
     * Çekilen ikonu bildirime uygun boyuta (örn: 24x24) getirir.
     */
    private Icon scaleIcon(Icon icon, int width, int height) {
        if (icon instanceof ImageIcon imageIcon) {
            Image img = imageIcon.getImage();
            Image resizedImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(resizedImg);
        }
        return icon;
    }

    private void dismissNotification(JPanel card) {
        if (activeNotifications.contains(card)) {
            activeNotifications.remove(card);
            pane.remove(card);
            pane.revalidate();
            pane.repaint();
            repositionNotifications();
        }
    }

    private void repositionNotifications() {
        int paneWidth = pane.getWidth();
        int paneHeight = pane.getHeight();

        if (paneWidth == 0 || paneHeight == 0) return;

        for (int i = 0; i < activeNotifications.size(); i++) {
            JPanel card = activeNotifications.get(i);

            // Sağ Alt Hizalama
            int x = paneWidth - notificationWidth - padding;
            int y = paneHeight - ((i + 1) * (notificationHeight + spacing)) - padding;

            card.setBounds(x, y, notificationWidth, notificationHeight);
        }

        pane.revalidate();
        pane.repaint();
    }
}