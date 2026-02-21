package com.hbm.world.feature;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockGlyphid;
import com.hbm.blocks.generic.BlockGlyphid.Type;
import com.hbm.blocks.generic.BlockGlyphidSpawner;
import com.hbm.util.LootGenerator;
import com.hbm.world.phased.AbstractPhasedStructure;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class GlyphidHive extends AbstractPhasedStructure {

    public static final int[][][] schematicSmall = new int[][][] {
            {
                    {0,0,0,0,0,0,0,0,0,0,0},
                    {0,0,0,0,0,0,0,0,0,0,0},
                    {0,0,0,0,0,0,0,0,0,0,0},
                    {0,0,0,0,0,0,0,0,0,0,0},
                    {0,0,0,0,1,1,1,0,0,0,0},
                    {0,0,0,0,1,1,1,0,0,0,0},
                    {0,0,0,0,1,1,1,0,0,0,0},
                    {0,0,0,0,0,0,0,0,0,0,0},
                    {0,0,0,0,0,0,0,0,0,0,0},
                    {0,0,0,0,0,0,0,0,0,0,0},
                    {0,0,0,0,0,0,0,0,0,0,0},
            },
            {
                    {0,0,0,0,0,0,0,0,0,0,0},
                    {0,0,0,0,0,0,0,0,0,0,0},
                    {0,0,0,0,1,1,1,0,0,0,0},
                    {0,0,0,1,1,1,1,1,0,0,0},
                    {0,0,1,1,1,1,1,1,1,0,0},
                    {0,0,1,1,1,1,1,1,1,0,0},
                    {0,0,1,1,1,1,1,1,1,0,0},
                    {0,0,0,1,1,1,1,1,0,0,0},
                    {0,0,0,0,1,1,1,0,0,0,0},
                    {0,0,0,0,0,0,0,0,0,0,0},
                    {0,0,0,0,0,0,0,0,0,0,0},
            },
            {
                    {0,0,0,0,0,0,0,0,0,0,0},
                    {0,0,0,0,1,1,1,0,0,0,0},
                    {0,0,1,1,1,1,1,1,1,0,0},
                    {0,0,1,1,1,1,1,1,1,0,0},
                    {0,1,1,1,3,3,3,1,1,1,0},
                    {0,1,1,1,3,3,3,1,1,1,0},
                    {0,1,1,1,3,3,3,1,1,1,0},
                    {0,0,1,1,1,1,1,1,1,0,0},
                    {0,0,1,1,1,1,1,1,1,0,0},
                    {0,0,0,0,1,1,1,0,0,0,0},
                    {0,0,0,0,0,0,0,0,0,0,0},
            },
            {
                    {0,0,0,0,0,0,0,0,0,0,0},
                    {0,0,0,0,1,1,1,0,0,0,0},
                    {0,0,1,1,1,1,1,1,1,0,0},
                    {0,0,1,1,2,2,2,1,1,0,0},
                    {0,1,1,2,2,2,2,2,1,1,0},
                    {0,1,1,2,2,2,2,2,1,1,0},
                    {0,1,1,2,2,2,2,2,1,1,0},
                    {0,0,1,1,2,2,2,1,1,0,0},
                    {0,0,1,1,1,1,1,1,1,0,0},
                    {0,0,0,0,1,1,1,0,0,0,0},
                    {0,0,0,0,0,0,0,0,0,0,0},
            },
            {
                    {0,0,0,0,0,0,0,0,0,0,0},
                    {0,0,0,0,1,1,1,0,0,0,0},
                    {0,0,1,1,1,1,1,1,1,0,0},
                    {0,0,1,1,1,1,1,1,1,0,0},
                    {0,1,1,1,1,1,1,1,1,1,0},
                    {0,1,1,1,1,1,1,1,1,1,0},
                    {0,1,1,1,1,1,1,1,1,1,0},
                    {0,0,1,1,1,1,1,1,1,0,0},
                    {0,0,1,1,1,1,1,1,1,0,0},
                    {0,0,0,0,1,1,1,0,0,0,0},
                    {0,0,0,0,0,0,0,0,0,0,0},
            }
    };
    public static final GlyphidHive INFECTED = new GlyphidHive(true, true);
    public static final GlyphidHive INFECTED_NOLOOT = new GlyphidHive(true, false);
    public static final GlyphidHive NORMAL = new GlyphidHive(false, true);
    public static final GlyphidHive NORMAL_NOLOOT = new GlyphidHive(false, false);

    private final boolean infected;
    private final boolean loot;

    private GlyphidHive(boolean infected, boolean loot) {
        this.infected = infected;
        this.loot = loot;
    }

    public static void generate(World world, int x, int y, int z, Random rand, boolean infected, boolean loot) {
        getInstance(infected, loot).generate(world, rand, new BlockPos(x, y, z));
    }

    private static GlyphidHive getInstance(boolean infected, boolean loot) {
        return infected ? (loot ? INFECTED : INFECTED_NOLOOT) : (loot ? NORMAL : NORMAL_NOLOOT);
    }

    /**
     * force = true
     */
    public static void generateSmall(World world, int x, int y, int z, Random rand, boolean infected, boolean loot) {
        getInstance(infected, loot).generate(world, rand, new BlockPos(x, y, z), true);
    }

    @Override
    protected boolean isCacheable() {
        return false;
    }

    @Override
    protected void buildStructure(@NotNull LegacyBuilder builder, @NotNull Random rand) {
        var baseState = ModBlocks.glyphid_base.getDefaultState().withProperty(BlockGlyphid.TYPE, this.infected ? Type.INFESTED : Type.BASE);
        var spawnerState = ModBlocks.glyphid_spawner.getDefaultState()
                                                    .withProperty(BlockGlyphidSpawner.TYPE, this.infected ? BlockGlyphidSpawner.Type.INFESTED : BlockGlyphidSpawner.Type.BASE);

        for (int i = 0; i < 11; i++) {
            for (int j = 0; j < 5; j++) {
                for (int k = 0; k < 11; k++) {
                    int block = schematicSmall[4 - j][i][k];
                    BlockPos relPos = new BlockPos(i - 5, j - 2, k - 5);

                    switch (block) {
                        case 1 -> builder.setBlockState(relPos, baseState);
                        case 2 -> {
                            if (rand.nextInt(3) == 0) {
                                builder.setBlockState(relPos, spawnerState);
                            } else {
                                builder.setBlockState(relPos, baseState);
                            }
                        }
                        case 3 -> {
                            int r = rand.nextInt(3);
                            if (r == 0) {
                                builder.setBlockState(relPos, Blocks.SKULL.getStateFromMeta(1), (world, random, pos, te) -> {
                                    if (te instanceof TileEntitySkull skull) {
                                        skull.setSkullRotation(random.nextInt(16));
                                    }
                                });
                            } else if (r == 1) {
                                builder.setBlockState(relPos, ModBlocks.deco_loot.getDefaultState(), (world, random, pos, te) -> LootGenerator.lootBones(world, pos.getX(), pos.getY(), pos.getZ()));
                            } else {
                                if (this.loot) {
                                    builder.setBlockState(relPos, ModBlocks.deco_loot.getDefaultState(), (world, random, pos, te) -> LootGenerator.lootGlyphidHive(world, pos.getX(), pos.getY(), pos.getZ()));
                                } else {
                                    builder.setBlockState(relPos, baseState);
                                }
                            }
                        }
                        default -> {
                        }
                    }
                }
            }
        }
    }
}
