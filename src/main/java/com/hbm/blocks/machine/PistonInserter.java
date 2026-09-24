package com.hbm.blocks.machine;

import com.hbm.api.block.IInsertable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.util.BufferUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

public class PistonInserter extends BlockContainer {

	public static final PropertyDirection FACING = BlockDirectional.FACING;

	public PistonInserter(Material materialIn, String s) {
		super(materialIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));

		ModBlocks.ALL_BLOCKS.add(this);
	}

	@Override
	public TileEntity createNewTileEntity(@NotNull World world, int meta) {
		return new TileEntityPistonInserter();
	}

	@Override
	protected @NotNull BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, FACING);
	}

	@Override
	public @NotNull IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(FACING, EnumFacing.byIndex(meta));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(FACING).getIndex();
	}

	@Override
	public @NotNull IBlockState getStateForPlacement(@NotNull World world, @NotNull BlockPos pos, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ, int meta, @NotNull EntityLivingBase placer) {
		return this.getDefaultState().withProperty(FACING, EnumFacing.getDirectionFromEntityLiving(pos, placer));
	}

	@Override
	public void neighborChanged(@NotNull IBlockState state, World world, @NotNull BlockPos pos, @NotNull Block neighbor, @NotNull BlockPos fromPos) {
		this.updateState(world, pos, state);
	}

	protected void updateState(World world, BlockPos pos, IBlockState state) {
		if(world.isRemote) return;

		EnumFacing dir = state.getValue(FACING);
		if(world.getBlockState(pos.offset(dir)).isNormalCube()) return; // no obstructions allowed

		TileEntity te = world.getTileEntity(pos);
		if(!(te instanceof TileEntityPistonInserter)) return;
		TileEntityPistonInserter piston = (TileEntityPistonInserter) te;

		boolean flag = world.isBlockPowered(pos);

		if(flag && !piston.lastState && piston.extend <= 0)
			piston.isRetracting = false;

		piston.lastState = flag;
	}

	@AutoRegister(name = "tileentity_piston_inserter")
	public static class TileEntityPistonInserter extends TileEntityLoadedBase implements ITickable {

		public ItemStack slot = ItemStack.EMPTY;

		public int extend;
		public static final int maxExtend = 25;
		public boolean isRetracting = true;
		public int delay;

		private boolean lastState;

		@SideOnly(Side.CLIENT) public double renderExtend;
		@SideOnly(Side.CLIENT) public double lastExtend;
		@SideOnly(Side.CLIENT) private int syncExtend;
		@SideOnly(Side.CLIENT) private int turnProgress;

		@Override
		public void update() {

			if(!world.isRemote) {

				if(delay <= 0) {

					if(this.isRetracting && this.extend > 0) {
						this.extend--;
					} else if(!this.isRetracting) {
						this.extend++;

						if(this.extend >= maxExtend) {
							world.playSound(null, pos, HBMSoundHandler.pressOperate, SoundCategory.BLOCKS, 1.0F, 1.5F);

							EnumFacing facing = world.getBlockState(pos).getValue(FACING);
							BlockPos target = pos.offset(facing, 2);
							Block b = world.getBlockState(target).getBlock();

							if(b instanceof IInsertable && ((IInsertable) b).insertItem(world, target.getX(), target.getY(), target.getZ(), facing, slot)) {
								this.slot.shrink(1);
								if(this.slot.isEmpty()) this.slot = ItemStack.EMPTY;
							}

							this.isRetracting = true;
							this.delay = 5;
						}
					}

				} else {
					delay--;
				}

				networkPackNT(25);

			} else {
				this.lastExtend = this.renderExtend;

				if(this.turnProgress > 0) {
					this.renderExtend += (this.syncExtend - this.renderExtend) / (double) this.turnProgress;
					this.turnProgress--;
				} else {
					this.renderExtend = this.syncExtend;
				}
			}
		}

		@Override
		public void serialize(ByteBuf buf) {
			buf.writeInt(extend);
			buf.writeBoolean(!this.slot.isEmpty());
			if(!this.slot.isEmpty()) BufferUtil.writeNBT(buf, this.slot.writeToNBT(new NBTTagCompound()));
		}

		@Override
		public void deserialize(ByteBuf buf) {
			this.syncExtend = buf.readInt();

			if(buf.readBoolean()) {
				this.slot = new ItemStack(BufferUtil.readNBT(buf));
			} else {
				this.slot = ItemStack.EMPTY;
			}

			this.turnProgress = 2;
		}

		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);
			this.extend = nbt.getInteger("extend");
			this.isRetracting = nbt.getBoolean("isRetracting");
			this.delay = nbt.getInteger("delay");
			this.lastState = nbt.getBoolean("lastState");
			this.slot = nbt.hasKey("slot") ? new ItemStack(nbt.getCompoundTag("slot")) : ItemStack.EMPTY;
		}

		@Override
		public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			nbt.setInteger("extend", extend);
			nbt.setBoolean("isRetracting", isRetracting);
			nbt.setInteger("delay", delay);
			nbt.setBoolean("lastState", lastState);
			if(!this.slot.isEmpty()) nbt.setTag("slot", this.slot.writeToNBT(new NBTTagCompound()));
			return super.writeToNBT(nbt);
		}
	}
}
