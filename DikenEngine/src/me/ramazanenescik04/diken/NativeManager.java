package me.ramazanenescik04.diken;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import me.ramazanenescik04.diken.SystemInfo.Architecture;
import me.ramazanenescik04.diken.SystemInfo.OS;

/**
 * Extracts native libraries from the classpath and loads them with System.load.
 */
public final class NativeManager {
	private static final String RESOURCE_ROOT = "natives";
	private static final Set<String> loadedPaths = new HashSet<String>();

	private static boolean loaded;
	private static File extractedNativeDirectory;

	private NativeManager() {
	}

	public static synchronized void loadNatives() {
		if (loaded) {
			return;
		}

		try {
			extractedNativeDirectory = createExtractedNativeDirectory();
			List<File> libraries = extractNativeLibraries(extractedNativeDirectory);

			if (libraries.isEmpty()) {
				throw new IllegalStateException("No native libraries found for " + SystemInfo.instance.getOS() + " "
						+ SystemInfo.instance.getArch());
			}

			System.setProperty("org.lwjgl.librarypath", extractedNativeDirectory.getAbsolutePath());

			for (File library : libraries) {
				loadNative(library);
			}

			loaded = true;
		} catch (IOException e) {
			throw new IllegalStateException("Failed to extract native libraries.", e);
		}
	}

	public static File getNativeDirectory() {
		if (extractedNativeDirectory != null) {
			return extractedNativeDirectory;
		}

		try {
			extractedNativeDirectory = createExtractedNativeDirectory();
			return extractedNativeDirectory;
		} catch (IOException e) {
			throw new IllegalStateException("Failed to create native directory.", e);
		}
	}

	public static List<File> extractNativeLibraries(File targetDirectory) throws IOException {
		List<File> libraries = new ArrayList<File>();
		for (String fileName : getNativeFileNames()) {
			if (!matchesCurrentArchitecture(fileName)) {
				continue;
			}

			File extractedFile = extractNativeLibrary(fileName, targetDirectory);
			if (extractedFile != null) {
				libraries.add(extractedFile);
			}
		}
		return libraries;
	}

	public static synchronized void loadNative(File library) {
		String absolutePath = library.getAbsolutePath();
		if (loadedPaths.contains(absolutePath)) {
			return;
		}

		System.load(absolutePath);
		loadedPaths.add(absolutePath);
	}

	private static File createExtractedNativeDirectory() throws IOException {
		String directoryName = "DikenEngine-natives-" + SystemInfo.instance.getOS().name().toLowerCase(Locale.ROOT)
				+ "-" + SystemInfo.instance.getArch().name().toLowerCase(Locale.ROOT) + "-";
		Path directory = Files.createTempDirectory(directoryName);
		directory.toFile().deleteOnExit();
		return directory.toFile();
	}

	private static File extractNativeLibrary(String fileName, File targetDirectory) throws IOException {
		String resourcePath = RESOURCE_ROOT + "/" + getNativeFolderName(SystemInfo.instance.getOS()) + "/" + fileName;

		try (InputStream stream = NativeManager.class.getClassLoader().getResourceAsStream(resourcePath)) {
			if (stream == null) {
				return null;
			}

			File targetFile = new File(targetDirectory, fileName);
			Files.copy(stream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			targetFile.deleteOnExit();
			return targetFile;
		}
	}

	private static List<String> getNativeFileNames() {
		switch (SystemInfo.instance.getOS()) {
			case WINDOWS:
				return Arrays.asList("OpenAL32.dll", "OpenAL64.dll", "jinput-dx8.dll", "jinput-dx8_64.dll",
						"jinput-raw.dll", "jinput-raw_64.dll", "lwjgl.dll", "lwjgl64.dll");
			case LINUX:
				return Arrays.asList("libopenal.so", "libopenal64.so", "libjinput-linux.so", "libjinput-linux64.so",
						"liblwjgl.so", "liblwjgl64.so");
			case MACOS:
				return Arrays.asList("openal.dylib", "libjinput-osx.dylib", "liblwjgl.dylib");
			default:
				throw new UnsupportedOperationException("Unsupported native OS: " + SystemInfo.instance.getOS());
		}
	}

	private static String getNativeFolderName(OS os) {
		switch (os) {
			case WINDOWS:
				return "WINDOWS";
			case LINUX:
				return "LINUX";
			case MACOS:
				return "MACOS";
			default:
				throw new UnsupportedOperationException("Unsupported native OS: " + os);
		}
	}

	private static boolean matchesCurrentArchitecture(String fileName) {
		String name = fileName.toLowerCase(Locale.ROOT);
		Architecture arch = SystemInfo.instance.getArch();

		if (SystemInfo.instance.getOS() == OS.MACOS) {
			return true;
		}

		if (arch == Architecture.X86_64 || arch == Architecture.ARM64) {
			return hasAny(name, "64", "amd64", "x86_64", "aarch64");
		}

		if (arch == Architecture.X86 || arch == Architecture.ARM) {
			return !hasAny(name, "64", "amd64", "x86_64", "aarch64");
		}

		return true;
	}

	private static boolean hasAny(String text, String... parts) {
		return Arrays.stream(parts).anyMatch(text::contains);
	}
}
