package com.hbm.items.special;

import com.hbm.config.BombConfig;
import com.hbm.config.WeaponConfig;
import com.hbm.entity.effect.EntityCloudFleija;
import com.hbm.entity.logic.EntityNukeExplosionMK3;
import com.hbm.forgefluid.SpecialContainerFillLists.EnumCell;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ModItems;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Cell meta = NTM fluid id
 * 0 = empty
 */
public class ItemCell extends Item {

    public ItemCell(String s) {
        this.setTranslationKey(s);
        this.setRegistryName(s);
        this.setHasSubtypes(true);
        this.setMaxDamage(0);

        ModItems.ALL_ITEMS.add(this);
    }

    public static boolean isFullCell(ItemStack stack, FluidType type) {
        return getFluidType(stack) == type;
    }

    public static boolean isEmptyCell(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() == ModItems.cell && stack.getMetadata() == 0;
    }

    public static boolean hasFluid(ItemStack stack, FluidType type) {
        return getFluidType(stack) == type;
    }

    @Nullable
    public static FluidType getFluidType(ItemStack stack) {
        if (stack.isEmpty() || stack.getItem() != ModItems.cell || stack.getMetadata() == 0) {
            return null;
        }
        return Fluids.fromID(stack.getMetadata());
    }

    /**
     * Balefire cell is ModItems.cell_balefire, don't use this!
     */
    public static ItemStack getFullCell(FluidType fluid, int amount) {
        if (EnumCell.contains(fluid)) {
            return new ItemStack(ModItems.cell, amount, fluid.getID());
        }
        return ItemStack.EMPTY;
    }

    /**
     * Balefire cell is ModItems.cell_balefire, don't use this!
     */
    public static ItemStack getFullCell(FluidType fluid) {
        return getFullCell(fluid, 1);
    }

    public static boolean hasEmptyCell(EntityPlayer player) {
        InventoryPlayer inv = player.inventory;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            if (isEmptyCell(inv.getStackInSlot(i))) {
                return true;
            }
        }
        return false;
    }

    public static void consumeEmptyCell(EntityPlayer player) {
        InventoryPlayer inv = player.inventory;
        for (int i = 0; i < inv.getSizeInventory(); i++) {
            if (isEmptyCell(inv.getStackInSlot(i))) {
                inv.getStackInSlot(i).shrink(1);
                return;
            }
        }
    }

    @Override
    public boolean hasContainerItem(@NotNull ItemStack stack) {
        return !isEmptyCell(stack);
    }

    @Override
    public @NotNull ItemStack getContainerItem(@NotNull ItemStack itemStack) {
        if (hasContainerItem(itemStack)) {
            return new ItemStack(this, 1, 0);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean onEntityItemUpdate(EntityItem entityItem) {
        if (entityItem.onGround || entityItem.isBurning()) {
            if (!WeaponConfig.dropCell) {
                return false;
            }

            ItemStack stack = entityItem.getItem();
            FluidType type = getFluidType(stack);

            if (type == null) {
                return false;
            }
            if (type == Fluids.ASCHRAB) {
                if (!entityItem.world.isRemote) {
                    entityItem.setDead();
                    entityItem.world.playSound(null, entityItem.posX, entityItem.posY, entityItem.posZ, SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.AMBIENT, 100.0f, entityItem.world.rand.nextFloat() * 0.1F + 0.9F);
                    EntityNukeExplosionMK3 entity = new EntityNukeExplosionMK3(entityItem.world);
                    entity.posX = entityItem.posX;
                    entity.posY = entityItem.posY;
                    entity.posZ = entityItem.posZ;
                    if (!EntityNukeExplosionMK3.isJammed(entityItem.world, entity)) {
                        entity.destructionRange = BombConfig.aSchrabRadius;
                        entity.speed = 25;
                        entity.coefficient = 1.0F;
                        entity.waste = false;
                        entityItem.world.spawnEntity(entity);

                        EntityCloudFleija cloud = new EntityCloudFleija(entityItem.world, BombConfig.aSchrabRadius);
                        cloud.posX = entityItem.posX;
                        cloud.posY = entityItem.posY;
                        cloud.posZ = entityItem.posZ;
                        entityItem.world.spawnEntity(cloud);
                    }
                }
                return true;
            }
            if (type == Fluids.AMAT) {
                if (!entityItem.world.isRemote) {
                    entityItem.setDead();
                    entityItem.world.createExplosion(entityItem, entityItem.posX, entityItem.posY, entityItem.posZ, 10.0F, true);
                }
                return true;
            }
        }
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public @NotNull String getItemStackDisplayName(@NotNull ItemStack stack) {
        if (isEmptyCell(stack)) {
            return I18n.format("item.cell_empty.name");
        }

        FluidType f = getFluidType(stack);
        if (f != null) {
            try {
                return I18n.format(EnumCell.getEnumFromFluid(f).getTranslateKey());
            } catch (NullPointerException ignored) {
            }
        }
        return I18n.format(this.getTranslationKey() + ".name");
    }

    @Override
    public void getSubItems(@NotNull CreativeTabs tab, @NotNull NonNullList<ItemStack> items) {
        if (this.isInCreativeTab(tab)) {
            items.add(new ItemStack(this, 1, 0)); // Empty cell
            for (FluidType f : EnumCell.getFluids()) {
                if (f != null) {
                    items.add(getFullCell(f));
                }
            }
        }
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flagIn) {
        FluidType type = getFluidType(stack);
        if (type == Fluids.AMAT) {
            tooltip.add("§eExposure to matter will lead to violent annihilation!§r");
            tooltip.add("§c[Dangerous Drop]§r");
        } else if (type == Fluids.ASCHRAB) {
            tooltip.add("§eExposure to matter will create a fólkvangr field!§r");
            tooltip.add("§c[Dangerous Drop]§r");
        }
    }
}