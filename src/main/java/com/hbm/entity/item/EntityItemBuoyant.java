package com.hbm.entity.item;

import com.hbm.interfaces.AutoRegister;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
@AutoRegister(name = "entity_item_buoyant", trackingRange = 100)
public class EntityItemBuoyant extends EntityItem {

    public EntityItemBuoyant(World worldIn) {
        super(worldIn);
    }

    public EntityItemBuoyant(World worldIn, double x, double y, double z, ItemStack stack) {
        super(worldIn, x, y, z, stack);
    }

    @Override
    public void onUpdate() {
        int x = MathHelper.floor(this.posX);
        int y = MathHelper.floor(this.posY - 0.0625D);
        int z = MathHelper.floor(this.posZ);
        BlockPos pos = new BlockPos(x, y, z);
        IBlockState state = this.world.getBlockState(pos);

        if (state.getMaterial() == Material.WATER && state.getBlock() instanceof BlockLiquid) {
            if (state.getValue(BlockLiquid.LEVEL) < 8) {
                this.motionY += 0.045D;
            }
        }

        super.onUpdate();
    }
}
