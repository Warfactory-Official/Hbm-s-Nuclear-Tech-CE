package com.hbm.entity.mob;

import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.*;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IRadiationImmune;
import com.hbm.items.ModItems;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

@AutoRegister(name = "entity_mob_gold_creeper", trackingRange = 80, eggColors = {0xECC136, 0x9E8B3E})
public class EntityCreeperGold extends EntityCreeper implements IRadiationImmune {

    public EntityCreeperGold(World world) {
        super(world);
    }

    @Override
    protected void explode() {
        if (!this.world.isRemote) {
            ExplosionVNT vnt = new ExplosionVNT(world, posX, posY, posZ, this.getPowered() ? 14 : 7, this);
            vnt.setBlockAllocator(new BlockAllocatorBulkie(60, this.getPowered() ? 32 : 16));
            vnt.setBlockProcessor(new BlockProcessorStandard().withBlockEffect(new BlockMutatorBulkie(Blocks.GOLD_ORE)));
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
        return Items.GUNPOWDER;
    }

    @Override
    protected void dropFewItems(boolean byPlayer, int looting) {
        int amount = byPlayer ? 5 + rand.nextInt(6 + looting * 2) : 3;
        for(int i = 0; i < amount; ++i) {
            this.entityDropItem(new ItemStack(ModItems.crystal_gold), 0F);
        }
    }
}
