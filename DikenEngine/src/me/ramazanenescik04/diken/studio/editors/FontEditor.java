
package me.ramazanenescik04.diken.studio.editors;

import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.gui.UniFont;

import javax.swing.*;
import java.awt.*;

public class FontEditor extends ResourceEditor<UniFont> {
    private static final long serialVersionUID = 1L;

    private JTextArea fontPreview;

    private Font selectedFont = new Font(Font.DIALOG, Font.PLAIN, 16);

    private int fontSize = 16;
    private int fontStyle = Font.PLAIN;

    public FontEditor(World world, String resourceKey) {
        super(resourceKey + " - Font Editor", world, resourceKey,
                UniFont.getFont("default_font"));

        JSplitPane splitPane = new JSplitPane();
        splitPane.setResizeWeight(0.5);
        add(splitPane, BorderLayout.CENTER);

        // Font list
        var fontList = GraphicsEnvironment
                .getLocalGraphicsEnvironment().getAllFonts();

        JList<Font> list = new JList<>(fontList);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(new FontCellRenderer());

        JScrollPane listScroll = new JScrollPane(list);
        splitPane.setLeftComponent(listScroll);

        // Right panel
        JPanel rightPanel = new JPanel(new BorderLayout(5, 5));
        splitPane.setRightComponent(rightPanel);

        // Preview
        fontPreview = new JTextArea("The quick brown fox jumps over the lazy dog.\n"
                + "0123456789\n"
                + "ABCÇDEFGĞHIİJKLMNOÖPRSŞTUÜVYZ\n"
                + "abcçdefgğhıijklmnoöprsştuüvyz");

        fontPreview.setLineWrap(true);
        fontPreview.setWrapStyleWord(true);
        fontPreview.setEditable(false);
        fontPreview.setMargin(new Insets(10, 10, 10, 10));

        rightPanel.add(new JScrollPane(fontPreview), BorderLayout.CENTER);

        // Properties
        JPanel properties = new JPanel(new GridLayout(0, 2, 8, 8));
        properties.setBorder(BorderFactory.createTitledBorder("Font Properties"));

        // Size
        properties.add(new JLabel("Font Size:"));

        JSpinner sizeSpinner = new JSpinner(
                new SpinnerNumberModel(16, 1, 512, 1));

        properties.add(sizeSpinner);

        // Style
        properties.add(new JLabel("Font Style:"));

        JComboBox<String> styleCombo = new JComboBox<>(
                new String[] {
                        "Regular",
                        "Bold",
                        "Italic",
                        "Bold Italic"
                });

        properties.add(styleCombo);

        rightPanel.add(properties, BorderLayout.SOUTH);

        // Listeners
        list.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && list.getSelectedValue() != null) {
                selectedFont = list.getSelectedValue();
                updateFont();
            }
        });

        sizeSpinner.addChangeListener(_ -> {
            fontSize = (int) sizeSpinner.getValue();
            updateFont();
        });

        styleCombo.addActionListener(_ -> {
            fontStyle = switch (styleCombo.getSelectedIndex()) {
                case 1 -> Font.BOLD;
                case 2 -> Font.ITALIC;
                case 3 -> Font.BOLD | Font.ITALIC;
                default -> Font.PLAIN;
            };

            updateFont();
        });

        list.setSelectedValue(selectedFont, true);
        updateFont();
    }

    private void updateFont() {
    	var updatedFont = selectedFont.deriveFont(fontStyle, (float) fontSize);
    	
        fontPreview.setFont(updatedFont);
        
        this.resource.second = UniFont.fromAwtFont(updatedFont, false);
    }

    @Override
    public void closing() {
        this.resource.second = UniFont.fromAwtFont(
                selectedFont.deriveFont(fontStyle, (float) fontSize), false);
    }

    private static class FontCellRenderer extends JLabel
            implements ListCellRenderer<Font> {

        private static final long serialVersionUID = 1L;

        public FontCellRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(
                JList<? extends Font> list,
                Font value,
                int index,
                boolean isSelected,
                boolean cellHasFocus) {

            setFont(value.deriveFont(14f));
            setText(value.getFamily() + " (" + value.getName() + ")");

            setBackground(isSelected ? Color.BLUE : Color.WHITE);
            setForeground(isSelected ? Color.WHITE : Color.BLACK);

            return this;
        }
    }
}
