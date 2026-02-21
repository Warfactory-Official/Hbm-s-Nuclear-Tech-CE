package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.IPersistentInfoProvider;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.lib.ForgeDirection;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IPersistentNBT;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.oil.TileEntityMachinePumpjack;
import com.hbm.util.BobMathUtil;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MachinePumpjack extends BlockDummyable implements IPersistentInfoProvider {
    public MachinePumpjack(Material materialIn, String s) {
		super(materialIn, s);
	}

	@Override
	public TileEntity createNewTileEntity(@NotNull World world, int meta) {

		if(meta >= 12) return new TileEntityMachinePumpjack();
		if(meta >= 6) return new TileEntityProxyCombo(false, true, true);
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {3, 0, 0, 0, 0, 6};
	}

	@Override
	public int getOffset() {
		return 0;
	}

	@Override
	public boolean checkRequirement(World world, int x, int y, int z, ForgeDirection dir, int o) {
		return super.checkRequirement(world, x, y, z, dir, o) &&
				MultiblockHandlerXR.checkSpace(world, x + dir.offsetX * o, y + dir.offsetY * o, z + dir.offsetZ * o, new int[] {0, 0, -1, 1, -2, 4}, x, y, z, dir) &&
				MultiblockHandlerXR.checkSpace(world, x + dir.offsetX * o, y + dir.offsetY * o, z + dir.offsetZ * o, new int[] {0, 0, 1, -1, -1, 5}, x, y, z, dir);
	}

	@Override
	public void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		super.fillSpace(world, x, y, z, dir, o);

		ForgeDirection rot = dir.getRotation(ForgeDirection.DOWN);
		MultiblockHandlerXR.fillSpace(world, x + rot.offsetX * 3, y, z + rot.offsetZ * 3, new int[] {0, 0, -1, 1, 1, 1}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x + rot.offsetX * 3, y, z + rot.offsetZ * 3, new int[] {0, 0, 1, -1, 2, 2}, this, dir);

		this.makeExtra(world, x + rot.offsetX * 3 + 1, y, z + rot.offsetZ * 3 + 1);
		this.makeExtra(world, x + rot.offsetX * 3 + 1, y, z + rot.offsetZ * 3 - 1);
		this.makeExtra(world, x + rot.offsetX * 3 - 1, y, z + rot.offsetZ * 3 + 1);
		this.makeExtra(world, x + rot.offsetX * 3 - 1, y, z + rot.offsetZ * 3 - 1);
	}

	@Override
	public boolean onBlockActivated(World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityPlayer player, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(world.isRemote) {
			return true;
		} else if(!player.isSneaking()) {
			int[] posC = this.findCore(world, pos.getX(), pos.getY(), pos.getZ());

			if(posC == null)
				return false;
			FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, posC[0], posC[1], posC[2]);
			return true;
		} else {
			return false;
		}
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

	@Override
	public void addInformation(ItemStack stack, NBTTagCompound persistentTag, EntityPlayer player, List<String> list, boolean ext) {
		list.add(TextFormatting.GREEN + BobMathUtil.getShortNumber(persistentTag.getLong("power")) + "HE");
		for(int i = 0; i < 2; i++) {
			FluidTankNTM tank = new FluidTankNTM(Fluids.NONE, 0);
			tank.readFromNBT(persistentTag, "t" + i);
			list.add(TextFormatting.YELLOW + "" + tank.getFill() + "/" + tank.getMaxFill() + "mB " + tank.getTankType().getLocalizedName());
		}
	}

}
