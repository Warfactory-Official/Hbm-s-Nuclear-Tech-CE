package com.hbm.items.tool;

import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemBattery;
import com.hbm.util.BobMathUtil;
import java.util.List;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

public class ItemAnchorRemote extends ItemBattery {

  public ItemAnchorRemote(String name) {
    super(1_000_000, 10_000, 0, name);
  }

  @Override
  public void addInformation(
      ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {
    long maxCharge = getMaxCharge(stack);
    long charge = maxCharge;

    if (stack.hasTagCompound()) charge = getCharge(stack);

    if (stack.getItem() != ModItems.fusion_core && stack.getItem() != ModItems.energy_core) {
      list.add(
          "Energy stored: "
              + BobMathUtil.getShortNumber(charge)
              + "/"
              + BobMathUtil.getShortNumber(maxCharge)
              + "HE");
    } else {
      String charge1 = BobMathUtil.getShortNumber((charge * 100) / maxCharge);
      list.add("Charge: " + charge1 + "%");
      list.add(
          "("
              + BobMathUtil.getShortNumber(charge)
              + "/"
              + BobMathUtil.getShortNumber(maxCharge)
              + "HE)");
    }

    list.add("Charge rate: " + BobMathUtil.getShortNumber(chargeRate) + "HE/t");
  }

  @Override
  public @NotNull EnumActionResult onItemUse(
      @NotNull EntityPlayer player,
      @NotNull World world,
      @NotNull BlockPos pos,
      @NotNull EnumHand hand,
      @NotNull EnumFacing facing,
      float hitX,
      float hitY,
      float hitZ) {
    if (world.getBlockState(pos).getBlock() == ModBlocks.teleanchor) {

      ItemStack stack = player.getHeldItem(hand);

      NBTTagCompound tag = stack.getTagCompound();
      if (tag == null) {
        tag = new NBTTagCompound();
        stack.setTagCompound(tag);
      }

      tag.setInteger("x", pos.getX());
      tag.setInteger("y", pos.getY());
      tag.setInteger("z", pos.getZ());

      return EnumActionResult.SUCCESS;
    }

    return EnumActionResult.FAIL;
  }

  @Override
  public @NotNull ActionResult<ItemStack> onItemRightClick(
      @NotNull World world, @NotNull EntityPlayer player, @NotNull EnumHand hand) {
    ItemStack stack = player.getHeldItem(hand);
    if (player.isSneaking() || world.isRemote) {
      return ActionResult.newResult(EnumActionResult.FAIL, stack);
    }

    if (!stack.hasTagCompound()) {
      world.playSound(
          null,
          player.getPosition(),
          SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
          SoundCategory.BLOCKS,
          0.25F,
          0.75F);
      return ActionResult.newResult(EnumActionResult.FAIL, stack);
    }

    if (this.getCharge(stack) < 10_000) {
      world.playSound(
          null,
          player.getPosition(),
          SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
          SoundCategory.BLOCKS,
          0.25F,
          0.75F);
      return ActionResult.newResult(EnumActionResult.FAIL, stack);
    }

    NBTTagCompound tag = stack.getTagCompound();
    if (tag == null) {
      return ActionResult.newResult(EnumActionResult.FAIL, stack);
    }

    int x = tag.getInteger("x");
    int y = tag.getInteger("y");
    int z = tag.getInteger("z");

    world.getChunk(x >> 4, z >> 4);

    if (world.getBlockState(new BlockPos(x, y, z)).getBlock() == ModBlocks.teleanchor) {

      if (player.isRiding()) {
        player.dismountRidingEntity();
      }

      world.newExplosion(player, x + 0.5, y + 1 + player.height / 2, z + 0.5, 2F, false, false);
      world.playSound(
          null,
          player.getPosition(),
          SoundEvents.ENTITY_ENDERMEN_TELEPORT,
          SoundCategory.BLOCKS,
          1.0F,
          1.0F);
      player.setPositionAndUpdate(x + 0.5, y + 1, z + 0.5);
      player.fallDistance = 0.0F;

      for (int i = 0; i < 32; ++i) {
        world.spawnParticle(
            EnumParticleTypes.PORTAL,
            player.posX,
            player.posY + player.getRNG().nextDouble() * 2.0D,
            player.posZ,
            player.getRNG().nextGaussian(),
            0.0D,
            player.getRNG().nextGaussian());
      }

      this.dischargeBattery(stack, 10_000);

    } else {
      world.playSound(
          null,
          player.getPosition(),
          SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
          SoundCategory.BLOCKS,
          0.25F,
          0.75F);
    }

    return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
  }
}
