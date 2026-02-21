package com.hbm.tileentity.machine;

import com.hbm.api.fluidmk2.FluidNode;
import com.hbm.api.fluidmk2.IFluidStandardTransceiverMK2;
import com.hbm.blocks.ModBlocks;
import com.hbm.capability.NTMFluidHandlerWrapper;
import com.hbm.handler.CompatHandler;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IFFtoNTMF;
import com.hbm.inventory.container.ContainerBarrel;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.fluid.trait.FT_Corrosive;
import com.hbm.inventory.gui.GUIBarrel;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.IPersistentNBT;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.uninos.UniNodespace;
import io.netty.buffer.ByteBuf;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.HashSet;

@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")})
@AutoRegister
public class TileEntityBarrel extends TileEntityMachineBase implements
        ITickable, IPersistentNBT, IFluidCopiable,
        IFluidStandardTransceiverMK2, SimpleComponent,
        CompatHandler.OCComponent, IFFtoNTMF, IGUIProvider {

    public static final short modes = 4;
    private static final int[] slots_top = new int[]{2};
    private static final int[] slots_bottom = new int[]{3, 5};
    private static final int[] slots_side = new int[]{4};
    private static boolean converted = false;
    protected FluidNode node;
    protected FluidType lastType;
    public FluidTank tank;
    public FluidTankNTM tankNew;
    //Drillgon200: I think this would be much easier to read as an enum.
    //Norwood: This could have been a byte
    public short mode = 0;
    private int age = 0;
    // Th3_Sl1ze: Ugh. Maybe there's a smarter way to convert fluids from forge tank to NTM tank but I don't know any other client-seamless methods.
    private Fluid oldFluid =Fluids.NONE.getFF();;
    private boolean shouldDrop = true;

    public TileEntityBarrel() {
        super(6);
        tank = new FluidTank(-1);
        tankNew = new FluidTankNTM(Fluids.NONE, 0);
        converted = true;
    }

    public TileEntityBarrel(int cap) {
        super(6);
        tank = new FluidTank(cap);
        tankNew = new FluidTankNTM(Fluids.NONE, cap);
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            if (facing == EnumFacing.UP) {
                return CapabilityFluidHandler.
                        FLUID_HANDLER_CAPABILITY.
                        cast(new NTMFluidHandlerWrapper(this) {
                                 @Override
                                 public int fill(FluidStack resource, boolean doFill) {
                                     if (mode == 0 || mode == 1)
                                         return super.fill(resource, doFill);
                                     return 0;
                                 }

                                 @Override
                                 public FluidStack drain(FluidStack resource, boolean doDrain) {
                                     return null;
                                 }

                                 @Override
                                 public FluidStack drain(int maxDrain, boolean doDrain) {
                                     return null;
                                 }
                             }
                        );

            } else if (facing == EnumFacing.DOWN) {

                return CapabilityFluidHandler.
                        FLUID_HANDLER_CAPABILITY.
                        cast(new NTMFluidHandlerWrapper(this) {
                                 @Override
                                 public int fill(FluidStack resource, boolean doFill) {
                                     return 0;
                                 }

                                 @Override
                                 public FluidStack drain(FluidStack resource, boolean doDrain) {

                                     if (mode == 2 || mode == 1)
                                         return super.drain(resource, doDrain);
                                     return null;
                                 }

                                 @Override
                                 public FluidStack drain(int maxDrain, boolean doDrain) {
                                     if (mode == 2 || mode == 1)
                                         return super.drain(maxDrain, doDrain);
                                     return null;
                                 }
                             }
                        );

            } else {

                return CapabilityFluidHandler.
                        FLUID_HANDLER_CAPABILITY.
                        cast(new NTMFluidHandlerWrapper(this) {
                                 @Override
                                 public int fill(FluidStack resource, boolean doFill) {
                                     if (mode == 0 || mode == 1)
                                         return super.fill(resource, doFill);
                                     return 0;
                                 }

                                 @Override
                                 public FluidStack drain(FluidStack resource, boolean doDrain) {

                                     if (mode == 2 || mode == 1)
                                         return super.drain(resource, doDrain);
                                     return null;
                                 }

                                 @Override
                                 public FluidStack drain(int maxDrain, boolean doDrain) {
                                     if (mode == 2 || mode == 1)
                                         return super.drain(maxDrain, doDrain);
                                     return null;
                                 }
                             }
                        );

            }

        }


        return super.getCapability(capability, facing);
    }

    @Override
    public long getDemand(FluidType type, int pressure) {
        if (this.mode == 2 || this.mode == 3) return 0;

        if (tankNew.getPressure() != pressure) return 0;

        return type == tankNew.getTankType() ? tankNew.getMaxFill() - tankNew.getFill() : 0;
    }

    @Override
    public long transferFluid(FluidType type, int pressure, long fluid) {
        long toTransfer = Math.min(getDemand(type, pressure), fluid);
        tankNew.setFill(tankNew.getFill() + (int) toTransfer);
        this.markDirty();
        return fluid - toTransfer;
    }

    @Override
    public void update() {
        //Fix up old tanks
        if (!converted && tankNew.getTankType() == Fluids.NONE) {
            this.resizeInventory(6);
            convertAndSetFluid(oldFluid, tank, tankNew);
            converted = true;
        }
        //

        if (!world.isRemote) {
            tankNew.setType(0, 1, inventory);
            tankNew.loadTank(2, 3, inventory);
            tankNew.unloadTank(4, 5, inventory);

            // In buffer mode, acts like a pipe block, providing fluid to its own node
            // otherwise, it is a regular providing/receiving machine, blocking further propagation
            if(mode == 1) {
                if(this.node == null || this.node.expired || tankNew.getTankType() != lastType) {

                    this.node = (FluidNode) UniNodespace.getNode(world, pos, tankNew.getTankType().getNetworkProvider());

                    if(this.node == null || this.node.expired || tankNew.getTankType() != lastType) {
                        this.node = this.createNode(tankNew.getTankType());
                        UniNodespace.createNode(world, this.node);
                        lastType = tankNew.getTankType();
                    }
                }

                if(node != null && node.hasValidNet()) {
                    node.net.addProvider(this);
                    node.net.addReceiver(this);
                }
            } else {
                if(this.node != null) {
                    UniNodespace.destroyNode(world, pos, tankNew.getTankType().getNetworkProvider());
                    this.node = null;
                }

                for(DirPos pos : getConPos()) {
                    FluidNode dirNode = (FluidNode) UniNodespace.getNode(world, pos.getPos(), tankNew.getTankType().getNetworkProvider());

                    if(mode == 2) {
                        tryProvide(tankNew, world, pos.getPos(), pos.getDir());
                    } else {
                        if(dirNode != null && dirNode.hasValidNet()) dirNode.net.removeProvider(this);
                    }

                    if(mode == 0) {
                        if(dirNode != null && dirNode.hasValidNet()) dirNode.net.addReceiver(this);
                    } else {
                        if(dirNode != null && dirNode.hasValidNet()) dirNode.net.removeReceiver(this);
                    }
                }
            }

            if (tankNew.getFill() > 0) {
                checkFluidInteraction();
            }

            this.networkPackNT(50);
        }
    }

    protected FluidNode createNode(FluidType type) {
        DirPos[] conPos = getConPos();

        HashSet<BlockPos> posSet = new HashSet<>();
        posSet.add(pos);
        for(DirPos pos : conPos) {
            ForgeDirection dir = pos.getDir();
            posSet.add(new BlockPos(pos.getPos().getX() - dir.offsetX, pos.getPos().getY() - dir.offsetY, pos.getPos().getZ() - dir.offsetZ));
        }

        return new FluidNode(type.getNetworkProvider(), posSet.toArray(new BlockPos[posSet.size()])).setConnections(conPos);
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeShort(mode);
        tankNew.serialize(buf);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        mode = buf.readShort();
        tankNew.deserialize(buf);
    }

    protected DirPos[] getConPos() {
        return new DirPos[]{
                new DirPos(pos.getX() + 1, pos.getY(), pos.getZ(), Library.POS_X),
                new DirPos(pos.getX() - 1, pos.getY(), pos.getZ(), Library.NEG_X),
                new DirPos(pos.getX(), pos.getY() + 1, pos.getZ(), Library.POS_Y),
                new DirPos(pos.getX(), pos.getY() - 1, pos.getZ(), Library.NEG_Y),
                new DirPos(pos.getX(), pos.getY(), pos.getZ() + 1, Library.POS_Z),
                new DirPos(pos.getX(), pos.getY(), pos.getZ() - 1, Library.NEG_Z)
        };
    }

    public void checkFluidInteraction() {
        Block b = this.getBlockType();

        //for when you fill antimatter into a matter tank
        if (b != ModBlocks.barrel_antimatter && tankNew.getTankType().isAntimatter()) {
            shouldDrop = false;
            world.destroyBlock(pos, false);
            world.newExplosion(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 5, true, true);
        }

        //for when you fill hot or corrosive liquids into a plastic tank
        if (b == ModBlocks.barrel_plastic && (tankNew.getTankType().isCorrosive() || tankNew.getTankType().isHot())) {
            shouldDrop = false;
            world.destroyBlock(pos, false);
            world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 1.0F, 1.0F);
        }

        //for when you fill corrosive liquid into an iron tank
        if ((b == ModBlocks.barrel_iron && tankNew.getTankType().isCorrosive()) ||
                (b == ModBlocks.barrel_steel && tankNew.getTankType().hasTrait(FT_Corrosive.class) && tankNew.getTankType().getTrait(FT_Corrosive.class).getRating() > 50)) {

            world.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.BLOCK_LAVA_EXTINGUISH, SoundCategory.BLOCKS, 1.0F, 1.0F);
            ItemStackHandler copy = new ItemStackHandler(this.inventory.getSlots());
            for (int i = 0; i < this.inventory.getSlots(); i++) {
                copy.setStackInSlot(i, this.inventory.getStackInSlot(i).copy());
            }

            this.inventory = new ItemStackHandler(6);
            shouldDrop = false;
            world.setBlockState(pos, ModBlocks.barrel_corroded.getDefaultState());

            TileEntityBarrel barrel = (TileEntityBarrel) world.getTileEntity(pos);

            if (barrel != null) {
                barrel.tankNew.setTankType(tankNew.getTankType());
                barrel.tankNew.setFill(Math.min(barrel.tankNew.getMaxFill(), tankNew.getFill()));
                barrel.inventory = copy;
            }
        }

        if (b == ModBlocks.barrel_corroded) {
            if (world.rand.nextInt(3) == 0) {
                tankNew.setFill(tankNew.getFill() - 1);
            }
            if (world.rand.nextInt(3 * 60 * 20) == 0) {
                shouldDrop = false;
                world.destroyBlock(pos, false);
            }
        }
    }

    @Override
    public boolean shouldDrop() {
        return IPersistentNBT.super.shouldDrop() && shouldDrop;
    }

    @Override
    public String getDefaultName() {
        return "container.barrel";
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound compound) {
        super.writeToNBT(compound);
        compound.setShort("mode", mode);
        if (!converted && tankNew.getTankType() == Fluids.NONE) {
            compound.setInteger("cap", tank.getCapacity());
            tank.writeToNBT(compound);
            compound.setBoolean("converted", true);
        } else tankNew.writeToNBT(compound, "tank");
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        mode = compound.getShort("mode");
        converted = compound.getBoolean("converted");
        tankNew.readFromNBT(compound, "tank");
        if (!converted && tankNew.getTankType() == Fluids.NONE) {
            if (tank == null || tank.getCapacity() <= 0)
                tank = new FluidTank(compound.getInteger("cap"));
            tank.readFromNBT(compound);
            if (tank.getFluid() != null) {
                oldFluid = tank.getFluid().getFluid();
            }
        } else {
            if (compound.hasKey("cap")) compound.removeTag("cap");
        }
    }

    @Override
    public FluidTankNTM[] getSendingTanks() {
        return (mode == 1 || mode == 2) ? new FluidTankNTM[]{tankNew} : new FluidTankNTM[0];
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return (mode == 0 || mode == 1) ? new FluidTankNTM[]{tankNew} : new FluidTankNTM[0];
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return new FluidTankNTM[]{tankNew};
    }

    @Override
    public int[] getFluidIDToCopy() {
        return new int[]{tankNew.getTankType().getID()};
    }

    @Override
    public FluidTankNTM getTankToPaste() {
        return tankNew;
    }

    @Override
    public void writeNBT(NBTTagCompound nbt) {
        if (tankNew.getFill() == 0) return;
        NBTTagCompound data = new NBTTagCompound();
        this.tankNew.writeToNBT(data, "tank");
        data.setShort("mode", mode);
        nbt.setTag(NBT_PERSISTENT_KEY, data);
    }

    @Override
    public void readNBT(NBTTagCompound nbt) {
        NBTTagCompound data = nbt.getCompoundTag(NBT_PERSISTENT_KEY);
        this.tankNew.readFromNBT(data, "tank");
        this.mode = data.getShort("nbt");
    }

    @Override
    public int[] getAccessibleSlotsFromSide(EnumFacing e) {
        int i = e.ordinal();
        return i == 0 ? slots_bottom : (i == 1 ? slots_top : slots_side);
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack stack) {
        Item item = stack.getItem();
        return switch (i) {
            case 0, 1 -> item instanceof IItemFluidIdentifier;
            case 2 -> Library.isStackDrainableForTank(stack, tankNew);
            case 4 -> Library.isStackFillableForTank(stack, tankNew);
            default -> true;
        };
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack stack) {
        return switch (slot) {
            case 1, 3, 5 -> false;
            default -> isItemValidForSlot(slot, stack);
        };
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, int amount) {
        return switch (slot) {
            case 1, 3, 5 -> true;
            default -> !isItemValidForSlot(slot, stack);
        };
    }

    @Override
    @Optional.Method(modid = "opencomputers")
    public String getComponentName() {
        return "ntm_fluid_tank";
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getFluidStored(Context context, Arguments args) {
        return new Object[]{tankNew.getFill()};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getMaxStored(Context context, Arguments args) {
        return new Object[]{tankNew.getMaxFill()};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getTypeStored(Context context, Arguments args) {
        return new Object[]{tankNew.getTankType().getName()};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getInfo(Context context, Arguments args) {
        return new Object[]{tankNew.getFill(), tankNew.getMaxFill(), tankNew.getTankType().getName()};
    }

    @Override
    @Optional.Method(modid = "opencomputers")
    public String[] methods() {
        return new String[]{
                "getFluidStored",
                "getMaxStored",
                "getTypeStored",
                "getInfo"
        };
    }

    @Override
    @Optional.Method(modid = "opencomputers")
    public Object[] invoke(String method, Context context, Arguments args) throws Exception {
        return switch (method) {
            case "getFluidStored" -> getFluidStored(context, args);
            case "getMaxStored" -> getMaxStored(context, args);
            case "getTypeStored" -> getTypeStored(context, args);
            case "getInfo" -> getInfo(context, args);
            default -> throw new NoSuchMethodException();
        };
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerBarrel(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIBarrel(player.inventory, this);
    }

}
