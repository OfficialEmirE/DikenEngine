package me.ramazanenescik04.diken.gui.screen;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.InputHandler;
import me.ramazanenescik04.diken.gui.compoment.Panel;
import me.ramazanenescik04.diken.resource.Bitmap;

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
			contentPane.setSize(engine.getWidth(), engine.getHeight());
			contentPane.tick(engine);
		}
	}
	
	public void keyboardEvent() {
		this.keyDown( Keyboard.getEventCharacter(), Keyboard.getEventKey());
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


	public void mouseEvent() {
		int mouseX = InputHandler.getMousePosition().x;
		int mouseY = InputHandler.getMousePosition().y;
		if (this.engine != null) {
			this.mouseGetInfo(mouseX, mouseY, (engine.wManager.screenActionMode(new java.awt.Point(mouseX, mouseY))), (InputHandler.isMouseOnScreen()));
		
			if (Mouse.getEventButtonState()) {
				this.mouseClick(mouseX, mouseY, Mouse.getEventButton(), (InputHandler.isMouseOnScreen()), (engine.wManager.screenActionMode(new java.awt.Point(mouseX, mouseY))));
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
