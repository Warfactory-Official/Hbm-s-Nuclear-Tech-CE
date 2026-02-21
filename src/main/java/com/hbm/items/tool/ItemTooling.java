package com.hbm.items.tool;

import com.hbm.api.block.IToolable;
import com.hbm.api.block.IToolable.ToolType;
import com.hbm.items.ItemBakedBase;
import com.hbm.main.MainRegistry;
import com.hbm.util.I18nUtil;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class ItemTooling extends ItemBakedBase {

	protected ToolType type;
	
	public ItemTooling(ToolType type, int dura, String s) {
		super(s);
		this.setMaxStackSize(1);
		this.setFull3D();
		this.setCreativeTab(MainRegistry.consumableTab);
		this.setMaxDamage(dura);
		this.type = type;
		
		type.register(new ItemStack(this));
	}
	
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		Block b = world.getBlockState(pos).getBlock();
		
		if(b instanceof IToolable) {
			if(((IToolable)b).onScrew(world, player, pos.getX(), pos.getY(), pos.getZ(), facing, hitX, hitY, hitZ, hand, this.type)) {
				
				if(this.getMaxDamage(player.getHeldItem(hand)) > 0)
					player.getHeldItem(hand).damageItem(1, player);
				
				return EnumActionResult.SUCCESS;
			}
		}
		return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		if(type == ToolType.SCREWDRIVER){
			tooltip.add(I18nUtil.resolveKey("desc.screwdriver1"));
		}
	}

	public ToolType getType(){
		return type;
	}
}
