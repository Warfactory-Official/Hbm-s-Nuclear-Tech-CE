package com.hbm.tileentity.network;

import com.hbm.api.fluidmk2.IFluidStandardReceiverMK2;
import com.hbm.api.ntl.IPneumaticConnector;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerPneumoTube;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUIPneumoTube;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.modules.ModulePatternMatcher;
import com.hbm.tileentity.IControlReceiverFilter;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.uninos.GenNode;
import com.hbm.uninos.UniNodespace;
import com.hbm.uninos.networkproviders.PneumaticNetwork;
import com.hbm.util.Compat;
import com.hbm.util.EnumUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

@AutoRegister
public class TileEntityPneumoTube extends TileEntityMachineBase implements IGUIProvider, IFluidStandardReceiverMK2, IControlReceiverFilter, ITickable {

    public ModulePatternMatcher pattern = new ModulePatternMatcher(15);
    public ForgeDirection insertionDir = ForgeDirection.UNKNOWN;
    public ForgeDirection ejectionDir = ForgeDirection.UNKNOWN;

    public boolean isIndirectlyPowered;

    public boolean whitelist = false;
    public boolean redstone = false;
    public byte sendOrder = 0;
    public byte receiveOrder = 0;

    public int soundDelay = 0;
    public int sendCounter = 0;

    public FluidTankNTM compair;
    protected PneumaticNode node;

    private byte cachedConnMask;
    private byte cachedConnectorMask;
    private boolean cachedMasksValid;
    private boolean computingClientMasks;

    public TileEntityPneumoTube() {
        super(15);
        this.compair = new FluidTankNTM(Fluids.AIR, 4_000).withOwner(this).withPressure(1);
    }

    public byte getCachedConnMask(IBlockAccess access) {
        if (access instanceof World && ((World) access).isRemote) {
            recomputeClientMasks(access);
            return this.cachedConnMask;
        }
        ensureMasks(access);
        return this.cachedConnMask;
    }

    public byte getCachedConnectorMask(IBlockAccess access) {
        if (access instanceof World && ((World) access).isRemote) {
            recomputeClientMasks(access);
            return this.cachedConnectorMask;
        }
        ensureMasks(access);
        return this.cachedConnectorMask;
    }

    public void invalidateConnectionCache() {
        this.cachedMasksValid = false;
        if (world != null && world.isRemote) {
            world.markBlockRangeForRenderUpdate(pos, pos);
        }
    }

    private void ensureMasks(IBlockAccess access) {
        if (!this.cachedMasksValid) {
            recomputeMasks(access);
            this.cachedMasksValid = true;
        }
    }

    private void recomputeClientMasks(IBlockAccess access) {
        if (computingClientMasks) return;
        computingClientMasks = true;
        try {
            recomputeMasks(access);
        } finally {
            computingClientMasks = false;
        }
    }

