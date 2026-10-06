package com.hbm.packet.toserver;

import com.hbm.api.fluidmk2.IFluidStandardReceiverMK2;
import com.hbm.api.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.tileentity.network.TileEntityPipeBaseNT;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class SchematicFluidPacket implements IMessage {

	long pos;
	int fluid;

	public SchematicFluidPacket() {
	}

	public SchematicFluidPacket(BlockPos pos, FluidType fluid) {
		this.pos = pos.toLong();
		this.fluid = fluid.getID();
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		pos = buf.readLong();
		fluid = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeLong(pos);
		buf.writeInt(fluid);
	}

	public static FluidTankNTM getTank(TileEntity te) {
		if(te instanceof IFluidCopiable copiable) {
			FluidTankNTM tank = copiable.getTankToPaste();
			if(tank != null || te instanceof IFluidStandardTransceiverMK2) return tank;
		}
		if(te instanceof IFluidStandardReceiverMK2 receiver) {
			FluidTankNTM[] tanks = receiver.getReceivingTanks();
			if(tanks.length > 0) return tanks[0];
		}
		return null;
	}

	public static FluidType getFluid(TileEntity te) {
		if(te instanceof TileEntityPipeBaseNT pipe) return pipe.getType();
		FluidTankNTM tank = getTank(te);
		return tank != null ? tank.getTankType() : null;
	}

	public static boolean hasIdentifier(EntityPlayer player) {
		for(int i = 0; i < player.inventory.getSizeInventory(); i++) {
			ItemStack stack = player.inventory.getStackInSlot(i);
			if(!stack.isEmpty() && stack.getItem() instanceof IItemFluidIdentifier) return true;
		}
		return false;
	}

	public static class Handler implements IMessageHandler<SchematicFluidPacket, IMessage> {

		@Override
		public IMessage onMessage(SchematicFluidPacket m, MessageContext ctx) {
			ctx.getServerHandler().player.getServer().addScheduledTask(() -> {
				EntityPlayerMP p = ctx.getServerHandler().player;
				BlockPos pos = BlockPos.fromLong(m.pos);
				FluidType type = Fluids.fromID(m.fluid);

				if(type == Fluids.NONE || !p.world.isBlockLoaded(pos) || p.getDistanceSq(pos) > 144 || !hasIdentifier(p)) return;

				TileEntity te = p.world.getTileEntity(pos);
				FluidType current = getFluid(te);
				if(current == null || current == type) return;

				if(te instanceof TileEntityPipeBaseNT pipe) {
					pipe.setType(type);
				} else {
					getTank(te).setTankType(type);
					te.markDirty();
				}
			});

			return null;
		}
	}
}
