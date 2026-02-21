package com.hbm.items.tool;

import com.google.common.collect.Multimap;
import com.hbm.api.block.IToolable;
import com.hbm.items.ModItems;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ItemToolingWeapon extends ItemTooling {

    protected float damage = 0;

    public ItemToolingWeapon(String s, IToolable.ToolType type, int durability, float damage) {
        super(type, durability, s);
        this.damage = damage;
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase entity, EntityLivingBase player) {

        World world = player.world;

        if(this == ModItems.wrench) {

            Vec3d vec = player.getLookVec();

            double dX = vec.x * 0.5;
            double dY = vec.y * 0.5;
            double dZ = vec.z * 0.5;

            entity.motionX += dX;
            entity.motionY += dY;
            entity.motionZ += dZ;
            world.playSound(null, entity.posX, entity.posY, entity.posZ, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 3.0F, 0.75F);
        }

        return false;
    }

    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {

        Multimap<String, AttributeModifier> multimap = super.getAttributeModifiers(slot, stack);
        multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Weapon modifier", damage, 0));

        if(this == ModItems.wrench) {
            multimap.put(SharedMonsterAttributes.MOVEMENT_SPEED.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Wrench modifier", -0.1, 1));
        }

        return multimap;
    }
}
