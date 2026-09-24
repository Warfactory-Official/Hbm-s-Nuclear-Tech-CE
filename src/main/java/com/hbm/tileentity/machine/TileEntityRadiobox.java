package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.blocks.machine.Radiobox;
import com.hbm.capability.NTMEnergyCapabilityWrapper;
import com.hbm.entity.mob.EntityFBI;
import com.hbm.entity.mob.EntityFBIDrone;
import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.ModDamageSource;
import com.hbm.tileentity.TileEntityLoadedBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@AutoRegister
public class TileEntityRadiobox extends TileEntityLoadedBase implements ITickable, IEnergyReceiverMK2 {

    long power;
    public static long maxPower = 500000;
    public boolean infinite = false;

    @ParametersAreNonnullByDefault
    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newState) {
        return oldState.getBlock() != newState.getBlock();
    }

    @Override
    public void update() {
        if (world.isRemote) {
            return;
        }

        IBlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof Radiobox)) return;

        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            this.trySubscribe(world, pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ, dir);
        }

        if (state.getValue(Radiobox.STATE) && (power >= 25000 || infinite)) {
            if (!infinite) {
                power -= 25000;
                this.markDirty();
            }
            int range = 15;

            List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(pos.getX() - range, pos.getY() - range, pos.getZ() - range, pos.getX() + range, pos.getY() + range, pos.getZ() + range));

            for (EntityLivingBase entity : entities) {
                if (entity instanceof EntityFBI || entity instanceof EntityFBIDrone || entity instanceof EntityAnimal || entity instanceof EntityPlayer) continue;

                entity.attackEntityFrom(ModDamageSource.enervation, 20.0F);
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        power = compound.getLong("power");
        infinite = compound.getBoolean("infinite");
        super.readFromNBT(compound);
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setLong("power", power);
        compound.setBoolean("infinite", infinite);
        return super.writeToNBT(compound);
    }

    @Override
    public void setPower(long i) {
        power = i;
    }

    @Override
    public long getPower() {
        return power;
    }

    @Override
    public long getMaxPower() {
        return maxPower;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
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
            return CapabilityEnergy.ENERGY.cast(new NTMEnergyCapabilityWrapper(this));
        }
        return super.getCapability(capability, facing);
    }
}
