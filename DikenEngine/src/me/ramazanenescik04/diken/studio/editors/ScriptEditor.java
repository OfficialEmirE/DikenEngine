package me.ramazanenescik04.diken.studio.editors;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;

import me.ramazanenescik04.diken.scripting.Script;

/**
 * In-memory script editor. Tab title comes from script.getName() dynamically.
 */
public class ScriptEditor extends BaseEditor {
	private static final long serialVersionUID = 1L;
	
	private RSyntaxTextArea textArea;
	private Script script;
	private JList<String> completionList;
	private JPopupMenu completionPopup;
	private JLabel titleLabel; // cached for dynamic updates
	
	// Listener references for proper cleanup in closing()
	private DocumentListener documentListener;
	private KeyAdapter completionKeyAdapter;
	private java.awt.event.MouseAdapter completionMouseAdapter;
	
	// Performance: debounce saving source to script object
	private boolean dirty = false;
	private Timer saveTimer;
	
	public ScriptEditor(Script script) {
		super(script.getName());
		
		this.script = script;
		textArea = new RSyntaxTextArea(20, 60);
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_LUA);
        textArea.setCodeFoldingEnabled(true);
        textArea.setAntiAliasingEnabled(true);
        textArea.setTabSize(4);
        textArea.setFont(new Font("Consolas", Font.PLAIN, 14));
        textArea.setText(script.getSource());
        textArea.setAutoIndentEnabled(true);
        textArea.setBracketMatchingEnabled(true);
		
		try {
            Theme theme = Theme.load(getClass().getResourceAsStream(
                "/org/fife/ui/rsyntaxtextarea/themes/dark.xml"));
            theme.apply(textArea);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Ctrl+Space ile otomatik tamamlama popup'ı
        setupAutoCompletion();

        RTextScrollPane scrollPane = new RTextScrollPane(textArea);
        scrollPane.setLineNumbersEnabled(true);
        
        // Debounced save: only persist source to script after user stops typing
        saveTimer = new Timer(400, _ -> {
            if (dirty && script != null) {
                script.setSource(textArea.getText());
                dirty = false;
            }
        });
        saveTimer.setRepeats(false);
        
        documentListener = new DocumentListener() {
			@Override public void insertUpdate(DocumentEvent e) { markDirty(); }
			@Override public void removeUpdate(DocumentEvent e) { markDirty(); }
			@Override public void changedUpdate(DocumentEvent e) { markDirty(); }
        };
        textArea.getDocument().addDocumentListener(documentListener);
        
        this.setLayout(new BorderLayout());
        this.add(scrollPane, BorderLayout.CENTER);
	}
	
	private void markDirty() {
		dirty = true;
		saveTimer.restart();
	}

	/** Force-save current text to script object immediately. */
	public void flushSource() {
		if (dirty && script != null) {
			script.setSource(textArea.getText());
			dirty = false;
		}
		saveTimer.stop();
	}
	
	public Script getScript() {
		return script;
	}

	@Override
	public void closing() {
		// Save pending changes
		flushSource();
		
		// Remove document listener to prevent memory leak
		if (textArea != null) {
			Document doc = textArea.getDocument();
			if (doc != null && documentListener != null) {
				doc.removeDocumentListener(documentListener);
			}
		}
		
		// Clean up save timer
		if (saveTimer != null) {
			saveTimer.stop();
		}
		
		// Clean up completion popup
		if (completionPopup != null) {
			completionPopup.setVisible(false);
			completionPopup.removeAll();
		}
		
		// Remove completion list listeners
		if (completionList != null) {
			if (completionKeyAdapter != null) {
				completionList.removeKeyListener(completionKeyAdapter);
			}
			if (completionMouseAdapter != null) {
				completionList.removeMouseListener(completionMouseAdapter);
			}
		}
		
		// Nullify references to allow garbage collection
		textArea = null;
		script = null;
		completionList = null;
		completionPopup = null;
		titleLabel = null;
		documentListener = null;
		completionKeyAdapter = null;
		completionMouseAdapter = null;
		saveTimer = null;
	}

