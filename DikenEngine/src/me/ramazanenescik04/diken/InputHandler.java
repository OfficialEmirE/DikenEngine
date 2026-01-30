package me.ramazanenescik04.diken;

import java.awt.Point;

import org.lwjgl.input.Mouse;

import me.ramazanenescik04.diken.gui.Hitbox;

public class InputHandler {
	private static Hitbox mouseHitbox = new Hitbox(0, 0, 2, 2);
	
	private static Point mousePosition = new Point(0, 0);
	
	/**
	 * Returns the mouse hitbox, which is a 2x2 box at the current mouse position.
	 * The mouse position is adjusted for the current scale of the engine.
	 *
	 * @return The mouse hitbox.
	 */
	public static Hitbox getMouseHitbox() {
		/*DikenEngine engine = DikenEngine.getEngine();
		int mouseX = (int) (Mouse.getX() / engine.scale);
		int mouseY = (int) (engine.getHeight() - (Mouse.getY() / engine.scale));
		mouseHitbox.setLocation(mouseX, mouseY);*/
		return mouseHitbox;
	}
	
	/**
	 * Returns the current mouse position as a Point object.
	 * The mouse position is adjusted for the current scale of the engine.
	 *
	 * @return The current mouse position.
	 */
	public static Point getMousePosition() {
		/*DikenEngine engine = DikenEngine.getEngine();
		int mouseX = (int) (Mouse.getX() / engine.scale);
		int mouseY = (int) (engine.getHeight() - (Mouse.getY() / engine.scale));
		mousePosition.setLocation(mouseX, mouseY);*/
		return mousePosition;
	}
	
	/**
	 * Updates the mouse position based on the current mouse coordinates.
	 * The mouse position is adjusted for the current scale of the engine.
	 */
	public static void updateMousePosition() {
		DikenEngine engine = DikenEngine.getEngine();
		int mouseX = (int) (Mouse.getX() / engine.scale);
		int mouseY = (int) (engine.getHeight() - (Mouse.getY() / engine.scale));
		mousePosition.setLocation(mouseX, mouseY);
		mouseHitbox.setLocation(mouseX, mouseY);
	}
	
	/**
	 * Farenin Pencere içinde mi yoksa beaşka terdemi olduğunu söyler.
	 * 
	 * @see org.lwjgl.input.Mouse#isInsideWindow()
	 * 
	 * @return mouse on screen
	 */
	public static boolean isMouseOnScreen() {
		return Mouse.isInsideWindow();
	}
}
