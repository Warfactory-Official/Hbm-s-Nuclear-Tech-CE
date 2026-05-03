package com.hbm.blocks.generic;

import com.hbm.api.fluid.IFluidStandardSender;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.TileEntityLoadedBase;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class BlockFissure extends BlockContainer {

    public static final PropertyBool CRATER = PropertyBool.create("crater");

    public BlockFissure(Material material, String s) {
        super(material);
        this.setTranslationKey(s);
        this.setRegistryName(s);
        this.setBlockUnbreakable();
        this.setResistance(1_000_000);
        this.setTickRandomly(true);
        this.setDefaultState(this.blockState.getBaseState().withProperty(CRATER, false));
        ModBlocks.ALL_BLOCKS.add(this);
    }

    @Override
    public void updateTick(World world, BlockPos pos, @NotNull IBlockState state, @NotNull Random rand) {
        if(world.getBlockState(pos.up()).getBlock().isReplaceable(world, pos.up())) {
            Block lava = state.getValue(CRATER) ? ModBlocks.rad_lava_block : ModBlocks.volcanic_lava_block;
            world.setBlockState(pos.up(), lava.getDefaultState());
        }
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, CRATER);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(CRATER) ? 1 : 0;
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(CRATER, meta != 0);
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public @NotNull BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }

    @Override
    public TileEntity createNewTileEntity(@NotNull World p_149915_1_, int p_149915_2_) {
        return new TileEntityFissure();
    }

    @AutoRegister
    public static class TileEntityFissure extends TileEntityLoadedBase implements ITickable, IFluidStandardSender {

        public FluidTankNTM lava = new FluidTankNTM(Fluids.LAVA, 1_000);

        @Override
        public void update() {

            if(!world.isRemote) {
                lava.setFill(1_000);
                this.sendFluid(lava, world, pos.getX(), pos.getY() + 1, pos.getZ(), ForgeDirection.UP);
            }
        }

        @Override
        public boolean canConnect(FluidType type, ForgeDirection dir) {
            return dir == ForgeDirection.DOWN && type == Fluids.LAVA;
        }

        @Override public FluidTankNTM[] getAllTanks() { return new FluidTankNTM[] {lava}; }
        @Override public FluidTankNTM[] getSendingTanks() { return new FluidTankNTM[] {lava}; }
    }
}
