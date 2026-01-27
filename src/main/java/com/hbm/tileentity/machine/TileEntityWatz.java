package com.hbm.tileentity.machine;

import com.hbm.api.fluid.IFluidStandardTransceiver;
import com.hbm.blocks.ModBlocks;
import com.hbm.entity.projectile.EntityShrapnel;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.container.ContainerWatz;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.fluid.trait.FT_Heatable;
import com.hbm.inventory.fluid.trait.FT_Heatable.HeatingStep;
import com.hbm.inventory.gui.GUIWatz;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemWatzPellet;
import com.hbm.items.machine.ItemWatzPellet.EnumWatzType;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.main.AdvancementManager;
import com.hbm.main.MainRegistry;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.Compat;
import com.hbm.util.EnumUtil;
import com.hbm.util.Function;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@AutoRegister
public class TileEntityWatz extends TileEntityMachineBase implements ITickable, IFluidStandardTransceiver, IControlReceiver, IGUIProvider, IFluidCopiable {

	public FluidTankNTM[] tanks;
	private FluidTankNTM[] sharedTanks;
    private final FluidTankNTM[] sharedTanksSync;
	public int heat;
	private double fluxLastBase;		//flux created by the previous passive emission, only used for display
	private double fluxLastReaction;	//flux created by the previous reaction, used for the next reaction
	public double fluxDisplay;
	public boolean isOn;
	
	/* lock types for item IO */
	public boolean isLocked = false;
	private ItemStack[] locks;
	
	public TileEntityWatz() {
		super(24, 1, true, false);
		this.locks = new ItemStack[inventory.getSlots()];
		this.tanks = new FluidTankNTM[3];
		this.tanks[0] = new FluidTankNTM(Fluids.COOLANT, 64_000);
		this.tanks[1] = new FluidTankNTM(Fluids.COOLANT_HOT, 64_000);
		this.tanks[2] = new FluidTankNTM(Fluids.WATZ, 64_000);
		this.sharedTanksSync = new FluidTankNTM[3];
		this.sharedTanksSync[0] = new FluidTankNTM(Fluids.COOLANT, 0);
		this.sharedTanksSync[1] = new FluidTankNTM(Fluids.COOLANT_HOT, 0);
		this.sharedTanksSync[2] = new FluidTankNTM(Fluids.WATZ, 0);
		resetSharedTanks();
	}

	@Override
	public String getDefaultName() {
		return "container.watz";
	}

	private void resetSharedTanks() {
		this.sharedTanks = new FluidTankNTM[3];
		this.sharedTanks[0] = new FluidTankNTM(Fluids.COOLANT, 64_000);
		this.sharedTanks[1] = new FluidTankNTM(Fluids.COOLANT_HOT, 64_000);
		this.sharedTanks[2] = new FluidTankNTM(Fluids.WATZ, 64_000);
		this.sharedTanks[0].setFill(tanks[0].getFill());
		this.sharedTanks[1].setFill(tanks[1].getFill());
		this.sharedTanks[2].setFill(tanks[2].getFill());
	}

