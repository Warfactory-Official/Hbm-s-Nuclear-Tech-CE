package com.hbm.items.armor;

import com.hbm.lib.HBMSoundHandler;
import com.hbm.render.anim.sedna.BusAnimationKeyframeSedna;
import com.hbm.render.anim.sedna.BusAnimationSedna;
import com.hbm.render.anim.sedna.BusAnimationSequenceSedna;
import com.hbm.render.anim.sedna.HbmAnimationsSedna;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.items.weapon.sedna.ItemGunBaseNT.LambdaContext;
import com.hbm.items.weapon.sedna.factory.ConfettiUtil;
import com.hbm.items.weapon.sedna.factory.XFactoryPA;
import com.hbm.main.ResourceManager;
import com.hbm.util.EntityDamageUtil;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;

public class ArmorRPAMelee implements IPAMelee {

    @Override public void clickPrimary(ItemStack stack, LambdaContext ctx) { XFactoryPA.doSwing(stack, ctx, HbmAnimationsSedna.GunAnimation.CYCLE, 14); }
    @Override public void clickSecondary(ItemStack stack, LambdaContext ctx) { XFactoryPA.doSwing(stack, ctx, HbmAnimationsSedna.GunAnimation.ALT_CYCLE, 20); }

    @Override
    public void orchestra(ItemStack stack, LambdaContext ctx) {
        EntityLivingBase entity = ctx.entity;
        if(entity.world.isRemote) return;
        HbmAnimationsSedna.GunAnimation type = ItemGunBaseNT.getLastAnim(stack, ctx.configIndex);
        int timer = ItemGunBaseNT.getAnimTimer(stack, ctx.configIndex);

        // refire check so you can just continuously beat the shit out of someone
        if(type == HbmAnimationsSedna.GunAnimation.CYCLE && timer == 14 && ItemGunBaseNT.getPrimary(stack, 0)) {
            XFactoryPA.doSwing(stack, ctx, HbmAnimationsSedna.GunAnimation.CYCLE, 14);
        }

        boolean swings = type == HbmAnimationsSedna.GunAnimation.CYCLE && (timer == 3 || timer == 9);
        boolean slap = type == HbmAnimationsSedna.GunAnimation.ALT_CYCLE && timer == 8;

        if((swings || slap) && ctx.getPlayer() != null) {
            RayTraceResult mop = EntityDamageUtil.getMouseOver(ctx.getPlayer(), 3.0D, 0.5D);

            if(mop != null && mop.typeOfHit != null) {
                if(mop.typeOfHit == RayTraceResult.Type.ENTITY) {
                    float damage = swings ? 15F : 35F;
                    float knockback = swings ? 0F : 1.5F;
                    float dt = swings ? 5F : 15F;
                    float pierce = swings ? 0.1F : 0.25F;

                    if(mop.entityHit instanceof EntityLivingBase living) {
                        if(living.getMaxHealth() >= 100) damage *= 2.5F;
                        EntityDamageUtil.attackEntityFromNT(living, DamageSource.causePlayerDamage(ctx.getPlayer()), damage, true, false, knockback, dt, pierce);
                        if(living.getRNG().nextInt(slap ? 3 : 10) == 0 && !living.isEntityAlive()) ConfettiUtil.gib(living);
                    } else {
                        mop.entityHit.attackEntityFrom(DamageSource.causePlayerDamage(ctx.getPlayer()), damage);
                    }

                    entity.world.playSound(null, mop.entityHit.getPosition(), HBMSoundHandler.smack, SoundCategory.PLAYERS, 1F, 0.9F + entity.getRNG().nextFloat() * 0.2F);
                }
                if(mop.typeOfHit == RayTraceResult.Type.BLOCK) {
                    BlockPos pos = mop.getBlockPos();
                    IBlockState state = entity.world.getBlockState(pos);
                    Block b = state.getBlock();
                    entity.world.playSound(null, mop.hitVec.x, mop.hitVec.y, mop.hitVec.z, b.getSoundType(state, entity.world, pos, entity).getStepSound(), SoundCategory.BLOCKS, 2F, 0.9F + entity.getRNG().nextFloat() * 0.2F);
                }
            }
        }
    }

