package com.hbm.items.machine;

import com.hbm.Tags;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.ModSoundTypes;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ModItems;
import com.hbm.tileentity.network.TileEntityPipeBaseNT;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class ItemFFFluidDuct extends Item {
	public static final ModelResourceLocation ductLoc = new ModelResourceLocation(
			Tags.MODID + ":ff_fluid_duct", "inventory");
	private static final List<ItemFFFluidDuct> INSTANCES = new ArrayList<>();

	public ItemFFFluidDuct(String s) {
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setHasSubtypes(true);
		
		ModItems.ALL_ITEMS.add(this);

		if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
			INSTANCES.add(this);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		if (this.isInCreativeTab(tab)) {
			FluidType[] order = Fluids.getInNiceOrder();
			for (int i = 1; i < order.length; ++i) {
				if (!order[i].hasNoID()) {
					items.add(new ItemStack(this, 1, order[i].getID()));
				}
			}
		}
	}


	@SideOnly(Side.CLIENT)
	public static void registerColorHandlers(ColorHandlerEvent.Item evt) {
		ItemColors itemColors = evt.getItemColors();
		IItemColor handler = new ItemFFFluidDuct.FluidDuctColorHandler();

		for (ItemFFFluidDuct item : INSTANCES) {
			itemColors.registerItemColorHandler(handler, item);
		}
	}

	@SideOnly(Side.CLIENT)
	private static class FluidDuctColorHandler implements IItemColor {
		@Override
		public int colorMultiplier(ItemStack stack, int tintIndex) {
			if (tintIndex == 1) {
				int color = Fluids.fromID(stack.getItemDamage()).getColor();
				return color < 0 ? 0xFFFFFF : color;
			}
			return 0xFFFFFF;
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public String getItemStackDisplayName(ItemStack stack) {
		String s = ("" + I18n.format(this.getTranslationKey() + ".name")).trim();
		String s1 = ("" + I18n.format(Fluids.fromID(stack.getItemDamage()).getConditionalName())).trim();

        if (s1 != null)
        {
            s = s + " " + s1;
        }

        return s;
	}
	
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(!world.getBlockState(pos).getBlock().isReplaceable(world, pos)){
			pos = pos.offset(facing);
			if (!world.isAirBlock(pos))
            {
                return EnumActionResult.FAIL;
            }
		}
		ItemStack stack = player.getHeldItem(hand);
		if (!player.canPlayerEdit(pos, facing, stack))
        {
            return EnumActionResult.FAIL;
        }
        else
        {
            world.setBlockState(pos, ModBlocks.fluid_duct_neo.getDefaultState());
            if(world.getTileEntity(pos) instanceof TileEntityPipeBaseNT) {
            	((TileEntityPipeBaseNT)world.getTileEntity(pos)).setType(Fluids.fromID(stack.getItemDamage()));;
            }
            stack.shrink(1);
            world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), ModSoundTypes.pipe.getPlaceSound(), SoundCategory.PLAYERS, 1F, 0.8F + world.rand.nextFloat() * 0.2F);

            return EnumActionResult.SUCCESS;
        }
	}
	
	public static ItemStack getStackFromFluid(FluidType f, int amount){
		return new ItemStack(ModItems.fluid_duct, amount, f.getID());
	}
	
	public static ItemStack getStackFromFluid(FluidType f){
		return getStackFromFluid(f, 1);
	}
}
