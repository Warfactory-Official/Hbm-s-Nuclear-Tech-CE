package com.hbm.blocks.gas;

import com.hbm.blocks.ModBlocks;
import com.hbm.capability.HbmLivingProps;
import com.hbm.config.GeneralConfig;
import com.hbm.handler.ArmorUtil;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.lib.ForgeDirection;
import com.hbm.potion.HbmPotion;
import com.hbm.util.ArmorRegistry;
import com.hbm.util.ContaminationUtil;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

public class BlockGasMeltdown extends BlockGasBase {

    public BlockGasMeltdown(String s) {
        super(0.1F, 0.4F, 0.1F, s);
    }

    @Override
    public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entity) {

        if (!(entity instanceof EntityLivingBase entityLiving) || !GeneralConfig.enableMeltdownGas)
            return;

        ContaminationUtil.contaminate(entityLiving, ContaminationUtil.HazardType.RADIATION, ContaminationUtil.ContaminationType.CREATIVE, 0.5F);
        entityLiving.addPotionEffect(new PotionEffect(HbmPotion.radiation, 60 * 20, 2));

        if (ArmorRegistry.hasAllProtection(entityLiving, EntityEquipmentSlot.HEAD, ArmorRegistry.HazardClass.PARTICLE_FINE)) {
            ArmorUtil.damageGasMaskFilter(entityLiving, 1);
        } else {
            HbmLivingProps.incrementAsbestos(entityLiving, 5); // Mesothelioma can be developed as a result of exposure to radiation in the lungs
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(IBlockState stateIn, World world, BlockPos pos, Random rand) {
        super.randomDisplayTick(stateIn, world, pos, rand);
        world.spawnParticle(EnumParticleTypes.TOWN_AURA, pos.getX() + rand.nextFloat(), pos.getY() + rand.nextFloat(), pos.getZ() + rand.nextFloat(), 0.0D, 0.0D, 0.0D);
    }

    @Override
    public ForgeDirection getFirstDirection(World world, int x, int y, int z) {

        if (world.rand.nextInt(2) == 0)
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

            if(!GeneralConfig.enableMeltdownGas){
                world.setBlockToAir(pos);
                return;
            }

            ForgeDirection dir = ForgeDirection.getOrientation(rand.nextInt(6));

            if (rand.nextInt(7) == 0 && world.getBlockState(pos.add(dir.offsetX, dir.offsetY, dir.offsetZ)).getBlock() == Blocks.AIR) {
                world.setBlockState(pos.add(dir.offsetX, dir.offsetY, dir.offsetZ), ModBlocks.gas_radon_dense.getDefaultState());
            }

            if (world.canSeeSky(pos)) {
                ChunkRadiationManager.proxy.incrementRad(world, pos, 5F, 50F);
            }

            if (rand.nextInt(350) == 0) {
                world.setBlockToAir(pos);
                return;
            }
        }

        super.updateTick(world, pos, state, rand);
    }
}
