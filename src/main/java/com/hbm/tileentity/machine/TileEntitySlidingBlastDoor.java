package com.hbm.tileentity.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.handler.radiation.RadiationSystemNT;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IAnimatedDoor;
import com.hbm.items.ModItems;
import com.hbm.items.tool.ItemKeyPin;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.sound.AudioWrapper;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.longs.LongIterable;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@AutoRegister
public class TileEntitySlidingBlastDoor extends TileEntityLockableBase implements ITickable, IAnimatedDoor {

    public DoorState state = DoorState.CLOSED;
    public byte texture = 0;
    public long sysTime;
    public boolean shouldUseBB = true;
    public boolean keypadLocked = false;
    private int timer = 0;
    private boolean wasPowered = false;
    private boolean redstoneOnly = false;
    private AudioWrapper audio;

    @Override
    public void update() {
        if (!world.isRemote) {
            // T-Flip-Flop Redstone Logic
            if (!this.isLocked() && !this.keypadLocked) {
                boolean isPowered = world.isBlockPowered(pos);
                if (isPowered && !wasPowered) {
                    tryToggle();
                }
                wasPowered = isPowered;
            }

            DoorState oldState = state;

            if (state.isStationaryState()) {
                timer = 0;
            } else {
                timer++;
                if (state == DoorState.CLOSING) {
                    if (timer == 2) {
                        placeDummy(-2);
                        placeDummy(2);
                    } else if (timer == 6) {
                        placeDummy(-1);
                        placeDummy(1);
                    } else if (timer == 12) {
                        placeDummy(0);
                    }
                    if (timer > 24) {
                        state = DoorState.CLOSED;

                        if (state != oldState) {
                            // With door finally closed, mark chunk for rad update since door is now rad resistant
                            // No need to update when open as well, as opening door should update
                            RadiationSystemNT.markSectionsForRebuild(world, getOccupiedSections());
                        }
                    }
                } else if (state == DoorState.OPENING) {
                    if (timer == 12) {
                        removeDummy(0);
                    } else if (timer == 16) {
                        removeDummy(-1);
                        removeDummy(1);
                    } else if (timer == 20) {
                        removeDummy(-2);
                        removeDummy(2);
                    } else if (timer > 24) {
                        state = DoorState.OPEN;
                    }
                }
            }
            networkPackNT(100);
        }
    }

    @Override
    public void serialize(ByteBuf buf){
        buf.writeBoolean(shouldUseBB);
        buf.writeByte(state.ordinal());
        if(texture != -1)
            buf.writeByte(texture);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        shouldUseBB = buf.readBoolean();

        DoorState newState = DoorState.values()[buf.readByte()];
        handleNewState(newState);
        if (buf.readableBytes() > 0)
            texture = buf.readByte();
    }

    public boolean tryOpen(EntityPlayer player) {
        if (state == DoorState.CLOSED) {
            if (!world.isRemote && canAccess(player)) {
                open();
            }
            return true;
        }
        return false;
    }

    public boolean tryToggle() {
        if (state == DoorState.CLOSED) {
            if (!world.isRemote) {
                open();
            }
            return true;
        } else if (state == DoorState.OPEN) {
            if (!world.isRemote) {
                close();
            }
            return true;
        }
        return false;
    }

    public boolean tryToggle(EntityPlayer player) {
        if (state == DoorState.CLOSED) {
            return tryOpen(player);
        } else if (state == DoorState.OPEN) {
            return tryClose(player);
        }
        return false;
    }

