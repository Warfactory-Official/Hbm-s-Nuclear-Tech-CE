package com.hbm.packet;

import com.hbm.handler.JetpackHandler;
import com.hbm.handler.JetpackHandler.JetpackInfo;
import com.hbm.packet.threading.PrecompiledPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class JetpackSyncPacket extends PrecompiledPacket {

	int playerId;
	JetpackInfo info;

	public JetpackSyncPacket() {
	}

	public JetpackSyncPacket(EntityPlayer player) {
		playerId = player.getEntityId();
		info = JetpackHandler.get(player);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		playerId = buf.readInt();
		info = new JetpackInfo(false);
		info.read(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(playerId);
		info.write(buf);
	}

	public static class Handler implements IMessageHandler<JetpackSyncPacket, IMessage> {

		@Override
		public IMessage onMessage(JetpackSyncPacket message, MessageContext ctx) {
			if(ctx.side == Side.SERVER) {
				ctx.getServerHandler().player.server.addScheduledTask(() -> {
					EntityPlayer player = ctx.getServerHandler().player;
					JetpackInfo info = JetpackHandler.get(player);
					if(info == null) {
						JetpackHandler.put(player, new JetpackInfo(false));
					}
					JetpackHandler.put(player, message.info);
				});
			} else {
				handleMessageClient(message, ctx);
			}
			return null;
		}

		@SideOnly(Side.CLIENT)
		public void handleMessageClient(JetpackSyncPacket m, MessageContext ctx) {
			Minecraft.getMinecraft().addScheduledTask(() -> {
				World world = Minecraft.getMinecraft().world;
				Entity ent = world.getEntityByID(m.playerId);
				if(ent instanceof EntityPlayer) {
					EntityPlayer player = (EntityPlayer) ent;
					JetpackInfo info = JetpackHandler.get(player);
					if(info == null) {
						info = new JetpackInfo(true);
						JetpackHandler.put(player, info);
					}
					info.setFromServer(m.info);
				}
			});
		}
	}
}
