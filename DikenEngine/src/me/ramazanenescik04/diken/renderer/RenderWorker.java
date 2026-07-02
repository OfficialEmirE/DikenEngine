package me.ramazanenescik04.diken.renderer;

import me.ramazanenescik04.diken.DikenEngine;
import me.ramazanenescik04.diken.resource.Bitmap;

public class RenderWorker implements Runnable {
	private volatile DikenEngine engine;
    private final RendererPanel rendererPanel;
    private final int scale;
    private volatile boolean running = false;
    private Thread thread;
    
    // Çizilecek son frame'i güvenli şekilde aktarmak için bir referans
    private volatile Bitmap nextFrameToRender = null;
    private final Object frameLock = new Object();

    public RenderWorker(DikenEngine engine, RendererPanel rendererPanel, int scale) {
    	this.engine = engine;
        this.rendererPanel = rendererPanel;
        this.scale = scale;
    }

	public synchronized void start() {
        if (running) return;
        running = true;
        thread = new Thread(this, "Render Thread");
        thread.setDaemon(true);
        thread.start();
    }

    public synchronized void stop() {
        running = false;
        if (thread != null) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    // Ana thread bu metotla çizilecek yeni resmi render thread'ine gönderir
    public void queueFrame(Bitmap bitmap) {
        synchronized (frameLock) {
            this.nextFrameToRender = bitmap;
            frameLock.notify(); // Uyuyan render thread'ini uyandır
        }
    }

    @Override
    public void run() {
        while (running) {
            Bitmap frameToRender = null;

            synchronized (frameLock) {
                // Eğer çizilecek yeni bir frame yoksa, thread'i uyut (CPU harcamasın)
                while (nextFrameToRender == null && running) {
                    try {
                        frameLock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                
                if (!running) break;

                // Çizilecek resmi al ve ana thread'in yeni resim koyabilmesi için boşa çıkar
                frameToRender = nextFrameToRender;
                nextFrameToRender = null;
            }

            if (frameToRender != null) {
                // Gerçek çizim ve ekrana sunma işlemleri burada yapılır
                FrameBitmapPool.beginFrame();
                engine.render(frameToRender);
                rendererPanel.present(scale);
            }
        }
    }
}