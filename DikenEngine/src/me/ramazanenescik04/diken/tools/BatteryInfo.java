package me.ramazanenescik04.diken.tools;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.sun.jna.*;

import me.ramazanenescik04.diken.SystemInfo;

/**
 * Bataryanın Bilgisini verir.
 * 
 * @author Ramazanenescik04
 */
public abstract class BatteryInfo {
	// Thread Güvenliği
	private static BatteryInfo instance;
	
	/**
	 * BatteryInfonun instance'i verir
	 * 
	 * @throws
	 * 		NullPointerException
	 * @return Instance
	 */
	
	public byte ACLineStatus;
    public byte BatteryFlag;
    public byte BatteryLifePercent;
    public byte Reserved1;
    public int BatteryLifeTime;
    public int BatteryFullLifeTime;
	
	public static BatteryInfo getInstance() {
		if (instance == null) {
			SystemInfo.OS os = SystemInfo.instance.getOS();
			
			if (os == SystemInfo.OS.WINDOWS) {
				instance = new WindowsBatteryInfo();
			} else if (os == SystemInfo.OS.MACOS) {
				instance = new LinuxBatteryInfo();
			} else if (os == SystemInfo.OS.LINUX) {
				instance = new MacBatteryInfo();
			} else {
				instance = null;
			}
		}
		
		instance.updateBatteryInfo();
		
		return instance;
	}
	
	/**
	 * Bu Method Batarya Durumunu Günceller
	 * 
	 */
	public abstract void updateBatteryInfo();

	/**
	 * Windows İçin
	 */
	private static class WindowsBatteryInfo extends BatteryInfo {
		
		private static SYSTEM_POWER_STATUS system_power_status = new SYSTEM_POWER_STATUS();
		
		public interface Kernel32 extends Library {
            Kernel32 INSTANCE = (Kernel32) Native.load("kernel32", Kernel32.class);
            
            int GetSystemPowerStatus(SYSTEM_POWER_STATUS result);
        }
        
        public static class SYSTEM_POWER_STATUS extends com.sun.jna.Structure {
            public byte ACLineStatus;
            public byte BatteryFlag;
            public byte BatteryLifePercent;
            public byte Reserved1;
            public int BatteryLifeTime;
            public int BatteryFullLifeTime;
            
            @Override
            protected java.util.List<String> getFieldOrder() {
                return java.util.Arrays.asList("ACLineStatus", "BatteryFlag", "BatteryLifePercent", 
                                              "Reserved1", "BatteryLifeTime", "BatteryFullLifeTime");
            }
        }

		public void updateBatteryInfo() {
			Kernel32.INSTANCE.GetSystemPowerStatus(system_power_status);
			
			this.ACLineStatus = system_power_status.ACLineStatus;
			this.BatteryFlag = system_power_status.BatteryFlag;
	        this.BatteryLifePercent = system_power_status.BatteryLifePercent;
	        this.Reserved1 = system_power_status.Reserved1;
	        this.BatteryLifeTime = system_power_status.BatteryLifeTime;
	        this.BatteryFullLifeTime = system_power_status.BatteryFullLifeTime;
		}
		
	}
	
	/**
	 * Linux İçin
	 */
	private static class LinuxBatteryInfo extends BatteryInfo {

