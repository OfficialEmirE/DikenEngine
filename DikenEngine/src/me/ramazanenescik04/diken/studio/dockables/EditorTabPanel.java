package me.ramazanenescik04.diken.studio.dockables;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;

import me.ramazanenescik04.diken.game.EnumSettingType;
import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.game.nodes.SpriteSheet;
import me.ramazanenescik04.diken.game.setting.Setting;
import me.ramazanenescik04.diken.gui.AnimationEditor;
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
import java.util.List;
import java.util.Map;

public class EditorTabPanel extends DockablePanel {
	private static final long serialVersionUID = -8433100060634996548L;
	
	@SuppressWarnings("unused")
	private World theWorld;
	
	private JTabbedPane tabbedPane;
	private ObjectBrowserPanel reference = new ObjectBrowserPanel();
	private Map<Script, RTextScrollPane> openScripts = new HashMap<>();
	private Map<SpriteSheet, AnimationEditor> openAnims = new HashMap<>();
    private Map<Script, RSyntaxTextArea> textAreas = new HashMap<>();

	public EditorTabPanel(RendererPanel gamePanel, World theWorld) {
		super("editor_tab_id", "Editörler");
		this.theWorld = theWorld;
		
		setBorder(new LineBorder(new Color(0, 0, 0)));
		
		dock.setTitleShown(false);
		
		setLayout(new BorderLayout(0, 0));
		
		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		add(tabbedPane, BorderLayout.CENTER);
		
		tabbedPane.addTab("Oyun Önizleme", null, gamePanel, null);
	}
	
	public void reloadWorld(World theWorld, boolean playtest) {
		this.theWorld = theWorld;
		setBorder(new LineBorder(playtest ? Color.green : Color.black));
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
        textArea.setAutoIndentEnabled(true);
        
       /* var provider = AutoCompleteHelper.createLuaProvider(theWorld);
        AutoCompletion ac = new AutoCompletion(provider);
        ac.setAutoActivationEnabled(true);
        ac.setAutoActivationDelay(150);
        
        ac.install(textArea);*/

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
        
        script.OnDestroy.Connect(_ -> close(script));
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
	
	public void openAnimation(SpriteSheet anim) {
		/*AnimationEditor editor = new AnimationEditor(anim);
		
		openAnims.put(anim, editor);
		
		tabbedPane.addTab(null, editor);
        int index = tabbedPane.indexOfComponent(editor);
        tabbedPane.setTabComponentAt(index, getTabHeader(anim, anim.getName()));
        tabbedPane.setSelectedComponent(editor);
        
        anim.OnDestroy.Connect(_ -> close(anim));*/
	}
	
	public void openObjectReference() {
		if (tabbedPane.indexOfTabComponent(reference) >= 0) {
			return;
		}
		
		tabbedPane.addTab(null, reference);
        int index = tabbedPane.indexOfComponent(reference);
        tabbedPane.setTabComponentAt(index, getTabHeader(reference, "Obje Tarayıcısı"));
        tabbedPane.setSelectedComponent(reference);
	}
    
    private void close(Object obj) {
    	Component c = null;
    	if (obj instanceof Script script) {
    		var scrollPane = openScripts.remove(script);
    		textAreas.remove(script);
            c = scrollPane;
    	} else if (obj instanceof SpriteSheet anim) {
    		var editor = openAnims.remove(anim);
    		//editor.saveAnimation(anim);
    		c = editor;
    	} else if (obj instanceof Component) {
    		c = (Component) obj;
    	}
    	
		if (c != null) {
			int index = tabbedPane.indexOfComponent(c);
            if (index >= 0) tabbedPane.removeTabAt(index);
		}
	}

	@Override
	public List<Setting<?>> getDockableSettings() {
		List<Setting<?>> list = super.getDockableSettings();
		list.add(new Setting<>("Obje Tarayıcısı: showUnlisted", this.reference.isShowUnlisted(), Boolean.class,
				EnumSettingType.CHECK_BOX).addChangeListener(reference::setShowUnlisted));
		return list;
	}
}
