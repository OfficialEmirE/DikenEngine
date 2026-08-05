package me.ramazanenescik04.diken.studio.builders;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.*;

import me.ramazanenescik04.diken.game.setting.Setting;
import me.ramazanenescik04.diken.language.Lang;

public final class SettingEditorFactory {

    private SettingEditorFactory() {}

    @SuppressWarnings("unchecked")
	public static JComponent create(Setting<?> setting) {
        return switch (setting.getType()) {
            case CHECK_BOX -> checkBox((Setting<Boolean>) setting);
            case TEXT_FIELD -> textField(setting);
            case SLIDER -> slider((Setting<? extends Number>) setting);
            case COLOR_PICKER -> colorPicker((Setting<Integer>) setting);
            case KEY_BIND -> keyBind(setting);
            case LIST_SELECT -> listSelect(setting);
            case RESOURCE_SELECT -> resourceSelect((Setting<String>) setting);
            case OBJECT_SELECT -> unsupported("studio.windows.settings.unsupportedObject");
            case TEXT -> text((Setting<Object>) setting);
            case UNKNOWN -> unsupported("studio.windows.settings.unsupported");
        };
    }

    private static JComponent checkBox(Setting<Boolean> setting) {
        JCheckBox box = new JCheckBox();
        box.setSelected(Boolean.TRUE.equals(setting.getValue()));
        box.addActionListener(_ -> setting.setValue(box.isSelected()));
        return box;
    }

    private static JComponent textField(Setting<?> setting) {
        JTextField field = new JTextField(String.valueOf(setting.getValue()));
        field.addActionListener(_ -> applyTextValue(setting, field));
        field.addFocusListener(new FocusAdapter() {
            @Override public void focusLost(FocusEvent e) { applyTextValue(setting, field); }
        });
        return field;
    }

    @SuppressWarnings("unchecked")
    private static void applyTextValue(Setting<?> setting, JTextField field) {
        Setting<Object> s = (Setting<Object>) setting;
        String text = field.getText();
        try {
            Object parsed = switch (setting.getTypeClass().getSimpleName()) {
                case "Integer" -> Integer.parseInt(text);
                case "Float"   -> Float.parseFloat(text);
                case "Double"  -> Double.parseDouble(text);
                case "Short"   -> Short.parseShort(text);
                case "Byte"    -> Byte.parseByte(text);
                case "Long"    -> Long.parseLong(text);
                default -> text;
            };
            s.setValue(parsed);
        } catch (NumberFormatException ex) {
            field.setText(String.valueOf(setting.getValue()));
        }
    }

    private static JComponent slider(Setting<? extends Number> setting) {
        int min = setting.getMin() != null ? setting.getMin().intValue() : 0;
        int max = setting.getMax() != null ? setting.getMax().intValue() : 100;
        int value = setting.getValue() != null ? setting.getValue().intValue() : min;

        JSlider slider = new JSlider(min, max, value);
        JLabel valueLabel = new JLabel(String.valueOf(value));
        valueLabel.setPreferredSize(new Dimension(40, 24));

        slider.addChangeListener(_ -> {
            valueLabel.setText(String.valueOf(slider.getValue()));
            if (!slider.getValueIsAdjusting()) {
                applyNumericValue(setting, slider.getValue());
            }
        });

        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.add(slider, BorderLayout.CENTER);
        panel.add(valueLabel, BorderLayout.EAST);
        return panel;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static void applyNumericValue(Setting<? extends Number> setting, int intValue) {
        Setting s = (Setting) setting;
        Object value = switch (setting.getTypeClass().getSimpleName()) {
            case "Float"  -> (float) intValue;
            case "Double" -> (double) intValue;
            default -> intValue;
        };
        s.setValue(value);
    }

    private static JComponent colorPicker(Setting<Integer> setting) {
        JButton button = new JButton();
        button.setOpaque(true);
        button.setBackground(new Color(setting.getValue() != null ? setting.getValue() : 0));
        button.addActionListener(_ -> {
            Color chosen = JColorChooser.showDialog(button, Lang.get("select.color"), button.getBackground());
            if (chosen != null) {
                button.setBackground(chosen);
                setting.setValue(chosen.getRGB());
            }
        });
        return button;
    }

    @SuppressWarnings("unchecked")
    private static JComponent keyBind(Setting<?> setting) {
        Setting<Object> s = (Setting<Object>) setting;
        JTextField field = new JTextField(KeyEvent.getKeyText((int) setting.getValue()));
        field.setEditable(false);
        field.addKeyListener(new KeyAdapter() {
            @Override public void keyPressed(KeyEvent e) {
                Object newValue = setting.getTypeClass() == Character.class
                        ? (Character) e.getKeyChar()
                        : (Integer) e.getKeyCode();
                s.setValue(newValue);
                field.setText(KeyEvent.getKeyText(e.getKeyCode()));
            }
        });
        return field;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static JComponent listSelect(Setting<?> setting) {
        JComboBox<Object> combo = new JComboBox<>(setting.getOptions());
        combo.setSelectedItem(setting.getValue());
        combo.addActionListener(_ -> ((Setting) setting).setValue(combo.getSelectedItem()));
        return combo;
    }

    private static JComponent resourceSelect(Setting<String> setting) {
        JButton button = new JButton(setting.getValue() != null ? setting.getValue() : Lang.get("select"));
        button.addActionListener(_ -> {
            JFileChooser chooser = new JFileChooser();
            if (chooser.showOpenDialog(button) == JFileChooser.APPROVE_OPTION) {
                String path = chooser.getSelectedFile().getAbsolutePath();
                setting.setValue(path);
                button.setText(path);
            }
        });
        return button;
    }

    private static JComponent unsupported(String message) {
        JLabel label = new JLabel(Lang.get(message));
        label.setEnabled(false);
        return label;
    }
    
    private static JComponent text(Setting<Object> message) {
        JLabel label = new JLabel(message.getValue().toString());
        return label;
    }
}