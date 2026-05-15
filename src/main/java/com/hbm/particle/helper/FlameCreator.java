package com.hbm.particle.helper;

import com.hbm.main.MainRegistry;
import com.hbm.particle.ParticleFlamethrower;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class FlameCreator implements IParticleCreator {

  public static int META_FIRE = 0;
  public static int META_BALEFIRE = 1;
  public static int META_DIGAMMA = 2;

  public static void composeEffect(World world, double x, double y, double z, int meta) {
    NBTTagCompound data = new NBTTagCompound();
    data.setInteger("meta", meta);
    IParticleCreator.sendPacket(world, HbmEffectNT.Flamethrower, x, y, z, 50, data);
  }

  public static void composeEffectClient(World world, double x, double y, double z, int meta) {
    NBTTagCompound data = new NBTTagCompound();
    data.setInteger("meta", meta);
    MainRegistry.proxy.effectNT(HbmEffectNT.Flamethrower, x, y, z, data);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void makeParticle(World world, EntityPlayer player, TextureManager texman, Random rand, double x, double y, double z, NBTTagCompound data) {
    ParticleFlamethrower particle = new ParticleFlamethrower(world, x, y, z, data.getInteger("meta"));
    Minecraft.getMinecraft().effectRenderer.addEffect(particle);
  }
}
