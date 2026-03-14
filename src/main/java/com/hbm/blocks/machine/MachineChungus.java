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
import com.hbm.util.BobMathUtil;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MachineChungus extends BlockDummyable implements ILookOverlay {

	public MachineChungus(Material mat, String s) {
		super(mat, s);
	}

	@Override
	public TileEntity createNewTileEntity(@NotNull World world, int meta) {
		if(meta >= 12) return new TileEntityChungus();
		if(meta >= 6) return new TileEntityProxyCombo(false, true, true);
		return null;
	}
	
	@Override
	public boolean onBlockActivated(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState state, EntityPlayer player, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ){
		if(!player.isSneaking()) {
			int x = pos.getX();
			int y = pos.getY();
			int z = pos.getZ();
			BlockPos posC = this.findCore(world, pos);

			if(posC == null)
				return true;

			TileEntityChungus entity = (TileEntityChungus) world.getTileEntity(posC);
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
						if(!entity.operational) {
							world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, HBMSoundHandler.chungus_lever, SoundCategory.BLOCKS, 1.5F, 1.0F);
							entity.onLeverPull();
						} else {
							player.sendMessage(new TextComponentString(TextFormatting.RED + "Cannot change compressor setting while operational!"));
						}
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
        return world.getBlockState(new BlockPos(x + dir.offsetX, y + 2, z + dir.offsetZ)).getBlock().canPlaceBlockAt(world, new BlockPos(x + dir.offsetX, y + 2, z + dir.offsetZ));
    }

	@Override
	public void printHook(Pre event, World world, BlockPos pos) {
		BlockPos corePos = this.findCore(world, pos);
		
		if(corePos == null)
			return;
		
		TileEntity te = world.getTileEntity(corePos);
		if (!(te instanceof TileEntityChungus chungus)) return;

        List<String> text = new ArrayList<>();
		FluidType inputType = chungus.tanks[0].getTankType();
		if (inputType != Fluids.NONE)
			text.add("§a-> §r" + inputType.getLocalizedName() + ": " + chungus.tanks[0].getFill() + "/" + chungus.tanks[0].getMaxFill() + "mB");
		FluidType outputType = chungus.tanks[1].getTankType();
		if (outputType != Fluids.NONE)
			text.add("§c<- §r" + outputType.getLocalizedName() + ": " + chungus.tanks[1].getFill() + "/" + chungus.tanks[1].getMaxFill() + "mB");
		text.add(TextFormatting.RED + "<- " + TextFormatting.RESET + BobMathUtil.getShortNumber(chungus.powerBuffer) + "HE");
		ILookOverlay.printGeneric(event, getLocalizedName(), 0xffff00, 0x404000, text);
	}
}