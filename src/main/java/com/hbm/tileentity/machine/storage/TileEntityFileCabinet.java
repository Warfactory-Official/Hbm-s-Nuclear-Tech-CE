package com.hbm.tileentity.machine.storage;

import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.container.ContainerFileCabinet;
import com.hbm.inventory.gui.GUIFileCabinet;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.tileentity.IBufPacketReceiver;
import com.hbm.tileentity.IGUIProvider;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@AutoRegister
public class TileEntityFileCabinet extends TileEntityCrateBase implements IGUIProvider, IBufPacketReceiver, ITickable {

    private int timer = 0;
    private int playersUsing = 0;
    //meh, it's literally just two extra variables
    public float lowerExtent = 0; //i don't know a term for how 'open' something is
    public float prevLowerExtent = 0;
    public float upperExtent = 0;
    public float prevUpperExtent = 0;

    public TileEntityFileCabinet() {
        super(8);
    }

    @Override
    public String getName() {
        return this.hasCustomName() ? this.customName : "container.fileCabinet";
    }

    @Override
    public void openInventory(EntityPlayer player) {
        if(!world.isRemote && !player.isSpectator()) playersUsing++;
    }

    @Override
    public void closeInventory(EntityPlayer player) {
        if(!world.isRemote && !player.isSpectator()) playersUsing--;
    }

    @Override public void serialize(ByteBuf buf) {
        buf.writeInt(timer);
        buf.writeInt(playersUsing);
    }

    @Override public void deserialize(ByteBuf buf) {
        timer = buf.readInt();
        playersUsing = buf.readInt();
    }

    @Override
    public void update() {

        if(!world.isRemote) {

            if(this.playersUsing > 0) {
                if(timer < 10) {
                    timer++;
                }
            } else
                timer = 0;

            networkPackNT(25);
        } else {
            this.prevLowerExtent = lowerExtent;
            this.prevUpperExtent = upperExtent;
        }

        float openSpeed = playersUsing > 0 ? 1F / 16F : 1F / 25F;
        float maxExtent = 0.8F;

        if(this.playersUsing > 0) {
            if(lowerExtent == 0F && upperExtent == 0F)
                this.world.playSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, HBMSoundHandler.crateOpen, SoundCategory.BLOCKS, 0.8F, 1.0F, false);
            else {
                if(upperExtent + openSpeed >= maxExtent && lowerExtent < maxExtent) {
                    this.world.playSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, HBMSoundHandler.crateOpen, SoundCategory.BLOCKS, 0.5F, this.world.rand.nextFloat() * 0.1F + 0.7F, false);
                }

                if(lowerExtent + openSpeed >= maxExtent && lowerExtent < maxExtent) {
                    this.world.playSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, HBMSoundHandler.crateOpen, SoundCategory.BLOCKS, 0.5F, this.world.rand.nextFloat() * 0.1F + 0.7F, false);
                }
            }

            this.lowerExtent += openSpeed;

            if(timer >= 10) {
                this.upperExtent += openSpeed;
            }

        } else if(lowerExtent > 0) {
            if(upperExtent - openSpeed < maxExtent / 2 && upperExtent >= maxExtent / 2 && upperExtent != lowerExtent) {
                this.world.playSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, HBMSoundHandler.crateClose, SoundCategory.BLOCKS, 0.8F, 1.0F, false);
            }

            if(lowerExtent - openSpeed < maxExtent / 2 && lowerExtent >= maxExtent / 2) {
                this.world.playSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, HBMSoundHandler.crateClose, SoundCategory.BLOCKS, 0.8F, 1.0F, false);
            }

            this.upperExtent -= openSpeed;
            this.lowerExtent -= openSpeed;
        }

        this.lowerExtent = MathHelper.clamp(lowerExtent, 0F, maxExtent);
        this.upperExtent = MathHelper.clamp(upperExtent, 0F, maxExtent);
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new ContainerFileCabinet(player.inventory, this);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIFileCabinet(player.inventory, this);
    }

    //No automation, it's a filing cabinet.
    @Override
    public boolean isItemValidForSlot(int i, ItemStack stack) {
        return false;
    }

    @Override
    public boolean canInsertItem(int i, ItemStack itemStack, int j) {
        return false;
    }

    @Override
    public boolean canExtractItem(int i, ItemStack itemStack, int j) {
        return false;
    }

    AxisAlignedBB bb = null;

    @Override
    public AxisAlignedBB getRenderBoundingBox() {

        if(bb == null) {
            bb = new AxisAlignedBB(
                    pos.getX() - 1,
                    pos.getY(),
                    pos.getZ() - 1,
                    pos.getX() + 1,
                    pos.getY() + 1,
                    pos.getZ() + 1
            );
        }

        return bb;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return 65536.0D;
    }
}
