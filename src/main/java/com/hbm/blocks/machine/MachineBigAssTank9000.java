package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.lib.ForgeDirection;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachineBAT9000;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;

public class MachineBigAssTank9000 extends BlockDummyable {

	public MachineBigAssTank9000(Material mat, String s) {
		super(mat, s);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		
		if(meta >= 12) return new TileEntityMachineBAT9000();
		if(meta >= 6) return new TileEntityProxyCombo(true, false, true);
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {4, 0, 2, 2, 1, 1};
	}

	@Override
	public int getOffset() {
		return 2;
	}

	@Override
	public void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		super.fillSpace(world, x, y, z, dir, o);
		MultiblockHandlerXR.fillSpace(world, x + dir.offsetX * o , y + dir.offsetY * o, z + dir.offsetZ * o, new int[] {4, 0, 1, 1, 2, -2}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x + dir.offsetX * o , y + dir.offsetY * o, z + dir.offsetZ * o, new int[] {4, 0, 1, 1, -2, 2}, this, dir);

		this.makeExtra(world, x + dir.offsetX * o + 1, y, z + dir.offsetZ * o + 2);
		this.makeExtra(world, x + dir.offsetX * o - 1, y, z + dir.offsetZ * o + 2);
		this.makeExtra(world, x + dir.offsetX * o + 1, y, z + dir.offsetZ * o - 2);
		this.makeExtra(world, x + dir.offsetX * o - 1, y, z + dir.offsetZ * o - 2);
		this.makeExtra(world, x + dir.offsetX * o + 2, y, z + dir.offsetZ * o + 1);
		this.makeExtra(world, x + dir.offsetX * o - 2, y, z + dir.offsetZ * o + 1);
		this.makeExtra(world, x + dir.offsetX * o + 2, y, z + dir.offsetZ * o - 1);
		this.makeExtra(world, x + dir.offsetX * o - 2, y, z + dir.offsetZ * o - 1);
	}

	@Override
	public boolean checkRequirement(World world, int x, int y, int z, ForgeDirection dir, int o) {

		if(!MultiblockHandlerXR.checkSpace(world, x + dir.offsetX * o , y + dir.offsetY * o, z + dir.offsetZ * o, getDimensions(), x, y, z, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, x + dir.offsetX * o , y + dir.offsetY * o, z + dir.offsetZ * o, new int[] {4, 0, 1, 1, 2, -2}, x, y, z, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, x + dir.offsetX * o , y + dir.offsetY * o, z + dir.offsetZ * o, new int[] {4, 0, 1, 1, -2, 2}, x, y, z, dir)) return false;
		
		return true;
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos1, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ){
		if(world.isRemote) {
			return true;
		} else if(!player.isSneaking()) {
			
			int[] pos = this.findCore(world, pos1.getX(), pos1.getY(), pos1.getZ());
			
			if(pos == null)
				return false;
			
			FMLNetworkHandler.openGui(player, MainRegistry.instance, ModBlocks.guiID_barrel, world, pos[0], pos[1], pos[2]); //we can do this because nobody is stopping me from doing this
			return true;
		} else {
			return true;
		}
	}
	
}