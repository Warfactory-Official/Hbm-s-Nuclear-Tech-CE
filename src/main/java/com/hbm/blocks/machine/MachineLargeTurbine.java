package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.lib.ForgeDirection;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachineLargeTurbine;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import org.jetbrains.annotations.NotNull;

public class MachineLargeTurbine extends BlockDummyable {

	public MachineLargeTurbine(Material materialIn, String s) {
		super(materialIn, s);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		if(meta >= 12)
			return new TileEntityMachineLargeTurbine();

		if(meta >= 6)
			return new TileEntityProxyCombo(false, true, true);

		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] { 1, 0, 3, 1, 1, 1 };
	}

	@Override
	public int getOffset() {
		return 1;
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
			return true;
		}
	}
	
	@Override
	public void onBlockPlacedBy(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityLivingBase player, @NotNull ItemStack itemStack) {
		super.onBlockPlacedBy(world, pos, state, player, itemStack);
		
		if(world.isRemote)
			return;

		int k = MathHelper.floor(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
		ForgeDirection dir = ForgeDirection.NORTH;

		if(k == 0)
			dir = ForgeDirection.getOrientation(2);
		if(k == 1)
			dir = ForgeDirection.getOrientation(5);
		if(k == 2)
			dir = ForgeDirection.getOrientation(3);
		if(k == 3)
			dir = ForgeDirection.getOrientation(4);

		ForgeDirection dir2 = dir.getRotation(ForgeDirection.UP);

		//back connector
		this.makeExtra(world, pos.getX() + dir.offsetX * -4, pos.getY(), pos.getZ() + dir.offsetZ * -4);
		//front connector
		this.makeExtra(world, pos.getX(), pos.getY(), pos.getZ());

		int xc = pos.getX() - dir.offsetX;
		int zc = pos.getZ() - dir.offsetZ;

		//side connectors
		this.makeExtra(world, xc + dir2.offsetX, pos.getY(), zc + dir2.offsetZ);
		this.makeExtra(world, xc - dir2.offsetX, pos.getY(), zc - dir2.offsetZ);
	}
	
}
