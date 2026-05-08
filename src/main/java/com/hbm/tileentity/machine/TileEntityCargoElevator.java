package com.hbm.tileentity.machine;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.tileentity.TileEntityLoadedBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

import java.util.List;

@AutoRegister
public class TileEntityCargoElevator extends TileEntityLoadedBase implements ITickable {

    public int height;

    public double extension;
    public double prevExtension;
    public double syncExtension;
    private int sync;

    public boolean isExtending;
    public static final double speed = 2D / 20D;
    public boolean renderPlatform;

    private AxisAlignedBB bb;

    @Override
    public void update() {
        this.prevExtension = this.extension;

        if (!world.isRemote) {
            if (world.getBlockState(pos.down()).getBlock() == ModBlocks.cargo_elevator) {
                int[] lowerCore = ((BlockDummyable) ModBlocks.cargo_elevator).findCore(world, pos.getX(), pos.getY() - 1, pos.getZ());
                if (lowerCore != null && lowerCore[0] == pos.getX() && lowerCore[2] == pos.getZ()) {
                    TileEntityCargoElevator lower = (TileEntityCargoElevator) world.getTileEntity(new BlockPos(lowerCore[0], lowerCore[1], lowerCore[2]));
                    if (lower != null) {
                        lower.height += this.height + 1;
                        for (int x = pos.getX() - 1; x < pos.getX() + 2; x++) {
                            for (int z = pos.getZ() - 1; z < pos.getZ() + 2; z++) {
                                for (int y = pos.getY(); y <= pos.getY() + this.height; y++) {
                                    world.setBlockState(new BlockPos(x, y, z), ModBlocks.cargo_elevator.getDefaultState().withProperty(BlockDummyable.META, 1), 3);
                                }
                            }
                        }
                        lower.markDirty();
                        lower.markChanged();
                    }
                    return;
                }
            }

            if (this.isExtending && this.extension < this.height) {
                this.extension += speed;
            }

            if (!this.isExtending && this.extension > 0) {
                this.extension -= speed;
            }

            this.extension = MathHelper.clamp(this.extension, 0D, this.height);
            this.renderPlatform = true;
            this.networkPackNT(100);
        } else {
            if (this.sync > 0) {
                this.extension = this.extension + ((this.syncExtension - this.extension) / (float) this.sync);
                --this.sync;
            } else {
                this.extension = this.syncExtension;
            }
        }

        if (this.extension != this.prevExtension) {
            double liftUpper = this.pos.getY() + 1D + Math.max(this.extension, this.prevExtension);
            double liftLower = this.pos.getY() + 1D + Math.min(this.extension, this.prevExtension);
            List<Entity> toLift = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(this.pos.getX() - 0.99D, liftLower, this.pos.getZ() - 0.99D, this.pos.getX() + 1.99D, liftUpper, this.pos.getZ() + 1.99D));

            for (Entity entity : toLift) {
                if (entity instanceof EntityPlayer && !world.isRemote) continue;
                if (entity.getEntityBoundingBox().minY >= liftLower && entity.getEntityBoundingBox().minY <= liftUpper) {
                    double delta = entity.getEntityBoundingBox().minY - (this.pos.getY() + 1D + this.extension);
                    entity.move(MoverType.SELF, 0.0D, -delta, 0.0D);
                    entity.onGround = true;
                    entity.move(MoverType.SELF, 0.0D, -0.125D, 0.0D);
                }
            }
        }
    }

    public void toggleElevator() {
        if (this.extension >= this.height) {
            this.isExtending = false;
        }
        if (this.extension <= 0) {
            this.isExtending = true;
        }
        this.markDirty();
        this.markChanged();
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeBoolean(this.renderPlatform);
        buf.writeShort((short) this.height);
        buf.writeDouble(this.extension);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        this.renderPlatform = buf.readBoolean();
        this.height = buf.readShort();
        this.syncExtension = buf.readDouble();
        if (this.syncExtension > 0 && this.syncExtension < this.height) {
            this.sync = 3;
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.extension = nbt.getDouble("extension");
        this.isExtending = nbt.getBoolean("isExtending");
        this.height = nbt.getInteger("height");
        this.renderPlatform = nbt.getBoolean("renderPlatform");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setDouble("extension", this.extension);
        nbt.setBoolean("isExtending", this.isExtending);
        nbt.setInteger("height", this.height);
        nbt.setBoolean("renderPlatform", this.renderPlatform);
        return super.writeToNBT(nbt);
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        int renderHeight = 1 + this.height;
        if (bb == null || bb.maxY - bb.minY < renderHeight) {
            bb = new AxisAlignedBB(
                    pos.getX() - 1,
                    pos.getY(),
                    pos.getZ() - 1,
                    pos.getX() + 2,
                    pos.getY() + renderHeight,
                    pos.getZ() + 2
            );
        }
        return bb;
    }
}
