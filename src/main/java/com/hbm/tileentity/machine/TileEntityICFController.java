package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.MachineICFController;
import com.hbm.capability.NTMEnergyCapabilityWrapper;
import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.TileEntityTickingBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@AutoRegister
public class TileEntityICFController extends TileEntityTickingBase implements IEnergyReceiverMK2 {

    private final List<BlockPos> ports = new ArrayList<>();
    public long power;
    public int laserLength;
    public boolean assembled;
    private int cellCount;
    private int emitterCount;
    private int capacitorCount;
    private int turbochargerCount;
    private AxisAlignedBB bb = null;
    private long powerSync;

    public void setup(HashSet<BlockPos> ports, HashSet<BlockPos> cells, HashSet<BlockPos> emitters, HashSet<BlockPos> capacitors,
                      HashSet<BlockPos> turbochargers) {

        this.cellCount = 0;
        this.emitterCount = 0;
        this.capacitorCount = 0;
        this.turbochargerCount = 0;

        IBlockState controllerState = this.world.getBlockState(this.pos);
        if (!(controllerState.getBlock() instanceof MachineICFController)) return;
        EnumFacing structureDirection = controllerState.getValue(MachineICFController.FACING).getOpposite();
        HashSet<BlockPos> validCells = new HashSet<>();
        HashSet<BlockPos> validEmitters = new HashSet<>();
        HashSet<BlockPos> validCapacitors = new HashSet<>();

        for (int i = 1; i <= cells.size(); i++) {
            BlockPos currentCellPos = this.pos.offset(structureDirection, i);
            if (cells.contains(currentCellPos)) {
                this.cellCount++;
                validCells.add(currentCellPos);
            } else {
                break;
            }
        }

        // Check for emitters adjacent to valid cells
        for (BlockPos emitterPos : emitters) {
            for (EnumFacing facing : EnumFacing.VALUES) {
                if (validCells.contains(emitterPos.offset(facing))) {
                    this.emitterCount++;
                    validEmitters.add(emitterPos);
                    break; // Emitter is valid, move to the next one
                }
            }
        }
        for (BlockPos capacitorPos : capacitors) {
            for (EnumFacing facing : EnumFacing.VALUES) {
                if (validEmitters.contains(capacitorPos.offset(facing))) {
                    this.capacitorCount++;
                    validCapacitors.add(capacitorPos);
                    break;
                }
            }
        }
        for (BlockPos turboPos : turbochargers) {
            for (EnumFacing facing : EnumFacing.VALUES) {
                if (validCapacitors.contains(turboPos.offset(facing))) {
                    this.turbochargerCount++;
                    break;
                }
            }
        }
        this.ports.clear();
        this.ports.addAll(ports);
        this.markDirty();
    }

    @Override
    public String getInventoryName() {
        return "container.icfController";
    }

