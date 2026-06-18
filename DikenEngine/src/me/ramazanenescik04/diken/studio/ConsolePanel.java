package me.ramazanenescik04.diken.studio;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.BorderLayout;
import javax.swing.JTextPane;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.log.ConsoleLog;
import me.ramazanenescik04.diken.log.ConsoleLog.LogText;
import me.ramazanenescik04.diken.log.ConsoleLog.LogType;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;
import me.ramazanenescik04.diken.scripting.Script;
import me.ramazanenescik04.diken.tools.ListAdapter;

import java.awt.Font;
import java.awt.Image;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.util.List;
import java.awt.event.ActionEvent;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import bibliothek.gui.dock.common.action.CButton;

import javax.swing.JTextField;
import javax.swing.JScrollPane;

public class ConsolePanel extends DockablePanel {

	private static final long serialVersionUID = 1L;
	private JTextField textField;

	public ConsolePanel() {
		super("console_id", "Konsol");
		
		setForeground(new Color(255, 255, 255));
		setBackground(new Color(63, 63, 63));
		setLayout(new BorderLayout(0, 0));
		
		var img = ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(12, 0).toImage();
		var scaled = img.getScaledInstance(20, 20, Image.SCALE_SMOOTH);
		CButton clearScreen = new CButton("Konsolu Temizle", new ImageIcon(scaled));
		dock.addAction(clearScreen);
		
		JPanel commandLinePanel = new JPanel();
		commandLinePanel.setBackground(new Color(83, 83, 83));
		add(commandLinePanel, BorderLayout.SOUTH);
		commandLinePanel.setLayout(new BorderLayout(0, 0));
		
		textField = new JTextField();
		textField.setColumns(10);
		commandLinePanel.add(textField);
		
		JButton sendButton = new JButton("Gönder");
		sendButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				var command = textField.getText();
				textField.setText("");
				
				sendCommand(command);
			}
		});
		commandLinePanel.add(sendButton, BorderLayout.EAST);
		
		JScrollPane scrollPane = new JScrollPane();
		add(scrollPane, BorderLayout.CENTER);
		
		JTextPane logArea = new JTextPane();
		scrollPane.setViewportView(logArea);
		logArea.setEditable(false);
		logArea.setFont(new Font("Lucida Console", Font.PLAIN, 15));
		logArea.setForeground(new Color(255, 255, 255));
		logArea.setBackground(new Color(63, 63, 63));
		
		DefaultCaret caret = (DefaultCaret) logArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
		clearScreen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				logArea.setText("");
			}
		});
		
		textField.addKeyListener(new java.awt.event.KeyAdapter() {
		    @Override
		    public void keyPressed(java.awt.event.KeyEvent e) {
		        if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
		        	var command = textField.getText();
					textField.setText("");
					
					sendCommand(command);
		        }
		    }
		});
		
		loadLogs(logArea);
		
		ConsoleLog.setListAdapter(new ListAdapter<LogText>() {
			@Override
			public void onAdd(LogText item) {
				if (item.type() == LogType.C_ERR || item.type() == LogType.S_ERR) {
					printText(logArea, item.toString(), Color.RED);
				} else if(item.type() == LogType.C_WARN || item.type() == LogType.S_WARN) {
					printText(logArea, item.toString(), Color.YELLOW);
				} else {
					printText(logArea, item.toString(), Color.WHITE);
				}
			}

			@Override
			public void onRemove(LogText item) {}

			@Override
			public void onUpdate() {}

			@Override
			public void onClear() {
				logArea.setText("");
			}
		});
	}
	
	private void printText(JTextPane logArea, String text, Color color) {
		StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, color);

        aset = sc.addAttribute(aset, StyleConstants.FontFamily, "Lucida Console");
        aset = sc.addAttribute(aset, StyleConstants.FontSize, 15);
        aset = sc.addAttribute(aset, StyleConstants.Alignment, StyleConstants.ALIGN_JUSTIFIED);

        try {
        	var doc = logArea.getStyledDocument();
        	
        	doc.insertString(doc.getLength(), text + "\n", aset);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
	
	private void loadLogs(JTextPane logArea) {
		List<LogText> logs = ConsoleLog.getLogs();
		
		for (int i = 0; i < logs.size(); i++) {
			LogText item = logs.get(i);
			
			if (item.type() == LogType.C_ERR || item.type() == LogType.S_ERR) {
				printText(logArea, item.toString(), Color.RED);
			} else if(item.type() == LogType.C_WARN || item.type() == LogType.S_WARN) {
				printText(logArea, item.toString(), Color.YELLOW);
			} else {
				printText(logArea, item.toString(), Color.WHITE);
			}
		}
	}
	
	@SuppressWarnings("unused")
	// TODO: burası kodlanacak
	private void sendCommand(String command) {
		var world = DikenEngine.getEngine().getWorld();
		
		if (true) {
			var script = new Script();
			script.setSource(command);
			script.initialize(world);
		} else {
			DikenEngine.errorLog("Your game doesn't allow you to run this command!");
		}
	}
}
