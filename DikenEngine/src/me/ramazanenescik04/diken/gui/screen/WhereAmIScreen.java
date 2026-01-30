package me.ramazanenescik04.diken.gui.screen;

import java.util.Random;

import me.ramazanenescik04.diken.Timer;
import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.tools.PixelToColor;
import me.ramazanenescik04.diken.tools.Utils;

public class WhereAmIScreen extends Screen {
	
	private Timer darknessTimer;
	private Bitmap canvas;
	private Particle[] particles;
	private int alpha = 255; // Ekranın başlangıçta görünürlüğü
	
	public WhereAmIScreen() {
		this.canvas = Bitmap.empty;
		this.particles = new Particle[1000];
	}

	public void openScreen() {
		darknessTimer = new Timer((ct, mt, st, et, isEnd) -> {
			if (isEnd) {
				alpha = 0; // Ekran tamamen görünür olacak
			} else {
				alpha = ct < mt ? (int) ((1.0 - (double) ct / mt) * 255) : 0; // Ekran aydınlaşma hesaplaması
			}
		}, 0, Utils.timeToLong(5));
		this.canvas = new Bitmap(engine.getWidth(), engine.getHeight());
	    
	    Random rand = new Random();
	    
	    for (int i = 0; i < particles.length; i++) {
            particles[i] = new Particle(engine.getWidth(), engine.getHeight(), rand);
        }
	    
	    darknessTimer.start();
	}
	
	public void render(Bitmap bitmap) {
		super.render(bitmap);
		
		if (canvas == null) {
			canvas = new Bitmap(engine.getWidth(), engine.getHeight());
		}
		bitmap.draw((canvas), 0, 0);
		
		var color = PixelToColor.toColor(alpha, 0, 0, 0);
		bitmap.blendFill(0, 0, engine.getWidth(), engine.getHeight(), color);
	}
	
	public void tick() {
		super.tick();
		
		update();
	}
	
	private void update() {       
		canvas.clear(0xff000000);
		
        for (Particle p : particles) {
        	if (p == null) continue; // Null kontrolü
        	
            p.update(canvas.w, canvas.h);;
            canvas.setPixel((int) p.x, (int) p.y, p.color);
        }
    }

	public void closeScreen() {
		darknessTimer.stop();
	}
	
	public void resized() {
		this.canvas = new Bitmap(engine.getWidth(), engine.getHeight());
	}

	static class Particle {
        float x, y;
        float dx, dy;
        int color;

        Particle(int width, int height, Random rand) {
            x = rand.nextInt(width);
            y = rand.nextInt(height);
            dx = (rand.nextFloat() - 0.5f) * 2;
            dy = (rand.nextFloat() - 0.5f) * 2;
            color = PixelToColor.toColor(1.0f, rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
        }

        void update(int width, int height) {
            x += dx;
            y += dy;

            if (x < 0 || x >= width) dx *= -1;
            if (y < 0 || y >= height) dy *= -1;
        }
    }
}