		public void updateBatteryInfo() {
			String[] batteryPaths = {
					"/sys/class/power_supply/BAT0",
	    		"/sys/class/power_supply/BAT1",
	    		"/sys/class/power_supply/battery"
			};
			
			String batteryPath = null;
	        for (String path : batteryPaths) {
	            if (Files.exists(Paths.get(path))) {
	                batteryPath = path;
	                break;
	            }
	        }
	        
	        if (batteryPath == null) {
	            this.ACLineStatus = (byte)255;  // Bilinmiyor
	            this.BatteryFlag = (byte)128;   // Pil yok
	            this.BatteryLifePercent = (byte)255;  // Bilinmiyor
	            this.BatteryLifeTime = -1;
	            this.BatteryFullLifeTime = -1;
	        }
	        
	        // AC durumu kontrolü
	        boolean acOnline = false;
	        String[] acPaths = {
	            "/sys/class/power_supply/AC/online",
	            "/sys/class/power_supply/AC0/online",
	            "/sys/class/power_supply/ADP0/online",
	            "/sys/class/power_supply/ADP1/online"
	        };
	        try {
	        	for (String path : acPaths) {
	        		if (Files.exists(Paths.get(path))) {
	        			String acStatus = new String(Files.readAllBytes(Paths.get(path))).trim();
	        			acOnline = "1".equals(acStatus);
	                	break;
	            	}
	        	}
	        } catch (Exception e) {
	        	e.printStackTrace();
	        }
	        
	        this.ACLineStatus = (byte) (acOnline ? 1 : 0);
	        
	        
	     // Pil durumu
	        try {
	            // Şarj yüzdesi
	            if (Files.exists(Paths.get(batteryPath + "/capacity"))) {
	                String capacity = new String(Files.readAllBytes(Paths.get(batteryPath + "/capacity"))).trim();
	                this.BatteryLifePercent = Byte.parseByte(capacity);
	            } else {
	            	this.BatteryLifePercent = (byte)255;  // Bilinmiyor
	            }
	            
	            // Pil durumu
	            if (Files.exists(Paths.get(batteryPath + "/status"))) {
	                String batteryStatus = new String(Files.readAllBytes(Paths.get(batteryPath + "/status"))).trim();
	                
	                switch (batteryStatus.toLowerCase()) {
	                    case "charging":
	                    	this.BatteryFlag = (byte)8;  // Şarj oluyor
	                        break;
	                    case "discharging":
	                        if (this.BatteryLifePercent < 10)
	                        	this.BatteryFlag = (byte)4;  // Kritik
	                        else if (this.BatteryLifePercent < 30)
	                        	this.BatteryFlag = (byte)2;  // Düşük
	                        else
	                        	this.BatteryFlag = (byte)1;  // Yüksek
	                        break;
	                    case "full":
	                    	this.BatteryFlag = (byte)1;  // Yüksek
	                        break;
	                    default:
	                    	this.BatteryFlag = (byte)255;  // Bilinmiyor
	                }
	            } else {
	            	this.BatteryFlag = (byte)255;  // Bilinmiyor
	            }
	            
	            // Enerji bilgilerini al
	            long energyNow = -1;
	            long energyFull = -1;
	            long powerNow = -1;
	            
	            // Enerji değerleri (µWh cinsinden)
	            if (Files.exists(Paths.get(batteryPath + "/energy_now"))) {
	                String energyStr = new String(Files.readAllBytes(Paths.get(batteryPath + "/energy_now"))).trim();
	                energyNow = Long.parseLong(energyStr);
	            } else if (Files.exists(Paths.get(batteryPath + "/charge_now"))) {
	                // Bazı sistemler charge_now kullanır (µAh cinsinden)
	                String chargeStr = new String(Files.readAllBytes(Paths.get(batteryPath + "/charge_now"))).trim();
	                energyNow = Long.parseLong(chargeStr);
	            }
	            
	            if (Files.exists(Paths.get(batteryPath + "/energy_full"))) {
	                String energyStr = new String(Files.readAllBytes(Paths.get(batteryPath + "/energy_full"))).trim();
	                energyFull = Long.parseLong(energyStr);
	            } else if (Files.exists(Paths.get(batteryPath + "/charge_full"))) {
	                String chargeStr = new String(Files.readAllBytes(Paths.get(batteryPath + "/charge_full"))).trim();
	                energyFull = Long.parseLong(chargeStr);
	            }
	            
	            // Güç tüketimi (µW cinsinden)
	            if (Files.exists(Paths.get(batteryPath + "/power_now"))) {
	                String powerStr = new String(Files.readAllBytes(Paths.get(batteryPath + "/power_now"))).trim();
	                powerNow = Long.parseLong(powerStr);
	            } else if (Files.exists(Paths.get(batteryPath + "/current_now"))) {
	                String currentStr = new String(Files.readAllBytes(Paths.get(batteryPath + "/current_now"))).trim();
	                powerNow = Long.parseLong(currentStr);
	            }
	            
	            // Kalan süreyi hesapla (saniye cinsinden)
	            if (energyNow != -1 && powerNow != -1 && powerNow > 0) {
	            	this.BatteryLifeTime = (int) ((energyNow * 3600) / powerNow);  // µWh / µW = saat, sonra saniyeye çevir
	            } else {
	            	this.BatteryLifeTime = -1;  // Bilinmiyor
	            }
	            
	            // Tam şarj kapasitesini hesapla (saniye cinsinden)
	            if (energyFull != -1 && powerNow != -1 && powerNow > 0) {
	            	this.BatteryFullLifeTime = (int) ((energyFull * 3600) / powerNow);
	            } else {
	            	this.BatteryFullLifeTime = -1;  // Bilinmiyor
	            }
	            
	        } catch (Exception e) {
	            e.printStackTrace();
	            this.BatteryLifePercent = (byte)255;  // Bilinmiyor
	            this.BatteryFlag = (byte)255;  // Bilinmiyor
	            this.BatteryLifeTime = -1;
	            this.BatteryFullLifeTime = -1;
	        }
		}
		
	}
	
	/**
	 * MacOS İçin
	 */
	private static class MacBatteryInfo extends BatteryInfo {

