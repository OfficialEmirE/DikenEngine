package me.ramazanenescik04.diken;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class NativeManager {
    private static final Path NATIVE_DIR;
    
    static {
        NATIVE_DIR = Path.of(System.getProperty("user.home"), "AppData", "Local", "diken-natives");
        try {
            Files.createDirectories(NATIVE_DIR);
            NATIVE_DIR.toFile().deleteOnExit();
        } catch (IOException e) {
            throw new RuntimeException("Failed to create native directory", e);
        }
    }
    
    public static void loadLibraryFromJar(String path) throws IOException {
        var os = SystemInfo.instance.getOS();
        var fullPath = "/natives/" + path;
        var expectedExtension = getLibraryExtension(os);
        
        if (!fullPath.endsWith(expectedExtension)) {
            DikenEngine.log("Native kütüphanenin uzantısı uyumsuz: %s != %s".formatted(fullPath, expectedExtension));
            return;
        }
        
        loadNativeLibrary(fullPath, os);
    }
    
    public static void loadLibraryFromOSPathFromJar(String path) throws IOException {
        var os = SystemInfo.instance.getOS();
        var fullPath = "/natives/%s%s".formatted(os.name(), path);
        var expectedExtension = getLibraryExtension(os);
        
        if (!fullPath.endsWith(expectedExtension)) {
            DikenEngine.log("Native kütüphanenin uzantısı uyumsuz: %s != %s".formatted(fullPath, expectedExtension));
            return;
        }
        
        loadNativeLibrary(fullPath, os);
    }
    
    private static void loadNativeLibrary(String resourcePath, SystemInfo.OS os) throws IOException {
        try (InputStream in = NativeManager.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IOException("Library not found: " + resourcePath);
            }
            
            var libExtension = getLibraryExtension(os);
            var libName = getLibraryName(resourcePath);
            var tempFile = NATIVE_DIR.resolve(libName + libExtension);
            
            // Dosya yoksa kopyala
            if (Files.notExists(tempFile)) {
                Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }
            
            // Native kütüphaneyi yükle
            System.load(tempFile.toAbsolutePath().toString());
        }
    }
    
    public static void loadedNativeSetProperty(String property) {
        System.setProperty(property, NATIVE_DIR.toAbsolutePath().toString());
    }
    
    private static String getLibraryExtension(SystemInfo.OS os) {
        return switch (os) {
            case WINDOWS -> ".dll";
            case MACOS -> ".dylib";
            default -> ".so";
        };
    }

    private static String getLibraryName(String path) {
        var fileName = Path.of(path).getFileName().toString();
        var dotIndex = fileName.lastIndexOf('.');
        return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
    }
}