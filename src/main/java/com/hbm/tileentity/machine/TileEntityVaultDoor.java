package com.hbm.tileentity.machine;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.DummyBlockVault;
import com.hbm.blocks.machine.VaultDoor;
import com.hbm.handler.radiation.RadiationSystemNT;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IAnimatedDoor;
import com.hbm.interfaces.IDoor;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.longs.LongIterable;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@AutoRegister
public class TileEntityVaultDoor extends TileEntityLockableBase implements ITickable, IAnimatedDoor {

    private static final int ANIM_TICKS = 120;
    private static final int[] OPENING_THUD_TICKS = {45, 55, 65, 75, 85, 95, 105, 115};
    private static final int[] CLOSING_THUD_TICKS = {0, 10, 20, 30, 40, 50, 60, 70};
    private static final int CLOSING_SCRAPE_TICK = 80;

    public static final int maxTypes = 32;
    public DoorState state = DoorState.CLOSED;
    public long sysTime;
    public int type;
    private int timer = 0;
    private boolean wasPowered = false;
    private boolean redstoneOnly = false;
    private int lastClientAudioTick = -1;

    @Override
    public void update() {
        if (!world.isRemote) {

            if (!isLocked()) {
                boolean isPowered = false;
                Axis axis = world.getBlockState(pos).getValue(VaultDoor.FACING).getAxis();

                if (axis == Axis.Z) {
                    // Check X-plane
                    for (int x = pos.getX() - 2; x <= pos.getX() + 2 && !isPowered; x++) {
                        for (int y = pos.getY(); y <= pos.getY() + 5; y++) {
                            if (world.isBlockPowered(new BlockPos(x, y, pos.getZ()))) {
                                isPowered = true;
                                break;
                            }
                        }
                    }
                } else if (axis == Axis.X) {
                    // Check Z-plane
                    for (int z = pos.getZ() - 2; z <= pos.getZ() + 2 && !isPowered; z++) {
                        for (int y = pos.getY(); y <= pos.getY() + 5; y++) {
                            if (world.isBlockPowered(new BlockPos(pos.getX(), y, z))) {
                                isPowered = true;
                                break;
                            }
                        }
                    }
                }

                if (isPowered && !wasPowered) {
                    tryToggle();
                }
                wasPowered = isPowered;
            }

            if (state.isStationaryState()) {
                timer = 0;
            } else {
                timer++;

                if (timer >= ANIM_TICKS) {

                    if (state == DoorState.OPENING) {
                        state = DoorState.OPEN;
                    } else {
                        state = DoorState.CLOSED;

                        // With door finally closed, mark chunk for rad update since door is now rad resistant
                        // No need to update when open as well, as opening door should update
                        RadiationSystemNT.markSectionsForRebuild(world, getOccupiedSections());
                    }
                }
            }
            networkPackNT(300);
        } else {
            updateClientAudio();
        }
    }

    @SideOnly(Side.CLIENT)
    private void updateClientAudio() {
        if (!state.isMovingState()) {
            lastClientAudioTick = -1;
            return;
        }
        int tick = (int) Math.min((long) ANIM_TICKS, Math.max(0L, System.currentTimeMillis() - sysTime) / 50L);
        if (tick == lastClientAudioTick) return;

        if (state == DoorState.OPENING) {
            if (lastClientAudioTick < 0) {
                playClientSound(HBMSoundHandler.vaultScrapeNew);
            }
            fireThuds(tick, OPENING_THUD_TICKS);
        } else {
            fireThuds(tick, CLOSING_THUD_TICKS);
            if (lastClientAudioTick < CLOSING_SCRAPE_TICK && tick >= CLOSING_SCRAPE_TICK) {
                playClientSound(HBMSoundHandler.vaultScrapeNew);
            }
        }
        lastClientAudioTick = tick;
    }

    @SideOnly(Side.CLIENT)
    private void fireThuds(int tick, int[] thudTicks) {
        for (int t : thudTicks) {
            if (lastClientAudioTick < t && tick >= t) {
                playClientSound(HBMSoundHandler.vaultThudNew);
            }
        }
    }

