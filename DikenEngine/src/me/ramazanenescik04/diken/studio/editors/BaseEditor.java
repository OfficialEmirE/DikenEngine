package me.ramazanenescik04.diken.studio.editors;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.language.Lang;
import me.ramazanenescik04.diken.studio.dockables.EditorTabPanel;

public abstract class BaseEditor extends JPanel {
	private static final long serialVersionUID = 1L;
	
	public String title;
	protected boolean closeable = true;
	private EditorTabPanel tabPanel;
	
	public BaseEditor(String title) {
		this.title = title;
	}
	
	public void init(EditorTabPanel tabPanel) {
		this.tabPanel = tabPanel;
	}
	
	public JPanel getTabHeader() {
        JPanel tabHeader = new JPanel(new BorderLayout(4, 0));
        tabHeader.setOpaque(false);

        JLabel titleLabel = new JLabel(Lang.get(title));
        tabHeader.add(titleLabel, BorderLayout.CENTER);

        JButton closeButton = new JButton("x");
        closeButton.setOpaque(true);
        closeButton.setMargin(new Insets(2, 4, 2, 4));
        closeButton.setFont(new Font("Tahoma", Font.PLAIN, 10));
        closeButton.setBackground(new Color(180, 50, 50));
        closeButton.setForeground(Color.white);
        closeButton.addActionListener(_ -> tabPanel.removeEditor(this));
        closeButton.setEnabled(closeable);

        if (closeable) {
        	closeButton.setBorder(new LineBorder(new Color(180, 125, 125)));
        } else {
        	Border emptyBorder = BorderFactory.createEmptyBorder();
            closeButton.setBorder(emptyBorder);
        }

        tabHeader.add(closeButton, BorderLayout.EAST);
        
		return tabHeader;
	}
	
	protected void updateTabHeader() {
		tabPanel.updateEditor(this);
	}
	
	public void refreshWorld(World world, boolean playtest) {}
	
	public void closing() {}
}
