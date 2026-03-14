package com.hbm.blocks.bomb;

import com.hbm.blocks.ModBlocks;
import com.hbm.entity.item.EntityTNTPrimedBase;
import com.hbm.explosion.ExplosionNukeSmall;

import com.hbm.render.block.BlockBakeFrame;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockFissureBomb extends BlockTNTBase {

    private static final BlockBakeFrame frame = BlockBakeFrame.sideTopBottom("fissure_bomb_side", "fissure_bomb_top", "fissure_bomb_bottom");

    public BlockFissureBomb(String s) {
        super(s, frame, frame);
    }

    @Override
    public void explodeEntity(World world, double x, double y, double z, EntityTNTPrimedBase entity) {
        ExplosionNukeSmall.explode(world, x, y, z, ExplosionNukeSmall.PARAMS_MEDIUM);

        int range = 5;

        for(int i = -range; i <= range; i++) {
            for(int j = -range; j <= range; j++) {
                for(int k = -range; k <= range; k++) {

                    int a = (int) Math.floor(x + i);
                    int b = (int) Math.floor(y + j);
                    int c = (int) Math.floor(z + k);
                    BlockPos pos = new BlockPos(a, b, c);
                    Block block = world.getBlockState(pos).getBlock();

                    if(block == ModBlocks.ore_bedrock_block) {
                        world.setBlockState(pos, ModBlocks.ore_volcano.getDefaultState());
                    } else if(block == ModBlocks.ore_bedrock_oil) {
                        world.setBlockState(pos, Blocks.BEDROCK.getDefaultState());
                    }
                }
            }
        }
    }
}