    @Override
    public void update() {

        if (!world.isRemote) {

            this.networkPackNT(50);

            if (this.assembled) {
                for (BlockPos pos : ports) {
                    for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                        BlockPos portPos = pos.offset(dir.toEnumFacing());
                        if (this.getMaxPower() > 0) this.trySubscribe(world, portPos.getX(), portPos.getY(), portPos.getZ(), dir);
                    }
                }

                if (this.power > 0) {

                    ForgeDirection dir = ForgeDirection.getOrientation(world.getBlockState(this.pos).getValue(MachineICFController.FACING));

                    for (int i = 1; i < 50; i++) {
                        this.laserLength = i;

                        IBlockState b = world.getBlockState(getPos().add(dir.offsetX * i, 0, dir.offsetZ * i));
                        if (b.getBlock() == ModBlocks.icf) {
                            TileEntity tile = world.getTileEntity(getPos().add(dir.offsetX * (i + 8), -3, dir.offsetZ * (i + 8)));
                            if (tile instanceof TileEntityICF icf) {
                                icf.laser += this.getPower();
                                icf.maxLaser += this.getMaxPower();
                                break;
                            }
                        }

                        if (!b.getBlock().isAir(b, world, getPos().add(dir.offsetX * i, 0, dir.offsetZ * i))) {
                            float hardness = b.getBlock().getExplosionResistance(null);
                            if (hardness < 6000) world.destroyBlock(getPos().add(dir.offsetX * i, 0, dir.offsetZ * i), false);
                            break;
                        }
                    }
                    int xCoord = getPos().getX(), yCoord = getPos().getY(), zCoord = getPos().getZ();
                    double blx = Math.min(xCoord, xCoord + dir.offsetX * laserLength) + 0.2;
                    double bux = Math.max(xCoord, xCoord + dir.offsetX * laserLength) + 0.8;
                    double bly = Math.min(yCoord, yCoord + dir.offsetY * laserLength) + 0.2;
                    double buy = Math.max(yCoord, yCoord + dir.offsetY * laserLength) + 0.8;
                    double blz = Math.min(zCoord, zCoord + dir.offsetZ * laserLength) + 0.2;
                    double buz = Math.max(zCoord, zCoord + dir.offsetZ * laserLength) + 0.8;

                    List<Entity> list = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(blx, bly, blz, bux, buy, buz));

                    for (Entity e : list) {
                        e.attackEntityFrom(DamageSource.IN_FIRE, 50);
                        e.setFire(5);
                    }
                    powerSync = power;
                    this.setPower(0);
                } else {
                    this.laserLength = 0;
                }

            } else {
                this.laserLength = 0;
            }
        } else {
            if (this.laserLength > 0 && world.rand.nextInt(5) == 0) {
                ForgeDirection dir = ForgeDirection.getOrientation(world.getBlockState(this.pos).getValue(MachineICFController.FACING));
                ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
                double offXZ = world.rand.nextDouble() * 0.25 - 0.125;
                double offY = world.rand.nextDouble() * 0.25 - 0.125;
                double dist = 0.55;
                int xCoord = getPos().getX(), yCoord = getPos().getY(), zCoord = getPos().getZ();
                world.spawnParticle(EnumParticleTypes.REDSTONE, xCoord + 0.5 + dir.offsetX * dist + rot.offsetX * offXZ, yCoord + 0.5 + offY,
                        zCoord + 0.5 + dir.offsetZ * dist + rot.offsetZ * offXZ, 0, 0, 0);
            }
        }
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(powerSync);
        buf.writeInt(capacitorCount);
        buf.writeInt(turbochargerCount);
        buf.writeInt(laserLength);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        this.power = buf.readLong();
        this.capacitorCount = buf.readInt();
        this.turbochargerCount = buf.readInt();
        this.laserLength = buf.readInt();
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        this.power = nbt.getLong("power");

        this.assembled = nbt.getBoolean("assembled");
        this.cellCount = nbt.getInteger("cellCount");
        this.emitterCount = nbt.getInteger("emitterCount");
        this.capacitorCount = nbt.getInteger("capacitorCount");
        this.turbochargerCount = nbt.getInteger("turbochargerCount");

        ports.clear();
        int portCount = nbt.getInteger("portCount");
        for (int i = 0; i < portCount; i++) {
            int[] port = nbt.getIntArray("p" + i);
            ports.add(new BlockPos(port[0], port[1], port[2]));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        nbt.setLong("power", power);

        nbt.setBoolean("assembled", assembled);
        nbt.setInteger("cellCount", cellCount);
        nbt.setInteger("emitterCount", emitterCount);
        nbt.setInteger("capacitorCount", capacitorCount);
        nbt.setInteger("turbochargerCount", turbochargerCount);

        nbt.setInteger("portCount", ports.size());
        for (int i = 0; i < ports.size(); i++) {
            BlockPos pos = ports.get(i);
            nbt.setIntArray("p" + i, new int[]{pos.getX(), pos.getY(), pos.getZ()});
        }
        return nbt;
    }

    @Override
    public long getPower() {
        return Math.min(power, this.getMaxPower());
    }

    @Override
    public void setPower(long power) {
        this.power = power;
    }

    @Override
    public long getMaxPower() {
        return (long) (Math.sqrt(capacitorCount) * 2_500_000 + Math.sqrt(Math.min(turbochargerCount, capacitorCount)) * 5_000_000);
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        if (bb == null) {
            int xCoord = getPos().getX(), yCoord = getPos().getY(), zCoord = getPos().getZ();
            bb = new AxisAlignedBB(xCoord + 0.5 - 50, yCoord, zCoord + 0.5 - 50, xCoord + 0.5 + 50, yCoord + 1, zCoord + 0.5 + 50);
        }
        return bb;
    }

    @Override
    public boolean hasCapability(@NotNull Capability<?> capability, EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(@NotNull Capability<T> capability, EnumFacing facing) {
        if (capability == CapabilityEnergy.ENERGY) {
            return CapabilityEnergy.ENERGY.cast(
                    new NTMEnergyCapabilityWrapper(this)
            );
        }
        return super.getCapability(capability, facing);
    }
}
