package me.ramazanenescik04.diken.gui.component;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.game.EnumSettingType;
import me.ramazanenescik04.diken.game.Setting;
import me.ramazanenescik04.diken.game.SettingCategory;
import me.ramazanenescik04.diken.game.World;
import me.ramazanenescik04.diken.gui.UDim2;
import me.ramazanenescik04.diken.renderer.FrameBitmapPool;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.EnumResource;
import me.ramazanenescik04.diken.resource.ResourceLocator;

/**
 * Represents the `Button` type within the DikenEngine `gui.compoment` package.
 */
public class Button extends GuiComponent {
	private String text = "";
	private int tColor = 0xff000000, bColor = 0xffffffff;
	
	private int textOffset = 0; // Yazı kaydırma için offset
    private boolean movingRight = true; // Yazının hareket yönü
    private final double SCROLL_SPEED = 0.49888d; // Kaydırma hızı
    private double textOffsetLong = 0;
    
    private Consumer<Button> runnable;
    private transient Bitmap icon;
    private transient boolean iconLoaded = false;
    private String iconID = "empty";
    
    protected boolean isTouching = false;
    private boolean isIconLeft = true;
	
	public Button(String text, UDim2 position, UDim2 size) {
		super("Button", position, size);
		this.text = text;	
	}

	public Button(DataInputStream in) throws IOException {
		super(in);
		if (getClass() == Button.class) {
			loadNodeData(in);
		}
	}
	
	public Button setTextColor(int color) {
		this.tColor = color;
		return this;
	}
	
	public Button setButtonColor(int color) {
		this.bColor = color;
		return this;
	}
	
	public Button setButtonIcon(String icon) {
		this.iconID = (icon == null || icon.isBlank() ? "empty" : icon);
		this.iconLoaded = false;
		return this;
	}
	
	public String getButtonIcon() {
		return this.iconID;
	}
	
	public Bitmap GetButtonIconBitmap() {
		return this.icon;
	}
	
	public Button setButtonIconLeft(boolean iconLeft) {
		this.isIconLeft = iconLeft;
		return this;
	}
	
	public boolean isButtonIconLeft() {
		return this.isIconLeft;
	}
	
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	protected Bitmap createButtonTexture(int width, int height) {		
		Bitmap buttonBitmap = FrameBitmapPool.newBitmap(width, height);
		buttonBitmap.fill(0, 0, width, height, 0xffd3d3d3);
		
		ArrayBitmap button = (ArrayBitmap) ResourceLocator.getResource("button-array");
		
		for (int i = 0; i < width - 4; i++) {
			buttonBitmap.draw(button.bitmap[0][1], i, height - 16);
			buttonBitmap.draw(button.bitmap[2][1], i, 0);
		}
		
		for (int i = 0; i < height - 4; i++) {
			buttonBitmap.draw(button.bitmap[1][0], 0, 0 + i);
			buttonBitmap.draw(button.bitmap[1][1], 0 + width - 16, 0 + i);
		}
		
		buttonBitmap.draw(button.bitmap[0][0], 0, 0 + (height - 16));
		buttonBitmap.draw(button.bitmap[0][2], width - 4, 0 + (height - 16));
		buttonBitmap.draw(button.bitmap[2][0], 0, 0);
		buttonBitmap.draw(button.bitmap[1][2], width - 4, 0);
		return buttonBitmap;
	}
	
	public Bitmap render() {
		var bounds = this.getLocalAbsoluteBounds();
		var width = bounds.getWidth();
		var height = bounds.getHeight();
		
		Bitmap bitmap = FrameBitmapPool.newBitmap(width, height);
		bitmap.blendDraw(createButtonTexture(width, height), 0, 0, bColor);
		
		// Yazı genişliğini kontrol et
        int textWidth = Text.stringBitmapWidth(text, DikenEngine.getEngine().defaultFont) + (this.icon != null ? this.icon.w + 6 : 0);
        
        if (textWidth > width) {     
        	int spacing = 6;
        	int startPosition = -textOffset + 10;

        	if (this.icon != null) {
        	    if (isIconLeft) {
        	        // İKON SOLDA: Önce ikonu çiz, sonra startPosition'ı kaydır
        	        bitmap.draw(this.icon, startPosition, (height / 2) - (this.icon.h / 2));
        	        Text.render(text, bitmap, startPosition + this.icon.w + spacing, (height / 2) - 4, tColor);
        	    } else {
        	        // İKON SAĞDA: Önce metni çiz, yanına ikonu ekle
        	        Text.render(text, bitmap, startPosition, (height / 2) - 4, tColor);
        	        bitmap.draw(this.icon, startPosition + textWidth + spacing, (height / 2) - (this.icon.h / 2));
        	    }
        	} else {
        	    // İkon yoksa sadece metni çiz
        	    Text.render(text, bitmap, startPosition, (height / 2) - 4, tColor);
        	}
        } else {
        	int spacing = 6;

        	// Grubun başlangıç noktası (Tam merkezi hesaplama)
        	int startX = (width - textWidth) / 2;

        	if (this.icon != null) {
        	    if (isIconLeft) {
        	        // [İKON] [METİN]
        	        bitmap.draw(this.icon, startX, (height / 2) - (this.icon.h / 2));
        	        Text.render(text, bitmap, startX + this.icon.w + spacing, (height / 2) - 4, tColor);
        	    } else {
        	        // [METİN] [İKON]
        	        Text.render(text, bitmap, startX, (height / 2) - 4, tColor);
        	        bitmap.draw(this.icon, startX + (textWidth - this.icon.w - spacing), (height / 2) - (this.icon.h / 2));
        	    }
        	} else {
        	    // Sadece metni ortala
        	    Text.render(text, bitmap, startX, (height / 2) - 4, tColor);
        	}
        }
        
        if (isTouching) {
        	bitmap.box(0, 0, width - 1, height - 1, 0xffFFDF00);
        }
        
        if (!isActive()) {
			bitmap.blendFill(0, 0, width - 1, height - 1, 0x7f000000);
		}
        
        return bitmap;
	}
	
