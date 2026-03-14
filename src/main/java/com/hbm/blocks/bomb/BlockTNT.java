package com.hbm.blocks.bomb;

import com.hbm.entity.item.EntityTNTPrimedBase;

import com.hbm.render.block.BlockBakeFrame;
import net.minecraft.world.World;

public class BlockTNT extends BlockTNTBase {

    private static final BlockBakeFrame frame = BlockBakeFrame.sideTopBottom("tnt_side", "tnt_top", "tnt_bottom");

    public BlockTNT(String s) {
        super(s, frame, frame);
    }

    @Override
    public void explodeEntity(World world, double x, double y, double z, EntityTNTPrimedBase entity) {
        world.createExplosion(entity, x, y, z, 10F, true);
    }
}
