package com.hbm.tileentity.machine.storage;

import com.hbm.api.energymk2.IBatteryItem;
import com.hbm.api.redstoneoverradio.IRORInteractive;
import com.hbm.api.redstoneoverradio.IRORValueProvider;
import com.hbm.entity.projectile.EntityBulletBeamBase;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.EntityProcessorCrossSmooth;
import com.hbm.explosion.vanillant.standard.ExplosionEffectStandard;
import com.hbm.explosion.vanillant.standard.PlayerProcessorStandard;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerBatterySocket;
import com.hbm.inventory.gui.GUIBatterySocket;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemBatterySC;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.util.*;
import io.netty.buffer.ByteBuf;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;

@AutoRegister
public class TileEntityBatterySocket extends TileEntityBatteryBase implements IRORValueProvider, IRORInteractive {

    public static BulletConfig discharge;
    public static BiConsumer<EntityBulletBeamBase, RayTraceResult> BEAM_DISCHARGE_HIT = (beam, mop) -> {

        if(mop.typeOfHit == mop.typeOfHit.BLOCK) {
            beam.world.destroyBlock(mop.getBlockPos(), false);
            explodeDischarge(beam.world, mop.hitVec.x, mop.hitVec.y, mop.hitVec.z);
        }

        if(mop.typeOfHit == mop.typeOfHit.ENTITY) {
            explodeDischarge(beam.world, mop.hitVec.x, mop.hitVec.y, mop.hitVec.z);
        }
    };

    public static void explodeDischarge(World world, double x, double y, double z) {
        ExplosionVNT vnt = new ExplosionVNT(world, x, y, z, 5F);
        vnt.setEntityProcessor(new EntityProcessorCrossSmooth(1, 20).setDamageClass(DamageResistanceHandler.DamageClass.ELECTRIC));
        vnt.setPlayerProcessor(new PlayerProcessorStandard());
        vnt.setSFX(new ExplosionEffectStandard());
        vnt.explode();
        world.playSound(null, x, y, z, HBMSoundHandler.ufoBlast, SoundCategory.BLOCKS, 5.0F, 0.9F + world.rand.nextFloat() * 0.2F);
    }

    static {
        discharge = new BulletConfig().setupDamageClass(DamageResistanceHandler.DamageClass.ELECTRIC).setBeam().setSpread(0.0F).setLife(3).setThresholdNegation(20F).setArmorPiercing(0.5F).setRenderRotations(false).setDoesPenetrate(true)
                .setOnBeamImpact(BEAM_DISCHARGE_HIT);
    }

    public long[] log = new long[20];
    public long delta = 0;
    public int damageTimer;
    public int damageTarget;
    public double scPowerMult = 1D;

    public ItemStack syncStack;

    public TileEntityBatterySocket() {
        super(1);
    }

    @Override
    public String getDefaultName() {
        return "container.batterySocket";
    }

    @Override
    public void update() {
        long prevPower = this.getPower();

        super.update();
        if (!world.isRemote) {

            if(hasSCLoaded()) {
                if(this.damageTarget == 0) pickNewSCTarget();
                this.damageTimer++;
                if(this.damageTimer >= this.damageTarget) discharge();
                fluctuate();
            }

            long avg = (this.getPower() + prevPower) / 2;
            this.delta = avg - this.log[0];

            for (int i = 1; i < this.log.length; i++) {
                this.log[i - 1] = this.log[i];
            }

            this.log[19] = avg;
        }
    }

    protected boolean hasSCLoaded() {
        return !inventory.getStackInSlot(0).isEmpty() && inventory.getStackInSlot(0).getItem() == ModItems.battery_sc && inventory.getStackInSlot(0).getItemDamage() != ItemBatterySC.EnumBatterySC.EMPTY.ordinal();
    }

    protected void pickNewSCTarget() {
        this.damageTimer = 0;
        this.damageTarget = 1200 + world.rand.nextInt(2400); // 1-3 minutes;
        this.markChanged();
    }

