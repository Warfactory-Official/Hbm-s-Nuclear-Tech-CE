package com.hbm.tileentity.machine.oil;

import com.hbm.api.energymk2.IEnergyProviderMK2;
import com.hbm.api.fluid.IFluidStandardReceiver;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.container.ContainerMachineGasFlare;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.fluid.trait.FT_Flammable;
import com.hbm.inventory.fluid.trait.FT_Polluting;
import com.hbm.inventory.fluid.trait.FluidTrait;
import com.hbm.inventory.fluid.trait.FluidTraitSimple;
import com.hbm.inventory.gui.GUIMachineGasFlare;
import com.hbm.items.machine.ItemMachineUpgrade.UpgradeType;
import com.hbm.lib.DirPos;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IFluidCopiable;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.ParticleUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@AutoRegister
public class TileEntityMachineGasFlare extends TileEntityMachineBase implements ITickable, IEnergyProviderMK2, IFluidStandardReceiver, IGUIProvider, IControlReceiver, IFluidCopiable {
    public static final long maxPower = 1000000;
    private final UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);
    public long power;
    public Fluid tankType;
    public FluidTankNTM tank;
    public boolean isOn = false;
    public boolean doesBurn = false;
    protected int fluidUsed = 0;
    protected int output = 0;

    public TileEntityMachineGasFlare() {
        super(6, true, true);
        tankType = Fluids.GAS.getFF();
        ;
        tank = new FluidTankNTM(Fluids.GAS, 64000);
    }

    @Override
    public String getDefaultName() {
        return "container.gasFlare";
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.power = nbt.getLong("powerTime");
        tank.readFromNBT(nbt, "gas");
        isOn = nbt.getBoolean("isOn");
        doesBurn = nbt.getBoolean("doesBurn");
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setLong("powerTime", power);
        tank.writeToNBT(nbt, "gas");
        nbt.setBoolean("isOn", isOn);
        nbt.setBoolean("doesBurn", doesBurn);
        return nbt;
    }

    public long getPowerScaled(long i) {
        return (power * i) / maxPower;
    }

    @Override
    public void update() {

        if (!world.isRemote) {

            this.fluidUsed = 0;
            this.output = 0;

            for (DirPos pos : getConPos()) {
                this.tryProvide(world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
                this.trySubscribe(tank.getTankType(), world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
            }

            tank.setType(3, inventory);
            tank.loadTank(1, 2, inventory);

            int maxVent = 50;
            int maxBurn = 10;

            if (isOn && tank.getFill() > 0) {
                upgradeManager.checkSlots(inventory, 4, 5);

                int burn = Math.min(upgradeManager.getLevel(UpgradeType.SPEED), 6);
                int yield = Math.min(upgradeManager.getLevel(UpgradeType.EFFECT), 6);

                maxVent += maxVent * burn;
                maxBurn += maxBurn * burn;

                if (!doesBurn || !(tank.getTankType().hasTrait(FT_Flammable.class))) {

                    if (tank.getTankType().hasTrait(FluidTraitSimple.FT_Gaseous.class) || tank.getTankType().hasTrait(FluidTraitSimple.FT_Gaseous_ART.class)) {
                        int eject = Math.min(maxVent, tank.getFill());
                        this.fluidUsed = eject;
                        tank.setFill(tank.getFill() - eject);
                        tank.getTankType().onFluidRelease(this, tank, eject);
                    }
                } else {

                    if (tank.getTankType().hasTrait(FT_Flammable.class)) {
                        int eject = Math.min(maxBurn, tank.getFill());
                        this.fluidUsed = eject;
                        tank.setFill(tank.getFill() - eject);

                        int penalty = 5;
                        if (!tank.getTankType().hasTrait(FluidTraitSimple.FT_Gaseous.class) && !tank.getTankType().hasTrait(FluidTraitSimple.FT_Gaseous_ART.class))
                            penalty = 10;

                        long powerProd = tank.getTankType().getTrait(FT_Flammable.class).getHeatEnergy() * eject / 1_000; // divided by 1000 per mB
                        powerProd /= penalty;
                        powerProd += powerProd * yield / 3;

                        this.output = (int) powerProd;
                        power += powerProd;

                        if (power > maxPower)
                            power = maxPower;

                        ParticleUtil.spawnGasFlame(world, pos.getX() + 0.5F, pos.getY() + 11.75F, pos.getZ() + 0.5F, world.rand.nextGaussian() * 0.15, 0.2, world.rand.nextGaussian() * 0.15);

                        if (this.world.getTotalWorldTime() % 5 == 0)
                            this.world.playSound(null, pos.getX(), pos.getY() + 11, pos.getZ(), HBMSoundHandler.flamethrowerShoot, SoundCategory.BLOCKS, 1.5F, 1F);

                        List<Entity> list = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos.add(-1, 12, -2), pos.add(2, 17, 2)));
                        for (Entity e : list) {
                            e.setFire(5);
                            e.attackEntityFrom(DamageSource.ON_FIRE, 5F);
                        }

                        if (world.getTotalWorldTime() % 3 == 0)
                            this.world.playSound(null, this.pos.getX(), this.pos.getY() + 11, this.pos.getZ(), HBMSoundHandler.flamethrowerShoot, SoundCategory.BLOCKS, getVolume(1.5F), 0.75F);

                        if (world.getTotalWorldTime() % 5 == 0 && eject > 0) {
                            FT_Polluting.pollute(world, pos.getX(), pos.getY(), pos.getZ(), tank.getTankType(), FluidTrait.FluidReleaseType.BURN, eject * 5);
                        }
                    }
                }
            }

            power = Library.chargeItemsFromTE(inventory, 0, power, maxPower);

            networkPackNT(50);

        } else {

            if (isOn && tank.getFill() > 0) {

                if ((!doesBurn || !(tank.getTankType().hasTrait(FT_Flammable.class))) && (tank.getTankType().hasTrait(FluidTraitSimple.FT_Gaseous.class) || tank.getTankType().hasTrait(FluidTraitSimple.FT_Gaseous_ART.class))) {

                    NBTTagCompound data = new NBTTagCompound();
                    data.setString("type", "tower");
                    data.setFloat("lift", 1F);
                    data.setFloat("base", 0.25F);
                    data.setFloat("max", 3F);
                    data.setInteger("life", 150 + world.rand.nextInt(20));
                    data.setInteger("color", tank.getTankType().getColor());

                    data.setDouble("posX", pos.getX() + 0.5);
                    data.setDouble("posZ", pos.getZ() + 0.5);
                    data.setDouble("posY", pos.getY() + 11);

                    MainRegistry.proxy.effectNT(data);

                }

                if (doesBurn && tank.getTankType().hasTrait(FT_Flammable.class) && MainRegistry.proxy.me().getDistanceSq(pos.getX(), pos.getY() + 10, pos.getZ()) <= 1024) {

                    NBTTagCompound data = new NBTTagCompound();
                    data.setString("type", "vanillaExt");
                    data.setString("mode", "smoke");
                    data.setBoolean("noclip", true);
                    data.setInteger("overrideAge", 50);

                    if (world.getTotalWorldTime() % 2 == 0) {
                        data.setDouble("posX", pos.getX() + 1.5);
                        data.setDouble("posZ", pos.getZ() + 1.5);
                        data.setDouble("posY", pos.getY() + 10.75);
                    } else {
                        data.setDouble("posX", pos.getX() + 1.125);
                        data.setDouble("posZ", pos.getZ() - 0.5);
                        data.setDouble("posY", pos.getY() + 11.75);
                    }

                    MainRegistry.proxy.effectNT(data);
                }
            }
        }
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(this.power);
        buf.writeBoolean(isOn);
        buf.writeBoolean(doesBurn);
        tank.serialize(buf);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        this.power = buf.readLong();
        this.isOn = buf.readBoolean();
        this.doesBurn = buf.readBoolean();
        tank.deserialize(buf);
    }

    @Override
    public int[] getAccessibleSlotsFromSide(EnumFacing e) {
        return new int[]{0, 1, 2, 3, 4, 5};
    }

    public DirPos[] getConPos() {
        return new DirPos[]{
                new DirPos(pos.getX() + 2, pos.getY(), pos.getZ(), Library.POS_X),
                new DirPos(pos.getX() - 2, pos.getY(), pos.getZ(), Library.NEG_X),
                new DirPos(pos.getX(), pos.getY(), pos.getZ() + 2, Library.POS_Z),
                new DirPos(pos.getX(), pos.getY(), pos.getZ() - 2, Library.NEG_Z)
        };
    }

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
    public FluidTankNTM[] getReceivingTanks() {
        return new FluidTankNTM[]{tank};
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return new FluidTankNTM[]{tank};
    }

    @Override
    public long getPower() {
        return power;
    }

    @Override
    public void setPower(long i) {
        power = i;
    }

    @Override
    public long getMaxPower() {
        return maxPower;
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerMachineGasFlare(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIMachineGasFlare(player.inventory, this);
    }

    @Override
    public boolean hasPermission(EntityPlayer player) {
        return player.getDistanceSq(pos) <= 256D;
    }

    @Override
    public void receiveControl(NBTTagCompound data) {
        if (data.hasKey("valve")) this.isOn = !this.isOn;
        if (data.hasKey("dial")) this.doesBurn = !this.doesBurn;
        markDirty();
    }

    @Override
    public NBTTagCompound getSettings(World world, int x, int y, int z) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setIntArray("fluidID", new int[]{tank.getTankType().getID()});
        tag.setBoolean("isOn", isOn);
        tag.setBoolean("doesBurn", doesBurn);
        return tag;
    }

    @Override
    public void pasteSettings(NBTTagCompound nbt, int index, World world, EntityPlayer player, int x, int y, int z) {
        int id = nbt.getIntArray("fluidID")[index];
        tank.setTankType(Fluids.fromID(id));
        if (nbt.hasKey("isOn")) isOn = nbt.getBoolean("isOn");
        if (nbt.hasKey("doesBurn")) doesBurn = nbt.getBoolean("doesBurn");
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        if (this.world.getTileEntity(this.pos) != this) return false;
        return player.getDistanceSq(this.pos.getX() + 0.5D, this.pos.getY() + 0.5D, this.pos.getZ() + 0.5D) <= 1024.0D;
    }
}
