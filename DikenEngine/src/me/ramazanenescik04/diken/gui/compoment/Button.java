package me.ramazanenescik04.diken.gui.compoment;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

public class Button extends GuiCompoment {
	private static final long serialVersionUID = 1L;
	public String text = "";
	public int renderX, renderY, xa, ya, tColor = 0xffffffff, bColor = 0xffffffff;
	
	private int textOffset = 0; // Yazı kaydırma için offset
    private boolean movingRight = true; // Yazının hareket yönü
    private final double SCROLL_SPEED = 0.49888d; // Kaydırma hızı
    private double textOffsetLong = 0;
    
    private Runnable runnable;
    
    private boolean isTouching = false;
	
	public Button(String text, int x, int y, int width, int height) {
		super(x, y, width, height);
		this.xa = x;
		this.ya = y;
		this.renderX = xa;
		this.renderY = ya;
		this.text = text;	
	}
	
	public Button setTextColor(int color) {
		this.tColor = color;
		return this;
	}
	
	public Button setButtonColor(int color) {
		this.bColor = color;
		return this;
	}
	
	public Bitmap render() {
		Bitmap bitmap = new Bitmap(width, height);
		Bitmap buttonBitmap = bitmap.clone();
		buttonBitmap.fill(0, 0, width, height, 0xff484848);
		
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
		
		bitmap.blendDraw(buttonBitmap, 0, 0, bColor);
		
		// Yazı genişliğini kontrol et
        int textWidth = Text.stringBitmapWidth(text, DikenEngine.getEngine().defaultFont);
        
        if (textWidth > width) {     
            Text.render(text, bitmap, -textOffset + 10, ((height / 2) - 4), tColor);
        } else {
            // Normal merkezi render
            Text.renderCenter(text, bitmap, width / 2, ((height / 2) - 4), tColor);
        }
        
        if (isTouching) {
        	bitmap.box(0, 0, width - 1, height - 1, 0xffffffff);
        }
        
        if (!active) {
			bitmap.box(0, 0, width - 1, height - 1, 0x7f000000);
		}
        
        return bitmap;
	}

	public void tick(DikenEngine engine) {
		int textWidth = Text.stringBitmapWidth(text, engine.defaultFont);
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
			if (button == 0 && runnable != null && this.active) {
				runnable.run();
			}
		}
	}
	
	public void mouseGetInfo(int x, int y, boolean isTouch) {
		if (active)
			this.isTouching = isTouch;
		else
			this.isTouching = false;
	}

	public Button setRunnable(Runnable runnable) {
		this.runnable = runnable;
		return this;
	}
}
