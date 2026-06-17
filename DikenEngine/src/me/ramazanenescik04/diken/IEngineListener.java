package me.ramazanenescik04.diken;

import java.awt.Rectangle;

import me.ramazanenescik04.diken.game.World;

public interface IEngineListener {
	void worldChanged(World oldScreen, World newScreen);
	void windowResized(Rectangle windowSize);
}
