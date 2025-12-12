package me.ramazanenescik04.diken;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

import org.json.JSONObject;

import me.ramazanenescik04.diken.tools.Utils;
import me.ramazanenescik04.reportbugs.Issue;
import me.ramazanenescik04.reportbugs.gui.ReportBugGUI;
import me.ramazanenescik04.reportbugs.handler.BugHandler;

public class DEngineBugHandler implements BugHandler {
	
	public void handle(Issue arg0) {
		JSONObject json = new JSONObject();
		json.put("title", arg0.getTitle());
		json.put("body", arg0.getDesc());
		/*json.put("timestamp", arg0.getTimestamp());
		json.put("severity", arg0.getSeverity().ordinal());*/
		
		try {
			URL url = URI.create("https://dikenengine-bot-production.up.railway.app/report").toURL();
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setDoOutput(true);

			try (OutputStream os = conn.getOutputStream()) {
			    os.write(json.toString(2).getBytes(StandardCharsets.UTF_8));
			}
			
			DikenEngine.log(conn.getResponseMessage() + " - " + conn.getResponseCode());
			
		} catch (Exception e) {
			e.printStackTrace();
			String lines = Utils.getStackTraceString(e);
			
			ReportBugGUI.showError(e.getMessage(), lines);
		}
	}
}
