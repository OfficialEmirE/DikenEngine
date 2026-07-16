package me.ramazanenescik04.diken.studio.editors;

import java.awt.BorderLayout;

import me.ramazanenescik04.diken.renderer.RendererPanel;

public class GamePreview extends BaseEditor {
	private static final long serialVersionUID = 1L;

	public GamePreview(RendererPanel panel) {
		super("Game Preview");
		this.closeable = false;
		
		this.setLayout(new BorderLayout());
		this.add(panel, BorderLayout.CENTER);
	}
}
