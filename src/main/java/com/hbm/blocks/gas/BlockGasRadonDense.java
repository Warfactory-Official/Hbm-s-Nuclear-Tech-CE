package com.hbm.blocks.gas;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import com.hbm.handler.ArmorUtil;
import com.hbm.lib.ForgeDirection;
import com.hbm.util.ArmorRegistry;
import com.hbm.util.ArmorRegistry.HazardClass;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class BlockGasRadonDense extends BlockGasBase {

    public BlockGasRadonDense(String s) {
        super(0.1F, 0.5F, 0.1F, s);
    }

    @Override
    public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {
        if (!(entity instanceof EntityLivingBase entityLiving) || !GeneralConfig.enableRadon)
            return;

        if (ArmorRegistry.hasProtection(entityLiving, EntityEquipmentSlot.HEAD, HazardClass.PARTICLE_FINE)) {
            ArmorUtil.damageGasMaskFilter(entityLiving, 2);
            ContaminationUtil.contaminate(entityLiving, HazardType.RADIATION, ContaminationType.CREATIVE, 0.5F);
        } else {
            ContaminationUtil.contaminate(entityLiving, HazardType.RADIATION, ContaminationType.RAD_BYPASS, 0.5F);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
        super.randomDisplayTick(stateIn, worldIn, pos, rand);
        worldIn.spawnParticle(EnumParticleTypes.TOWN_AURA, pos.getX() + rand.nextFloat(), pos.getY() + rand.nextFloat(), pos.getZ() + rand.nextFloat(), 0.0D, 0.0D, 0.0D);
    }

    @Override
    public ForgeDirection getFirstDirection(World world, int x, int y, int z) {

        if (world.rand.nextInt(5) == 0)
            return ForgeDirection.UP;

        return ForgeDirection.DOWN;
    }

    @Override
    public ForgeDirection getSecondDirection(World world, int x, int y, int z) {
        return this.randomHorizontal(world);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        if (!world.isRemote) {

            if (!GeneralConfig.enableRadon) {
                world.setBlockToAir(pos);
                return;
            }

            if (rand.nextInt(10) == 0) {
                if (world.getBlockState(pos.down()).getBlock() == Blocks.GRASS)
                    world.setBlockState(pos.down(), ModBlocks.waste_earth.getDefaultState());
            }

            if (rand.nextInt(20) == 0) {
                world.setBlockToAir(pos);

                if (ModBlocks.fallout.canPlaceBlockAt(world, pos)) {
                    world.setBlockState(pos, ModBlocks.fallout.getDefaultState());
                }

                return;
            }
        }

        super.updateTick(world, pos, state, rand);
    }

}