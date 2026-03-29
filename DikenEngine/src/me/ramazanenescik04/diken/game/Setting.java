package me.ramazanenescik04.diken.game;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * DikenEngine için geliştirilmiş evrensel ayar sınıfı.
 * @param <T> Ayarın tutacağı veri tipi (Boolean, String, Integer, Float vb.)
 */
public class Setting<T> {
	private final Class<T> valueClass;

    private String name;
    private String description = ""; // Ayarın ne işe yaradığını gösteren tooltip için
    private T value;
    private final T defaultValue;
    private final EnumSettingType type;
    
    // Değer değiştiğinde tetiklenecek olay (Lambda fonksiyonu)
    private Consumer<T> onChangeEvent;

    // Sayısal değerler için (Slider/Scrollbar) opsiyonel sınırlar
    private T min;
    private T max;
    
    private boolean changeable = true; // Ayarın değiştirilebilir olup olmadığını kontrol eder (örneğin, bazı ayarlar sadece belirli koşullarda değiştirilebilir olabilir)

    // Basit Constructor
	public Setting(String name, T defaultValue, Class<T> type, EnumSettingType settingType) {
		Objects.requireNonNull(settingType);
		Objects.requireNonNull(type);
		
        this.name = name;
        this.value = defaultValue;
        this.defaultValue = defaultValue;
        this.type = settingType;
        
        if (settingType.isAllowedClasses(type)) {
        	this.valueClass = type;
        } else {
        	throw new IllegalStateException("EnumSettingType'deki allowedClasses ler type sınıfıyla uyumlu değil!");
        }
    }

    // Slider/Scrollbar için gelişmiş Constructor (Min/Max içeren)
    public Setting(String name, T defaultValue, T min, T max, Class<T> type, EnumSettingType settingType) {
        this(name, defaultValue, type, settingType);
        this.min = min;
        this.max = max;
    }
    
    // Deep Copy için Constructor
    public Setting(Setting<T> setting) {
        // Temel alanları kopyala
        this.name = setting.name;
        this.value = setting.value;
        this.defaultValue = setting.defaultValue;
        this.type = setting.type; // EnumSettingType (Immutable kabul edilir)
        this.valueClass = setting.valueClass;
        
        // Opsiyonel Slider/Scrollbar alanlarını kopyala
        this.min = (T) setting.min;
        this.max = (T) setting.max;
    }

    // --- Getter ve Setter Metotları ---
    
    public boolean isChangeable() {
		return changeable;
	}
    
    public void setChangeable(boolean changeable) {
    	this.changeable = changeable;
    }
    
    public boolean isDefault() {
		return Objects.equals(value, defaultValue);
	}
    
    public T getDefaultValue() {
		return defaultValue;
	}
    
    public boolean isSlider() {
		return type == EnumSettingType.SLIDER;
	}

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        // Eğer değer değişmediyse işlem yapma (Performans)
        if (this.value != null && this.value.equals(value)) return;

        // Sayısal sınır kontrolü (Eğer sayısal bir ayarsa ve sınırlar belirlendiyse)
        if (min != null && max != null && value instanceof Number) {
            double val = ((Number) value).doubleValue();
            double minVal = ((Number) min).doubleValue();
            double maxVal = ((Number) max).doubleValue();
            
            if (val < minVal) value = min;
            else if (val > maxVal) value = max;
        }

        this.value = value;

