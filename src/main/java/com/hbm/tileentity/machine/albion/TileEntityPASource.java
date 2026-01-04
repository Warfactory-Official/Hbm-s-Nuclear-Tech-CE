package com.hbm.tileentity.machine.albion;

import com.hbm.blocks.BlockDummyable;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.container.ContainerPASource;
import com.hbm.inventory.gui.GUIPASource;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.util.EnumUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

@AutoRegister
public class TileEntityPASource extends TileEntityCooledBase implements IGUIProvider, IControlReceiver {

    public static final long usage = 100_000;
    public Particle particle;
    public PAState state = PAState.IDLE;

    public int lastSpeed;

    public int debugSpeed;

    public enum PAState {
        IDLE(0x8080ff),                    //no particle active
        RUNNING(0xffff00),                //running without further issue
        SUCCESS(0x00ff00),                //completed recipe
        PAUSE_UNLOADED(0x808080),        //particle suspended because it entered unloaded chunks
        CRASH_DEFOCUS(0xff0000),        //crash from excessive defocus
        CRASH_DERAIL(0xff0000),            //crash due to leaving the beamline
        CRASH_CANNOT_ENTER(0xff0000),    //crash due to hitting PA component from invalid side
        CRASH_NOCOOL(0xff0000),            //crash due to lack of cooling
        CRASH_NOPOWER(0xff0000),        //crash due to power outage
        CRASH_NOCOIL(0xff0000),            //crash due to no coil installed (QP, DP)
        CRASH_OVERSPEED(0xff0000),        //crash due to coil max speed exceeded (QP, DP)
        CRASH_UNDERSPEED(0xff0000),    //crash due to recipe momentum requirements not being met
        CRASH_NORECIPE(0xff0000);        //crash due to failing to match recipe

        public final int color;

        PAState(int color) {
            this.color = color;
        }
    }

    public void updateState(PAState state) {
        this.state = state;
    }

    public TileEntityPASource() {
        super(5, 1);
    }

    @Override
    public String getDefaultName() {
        return "container.paSource";
    }

    @Override
    public void update() {

        if (!world.isRemote) {
            this.power = Library.chargeTEFromItems(inventory, 0, power, this.getMaxPower());

            for (int i = 0; i < 10; i++) {
                if (particle != null) {
                    this.state = PAState.RUNNING;
                    steppy();
                    this.debugSpeed = particle.momentum;
                    if (particle.invalid) this.particle = null;
                } else if (this.power >= usage && !inventory.getStackInSlot(1).isEmpty() && !inventory.getStackInSlot(2).isEmpty()) {
                    tryRun();
                    break;
                }
            }
        }

        super.update();
    }

    public void steppy() {
        if (!((WorldServer)world).getChunkProvider().chunkExists(particle.pos.getX() >> 4, particle.pos.getZ() >> 4)) {
            this.state = PAState.PAUSE_UNLOADED;
            return;
        } //halt if we reach unloaded areas
        //ExplosionSmallCreator.composeEffect(world, particle.x + 0.5, particle.y + 0.5, particle.z + 0.5, 10, 1, 1);

        Block b = world.getBlockState(particle.pos).getBlock();
        if (b instanceof BlockDummyable) {
            BlockPos pos = ((BlockDummyable) b).findCore(world, particle.pos);
            if (pos == null) {
                particle.crash(PAState.CRASH_DERAIL);
                return;
            }
            TileEntity tile = world.getTileEntity(pos);
            if (!(tile instanceof IParticleUser pa)) {
                particle.crash(PAState.CRASH_DERAIL);
                return;
            }
            if (pa.canParticleEnter(particle, particle.dir, particle.pos)) {
                pa.onEnter(particle, particle.dir);
                BlockPos exit = pa.getExitPos(particle);
                if (exit != null) particle.move(exit);
            } else {
                particle.crash(PAState.CRASH_CANNOT_ENTER);
            }
        } else {
            particle.crash(PAState.CRASH_DERAIL);
        }
    }

