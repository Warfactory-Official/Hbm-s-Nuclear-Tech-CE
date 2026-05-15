package com.hbm.tileentity.machine;

import com.hbm.interfaces.AutoRegister;
import com.hbm.main.MainRegistry;
import com.hbm.particle.helper.HbmEffectNT;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@AutoRegister
public class TileEntityChimneyBrick extends TileEntityChimneyBase {

    @Override
    public void spawnParticles() {

        if(world.getTotalWorldTime() % 2 == 0) {
            NBTTagCompound fx = new NBTTagCompound();
            fx.setFloat("lift", 10F);
            fx.setFloat("base", 0.5F);
            fx.setFloat("max", 3F);
            fx.setInteger("life", 250 + world.rand.nextInt(50));
            fx.setInteger("color",0x404040);
            MainRegistry.proxy.effectNT(HbmEffectNT.Tower, pos.getX() + .5, pos.getY() + 12, pos.getZ() + .5, fx);
        }
    }

    @Override
    public double getPollutionMod() {
        return 0.25D; // no guys I'm not doing these shitty glyphids, too easy to deal with them in-game xd
        //return MobConfig.rampantMode ? MobConfig.rampantSmokeStackOverride : 0.25D;
    }

    AxisAlignedBB bb = null;

    @Override
    public AxisAlignedBB getRenderBoundingBox() {

        if(bb == null) {
            bb = new AxisAlignedBB(
                    pos.getX() - 1,
                    pos.getY(),
                    pos.getZ() - 1,
                    pos.getX() + 2,
                    pos.getY() + 13,
                    pos.getZ() + 2
            );
        }

        return bb;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }
}
