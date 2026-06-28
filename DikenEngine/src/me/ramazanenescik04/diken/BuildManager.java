package me.ramazanenescik04.diken;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

public final class BuildManager {
    private BuildManager() {
        throw new UnsupportedOperationException("Bu bir yardımcı sınıftır, nesnesi oluşturulamaz.");
    }

    private static boolean isUsingJDK() {
    	String javaHome = System.getenv("JAVA_HOME");

        if (javaHome != null && javaHome.toLowerCase().contains("jdk")) {
            return true;
        }
        
        return false;
    }
    
    public static interface IBuildInfo {
    	void buildSuccessful(Path path);
		void buildLog(String log);
    	void buildError(String error);
    	void buildError(String error, Throwable e);
    }
    
    private static class DefaultBuildInfo implements IBuildInfo {
		@Override
		public void buildSuccessful(Path p) {
			DikenEngine.log("Dışarıya Aktarme Başarıyla Tamamlandı!\n- Konum: " + p.toString() + " -");
		}

		@Override
		public void buildLog(String log) {
			DikenEngine.log(log);
		}

		@Override
		public void buildError(String error) {
			DikenEngine.errorLog(error);
		}

		@Override
		public void buildError(String error, Throwable e) {
			DikenEngine.errorLog(error, e);
		}
    }
}