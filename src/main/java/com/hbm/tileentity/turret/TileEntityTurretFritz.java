package com.hbm.tileentity.turret;

import com.hbm.api.fluid.IFluidStandardReceiver;
import com.hbm.blocks.BlockDummyable;
import com.hbm.capability.NTMFluidHandlerWrapper;
import com.hbm.entity.projectile.EntityBulletBaseMK4;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IFFtoNTMF;
import com.hbm.inventory.container.ContainerTurretBase;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.fluid.trait.FT_Combustible;
import com.hbm.inventory.fluid.trait.FT_Flammable;
import com.hbm.inventory.fluid.trait.FluidTraitSimple;
import com.hbm.inventory.gui.GUITurretFritz;
import com.hbm.items.ModItems;
import com.hbm.items.weapon.sedna.factory.GunFactory;
import com.hbm.items.weapon.sedna.factory.XFactoryFlamer;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.util.Vec3NT;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@AutoRegister
public class TileEntityTurretFritz extends TileEntityTurretBaseNT implements IFluidStandardReceiver, IFluidCopiable, IFFtoNTMF, IGUIProvider {

    public static int drain = 2;
    private static boolean converted = false;
    public FluidTankNTM tank;

    public TileEntityTurretFritz() {
        super();
        this.tank = new FluidTankNTM(Fluids.DIESEL, 16000);
    }

    @Override
    public String getDefaultName() {
        return "container.turretFritz";
    }

    @Override
    protected List<Integer> getAmmoList() {
        return null;
    }

    @SideOnly(Side.CLIENT)
    public List<ItemStack> getAmmoTypesForDisplay() {

        if (ammoStacks != null)
            return ammoStacks;

        ammoStacks = new ArrayList();

        ammoStacks.add(new ItemStack(ModItems.ammo_standard, 1, GunFactory.EnumAmmo.FLAME_DIESEL.ordinal()));

        for (FluidType type : Fluids.getInNiceOrder()) {
            if (type.hasTrait(FT_Combustible.class) && type.hasTrait(FluidTraitSimple.FT_Liquid.class)) {
                ammoStacks.add(new ItemStack(ModItems.fluid_icon, 1, type.getID()));
            }
        }

        return ammoStacks;
    }


    @Override
    public double getDecetorRange() {
        return 32D;
    }

    @Override
    public double getDecetorGrace() {
        return 2D;
    }

    @Override
    public double getTurretElevation() {
        return 45D;
    }

    @Override
    public long getMaxPower() {
        return 10000;
    }

    @Override
    public double getBarrelLength() {
        return 2.25D;
    }

    @Override
    public double getAcceptableInaccuracy() {
        return 15;
    }

    @Override
    public void updateFiringTick() {
        if (this.tank.getTankType().hasTrait(FT_Flammable.class) && this.tank.getTankType().hasTrait(FluidTraitSimple.FT_Liquid.class) && this.tank.getFill() >= 2) {

            FT_Flammable trait = this.tank.getTankType().getTrait(FT_Flammable.class);
            this.tank.setFill(this.tank.getFill() - 2);

            Vec3d turretPos = this.getTurretPos();

            Vec3NT muzzleOffset = new Vec3NT(this.getBarrelLength(), 0, 0);

            muzzleOffset.rotateAroundZRad((float) -this.rotationPitch);
            muzzleOffset.rotateAroundYRad((float) -(this.rotationYaw + Math.PI * 0.5));

            EntityBulletBaseMK4 proj = new EntityBulletBaseMK4(
                    world,
                    XFactoryFlamer.flame_nograv,
                    trait.getHeatEnergy() / 500_000F,
                    0.05F,
                    (float) rotationYaw,
                    (float) rotationPitch
            );

            double muzzleX = turretPos.x + muzzleOffset.x;
            double muzzleY = turretPos.y + muzzleOffset.y;
            double muzzleZ = turretPos.z + muzzleOffset.z;

            proj.setPositionAndRotation(muzzleX, muzzleY, muzzleZ, proj.rotationYaw, proj.rotationPitch);
            world.spawnEntity(proj);

            world.playSound(null, this.pos.getX(), this.pos.getY(), this.pos.getZ(),
                    HBMSoundHandler.flamethrowerShoot, SoundCategory.BLOCKS, 2F,
                    1F + world.rand.nextFloat() * 0.5F);

            NBTTagCompound data = new NBTTagCompound();
            data.setString("type", "vanillaburst");
            data.setString("mode", "flame");
            data.setInteger("count", 2);
            data.setDouble("motion", 0.025D);

            PacketThreading.createAllAroundThreadedPacket(
                    new AuxParticlePacketNT(data, muzzleX, muzzleY, muzzleZ),
                    new TargetPoint(world.provider.getDimension(), this.pos.getX(), this.pos.getY(), this.pos.getZ(), 50)
            );

        }

    }

    public int getDelay() {
        return 2;
    }

