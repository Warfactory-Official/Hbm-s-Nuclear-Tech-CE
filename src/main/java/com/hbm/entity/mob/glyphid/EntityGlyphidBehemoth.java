package com.hbm.entity.mob.glyphid;

import com.hbm.entity.effect.EntityMist;
import com.hbm.entity.mob.glyphid.GlyphidStats.StatBundle;
import com.hbm.entity.projectile.EntityChemical;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ModItems;
import com.hbm.main.ResourceManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@AutoRegister(name = "entity_glyphid_behemoth", eggColors = {0x267F00, 0xD2BB72})
public class EntityGlyphidBehemoth extends EntityGlyphid {

    public EntityGlyphidBehemoth(World world) {
        super(world);
        this.setSize(2.5F, 1.5F);
    }

    @Override
    public ResourceLocation getSkin() {
        return ResourceManager.glyphid_behemoth_tex;
    }

    @Override
    public double getScale() {
        return 1.5D;
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        this.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(GlyphidStats.getStats().getBehemoth().health());
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(GlyphidStats.getStats().getBehemoth().speed());
        this.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(GlyphidStats.getStats().getBehemoth().damage());
    }

    public StatBundle getStats() {
        return GlyphidStats.getStats().statsBehemoth;
    }

    public int timer = 120;
    int breathTime = 0;

    @Override
    public void onUpdate(){
        super.onUpdate();
        Entity e = this.getAttackTarget();
        if (e == null) {
            timer = 120;
            breathTime = 0;
        } else {
            if (breathTime > 0) {
                if(!isSwingInProgress){
                    this.swingArm(EnumHand.MAIN_HAND);
                }
                acidAttack();
                rotationYaw = prevRotationYaw;
                breathTime--;
            } else if (--timer <= 0) {
                breathTime = 120;
                timer = 120;
            }
        }
    }

    @Override
    public void onDeath(@NotNull DamageSource source) {
        super.onDeath(source);
        if (!world.isRemote) {
            EntityMist mist = new EntityMist(world);
            mist.setType(Fluids.SULFURIC_ACID);
            mist.setPosition(posX, posY, posZ);
            mist.setArea(10, 4);
            mist.setDuration(120);
            world.spawnEntity(mist);
        }
    }


    public void acidAttack(){
        if(!world.isRemote && getAttackTarget() instanceof EntityLivingBase && this.getDistance(getAttackTarget()) < 20) {
            this.addPotionEffect(new PotionEffect(Objects.requireNonNull(Potion.getPotionFromResourceLocation("slowness")), 2 * 20, 6));
            EntityChemical chem = new EntityChemical(world, this, 0, 0, 0);

            chem.setFluid(Fluids.SULFURIC_ACID);
            world.spawnEntity(chem);
        }
    }

    @Override
    protected void dropFewItems(boolean byPlayer, int looting) {
        this.entityDropItem(new ItemStack(ModItems.glyphid_gland, 1, Fluids.SULFURIC_ACID.getID()), 1);
        super.dropFewItems(byPlayer, looting);
    }
    @Override
    public boolean isArmorBroken(float amount) {
        // amount < 5 ? 5 : amount < 10 ? 3 : 2;
        return this.rand.nextInt(100) <= Math.min(Math.pow(amount * 0.15, 2), 100);
    }
    @Override
    public int swingDuration() {
        return 100;
    }
}