    @SideOnly(Side.CLIENT)
    private void playClientSound(SoundEvent sound) {
        world.playSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, sound, SoundCategory.BLOCKS, 1.0F, 1.0F, false);
    }

    public boolean tryOpen() {
        if (state == DoorState.CLOSED) {
            if (!world.isRemote) {
                open();
            }
            return true;
        }
        return false;
    }

    public boolean tryToggle() {
        if (state == DoorState.CLOSED) {
            return tryOpen();
        } else if (state == DoorState.OPEN && isHatchFree()) {
            return tryClose();
        }
        return false;
    }

    public boolean tryClose() {
        if (state == DoorState.OPEN) {
            if (!world.isRemote) {
                close();
            }
            return true;
        }
        return false;
    }

    public boolean placeDummy(int x, int y, int z) {
        return placeDummy(new BlockPos(x, y, z));
    }

    public boolean placeDummy(BlockPos dummyPos) {
        if (!world.getBlockState(dummyPos).getBlock().isReplaceable(world, dummyPos))
            return false;

        world.setBlockState(dummyPos, ModBlocks.dummy_block_vault.getDefaultState());

        TileEntity te = world.getTileEntity(dummyPos);

        if (te instanceof TileEntityDummy dummy) {
            dummy.target = pos;
        }

        return true;
    }

    public void removeDummy(BlockPos dummyPos) {
        if (world.getBlockState(dummyPos).getBlock() == ModBlocks.dummy_block_vault) {
            DummyBlockVault.safeBreak = true;
            world.setBlockState(dummyPos, Blocks.AIR.getDefaultState());
            DummyBlockVault.safeBreak = false;
        }
    }

    private boolean isHatchFree() {
        if (world.getBlockState(pos).getValue(VaultDoor.FACING).getAxis() == Axis.Z)
            return checkNS();
        else if (world.getBlockState(pos).getValue(VaultDoor.FACING).getAxis() == Axis.X)
            return checkEW();
        else
            return true;
    }

    private void closeHatch() {
        if (world.getBlockState(pos).getValue(VaultDoor.FACING).getAxis() == Axis.Z)
            fillNS();
        else if (world.getBlockState(pos).getValue(VaultDoor.FACING).getAxis() == Axis.X)
            fillEW();
    }

    private void openHatch() {
        if (world.getBlockState(pos).getValue(VaultDoor.FACING).getAxis() == Axis.Z)
            removeNS();
        else if (world.getBlockState(pos).getValue(VaultDoor.FACING).getAxis() == Axis.X)
            removeEW();
    }

    private boolean checkNS() {
        return world.getBlockState(pos.add(-1, 1, 0)).getBlock().isReplaceable(world, pos.add(-1, 1, 0)) &&
                world.getBlockState(pos.add(0, 1, 0)).getBlock().isReplaceable(world, pos.add(0, 1, 0)) &&
                world.getBlockState(pos.add(1, 1, 0)).getBlock().isReplaceable(world, pos.add(1, 1, 0)) &&
                world.getBlockState(pos.add(-1, 2, 0)).getBlock().isReplaceable(world, pos.add(-1, 2, 0)) &&
                world.getBlockState(pos.add(0, 2, 0)).getBlock().isReplaceable(world, pos.add(0, 2, 0)) &&
                world.getBlockState(pos.add(1, 2, 0)).getBlock().isReplaceable(world, pos.add(1, 2, 0)) &&
                world.getBlockState(pos.add(-1, 3, 0)).getBlock().isReplaceable(world, pos.add(-1, 3, 0)) &&
                world.getBlockState(pos.add(0, 3, 0)).getBlock().isReplaceable(world, pos.add(0, 3, 0)) &&
                world.getBlockState(pos.add(1, 3, 0)).getBlock().isReplaceable(world, pos.add(1, 3, 0));
    }

    private boolean checkEW() {
        return world.getBlockState(pos.add(0, 1, -1)).getBlock().isReplaceable(world, pos.add(0, 1, -1)) &&
                world.getBlockState(pos.add(0, 1, 0)).getBlock().isReplaceable(world, pos.add(0, 1, 0)) &&
                world.getBlockState(pos.add(0, 1, 1)).getBlock().isReplaceable(world, pos.add(0, 1, 1)) &&
                world.getBlockState(pos.add(0, 2, -1)).getBlock().isReplaceable(world, pos.add(0, 2, -1)) &&
                world.getBlockState(pos.add(0, 2, 0)).getBlock().isReplaceable(world, pos.add(0, 2, 0)) &&
                world.getBlockState(pos.add(0, 2, 1)).getBlock().isReplaceable(world, pos.add(0, 2, 1)) &&
                world.getBlockState(pos.add(0, 3, -1)).getBlock().isReplaceable(world, pos.add(0, 3, -1)) &&
                world.getBlockState(pos.add(0, 3, 0)).getBlock().isReplaceable(world, pos.add(0, 3, 0)) &&
                world.getBlockState(pos.add(0, 3, 1)).getBlock().isReplaceable(world, pos.add(0, 3, 1));
    }

    private void fillNS() {

        placeDummy(pos.add(-1, 1, 0));
        placeDummy(pos.add(-1, 2, 0));
        placeDummy(pos.add(-1, 3, 0));
        placeDummy(pos.add(0, 1, 0));
        placeDummy(pos.add(0, 2, 0));
        placeDummy(pos.add(0, 3, 0));
        placeDummy(pos.add(1, 1, 0));
        placeDummy(pos.add(1, 2, 0));
        placeDummy(pos.add(1, 3, 0));
    }

    private void fillEW() {

        placeDummy(pos.add(0, 1, -1));
        placeDummy(pos.add(0, 2, -1));
        placeDummy(pos.add(0, 3, -1));
        placeDummy(pos.add(0, 1, 0));
        placeDummy(pos.add(0, 2, 0));
        placeDummy(pos.add(0, 3, 0));
        placeDummy(pos.add(0, 1, 1));
        placeDummy(pos.add(0, 2, 1));
        placeDummy(pos.add(0, 3, 1));
    }

    private void removeNS() {

        removeDummy(pos.add(-1, 1, 0));
        removeDummy(pos.add(-1, 2, 0));
        removeDummy(pos.add(-1, 3, 0));
        removeDummy(pos.add(0, 1, 0));
        removeDummy(pos.add(0, 2, 0));
        removeDummy(pos.add(0, 3, 0));
        removeDummy(pos.add(1, 1, 0));
        removeDummy(pos.add(1, 2, 0));
        removeDummy(pos.add(1, 3, 0));
    }

    private void removeEW() {

        removeDummy(pos.add(0, 1, -1));
        removeDummy(pos.add(0, 2, -1));
        removeDummy(pos.add(0, 3, -1));
        removeDummy(pos.add(0, 1, 0));
        removeDummy(pos.add(0, 2, 0));
        removeDummy(pos.add(0, 3, 0));
        removeDummy(pos.add(0, 1, 1));
        removeDummy(pos.add(0, 2, 1));
        removeDummy(pos.add(0, 3, 1));
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        state = DoorState.VALUES[compound.getInteger("state")];
        sysTime = compound.getLong("sysTime");
        timer = compound.getInteger("timer");
        wasPowered = compound.getBoolean("wasPowered");
        redstoneOnly = compound.getBoolean("redstoneOnly");
        type = compound.getInteger("type");
        super.readFromNBT(compound);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setInteger("state", state.ordinal());
        compound.setLong("sysTime", sysTime);
        compound.setInteger("timer", timer);
        compound.setBoolean("wasPowered", wasPowered);
        compound.setBoolean("redstoneOnly", redstoneOnly);
        compound.setInteger("type", type);
        return super.writeToNBT(compound);
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeByte(state.ordinal());
        buf.writeLong(sysTime);
        buf.writeByte(type);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        DoorState newState = IDoor.DoorState.VALUES[buf.readByte()];
        long syncedSysTime = buf.readLong();
        int newType = buf.readByte();
        Minecraft.getMinecraft().addScheduledTask(() -> {
            if (world == null || isInvalid() || world.getTileEntity(pos) != this) return;
            handleNewState(newState);
            if (!newState.isMovingState()) sysTime = syncedSysTime;
            type = newType;
        });
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
            timer = 0;
            sysTime = System.currentTimeMillis();
            openHatch();
            networkPackNT(300);

            // With door opening, mark chunk for rad update
            RadiationSystemNT.markSectionsForRebuild(world, getOccupiedSections());
        } else if (state == DoorState.OPEN) {
            state = DoorState.CLOSING;
            timer = 0;
            sysTime = System.currentTimeMillis();
            closeHatch();
            networkPackNT(300);

            // With door closing, mark chunk for rad update
            RadiationSystemNT.markSectionsForRebuild(world, getOccupiedSections());
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void handleNewState(DoorState newState) {
        if (state != newState) {
            sysTime = IAnimatedDoor.clientAnimStart(state, newState, sysTime);
            state = newState;
        }
    }

    public boolean getRedstoneOnly() {
        return redstoneOnly;
    }

    public void setRedstoneOnly(boolean redstoneOnly) {
        this.redstoneOnly = redstoneOnly;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }

    public LongIterable getOccupiedSections() {
        LongOpenHashSet sections = new LongOpenHashSet();
        sections.add(Library.blockPosToSectionLong(pos));

        Axis axis = world.getBlockState(pos).getValue(VaultDoor.FACING).getAxis();
        int ox = pos.getX();
        int oy = pos.getY();
        int oz = pos.getZ();

        if (axis == Axis.Z) {
            for (int x = -1; x <= 1; x++) {
                for (int y = 1; y <= 3; y++) {
                    sections.add(Library.blockPosToSectionLong(ox + x, oy + y, oz));
                }
            }
        } else if (axis == Axis.X) {
            for (int z = -1; z <= 1; z++) {
                for (int y = 1; y <= 3; y++) {
                    sections.add(Library.blockPosToSectionLong(ox, oy + y, oz + z));
                }
            }
        }
        return sections;
    }
}
