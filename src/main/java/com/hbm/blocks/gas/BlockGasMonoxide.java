package com.hbm.blocks.gas;

import com.hbm.config.GeneralConfig;
import com.hbm.handler.ArmorUtil;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.ModDamageSource;
import com.hbm.util.ArmorRegistry;
import com.hbm.util.ArmorRegistry.HazardClass;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.Random;

public class BlockGasMonoxide extends BlockGasBase {

    public BlockGasMonoxide(String s) {
        super(0.1F, 0.1F, 0.1F, s);
    }

    @Override
    public void onEntityCollision(World worldIn, BlockPos pos, IBlockState state, Entity entity) {
        if (!(entity instanceof EntityLivingBase entityLiving) || !GeneralConfig.enableCarbonMonoxide)
            return;

        if (ArmorRegistry.hasAllProtection(entityLiving, EntityEquipmentSlot.HEAD, HazardClass.GAS_MONOXIDE))
            ArmorUtil.damageGasMaskFilter(entityLiving, 1);
        else
            entityLiving.attackEntityFrom(ModDamageSource.monoxide, 1);
    }

    @Override
    public ForgeDirection getFirstDirection(World world, int x, int y, int z) {
        return ForgeDirection.DOWN;
    }

    @Override
    public ForgeDirection getSecondDirection(World world, int x, int y, int z) {
        return this.randomHorizontal(world);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {

        if(world.isRemote){
            return;
        }

        if (rand.nextInt(100) == 0 || !GeneralConfig.enableCarbonMonoxide) {
            world.setBlockToAir(pos);
            return;
        }

        super.updateTick(world, pos, state, rand);
    }
}