package com.hbm.tileentity.machine;

import com.hbm.api.block.ICrucibleAcceptor;
import com.hbm.interfaces.ICopiable;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.TileEntityLoadedBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Base class for all foundry channel type blocks - channels, casts, basins, tanks, etc.
 * Foundry type blocks can only hold one type at a time and usually either store or move it around.
 * @author hbm
 *
 */
public abstract class TileEntityFoundryBase extends TileEntityLoadedBase implements ITickable, ICrucibleAcceptor, ICopiable {
	
	public NTMMaterial type;
	protected NTMMaterial lastType;
	public int amount;
	protected int lastAmount;
	
	@Override
	public void update() {
		if(this.lastType != this.type || this.lastAmount != this.amount){
			if(!world.isRemote || (world.isRemote && shouldClientReRender())) {
			
				IBlockState state = world.getBlockState(pos);
				world.markAndNotifyBlock(pos, world.getChunk(pos), state, state, 2);
				this.lastType = this.type;
				this.lastAmount = this.amount;
				this.markDirty();
			}
		}
	}
	
	/** Recommended FALSE for things that update a whole lot. TRUE if updates only happen once every few ticks. */
	protected boolean shouldClientReRender() {
		return true;
	}
	
	@Override
	public void serializeInitial(ByteBuf buf) {
		super.serializeInitial(buf);
		NBTTagCompound nbt = new NBTTagCompound();
		this.writeToNBT(nbt);
		ByteBufUtils.writeTag(buf, nbt);
	}

	@Override
	public void deserializeInitial(ByteBuf buf) {
		super.deserializeInitial(buf);
		NBTTagCompound nbt = ByteBufUtils.readTag(buf);
		if (nbt != null) this.readFromNBT(nbt);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		if(nbt.hasKey("type"))
			this.type = Mats.matById.get(nbt.getInteger("type"));
		if(nbt.hasKey("amount"))
			this.amount = nbt.getInteger("amount");
		super.readFromNBT(nbt);
	}

	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setInteger("type", this.type == null ? -1 : this.type.id);
		nbt.setInteger("amount", this.amount);
		return super.writeToNBT(nbt);
	}
	
	public abstract int getCapacity();
	
	/**
	 * Standard check for testing if this material stack can be added to the casting block. Checks:<br>
	 * - type matching<br>
	 * - amount being at max<br>
	 */
	public boolean standardCheck(World world, BlockPos p, ForgeDirection side, MaterialStack stack) {
		if(this.type != null && this.type != stack.material && this.amount > 0) return false; //reject if there's already a different material
        return this.amount < this.getCapacity(); //reject if the buffer is already full
    }
	
	/**
	 * Standardized adding of material via pouring or flowing. Does:<br>
	 * - sets material to match the input
	 * - adds the amount, not exceeding the maximum
	 * - returns the amount that cannot be added
	 */
	public MaterialStack standardAdd(World world, BlockPos p, ForgeDirection side, MaterialStack stack) {
		this.type = stack.material;
		
		if(stack.amount + this.amount <= this.getCapacity()) {
			this.amount += stack.amount;
			return null;
		}
		
		int required = this.getCapacity() - this.amount;
		this.amount = this.getCapacity();
		
		stack.amount -= required;
		
		return stack;
	}

	/** Standard check with no additional limitations added */
	@Override
	public boolean canAcceptPartialFlow(World world, BlockPos p, ForgeDirection side, MaterialStack stack) {
		return this.standardCheck(world, p, side, stack);
	}
	
	/** Standard flow, no special handling required */
	@Override
	public MaterialStack flow(World world, BlockPos p, ForgeDirection side, MaterialStack stack) {
		return this.standardAdd(world, p, side, stack);
	}

	/** Standard check, but with the additional limitation that the only valid source direction is UP */
	@Override
	public boolean canAcceptPartialPour(World world, BlockPos p, double dX, double dY, double dZ, ForgeDirection side, MaterialStack stack) {
		if(side != ForgeDirection.UP) return false;
		return this.standardCheck(world, p, side, stack);
	}

	/** Standard flow, no special handling required */
	@Override
	public MaterialStack pour(World world, BlockPos p, double dX, double dY, double dZ, ForgeDirection side, MaterialStack stack) {
		return this.standardAdd(world, p, side, stack);
	}

	@Override
	public NBTTagCompound getSettings(World world, int x, int y, int z) {
		NBTTagCompound nbt = new NBTTagCompound();
		if(type != null) nbt.setIntArray("matFilter", new int[]{ type.id });
		return nbt;
	}

	@Override
	public void pasteSettings(NBTTagCompound nbt, int index, World world, EntityPlayer player, int x, int y, int z) {

	}
}
