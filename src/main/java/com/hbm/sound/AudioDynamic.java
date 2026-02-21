package com.hbm.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class AudioDynamic extends MovingSound {

	public float maxVolume = 1;
	public float range;
	public float intendedVolume;
	public int keepAlive;
	public int timeSinceKA;
	public boolean shouldExpire = false;
	private final boolean nonLegacy;
	// shitty addition that should make looped sounds on tools and guns work right
	// position updates happen automatically and if the parent is the client player, volume is always on max
	public Entity parentEntity = null;

	protected AudioDynamic(SoundEvent loc, SoundCategory cat, boolean useNewSystem) {
		super(loc, cat);
		this.repeat = true;
		this.attenuationType = ISound.AttenuationType.NONE;
		this.intendedVolume = 10;
        this.range = 10;
		this.nonLegacy = useNewSystem;
	}
	
	public void setPosition(float x, float y, float z) {
		this.xPosF = x;
		this.yPosF = y;
		this.zPosF = z;
	}

	public void setAttenuation(ISound.AttenuationType type){
		this.attenuationType = type;
		volume = intendedVolume;
	}

	@Override
	public void update() {
		EntityPlayerSP player = Minecraft.getMinecraft().player;
		float f;

		if (parentEntity != null && player != parentEntity) {
			this.setPosition((float) parentEntity.posX, (float) parentEntity.posY, (float) parentEntity.posZ);
		}
		// only adjust volume over distance if the sound isn't attached to this entity
		if (nonLegacy) {
			if (player != null && player != parentEntity) {
				f = (float) Math.sqrt(Math.pow(xPosF - player.posX, 2)
						+ Math.pow(yPosF - player.posY, 2)
						+ Math.pow(zPosF - player.posZ, 2));
				volume = func(f);
			} else {
				// shitty hack that prevents stereo weirdness when using 0 0 0
				if (player == parentEntity) {
					this.setPosition((float) parentEntity.posX, (float) parentEntity.posY + 10, (float) parentEntity.posZ);
				}
				volume = maxVolume;
			}

			if (this.shouldExpire) {
				if (this.timeSinceKA > this.keepAlive) {
					this.stop();
				}
				this.timeSinceKA++;
			}
		} else {
			if (player != null && player != parentEntity) {
				f = (float) Math.sqrt(Math.pow(xPosF - player.posX, 2)
						+ Math.pow(yPosF - player.posY, 2)
						+ Math.pow(zPosF - player.posZ, 2));

				if (attenuationType == ISound.AttenuationType.LINEAR) {
					volume = func(f);
				} else {
					volume = func(f, intendedVolume);
				}
			} else {
				if (player == parentEntity) {
					this.setPosition((float) parentEntity.posX, (float) parentEntity.posY + 10, (float) parentEntity.posZ);
				}
				volume = intendedVolume;
			}
		}
	}

	
	public void start() {
		Minecraft.getMinecraft().getSoundHandler().playSound(this);
	}
	
	public void stop() {
		Minecraft.getMinecraft().getSoundHandler().stopSound(this);
	}
	
	public void setVolume(float volume) {
		this.maxVolume = volume;
	}

	public void setRange(float range) {
		this.range = range;
	}
	
	public void setPitch(float pitch) {
		this.pitch = pitch;
	}
	public void setKeepAlive(int keepAlive) {
		this.keepAlive = keepAlive;
		this.shouldExpire = true;
	}

	public void keepAlive() {
		this.timeSinceKA = 0;
	}
	
	public float func(float f, float v) {
		return (f / v) * -2 + 2;
	}

	public float func(float dist) {
		return (dist / range) * -maxVolume + maxVolume;
	}

	public boolean isPlaying() {
		return Minecraft.getMinecraft().getSoundHandler().isSoundPlaying(this);
	}
}
