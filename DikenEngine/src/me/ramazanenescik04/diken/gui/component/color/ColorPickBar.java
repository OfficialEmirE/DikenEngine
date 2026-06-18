package me.ramazanenescik04.diken.gui.component.color;

import java.util.List;
import java.util.function.Consumer;

import me.ramazanenescik04.diken.game.EnumSettingType;
import me.ramazanenescik04.diken.game.Setting;
import me.ramazanenescik04.diken.game.SettingCategory;
import me.ramazanenescik04.diken.gui.UDim2;
import me.ramazanenescik04.diken.gui.component.GuiComponent;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;
import me.ramazanenescik04.diken.tools.PixelToColor;

/**
 * Optimized and modernized ColorPickBar for DikenEngine.
 */
public class ColorPickBar extends GuiComponent {
    private static final long serialVersionUID = 2L;
    
    private int selectedColor;
    private int selectorX = 1; // Seçicinin yatay pozisyonu
    private Consumer<Integer> consumer;
    private Bitmap cachedHueMap; // Performans için renk haritasını önbelleğe alıyoruz

    public ColorPickBar(UDim2 position, UDim2 size) {
        super("ColorPickBar", position, size);
    }
    
    public ColorPickBar setConsumer(Consumer<Integer> consumer) {
        this.consumer = consumer;
        return this;
    }
    
    public ColorPickBar setSelectedColor(int color) {
        this.selectedColor = color;
        this.selectorX = (int)(PixelToColor.rgbToHsv(color)[0] * 255) * (this.getAbsoluteBounds().getWidth() - 3) / 255;
        return this;
    }
    
    public int getSelectedColor() {
        return selectedColor;
    }

    /**
     * Renk haritasını sadece boyut değiştiğinde veya ilk açılışta oluşturur.
     */
    private void updateCache() {
    	var bounds = this.getAbsoluteBounds();
        if (cachedHueMap == null || cachedHueMap.w != bounds.getWidth() - 2 || cachedHueMap.h != bounds.getHeight() - 2) {
            // Sadece gerekli alanı kaplayacak şekilde oluştur
            cachedHueMap = PixelToColor.createHColorRect(bounds.getWidth() - 2, bounds.getHeight() - 2);
        }
    }

    @Override
    public Bitmap render() {
        // Üst sınıftan temiz bir bitmap al (Genelde arka plan rengiyle temizlenmiş gelir)
        Bitmap bitmap = super.render();
        
        updateCache();
        
        // Önbellekteki renk paletini çiz
        if (cachedHueMap != null) {
            bitmap.draw(cachedHueMap, 1, 1);
        }
        
        // Kenarlık çizimi (Siyah çerçeve)
        bitmap.box(0, 0, bitmap.w - 1, bitmap.h - 1, 0xff000000);
        
        // Seçili rengin nerede olduğunu belirten küçük bir beyaz imleç (Opsiyonel ama şık durur)
        drawSelector(bitmap);
        
        return bitmap;
    }

    private void drawSelector(Bitmap bitmap) {
    	// Kenarlık ve Seçili Konum İşaretçisi
        bitmap.box(0, 0, bitmap.w - 1, bitmap.h - 1, 0xff000000);
        bitmap.box(selectorX, 0, selectorX + 2, bitmap.h - 1, 0xffcfcfcf); // Beyaz bir imleç
    }

    @Override
    public void mouseClicked(int x, int y, int button, boolean isTouch) {
        var bounds = this.getAbsoluteBounds();
        if (this.active && button == 0) {
        	if (x <= 0 || y <= 0 || x >= bounds.getWidth() - 1 || y >= bounds.getHeight() - 1 || cachedHueMap == null) return;
            // Pikselleri render edilmiş bitmap'ten çekmek yerine direkt cache'den çekmek daha güvenlidir
            if (cachedHueMap != null) {
                // Koordinatları 1 piksel içeri kaydırıyoruz (kenarlıktan dolayı)
                int checkX = Math.max(0, Math.min(x - 1, cachedHueMap.w - 1));
                int checkY = Math.max(0, Math.min(y - 1, cachedHueMap.h - 1));
                
                this.selectedColor = cachedHueMap.getPixel(checkX, checkY);
                this.selectorX = checkX;
                
                if (this.consumer != null) {
                    this.consumer.accept(this.selectedColor);
                }
            }
        }
    }
    
    @Override
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("colorPickBar", "ColorPickBar", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(14, 3));
		var settingCategory = 
				SettingCategory.createSettingCategory(key)
						.addSetting(new Setting<Integer>("Selector X", this.selectorX, 0, 255, Integer.class,
								EnumSettingType.SLIDER).addChangeListener(
										e -> this.selectorX = e * (this.getAbsoluteBounds().getWidth() - 3) / 255))
				.addSetting(new Setting<Integer>("Selected Color", this.selectedColor, Integer.class, EnumSettingType.COLOR_PICKER).addChangeListener(this::setSelectedColor));
		
		var list = super.getNodeSettings();
		list.add(settingCategory);
		return list;
	}
}