    @Override
    public void update() {
        super.update();
        if (!world.isRemote) {
            tank.setType(9, 9, inventory);


            for (int i = 1; i < 10; i++) {

                if (!inventory.getStackInSlot(i).isEmpty() && inventory.getStackInSlot(i).getItem() == ModItems.ammo_standard && inventory.getStackInSlot(i).getItemDamage() == GunFactory.EnumAmmo.FLAME_DIESEL.ordinal()) { //ammo_fuel
                    if (this.tank.getTankType() == Fluids.DIESEL && this.tank.getFill() + 1000 <= this.tank.getMaxFill()) {
                        this.tank.setFill(this.tank.getFill() + 1000);
                        this.inventory.getStackInSlot(i).shrink(1);
                    }
                }
            }
        }
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        tank.serialize(buf);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        tank.deserialize(buf);
    }

    @Override //TODO: clean this shit up
    protected void updateConnections() {
        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset).getOpposite();
        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

        this.trySubscribe(world, pos.getX() + dir.offsetX * -1 + rot.offsetX * 0, pos.getY(), pos.getZ() + dir.offsetZ * -1 + rot.offsetZ * 0, dir.getOpposite());
        this.trySubscribe(world, pos.getX() + dir.offsetX * -1 + rot.offsetX * -1, pos.getY(), pos.getZ() + dir.offsetZ * -1 + rot.offsetZ * -1, dir.getOpposite());
        this.trySubscribe(world, pos.getX() + dir.offsetX * 0 + rot.offsetX * -2, pos.getY(), pos.getZ() + dir.offsetZ * 0 + rot.offsetZ * -2, rot.getOpposite());
        this.trySubscribe(world, pos.getX() + dir.offsetX * 1 + rot.offsetX * -2, pos.getY(), pos.getZ() + dir.offsetZ * 1 + rot.offsetZ * -2, rot.getOpposite());
        this.trySubscribe(world, pos.getX() + dir.offsetX * 0 + rot.offsetX * 1, pos.getY(), pos.getZ() + dir.offsetZ * 0 + rot.offsetZ * 1, rot);
        this.trySubscribe(world, pos.getX() + dir.offsetX * 1 + rot.offsetX * 1, pos.getY(), pos.getZ() + dir.offsetZ * 1 + rot.offsetZ * 1, rot);
        this.trySubscribe(world, pos.getX() + dir.offsetX * 2 + rot.offsetX * 0, pos.getY(), pos.getZ() + dir.offsetZ * 2 + rot.offsetZ * 0, dir);
        this.trySubscribe(world, pos.getX() + dir.offsetX * 2 + rot.offsetX * -1, pos.getY(), pos.getZ() + dir.offsetZ * 2 + rot.offsetZ * -1, dir);

        this.trySubscribe(tank.getTankType(), world, pos.getX() + dir.offsetX * -1 + rot.offsetX * 0, pos.getY(), pos.getZ() + dir.offsetZ * -1 + rot.offsetZ * 0, dir.getOpposite());
        this.trySubscribe(tank.getTankType(), world, pos.getX() + dir.offsetX * -1 + rot.offsetX * -1, pos.getY(), pos.getZ() + dir.offsetZ * -1 + rot.offsetZ * -1, dir.getOpposite());
        this.trySubscribe(tank.getTankType(), world, pos.getX() + dir.offsetX * 0 + rot.offsetX * -2, pos.getY(), pos.getZ() + dir.offsetZ * 0 + rot.offsetZ * -2, rot.getOpposite());
        this.trySubscribe(tank.getTankType(), world, pos.getX() + dir.offsetX * 1 + rot.offsetX * -2, pos.getY(), pos.getZ() + dir.offsetZ * 1 + rot.offsetZ * -2, rot.getOpposite());
        this.trySubscribe(tank.getTankType(), world, pos.getX() + dir.offsetX * 0 + rot.offsetX * 1, pos.getY(), pos.getZ() + dir.offsetZ * 0 + rot.offsetZ * 1, rot);
        this.trySubscribe(tank.getTankType(), world, pos.getX() + dir.offsetX * 1 + rot.offsetX * 1, pos.getY(), pos.getZ() + dir.offsetZ * 1 + rot.offsetZ * 1, rot);
        this.trySubscribe(tank.getTankType(), world, pos.getX() + dir.offsetX * 2 + rot.offsetX * 0, pos.getY(), pos.getZ() + dir.offsetZ * 2 + rot.offsetZ * 0, dir);
        this.trySubscribe(tank.getTankType(), world, pos.getX() + dir.offsetX * 2 + rot.offsetX * -1, pos.getY(), pos.getZ() + dir.offsetZ * 2 + rot.offsetZ * -1, dir);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.tank.readFromNBT(nbt, "diesel");
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        this.tank.writeToNBT(nbt, "diesel");
        return super.writeToNBT(nbt);
    }

    @Override
    public int[] getAccessibleSlotsFromSide(EnumFacing e) {
        return new int[]{1, 2, 3, 4, 5, 6, 7, 8};
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return new FluidTankNTM[]{tank};
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return new FluidTankNTM[]{tank};
    }

    @Override
    public FluidTankNTM getTankToPaste() {
        return tank;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return true;
        }
        return super.hasCapability(capability, facing);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(
                    new NTMFluidHandlerWrapper(this)
            );
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerTurretBase(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUITurretFritz(player.inventory, this);
    }
}
