package com.hbm.entity.mob;

import com.hbm.interfaces.AutoRegister;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.IAnimals;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
@AutoRegister(name = "entity_ntm_test_dummy", trackingRange = 80, updateFrequency = 3, eggColors = {0xffffff, 0x000000})
public class EntityDummy extends EntityLiving implements IAnimals {

    public EntityDummy(World worldIn) {
        super(worldIn);
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        if (hand == EnumHand.MAIN_HAND) {
            ItemStack stack = player.getHeldItem(hand);
            if (!stack.isEmpty() && stack.getItem() instanceof ItemArmor) {
                if (!this.world.isRemote) {
                    ItemArmor armor = (ItemArmor) stack.getItem();
                    ItemStack current = this.getItemStackFromSlot(armor.armorType);
                    if (!current.isEmpty()) {
                        ItemStack currentCopy = current.copy();
                        if (!player.inventory.addItemStackToInventory(currentCopy)) {
                            this.entityDropItem(currentCopy, 0.0F);
                        }
                    }
                    ItemStack equipStack = stack.copy();
                    equipStack.setCount(1);
                    this.setItemStackToSlot(armor.armorType, equipStack);
                    this.world.playSound(null, this.posX, this.posY, this.posZ, SoundEvents.ITEM_ARMOR_EQUIP_GENERIC, this.getSoundCategory(), 1.0F, 1.0F);
                    if (!player.capabilities.isCreativeMode) {
                        stack.shrink(1);
                    }
                }
                return true;
            }
        }
        return super.processInteract(player, hand);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean getAlwaysRenderNameTag() {
        return true;
    }

    @Override
    public String getName() {
        return ((int) (this.getHealth() * 10)) / 10F + " / " + ((int) (this.getMaxHealth() * 10)) / 10F;
    }

    @Override
    protected void dropEquipment(boolean wasRecentlyHit, int lootingModifier) {
    }
}
