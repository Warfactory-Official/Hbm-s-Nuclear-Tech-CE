package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityChungus;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;

import java.util.ArrayList;
import java.util.List;

public class MachineChungus extends BlockDummyable implements ILookOverlay {

	public MachineChungus(Material mat, String s) {
		super(mat, s);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		
		if(meta >= 12)
			return new TileEntityChungus();
		
		if(meta >= 6)
			return new TileEntityProxyCombo(false, true, true);
		
		return null;
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos1, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ){
		if(!player.isSneaking()) {
			int x = pos1.getX();
			int y = pos1.getY();
			int z = pos1.getZ();
			int[] pos = this.findCore(world, x, y, z);

			if(pos == null)
				return true;

			TileEntityChungus entity = (TileEntityChungus) world.getTileEntity(new BlockPos(pos[0], pos[1], pos[2]));
			if(entity != null) {
				
				ForgeDirection dir = ForgeDirection.getOrientation(entity.getBlockMetadata() - offset);
				ForgeDirection turn = dir.getRotation(ForgeDirection.DOWN);

				int iX = entity.getPos().getX() + dir.offsetX + turn.offsetX * 2;
				int iX2 = entity.getPos().getX() + dir.offsetX * 2 + turn.offsetX * 2;
				int iZ = entity.getPos().getZ() + dir.offsetZ + turn.offsetZ * 2;
				int iZ2 = entity.getPos().getZ() + dir.offsetZ * 2 + turn.offsetZ * 2;
				
				if((x == iX || x == iX2) && (z == iZ || z == iZ2) && y < entity.getPos().getY() + 2) {
					world.playSound(null, x + 0.5, y + 0.5, z + 0.5, HBMSoundHandler.chungus_lever, SoundCategory.BLOCKS, 1.5F, 1.0F);
					
					if(!world.isRemote) {
						FluidType currentType = entity.tanks[0].getTankType();
						FluidType newType = currentType;
						int currentFill = entity.tanks[0].getFill();
						int newFill = 0;
						entity.onLeverPull(currentType);
						if (currentType == Fluids.STEAM) {
							newType = Fluids.HOTSTEAM;
							newFill = currentFill / 10;
						} else if (currentType == Fluids.HOTSTEAM) {
							newType = Fluids.SUPERHOTSTEAM;
							newFill = currentFill / 10;
						} else if (currentType == Fluids.SUPERHOTSTEAM) {
							newType = Fluids.ULTRAHOTSTEAM;
							newFill = currentFill / 10;
						} else if (currentType == Fluids.ULTRAHOTSTEAM) {
							newType = Fluids.STEAM;
							newFill = (int) Math.min((long)currentFill * 1000, entity.tanks[0].getMaxFill());
						}
						entity.tanks[0].setTankType(newType);
						entity.tanks[0].setFill(newFill);
						entity.tanks[1].setFill(0);
						entity.markDirty();
					}
					
					return true;
				}
			}
		}
		
		return false;
	}

	@Override
	public int[] getDimensions() {
		return new int[] { 3, 0, 0, 3, 2, 2 };
	}

	@Override
	public int getOffset() {
		return 3;
	}

	@Override
	public void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		super.fillSpace(world, x, y, z, dir, o);
		MultiblockHandlerXR.fillSpace(world, x + dir.offsetX * o , y + dir.offsetY * o, z + dir.offsetZ * o, new int[] {4, -4, 0, 3, 1, 1}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x + dir.offsetX * o , y + dir.offsetY * o, z + dir.offsetZ * o, new int[] {3, 0, 6, -1, 1, 1}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x + dir.offsetX * o , y + dir.offsetY * o, z + dir.offsetZ * o, new int[] {2, 0, 10, -7, 1, 1}, this, dir);
		world.setBlockState(new BlockPos(x + dir.offsetX, y + 2, z + dir.offsetZ), this.getDefaultState().withProperty(META, dir.ordinal()), 3);

		this.makeExtra(world, x + dir.offsetX, y + 2, z + dir.offsetZ);
		this.makeExtra(world, x + dir.offsetX * (o - 10), y, z + dir.offsetZ * (o - 10));
		ForgeDirection side = dir.getRotation(ForgeDirection.UP);
		this.makeExtra(world, x + dir.offsetX * o + side.offsetX * 2 , y, z + dir.offsetZ * o + side.offsetZ * 2);
		this.makeExtra(world, x + dir.offsetX * o - side.offsetX * 2 , y, z + dir.offsetZ * o - side.offsetZ * 2);
	}

	@Override
	public boolean checkRequirement(World world, int x, int y, int z, ForgeDirection dir, int o) {

		if(!MultiblockHandlerXR.checkSpace(world, x + dir.offsetX * o , y + dir.offsetY * o, z + dir.offsetZ * o, getDimensions(), x, y, z, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, x + dir.offsetX * o , y + dir.offsetY * o, z + dir.offsetZ * o, new int[] {3, 0, 6, -1, 1, 1}, x, y, z, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, x + dir.offsetX * o , y + dir.offsetY * o, z + dir.offsetZ * o, new int[] {2, 0, 10, -7, 1, 1}, x, y, z, dir)) return false;
		if(!world.getBlockState(new BlockPos(x + dir.offsetX, y + 2, z + dir.offsetZ)).getBlock().canPlaceBlockAt(world, new BlockPos(x + dir.offsetX, y + 2, z + dir.offsetZ))) return false;
		
		return true;
	}

	@Override
	public void printHook(Pre event, World world, BlockPos pos) {
		BlockPos corePos = this.findCore(world, pos);
		
		if(corePos == null)
			return;
		
		TileEntity te = world.getTileEntity(corePos);
		if (!(te instanceof TileEntityChungus chungus)) return;

        List<String> text = new ArrayList<>();
		text.add(Library.getShortNumber(chungus.power) + "/" + Library.getShortNumber(chungus.getMaxPower()) + " HE");
		FluidType inputType = chungus.tanks[0].getTankType();
		if (inputType != Fluids.NONE)
			text.add("§a-> §r" + inputType.getLocalizedName() + ": " + chungus.tanks[0].getFill() + "/" + chungus.tanks[0].getMaxFill() + "mB");
		FluidType outputType = chungus.tanks[1].getTankType();
		if (outputType != Fluids.NONE)
			text.add("§c<- §r" + outputType.getLocalizedName() + ": " + chungus.tanks[1].getFill() + "/" + chungus.tanks[1].getMaxFill() + "mB");
		ILookOverlay.printGeneric(event, getLocalizedName(), 0xffff00, 0x404000, text);
	}
}