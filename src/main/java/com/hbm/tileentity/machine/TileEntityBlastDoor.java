package com.hbm.tileentity.machine;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.DummyBlockBlast;
import com.hbm.handler.radiation.RadiationSystemNT;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IAnimatedDoor;
import com.hbm.interfaces.IDoor;
import com.hbm.inventory.control_panel.ControlEvent;
import com.hbm.inventory.control_panel.ControlEventSystem;
import com.hbm.inventory.control_panel.DataValueFloat;
import com.hbm.inventory.control_panel.IControllable;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.TEVaultPacket;
import it.unimi.dsi.fastutil.longs.LongIterable;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Arrays;
import java.util.List;

@AutoRegister
public class TileEntityBlastDoor extends TileEntityLockableBase implements ITickable, IControllable, IAnimatedDoor {

    public IDoor.DoorState state = IDoor.DoorState.CLOSED;
    public long sysTime;
    private int timer = 0;
    private boolean wasPowered = false;
    private boolean redstoneOnly = false;

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(pos, pos.up(6).add(1, 1, 1)).grow(0.25F);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }

    @Override
    public void update() {
        if (!world.isRemote) {
            if (!isLocked()) {
                boolean isPowered = world.isBlockPowered(pos) || world.isBlockPowered(pos.up(6));
                if (isPowered && !wasPowered) {
                    this.tryToggle();
                }
                wasPowered = isPowered;
            }

            if (state.isStationaryState()) {
                timer = 0;
            } else {
                timer++;

                if (state == DoorState.OPENING) {
                    if (timer >= 0) {
                        removeDummy(pos.up(1));
                    }
                    if (timer >= 20) {
                        removeDummy(pos.up(2));
                    }
                    if (timer >= 40) {
                        removeDummy(pos.up(3));
                    }
                    if (timer >= 60) {
                        removeDummy(pos.up(4));
                    }
                    if (timer >= 80) {
                        removeDummy(pos.up(5));
                    }
                } else {
                    if (timer >= 20) {
                        placeDummy(pos.up(5));
                    }
                    if (timer >= 40) {
                        placeDummy(pos.up(4));
                    }
                    if (timer >= 60) {
                        placeDummy(pos.up(3));
                    }
                    if (timer >= 80) {
                        placeDummy(pos.up(2));
                    }
                    if (timer >= 100) {
                        placeDummy(pos.up(1));
                    }
                }

                if (timer >= 100) {

                    if (state == DoorState.OPENING) {
                        state = DoorState.OPEN;
                        broadcastControlEvt();
                        this.world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.reactorStop, SoundCategory.BLOCKS, 0.5F, 1.0F);
                    } else if (state == DoorState.CLOSING) {
                        state = DoorState.CLOSED;
                        broadcastControlEvt();
                        this.world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.reactorStop, SoundCategory.BLOCKS, 0.5F, 1.0F);

                        // With door finally closed, mark chunk for rad update since door is now rad resistant
                        // No need to update when open as well, as opening door should update
                        RadiationSystemNT.markSectionsForRebuild(world, getOccupiedSections());
                    }
                }
            }

            PacketDispatcher.wrapper.sendToAllTracking(new TEVaultPacket(pos.getX(), pos.getY(), pos.getZ(), state.ordinal(), 0, 0),
					new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 150));
        }
    }

    public void broadcastControlEvt() {
        ControlEventSystem.get(world).broadcastToSubscribed(this, ControlEvent.newEvent("door_open_state").setVar("state",
				new DataValueFloat(state.ordinal())));
    }

    public void openNeigh() {

        TileEntity te0 = world.getTileEntity(pos.add(1, 0, 0));
        TileEntity te1 = world.getTileEntity(pos.add(-1, 0, 0));
        TileEntity te2 = world.getTileEntity(pos.add(0, 0, 1));
        TileEntity te3 = world.getTileEntity(pos.add(0, 0, -1));

        if (te0 instanceof TileEntityBlastDoor) {

            if (((TileEntityBlastDoor) te0).state == DoorState.CLOSED && (!((TileEntityBlastDoor) te0).isLocked() || ((TileEntityBlastDoor) te0).lock == lock)) {
                ((TileEntityBlastDoor) te0).open();
                ((TileEntityBlastDoor) te0).openNeigh();
            }
        }

        if (te1 instanceof TileEntityBlastDoor) {

            if (((TileEntityBlastDoor) te1).state == DoorState.CLOSED && (!((TileEntityBlastDoor) te1).isLocked() || ((TileEntityBlastDoor) te1).lock == lock)) {
                ((TileEntityBlastDoor) te1).open();
                ((TileEntityBlastDoor) te1).openNeigh();
            }
        }

        if (te2 instanceof TileEntityBlastDoor) {

            if (((TileEntityBlastDoor) te2).state == DoorState.CLOSED && (!((TileEntityBlastDoor) te2).isLocked() || ((TileEntityBlastDoor) te2).lock == lock)) {
                ((TileEntityBlastDoor) te2).open();
                ((TileEntityBlastDoor) te2).openNeigh();
            }
        }

        if (te3 instanceof TileEntityBlastDoor) {

            if (((TileEntityBlastDoor) te3).state == DoorState.CLOSED && (!((TileEntityBlastDoor) te3).isLocked() || ((TileEntityBlastDoor) te3).lock == lock)) {
                ((TileEntityBlastDoor) te3).open();
                ((TileEntityBlastDoor) te3).openNeigh();
            }
        }
    }

    @Override
    public void lock() {
        super.lock();
        lockNeigh();
    }

    public void closeNeigh() {

        TileEntity te0 = world.getTileEntity(pos.add(1, 0, 0));
        TileEntity te1 = world.getTileEntity(pos.add(-1, 0, 0));
        TileEntity te2 = world.getTileEntity(pos.add(0, 0, 1));
        TileEntity te3 = world.getTileEntity(pos.add(0, 0, -1));

        if (te0 instanceof TileEntityBlastDoor) {

            if (((TileEntityBlastDoor) te0).state == DoorState.OPEN && (!((TileEntityBlastDoor) te0).isLocked() || ((TileEntityBlastDoor) te0).lock == lock)) {
                ((TileEntityBlastDoor) te0).close();
                ((TileEntityBlastDoor) te0).closeNeigh();
            }
        }

        if (te1 instanceof TileEntityBlastDoor) {

            if (((TileEntityBlastDoor) te1).state == DoorState.OPEN && (!((TileEntityBlastDoor) te1).isLocked() || ((TileEntityBlastDoor) te1).lock == lock)) {
                ((TileEntityBlastDoor) te1).close();
                ((TileEntityBlastDoor) te1).closeNeigh();
            }
        }

        if (te2 instanceof TileEntityBlastDoor) {

            if (((TileEntityBlastDoor) te2).state == DoorState.OPEN && (!((TileEntityBlastDoor) te2).isLocked() || ((TileEntityBlastDoor) te2).lock == lock)) {
                ((TileEntityBlastDoor) te2).close();
                ((TileEntityBlastDoor) te2).closeNeigh();
            }
        }

        if (te3 instanceof TileEntityBlastDoor) {

            if (((TileEntityBlastDoor) te3).state == DoorState.OPEN && (!((TileEntityBlastDoor) te3).isLocked() || ((TileEntityBlastDoor) te3).lock == lock)) {
                ((TileEntityBlastDoor) te3).close();
                ((TileEntityBlastDoor) te3).closeNeigh();
            }
        }
    }

    public void lockNeigh() {

        TileEntity te0 = world.getTileEntity(pos.add(1, 0, 0));
        TileEntity te1 = world.getTileEntity(pos.add(-1, 0, 0));
        TileEntity te2 = world.getTileEntity(pos.add(0, 0, 1));
        TileEntity te3 = world.getTileEntity(pos.add(0, 0, -1));

        if (te0 instanceof TileEntityBlastDoor) {

            if (!((TileEntityBlastDoor) te0).isLocked()) {
                ((TileEntityBlastDoor) te0).setPins(this.lock);
                ((TileEntityBlastDoor) te0).lock();
                ((TileEntityBlastDoor) te0).setMod(lockMod);
            }
        }

        if (te1 instanceof TileEntityBlastDoor) {

            if (!((TileEntityBlastDoor) te1).isLocked()) {
                ((TileEntityBlastDoor) te1).setPins(this.lock);
                ((TileEntityBlastDoor) te1).lock();
                ((TileEntityBlastDoor) te1).setMod(lockMod);
            }
        }

        if (te2 instanceof TileEntityBlastDoor) {

            if (!((TileEntityBlastDoor) te2).isLocked()) {
                ((TileEntityBlastDoor) te2).setPins(this.lock);
                ((TileEntityBlastDoor) te2).lock();
                ((TileEntityBlastDoor) te2).setMod(lockMod);
            }
        }

        if (te3 instanceof TileEntityBlastDoor) {

            if (!((TileEntityBlastDoor) te3).isLocked()) {
                ((TileEntityBlastDoor) te3).setPins(this.lock);
                ((TileEntityBlastDoor) te3).lock();
                ((TileEntityBlastDoor) te3).setMod(lockMod);
            }
        }
    }

    public boolean tryOpen() {
        if (state == DoorState.CLOSED) {
            if (!world.isRemote) {
                open();
                openNeigh();
            }
            return true;
        }
        return false;
    }

    public boolean tryToggle() {
        if (state == DoorState.CLOSED) {
            return tryOpen();
        } else if (state == DoorState.OPEN) {
            return tryClose();
        }
        return false;
    }

    public boolean tryClose() {
        if (state == DoorState.OPEN) {
            if (!world.isRemote) {
                close();
                closeNeigh();
            }
            return true;
        }
        return false;
    }

    public boolean placeDummy(BlockPos pos) {

        if (!world.getBlockState(pos).getBlock().isReplaceable(world, pos))
            return false;

        world.setBlockState(pos, ModBlocks.dummy_block_blast.getDefaultState());

        TileEntity te = world.getTileEntity(pos);

        if (te instanceof TileEntityDummy dummy) {
            dummy.target = this.pos;
        }

        return true;
    }

    public void removeDummy(BlockPos pos) {

        if (world.getBlockState(pos).getBlock() == ModBlocks.dummy_block_blast) {
            DummyBlockBlast.safeBreak = true;
            world.setBlockState(pos, Blocks.AIR.getDefaultState());
            DummyBlockBlast.safeBreak = false;
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        state = DoorState.values()[compound.getInteger("state")];
        sysTime = compound.getLong("sysTime");
        timer = compound.getInteger("timer");
        wasPowered = compound.getBoolean("wasPowered");
        redstoneOnly = compound.getBoolean("redstoneOnly");
        super.readFromNBT(compound);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setInteger("state", state.ordinal());
        compound.setLong("sysTime", sysTime);
        compound.setInteger("timer", timer);
        compound.setBoolean("wasPowered", wasPowered);
        compound.setBoolean("redstoneOnly", redstoneOnly);
        return super.writeToNBT(compound);
    }

    @Override
    public void receiveEvent(BlockPos from, ControlEvent e) {
        if (e.name.equals("door_toggle")) {
            tryToggle();
        }
    }

    @Override
    public List<String> getInEvents() {
        return Arrays.asList("door_toggle");
    }

    @Override
    public List<String> getOutEvents() {
        return Arrays.asList("door_open_state");
    }

    @Override
    public void validate() {
        super.validate();
        ControlEventSystem.get(world).addControllable(this);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        ControlEventSystem.get(world).removeControllable(this);
    }

    @Override
    public BlockPos getControlPos() {
        return getPos();
    }

    @Override
    public World getControlWorld() {
        return getWorld();
    }

    @Override
    public void open() {
        if (state == DoorState.CLOSED)
            toggle();
    }

    @Override
    public void close() {
        if (state == DoorState.OPEN)
            toggle();
    }

    @Override
    public DoorState getState() {
        return state;
    }

    @Override
    public void toggle() {
        if (state == DoorState.CLOSED) {
            state = DoorState.OPENING;
            PacketDispatcher.wrapper.sendToAllTracking(new TEVaultPacket(pos.getX(), pos.getY(), pos.getZ(), state.ordinal(), 1, 0),
					new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 150));
            closeNeigh();
            broadcastControlEvt();
            this.world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.reactorStart, SoundCategory.BLOCKS, 0.5F, 0.75F);

            // With door opening, mark chunk for rad update
            RadiationSystemNT.markSectionsForRebuild(world, getOccupiedSections());
        } else if (state == DoorState.OPEN) {
            state = DoorState.CLOSING;
            PacketDispatcher.wrapper.sendToAllTracking(new TEVaultPacket(pos.getX(), pos.getY(), pos.getZ(), state.ordinal(), 1, 0),
					new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 150));
            openNeigh();
            broadcastControlEvt();
            this.world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.reactorStart, SoundCategory.BLOCKS, 0.5F, 0.75F);


            // With door closing, mark chunk for rad update
            RadiationSystemNT.markSectionsForRebuild(world, getOccupiedSections());
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void handleNewState(DoorState newState) {

    }

    public boolean getRedstoneOnly() {
        return redstoneOnly;
    }

    public void setRedstoneOnly(boolean redstoneOnly) {
        this.redstoneOnly = redstoneOnly;
    }

    public LongIterable getOccupiedSections() {
        LongOpenHashSet sections = new LongOpenHashSet();
        int ox = pos.getX();
        int oy = pos.getY();
        int oz = pos.getZ();
        for (int i = 0; i <= 5; i++) {
            sections.add(Library.blockPosToSectionLong(ox, oy + i, oz));
        }
        return sections;
    }
}
