package me.ramazanenescik04.diken.scripting;

import me.ramazanenescik04.diken.game.World;

public interface ScriptableScreen {
	boolean isAllowRunCode();

	World getWorld();
}
