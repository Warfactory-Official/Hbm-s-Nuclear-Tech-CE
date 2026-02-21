package com.hbm.packet.toclient;

import com.hbm.items.weapon.ItemGunBase;
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

import java.util.UUID;

public class GunFXPacket implements IMessage {

	public String playerUUID;
	public EnumHand hand;
	public FXType type;
	
	public GunFXPacket() {
	}
	
	public GunFXPacket(EntityPlayer player, EnumHand hand, FXType type) {
		this.hand = hand;
		this.playerUUID = player.getUniqueID().toString();
		this.type = type;
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		hand = buf.readBoolean() ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
		byte[] bytes = new byte[buf.readInt()];
		buf.readBytes(bytes);
		playerUUID = new String(bytes);
		type = FXType.VALUES[buf.readInt()];
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeBoolean(hand == EnumHand.MAIN_HAND ? true : false);
		byte[] bytes = playerUUID.getBytes();
		buf.writeInt(bytes.length);
		buf.writeBytes(bytes);
		buf.writeInt(type.ordinal());
	}
	
	public static class Handler implements IMessageHandler<GunFXPacket, IMessage> {

		@Override
		@SideOnly(Side.CLIENT)
		public IMessage onMessage(GunFXPacket m, MessageContext ctx) {
			Minecraft.getMinecraft().addScheduledTask(() -> {
				EntityPlayer player = Minecraft.getMinecraft().world.getPlayerEntityByUUID(UUID.fromString(m.playerUUID));
				ItemStack stack = player.getHeldItem(m.hand);
				if(stack.getItem() instanceof ItemGunBase){
					boolean isThirdPerson = Minecraft.getMinecraft().gameSettings.thirdPersonView > 0 || player != Minecraft.getMinecraft().player;
					if(m.type == FXType.FIRE)
						((ItemGunBase)stack.getItem()).onFireClient(stack, player, isThirdPerson);
				}
			});
			return null;
		}
		
	}

    //mlbv: why does it only have one enum constant?
	public static enum FXType {
		FIRE;

        public static final FXType[] VALUES = values();
	}
}
