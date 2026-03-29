package me.ramazanenescik04.diken.gui.window;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.Config;
import me.ramazanenescik04.diken.game.Setting;
import me.ramazanenescik04.diken.game.Setting.EnumSettingType;
import me.ramazanenescik04.diken.gui.compoment.*;
import me.ramazanenescik04.diken.gui.screen.StaticBackground;
import me.ramazanenescik04.diken.gui.window.ColorPickWindow.ColorPickFuture;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

/**
 * Represents the `SettingsWindow` type within the DikenEngine `gui.window` package.
 */
public class SettingsWindow extends Window {
	private static final long serialVersionUID = 1L;
	private List<GuiComponent> settingsItems = new ArrayList<>();
	private ScrollBar bar;
	private int itemHeight = 20;
	private int footerHeight = 24;
	
	private Panel scrollArea;

	public SettingsWindow() {
		super(2, 2, 200, 200);
		this.setTitle("Settings");
		ArrayBitmap icons = (ArrayBitmap) ResourceLocator.getResource("win-icons");
		this.setIcon(icons.getBitmap(10, 0));
	}
	
	// Bu metodu bir yere koy
	@SuppressWarnings("unchecked")
	private <T> void forceUpdate(Setting<T> s, Object val) {
		if (s.getType().isAllowedClasses(val.getClass())) {
			 s.setValue((T) val);
		}
	}
	
	public void addSettingComponent(GuiComponent gc) {
		settingsItems.add(gc);
        scrollArea.add(gc);
	}
	
	public void open() {
		DikenEngine engine = DikenEngine.getEngine();
		
		Map<String, Setting<?>> settings = new HashMap<>();

		for (Map.Entry<String, Setting<?>> entry : engine.config.getConfig().entrySet()) {
		    if (entry.getValue() == null) continue;
		    
			settings.put(entry.getKey(), new Setting<>(entry.getValue())); 
		}
		
		this.setLocation(engine.getScaledWidth() / 2 - this.width / 2, engine.getScaledHeight() / 2 - this.height / 2);
		this.getContentPane().setBackground(new StaticBackground(Bitmap.createClearedBitmap(16, 16, 0xffa0a0a0)));
		Panel mainPanel = this.getContentPane();
	    
	    // 1. Ayarlar için bir alt panel (Scroll Area)
	    // Alttaki butonlar için 40-50 px yer bırakıyoruz
	    
	    scrollArea = new Panel(0, 0, mainPanel.getWidth(), mainPanel.getHeight() - footerHeight) {
			@Override
			public Bitmap render() {
				Bitmap bitmap = super.render();
				bitmap.drawLine(0, bitmap.h - 1, bitmap.w - 1, bitmap.h - 1, 0xffffffff, 1);
				return bitmap;
			}
	    	
	    };
	    mainPanel.add(scrollArea);
	    
	    int i = 0;
        for (Setting<?> setting : settings.values()) {
        	if (setting == null) continue;
        	
        	GuiComponent component = null;
        	switch (setting.getType()) {
        		case EnumSettingType.CHECK_BOX:
        			component = new CheckBox(setting.getName() , 2, i * itemHeight).setConsumer((cBox) -> {
        				forceUpdate(setting, cBox.isChecked());
        			}).setChecked((boolean) setting.getValue());
        			break;
        		case EnumSettingType.TEXT_FIELD:
        			component = new Text(setting.getName(), 2, i * itemHeight, 0xffffffff).setOffsetLocation(0, 6);
        			addSettingComponent(component);
        			i++;
        			TextField field = new TextField(2, i * itemHeight, 176, itemHeight).setTextChanced((s) -> {
        				forceUpdate(setting, s);
        			});
        			field.setText(setting.getValue().toString());
        			component = field;
        			break;
        		case EnumSettingType.COLOR_PICKER:
        			Button button = new Button(setting.getName() + ": ", 2, i * itemHeight, 176, itemHeight).setButtonIcon(
        					Bitmap.createClearedBitmap(12, 12, (int) setting.getValue())
        			).setButtonIconLeft(false).setRunnable((e) -> {
        				ColorPickWindow window = new ColorPickWindow(0, 0).setSelectedColor(0xff0000ff).setColorPickFuture(new ColorPickFuture() {
							@Override
							public void cancelled() {
							}

							@Override
							public void succesed(int color) {
								forceUpdate(setting, color);
								e.setButtonIcon(Bitmap.createClearedBitmap(12, 12, color));
							}

							@Override
							public void closed() {
								SettingsWindow.this.getContentPane().active = true;
								SettingsWindow.this.setCloseable(true);
								
								engine.wManager.activeWindow = SettingsWindow.this;
							}
        				});
        				engine.wManager.addWindow(window);
        				SettingsWindow.this.getContentPane().active = false;
        				this.setCloseable(false);
        			});
        			component = button;
        			break;
        		case EnumSettingType.SLIDER:
        			component = new Text(setting.getName(), 2, i * itemHeight, 0xffffffff).setOffsetLocation(0, 6);
        			addSettingComponent(component);
        			i++;
        			ScrollBar scroll = new ScrollBar(2, i * itemHeight, 176, itemHeight, ScrollBar.HORIZONTAL).addDraggedListener((val) -> {
        				Class<?> type = setting.getTypeClass();
        				if (type.isAssignableFrom(Float.class)) {
        					forceUpdate(setting, map(val, 0.0, 1.0, (Float)setting.getMin(), (Float)setting.getMax()).floatValue());
        				} else if (type.isAssignableFrom(Double.class)) {
        					forceUpdate(setting, map(val, 0.0, 1.0, (Double)setting.getMin(), (Double)setting.getMax()).doubleValue());
        				} else if (type.isAssignableFrom(Integer.class)) {
        					forceUpdate(setting, map(val, 0.0, 1.0, (Integer)setting.getMin(), (Integer)setting.getMax()).intValue());
        				}
        			});
        			Number number = (Number) setting.getValue();
        			Number min = (Number) setting.getMin();
        			Number max = (Number) setting.getMax();
        			scroll.setScrollValue((number.floatValue() - min.floatValue()) / (max.floatValue() - min.floatValue()));
        			component = scroll;
        			break;
        		default:
        			component = new Button(setting.getName(), 2, i * itemHeight, 176, itemHeight);
        			break;
        	}
        	addSettingComponent(component);
            i++;
        }
        
        Button resetButton = new Button("Ayarları Sıfırla", 2, i * itemHeight, 176, itemHeight).setRunnable(() -> {
        	OptionWindow.showMessageNoWait("Ayarlar Sıfırlamak İstediğinden \nEmin Misin?", "Soru", OptionWindow.PLAIN_MESSAGE, OptionWindow.YES_NO_OPTION, (e) -> {
        		if (e == OptionWindow.YES_BUTTON) {
        			this.close();
        			
        			engine.config.getConfig().clear();
        			engine.config.getConfig().putAll(Config.defaultConfig);
        		}
        	});
        });
        addSettingComponent(resetButton); 

	    // 2. Sabit Butonlar Paneli (Footer)
	    Panel footer = new Panel(0, mainPanel.getHeight() - footerHeight, mainPanel.getWidth(), footerHeight);
	    mainPanel.add(footer);

	    // 3. Butonları footer'a ekle (Bunlar asla kaymaz)
	    Button saveBtn = new Button("Save", 10, 2, 80, 20).setRunnable(() -> {
	    	engine.config.getConfig().putAll(settings);
	    	this.close();
	    });
	    Button cancelBtn = new Button("Cancel", 100, 2, 80, 20).setRunnable(() -> {
	    	this.close();
	    });
	    footer.add(saveBtn);
	    footer.add(cancelBtn);

	    // 4. ScrollBar'ı ve ayar elemanlarını scrollArea'ya ekle
	    bar = new ScrollBar(scrollArea.getWidth() - 20, 0, 20, scrollArea.getHeight());
	    scrollArea.add(bar);
	    
	    // 3. Handle Boyutunu Güncelle
        int totalContentHeight = settingsItems.size() * itemHeight;
        bar.updateHandleSize(scrollArea.getHeight(), totalContentHeight);

        // 4. "Pürüzlü" Kaydırma Listener'ı
        bar.addDraggedListener(scrollValue -> {
            updateItemsPosition(scrollValue);
        });     
    }
	
