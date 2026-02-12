package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.oil.TileEntityMachineFractionTower;
import com.hbm.util.I18nUtil;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;

import java.util.ArrayList;
import java.util.List;

public class MachineFractionTower extends BlockDummyable implements ILookOverlay {

	public MachineFractionTower(Material mat, String s) {
		super(mat, s);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		
		if(meta >= 12)
			return new TileEntityMachineFractionTower();
		if(meta >= extra)
			return new TileEntityProxyCombo(false, false, true);
		
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {2, 0, 1, 1, 1, 1};
	}

	@Override
	public int getOffset() {
		return 1;
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos1, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ){
		if(!world.isRemote && !player.isSneaking()) {
			
			if(player.getHeldItem(hand).isEmpty() || player.getHeldItem(hand).getItem() instanceof IItemFluidIdentifier) {
				int[] pos = this.findCore(world, pos1.getX(), pos1.getY(), pos1.getZ());
					
				if(pos == null)
					return false;
				
				TileEntity te = world.getTileEntity(new BlockPos(pos[0], pos[1], pos[2]));
				
				if(!(te instanceof TileEntityMachineFractionTower frac))
					return false;

				if(player.getHeldItem(hand).isEmpty()) {
					player.sendMessage(new TextComponentTranslation("chat.fractioning.y", pos[1]));

					for(int i = 0; i < frac.tanks.length; i++)
						player.sendMessage(new TextComponentTranslation(frac.tanks[i].getTankType().getTranslationKey()).appendSibling(new TextComponentString(": " + frac.tanks[i].getFill() + "/" + frac.tanks[i].getMaxFill() + "mB")));
				} else {
					
					if(world.getTileEntity(new BlockPos(pos[0], pos[1] - 3, pos[2])) instanceof TileEntityMachineFractionTower) {
						player.sendMessage(new TextComponentTranslation("chat.fractioning.onlybottom"));
					} else {
						FluidType type = ((IItemFluidIdentifier) player.getHeldItem(hand).getItem()).getType(world, pos[0], pos[1], pos[2], player.getHeldItem(hand));
						frac.tanks[0].setTankType(type);
						frac.markDirty();
						player.sendMessage(new TextComponentTranslation("chat.fractioning.changedto", I18n.format(type.getConditionalName())));
					}
				}
				
				return true;
			}
			return false;
			
		} else {
			return true;
		}
	}
	
	@Override
	public void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		super.fillSpace(world, x, y, z, dir, o);
		
		x = x + dir.offsetX * o;
		z = z + dir.offsetZ * o;

		this.makeExtra(world, x + 1, y, z);
		this.makeExtra(world, x - 1, y, z);
		this.makeExtra(world, x, y, z + 1);
		this.makeExtra(world, x, y, z - 1);
	}

	@Override
	public void printHook(Pre event, World world, BlockPos pos) {
        BlockPos corePos = this.findCore(world, pos);

        if(corePos == null)
            return;
		
		TileEntity te = world.getTileEntity(corePos);
		
		if(!(te instanceof TileEntityMachineFractionTower frac))
			return;

		List<String> text = new ArrayList<>();

		for(int i = 0; i < frac.tanks.length; i++){
			if(frac.tanks[i] != null){
				text.add((i < 1 ? ("§a" + "-> ") : ("§c" + "<- ")) + "§r" + frac.tanks[i].getTankType().getLocalizedName() + ": " + frac.tanks[i].getFill() + "/" + frac.tanks[i].getMaxFill() + "mB");
			}
		}

		ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
	}
}