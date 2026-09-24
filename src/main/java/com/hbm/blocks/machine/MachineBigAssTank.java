package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.IPersistentInfoProvider;
import com.hbm.blocks.ModBlocks;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.lib.ForgeDirection;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IPersistentNBT;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachineBigAssTank;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MachineBigAssTank extends BlockDummyable implements IPersistentInfoProvider {

	public MachineBigAssTank(Material mat, String s) {
		super(mat, s);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		if(meta >= 12) return new TileEntityMachineBigAssTank();
		if(meta >= 6) return new TileEntityProxyCombo(false, false, true);
		return null;
	}

	@Override public int[] getDimensions() { return new int[] {5, 0, 4, 4, 4, 4}; }
	@Override public int getOffset() { return 6; }

	@Override
	public void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		super.fillSpace(world, x, y, z, dir, o);
		x += dir.offsetX * o;
		z += dir.offsetZ * o;
		MultiblockHandlerXR.fillSpace(world, x, y, z, new int[] {4, 0, 5, -4, 2, 2}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x, y, z, new int[] {4, 0, -4, 5, 2, 2}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x, y, z, new int[] {4, 0, 2, 2, 5, -4}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x, y, z, new int[] {4, 0, 2, 2, -4, 5}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x, y, z, new int[] {3, 0, 6, -5, 0, 0}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x, y, z, new int[] {3, 0, -5, 6, 0, 0}, this, dir);

		this.makeExtra(world, x + dir.offsetX * 6, y, z + dir.offsetZ * o);
		this.makeExtra(world, x - dir.offsetX * 6, y, z - dir.offsetZ * o);
	}

	@Override
	public boolean checkRequirement(World world, int x, int y, int z, ForgeDirection dir, int o) {
		if(!MultiblockHandlerXR.checkSpace(world, x + dir.offsetX * o , y + dir.offsetY * o, z + dir.offsetZ * o, getDimensions(), x, y, z, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, x + dir.offsetX * o , y + dir.offsetY * o, z + dir.offsetZ * o, new int[] {4, 0, 5, -4, 2, 2}, x, y, z, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, x + dir.offsetX * o , y + dir.offsetY * o, z + dir.offsetZ * o, new int[] {4, 0, -4, 5, 2, 2}, x, y, z, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, x + dir.offsetX * o , y + dir.offsetY * o, z + dir.offsetZ * o, new int[] {4, 0, 2, 2, 5, -4}, x, y, z, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, x + dir.offsetX * o , y + dir.offsetY * o, z + dir.offsetZ * o, new int[] {4, 0, 2, 2, -4, 5}, x, y, z, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, x + dir.offsetX * o , y + dir.offsetY * o, z + dir.offsetZ * o, new int[] {3, 0, 6, -5, 0, 0}, x, y, z, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, x + dir.offsetX * o , y + dir.offsetY * o, z + dir.offsetZ * o, new int[] {3, 0, -5, 6, 0, 0}, x, y, z, dir)) return false;
		return true;
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos1, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

		if(world.isRemote) return true;

		if(!player.isSneaking()) {

			int[] pos = this.findCore(world, pos1.getX(), pos1.getY(), pos1.getZ());
			if(pos == null) return false;
			FMLNetworkHandler.openGui(player, MainRegistry.instance, ModBlocks.guiID_barrel, world, pos[0], pos[1], pos[2]); //we can do this because nobody is stopping me from doing this
			return true;
		} else {
			int[] pos = this.findCore(world, pos1.getX(), pos1.getY(), pos1.getZ());
			if(pos == null) return false;

			TileEntity te = world.getTileEntity(new BlockPos(pos[0], pos[1], pos[2]));

			if(te instanceof TileEntityMachineBigAssTank tank) {
				if(!player.getHeldItem(hand).isEmpty() && player.getHeldItem(hand).getItem() instanceof IItemFluidIdentifier) {
					FluidType type = ((IItemFluidIdentifier) player.getHeldItem(hand).getItem()).getType(world, pos[0], pos[1], pos[2], player.getHeldItem(hand));

					tank.tankNew.setTankType(type);
					tank.markDirty();
					player.sendMessage(new TextComponentString("Changed type to ")
							.setStyle(new Style().setColor(TextFormatting.YELLOW))
							.appendSibling(new TextComponentTranslation(type.getConditionalName()))
							.appendSibling(new TextComponentString("!")));
				}
			}
			return true;
		}
	}

	@Override
	public boolean hasComparatorInputOverride(IBlockState state) {
		return true;
	}

	@Override
	public int getComparatorInputOverride(IBlockState blockState, World worldIn, BlockPos pos) {
		TileEntity te = worldIn.getTileEntity(pos);
		if (te instanceof TileEntityMachineBigAssTank tank) {
			return tank.tankNew.getRedstoneComparatorPower();
		}
		TileEntity core = this.findCoreTE(worldIn, pos);
		if (core instanceof TileEntityMachineBigAssTank tank) {
			return tank.tankNew.getRedstoneComparatorPower();
		}
		return 0;
	}

	@Override
	public void addInformation(ItemStack stack, NBTTagCompound persistentTag, EntityPlayer player, List<String> list, boolean ext) {
		FluidTankNTM tank = new FluidTankNTM(Fluids.NONE, 0);
		tank.readFromNBT(persistentTag, "tank");
		list.add(TextFormatting.YELLOW + "" + tank.getFill() + "/" + tank.getMaxFill() + "mB " + tank.getTankType().getLocalizedName());
	}

	@Override
	public void onBlockHarvested(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
		IPersistentNBT.onBlockHarvested(world, pos, player);
	}

	@Override
	public void breakBlock(@NotNull World worldIn, @NotNull BlockPos pos, IBlockState state) {
		IPersistentNBT.breakBlock(worldIn, pos, state);
		super.breakBlock(worldIn, pos, state);
	}

	@Override
	public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune) {
	}
}
