package com.hbm.sound;

import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class AudioWrapperClient extends AudioWrapper {

	AudioDynamic sound;
	// Th3_Sl1ze: this boolean is a temporary solution, should just move all sounds to PROPER getLoopedSound immediately
	public AudioWrapperClient(SoundEvent source, SoundCategory cat, boolean useNewSystem) {
		if(source != null)
			sound = new AudioDynamic(source, cat, useNewSystem);
	}

	@Override
	public void setKeepAlive(int keepAlive) {
		if(sound != null)
			sound.setKeepAlive(keepAlive);
	}

	@Override
	public void keepAlive() {
		if(sound != null)
			sound.keepAlive();
	}

	@Override
	public void updatePosition(float x, float y, float z) {
		if(sound != null)
			sound.setPosition(x, y, z);
	}

	@Override
	public void updateVolume(float volume) {
		if(sound != null)
			sound.setVolume(volume);
	}

	@Override
	public void updateRange(float range) {
		if(sound != null)
			sound.setRange(range);
	}

	@Override
	public float getVolume() {
		if(sound != null)
			return sound.getVolume();
		return 1;
	}

	@Override
	public float getPitch() {
		if(sound != null)
			return sound.getPitch();
		return 1;
	}

	@Override
	public void startSound() {
		if(sound != null)
			sound.start();
	}

	@Override
	public void stopSound() {
		if(sound != null) {
			sound.stop();
			sound.setKeepAlive(0);
		}
	}

	@Override
	public boolean isPlaying() {
		if(sound != null)
			return sound.isPlaying();
		return false;
	}
}