    public boolean tryClose(EntityPlayer player) {
        if (state == DoorState.OPEN) {
            if (!world.isRemote && canAccess(player)) {
                close();
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean canAccess(EntityPlayer player) {
        if (keypadLocked && player != null)
            return false;

        if (!this.isLocked()) {
            return true;
        } else {
            ItemStack stack = player.getHeldItemMainhand();

            if (stack.getItem() instanceof ItemKeyPin && ItemKeyPin.getPins(stack) == this.lock) {
                world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.lockOpen, SoundCategory.BLOCKS, 1.0F, 1.0F);
                return true;
            }

            if (stack.getItem() == ModItems.key_red) {
                world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.lockOpen, SoundCategory.BLOCKS, 1.0F, 1.0F);
                return true;
            }

            return this.tryPick(player);
        }
    }

    private void placeDummy(int offset) {
        ForgeDirection dir = ForgeDirection.getOrientation(getBlockMetadata() - BlockDummyable.offset);
        BlockPos placePos = null;
        switch (dir) {
            case SOUTH:
                placePos = pos.add(offset, 0, 0);
                break;
            case NORTH:
                placePos = pos.add(-offset, 0, 0);
                break;
            case EAST:
                placePos = pos.add(0, 0, offset);
                break;
            case WEST:
                placePos = pos.add(0, 0, -offset);
                break;
            default:
                return;
        }
        if (offset == 0) {
            shouldUseBB = true;
        } else {
            ((BlockDummyable) getBlockType()).removeExtra(world, placePos.getX(), placePos.getY(), placePos.getZ());
        }
        ((BlockDummyable) getBlockType()).removeExtra(world, placePos.getX(), placePos.getY() + 1, placePos.getZ());
        ((BlockDummyable) getBlockType()).removeExtra(world, placePos.getX(), placePos.getY() + 2, placePos.getZ());
        ((BlockDummyable) getBlockType()).removeExtra(world, placePos.getX(), placePos.getY() + 3, placePos.getZ());
    }

    private void removeDummy(int offset) {
        ForgeDirection dir = ForgeDirection.getOrientation(getBlockMetadata() - BlockDummyable.offset);
        BlockPos placePos = null;
        switch (dir) {
            case SOUTH:
                placePos = pos.add(offset, 0, 0);
                break;
            case NORTH:
                placePos = pos.add(-offset, 0, 0);
                break;
            case EAST:
                placePos = pos.add(0, 0, offset);
                break;
            case WEST:
                placePos = pos.add(0, 0, -offset);
                break;
            default:
                return;
        }
        BlockDummyable.safeRem = true;
        if (offset == 0) {
            shouldUseBB = false;
        } else {
            ((BlockDummyable) getBlockType()).makeExtra(world, placePos.getX(), placePos.getY(), placePos.getZ());
        }
        ((BlockDummyable) getBlockType()).makeExtra(world, placePos.getX(), placePos.getY() + 1, placePos.getZ());
        ((BlockDummyable) getBlockType()).makeExtra(world, placePos.getX(), placePos.getY() + 2, placePos.getZ());
        ((BlockDummyable) getBlockType()).makeExtra(world, placePos.getX(), placePos.getY() + 3, placePos.getZ());
        BlockDummyable.safeRem = false;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }

    @Override
    public double getMaxRenderDistanceSquared() {
        return 65536D;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        state = DoorState.values()[compound.getByte("state")];
        sysTime = compound.getLong("sysTime");
        timer = compound.getInteger("timer");
        wasPowered = compound.getBoolean("wasPowered");
        keypadLocked = compound.getBoolean("keypadLocked");
        shouldUseBB = compound.getBoolean("shouldUseBB");
        redstoneOnly = compound.getBoolean("redstoneOnly");
        texture = compound.getByte("texture");
        super.readFromNBT(compound);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setByte("state", (byte) state.ordinal());
        compound.setLong("sysTime", sysTime);
        compound.setInteger("timer", timer);
        compound.setBoolean("wasPowered", wasPowered);
        compound.setBoolean("keypadLocked", keypadLocked);
        compound.setBoolean("shouldUseBB", shouldUseBB);
        compound.setBoolean("redstoneOnly", redstoneOnly);
        compound.setByte("texture", texture);
        return super.writeToNBT(compound);
    }

    @Override
    public void onChunkUnload() {
        if (audio != null) {
            audio.stopSound();
            audio = null;
        }
    }

    @Override
    public void invalidate() {
        if (audio != null) {
            audio.stopSound();
            audio = null;
        }
        super.invalidate();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void handleNewState(DoorState newState) {
        if (this.state != newState) {
            if (this.state == DoorState.CLOSED && newState == DoorState.OPENING) {
                if (audio == null) {
                    audio = MainRegistry.proxy.getLoopedSoundStartStop(world, HBMSoundHandler.qe_sliding_opening, null,
							HBMSoundHandler.qe_sliding_opened, SoundCategory.BLOCKS, pos.getX(), pos.getY(), pos.getZ(), 2, 1);
                    audio.startSound();
                }
            }
            if (this.state == DoorState.OPEN && newState == DoorState.CLOSING) {
                if (audio == null) {
                    audio = MainRegistry.proxy.getLoopedSoundStartStop(world, HBMSoundHandler.qe_sliding_opening, null,
							HBMSoundHandler.qe_sliding_shut, SoundCategory.BLOCKS, pos.getX(), pos.getY(), pos.getZ(), 2, 1);
                    audio.startSound();
                }
            }
            if (this.state.isMovingState() && newState.isStationaryState()) {
                if (audio != null) {
                    audio.stopSound();
                    audio = null;
                }
            }
            if (this.state.isStationaryState() && newState.isMovingState()) {
                sysTime = System.currentTimeMillis();
            }
            this.state = newState;
        }
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
    public void setTextureState(byte tex) {
        this.texture = tex;
    }

    @Override
    public boolean setTexture(String tex) {
        if (tex.equals("sliding_blast_door")) {
            this.texture = 0;
            return true;
        } else if (tex.equals("sliding_blast_door_variant1")) {
            this.texture = 1;
            return true;
        } else if (tex.equals("sliding_blast_door_variant2")) {
            this.texture = 2;
            return true;
        }
        return false;
    }

    public boolean getRedstoneOnly() {
        return redstoneOnly;
    }

    public void setRedstoneOnly(boolean redstoneOnly) {
        this.redstoneOnly = redstoneOnly;
    }

    public LongIterable getOccupiedSections() {
        LongOpenHashSet sections = new LongOpenHashSet();
        ForgeDirection dir = ForgeDirection.getOrientation(getBlockMetadata() - BlockDummyable.offset);

        int ox = pos.getX();
        int oy = pos.getY();
        int oz = pos.getZ();

        for (int offset = -2; offset <= 2; offset++) {
            int px = ox;
            int pz = oz;
            switch (dir) {
                case SOUTH -> px += offset;
                case NORTH -> px -= offset;
                case EAST -> pz += offset;
                case WEST -> pz -= offset;
            }
            for (int y = 0; y <= 3; y++) {
                sections.add(Library.blockPosToSectionLong(px, oy + y, pz));
            }
        }
        return sections;
    }
}
