package me.ramazanenescik04.diken.gui.compoment;

import me.ramazanenescik04.diken.gui.hitbox.IHitbox;

public interface IGuiListener {
	void changedBounds(IHitbox newBounds);
	void changedSize(IHitbox newBounds, int width, int height);
	void changedLocation(IHitbox newBounds, int x, int y);
}
