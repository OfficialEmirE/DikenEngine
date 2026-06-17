package me.ramazanenescik04.diken.language;

import java.io.IOException;
import java.util.List;

import me.ramazanenescik04.diken.DikenEngine;

public record Translation(String key, String value) {

	public static List<Translation> load(String translationsFilePath) {
		try {
			String data = new String(DikenEngine.class.getResourceAsStream(translationsFilePath).readAllBytes());
			
			return data.lines()
				.filter(line -> line.contains("="))
				.map(line -> {
					String[] parts = line.split("=", 2);
					return new Translation(parts[0].trim(), parts[1].trim());
				})
				.toList();
		} catch (IOException e) {
			e.printStackTrace();
			return List.of();
		}
	}
}
