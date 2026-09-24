package com.hbm.blocks.bomb;

import com.hbm.entity.item.EntityTNTPrimedBase;

import com.hbm.render.block.BlockBakeFrame;
import net.minecraft.world.World;

public class BlockDynamite extends BlockTNTBase {

    private static final BlockBakeFrame frame = BlockBakeFrame.sideTopBottom("dynamite_side", "dynamite_top", "dynamite_bottom");

    public BlockDynamite(String s) {
        super(s, frame, frame);
    }

    @Override
    public void explodeEntity(World world, double x, double y, double z, EntityTNTPrimedBase entity) {
        world.createExplosion(entity, x, y, z, 8F, true);
    }
}
