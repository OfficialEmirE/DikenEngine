package me.ramazanenescik04.diken.resource;

import javax.sound.sampled.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * Represents the `SoundResource` type within the DikenEngine `resource` package.
 */
public class SoundResource implements IResource {

    private static final long serialVersionUID = 1L;

    private String id;
    private byte[] wavBytes;

    private float volume = 1.0f;   // 0..1
    private float pitch = 1.0f; // default
    private long maxPosition;
    private boolean loop = false;

    // runtime (serialize edilmez)
    private transient Clip clip;
    private transient boolean loaded;
    private transient Float originalSampleRate = null;

    public SoundResource() {
        // reflection için zorunlu
    }

    public SoundResource(String id, byte[] wavBytes) {
        this.id = id;
        this.wavBytes = wavBytes;
    }

    @Override
    public EnumResource getResourceType() {
        return EnumResource.SOUND;
    }

    // ---------------------------
    // Static factory: WAV -> SoundResource
    // ---------------------------
    public static SoundResource fromWav(Path wavPath, String id) throws IOException {
        byte[] bytes = Files.readAllBytes(wavPath);
        ensureLooksLikeWav(bytes, wavPath.toString());
        return new SoundResource(id, bytes);
    }

    public static SoundResource fromWav(File wavFile, String id) throws IOException {
        return fromWav(wavFile.toPath(), id);
    }

    public static SoundResource fromWavBytes(byte[] wavBytes, String id) {
        ensureLooksLikeWav(wavBytes, "byte[]");
        return new SoundResource(id, wavBytes);
    }
    
	public static SoundResource fromWav(InputStream inputStream, String id) throws IOException {
		if (inputStream == null) {
			throw new IllegalArgumentException("InputStream is null");
		}

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		byte[] data = new byte[8192];
		int nRead;

		while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
			buffer.write(data, 0, nRead);
		}

		byte[] wavBytes = buffer.toByteArray();
		
		ensureLooksLikeWav(wavBytes, "InputStream");

