package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.IPersistentInfoProvider;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.lib.ForgeDirection;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachineOrbus;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;

import java.util.List;

public class MachineOrbus extends BlockDummyable implements IPersistentInfoProvider {

	public MachineOrbus(Material mat, String s) {
		super(mat, s);
	}

	@Override
	public boolean hasComparatorInputOverride(IBlockState state) {
		return true;
	}
	@Override
	public int getComparatorInputOverride(IBlockState blockState, World worldIn, BlockPos pos) {
		TileEntity te = worldIn.getTileEntity(pos);
		if (te instanceof TileEntityMachineOrbus orbus) {
			return orbus.tankNew.getRedstoneComparatorPower();
		}
		TileEntity core = this.findCoreTE(worldIn, pos);
		if (core instanceof TileEntityMachineOrbus orbus) {
			return orbus.tankNew.getRedstoneComparatorPower();
		}
		return 0;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {

		if(meta >= 12) return new TileEntityMachineOrbus();
		if(meta >= 6) return new TileEntityProxyCombo(true, false, true);
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {4, 0, 2, 1, 2, 1};
	}

	@Override
	public int getOffset() {
		return 1;
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos1, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ){
		if(world.isRemote) {
			return true;
		} else if(!player.isSneaking()) {
			
			int[] pos = this.findCore(world, pos1.getX(), pos1.getY(), pos1.getZ());
			
			if(pos == null)
				return false;
			
			FMLNetworkHandler.openGui(player, MainRegistry.instance, ModBlocks.guiID_barrel, world, pos[0], pos[1], pos[2]);
			return true;
		} else {
			return true;
		}
	}

	@Override
	public void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		super.fillSpace(world, x, y, z, dir, o);
		
		x = x + dir.offsetX * o;
		z = z + dir.offsetZ * o;
		
		ForgeDirection d2 = dir.getRotation(ForgeDirection.UP);
		dir = dir.getOpposite();

		for(int i = 0; i < 5; i += 4) {
			this.makeExtra(world, x, y + i, z);
			this.makeExtra(world, x + dir.offsetX, y + i, z + dir.offsetZ);
			this.makeExtra(world, x + d2.offsetX, y + i, z + d2.offsetZ);
			this.makeExtra(world, x + dir.offsetX + d2.offsetX, y + i, z + dir.offsetZ + d2.offsetZ);
		}
	}

    @Override
    public void addInformation(ItemStack stack, NBTTagCompound persistentTag, EntityPlayer player, List<String> list, boolean ext) {
        FluidTankNTM tank = new FluidTankNTM(Fluids.NONE, 0);
        tank.readFromNBT(persistentTag, "tank");
        list.add(ChatFormatting.YELLOW + "" + tank.getFill() + "/" + tank.getMaxFill() + "mB " + tank.getTankType().getLocalizedName());
    }
}