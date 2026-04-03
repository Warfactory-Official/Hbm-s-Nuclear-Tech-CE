package com.hbm.tileentity.machine;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.MobConfig;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.RecipesCommon;
import com.hbm.inventory.container.ContainerReactorResearch;
import com.hbm.inventory.gui.GUIReactorResearch;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemPlateFuel;
import com.hbm.lib.ForgeDirection;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.BufferUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;

@AutoRegister
public class TileEntityReactorResearch extends TileEntityMachineBase implements IControlReceiver, IGUIProvider, ITickable {

    private AxisAlignedBB bb;
    @SideOnly(Side.CLIENT)
    public double lastLevel;
    public double level;
    public double speed = 0.04;
    public double targetLevel;

    public int heat;
    public byte water;
    public final int maxHeat = 50000;
    public int[] slotFlux = new int[12];
    public int totalFlux = 0;

    private static final int[] slot_io = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11 };

    public TileEntityReactorResearch() {
        super(12);
    }

    private static final HashMap<RecipesCommon.ComparableStack, ItemStack> fuelMap = new HashMap<RecipesCommon.ComparableStack, ItemStack>();
    static {
        fuelMap.put(new RecipesCommon.ComparableStack(ModItems.plate_fuel_u233), new ItemStack(ModItems.waste_plate_u233, 1, 1));
        fuelMap.put(new RecipesCommon.ComparableStack(ModItems.plate_fuel_u235), new ItemStack(ModItems.waste_plate_u235, 1, 1));
        fuelMap.put(new RecipesCommon.ComparableStack(ModItems.plate_fuel_mox), new ItemStack(ModItems.waste_plate_mox, 1, 1));
        fuelMap.put(new RecipesCommon.ComparableStack(ModItems.plate_fuel_pu239), new ItemStack(ModItems.waste_plate_pu239, 1, 1));
        fuelMap.put(new RecipesCommon.ComparableStack(ModItems.plate_fuel_sa326), new ItemStack(ModItems.waste_plate_sa326, 1, 1));
        fuelMap.put(new RecipesCommon.ComparableStack(ModItems.plate_fuel_ra226be), new ItemStack(ModItems.waste_plate_ra226be, 1, 1));
        fuelMap.put(new RecipesCommon.ComparableStack(ModItems.plate_fuel_pu238be), new ItemStack(ModItems.waste_plate_pu238be, 1, 1));
    }

    public String getDefaultName() {
        return "container.reactorResearch";
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemStack) {
        if(i < 12 && i <= 0)
            if(itemStack.getItem().getClass() == ItemPlateFuel.class)
                return true;
        return false;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        heat = nbt.getInteger("heat");
        water = nbt.getByte("water");
        level = nbt.getDouble("level");
        targetLevel = nbt.getDouble("targetLevel");
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        nbt.setInteger("heat", heat);
        nbt.setByte("water", water);
        nbt.setDouble("level", level);
        nbt.setDouble("targetLevel", targetLevel);
        return super.writeToNBT(nbt);
    }

    @Override
    public int[] getAccessibleSlotsFromSide(EnumFacing side) {
        return slot_io;
    }

    @Override
    public boolean canExtractItem(int i, ItemStack stack, int j) {
        if(i < 12 && i >= 0)
            if(fuelMap.containsValue(stack))
                return true;

        return false;

    }

    @Override
    public void update() {

        rodControl();

        if(!world.isRemote) {
            totalFlux = 0;

            if(level > 0) {
                reaction();
            }

            if(this.heat > 0) {
                water = getWater();

                if(water > 0) {
                    this.heat -= (this.heat * (float) 0.07 * water / 12);
                } else if(water == 0) {
                    this.heat -= 1;
                }

                if(this.heat < 0)
                    this.heat = 0;
            }

            if(this.heat > maxHeat) {
                this.explode();
            }

            if(level > 0 && heat > 0 && !(blocksRad(pos.add(1, 1, 0)) && blocksRad(pos.add(-1, 1, 0)) && blocksRad(pos.add(0, 1, 1)) && blocksRad(pos.add(0, 1, -1)))) {
                float rad = (float) heat / (float) maxHeat * 50F;
                ChunkRadiationManager.proxy.incrementRad(world, pos, rad, (float) 25000);
            }

            networkPackNT(150);
        }
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeInt(heat);
        buf.writeByte(water);
        buf.writeDouble(level);
        buf.writeDouble(targetLevel);
        BufferUtil.writeIntArray(buf, slotFlux);
        buf.writeInt(totalFlux);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        this.heat = buf.readInt();
        this.water = buf.readByte();
        this.level = buf.readDouble();
        this.targetLevel = buf.readDouble();
        this.slotFlux = BufferUtil.readIntArray(buf);
        this.totalFlux = buf.readInt();
    }

    public byte getWater() {
        byte water = 0;

        for(byte d = 0; d < 6; d++) {
            ForgeDirection dir = ForgeDirection.getOrientation(d);
            if(d < 2) {
                if(world.getBlockState(pos.add(0, 1 + dir.offsetY * 2, 0)).getMaterial() == Material.WATER)
                    water++;
            } else {
                for(byte i = 0; i < 3; i++) {
                    if(world.getBlockState(pos.add(dir.offsetX, i, dir.offsetZ)).getMaterial() == Material.WATER)
                        water++;
                }
            }
        }

        return water;
    }

    public boolean isSubmerged() {

        return world.getBlockState(pos.add(1, 1, 0)).getMaterial() == Material.WATER ||
                world.getBlockState(pos.add(0, 1, 1)).getMaterial() == Material.WATER ||
                world.getBlockState(pos.add(-1, 1, 0)).getMaterial() == Material.WATER ||
                world.getBlockState(pos.add(0, 1, -1)).getMaterial() == Material.WATER;
    }

	/*private void getInteractions() {
		getInteractionForBlock(xCoord + 1, yCoord + 1, zCoord);
		getInteractionForBlock(xCoord - 1, yCoord + 1, zCoord);
		getInteractionForBlock(xCoord, yCoord + 1, zCoord + 1);
		getInteractionForBlock(xCoord, yCoord + 1, zCoord - 1);
	}

	private void getInteractionForBlock(int x, int y, int z) {

		Block b = world.getBlock(x, y, z);
		TileEntity te = world.getTileEntity(x, y, z);
	}*/

    private boolean blocksRad(BlockPos pos) {

        Block b = world.getBlockState(pos).getBlock();

        if((b == Blocks.WATER || b == Blocks.FLOWING_WATER) && world.getBlockState(pos).getBlock().getMetaFromState(world.getBlockState(pos)) == 0)
            return true;

        if(b == ModBlocks.block_lead || b == ModBlocks.block_desh || b == ModBlocks.reactor_research || b == ModBlocks.machine_reactor_breeding)
            return true;

        if(b.getExplosionResistance(null) >= 100)
            return true;

        return false;
    }

    private int[] getNeighboringSlots(int id) {

        switch(id) {
            case 0:
                return new int[] { 1, 5 };
            case 1:
                return new int[] { 0, 6 };
            case 2:
                return new int[] { 3, 7 };
            case 3:
                return new int[] { 2, 4, 8 };
            case 4:
                return new int[] { 3, 9 };
            case 5:
                return new int[] { 0, 6, 0xA };
            case 6:
                return new int[] { 1, 5, 0xB };
            case 7:
                return new int[] { 2, 8 };
            case 8:
                return new int[] { 3, 7, 9 };
            case 9:
                return new int[] { 4, 8 };
            case 10:
                return new int[] { 5, 0xB };
            case 11:
                return new int[] { 6, 0xA };
        }

        return null;
    }

    private void reaction() {
        for(byte i = 0; i < 12; i++) {
            if(inventory.getStackInSlot(i).isEmpty())  {
                slotFlux[i] = 0;
                continue;
            }

            if(inventory.getStackInSlot(i).getItem() instanceof ItemPlateFuel) {
                ItemPlateFuel rod = (ItemPlateFuel) inventory.getStackInSlot(i).getItem();

                int outFlux = rod.react(world, inventory.getStackInSlot(i), slotFlux[i]);
                this.heat += outFlux * 2;
                slotFlux[i] = 0;
                totalFlux += outFlux;

                int[] neighborSlots = getNeighboringSlots(i);

                if(ItemPlateFuel.getLifeTime(inventory.getStackInSlot(i)) > rod.lifeTime) {
                    inventory.setStackInSlot(i, fuelMap.get(new RecipesCommon.ComparableStack(inventory.getStackInSlot(i))).copy());
                }

                for(byte j = 0; j < neighborSlots.length; j++) {
                    slotFlux[neighborSlots[j]] += (int) (outFlux * level);
                }
                continue;
            }

            if(inventory.getStackInSlot(i).getItem() == ModItems.meteorite_sword_bred)
                inventory.setStackInSlot(i, new ItemStack(ModItems.meteorite_sword_irradiated));

            slotFlux[i] = 0;
        }
    }

    private void explode() {

        for(int i = 0; i < inventory.getSlots(); i++) {
            this.inventory.setStackInSlot(i, ItemStack.EMPTY);
        }

        world.setBlockToAir(pos);

        for(byte d = 0; d < 6; d++) {
            ForgeDirection dir = ForgeDirection.getOrientation(d);
            if(d < 2) {
                if(world.getBlockState(pos.add(0, 1 + dir.offsetY * 2, 0)).getMaterial() == Material.WATER)
                    world.setBlockToAir(pos.add(0, 1 + dir.offsetY * 2, 0));
            } else {
                for(byte i = 0; i < 3; i++) {
                    if(world.getBlockState(pos.add(dir.offsetX, i, dir.offsetZ)).getMaterial() == Material.WATER)
                        world.setBlockToAir(pos.add(dir.offsetX, i, dir.offsetZ));
                }
            }
        }

        world.createExplosion(null, this.pos.getX(), this.pos.getY(), this.pos.getZ(), 18.0F, true);
        world.setBlockState(this.pos, ModBlocks.deco_steel.getDefaultState());
        world.setBlockState(this.pos.add(0, 1, 0), ModBlocks.corium_block.getDefaultState());
        world.setBlockState(this.pos.add(0, 2, 0), ModBlocks.deco_steel.getDefaultState());

        ChunkRadiationManager.proxy.incrementRad(world, pos, 50F, 15000F);

        if(MobConfig.enableElementals) {
            List<EntityPlayer> players = world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5).expand(100, 100, 100));

            for(EntityPlayer player : players) {
                player.getEntityData().getCompoundTag(player.PERSISTED_NBT_TAG).setBoolean("radMark", true);
            }
        }
    }

    //Control Rods
    @Override
    public boolean hasPermission(EntityPlayer player) {
        return new Vec3d(pos.getX() - player.posX, pos.getY() - player.posY, pos.getZ() - player.posZ).length() < 20;
    }

    @Override
    public void receiveControl(NBTTagCompound data) {
        if(data.hasKey("level")) {
            this.setTarget(data.getDouble("level"));
        }

        this.markDirty();
    }

    public void setTarget(double target) {
        this.targetLevel = target;
    }

    public void rodControl() {
        if(world.isRemote) {

            this.lastLevel = this.level;

        } else {

            if(level < targetLevel) {

                level += speed;

                if(level >= targetLevel)
                    level = targetLevel;
            }

            if(level > targetLevel) {

                level -= speed;

                if(level <= targetLevel)
                    level = targetLevel;
            }
        }
    }

    public int[] getDisplayData() {
        int[] data = new int[2];
        data[0] = this.totalFlux;
        data[1] = (int) Math.round((this.heat) * 0.00002 * 980 + 20);
        return data;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        if (bb == null) bb = new AxisAlignedBB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 3, pos.getZ() + 1);
        return bb;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }

    // do some opencomputer stuff - FOR NOW IT'S NOT WORKING ANYWAY

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerReactorResearch(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIReactorResearch(player.inventory, this);
    }
}
