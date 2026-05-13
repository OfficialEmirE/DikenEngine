package me.ramazanenescik04.diken.renderer;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.VolatileImage;

import javax.swing.JPanel;

import me.ramazanenescik04.diken.resource.Bitmap;

/**
 * Represents the `RendererPanel` type within the DikenEngine `renderer` package.
 */
public class RendererPanel extends JPanel {
	private static final long serialVersionUID = -4127449942800588594L;

	private final Object frameLock = new Object();

	private Bitmap frameBitmap = new Bitmap(1, 1);
	private BufferedImage frameImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
	private VolatileImage volatileFrame;

	private int logicalWidth = 1;
	private int logicalHeight = 1;
	private int renderScale = 1;

	public RendererPanel(int width, int height) {
		setPreferredSize(new Dimension(Math.max(1, width), Math.max(1, height)));
		setDoubleBuffered(false);
		setBackground(Color.BLACK);
		setFocusable(true);
	}

	public Bitmap acquireFrameBuffer(int logicalWidth, int logicalHeight) {
		synchronized (frameLock) {
			ensureFrameBuffers(logicalWidth, logicalHeight);
			return frameBitmap;
		}
	}

	public void present(int scale) {
		synchronized (frameLock) {
			this.renderScale = Math.max(1, scale);
			blitBufferedToVolatile(Math.max(1, this.logicalWidth * this.renderScale),
					Math.max(1, this.logicalHeight * this.renderScale));
		}

		repaint();
	}

	public Bitmap getFrameSnapshot() {
		synchronized (frameLock) {
			return frameBitmap.clone();
		}
	}

	private void ensureFrameBuffers(int requestedWidth, int requestedHeight) {
		int safeWidth = Math.max(1, requestedWidth);
		int safeHeight = Math.max(1, requestedHeight);

		logicalWidth = safeWidth;
		logicalHeight = safeHeight;

		if (frameBitmap.w != safeWidth || frameBitmap.h != safeHeight) {
			frameImage = new BufferedImage(safeWidth, safeHeight, BufferedImage.TYPE_INT_ARGB);
			frameBitmap = Bitmap.wrap(safeWidth, safeHeight,
					((DataBufferInt) frameImage.getRaster().getDataBuffer()).getData());
		}
	}

	private boolean ensureVolatileFrame(int width, int height) {
		GraphicsConfiguration gc = getGraphicsConfiguration();
		if (gc == null) {
			return false;
		}

		if (volatileFrame == null || volatileFrame.getWidth() != width || volatileFrame.getHeight() != height) {
			if (volatileFrame != null) {
				volatileFrame.flush();
			}
			volatileFrame = gc.createCompatibleVolatileImage(width, height);
		}

		if (volatileFrame == null) {
			return false;
		}

		int validationResult = volatileFrame.validate(gc);
		if (validationResult == VolatileImage.IMAGE_INCOMPATIBLE) {
			volatileFrame.flush();
			volatileFrame = gc.createCompatibleVolatileImage(width, height);
		}

		return volatileFrame != null;
	}

	private void blitBufferedToVolatile(int targetWidth, int targetHeight) {
		if (!ensureVolatileFrame(targetWidth, targetHeight)) {
			return;
		}

		do {
			Graphics2D g = volatileFrame.createGraphics();
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, targetWidth, targetHeight);
			g.drawImage(frameImage, 0, 0, targetWidth, targetHeight, null);
			g.dispose();
		} while (volatileFrame.contentsLost());
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		int panelWidth = Math.max(1, getWidth());
		int panelHeight = Math.max(1, getHeight());

		synchronized (frameLock) {
			g.setColor(Color.BLACK);
			g.fillRect(0, 0, panelWidth, panelHeight);

			int targetWidth = Math.max(1, logicalWidth * Math.max(1, renderScale));
			int targetHeight = Math.max(1, logicalHeight * Math.max(1, renderScale));

			if (volatileFrame != null) {
				if (volatileFrame.contentsLost() || volatileFrame.getWidth() != targetWidth
						|| volatileFrame.getHeight() != targetHeight) {
					blitBufferedToVolatile(targetWidth, targetHeight);
				}
			}

			if (volatileFrame != null) {
				if (panelWidth == volatileFrame.getWidth() && panelHeight == volatileFrame.getHeight()) {
					g.drawImage(volatileFrame, 0, 0, null);
				} else {
					g.drawImage(volatileFrame, 0, 0, panelWidth, panelHeight, null);
				}
			} else {
				g.drawImage(frameImage, 0, 0, panelWidth, panelHeight, null);
			}
		}
	}

	@Override
	public void removeNotify() {
		synchronized (frameLock) {
			if (volatileFrame != null) {
				volatileFrame.flush();
				volatileFrame = null;
			}
		}
		super.removeNotify();
	}
}