    private void recomputeMasks(IBlockAccess access) {
        byte conn = 0;
        byte connector = 0;
        boolean compressor = this.isCompressor();
        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            int bit = 1 << dir.ordinal();
            int ax = pos.getX() + dir.offsetX;
            int ay = pos.getY() + dir.offsetY;
            int az = pos.getZ() + dir.offsetZ;
            BlockPos adj = new BlockPos(ax, ay, az);
            if (access instanceof World w && !w.isBlockLoaded(adj)) {
                continue;
            }
            TileEntity tile = access.getTileEntity(adj);
            if (tile instanceof TileEntityPneumoTube) {
                conn |= (byte) bit;
            } else if (tile instanceof IPneumaticConnector pc && pc.canConnectPneumatic(dir.getOpposite())) {
                conn |= (byte) bit;
            } else if (compressor && this.insertionDir != dir && this.ejectionDir != dir
                    && Library.canConnectFluid(access, ax, ay, az, dir, Fluids.AIR)) {
                connector |= (byte) bit;
            }
        }
        this.cachedConnMask = conn;
        this.cachedConnectorMask = connector;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (world.isRemote) {
            invalidateConnectionCache();
            for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                BlockPos neighborPos = pos.add(dir.offsetX, dir.offsetY, dir.offsetZ);
                if (!world.isBlockLoaded(neighborPos)) continue;
                TileEntity te = world.getTileEntity(neighborPos);
                if (te instanceof TileEntityPneumoTube tube) {
                    tube.invalidateConnectionCache();
                }
            }
        } else {
            isIndirectlyPowered = world.isBlockPowered(pos);
        }
    }

    public static int getRangeFromPressure(int pressure) {
        return switch (pressure) {
            case 1 -> 10;
            case 2 -> 25;
            case 3 -> 100;
            case 4 -> 250;
            case 5 -> 1_000;
            default -> 0; // case 0
        };
    }

    // tactfully copy-pasted from (1.7 ntm impl of) BlockPos
    public static int getIdentifier(BlockPos pos) {
        return (pos.getY() + pos.getZ() * 27644437) * 27644437 + pos.getX();
    }

    @Override
    public String getDefaultName() {
        return "container.pneumoTube";
    }

    public boolean matchesFilter(ItemStack stack) {
        for (int i = 0; i < 15; i++) {
            ItemStack filter = inventory.getStackInSlot(i);
            if (!filter.isEmpty() && this.pattern.isValidForFilter(filter, i, stack)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void update() {
        if (world.isRemote) return;

        if (this.soundDelay > 0) this.soundDelay--;

        if (this.node == null || this.node.expired) {
            this.node = UniNodespace.getNode(world, pos, PneumaticNetwork.THE_PNEUMATIC_PROVIDER);

            if (this.node == null || this.node.expired) {
                this.node = new PneumaticNode(new BlockPos(pos.getX(), pos.getY(), pos.getZ())).setConnections(
                        new DirPos(pos.getX() + 1, pos.getY(), pos.getZ(), Library.POS_X),
                        new DirPos(pos.getX() - 1, pos.getY(), pos.getZ(), Library.NEG_X),
                        new DirPos(pos.getX(), pos.getY() + 1, pos.getZ(), Library.POS_Y),
                        new DirPos(pos.getX(), pos.getY() - 1, pos.getZ(), Library.NEG_Y),
                        new DirPos(pos.getX(), pos.getY(), pos.getZ() + 1, Library.POS_Z),
                        new DirPos(pos.getX(), pos.getY(), pos.getZ() - 1, Library.NEG_Z)
                );
                UniNodespace.createNode(world, this.node);
            }
        }

        if (this.isCompressor() && (!isIndirectlyPowered ^ this.redstone)) {

            int randTime = Math.abs((int) (world.getTotalWorldTime() + getIdentifier(pos)));

            if (world.getTotalWorldTime() % 10 == 0) {
                for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                    if (dir != this.insertionDir && dir != this.ejectionDir) {
                        this.trySubscribe(compair.getTankType(),
                                world,
                                pos.getX() + dir.offsetX,
                                pos.getY() + dir.offsetY,
                                pos.getZ() + dir.offsetZ,
                                dir);
                    }
                }
            }

            if (randTime % 5 == 0
                    && this.node != null && !this.node.expired && this.node.net != null
                    && this.compair.getFill() >= 50) {
                TileEntity sendFrom = Compat.getTileStandard(world,
                        pos.getX() + insertionDir.offsetX,
                        pos.getY() + insertionDir.offsetY,
                        pos.getZ() + insertionDir.offsetZ);

                if (sendFrom != null) {
                    PneumaticNetwork net = node.net;
                    boolean moved = net.send(
                            sendFrom,
                            this,
                            this.insertionDir.getOpposite(),
                            sendOrder,
                            receiveOrder,
                            getRangeFromPressure(compair.getPressure()),
                            this.sendCounter
                    );

                    if (moved) {
                        this.compair.setFill(this.compair.getFill() - 50);
                        this.dataChanged();

                        if (this.soundDelay <= 0 && !this.muffled) {
                            world.playSound(null,
                                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                                    HBMSoundHandler.tubeFwoomp,
                                    SoundCategory.BLOCKS,
                                    0.25F,
                                    0.9F + world.rand.nextFloat() * 0.2F);
                            this.soundDelay = 20;
                        }
                    }
                    this.sendCounter++;
                    this.markDirty();
                }
            }
        }

        if (this.isEndpoint() && this.node != null && this.node.net != null && world.getTotalWorldTime() % 10 == 0) {
            TileEntity tile = Compat.getTileStandard(world,
                    pos.getX() + this.ejectionDir.offsetX,
                    pos.getY() + this.ejectionDir.offsetY,
                    pos.getZ() + this.ejectionDir.offsetZ);

            if (tile != null && PneumaticNetwork.hasItemHandler(tile, this.ejectionDir.getOpposite())) {
                this.node.net.addReceiver(new PneumaticNetwork.ReceiverTarget(tile.getPos(), this.ejectionDir, this));
            }
        }

        this.networkPackMK2(15);
    }

    @Override
    public long getReceiverSpeed(FluidType type, int pressure) {
        return MathHelper.clamp((this.compair.getMaxFill() - this.compair.getFill()) / 25, 1, 100);
    }

    @Override
    public boolean canConnect(FluidType type, ForgeDirection dir) {
        return dir != this.insertionDir && dir != this.ejectionDir && type == Fluids.AIR && this.isCompressor();
    }

    @Override
    public void invalidate() {
        super.invalidate();

        if (!world.isRemote) {
            if (this.node != null) {
                UniNodespace.destroyNode(world, pos, PneumaticNetwork.THE_PNEUMATIC_PROVIDER);
            }
        }
    }

    public boolean isCompressor() {
        return this.insertionDir != ForgeDirection.UNKNOWN;
    }

    public boolean isEndpoint() {
        return this.ejectionDir != ForgeDirection.UNKNOWN;
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeBoolean(redstone);
        buf.writeBoolean(whitelist);
        buf.writeByte(sendOrder);
        buf.writeByte(receiveOrder);
        pattern.serialize(buf);
        compair.serialize(buf);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        this.redstone = buf.readBoolean();
        this.whitelist = buf.readBoolean();
        this.sendOrder = buf.readByte();
        this.receiveOrder = buf.readByte();
        pattern.deserialize(buf);
        compair.deserialize(buf);
    }

    @Override
    public void serializeInitial(ByteBuf buf) {
        super.serializeInitial(buf);
        buf.writeByte(insertionDir.ordinal());
        buf.writeByte(ejectionDir.ordinal());
    }

    @Override
    public void deserializeInitial(ByteBuf buf) {
        super.deserializeInitial(buf);
        this.insertionDir = EnumUtil.grabEnumSafely(ForgeDirection.VALUES, buf.readByte());
        this.ejectionDir = EnumUtil.grabEnumSafely(ForgeDirection.VALUES, buf.readByte());
    }

    public void nextMode(int index) {
        this.pattern.nextMode(world, inventory.getStackInSlot(index), index);
        this.dataChanged();
        this.markDirty();
    }

    public void initPattern(ItemStack stack, int index) {
        this.pattern.initPatternSmart(world, stack, index);
        this.dataChanged();
        this.markDirty();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.insertionDir = EnumUtil.grabEnumSafely(ForgeDirection.VALUES, nbt.getByte("insertionDir"));
        this.ejectionDir = EnumUtil.grabEnumSafely(ForgeDirection.VALUES, nbt.getByte("ejectionDir"));
        this.compair.readFromNBT(nbt, "tank");
        this.pattern.readFromNBT(nbt);

        this.sendOrder = nbt.getByte("sendOrder");
        this.receiveOrder = nbt.getByte("receiveOrder");
        this.sendCounter = nbt.getInteger("sendCounter");

        this.whitelist = nbt.getBoolean("whitelist");
        this.redstone = nbt.getBoolean("redstone");
        invalidateConnectionCache();
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setByte("insertionDir", (byte) insertionDir.ordinal());
        nbt.setByte("ejectionDir", (byte) ejectionDir.ordinal());
        this.compair.writeToNBT(nbt, "tank");
        this.pattern.writeToNBT(nbt);

        nbt.setByte("sendOrder", sendOrder);
        nbt.setByte("receiveOrder", receiveOrder);
        nbt.setInteger("sendCounter", sendCounter);

        nbt.setBoolean("whitelist", whitelist);
        nbt.setBoolean("redstone", redstone);
        return nbt;
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerPneumoTube(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIPneumoTube(player.inventory, this, ID == 1);
    }

    @Override
    public void receiveControl(NBTTagCompound data) {
        if (data.hasKey("whitelist")) {
            this.whitelist = !this.whitelist;
        }
        if (data.hasKey("redstone")) {
            this.redstone = !this.redstone;
        }
        if (data.hasKey("pressure")) {
            int pressure = this.compair.getPressure() + 1;
            if (pressure > 5) pressure = 1;
            this.compair.withPressure(pressure);
        }
        if (data.hasKey("send")) {
            this.sendOrder++;
            if (this.sendOrder > 2) this.sendOrder = 0;
        }
        if (data.hasKey("receive")) {
            this.receiveOrder++;
            if (this.receiveOrder > 1) this.receiveOrder = 0;
        }
        if (data.hasKey("slot")) {
            setFilterContents(data);
        }

        this.dataChanged();
        this.markDirty();
    }

    @Override
    public boolean hasPermission(EntityPlayer player) {
        return this.isUseableByPlayer(player);
    }

    @Override
    public int[] getFilterSlots() {
        return new int[]{0, 15};
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return new FluidTankNTM[]{compair};
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return new FluidTankNTM[]{compair};
    }

    @Override
    public long transferFluid(FluidType type, int pressure, long amount) {
        long remaining = IFluidStandardReceiverMK2.super.transferFluid(type, pressure, amount);
        if(remaining != amount) dataChanged();
        return remaining;
    }

    @Override
    public NBTTagCompound getSettings(World world, int x, int y, int z) {
        var nbt = IControlReceiverFilter.super.getSettings(world, x, y, z);
        nbt.setByte("sendOrder", sendOrder);
        nbt.setByte("receiveOrder", receiveOrder);
        nbt.setInteger("sendCounter", sendCounter);

        nbt.setBoolean("whitelist", whitelist);
        nbt.setBoolean("redstone", redstone);


        return nbt;

    }

    @Override
    public void pasteSettings(NBTTagCompound nbt, int index, World world, EntityPlayer player, int x, int y, int z) {
        IControlReceiverFilter.super.pasteSettings(nbt, index, world, player, x, y, z);
        this.sendOrder = nbt.getByte("sendOrder");
        this.receiveOrder = nbt.getByte("receiveOrder");
        this.sendCounter = nbt.getInteger("sendCounter");

        this.whitelist = nbt.getBoolean("whitelist");
        this.redstone = nbt.getBoolean("redstone");

        this.dataChanged();
    }

    public static class PneumaticNode extends GenNode<PneumaticNetwork> {
        public PneumaticNode(BlockPos... positions) {
            super(PneumaticNetwork.THE_PNEUMATIC_PROVIDER, positions);
        }

        @Override
        public PneumaticNode setConnections(DirPos... connections) {
            return (PneumaticNode) super.setConnections(connections);
        }

        @Override
        public PneumaticNode addConnection(DirPos connection) {
            return (PneumaticNode) super.addConnection(connection);
        }
    }
}
