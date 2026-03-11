package com.hbm.items.weapon;

import com.hbm.items.ItemEnumMulti;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.util.I18nUtil;
import com.hbm.util.InventoryUtil;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItemAmmoContainer extends ItemEnumMulti<ItemAmmoContainer.EnumAmmoContainerType> {

    public enum EnumAmmoContainerType {
        DEFAULT, CONSTRAINED;

        public static final ItemAmmoContainer.EnumAmmoContainerType[] VALUES = values();
    }

    public ItemAmmoContainer(String regName) {
        super(regName, EnumAmmoContainerType.VALUES, false, true);
        setMaxStackSize(1);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(@NotNull CreativeTabs tab, NonNullList<ItemStack> items) {
        if (isInCreativeTab(tab)) {
            for(int i = 0; i < EnumAmmoContainerType.values().length; i++) {
                items.add(new ItemStack(this, 1, i));
            }
        }
    }

    @Override
    public @NotNull ActionResult<ItemStack> onItemRightClick(@NotNull World world, EntityPlayer player, @NotNull EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        boolean makeshift = stack.getItemDamage() == 1;

        List<ItemStack> stacks = new ArrayList<>();

        for (ItemStack inv : player.inventory.mainInventory) {
            if (!inv.isEmpty() && inv.getItem() instanceof ItemGunBaseNT gun) {
                if (!gun.defaultAmmo.isEmpty() && !(makeshift && gun.isDefaultExpensive)) stacks.add(inv);
            }
        }

        if (stacks.isEmpty()) return ActionResult.newResult(EnumActionResult.SUCCESS, stack);

        Collections.shuffle(stacks);

        int maxGunCount = 3;

        for (int i = 0; i < maxGunCount && i < stacks.size(); i++) {
            ItemStack gunStack = stacks.get(i);
            ItemGunBaseNT gun = (ItemGunBaseNT) gunStack.getItem();
            ItemStack ammo = gun.defaultAmmo.copy();
            if (makeshift) {
                ammo.setCount((int) Math.ceil(ammo.getCount() / 2D));
            }
            ItemStack remainder = InventoryUtil.tryAddItemToInventory(player.inventory.mainInventory, ammo);
            if (!remainder.isEmpty()) player.dropItem(remainder, false);
        }

        world.playSound(player, player.posX, player.posY, player.posZ, HBMSoundHandler.itemUnpack, SoundCategory.PLAYERS, 1.0F, 1.0F);
        player.inventoryContainer.detectAndSendChanges();
        stack.shrink(1);

        return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, @NotNull List<String> list, @NotNull ITooltipFlag flagIn) {
        String[] lines = I18nUtil.resolveKeyArray(getTranslationKey() + (stack.getItemDamage() == 1 ? ".1" : "") + ".desc");
        for (String line : lines) list.add(ChatFormatting.YELLOW + line);
    }
}
