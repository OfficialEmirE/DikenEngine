package me.ramazanenescik04.diken.game;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

import me.ramazanenescik04.diken.resource.Bitmap;
import me.ramazanenescik04.diken.resource.EnumResource;
import me.ramazanenescik04.diken.resource.IResource;

public class Animation implements IResource {
    private static final long serialVersionUID = 1L;
	private final List<Bitmap> frames = new ArrayList<>();
    private int fps = 12; // varsayılan FPS

    private int currentFrame = 0;
    private long lastFrameTime = 0;

    public Animation(int fps) {
        this.fps = fps;
    }

    // --- ÇERÇEVE YÖNETİMİ ---

    public void addFrame(Bitmap bitmap) {
        if (bitmap != null) frames.add(bitmap);
    }

    public void removeFrame(int index) {
        if (index >= 0 && index < frames.size()) {
            frames.remove(index);
            if (currentFrame >= frames.size())
                currentFrame = 0;
        }
    }

    public void clearFrames() {
        frames.clear();
        currentFrame = 0;
    }

    public int getFrameCount() {
        return frames.size();
    }

    public Bitmap getFrame(int index) {
        if (frames.isEmpty()) return null;
        return frames.get(index % frames.size());
    }

    // --- ANİMASYON ZAMANI ---

    public void update(long currentTimeMillis) {
        if (frames.isEmpty()) return;

        long frameDuration = 1000L / fps; // her frame kaç ms sürer
        if (currentTimeMillis - lastFrameTime >= frameDuration) {
            currentFrame = (currentFrame + 1) % frames.size();
            lastFrameTime = currentTimeMillis;
        }
    }

    public Bitmap getCurrentFrame() {
        if (frames.isEmpty()) return null;
        return frames.get(currentFrame);
    }

    // --- EKSTRA BİLGİLER ---
    public long getDurationMillis() {
        if (frames.isEmpty()) return 0;
        return (long) frames.size() * (1000L / fps);
    }

    public int getFPS() {
        return fps;
    }

    public void setFPS(int fps) {
        if (fps > 0) this.fps = fps;
    }
    
	public void saveResource(OutputStream stream) throws IOException {
		try (DataOutputStream out = new DataOutputStream(stream)) {
        	out.writeUTF("DIKEN_ANIM_" + me.ramazanenescik04.diken.DikenEngine.protocolVersion);
            out.writeInt(this.getFPS());
            out.writeInt(this.getFrameCount());

            for (int i = 0; i < this.getFrameCount(); i++) {
                Bitmap bmp = this.getFrame(i);
                byte[] data = bmp.toBytes("png"); // Bitmap sınıfında olmalı (BufferedImage -> PNG)
                out.writeInt(data.length);
                out.write(data);
            }
        }
	}

	public void save(File file) throws IOException {
        this.saveResource(new FileOutputStream(file));
    }

    // --- .bin'den animasyonu yükle ---
    public static Animation load(InputStream stream) throws IOException {
        try (DataInputStream in = new DataInputStream(stream)) {        	
        	String signature = in.readUTF();

        	if (!signature.startsWith("DIKEN_ANIM_")) {
        		throw new IOException("Bu dosya DikenEngine animasyon formatında değil!");
        	}
            int fps = in.readInt();
            int count = in.readInt();

            Animation anim = new Animation(fps);
            for (int i = 0; i < count; i++) {
                int len = in.readInt();
                byte[] data = new byte[len];
                in.readFully(data);
                Bitmap bmp = Bitmap.fromBytes(data); // PNG'den geri yükleme
                anim.addFrame(bmp);
            }
            return anim;
        }
    }
    
    public IResource clone() {
    	Animation clonedAnim = new Animation(fps);
    	for (int i = 0; i < this.getFrameCount(); i++) {
    		clonedAnim.addFrame(this.getFrame(i));
    	}
    	
    	clonedAnim.currentFrame = this.currentFrame;
    	clonedAnim.lastFrameTime = this.lastFrameTime;
    	
		return clonedAnim;
    }
    
    public void setCurrentFrame(int frameIndex) {
        if (frameIndex >= 0 && frameIndex < frames.size()) {
            currentFrame = frameIndex;
            lastFrameTime = System.currentTimeMillis(); // animasyonu hemen güncelle
        }
    }

	@Override
	public EnumResource getResourceType() {
		return EnumResource.ANIMATION;
	}
}
