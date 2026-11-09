package me.ramazanenescik04.diken.gui.component;

import me.ramazanenescik04.diken.game.Node;
import me.ramazanenescik04.diken.gui.hitbox.Hitbox;
import me.ramazanenescik04.diken.resource.Bitmap;

public class BaseScreenGui extends Node {
	public BaseScreenGui() {
		this("BaseScreenGui");
	}
	
	public BaseScreenGui(String name) {
		super(name);
	}
	
	public void drawScreen(Bitmap sceneBitmap, Hitbox viewport, boolean allowDrawBitmap) {}

	public void keyHandled(int inputMode, int key, char character) {}

	public void mouseHandled(int inputMode, int x, int y, int clicked) {}
}