    public void tryRun() {
        ItemStack slot1 = inventory.getStackInSlot(1);
        ItemStack slot2 = inventory.getStackInSlot(2);
        ItemStack slot3 = inventory.getStackInSlot(3);
        ItemStack slot4 = inventory.getStackInSlot(4);

        if (slot1.getItem().hasContainerItem(slot1) && !slot3.isEmpty()) return;
        if (slot2.getItem().hasContainerItem(slot2) && !slot4.isEmpty()) return;

        if (slot1.getItem().hasContainerItem(slot1))
            inventory.setStackInSlot(3, slot1.getItem().getContainerItem(slot1).copy());
        if (slot2.getItem().hasContainerItem(slot2))
            inventory.setStackInSlot(4, slot2.getItem().getContainerItem(slot2).copy());

        this.power -= usage;
        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
        ForgeDirection rot = dir.getRotation(ForgeDirection.DOWN);
        this.particle = new Particle(this, getPos().add(rot.offsetX * 5, 0, rot.offsetZ * 5), rot, slot1, slot2);
        inventory.setStackInSlot(1, ItemStack.EMPTY);
        inventory.setStackInSlot(2, ItemStack.EMPTY);
        this.markDirty();
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeInt(debugSpeed);
        buf.writeByte((byte) this.state.ordinal());
        buf.writeInt(this.lastSpeed);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        debugSpeed = buf.readInt();
        state = EnumUtil.grabEnumSafely(PAState.class, buf.readByte());
        this.lastSpeed = buf.readInt();
    }

    @Override
    public DirPos[] getConPos() {
        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
        return new DirPos[]{
                new DirPos(getPos().add(dir.offsetX * 2, 0, dir.offsetZ * 2), dir),
                new DirPos(getPos().add(dir.offsetX * 2 + rot.offsetX * 2, 0, dir.offsetZ * 2 + rot.offsetZ * 2), dir),
                new DirPos(getPos().add(dir.offsetX * 2 - rot.offsetX * 2, 0, dir.offsetZ * 2 - rot.offsetZ * 2), dir),
                new DirPos(getPos().add(-dir.offsetX * 2, 0, -dir.offsetZ * 2), dir.getOpposite()),
                new DirPos(getPos().add(-dir.offsetX * 2 + rot.offsetX * 2, 0, -dir.offsetZ * 2 + rot.offsetZ * 2), dir.getOpposite()),
                new DirPos(getPos().add(-dir.offsetX * 2 - rot.offsetX * 2, 0, -dir.offsetZ * 2 - rot.offsetZ * 2), dir.getOpposite()),
                new DirPos(getPos().add(rot.offsetX * 5, 0, rot.offsetZ * 5), rot),
        };
    }

