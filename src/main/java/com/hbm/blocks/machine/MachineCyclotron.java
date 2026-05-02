package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachineCyclotron;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;

public class MachineCyclotron extends BlockDummyable {

	public MachineCyclotron(Material materialIn, String s) {
		super(materialIn, s);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		if(meta >= 12)
			return new TileEntityMachineCyclotron();
		if(meta >= 6)
			return new TileEntityProxyCombo().inventory().power().fluid();
		return null;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(world.isRemote) {
			return true;
		} else if(!player.isSneaking()) {
			int[] pos1 = this.findCore(world, pos.getX(), pos.getY(), pos.getZ());

			if(pos1 == null)
				return false;
			BlockPos tPos = new BlockPos(pos1[0], pos1[1], pos1[2]);
			if(!(world.getTileEntity(tPos) instanceof TileEntityMachineCyclotron cyc)) return false;

			if(!player.getHeldItemMainhand().isEmpty()) {

				for(int i = 0; i < 4; i++) {

					if(player.getHeldItemMainhand().getItem() == TileEntityMachineCyclotron.getItemForPlug(i) && !cyc.getPlug(i)) {
						player.getHeldItemMainhand().shrink(1);
						cyc.setPlug(i);
						world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, HBMSoundHandler.upgradePlug, SoundCategory.BLOCKS, 1.5F, 1.0F);
						return true;
					}
				}
			}

			FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos1[0], pos1[1], pos1[2]);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int[] getDimensions() {
		return new int[] {2, 0, 2, 2, 2, 2};
	}

	@Override
	public int getOffset() {
		return 2;
	}
	
	@Override
	protected void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		super.fillSpace(world, x, y, z, dir, o);
		this.makeExtra(world, x + dir.offsetX * o + 2, y, z + dir.offsetZ * o + 1);
		this.makeExtra(world, x + dir.offsetX * o + 2, y, z + dir.offsetZ * o);
		this.makeExtra(world, x + dir.offsetX * o + 2, y, z + dir.offsetZ * o - 1);
		this.makeExtra(world, x + dir.offsetX * o - 2, y, z + dir.offsetZ * o + 1);
		this.makeExtra(world, x + dir.offsetX * o - 2, y, z + dir.offsetZ * o);
		this.makeExtra(world, x + dir.offsetX * o - 2, y, z + dir.offsetZ * o - 1);
		this.makeExtra(world, x + dir.offsetX * o + 1, y, z + dir.offsetZ * o + 2);
		this.makeExtra(world, x + dir.offsetX * o, y, z + dir.offsetZ * o + 2);
		this.makeExtra(world, x + dir.offsetX * o - 1, y, z + dir.offsetZ * o + 2);
		this.makeExtra(world, x + dir.offsetX * o + 1, y, z + dir.offsetZ * o - 2);
		this.makeExtra(world, x + dir.offsetX * o, y, z + dir.offsetZ * o - 2);
		this.makeExtra(world, x + dir.offsetX * o - 1, y, z + dir.offsetZ * o - 2);
	}

}
