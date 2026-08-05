package me.ramazanenescik04.diken.gui.component;

import java.awt.Point;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.NodeResource;
import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.game.setting.EnumSettingType;
import me.ramazanenescik04.diken.game.setting.Setting;
import me.ramazanenescik04.diken.game.setting.SettingCategory;
import me.ramazanenescik04.diken.gui.TextRenderer;
import me.ramazanenescik04.diken.gui.UDim2;
import me.ramazanenescik04.diken.gui.UniFont;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.EnumResource;
import me.ramazanenescik04.diken.resource.ResourceLocator;

/**
 * Represents the `Text` type within the DikenEngine `gui.compoment` package.
 */
public class Text extends GuiComponent {
	private String text;
	private int color;
	private NodeResource<UniFont> font;
	private TextPosition textPosition = TextPosition.Center;
	
	public enum TextPosition {
		NorthWest(0, 0),
		NorthEast(1, 0),
		North(0.5, 0),
		
		SouthEast(0, 1),
		SouthWest(1, 1),
		South(0.5, 1),
		
		East(1, 0.5),
		West(0, 0.5),
		Center(0.5, 0.5);
		
		public final double x, y;
		
		TextPosition(double x, double y) {
			this.x = x;
			this.y = y;
		}
	}

	public Text(String text, UDim2 position) {
		this(text, position, UDim2.defaultV, 0xffffffff, "default_font");
	}
	
	public Text(String text, UDim2 position, UDim2 size, int color, String font) {
		super("Text", position, size);
		
		this.text = text;
		this.color = color;
		this.font = new NodeResource<>(font, EnumResource.FONT);
	}

	public Text(DataInputStream in) throws IOException {
		super(in);
		loadNodeData(in);
	}
	
	public Bitmap render() {
	    Bitmap bitmap = super.render();
	    
	    if (this.font.getResource() == null) {
	    	return bitmap;
	    }
	    
	    int containerWidth = this.getWidth();
	    int containerHeight = this.getHeight();
	    
	    int textWidth = TextRenderer.stringBitmapWidth(text, this.font.getResource());
	    int textHeight = TextRenderer.stringBitmapAverageHeight(text, this.font.getResource());
	    
	    // Ham koordinatları hesapla
	    int x = (int) (textPosition.x * containerWidth);
	    int y = (int) (textPosition.y * containerHeight);
	    
	    // Hizalamayı düzelt (Yazının kutunun dışına taşmasını engelle)
	    if (textPosition.x == 0.5) {
	        x -= (textWidth / 2);
	    } else if (textPosition.x == 1.0) {
	        x -= textWidth;
	    }
	    
	    if (textPosition.y == 0.5) {
	        y -= (textHeight / 2);
	    } else if (textPosition.y == 1.0) {
	        y -= textHeight;
	    }
	    
	    // Doğru koordinatlarla çizdir
	    bitmap.drawText(text, x, y, color, this.font.getResource(), false);
	    
	    return bitmap;
	}
	
	@Override
	public void update(World world, DikenEngine engine) {
		super.update(world, engine);
		
		font.update(world);
	}
	
	public Point calculateTextCoordinates(TextPosition position, int containerWidth, int containerHeight, int textWidth, int textHeight) {
        int rawX = (int) (position.x * containerWidth);
        int rawY = (int) (position.y * containerHeight);
        
        int finalX = rawX;
        int finalY = rawY;
        
        if (position.x == 0.5) {
            finalX = rawX - (textWidth / 2);
        } else if (position.x == 1.0) {
            finalX = rawX - textWidth;
        }
        
        if (position.y == 0.5) {
            finalY = rawY - (textHeight / 2);
        } else if (position.y == 1.0) {
            finalY = rawY - textHeight;
        }
        
        return new Point(finalX, finalY);
    }

	public String getText() {
		return this.text;
	}
	
	public Text setText(String text) {
		this.text = text;
		return this;
	}
	
	public int getColor() {
		return this.color;
	}
	
	public Text setColor(int color) {
		this.color = color;
		return null;
	}
	
	public TextPosition getTextPosition() {
		return textPosition;
	}

	public void setTextPosition(TextPosition textPostion) {
		this.textPosition = textPostion;
	}
	
	public String getFont() {
		return font.getKey();
	}

	public void setFont(String key) {
		font.setKey(key);
	}

	@Override
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("text", "Text", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(6, 2));
		
		var settingCategory = SettingCategory
				.createSettingCategory(key)
				.addSetting(new Setting<>("Text", this.text, String.class, EnumSettingType.TEXT_FIELD).addChangeListener(this::setText))
				.addSetting(new Setting<>("Text Position", this.textPosition, TextPosition.values(), TextPosition.class,
						EnumSettingType.LIST_SELECT).addChangeListener(this::setTextPosition))
				.addSetting(new Setting<>("Color", this.color, Integer.class, EnumSettingType.COLOR_PICKER).addChangeListener(this::setColor))
				.addSetting(new Setting<>("Font", this.font.getKey(), String.class, EnumSettingType.RESOURCE_SELECT).addChangeListener(this::setFont));
		
		var list = super.getNodeSettings();
		list.add(settingCategory);
		return list;
	}
	
	@Override
	public void saveNodeData(DataOutputStream out) throws IOException {
		super.saveNodeData(out);
		out.writeUTF(text);
		out.writeInt(color);
		out.writeUTF(font.getKey());
		out.writeUTF(textPosition.name());
	}

	@Override
	public void loadNodeData(DataInputStream in) throws IOException {
		super.loadNodeData(in);
		this.text = in.readUTF();
		this.color = in.readInt();
		this.font = new NodeResource<>(in.readUTF(), EnumResource.FONT);
		this.textPosition = TextPosition.valueOf(in.readUTF());
	}
}
