package me.ramazanenescik04.diken.log;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import me.ramazanenescik04.diken.tools.ListAdapter;
import me.ramazanenescik04.diken.tools.ObservableList;

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
		logs.add(new LogText(type, log));
	}
	
	public static void sendLog(String log) {
		sendLog(LogType.CLIENT, log);
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
			return "[" + type.name() + "] " + log;
		}
	}
	
	public static enum LogType {
		SERVER,
		CLIENT
	}
}