	/** Dynamic tab header that reads script.getName() each time. */
	@Override
	public JPanel getTabHeader() {
		JPanel tabHeader = new JPanel(new BorderLayout(4, 0));
		tabHeader.setOpaque(false);

		titleLabel = new JLabel(script != null ? script.getName() : title);
		tabHeader.add(titleLabel, BorderLayout.CENTER);

		JButton closeButton = new JButton("x");
		closeButton.setOpaque(true);
		closeButton.setMargin(new Insets(2, 4, 2, 4));
		closeButton.setFont(new Font("Tahoma", Font.PLAIN, 10));
		closeButton.setBackground(new Color(180, 50, 50));
		closeButton.setForeground(Color.white);
		closeButton.addActionListener(_ -> {
			if (tabPanel != null) tabPanel.removeEditor(this);
		});
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

	/** Call this when the script name changes in Properties, so the tab updates. */
	public void updateTitle() {
		this.title = (script != null) ? script.getName() : title;
		if (titleLabel != null && script != null) {
			titleLabel.setText(script.getName());
		}
		updateTabHeader();
	}
	
	// -------------------------------------------------------------------------
	//  Autocomplete popup (Ctrl+Space)
	// -------------------------------------------------------------------------
	
	private void setupAutoCompletion() {
		DefaultListModel<String> model = new DefaultListModel<>();
		completionList = new JList<>(model);
		completionList.setFont(new Font("Consolas", Font.PLAIN, 13));
		completionList.setBackground(new Color(50, 50, 50));
		completionList.setForeground(new Color(220, 220, 220));
		completionList.setSelectionBackground(new Color(70, 70, 70));
		completionList.setSelectionForeground(Color.WHITE);
		completionList.setFixedCellHeight(22);
		completionList.setVisibleRowCount(8);
		
		// Popup
		completionPopup = new JPopupMenu();
		completionPopup.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100)));
		JScrollPane sp = new JScrollPane(completionList);
		sp.setBorder(null);
		completionPopup.add(sp);
		
		// Enter ile seç - store reference for cleanup
		completionKeyAdapter = new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					insertSelectedCompletion();
					e.consume();
				} else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					completionPopup.setVisible(false);
				}
			}
		};
		completionList.addKeyListener(completionKeyAdapter);
		
		// Çift tıkla seç - store reference for cleanup
		completionMouseAdapter = new java.awt.event.MouseAdapter() {
			@Override
			public void mouseClicked(java.awt.event.MouseEvent e) {
				if (e.getClickCount() == 2) insertSelectedCompletion();
			}
		};
		completionList.addMouseListener(completionMouseAdapter);
		
		// Ctrl+Space tuşu
		textArea.getInputMap().put(KeyStroke.getKeyStroke("control SPACE"), "autocomplete");
		textArea.getActionMap().put("autocomplete", new AbstractAction() {
			@Override
			public void actionPerformed(java.awt.event.ActionEvent e) {
				showCompletionPopup();
			}
		});
	}
	
	private void showCompletionPopup() {
		if (textArea == null) return;
		String word = getCurrentWord();
		if (word == null) return;
		
		List<String> suggestions = LuaCompleter.complete(word);
		if (suggestions.isEmpty()) return;
		
		DefaultListModel<String> model = (DefaultListModel<String>) completionList.getModel();
		model.clear();
		for (String s : suggestions) model.addElement(s);
		
		// Popup'ı caret pozisyonunda göster
		try {
			Point p = textArea.getCaret().getMagicCaretPosition();
			if (p == null) return;
			p.x += 10;
			p.y += textArea.getFontMetrics(textArea.getFont()).getHeight();
			SwingUtilities.convertPointToScreen(p, textArea);
			completionPopup.setPreferredSize(new java.awt.Dimension(280, Math.min(250, suggestions.size() * 22 + 4)));
			completionPopup.show(textArea, p.x - textArea.getLocationOnScreen().x, p.y - textArea.getLocationOnScreen().y);
			completionList.setSelectedIndex(0);
			completionList.requestFocusInWindow();
		} catch (Exception ex) {
			completionPopup.show(textArea, 100, 100);
		}
	}
	
	private void insertSelectedCompletion() {
		if (textArea == null || completionList == null) return;
		String selected = completionList.getSelectedValue();
		if (selected == null) return;
		
		// Replace current word with selected completion
		try {
			int pos = textArea.getCaretPosition();
			javax.swing.text.Document doc = textArea.getDocument();
			String text = doc.getText(0, pos);
			int start = text.length() - 1;
			while (start >= 0 && (Character.isLetterOrDigit(text.charAt(start)) || text.charAt(start) == '_' || text.charAt(start) == '.')) {
				start--;
			}
			start++;
			doc.remove(start, pos - start);
			doc.insertString(start, selected, null);
		} catch (BadLocationException ignored) {}
		
		completionPopup.setVisible(false);
		textArea.requestFocusInWindow();
	}
	
	private String getCurrentWord() {
		if (textArea == null) return null;
		try {
			int pos = textArea.getCaretPosition();
			javax.swing.text.Document doc = textArea.getDocument();
			String text = doc.getText(0, pos);
			int start = text.length() - 1;
			if (start < 0) return null;
			// Geriye doğru word boundary
			while (start >= 0 && (Character.isLetterOrDigit(text.charAt(start)) || text.charAt(start) == '_' || text.charAt(start) == '.')) {
				start--;
			}
			start++;
			if (start >= text.length()) return null;
			return text.substring(start);
		} catch (BadLocationException e) {
			return null;
		}
	}
}
