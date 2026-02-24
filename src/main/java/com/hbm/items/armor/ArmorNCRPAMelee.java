package com.hbm.items.armor;

import com.hbm.lib.HBMSoundHandler;
import com.hbm.render.anim.sedna.BusAnimationKeyframeSedna;
import com.hbm.render.anim.sedna.BusAnimationSedna;
import com.hbm.render.anim.sedna.BusAnimationSequenceSedna;
import com.hbm.render.anim.sedna.HbmAnimationsSedna;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import org.lwjgl.opengl.GL11;

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

public class ArmorNCRPAMelee implements IPAMelee {

    @Override public void clickPrimary(ItemStack stack, LambdaContext ctx) { XFactoryPA.doSwing(stack, ctx, HbmAnimationsSedna.GunAnimation.CYCLE, 25); }
    @Override public void clickSecondary(ItemStack stack, LambdaContext ctx) { XFactoryPA.doSwing(stack, ctx, HbmAnimationsSedna.GunAnimation.ALT_CYCLE, 30); }

    @Override
    public void orchestra(ItemStack stack, LambdaContext ctx) {
        EntityLivingBase entity = ctx.entity;
        if(entity.world.isRemote) return;
        HbmAnimationsSedna.GunAnimation type = ItemGunBaseNT.getLastAnim(stack, ctx.configIndex);
        int timer = ItemGunBaseNT.getAnimTimer(stack, ctx.configIndex);

        boolean swings = type == HbmAnimationsSedna.GunAnimation.CYCLE && (timer == 5 || timer == 15);
        boolean sweep = type == HbmAnimationsSedna.GunAnimation.ALT_CYCLE && timer == 5;

        if((swings || sweep) && ctx.getPlayer() != null) {
            RayTraceResult mop = EntityDamageUtil.getMouseOver(ctx.getPlayer(), 3.0D, 0.5D);

            if(mop != null && mop.typeOfHit != null) {
                if(mop.typeOfHit == RayTraceResult.Type.ENTITY && mop.entityHit.isEntityAlive()) {
                    float damage = swings ? 15F : 35F;
                    float knockback = swings ? 0F : 1.5F;
                    float dt = swings ? 5F : 15F;
                    float pierce = swings ? 0.1F : 0.25F;

                    if(mop.entityHit instanceof EntityLivingBase living) {
                        if(living.getMaxHealth() >= 100) damage *= 2.5F;
                        EntityDamageUtil.attackEntityFromNT(living, DamageSource.causePlayerDamage(ctx.getPlayer()), damage, true, false, knockback, dt, pierce);
                        if(!living.isEntityAlive()) ConfettiUtil.gib(living);
                    } else {
                        mop.entityHit.attackEntityFrom(DamageSource.causePlayerDamage(ctx.getPlayer()), damage);
                    }

                    entity.world.playSound(null, mop.entityHit.getPosition(), HBMSoundHandler.fireStab, SoundCategory.PLAYERS, 1F, 0.9F + entity.getRNG().nextFloat() * 0.2F);
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
                .addBus("EQUIP", new BusAnimationSequenceSedna().setPos(-1, 0, 0).addPos(0, 0, 0, 750, BusAnimationKeyframeSedna.IType.SIN_DOWN));
        if(type == HbmAnimationsSedna.GunAnimation.CYCLE) return new BusAnimationSedna()
                .addBus("SWINGRIGHT", new BusAnimationSequenceSedna().addPos(1, 0, 0, 250, BusAnimationKeyframeSedna.IType.SIN_DOWN).addPos(0, 0, 0, 500, BusAnimationKeyframeSedna.IType.SIN_FULL))
                .addBus("SWINGLEFT", new BusAnimationSequenceSedna().addPos(0, 0, 0, 500).addPos(1, 0, 0, 250, BusAnimationKeyframeSedna.IType.SIN_DOWN).addPos(0, 0, 0, 500, BusAnimationKeyframeSedna.IType.SIN_FULL));
        if(type == HbmAnimationsSedna.GunAnimation.ALT_CYCLE) return new BusAnimationSedna()
                .addBus("SWEEPTURN", new BusAnimationSequenceSedna().addPos(1, 0, 0, 100, BusAnimationKeyframeSedna.IType.LINEAR).hold(350).addPos(0, 0, 0, 500, BusAnimationKeyframeSedna.IType.LINEAR))
                .addBus("SWEEPCUT", new BusAnimationSequenceSedna().hold(100).addPos(1, 0, 0, 250, BusAnimationKeyframeSedna.IType.SIN_DOWN).hold(100).addPos(0, 0, 0, 500, BusAnimationKeyframeSedna.IType.SIN_FULL));

        return null;
    }

    @Override public void setupFirstPerson(ItemStack stack) { }

    @Override
    public void renderFirstPerson(ItemStack stack) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.ncrpa_arm);

        GL11.glTranslated(0, -1.5, 0.5);
        double scale = 0.125D;
        GL11.glScaled(scale, scale, scale);

        double[] equip = HbmAnimationsSedna.getRelevantTransformation("EQUIP");
        double swingRight = HbmAnimationsSedna.getRelevantTransformation("SWINGRIGHT")[0];
        double swingLeft = HbmAnimationsSedna.getRelevantTransformation("SWINGLEFT")[0];
        double sweepTurn = HbmAnimationsSedna.getRelevantTransformation("SWEEPTURN")[0];
        double sweepCut = HbmAnimationsSedna.getRelevantTransformation("SWEEPCUT")[0];

        double forwardTilt = 60 - 60 * equip[0];
        double offsetOutward = 3;
        double roll = 60;

        GL11.glPushMatrix();

        GL11.glTranslated(-14 * swingLeft - 4 * sweepTurn, 6 * sweepCut, 2 * swingLeft + 8 * sweepCut);
        GL11.glRotated(forwardTilt + swingRight * 40 - 60 * sweepCut, 1, 0, 0);

        GL11.glTranslated(offsetOutward, 0, 0);
        GL11.glTranslated(6, 8, 0);
        GL11.glRotated(90 * swingLeft, 0, 0, 1);
        GL11.glRotated(roll + 30 * swingLeft - 90 * sweepTurn, 0, 1, 0);
        GL11.glTranslated(-6, -8, 0);
        ResourceManager.armor_ncr.renderPart("LeftArm");
        GL11.glPopMatrix();

        GL11.glPushMatrix();

        GL11.glTranslated(14 * swingRight + 4 * sweepTurn, 6 * sweepCut, 2 * swingRight + 8 * sweepCut);
        GL11.glRotated(forwardTilt + swingLeft * 40 - 60 * sweepCut, 1, 0, 0);

        GL11.glTranslated(-offsetOutward, 0, 0);
        GL11.glTranslated(-6, 8, 0);
        GL11.glRotated(-90 * swingRight, 0, 0, 1);
        GL11.glRotated(-roll - 30 * swingRight + 90 * sweepTurn, 0, 1, 0);
        GL11.glTranslated(6, -8, 0);
        ResourceManager.armor_ncr.renderPart("RightArm");
        GL11.glPopMatrix();
    }
}
