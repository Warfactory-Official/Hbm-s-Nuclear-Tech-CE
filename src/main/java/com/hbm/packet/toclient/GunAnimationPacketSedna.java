package com.hbm.packet.toclient;

import com.hbm.items.armor.ArmorTrenchmaster;
import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.items.weapon.sedna.Receiver;
import com.hbm.render.anim.sedna.BusAnimationSedna;
import com.hbm.render.anim.sedna.HbmAnimationsSedna;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public class GunAnimationPacketSedna implements IMessage {

    public short type;
    public int receiverIndex;
    public int gunIndex;

    public GunAnimationPacketSedna() { }

    public GunAnimationPacketSedna(int type) {
        this.type = (short) type;
        this.receiverIndex = 0;
        this.gunIndex = 0;
    }

    public GunAnimationPacketSedna(int type, int rec) {
        this.type = (short) type;
        this.receiverIndex = rec;
        this.gunIndex = 0;
    }

    public GunAnimationPacketSedna(int type, int rec, int gun) {
        this.type = (short) type;
        this.receiverIndex = rec;
        this.gunIndex = gun;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        type = buf.readShort();
        receiverIndex = buf.readInt();
        gunIndex = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeShort(type);
        buf.writeInt(receiverIndex);
        buf.writeInt(gunIndex);
    }

    public static class Handler implements IMessageHandler<GunAnimationPacketSedna, IMessage> {

        @Override
        @SideOnly(Side.CLIENT)
        public IMessage onMessage(GunAnimationPacketSedna m, MessageContext ctx) {

            try {

                EntityPlayer player = Minecraft.getMinecraft().player;
                ItemStack stack = player.getHeldItemMainhand();
                int slot = player.inventory.currentItem;

                if(stack.isEmpty()) return null;

                if(stack.getItem() instanceof ItemGunBaseNT) {
                    handleSedna(player, stack, slot, HbmAnimationsSedna.GunAnimation.values()[m.type], m.receiverIndex, m.gunIndex);
                }

            } catch(Exception x) { }

            return null;
        }

        public static void handleSedna(EntityPlayer player, ItemStack stack, int slot, HbmAnimationsSedna.GunAnimation type, int receiverIndex, int gunIndex) {
            ItemGunBaseNT gun = (ItemGunBaseNT) stack.getItem();
            GunConfig config = gun.getConfig(stack, gunIndex);

            if(type == HbmAnimationsSedna.GunAnimation.CYCLE) {
                if(gunIndex < gun.lastShot.length) gun.lastShot[gunIndex] = System.currentTimeMillis();
                gun.shotRand = player.world.rand.nextDouble();

                Receiver[] receivers = config.getReceivers(stack);
                if(receiverIndex >= 0 && receiverIndex < receivers.length) {
                    Receiver rec = receivers[receiverIndex];
                    BiConsumer<ItemStack, ItemGunBaseNT.LambdaContext> onRecoil= rec.getRecoil(stack);
                    if(onRecoil != null) onRecoil.accept(stack, new ItemGunBaseNT.LambdaContext(config, player, player.inventory, receiverIndex));
                }
            }

            BiFunction<ItemStack, HbmAnimationsSedna.GunAnimation, BusAnimationSedna> anims = config.getAnims(stack);
            BusAnimationSedna animation = anims.apply(stack, type);

            if(animation == null && type == HbmAnimationsSedna.GunAnimation.RELOAD_EMPTY) {
                animation = anims.apply(stack, HbmAnimationsSedna.GunAnimation.RELOAD);
            }
            if(animation == null && (type == HbmAnimationsSedna.GunAnimation.ALT_CYCLE || type == HbmAnimationsSedna.GunAnimation.CYCLE_EMPTY)) {
                animation = anims.apply(stack, HbmAnimationsSedna.GunAnimation.CYCLE);
            }

            if(animation != null) {
                Minecraft.getMinecraft().entityRenderer.itemRenderer.resetEquippedProgress(EnumHand.MAIN_HAND);
                Minecraft.getMinecraft().entityRenderer.itemRenderer.updateEquippedItem();
                boolean isReloadAnimation = type == HbmAnimationsSedna.GunAnimation.RELOAD || type == HbmAnimationsSedna.GunAnimation.RELOAD_CYCLE || type == HbmAnimationsSedna.GunAnimation.RELOAD_EMPTY;
                if(isReloadAnimation && ArmorTrenchmaster.isTrenchMaster(player)) animation.setTimeMult(0.5D);
                HbmAnimationsSedna.hotbar[slot][gunIndex] = new HbmAnimationsSedna.Animation(stack.getItem().getTranslationKey(), System.currentTimeMillis(), animation, type, isReloadAnimation && config.getReloadAnimSequential(stack));
            }
        }
    }
}
