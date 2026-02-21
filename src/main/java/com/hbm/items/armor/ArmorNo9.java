package com.hbm.items.armor;

import com.hbm.capability.HbmCapability;
import com.hbm.capability.HbmLivingProps;
import com.hbm.items.gear.ArmorModel;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.render.model.ModelNo9;
import com.hbm.util.Tuple;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;

public class ArmorNo9 extends ArmorModel implements IAttackHandler, IDamageHandler {

    @SideOnly(Side.CLIENT)
    protected ModelNo9 model;

    public ArmorNo9(ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot equipmentSlotIn, String s) {
        super(materialIn, renderIndexIn, equipmentSlotIn, s);
        this.setMaxDamage(0);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World world, List<String> list, ITooltipFlag flagIn) {
        list.add(TextFormatting.BLUE + "+0.5 DT");
        list.add(TextFormatting.YELLOW + "Lets you breathe coal, neat!");
    }

    @Override
    public void handleDamage(LivingHurtEvent event, ItemStack stack) {

        if(event.getSource().isUnblockable())
            return;

        event.setAmount(event.getAmount() -0.5F);

        if(event.getAmount() < 0)
            event.setAmount(0);
    }

    @Override
    public void handleAttack(LivingAttackEvent event, ItemStack armor) {

        if(event.getSource().isUnblockable())
            return;

        if(event.getAmount() <= 0.5F) {
            event.setCanceled(true);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, EntityEquipmentSlot armorSlot, ModelBiped _default) {

        if(model == null) {
            model = new ModelNo9(0);
        }

        return model;
    }

    @Override
    public void onArmorTick(World world, EntityPlayer player, ItemStack armor) {

        if(!world.isRemote) {
            if(!armor.hasTagCompound()) {
                armor.setTagCompound(new NBTTagCompound());
            }

            boolean turnOn = HbmCapability.getData(player).getEnableHUD();
            boolean wasOn = armor.getTagCompound().getBoolean("isOn");
            BlockPos pos = player.getPosition();
            if(turnOn && !wasOn) world.playSound(player, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.ITEM_FLINTANDSTEEL_USE, SoundCategory.PLAYERS, 1F, 1.5F);
            if(!turnOn && wasOn) world.playSound(player, pos.getX(), pos.getY(), pos.getZ(), SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS, 0.5F, 2F);
            armor.getTagCompound().setBoolean("isOn", turnOn); // a crude way of syncing the "enableHUD" prop to other players is just by piggybacking off the NBT sync

            if(HbmLivingProps.getBlackLung(player) > HbmLivingProps.maxBlacklung * 0.9) {
                HbmLivingProps.setBlackLung(player, (int) (HbmLivingProps.maxBlacklung * 0.9));
            }

            if(HbmLivingProps.getBlackLung(player) >= HbmLivingProps.maxBlacklung * 0.25) {
                HbmLivingProps.setBlackLung(player, HbmLivingProps.getBlackLung(player) - 1);
            }
        }

        if(world.isRemote && world.getTotalWorldTime() % 2 == 0 && armor.hasTagCompound() && armor.getTagCompound().getBoolean("isOn")) {
            checkLights(world, false);
            float range = 50F;
            RayTraceResult mop = Library.rayTrace(player, range, 0F, false, true, false);

            if(mop != null && mop.typeOfHit == mop.typeOfHit.BLOCK) {
                Vec3d look = new Vec3d(player.posX - mop.hitVec.x, player.posY + player.getEyeHeight() - mop.hitVec.y, player.posZ - mop.hitVec.z);
                ForgeDirection dir = ForgeDirection.getOrientation(mop.sideHit);
                int level = Math.min(15, (int) (25 - (look.length() * 25 / range)));
                lightUpRecursively(world, mop.getBlockPos().add(dir.offsetX, dir.offsetY, dir.offsetZ), level);
                breadcrumb.clear();
            }
        }
    }

    public static Set<BlockPos> breadcrumb = new HashSet<>();
    public static void lightUpRecursively(World world, BlockPos pos, int light) {
        if(light <= 0) return;
        if(breadcrumb.contains(pos)) return;
        breadcrumb.add(pos);

        int existingLight = world.getLightFor(EnumSkyBlock.BLOCK, pos);
        int occupancy = world.getBlockLightOpacity(pos);

        if(occupancy >= 255) return; // only block if it's fully blocking, light goes through semi-translucent blocks like it's air

        int newLight = Math.min(15, Math.max(existingLight, light));
        world.setLightFor(EnumSkyBlock.BLOCK, pos, newLight);
        lightCheck.put(new Tuple.Pair<>(world, pos), world.getTotalWorldTime() + 5);

        lightUpRecursively(world, pos.add(1, 0, 0), light - 1);
        lightUpRecursively(world, pos.add(-1, 0, 0), light - 1);
        lightUpRecursively(world, pos.up(), light - 1);
        lightUpRecursively(world, pos.down(), light - 1);
        lightUpRecursively(world, pos.add(0, 0, 1), light - 1);
        lightUpRecursively(world, pos.add(0, 0, -1), light - 1);
    }

    public static HashMap<World, Long> lastChecks = new HashMap<>();
    public static HashMap<Tuple.Pair<World, BlockPos>, Long> lightCheck = new HashMap<>();

    public static void checkLights(World world, boolean force) {
        Iterator it = lightCheck.entrySet().iterator();

        while(it.hasNext()) {
            Map.Entry<Tuple.Pair<World, BlockPos>, Long> entry = (Map.Entry<Tuple.Pair<World, BlockPos>, Long>) it.next();

            if(entry.getKey().getKey() == world && (world.getTotalWorldTime() > entry.getValue() || force)) {
                BlockPos pos = entry.getKey().getValue();
                world.checkLightFor(EnumSkyBlock.BLOCK, pos);
                it.remove();
            }
        }

        lastChecks.put(world, world.getTotalWorldTime());
    }

    public static void updateWorldHook(World world) {
        if(world == null || !world.isRemote) return;
        Long last = lastChecks.get(world);

        if(last != null && last < world.getTotalWorldTime() + 15) {
            checkLights(world, false);
        }
    }
}