    @Override
    public BusAnimationSedna playAnim(ItemStack stack, HbmAnimationsSedna.GunAnimation type) {
        if(type == HbmAnimationsSedna.GunAnimation.EQUIP) return new BusAnimationSedna()
                .addBus("EQUIP", new BusAnimationSequenceSedna().setPos(-1, 0, 0).addPos(0, 0, 0, 250, BusAnimationKeyframeSedna.IType.SIN_DOWN));
        if(type == HbmAnimationsSedna.GunAnimation.CYCLE) return new BusAnimationSedna()
                .addBus("SWINGRIGHT", new BusAnimationSequenceSedna().addPos(1, 0, 0, 150, BusAnimationKeyframeSedna.IType.SIN_DOWN).addPos(0, 0, 0, 250, BusAnimationKeyframeSedna.IType.SIN_FULL))
                .addBus("SWINGLEFT", new BusAnimationSequenceSedna().addPos(0, 0, 0, 300).addPos(1, 0, 0, 150, BusAnimationKeyframeSedna.IType.SIN_DOWN).addPos(0, 0, 0, 250, BusAnimationKeyframeSedna.IType.SIN_FULL));
        if(type == HbmAnimationsSedna.GunAnimation.ALT_CYCLE) return new BusAnimationSedna()
                .addBus("SLAPTURN", new BusAnimationSequenceSedna().addPos(1, 0, 0, 250, BusAnimationKeyframeSedna.IType.LINEAR).hold(150).addPos(0, 0, 0, 350, BusAnimationKeyframeSedna.IType.LINEAR))
                .addBus("SLAP", new BusAnimationSequenceSedna().hold(250).addPos(1, 0, 0, 150, BusAnimationKeyframeSedna.IType.SIN_DOWN).addPos(0, 0, 0, 350, BusAnimationKeyframeSedna.IType.SIN_FULL));

        return null;
    }

    @Override public void setupFirstPerson(ItemStack stack) { }

    @Override
    public void renderFirstPerson(ItemStack stack) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.rpa_arm);

        GlStateManager.translate(0.0D, -1.5D, 0.5D);
        double scale = 0.125D;
        GlStateManager.scale(scale, scale, scale);

        double[] equip = HbmAnimationsSedna.getRelevantTransformation("EQUIP");
        double swingRight = HbmAnimationsSedna.getRelevantTransformation("SWINGRIGHT")[0];
        double swingLeft = HbmAnimationsSedna.getRelevantTransformation("SWINGLEFT")[0];
        double slapTurn = HbmAnimationsSedna.getRelevantTransformation("SLAPTURN")[0];
        double slap = HbmAnimationsSedna.getRelevantTransformation("SLAP")[0];

        double forwardTilt = 60 - 60 * equip[0];
        double offsetOutward = 3;
        double roll = 60;

        GlStateManager.pushMatrix();

        GlStateManager.translate(-12 * swingLeft + 2 * slapTurn - 5 * slap, 6 * slap, 5 * swingLeft + 8 * slap);
        GlStateManager.rotate((float) (forwardTilt - swingRight * 20), 1.0F, 0.0F, 0.0F);

        GlStateManager.translate(offsetOutward, 0.0D, 0.0D);
        GlStateManager.translate(6.0D, 8.0D, 0.0D);
        GlStateManager.rotate((float) (60 * swingLeft + 45 * slap), 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate((float) (roll + 15 * swingLeft + 45 * slapTurn), 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(-6.0D, -8.0D, 0.0D);
        ResourceManager.armor_remnant.renderPart("LeftArm");
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();

        GlStateManager.translate(12 * swingRight - 2 * slapTurn + 5 * slap, 6 * slap, 5 * swingRight + 8 * slap);
        GlStateManager.rotate((float) (forwardTilt - swingLeft * 20), 1.0F, 0.0F, 0.0F);

        GlStateManager.translate(-offsetOutward, 0.0D, 0.0D);
        GlStateManager.translate(-6.0D, 8.0D, 0.0D);
        GlStateManager.rotate((float) (-60 * swingRight - 45 * slap), 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate((float) (-roll - 15 * swingRight - 45 * slapTurn), 0.0F, 1.0F, 0.0F);
        GlStateManager.translate(6.0D, -8.0D, 0.0D);
        ResourceManager.armor_remnant.renderPart("RightArm");
        GlStateManager.popMatrix();
    }
}
