package com.hbm.packet.toclient;

import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.render.item.weapon.sedna.ItemRenderWeaponBase;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class MuzzleFlashPacket implements IMessage {

    private int entityID;

    public MuzzleFlashPacket() { }


    public MuzzleFlashPacket(EntityLivingBase entity) {
        this.entityID = entity.getEntityId();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(entityID);
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entityID = buf.readInt();
    }

    public static class Handler implements IMessageHandler<MuzzleFlashPacket, IMessage> {

        @SideOnly(Side.CLIENT)
        @Override
        public IMessage onMessage(MuzzleFlashPacket m, MessageContext ctx) {
            EntityLivingBase entity = (EntityLivingBase) Minecraft.getMinecraft().world.getEntityByID(m.entityID);
            if(entity == null || entity == Minecraft.getMinecraft().player) return null; //packets are sent to the player who fired
            ItemStack stack = entity.getHeldItemMainhand();
            if(stack.isEmpty()) return null;

            if(stack.getItem() instanceof ItemGunBaseNT) {
                ItemRenderWeaponBase.flashMap.put(entity, System.currentTimeMillis());
            }

            return null;
        }

    }
}
