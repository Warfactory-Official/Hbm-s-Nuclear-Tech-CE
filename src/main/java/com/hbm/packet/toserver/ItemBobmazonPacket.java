package com.hbm.packet.toserver;

import com.hbm.entity.missile.EntityBobmazon;
import com.hbm.handler.BobmazonOfferFactory;
import com.hbm.inventory.gui.GUIScreenBobmazon.Offer;
import com.hbm.items.ModItems;
import com.hbm.lib.ModDamageSource;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.Random;

public class ItemBobmazonPacket implements IMessage {

	int offer;

	public ItemBobmazonPacket()
	{
		
	}

	public ItemBobmazonPacket(EntityPlayer player, Offer offer)
	{
		if(player.getHeldItemMainhand().getItem() == ModItems.bobmazon)
			this.offer = BobmazonOfferFactory.standard.indexOf(offer);
		if(player.getHeldItemMainhand().getItem() == ModItems.bobmazon_hidden)
			this.offer = BobmazonOfferFactory.special.indexOf(offer);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		offer = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(offer);
	}

	public static class Handler implements IMessageHandler<ItemBobmazonPacket, IMessage> {
		
		@Override
		public IMessage onMessage(ItemBobmazonPacket m, MessageContext ctx) {
			
			EntityPlayerMP p = ctx.getServerHandler().player;
			
			p.getServer().addScheduledTask(() -> {
				World world = p.world;

				Item mainHand = p.getHeldItemMainhand().getItem();
				Item offHand = p.getHeldItemOffhand().getItem();
				Offer offer = null;
				if(mainHand == ModItems.bobmazon || offHand == ModItems.bobmazon)
					offer = BobmazonOfferFactory.standard.get(m.offer);
				if(mainHand == ModItems.bobmazon_hidden || offHand == ModItems.bobmazon_hidden)
					offer = BobmazonOfferFactory.special.get(m.offer);
				
				if(offer == null) {
					p.sendMessage(new TextComponentTranslation("chat.bobmazon.failsafe.1"));
					p.sendMessage(new TextComponentTranslation("chat.bobmazon.failsafe.2"));
					p.attackEntityFrom(ModDamageSource.nuclearBlast, 1000);
					p.motionY = 2.0D;
					return;
				}
				
				ItemStack stack = offer.offer;

				if(offer.requirement.fullfills(p) || p.capabilities.isCreativeMode) {
					
					if(countCaps(p) >= offer.cost || p.capabilities.isCreativeMode) {
						
						payCaps(p, offer.cost);
						p.inventoryContainer.detectAndSendChanges();
						
						Random rand = world.rand;
						EntityBobmazon bob = new EntityBobmazon(world);
						bob.posX = p.posX + rand.nextGaussian() * 10;
						bob.posY = 300;
						bob.posZ = p.posZ + rand.nextGaussian() * 10;
						bob.payload = stack;
						
						world.spawnEntity(bob);
					} else {
						p.sendMessage(new TextComponentTranslation("chat.bobmazon.broke"));
					}
					
				} else {
					p.sendMessage(new TextComponentTranslation("chat.bobmazon.noob"));
				}
			});
			
			
			return null;
		}
		
		private int countCaps(EntityPlayer player) {
			
			int count = 0;
			
			for(int i = 0; i < player.inventory.getSizeInventory(); i++) {
				
				ItemStack stack = player.inventory.getStackInSlot(i);
				
				if(!stack.isEmpty()) {
					
					Item item = stack.getItem();

					if(item == ModItems.cap_fritz ||
							item == ModItems.cap_korl ||
							item == ModItems.cap_nuka ||
							item == ModItems.cap_quantum ||
							item == ModItems.cap_rad ||
							item == ModItems.cap_sparkle)
						count += stack.getCount();
					
				}
			}
			
			return count;
		}
		
		private void payCaps(EntityPlayer player, int price) {
			
			if(price == 0)
				return;
			
			for(int i = 0; i < player.inventory.getSizeInventory(); i++) {
				
				ItemStack stack = player.inventory.getStackInSlot(i);
				
				if(!stack.isEmpty()) {
					
					Item item = stack.getItem();
					
					if(item == ModItems.cap_fritz ||
							item == ModItems.cap_korl ||
							item == ModItems.cap_nuka ||
							item == ModItems.cap_quantum ||
							item == ModItems.cap_rad ||
							item == ModItems.cap_sparkle) {
						
						int size = stack.getCount();
						for(int j = 0; j < size; j++) {
							
							player.inventory.decrStackSize(i, 1);
							price--;
							
							if(price == 0)
								return;
						}
					}
				}
			}
		}
	}
}