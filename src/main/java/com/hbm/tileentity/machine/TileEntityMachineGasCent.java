package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.api.fluid.IFluidStandardReceiver;
import com.hbm.blocks.BlockDummyable;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IFFtoNTMF;
import com.hbm.inventory.container.ContainerMachineGasCent;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUIMachineGasCent;
import com.hbm.inventory.recipes.GasCentrifugeRecipes;
import com.hbm.items.ModItems;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BufferUtil;
import com.hbm.util.InventoryUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

@AutoRegister
public class TileEntityMachineGasCent extends TileEntityMachineBase implements ITickable, IEnergyReceiverMK2, IFluidStandardReceiver, IGUIProvider, IFFtoNTMF {

	public int progress;
	public long power;
	public boolean isProgressing;
	public static final int maxPower = 100000;
	public static final int processingSpeed = 150;

	public FluidTankNTM tank;
	public PseudoFluidTank inputTank;
	public PseudoFluidTank outputTank;

	private int audioDuration = 0;
	private AudioWrapper audio;
	private static final int[] slots_io = new int[] { 0, 1, 2, 3 };

	public TileEntityMachineGasCent() {
		// 0~3 = output,
		// 4 = battery
		// 5 = fluid ID
		// 6 = upgrade
		super(9, true, true);
		tank = new FluidTankNTM(Fluids.UF6, 2000);
		inputTank = new PseudoFluidTank(GasCentrifugeRecipes.PseudoFluidType.NUF6, 8000);
		outputTank = new PseudoFluidTank(GasCentrifugeRecipes.PseudoFluidType.LEUF6, 8000);
	}

