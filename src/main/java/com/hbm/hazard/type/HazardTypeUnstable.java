package com.hbm.hazard.type;

import com.hbm.config.BombConfig;
import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.entity.logic.EntityNukeExplosionMK5;
import com.hbm.hazard.modifier.IHazardModifier;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.ModDamageSource;
import com.hbm.lib.ObjObjDoubleConsumer;
import com.hbm.util.I18nUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.ObjDoubleConsumer;

public class HazardTypeUnstable implements IHazardType {
    public static final String NBT_TAG_TIMER = "timer";
    private final ObjObjDoubleConsumer<EntityLivingBase, ItemStack> onUpdate;
    private final ObjDoubleConsumer<EntityItem> onDrop;
    private final HazardInfoConsumer customInfo;
    private int timer = -1;

    public HazardTypeUnstable(@NotNull ObjObjDoubleConsumer<EntityLivingBase, ItemStack> onUpdate, @NotNull ObjDoubleConsumer<EntityItem> onDrop) {
        this(onUpdate, onDrop, null);
    }

    public HazardTypeUnstable(int timer) {
        this(timer, null);
    }

    public HazardTypeUnstable(@NotNull ObjObjDoubleConsumer<EntityLivingBase, ItemStack> onUpdate, @NotNull ObjDoubleConsumer<EntityItem> onDrop, HazardInfoConsumer customInfo) {
        this.onUpdate = onUpdate;
        this.onDrop = onDrop;
        this.customInfo = customInfo;
    }

    public HazardTypeUnstable(int timer, HazardInfoConsumer customInfo) {
        if (timer <= 0) throw new IllegalArgumentException("timer must be greater than 0");
        this.timer = timer;
        this.onUpdate = (entity, stack, level) -> {
            World world = entity.world;
            final int count = stack.getCount();
            final int radius = scaledRadius(level, count);
            setTimer(stack, getTimer(stack) + 1);
            if(getTimer(stack) == this.timer && !world.isRemote) {
                stack.setCount(0);
                world.spawnEntity(EntityNukeExplosionMK5.statFac(world, radius, entity.posX, entity.posY, entity.posZ).setDetonator(entity));

                if(BombConfig.enableNukeClouds) {
                    EntityNukeTorex.statFac(world, entity.posX, entity.posY, entity.posZ, radius);
                }
                world.playSound(null, entity.posX, entity.posY, entity.posZ, HBMSoundHandler.oldExplosion, SoundCategory.PLAYERS, 1.0F, 1.0F);
                entity.attackEntityFrom(ModDamageSource.nuclearBlast, 10000);
            }
        };
        this.onDrop = (itemEntity, level) -> {
            World world = itemEntity.world;
            int radius = (int) level;
            setTimer(itemEntity.getItem(), getTimer(itemEntity.getItem()) + 1);

            if(getTimer(itemEntity.getItem()) == this.timer && !world.isRemote) {
                EntityPlayerMP thrower = itemEntity.world.getMinecraftServer().getPlayerList().getPlayerByUsername(itemEntity.getThrower());
                world.spawnEntity(EntityNukeExplosionMK5.statFac(world, radius, itemEntity.posX, itemEntity.posY, itemEntity.posZ).setDetonator(thrower));

                if(BombConfig.enableNukeClouds) {
                    EntityNukeTorex.statFac(world, itemEntity.posX, itemEntity.posY, itemEntity.posZ, radius);
                }
                world.playSound(null, itemEntity.posX, itemEntity.posY, itemEntity.posZ, HBMSoundHandler.oldExplosion, SoundCategory.PLAYERS, 1.0F, 1.0F);
                itemEntity.attackEntityFrom(ModDamageSource.nuclearBlast, 10000);
                itemEntity.setDead();
            }
        };
        this.customInfo = customInfo;
    }

    @Override
    public void onUpdate(EntityLivingBase target, double level, ItemStack stack) {
        onUpdate.accept(target, stack, level);
    }

    @Override
    public void updateEntity(EntityItem item, double level) {
        onDrop.accept(item, level);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addHazardInformation(EntityPlayer player, List<String> tooltip, double level, ItemStack stack, List<IHazardModifier> modifiers) {
        if(customInfo != null){
            customInfo.accept(player, tooltip, level, stack, modifiers);
        } else if (this.timer != -1){
            final int scaled = scaledRadius(level, stack.getCount());
            tooltip.add("§4"+ I18nUtil.resolveKey("trait.unstable") + "§r");
            tooltip.add("§cDecay Time: " + (this.timer / 20)
                    + "s - Explosion Radius: " + scaled
                    + "m§r");
            tooltip.add("§cDecay: " + (getTimer(stack) * 100 / this.timer) + "%§r");
        }
    }

    private static int scaledRadius(double baseLevel, int count) {
        if (count <= 1) return (int) baseLevel;
        int r = (int) (baseLevel * Math.cbrt(count) + 0.5);
        return Math.max(1, r);
    }

    public static int getTimer(ItemStack stack) {
        if(!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());
        return stack.getTagCompound().getInteger(NBT_TAG_TIMER);
    }

    public static void setTimer(ItemStack stack, int timer) {
        if(!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());
        stack.getTagCompound().setInteger(NBT_TAG_TIMER, timer);
    }
}
