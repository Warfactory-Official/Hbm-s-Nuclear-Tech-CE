package com.hbm.tileentity.machine;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.DummyBlockVault;
import com.hbm.blocks.machine.VaultDoor;
import com.hbm.handler.radiation.RadiationSystemNT;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IAnimatedDoor;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.TEVaultPacket;
import it.unimi.dsi.fastutil.longs.LongIterable;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing.Axis;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@AutoRegister
public class TileEntityVaultDoor extends TileEntityLockableBase implements ITickable, IAnimatedDoor {

    public static final int maxTypes = 32;
    public DoorState state = DoorState.CLOSED;
    public long sysTime;
    public int type;
    private int timer = 0;
    private boolean wasPowered = false;
    private boolean redstoneOnly = false;

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return TileEntity.INFINITE_EXTENT_AABB;
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
                    this.tryToggle();
                }
                wasPowered = isPowered;
            }

            if (state == DoorState.OPENING && state.isMovingState()) {

                if (timer == 0)
                    this.world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.vaultScrapeNew, SoundCategory.BLOCKS, 1.0F, 1.0F);
                if (timer == 45)
                    this.world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.vaultThudNew, SoundCategory.BLOCKS, 1.0F, 1.0F);
                if (timer == 55)
                    this.world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.vaultThudNew, SoundCategory.BLOCKS, 1.0F, 1.0F);
                if (timer == 65)
                    this.world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.vaultThudNew, SoundCategory.BLOCKS, 1.0F, 1.0F);
                if (timer == 75)
                    this.world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.vaultThudNew, SoundCategory.BLOCKS, 1.0F, 1.0F);
                if (timer == 85)
                    this.world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.vaultThudNew, SoundCategory.BLOCKS, 1.0F, 1.0F);
                if (timer == 95)
                    this.world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.vaultThudNew, SoundCategory.BLOCKS, 1.0F, 1.0F);
                if (timer == 105)
                    this.world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.vaultThudNew, SoundCategory.BLOCKS, 1.0F, 1.0F);
                if (timer == 115)
                    this.world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.vaultThudNew, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }
            if (state == DoorState.CLOSING && state.isMovingState()) {

                if (timer == 0)
                    this.world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.vaultThudNew, SoundCategory.BLOCKS, 1.0F, 1.0F);
                if (timer == 10)
                    this.world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.vaultThudNew, SoundCategory.BLOCKS, 1.0F, 1.0F);
                if (timer == 20)
                    this.world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.vaultThudNew, SoundCategory.BLOCKS, 1.0F, 1.0F);
                if (timer == 30)
                    this.world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.vaultThudNew, SoundCategory.BLOCKS, 1.0F, 1.0F);
                if (timer == 40)
                    this.world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.vaultThudNew, SoundCategory.BLOCKS, 1.0F, 1.0F);
                if (timer == 50)
                    this.world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.vaultThudNew, SoundCategory.BLOCKS, 1.0F, 1.0F);
                if (timer == 60)
                    this.world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.vaultThudNew, SoundCategory.BLOCKS, 1.0F, 1.0F);
                if (timer == 70)
                    this.world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.vaultThudNew, SoundCategory.BLOCKS, 1.0F, 1.0F);

                if (timer == 80)
                    this.world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.vaultScrapeNew, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }

            if (state.isStationaryState()) {
                timer = 0;
            } else {
                timer++;

                if (timer >= 120) {

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
            PacketDispatcher.wrapper.sendToAllAround(new TEVaultPacket(pos.getX(), pos.getY(), pos.getZ(), state.ordinal(), 0, type),
					new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 300));
        }
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
        state = DoorState.values()[compound.getInteger("state")];
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
            openHatch();
            PacketDispatcher.wrapper.sendToAllAround(new TEVaultPacket(pos.getX(), pos.getY(), pos.getZ(), state.ordinal(), 1, type),
					new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 300));

            // With door opening, mark chunk for rad update
            RadiationSystemNT.markSectionsForRebuild(world, getOccupiedSections());
        } else if (state == DoorState.OPEN) {
            state = DoorState.CLOSING;
            timer = 0;
            closeHatch();
            PacketDispatcher.wrapper.sendToAllAround(new TEVaultPacket(pos.getX(), pos.getY(), pos.getZ(), state.ordinal(), 1, type),
					new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 300));

            // With door closing, mark chunk for rad update
            RadiationSystemNT.markSectionsForRebuild(world, getOccupiedSections());
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void handleNewState(DoorState state) {
        // TODO: Move audio into this method from update method to match sliding blast door
    }

    public boolean getRedstoneOnly() {
        return redstoneOnly;
    }

    public void setRedstoneOnly(boolean redstoneOnly) {
        this.redstoneOnly = redstoneOnly;
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
