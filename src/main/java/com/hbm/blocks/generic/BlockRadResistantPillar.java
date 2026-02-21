package com.hbm.blocks.generic;


import com.hbm.blocks.ModBlocks;
import com.hbm.handler.radiation.RadiationSystemNT;
import com.hbm.interfaces.IRadResistantBlock;
import com.hbm.util.I18nUtil;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class BlockRadResistantPillar extends BlockRotatedPillar implements IRadResistantBlock{

    public BlockRadResistantPillar(Material materialIn, String s, SoundType sound) {
        this(materialIn, s);
        this.setSoundType(sound);
    }

    public BlockRadResistantPillar(Material materialIn, String s) {
        super(materialIn);
        this.setTranslationKey(s);
        this.setRegistryName(s);

        ModBlocks.ALL_BLOCKS.add(this);


    }
    @Override
    public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
        RadiationSystemNT.markSectionForRebuild(worldIn, pos);
        super.onBlockAdded(worldIn, pos, state);
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        RadiationSystemNT.markSectionForRebuild(worldIn, pos);
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public void addInformation(ItemStack stack, World player, List<String> tooltip, ITooltipFlag advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add("§2[" + I18nUtil.resolveKey("trait.radshield") + "]");
        float hardness = this.getExplosionResistance(null);
        if(hardness > 50){
            tooltip.add("§6" + I18nUtil.resolveKey("trait.blastres", hardness));
        }
    }
}
