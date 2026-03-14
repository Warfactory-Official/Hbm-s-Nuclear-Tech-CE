package com.hbm.tileentity.machine;

import com.hbm.config.BombConfig;
import com.hbm.entity.effect.EntityCloudFleijaRainbow;
import com.hbm.entity.logic.EntityNukeExplosionMK3;
import com.hbm.handler.ArmorUtil;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerCore;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.inventory.gui.GUICore;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemCatalyst;
import com.hbm.items.special.ItemAMSCore;
import com.hbm.lib.Library;
import com.hbm.lib.ModDamageSource;
import com.hbm.main.AdvancementManager;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.Vec3NT;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

@AutoRegister
public class TileEntityCore extends TileEntityMachineBase implements ITickable, IGUIProvider {


    public int field;
    public int heat;
    public int prevHeat;
    public int color;
    public FluidTankNTM[] tanks;
    public boolean meltdownTick = false;
    protected int consumption;
    protected int prevConsumption;
    private boolean lastTickValid = false;
    private boolean hasGranted = false;

    public TileEntityCore() {
        super(3);
        tanks = new FluidTankNTM[2];
        tanks[0] = new FluidTankNTM(Fluids.DEUTERIUM, 128000);
        tanks[1] = new FluidTankNTM(Fluids.TRITIUM, 128000);
    }

    @Override
    public String getDefaultName() {
        return "container.dfcCore";
    }

    @Override
    public void update() {


        if (!world.isRemote) {

            this.prevConsumption = this.consumption;
            this.consumption = 0;

            int chunkX = pos.getX() >> 4;
            int chunkZ = pos.getZ() >> 4;

            meltdownTick = false;

            ChunkProviderServer provider = (ChunkProviderServer) world.getChunkProvider();
            lastTickValid =
                    provider.chunkExists(chunkX, chunkZ) &&
                            provider.chunkExists(chunkX + 1, chunkZ + 1) &&
                            provider.chunkExists(chunkX + 1, chunkZ - 1) &&
                            provider.chunkExists(chunkX - 1, chunkZ + 1) &&
                            provider.chunkExists(chunkX - 1, chunkZ - 1);

            if (lastTickValid && heat > 0 && heat >= field) {

                int fill = tanks[0].getFill() + tanks[1].getFill();
                int max = tanks[0].getMaxFill() + tanks[1].getMaxFill();
                int mod = heat * 10;

                int size = Math.max(Math.min(fill * mod / max, 1000), 50);

                boolean canExplode = true;
                Iterator<Map.Entry<EntityNukeExplosionMK3.ATEntry, Long>> it = EntityNukeExplosionMK3.at.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry<EntityNukeExplosionMK3.ATEntry, Long> next = it.next();
                    if (next.getValue() < world.getTotalWorldTime()) {
                        it.remove();
                        continue;
                    }
                    EntityNukeExplosionMK3.ATEntry entry = next.getKey();
                    if (entry.dim != world.provider.getDimension()) continue;
                    Vec3NT vec = new Vec3NT(pos.getX() + 0.5 - entry.x, pos.getY() + 0.5 - entry.y, pos.getZ() + 0.5 - entry.z);
                    if (vec.length() < 300) {
                        canExplode = false;
                        break;
                    }
                }

                if (canExplode) {

                    EntityNukeExplosionMK3 ex = new EntityNukeExplosionMK3(world);
                    ex.posX = pos.getX() + 0.5;
                    ex.posY = pos.getY() + 0.5;
                    ex.posZ = pos.getZ() + 0.5;
                    ex.destructionRange = size;
                    ex.speed = BombConfig.blastSpeed;
                    ex.coefficient = 1.0F;
                    ex.waste = false;
                    world.spawnEntity(ex);

                    world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ENTITY_GENERIC_EXPLODE, SoundCategory.BLOCKS, 100000.0F, 1.0F);

                    EntityCloudFleijaRainbow cloud = new EntityCloudFleijaRainbow(world, size);
                    cloud.posX = pos.getX();
                    cloud.posY = pos.getY();
                    cloud.posZ = pos.getZ();
                    world.spawnEntity(cloud);

                } else {
                    meltdownTick = true;
                    ChunkRadiationManager.proxy.incrementRad(world, new BlockPos(pos.getX(), pos.getY(), pos.getZ()), 100);
                }
            }

