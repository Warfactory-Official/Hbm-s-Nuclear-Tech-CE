package com.hbm.items.weapon;

import com.hbm.entity.grenade.EntityDisperserCanister;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemFluidTank;
import com.hbm.util.I18nUtil;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ItemDisperser extends ItemFluidTank {
    public ItemDisperser(String s, int cap) {
        super(s, cap);
        // oh fuck it, I'll do a sprite copy
        // if(this == ModItems.glyphid_gland) overlayTextureLocation = new ResourceLocation(RefStrings.MODID, ROOT_PATH + "fluid_identifier_overlay");
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand handIn) {
        ItemStack stack = player.getHeldItem(handIn);
        if (!player.capabilities.isCreativeMode) stack.shrink(1);

        world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_SNOWBALL_THROW, SoundCategory.NEUTRAL, 0.5F,
                0.4F / (itemRand.nextFloat() * 0.4F + 0.8F));

        if (!world.isRemote) {

            EntityDisperserCanister canister = new EntityDisperserCanister(world, player, handIn);
            canister.setType(Item.getIdFromItem(this));
            canister.setFluid(stack.getItemDamage());
            world.spawnEntity(canister);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (tab != this.getCreativeTab() && tab != CreativeTabs.SEARCH) return;
        FluidType[] order = Fluids.getInNiceOrder();

        for (int i = 1; i < order.length; ++i) {
            FluidType type = order[i];
            int id = type.getID();
            if (type.isDispersable() && this == ModItems.disperser_canister) {
                items.add(new ItemStack(ModItems.disperser_canister, 1, id));
            } else if (type == Fluids.PHEROMONE || type == Fluids.SULFURIC_ACID && this == ModItems.glyphid_gland) {
                items.add(new ItemStack(ModItems.glyphid_gland, 1, id));
            }

        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public String getItemStackDisplayName(ItemStack stack) {
        return I18nUtil.resolveKey(getTranslationKey() + ".name", Fluids.fromID(stack.getItemDamage()).getLocalizedName());
    }
}
