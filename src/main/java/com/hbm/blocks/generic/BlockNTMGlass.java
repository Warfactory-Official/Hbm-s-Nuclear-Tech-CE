package com.hbm.blocks.generic;

import com.hbm.blocks.ModBlocks;
import com.hbm.handler.radiation.RadiationSystemNT;
import com.hbm.interfaces.IRadResistantBlock;
import com.hbm.util.I18nUtil;
import net.minecraft.block.BlockBreakable;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;
import java.util.Random;

public class BlockNTMGlass extends BlockBreakable implements IRadResistantBlock {

	BlockRenderLayer layer;
	private final boolean doesDrop;
	private final boolean isRadResistant;

	public BlockNTMGlass(Material materialIn, BlockRenderLayer layer, String s) {
		this(materialIn, layer, false, s);
	}

	public BlockNTMGlass(Material materialIn, BlockRenderLayer layer, boolean doesDrop, String s) {
		this(materialIn, layer, doesDrop, false, s);
	}

	public BlockNTMGlass(Material materialIn, BlockRenderLayer layer, boolean doesDrop, boolean isRadResistant, String s) {
		super(materialIn, false);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.layer = layer;
		this.doesDrop = doesDrop;
		this.isRadResistant = isRadResistant;
        lightOpacity = 0;
        translucent = true;
		ModBlocks.ALL_BLOCKS.add(this);
	}

	@Override
	public int quantityDropped(Random random){
		return doesDrop ? 1 : 0;
	}

	@Override
	public void onBlockAdded(World worldIn, BlockPos pos, IBlockState state) {
		if(this.isRadResistant){
			RadiationSystemNT.markSectionForRebuild(worldIn, pos);
		}
		super.onBlockAdded(worldIn, pos, state);
	}

    @Override
    public boolean isBlockNormalCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean causesSuffocation(IBlockState state) {
        return false;
    }

	@Override
	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		if(this.isRadResistant){
			RadiationSystemNT.markSectionForRebuild(worldIn, pos);
		}
		super.breakBlock(worldIn, pos, state);
	}

	@Override
	public int quantityDropped(IBlockState state, int fortune, Random random) {
		return doesDrop ? 1 : 0;
	}

	@Override
	public BlockRenderLayer getRenderLayer() {
		return layer;
	}

	@Override
    public boolean canSilkHarvest(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
        return true;
    }

	@Override
	public boolean isRadResistant(World worldIn, BlockPos blockPos){
		return this.isRadResistant;
	}

	@Override
	public void addInformation(ItemStack stack, World player, List<String> tooltip, ITooltipFlag advanced) {
		super.addInformation(stack, player, tooltip, advanced);
		float hardness = this.getExplosionResistance(null);
		if(this.isRadResistant){
			tooltip.add("§2[" + I18nUtil.resolveKey("trait.radshield") + "]");
		}
		if(hardness > 50){
			tooltip.add("§6" + I18nUtil.resolveKey("trait.blastres", hardness));
		}
	}
}
