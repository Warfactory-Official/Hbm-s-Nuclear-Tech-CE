package com.hbm.items.weapon.grenade;

import com.hbm.capability.HbmLivingCapability.IEntityHbmProps;
import com.hbm.capability.HbmLivingProps;
import com.hbm.entity.grenade.EntityGrenadeUniversal;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.items.IAnimatedItem;
import com.hbm.items.IEquipReceiver;
import com.hbm.items.ItemBase;
import com.hbm.items.ModItems;
import com.hbm.items.weapon.grenade.ItemGrenadeExtra.EnumGrenadeExtra;
import com.hbm.items.weapon.grenade.ItemGrenadeFilling.EnumGrenadeFilling;
import com.hbm.items.weapon.grenade.ItemGrenadeFuze.EnumGrenadeFuze;
import com.hbm.items.weapon.grenade.ItemGrenadeShell.EnumGrenadeShell;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.particle.helper.HbmEffectNT;
import com.hbm.render.anim.BusAnimation;
import com.hbm.render.anim.BusAnimationKeyframe.IType;
import com.hbm.render.anim.BusAnimationSequence;
import com.hbm.util.EnumUtil;
import com.hbm.util.I18nUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;

public class ItemGrenadeUniversal extends ItemBase implements IAnimatedItem, IEquipReceiver {

    /*
     *  __________
     * | ________ | ______ SHELL - determines what filling can be used, various bonuses like throw distance and fragmentation, max stack size
     * ||        ||
     * ||       __________ FILLING - the bang - high explosive, fragmentation, incendiary, etc
     * ||________||
     *  \   /\   /
     *   \_ || _/
     *    | || |
     *    | |_____________ FUZE - what triggers the explosive, timed, impact, or airburst
     *    | || |
     *    | || |
     *    | || |
     *    | || | _________ EXTRA - optional bonus like additional fuzes, special explosion effects, glue for sticky bombs, et cetera
     *    | || |
     *    | || |
     *    / || \
     *   |__||__|
     *     {__}
     */

    public static final String KEY_SHELL   = "shell";
    public static final String KEY_FILLING = "filling";
    public static final String KEY_FUZE    = "fuze";
    public static final String KEY_EXTRA   = "extra";

    public ItemGrenadeUniversal(String registryName) {
        super(registryName);
        this.setHasSubtypes(false);
    }

    @Override
    public int getItemStackLimit(ItemStack stack) {
        return getShell(stack).getStackLimit();
    }

    @Override
    public void onEquip(EntityPlayer player, EnumHand hand) {
        resetDeployment(player);
        sendEquipAnimation(player, hand);
    }

    @Override
    public void onEquip(EntityPlayer player, ItemStack stack) {
        resetDeployment(player);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        EnumGrenadeShell shell = getShell(stack);
        IEntityHbmProps props = HbmLivingProps.getData(player);
        if (props.getGrenadeDeployment() >= shell.getDrawDuration()) {
            if (!world.isRemote) {
                EntityGrenadeUniversal grenade = new EntityGrenadeUniversal(world, player, stack, hand);
                world.spawnEntity(grenade);
            }
            if (!player.capabilities.isCreativeMode) stack.shrink(1);
            if (!stack.isEmpty()) this.onEquip(player, hand);
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        return new ActionResult<>(EnumActionResult.PASS, stack);
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isHeld) {
        if (!(entity instanceof EntityLivingBase)) return;
        EntityPlayer player = entity instanceof EntityPlayer ? (EntityPlayer) entity : null;

        if (player != null) {
            EnumHand heldHand = getHeldHand(player, stack);
            boolean actuallyHeld = heldHand != null;
            boolean wasHeld = ItemGunBaseNT.getIsEquipped(stack);

            if (!wasHeld && actuallyHeld) {
                this.onEquip(player, heldHand);
            } else if (isDeploymentOwner(player, stack, heldHand)) {
                IEntityHbmProps props = HbmLivingProps.getData(player);
                int deployment = props.getGrenadeDeployment() + 1;
                props.setGrenadeDeployment(deployment);

                EnumGrenadeShell shell = getShell(stack);
                if (shell == EnumGrenadeShell.FRAG && deployment == 18) {
                    playCue(world, player, HBMSoundHandler.revolverCock, 1F);
                }
                if (shell == EnumGrenadeShell.STICK) {
                    if (deployment == 16) playCue(world, player, HBMSoundHandler.boltOpen, 1.25F);
                    if (deployment == 25) playCue(world, player, HBMSoundHandler.boltOpen, 1.25F);
                }
                if (shell == EnumGrenadeShell.TECH && deployment == 18) {
                    playCue(world, player, HBMSoundHandler.grenadeTech, 1F);
                }
                if (shell == EnumGrenadeShell.NUKE && deployment == 26) {
                    playCue(world, player, HBMSoundHandler.grenadeNuka, 1F);
                }
            }

            isHeld = actuallyHeld;
        }

        ItemGunBaseNT.setIsEquipped(stack, isHeld);
    }

    private static void resetDeployment(EntityPlayer player) {
        HbmLivingProps.getData(player).setGrenadeDeployment(0);
    }

    private void sendEquipAnimation(EntityPlayer player, EnumHand hand) {
        if (!(player instanceof EntityPlayerMP)) return;
        NBTTagCompound data = new NBTTagCompound();
        data.setString("mode", "generic");
        data.setInteger("hand", hand.ordinal());
        data.setString("name", this.getRegistryName().getPath());
        PacketThreading.createSendToThreadedPacket(new AuxParticlePacketNT(HbmEffectNT.Anim, data, 0, 0, 0), (EntityPlayerMP) player);
    }

    private static EnumHand getHeldHand(EntityPlayer player, ItemStack stack) {
        if (player.getHeldItemMainhand() == stack) return EnumHand.MAIN_HAND;
        if (player.getHeldItemOffhand() == stack) return EnumHand.OFF_HAND;
        return null;
    }

    private boolean isDeploymentOwner(EntityPlayer player, ItemStack stack, EnumHand heldHand) {
        if (heldHand == null) return false;
        if (heldHand == EnumHand.MAIN_HAND) return true;
        return player.getHeldItemMainhand().getItem() != this;
    }

    private static void playCue(World world, EntityPlayer player, SoundEvent event, float pitch) {
        world.playSound(null, player.posX, player.posY, player.posZ, event, SoundCategory.PLAYERS, 1F, pitch);
    }

    public static EnumGrenadeShell getShell(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTagCompound()) return EnumGrenadeShell.FRAG;
        return EnumUtil.grabEnumSafely(EnumGrenadeShell.VALUES, stack.getTagCompound().getInteger(KEY_SHELL));
    }

