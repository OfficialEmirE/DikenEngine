package me.ramazanenescik04.diken.tools;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Utils {
	public static String[] readFileArray(File file) {
		if (!file.exists()) {
			return new String[] { "" };
		}

		try {
			Path filePath = file.toPath();
			List<String> list = Files.readAllLines(filePath);

			return list.toArray(new String[list.size()]);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new String[] { "" };
	}

	public static String readFile(File file) {
		String str = "";
		String[] array = readFileArray(file);

		for (int i = 0; i < array.length; i++) {
			str += array[i];
		}

		return str;
	}

	public static <T> void writeFileArray(File file, T[] array) {
		try {
			if (!file.exists()) {
				file.createNewFile();
			}

			ObjectOutputStream stream = new ObjectOutputStream(new FileOutputStream(file));
			for (T var : array) {
				if (var instanceof java.io.Serializable) {
					stream.writeObject(var);
				} else {
					stream.writeUTF(var.toString());
				}
			}
			stream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static String[] getStackTraceStringArray(Throwable _throw) {
		StringWriter writer = new StringWriter();
		_throw.printStackTrace(new PrintWriter(writer));

		return writer.toString().lines().toArray(String[]::new);
	}

	public static String getStackTraceString(Throwable _throw) {
		StringWriter writer = new StringWriter();
		_throw.printStackTrace(new PrintWriter(writer));

		return writer.toString();
	}

	public static long timeToLong(int... time) {
		if (time.length == 0) {
			return 0;
		}

		long total = 0;
		int multiplier = 1;

		for (int i = time.length - 1; i >= 0; i--) {
			total += time[i] * multiplier;
			multiplier *= 60;
		}

		return total * 1000; // Convert to milliseconds
	}

	public static int toProccesBarValue(long currentValue, long maxValue, int width) {
		if (maxValue <= 0) {
			return 0;
		}

		double percentage = (double) currentValue / maxValue;
		return (int) (percentage * width);
	}

	public static String getWebData(String uri) {
		HttpClient client = HttpClient.newHttpClient();

		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(uri))
				.GET()
				.build();

		HttpResponse<String> response = null;
		try {
			response = client.send(request, HttpResponse.BodyHandlers.ofString());
		} catch (Exception e) {
			e.printStackTrace();
			return """
					{
						"status": "error",
						"message": "%s"
					}
					""".formatted(e.getMessage());
		}
		return response.body();
	}
}
