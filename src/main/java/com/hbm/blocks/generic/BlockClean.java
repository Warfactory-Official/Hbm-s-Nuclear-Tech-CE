package com.hbm.blocks.generic;

import com.hbm.blocks.BlockBase;
import com.hbm.blocks.ModBlocks;
import com.hbm.config.RadiationConfig;
import com.hbm.util.I18nUtil;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class BlockClean extends BlockBase {

    public BlockClean(Material m, String s) {
        super(m, s);
    }

    @Override
    public void addInformation(ItemStack stack, World player, List<String> tooltip, ITooltipFlag advanced) {
        if(!RadiationConfig.enableContaminationOnGround) return;
        tooltip.add("§3[" + I18nUtil.resolveKey("trait.cleanroom") + "§3]");
        tooltip.add(" §b" + I18nUtil.resolveKey("trait.cleanroom.desc"));
        super.addInformation(stack, player, tooltip, advanced);
    }
}
