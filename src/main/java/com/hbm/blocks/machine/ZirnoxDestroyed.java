package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.items.ModItems;
import com.hbm.lib.ForgeDirection;
import com.hbm.main.MainRegistry;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityZirnoxDestroyed;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class ZirnoxDestroyed extends BlockDummyable {

    public ZirnoxDestroyed(Material mat, String s) {
        super(mat, s);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {

        if(meta >= 12)
            return new TileEntityZirnoxDestroyed();
        if(meta >= 6)
            return new TileEntityProxyCombo(false, true, true);

        return null;
    }

    // mlbv: 1.7 have sides here fucked up as well, if I were Bob I would've had all this guarded by a !world.isRemote
    // and have the TileEntityZirnoxDestroyed#onFire synced to clients via the TE instead of being set(and overridden) here
    // but, to preserve the behavior, I am only guarding the world.setBlockState calls
    @Override
    public void updateTick(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull Random rand) {
        BlockPos posUp = pos.up();
        Block block = world.getBlockState(posUp).getBlock();

        if (block == Blocks.AIR) {
            if (!world.isRemote && GeneralConfig.enableMeltdownGas && rand.nextInt(10) == 0)
                world.setBlockState(posUp, ModBlocks.gas_meltdown.getDefaultState());

        } else if (block == ModBlocks.foam_layer || block == ModBlocks.block_foam) {
            if (rand.nextInt(25) == 0 && findCoreTE(world, pos) instanceof TileEntityZirnoxDestroyed zirnoxDestroyed) {
                zirnoxDestroyed.onFire = false;
            }
        }

        if (!world.isRemote && GeneralConfig.enableMeltdownGas && rand.nextInt(10) == 0 && world.getBlockState(posUp).getBlock() == Blocks.AIR)
            world.setBlockState(posUp, ModBlocks.gas_meltdown.getDefaultState());
        super.updateTick(world, pos, state, rand);
    }

    @Override
    public int tickRate(World world) {
        return 100 + world.rand.nextInt(20);
    }

    @Override
    public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
        super.onBlockAdded(world, pos, state);

        if(!world.isRemote) {
            if(world.rand.nextInt(4) == 0) {
                NBTTagCompound data = new NBTTagCompound();
                data.setString("type", "rbmkflame");
                data.setInteger("maxAge", 90);
                PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, pos.getX() + 0.25 + world.rand.nextDouble() * 0.5, pos.getY() + 1.75, pos.getZ() + 0.25 + world.rand.nextDouble() * 0.5), new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX() + 0.5, pos.getY() + 1.75, pos.getZ() + 0.5, 75));
                MainRegistry.proxy.effectNT(data);
                world.playSound(null, pos.getX() + 0.5F, pos.getY() + 0.5F, pos.getZ() + 0.5F, SoundEvents.BLOCK_FIRE_AMBIENT, SoundCategory.BLOCKS, 1.0F + world.rand.nextFloat(), world.rand.nextFloat() * 0.7F + 0.3F);
            }
        }

        world.scheduleUpdate(pos, this, this.tickRate(world));
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Items.AIR;
    }

    @Override
    public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune){
        drops.add(new ItemStack(ModBlocks.concrete_smooth, 6));
        drops.add(new ItemStack(ModBlocks.steel_grate, 2));
        drops.add(new ItemStack(ModItems.debris_metal, 6));
        drops.add(new ItemStack(ModItems.debris_graphite, 2));
        drops.add(new ItemStack(ModItems.fallout, 4));
    }

    @Override
    public int[] getDimensions() {
        return new int[] {1, 0, 2, 2, 2, 2,};
    }

    @Override
    public int getOffset() {
        return 2;
    }

    protected void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
        super.fillSpace(world, x, y, z, dir, o);
    }

}
