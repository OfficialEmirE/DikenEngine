package me.ramazanenescik04.diken.gui.window;

import java.awt.Color;
import java.awt.Point;

import org.lwjgl.input.Keyboard;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.gui.compoment.*;
import me.ramazanenescik04.diken.resource.Bitmap;

public class Window extends GuiCompoment {
	private static final long serialVersionUID = 1L;
	
	private String title = "";
	private Bitmap icon;
	private final static int BAR_HEIGHT = 20;
	
	private Panel contentPane = new Panel();
	private Button[] barButtons = new Button[3];

	public boolean closed;
	public boolean resizable;
	
	public Window(int x, int y, int width, int height) {
		super(x, y, width, height);
		contentPane.setBounds(this.x + 1, this.y + BAR_HEIGHT + 1, width - 2, height - BAR_HEIGHT - 2);
		contentPane.init(DikenEngine.getEngine());
		
		barButtons[0] = new Button("X", width - 18, 2, 16, 16).setButtonColor(0xffff0000).setTextColor(0xffffffff);
	}
	
	protected void open() {};
	
	public boolean isCloseable() {
		return this.barButtons[0].active;
	}
	
	public void setCloseable(boolean b) {
		this.barButtons[0].active = b;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Bitmap getIcon() {
		return icon;
	}

	public void setIcon(Bitmap icon) {
		this.icon = icon;
	}
	
	public Panel getContentPane() {
		return contentPane;
	}

	public void setContentPane(Panel contentPane) {
		contentPane.setBounds(this.x + 1, this.y + BAR_HEIGHT + 1, width - 2, height - BAR_HEIGHT - 2);
		contentPane.init(DikenEngine.getEngine());
		this.contentPane = contentPane;
	}

	public void resized() {
		contentPane.setBounds(this.x + 1, this.y + BAR_HEIGHT + 1, width - 2, height - BAR_HEIGHT - 2);
		barButtons[0].setLocation(width - 18, 2);
	}

	/*public Bitmap render() {
		Bitmap bitmap = super.render();
		bitmap.fill(0, 0, width - 1, height - 1, 0xff0000e9);
		Text.render(title, bitmap, 2, 2);
		
		bitmap.fill(contentPane.x, contentPane.y, contentPane.width - 1, contentPane.height + BAR_HEIGHT - 1, 0xffffffff);
		bitmap.draw(contentPane.render(), contentPane.x, contentPane.y);
		Button closeButton = barButtons[0];
		bitmap.draw(closeButton.render(), closeButton.x, closeButton.y);
		bitmap.box(0, 0, width - 1, height - 1, 0xff000000);
		return bitmap;
	}*/
	
	public Bitmap render() {
		int px = 0;
        int py = 0;
        int pg = width;
        int py2 = height;
		
		Bitmap bitmap = super.render();
		
		bitmap.fill(px, py, pg, py2, 0xffffffff);
		
		bitmap.draw(contentPane.render(), 1, BAR_HEIGHT + 1);
		
		bitmap.fill(px, py, pg, BAR_HEIGHT, active ? 0xff000080: Color.GRAY.getRGB());
		
		bitmap.box(px, py, pg - 1, py2 - 1, 0xff000000);
		bitmap.drawLine(px, py + BAR_HEIGHT, px + pg, py + BAR_HEIGHT, 0xff000000, 1);
		
		int titleX, titleY;
		
		if (icon != null) {
			bitmap.draw(icon, px + 2, py + 2);
			titleX = px + 20;
			titleY = py + 13 / 2;
		} else {
			titleX = px + 2;
			titleY = py + 13 / 2;
		}
		
		//Text.render(title, bitmap, px + 10 / 2, py + 13 / 2);
		Text.render(title, bitmap, titleX, titleY);
		
		Button closeButton = barButtons[0];
		bitmap.draw(closeButton.render(), closeButton.x, closeButton.y);
		
		/*bitmap.draw(fullscreenButton.render(), fullscreenButton.x, fullscreenButton.y);	
		Text.renderCenter(((Button)fullscreenButton).text, bitmap, fullscreenButton.x + fullscreenButton.width / 2 + 4, fullscreenButton.y + ((fullscreenButton.height / 2) - 2));
		
		bitmap.draw(smaleButton.render(), smaleButton.x, smaleButton.y);	
		Text.renderCenter(((Button)smaleButton).text, bitmap, smaleButton.x + smaleButton.width / 2 + 2, smaleButton.y + ((smaleButton.height / 2) - 2));*/
		
        /*bitmap.fill(px + pg - 20, py + 3, pg - 20 + 15, 18, 0xffff0000);
        bitmap.drawLine(px + pg - 17, py + 5, px + pg - 8, py + 14, 0xffffffff, 1);
        bitmap.drawLine(px + pg - 17, py + 14, px + pg - 8, py + 5, 0xffffffff, 1);*/
		
		if(active && resizable) {
			int[] resizeCornerX = {px + pg - 10, px + pg, px + pg};
			int[] resizeCornerY = {py + py2, py + py2 - 10, py + py2};
			bitmap.fillPolygon(resizeCornerX, resizeCornerY, 3, 0xff000000);
		}
		
		return bitmap;
	}

	public void tick(DikenEngine engine) {
		if (contentPane != null ) {
			contentPane.tick(engine);
		}
		barButtons[0].tick(engine);
	}

	public void keyPressed(char var1, int var2) {
		contentPane.keyPressed(var1, var2);
		
		if (var2 == Keyboard.KEY_ESCAPE) {
			close();
		}
	}

	public void mouseClicked(int x, int y, int button, boolean isTouch) {
		contentPane.mouseClicked(x - contentPane.x, y - contentPane.y, button, isTouch);
	}

	public void mouseGetInfo(int x, int y, boolean isTouch) {
		contentPane.mouseGetInfo(x - contentPane.x, y - contentPane.y, isTouch);
		barButtons[0].mouseGetInfo(x, y, closeButtonClicked(new Point(x, y)));
	}

	public void close() {
		this.closed = true;
	}

	public boolean closeButtonClicked(Point currentMousePoint) {
		Button closeButton = barButtons[0];
		
		if (!closeButton.active)
			return false;
		
		int closeButtonX = x + closeButton.x;
		int closeButtonY = y + closeButton.y;
		int w = closeButton.width;
		int h = closeButton.width;
		return currentMousePoint.x >= closeButtonX && 
				currentMousePoint.x <= closeButtonX + w && 
				currentMousePoint.y >= closeButtonY && 
				currentMousePoint.y <= closeButtonY + h;
	}

	public boolean isTouching(Point point) {
		return point.x >= x && 
				   point.x <= x + width && 
				   point.y >= y && 
				   point.y <= y + height;
	}

	public void moved() {
		contentPane.setLocation(this.x + 1, this.y + BAR_HEIGHT + 1);
		barButtons[0].setLocation(width - 18, 2);
	};
	
}
