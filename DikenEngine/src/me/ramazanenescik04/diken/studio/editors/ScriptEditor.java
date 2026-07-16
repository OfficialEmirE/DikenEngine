package me.ramazanenescik04.diken.studio.editors;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;

import me.ramazanenescik04.diken.scripting.Script;

public class ScriptEditor extends BaseEditor {
	private static final long serialVersionUID = 1L;
	
	private RSyntaxTextArea textArea;
	private Script script;
	
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
        
        this.setLayout(new BorderLayout());
        this.add(scrollPane, BorderLayout.CENTER);
	}
	
	@Override
	public void closing() {
		script.setSource(textArea.getText());
	}
}
