package com.hbm.blocks.bomb;

import com.hbm.entity.item.EntityTNTPrimedBase;
import com.hbm.render.block.BlockBakeFrame;
import net.minecraft.world.World;

public class BlockC4 extends BlockTNTBase{

    private static final BlockBakeFrame frame = BlockBakeFrame.sideTopBottom("c4_side", "c4_top", "c4_bottom");

    public BlockC4(String s) {
        super(s, frame, frame);
    }

    @Override
    public void explodeEntity(World world, double x, double y, double z, EntityTNTPrimedBase entity) {
        world.createExplosion(entity, x, y, z, 15F, true);
    }

}
