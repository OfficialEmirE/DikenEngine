package me.ramazanenescik04.diken.renderer;

import java.awt.Color;
import java.awt.Dimension;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.AWTGLCanvas;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import me.ramazanenescik04.diken.resource.Bitmap;

/**
 * Represents the `RendererPanel` type within the DikenEngine `renderer` package.
 */
public class RendererPanel extends AWTGLCanvas {
	private static final long serialVersionUID = -4127449942800588594L;

	private final Object frameLock = new Object();

	private Bitmap frameBitmap = new Bitmap(1, 1);
	private IntBuffer presentedPixels = createDirectIntBuffer(1);

	private int logicalWidth = 1;
	private int logicalHeight = 1;
	private int presentedWidth = 1;
	private int presentedHeight = 1;
	private int renderScale = 1;

	private boolean glInitialized;
	private int textureId;
	private int textureWidth = 1;
	private int textureHeight = 1;
	
	public RendererPanel(int width, int height) throws LWJGLException {
		setPreferredSize(new Dimension(Math.max(1, width), Math.max(1, height)));
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
			copyFrameForPresentation();
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
			frameBitmap = new Bitmap(safeWidth, safeHeight);
		}
	}

	private void copyFrameForPresentation() {
		int pixelCount = logicalWidth * logicalHeight;
		if (presentedPixels.capacity() != pixelCount) {
			presentedPixels = createDirectIntBuffer(pixelCount);
		}

		IntBuffer source = frameBitmap.pixels.convertDirectIntBuffer();
		source.clear();
		source.limit(pixelCount);

		presentedPixels.clear();
		presentedPixels.put(source);
		presentedPixels.flip();

		presentedWidth = logicalWidth;
		presentedHeight = logicalHeight;
	}

	@Override
	protected void paintGL() {
		int canvasWidth = Math.max(1, getWidth());
		int canvasHeight = Math.max(1, getHeight());
		
		initGLIfNeeded(canvasWidth, canvasHeight);

		GL11.glViewport(0, 0, canvasWidth, canvasHeight);
		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();
		GL11.glOrtho(0.0D, canvasWidth, canvasHeight, 0.0D, -1.0D, 1.0D);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();

		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);

		synchronized (frameLock) {
			ensureTextureCapacity(presentedWidth, presentedHeight);
			uploadPresentedFrame();
			drawPresentedFrame(canvasWidth, canvasHeight);
		}

		try {
			swapBuffers();
		} catch (LWJGLException e) {
			e.printStackTrace();
		}
	}

	private void initGLIfNeeded(int w, int h) {
		if (glInitialized) {
			return;
		}

		GL11.glClearColor(0.0F, 0.0F, 0.0F, 1.0F);
		GL11.glDisable(GL11.GL_DEPTH_TEST);
		GL11.glDisable(GL11.GL_LIGHTING);
		GL11.glEnable(GL11.GL_TEXTURE_2D);

		textureId = GL11.glGenTextures();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, w, h, 0,
				GL12.GL_BGRA, GL12.GL_UNSIGNED_INT_8_8_8_8_REV, (java.nio.ByteBuffer) null);

		glInitialized = true;
	}

	private void ensureTextureCapacity(int width, int height) {
		int requiredWidth = nextPowerOfTwo(Math.max(1, width));
		int requiredHeight = nextPowerOfTwo(Math.max(1, height));

		if (textureWidth == requiredWidth && textureHeight == requiredHeight) {
			return;
		}

		textureWidth = requiredWidth;
		textureHeight = requiredHeight;

		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, textureWidth, textureHeight, 0, GL12.GL_BGRA,
				GL11.GL_UNSIGNED_BYTE, (ByteBuffer) null);
	}

	private void uploadPresentedFrame() {
		presentedPixels.rewind();
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
		GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, presentedWidth, presentedHeight, GL12.GL_BGRA,
				GL11.GL_UNSIGNED_BYTE, presentedPixels);
	}

	private void drawPresentedFrame(int canvasWidth, int canvasHeight) {
		float textureU = (float) presentedWidth / (float) textureWidth;
		float textureV = (float) presentedHeight / (float) textureHeight;

		int targetWidth = Math.max(1, presentedWidth * Math.max(1, renderScale));
		int targetHeight = Math.max(1, presentedHeight * Math.max(1, renderScale));

		if (targetWidth != canvasWidth || targetHeight != canvasHeight) {
			targetWidth = canvasWidth;
			targetHeight = canvasHeight;
		}

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glBegin(GL11.GL_QUADS);
		GL11.glTexCoord2f(0.0F, 0.0F);
		GL11.glVertex2i(0, 0);
		GL11.glTexCoord2f(textureU, 0.0F);
		GL11.glVertex2i(targetWidth, 0);
		GL11.glTexCoord2f(textureU, textureV);
		GL11.glVertex2i(targetWidth, targetHeight);
		GL11.glTexCoord2f(0.0F, textureV);
		GL11.glVertex2i(0, targetHeight);
		GL11.glEnd();
	}

	private int nextPowerOfTwo(int value) {
		int result = 1;
		while (result < value) {
			result <<= 1;
		}
		return result;
	}

	private static IntBuffer createDirectIntBuffer(int capacity) {
		return ByteBuffer.allocateDirect(Math.max(1, capacity) * 4).order(ByteOrder.nativeOrder()).asIntBuffer();
	}

	@Override
	public void removeNotify() {
		try {
			if (glInitialized) {
				makeCurrent();
				GL11.glDeleteTextures(textureId);
				glInitialized = false;
				textureId = 0;
			}
		} catch (LWJGLException e) {
			e.printStackTrace();
		}

		super.removeNotify();
	}
	
	@Override
    public java.awt.Dimension getMinimumSize() {
        return new java.awt.Dimension(0, 0);
    }
}
