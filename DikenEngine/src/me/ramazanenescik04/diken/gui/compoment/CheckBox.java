package me.ramazanenescik04.diken.gui.compoment;

import java.util.function.Consumer;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.resource.ArrayBitmap;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.ResourceLocator;

public class CheckBox extends GuiComponent {
	private static final long serialVersionUID = 1L;
	
	private Consumer<CheckBox> checkBoxClicked;
	private boolean touching;
	private boolean checked = false;
	
	public Text text;

	public CheckBox(String text, int x, int y) {
		super(x, y, 16, 16);
		this.text = new Text(text, x, y, 0, 0, 0xFFFFFFFF, DikenEngine.getEngine().defaultFont);
	}
	
	public CheckBox setConsumer(Consumer<CheckBox> r) {
		this.checkBoxClicked = r;
		return this;
	}

	public boolean isChecked() {
		return checked;
	}

	public CheckBox setChecked(boolean checked) {
		this.checked = checked;
		return this;
	}

	@Override
	public Bitmap render() {
		Bitmap bitmap = new Bitmap(20 + text.width + 2, 20);
		
		ArrayBitmap array = (ArrayBitmap) ResourceLocator.getResource("checkbox-array");
		bitmap.draw(checked ? array.getBitmap(0, 0) : array.getBitmap(1, 0), 2, 2);
		if (touching) {
			bitmap.box(2, 2, 17, 17, 0xffffffff);
		}
		
		bitmap.draw(text.render(), 20, 6);
		
		return bitmap;
	}

	@Override
	public void tick(DikenEngine engine) {
		text.tick(engine);
	}

	@Override
	public void mouseClicked(int x, int y, int button, boolean isTouch) {
		if (this.active && (isTouch || touching)) {
			checked = !checked;
			if (checkBoxClicked != null) {
				checkBoxClicked.accept(this);
			}
		}
	}

	@Override
	public void mouseGetInfo(int x, int y, boolean isTouch) {
		this.touching = isTouch;
	}
}
