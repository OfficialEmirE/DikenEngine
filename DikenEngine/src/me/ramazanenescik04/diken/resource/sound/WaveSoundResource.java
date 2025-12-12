package me.ramazanenescik04.diken.resource.sound;

import org.lwjgl.openal.AL10;

import me.ramazanenescik04.diken.resource.IResource;

public class WaveSoundResource implements SoundResource {
	private static final long serialVersionUID = 1L;
	
	private int sourceID;
	
	public WaveSoundResource(int sourceID) {
		this.sourceID = sourceID;
	}
	
	public void play() {
		AL10.alSourcePlay(sourceID);
	}
	
	public void stop() {
		AL10.alSourceStop(sourceID);
	}
	
	public void pause() {
		AL10.alSourcePause(sourceID);
	}
	
	public void rewind() {
		AL10.alSourceRewind(sourceID);
	}
	
	public void setPosition(float x, float y) {
		AL10.alSource3f(sourceID, AL10.AL_POSITION, x, y, 0);
	}
	
	public void setVolume(float volume) {
		AL10.alSourcef(sourceID, AL10.AL_GAIN, volume);
	}
	
	public void setPitch(float pitch) {
		AL10.alSourcef(sourceID, AL10.AL_PITCH, pitch);
	}
	
	public void setPosition(float x, float y, float z) {
		AL10.alSource3f(sourceID, AL10.AL_POSITION, x, y, z);
	}
	
	public void setLooping(boolean looping) {
		AL10.alSourcei(sourceID, AL10.AL_LOOPING, looping ? AL10.AL_TRUE : AL10.AL_FALSE);
	}
	
	public void setDistance(float distance) {
		AL10.alSourcef(sourceID, AL10.AL_REFERENCE_DISTANCE, distance);
	}
	
	public void setMaxDistance(float maxDistance) {
		AL10.alSourcef(sourceID, AL10.AL_MAX_DISTANCE, maxDistance);
	}
	
	public void destorySound() {
		AL10.alDeleteSources(sourceID);
	}

	// Bir ses kaynağının durumunu kontrol etme
	public boolean isPlaying() {
	    return AL10.alGetSourcei(sourceID, AL10.AL_SOURCE_STATE) == AL10.AL_PLAYING;
	}

	// Bir ses kaynağının döngüde olup olmadığını kontrol etme
	public boolean isLooping() {
	    return AL10.alGetSourcei(sourceID, AL10.AL_LOOPING) == AL10.AL_TRUE;
	}
	
	public IResource clone() {
		return new WaveSoundResource(sourceID);
	}

}
