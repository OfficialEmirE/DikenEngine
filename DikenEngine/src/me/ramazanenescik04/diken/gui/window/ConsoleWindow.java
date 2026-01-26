package me.ramazanenescik04.diken.gui.window;

import org.lwjgl.input.Keyboard;

import me.ramazanenescik04.diken.gui.compoment.*;
import me.ramazanenescik04.diken.gui.screen.StaticBackground;
import me.ramazanenescik04.diken.log.ConsoleLog;
import me.ramazanenescik04.diken.log.ConsoleLog.LogText;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.tools.ListAdapter;

public class ConsoleWindow extends Window {
	private static final long serialVersionUID = 1L;
	
	private TextLine textLine;

	public ConsoleWindow(int x, int y, int width, int height) {
		super(x, y, width, height);
		this.setTitle("Konsol");
		this.resizable = true;
	}

	protected void open() {
		Panel contentPane = this.getContentPane();
		this.getContentPane().setBackground(new StaticBackground(Bitmap.createClearedBitmap(16, 16, 0xffa0a0a0)));
		
		ScrollPanel panel = new ScrollPanel(0, 0, contentPane.width, contentPane.height - 20);
		contentPane.add(panel);
		
		textLine = new TextLine(0, 0, contentPane.width, contentPane.height - 20);
		textLine.setTextLines(ConsoleLog.getLogsToString());
		textLine.autoSetSize();
		textLine.setEditable(false);
		textLine.setFocused(false);
		textLine.setActive(false);
		panel.setScrollComponent(textLine);
		
		ConsoleLog.setListAdapter(new ListAdapter<LogText>() {
			@Override
			public void onAdd(LogText item) {
				textLine.add(item.toString());
				textLine.autoSetSize();
			}

			@Override
			public void onRemove(LogText item) {
				textLine.remove(item.toString());
				textLine.autoSetSize();
			}

			@Override
			public void onUpdate() {
				textLine.autoSetSize();
			}

			@Override
			public void onClear() {
				textLine.clear();
				textLine.setBounds(0, 0, contentPane.width, contentPane.height - 20);
			}
		});
		
		TextField textField = new TextField(0, contentPane.height - 20, contentPane.width - 50, 20);
		
		Button sendButton = new Button("Gönder", 0, contentPane.height - 20, 50, 20).setRunnable(() -> {
			String text = textField.text;
			if (!text.isEmpty()) {
				textLine.add(text);
				textField.text = "";
			}
		});
		
		contentPane.add(sendButton);
		contentPane.add(textField);
	}

	public void resized() {
		super.resized();
		
		Panel contentPane = this.getContentPane();
		ScrollPanel textLine = (ScrollPanel) contentPane.get(0);
		textLine.setSize(contentPane.width, contentPane.height - 20);
		
		Button sendButton = (Button) contentPane.get(1);
		sendButton.setLocation(contentPane.width - 50, contentPane.height - 20);
		
		TextField textField = (TextField) contentPane.get(2);
		textField.setLocation(0, contentPane.height - 20);
		textField.setSize(contentPane.width - 50, 20);
	}

	public void keyPressed(char var1, int var2) {
		super.keyPressed(var1, var2);
		
		if (var2 == Keyboard.KEY_RETURN || var2 == Keyboard.KEY_NUMPADENTER) {
			Panel contentPane = this.getContentPane();
			TextField textField = (TextField) contentPane.get(2);
			
			String text = textField.text;
			if (!text.isEmpty()) {
				textLine.add(text);
				textField.text = "";
			}
		}
	}

	@Override
	public void close() {
		super.close();
		
		ConsoleLog.setListAdapter(null);
	}
}
