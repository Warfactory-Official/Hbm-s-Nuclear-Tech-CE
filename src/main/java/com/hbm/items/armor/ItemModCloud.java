package com.hbm.items.armor;

import com.google.common.collect.Multimap;
import com.hbm.handler.ArmorModHandler;
import com.hbm.interfaces.IArmorModDash;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;
import java.util.UUID;

public class ItemModCloud extends ItemArmorMod implements IArmorModDash {

    private static final UUID speed = UUID.fromString("1d11e63e-28c4-4e14-b09f-fe0bd1be708f");

    public ItemModCloud(String s) {
        super(ArmorModHandler.plate_only, false, true, false, false, s);
    }

    @Override
    public Multimap<String, AttributeModifier> getModifiers(EntityEquipmentSlot slot, ItemStack armor){
        Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(slot, armor);
        multimap.put(SharedMonsterAttributes.MOVEMENT_SPEED.getName(), new AttributeModifier(speed, "CLOUD SPEED", 0.125, 2));
        return multimap;
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {

        list.add(TextFormatting.WHITE + "Grants horizontal dashes");
        list.add("");
        super.addInformation(stack, worldIn, list, flagIn);
    }

    @Override
    public void addDesc(List<String> list, ItemStack stack, ItemStack armor) {
        list.add(TextFormatting.RED + "  " + stack.getDisplayName() + " (Dashes)");
    }

    public int getDashes() {
        return 3;
    }
}
