package com.hbm.items.tool;

import com.hbm.blocks.turret.TurretBaseNT;
import com.hbm.items.ModItems;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.tileentity.turret.TileEntityTurretBaseNT;
import com.hbm.util.I18nUtil;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.ArrowNockEvent;

import java.util.List;

public class ItemTurretControl extends Item {

    public ItemTurretControl(String s) {
        this.setTranslationKey(s);
        this.setRegistryName(s);
        this.setCreativeTab(MainRegistry.weaponTab);

        ModItems.ALL_ITEMS.add(this);
    }


    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {
        if (stack.getTagCompound() != null) {
            list.add(I18nUtil.resolveKey("desc.turrectcontrol"));
            list.add("X: " + stack.getTagCompound().getInteger("xCoord"));
            list.add("Y: " + stack.getTagCompound().getInteger("yCoord"));
            list.add("Z: " + stack.getTagCompound().getInteger("zCoord"));
        } else {
            list.add(I18nUtil.resolveKey("desc.turrectcontrol.noconnect"));
        }
    }

    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        boolean valid = false;
        int x, y, z;
        x = y = z = 0;
        if (worldIn.getBlockState(pos).getBlock() instanceof TurretBaseNT) {
            int[] cPos = ((TurretBaseNT) worldIn.getBlockState(pos).getBlock()).findCore(worldIn, pos.getX(), pos.getY(), pos.getZ());
            if (pos != null) {
                x = cPos[0];
                y = cPos[1];
                z = cPos[2];

                valid = true;
            }
        }

        if (valid) {
            ItemStack stack = player.getHeldItem(hand);
            if (stack.getTagCompound() != null) {
                stack.getTagCompound().setInteger("xCoord", x);
                stack.getTagCompound().setInteger("yCoord", y);
                stack.getTagCompound().setInteger("zCoord", z);
            } else {
                stack.setTagCompound(new NBTTagCompound());
                stack.getTagCompound().setInteger("xCoord", x);
                stack.getTagCompound().setInteger("yCoord", y);
                stack.getTagCompound().setInteger("zCoord", z);
            }
            if (worldIn.isRemote) {
                player.sendMessage(new TextComponentTranslation("chat.turretcontrol.linked"));
            }

            worldIn.playSound(player.posX, player.posY, player.posZ, HBMSoundHandler.techBleep, SoundCategory.PLAYERS, 1.0F, 1.0F, false);

            return EnumActionResult.SUCCESS;
        }

        return EnumActionResult.PASS;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.BOW;
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World worldIn, EntityLivingBase entityLiving, int timeLeft) {
        int j = this.getMaxItemUseDuration(stack) - timeLeft;
        if (entityLiving instanceof EntityPlayer) {
            ArrowLooseEvent event = new ArrowLooseEvent((EntityPlayer) entityLiving, stack, worldIn, j, false);
            MinecraftForge.EVENT_BUS.post(event);

            j = event.getCharge();
        }
        if (stack.hasTagCompound()) {
            int x = stack.getTagCompound().getInteger("xCoord");
            int y = stack.getTagCompound().getInteger("yCoord");
            int z = stack.getTagCompound().getInteger("zCoord");

        }
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World worldIn, EntityPlayer playerIn, EnumHand handIn) {
        ArrowNockEvent event = new ArrowNockEvent(playerIn, playerIn.getHeldItem(handIn), handIn, worldIn, false);
        MinecraftForge.EVENT_BUS.post(event);
        {
            playerIn.setActiveHand(handIn);
        }

        return super.onItemRightClick(worldIn, playerIn, handIn);
    }

    @Override
    public void onUsingTick(ItemStack stack, EntityLivingBase mob, int count) {
        World world = mob.world;

        if (!world.isRemote && stack.hasTagCompound()) {
            int x = stack.getTagCompound().getInteger("xCoord");
            int y = stack.getTagCompound().getInteger("yCoord");
            int z = stack.getTagCompound().getInteger("zCoord");
            BlockPos pos = new BlockPos(x, y, z);

            if ((world.getBlockState(pos).getBlock() instanceof TurretBaseNT) && (mob instanceof EntityPlayer player)) {
                TileEntity te = world.getTileEntity(pos);

                if (te != null && te instanceof TileEntityTurretBaseNT turret) {

                    RayTraceResult rpos = Library.rayTrace(player, 200, 1, true, true, false);

                    if (pos == null)
                        rpos = Library.rayTrace(player, 200, 1);

                    if (pos != null && rpos != null) {

                        Vec3 vecOrigin = Vec3.createVectorHelper(player.posX, player.posY + player.eyeHeight - player.getYOffset(), player.posZ);
                        Vec3 vecDestination = Vec3.createVectorHelper(rpos.getBlockPos().getX(), rpos.getBlockPos().getY(), rpos.getBlockPos().getZ());

                        List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(player, player.getEntityBoundingBox().grow(200, 200, 200));

                        for (Entity e : list) {

                            if (e.canBeCollidedWith() && e != player) {

                                RayTraceResult mop = e.getEntityBoundingBox().expand(0.2, 0.2, 0.2).calculateIntercept(vecOrigin.toVec3d(), vecDestination.toVec3d());

                                if (mop != null) {

                                    rpos = mop;
                                    rpos.typeOfHit = RayTraceResult.Type.ENTITY;
                                    rpos.entityHit = e;
                                }
                            }
                        }

                        if (rpos.typeOfHit == RayTraceResult.Type.ENTITY) {
                            turret.target = rpos.entityHit;
                            turret.turnTowards(turret.getEntityPos(rpos.entityHit));
                            world.playSound(player.posX, player.posY, player.posZ, HBMSoundHandler.techBleep, SoundCategory.PLAYERS, 1.0F, 1.0F, false);
                        } else if (rpos.typeOfHit == RayTraceResult.Type.BLOCK) {
                            turret.tPos = vecDestination.toVec3d();
                            turret.manualOverride = true;
                            turret.turnTowards(Vec3.createVectorHelper(rpos.getBlockPos().getX() + 0.5, rpos.getBlockPos().getY() + 0.5, rpos.getBlockPos().getZ() + 0.5).toVec3d());
                        }
                    }
                }
            }
        }
    }
}