		return new SoundResource(id, wavBytes);
	}

    // ---------------------------
    // Playback API
    // ---------------------------
    public synchronized void play() throws LineUnavailableException, IOException, UnsupportedAudioFileException {
        if (!loaded || clip == null) reload();
        
        if (clip == null) return;
 
        if (clip.isRunning()) {
            clip.stop();
        }
        clip.setFramePosition(0);
        this.maxPosition = clip.getFrameLength();
        this.originalSampleRate = null;
        applyVolume(volume);
        applyPitch(pitch);

        if (loop) {
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        } else {
            clip.start();
        }
    }

    public synchronized void stop() {
        if (clip != null) {
            clip.stop();
            clip.setFramePosition(0);
        }
    }

    public synchronized boolean isPlaying() {
        return clip != null && clip.isRunning();
    }

    // ---------------------------
    // IResource serialization
    // ---------------------------
    @Override
    public void saveResource(DataOutputStream out) throws IOException {
        writeString(out, id);

        out.writeFloat(volume);
        out.writeBoolean(loop);

        if (wavBytes == null) {
            out.writeInt(-1);
        } else {
            out.writeInt(wavBytes.length);
            out.write(wavBytes);
        }
    }

    @Override
    public void loadResource(DataInputStream in) throws IOException {
        this.id = readString(in);

        this.volume = in.readFloat();
        this.loop = in.readBoolean();

        int len = in.readInt();
        if (len < 0) {
            this.wavBytes = null;
        } else {
            this.wavBytes = new byte[len];
            in.readFully(this.wavBytes);
        }

        // runtime state reset
        this.loaded = false;
        safeCloseClip();
    }

    // ---------------------------
    // Lifecycle hooks
    // ---------------------------
    @Override
    public synchronized void reload() {
        safeCloseClip();

        if (wavBytes == null || wavBytes.length == 0) {
            loaded = false;
            return;
        }

        try (ByteArrayInputStream bais = new ByteArrayInputStream(wavBytes);
             BufferedInputStream bis = new BufferedInputStream(bais);
             AudioInputStream ais = AudioSystem.getAudioInputStream(bis)) {

            Clip newClip = AudioSystem.getClip();
            newClip.open(ais);

            this.clip = newClip;
            this.loaded = true;

            this.maxPosition = clip.getMicrosecondLength();
            this.originalSampleRate = null;
            applyVolume(volume);
            applyPitch(pitch);
            
        } catch (Exception e) {
            this.clip = null;
            this.loaded = false;
            throw new RuntimeException("SoundResource reload failed (id=" + id + "): " + e.getMessage(), e);
        }
    }

    @Override
    public synchronized void disponse() {
        safeCloseClip();
        loaded = false;
    }

    private void safeCloseClip() {
        if (clip != null) {
            try {
                clip.stop();
            } catch (Exception ignored) {}
            try {
                clip.close();
            } catch (Exception ignored) {}
            clip = null;
        }
    }

    // ---------------------------
    // Volume helper
    // ---------------------------
    private void applyVolume(float v01) {
        if (clip == null) return;

        float v = Math.max(0f, Math.min(1f, v01));

        if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);

            if (v == 0f) {
                gain.setValue(gain.getMinimum());
            } else {
                float dB = (float) (20.0 * Math.log10(v));
                dB = Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), dB));
                gain.setValue(dB);
            }
        }
    }
    
    // ---------------------------
    // Position helper
    // ---------------------------
    private void applyPosition(long position) {
        if (clip == null) return;

        position = Math.max(0, Math.min(clip.getMicrosecondLength(), position));
        
        clip.setMicrosecondPosition(position);
    }
    
	// ---------------------------
    // Volume helper
    // ---------------------------
    
    private void applyPitch(float v01) {
        if (clip == null) return;

        float v = Math.max(0f, v01);

        if (clip.isControlSupported(FloatControl.Type.SAMPLE_RATE)) {
            FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.SAMPLE_RATE);
            if (originalSampleRate == null) {
            	originalSampleRate = gain.getValue();
            }

            float dB = (float) originalSampleRate * v;
            dB = Math.max(gain.getMinimum(), Math.min(gain.getMaximum(), dB));
            gain.setValue(dB);
        }
    }

    // ---------------------------
    // Clone
    // ---------------------------
    @Override
    public SoundResource clone() {
        try {
            SoundResource c = (SoundResource) super.clone();
            c.wavBytes = (this.wavBytes == null) ? null : Arrays.copyOf(this.wavBytes, this.wavBytes.length);
            c.clip = null;
            c.loaded = false;
            return c;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }

    // ---------------------------
    // Binary String helpers
    // ---------------------------
    private static void writeString(DataOutputStream out, String s) throws IOException {
        if (s == null) {
            out.writeInt(-1);
            return;
        }
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        out.writeInt(bytes.length);
        out.write(bytes);
    }

    private static String readString(DataInputStream in) throws IOException {
        int len = in.readInt();
        if (len < 0) return null;
        byte[] bytes = new byte[len];
        in.readFully(bytes);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    // ---------------------------
    // Quick WAV sanity check (RIFF/WAVE)
    // ---------------------------
    private static void ensureLooksLikeWav(byte[] bytes, String name) {
        if (bytes == null || bytes.length < 12) {
            throw new IllegalArgumentException("Invalid WAV (" + name + "): too small");
        }
        boolean riff =
                bytes[0] == 'R' && bytes[1] == 'I' && bytes[2] == 'F' && bytes[3] == 'F';
        boolean wave =
                bytes[8] == 'W' && bytes[9] == 'A' && bytes[10] == 'V' && bytes[11] == 'E';
        if (!riff || !wave) {
            throw new IllegalArgumentException("Invalid WAV (" + name + "): missing RIFF/WAVE header");
        }
    }

    // ---------------------------
    // Getters/Setters
    // ---------------------------
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public byte[] getWavBytes() { return wavBytes; }
    public void setWavBytes(byte[] wavBytes) {
        this.wavBytes = wavBytes;
        this.loaded = false;
        safeCloseClip();
    }

    public float getVolume() { return volume; }
    public void setVolume(float volume) {
        this.volume = volume;
        applyVolume(volume);
    }
    
    public synchronized long getPosition() {
    	if (clip != null) {
    		return clip.getMicrosecondPosition();
    	}
    	
    	return 0;
    }
    public void setPosition(long position) {
        applyPosition(position);
    }
    
    public float getPitch() { return pitch; }
    public void setPitch(float pitch) {
        this.pitch = pitch;
        applyPitch(pitch);
    }
    
    public long getMaxPosition() { return this.maxPosition; }

    public boolean isLoop() { return loop; }
    public void setLoop(boolean loop) { this.loop = loop; }

    public boolean isLoaded() { return loaded; }
}
