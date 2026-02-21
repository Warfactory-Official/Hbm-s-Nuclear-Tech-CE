package com.hbm.hazard.type;

import com.hbm.hazard.modifier.IHazardModifier;
import com.hbm.util.I18nUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.ObjDoubleConsumer;

public class HazardTypeDangerousDrop implements IHazardType {
    private final ObjDoubleConsumer<EntityItem> onDroppedItemUpdate;

    public HazardTypeDangerousDrop(@NotNull ObjDoubleConsumer<EntityItem> onDrop) {
        this.onDroppedItemUpdate = onDrop;
    }

    @Override
    public void onUpdate(EntityLivingBase target, double level, ItemStack stack) {
        //Nothing
    }

    @Override
    public void updateEntity(EntityItem item, double level) {
        onDroppedItemUpdate.accept(item, level);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addHazardInformation(EntityPlayer player, List<String> list, double level, ItemStack stack, List<IHazardModifier> modifiers) {
        list.add(TextFormatting.RED + "[" + I18nUtil.resolveKey("trait.drop") + "]");
    }
}
