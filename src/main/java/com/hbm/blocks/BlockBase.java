package com.hbm.blocks;

import com.hbm.main.MainRegistry;
import com.hbm.util.I18nUtil;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;

public class BlockBase extends Block {

    protected boolean noSpawn = false;

    public BlockBase setNoSpawn() {
        this.noSpawn = true;
        return this;
    }

    public BlockBase(Material m, String s) {
        super(m);
        setTranslationKey(s);
        setRegistryName(s);
        setHarvestLevel("pickaxe", 0);
        setCreativeTab(MainRegistry.controlTab);
        ModBlocks.ALL_BLOCKS.add(this);
    }

    public BlockBase(Material material) {
        super(material);
        ModBlocks.ALL_BLOCKS.add(this);
    }

    public BlockBase() {
        super(Material.ROCK);
        ModBlocks.ALL_BLOCKS.add(this);
    }

    public BlockBase(Material m, SoundType sound, String s) {
        this(m, s);

        setSoundType(sound);
    }

    @Override
    public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos, SpawnPlacementType type) {
        return !noSpawn && super.canCreatureSpawn(state, world, pos, type);
    }

    @Override
    public void addInformation(ItemStack stack, World player, List<String> list, ITooltipFlag advanced) {
        if (stack.getItem() == Item.getItemFromBlock(ModBlocks.meteor_battery)) {
            list.add(I18nUtil.resolveKey("desc.teslacoils"));
        }

        float hardness = getExplosionResistance(null);
        if (hardness > 50) {
            list.add(TextFormatting.GOLD + I18nUtil.resolveKey("trait.blastres", hardness));
        }

        if (noSpawn) {
            list.add(TextFormatting.RED + I18nUtil.resolveKey("tile.nospawn"));
        }
    }
}