    protected void discharge() {
        pickNewSCTarget();

        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

        double x = pos.getX() + 0.5 - dir.offsetX * 0.5 + rot.offsetX * 0.5;
        double y = pos.getY() + 1;
        double z = pos.getZ() + 0.5 - dir.offsetZ * 0.5 + rot.offsetX * 0.5;

        double range = 15;
        List<EntityLivingBase> potentialTargets = world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(x, y, z, x, y, z).expand(range, range, range));
        Collections.shuffle(potentialTargets);

        for(EntityLivingBase target : potentialTargets) {

            Vec3NT initialDelta = new Vec3NT(target.posX - x, target.posY + target.height / 2 - y, target.posZ - z);
            if(initialDelta.length() > range) continue;
            EntityBulletBeamBase sub = new EntityBulletBeamBase(world, discharge, 50F);
            initialDelta.normalizeSelf();
            double dominantAxis = BobMathUtil.max(Math.abs(initialDelta.x), Math.abs(initialDelta.y), Math.abs(initialDelta.z));
            initialDelta.multiply(1.125D / dominantAxis); // move 1.125 blocks outwards
            sub.setPosition(pos.getX() + initialDelta.x, pos.getY() + initialDelta.y, pos.getZ() + initialDelta.z);
            Vec3NT actualDelta = new Vec3NT(target.posX - sub.posX, target.posY + target.height / 2 - sub.posY, target.posZ - sub.posZ);

            sub.setRotationsFromVector(actualDelta);
            sub.performHitscanExternal(actualDelta.length());
            world.spawnEntity(sub);
        }

