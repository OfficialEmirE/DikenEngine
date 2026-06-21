package me.ramazanenescik04.diken.studio.dockables;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;

import me.ramazanenescik04.diken.renderer.RendererPanel;
import me.ramazanenescik04.diken.scripting.Script;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class ScriptTabPanel extends DockablePanel {
	private static final long serialVersionUID = -8433100060634996548L;
	
	private JTabbedPane tabbedPane;
	private Map<Script, RTextScrollPane> openScripts = new HashMap<>();
    private Map<Script, RSyntaxTextArea> textAreas = new HashMap<>();

	public ScriptTabPanel(RendererPanel gamePanel) {
		super("script_tab_id", "Script Editörleri");
		setBorder(new LineBorder(new Color(0, 0, 0)));
		
		dock.setTitleShown(false);
		
		setLayout(new BorderLayout(0, 0));
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		add(tabbedPane, BorderLayout.CENTER);
		
		tabbedPane.addTab("Oyun Önizleme", null, gamePanel, null);
	}
	
	private JPanel getTabHeader(Object c, String text) {
		// Tab başlığı + kapatma butonu
        JPanel tabHeader = new JPanel(new BorderLayout(4, 0));
        tabHeader.setOpaque(false);

        JLabel titleLabel = new JLabel(text);
        tabHeader.add(titleLabel, BorderLayout.CENTER);

        JButton closeButton = new JButton("x");
        closeButton.setMargin(new Insets(2, 4, 2, 4));
        closeButton.setFont(new Font("Tahoma", Font.PLAIN, 10));
        closeButton.addActionListener(_ -> close(c));

        Border emptyBorder = BorderFactory.createEmptyBorder();
        closeButton.setBorder(emptyBorder);

        tabHeader.add(closeButton, BorderLayout.EAST);
        
		return tabHeader;
	}

	public void openScript(Script script) {
        if (openScripts.containsKey(script)) {
            tabbedPane.setSelectedComponent(openScripts.get(script));
            return;
        }

        RSyntaxTextArea textArea = new RSyntaxTextArea(20, 60);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_LUA);
        textArea.setCodeFoldingEnabled(true);
        textArea.setAntiAliasingEnabled(true);
        textArea.setTabSize(4);
        textArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        textArea.setText(script.getSource());

        try {
            Theme theme = Theme.load(getClass().getResourceAsStream(
                "/org/fife/ui/rsyntaxtextarea/themes/dark.xml"));
            theme.apply(textArea);
        } catch (IOException e) {
            e.printStackTrace();
        }

        RTextScrollPane scrollPane = new RTextScrollPane(textArea);
        scrollPane.setLineNumbersEnabled(true);
        
        textArea.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				script.setSource(textArea.getText());
			}
        });

        openScripts.put(script, scrollPane);
        textAreas.put(script, textArea);

        tabbedPane.addTab(null, scrollPane);
        int index = tabbedPane.indexOfComponent(scrollPane);
        tabbedPane.setTabComponentAt(index, getTabHeader(script, script.getName()));
        tabbedPane.setSelectedComponent(scrollPane);
    }
	
	public void openWebSite(URI uri) {
		JEditorPane wv = new JEditorPane();
		wv.setEditable(false);
		wv.setContentType("text/html");
		
		wv.addHyperlinkListener(new HyperlinkListener() {
            @Override
            public void hyperlinkUpdate(HyperlinkEvent e) {
                if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    try {
                    	wv.setPage(e.getURL());
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

		SwingUtilities.invokeLater(() -> {
		    try {
		        wv.setPage(uri.toURL()); 
		    } catch (Exception e) {
		        e.printStackTrace();
		    }
		});
        
        JScrollPane scrollPane = new JScrollPane(wv);
        
        tabbedPane.addTab(null, scrollPane);
        int index = tabbedPane.indexOfComponent(scrollPane);
        tabbedPane.setTabComponentAt(index, getTabHeader(scrollPane, "Web Page"));
        tabbedPane.setSelectedComponent(scrollPane);
	}
    
    private void close(Object obj) {
    	Component c = null;
    	if (obj instanceof Script script) {
    		RTextScrollPane scrollPane = openScripts.remove(script);
    		textAreas.remove(script);
            c  = scrollPane;
    	} else if (obj instanceof Component) {
    		c = (Component) obj;
    	}
    	
		if (c != null) {
			int index = tabbedPane.indexOfComponent(c);
            if (index >= 0) tabbedPane.removeTabAt(index);
		}
	}
}
