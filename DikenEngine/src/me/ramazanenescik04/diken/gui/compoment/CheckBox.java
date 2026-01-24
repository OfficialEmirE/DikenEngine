package me.ramazanenescik04.diken.gui.compoment;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

public class CheckBox extends GuiCompoment {
	private static final long serialVersionUID = 1L;
	
	private Runnable checkBoxClicked;
	private boolean touching;
	private boolean checked = false;
	
	public Text text;

	public CheckBox(String text, int x, int y, int width, int height) {
		super(x, y, 16, 16);
		this.text = new Text(text, x, y, 0xFFFFFFFF, DikenEngine.getEngine().defaultFont);
	}
	
	public void setRunnable(Runnable r) {
		this.checkBoxClicked = r;
	}

	@Override
	public Bitmap render() {
		Bitmap bitmap = super.render();
		
		ArrayBitmap array = (ArrayBitmap) ResourceLocator.getResource("checkbox-array");
		bitmap.draw(checked ? array.getBitmap(0, 0) : array.getBitmap(1, 0), 0, 0);
		if (touching) {
			bitmap.box(0, 0, width - 1, height - 1, 0xffffffff);
		}
		
		bitmap.draw(text.render(), 18, 2);
		
		return bitmap;
	}

	@Override
	public void tick(DikenEngine engine) {
		super.tick(engine);
	}

	@Override
	public void mouseClicked(int x, int y, int button, boolean isTouch) {
		if (this.active && (isTouch || touching)) {
			checked = !checked;
			if (checkBoxClicked != null) {
				checkBoxClicked.run();
			}
		}
	}

	@Override
	public void mouseGetInfo(int x, int y, boolean isTouch) {
		this.touching = isTouch;
	}
}
