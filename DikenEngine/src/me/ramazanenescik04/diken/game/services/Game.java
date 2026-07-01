package me.ramazanenescik04.diken.game.services;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import org.luaj.vm2.LuaValue;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.setting.SettingCategory;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

public class Game extends AbstractService {
	public Game() {
		this("game");
	}

	public Game(String name) {
		super(name);
	}

	public Game(DataInputStream in) throws IOException {
		super(in);
		loadNodeData(in);
	}
	
	public Object HttpSend(String url, String requestMethod, Object data) {
		try {
			var httpClient = HttpClient.newHttpClient();
			var httpRequestBuilder = HttpRequest.newBuilder(URI.create(url))
					.GET()
					.header("User-Agent",
							"Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:151.0) Gecko/20100101 Firefox/151.0");
			
			HttpRequest.BodyPublisher bodyPublisher = HttpRequest.BodyPublishers.noBody();
	        
	        if (data != null) {
	        	if (data instanceof byte[] bytesData) {
	                bodyPublisher = HttpRequest.BodyPublishers.ofByteArray(bytesData);
	                
	                httpRequestBuilder.header("Content-Type", "application/octet-stream");
	            } else if (data instanceof String strData) {
	                bodyPublisher = HttpRequest.BodyPublishers.ofString(strData);
	            } else {
	                bodyPublisher = HttpRequest.BodyPublishers.ofString(data.toString());
	                
	                httpRequestBuilder.header("Content-Type", "application/json");
	            }
	        }
			
			switch (requestMethod) {
				case "GET" -> httpRequestBuilder.GET();
				case "POST" -> httpRequestBuilder.POST(bodyPublisher);
				case "PUT" -> httpRequestBuilder.PUT(bodyPublisher);
				case "DELETE" -> httpRequestBuilder.DELETE();
				default -> throw new IllegalArgumentException("Desteklenmeyen HTTP metodu: " + requestMethod);
			}

			var response = httpClient.send(httpRequestBuilder.build(), HttpResponse.BodyHandlers.ofString());

			return response.body();
		} catch (Exception e) {
			DikenEngine.errorLog("HttpSend Error: " + e.getMessage());
		}
		
		return null;
	}
	
	public Object HttpGet(String url) {
		return HttpSend(url, "GET", "");
	}
	
	public Object HttpPost(String url, Object data) {
		return HttpSend(url, "POST", data);
	}
	
	public Runnable toRunnable(LuaValue value) {
		return () -> value.invoke();
	}

	@Override
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("workspace", "Workspace", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(9, 1));
		
		var settingCategory = SettingCategory
				.createSettingCategory(key);
		
		var list = super.getNodeSettings();
		list.add(settingCategory);
		return list;
	}
}
