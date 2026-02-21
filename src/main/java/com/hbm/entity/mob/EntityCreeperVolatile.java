package com.hbm.entity.mob;

import com.hbm.blocks.ModBlocks;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.*;
import com.hbm.interfaces.AutoRegister;
import com.hbm.items.ModItems;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

@AutoRegister(name = "entity_mob_volatile_creeper", trackingRange = 80, eggColors = {0x000020, 0x2D2D72})
public class EntityCreeperVolatile extends EntityCreeper {

    public EntityCreeperVolatile(World world) {
        super(world);
    }

    @Override
    protected void explode() {
        if (!this.world.isRemote) {
            ExplosionVNT vnt = new ExplosionVNT(world, posX, posY, posZ, this.getPowered() ? 14 : 7, this);
            vnt.setBlockAllocator(new BlockAllocatorBulkie(60, this.getPowered() ? 32 : 16));
            vnt.setBlockProcessor(new BlockProcessorStandard().withBlockEffect(new BlockMutatorBulkie(ModBlocks.block_slag, 1)));
            vnt.setEntityProcessor(new EntityProcessorStandard().withRangeMod(0.5F));
            vnt.setPlayerProcessor(new PlayerProcessorStandard());
            vnt.setSFX(new ExplosionEffectStandard());
            vnt.explode();
            this.setDead();
        }
    }

    @Override
    public boolean getCanSpawnHere() {
        return super.getCanSpawnHere() && this.posY <= 40 && this.dimension == 0;
    }

    @Nullable
    protected ResourceLocation getLootTable() {
        return null;
    }

    @Override
    protected Item getDropItem() {
        //mlbv: 1.12.2 uses lootTable to drop gunpowder, since we set it to null we have to manually drop it
        //in case you are wondering: if I don't set lootTable to null, dropFewItems won't be called
        return Items.GUNPOWDER;
    }

    @Override
    protected void dropFewItems(boolean byPlayer, int looting) {
        this.entityDropItem(new ItemStack(ModItems.sulfur, 2 + rand.nextInt(3)), 0F);
        this.entityDropItem(new ItemStack(ModItems.stick_tnt, 1 + rand.nextInt(2)), 0F);
    }
}