	@Override
	public void update() {

       if(!world.isRemote)
        resetSharedTanks();

		if (!world.isRemote && !updateLock()) {
            boolean turnedOn = world.getBlockState(pos.add(0, 3, 0)).getBlock() == ModBlocks.watz_pump && world.getRedstonePower(pos.add(0, 5, 0), EnumFacing.DOWN) > 0;
            List<TileEntityWatz> segments = new ArrayList<>();
            segments.add(this);
            this.subscribeToTop();

            /* accumulate all segments */
            for (int y = pos.getY() - 3; y >= 0; y -= 3) {
                TileEntity tile = Compat.getTileStandard(world, pos.getX(), y, pos.getZ());
                if (tile instanceof TileEntityWatz) {
                    segments.add((TileEntityWatz) tile);
                } else {
                    break;
                }
            }

            /* set up shared tanks */
            this.sharedTanks = new FluidTankNTM[3];
            for (int i = 0; i < 3; i++) this.sharedTanks[i] = new FluidTankNTM(tanks[i].getTankType(), 0);

            for (TileEntityWatz segment : segments) {
                segment.setupCoolant();
                for (int i = 0; i < 3; i++) {
                    this.sharedTanks[i].changeTankSize(this.sharedTanks[i].getMaxFill() + segment.tanks[i].getMaxFill());
                    this.sharedTanks[i].setFill(this.sharedTanks[i].getFill() + segment.tanks[i].getFill());
                }
            }

            //update coolant, bottom to top
            for (int i = segments.size() - 1; i >= 0; i--) {
                TileEntityWatz segment = segments.get(i);
                segment.updateCoolant(this.sharedTanks);
            }

            /* update reaction, top to bottom */
            this.updateReaction(null, this.sharedTanks, turnedOn);
            for (int i = 1; i < segments.size(); i++) {
                TileEntityWatz segment = segments.get(i);
                TileEntityWatz above = segments.get(i - 1);
                segment.updateReaction(above, this.sharedTanks, turnedOn);
            }

            /* send sync packets (order doesn't matter) */
            for (TileEntityWatz segment : segments) {
                for (int i = 0; i < 3; i++) {
                    segment.sharedTanksSync[i].changeTankSize(this.sharedTanks[i].getMaxFill());
                    segment.sharedTanksSync[i].setFill(this.sharedTanks[i].getFill());
                }
                segment.sharedTanks = this.sharedTanks;
                segment.isOn = turnedOn;
                segment.networkPackNT(25);
                segment.heat *= 0.99; //cool 1% per tick
            }

            /* re-distribute fluid from shared tanks back into actual tanks, bottom to top */
            for (int i = segments.size() - 1; i >= 0; i--) {
                TileEntityWatz segment = segments.get(i);
                for (int j = 0; j < 3; j++) {

                    int min = Math.min(segment.tanks[j].getMaxFill(), sharedTanks[j].getFill());
                    sharedTanks[j].setFill(sharedTanks[j].getFill() - min);
                    segment.tanks[j].setFill(min);
                }
            }

            segments.get(segments.size() - 1).sendOutBottom();

            /* explode on mud overflow */
            if (sharedTanks[2].getFill() > 0) {
                for (int x = -3; x <= 3; x++) {
                    for (int y = 3; y < 6; y++) {
                        for (int z = -3; z <= 3; z++) {
                            world.setBlockToAir(pos.add(x, y, z));
                        }
                    }
                }
                this.disassemble();

                ChunkRadiationManager.proxy.incrementRad(world, pos.add(0, 1, 0), 1_000F);
                world.playSound(null, pos.getX() + 0.5, pos.getY() + 2, pos.getZ() + 0.5, HBMSoundHandler.rbmk_explosion, SoundCategory.BLOCKS, 50.0F, 1.0F);
                NBTTagCompound data = new NBTTagCompound();
                data.setString("type", "rbmkmush");
                data.setFloat("scale", 5);
                PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, pos.getX() + 0.5, pos.getY() + 2, pos.getZ() + 0.5), new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 250));
                MainRegistry.proxy.effectNT(data);

            }

        }
	}

	/** basic sanity checking, usually wouldn't do anything except when NBT loading borks */
    private void setupCoolant() {
		tanks[0].setTankType(Fluids.COOLANT);
		tanks[1].setTankType(tanks[0].getTankType().getTrait(FT_Heatable.class).getFirstStep().typeProduced);
	}

	private void updateCoolant(FluidTankNTM[] tanks) {

		double coolingFactor = 0.2D; //20% per tick
		double heatToUse = this.heat * coolingFactor;

		FT_Heatable trait = tanks[0].getTankType().getTrait(FT_Heatable.class);
		HeatingStep step = trait.getFirstStep();

		int heatCycles = (int) (heatToUse / step.heatReq);
		int coolCycles = tanks[0].getFill() / step.amountReq;
		int hotCycles = (tanks[1].getMaxFill() - tanks[1].getFill()) / step.amountProduced;

		int cycles = Math.min(heatCycles, Math.min(hotCycles, coolCycles));
		this.heat -= cycles * step.heatReq;
		tanks[0].setFill(tanks[0].getFill() - cycles * step.amountReq);
		tanks[1].setFill(tanks[1].getFill() + cycles * step.amountProduced);
	}

	/** enforces strict top to bottom update order (instead of semi-random based on placement) */
    private void updateReaction(TileEntityWatz above, FluidTankNTM[] tanks, boolean turnedOn) {

		if(turnedOn) {
			List<ItemStack> pellets = new ArrayList<>();

			for(int i = 0; i < 24; i++) {
				ItemStack stack = inventory.getStackInSlot(i);
				if(!stack.isEmpty() && stack.getItem() == ModItems.watz_pellet) {
					pellets.add(stack);
				}
			}

			double baseFlux = 0D;

			/* init base flux */
			for(ItemStack stack : pellets) {
				EnumWatzType type = EnumUtil.grabEnumSafely(EnumWatzType.VALUES, stack.getItemDamage());
				baseFlux += type.passive;
			}

			double inputFlux = baseFlux + fluxLastReaction;
			double addedFlux = 0D;
			double addedHeat = 0D;

			for(ItemStack stack : pellets) {
				EnumWatzType type = EnumUtil.grabEnumSafely(EnumWatzType.VALUES, stack.getItemDamage());
				Function burnFunc = type.burnFunc;
				Function heatDiv = type.heatDiv;

				if(burnFunc != null) {
					double div = heatDiv != null ? heatDiv.effonix(heat) : 1D;
					double burn = burnFunc.effonix(inputFlux) / div;
					ItemWatzPellet.setYield(stack, ItemWatzPellet.getYield(stack) - burn);
					addedFlux += burn;
					addedHeat += type.heatEmission * burn;
					tanks[2].setFill(tanks[2].getFill() + (int) Math.round(type.mudContent * burn));
				}
			}

			for(ItemStack stack : pellets) {
				EnumWatzType type = EnumUtil.grabEnumSafely(EnumWatzType.VALUES, stack.getItemDamage());
				Function absorbFunc = type.absorbFunc;

				if(absorbFunc != null) {
					double absorb = absorbFunc.effonix(baseFlux + fluxLastReaction);
					addedHeat += absorb;
					ItemWatzPellet.setYield(stack, ItemWatzPellet.getYield(stack) - absorb);
					tanks[2].setFill(tanks[2].getFill() + (int) Math.round(type.mudContent * absorb));
				}
			}

			this.heat += addedHeat;
			this.fluxLastBase = baseFlux;
			this.fluxLastReaction = addedFlux;

		} else {
			this.fluxLastBase = 0;
			this.fluxLastReaction = 0;

		}

		for(int i = 0; i < 24; i++) {
			ItemStack stack = inventory.getStackInSlot(i);

			/* deplete */
			if(!stack.isEmpty() && stack.getItem() == ModItems.watz_pellet && ItemWatzPellet.getEnrichment(stack) <= 0) {
				inventory.setStackInSlot(i, new ItemStack(ModItems.watz_pellet_depleted, 1, stack.getItemDamage()));
				// depleted pellets may persist for one tick
			}
		}

		if(above != null) {
			for(int i = 0; i < 24; i++) {
				ItemStack stackBottom = inventory.getStackInSlot(i);
				ItemStack stackTop = above.inventory.getStackInSlot(i);

				/* items fall down if the bottom slot is empty */
				if(stackBottom.isEmpty() && !stackTop.isEmpty()) {
					inventory.setStackInSlot(i, stackTop.copy());
					above.inventory.getStackInSlot(i).shrink(stackTop.getCount());
				}

				/* items switch places if the top slot is depleted */
				if(!stackBottom.isEmpty() && stackBottom.getItem() == ModItems.watz_pellet && !stackTop.isEmpty() && stackTop.getItem() == ModItems.watz_pellet_depleted) {
					ItemStack buf = stackTop.copy();
					above.inventory.setStackInSlot(i, stackBottom.copy());
					inventory.setStackInSlot(i, buf);
				}
			}
		}
	}

	@Override
	public void serialize(ByteBuf buf) {
		super.serialize(buf);
		buf.writeInt(this.heat);
		buf.writeBoolean(isOn);
		buf.writeBoolean(isLocked);
		buf.writeDouble(this.fluxLastReaction + this.fluxLastBase);
		for (FluidTankNTM tank : sharedTanksSync) {
			tank.serialize(buf);
		}
	}

	@Override
	public void deserialize(ByteBuf buf) {
		super.deserialize(buf);
		this.heat = buf.readInt();
		this.isOn = buf.readBoolean();
		this.isLocked = buf.readBoolean();
		this.fluxDisplay = buf.readDouble();
		for (FluidTankNTM tank : tanks) {
			tank.deserialize(buf);
		}
	}
	
	/** Prevent manual updates when another segment is above this one */
    private boolean updateLock() {
		return Compat.getTileStandard(world, pos.getX(), pos.getY() + 3, pos.getZ()) instanceof TileEntityWatz;
	}
	
	private void subscribeToTop() {
		this.trySubscribe(tanks[0].getTankType(), world, pos.getX(), pos.getY() + 3, pos.getZ(), ForgeDirection.UP);
		this.trySubscribe(tanks[0].getTankType(), world, pos.getX() + 2, pos.getY() + 3, pos.getZ(), ForgeDirection.UP);
		this.trySubscribe(tanks[0].getTankType(), world, pos.getX() - 2, pos.getY() + 3, pos.getZ(), ForgeDirection.UP);
		this.trySubscribe(tanks[0].getTankType(), world, pos.getX(), pos.getY() + 3, pos.getZ() + 2, ForgeDirection.UP);
		this.trySubscribe(tanks[0].getTankType(), world, pos.getX(), pos.getY() + 3, pos.getZ() - 2, ForgeDirection.UP);
	}

	private void sendOutBottom() {

		for(DirPos pos : getSendingPos()) {
			if(tanks[1].getFill() > 0) this.sendFluid(tanks[1], world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
			if(tanks[2].getFill() > 0) this.sendFluid(tanks[2], world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
		}
	}

	private DirPos[] getSendingPos() {
		return new DirPos[] {
				new DirPos(pos.getX(), pos.getY() - 1, pos.getZ(), ForgeDirection.DOWN),
				new DirPos(pos.getX() + 2, pos.getY() - 1, pos.getZ(), ForgeDirection.DOWN),
				new DirPos(pos.getX() - 2, pos.getY() - 1, pos.getZ(), ForgeDirection.DOWN),
				new DirPos(pos.getX(), pos.getY() - 1, pos.getZ() + 2, ForgeDirection.DOWN),
				new DirPos(pos.getX(), pos.getY() - 1, pos.getZ() - 2, ForgeDirection.DOWN)
		};
	}


	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);

		NBTTagList list = nbt.getTagList("locks", 10);

		for(int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound nbt1 = list.getCompoundTagAt(i);
			byte b0 = nbt1.getByte("slot");
			if(b0 >= 0 && b0 < inventory.getSlots()) {
				locks[b0] = new ItemStack(nbt1);
			}
		}

		for(int i = 0; i < tanks.length; i++) tanks[i].readFromNBT(nbt, "t" + i);
		this.heat = nbt.getInteger("heat");
		this.fluxLastBase = nbt.getDouble("lastFluxB");
		this.fluxLastReaction = nbt.getDouble("lastFluxR");

		this.isLocked = nbt.getBoolean("isLocked");
	}

	@Override
	public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		NBTTagList list = new NBTTagList();

		for(int i = 0; i < locks.length; i++) {
			if(locks[i] != null) {
				NBTTagCompound nbt1 = new NBTTagCompound();
				nbt1.setByte("slot", (byte) i);
				locks[i].writeToNBT(nbt1);
				list.appendTag(nbt1);
			}
		}
		nbt.setTag("locks", list);

		for(int i = 0; i < tanks.length; i++) tanks[i].writeToNBT(nbt, "t" + i);
		nbt.setInteger("heat", this.heat);
		nbt.setDouble("lastFluxB", fluxLastBase);
		nbt.setDouble("lastFluxR", fluxLastReaction);

		nbt.setBoolean("isLocked", isLocked);
		return nbt;
	}

	@Override
	public boolean hasPermission(EntityPlayer player) {
		return this.isUseableByPlayer(player);
	}

	@Override
	public void receiveControl(NBTTagCompound data) {

		if(data.hasKey("lock")) {

			if(this.isLocked) {
				this.locks = new ItemStack[inventory.getSlots()];
			} else {
				for(int i = 0; i < inventory.getSlots(); i++) {
					this.locks[i] = inventory.getStackInSlot(i);
				}
			}

			this.isLocked = !this.isLocked;
			this.markDirty();
		}
	}

	@Override
	public boolean isItemValidForSlot(int i, ItemStack stack) {
		if(stack.getItem() != ModItems.watz_pellet) return false;
		if(!this.isLocked) return true;
		return this.locks[i] != null && this.locks[i].getItem() == stack.getItem() && locks[i].getItemDamage() == stack.getItemDamage();
	}

	@Override
	public int[] getAccessibleSlotsFromSide(EnumFacing side) {
		return new int[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23};
	}

	@Override
	public boolean canExtractItem(int i, ItemStack stack, int j) {
		return stack.getItem() != ModItems.watz_pellet;
	}

	private AxisAlignedBB bb = null;

	@Override
	public AxisAlignedBB getRenderBoundingBox() {

		if(bb == null) {
			bb = new AxisAlignedBB(
					pos.getX() - 3,
					pos.getY(),
					pos.getZ() - 3,
					pos.getX() + 4,
					pos.getY() + 3,
					pos.getZ() + 4);
		}

		return bb;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}

	private void disassemble() {

		int count = 20;
		Random rand = world.rand;
		for(int i = 0; i < count * 5; i++) {
			EntityShrapnel shrapnel = new EntityShrapnel(world);
			shrapnel.posX = pos.getX() + 0.5;
			shrapnel.posY = pos.getY() + 3;
			shrapnel.posZ = pos.getZ() + 0.5;
			shrapnel.motionY = ((rand.nextFloat() * 0.5) + 0.5) * (1 + (count / (15.0F + rand.nextInt(21)))) + (rand.nextFloat() / 50 * count);
			shrapnel.motionX = rand.nextGaussian() * 1	* (1 + (count / 100.0F));
			shrapnel.motionZ = rand.nextGaussian() * 1	* (1 + (count / 100.0F));
			shrapnel.setWatz(true);
			world.spawnEntity(shrapnel);
		}

		world.setBlockState(pos, ModBlocks.mud_block.getDefaultState());
		world.setBlockState(pos.up(), ModBlocks.mud_block.getDefaultState());
		world.setBlockState(pos.up(2), ModBlocks.mud_block.getDefaultState());

		setBrokenColumn(0, ModBlocks.watz_element, 0, 1, 0);
		setBrokenColumn(0, ModBlocks.watz_element, 0, 2, 0);
		setBrokenColumn(0, ModBlocks.watz_element, 0, 0, 1);
		setBrokenColumn(0, ModBlocks.watz_element, 0, 0, 2);
		setBrokenColumn(0, ModBlocks.watz_element, 0, -1, 0);
		setBrokenColumn(0, ModBlocks.watz_element, 0, -2, 0);
		setBrokenColumn(0, ModBlocks.watz_element, 0, 0, -1);
		setBrokenColumn(0, ModBlocks.watz_element, 0, 0, -2);
		setBrokenColumn(0, ModBlocks.watz_element, 0, 1, 1);
		setBrokenColumn(0, ModBlocks.watz_element, 0, 1, -1);
		setBrokenColumn(0, ModBlocks.watz_element, 0, -1, 1);
		setBrokenColumn(0, ModBlocks.watz_element, 0, -1, -1);
		setBrokenColumn(0, ModBlocks.watz_cooler, 0, 2, 1);
		setBrokenColumn(0, ModBlocks.watz_cooler, 0, 2, -1);
		setBrokenColumn(0, ModBlocks.watz_cooler, 0, 1, 2);
		setBrokenColumn(0, ModBlocks.watz_cooler, 0, -1, 2);
		setBrokenColumn(0, ModBlocks.watz_cooler, 0, -2, 1);
		setBrokenColumn(0, ModBlocks.watz_cooler, 0, -2, -1);
		setBrokenColumn(0, ModBlocks.watz_cooler, 0, 1, -2);
		setBrokenColumn(0, ModBlocks.watz_cooler, 0, -1, -2);

		for(int j = -1; j < 2; j++) {
			setBrokenColumn(1, ModBlocks.watz_casing, 1, 3, j);
			setBrokenColumn(1, ModBlocks.watz_casing, 1, j, 3);
			setBrokenColumn(1, ModBlocks.watz_casing, 1, -3, j);
			setBrokenColumn(1, ModBlocks.watz_casing, 1, j, -3);
		}
		setBrokenColumn(1, ModBlocks.watz_casing, 1, 2, 2);
		setBrokenColumn(1, ModBlocks.watz_casing, 1, 2, -2);
		setBrokenColumn(1, ModBlocks.watz_casing, 1, -2, 2);
		setBrokenColumn(1, ModBlocks.watz_casing, 1, -2, -2);

		List<EntityPlayerMP> players = world.getEntitiesWithinAABB(
				EntityPlayerMP.class, new AxisAlignedBB(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5).expand(50, 50, 50));

		for(EntityPlayerMP player : players) {
			AdvancementManager.grantAchievement(player, AdvancementManager.achWatzBoom);
		}
	}

	private void setBrokenColumn(int minHeight, Block b, int meta, int x, int z) {

		int height = minHeight + world.rand.nextInt(3 - minHeight);

		for(int i = 0; i < 3; i++) {

			if(i <= height) {
				world.setBlockState(pos.add(x, i, z), b.getDefaultState(), 3);
			} else {
				world.setBlockState(pos.add(x, i, z), ModBlocks.mud_block.getDefaultState());
			}
		}
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new ContainerWatz(player.inventory, this);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIWatz(player.inventory, this);
	}

	@Override
	public FluidTankNTM[] getAllTanks() {
		return tanks;
	}

	@Override
	public FluidTankNTM[] getSendingTanks() {
		return new FluidTankNTM[] { tanks[1], tanks[2] };
	}

	@Override
	public FluidTankNTM[] getReceivingTanks() {
		return new FluidTankNTM[] { tanks[0] };
	}

	@Override
	public FluidTankNTM getTankToPaste() {
		return null;
	}
}
