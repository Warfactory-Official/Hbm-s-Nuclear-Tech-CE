package com.hbm.items.tool;

import com.hbm.api.block.IToolable;
import com.hbm.api.block.IToolable.ToolType;
import com.hbm.handler.NTMToolHandler;
import com.hbm.handler.threading.PacketThreading;
import com.hbm.inventory.material.Mats;
import com.hbm.items.IAnimatedItem;
import com.hbm.items.ModItems;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.main.AdvancementManager;
import com.hbm.main.MainRegistry;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.particle.helper.HbmEffectNT;
import com.hbm.render.anim.BusAnimation;
import com.hbm.render.anim.BusAnimationKeyframe;
import com.hbm.render.anim.BusAnimationSequence;
import com.hbm.util.EntityDamageUtil;
import com.hbm.util.InventoryUtil;
import com.hbm.util.Tuple.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.hbm.inventory.RecipesCommon.AStack;
import static com.hbm.inventory.RecipesCommon.MetaBlock;

public class ItemBoltgun extends Item implements IAnimatedItem {

    //Takes input block and tuple of oredict bolt, output block
    public ItemBoltgun(String s) {
        this.setTranslationKey(s);
        this.setRegistryName(s);
        this.setMaxStackSize(1);
        this.setCreativeTab(MainRegistry.controlTab);

        ToolType.BOLT.register(new ItemStack(this));
        ModItems.ALL_ITEMS.add(this);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {

        World world = player.world;
        if (!entity.isEntityAlive()) return false;

        //FIXME
        //ItemStack[] bolts = new ItemStack[]{ /*new ItemStack(ModItems.bolt_spike), Mats.MAT_STEEL.make(ModItems.bolt), Mats.MAT_TUNGSTEN.make(ModItems.bolt), Mats.MAT_DURA.make(ModItems.bolt)*/};
        ItemStack[] bolts = new ItemStack[]{Mats.MAT_STEEL.make(ModItems.bolt), Mats.MAT_TUNGSTEN.make(ModItems.bolt), Mats.MAT_DURA.make(ModItems.bolt)};

        for (ItemStack bolt : bolts) {
            for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                ItemStack slot = player.inventory.getStackInSlot(i);

                if (!slot.isEmpty()) {
                    if (slot.getItem() == bolt.getItem() && slot.getItemDamage() == bolt.getItemDamage()) {
                        if (!world.isRemote) {
                            world.playSound(null, entity.posX, entity.posY, entity.posZ, HBMSoundHandler.boltgun, SoundCategory.PLAYERS, 1.0F, 1.0F);
                            player.inventory.decrStackSize(i, 1);
                            player.inventoryContainer.detectAndSendChanges();
                            EntityDamageUtil.attackEntityFromIgnoreIFrame(entity, DamageSource.causePlayerDamage(player).setDamageBypassesArmor(), 10F);

                            if (!entity.isEntityAlive() && entity instanceof EntityPlayer) {
                                AdvancementManager.grantAchievement((EntityPlayer) entity, AdvancementManager.achGoFish);
                            }

                            NBTTagCompound data = new NBTTagCompound();
                            data.setFloat("size", 1F);
                            data.setByte("count", (byte) 1);
                            PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(HbmEffectNT.VanillaExt_LargeExplode, data, entity.posX, entity.posY + entity.height / 2 - entity.getYOffset(), entity.posZ), new NetworkRegistry.TargetPoint(world.provider.getDimension(), entity.posX, entity.posY, entity.posZ, 50));
                        } else {
                            // doing this on the client outright removes the packet delay and makes the animation silky-smooth
                            NBTTagCompound d0 = new NBTTagCompound();
                            d0.setString("mode", "generic");
                            MainRegistry.proxy.effectNT(HbmEffectNT.Anim, 0, 0 ,0, d0);
                        }
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @SuppressWarnings("deprecation")
    @Override
    public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

        Block b = world.getBlockState(pos).getBlock();

        if (b instanceof IToolable && ((IToolable) b).onScrew(world, player, pos.getX(), pos.getY(), pos.getZ(), facing, hitX, hitY, hitZ, hand, ToolType.BOLT)) {

            if (!world.isRemote) {
                processNetwork(world, pos, player, facing, hitX, hitY, hitZ);
            }
            return EnumActionResult.FAIL;
        } else if (!world.isRemote && NTMToolHandler.getConversions().containsKey(new Pair<>(ToolType.BOLT, new MetaBlock(b, b.getMetaFromState(world.getBlockState(pos)))))) {


            Pair<AStack[], MetaBlock> result = NTMToolHandler.getConversions().get(new Pair<>(ToolType.BOLT, new MetaBlock(b, b.getMetaFromState(world.getBlockState(pos)))));

            if (result == null) return EnumActionResult.FAIL;
            List<AStack> materials = new ArrayList<>(Arrays.asList(result.getKey()));

            if ((materials.isEmpty() || InventoryUtil.doesPlayerHaveAStacks(player, materials, true)) && result.value.block != null) {
                IBlockState resultBlock = result.value.block.getStateFromMeta(result.value.meta);

                world.setBlockState(pos, resultBlock, 3);

                processNetwork(world, pos, player, facing, hitX, hitY, hitZ);
            }
            return EnumActionResult.FAIL;
        }
        return EnumActionResult.FAIL;
    }

    private void processNetwork(World world, BlockPos pos, EntityPlayer player, EnumFacing facing, float hitX, float hitY, float hitZ) {

        world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.boltgun, SoundCategory.PLAYERS, 1.0F, 1.0F);
        player.inventoryContainer.detectAndSendChanges();
        ForgeDirection dir = ForgeDirection.getOrientation(facing.getIndex());
        double off = 0.25;

        NBTTagCompound data = new NBTTagCompound();
        data.setFloat("size", 1F);
        data.setByte("count", (byte) 1);
        PacketThreading.createAllAroundThreadedPacket(new AuxParticlePacketNT(HbmEffectNT.VanillaExt_LargeExplode, data, pos.getX() + hitX + dir.offsetX * off, pos.getY() + hitY + dir.offsetY * off, pos.getZ() + hitZ + dir.offsetZ * off), new NetworkRegistry.TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 50));

        NBTTagCompound d0 = new NBTTagCompound();
        d0.setString("mode", "generic");
        PacketThreading.createSendToThreadedPacket(new AuxParticlePacketNT(HbmEffectNT.Anim, d0, 0, 0, 0), (EntityPlayerMP) player);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BusAnimation getAnimation(NBTTagCompound data, ItemStack stack) {
        return new BusAnimation().addBus("RECOIL", new BusAnimationSequence().addKeyframe(new BusAnimationKeyframe(1, 0, 1, 50)).addKeyframe(new BusAnimationKeyframe(0, 0, 1, 100)));
    }
}
