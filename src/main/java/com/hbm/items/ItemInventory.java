package com.hbm.items;

import com.hbm.lib.HBMSoundHandler;
import com.hbm.util.ItemStackUtil;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemStackHandler;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Random;
import java.util.zip.GZIPOutputStream;

/**
 * Base class for items containing an inventory. This can be seen in crates, containment boxes, and the toolbox.
 * @author BallOfEnergy/Gammawave, Th3_Sl1ze
 */
public class ItemInventory extends ItemStackHandler {

    public EntityPlayer player;
    public ItemStack target;

    public ItemInventory(EntityPlayer player, ItemStack target, int size) {
        super(size);
        this.player = player;
        this.target = target;

        if (this.target.getTagCompound() == null) {
            this.target.setTagCompound(new NBTTagCompound());
        }

        // Load from NBT
        ItemStack[] stacks = ItemStackUtil.readStacksFromNBT(target, getSlots());
        for (int i = 0; i < stacks.length; i++) {
            if (stacks[i] == null) this.setStackInSlot(i, ItemStack.EMPTY);
            else this.setStackInSlot(i, stacks[i]);
        }
    }

    @Override
    protected void onContentsChanged(int slot) {
        // Clean zero-sized stacks just in case
        for (int i = 0; i < getSlots(); ++i) {
            ItemStack s = getStackInSlot(i);
            if (!s.isEmpty() && s.getCount() <= 0) {
                setStackInSlot(i, ItemStack.EMPTY);
            }
        }

        // Write back inventory into target NBT (preserving other keys)
        ItemStack[] stacks = new ItemStack[getSlots()];
        for (int i = 0; i < getSlots(); i++) {
            stacks[i] = getStackInSlot(i);
        }
        ItemStackUtil.addStacksToNBT(target, stacks);
    }

    public NBTTagCompound checkNBT(NBTTagCompound nbt) {

        if (nbt == null || nbt.isEmpty())
            return null;

        Random random = new Random();

        try {
            byte[] abyte = compress(nbt);

            if (abyte.length > 6000) {
                player.sendMessage(new TextComponentString("§cWarning: Container NBT exceeds 6kB, contents will be ejected!"));

                World world = player.world;
                for (int i1 = 0; i1 < this.getSlots(); ++i1) {
                    ItemStack stack = this.getStackInSlot(i1);

                    if (!stack.isEmpty()) {
                        float f = random.nextFloat() * 0.8F + 0.1F;
                        float f1 = random.nextFloat() * 0.8F + 0.1F;
                        float f2 = random.nextFloat() * 0.8F + 0.1F;

                        while (!stack.isEmpty()) {
                            int j1 = random.nextInt(21) + 10;
                            if (j1 > stack.getCount()) {
                                j1 = stack.getCount();
                            }

                            ItemStack drop = stack.splitStack(j1);
                            EntityItem entityitem = new EntityItem(world, player.posX + f, player.posY + f1, player.posZ + f2, drop);

                            float f3 = 0.05F;
                            entityitem.motionX = (float) random.nextGaussian() * f3 + player.motionX;
                            entityitem.motionY = (float) random.nextGaussian() * f3 + 0.2F + player.motionY;
                            entityitem.motionZ = (float) random.nextGaussian() * f3 + player.motionZ;
                            world.spawnEntity(entityitem);
                        }

                        this.setStackInSlot(i1, ItemStack.EMPTY);
                    }
                }

                return null; // Reset.
            }
        } catch (IOException ignored) {}

        return nbt;
    }

    public void openInventory() {
        if (player == null) return;
        player.world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.crateOpen, SoundCategory.BLOCKS, 1.0F, 0.8F);
    }

    public void closeInventory() {
        if (player == null) return;
        player.world.playSound(null, player.posX, player.posY, player.posZ, HBMSoundHandler.crateClose, SoundCategory.BLOCKS, 1.0F, 0.8F);
    }

    public static byte[] compress(NBTTagCompound p_74798_0_) throws IOException
    {
        ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
        DataOutputStream dataoutputstream = new DataOutputStream(new GZIPOutputStream(bytearrayoutputstream));

        try
        {
            CompressedStreamTools.write(p_74798_0_, dataoutputstream);
        }
        finally
        {
            dataoutputstream.close();
        }

        return bytearrayoutputstream.toByteArray();
    }
}