        explodeDischarge(world, x + world.rand.nextGaussian() * 0.5, y + world.rand.nextGaussian() * 0.5, z + world.rand.nextGaussian() * 0.5);
    }

    protected void fluctuate() {
        double steppy = 1D / 100D;
        this.scPowerMult += (steppy * (world.rand.nextDouble() * 2 - 1));
        this.scPowerMult = MathHelper.clamp(scPowerMult, 0.1D, 1D);
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(delta);
        BufferUtil.writeItemStack(buf, this.inventory.getStackInSlot(0));
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        delta = buf.readLong();
        this.syncStack = BufferUtil.readItemStack(buf);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        this.damageTimer = nbt.getInteger("damageTimer");
        this.damageTarget = nbt.getInteger("damageTarget");
        this.scPowerMult = nbt.getDouble("scPowerMult");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setInteger("damageTimer", damageTimer);
        nbt.setInteger("damageTarget", damageTarget);
        nbt.setDouble("scPowerMult", scPowerMult);
        return super.writeToNBT(nbt);
    }

    @Override
    public boolean canExtractItem(int i, ItemStack stack, int j) {
        if (stack.getItem() instanceof IBatteryItem) {
            if (i == mode_input && ((IBatteryItem) stack.getItem()).getCharge(stack) == 0) return true;
            return i == mode_output && ((IBatteryItem) stack.getItem()).getCharge(stack) == ((IBatteryItem) stack.getItem()).getMaxCharge(stack);
        }
        return false;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(EnumFacing side) {
        return new int[]{0};
    }

    @Override
    public long getPower() {
        return powerFromStack(this.inventory.getStackInSlot(0));
    }

    @Override
    public long getMaxPower() {
        return maxPowerFromStack(this.inventory.getStackInSlot(0));
    }

    @Override
    public void setPower(long power) {
        if (inventory.getStackInSlot(0).isEmpty() || !(inventory.getStackInSlot(0).getItem() instanceof IBatteryItem))
            return;
        ((IBatteryItem) inventory.getStackInSlot(0).getItem()).setCharge(inventory.getStackInSlot(0), power);
    }

    public static long powerFromStack(ItemStack stack) {
        if (stack == null || !(stack.getItem() instanceof IBatteryItem)) return 0;
        return ((IBatteryItem) stack.getItem()).getCharge(stack);
    }

    public static long maxPowerFromStack(ItemStack stack) {
        if (stack == null || !(stack.getItem() instanceof IBatteryItem)) return 0;
        return ((IBatteryItem) stack.getItem()).getMaxCharge(stack);
    }

    @Override
    public long getProviderSpeed() {
        if (inventory.getStackInSlot(0).isEmpty() || !(inventory.getStackInSlot(0).getItem() instanceof IBatteryItem))
            return 0;
        int mode = this.getRelevantMode(true);
        return mode == mode_output || mode == mode_buffer ? ((IBatteryItem) inventory.getStackInSlot(0).getItem()).getDischargeRate(inventory.getStackInSlot(0)) : 0;
    }

    @Override
    public long getReceiverSpeed() {
        if (inventory.getStackInSlot(0).isEmpty() || !(inventory.getStackInSlot(0).getItem() instanceof IBatteryItem))
            return 0;
        int mode = this.getRelevantMode(true);
        return mode == mode_input || mode == mode_buffer ? ((IBatteryItem) inventory.getStackInSlot(0).getItem()).getChargeRate(inventory.getStackInSlot(0)) : 0;
    }

    @Override
    public BlockPos[] getPortPos() {
        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
        return new BlockPos[]{new BlockPos(pos.getX(), pos.getY(), pos.getZ()), new BlockPos(pos.getX() - dir.offsetX, pos.getY(), pos.getZ() - dir.offsetZ), new BlockPos(pos.getX() + rot.offsetX, pos.getY(), pos.getZ() + rot.offsetZ), new BlockPos(pos.getX() - dir.offsetX + rot.offsetX, pos.getY(), pos.getZ() - dir.offsetZ + rot.offsetZ)};
    }

    @Override
    public DirPos[] getConPos() {
        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);
        return new DirPos[]{new DirPos(pos.getX() + dir.offsetX, pos.getY(), pos.getZ() + dir.offsetZ, dir), new DirPos(pos.getX() + dir.offsetX + rot.offsetX, pos.getY(), pos.getZ() + dir.offsetZ + rot.offsetZ, dir),

                new DirPos(pos.getX() - dir.offsetX * 2, pos.getY(), pos.getZ() - dir.offsetZ * 2, dir.getOpposite()), new DirPos(pos.getX() - dir.offsetX * 2 + rot.offsetX, pos.getY(), pos.getZ() - dir.offsetZ * 2 + rot.offsetZ, dir.getOpposite()),

                new DirPos(pos.getX() + rot.offsetX * 2, pos.getY(), pos.getZ() + rot.offsetZ * 2, rot), new DirPos(pos.getX() + rot.offsetX * 2 - dir.offsetX, pos.getY(), pos.getZ() + rot.offsetZ * 2 - dir.offsetZ, rot),

                new DirPos(pos.getX() - rot.offsetX, pos.getY(), pos.getZ() - rot.offsetZ, rot.getOpposite()), new DirPos(pos.getX() - rot.offsetX - dir.offsetX, pos.getY(), pos.getZ() - rot.offsetZ - dir.offsetZ, rot.getOpposite())};
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerBatterySocket(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIBatterySocket(player.inventory, this);
    }

    AxisAlignedBB bb = null;

    @Override
    public AxisAlignedBB getRenderBoundingBox() {

        if (bb == null) {
            bb = new AxisAlignedBB(pos.getX() - 1, pos.getY(), pos.getZ() - 1, pos.getX() + 2, pos.getY() + 2, pos.getZ() + 2);
        }

        return bb;
    }

    @Override
    public String[] getFunctionInfo() {
        return new String[]{PREFIX_VALUE + "fill", PREFIX_VALUE + "fillpercent", PREFIX_VALUE + "delta", PREFIX_FUNCTION + "setmode" + NAME_SEPARATOR + "mode (0-3)", PREFIX_FUNCTION + "setmode" + NAME_SEPARATOR + "mode" + PARAM_SEPARATOR + "fallback (0-3)", PREFIX_FUNCTION + "setredmode" + NAME_SEPARATOR + "mode (0-3)", PREFIX_FUNCTION + "setredmode" + NAME_SEPARATOR + "mode" + PARAM_SEPARATOR + "fallback (0-3)", PREFIX_FUNCTION + "setpriority" + NAME_SEPARATOR + "priority (0-2)",};
    }

    @Override
    public String provideRORValue(String name) {
        if ((PREFIX_VALUE + "fill").equals(name)) return "" + this.getPower();
        if ((PREFIX_VALUE + "fillpercent").equals(name))
            return "" + this.getPower() * 100 / (Math.max(this.getMaxPower(), 1));
        if ((PREFIX_VALUE + "delta").equals(name)) return "" + delta;
        return null;
    }

    @Override
    public String runRORFunction(String name, String[] params) {

        if ((PREFIX_FUNCTION + "setmode").equals(name) && params.length > 0) {
            int mode = IRORInteractive.parseInt(params[0], 0, 3);

            if (mode != this.redLow) {
                this.redLow = (short) mode;
                this.markChanged();
                return null;
            } else if (params.length > 1) {
                int altmode = IRORInteractive.parseInt(params[1], 0, 3);
                this.redLow = (short) altmode;
                this.markChanged();
                return null;
            }
            return null;
        }

        if ((PREFIX_FUNCTION + "setredmode").equals(name) && params.length > 0) {
            int mode = IRORInteractive.parseInt(params[0], 0, 3);

            if (mode != this.redHigh) {
                this.redHigh = (short) mode;
                this.markChanged();
                return null;
            } else if (params.length > 1) {
                int altmode = IRORInteractive.parseInt(params[1], 0, 3);
                this.redHigh = (short) altmode;
                this.markChanged();
                return null;
            }
            return null;
        }

        if ((PREFIX_FUNCTION + "setpriority").equals(name) && params.length > 0) {
            int priority = IRORInteractive.parseInt(params[0], 0, 2) + 1;
            this.priority = EnumUtil.grabEnumSafely(ConnectionPriority.values(), priority);
            this.markChanged();
            return null;
        }
        return null;
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getEnergyInfo(Context context, Arguments args) {
        return new Object[]{getPower(), getMaxPower(), this.delta};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getPackInfo(Context context, Arguments args) {
        if (inventory.getStackInSlot(0).isEmpty() || !(inventory.getStackInSlot(0).getItem() instanceof IBatteryItem bat))
            return new Object[]{"", 0, 0};
        return new Object[]{inventory.getStackInSlot(0).getTranslationKey(), bat.getChargeRate(inventory.getStackInSlot(0)), bat.getDischargeRate(inventory.getStackInSlot(0))};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getInfo(Context context, Arguments args) {
        Object[] energyInfo = getEnergyInfo(context, args);
        Object[] packInfo = getPackInfo(context, args);
        Object[] modeInfo = getModeInfo(context, args);
        return new Object[]{energyInfo[0], energyInfo[1], energyInfo[2], modeInfo[0], modeInfo[1], modeInfo[2], packInfo[0], packInfo[1], packInfo[2]};
    }

    @Override
    @Optional.Method(modid = "opencomputers")
    public String[] methods() {
        return new String[]{"getEnergyInfo", "getPackInfo", "getModeInfo", "setModeLow", "setModeHigh", "setPriority", "getInfo"};
    }

    @Override
    @Optional.Method(modid = "opencomputers")
    public Object[] invoke(String method, Context context, Arguments args) throws Exception {
        return switch (method) {
            case "getEnergyInfo" -> getEnergyInfo(context, args);
            case "getPackInfo" -> getPackInfo(context, args);
            case "getModeInfo" -> getModeInfo(context, args);
            case "setModeLow" -> setModeLow(context, args);
            case "setModeHigh" -> setModeHigh(context, args);
            case "setPriority" -> setPriority(context, args);
            case "getInfo" -> getInfo(context, args);
            default -> throw new NoSuchMethodException();
        };
    }
}
