package com.hbm.entity.item;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityMinecartTNT;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

@AutoRegister(name = "entity_minecart_test", trackingRange = 1000)
public class EntityMinecartTest extends EntityMinecartTNT {

    public EntityMinecartTest(World world) {
        super(world);
    }

    public EntityMinecartTest(World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    @Override
    public @NotNull IBlockState getDisplayTile() {
        return ModBlocks.crate.getDefaultState();
    }
}