	@Override
	public void update(World world, DikenEngine engine) {
		super.update(world, engine);
		
		if (!iconLoaded) {
			this.icon = world.getResource(iconID, EnumResource.IMAGE);
			this.iconLoaded = true;
		}
	}

	public void tick(DikenEngine engine) {
		var bounds = this.getAbsoluteBounds();
		var width = bounds.getWidth();
		
		int textWidth = Text.stringBitmapWidth(text, engine.defaultFont) + (this.icon != null ? this.icon.w + 6 : 0);;
		if (textWidth > width) {
			// Yazı genişlikten büyükse kaydırma işlemi yap
            if (movingRight) {
            	textOffsetLong += SCROLL_SPEED;
                if (textOffset > textWidth - width + 20) { // Biraz boşluk bırak
                    movingRight = false;
                }
            } else {
            	textOffsetLong -= SCROLL_SPEED;
                if (textOffset < 0) {
                    movingRight = true;
                }
            }
            textOffset = (int) textOffsetLong;
		}
	}

	public void mouseClicked(int x, int y, int button, boolean isTouch) {
		if (isTouch || isTouching) {
			if (button == 0 && runnable != null && this.isActive()) {
				runnable.accept(this);
			}
		}
	}
	
	public void mouseGetInfo(int x, int y, boolean isTouch) {
		if (isActive()) this.isTouching = isTouch;
		else this.isTouching = false;
	}

	public Button setRunnable(Runnable runnable) {
		this.runnable = (_) -> {runnable.run();};
		return this;
	}
	
	public Button setRunnable(Consumer<Button> runnable) {
		this.runnable = runnable;
		return this;
	}
	
	public boolean isTouchingMouse() {
		return this.isTouching;
	}
	
	@Override
	protected void reloadNode() {
		iconLoaded = false;
	}
	
	@Override
	public List<SettingCategory> getNodeSettings() {
		var key = new SettingCategory.SettingKey("button", "Button", ((ArrayBitmap) ResourceLocator.getResource("editor_icons")).getBitmap(1, 2));
		
		var settingCategory = SettingCategory
				.createSettingCategory(key)
				.addSetting(new Setting<String>("Text", this.text, String.class, EnumSettingType.TEXT_FIELD).addChangeListener(this::setText))
				.addSetting(new Setting<Integer>("Text Color", this.tColor, Integer.class, EnumSettingType.COLOR_PICKER).addChangeListener(this::setTextColor))
				.addSetting(new Setting<Integer>("Button Color", this.bColor, Integer.class, EnumSettingType.COLOR_PICKER).addChangeListener(this::setButtonColor))
				.addSetting(new Setting<String>("Button Icon", this.iconID, String.class, EnumSettingType.RESOURCE_SELECT).addChangeListener(this::setButtonIcon))
				.addSetting(new Setting<Boolean>("Is Icon Left", this.isIconLeft, Boolean.class, EnumSettingType.CHECK_BOX).addChangeListener(this::setButtonIconLeft));
		
		var list = super.getNodeSettings();
		list.add(settingCategory);
		return list;
	}
	
	@Override
	public void saveNodeData(DataOutputStream out) throws IOException {
		super.saveNodeData(out);
		
		out.writeUTF(text);
		out.writeInt(tColor);
		out.writeInt(bColor);
		
		out.writeInt(textOffset);
		out.writeBoolean(movingRight);
		out.writeDouble(textOffsetLong);
		
		out.writeUTF(iconID);
		out.writeBoolean(isTouching);
		out.writeBoolean(isIconLeft);
	}

	@Override
	public void loadNodeData(DataInputStream in) throws IOException {
		super.loadNodeData(in);
		
		this.text = in.readUTF();
		this.tColor = in.readInt();
		this.bColor = in.readInt();
		
		this.textOffset = in.readInt();
		this.movingRight = in.readBoolean();
		this.textOffsetLong = in.readDouble();
		
		this.iconID = in.readUTF();
		this.isTouching = in.readBoolean();
		this.isIconLeft = in.readBoolean();
		this.iconLoaded = false;
	}
}
