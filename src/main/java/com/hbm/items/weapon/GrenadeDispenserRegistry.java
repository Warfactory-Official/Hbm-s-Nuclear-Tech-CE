package com.hbm.items.weapon;

import com.hbm.items.ModItems;
import com.hbm.items.tool.ItemFertilizer;
import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class GrenadeDispenserRegistry {

    public static void registerDispenserBehaviors() {
    }

    public static void registerDispenserBehaviorFertilizer() {
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(ModItems.powder_fertilizer, new BehaviorDefaultDispenseItem() {

            private boolean dispenseSound = true;

            @Override
            protected ItemStack dispenseStack(IBlockSource source, ItemStack stack) {
                World world = source.getWorld();
                EnumFacing facing = (EnumFacing) source.getBlockState().getValue(BlockDispenser.FACING);
                BlockPos targetPos = source.getBlockPos().offset(facing);
                this.dispenseSound = ItemFertilizer.useFertillizer(stack, world, targetPos.getX(), targetPos.getY(), targetPos.getZ());
                return stack;
            }

            @Override
            protected void playDispenseSound(IBlockSource source) {
                World world = source.getWorld();
                BlockPos pos = source.getBlockPos();
                if (this.dispenseSound) {
                    world.playEvent(1000, pos, 0);
                } else {
                    world.playEvent(1001, pos, 0);
                }
            }
        });
    }
}
