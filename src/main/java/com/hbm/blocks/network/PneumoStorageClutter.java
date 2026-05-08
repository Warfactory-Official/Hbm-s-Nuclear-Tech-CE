package com.hbm.blocks.network;

import com.hbm.blocks.BlockContainerBakeableNormal;
import com.hbm.main.MainRegistry;
import com.hbm.render.block.BlockBakeFrame;
import com.hbm.tileentity.network.TileEntityPneumoStorageClutter;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;

public class PneumoStorageClutter extends BlockContainerBakeableNormal {

    public PneumoStorageClutter(String registryName) {
        super(Material.IRON, registryName, BlockBakeFrame.cubeAll("pneumatic_storage_clutter"));
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityPneumoStorageClutter();
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, net.minecraft.block.state.IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (player.isSneaking()) return false;
        if (!world.isRemote) {
            FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
    }
}
