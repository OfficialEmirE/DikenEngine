package me.ramazanenescik04.diken.scripting;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.InstanceList;
import me.ramazanenescik04.diken.game.Node;

public class LuaBridge {
	private Script script;
	
	public LuaBridge(Script script) {
		this.script = script;
	}
	
	public Object create(String className) {
		for (Node node : InstanceList.getNodeList()) {
			if (node.getClass().getSimpleName().equalsIgnoreCase(className)) {
				return node.copy();
			}
		}
		DikenEngine.errorLog("Instance.new Error: '" + className + "' adında bir Node bulunamadı!");
		return null;
	}

	public Object clone(Object object) {
		if (object == null) {
			DikenEngine.errorLog("Instance.clone Error: Object Null Olamaz!");
			return null;
		}

		if (object instanceof Node node) {
			var copyNode = node.copy();
			if (copyNode != null) {
				return copyNode;
			}

			DikenEngine.errorLog(
					"Instance.clone Error: '" + object.getClass().getSimpleName() + ", Archiveable true değil.");
			return null;
		} else if (object instanceof Cloneable c) {
			return c;
		}

		DikenEngine.errorLog(
				"Instance.clone Error: '" + object.getClass().getSimpleName() + ", Klonlamayı desteklemiyor.");
		return null;
	}

	public Object httpGet(String url) {
		try {
			var httpClient = HttpClient.newHttpClient();
			var httpRequest = HttpRequest.newBuilder(URI.create(url)).GET().header("User-Agent",
					"Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:151.0) Gecko/20100101 Firefox/151.0").build();

			var response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

			return response.body();
		} catch (Exception e) {
			DikenEngine.errorLog("HttpGet Error: " + e.getMessage());
		}

		return null;
	}

	public Object getCurrentScript() {
		return this.script;
	}

	public void log(String message) {
		DikenEngine.log(message);
	}
}
