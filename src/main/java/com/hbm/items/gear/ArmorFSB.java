package com.hbm.items.gear;

import com.hbm.capability.HbmLivingProps;
import com.hbm.config.PotionConfig;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.interfaces.NotableComments;
import com.hbm.items.ModItems;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.render.NTMRenderHelper;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.I18nUtil;
import com.hbm.util.InventoryUtil;
import com.hbm.util.ShadyUtil;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

// mlbv: heads up! The original 1.7 version has almost all the methods expect a param of EntityPlayer.
// Alcater made it "more universally compatible" on May 2023(commit 7310c89b7e637ce28b630b6eb193a1968ee4e60d),
// by making them accept EntityLivingBase instead and moving update methods to onLivingUpdate.
// This is hereby reverted by me since I'd rather trade compatibility for convenience.
@NotableComments
public class ArmorFSB extends ItemArmor {


    public List<PotionEffect> effects = new ArrayList<>();
    public boolean noHelmet = false;
    public boolean vats = false;
    public boolean thermal = false;
    public boolean geigerSound = false;
    public boolean customGeiger = false;
    public boolean hardLanding = false;
    public int dashCount = 0;
    public int stepSize = 0;
    public SoundEvent step;
    public SoundEvent jump;
    public SoundEvent fall;
    private String texture = "";
    private ResourceLocation overlay = null;


    public ArmorFSB(ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot equipmentSlotIn, String texture, String name) {
        super(materialIn, renderIndexIn, equipmentSlotIn);
        this.setTranslationKey(name);
        this.setRegistryName(name);
        this.texture = texture;

        ModItems.ALL_ITEMS.add(this);
    }

    public static boolean hasFSBArmor(EntityPlayer entity) {
        if (entity == null)
            return false;

        ItemStack plate = entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);