	public String getDefaultName() {
		return "container.gasCentrifuge";
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		power = nbt.getLong("power");
		progress = nbt.getShort("progress");
		tank.readFromNBT(nbt, "tank");
		inputTank.readFromNBT(nbt, "inputTank");
		outputTank.readFromNBT(nbt, "outputTank");

		super.readFromNBT(nbt);
	}

	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt.setLong("power", power);
		nbt.setShort("progress", (short) progress);
		tank.writeToNBT(nbt, "tank");
		inputTank.writeToNBT(nbt, "inputTank");
		outputTank.writeToNBT(nbt, "outputTank");
		return super.writeToNBT(nbt);
	}

	public int getCentrifugeProgressScaled(int i) {
		return (progress * i) / processingSpeed;
	}

	public long getPowerRemainingScaled(int i) {
		return (power * i) / maxPower;
	}

	private boolean canEnrich() {
		if(power > 0 && this.inputTank.getFill() >= inputTank.getTankType().getFluidConsumed() && this.outputTank.getFill() + this.inputTank.getTankType().getFluidProduced() <= outputTank.getMaxFill()) {

			ItemStack[] list = inputTank.getTankType().getOutput();

			if(list == null)
				return false;

			if(list.length < 1)
				return false;

            return InventoryUtil.doesArrayHaveSpace(inventory, 0, 3, list);
		}

		return false;
	}

	private void enrich() {
		ItemStack[] output = inputTank.getTankType().getOutput();

		this.progress = 0;
		inputTank.setFill(inputTank.getFill() - inputTank.getTankType().getFluidConsumed());
		outputTank.setFill(outputTank.getFill() + inputTank.getTankType().getFluidProduced());

        for (ItemStack itemStack : output)
			InventoryUtil.tryAddItemToInventory(inventory, 0, 3, itemStack.copy());
	}

	private void attemptConversion() {
		if(inputTank.getFill() < inputTank.getMaxFill() && tank.getFill() > 0) {
			int fill = Math.min(inputTank.getMaxFill() - inputTank.getFill(), tank.getFill());

			tank.setFill(tank.getFill() - fill);
			inputTank.setFill(inputTank.getFill() + fill);
		}
	}

	private boolean attemptTransfer(TileEntity te) {
		if(te instanceof TileEntityMachineGasCent cent) {

            if(cent.tank.getFill() == 0 && cent.tank.getTankType() == tank.getTankType()) {
				if(cent.inputTank.getTankType() != outputTank.getTankType() && outputTank.getTankType() != GasCentrifugeRecipes.PseudoFluidType.NONE) {
					cent.inputTank.setTankType(outputTank.getTankType());
					cent.outputTank.setTankType(outputTank.getTankType().getOutputType());
				}

				//God, why did I forget about the entirety of the fucking math library?
				if(cent.inputTank.getFill() < cent.inputTank.getMaxFill() && outputTank.getFill() > 0) {
					int fill = Math.min(cent.inputTank.getMaxFill() - cent.inputTank.getFill(), outputTank.getFill());

					outputTank.setFill(outputTank.getFill() - fill);
					cent.inputTank.setFill(cent.inputTank.getFill() + fill);
				}

				return true;
			}
		}

		return false;
	}

	@Override
	public void update() {

		if(!world.isRemote) {
			updateConnections();

			power = Library.chargeTEFromItems(inventory, 4, power, maxPower);
			setTankType(5);

			if (GasCentrifugeRecipes.fluidConversions.containsValue(inputTank.getTankType())) {
				attemptConversion();
			}

			if (canEnrich()) {

				isProgressing = true;
				this.progress++;
				this.power -= 200;

				if (this.power < 0) {
					power = 0;
					this.progress = 0;
				}

				if (progress >= processingSpeed)
					enrich();

			} else {
				isProgressing = false;
				this.progress = 0;
			}

			if (world.getTotalWorldTime() % 10 == 0) {
				ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
				TileEntity te = world.getTileEntity(pos.add(-dir.offsetX, 0, -dir.offsetZ));

				//*AT THE MOMENT*, there's not really any need for a dedicated method for this. Yet.
				if (!attemptTransfer(te) && this.inputTank.getTankType() == GasCentrifugeRecipes.PseudoFluidType.LEUF6) {
					ItemStack[] converted = new ItemStack[]{new ItemStack(ModItems.nugget_uranium_fuel, 6), new ItemStack(ModItems.fluorite)};

					if (this.outputTank.getFill() >= 600 && InventoryUtil.doesArrayHaveSpace(inventory, 0, 3, converted)) {
						this.outputTank.setFill(this.outputTank.getFill() - 600);
						for (ItemStack stack : converted)
							InventoryUtil.tryAddItemToInventory(inventory, 0, 3, stack);
					}
				}
			}

			this.networkPackNT(50);
		} else {

			if(isProgressing) {
				audioDuration += 2;
			} else {
				audioDuration -= 3;
			}

			audioDuration = MathHelper.clamp(audioDuration, 0, 60);

			if(audioDuration > 10 && MainRegistry.proxy.me().getDistance(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < 25) {

				if(audio == null) {
					audio = createAudioLoop();
					audio.startSound();
				} else if(!audio.isPlaying()) {
					audio = rebootAudio(audio);
				}

				audio.updateVolume(getVolume(1F));
				audio.updatePitch((audioDuration - 10) / 100F + 0.5F);
				audio.keepAlive();

			} else {

				if(audio != null) {
					audio.stopSound();
					audio = null;
				}
			}
		}
	}

	@Override
	public AudioWrapper createAudioLoop() {
		return MainRegistry.proxy.getLoopedSound(HBMSoundHandler.centrifugeOperate, SoundCategory.BLOCKS, pos.getX(), pos.getY(), pos.getZ(), 1.0F, 10F, 1.0F, 20);
	}

	@Override
	public void onChunkUnload() {

		if(audio != null) {
			audio.stopSound();
			audio = null;
		}
	}

	@Override
	public void invalidate() {

		super.invalidate();

		if(audio != null) {
			audio.stopSound();
			audio = null;
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeLong(power);
		buf.writeInt(progress);
		buf.writeBoolean(isProgressing);
		//pseudofluids can be refactored another day
		buf.writeInt(inputTank.getFill());
		buf.writeInt(outputTank.getFill());
		BufferUtil.writeString(buf, inputTank.getTankType().name);
		BufferUtil.writeString(buf, outputTank.getTankType().name);

		tank.serialize(buf);
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		power = buf.readLong();
		progress = buf.readInt();
		isProgressing = buf.readBoolean();

		inputTank.setFill(buf.readInt());
		outputTank.setFill(buf.readInt());
		inputTank.setTankType(GasCentrifugeRecipes.PseudoFluidType.types.get(BufferUtil.readString(buf)));
		outputTank.setTankType(GasCentrifugeRecipes.PseudoFluidType.types.get(BufferUtil.readString(buf)));

		tank.deserialize(buf);
	}

	private void updateConnections() {
		for(DirPos dirPos : getConPos()) {
			this.trySubscribe(world, dirPos.getPos().getX(), dirPos.getPos().getY(), dirPos.getPos().getZ(), dirPos.getDir());

			if(GasCentrifugeRecipes.fluidConversions.containsValue(inputTank.getTankType())) {
				this.trySubscribe(tank.getTankType(), world, dirPos.getPos().getX(), dirPos.getPos().getY(), dirPos.getPos().getZ(), dirPos.getDir());
			}
		}
	}

	private DirPos[] getConPos() {
		return new DirPos[] {
				new DirPos(pos.getX(), pos.getY() - 1, pos.getZ(), Library.NEG_Y),
				new DirPos(pos.getX() + 1, pos.getY(), pos.getZ(), Library.POS_X),
				new DirPos(pos.getX() - 1, pos.getY(), pos.getZ(), Library.NEG_X),
				new DirPos(pos.getX(), pos.getY(), pos.getZ() + 1, Library.POS_Z),
				new DirPos(pos.getX(), pos.getY(), pos.getZ() - 1, Library.NEG_Z)
		};
	}

	public void setTankType(int in) {

		if(!inventory.getStackInSlot(in).isEmpty() && inventory.getStackInSlot(in).getItem() instanceof IItemFluidIdentifier id) {
            FluidType newType = id.getType(world, pos.getX(), pos.getY(), pos.getZ(), inventory.getStackInSlot(in));

			if(tank.getTankType() != newType) {
				GasCentrifugeRecipes.PseudoFluidType pseudo = GasCentrifugeRecipes.fluidConversions.get(newType);

				if(pseudo != null) {
					inputTank.setTankType(pseudo);
					outputTank.setTankType(pseudo.getOutputType());
					tank.setTankType(newType);
				}
			}

		}
	}

	@Override
	public boolean canExtractItem(int slot, ItemStack itemStack, int amount) {
		return slot < 4;
	}

	@Override
	public int[] getAccessibleSlotsFromSide(EnumFacing e){
		return slots_io;
	}

	@Override
	public @NotNull AxisAlignedBB getRenderBoundingBox() {
		return new AxisAlignedBB(pos, pos.add(1, 4, 1));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared()
	{
		return 65536.0D;
	}

	@Override
	public void setPower(long i) {
		power = i;
	}

	@Override
	public long getPower() {
		return power;
	}

	@Override
	public long getMaxPower() {
		return maxPower;
	}

	@Override
	public FluidTankNTM[] getReceivingTanks() {
		return new FluidTankNTM[] {tank};
	}

	@Override
	public FluidTankNTM[] getAllTanks() {
		return new FluidTankNTM[] {tank};
	}

	public static class PseudoFluidTank {
		GasCentrifugeRecipes.PseudoFluidType type;
		int fluid;
		int maxFluid;

		public PseudoFluidTank(GasCentrifugeRecipes.PseudoFluidType type, int maxFluid) {
			this.type = type;
			this.maxFluid = maxFluid;
		}

		public void setFill(int i) {
			fluid = i;
		}

		public void setTankType(GasCentrifugeRecipes.PseudoFluidType type) {

			if(this.type.equals(type))
				return;

			if(type == null)
				this.type = GasCentrifugeRecipes.PseudoFluidType.NONE;
			else
				this.type = type;

			this.setFill(0);
		}

		public GasCentrifugeRecipes.PseudoFluidType getTankType() {
			return type;
		}

		public int getFill() {
			return fluid;
		}

		public int getMaxFill() {
			return maxFluid;
		}

		//Called by TE to save fillstate
		public void writeToNBT(NBTTagCompound nbt, String s) {
			nbt.setInteger(s, fluid);
			nbt.setInteger(s + "_max", maxFluid);
			nbt.setString(s + "_type", type.name);
		}

		//Called by TE to load fillstate
		public void readFromNBT(NBTTagCompound nbt, String s) {
			fluid = nbt.getInteger(s);
			int max = nbt.getInteger(s + "_max");
			if(max > 0) maxFluid = nbt.getInteger(s + "_max");
			type = GasCentrifugeRecipes.PseudoFluidType.types.get(nbt.getString(s + "_type"));
			if(type == null) type = GasCentrifugeRecipes.PseudoFluidType.NONE;
		}

		/*        ______      ______
		 *       _I____I_    _I____I_
		 *      /      \\\  /      \\\
		 *     |IF{    || ||     } || |
		 *     | IF{   || ||    }  || |
		 *     |  IF{  || ||   }   || |
		 *     |   IF{ || ||  }    || |
		 *     |    IF{|| || }     || |
		 *     |       || ||       || |
		 *     |     } || ||IF{    || |
		 *     |    }  || || IF{   || |
		 *     |   }   || ||  IF{  || |
		 *     |  }    || ||   IF{ || |
		 *     | }     || ||    IF{|| |
		 *     |IF{    || ||     } || |
		 *     | IF{   || ||    }  || |
		 *     |  IF{  || ||   }   || |
		 *     |   IF{ || ||  }    || |
		 *     |    IF{|| || }     || |
		 *     |       || ||       || |
		 *     |     } || ||IF{	   || |
		 *     |    }  || || IF{   || |
		 *     |   }   || ||  IF{  || |
		 *     |  }    || ||   IF{ || |
		 *     | }     || ||    IF{|| |
		 *     |IF{    || ||     } || |
		 *     | IF{   || ||    }  || |
		 *     |  IF{  || ||   }   || |
		 *     |   IF{ || ||  }    || |
		 *     |    IF{|| || }     || |
		 *     |       || ||       || |
		 *     |     } || ||IF{	   || |
		 *     |    }  || || IF{   || |
		 *     |   }   || ||  IF{  || |
		 *     |  }    || ||   IF{ || |
		 *     | }     || ||    IF{|| |
		 *    _|_______||_||_______||_|_
		 *   |                          |
		 *   |                          |
		 *   |       |==========|       |
		 *   |       |NESTED    |       |
		 *   |       |IF  (:    |       |
		 *   |       |STATEMENTS|       |
		 *   |       |==========|       |
		 *   |                          |
		 *   |                          |
		 *   ----------------------------
		 *
		 *that's sick af..
		 *
		 */
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerMachineGasCent(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIMachineGasCent(player.inventory, this);
	}
}
