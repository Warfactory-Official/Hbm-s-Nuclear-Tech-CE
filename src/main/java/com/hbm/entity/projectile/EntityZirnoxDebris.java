package com.hbm.entity.projectile;

import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.interfaces.AutoRegister;
import com.hbm.items.ModItems;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
@AutoRegister(name = "entity_zirnox_debris", trackingRange = 1000)
public class EntityZirnoxDebris extends EntityDebrisBase {

    public EntityZirnoxDebris(World world) {
        super(world);
    }

    public EntityZirnoxDebris(World world, double x, double y, double z, DebrisType type) {
        super(world, x, y, z);
        this.setType(type);
    }

    @Override
    public void onUpdate() {
        if (!world.isRemote) {
            if (this.getType() == DebrisType.ELEMENT) {
                BlockPos pos = new BlockPos(this.posX, this.posY, this.posZ);
                ChunkRadiationManager.proxy.incrementRad(world, pos, 20F, 150F);
            }
        }
        super.onUpdate();
    }

    @Override
    public boolean processInitialInteract(EntityPlayer player, EnumHand hand) {
        if (!world.isRemote && !isDead) {
            switch (this.getType()) {
                case BLANK:
                    if (player.inventory.addItemStackToInventory(new ItemStack(ModItems.debris_metal)))
                        this.setDead();
                    break;
                case ELEMENT:
                    if (player.inventory.addItemStackToInventory(new ItemStack(ModItems.debris_element)))
                        this.setDead();
                    break;
                case SHRAPNEL:
                    if (player.inventory.addItemStackToInventory(new ItemStack(ModItems.debris_shrapnel)))
                        this.setDead();
                    break;
                case GRAPHITE:
                    if (player.inventory.addItemStackToInventory(new ItemStack(ModItems.debris_graphite)))
                        this.setDead();
                    break;
                case CONCRETE:
                    if (player.inventory.addItemStackToInventory(new ItemStack(ModItems.debris_concrete)))
                        this.setDead();
                    break;
                case EXCHANGER:
                    if (player.inventory.addItemStackToInventory(new ItemStack(ModItems.debris_exchanger)))
                        this.setDead();
                    break;
            }

            player.inventoryContainer.detectAndSendChanges();
        }

        return false;
    }

    @Override
    public void setSize() {
        switch (this.getType()) {
            case BLANK:
                this.setSize(0.5F, 0.5F);
                break;
            case ELEMENT:
                this.setSize(0.75F, 0.5F);
                break;
            case SHRAPNEL:
                this.setSize(0.5F, 0.5F);
                break;
            case GRAPHITE:
                this.setSize(0.25F, 0.25F);
                break;
            case CONCRETE:
                this.setSize(0.75F, 0.5F);
                break;
            case EXCHANGER:
                this.setSize(1F, 0.5F);
                break;
        }
    }

    @Override
    public int getLifetime() {

        switch (this.getType()) {
            case BLANK:
                return 3 * 60 * 20;
            case ELEMENT:
                return 10 * 60 * 20;
            case SHRAPNEL:
                return 15 * 60 * 20;
            case GRAPHITE:
                return 15 * 60 * 20;
            case CONCRETE:
                return 60 * 20;
            case EXCHANGER:
                return 60 * 20;
            default:
                return 0;
        }
    }

    public DebrisType getType() {
        return DebrisType.VALUES[Math.abs(this.getDataManager().get(TYPE_ID)) % DebrisType.VALUES.length];
    }

    public void setType(DebrisType type) {
        this.getDataManager().set(TYPE_ID, type.ordinal());
    }

    public static enum DebrisType {
        BLANK,            //just a metal beam
        ELEMENT,        //fuel element
        SHRAPNEL,        //steel shrapnel from the pipes and walkways
        GRAPHITE,        //spicy rock
        CONCRETE,        //the all destroying harbinger of annihilation
        EXCHANGER;        //the all destroying harbinger of annihilation: sideways edition

        public static final DebrisType[] VALUES = values();
    }
}
