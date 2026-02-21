package com.hbm.items.tool;

import com.hbm.blocks.BlockDummyable;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.network.TileEntityPipelineBase;
import net.minecraft.block.Block;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemWrench extends ItemSword {

    public ItemWrench(ToolMaterial mat, String s) {
        super(mat);
        this.setTranslationKey(s);
        this.setRegistryName(s);
        this.setCreativeTab(MainRegistry.controlTab);
        ModItems.ALL_ITEMS.add(this);
    }

    @Override
    public @NotNull EnumActionResult onItemUse(EntityPlayer player, @NotNull World world, @NotNull BlockPos pos, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);

        if (player.isSneaking()) {
            return EnumActionResult.PASS;
        }

        Block block = world.getBlockState(pos).getBlock();

        if (block instanceof BlockDummyable) {
            int[] core = ((BlockDummyable) block).findCore(world, pos.getX(), pos.getY(), pos.getZ());
            if (core != null) {
                pos = new BlockPos(core[0], core[1], core[2]);
            }
        }

        TileEntity te = world.getTileEntity(pos);

        if (te instanceof TileEntityPipelineBase second) {
            NBTTagCompound tag = stack.getTagCompound();

            if (tag == null) {
                tag = new NBTTagCompound();
                tag.setInteger("x", pos.getX());
                tag.setInteger("y", pos.getY());
                tag.setInteger("z", pos.getZ());
                stack.setTagCompound(tag);

                if (!world.isRemote) {
                    player.sendMessage(new TextComponentString("Pipe start"));
                }
            } else if (!world.isRemote) {
                BlockPos firstPos = new BlockPos(tag.getInteger("x"), tag.getInteger("y"), tag.getInteger("z"));
                TileEntity firstTe = world.getTileEntity(firstPos);

                if (firstTe instanceof TileEntityPipelineBase first) {

                    switch (TileEntityPipelineBase.canConnect(first, second)) {
                        case 0:
                            first.addConnection(pos.getX(), pos.getY(), pos.getZ());
                            second.addConnection(firstPos.getX(), firstPos.getY(), firstPos.getZ());
                            player.sendMessage(new TextComponentString("Pipe end"));
                            break;
                        case 1:
                            player.sendMessage(new TextComponentString("Pipe error - Pipes are not the same type"));
                            break;
                        case 2:
                            player.sendMessage(new TextComponentString("Pipe error - Cannot connect to the same pipe anchor"));
                            break;
                        case 3:
                            player.sendMessage(new TextComponentString("Pipe error - Pipe anchor is too far away"));
                            break;
                        case 4:
                            player.sendMessage(new TextComponentString("Pipe error - Pipe anchor fluid types do not match"));
                            break;
                    }
                    stack.setTagCompound(null);
                } else {
                    player.sendMessage(new TextComponentString("Pipe error"));
                    stack.setTagCompound(null);
                }
            }

            player.swingArm(hand);
            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.PASS;
    }

    @Override
    public boolean hitEntity(@NotNull ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        World world = target.world;
        Vec3d vec = attacker.getLookVec();

        double dX = vec.x * 0.5D;
        double dY = vec.y * 0.5D;
        double dZ = vec.z * 0.5D;

        target.motionX += dX;
        target.motionY += dY;
        target.motionZ += dZ;

        world.playSound(null, target.posX, target.posY, target.posZ, SoundEvents.BLOCK_ANVIL_LAND, SoundCategory.PLAYERS, 3.0F, 0.75F);
        return false;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, @NotNull List<String> tooltip, @NotNull ITooltipFlag flagIn) {
        NBTTagCompound tag = stack.getTagCompound();

        if (tag != null) {
            tooltip.add("Pipe start x: " + tag.getInteger("x"));
            tooltip.add("Pipe start y: " + tag.getInteger("y"));
            tooltip.add("Pipe start z: " + tag.getInteger("z"));
        } else {
            tooltip.add("Right-click anchor to connect");
        }
    }

    @Override
    public void onUpdate(@NotNull ItemStack stack, World world, @NotNull Entity entity, int slot, boolean inHand) {
        if (world.isRemote && stack.getTagCompound() != null) {
            NBTTagCompound tag = stack.getTagCompound();
            Vec3d vec = new Vec3d(
                    entity.posX - tag.getInteger("x"),
                    entity.posY - tag.getInteger("y"),
                    entity.posZ - tag.getInteger("z")
            );

            MainRegistry.proxy.displayTooltip(stack.getDisplayName() + ": " + (int) vec.length() + "m");
        }
    }
}
