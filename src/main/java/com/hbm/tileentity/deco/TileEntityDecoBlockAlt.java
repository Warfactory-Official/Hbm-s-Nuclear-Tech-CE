package com.hbm.tileentity.deco;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;

import java.util.List;

@AutoRegister
public class TileEntityDecoBlockAlt extends TileEntity implements ITickable {
	
	@Override
	public void update() {
		if(world.getBlockState(pos).getBlock() != ModBlocks.statue_elb_f)
			return;
		int strength = 4;
		float f = strength;
        int i;
        int j;
        int k;
        double d5;
        double d6;
        double d7;
        double wat = 4*2;
        

        strength *= 2.0F;
        i = MathHelper.floor(pos.getX() - wat - 1.0D);
        j = MathHelper.floor(pos.getX() + wat + 1.0D);
        k = MathHelper.floor(pos.getY() - wat - 1.0D);
        int i2 = MathHelper.floor(pos.getY() + wat + 1.0D);
        int l = MathHelper.floor(pos.getZ() - wat - 1.0D);
        int j2 = MathHelper.floor(pos.getZ() + wat + 1.0D);
        List<Entity> list = this.world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(i, k, l, j, i2, j2));

        for (int i1 = 0; i1 < list.size(); ++i1)
        {
            Entity entity = (Entity)list.get(i1);
            double d4 = entity.getDistance(pos.getX(), pos.getY(), pos.getZ()) / 4;

            if (d4 <= 1.0D)
            {
                d5 = entity.posX - pos.getX();
                d6 = entity.posY + entity.getEyeHeight() - pos.getY();
                d7 = entity.posZ - pos.getZ();
                double d9 = MathHelper.sqrt(d5 * d5 + d6 * d6 + d7 * d7);
                if (d9 < wat)
                {
                	if(entity instanceof EntityPlayer) {
                		((EntityPlayer)entity).addPotionEffect(new PotionEffect(MobEffects.INSTANT_HEALTH, 5, 99));
                		((EntityPlayer)entity).addPotionEffect(new PotionEffect(MobEffects.SATURATION, 5, 99));
                    }
                }
            }
        }

        strength = (int)f;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

}
