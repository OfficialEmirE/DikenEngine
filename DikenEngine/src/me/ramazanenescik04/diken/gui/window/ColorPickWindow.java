package me.ramazanenescik04.diken.gui.window;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.gui.compoment.Button;
import me.ramazanenescik04.diken.gui.compoment.ColorPickBar;
import me.ramazanenescik04.diken.gui.compoment.ColorPickBox;
import me.ramazanenescik04.diken.gui.compoment.Panel;
import me.ramazanenescik04.diken.gui.compoment.RenderImage;
import me.ramazanenescik04.diken.gui.screen.StaticBackground;
import me.ramazanenescik04.diken.resource.Bitmap;

public class ColorPickWindow extends Window {
	private static final long serialVersionUID = -2044184046321486559L;
	private int selectedColor;
	
	private ColorPickBox box;
	private ColorPickBar bar;
	private RenderImage colorBitmap;
	
	private ColorPickFuture future;
	
	public ColorPickWindow(int x, int y) {
		super(x, y, 100, 165);
		this.setTitle("Select Color");
	}

	@Override
	protected void open() {
		DikenEngine engine = DikenEngine.getEngine();
		this.setLocation(engine.getScaledWidth() / 2 - this.width / 2, engine.getScaledHeight() / 2 - this.height / 2);
		this.getContentPane().setBackground(new StaticBackground(Bitmap.createClearedBitmap(16, 16, 0xffa0a0a0)));
		Panel panel = this.getContentPane();
		colorBitmap = new RenderImage(Bitmap.createClearedBitmap(16, 16, selectedColor), 77, 97) {
			@Override
			public Bitmap render() {
				Bitmap bitmap = new Bitmap(this.bitmap.w + 2, this.bitmap.h + 2);
				bitmap.draw(this.bitmap, 1, 1);
				bitmap.box(0, 0, bitmap.w - 1, bitmap.h - 1, 0xff000000);
				return bitmap;
			}	
		};
		box = new ColorPickBox(2, 2, 95, 95).setSelectedColor(selectedColor).setHueColor(this.selectedColor).setConsumer((color) -> {
			this.selectedColor = color;
			this.colorBitmap.setBitmap(Bitmap.createClearedBitmap(16, 16, selectedColor));
		});	
		bar = new ColorPickBar(2, 97, 75, 19).setSelectedColor(this.selectedColor).setConsumer((color) -> {
			box.setSelectedColor(color).setHueColor(color);

			this.selectedColor = color;
			this.colorBitmap.setBitmap(Bitmap.createClearedBitmap(16, 16, color));
		});
		panel.add(colorBitmap);
		panel.add(box);
		panel.add(bar);
		
		Button cancelButton = new Button("Cancel", 2, 118, 46, 16).setRunnable(() -> {
			if (this.future != null) {
				this.future.cancelled();
			}
			this.close();
		});
		Button okButton = new Button("OK", 50, 118, 46, 16).setRunnable(() -> {
			if (this.future != null) {
				this.future.succesed(selectedColor);
			}
			this.close();
		});
		panel.add(cancelButton);
		panel.add(okButton);
	}

	public ColorPickWindow setSelectedColor(int color) {
		this.selectedColor = color;
		return this;
	}
	
	public ColorPickWindow setColorPickFuture(ColorPickFuture cpf) {
		this.future = cpf;
		return this;
	}
	
	public int getSelectColor() {
		return this.selectedColor;
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
