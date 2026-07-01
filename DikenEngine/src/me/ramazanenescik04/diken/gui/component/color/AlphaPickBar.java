package me.ramazanenescik04.diken.gui.component.color;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import me.ramazanenescik04.diken.game.EnumSettingType;
import me.ramazanenescik04.diken.game.setting.Setting;
import me.ramazanenescik04.diken.game.setting.SettingCategory;
import me.ramazanenescik04.diken.gui.UDim2;
import me.ramazanenescik04.diken.gui.component.GuiComponent;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;
import me.ramazanenescik04.diken.tools.PixelToColor;

/**
 * Saydamlık (Alpha) seçimi için özel bar bileşeni.
 */
public class AlphaPickBar extends GuiComponent {
    private int selectedAlpha = 255;
    private int baseColor = 0xffffff; // Alpha'nın uygulanacağı ana renk (genelde beyaz veya seçili renk)
    private Consumer<Integer> consumer;

	private int cursorX;

    public AlphaPickBar(UDim2 position, UDim2 size) {
        super("AlphaPickBar", position, size);
    }

    public AlphaPickBar(DataInputStream in) throws IOException {
    	super(in);
    	loadNodeData(in);
    }
    
    public AlphaPickBar setConsumer(Consumer<Integer> consumer) {
        this.consumer = consumer;
        return this;
    }
    
    public AlphaPickBar setSelectedAlpha(int alpha) {
        this.selectedAlpha = Math.max(0, Math.min(255, alpha));
        this.cursorX = (selectedAlpha * (this.getAbsoluteBounds().getWidth() - 3)) / 255;
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
        	cursorX = Math.max(0, Math.min(x - 1, this.getAbsoluteBounds().getWidth() - 3));
            this.selectedAlpha = ((x - 1) * 255) / (this.getAbsoluteBounds().getHeight() - 2);
            this.selectedAlpha = Math.max(0, Math.min(255, this.selectedAlpha));
            
            if (this.consumer != null) this.consumer.accept(this.selectedAlpha);
        }
    }
    
    @Override
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("alphaPickBar", "AlphaPickBar", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(15, 3));
		var settingCategory = 
				SettingCategory.createSettingCategory(key)
				.addSetting(new Setting<Integer>("Selected Alpha", this.selectedAlpha, 0, 255, Integer.class, EnumSettingType.SLIDER).addChangeListener(this::setSelectedAlpha))
				.addSetting(new Setting<Integer>("Base Color", this.baseColor, Integer.class, EnumSettingType.COLOR_PICKER).addChangeListener(this::setBaseColor));
		
		var list = super.getNodeSettings();
		list.add(settingCategory);
		return list;
	}

	@Override
	public void saveNodeData(DataOutputStream out) throws IOException {
		super.saveNodeData(out);
		
		out.writeInt(selectedAlpha);
		out.writeInt(baseColor);
		out.writeInt(cursorX);
	}

	@Override
	public void loadNodeData(DataInputStream in) throws IOException {
		super.loadNodeData(in);
		
		this.selectedAlpha = in.readInt();
		this.baseColor = in.readInt();
		this.cursorX = in.readInt();
	}
}
