package com.hbm.tileentity.machine.pile;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.generic.BlockMeta;
import com.hbm.blocks.machine.pile.BlockPile;
import com.hbm.handler.CompatHandler;
import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.machine.pile.TileEntityPileCore.PileChannel;
import com.hbm.util.Compat;

import com.hbm.api.redstoneoverradio.IRORInteractive;
import io.netty.buffer.ByteBuf;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.Optional;

@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")})
@AutoRegister
public class TileEntityPileControl extends TileEntityPileDeviceBase implements IRORInteractive, CompatHandler.OCComponent {

	public double syncLevel;
	public double level;
	public double lastLevel;

	public int turnProgress;

	public double targetLevel;
	public static final double SPEED = 1D / 60D;
	public boolean wasRedstone;

	@Override
	public void update() {

		if(!world.isRemote) {

			boolean canMove = false;

			BlockPos below = pos.down();
			IBlockState belowState = world.getBlockState(below);

			if(belowState.getBlock() == ModBlocks.pile_block && belowState.getValue(BlockMeta.META) == BlockPile.META_CONTROL) {

				TileEntity tile = Compat.getTileStandard(world, below.getX(), below.getY(), below.getZ());

				if(tile instanceof TileEntityPileBaseMK2) {
					TileEntityPileBaseMK2 pile = (TileEntityPileBaseMK2) tile;
					TileEntityPileCore core = pile.getCore();

					if(core != null) {
						PileChannel controlChan = core.getControlChannel(below.getX(), below.getY(), below.getZ());

						if(controlChan != null) {
							canMove = true;
							this.chanNum = core.getControlChannelNum(controlChan);
							controlChan.control = this.level;
						}
					}
				}
			}

			if(canMove && this.level != this.targetLevel) {
				if(Math.abs(level - targetLevel) <= SPEED) {
					this.level = this.targetLevel;
				} else if(level < targetLevel) {
					this.level += SPEED;
				} else if(level > targetLevel) {
					this.level -= SPEED;
				}
			}

			ForgeDirection dir = this.getOrientation();
			boolean redstone = world.getRedstonePower(pos.add(dir.offsetX, 0, dir.offsetZ), dir.getOpposite().toEnumFacing()) > 0;

			if(redstone && !wasRedstone) this.setTarget(1D);
			if(!redstone && wasRedstone) this.setTarget(0D);

			this.wasRedstone = redstone;

			this.networkPackNT(100);

		} else {

			this.lastLevel = this.level;

			if(this.turnProgress > 0) {
				this.level = this.level + ((this.syncLevel - this.level) / (double) this.turnProgress);
				--this.turnProgress;
			} else {
				this.level = this.syncLevel;
			}
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeDouble(this.level);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		double lastSync = this.syncLevel;
		this.syncLevel = buf.readDouble();

		if(this.syncLevel != lastSync) this.turnProgress = 2;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.level = nbt.getDouble("level");
		this.targetLevel = nbt.getDouble("targetLevel");
		this.wasRedstone = nbt.getBoolean("redstone");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setDouble("level", level);
		nbt.setDouble("targetLevel", targetLevel);
		nbt.setBoolean("wasRedstone", wasRedstone);
		return nbt;
	}

	@Override
	public String[] getFunctionInfo() {
		return new String[] {
				PREFIX_FUNCTION + "setrods" + NAME_SEPARATOR + "percent",
				PREFIX_FUNCTION + "extendrods" + NAME_SEPARATOR + "percent"
		};
	}

	@Override
	public String runRORFunction(String name, String[] params) {

		if((PREFIX_FUNCTION + "setrods").equals(name) && params.length > 0) {
			int percent = IRORInteractive.parseInt(params[0], 0, 100);
			this.setTarget(percent / 100D);
			this.markChanged();
			return null;
		}

		if((PREFIX_FUNCTION + "extendrods").equals(name) && params.length > 0) {
			int percent = IRORInteractive.parseInt(params[0], -100, 100);
			this.setTarget(MathHelper.clamp(this.targetLevel + percent / 100D, 0D, 1D));
			this.markChanged();
			return null;
		}

		return null;
	}

	public void setTarget(double target) {
		this.targetLevel = target;
	}

	@Override
	@Optional.Method(modid = "opencomputers")
	public String getComponentName() {
		return "ntm_pile_control";
	}

	@Callback(direct = true, doc = "function():number - Returns current level")
	@Optional.Method(modid = "opencomputers")
	public Object[] getLevel(Context context, Arguments args) {
		return new Object[] {this.level};
	}

	@Callback(direct = true, limit = 4, doc = "function(targ: number) - Sets target level (0-100)")
	@Optional.Method(modid = "opencomputers")
	public Object[] setLevel(Context context, Arguments args) {
		setTarget(MathHelper.clamp(args.checkDouble(0) / 100D, 0D, 1D));
		return new Object[] {};
	}
}
