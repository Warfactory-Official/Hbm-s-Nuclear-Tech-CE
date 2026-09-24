package com.hbm.blocks.bomb;

import com.hbm.entity.item.EntityTNTPrimedBase;

import com.hbm.render.block.BlockBakeFrame;
import net.minecraft.world.World;

public class BlockSemtex extends BlockTNTBase {

    private static final BlockBakeFrame frame = BlockBakeFrame.sideTopBottom("semtex_side", "semtex_top", "semtex_bottom");

    public BlockSemtex(String s) {
        super(s, frame, frame);
    }

    @Override
    public void explodeEntity(World world, double x, double y, double z, EntityTNTPrimedBase entity) {
        world.createExplosion(entity, x, y, z, 12F, true);
    }
}
