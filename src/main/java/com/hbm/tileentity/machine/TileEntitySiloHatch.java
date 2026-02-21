package com.hbm.tileentity.machine;

import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.BlockSiloHatch;
import com.hbm.blocks.machine.DummyBlockSiloHatch;
import com.hbm.handler.radiation.RadiationSystemNT;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IAnimatedDoor;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.TEDoorAnimationPacket;
import it.unimi.dsi.fastutil.longs.LongIterable;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@AutoRegister
public class TileEntitySiloHatch extends TileEntityLockableBase implements ITickable, IAnimatedDoor {

    public DoorState state = DoorState.CLOSED;
    public long sysTime;
    public int timer = -1;
    public EnumFacing facing = null;
    public AxisAlignedBB renderBox = null;
    private boolean wasPowered = false;
    private boolean redstoneOnly = false;

    @Override
    public void update() {
        if (!world.isRemote) {
            // T-Flip-Flop redstone behavior
            if (!this.isLocked()) {
                boolean isPowered = world.isBlockPowered(pos);
                if (isPowered && !wasPowered) {
                    tryToggle();
                }
                wasPowered = isPowered;
            }
            DoorState oldState = state;
            if (timer < 0) {
                //oldState = -1; // what
                oldState = null;
            }

            if (this.state.isStationaryState()) {
                timer = 0;
            } else {
                if (facing == null)
                    facing = world.getBlockState(pos).getValue(BlockSiloHatch.FACING).getOpposite();
                timer++;
                if (state == DoorState.CLOSING) {
                    if (timer == 1) {
                        BlockPos hydrolics = pos.offset(facing, 5);
                        this.world.playSound(null, hydrolics.getX(), hydrolics.getY(), hydrolics.getZ(), HBMSoundHandler.siloclose,
								SoundCategory.BLOCKS, 3F, 1F);
                    }
                    if (timer == 50) {
                        BlockPos mid = pos.offset(facing, 3);
                        for (int i = -1; i <= 1; i++) {
                            for (int j = -1; j <= 1; j++) {
                                placeDummy(mid.add(i, 0, j));
                            }
                        }
                    }
                    if (timer > 100) {
                        state = DoorState.CLOSED;

                        if (state != oldState) {
                            // With door finally closed, mark chunk for rad update since door is now rad resistant
                            // No need to update when open as well, as opening door should update
                            RadiationSystemNT.markSectionsForRebuild(world, getOccupiedSections());
                        }
                    }
                } else if (state == DoorState.OPENING) {
                    if (timer == 1) {
                        BlockPos hydrolics = pos.offset(facing, 5);
                        this.world.playSound(null, hydrolics.getX(), hydrolics.getY(), hydrolics.getZ(), HBMSoundHandler.siloopen,
								SoundCategory.BLOCKS, 4F, 1F);
                    }
                    if (timer == 70) {
                        BlockPos mid = pos.offset(facing, 3);
                        for (int i = -1; i <= 1; i++) {
                            for (int j = -1; j <= 1; j++) {
                                removeDummy(mid.add(i, 0, j));
                            }
                        }
                    }
                    if (timer > 100) {
                        state = DoorState.OPEN;
                    }
                }
            }
            if (oldState != state)
                PacketDispatcher.wrapper.sendToAllTracking(new TEDoorAnimationPacket(pos, (byte) state.ordinal()),
						new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 200));
        }
    }

    public void tryToggle() {
        if (state == DoorState.CLOSED) {
            tryOpen();
        } else if (state == DoorState.OPEN) {
            tryClose();
        }
    }

    public void tryOpen() {
        if (this.state == DoorState.CLOSED) {
            if (!world.isRemote) {
                open();
                timer = -1;
            }
        }
    }

    public void tryClose() {
        if (this.state == DoorState.OPEN) {
            if (!world.isRemote) {
                close();
                timer = -1;
            }
        }
    }

    public boolean placeDummy(BlockPos pos) {

        if (!world.getBlockState(pos).getBlock().isReplaceable(world, pos))
            return false;

        world.setBlockState(pos, ModBlocks.dummy_block_silo_hatch.getDefaultState());

        TileEntity te = world.getTileEntity(pos);

        if (te instanceof TileEntityDummy dummy) {
            dummy.target = this.pos;
        }

        return true;
    }

    public void removeDummy(BlockPos pos) {
        if (world.getBlockState(pos).getBlock() == ModBlocks.dummy_block_silo_hatch) {
            DummyBlockSiloHatch.safeBreak = true;
            world.setBlockState(pos, Blocks.AIR.getDefaultState());
            DummyBlockSiloHatch.safeBreak = false;
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        state = DoorState.values()[compound.getByte("state")];
        wasPowered = compound.getBoolean("wasPowered");
        redstoneOnly = compound.getBoolean("redstoneOnly");
        super.readFromNBT(compound);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setByte("state", (byte) state.ordinal());
        compound.setBoolean("wasPowered", wasPowered);
        compound.setBoolean("redstoneOnly", redstoneOnly);
        return super.writeToNBT(compound);
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        if (facing == null)
            facing = world.getBlockState(pos).getValue(BlockSiloHatch.FACING).getOpposite();
        if (renderBox == null)
            renderBox = new AxisAlignedBB(-3.3, 0, -3.3, 4.3, 2, 4.3).offset(pos.offset(facing, 3));
        return renderBox;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
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
            // With door opening, mark chunk for rad update
            RadiationSystemNT.markSectionsForRebuild(world, getOccupiedSections());
        } else if (state == DoorState.OPEN) {
            state = DoorState.CLOSING;
            // With door closing, mark chunk for rad update
            RadiationSystemNT.markSectionsForRebuild(world, getOccupiedSections());
        }
    }

    @Override
    public void handleNewState(DoorState newState) {
        if (this.state != newState) {
            if (this.state.isStationaryState() && newState.isMovingState()) {
                sysTime = System.currentTimeMillis();
            }
            this.state = newState;
        }
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
        EnumFacing face = facing;
        if (face == null) {
            if (world != null) {
                face = world.getBlockState(pos).getValue(BlockSiloHatch.FACING).getOpposite();
            } else {
                return sections;
            }
        }
        int midX = pos.getX() + face.getXOffset() * 3;
        int midY = pos.getY() + face.getYOffset() * 3;
        int midZ = pos.getZ() + face.getZOffset() * 3;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                sections.add(Library.blockPosToSectionLong(midX + i, midY, midZ + j));
            }
        }
        return sections;
    }
}
