package me.ramazanenescik04.diken.resource;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL10;

public class SoundResource implements IResource {
	private static final long serialVersionUID = -845497349641898425L;
	private static transient List<SoundResource> loadedSounds = new ArrayList<>();
	
	// Bunlar dosyaya kaydedilecek ham veriler
    private byte[] rawAudioData;
    private int sampleRate;
    private int format; // AL_FORMAT_MONO16, AL_FORMAT_STEREO16 vb.

    // transient: Bu değişkenler kaydedilmez, her açılışta sıfırdan oluşur
    private transient int bufferId = -1;
    private transient int sourceId = -1;
    
    public SoundResource() {}
    
    private void setFormat(int format2) {
    	format = format2;
	}

	private void setSampleRate(int sampleRate2) {
		sampleRate = sampleRate2;
	}

	private void setRawData(byte[] rawAudioData2) {
		rawAudioData = rawAudioData2;
	}
	
	public void play() {
		if (sourceId != -1) {
            AL10.alSourcePlay(sourceId);
        }
	}
	
	public void stop() {
		if (sourceId != -1)
			AL10.alSourceStop(sourceId);
	}
	
	public void pause() {
		if (sourceId != -1)
			AL10.alSourcePause(sourceId);
	}
	
	public void rewind() {
		if (sourceId != -1)
			AL10.alSourceRewind(sourceId);
	}
	
	public void setPosition(float x, float y) {
		if (sourceId != -1)
			AL10.alSource3f(sourceId, AL10.AL_POSITION, x, y, 0);
	}
	
	public void setVolume(float volume) {
		if (sourceId != -1)
			AL10.alSourcef(sourceId, AL10.AL_GAIN, volume);
	}
	
	public void setPitch(float pitch) {
		if (sourceId != -1)
			AL10.alSourcef(sourceId, AL10.AL_PITCH, pitch);
	}
	
	public void setPosition(float x, float y, float z) {
		if (sourceId != -1)
			AL10.alSource3f(sourceId, AL10.AL_POSITION, x, y, z);
	}
	
	public void setLooping(boolean looping) {
		if (sourceId != -1)
			AL10.alSourcei(sourceId, AL10.AL_LOOPING, looping ? AL10.AL_TRUE : AL10.AL_FALSE);
	}
	
	public void setDistance(float distance) {
		if (sourceId != -1)
			AL10.alSourcef(sourceId, AL10.AL_REFERENCE_DISTANCE, distance);
	}
	
	public void setMaxDistance(float maxDistance) {
		if (sourceId != -1)
			AL10.alSourcef(sourceId, AL10.AL_MAX_DISTANCE, maxDistance);
	}
	
	public void dispose() {
	    if (sourceId != -1) {
	        AL10.alSourceStop(sourceId); // Önce durdur
	        AL10.alDeleteSources(sourceId);
	        sourceId = -1;
	    }
	    if (bufferId != -1) {
	        AL10.alDeleteBuffers(bufferId);
	        bufferId = -1;
	    }
	}

	// Bir ses kaynağının durumunu kontrol etme
	public boolean isPlaying() {
	    return AL10.alGetSourcei(sourceId, AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING;
	}

	// Bir ses kaynağının döngüde olup olmadığını kontrol etme
	public boolean isLooping() {
	    return AL10.alGetSourcei(sourceId, AL10.AL_LOOPING) == AL10.AL_TRUE;
	}

	public void reloadInOpenAL() {
		this.bufferId = AL10.alGenBuffers();
        
        // 2. Ham veriyi Byte Buffer'a çevir (LWJGL genellikle ByteBuffer ister)
        ByteBuffer dataBuffer = BufferUtils.createByteBuffer(rawAudioData.length);
        dataBuffer.put(rawAudioData);
        dataBuffer.flip();

        // 3. Veriyi OpenAL Buffer'ına yükle
        AL10.alBufferData(bufferId, format, dataBuffer, sampleRate);

        // 4. Sesi çalmak için bir Source (Kaynak) oluştur
        this.sourceId = AL10.alGenSources();
        AL10.alSourcei(sourceId, AL10.AL_BUFFER, bufferId);
        
        if (!loadedSounds.contains(this)) {
            loadedSounds.add(this);
        }
        
        System.out.println("OpenAL Kaynağı Yeniden Yüklendi. Buffer ID: " + bufferId);
	}
	
	@Override
	public EnumResource getResourceType() {
		return EnumResource.SOUND;
	}
	
	public static void destroySounds() {
		loadedSounds.forEach(IResource::disponse);
		loadedSounds.clear(); // Listeyi boşalt
	}
	
	public static void unregisterSound(SoundResource sound) {
	    if (sound != null) {
	        sound.dispose(); // Donanım tarafını sil
	        loadedSounds.remove(sound); // Listeden çıkar
	    }
	}
	
	public static IResource loadSound(byte[] rawAudioData, int sampleRate, int format) {
	    SoundResource sound = new SoundResource();
	    
	    // Verileri nesneye ata (Serialization için saklıyoruz)
	    sound.setRawData(rawAudioData);
	    sound.setSampleRate(sampleRate);
	    sound.setFormat(format);
	    
	    // OpenAL tarafında buffer ve source oluştur
	    sound.reloadInOpenAL(); 
	    
	    return sound;
	}

	public static IResource loadSound(InputStream stream) {
	    try {
	        // Java'nın standart ses kütüphanesi ile WAV dosyasını oku
	        AudioInputStream ais = AudioSystem.getAudioInputStream(new BufferedInputStream(stream));
	        AudioFormat audioFormat = ais.getFormat();
	        
	        // Formata göre OpenAL tipini belirle (Mono/Stereo)
	        int channels = audioFormat.getChannels();
	        int bitDepth = audioFormat.getSampleSizeInBits();
	        int alFormat = -1;

	        if (channels == 1) {
	            alFormat = (bitDepth == 8) ? AL10.AL_FORMAT_MONO8 : AL10.AL_FORMAT_MONO16;
	        } else if (channels == 2) {
	            alFormat = (bitDepth == 8) ? AL10.AL_FORMAT_STEREO8 : AL10.AL_FORMAT_STEREO16;
	        }

	        // Veriyi byte array'e dönüştür
	        byte[] data = ais.readAllBytes();
	        int sampleRate = (int) audioFormat.getSampleRate();
	        
	        ais.close();
	        
	        // Yukarıdaki metodunu çağırarak oluştur
	        return loadSound(data, sampleRate, alFormat);

	    } catch (Exception e) {
	        e.printStackTrace();
	        return null;
	    }
	}

	@Override
	public void saveResource(DataOutputStream out) throws IOException {
		int rawAudioDataLenght = this.rawAudioData.length;
		
		out.writeInt(rawAudioDataLenght);
		out.write(this.rawAudioData);
		
		out.writeInt(this.sampleRate);
		out.writeInt(this.format);
	}

	@Override
	public void loadResource(DataInputStream in) throws IOException {
		int rawAudioDataLenght = in.readInt();
		byte[] data = new byte[rawAudioDataLenght];
		in.readFully(data);
		
		int sampleRate = in.readInt();
		int format = in.readInt();
		
		setRawData(data);
		setSampleRate(sampleRate);
		setFormat(format);
		
		reloadInOpenAL();
	}
}
