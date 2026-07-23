package me.ramazanenescik04.diken.gui;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.List;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.game.services.UIService;
import me.ramazanenescik04.diken.gui.component.Button;
import me.ramazanenescik04.diken.gui.component.Panel;
import me.ramazanenescik04.diken.gui.component.Panel.BorderStyle;
import me.ramazanenescik04.diken.gui.component.ScreenGui;
import me.ramazanenescik04.diken.gui.component.Text;
import me.ramazanenescik04.diken.gui.component.Text.TextPosition;
import me.ramazanenescik04.diken.gui.component.TextField;
import me.ramazanenescik04.diken.gui.component.TextLine;
import me.ramazanenescik04.diken.log.ConsoleLog;
import me.ramazanenescik04.diken.log.ConsoleLog.LogText;
import me.ramazanenescik04.diken.log.ConsoleLog.LogType;
import me.ramazanenescik04.diken.scripting.Script;
import me.ramazanenescik04.diken.tools.ListAdapter;

public class DebugScreen implements ListAdapter<LogText> {
	public static final DebugScreen instance = new DebugScreen();
	
	private final OperatingSystemMXBean os =
	        ManagementFactory.getOperatingSystemMXBean();

	private long lastTitleUpdate;
	private double cpu;
	
	public UDim2 startPos = UDim2.of(0, 0, 0, 0);
	public boolean allowCommands = true;
	public boolean showDebugInfo = true;
	
	private ScreenGui screenGui;
	private TextLine textLine;
	private Text titleText;
	private boolean isOpen = false;
	
	public DebugScreen() {
		ConsoleLog.setListAdapter(this);
	}
	
	public void init(World theWorld) {
		screenGui = new ScreenGui("DebugScreen");
		screenGui.setArchiveable(false);
		screenGui.OnUpdate.Connect(_ -> this.updateTitle());
		
		var panel = new Panel(startPos, UDim2.of(
											1 - startPos.x.scale,
											-startPos.x.offset,
											1 - startPos.y.scale,
											-startPos.y.offset
										));
		panel.setParent(screenGui);
		panel.setBorderStyle(BorderStyle.Fill);
		panel.setBorderColor(0xffffffff);
		panel.setBackgroundColor(0x40000000);
		
		textLine = new TextLine(UDim2.of(0, 0, 0, 18), UDim2.of(1, 0, 1, (allowCommands ? -36 : -18)));
		textLine.setParent(panel);
		textLine.drawBackground = false;
		textLine.setEditable(false);
		textLine.setFocused(false);
		textLine.setActive(false);
		loadLogs();
		
		var title = new Panel(UDim2.zero, UDim2.of(1, 0, 0, 18));
		title.setParent(panel);
		title.setBorderColor(0xffffffff);
		
		var closeButton = new Button("X", UDim2.of(1, -18, 0, 1), UDim2.of(0, 16, 0, 16));
		closeButton.setRunnable(this::closeDebugScreen);
		closeButton.setButtonColor(0xffff0000);
		closeButton.setTextColor(0xffffffff);
		closeButton.setParent(title);
		
		var helpButton = new Button("?", UDim2.of(1, -36, 0, 1), UDim2.of(0, 16, 0, 16));
		helpButton.setActive(false);
		helpButton.setParent(title);
		
		titleText = new Text(
							"Developer Console",
							UDim2.of(0, 2, 0, 0),
							UDim2.of(1, -2, 1, 0),
							0xff000000,
							"default_font"
						);
		titleText.setParent(title);
		titleText.setTextPosition(TextPosition.West);
		
		if (allowCommands) {
			var commandPanel = new Panel(UDim2.of(0, 0, 1, -17), UDim2.of(1, 0, 0, 17));
			commandPanel.setParent(panel);
			commandPanel.setBorderColor(0xffffffff);
			
			var commandField = new TextField("", UDim2.zero, UDim2.of(1, -28, 1, 0));
			commandField.setParent(commandPanel);
			commandField.setPressedEnter(() -> {
				sendCommand(commandField.getText());
				commandField.setText("");
			});
			
			var sendButton = new Button("Send", UDim2.of(1, -28, 0, 1), UDim2.of(0, 27, 0, 15));
			sendButton.setParent(commandPanel);
			sendButton.setRunnable(() -> {
				sendCommand(commandField.getText());
				commandField.setText("");
			});
		}
	}

	private void sendCommand(String command) {
		var world = DikenEngine.getEngine().getWorld();
		
		var script = new Script();
		script.setSource(command);
		script.initialize(world);
	}
	
	public void openDebugScreen(World theWorld) {
		init(theWorld);
		isOpen = true;
		
		theWorld.getService(UIService.class).addChild(screenGui);
	}
	
	public void closeDebugScreen() {
		screenGui.removeNode();
		isOpen = false;
	}

	public boolean isOpen() {
		return isOpen;
	}
	
	public void updateTitle() {
		if (!showDebugInfo) {
			titleText.setText("Developer Console");
			
			return;
		}
		
	    long now = System.currentTimeMillis();

	    if (now - lastTitleUpdate < 500)
	        return;

	    lastTitleUpdate = now;

	    Runtime runtime = Runtime.getRuntime();

	    long used = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024;
	    long max = runtime.maxMemory() / 1024 / 1024;

	    int percent = (int)((used * 100) / max);

	    if (os instanceof com.sun.management.OperatingSystemMXBean sunOs) {
	        cpu = sunOs.getProcessCpuLoad() * 100.0;
	    }

	    titleText.setText(
	        "Developer Console | FPS: " +
	        DikenEngine.getEngine().currentFPS +
	        " | RAM: " + used + " MB (" + percent + "%)" +
	        " | CPU: " + String.format("%.1f", cpu) + "%"
	    );
	}
	
	private void loadLogs() {
		List<LogText> logs = ConsoleLog.getLogs();
		
		for (int i = 0; i < logs.size(); i++) {
			LogText item = logs.get(i);
			
			printText(item);
		}
	}
	
	private void printText(LogText item) { 
		if (item.type() == LogType.C_ERR || item.type() == LogType.S_ERR) {
			textLine.add("§ff0000" + item.toString());
		} else if(item.type() == LogType.C_WARN || item.type() == LogType.S_WARN) {
			textLine.add("§ffff00" + item.toString());
		} else {
			textLine.add(item.toString());
		}
		
		textLine.autoSetSize();
	}

	public void onAdd(LogText item) { printText(item); }

	public void onRemove(LogText item) {}

	public void onUpdate() {}

	public void onClear() {}
}
