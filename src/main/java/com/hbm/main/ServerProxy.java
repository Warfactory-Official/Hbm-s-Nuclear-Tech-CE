package com.hbm.main;

import com.hbm.handler.HbmKeybinds.EnumKeybind;
import com.hbm.particle.helper.HbmEffectNT;
import com.hbm.saveddata.TomSaveData;
import com.hbm.sound.AudioWrapper;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ServerProxy {
	public void registerRenderInfo() {}
	public void registerGunCfg() {}
	
	public void particleControl(double x, double y, double z, int type) {}

	public void spawnSpark(World world, double posX, double posY, double posZ, Vec3d payload) {}

    /**
     * @deprecated Use {@link #effectNT(HbmEffectNT, double, double, double, NBTTagCompound)} or {@link #effectNT(HbmEffectNT, double, double, double)} whenever possible
     */
    @Deprecated
    public void effectNT(NBTTagCompound data) {}

    public void effectNT(HbmEffectNT type, double x, double y, double z, @Nullable NBTTagCompound data) {}
    public void effectNT(HbmEffectNT type, double x, double y, double z) { effectNT(type, x, y, z, null); }

	public void registerSpecialItemRenderers(IRegistry<ModelResourceLocation, IBakedModel> reg) {}

	public AudioWrapper getLoopedSound(SoundEvent sound, SoundCategory cat, float x, float y, float z, float volume, float range, float pitch, int keepAlive) { return null; }
	public AudioWrapper getLoopedSound(SoundEvent sound, SoundCategory cat, float x, float y, float z, float volume, float range, float pitch) { return null; }

	public AudioWrapper getLoopedSound(SoundEvent sound, SoundCategory cat, float x, float y, float z, float volume, float pitch) { return null; }
	
	public AudioWrapper getLoopedSoundStartStop(World world, SoundEvent sound, SoundEvent start, SoundEvent stop, SoundCategory cat, float x, float y, float z, float volume, float pitch){return null;}
	
	public void preInit(FMLPreInitializationEvent evt) {}

	public void init(FMLInitializationEvent evt) {}

	public void checkGLCaps() {}
	
	public File getDataDir(){
		return FMLCommonHandler.instance().getMinecraftServerInstance().getDataDirectory();
	}
	
	public void postInit(FMLPostInitializationEvent e) {}

	public boolean getIsKeyPressed(EnumKeybind key) { return false; }

	public EntityPlayer me() { return null; }
	
	public float partialTicks() { return 1; }

	public void displayTooltip(String msg) {}

	public void displayTooltipLegacy(String msg, int id) {
		displayTooltipLegacy(msg, 1000, id);
	}

	public void displayTooltipLegacy(String msg, int time, int id) {}
	
	public void setRecoil(float rec) {}

	public void playSoundClient(double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch) {}
	
	public boolean isVanished(Entity e) { return false; }

	public List<ItemStack> getSubItems(ItemStack stack) {
		List<ItemStack> list = new ArrayList<>();
		list.add(stack);
		return list;
	}

    public float getImpactDust(World world) {
        return TomSaveData.forWorld(world).dust;
    }

    public float getImpactFire(World world) {
        return TomSaveData.forWorld(world).fire;
    }

    public boolean getImpact(World world) {
        return TomSaveData.forWorld(world).impact;
    }

	public int getStackColor(ItemStack stack, boolean amplify) { return 0x000000; }
    public void onLoadComplete(FMLLoadCompleteEvent event){}

}