        if (!plate.isEmpty() && plate.getItem() instanceof ArmorFSB chestplate) {

            boolean noHelmet = chestplate.noHelmet;

            for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
                if (slot == EntityEquipmentSlot.MAINHAND || slot == EntityEquipmentSlot.OFFHAND)
                    continue;
                if (noHelmet && slot == EntityEquipmentSlot.HEAD)
                    continue;
                ItemStack armor = entity.getItemStackFromSlot(slot);

                if (armor.isEmpty() || !(armor.getItem() instanceof ArmorFSB armorFSB))
                    return false;

                if (armorFSB.getArmorMaterial() != chestplate.getArmorMaterial())
                    return false;

                if (!armorFSB.isArmorEnabled(armor))
                    return false;
            }
            return true;
        }

        return false;
    }

    public static boolean hasFSBArmorHelmet(EntityPlayer entity) {
        ItemStack plate = entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);

        if (!plate.isEmpty() && plate.getItem() instanceof ArmorFSB chestplate) {
            return !chestplate.noHelmet && hasFSBArmor(entity);
        }
        return false;
    }

    public static boolean hasFSBArmorIgnoreCharge(EntityPlayer entity) {
        if (entity == null)
            return false;

        ItemStack plate = entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);

        if (!plate.isEmpty() && plate.getItem() instanceof ArmorFSB chestplate) {
            boolean noHelmet = chestplate.noHelmet;

            for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
                if (slot == EntityEquipmentSlot.MAINHAND || slot == EntityEquipmentSlot.OFFHAND)
                    continue;
                if (noHelmet && slot == EntityEquipmentSlot.HEAD)
                    continue;
                ItemStack armor = entity.getItemStackFromSlot(slot);

                if (armor.isEmpty() || !(armor.getItem() instanceof ArmorFSB armorFSB))
                    return false;

                if (armorFSB.getArmorMaterial() != chestplate.getArmorMaterial())
                    return false;
            }
            return true;
        }

        return false;
    }

    public void handleAttack(LivingAttackEvent event) {
    }

    public void handleHurt(LivingHurtEvent event) {
    }

    public void handleTick(TickEvent.PlayerTickEvent event) {
        // This phase guard doesn't exist in 1.7, remove if it's problematic
        if (event.phase != TickEvent.Phase.START) return;
        EntityPlayer player = event.player;
        boolean step;
        if(player.getUniqueID().equals(ShadyUtil.the_NCR) || player.getUniqueID().equals(ShadyUtil.Barnaby99_x)) {
            step = false;

            if(player.world.isRemote && player.onGround) {
                steppy(player, HBMSoundHandler.poweredStep);
            }
        } else step = true;

        if (ArmorFSB.hasFSBArmor(player)) {

            ItemStack plate = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);

            ArmorFSB chestplate = (ArmorFSB) plate.getItem();

            if (!chestplate.effects.isEmpty()) {

                for (PotionEffect i : chestplate.effects) {
                    player.addPotionEffect(new PotionEffect(i.getPotion(), i.getDuration(), i.getAmplifier(), i.getIsAmbient(), false));//Norwood: I prefer not to have particles show with armor on
                }
            }


            if (step && chestplate.step != null && player.world.isRemote && player.onGround && !player.isSneaking()) {
                steppy(player, chestplate.step);
            }
        }
    }

    private static void steppy(EntityPlayer player, SoundEvent sound) {
        if (player.getEntityData().getFloat("hfr_nextStepDistance") == 0) {
            player.getEntityData().setFloat("hfr_nextStepDistance", player.nextStepDistance);
        }

        int px = MathHelper.floor(player.posX);
        int py = MathHelper.floor(player.posY - 0.2D);
        int pz = MathHelper.floor(player.posZ);
        IBlockState block = player.world.getBlockState(new BlockPos(px, py, pz));
        if (block.getMaterial() != Material.AIR && player.getEntityData().getFloat("hfr_nextStepDistance") <= player.distanceWalkedOnStepModified) {
            player.playSound(sound, 1.0F, 1.0F);
        }

        player.getEntityData().setFloat("hfr_nextStepDistance", player.nextStepDistance);
    }

    public static void handleJump(EntityPlayer entity) {

        if (ArmorFSB.hasFSBArmor(entity)) {

            ArmorFSB chestplate = (ArmorFSB) entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem();

            if (chestplate.jump != null)
                entity.playSound(chestplate.jump, 1.0F, 1.0F);
        }
    }

    public static void handleFall(EntityPlayer player) {

        if (ArmorFSB.hasFSBArmor(player)) {

            ArmorFSB chestplate = (ArmorFSB) player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem();

            if (chestplate.hardLanding && player.fallDistance > 10) {

                List<Entity> entities = player.world.getEntitiesWithinAABBExcludingEntity(player, player.getEntityBoundingBox().grow(3, 0, 3));

                for (Entity e : entities) {
                    if(e instanceof EntityItem) continue;
                    Vec3d vec = new Vec3d(player.posX - e.posX, 0, player.posZ - e.posZ);

                    if (vec.length() < 3) {

                        double intensity = 3 - vec.length();
                        e.motionX += vec.x * intensity * -2;
                        e.motionY += 0.1D * intensity;
                        e.motionZ += vec.z * intensity * -2;

                        e.attackEntityFrom(DamageSource.causePlayerDamage(player).setDamageBypassesArmor(), (float) (intensity * 10));
                    }
                }
                // return;
            }

            if (chestplate.fall != null && player.fallDistance > 0.25) {
                player.playSound(chestplate.fall, 1.0F, 1.0F);
            }
        }
    }

    @Override
    public void onArmorTick(World world, EntityPlayer entity, ItemStack stack) {

        if (this.armorType != EntityEquipmentSlot.CHEST) return;
        if (!hasFSBArmor(entity) || !this.geigerSound) return;
        if(InventoryUtil.hasItem(entity, ModItems.geiger_counter) || InventoryUtil.hasItem(entity, ModItems.dosimeter)) return;
        if(world.getTotalWorldTime() % 5 == 0) {
            float x = (float) ContaminationUtil.getActualPlayerRads(entity);

            if(x > 1e-5) {
                IntArrayList list = new IntArrayList();

                if(x < 1) list.add(0);
                if(x < 5) list.add(0);
                if(x < 10) list.add(1);
                if(x > 5 && x < 15) list.add(2);
                if(x > 10 && x < 20) list.add(3);
                if(x > 15 && x < 25) list.add(4);
                if(x > 20 && x < 30) list.add(5);
                if(x > 25) list.add(6);

                int r = list.getInt(world.rand.nextInt(list.size()));

                if(r > 0) world.playSound(null, entity.posX, entity.posY, entity.posZ, HBMSoundHandler.geigerSounds[r-1], SoundCategory.PLAYERS, 1.0F, 1.0F);
            }
        }
    }

    public static int check(World world, BlockPos pos) {
        return (int) Math.ceil(ChunkRadiationManager.proxy.getRadiation(world, pos));
    }

    public boolean isArmorEnabled(ItemStack stack) {
        return true;
    }

    //For crazier stuff not possible without hooking the event
    @SideOnly(Side.CLIENT)
    public void handleOverlay(RenderGameOverlayEvent.Pre event, EntityPlayer player) {
    }

    public ArmorFSB enableThermalSight(boolean thermal) {
        this.thermal = thermal;
        return this;
    }

    public ArmorFSB setHasGeigerSound(boolean geiger) {
        this.geigerSound = geiger;
        return this;
    }

    public ArmorFSB setHasCustomGeiger(boolean geiger) {
        this.customGeiger = geiger;
        return this;
    }

    public ArmorFSB setHasHardLanding(boolean hardLanding) {
        this.hardLanding = hardLanding;
        return this;
    }


    public ArmorFSB setDashCount(int dashCount) {
        this.dashCount = dashCount;
        return this;
    }

    public ArmorFSB setStepSize(int stepSize) {
        this.stepSize = stepSize;
        return this;
    }

    public ArmorFSB setStep(SoundEvent step) {
        this.step = step;
        return this;
    }

    public ArmorFSB setJump(SoundEvent jump) {
        this.jump = jump;
        return this;
    }

    public ArmorFSB setFall(SoundEvent fall) {
        this.fall = fall;
        return this;
    }

    public ArmorFSB addEffect(PotionEffect effect) {
        if (!PotionConfig.doJumpBoost && effect.getPotion() == MobEffects.JUMP_BOOST)
            return this;
        effects.add(effect);
        return this;
    }

    public ArmorFSB setNoHelmet(boolean noHelmet) {
        this.noHelmet = noHelmet;
        return this;
    }

    public ArmorFSB enableVATS(boolean vats) {
        this.vats = vats;
        return this;
    }


    public ArmorFSB setOverlay(String path) {
        this.overlay = new ResourceLocation(path);
        return this;
    }

    public ArmorFSB cloneStats(ArmorFSB original) {
        //lists aren't being modified after instantiation, so there's no need to dereference
        this.effects = original.effects;
        this.noHelmet = original.noHelmet;
        this.vats = original.vats;
        this.thermal = original.thermal;
        this.geigerSound = original.geigerSound;
        this.customGeiger = original.customGeiger;
        this.hardLanding = original.hardLanding;
        this.dashCount = original.dashCount;
        this.stepSize = original.stepSize;
        this.step = original.step;
        this.jump = original.jump;
        this.fall = original.fall;
        //overlay doesn't need to be copied because it's helmet exclusive
        return this;
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        return texture;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {

        List toAdd = new ArrayList();

        if (!effects.isEmpty()) {
            List potionList = new ArrayList();
            for (PotionEffect effect : effects) {
                potionList.add(I18n.format(effect.getEffectName()));
            }

            toAdd.add(TextFormatting.AQUA + String.join(", ", potionList));
        }

        if (geigerSound) toAdd.add(TextFormatting.GOLD + "  " + I18nUtil.resolveKey("armor.geigerSound"));
        if (customGeiger) toAdd.add(TextFormatting.GOLD + "  " + I18nUtil.resolveKey("armor.geigerHUD"));
        if (vats) toAdd.add(TextFormatting.RED + "  " + I18nUtil.resolveKey("armor.vats"));
        if (thermal) toAdd.add(TextFormatting.RED + "  " + I18nUtil.resolveKey("armor.thermal"));
        if (hardLanding) toAdd.add(TextFormatting.RED + "  " + I18nUtil.resolveKey("armor.hardLanding"));
        if (stepSize != 0) toAdd.add(TextFormatting.BLUE + "  " + I18nUtil.resolveKey("armor.stepSize", stepSize));
        if (dashCount > 0) toAdd.add(TextFormatting.AQUA + "  " + I18nUtil.resolveKey("armor.dash", dashCount));

        if (!toAdd.isEmpty()) {
            list.add(TextFormatting.GOLD + I18nUtil.resolveKey("armor.fullSetBonus"));
            list.addAll(toAdd);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderHelmetOverlay(ItemStack stack, EntityPlayer player, ScaledResolution resolution, float partialTicks) {
        if (overlay == null)
            return;
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableAlpha();
        Minecraft.getMinecraft().getTextureManager().bindTexture(overlay);
        NTMRenderHelper.startDrawingTexturedQuads();
        NTMRenderHelper.addVertexWithUV(0.0D, (double) resolution.getScaledHeight(), -90.0D, 0.0D, 1.0D);
        NTMRenderHelper.addVertexWithUV((double) resolution.getScaledWidth(), (double) resolution.getScaledHeight(), -90.0D, 1.0D, 1.0D);
        NTMRenderHelper.addVertexWithUV((double) resolution.getScaledWidth(), 0.0D, -90.0D, 1.0D, 0.0D);
        NTMRenderHelper.addVertexWithUV(0.0D, 0.0D, -90.0D, 0.0D, 0.0D);
        NTMRenderHelper.draw();
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableAlpha();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

}
