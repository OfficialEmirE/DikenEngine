package me.ramazanenescik04.diken.log;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.tools.ListAdapter;
import me.ramazanenescik04.diken.tools.ObservableList;

/**
 * Represents the `ConsoleLog` type within the DikenEngine `log` package.
 */
public class ConsoleLog {
	private static final List<LogText> logs = new ObservableList<>();
	private static File logsFile = new File("./logs/");
	
	public static void setListAdapter(ListAdapter<LogText> e) {
		((ObservableList<LogText>) logs).setListAdapter(e);
	}
	
	public static File getLogDirectory() {
		return logsFile;
	}
	
	public static void setLogDirectory(File logDirectory) {
		if (logDirectory.isDirectory()) {
			logsFile = logDirectory;
		}
	}
	
	public static void sendLog(LogType type, String log) {
		var logText = new LogText(type, log);
		
		logs.add(logText);
		
		if (type == LogType.C_ERR || type == LogType.S_ERR) {
			System.err.println(logText.toString());
		} else {
			System.out.println(logText.toString());
		}
	}
	
	public static void sendLog(String log) {
		sendLog(LogType.C_LOG, log);
	}
	
	public static List<LogText> getLogs() {
		return new ArrayList<>(logs);
	}
	
	public static List<String> getLogsToString() {
		List<String> list = new ArrayList<>();
		for (LogText text : logs) {
			list.add(text.toString());
		}
		return list;
	}
	
	public static void clear() {
		logs.clear();
	}
	
	public static String logToString() {
		StringBuilder builder = new StringBuilder();
		
		for (LogText text : logs) {
			builder.append(text.log.toString());
		}
		
		return builder.toString();
	}
	
	public static void saveLogs() {
		if (DikenEngine.getEngine().config.getSetting("saveLog", Boolean.class).getValue() == false)
			return;
		
		StringBuilder builder = new StringBuilder();
		
		for (LogText text : logs) {
			builder.append(text.log.toString() + "\n");
		}
		
		try {
			if (!logsFile.exists())
				logsFile.mkdirs();
			
			File logFile = new File(logsFile, new Date().toString().replaceAll(" ", "_").replaceAll(":", "-") + ".txt");
			
			Files.writeString(logFile.toPath(), builder.toString(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public record LogText(LogType type, String log) {
		public String toString() {
			return "[%s] [%s] %s".formatted(new SimpleDateFormat("HH:mm:ss").format(new Date()), type.name(), log);
		}
	}
	
	public static enum LogType {
		// Server LogType
		S_ERR,
		S_WARN,
		S_LOG,

		// Client LogType
		C_ERR,
		C_WARN,
		C_LOG
	}
}
