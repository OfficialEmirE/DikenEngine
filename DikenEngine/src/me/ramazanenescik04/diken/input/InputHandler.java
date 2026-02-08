package me.ramazanenescik04.diken.input;

import java.awt.Point;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.gui.Hitbox;

public class InputHandler implements MouseListener, MouseMotionListener, MouseWheelListener, KeyListener {

    public static final int INPUT_NONE     = 0;
    public static final int INPUT_PRESSED  = 1;
    public static final int INPUT_REPEATED = 2;
    public static final int INPUT_RELEASED = 3;
    public static final int INPUT_WHEEL    = 4;

    private final boolean[] keys = new boolean[65536];
    private final boolean[] lastKeys = new boolean[65536];

    private final javax.swing.JPanel thePanel;
    private final List<IInputListener> listeners = new ArrayList<>();

    private Hitbox mouseHitbox = new Hitbox(0, 0, 1, 1);
    private Point mousePosition = new Point(0, 0);
    private boolean isMouseOnScreen;

    private boolean mousePressed;
    private int lastMouseButton = -1;
    private int wheelValue;

    public InputHandler(javax.swing.JPanel panel) {
        this.thePanel = panel;

        panel.setFocusable(true);
        panel.requestFocusInWindow();

        panel.addMouseListener(this);
        panel.addMouseMotionListener(this);
        panel.addMouseWheelListener(this);
        panel.addKeyListener(this);
    }

    /* ================= LISTENER ================= */

    public void addListener(IInputListener listener) {
        listeners.add(listener);
    }

    public void removeListener(IInputListener listener) {
        listeners.remove(listener);
    }

    /* ================= KEY ================= */

    @Override
    public void keyTyped(KeyEvent e) {
        //notifyKey(INPUT_TYPED, e.getKeyCode(), e.getKeyChar());
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();

        if (code >= 0 && code < keys.length) {
            if (keys[code]) {
                notifyKey(INPUT_REPEATED, code, e.getKeyChar());
            } else {
                keys[code] = true;
                notifyKey(INPUT_PRESSED, code, e.getKeyChar());
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();

        if (code >= 0 && code < keys.length) {
            keys[code] = false;
            notifyKey(INPUT_RELEASED, code, e.getKeyChar());
        }
    }

    private void notifyKey(int mode, int key, char ch) {
        for (IInputListener l : listeners) {
            l.keyHandled(mode, key, ch);
        }
    }

    /* ================= MOUSE ================= */

    @Override
    public void mousePressed(MouseEvent e) {
        mousePressed = true;
        lastMouseButton = e.getButton() - 1; // Convert to 0-based index

        notifyMouse(INPUT_PRESSED, e.getX(), e.getY(), lastMouseButton);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        mousePressed = false;

        notifyMouse(INPUT_RELEASED, e.getX(), e.getY(), e.getButton());
        lastMouseButton = -1;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
    	 lastMouseButton = e.getButton() - 1; // Convert to 0-based index

         /*notifyMouse(INPUT_CLICKED, e.getX(), e.getY(), lastMouseButton);*/
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        updateMouse(e);
        notifyMouse(INPUT_NONE, e.getX(), e.getY(), -1);
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        updateMouse(e);
        notifyMouse(INPUT_REPEATED, e.getX(), e.getY(), lastMouseButton);
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        notifyMouse(INPUT_WHEEL, e.getX(), e.getY(), -e.getWheelRotation());
        this.wheelValue = -e.getWheelRotation();
    }

    private void updateMouse(MouseEvent e) {
    	int mouseX = e.getX() / DikenEngine.getEngine().getScale();
    	int mouseY = e.getY() / DikenEngine.getEngine().getScale();
    	
        mousePosition.setLocation(mouseX, mouseY);
        mouseHitbox.setLocation(mouseX, mouseY);
        isMouseOnScreen = e.getX() >= 0 && e.getY() >= 0 && e.getX() < this.thePanel.getWidth() && e.getY() < this.thePanel.getHeight();
    }

    private void notifyMouse(int mode, int x, int y, int clicked) {
        for (IInputListener l : listeners) {
            l.mouseHandled(mode, x / DikenEngine.getEngine().getScale(), y / DikenEngine.getEngine().getScale(), clicked);
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        isMouseOnScreen = true;
    }

    @Override
    public void mouseExited(MouseEvent e) {
        isMouseOnScreen = false;
    }
    
    public void update() {
        System.arraycopy(keys, 0, lastKeys, 0, keys.length);
    }

    /* ================= GETTERS ================= */

    public Hitbox getMouseHitbox() {
        return mouseHitbox;
    }

    public Point getMousePosition() {
        return mousePosition;
    }

    public boolean isMouseOnScreen() {
        return isMouseOnScreen;
    }
    
    public boolean isMouseDown(int button) {
        return mousePressed && lastMouseButton == button;
    }
    
    public boolean isKeyDown(int keyCode) {
        if (keyCode < 0 || keyCode >= keys.length) return false;
        return keys[keyCode];
    }
    
    public boolean isKeyPressed(int key) {
        return isKeyDown(key) && !lastKeys[key];
    }

    public boolean isKeyReleased(int key) {
        return !isKeyDown(key) && lastKeys[key];
    }

	public int getWheelValue() {
		return wheelValue;
	}

	public static String actionToString(int action) {
		switch (action) {
			case (INPUT_NONE) -> {return "INPUT_NONE";}
			case (INPUT_PRESSED) -> {return "INPUT_PRESSED";}
			case (INPUT_REPEATED) -> {return "INPUT_REPEATED";}
			case (INPUT_RELEASED) -> {return "INPUT_RELEASED";}
			//case (INPUT_TYPED) -> {return "INPUT_TYPED";}
			case (INPUT_WHEEL) -> {return "INPUT_WHEEL";}
			//case (INPUT_CLICKED) -> {return "INPUT_CLICKED";}
		}
		return "INPUT_UNKNOWN";
	}
}
