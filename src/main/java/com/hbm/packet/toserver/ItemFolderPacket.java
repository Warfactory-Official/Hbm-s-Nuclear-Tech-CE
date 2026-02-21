package com.hbm.packet.toserver;
import com.hbm.items.ModItems;
import com.hbm.items.machine.*;
import com.hbm.lib.Library;
import com.hbm.util.InventoryUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.io.IOException;

public class ItemFolderPacket implements IMessage {

	private ItemStack stack;

	public ItemFolderPacket() {

	}

	public ItemFolderPacket(ItemStack stack) {
		this.stack = stack;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		PacketBuffer packetBuffer = new PacketBuffer(buf);
		try {
			this.stack = packetBuffer.readItemStack();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		PacketBuffer packetBuffer = new PacketBuffer(buf);
		packetBuffer.writeItemStack(this.stack);
	}

	public static class Handler implements IMessageHandler<ItemFolderPacket, IMessage> {

		@Override
		public IMessage onMessage(ItemFolderPacket m, MessageContext ctx) {

			EntityPlayer p = ctx.getServerHandler().player;
			ItemStack stack = m.stack;
			p.getServer().addScheduledTask(() -> {

				if (!(p.getHeldItemMainhand().getItem() instanceof ItemTemplateFolder) && !(p.getHeldItemOffhand().getItem() instanceof ItemTemplateFolder))
					return;

				if(p.capabilities.isCreativeMode) {
					p.inventory.addItemStackToInventory(stack.copy());
					return;
				}

				Item item = stack.getItem();

				if (item instanceof ItemCrucibleTemplate) {
					tryMakeItem(p, stack, Items.PAPER, "dye");
				} else if (item instanceof ItemCassette) {
					tryMakeItem(p, stack, ModItems.plate_polymer, "plateSteel");
				} else if (item == ModItems.stamp_stone_plate || item == ModItems.stamp_stone_wire || item == ModItems.stamp_stone_circuit) {
					tryConvert(p, ModItems.stamp_stone_flat, stack.getItem());
				} else if (item == ModItems.stamp_iron_plate || item == ModItems.stamp_iron_wire || item == ModItems.stamp_iron_circuit) {
					tryConvert(p, ModItems.stamp_iron_flat, stack.getItem());
				} else if (item == ModItems.stamp_steel_plate || item == ModItems.stamp_steel_wire || item == ModItems.stamp_steel_circuit) {
					tryConvert(p, ModItems.stamp_steel_flat, stack.getItem());
				} else if (item == ModItems.stamp_titanium_plate || item == ModItems.stamp_titanium_wire || item == ModItems.stamp_titanium_circuit) {
					tryConvert(p, ModItems.stamp_titanium_flat, stack.getItem());
				} else if (item == ModItems.stamp_obsidian_plate || item == ModItems.stamp_obsidian_wire || item == ModItems.stamp_obsidian_circuit) {
					tryConvert(p, ModItems.stamp_obsidian_flat, stack.getItem());
				} else if (item == ModItems.stamp_desh_plate || item == ModItems.stamp_desh_wire || item == ModItems.stamp_desh_circuit) {
					tryConvert(p, ModItems.stamp_desh_flat, stack.getItem());
				}
			});

			return null;
		}

		private void tryMakeItem(EntityPlayer player, ItemStack output, Object... ingredients) {

			//check
			for (Object o : ingredients) {

				if (o instanceof Item) {
					if (!Library.hasInventoryItem(player.inventory, (Item) o))
						return;
				}

				if (o instanceof String) {
					if (!InventoryUtil.hasOreDictMatches(player, (String) o, 1))
						return;
				}
			}

			//consume
			for (Object o : ingredients) {

				if (o instanceof Item) {
					Library.consumeInventoryItem(player.inventory, (Item) o);
				}

				if (o instanceof String) {
					InventoryUtil.consumeOreDictMatches(player, (String) o, 1);
				}
			}

			if (!player.inventory.addItemStackToInventory(output))
				player.dropItem(output, true);
		}

		private void tryConvert(EntityPlayer player, Item target, Item result) {
			for(int i = 0; i < player.inventory.getSizeInventory(); i++) {
				ItemStack stack = player.inventory.getStackInSlot(i);
				if(stack.getItem() == target) {
					Library.consumeInventoryItem(player.inventory, target);
					ItemStack drop = new ItemStack(result, 1, stack.getItemDamage());
					if (!player.inventory.addItemStackToInventory(drop))
						player.dropItem(drop, true);
				}
			}
		}
	}
}