		public void updateBatteryInfo() {
			try {
				// pmset ve ioreg komutları ile pil bilgisini al
		        Process pmsetProcess = Runtime.getRuntime().exec(new String[] {"pmset", "-g", "batt"});
		        pmsetProcess.waitFor();
		        
		        BufferedReader pmsetReader = new BufferedReader(new InputStreamReader(pmsetProcess.getInputStream()));
		        String pmsetLine;
		        
		        // AC durumu ve yüzde bilgisi için pmset çıktısını okuma
		        while ((pmsetLine = pmsetReader.readLine()) != null) {
		            if (pmsetLine.contains("%")) {
		                // AC durum kontrolü
		                this.ACLineStatus = pmsetLine.contains("AC attached") || 
		                                    pmsetLine.contains("charging") ? (byte)1 : (byte)0;
		                
		                // Pil durumu
		                if (pmsetLine.contains("charging")) {
		                	this.BatteryFlag = (byte)8;  // Şarj oluyor
		                } else {
		                    // Şarj durumuna göre bayrak ayarla
		                    if (pmsetLine.matches(".*?(\\d+)%.*")) {
		                        int percentage = Integer.parseInt(pmsetLine.replaceAll(".*?(\\d+)%.*", "$1"));
		                        this.BatteryLifePercent = (byte)percentage;
		                        
		                        if (percentage < 10)
		                        	this.BatteryFlag = (byte)4;  // Kritik
		                        else if (percentage < 30)
		                        	this.BatteryFlag = (byte)2;  // Düşük
		                        else
		                        	this.BatteryFlag = (byte)1;  // Yüksek
		                    } else {
		                    	this.BatteryLifePercent = (byte)255;  // Bilinmiyor
		                        this.BatteryFlag = (byte)255;  // Bilinmiyor
		                    }
		                }
		                
		                // Kalan süre bilgisini ayıkla
		                if (pmsetLine.matches(".*?(\\d+:\\d+)\\s+remaining.*")) {
		                    String timeStr = pmsetLine.replaceAll(".*?(\\d+):(\\d+)\\s+remaining.*", "$1:$2");
		                    String[] timeParts = timeStr.split(":");
		                    int hours = Integer.parseInt(timeParts[0]);
		                    int minutes = Integer.parseInt(timeParts[1]);
		                    this.BatteryLifeTime = (hours * 3600) + (minutes * 60);  // Saniyeye çevir
		                } else {
		                	this.BatteryLifeTime = -1;  // Bilinmiyor
		                }
		                
		                break;
		            }
		        }
		        
		        // Tam şarj kapasitesi için ioreg komutunu kullan
		        Process ioregProcess = Runtime.getRuntime().exec(new String[] {"ioreg", "-l", "-w0", "|", "grep", "Capacity"});
		        ioregProcess.waitFor();
		        
		        BufferedReader ioregReader = new BufferedReader(new InputStreamReader(ioregProcess.getInputStream()));
		        String ioregLine;
		        
		        int maxCapacity = -1;
		        int currentCapacity = -1;
		        //int designCapacity = -1;
		        
		        while ((ioregLine = ioregReader.readLine()) != null) {
		            if (ioregLine.contains("MaxCapacity")) {
		                maxCapacity = Integer.parseInt(ioregLine.replaceAll(".*\"MaxCapacity\"\\s*=\\s*(\\d+).*", "$1"));
		            } else if (ioregLine.contains("CurrentCapacity")) {
		                currentCapacity = Integer.parseInt(ioregLine.replaceAll(".*\"CurrentCapacity\"\\s*=\\s*(\\d+).*", "$1"));
		            } else if (ioregLine.contains("DesignCapacity")) {
		                //designCapacity = Integer.parseInt(ioregLine.replaceAll(".*\"DesignCapacity\"\\s*=\\s*(\\d+).*", "$1"));
		            }
		        }
		        
		        // Full life time hesaplama (çok yaklaşık bir hesaplama)
		        if (this.BatteryLifeTime != -1 && currentCapacity > 0 && maxCapacity > 0) {
		        	this.BatteryFullLifeTime = (int) ((double) this.BatteryLifeTime / currentCapacity * maxCapacity);
		        } else {
		        	this.BatteryFullLifeTime = -1;
		        }
		        
		        // Eğer yüzde bilgisi alınamadıysa ve kapasite değerleri mevcutsa hesapla
		        if (this.BatteryLifePercent == (byte)255 && currentCapacity > 0 && maxCapacity > 0) {
		        	this.BatteryLifePercent = (byte)((currentCapacity * 100) / maxCapacity);
		        }
			} catch (Exception e) {
				e.printStackTrace();
				this.ACLineStatus = (byte)255;  // Bilinmiyor
		        this.BatteryFlag = (byte)128;   // Pil yok
		        this.BatteryLifePercent = (byte)255;  // Bilinmiyor
		        this.BatteryLifeTime = -1;
		        this.BatteryFullLifeTime = -1;
			}
		}

	}
	
}
