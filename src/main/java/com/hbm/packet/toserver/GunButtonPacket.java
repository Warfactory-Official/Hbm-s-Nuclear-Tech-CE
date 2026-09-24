package com.hbm.packet.toserver;

import com.hbm.items.weapon.ItemGunBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class GunButtonPacket implements IMessage {

	//true or false, whether or not the key is pressed
		boolean state;
		//0: [M1]
		//1: [M2]
		//2: [R]
		byte button;
		//The hand the gun is in
		EnumHand hand;

		public GunButtonPacket() { }

		public GunButtonPacket(boolean m1, byte b, EnumHand hand) {
			state = m1;
			button = b;
			this.hand = hand;
		}

		@Override
		public void fromBytes(ByteBuf buf) {
			state = buf.readBoolean();
			button = buf.readByte();
			hand = buf.readBoolean() ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
		}

		@Override
		public void toBytes(ByteBuf buf) {
			buf.writeBoolean(state);
			buf.writeByte(button);
			buf.writeBoolean(hand == EnumHand.MAIN_HAND);
		}

		public static class Handler implements IMessageHandler<GunButtonPacket, IMessage> {

			@Override
			public IMessage onMessage(GunButtonPacket m, MessageContext ctx) {
				ctx.getServerHandler().player.getServer().addScheduledTask(() -> {
					if(FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
						return;
					
					EntityPlayer p = ctx.getServerHandler().player;
					
					if(!p.getHeldItem(m.hand).isEmpty() && p.getHeldItem(m.hand).getItem() instanceof ItemGunBase) {
						ItemGunBase item = (ItemGunBase)p.getHeldItem(m.hand).getItem();
						
						switch(m.button) {
						case 0: ItemGunBase.setIsMouseDown(p.getHeldItem(m.hand), m.state);
								if(m.state)
									item.startAction(p.getHeldItem(m.hand), p.world, p, true, m.hand);
								else
									item.endAction(p.getHeldItem(m.hand), p.world, p, true, m.hand);
								break;
								
						case 1: ItemGunBase.setIsAltDown(p.getHeldItem(m.hand), m.state);
								if(m.state)
									item.startAction(p.getHeldItem(m.hand), p.world, p, false, m.hand);
								else
									item.endAction(p.getHeldItem(m.hand), p.world, p, false, m.hand);
								break;
								
						case 2:
						if(item.canReload(p.getHeldItem(m.hand), p.world, p)) {
							item.startReloadAction(p.getHeldItem(m.hand), p.world, p, m.hand);
						}
								break;
						}
					}
				});
				
				
				return null;
			}
		}
}
