package com.hbm.blocks.generic;

import com.hbm.hazard.HazardSystem;
import com.hbm.render.block.BlockBakeFrame;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.stream.IntStream;

/**
 * A hazard block with metadata support.
 */
public class BlockHazardMeta extends BlockMeta {


    public BlockHazardMeta(Material mat, SoundType type, String registryName, String locPrefix, BlockBakeFrame.BlockForm bForm, short metaCount) {
        super(mat, type, registryName, metaCount);
        if (metaCount < 0 || metaCount > 15) {
            throw new IllegalArgumentException(String.format("metaCount must be between 0 and 15 (inclusive), in %s", registryName));
        }
        this.blockFrames = generateBlockFrames(registryName, locPrefix, bForm);
    }

    protected BlockBakeFrame[] generateBlockFrames(String registryName, String locPrefix, BlockBakeFrame.BlockForm blockForm) {
        String locTemplate = locPrefix + registryName + "%s%d";
        return IntStream.range(0, META_COUNT)
                .mapToObj(id -> {
                    switch (blockForm) {
                        case ALL -> {
                            return BlockBakeFrame.cubeAll(String.format(locTemplate, "_", id));
                        }
                        case BOTTOM_TOP -> {
                            return BlockBakeFrame.cubeBottomTop(
                                    String.format(locTemplate, "_top_", id),
                                    String.format(locTemplate, "_side_", id),
                                    String.format(locTemplate, "_bottom_", id)
                            );
                        }
                        case COLUMN -> {
                            return BlockBakeFrame.column(
                                    String.format(locTemplate, "_top_", id),
                                    String.format(locTemplate, "_side_", id)
                            );
                        }
                        default -> throw new IllegalStateException("Unexpected value: " + blockForm);
                    }
                })
                .toArray(BlockBakeFrame[]::new);
    }

    @Override
    public void onEntityWalk(World worldIn, BlockPos pos, Entity entity) {
        if (!(entity instanceof EntityLivingBase)) return;

        HazardSystem.applyHazards(this, (EntityLivingBase)entity);
    }

    @Override
    public void onEntityCollision(World worldIn, BlockPos pos, IBlockState state, Entity entity) {
        if (!(entity instanceof EntityLivingBase)) return;

        HazardSystem.applyHazards(this, (EntityLivingBase) entity);
    }
}
