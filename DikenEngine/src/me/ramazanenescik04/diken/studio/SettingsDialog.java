package me.ramazanenescik04.diken.studio;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import me.ramazanenescik04.diken.game.setting.Setting;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.IOResource;
import me.ramazanenescik04.diken.resource.ResourceLocator;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * DikenEngine Studio için jenerik, Setting&lt;T&gt; tabanlı ayarlar penceresi.
 *
 * Kullanımı çok basit:
 * <pre>
 *   SettingsDialog dialog = new SettingsDialog(owner, "Engine Settings");
 *   dialog.addSetting(new Setting&lt;&gt;("VSync", true, Boolean.class, EnumSettingType.CHECK_BOX));
 *   dialog.addSetting(new Setting&lt;&gt;("Resolution Scale", 1.0f, 0.25f, 2.0f, Float.class, EnumSettingType.SLIDER));
 *   dialog.setVisible(true);
 * </pre>
 *
 * Değer değişimleri doğrudan {@link Setting#setValue(Object)} üzerinden yapılır,
 * yani Setting'e {@code addChangeListener(...)} ile bağladığın her şey otomatik tetiklenir.
 * Bu dialog UI tarafını yönetir, gerçek "ayar mantığı" hep Setting sınıfında kalır.
 */
public class SettingsDialog extends JDialog {
	private static final long serialVersionUID = -5480430950209271244L;

	/** OBJECT_SELECT için özel seçim mantığı eklemek isteyenler bunu implement edebilir
     *  (örn. ExplorerPanel'deki Node ağacından seçim yaptırmak gibi). */
    public interface ObjectPicker {
        Object pick(Setting<?> setting, Component parent);
    }

    /** RESOURCE_SELECT için varsayılan JFileChooser yerine kendi ResourcesPanel'ini
     *  bağlamak isteyenler bunu kullanabilir. */
    public interface ResourcePicker {
        String pick(Setting<?> setting, Component parent);
    }

    private static final int SLIDER_FLOAT_SCALE = 1000; // float/double slider hassasiyeti

    private final List<Setting<?>> settings = new ArrayList<>();
    private final Map<Setting<?>, JComponent> componentMap = new LinkedHashMap<>();

    private final JPanel contentPanel;

    private ObjectPicker objectPicker;
    private ResourcePicker resourcePicker;

    public SettingsDialog(Window owner, String title) {
        super(owner, title, ModalityType.APPLICATION_MODAL);
        getContentPane().setLayout(new BorderLayout());

        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        getContentPane().add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton resetButton = new JButton("Varsayılanlara Dön");
        resetButton.setEnabled(false);
        resetButton.addActionListener(_ -> resetAll());
        JButton closeButton = new JButton("Kapat");
        closeButton.addActionListener(_ -> dispose());
        buttonPanel.add(resetButton);
        buttonPanel.add(closeButton);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        setSize(665, 425);
        setLocationRelativeTo(owner);
    }

    // ------------------------------------------------------------------
    // Genişletme hook'ları
    // ------------------------------------------------------------------

    public SettingsDialog setObjectPicker(ObjectPicker picker) {
        this.objectPicker = picker;
        return this;
    }

    public SettingsDialog setResourcePicker(ResourcePicker picker) {
        this.resourcePicker = picker;
        return this;
    }
    
    public SettingsDialog addSection(String title) {
    	return addSection(-1, -1, title);
    }

    public SettingsDialog addSection(int iconX, int iconY, String title) {
    	var icon = getIcon(iconX, iconY);
    	
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(60, 60, 60));
        headerPanel.setBorder(new EmptyBorder(4, 8, 4, 8));
        headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

        JLabel headerLabel = new JLabel(title);
        headerLabel.setForeground(new Color(200, 200, 200));
        headerLabel.setFont(new Font("Tahoma", Font.BOLD, 13));
        
        if(icon != null)
        	headerLabel.setIcon(icon);
        
        headerPanel.add(headerLabel, BorderLayout.WEST);
        contentPanel.add(headerPanel);
        return this;
    }

    // ------------------------------------------------------------------
    // Ayar ekleme
    // ------------------------------------------------------------------

    public SettingsDialog addSettings(Setting<?>... toAdd) {
        for (Setting<?> s : toAdd) addSetting(s);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> SettingsDialog addSetting(Setting<T> setting) {
        settings.add(setting);

        JPanel row = new JPanel(new GridLayout(1, 2, 4, 0));
        row.setBackground(new Color(50, 50, 50));
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(40, 40, 40)),
            new EmptyBorder(4, 8, 4, 8)
        ));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel label = new JLabel(setting.getName());
        label.setForeground(new Color(180, 180, 180));
        label.setFont(new Font("Tahoma", Font.PLAIN, 13));
        row.add(label);
        
        if (!setting.getDescription().isEmpty()) {
            label.setToolTipText(setting.getDescription());
        }

        JComponent editor = switch (setting.getType()) {
            case CHECK_BOX -> buildCheckBox((Setting<Boolean>) setting);
            case TEXT_FIELD -> buildTextField(setting);
            case SLIDER -> buildSlider(setting);
            case COLOR_PICKER -> buildColorPicker((Setting<Integer>) setting);
            case KEY_BIND -> buildKeyBind(setting);
            case RESOURCE_SELECT -> buildResourceSelect((Setting<String>) setting);
            case LIST_SELECT -> buildListSelect(setting);
            case OBJECT_SELECT -> buildObjectSelect(setting);
            default -> buildUnknown(setting);
        };

        if (!setting.getDescription().isEmpty()) {
            editor.setToolTipText(setting.getDescription());
        }
        if (!setting.isChangeable()) {
            editor.setEnabled(false);
            label.setEnabled(false);
        }

        componentMap.put(setting, editor);
        
        JPanel inputWrapper = new JPanel(new BorderLayout());
        inputWrapper.setOpaque(false);
        inputWrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 2)); // sağdan 5px
        inputWrapper.add(editor, BorderLayout.CENTER);
        
        row.add(inputWrapper);
        contentPanel.add(row);
        return this;
    }

    public Setting<?> getSetting(String name) {
        for (Setting<?> s : settings) {
            if (s.getName().equals(name)) return s;
        }
        return null;
    }

    public List<Setting<?>> getSettings() {
        return settings;
    }

    /** Tüm ayarları varsayılan değerlerine döndürür ve UI'ı günceller. */
    public void resetAll() {
        for (Setting<?> s : settings) {
            s.reset();
        }
        refreshAll();
    }

    /** Setting değerleri dialog dışından (programatik olarak) değiştirildiyse
     *  UI bileşenlerini güncel değerlerle senkronize eder. */
    public void refreshAll() {
        for (Map.Entry<Setting<?>, JComponent> entry : componentMap.entrySet()) {
            refreshComponent(entry.getKey(), entry.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    private void refreshComponent(Setting<?> setting, JComponent editor) {
        Object value = setting.getValue();
        switch (setting.getType()) {
            case CHECK_BOX -> ((JCheckBox) editor).setSelected(Boolean.TRUE.equals(value));
            case TEXT_FIELD -> ((JTextField) editor).setText(value == null ? "" : String.valueOf(value));
            case SLIDER -> {
                JPanel panel = (JPanel) editor;
                JSlider slider = (JSlider) panel.getClientProperty("slider");
                JLabel valueLabel = (JLabel) panel.getClientProperty("valueLabel");
                int scaled = toSliderInt(setting, value);
                slider.setValue(scaled);
                valueLabel.setText(String.valueOf(value));
            }
            case COLOR_PICKER -> {
                JButton btn = (JButton) editor;
                int rgb = value == null ? 0 : (Integer) value;
                btn.setBackground(new Color(rgb));
            }
            case KEY_BIND -> ((JButton) editor).setText(keyBindLabel(setting, value));
            case RESOURCE_SELECT -> {
                JPanel panel = (JPanel) editor;
                JTextField field = (JTextField) panel.getClientProperty("field");
                field.setText(value == null ? "" : String.valueOf(value));
            }
            case LIST_SELECT -> ((JComboBox<Object>) editor).setSelectedItem(value);
            case OBJECT_SELECT -> ((JButton) editor).setText(value == null ? "Seçilmedi" : value.toString());
            default -> ((JLabel) editor).setText(value == null ? "" : value.toString());
        }
    }

    // ------------------------------------------------------------------
    // Tip bazlı bileşen üreticileri
    // ------------------------------------------------------------------

    private JCheckBox buildCheckBox(Setting<Boolean> setting) {
        JCheckBox box = new JCheckBox();
        box.setSelected(Boolean.TRUE.equals(setting.getValue()));
        box.addItemListener(_ -> setting.setValue(box.isSelected()));
        return box;
    }

    private <T> JTextField buildTextField(Setting<T> setting) {
        Object current = setting.getValue();
        JTextField field = new JTextField(current == null ? "" : String.valueOf(current));
        Border normalBorder = field.getBorder();

        Runnable commit = () -> {
            Object parsed = parseTextValue(field.getText(), setting.getTypeClass());
            if (parsed == null) {
                field.setBorder(BorderFactory.createLineBorder(Color.RED, 1));
            } else {
                field.setBorder(normalBorder);
                @SuppressWarnings("unchecked")
                T typed = (T) parsed;
                setting.setValue(typed);
            }
        };

        field.addActionListener(_ -> commit.run());
        field.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                commit.run();
            }
        });
        return field;
    }

    private Object parseTextValue(String text, Class<?> typeClass) {
        String trimmed = text == null ? "" : text.trim();
        try {
            if (typeClass == String.class) return text;
            if (typeClass == Integer.class) return Integer.parseInt(trimmed);
            if (typeClass == Short.class) return Short.parseShort(trimmed);
            if (typeClass == Byte.class) return Byte.parseByte(trimmed);
            if (typeClass == Long.class) return Long.parseLong(trimmed);
            if (typeClass == Float.class) return Float.parseFloat(trimmed);
            if (typeClass == Double.class) return Double.parseDouble(trimmed);
        } catch (NumberFormatException ex) {
            return null;
        }
        return text;
    }

    private <T> JPanel buildSlider(Setting<T> setting) {
        JPanel panel = new JPanel(new BorderLayout(8, 0));

        Object min = setting.getMin() != null ? setting.getMin() : 0;
        Object max = setting.getMax() != null ? setting.getMax() : 100;

        int sliderMin = toSliderInt(setting, min);
        int sliderMax = toSliderInt(setting, max);
        int sliderVal = toSliderInt(setting, setting.getValue());

        JSlider slider = new JSlider(sliderMin, sliderMax, sliderVal);
        JLabel valueLabel = new JLabel(String.valueOf(setting.getValue()));
        valueLabel.setPreferredSize(new Dimension(56, valueLabel.getPreferredSize().height));

        slider.addChangeListener(_ -> {
            T realValue = fromSliderInt(setting, slider.getValue());
            valueLabel.setText(String.valueOf(realValue));
            if (!slider.getValueIsAdjusting()) {
                setting.setValue(realValue);
            }
        });

        panel.putClientProperty("slider", slider);
        panel.putClientProperty("valueLabel", valueLabel);

        panel.add(slider, BorderLayout.CENTER);
        panel.add(valueLabel, BorderLayout.EAST);
        return panel;
    }

    private int toSliderInt(Setting<?> setting, Object value) {
        if (value == null) return 0;
        Class<?> cls = setting.getTypeClass();
        if (cls == Float.class || cls == Double.class) {
            return Math.round(((Number) value).floatValue() * SLIDER_FLOAT_SCALE);
        }
        return ((Number) value).intValue();
    }

    @SuppressWarnings("unchecked")
    private <T> T fromSliderInt(Setting<T> setting, int sliderValue) {
        Class<?> cls = setting.getTypeClass();
        if (cls == Float.class) {
            return (T) Float.valueOf(sliderValue / (float) SLIDER_FLOAT_SCALE);
        }
        if (cls == Double.class) {
            return (T) Double.valueOf(sliderValue / (double) SLIDER_FLOAT_SCALE);
        }
        return (T) Integer.valueOf(sliderValue);
    }

    private JButton buildColorPicker(Setting<Integer> setting) {
        JButton btn = new JButton(" ");
        int rgb = setting.getValue() != null ? setting.getValue() : 0xFFFFFF;
        btn.setBackground(new Color(rgb));
        btn.addActionListener(_ -> {
            Color chosen = JColorChooser.showDialog(this, setting.getName(), btn.getBackground());
            if (chosen != null) {
                btn.setBackground(chosen);
                setting.setValue(chosen.getRGB());
            }
        });
        return btn;
    }

    private <T> JButton buildKeyBind(Setting<T> setting) {
        JButton btn = new JButton(keyBindLabel(setting, setting.getValue()));
        btn.setFocusable(true);
        btn.addActionListener(_ -> {
            btn.setText("Bir tuşa basın...");
            btn.requestFocusInWindow();
        });
        btn.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                Class<?> cls = setting.getTypeClass();
                Object newValue;
                if (cls == Character.class) {
                    newValue = e.getKeyChar();
                } else {
                    newValue = e.getKeyCode();
                }
                @SuppressWarnings("unchecked")
                T typed = (T) newValue;
                setting.setValue(typed);
                btn.setText(keyBindLabel(setting, typed));
                e.consume();
            }
        });
        return btn;
    }

    private String keyBindLabel(Setting<?> setting, Object value) {
        if (value == null) return "Atanmadı";
        if (setting.getTypeClass() == Integer.class) {
            return KeyEvent.getKeyText((Integer) value);
        }
        return String.valueOf(value);
    }

    private JPanel buildResourceSelect(Setting<String> setting) {
        JPanel panel = new JPanel(new BorderLayout(4, 0));
        JTextField field = new JTextField(setting.getValue() != null ? setting.getValue() : "");
        field.setEditable(false);
        JButton browse = new JButton("...");

        browse.addActionListener(_ -> {
            String picked;
            if (resourcePicker != null) {
                picked = resourcePicker.pick(setting, this);
            } else {
                JFileChooser chooser = new JFileChooser();
                int result = chooser.showOpenDialog(this);
                picked = result == JFileChooser.APPROVE_OPTION
                        ? chooser.getSelectedFile().getAbsolutePath()
                        : null;
            }
            if (picked != null) {
                field.setText(picked);
                setting.setValue(picked);
            }
        });

        panel.putClientProperty("field", field);
        panel.add(field, BorderLayout.CENTER);
        panel.add(browse, BorderLayout.EAST);
        return panel;
    }

    @SuppressWarnings("unchecked")
    private <T> JComboBox<T> buildListSelect(Setting<T> setting) {
        T[] options = setting.getOptions();
        JComboBox<T> combo = new JComboBox<>(options != null ? options : (T[]) new Object[0]);
        combo.setSelectedItem(setting.getValue());
        combo.addActionListener(_ -> setting.setValue((T) combo.getSelectedItem()));
        return combo;
    }

    private <T> JButton buildObjectSelect(Setting<T> setting) {
        Object current = setting.getValue();
        JButton btn = new JButton(current == null ? "Seçilmedi" : current.toString());
        btn.addActionListener(_ -> {
            if (objectPicker != null) {
                Object picked = objectPicker.pick(setting, this);
                if (picked != null) {
                    @SuppressWarnings("unchecked")
                    T typed = (T) picked;
                    setting.setValue(typed);
                    btn.setText(picked.toString());
                }
            } else {
                JOptionPane.showMessageDialog(this,
                        "Bu ayar için bir ObjectPicker tanımlanmadı.\n" +
                        "SettingsDialog.setObjectPicker(...) ile bağlayabilirsin.",
                        "Seçici Yok", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        return btn;
    }

    private JLabel buildUnknown(Setting<?> setting) {
        Object value = setting.getValue();
        JLabel label = new JLabel(value == null ? "" : value.toString());
        return label;
    }
    
    private static ImageIcon getIcon(int x, int y) {
		if (x >= 0 && y >= 0) {
			// load icon
			ImageIcon imageIcon;
			try {
				var image = ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(x, y);
				imageIcon = new ImageIcon(image.toImage());
			} catch (Exception ignore) {
				imageIcon = new ImageIcon(IOResource.missingTexture.toImage());
			} 
			
			return imageIcon;
		}
		
		return null;
	}
}