        // Eğer bir dinleyici (listener) varsa onu tetikle
        if (onChangeEvent != null) {
            onChangeEvent.accept(this.value);
        }
    }

    public void reset() {
        setValue(defaultValue);
    }

    // Fluent API stili: Ayara bir açıklama eklemek için zincirleme metod
    public Setting<T> setDescription(String description) {
        this.description = description;
        return this; // this döndürerek zincirleme kullanımı sağlar
    }

    // Değişiklik olduğunda çalışacak kodu ayarla
    public Setting<T> addChangeListener(Consumer<T> action) {
        this.onChangeEvent = action;
        return this;
    }

    public String getName() { return name; }
    public EnumSettingType getType() { return type; }
    public String getDescription() { return description; }
    public T getMin() { return min; }
    public T getMax() { return max; }
	public Class<T> getTypeClass() { return valueClass; }
	
	private void writeObject(Object value, DataOutput out) throws IOException {
	    if (value == null) {
	        out.writeByte(-1);
	        return;
	    }

	    switch (value) {
	        case Boolean b -> {
	            out.writeByte(0);
	            out.writeBoolean(b);
	        }
	        case Integer i -> {
	            out.writeByte(1);
	            out.writeInt(i);
	        }
	        case Float f -> {
	            out.writeByte(2);
	            out.writeFloat(f);
	        }
	        case Double d -> {
	            out.writeByte(3);
	            out.writeDouble(d);
	        }
	        case String s -> {
	            out.writeByte(4);
	            out.writeUTF(s);
	        }
	        case Byte b -> {
	            out.writeByte(5);
	            out.writeByte(b);
	        }
	        case Short s -> {
	            out.writeByte(6);
	            out.writeShort(s);
	        }
	        case Character c -> {
	            out.writeByte(7);
	            out.writeChar(c);
	        }
	        case Class<?> clazz -> {
	            out.writeByte(8);
	            out.writeUTF(clazz.getName());
	        }
	        default -> throw new IllegalArgumentException("Unsupported type: " + value.getClass());
	    }
	}
	
	private static Object readObject(DataInput in) throws IOException, ClassNotFoundException {
		var classType = in.readByte();
    	Object defaultSetting = null;
    	
    	switch(classType) {
    		case(0) -> defaultSetting = (in.readBoolean());
    		case(1) -> defaultSetting = (in.readInt());
    		case(2) -> defaultSetting = (in.readFloat());
    		case(3) -> defaultSetting = (in.readDouble());
    		case(4) -> defaultSetting = (in.readUTF());
    		case(5) -> defaultSetting = (in.readByte());
    		case(6) -> defaultSetting = (in.readShort());
    		case(7) -> defaultSetting = (in.readChar());
    		case 8 -> {
    		    String className = in.readUTF();
    		    try {
    		        defaultSetting = switch (className) {
    		            case "int"     -> int.class;
    		            case "boolean" -> boolean.class;
    		            case "byte"    -> byte.class;
    		            case "short"   -> short.class;
    		            case "long"    -> long.class;
    		            case "float"   -> float.class;
    		            case "double"  -> double.class;
    		            case "char"    -> char.class;
    		            default        -> Class.forName(className);
    		        };
    		    } catch (ClassNotFoundException e) {
    		        e.printStackTrace();
    		        defaultSetting = null;
    		    }
    		}
    		case(-1) -> defaultSetting = null;
    	}
    	
    	return defaultSetting;
	}
 
    @java.io.Serial
    public void writeSetting(DataOutput out) throws IOException {
    	out.writeUTF(this.name);
    	out.writeUTF(this.description);
    	out.writeInt(this.type.ordinal());
    	
    	writeObject(this.valueClass, out);
    	writeObject(this.value, out);
    	writeObject(this.defaultValue, out);
    	writeObject(this.min, out);
    	writeObject(this.max, out);
    }
    
    @SuppressWarnings("unchecked")
	@java.io.Serial
    public static <T> Setting<T> readSetting(DataInput in) throws IOException {
    	String name = in.readUTF();
    	String desc = in.readUTF();
    	int typeID = in.readInt();
    	
    	Class<?> clazz = null;
    	Object value = null;
    	Object defaultValue = null;
    	Object min = null;
    	Object max = null;
		try {
			clazz = (Class<?>) readObject(in);
			value = readObject(in);
			defaultValue = readObject(in);
			min = readObject(in);
			max = readObject(in);
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
    	
		Setting<T> setting = new Setting<T>(name, (T) defaultValue, (Class<T>) clazz, EnumSettingType.values()[typeID]);
    	setting.setDescription(desc);
    	setting.setValue((T) value);
    	
    	setting.min = (T) min;
    	setting.max = (T) max;
    	
		return setting;
    }
    
    // --- Enum ---
    public enum EnumSettingType {
        CHECK_BOX(Boolean.class),      					   // Boolean değerler için
        TEXT_FIELD(String.class, Integer.class, 
        	 Short.class, Byte.class, Long.class, 
        	 Float.class, Double.class),     			   // String değerler için
        SLIDER(Float.class, Double.class, Integer.class),  // Float/Double/Int değerler için (ScrollBar yerine Slider daha yaygın terimdir)
        COLOR_PICKER(Integer.class),   					   // Renk seçimi için
        KEY_BIND(Character.class, Integer.class);          // Tuş atamaları için
    	
    	public Class<?>[] allowedClasses;
    	
    	EnumSettingType(Class<?>...classes) {
    		this.allowedClasses = classes;
    	}

		public boolean isAllowedClasses(Class<?> type) {
			return Arrays.stream(allowedClasses).anyMatch(clazz -> clazz.isAssignableFrom(type));
		}
    }
}
