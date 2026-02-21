package com.hbm.blocks.fluid;

import com.hbm.blocks.ModBlocks;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.lib.ModDamageSource;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidFinite;
import net.minecraftforge.fluids.Fluid;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class CoriumFinite extends BlockFluidFinite implements IFluidFog {

    private final Random rand = new Random();

    public CoriumFinite(Fluid fluid, Material material, String s) {
        super(fluid, material);
        this.setTranslationKey(s);
        this.setRegistryName(s);
        this.tickRate = 30;
        this.setTickRandomly(true);
        displacements.put(this, false);

        ModBlocks.ALL_BLOCKS.add(this);
    }

    @Override
    @NotNull
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta,
                                            EntityLivingBase placer, EnumHand hand) {
        return getDefaultState().withProperty(LEVEL, Math.max(0, quantaPerBlock - 1));
    }

    @Override
    public boolean canDisplace(@NotNull IBlockAccess world, @NotNull BlockPos pos) {
        IBlockState state = world.getBlockState(pos);
        Material mat = state.getMaterial();
        Block block = state.getBlock();
        if (mat.isLiquid()) return true;
        float resistance = block.getExplosionResistance(null);
        float scaled = (float) (Math.sqrt(resistance) * 3.0);
        if (scaled < 1F) return true;
        return rand.nextInt(Math.max(1, (int) scaled)) == 0;
    }

    @Override
    public boolean displaceIfPossible(@NotNull World world, @NotNull BlockPos pos) {
        if (world.getBlockState(pos).getMaterial().isLiquid()) {
            return false;
        }
        return canDisplace(world, pos);
    }

    @Override
    public void onEntityCollision(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull Entity entity) {
        if (entity instanceof EntityPlayerMP playerMP && (playerMP.isSpectator() || playerMP.isCreative())) return;
        entity.setInWeb();
        entity.setFire(3);
        // mlbv: 1.7 has a very low damage here(2F radiation damage and 1F contamination), looks weird, so I used the value in the old CoriumBlock
        entity.attackEntityFrom(ModDamageSource.radiation, 200F);
        if (entity instanceof EntityLivingBase) {
            ContaminationUtil.contaminate((EntityLivingBase) entity, HazardType.RADIATION, ContaminationType.CREATIVE, 500F);
        }
    }

    @Override
    public void updateTick(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull Random random) {
        super.updateTick(world, pos, state, random);
        ChunkRadiationManager.proxy.incrementRad(world, pos, 50);
        if (!world.isRemote && random.nextInt(10) == 0 && world.getBlockState(pos.down()).getBlock() != this) {
            if (random.nextInt(3) == 0) {
                world.setBlockState(pos, ModBlocks.block_corium.getDefaultState());
            } else {
                world.setBlockState(pos, ModBlocks.block_corium_cobble.getDefaultState());
            }
        }
    }

    @Override
    public boolean isReplaceable(@NotNull IBlockAccess world, @NotNull BlockPos pos) {
        return false;
    }

    @Override
    public float getFogDensity() {
        return 2.0F;
    }

    @Override
    public int getFogColor() {
        return 0x343435;
    }
}