    public static EnumGrenadeFilling getFilling(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTagCompound()) return EnumGrenadeFilling.HE;
        return EnumUtil.grabEnumSafely(EnumGrenadeFilling.VALUES, stack.getTagCompound().getInteger(KEY_FILLING));
    }

    public static EnumGrenadeFuze getFuze(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTagCompound()) return EnumGrenadeFuze.S3;
        return EnumUtil.grabEnumSafely(EnumGrenadeFuze.VALUES, stack.getTagCompound().getInteger(KEY_FUZE));
    }

    public static EnumGrenadeExtra getExtra(ItemStack stack) {
        if (stack.isEmpty() || !stack.hasTagCompound() || !stack.getTagCompound().hasKey(KEY_EXTRA)) return null;
        return EnumUtil.grabEnumSafely(EnumGrenadeExtra.VALUES, stack.getTagCompound().getInteger(KEY_EXTRA));
    }

    public static ItemStack make(EnumGrenadeShell shell, EnumGrenadeFilling filling, EnumGrenadeFuze fuze) {
        return make(shell, filling, fuze, null, 1);
    }

    public static ItemStack make(EnumGrenadeShell shell, EnumGrenadeFilling filling, EnumGrenadeFuze fuze, EnumGrenadeExtra extra) {
        return make(shell, filling, fuze, extra, 1);
    }

    public static ItemStack make(EnumGrenadeShell shell, EnumGrenadeFilling filling, EnumGrenadeFuze fuze, EnumGrenadeExtra extra, int amount) {
        ItemStack stack = new ItemStack(ModItems.grenade_universal, amount);
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setInteger(KEY_SHELL, shell.ordinal());
        nbt.setInteger(KEY_FILLING, filling.ordinal());
        nbt.setInteger(KEY_FUZE, fuze.ordinal());
        if (extra != null) nbt.setInteger(KEY_EXTRA, extra.ordinal());
        stack.setTagCompound(nbt);
        return stack;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
        if (!this.isInCreativeTab(tab)) return;
        for (EnumGrenadeShell shell : EnumGrenadeShell.VALUES) {
            for (EnumGrenadeFilling filling : EnumGrenadeFilling.VALUES) {
                if (!filling.compatibleShells.contains(shell)) continue;
                for (EnumGrenadeFuze fuze : EnumGrenadeFuze.VALUES) {
                    items.add(make(shell, filling, fuze));
                    for (EnumGrenadeExtra extra : EnumGrenadeExtra.VALUES) items.add(make(shell, filling, fuze, extra));
                }
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(TextFormatting.YELLOW + I18nUtil.resolveKey(
                ModItems.grenade_shell.getTranslationKey() + "." + getShell(stack).name().toLowerCase(Locale.US) + ".name"));
        tooltip.add(TextFormatting.YELLOW + I18nUtil.resolveKey(
                ModItems.grenade_filling.getTranslationKey() + "." + getFilling(stack).name().toLowerCase(Locale.US) + ".name"));
        tooltip.add(TextFormatting.YELLOW + I18nUtil.resolveKey(
                ModItems.grenade_fuze.getTranslationKey() + "." + getFuze(stack).name().toLowerCase(Locale.US) + ".name"));
        EnumGrenadeExtra extra = getExtra(stack);
        if (extra != null) {
            tooltip.add(TextFormatting.RED + I18nUtil.resolveKey(
                    ModItems.grenade_extra.getTranslationKey() + "." + extra.name().toLowerCase(Locale.US) + ".name"));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BusAnimation getAnimation(NBTTagCompound data, ItemStack stack) {
        EnumGrenadeShell shell = getShell(stack);

        if (shell == EnumGrenadeShell.FRAG) {
            return new BusAnimation()
                    .addBus("BODYMOVE", new BusAnimationSequence().setPos(0, -5, 0).addPos(0, -3, 0, 350).addPos(0, 0, 0, 350, IType.SIN_DOWN))
                    .addBus("BODYTURN", new BusAnimationSequence().addPos(0, 0, 45, 350).addPos(0, 0, -15, 350, IType.SIN_DOWN).hold(200).addPos(0, 0, -20, 100, IType.SIN_DOWN).addPos(0, 0, 0, 500, IType.SIN_FULL))
                    .addBus("RINGMOVE", new BusAnimationSequence().hold(900).addPos(0, 0, 1, 150).addPos(0, -3, 3, 300))
                    .addBus("RINGTURN", new BusAnimationSequence().hold(900).addPos(0, 0, 45, 300))
                    .addBus("RENDERRING", new BusAnimationSequence().setPos(1, 1, 1).hold(1350).setPos(0, 0, 0));
        }

        if (shell == EnumGrenadeShell.STICK) {
            return new BusAnimation()
                    .addBus("BODYMOVE", new BusAnimationSequence().setPos(0, -7, 0).addPos(0, 3, 0, 750, IType.SIN_DOWN).holdUntil(1900).addPos(0, 0, 0, 250, IType.SIN_FULL))
                    .addBus("BODYTURN", new BusAnimationSequence().setPos(0, 0, 90).addPos(0, 0, -45, 750, IType.SIN_DOWN).holdUntil(1900).addPos(0, 0, 0, 250, IType.SIN_FULL))
                    .addBus("RINGMOVE", new BusAnimationSequence().hold(800).addPos(0, -0.25, 0, 200, IType.SIN_FULL).hold(250).addPos(0, -0.5, 0, 200, IType.SIN_FULL).addPos(2, -5, 0, 350, IType.SIN_UP))
                    .addBus("RINGTURN", new BusAnimationSequence().hold(800).addPos(0, 360, 0, 200, IType.SIN_FULL).hold(250).addPos(0, 360 * 2, 0, 200, IType.SIN_FULL))
                    .addBus("RENDERRING", new BusAnimationSequence().setPos(1, 1, 1).hold(2100).setPos(0, 0, 0));
        }

        if (shell == EnumGrenadeShell.TECH) {
            return new BusAnimation()
                    .addBus("BODYMOVE", new BusAnimationSequence().setPos(0, -5, 0).addPos(0, -3, 0, 350).addPos(0, 0, 0, 350, IType.SIN_DOWN))
                    .addBus("BODYTURN", new BusAnimationSequence().addPos(0, 0, 45, 350).addPos(0, 0, -15, 350, IType.SIN_DOWN).hold(200).addPos(0, 0, -20, 100, IType.SIN_DOWN).addPos(0, 0, 0, 500, IType.SIN_FULL))
                    .addBus("RINGMOVE", new BusAnimationSequence().hold(900).addPos(0, 0, 1, 150).addPos(0, -3, 3, 300))
                    .addBus("RINGTURN", new BusAnimationSequence().hold(900).addPos(0, 0, 45, 300))
                    .addBus("RENDERRING", new BusAnimationSequence().setPos(1, 1, 1).hold(1350).setPos(0, 0, 0));
        }

        if (shell == EnumGrenadeShell.NUKE) {
            return new BusAnimation()
                    .addBus("BODYMOVE", new BusAnimationSequence().setPos(0, -5, 0).hold(250).addPos(0, 0, 0, 850, IType.SIN_DOWN))
                    .addBus("BODYTURN", new BusAnimationSequence().setPos(0, 0, 90).hold(250).addPos(0, 0, -25, 850, IType.SIN_DOWN).hold(200).addPos(0, 0, -30, 100, IType.SIN_DOWN).addPos(0, 0, 0, 750, IType.SIN_FULL))
                    .addBus("RINGMOVE", new BusAnimationSequence().hold(1300).addPos(0, 0, 1, 150).addPos(0, -3, 3, 300))
                    .addBus("RINGTURN", new BusAnimationSequence().hold(1300).addPos(0, 0, 720, 500)) // SPEEN
                    .addBus("RENDERRING", new BusAnimationSequence().setPos(1, 1, 1).hold(1750).setPos(0, 0, 0));
        }

        return null;
    }
}
