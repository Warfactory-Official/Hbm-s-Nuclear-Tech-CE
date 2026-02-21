package com.hbm.tileentity.bomb;

import com.hbm.api.item.IDesignatorItem;
import com.hbm.entity.missile.EntityMissileBaseNT;
import com.hbm.entity.missile.EntityMissileTier4;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IBomb;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.inventory.container.ContainerLaunchPadRusted;
import com.hbm.inventory.gui.GUILaunchPadRusted;
import com.hbm.items.ModItems;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.TrackerUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@AutoRegister
public class TileEntityLaunchPadRusted extends TileEntityMachineBase implements IGUIProvider, IControlReceiver, ITickable {

    public int prevRedstonePower;
    public int redstonePower;
    public Set<BlockPos> activatedBlocks = new HashSet<>(4);

    public boolean missileLoaded;

    public TileEntityLaunchPadRusted() {
        super(4);
    }

    @Override
    public String getDefaultName() {
        return "container.launchPadRusted";
    }

    @Override
    public void update() {

        if(!world.isRemote) {

            if(this.redstonePower > 0 && this.prevRedstonePower <= 0) {
                this.launch();
            }

            this.prevRedstonePower = this.redstonePower;
            this.networkPackNT(250);
        } else {

            List<EntityMissileBaseNT> entities = world.getEntitiesWithinAABB(EntityMissileBaseNT.class, new AxisAlignedBB(pos.getX() - 0.5, pos.getY(), pos.getZ() - 0.5, pos.getX() + 1.5, pos.getY() + 10, pos.getZ() + 1.5));

            if(!entities.isEmpty()) {
                for(int i = 0; i < 15; i++) {

                    ForgeDirection dir = ForgeDirection.getOrientation(this.getBlockMetadata() - 10);
                    if(world.rand.nextBoolean()) dir = dir.getOpposite();
                    if(world.rand.nextBoolean()) dir = dir.getRotation(ForgeDirection.UP);
                    float moX = (float) (world.rand.nextGaussian() * 0.15F + 0.75) * dir.offsetX;
                    float moZ = (float) (world.rand.nextGaussian() * 0.15F + 0.75) * dir.offsetZ;

                    NBTTagCompound data = new NBTTagCompound();
                    data.setDouble("posX", pos.getX() + 0.5);
                    data.setDouble("posY", pos.getY() + 0.25);
                    data.setDouble("posZ", pos.getZ() + 0.5);
                    data.setString("type", "launchSmoke");
                    data.setDouble("moX", moX);
                    data.setDouble("moY", 0);
                    data.setDouble("moZ", moZ);
                    MainRegistry.proxy.effectNT(data);

                }
            }
        }
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeBoolean(this.missileLoaded);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        this.missileLoaded = buf.readBoolean();
    }

    public IBomb.BombReturnCode launch() {

        if(!inventory.getStackInSlot(1).isEmpty() && !inventory.getStackInSlot(2).isEmpty() && !inventory.getStackInSlot(3).isEmpty() && this.missileLoaded) {
            if(inventory.getStackInSlot(1).getItem() == ModItems.launch_code && inventory.getStackInSlot(2).getItem() == ModItems.launch_key) {
                if(!inventory.getStackInSlot(3).isEmpty() && inventory.getStackInSlot(3).getItem() instanceof IDesignatorItem designator) {

                    if(!designator.isReady(world, inventory.getStackInSlot(3), pos.getX(), pos.getY(), pos.getZ())) return IBomb.BombReturnCode.ERROR_MISSING_COMPONENT;

                    Vec3d coords = designator.getCoords(world, inventory.getStackInSlot(3), pos.getX(), pos.getY(), pos.getZ());
                    int targetX = (int) Math.floor(coords.x);
                    int targetZ = (int) Math.floor(coords.z);

                    EntityMissileTier4.EntityMissileDoomsdayRusted missile = new EntityMissileTier4.EntityMissileDoomsdayRusted(world, pos.getX() + 0.5F, pos.getY() + 1F, pos.getZ() + 0.5F, targetX, targetZ);
                    world.spawnEntity(missile);
                    TrackerUtil.setTrackingRange(world, missile, 500);
                    world.playSound(null, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, HBMSoundHandler.missileTakeoff, SoundCategory.BLOCKS, 2.0F, 1.0F);
                    this.missileLoaded = false;
                    // idk if shrink will work, I'll do that for safety purposes
                    this.inventory.setStackInSlot(1, new ItemStack(inventory.getStackInSlot(1).getItem(), inventory.getStackInSlot(1).getCount() - 1));
                    this.markDirty();

                    return IBomb.BombReturnCode.LAUNCHED;
                }
            }
        }

        return IBomb.BombReturnCode.ERROR_MISSING_COMPONENT;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        this.missileLoaded = nbt.getBoolean("missileLoaded");

        this.redstonePower = nbt.getInteger("redstonePower");
        this.prevRedstonePower = nbt.getInteger("prevRedstonePower");
        NBTTagCompound activatedBlocks = nbt.getCompoundTag("activatedBlocks");
        this.activatedBlocks.clear();
        for(int i = 0; i < activatedBlocks.getKeySet().size() / 3; i++) {
            this.activatedBlocks.add(new BlockPos(activatedBlocks.getInteger("x" + i), activatedBlocks.getInteger("y" + i), activatedBlocks.getInteger("z" + i)));
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {

        nbt.setBoolean("missileLoaded", missileLoaded);

        nbt.setInteger("redstonePower", redstonePower);
        nbt.setInteger("prevRedstonePower", prevRedstonePower);
        NBTTagCompound activatedBlocks = new NBTTagCompound();
        int i = 0;
        for(BlockPos p : this.activatedBlocks) {
            activatedBlocks.setInteger("x" + i, p.getX());
            activatedBlocks.setInteger("y" + i, p.getY());
            activatedBlocks.setInteger("z" + i, p.getZ());
            i++;
        }
        nbt.setTag("activatedBlocks", activatedBlocks);
        return super.writeToNBT(nbt);
    }

    public void updateRedstonePower(BlockPos pos) {
        boolean powered = world.isBlockPowered(pos);
        boolean contained = activatedBlocks.contains(pos);
        if(!contained && powered){
            activatedBlocks.add(pos);
            if(redstonePower == -1){
                redstonePower = 0;
            }
            redstonePower++;
        } else if(contained && !powered){
            activatedBlocks.remove(pos);
            redstonePower--;
            if(redstonePower == 0){
                redstonePower = -1;
            }
        }
    }

    AxisAlignedBB bb = null;

    @Override
    public AxisAlignedBB getRenderBoundingBox() {

        if(bb == null) {
            bb = new AxisAlignedBB(
                    pos.getX() - 2,
                    pos.getY(),
                    pos.getZ() - 2,
                    pos.getX() + 3,
                    pos.getY() + 15,
                    pos.getZ() + 3
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
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerLaunchPadRusted(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUILaunchPadRusted(player.inventory, this);
    }

    @Override
    public boolean hasPermission(EntityPlayer player) {
        return this.isUseableByPlayer(player);
    }

    @Override
    public void receiveControl(NBTTagCompound data) {
        if(data.hasKey("release")) {
            if(this.missileLoaded && inventory.getStackInSlot(0).isEmpty()) {
                this.missileLoaded = false;
                inventory.setStackInSlot(0, new ItemStack(ModItems.missile_doomsday_rusted));
                this.markDirty();
            }
        }
    }
}
