package me.ramazanenescik04.diken.studio.dockables;

import me.ramazanenescik04.diken.game.EnumSettingType;
import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.game.setting.Setting;
import me.ramazanenescik04.diken.game.setting.SettingCategory;
import me.ramazanenescik04.diken.resource.Bitmap;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class PropertiesPanel extends DockablePanel {

    private static final long serialVersionUID = 1L;

    private ExplorerPanel explorerPanel;
    private JPanel contentPanel;
    private JFrame window;

    public PropertiesPanel(ExplorerPanel explorerPanel, JFrame window) {
    	super("properties_id", "Özellikler");
    	
    	this.explorerPanel = explorerPanel;
    	this.window = window;
    	
        setLayout(new BorderLayout());
        setBackground(new Color(45, 45, 45));

        contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(new Color(45, 45, 45));

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBackground(new Color(45, 45, 45));
        add(scrollPane, BorderLayout.CENTER);
        
        explorerPanel.addSelectedNodeListener(new ExplorerPanel.SelectedNodeListener() {
		    @Override
		    public void onSelectedNode(Node node) {
		        inspect(node);
		    }

		    @Override
		    public void onSelectedNodes(List<Node> nodes) {
		        if (nodes.size() == 1) {
		            inspect(nodes.get(0));
		        } else {
		            inspect(null);
		        }
		    }
		});
    }

    private void startObjectPick(Setting<Node> setting, JLabel valueLabel) {
        Class<?> requiredClass = setting.getTypeClass();

        explorerPanel.startPickMode(node -> {
            if (node != null && !requiredClass.isInstance(node)) {
                JOptionPane.showMessageDialog(this,
                    "Bu ayar sadece " + requiredClass.getSimpleName() + " tipini kabul ediyor.",
                    "Geçersiz Tip", JOptionPane.WARNING_MESSAGE);
                valueLabel.setText(setting.getValue() instanceof Node n ? n.getName() : "Yok");
                valueLabel.setForeground(new Color(220, 220, 220));
                return;
            }

            if (node != null) {
                setting.setValue(node);
                valueLabel.setText(node.getName());
            } else {
                valueLabel.setText(setting.getValue() instanceof Node n ? n.getName() : "Yok");
            }
            valueLabel.setForeground(new Color(220, 220, 220));
        });
    }

    public void inspect(Node node) {
        contentPanel.removeAll();

        if (node == null) {
            contentPanel.revalidate();
            contentPanel.repaint();
            return;
        }

        List<SettingCategory> categories = node.getNodeSettings();

        for (int i = (categories.size() - 1); i >= 0; i--) {
        	var category = categories.get(i);
        	
            JPanel headerPanel = new JPanel(new BorderLayout());
            headerPanel.setBackground(new Color(60, 60, 60));
            headerPanel.setBorder(new EmptyBorder(4, 8, 4, 8));
            headerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));

            JLabel headerLabel = new JLabel(category.getKey().getCategory());
            headerLabel.setForeground(new Color(200, 200, 200));
            headerLabel.setFont(new Font("Tahoma", Font.BOLD, 13));
            headerLabel.setIcon(new ImageIcon(category.getKey().getImage().toImage()));
            headerPanel.add(headerLabel, BorderLayout.WEST);
            contentPanel.add(headerPanel);

            // Ayarlar
            for (Setting<?> setting : category.getSettings()) {
                JPanel row = buildSettingRow(node, setting);
                row.setToolTipText(setting.getDescription());
                if (row != null) contentPanel.add(row);
            }
        }

        contentPanel.add(Box.createVerticalGlue());
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    private <T> JPanel buildSettingRow(Node node, Setting<T> setting) {
        JPanel row = new JPanel(new GridLayout(1, 2, 4, 0));
        row.setBackground(new Color(50, 50, 50));
        row.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(40, 40, 40)),
            new EmptyBorder(4, 8, 4, 8)
        ));
        row.setPreferredSize(new Dimension(row.getPreferredSize().width, 30));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));

        JLabel label = new JLabel(setting.getName());
        label.setForeground(new Color(180, 180, 180));
        label.setFont(new Font("Tahoma", Font.PLAIN, 13));
        row.add(label);

        JComponent inputComponent = buildInputComponent(node, setting);
        if (inputComponent == null) return null;
        if (!setting.isChangeable() && inputComponent.isEnabled()) inputComponent.setEnabled(false);
        
        JPanel inputWrapper = new JPanel(new BorderLayout());
        inputWrapper.setOpaque(false);
        inputWrapper.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 2));
        inputWrapper.add(inputComponent, BorderLayout.CENTER);
        
        row.add(inputWrapper);

        return row;
    }

    @SuppressWarnings("unchecked")
	private <T> JComponent buildInputComponent(Node node, Setting<T> setting) {    	
        switch (setting.getType()) {
        	case TEXT_FIELD -> {
        		var textField = buildTextField(setting);

        		/*if (node instanceof Service) {
        			textField.setEnabled(false);
        		}*/
        		
        		return textField;
        	}
        	case COLOR_PICKER -> {
				var color = new Color((int) setting.getValue(), true);
				var textField = new JButton("%d, %d, %d".formatted(color.getRed(), color.getGreen(), color.getBlue()));
				textField.setIcon(new ImageIcon(Bitmap.createClearedBitmap(16, 16, color.getRGB()).toImage()));
				textField.addActionListener(_ -> {
					explorerPanel.suppressRebuild = true;
					try {
						var color2 = JColorChooser.showDialog(window, "Renk seç.", color);

						if (color2 != null) {
							textField.setText(
									"%d, %d, %d".formatted(color2.getRed(), color2.getGreen(), color2.getBlue()));
							textField.setIcon(
									new ImageIcon(Bitmap.createClearedBitmap(16, 16, color2.getRGB()).toImage()));

							applySettingValue(setting, "" + color2.getRGB());
						}
					} finally {
						explorerPanel.suppressRebuild = false;
						Node currentSelected = explorerPanel.getSelectedNode();
						javax.swing.SwingUtilities.invokeLater(() -> {
							explorerPanel.rebuildExplorer();
							explorerPanel.selectNode(currentSelected);
						});
					}

				});
        		
        		return textField;
        	}
        	case RESOURCE_SELECT -> {
        		Setting<String> stringSetting = (Setting<String>) setting;
        		var world = explorerPanel.theWorld;
        		
        		var comboBox = new JComboBox<String>(world.resources.keySet().toArray(new String[0]));
            	comboBox.setSelectedItem(setting.getValue());
            	comboBox.setEditable(true);
            	comboBox.addActionListener(_ -> {
            		applySettingValue(stringSetting, comboBox.getSelectedItem());
            	});
            	
            	return comboBox;
        	}
        	case OBJECT_SELECT -> {
        	    JPanel panel = new JPanel(new BorderLayout(4, 0));
        	    panel.setBackground(new Color(50, 50, 50));

        	    JLabel valueLabel = new JLabel();
        	    valueLabel.setForeground(new Color(220, 220, 220));
        	    valueLabel.setFont(new Font("Tahoma", Font.PLAIN, 13));
        	    valueLabel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));

        	    Object currentValue = setting.getValue();
        	    valueLabel.setText(currentValue instanceof Node n ? n.getName() : "Yok");

        	    JButton selectButton = new JButton("S");
        	    selectButton.setMargin(new Insets(2, 4, 2, 4));
        	    selectButton.setFont(new Font("Tahoma", Font.PLAIN, 11));

        	    JButton clearButton = new JButton("X");
        	    clearButton.setMargin(new Insets(2, 4, 2, 4));
        	    clearButton.setFont(new Font("Tahoma", Font.PLAIN, 11));

        	    selectButton.addActionListener(_ -> {
        	    	if (setting.getType() == EnumSettingType.OBJECT_SELECT) {
        	    		startObjectPick((Setting<Node>) setting, valueLabel);
        	    	}
        	    });

        	    clearButton.addActionListener(_ -> {
        	    	applySettingValue(setting, null);
        	        valueLabel.setText("Yok");
        	    });

        	    JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
        	    buttonsPanel.setBackground(new Color(50, 50, 50));
        	    buttonsPanel.add(selectButton);
        	    buttonsPanel.add(clearButton);

        	    panel.add(valueLabel, BorderLayout.CENTER);
        	    panel.add(buttonsPanel, BorderLayout.EAST);
        	    
        	    if (!setting.isChangeable()) {
        	    	selectButton.setEnabled(false);
        	    	clearButton.setEnabled(false);
        	    	valueLabel.setEnabled(false);
        		}

        	    return panel;
        	}
            case CHECK_BOX -> {
                JCheckBox checkBox = new JCheckBox();
                checkBox.setSelected(Boolean.TRUE.equals(setting.getValue()));
                checkBox.setBackground(new Color(50, 50, 50));
                checkBox.addActionListener(_ -> {
                	applySettingValue(setting, checkBox.isSelected());
                });
                return checkBox;
            }
            case SLIDER -> {
            	var sliderSetting = (Setting<? extends Number>)setting;
            	
            	JSlider slider = new JSlider();

            	slider.setValue(sliderSetting.getValue().intValue());
            	slider.setMinimum(sliderSetting.getMin().intValue());
            	slider.setMaximum(sliderSetting.getMax().intValue());
            	
            	slider.addChangeListener(_ -> {
            		applySettingValue(setting, slider.getValue());
            	});
            	
                return slider;
            }
            case LIST_SELECT -> {
            	var comboBox = new JComboBox<T>(setting.getOptions());
            	comboBox.setSelectedItem(setting.getValue());
            	comboBox.addActionListener(_ -> {
            		applySettingValue(setting, comboBox.getSelectedItem());
            	});
            	
            	return comboBox;
            }
            default -> {
                JLabel fallback = new JLabel(String.valueOf(setting.getValue()));
                fallback.setForeground(new Color(150, 150, 150));
                fallback.setFont(new Font("Tahoma", Font.PLAIN, 13));
                return fallback;
            }
        }
    }
    
    private <T> JTextField buildTextField(Setting<T> setting) {
    	JTextField textField = new JTextField(String.valueOf(setting.getValue()));
		textField.setBackground(new Color(35, 35, 35));
    	textField.setForeground(new Color(220, 220, 220));
    	textField.setCaretColor(Color.WHITE);
    	textField.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
    	textField.setFont(new Font("Tahoma", Font.PLAIN, 13));
    	
    	textField.addActionListener(_ -> {
    		explorerPanel.suppressRebuild = true;
    		try {
    			applySettingValue(setting, textField.getText());
    		} finally {
    			explorerPanel.suppressRebuild = false;
                Node currentSelected = explorerPanel.getSelectedNode();
                javax.swing.SwingUtilities.invokeLater(() -> {
                    explorerPanel.rebuildExplorer();
                    // rebuild sonrası seçimi geri yükle
                    explorerPanel.selectNode(currentSelected);
                });
    		}
    	});
    
    	textField.addFocusListener(new java.awt.event.FocusAdapter() {
    		@Override
    		public void focusLost(java.awt.event.FocusEvent e) {
    			explorerPanel.suppressRebuild = true;
    			try {
    				applySettingValue(setting, textField.getText());
    			} finally {
    				explorerPanel.suppressRebuild = false;
    	            Node currentSelected = explorerPanel.getSelectedNode();
    	            javax.swing.SwingUtilities.invokeLater(() -> {
    	                explorerPanel.rebuildExplorer();
    	                // rebuild sonrası seçimi geri yükle
    	                explorerPanel.selectNode(currentSelected);
    	            });
    			}
    		}
    	});

    	return textField;
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	private void applySettingValue(Setting<?> setting, Object object) {
    	if (object == null) ((Setting) setting).setValue(null);
    	
		try {
			Class<?> clazz = setting.getTypeClass();
			if (object instanceof String value) {
				if (clazz == String.class) {
					((Setting) setting).setValue(object);
				} else if (!value.isBlank()) {
					if (clazz == Character.class) {
						((Setting) setting).setValue(value.charAt(0));
					} else if (clazz == Integer.class) {
						((Setting) setting).setValue(Integer.parseInt(value));
					} else if (clazz == Byte.class) {
						((Setting) setting).setValue(Byte.parseByte(value));
					} else if (clazz == Short.class) {
						((Setting) setting).setValue(Short.parseShort(value));
					} else if (clazz == Long.class) {
						((Setting) setting).setValue(Long.parseLong(value));
					} else if (clazz == Float.class) {
						((Setting) setting).setValue(Float.parseFloat(value));
					} else if (clazz == Double.class) {
						((Setting) setting).setValue(Double.parseDouble(value));
					}
				}
			} else if (setting.getTypeClass().isInstance(object)) {
				((Setting) setting).setValue(object);
			}
		} catch (NumberFormatException ignored) {
		}
	}
}