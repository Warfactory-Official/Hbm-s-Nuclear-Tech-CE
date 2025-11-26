package com.hbm.items.gear;

import com.hbm.interfaces.IHasCustomModel;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class RedstoneSword extends ItemSword implements IHasCustomModel {

	
	//Pridenauer you damn bastard.
	//Drillgon200: ^^ What's this supposed to mean? No idea.
	//Alcater: No idea. I am adding a tooltip to pay some respect to this item.
	
	public static final ModelResourceLocation rsModel = new ModelResourceLocation("hbm:redstone_sword", "inventory");
	
	public RedstoneSword(ToolMaterial t, String s){
		super(t);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setCreativeTab(MainRegistry.controlTab);
		ModItems.ALL_ITEMS.add(this);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public boolean isFull3D() {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flagIn) {
		list.add("§5§lVery First NTM Item");
	}

	
	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(worldIn.isRemote){
			return EnumActionResult.PASS;
		}
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		
		if (facing.ordinal() == 0)
        {
            --y;
        }

        if (facing.ordinal() == 1)
        {
            ++y;
        }

        if (facing.ordinal() == 2)
        {
            --z;
        }

        if (facing.ordinal() == 3)
        {
            ++z;
        }

        if (facing.ordinal() == 4)
        {
            --x;
        }

        if (facing.ordinal() == 5)
        {
            ++x;
        }
        BlockPos editpos = new BlockPos(x, y, z);
        if(!player.canPlayerEdit(editpos, facing, player.getHeldItem(hand))){
        	return EnumActionResult.PASS;
        } else {
        	if(worldIn.isAirBlock(editpos) && worldIn.isBlockFullCube(pos)){
        		 worldIn.playSound(x + 0.5D, y + 0.5D, z + 0.5D, SoundEvents.BLOCK_STONE_BREAK, SoundCategory.BLOCKS, 1.0F, itemRand.nextFloat() * 0.4F + 0.8F, false);
                 worldIn.setBlockState(editpos, Blocks.REDSTONE_WIRE.getDefaultState(), 1);
                 player.getHeldItem(hand).damageItem(14, player);
             	if(player.getHeldItem(hand).getItemDamage() >= player.getHeldItem(hand).getMaxDamage()){
             		player.getHeldItem(hand).shrink(1);
             	}
        	}
        	
        }
		return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
	}

	@Override
	public ModelResourceLocation getResourceLocation() {
		return rsModel;
	}

    @Override
    public void bakeModel(ModelBakeEvent event) {

    }

    @Override
    public void registerSprite(TextureMap map) {

    }
}
