package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.lib.ForgeDirection;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachinePlasmaHeater;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class MachinePlasmaHeater extends BlockDummyable {

	public MachinePlasmaHeater(String s) {
		super(Material.IRON, s);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		if(meta >= 12)
			return new TileEntityMachinePlasmaHeater();
		if(meta >= 6)
			return new TileEntityProxyCombo(false, true, true);
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {3, 0, 8, 1, 1, 1};
	}

	@Override
	public int getOffset() {
		return 1;
	}
	
	@Override
	public void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		super.fillSpace(world, x, y, z, dir, o);
		
		MultiblockHandlerXR.fillSpace(world, x + dir.offsetX * o , y + dir.offsetY * o, z + dir.offsetZ * o, new int[] {4, -3, 2, 1, 1, 1}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x + dir.offsetX * o , y + 2, z + dir.offsetZ * o, new int[] {0, 1, 10, -8, 0, 0}, this, dir);
		
		ForgeDirection side = dir.getRotation(ForgeDirection.UP);

		for(int i = 1; i < 4; i++) {
			for(int j = -1; j < 2; j++) {

				this.makeExtra(world, x + side.offsetX * j, y + i, z + side.offsetZ * j);
			}
		}
	}
	
	@Override
	public void breakBlock(@NotNull World world, @NotNull BlockPos pos, IBlockState state) {
		int i = state.getValue(META);
		if(i >= 12) {

            for(int l = 0; l < 2; l++)
            	world.spawnEntity(new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(ModBlocks.fusion_heater, 64)));

        	world.spawnEntity(new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(ModBlocks.fusion_heater, 7)));
        	world.spawnEntity(new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(ModBlocks.struct_plasma_core, 1)));
    	}

		super.breakBlock(world, pos, state);
	}
	
	@Override
	public @NotNull AxisAlignedBB getBoundingBox(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos) {
		final float f = 1/16F;
		if(state.getValue(META) == ForgeDirection.UP.ordinal() && world.getBlockState(pos.up()).getBlock() != this) {
    		return new AxisAlignedBB(0, 0, 0, 1, f * 8F, 1);
    	} else if(state.getValue(META) == ForgeDirection.DOWN.ordinal() && world.getBlockState(pos.down()).getBlock() != this) {
    		return new AxisAlignedBB(0, f * 8F, 0, 1, 1, 1);
    	} else {
    		return new AxisAlignedBB(0, 0, 0, 1, 1, 1);
    	}
	}
	
	@Override
	public boolean checkRequirement(World world, int x, int y, int z, ForgeDirection dir, int o) {
		if(!MultiblockHandlerXR.checkSpace(world, x + dir.offsetX * o , y + dir.offsetY * o, z + dir.offsetZ * o, getDimensions(), x, y, z, dir))
			return false;

		if(!MultiblockHandlerXR.checkSpace(world, x + dir.offsetX * o , y + dir.offsetY * o, z + dir.offsetZ * o, new int[] {4, -3, 1, 1, 1, 1}, x, y, z, dir))
			return false;
		
		if(!MultiblockHandlerXR.checkSpace(world, x + dir.offsetX * o , y + 2, z + dir.offsetZ * o, new int[] {0, 1, 10, -8, 0, 0}, x, y, z, dir))
			return false;

		return true;
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos1, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(world.isRemote)
		{
			return true;
		} else if(!player.isSneaking())
		{
			int[] pos = this.findCore(world, pos1.getX(), pos1.getY(), pos1.getZ());

			if(pos == null)
				return false;

			FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos[0], pos[1], pos[2]);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return Items.AIR;
	}
	
}