            if (inventory.getStackInSlot(0).getItem() instanceof ItemCatalyst && inventory.getStackInSlot(2).getItem() instanceof ItemCatalyst)
                color = calcAvgHex(
                        ((ItemCatalyst) inventory.getStackInSlot(0).getItem()).getColor(),
                        ((ItemCatalyst) inventory.getStackInSlot(2).getItem()).getColor()
                );
            else
                color = 0;

            if (heat > 0)
                radiation();

            prevHeat = heat;
            networkPackNT(250);

            heat = 0;

            if (lastTickValid && field > 0) {
                field -= 1;
            }

            this.markDirty();
        } else {

            //TODO: sick particle effects
            //They never happened LOL
        }
    }


    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);

        tanks[0].serialize(buf);
        tanks[1].serialize(buf);
        buf.writeInt(field);
        buf.writeInt(prevHeat);
        buf.writeInt(color);
        buf.writeBoolean(meltdownTick);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);

        tanks[0].deserialize(buf);
        tanks[1].deserialize(buf);
        this.field = buf.readInt();
        this.heat = buf.readInt();
        this.color = buf.readInt();
        this.meltdownTick = buf.readBoolean();
    }


    private void radiation() {
        double scale = this.meltdownTick ? 5 : 3;
        double range = this.meltdownTick ? 50 : 10;

        List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos.getX() - range + 0.5, pos.getY() - range + 0.5, pos.getZ() - range + 0.5, pos.getX() + range + 0.5, pos.getY() + range + 0.5, pos.getZ() + range + 0.5));

        for (Entity e : list) {
            if (!(e instanceof EntityPlayer && ArmorUtil.checkForHazmat((EntityPlayer) e)))
                if (!Library.isObstructed(world, pos.getX() + 0.5, pos.getY() + 0.5 + 6, pos.getZ() + 0.5, e.posX, e.posY + e.getEyeHeight(), e.posZ)) {
                    e.attackEntityFrom(ModDamageSource.ams, 1000);
                    e.setFire(3);
                }
        }

        List<Entity> list2 = world.getEntitiesWithinAABBExcludingEntity(null, new AxisAlignedBB(pos.getX() - scale + 0.5, pos.getY() - scale + 0.5, pos.getZ() - scale + 0.5, pos.getX() + scale + 0.5, pos.getY() + scale + 0.5, pos.getZ() + scale + 0.5));

        for (Entity e : list2) {
            if (!(e instanceof EntityPlayer && ArmorUtil.checkForHaz2((EntityPlayer) e)))
                e.attackEntityFrom(ModDamageSource.amsCore, 10000);
        }

    }

    public int getFieldScaled(int i) {
        return (field * i) / 100;
    }

    public int getHeatScaled(int i) {
        return (heat * i) / 100;
    }

    public boolean isReady() {

        if(!lastTickValid)
            return false;

        if (getCore() == 0)
            return false;

        if (color == 0)
            return false;

        if(getFuelEfficiency(tanks[0].getTankType()) <= 0 || getFuelEfficiency(tanks[1].getTankType()) <= 0)
            return false;

        return true;
    }

    //100 emitter watt = 10000 joules = 1 heat = 10mB burned
    public long burn(long joules) {
        //check if a reaction can take place
        if (!isReady())
            return joules;
        if (!hasGranted){
            List<EntityPlayerMP> players = world.getEntitiesWithinAABB(
                    EntityPlayerMP.class, new AxisAlignedBB(pos.getX() - 50 + 0.5, pos.getY() - 50 + 0.5, pos.getZ() - 50 + 0.5, pos.getX() + 50 + 0.5, pos.getY() + 50 + 0.5, pos.getZ() + 50 + 0.5));
            for (EntityPlayerMP player : players) {
                AdvancementManager.grantAchievement(player, AdvancementManager.progress_dfc);
            }
            hasGranted = true;
        }
        int demand = (int) Math.ceil((double) joules / 1000D);

        //check if the reaction has enough valid fuel
        if (tanks[0].getFill() < demand || tanks[1].getFill() < demand)
            return joules;

        this.consumption += demand;

        heat += (int) Math.ceil((double) joules / 10000D);

        tanks[0].setFill(tanks[0].getFill() - demand);
        tanks[1].setFill(tanks[1].getFill() - demand);

        return (long) (joules * getCore() * getFuelEfficiency(tanks[0].getTankType()) * getFuelEfficiency(tanks[1].getTankType()));
    }

    public float getFuelEfficiency(FluidType type) {
        if (type == Fluids.HYDROGEN)
            return 1.0F;
        if (type == Fluids.DEUTERIUM)
            return 1.5F;
        if (type == Fluids.TRITIUM)
            return 1.7F;
        if (type == Fluids.OXYGEN)
            return 1.2F;
        if (type == Fluids.PEROXIDE)
            return 1.4F;
        if (type == Fluids.XENON)
            return 1.5F;
        if (type == Fluids.SAS3)
            return 2.0F;
        if (type == Fluids.BALEFIRE)
            return 2.5F;
        if (type == Fluids.AMAT)
            return 2.2F;
        if (type == Fluids.ASCHRAB)
            return 2.7F;
        return 0;
    }
    public boolean hasCore(){
        return getCore() != 0;
    }

    public int getCore() {
        ItemStack slot = inventory.getStackInSlot(1);
        if (slot.isEmpty()) {
            return 0;
        }

        if (slot.getItem() == ModItems.ams_core_sing)
            return 500;

        if (slot.getItem() == ModItems.ams_core_wormhole)
            return 650;

        if (slot.getItem() == ModItems.ams_core_eyeofharmony)
            return 800;

        if (slot.getItem() == ModItems.ams_core_thingy)
            return 2500;

        return 0;
    }

    //TODO: move stats to the AMSCORE class
    //Alcater: ok did that
    public int getCorePower() {
        return ItemAMSCore.getPowerBase(inventory.getStackInSlot(1));
    }

    private int calcAvgHex(int h1, int h2) {

        int r1 = ((h1 & 0xFF0000) >> 16);
        int g1 = ((h1 & 0x00FF00) >> 8);
        int b1 = ((h1 & 0x0000FF) >> 0);

        int r2 = ((h2 & 0xFF0000) >> 16);
        int g2 = ((h2 & 0x00FF00) >> 8);
        int b2 = ((h2 & 0x0000FF) >> 0);

        int r = (((r1 + r2) / 2) << 16);
        int g = (((g1 + g2) / 2) << 8);
        int b = (((b1 + b2) / 2) << 0);

        return r | g | b;
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
    public void readFromNBT(NBTTagCompound compound) {
        super.readFromNBT(compound);
        tanks[0].readFromNBT(compound, "fuel1");
        tanks[1].readFromNBT(compound, "fuel2");
        this.field = compound.getInteger("field");
        this.hasGranted = compound.getBoolean("hasGranted");
    }

    @Override
    public @NotNull NBTTagCompound writeToNBT(NBTTagCompound compound) {
        tanks[0].writeToNBT(compound, "fuel1");
        tanks[1].writeToNBT(compound, "fuel2");
        compound.setInteger("field", this.field);
        compound.setBoolean("hasGranted", this.hasGranted);
        return super.writeToNBT(compound);
    }


    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerCore(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUICore(player.inventory, this);
    }
}
