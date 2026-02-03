package com.hbm.tileentity.machine;

import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.api.fluid.IFluidStandardTransceiver;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.UpgradeManagerNT;
import com.hbm.inventory.container.ContainerElectrolyserFluid;
import com.hbm.inventory.container.ContainerElectrolyserMetal;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUIElectrolyserFluid;
import com.hbm.inventory.gui.GUIElectrolyserMetal;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.recipes.ElectrolyserFluidRecipes;
import com.hbm.inventory.recipes.ElectrolyserFluidRecipes.ElectrolysisRecipe;
import com.hbm.inventory.recipes.ElectrolyserMetalRecipes;
import com.hbm.inventory.recipes.ElectrolyserMetalRecipes.ElectrolysisMetalRecipe;
import com.hbm.items.machine.ItemMachineUpgrade;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.packet.threading.ThreadedPacket;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.tileentity.*;
import com.hbm.util.BobMathUtil;
import com.hbm.util.CrucibleUtil;
import com.hbm.util.I18nUtil;
import com.hbm.util.MutableVec3d;
import com.mojang.realmsclient.gui.ChatFormatting;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@AutoRegister
public class TileEntityElectrolyser extends TileEntityMachineBase implements IEnergyReceiverMK2, IFluidStandardTransceiver, IControlReceiver,
        IGUIProvider, IUpgradeInfoProvider, IFluidCopiable, IMetalCopiable, ITickable {

    public long power;
    public static final long maxPower = 20000000;
    public static final int usageOreBase = 10_000;
    public static final int usageFluidBase = 10_000;
    public int usageOre;
    public int usageFluid;

    public int progressFluid;
    public int processFluidTime = 100;
    public int progressOre;
    public int processOreTime = 600;

    public Mats.MaterialStack leftStack;
    public Mats.MaterialStack rightStack;
    public int maxMaterial = MaterialShapes.BLOCK.q(16);

    public FluidTankNTM[] tanks;

    public UpgradeManagerNT upgradeManager = new UpgradeManagerNT(this);
    private int lastSelectedGUI = 0;

    public TileEntityElectrolyser() {
        //0: Battery
        //1-2: Upgrades
        //// FLUID
        //3-4: Fluid ID
        //5-10: Fluid IO
        //11-13: Byproducts
        //// METAL
        //14: Crystal
        //15-20: Outputs
        super(21, true, true);
        tanks = new FluidTankNTM[4];
        tanks[0] = new FluidTankNTM(Fluids.WATER, 16000);
        tanks[1] = new FluidTankNTM(Fluids.HYDROGEN, 16000);
        tanks[2] = new FluidTankNTM(Fluids.OXYGEN, 16000);
        tanks[3] = new FluidTankNTM(Fluids.NITRIC_ACID, 16000);
    }

    @Override
    public int[] getAccessibleSlotsFromSide(EnumFacing e) {
        return new int[] { 11, 12, 13, 14, 15, 16, 17, 18, 19, 20 };
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemStack) {
        if(i == 14) return ElectrolyserMetalRecipes.getRecipe(itemStack) != null;
        return false;
    }

    @Override
    public boolean canExtractItem(int i, ItemStack itemStack, int j) {
        return i != 14;
    }

    @Override
    public String getDefaultName() {
        return "container.machineElectrolyser";
    }

    @Override
    public void update() {

        if(!world.isRemote) {

            this.power = Library.chargeTEFromItems(inventory, 0, power, maxPower);
            this.tanks[0].setType(3, 4, inventory);
            this.tanks[0].loadTank(5, 6, inventory);
            this.tanks[1].unloadTank(7, 8, inventory);
            this.tanks[2].unloadTank(9, 10, inventory);

            if(world.getTotalWorldTime() % 20 == 0) {
                for(DirPos pos : this.getConPos()) {
                    this.trySubscribe(world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
                    this.trySubscribe(tanks[0].getTankType(), world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
                    this.trySubscribe(tanks[3].getTankType(), world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());

                    if(tanks[1].getFill() > 0) this.sendFluid(tanks[1], world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
                    if(tanks[2].getFill() > 0) this.sendFluid(tanks[2], world, pos.getPos().getX(), pos.getPos().getY(), pos.getPos().getZ(), pos.getDir());
                }
            }

            ItemStack[] allSlots = new ItemStack[inventory.getSlots()];
            for(int i = 0; i < inventory.getSlots(); i++) {
                allSlots[i] = inventory.getStackInSlot(i);
            }

            upgradeManager.checkSlots(allSlots, 1, 2);
            int speedLevel = upgradeManager.getLevel(ItemMachineUpgrade.UpgradeType.SPEED);
            int powerLevel = upgradeManager.getLevel(ItemMachineUpgrade.UpgradeType.POWER);

            usageOre = usageOreBase - usageOreBase * powerLevel / 4 + usageOreBase * speedLevel;
            usageFluid = usageFluidBase - usageFluidBase * powerLevel / 4 + usageFluidBase * speedLevel;

            for(int i = 0; i < getCycleCount(); i++) {
                if (this.canProcessFluid()) {
                    this.progressFluid++;
                    this.power -= this.usageFluid;

                    if (this.progressFluid >= this.getDurationFluid()) {
                        this.processFluids();
                        this.progressFluid = 0;
                        this.markDirty();
                    }
                }

                if (this.canProcessMetal()) {
                    this.progressOre++;
                    this.power -= this.usageOre;

                    if (this.progressOre >= this.getDurationMetal()) {
                        this.processMetal();
                        this.progressOre = 0;
                        this.markDirty();
                    }
                }
            }

            if(this.leftStack != null) {

                ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset).getOpposite();
                List<Mats.MaterialStack> toCast = new ArrayList<>();
                toCast.add(this.leftStack);

                MutableVec3d impact = new MutableVec3d();
                Mats.MaterialStack didPour = CrucibleUtil.pourFullStack(world, pos.getX() + 0.5D + dir.offsetX * 5.875D, pos.getY() + 2D, pos.getZ() + 0.5D + dir.offsetZ * 5.875D, 6, true, toCast, MaterialShapes.NUGGET.q(3) * Math.max (getCycleCount() * speedLevel, 1), impact);

                if(didPour != null) {
                    NBTTagCompound data = new NBTTagCompound();
                    data.setString("type", "foundry");
                    data.setInteger("color", didPour.material.moltenColor);
                    data.setByte("dir", (byte) dir.ordinal());
                    data.setFloat("off", 0.625F);
                    data.setFloat("base", 0.625F);
                    data.setFloat("len", Math.max(1F, pos.getY() - (float) (Math.ceil(impact.y) - 0.875) + 2));
                    ThreadedPacket message = new AuxParticlePacketNT(data, pos.getX() + 0.5D + dir.offsetX * 5.875D, pos.getY() + 2, pos.getZ() + 0.5D + dir.offsetZ * 5.875D);
                    PacketThreading.createAllAroundThreadedPacket(message,
                            new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 50));

                    if(this.leftStack.amount <= 0) this.leftStack = null;
                }
            }

            if(this.rightStack != null) {

                ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - BlockDummyable.offset);
                List<Mats.MaterialStack> toCast = new ArrayList<>();
                toCast.add(this.rightStack);

                MutableVec3d impact = new MutableVec3d();
                Mats.MaterialStack didPour = CrucibleUtil.pourFullStack(world, pos.getX() + 0.5D + dir.offsetX * 5.875D, pos.getY() + 2D, pos.getZ() + 0.5D + dir.offsetZ * 5.875D, 6, true, toCast, MaterialShapes.NUGGET.q(3) * Math.max (getCycleCount() * speedLevel, 1), impact);

                if(didPour != null) {
                    NBTTagCompound data = new NBTTagCompound();
                    data.setString("type", "foundry");
                    data.setInteger("color", didPour.material.moltenColor);
                    data.setByte("dir", (byte) dir.ordinal());
                    data.setFloat("off", 0.625F);
                    data.setFloat("base", 0.625F);
                    data.setFloat("len", Math.max(1F, pos.getY() - (float) (Math.ceil(impact.y) - 0.875) + 2));
                    ThreadedPacket message = new AuxParticlePacketNT(data, pos.getX() + 0.5D + dir.offsetX * 5.875D, pos.getY() + 2, pos.getZ() + 0.5D + dir.offsetZ * 5.875D);
                    PacketThreading.createAllAroundThreadedPacket(message,
                            new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 50));

                    if(this.rightStack.amount <= 0) this.rightStack = null;
                }
            }

            this.networkPackNT(50);
        }
    }

    public DirPos[] getConPos() {
        ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
        ForgeDirection rot = dir.getRotation(ForgeDirection.UP);

        return new DirPos[] {
                new DirPos(pos.getX() - dir.offsetX * 6, pos.getY(), pos.getZ() - dir.offsetZ * 6, dir.getOpposite()),
                new DirPos(pos.getX() - dir.offsetX * 6 + rot.offsetX, pos.getY(), pos.getZ() - dir.offsetZ * 6 + rot.offsetZ, dir.getOpposite()),
                new DirPos(pos.getX() - dir.offsetX * 6 - rot.offsetX, pos.getY(), pos.getZ() - dir.offsetZ * 6 - rot.offsetZ, dir.getOpposite()),
                new DirPos(pos.getX() + dir.offsetX * 6, pos.getY(), pos.getZ() + dir.offsetZ * 6, dir),
                new DirPos(pos.getX() + dir.offsetX * 6 + rot.offsetX, pos.getY(), pos.getZ() + dir.offsetZ * 6 + rot.offsetZ, dir),
                new DirPos(pos.getX() + dir.offsetX * 6 - rot.offsetX, pos.getY(), pos.getZ() + dir.offsetZ * 6 - rot.offsetZ, dir)
        };
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeLong(this.power);
        buf.writeInt(this.progressFluid);
        buf.writeInt(this.progressOre);
        buf.writeInt(this.usageOre);
        buf.writeInt(this.usageFluid);
        buf.writeInt(this.getDurationFluid());
        buf.writeInt(this.getDurationMetal());
        for(int i = 0; i < 4; i++) tanks[i].serialize(buf);
        buf.writeBoolean(this.leftStack != null);
        buf.writeBoolean(this.rightStack != null);
        if(this.leftStack != null) {
            buf.writeInt(leftStack.material.id);
            buf.writeInt(leftStack.amount);
        }
        if(this.rightStack != null) {
            buf.writeInt(rightStack.material.id);
            buf.writeInt(rightStack.amount);
        }
        buf.writeInt(lastSelectedGUI);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        this.power = buf.readLong();
        this.progressFluid = buf.readInt();
        this.progressOre = buf.readInt();
        this.usageOre = buf.readInt();
        this.usageFluid = buf.readInt();
        this.processFluidTime = buf.readInt();
        this.processOreTime = buf.readInt();
        for(int i = 0; i < 4; i++) tanks[i].deserialize(buf);
        boolean left = buf.readBoolean();
        boolean right = buf.readBoolean();
        if(left) {
            this.leftStack = new Mats.MaterialStack(Mats.matById.get(buf.readInt()), buf.readInt());
        }
        if(right) {
            this.rightStack = new Mats.MaterialStack(Mats.matById.get(buf.readInt()), buf.readInt());
        }
        this.lastSelectedGUI = buf.readInt();
    }

    public boolean canProcessFluid() {

        if(this.power < usageFluid) return false;

        ElectrolysisRecipe recipe = ElectrolyserFluidRecipes.recipes.get(tanks[0].getTankType());

        if(recipe == null) return false;
        if(recipe.amount > tanks[0].getFill()) return false;
        if(recipe.output1.type == tanks[1].getTankType() && recipe.output1.fill + tanks[1].getFill() > tanks[1].getMaxFill()) return false;
        if(recipe.output2.type == tanks[2].getTankType() && recipe.output2.fill + tanks[2].getFill() > tanks[2].getMaxFill()) return false;

        if(recipe.byproduct != null) {

            for(int i = 0; i < recipe.byproduct.length; i++) {
                ItemStack slot = inventory.getStackInSlot(11 + i);
                ItemStack byproduct = recipe.byproduct[i];

                if(slot.isEmpty()) continue;
                if(!slot.isItemEqual(byproduct)) return false;
                if(slot.getCount() + byproduct.getCount() > slot.getMaxStackSize()) return false;
            }
        }

        return true;
    }

    public void processFluids() {

        ElectrolysisRecipe recipe = ElectrolyserFluidRecipes.recipes.get(tanks[0].getTankType());
        tanks[0].setFill(tanks[0].getFill() - recipe.amount);
        tanks[1].setTankType(recipe.output1.type);
        tanks[2].setTankType(recipe.output2.type);
        tanks[1].setFill(tanks[1].getFill() + recipe.output1.fill);
        tanks[2].setFill(tanks[2].getFill() + recipe.output2.fill);

        if(recipe.byproduct != null) {

            for(int i = 0; i < recipe.byproduct.length; i++) {
                ItemStack slot = inventory.getStackInSlot(11 + i);
                ItemStack byproduct = recipe.byproduct[i];

                if(slot.isEmpty()) {
                    inventory.setStackInSlot(11 + i, byproduct.copy());
                } else {
                    inventory.getStackInSlot(11 + i).grow(byproduct.getCount());
                }
            }
        }
    }

    public boolean canProcessMetal() {

        if(inventory.getStackInSlot(14).isEmpty()) return false;
        if(this.power < usageOre) return false;
        if(this.tanks[3].getFill() < 100) return false;

        ElectrolysisMetalRecipe recipe = ElectrolyserMetalRecipes.getRecipe(inventory.getStackInSlot(14));
        if(recipe == null) return false;

        if(leftStack != null && recipe.output1 != null) {
            if(recipe.output1.material != leftStack.material) return false;
            if(recipe.output1.amount + leftStack.amount > this.maxMaterial) return false;
        }

        if(rightStack != null && recipe.output2 != null) {
            if(recipe.output2.material != rightStack.material) return false;
            if(recipe.output2.amount + rightStack.amount > this.maxMaterial) return false;
        }

        if(recipe.byproduct != null) {

            for(int i = 0; i < recipe.byproduct.length; i++) {
                ItemStack slot = inventory.getStackInSlot(15 + i);
                ItemStack byproduct = recipe.byproduct[i];

                if(slot.isEmpty()) continue;
                if(!slot.isItemEqual(byproduct)) return false;
                if(slot.getCount() + byproduct.getCount() > slot.getMaxStackSize()) return false;
            }
        }

        return true;
    }

    public void processMetal() {

        ElectrolysisMetalRecipe recipe = ElectrolyserMetalRecipes.getRecipe(inventory.getStackInSlot(14));
        if(recipe.output1 != null)
            if(leftStack == null) {
                leftStack = new Mats.MaterialStack(recipe.output1.material, recipe.output1.amount);
            } else {
                leftStack.amount += recipe.output1.amount;
            }

        if(recipe.output2 != null)
            if(rightStack == null ) {
                rightStack = new Mats.MaterialStack(recipe.output2.material, recipe.output2.amount);
            } else {
                rightStack.amount += recipe.output2.amount;
            }

        if(recipe.byproduct != null) {

            for(int i = 0; i < recipe.byproduct.length; i++) {
                ItemStack slot = inventory.getStackInSlot(15 + i);
                ItemStack byproduct = recipe.byproduct[i];

                if(slot.isEmpty()) {
                    inventory.setStackInSlot(15 + i, byproduct.copy());
                } else {
                    inventory.getStackInSlot(15 + i).grow(byproduct.getCount());
                }
            }
        }

        this.tanks[3].setFill(this.tanks[3].getFill() - 100);
        this.inventory.getStackInSlot(14).shrink(1);
    }

    public int getDurationMetal() {
        ElectrolysisMetalRecipe result = ElectrolyserMetalRecipes.getRecipe(inventory.getStackInSlot(14));
        int base = result != null ? result.duration : 600;
        int speed = upgradeManager.getLevel(ItemMachineUpgrade.UpgradeType.SPEED) - Math.min(upgradeManager.getLevel(ItemMachineUpgrade.UpgradeType.POWER), 1);
        return (int) Math.ceil((base * Math.max(1F - 0.25F * speed, 0.2)));
    }
    public int getDurationFluid() {
        ElectrolysisRecipe result = ElectrolyserFluidRecipes.getRecipe(tanks[0].getTankType());
        int base = result != null ? result.duration : 100;
        int speed = upgradeManager.getLevel(ItemMachineUpgrade.UpgradeType.SPEED) - Math.min(upgradeManager.getLevel(ItemMachineUpgrade.UpgradeType.POWER), 1);
        return (int) Math.ceil((base * Math.max(1F - 0.25F * speed, 0.2)));

    }

    public int getCycleCount() {
        int speed = upgradeManager.getLevel(ItemMachineUpgrade.UpgradeType.OVERDRIVE);
        return Math.min(1 + speed * 2, 7);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        this.power = nbt.getLong("power");
        this.progressFluid = nbt.getInteger("progressFluid");
        this.progressOre = nbt.getInteger("progressOre");
        this.processFluidTime = nbt.getInteger("processFluidTime");
        this.processOreTime = nbt.getInteger("processOreTime");
        if(nbt.hasKey("leftType")) this.leftStack = new Mats.MaterialStack(Mats.matById.get(nbt.getInteger("leftType")), nbt.getInteger("leftAmount"));
        else this.leftStack = null;
        if(nbt.hasKey("rightType")) this.rightStack = new Mats.MaterialStack(Mats.matById.get(nbt.getInteger("rightType")), nbt.getInteger("rightAmount"));
        else this.rightStack = null;
        for(int i = 0; i < 4; i++) tanks[i].readFromNBT(nbt, "t" + i);
        this.lastSelectedGUI = nbt.getInteger("lastSelectedGUI");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setLong("power", this.power);
        nbt.setInteger("progressFluid", this.progressFluid);
        nbt.setInteger("progressOre", this.progressOre);
        nbt.setInteger("processFluidTime", getDurationFluid());
        nbt.setInteger("processOreTime", getDurationMetal());
        if(this.leftStack != null) {
            nbt.setInteger("leftType", leftStack.material.id);
            nbt.setInteger("leftAmount", leftStack.amount);
        }
        if(this.rightStack != null) {
            nbt.setInteger("rightType", rightStack.material.id);
            nbt.setInteger("rightAmount", rightStack.amount);
        }
        for(int i = 0; i < 4; i++) tanks[i].writeToNBT(nbt, "t" + i);
        nbt.setInteger("lastSelectedGUI", this.lastSelectedGUI);
        return super.writeToNBT(nbt);
    }

    AxisAlignedBB bb = null;

    @Override
    public AxisAlignedBB getRenderBoundingBox() {

        if(bb == null) {
            bb = new AxisAlignedBB(
                    pos.getX() - 5,
                    pos.getY(),
                    pos.getZ() - 5,
                    pos.getX() + 6,
                    pos.getY() + 4,
                    pos.getZ() + 6
            );
        }

        return bb;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }

    @Override
    public long getPower() {
        return this.power;
    }

    @Override
    public long getMaxPower() {
        return maxPower;
    }

    @Override
    public void setPower(long power) {
        this.power = power;
    }

    @Override
    public FluidTankNTM[] getAllTanks() {
        return tanks;
    }

    @Override
    public FluidTankNTM[] getSendingTanks() {
        return new FluidTankNTM[] {tanks[1], tanks[2]};
    }

    @Override
    public FluidTankNTM[] getReceivingTanks() {
        return new FluidTankNTM[] {tanks[0], tanks[3]};
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if(ID == -1) ID = lastSelectedGUI;
        if(ID == 0) return new ContainerElectrolyserFluid(player.inventory, this);
        return new ContainerElectrolyserMetal(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if(ID == -1) ID = lastSelectedGUI;
        if(ID == 0) return new GUIElectrolyserFluid(player.inventory, this);
        return new GUIElectrolyserMetal(player.inventory, this);
    }

    @Override
    public void receiveControl(NBTTagCompound data) { }

    @Override
    public void receiveControl(EntityPlayerMP player, NBTTagCompound data) {

        if(data.hasKey("sgm")) lastSelectedGUI = 1;
        if(data.hasKey("sgf")) lastSelectedGUI = 0;

        FMLNetworkHandler.openGui(player, MainRegistry.instance, lastSelectedGUI, world, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public boolean hasPermission(EntityPlayer player) {
        return this.isUseableByPlayer(player);
    }

    @Override
    public boolean canProvideInfo(ItemMachineUpgrade.UpgradeType type, int level, boolean extendedInfo) {
        return type == ItemMachineUpgrade.UpgradeType.SPEED || type == ItemMachineUpgrade.UpgradeType.POWER || type == ItemMachineUpgrade.UpgradeType.OVERDRIVE;
    }

    @Override
    public void provideInfo(ItemMachineUpgrade.UpgradeType type, int level, List<String> info, boolean extendedInfo) {
        info.add(IUpgradeInfoProvider.getStandardLabel(ModBlocks.machine_electrolyser));
        if(type == ItemMachineUpgrade.UpgradeType.SPEED) {
            info.add(ChatFormatting.GREEN + I18nUtil.resolveKey(this.KEY_DELAY, "-" + (level * 25) + "%"));
            info.add(ChatFormatting.RED + I18nUtil.resolveKey(this.KEY_CONSUMPTION, "+" + (level * 100) + "%"));
        }
        if(type == ItemMachineUpgrade.UpgradeType.POWER) {
            info.add(ChatFormatting.GREEN + I18nUtil.resolveKey(this.KEY_CONSUMPTION, "-" + (level * 25) + "%"));
            info.add(ChatFormatting.RED + I18nUtil.resolveKey(this.KEY_DELAY, "+" + (25) + "%"));
        }
        if(type == ItemMachineUpgrade.UpgradeType.OVERDRIVE) {
            info.add((BobMathUtil.getBlink() ? ChatFormatting.RED : ChatFormatting.DARK_GRAY) + "YES");
        }
    }

    @Override
    public HashMap<ItemMachineUpgrade.UpgradeType, Integer> getValidUpgrades() {
        HashMap<ItemMachineUpgrade.UpgradeType, Integer> upgrades = new HashMap<>();
        upgrades.put(ItemMachineUpgrade.UpgradeType.SPEED, 3);
        upgrades.put(ItemMachineUpgrade.UpgradeType.POWER, 3);
        upgrades.put(ItemMachineUpgrade.UpgradeType.OVERDRIVE, 3);
        return upgrades;
    }

    @Override
    public FluidTankNTM getTankToPaste() {
        return tanks[0];
    }

    @Override
    public NBTTagCompound getSettings(World world, int x, int y, int z) {
        NBTTagCompound tag = new NBTTagCompound();
        if(getFluidIDToCopy().length > 0)
            tag.setIntArray("fluidID", getFluidIDToCopy());
        if(getMatsToCopy().length > 0)
            tag.setIntArray("matFilter", getMatsToCopy());
        return tag;
    }

    @Override
    public void pasteSettings(NBTTagCompound nbt, int index, World world, EntityPlayer player, int x, int y, int z) {
        IFluidCopiable.super.pasteSettings(nbt, index, world, player, x, y, z);
    }

    @Override
    public String[] infoForDisplay(World world, int x, int y, int z) {
        ArrayList<String> names = new ArrayList<>();
        int[] fluidIDs = getFluidIDToCopy();
        int[] matIDs = getMatsToCopy();

        for (int fluidID : fluidIDs) {
            names.add(Fluids.fromID(fluidID).getTranslationKey());
        }
        for (int matID : matIDs) {
            names.add(Mats.matById.get(matID).getTranslationKey());
        }

        return names.toArray(new String[0]);
    }

    @Override
    public int[] getMatsToCopy() {
        ArrayList<Integer> types = new ArrayList<>();
        if(leftStack != null)	types.add(leftStack.material.id);
        if(rightStack != null)	types.add(rightStack.material.id);
        return BobMathUtil.intCollectionToArray(types);
    }
}
