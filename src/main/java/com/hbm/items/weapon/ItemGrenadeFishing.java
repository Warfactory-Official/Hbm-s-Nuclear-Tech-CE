package com.hbm.items.weapon;

import com.hbm.entity.item.EntityItemBuoyant;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.LootTableList;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.List;
import java.util.Random;

public class ItemGrenadeFishing extends ItemGenericGrenade {

    public ItemGrenadeFishing(int fuse, String s) {
        super(fuse, s);
    }

    @Override
    public void explode(Entity grenade, EntityLivingBase thrower, World worldIn, double x, double y, double z) {
        worldIn.newExplosion(null, x, y + 0.25D, z, 3.0F, false, false);

        BlockPos basePos = new BlockPos(x, y, z);

        for (int i = 0; i < 15; i++) {
            int rX = basePos.getX() + worldIn.rand.nextInt(15) - 7;
            int rY = basePos.getY() + worldIn.rand.nextInt(15) - 7;
            int rZ = basePos.getZ() + worldIn.rand.nextInt(15) - 7;
            BlockPos pos = new BlockPos(rX, rY, rZ);

            if (worldIn.getBlockState(pos).getMaterial() == Material.WATER) {
                ItemStack loot = getRandomLoot(worldIn.rand);
                if (loot != null && !loot.isEmpty()) {
                    EntityItemBuoyant item = new EntityItemBuoyant(worldIn, rX + 0.5D, rY + 0.5D, rZ + 0.5D, loot.copy());
                    item.motionY = 1.0D;
                    worldIn.spawnEntity(item);
                }
            }
        }
    }

    public static ItemStack getRandomLoot(Random rand) {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null) {
            return ItemStack.EMPTY;
        }

        WorldServer world = server.getWorld(0);
        if (world == null) {
            return ItemStack.EMPTY;
        }

        LootTable lootTable = world.getLootTableManager().getLootTableFromLocation(LootTableList.GAMEPLAY_FISHING);
        LootContext.Builder builder = new LootContext.Builder(world).withLuck(0.0F);
        List<ItemStack> loot = lootTable.generateLootForPools(rand, builder.build());

        return loot.isEmpty() ? ItemStack.EMPTY : loot.get(0);
    }

    @Override
    public int getMaxTimer() {
        return 60;
    }

    @Override
    public double getBounceMod() {
        return 0.5D;
    }
}
