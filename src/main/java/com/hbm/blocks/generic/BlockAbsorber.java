package com.hbm.blocks.generic;

import com.hbm.blocks.BlockEnumMeta;
import com.hbm.blocks.IBlockMulti;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.render.block.BlockBakeFrame;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Random;

public class BlockAbsorber extends BlockEnumMeta<BlockAbsorber.EnumAbsorberTier> implements IBlockMulti {

	public enum EnumAbsorberTier {

		BASE(2.5F, "absorber"), RED(10F, "absorber_red"), GREEN(100F, "absorber_green"), PINK(10000F, "absorber_pink");

		public final float absorbAmount;
		public final String textureName;

		public static final EnumAbsorberTier[] VALUES = values();

		EnumAbsorberTier(float absorb, String texture) {
			this.absorbAmount = absorb;
			this.textureName = texture;
		}
	}


	public BlockAbsorber(Material mat, String s) {
		super(mat, SoundType.METAL, s, EnumAbsorberTier.VALUES, true, true);
	}

	public EnumAbsorberTier getTier(int meta) {
		return EnumAbsorberTier.VALUES[rectify(meta)];
	}

	@Override
	public int getSubCount() {
		return EnumAbsorberTier.VALUES.length;
	}

	@Override
	public String getTranslationKey(ItemStack stack) {
		EnumAbsorberTier tier = getTier(stack.getItemDamage());
        return I18n.translateToLocal("tile.rad_absorber." + tier.name().toLowerCase());
	}

	@Override
	protected BlockBakeFrame[] generateBlockFrames(String registryName) {
		return Arrays.stream(blockEnum)
				.map(tier -> tier.textureName)
				.map(BlockBakeFrame::new)
				.toArray(BlockBakeFrame[]::new);
	}

	@Override
	public int tickRate(@NotNull World worldIn) {
		return 1;
	}

	@Override
	public void updateTick(@NotNull World world, @NotNull BlockPos pos, IBlockState state, @NotNull Random rand) {
		EnumAbsorberTier tier = getTier(state.getValue(META));
        ChunkRadiationManager.proxy.decrementRad(world, pos, tier.absorbAmount / 10);

        world.scheduleUpdate(pos, this, this.tickRate(world));
	}

	@Override
	public void onBlockAdded(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state) {
		super.onBlockAdded(worldIn, pos, state);
		worldIn.scheduleUpdate(pos, this, this.tickRate(worldIn));
	}

}
