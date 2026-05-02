package com.hbm.items.tool;

import com.hbm.inventory.fluid.FluidType;
import com.hbm.items.ClaimedModelLocationRegistry;
import com.hbm.items.IAnimatedItem;
import com.hbm.main.MainRegistry;
import com.hbm.render.anim.BusAnimation;
import com.hbm.render.anim.BusAnimationKeyframe;
import com.hbm.render.anim.BusAnimationSequence;
import com.hbm.render.anim.HbmAnimations;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

public class ItemChainsaw extends ItemToolAbilityFueled implements IAnimatedItem {

    public ItemChainsaw(String s, float damage, float attackSpeed, double movement, ToolMaterial material, EnumToolType type, int maxFuel, int consumption, int fillRate, FluidType... acceptedFuels) {
        super(s, damage, attackSpeed, movement, material, type, maxFuel, consumption, fillRate, acceptedFuels);

        INSTANCES.remove(this);
        ClaimedModelLocationRegistry.unregisterTeisrBinding(this);
    }

    @Override
    public boolean onEntitySwing(@NotNull EntityLivingBase entityLiving, @NotNull ItemStack stack) {
        if (!(entityLiving instanceof EntityPlayerMP))
            return false;

        if (stack.getItemDamage() >= stack.getMaxDamage())
            return false;

        NBTTagCompound data = new NBTTagCompound();
        data.setString("type", "anim");
        data.setString("mode", "generic");
        MainRegistry.proxy.effectNT(data);
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BusAnimation getAnimation(NBTTagCompound data, ItemStack stack) {
        int forward = 150;
        int sideways = 100;
        int retire = 200;

        if (HbmAnimations.getRelevantAnim(EnumHand.MAIN_HAND) == null) {
            return new BusAnimation()
                    .addBus("SWING_ROT", new BusAnimationSequence()
                            .addKeyframe(new BusAnimationKeyframe(0, 90, 0, forward))
                            .addKeyframe(new BusAnimationKeyframe(45, 0, 90, sideways))
                            .addKeyframe(new BusAnimationKeyframe(0, 0, 0, retire)))
                    .addBus("SWING_TRANS", new BusAnimationSequence()
                            .addKeyframe(new BusAnimationKeyframe(0, 0, 3, forward))
                            .addKeyframe(new BusAnimationKeyframe(2, 0, 2, sideways))
                            .addKeyframe(new BusAnimationKeyframe(0, 0, 0, retire)));
        } else {
            double[] rot = HbmAnimations.getRelevantTransformation("SWING_ROT", EnumHand.MAIN_HAND);
            double[] trans = HbmAnimations.getRelevantTransformation("SWING_TRANS", EnumHand.MAIN_HAND);

            if (System.currentTimeMillis() - HbmAnimations.getRelevantAnim(EnumHand.MAIN_HAND).startMillis < 50)
                return null;

            return new BusAnimation()
                    .addBus("SWING_ROT", new BusAnimationSequence()
                            .addKeyframe(new BusAnimationKeyframe(rot[0], rot[1], rot[2], 0))
                            .addKeyframe(new BusAnimationKeyframe(0, 90, 0, forward))
                            .addKeyframe(new BusAnimationKeyframe(45, 0, 90, sideways))
                            .addKeyframe(new BusAnimationKeyframe(0, 0, 0, retire)))
                    .addBus("SWING_TRANS", new BusAnimationSequence()
                            .addKeyframe(new BusAnimationKeyframe(trans[0], trans[1], trans[2], 0))
                            .addKeyframe(new BusAnimationKeyframe(0, 0, 3, forward))
                            .addKeyframe(new BusAnimationKeyframe(2, 0, 2, sideways))
                            .addKeyframe(new BusAnimationKeyframe(0, 0, 0, retire)));
        }
    }
}
