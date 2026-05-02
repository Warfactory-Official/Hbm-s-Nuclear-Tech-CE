package com.hbm.items.bomb;

import com.hbm.items.ItemBase;
import com.hbm.main.MainRegistry;
import com.hbm.util.I18nUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

import java.util.List;

public class ItemTsar extends ItemBase {

    public ItemTsar(String s) {
        super(s);
        this.setCreativeTab(MainRegistry.nukeTab);
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flagIn) {
        list.add(I18nUtil.resolveKey("desc.usedin"));
        list.add(" "+ I18nUtil.resolveKey("tile.nuke_tsar.name"));
        super.addInformation(stack, world, list, flagIn);
    }
}
