package com.hbm.tileentity.machine.rbmk;

import com.hbm.blocks.machine.rbmk.RBMKBase;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.IControlReceiver;
import com.hbm.interfaces.ICopiable;
import com.hbm.inventory.container.ContainerRBMKAutoloader;
import com.hbm.inventory.gui.GUIRBMKAutoloader;
import com.hbm.items.machine.ItemRBMKRod;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.main.MainRegistry;
import com.hbm.sound.AudioWrapper;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.TileEntityMachineBase;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@AutoRegister
public class TileEntityRBMKAutoloader extends TileEntityMachineBase implements ITickable, IGUIProvider, IControlReceiver, ICopiable {

    public volatile double piston;
    public double renderPiston;
    public double lastPiston;
    private double syncPiston;
    private int turnProgress;
    private boolean isRetracting = true;
    private int delay = 0;

    public static double speed = 0.005D;

    public volatile int cycle = 50;

    private AudioWrapper audioLift;

    public TileEntityRBMKAutoloader() {
        super(18);
    }

    @Override
    public String getDefaultName() {
        return "container.rbmkAutoloader";
    }

    @Override
    public void update() {

        if(!world.isRemote) {

            if(delay > 0) delay--;

            if(delay <= 0 && this.isRetracting && this.piston > 0D) {
                this.piston -= speed;
                if(this.piston <= 0) {
                    this.piston = 0;
                    this.delay = 40;
                }
            }

            // check for connected fuel rod and decide whether to begin working
            if(isRetracting && world.getTotalWorldTime() % 20 == 0 && this.hasFuel() && this.hasSpace()) {
                BlockPos down = pos.down();
                Block below = world.getBlockState(down).getBlock();
                if(below instanceof RBMKBase rbmkBase) {
                    BlockPos corePos = rbmkBase.findCore(world, down);
                    if(corePos != null && world.getTileEntity(corePos) instanceof TileEntityRBMKRod rod) {
                        if(rod.inventory.getStackInSlot(0).isEmpty() || rod.inventory.getStackInSlot(0).getItem() instanceof ItemRBMKRod && ItemRBMKRod.getEnrichment(rod.inventory.getStackInSlot(0)) * 100 < cycle) {
                            this.isRetracting = false;
                        }
                    }
                }
            }

            if(delay <= 0 && !this.isRetracting && this.piston < 1D) {
                this.piston += speed;
                if(this.piston >= 1) {
                    this.piston = 1;
                    this.delay = 40;
                }
            }

            // once the piston is fully extended
            if(!isRetracting && this.piston >= 1D) {
                this.piston = 1D;
                BlockPos down = pos.down();
                Block below = world.getBlockState(down).getBlock();
                if(below instanceof RBMKBase rbmkBase) {
                    BlockPos corePos = rbmkBase.findCore(world, down);
                    if(corePos != null && world.getTileEntity(corePos) instanceof TileEntityRBMKRod rod) {
                        // try to take out the old fuel rod
                        if(!rod.inventory.getStackInSlot(0).isEmpty() && this.hasSpace()) {
                            for(int i = 9; i < 18; i++) {
                                if(inventory.getStackInSlot(i).isEmpty()) {
                                    inventory.setStackInSlot(i, rod.inventory.getStackInSlot(0).copy());
                                    rod.inventory.setStackInSlot(0, ItemStack.EMPTY);
                                    break;
                                }
                            }
                        }
                        // if there's space, try and insert a new fuel rod
                        if(rod.inventory.getStackInSlot(0).isEmpty()) {
                            for(int i = 0; i < 9; i++) {
                                ItemStack stack = inventory.getStackInSlot(i);
                                if(!stack.isEmpty() && stack.getItem() instanceof ItemRBMKRod && ItemRBMKRod.getEnrichment(stack) * 100 >= cycle) {
                                    rod.inventory.setStackInSlot(0, stack.copy());
                                    inventory.setStackInSlot(i, ItemStack.EMPTY);
                                    break;
                                }
                            }
                        }

                        this.isRetracting = true;
                        this.delay = 40;
                    }
                }
            }

            this.networkPackNT(100);
        } else {

            this.lastPiston = this.renderPiston;

            if(this.turnProgress > 0) {
                this.renderPiston = this.renderPiston + ((this.syncPiston - this.renderPiston) / (double) this.turnProgress);
                --this.turnProgress;
            } else {
                this.renderPiston = this.syncPiston;
            }

            if(this.renderPiston > 0.01 && this.renderPiston < 0.99) {
                if(this.audioLift == null || !this.audioLift.isPlaying()) {
                    this.audioLift = MainRegistry.proxy.getLoopedSound(HBMSoundHandler.wgh_start, SoundCategory.BLOCKS, pos.getX(), pos.getY(), pos.getZ(), this.getVolume(0.75F), 25F, 1.0F, 5);
                    this.audioLift.startSound();
                }
                this.audioLift.updateVolume(this.getVolume(0.75F));
                this.audioLift.keepAlive();
            } else {
                if(this.audioLift != null) {
                    this.audioLift.stopSound();
                    this.audioLift = null;
                    MainRegistry.proxy.playSoundClient(pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.wgh_stop, SoundCategory.BLOCKS, this.getVolume(2F), 1F);
                }
            }

            if(this.renderPiston > 0.99) {
                NBTTagCompound data = new NBTTagCompound();
                data.setString("type", "tower");
                data.setFloat("lift", 0F);
                data.setFloat("base", 0.25F);
                data.setFloat("max", 1.5F);
                data.setInteger("life", 70 + world.rand.nextInt(30));
                data.setDouble("posX", pos.getX() + 0.5 + world.rand.nextGaussian() * 0.125);
                data.setDouble("posZ", pos.getZ() + 0.5 + world.rand.nextGaussian() * 0.125);
                data.setDouble("posY", pos.getY() + 0.25);
                data.setBoolean("noWind", true);
                data.setFloat("alphaMod", 2F);
                data.setFloat("strafe", 0.05F);
                for(int i = 0; i < 3; i++) MainRegistry.proxy.effectNT(data);
            }
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        stopLiftSound();
    }

    @Override
    public void onChunkUnload() {
        super.onChunkUnload();
        stopLiftSound();
    }

    private void stopLiftSound() {
        if (this.audioLift != null) {
            this.audioLift.stopSound();
            this.audioLift = null;
        }
    }

    public boolean hasFuel() {
        for(int i = 0; i < 9; i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if(!stack.isEmpty() && stack.getItem() instanceof ItemRBMKRod && ItemRBMKRod.getEnrichment(stack) * 100 >= cycle) {
                return true;
            }
        }

        return false;
    }

    public boolean hasSpace() {
        for(int i = 9; i < 18; i++) if(inventory.getStackInSlot(i).isEmpty()) return true;
        return false;
    }

    @Override
    public void serialize(ByteBuf buf) {
        super.serialize(buf);
        buf.writeDouble(this.piston);
        buf.writeInt(this.cycle);
    }

    @Override
    public void deserialize(ByteBuf buf) {
        super.deserialize(buf);
        this.syncPiston = buf.readDouble();
        this.cycle = buf.readInt();

        this.turnProgress = 2;
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack stack) {
        return stack.getItem() instanceof ItemRBMKRod && ItemRBMKRod.getEnrichment(stack) * 100 >= cycle && i < 9;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(EnumFacing side) {
        return this.piston <= 0 ? new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17 } : new int[0];
    }

    @Override
    public boolean canExtractItem(int i, ItemStack itemStack, int j) {
        return i >= 9;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        piston = nbt.getDouble("piston");
        isRetracting = nbt.getBoolean("ret");
        delay = nbt.getInteger("delay");
        cycle = nbt.getInteger("cycle");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setDouble("piston", piston);
        nbt.setBoolean("ret", isRetracting);
        nbt.setInteger("delay", delay);
        nbt.setInteger("cycle", cycle);
        return nbt;
    }

    protected AxisAlignedBB aabb;

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        if(aabb != null) return aabb;
        aabb = new AxisAlignedBB(pos, pos.add(1, 9, 1));
        return aabb;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }

    @Override
    public boolean hasPermission(EntityPlayer player) {
        return this.isUseableByPlayer(player);
    }

    @Override
    public void receiveControl(NBTTagCompound data) {
        if(data.hasKey("minus") && this.cycle > 5) this.cycle -= 5;
        if(data.hasKey("plus") && this.cycle < 95) this.cycle += 5;
        this.cycle = MathHelper.clamp(cycle, 5, 95);
        this.markChanged();
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerRBMKAutoloader(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIRBMKAutoloader(player.inventory, this);
    }

    @Override
    public NBTTagCompound getSettings(World world, int x, int y, int z) {
        NBTTagCompound data = new NBTTagCompound();
        data.setInteger("cycle", cycle);
        return data;
    }

    @Override
    public void pasteSettings(NBTTagCompound nbt, int index, World world, EntityPlayer player, int x, int y, int z) {
        if(nbt.hasKey("cycle")) {
            this.cycle = MathHelper.clamp(nbt.getInteger("cycle"), 5, 95);
        }
    }
}