	private void updateItemsPosition(float scrollValue) {
	    int totalItems = settingsItems.size();
	    int maxVisible = bar.getHeight() / itemHeight; // Ekrana kaç tane sığıyor?
	    
	    // Kaydırılabilir maksimum index (Örn: 50 eleman var, 10'u görünüyor, max 40 kayabilir)
	    int maxScrollIndex = Math.max(0, totalItems - maxVisible);
	    
	    // Pürüzlü (Discrete) index hesaplama
	    // scrollValue 0.5 ise ve maxScrollIndex 40 ise, 20. elemana atlar.
	    int currentIndex = Math.round(scrollValue * maxScrollIndex);

	    // Elemanları yeniden diz
	    for (int i = 0; i < settingsItems.size(); i++) {
	        GuiComponent comp = settingsItems.get(i);
	        
	        // Elemanın yeni Y pozisyonu: (Kendi sırası - Kaydırma miktarı) * yükseklik
	        comp.y = (i - currentIndex) * itemHeight;
	        
	        // Panel dışındakileri görünmez yapabilirsin (Performans için)
	        boolean shouldBeVisible = (comp.y >= 0 && comp.y < scrollArea.getHeight());
	        if (comp.isVisible() != shouldBeVisible) {
	            comp.setVisible(shouldBeVisible);
	        }
	    }
	}

	@Override
	public void tick(DikenEngine engine) {
		super.tick(engine);
		
		int wheel = engine.input.getWheelValue(); // Fare tekerleği hareketi
		if (wheel != 0 && this.active) {
		    // Tekerlek döndükçe scrollValue'yu 0.0 ile 1.0 arasında değiştir
		    float step = 1.0f / (settingsItems.size() - (bar.getHeight() / itemHeight));
		    bar.setScrollValue(bar.getScrollValue() - (wheel * step)); 
		    updateItemsPosition(bar.getScrollValue());
		}
	}
	
	private static Double map(
	        double value,
	        double inMin, double inMax,
	        double outMin, double outMax) {

	    return outMin + (value - inMin) * (outMax - outMin) / (inMax - inMin);
	}

}
