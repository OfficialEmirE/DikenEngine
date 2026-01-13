package me.ramazanenescik04.diken.gui.window;

import java.util.function.Consumer;

/**
 * DikenEngine için geliştirilmiş evrensel ayar sınıfı.
 * @param <T> Ayarın tutacağı veri tipi (Boolean, String, Integer, Float vb.)
 */
public class Setting<T> {

    private String name;
    private String description; // Ayarın ne işe yaradığını gösteren tooltip için
    private T value;
    private final T defaultValue;
    private final EnumSettingType type;
    
    // Değer değiştiğinde tetiklenecek olay (Lambda fonksiyonu)
    private Consumer<T> onChangeEvent;

    // Sayısal değerler için (Slider/Scrollbar) opsiyonel sınırlar
    private T min;
    private T max;

    // Basit Constructor
    public Setting(String name, T defaultValue, EnumSettingType type) {
        this.name = name;
        this.value = defaultValue;
        this.defaultValue = defaultValue;
        this.type = type;
    }

    // Slider/Scrollbar için gelişmiş Constructor (Min/Max içeren)
    public Setting(String name, T defaultValue, T min, T max, EnumSettingType type) {
        this(name, defaultValue, type);
        this.min = min;
        this.max = max;
    }

    // --- Getter ve Setter Metotları ---

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

    // --- Enum ---
    public enum EnumSettingType {
        CHECK_BOX,      // Boolean değerler için
        TEXT_FIELD,     // String değerler için
        SLIDER,         // Float/Double/Int değerler için (ScrollBar yerine Slider daha yaygın terimdir)
        COLOR_PICKER,   // Renk seçimi için
        KEY_BIND        // Tuş atamaları için
    }
}