package me.ramazanenescik04.diken.game;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class Config {
	
	private volatile Properties config;
	public static final Properties defaultConfig = new Properties();
	public static File defaultConfigFile = new File("./config.dat");

	public Config() {		
		defaultConfig.setProperty("sync", "false");
		defaultConfig.setProperty("debug", "false");
		defaultConfig.setProperty("lang", "tr-TR");
		
		this.config = defaultConfig;
		this.loadConfig(defaultConfigFile);
	}

	public boolean loadConfig(File configFile) {
		if(configFile == null) {
			System.err.println("File is Null!");
			return false;
		}
		try {
			DataInputStream stream = new DataInputStream(new GZIPInputStream(new FileInputStream(configFile)));
			Properties properties = new Properties();
			int size = stream.readInt();
			for(int i = 0; i < size; i++) {
				String data = stream.readUTF();
				String[] datas = data.split("=");
				properties.setProperty(datas[0], datas[1]);
			}
			stream.close();
			this.config = properties;
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public void saveConfig(File configFile) {
		if(configFile == null) {
			System.err.println("File is Null!");
			return;
		}
		
		try {
			DataOutputStream stream = new DataOutputStream(new GZIPOutputStream(new FileOutputStream(configFile)));
			String[] keys = config.keySet().toArray(new String[] {});
			String[] values = config.values().toArray(new String[] {});
			stream.writeInt(config.size());
			for(int i = 0; i < config.size(); i++) {
				stream.writeUTF(keys[i] + "=" + values[i]);
			}
			stream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean loadConfig() {
		return this.loadConfig(defaultConfigFile);
	}
	
	public void saveConfig() {
		this.saveConfig(defaultConfigFile);
	}
	
	public String getProperty(String key) {
		return this.config.getProperty(key);
	}
	
	public String getOrDefault(String key, String defaultValue) {
		return (String) this.config.getOrDefault(key, defaultValue);
	}
	
	public void setProperty(String key, String value) {
		this.config.setProperty(key, value);
	}
	
	public Properties getConfig() {
		return this.config;
	}
}
