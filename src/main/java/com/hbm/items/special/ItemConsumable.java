package com.hbm.items.special;

import com.hbm.handler.ConsumableHandler;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.*;

public class ItemConsumable extends Item {

    Random rand = new Random();
    private boolean hasEffect = false;

    public ItemConsumable(final String s) {
        this.setTranslationKey(s);
        this.setRegistryName(s);
        this.setCreativeTab(MainRegistry.controlTab);
        ModItems.ALL_ITEMS.add(this);

    }

    public ItemConsumable setEffect() {
        this.hasEffect = true;
        return this;
    }

    @Override
    public boolean hasEffect(final ItemStack stack) {
        return this.hasEffect || super.hasEffect(stack);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(final World world, final EntityPlayer player, final EnumHand hand) {
        return ConsumableHandler.handleItemUse(world, player, hand, this);
    }

    @Override
    public boolean onLeftClickEntity(final ItemStack stack, final EntityPlayer attacker, final Entity target) {
        if (!(target instanceof EntityLivingBase))
            return false;
        return ConsumableHandler.handleHit(stack, attacker, (EntityLivingBase) target);
    }
    static public final Map<String, List<String>> tooltipMap = new HashMap<>();
    static {
        tooltipMap.put("syringe_antidote", Collections.singletonList("Removes all potion effects"));
        tooltipMap.put("syringe_awesome", Collections.singletonList("§2Every good effect for 50 seconds"));
        tooltipMap.put("syringe_metal_stimpak", Collections.singletonList("§aHeals 2.5 hearts"));
        tooltipMap.put("syringe_metal_medx", Collections.singletonList("§dResistance III for 4 minutes"));
        tooltipMap.put("syringe_metal_psycho", Arrays.asList("§dResistance I for 2 minutes", "§6Strength I for 2 minutes"));
        tooltipMap.put("syringe_metal_super", Arrays.asList("§aHeals 25 hearts", "§bSlowness I for 10 seconds"));
        tooltipMap.put("syringe_poison", Collections.singletonList("Deadly"));
        tooltipMap.put("syringe_taint", Arrays.asList("§5Tainted I for 60 seconds", "§eNausea I for 5 seconds", "Cloud damage + taint = ghoulified effect"));
        tooltipMap.put("med_bag", Arrays.asList("§aFull heal, regardless of max health", "Removes negative effects"));
        tooltipMap.put("gas_mask_filter_mono", Collections.singletonList("Repairs worn monoxide mask"));
        tooltipMap.put("syringe_mkunicorn", Collections.singletonList(TextFormatting.RED + "?"));
    }

    @Override
    public void addInformation(final ItemStack stack, final World worldIn, final List<String> tooltip, final ITooltipFlag flagIn) {
        final ResourceLocation registryName = stack.getItem().getRegistryName();
        if (registryName == null || !registryName.getNamespace().equals("hbm")) {
            return;
        }
        final List<String> lines = tooltipMap.get(registryName.getPath());
        if (lines != null) {
            tooltip.addAll(lines);
        }
    }
}
