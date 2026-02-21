package com.hbm.tileentity.machine;

import com.hbm.api.fluid.IFluidStandardTransceiver;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.config.MobConfig;
import com.hbm.entity.projectile.EntityZirnoxDebris;
import com.hbm.entity.projectile.EntityZirnoxDebris.DebrisType;
import com.hbm.explosion.ExplosionNukeGeneric;
import com.hbm.handler.CompatHandler;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.RecipesCommon;
import com.hbm.inventory.container.ContainerReactorZirnox;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUIReactorZirnox;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemZirnoxRod;
import com.hbm.items.machine.ItemZirnoxRod.EnumZirnoxType;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.main.AdvancementManager;
import com.hbm.main.MainRegistry;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.EnumUtil;
import io.netty.buffer.ByteBuf;
import li.cil.oc.api.machine.Arguments;
import li.cil.oc.api.machine.Callback;
import li.cil.oc.api.machine.Context;
import li.cil.oc.api.network.SimpleComponent;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Optional;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.hbm.items.machine.ItemZirnoxRodDepleted.EnumZirnoxTypeDepleted;

@Optional.InterfaceList({@Optional.Interface(iface = "li.cil.oc.api.network.SimpleComponent", modid = "opencomputers")})
@AutoRegister
public class TileEntityReactorZirnox extends TileEntityMachineBase implements ITickable, IControlReceiver, IFluidStandardTransceiver, SimpleComponent, IGUIProvider, CompatHandler.OCComponent {

