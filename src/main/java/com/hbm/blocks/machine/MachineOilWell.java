package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.IPersistentInfoProvider;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.explosion.ExplosionNT;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.BlockAllocatorStandard;
import com.hbm.explosion.vanillant.standard.BlockProcessorStandard;
import com.hbm.explosion.vanillant.standard.EntityProcessorStandard;
import com.hbm.explosion.vanillant.standard.PlayerProcessorStandard;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.lib.ForgeDirection;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IPersistentNBT;
import com.hbm.tileentity.machine.oil.TileEntityMachineOilWell;
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
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MachineOilWell extends BlockDummyable implements IPersistentInfoProvider {

	public MachineOilWell(Material materialIn, String s) {
		super(materialIn, s);
	}

	@Override
	public TileEntity createNewTileEntity(@NotNull World world, int meta) {
		if(meta >= 12) return new TileEntityMachineOilWell();
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {9, 0, 1, 1, 1, 1};
	}

	@Override
	public int getOffset() {
		return 0;
	}
	
	@Override
	public boolean onBlockActivated(World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityPlayer player, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(world.isRemote)
		{
			return true;
		} else if(!player.isSneaking())
		{
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
	public boolean checkRequirement(World world, int x, int y, int z, ForgeDirection dir, int o) {
		return MultiblockHandlerXR.checkSpace(world, x, y, z, new int[] {1, -1, 0, 0, 0, 0}, x, y, z, dir) &&
				MultiblockHandlerXR.checkSpace(world, x, y + 1, z, new int[] {8, 0, 1, 1, 1, 1}, x, y, z, dir) &&
				MultiblockHandlerXR.checkSpace(world, x + 1, y + 1, z + 1, new int[] {-1, 1, 0, 0, 0, 0}, x, y, z, dir) &&
				MultiblockHandlerXR.checkSpace(world, x + 1, y + 1, z - 1, new int[] {-1, 1, 0, 0, 0, 0}, x, y, z, dir) &&
				MultiblockHandlerXR.checkSpace(world, x - 1, y + 1, z + 1, new int[] {-1, 1, 0, 0, 0, 0}, x, y, z, dir) &&
				MultiblockHandlerXR.checkSpace(world, x - 1, y + 1, z - 1, new int[] {-1, 1, 0, 0, 0, 0}, x, y, z, dir);
	}

	@Override
	public void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		MultiblockHandlerXR.fillSpace(world, x, y, z, new int[] {1, -1, 0, 0, 0, 0}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x, y + 1, z, new int[] {8, 0, 1, 1, 1, 1}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x + 1, y + 1, z + 1, new int[] {-1, 1, 0, 0, 0, 0}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x + 1, y + 1, z - 1, new int[] {-1, 1, 0, 0, 0, 0}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x - 1, y + 1, z + 1, new int[] {-1, 1, 0, 0, 0, 0}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x - 1, y + 1, z - 1, new int[] {-1, 1, 0, 0, 0, 0}, this, dir);
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

	@Override
	public void onBlockExploded(@NotNull World world, BlockPos pos, @NotNull Explosion explosion) {

		int[] posC = this.findCore(world, pos.getX(), pos.getY(), pos.getZ());
		if(posC == null) return;
		TileEntity core = world.getTileEntity(new BlockPos(posC[0], posC[1], posC[2]));
		if(!(core instanceof TileEntityMachineOilWell well)) return;

		world.setBlockToAir(pos);
		onBlockExploded(world, pos, explosion);

		if(well.tanks[0].getFill() > 0 || well.tanks[1].getFill() > 0) {
			well.tanks[0].setFill(0);
			well.tanks[1].setFill(0);

			ExplosionVNT xnt = new ExplosionVNT(world, posC[0] + 0.5, posC[1] + 0.5, posC[2] + 0.5, 15F);
			xnt.setBlockAllocator(new BlockAllocatorStandard(24));
			xnt.setBlockProcessor(new BlockProcessorStandard().setNoDrop());
			xnt.setEntityProcessor(new EntityProcessorStandard());
			xnt.setPlayerProcessor(new PlayerProcessorStandard());
			xnt.explode();

			new ExplosionNT(world, null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 50).overrideResolution(64).explode();
			ExplosionLarge.spawnParticles(world, pos.getX(), pos.getY(), pos.getZ(), ExplosionLarge.cloudFunction(15));
			//ExplosionCreator.composeEffect(world, posC[0] + 0.5, posC[1] + 0.5, posC[2] + 0.5, 10, 2F, 0.5F, 25F, 5, 8, 20, 0.75F, 1F, -2F, 150);
		}
	}

}
