package com.hbm.blocks.machine;

import com.hbm.blocks.BlockDummyableMBB;
import com.hbm.blocks.ILookOverlay;
import com.hbm.lib.InventoryHelper;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IPersistentNBT;
import com.hbm.tileentity.machine.TileEntityMachineFENSU;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.oredict.OreDictionary;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
@Deprecated
public class MachineFENSU extends BlockDummyableMBB implements ILookOverlay {

	public MachineFENSU(Material materialIn, String s) {
		super(materialIn, s);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		if(meta >= 12)
			return new TileEntityMachineFENSU();
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {4, 0, 1, 1, 2, 2};
	}

	@Override
	public int getOffset() {
		return 1;
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos1, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(world.isRemote){
			return true;
		} else if(!player.isSneaking()){
			int[] pos = this.findCore(world, pos1.getX(), pos1.getY(), pos1.getZ());

			if(pos == null)
				return false;

			BlockPos corePos = new BlockPos(pos[0], pos[1], pos[2]);
			TileEntityMachineFENSU entity = (TileEntityMachineFENSU) world.getTileEntity(corePos);
			if(entity != null) {
				if(!player.getHeldItem(hand).isEmpty()){

					int[] ores = OreDictionary.getOreIDs(player.getHeldItem(hand));
					for(int ore : ores){
						String name = OreDictionary.getOreName(ore);
						//Why are these ones named differently
						if(name.equals("dyeLightBlue"))
							name = "dyeLight_Blue";
						if(name.equals("dyeLightGray"))
							name = "dyeSilver";
						if(name.length() > 3 && name.startsWith("dye")){
							try {
								EnumDyeColor color = EnumDyeColor.valueOf(name.substring(3, name.length()).toUpperCase());
								entity.color = color;
								entity.markDirty();
								world.notifyBlockUpdate(corePos, state, state, 2 | 4);
								if(!player.isCreative())
									player.getHeldItem(hand).shrink(1);
								return true;
							} catch(IllegalArgumentException e){}
						}
					}
				}
				FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos[0], pos[1], pos[2]);
			}
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
	public boolean hasComparatorInputOverride(IBlockState state){
		return true;
	}

	@Override
	public int getComparatorInputOverride(IBlockState blockState, World worldIn, BlockPos pos){
		
		TileEntity te = worldIn.getTileEntity(pos);
		
		if(!(te instanceof TileEntityMachineFENSU))
			return 0;
		
		TileEntityMachineFENSU battery = (TileEntityMachineFENSU) te;
		return (int)battery.getPowerRemainingScaled(15L);
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, list, flagIn);
		list.add("Change color using dyes");
		long charge = 0L;
		if(stack.hasTagCompound()){
			NBTTagCompound nbt = stack.getTagCompound();
			if(nbt.hasKey(IPersistentNBT.NBT_PERSISTENT_KEY)){
				charge = nbt.getCompoundTag(IPersistentNBT.NBT_PERSISTENT_KEY).getLong("power");
			}
		}

		if(charge == 0L){
			list.add("§c0§4/9.22EHE §c(0.0%)§r");
		}else {
			double percent = Math.round(1000D*((double)charge/(double)Long.MAX_VALUE))*0.1D;
			String color = "§e";
			String color2 = "§6"; 
			if(percent < 25){
				color = "§c";
				color2 = "§4";
			}else if(percent >= 75){
				color = "§a";
				color2 = "§2";
			}
			list.add(color+Library.getShortNumber(charge)+color2+"/9.22EHE "+color+"("+percent+"%)§r");
		}
	}

	@Override
	public void printHook(Pre event, World world, int x, int y, int z) {
			
		TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
		
		if(!(te instanceof TileEntityMachineFENSU))
			return;

		TileEntityMachineFENSU battery = (TileEntityMachineFENSU) te;
		List<String> text = new ArrayList();
		text.add("§6<> §rStored Energy: " + Library.getShortNumber(battery.power) + "/9.22EHE");
		if(battery.delta == 0)
			text.add("§e-- §r0HE/s");
		else if(battery.delta > 0)
			text.add("§a-> §r" + Library.getShortNumber(battery.delta) + "HE/s");
		else
			text.add("§c<- §r" + Library.getShortNumber(-battery.delta) + "HE/s");
		text.add("&["+Library.getColorProgress((double)battery.power/(double)Long.MAX_VALUE)+"&]    "+Library.getPercentage((double)battery.power/(double)Long.MAX_VALUE)+"%");
		ILookOverlay.printGeneric(event, getLocalizedName(), 0xffff00, 0x404000, text);
	}
}
