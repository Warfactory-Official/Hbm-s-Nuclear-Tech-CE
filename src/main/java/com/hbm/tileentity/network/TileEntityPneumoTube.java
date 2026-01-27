package com.hbm.tileentity.network;

import com.hbm.api.fluidmk2.IFluidStandardReceiverMK2;
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
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

@AutoRegister
public class TileEntityPneumoTube extends TileEntityMachineBase implements IGUIProvider, IFluidStandardReceiverMK2, IControlReceiverFilter, ITickable {

    public ModulePatternMatcher pattern = new ModulePatternMatcher(15);
    public ForgeDirection insertionDir = ForgeDirection.UNKNOWN;
    public ForgeDirection ejectionDir = ForgeDirection.UNKNOWN;

    public boolean whitelist = false;
    public boolean redstone = false;
    public byte sendOrder = 0;
    public byte receiveOrder = 0;

    public int soundDelay = 0;
    public int sendCounter = 0;

    public FluidTankNTM compair;
    protected PneumaticNode node;

    public TileEntityPneumoTube() {
        super(15);
        this.compair = new FluidTankNTM(Fluids.AIR, 4_000).withPressure(1);
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

        if (this.isCompressor() && (!this.world.isBlockPowered(pos) ^ this.redstone)) {

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

        this.networkPackNT(15);
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

    public void nextMode(int index) {
        this.pattern.nextMode(world, inventory.getStackInSlot(index), index);
    }

    public void initPattern(ItemStack stack, int index) {
        this.pattern.initPatternSmart(world, stack, index);
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setByte("insertionDir", (byte) insertionDir.ordinal());
        nbt.setByte("ejectionDir", (byte) ejectionDir.ordinal());
        return new SPacketUpdateTileEntity(this.pos, 0, nbt);
    }

    @Override
    public void onDataPacket(@NotNull NetworkManager net, SPacketUpdateTileEntity pkt) {
        NBTTagCompound nbt = pkt.getNbtCompound();
        this.insertionDir = EnumUtil.grabEnumSafely(ForgeDirection.VALUES, nbt.getByte("insertionDir"));
        this.ejectionDir = EnumUtil.grabEnumSafely(ForgeDirection.VALUES, nbt.getByte("ejectionDir"));
        world.markBlockRangeForRenderUpdate(pos, pos);
    }

    @Override
    public @NotNull NBTTagCompound getUpdateTag() {
        NBTTagCompound nbt = super.getUpdateTag();
        nbt.setByte("insertionDir", (byte) insertionDir.ordinal());
        nbt.setByte("ejectionDir", (byte) ejectionDir.ordinal());
        return nbt;
    }

    @Override
    public void handleUpdateTag(@NotNull NBTTagCompound nbt) {
        super.handleUpdateTag(nbt);
        this.insertionDir = EnumUtil.grabEnumSafely(ForgeDirection.VALUES, nbt.getByte("insertionDir"));
        this.ejectionDir = EnumUtil.grabEnumSafely(ForgeDirection.VALUES, nbt.getByte("ejectionDir"));
        world.markBlockRangeForRenderUpdate(pos, pos);
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

        if (world != null && world.isRemote) {
            world.markBlockRangeForRenderUpdate(pos, pos);
        }
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
