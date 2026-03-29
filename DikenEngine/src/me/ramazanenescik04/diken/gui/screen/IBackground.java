package me.ramazanenescik04.diken.gui.screen;

import me.ramazanenescik04.diken.resource.Bitmap;

/**
 * Defines the `IBackground` type within the DikenEngine `gui.screen` package.
 */
public interface IBackground {
	
	void render(Bitmap bitmap);
	
	void tick();

}
