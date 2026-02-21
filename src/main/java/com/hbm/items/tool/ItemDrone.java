package com.hbm.items.tool;

import com.hbm.entity.item.EntityDeliveryDrone;
import com.hbm.entity.item.EntityDroneBase;
import com.hbm.items.ItemEnumMulti;
import com.hbm.main.MainRegistry;
import com.hbm.util.I18nUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;

import java.util.List;

public class ItemDrone extends ItemEnumMulti<ItemDrone.EnumDroneType> {
    public ItemDrone(String s) {
        super(s, EnumDroneType.VALUES, true, true);
        this.setCreativeTab(MainRegistry.machineTab);
    }

    public enum EnumDroneType {
        PATROL,
        PATROL_CHUNKLOADING,
        PATROL_EXPRESS,
        PATROL_EXPRESS_CHUNKLOADING,
        REQUEST;

        public static final EnumDroneType[] VALUES = values();
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (facing != EnumFacing.UP) return EnumActionResult.FAIL;
        if (worldIn.isRemote) return EnumActionResult.SUCCESS;

        ItemStack stack = player.getHeldItem(hand);

        EntityDeliveryDrone drone = null;

        if (stack.getItemDamage() < 4) {
            drone = new EntityDeliveryDrone(worldIn);
            if (stack.getItemDamage() % 2 == 1) {
                drone.setChunkLoading();
            }
            if (stack.getItemDamage() > 1) {
                drone.getDataManager().set(EntityDroneBase.IS_EXPRESS, true);
            }
        }

        if (drone != null) {
            drone.setPosition(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
            worldIn.spawnEntity(drone);
        }

        stack.shrink(1);

        return EnumActionResult.PASS;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
            for(String s : I18nUtil.resolveKeyArray(stack.getTranslationKey() + ".desc"))
                tooltip.add(TextFormatting.YELLOW + s);
        } else {
            tooltip.add(TextFormatting.DARK_GRAY + "" + TextFormatting.ITALIC + "Hold <" + TextFormatting.YELLOW
                    + TextFormatting.ITALIC + "LSHIFT" + TextFormatting.DARK_GRAY + TextFormatting.ITALIC + "> to display more info");
        }
    }
}
