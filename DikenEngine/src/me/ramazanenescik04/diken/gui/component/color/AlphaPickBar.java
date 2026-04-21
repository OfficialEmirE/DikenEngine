package me.ramazanenescik04.diken.gui.component.color;

import java.util.function.Consumer;

import me.ramazanenescik04.diken.gui.component.GuiComponent;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.tools.PixelToColor;

/**
 * Saydamlık (Alpha) seçimi için özel bar bileşeni.
 */
public class AlphaPickBar extends GuiComponent {
    private static final long serialVersionUID = 1L;
    
    private int selectedAlpha = 255;
    private int baseColor = 0xffffff; // Alpha'nın uygulanacağı ana renk (genelde beyaz veya seçili renk)
    private Consumer<Integer> consumer;

	private int cursorX;

    public AlphaPickBar(int x, int y, int width, int height) {
        super(x, y, width, height);
    }
    
    public AlphaPickBar setConsumer(Consumer<Integer> consumer) {
        this.consumer = consumer;
        return this;
    }
    
    public AlphaPickBar setSelectedAlpha(int alpha) {
        this.selectedAlpha = Math.max(0, Math.min(255, alpha));
        this.cursorX = (selectedAlpha * (width - 3)) / 255;
        return this;
    }

    public AlphaPickBar setBaseColor(int color) {
        this.baseColor = color & 0x00FFFFFF; // Sadece RGB kısmını al
        return this;
    }

    @Override
    public Bitmap render() {
        Bitmap bitmap = super.render();
        int w = bitmap.w - 2;
        int h = bitmap.h - 2;
        
        // 1. Dama Tahtası Deseni Çiz (Saydamlığı belli etmek için)
        for (int yy = 0; yy < h; yy++) {
            for (int xx = 0; xx < w; xx++) {
                int checker = ((xx / 4) + (yy / 4)) % 2 == 0 ? 0xffffffff : 0xffcccccc;
                
                // 2. Alpha Gradyanı Hesapla (Soldan sağa 0 -> 255)
                int alpha = (xx * 255) / w;
                int blendedColor = PixelToColor.blend(checker, baseColor, alpha); // Motorunda blend fonksiyonu yoksa aşağıya ekliyorum
                
                bitmap.setPixel(xx + 1, yy + 1, blendedColor);
            }
        }
        
        // Kenarlık ve Seçili Konum İşaretçisi
        bitmap.box(0, 0, bitmap.w - 1, bitmap.h - 1, 0xff000000);
        bitmap.box(cursorX, 0, cursorX + 2, bitmap.h - 1, 0xffcfcfcf); // Beyaz bir imleç
        
        return bitmap;
    }

    @Override
    public void mouseClicked(int x, int y, int button, boolean isTouch) {
        if (isTouch && this.active && button == 0) {
            // Tıklanan yere göre 0-255 arası alpha hesapla
        	cursorX = Math.max(0, Math.min(x - 1, this.width - 3));
            this.selectedAlpha = ((x - 1) * 255) / (this.width - 2);
            this.selectedAlpha = Math.max(0, Math.min(255, this.selectedAlpha));
            
            if (this.consumer != null) this.consumer.accept(this.selectedAlpha);
        }
    }
}