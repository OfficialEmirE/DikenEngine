package me.ramazanenescik04.diken.gui.screen;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.gui.compoment.Panel;
import me.ramazanenescik04.diken.input.InputHandler;
import me.ramazanenescik04.diken.resource.Bitmap;

/**
 * Represents the `Screen` type within the DikenEngine `gui.screen` package.
 */
public abstract class Screen {
	private Panel contentPane = new Panel(0, 0, 1, 1);
	public DikenEngine engine;
	
	private IBackground background;
	public boolean renderBackground = true;
	
	public void tick() {
		if (renderBackground && background != null) {
			background.tick();
		}
		
		if (this.engine != null) {
			contentPane.setSize(engine.getScaledWidth(), engine.getScaledHeight());
			contentPane.tick(engine);
		}
	}
	
	public void keyboardEvent(int action, int key, char character) {
		if (action == InputHandler.INPUT_PRESSED || action == InputHandler.INPUT_REPEATED) {
			this.keyDown(character, key);
		}
	}

	public void keyDown(char eventCharacter, int eventKey) {
		contentPane.keyPressed(eventCharacter, eventKey);
	}

	public void mouseClick(int mouseX, int mouseY, int eventButton, boolean isScreenActionMode, boolean isMouseOnScreen) {
		contentPane.mouseClicked(mouseX - contentPane.x, mouseY - contentPane.y, eventButton, isScreenActionMode && isMouseOnScreen);
	}
	
	public void mouseGetInfo(int mouseX, int mouseY, boolean isScreenActionMode, boolean isMouseOnScreen) {
		contentPane.mouseGetInfo(mouseX - contentPane.x, mouseY - contentPane.y, isMouseOnScreen && isScreenActionMode);
	}
	
	public void openScreen() {
	}
	   
	public void closeScreen() {
	}
	
	public void resized() {};

	public void render(Bitmap bitmap) {
		if (renderBackground && background != null) {
			background.render(bitmap);
		}
		
		bitmap.draw(contentPane.render(), contentPane.x, contentPane.y);
	}
	
	public void setBackground(IBackground background) {
		this.background = background;
	}


	public void mouseEvent(int inputMode, int x, int y, int clicked) {
		if (this.engine != null) {
			this.mouseGetInfo(x, y, (engine.wManager.screenActionMode(new java.awt.Point(x, y))), engine.input.isMouseOnScreen());
		
			if (inputMode == InputHandler.INPUT_CLICKED) {
				this.mouseClick(x, y, clicked, engine.input.isMouseOnScreen(), (engine.wManager.screenActionMode(new java.awt.Point(x, y))));
			}
		}
	}
	
	public Panel getContentPane() {
		return contentPane;
	}
	
	public void setContentPane(Panel panel) {
		if (panel == null)
			return;
		
		panel.init(engine);
		this.contentPane = panel;
	}

}
