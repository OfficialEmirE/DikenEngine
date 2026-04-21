package me.ramazanenescik04.diken.gui.window;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.gui.component.Button;
import me.ramazanenescik04.diken.gui.component.Panel;
import me.ramazanenescik04.diken.gui.component.RenderImage;
import me.ramazanenescik04.diken.gui.component.color.AlphaPickBar;
import me.ramazanenescik04.diken.gui.component.color.ColorPickBar;
import me.ramazanenescik04.diken.gui.component.color.ColorPickBox;
import me.ramazanenescik04.diken.gui.screen.StaticBackground;
import me.ramazanenescik04.diken.resource.Bitmap;

/**
 * Modernized and refactored ColorPickWindow for DikenEngine.
 * Features separate ARGB channel management and a clean UI builder pattern.
 */
public class ColorPickWindow extends Window {
    private static final long serialVersionUID = 1L;
    
    // --- State Variables ---
    private int currentColor;
    private int currentAlpha = 255;
    
    // --- UI Components ---
    private ColorPickBox colorBox;
    private ColorPickBar hueSlider;
    private AlphaPickBar alphaSlider;
    private RenderImage colorPreview;
    
    // --- Callback ---
    private ColorPickFuture future;
    
    public ColorPickWindow(int x, int y) {
        // Genişletilmiş boyutlar: Alpha bar ve ferah butonlar için 105x170
        super(x, y, 200, 200); 
        this.setTitle("Select Color");
    }

    @Override
    protected void open() {
        DikenEngine engine = DikenEngine.getEngine();
        this.setLocation(
            (engine.getScaledWidth() - this.width) / 2, 
            (engine.getScaledHeight() - this.height) / 2
        );
        
        Panel panel = this.getContentPane();
        panel.setBackground(new StaticBackground(Bitmap.createClearedBitmap(16, 16, 0xffa0a0a0)));

        // Arayüz inşası (UI Builder Pattern)
        setupPickers(panel);
        setupPreview(panel);
        setupButtons(panel);
        
        // İlk açılışta görseli senkronize et
        updateColorVisuals();
    }

    // --- 1. Renk Seçicilerin Kurulumu ---
    private void setupPickers(Panel panel) {
        // Ana Renk Kutusu (Saturation & Value)
        colorBox = new ColorPickBox(2, 2, 95, 95);
        colorBox.setSelectedColor(this.currentColor)
                .setHueColor(this.currentColor)
                .setConsumer(color -> handleColorChange(color, false));
        
        // Hue (Ton) Çubuğu
        hueSlider = new ColorPickBar(2, 99, 78, 18);
        hueSlider.setSelectedColor(this.currentColor)
                 .setConsumer(color -> handleColorChange(color, true));

        // Alpha (Saydamlık) Çubuğu
        alphaSlider = new AlphaPickBar(2, 119, 78, 18);
        alphaSlider.setSelectedAlpha(this.currentAlpha)
                   .setConsumer(alpha -> {
                       this.currentAlpha = alpha;
                       handleColorChange(this.currentColor, true);
                   });

        panel.add(colorBox);
        panel.add(hueSlider);
        panel.add(alphaSlider);
    }

    // --- 2. Önizleme Kutusunun Kurulumu ---
    private void setupPreview(Panel panel) {
        // Hue ve Alpha barlarının yanına dikey, uzun bir önizleme kutusu
        colorPreview = new RenderImage(Bitmap.createClearedBitmap(17, 38, currentColor), 82, 99) {
            @Override
            public Bitmap render() {
                // Siyah dış çerçeve çizimi
                Bitmap borderBitmap = new Bitmap(this.bitmap.w + 2, this.bitmap.h + 2);
                borderBitmap.draw(this.bitmap, 1, 1);
                borderBitmap.box(0, 0, borderBitmap.w - 1, borderBitmap.h - 1, 0xff000000);
                return borderBitmap;
            }    
        };
        panel.add(colorPreview);
    }

    // --- 3. Butonların Kurulumu ---
    private void setupButtons(Panel panel) {
        Button btnCancel = new Button("Cancel", 4, panel.getHeight() - 20, 46, 18);
        btnCancel.setRunnable(() -> {
            if (this.future != null) this.future.cancelled();
            this.close();
        });
        
        Button btnOk = new Button("OK", 54, panel.getHeight() - 20, 46, 18);
        btnOk.setRunnable(() -> {
            if (this.future != null) this.future.succesed(this.currentColor);
            this.close();
        });
        
        panel.add(btnCancel);
        panel.add(btnOk);
    }

    // --- Merkezi Mantık Yönetimi ---
    
    /**
     * RGB değiştiğinde Alpha bozulmadan yeni rengi hesaplar.
     */
    private void handleColorChange(int newRgbColor, boolean isFromHue) {
        // Eğer değişiklik Hue barından geldiyse, ana kutuyu da güncelle
        if (isFromHue) {
            colorBox.setSelectedColor(newRgbColor).setHueColor(newRgbColor);
            newRgbColor = colorBox.getSelectedPosColor();
        }
        
        // Yeni rengin sadece RGB kısmını al, mevcut Alpha değeriyle birleştir
        this.currentColor = (this.currentAlpha << 24) | (newRgbColor & 0x00FFFFFF);
        updateColorVisuals();
    }

    /**
     * Sadece önizleme bitmap'ini güncelleyen tek sorumlu metod.
     */
    private void updateColorVisuals() {
        if (this.colorPreview != null) {
            this.colorPreview.setBitmap(Bitmap.createClearedBitmap(17, 38, this.currentColor));
        }
        
        alphaSlider.setBaseColor(this.currentColor);
    }

    // --- Getter / Setter / Lifecycle ---

    public ColorPickWindow setSelectedColor(int color) {
        this.currentColor = color;
        this.currentAlpha = (color >> 24) & 0xFF; // Rengin içinden alpha'yı ayıkla
        return this;
    }
    
    public ColorPickWindow setColorPickFuture(ColorPickFuture cpf) {
        this.future = cpf;
        return this;
    }
    
    public int getSelectedColor() {
        return this.currentColor;
    }
    
    @Override
    public void close() {
        super.close();
        if (this.future != null) {
            this.future.closed();
        }
    }

    public static interface ColorPickFuture {
        void cancelled();
        void succesed(int color);
        void closed();
    }
}