    public static final int maxHeat = 100000;
    private boolean redstonePowered = false;
    public static final int maxPressure = 100000;
    public static final HashMap<RecipesCommon.ComparableStack, ItemStack> fuelMap = new HashMap<RecipesCommon.ComparableStack, ItemStack>();
    private static final int[] slots_io = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23};

    static {
        fuelMap.put(new RecipesCommon.ComparableStack(ModItems.rod_zirnox, 1, EnumZirnoxType.NATURAL_URANIUM_FUEL.ordinal()), new ItemStack(ModItems.rod_zirnox_depleted, 1, EnumZirnoxTypeDepleted.NATURAL_URANIUM_FUEL.ordinal()));
        fuelMap.put(new RecipesCommon.ComparableStack(ModItems.rod_zirnox, 1, EnumZirnoxType.URANIUM_FUEL.ordinal()), new ItemStack(ModItems.rod_zirnox_depleted, 1, EnumZirnoxTypeDepleted.URANIUM_FUEL.ordinal()));
        fuelMap.put(new RecipesCommon.ComparableStack(ModItems.rod_zirnox, 1, EnumZirnoxType.TH232_FUEL.ordinal()), new ItemStack(ModItems.rod_zirnox, 1, EnumZirnoxType.THORIUM_FUEL.ordinal()));
        fuelMap.put(new RecipesCommon.ComparableStack(ModItems.rod_zirnox, 1, EnumZirnoxType.THORIUM_FUEL.ordinal()), new ItemStack(ModItems.rod_zirnox_depleted, 1, EnumZirnoxTypeDepleted.THORIUM_FUEL.ordinal()));
        fuelMap.put(new RecipesCommon.ComparableStack(ModItems.rod_zirnox, 1, EnumZirnoxType.MOX_FUEL.ordinal()), new ItemStack(ModItems.rod_zirnox_depleted, 1, EnumZirnoxTypeDepleted.MOX_FUEL.ordinal()));
        fuelMap.put(new RecipesCommon.ComparableStack(ModItems.rod_zirnox, 1, EnumZirnoxType.PLUTONIUM_FUEL.ordinal()), new ItemStack(ModItems.rod_zirnox_depleted, 1, EnumZirnoxTypeDepleted.PLUTONIUM_FUEL.ordinal()));
        fuelMap.put(new RecipesCommon.ComparableStack(ModItems.rod_zirnox, 1, EnumZirnoxType.U233_FUEL.ordinal()), new ItemStack(ModItems.rod_zirnox_depleted, 1, EnumZirnoxTypeDepleted.U233_FUEL.ordinal()));
        fuelMap.put(new RecipesCommon.ComparableStack(ModItems.rod_zirnox, 1, EnumZirnoxType.U235_FUEL.ordinal()), new ItemStack(ModItems.rod_zirnox_depleted, 1, EnumZirnoxTypeDepleted.U235_FUEL.ordinal()));
        fuelMap.put(new RecipesCommon.ComparableStack(ModItems.rod_zirnox, 1, EnumZirnoxType.LES_FUEL.ordinal()), new ItemStack(ModItems.rod_zirnox_depleted, 1, EnumZirnoxTypeDepleted.LES_FUEL.ordinal()));
        fuelMap.put(new RecipesCommon.ComparableStack(ModItems.rod_zirnox, 1, EnumZirnoxType.LITHIUM_FUEL.ordinal()), new ItemStack(ModItems.rod_zirnox_tritium));
        fuelMap.put(new RecipesCommon.ComparableStack(ModItems.rod_zirnox, 1, EnumZirnoxType.ZFB_MOX_FUEL.ordinal()), new ItemStack(ModItems.rod_zirnox_depleted, 1, EnumZirnoxTypeDepleted.ZFB_MOX_FUEL.ordinal()));
    }

    public int heat;
    public int pressure;
    public boolean isOn = false;
    public FluidTankNTM steam;
    public FluidTankNTM carbonDioxide;
    public FluidTankNTM water;
    protected int output;

    public TileEntityReactorZirnox() {
        super(28, true, false);
        steam = new FluidTankNTM(Fluids.SUPERHOTSTEAM, 8000);
        carbonDioxide = new FluidTankNTM(Fluids.CARBONDIOXIDE, 16000);
        water = new FluidTankNTM(Fluids.WATER, 32000);
    }
    public void setRedstonePowered(boolean powered) {
        this.redstonePowered = powered;
        if (!powered) {
            isOn = false;
        }
    }

    @Override
    public String getDefaultName() {
        return "container.zirnox";
    }

    @Override
    public int[] getAccessibleSlotsFromSide(EnumFacing e) {
        return slots_io;
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack stack) {
        return i < 24 && stack.getItem() instanceof ItemZirnoxRod;
    }

    @Override
    public boolean canExtractItem(int i, ItemStack stack, int j) {
        return i < 24 && !(stack.getItem() instanceof ItemZirnoxRod);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        heat = nbt.getInteger("heat");
        pressure = nbt.getInteger("pressure");
        isOn = nbt.getBoolean("isOn");
        steam.readFromNBT(nbt, "steam");
        carbonDioxide.readFromNBT(nbt, "carbondioxide");
        water.readFromNBT(nbt, "water");
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeInt(this.heat);
        buf.writeInt(this.pressure);
        buf.writeBoolean(this.isOn);
        steam.serialize(buf);
        carbonDioxide.serialize(buf);
        water.serialize(buf);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        this.heat = buf.readInt();
        this.pressure = buf.readInt();
        this.isOn = buf.readBoolean();
        steam.deserialize(buf);
        carbonDioxide.deserialize(buf);
        water.deserialize(buf);
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setInteger("heat", heat);
        nbt.setInteger("pressure", pressure);
        nbt.setBoolean("isOn", isOn);
        steam.writeToNBT(nbt, "steam");
        carbonDioxide.writeToNBT(nbt, "carbondioxide");
        water.writeToNBT(nbt, "water");
        return super.writeToNBT(nbt);
    }

    public int getGaugeScaled(int i, int type) {
        switch (type) {
            case 0:
                return (steam.getFill() * i) / steam.getMaxFill();
            case 1:
                return (carbonDioxide.getFill() * i) / carbonDioxide.getMaxFill();
            case 2:
                return (water.getFill() * i) / water.getMaxFill();
            case 3:
                return (this.heat * i) / maxHeat;
            case 4:
                return (this.pressure * i) / maxPressure;
            default:
                return 1;
        }
    }

    private int[] getNeighbouringSlots(int id) {

        switch (id) {
            case 0:
                return new int[]{1, 7};
            case 1:
                return new int[]{0, 2, 8};
            case 2:
                return new int[]{1, 9};
            case 3:
                return new int[]{4, 10};
            case 4:
                return new int[]{3, 5, 11};
            case 5:
                return new int[]{4, 6, 12};
            case 6:
                return new int[]{5, 13};
            case 7:
                return new int[]{0, 8, 14};
            case 8:
                return new int[]{1, 7, 9, 15};
            case 9:
                return new int[]{2, 8, 16};
            case 10:
                return new int[]{3, 11, 17};
            case 11:
                return new int[]{4, 10, 12, 18};
            case 12:
                return new int[]{5, 11, 13, 19};
            case 13:
                return new int[]{6, 12, 20};
            case 14:
                return new int[]{7, 15, 21};
            case 15:
                return new int[]{8, 14, 16, 22};
            case 16:
                return new int[]{9, 15, 23};
            case 17:
                return new int[]{10, 18};
            case 18:
                return new int[]{11, 17, 19};
            case 19:
                return new int[]{12, 18, 20};
            case 20:
                return new int[]{13, 19};
            case 21:
                return new int[]{14, 22};
            case 22:
                return new int[]{15, 21, 23};
            case 23:
                return new int[]{16, 22};
        }

        return null;
    }

    @Override
    public void update() {

        if (!world.isRemote) {
            if (redstonePowered) {
                isOn = true;
            }
            this.output = 0;

            if (world.getTotalWorldTime() % 20 == 0) {
                this.updateConnections();
            }

            carbonDioxide.loadTank(24, 26, inventory);
            water.loadTank(25, 27, inventory);

            if (isOn) {
                for (int i = 0; i < 24; i++) {

                    if (!inventory.getStackInSlot(i).isEmpty()) {
                        if (inventory.getStackInSlot(i).getItem() instanceof ItemZirnoxRod)
                            decay(i);
                        else if (inventory.getStackInSlot(i).getItem() == ModItems.meteorite_sword_bred)
                            inventory.setStackInSlot(i, new ItemStack(ModItems.meteorite_sword_irradiated));
                    }
                }
            }

            //2(fill) + (x * fill%)
            this.pressure = (this.carbonDioxide.getFill() * 2) + (int) ((float) this.heat * ((float) this.carbonDioxide.getFill() / (float) this.carbonDioxide.getMaxFill()));

            if (this.heat > 0 && this.heat < maxHeat) {
                if (this.water.getFill() > 0 && this.carbonDioxide.getFill() > 0 && this.steam.getFill() < this.steam.getMaxFill()) {
                    generateSteam();
                    //(x * pressure) / 1,000,000
                    this.heat -= (int) ((float) this.heat * (float) this.pressure / 1000000F);
                } else {
                    this.heat -= 10;
                }

            }

            for (DirPos pos : getConPos()) {
                this.sendFluid(steam, world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
            }

            checkIfMeltdown();

            this.networkPackNT(150);
        }
    }

    private void generateSteam() {

        // function of SHS produced per tick
        // (heat - 10256)/100000 * steamFill (max efficiency at 14b) * 25 * 5 (should get rid of any rounding errors)
        if (this.heat > 10256) {
            int cycle = (int) ((((float) heat - 10256F) / (float) maxHeat) * Math.min(((float) carbonDioxide.getFill() / 14000F), 1F) * 25F * 5F);
            this.output = cycle;

            water.setFill(water.getFill() - cycle);
            steam.setFill(steam.getFill() + cycle);

            if (water.getFill() < 0)
                water.setFill(0);

            if (steam.getFill() > steam.getMaxFill())
                steam.setFill(steam.getMaxFill());
        }
    }

    private boolean hasFuelRod(int id) {
        if (!inventory.getStackInSlot(id).isEmpty()) {
            if (inventory.getStackInSlot(id).getItem() instanceof ItemZirnoxRod) {
                final EnumZirnoxType num = EnumUtil.grabEnumSafely(EnumZirnoxType.VALUES, inventory.getStackInSlot(id).getItemDamage());
                return !num.breeding;
            }
        }

        return false;
    }

    private int getNeighbourCount(int id) {

        int[] neighbours = this.getNeighbouringSlots(id);

        if (neighbours == null)
            return 0;

        int count = 0;

        for (int i = 0; i < neighbours.length; i++)
            if (hasFuelRod(neighbours[i]))
                count++;

        return count;

    }

    // itemstack in slots[id] has to contain ItemZirnoxRod
    private void decay(int id) {
        int decay = getNeighbourCount(id);
        ItemStack zirnoxRodItemStack = inventory.getStackInSlot(id);
        final EnumZirnoxType num = EnumUtil.grabEnumSafely(EnumZirnoxType.VALUES, zirnoxRodItemStack.getItemDamage());

        if (!num.breeding)
            decay++;

        for (int i = 0; i < decay; i++) {
            this.heat += num.heat;
            ItemZirnoxRod.incrementLifeTime(zirnoxRodItemStack);

            if (ItemZirnoxRod.getLifeTime(zirnoxRodItemStack) > num.maxLife) {
                inventory.setStackInSlot(id, fuelMap.get(new RecipesCommon.ComparableStack(zirnoxRodItemStack)).copy());
                break;
            }
        }
    }

    private void checkIfMeltdown() {
        if (this.pressure > maxPressure || this.heat > maxHeat) {
            meltdown();
        }
    }

    private void spawnDebris(DebrisType type) {

        EntityZirnoxDebris debris = new EntityZirnoxDebris(world, pos.getX() + 0.5D, pos.getY() + 4D, pos.getZ() + 0.5D, type);
        debris.motionX = world.rand.nextGaussian() * 0.75D;
        debris.motionZ = world.rand.nextGaussian() * 0.75D;
        debris.motionY = 0.01D + world.rand.nextDouble() * 1.25D;

        if (type == DebrisType.CONCRETE) {
            debris.motionX *= 0.25D;
            debris.motionY += world.rand.nextDouble();
            debris.motionZ *= 0.25D;
        }

        if (type == DebrisType.EXCHANGER) {
            debris.motionX += 0.5D;
            debris.motionY *= 0.1D;
            debris.motionZ += 0.5D;
        }

        world.spawnEntity(debris);
    }

    private void zirnoxDebris() {

        for (int i = 0; i < 2; i++) {
            spawnDebris(DebrisType.EXCHANGER);
        }

        for (int i = 0; i < 20; i++) {
            spawnDebris(DebrisType.CONCRETE);
            spawnDebris(DebrisType.BLANK);
        }

        for (int i = 0; i < 10; i++) {
            spawnDebris(DebrisType.ELEMENT);
            spawnDebris(DebrisType.GRAPHITE);
            spawnDebris(DebrisType.SHRAPNEL);
        }

    }

    private void meltdown() { //FIXME: this doesnt work properly
        for (int i = 0; i < inventory.getSlots(); i++) {
            this.inventory.setStackInSlot(i, ItemStack.EMPTY);
        }
        NBTTagCompound data = new NBTTagCompound();
        data.setString("type", "rbmkmush");
        data.setFloat("scale", 4);
        PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(data, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5), new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 250));
        MainRegistry.proxy.effectNT(data);

        int meta = this.getBlockMetadata();
        for (int ox = -2; ox <= 2; ox++) {
            for (int oz = -2; oz <= 2; oz++) {
                for (int oy = 2; oy <= 5; oy++) {
                    world.setBlockToAir(pos.add(ox, oy, oz));
                }
            }
        }
        int[] dimensions = {1, 0, 2, 2, 2, 2};

        world.setBlockState(pos, ModBlocks.zirnox_destroyed.getStateFromMeta(meta), 3);
        MultiblockHandlerXR.fillSpace(world, pos.getX(), pos.getY(), pos.getZ(), dimensions, ModBlocks.zirnox_destroyed, ForgeDirection.getOrientation(meta - BlockDummyable.offset));

        world.playSound(null, pos.getX(), pos.getY() + 2, pos.getZ(), HBMSoundHandler.rbmk_explosion, SoundCategory.BLOCKS, 10.0F, 1.0F);
        world.createExplosion(null, pos.getX(), pos.getY() + 3, pos.getZ(), 12.0F, true);
        zirnoxDebris();
        ExplosionNukeGeneric.waste(world, pos.getX(), pos.getY(), pos.getZ(), 35);

        List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(pos).grow(100));
        for (EntityPlayer player : players) {
            AdvancementManager.grantAchievement(player, AdvancementManager.achZIRNOXBoom);
        }

        if (MobConfig.enableElementals) {
            for (EntityPlayer player : players) {
                player.getEntityData().getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG).setBoolean("radMark", true);
            }
        }
    }

    private void updateConnections() {
        for (DirPos pos : getConPos()) {
            this.trySubscribe(water.getTankType(), world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
            this.trySubscribe(carbonDioxide.getTankType(), world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
        }
    }

    private DirPos[] getConPos() {
        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

        return new DirPos[]{
                new DirPos(this.pos.getX() + rot.offsetX * 3, this.pos.getY() + 1, this.pos.getZ() + rot.offsetZ * 3, rot),
                new DirPos(this.pos.getX() + rot.offsetX * 3, this.pos.getY() + 3, this.pos.getZ() + rot.offsetZ * 3, rot),
                new DirPos(this.pos.getX() + rot.offsetX * -3, this.pos.getY() + 1, this.pos.getZ() + rot.offsetZ * -3, rot.getOpposite()),
                new DirPos(this.pos.getX() + rot.offsetX * -3, this.pos.getY() + 3, this.pos.getZ() + rot.offsetZ * -3, rot.getOpposite())
        };
    }

    public List<FluidTankNTM> getTanks() {
        List<FluidTankNTM> list = new ArrayList<FluidTankNTM>();
        list.add(steam);
        list.add(carbonDioxide);
        list.add(water);

        return list;
    }

    public AxisAlignedBB getRenderBoundingBox() {
        return new AxisAlignedBB(pos.getX() - 2, pos.getY(), pos.getZ() - 2, pos.getX() + 3, pos.getY() + 5, pos.getZ() + 3);
    }

    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }

    @Override
    public boolean hasPermission(EntityPlayer player) {
        return Vec3.createVectorHelper(pos.getX() - player.posX, pos.getY() - player.posY, pos.getZ() - player.posZ).length() < 20;
    }

    @Override
    public void receiveControl(NBTTagCompound data) {
        if (data.hasKey("control") && !redstonePowered) {
            this.isOn = !this.isOn;
        }

        if (data.hasKey("vent")) {
            int fill = this.carbonDioxide.getFill();
            this.carbonDioxide.setFill(fill - 1000);
            if (this.carbonDioxide.getFill() < 0)
                this.carbonDioxide.setFill(0);
        }

        this.markDirty();
    }

    @Override
    public FluidTankNTM[] getSendingTanks() {
        return new FluidTankNTM[]{steam};
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return new FluidTankNTM[]{water, carbonDioxide};
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return new FluidTankNTM[]{water, steam, carbonDioxide};
    }

    // do some opencomputer stuff
    @Override
    @Optional.Method(modid = "opencomputers")
    public String getComponentName() {
        return "zirnox_reactor";
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getTemp(Context context, Arguments args) {
        return new Object[] {Math.round(heat * 1.0E-5D * 780.0D + 20.0D)};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getPressure(Context context, Arguments args) {
        return new Object[] {Math.round(pressure * 1.0E-5D * 30.0D)};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getWater(Context context, Arguments args) {
        return new Object[]{water.getFill()};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getSteam(Context context, Arguments args) {
        return new Object[]{steam.getFill()};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getCarbonDioxide(Context context, Arguments args) {
        return new Object[]{carbonDioxide.getFill()};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] isActive(Context context, Arguments args) {
        return new Object[]{isOn};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] getInfo(Context context, Arguments args) {
        return new Object[] {Math.round(heat * 1.0E-5D * 780.0D + 20.0D), Math.round(pressure * 1.0E-5D * 30.0D), water.getFill(), steam.getFill(), carbonDioxide.getFill(), isOn};
    }

    @Callback(direct = true, limit = 4)
    @Optional.Method(modid = "opencomputers")
    public Object[] setActive(Context context, Arguments args) {
        isOn = args.checkBoolean(0);
        return new Object[]{};
    }

    @Callback(direct = true)
    @Optional.Method(modid = "opencomputers")
    public Object[] ventCarbonDioxide(Context context, Arguments args) {
        int ventAmount = MathHelper.clamp(args.optInteger(0, 1000), 0, carbonDioxide.getMaxFill()); // Get how much CO2 to vent in mB (1000mB default), clamp between 0 and carbonDioxide's max fill.
        int fill = this.carbonDioxide.getFill();
        this.carbonDioxide.setFill(Math.max(fill - ventAmount, 0)); // Make sure it isn't a negative number.
        return new Object[] {};
    }

    @Override
    @Optional.Method(modid = "opencomputers")
    public String[] methods() {
        return new String[] {
                "getTemp",
                "getPressure",
                "getWater",
                "getSteam",
                "getCarbonDioxide",
                "isActive",
                "getInfo",
                "setActive",
                "ventCarbonDioxide"
        };
    }

    @Override
    @Optional.Method(modid = "opencomputers")
    public Object[] invoke(String method, Context context, Arguments args) throws Exception {
        switch (method) {
            case ("getTemp"):
                return getTemp(context, args);
            case ("getPressure"):
                return getPressure(context, args);
            case ("getWater"):
                return getWater(context, args);
            case ("getSteam"):
                return getSteam(context, args);
            case ("getCarbonDioxide"):
                return getCarbonDioxide(context, args);
            case ("isActive"):
                return isActive(context, args);
            case ("getInfo"):
                return getInfo(context, args);
            case ("setActive"):
                return setActive(context, args);
            case ("ventCarbonDioxide"):
                return ventCarbonDioxide(context, args);
        }
        throw new NoSuchMethodException();
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerReactorZirnox(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIReactorZirnox(player.inventory, this);
    }
}