    //ISidedInventory
    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return slot == 1 || slot == 2;
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, int side) {
        return slot == 3 || slot == 4;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(EnumFacing side) { return new int[] { 3, 4 }; }

    //reusing the same fucking instance because doing anything else would be retarded
    public static final BlockPos.MutableBlockPos cheapAss = new BlockPos.MutableBlockPos();
    public static final int[] slotsRed = new int[]{1, 3, 4};
    public static final int[] slotsYellow = new int[]{2, 3, 4};

    @Override
    public int[] getAccessibleSlotsFromSide(EnumFacing side, BlockPos pos) {
        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
        cheapAss.setPos(pos);

        if (cheapAss.getX() == getPos().getX() + dir.offsetX - rot.offsetX * 2 && cheapAss.getY() == getPos().getY() && cheapAss.getZ() == getPos().getZ() + dir.offsetZ - rot.offsetZ * 2 ||
                cheapAss.getX() == getPos().getX() - dir.offsetX + rot.offsetX * 2 && cheapAss.getY() == getPos().getY() && cheapAss.getZ() == getPos().getZ() - dir.offsetZ + rot.offsetZ * 2) {
            return slotsYellow;
        }

        if (cheapAss.getX() == getPos().getX() - dir.offsetX - rot.offsetX * 2 && cheapAss.getY() == getPos().getY() && cheapAss.getZ() == getPos().getZ() - dir.offsetZ - rot.offsetZ * 2 ||
                cheapAss.getX() == getPos().getX() + dir.offsetX + rot.offsetX * 2 && cheapAss.getY() == getPos().getY() && cheapAss.getZ() == getPos().getZ() + dir.offsetZ + rot.offsetZ * 2) {
            return slotsRed;
        }

        return getAccessibleSlotsFromSide(side);
    }

    @Override
    public long getMaxPower() {
        return 10_000_000;
    }

    AxisAlignedBB bb = null;

    @Override
    public @NotNull AxisAlignedBB getRenderBoundingBox() {

        if (bb == null) {
            bb = new AxisAlignedBB(getPos().add(-4, -1, -4), getPos().add(5, 2, 6));
        }

        return bb;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerPASource(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIPASource(player.inventory, this);
    }

    @Override
    public boolean hasPermission(EntityPlayer player) {
        return this.isUseableByPlayer(player);
    }

    @Override
    public void receiveControl(NBTTagCompound data) {
        if (data.hasKey("cancel")) {
            this.particle = null;
            this.state = PAState.IDLE;
        }
    }

    @NotNull
    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        if (particle != null) {
            NBTTagCompound particleTag = new NBTTagCompound();
            particleTag.setInteger("x", particle.pos.getX());
            particleTag.setInteger("y", particle.pos.getY());
            particleTag.setInteger("z", particle.pos.getZ());
            particleTag.setByte("dir", (byte) particle.dir.ordinal());
            particleTag.setInteger("momentum", particle.momentum);
            particleTag.setInteger("defocus", particle.defocus);
            particleTag.setInteger("dist", particle.distanceTraveled);

            NBTTagCompound inputTag1 = new NBTTagCompound();
            NBTTagCompound inputTag2 = new NBTTagCompound();
            particle.input1.writeToNBT(inputTag1);
            particle.input2.writeToNBT(inputTag2);

            particleTag.setTag("input1", inputTag1);
            particleTag.setTag("input2", inputTag2);
            nbt.setTag("particle", particleTag);
        }
        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        if (!nbt.hasKey("particle")) return;

        NBTTagCompound particleTag = nbt.getCompoundTag("particle");
        int x = particleTag.getInteger("x");
        int y = particleTag.getInteger("y");
        int z = particleTag.getInteger("z");
        ForgeDirection dir = EnumUtil.grabEnumSafely(ForgeDirection.class, particleTag.getInteger("dir"));
        ItemStack input1 = new ItemStack(particleTag.getCompoundTag("input1"));
        ItemStack input2 = new ItemStack(particleTag.getCompoundTag("input2"));

        this.particle = new Particle(this, new BlockPos(x, y, z), dir, input1, input2);
        this.particle.momentum = particleTag.getInteger("momentum");
        this.particle.defocus = particleTag.getInteger("defocus");
        this.particle.distanceTraveled = particleTag.getInteger("dist");
    }

    public static class Particle {

        private final TileEntityPASource source;
        public BlockPos pos;
        public ForgeDirection dir;
        public int momentum;
        public int defocus;
        public int distanceTraveled;
        public static final int maxDefocus = 1000;
        public boolean invalid = false;

        public ItemStack input1;
        public ItemStack input2;

        public Particle(TileEntityPASource source, BlockPos pos, ForgeDirection dir, ItemStack input1, ItemStack input2) {
            this.source = source;
            this.pos = pos;
            this.dir = dir;
            this.input1 = input1;
            this.input2 = input2;
        }

        public void crash(PAState state) {
            this.invalid = true;
            this.source.updateState(state);
        }

        public void move(BlockPos pos) {
            this.pos = pos;
            this.source.lastSpeed = this.momentum;
        }

        public void addDistance(int dist) {
            this.distanceTraveled += dist;
        }

        public void resetDistance() {
            this.distanceTraveled = 0;
        }

        public void defocus(int amount) {
            this.defocus += amount;
            if (this.defocus > maxDefocus) this.crash(PAState.CRASH_DEFOCUS);
        }

        public void focus(int amount) {
            this.defocus -= amount;
            if (this.defocus < 0) this.defocus = 0;
        }
    }
}
