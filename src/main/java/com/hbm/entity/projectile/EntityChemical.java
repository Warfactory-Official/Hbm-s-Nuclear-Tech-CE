package com.hbm.entity.projectile;

import com.hbm.blocks.ModBlocks;
import com.hbm.capability.HbmLivingProps;
import com.hbm.entity.mob.glyphid.EntityGlyphid;
import com.hbm.handler.ArmorUtil;
import com.hbm.handler.radiation.ChunkRadiationManager;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.trait.*;
import com.hbm.lib.ModDamageSource;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IRepairable;
import com.hbm.tileentity.IRepairable.EnumExtinguishType;
import com.hbm.util.CompatExternal;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.EntityDamageUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.awt.*;
import java.util.List;

@AutoRegister(name = "entity_chemthrower_splash", trackingRange = 1000)
public class EntityChemical extends EntityThrowableNT{
    private static final DataParameter<? super Integer> FLUID_TYPE =  EntityDataManager.createKey(EntityChemical.class, DataSerializers.VARINT);
    /*
     * TYPE INFO:
     *
     * if ANTIMATTER: ignore all other traits, become a gamma beam with no gravity
     * if HOT: set fire and deal extra fire damage, scaling with the temperature
     * if COLD: freeze, duration scaling with temperature, assuming COMBUSTIBLE does not apply
     * if GAS: short range with the spread going up
     * if EVAP: same as gas
     * if LIQUID: if EVAP doesn't apply, create a narrow spray with long range affected by gravity
     * if COMBUSTIBLE: auto-ignite
     * if FLAMMABLE: if GAS or EVAP apply, do the same as COMBUSTIBLE, otherwise create a neutral spray that adds the "soaked" effect
     * if CORROSIVE: apply extra acid damage, poison effect as well as armor degradation
     */

    public EntityChemical(World world) {
        super(world);
        this.ignoreFrustumCheck = true;
        this.isImmuneToFire = true;
    }

    public EntityChemical(World world, EntityLivingBase thrower, double sideOffset, double heightOffset, double frontOffset) {
        super(world, thrower);
        this.ignoreFrustumCheck = true;
        this.isImmuneToFire = true;
    }

    @Override
    protected void entityInit() {
        this.getDataManager().register(FLUID_TYPE, Integer.valueOf(0));
    }

    public EntityChemical setFluid(FluidType fluid) {
        this.dataManager.set(FLUID_TYPE, fluid.getID());
        return this;
    }

    public FluidType getType() {
        return Fluids.fromID((int) this.dataManager.get(FLUID_TYPE));
    }

    @Override
    public void onUpdate() {

        if(!world.isRemote) {

            if(this.ticksExisted > this.getMaxAge()) {
                this.setDead();
            }

            FluidType type = this.getType();

            if(type.hasTrait(Fluids.GASEOUS.getClass()) || type.hasTrait(Fluids.EVAP.getClass())) {

                double intensity = 1D - (double) this.ticksExisted / (double) this.getMaxAge();
                List<Entity> affected = world.getEntitiesWithinAABB(
                        Entity.class,
                        this.getEntityBoundingBox().grow(intensity * 2.5),
                        e -> e != this.thrower
                );
                for(Entity e : affected) {
                    this.affect(e, intensity);
                }
            }

        } else {

            ChemicalStyle style = getStyle();

            if(style == ChemicalStyle.LIQUID) {

                FluidType type = getType();
                Color color = new Color(type.getColor());

                NBTTagCompound data = new NBTTagCompound();
                data.setString("type", "vanillaExt");
                data.setString("mode", "colordust");
                data.setDouble("posX", posX);
                data.setDouble("posY", posY);
                data.setDouble("posZ", posZ);
                data.setDouble("mX", motionX + world.rand.nextGaussian() * 0.05);
                data.setDouble("mY", motionY - 0.2 + world.rand.nextGaussian() * 0.05);
                data.setDouble("mZ", motionZ + world.rand.nextGaussian() * 0.05);
                data.setFloat("r", color.getRed() / 255F);
                data.setFloat("g", color.getGreen() / 255F);
                data.setFloat("b", color.getBlue() / 255F);
                MainRegistry.proxy.effectNT(data);
            }

            if(style == ChemicalStyle.BURNING) {

                double motion = Math.min(new Vec3d(motionX, motionY, motionZ).length(), 0.1);

                for(double d = 0; d < motion; d += 0.0625) {
                    NBTTagCompound nbt = new NBTTagCompound();
                    nbt.setString("type", "vanillaExt");
                    nbt.setString("mode", "flame");
                    nbt.setDouble("posX", (this.lastTickPosX - this.posX) * d + this.posX);
                    nbt.setDouble("posY", (this.lastTickPosY - this.posY) * d + this.posY);
                    nbt.setDouble("posZ", (this.lastTickPosZ - this.posZ) * d + this.posZ);
                    MainRegistry.proxy.effectNT(nbt);
                }
            }
        }
        super.onUpdate();
    }

    protected void affect(Entity e, double intensity) {

        ChemicalStyle style = getStyle();
        FluidType type = getType();
        EntityLivingBase living = e instanceof EntityLivingBase ? (EntityLivingBase) e : null;

        switch (style) {
            case LIQUID:
            case BURNING:
                intensity = 1D; // Ignore range penalty for liquids
                break;
            case AMAT:
                EntityDamageUtil.attackEntityFromIgnoreIFrame(e, ModDamageSource.radiation, 1F);
                if (living != null) {
                    ContaminationUtil.contaminate(living, ContaminationUtil.HazardType.RADIATION, ContaminationUtil.ContaminationType.CREATIVE, 50F * (float) intensity);
                    return;
                }
                break;
            case LIGHTNING:
                EntityDamageUtil.attackEntityFromIgnoreIFrame(e, ModDamageSource.electricity, 0.5F);
                if (living != null) {
                    living.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 60, 9));
                    living.addPotionEffect(new PotionEffect(MobEffects.WEAKNESS, 60, 9));
                    return;
                }
                break;
        }

        if(type.temperature >= 100) {
            //TODO
//            EntityDamageUtil.attackEntityFromIgnoreIFrame(e, getDamage(ModDamageSource.s_boil), 0.25F + (type.temperature - 100) * 0.001F); //.25 damage at 100°C with one extra damage every 1000°C

            if(type.temperature >= 500) {
                e.setFire(10); //afterburn for 10 seconds
            }
        }

        if(style == ChemicalStyle.LIQUID || style == ChemicalStyle.GAS) {
            if(type.temperature < -20) {
                if(living != null) { //only living things are affected
                }
            }

            if(type.hasTrait(Fluids.DELICIOUS.getClass())) {
                if(living != null && living.isEntityAlive()) {
                    living.heal(2F * (float) intensity);
                }
            }
        }

        if(style == ChemicalStyle.LIQUID) {

            if(type.hasTrait(FT_Flammable.class)) {
                if(living != null) {
                    HbmLivingProps.setOil(living, 300); //doused in oil for 15 seconds
                }
            }
            if(type.hasTrait(Fluids.DELICIOUS.getClass())) {
                if(living != null && living.isEntityAlive()) {
                    living.heal(2F * (float) intensity);
                }
            }

        }

        if(this.isExtinguishing()) {
            e.extinguish();
        }

        if(style == ChemicalStyle.BURNING) {
            FT_Combustible trait = type.getTrait(FT_Combustible.class);
            //TODO
            //EntityDamageUtil.attackEntityFromIgnoreIFrame(e, getDamage(ModDamageSource.s_flamethrower), 0.2F + (trait != null ? (trait.getCombustionEnergy() / 100_000F) : 0));
            e.setFire(5);
        }

        if(style == ChemicalStyle.GASFLAME) {
            FT_Flammable flammable = type.getTrait(FT_Flammable.class);
            FT_Combustible combustible = type.getTrait(FT_Combustible.class);

            float heat = Math.max(flammable != null ? flammable.getHeatEnergy() / 50_000F : 0, combustible != null ? combustible.getCombustionEnergy() / 100_000F : 0);
            heat *= intensity;
            //EntityDamageUtil.attackEntityFromIgnoreIFrame(e, getDamage(ModDamageSource.s_flamethrower), (0.2F + heat) * (float) intensity);
            e.setFire((int) Math.ceil(5 * intensity));
        }

        if(type.hasTrait(FT_Corrosive.class)) {
            FT_Corrosive trait = type.getTrait(FT_Corrosive.class);

            if(living != null) {
                EntityDamageUtil.attackEntityFromIgnoreIFrame(living, ModDamageSource.acid, trait.getRating() / 50F);
                if (living instanceof EntityPlayer player)
                    for(int i = 0; i < 4; i++) {
                        ArmorUtil.damageSuit(player, i, trait.getRating() / 40);
                    }
            }
        }

        if(type.hasTrait(FT_VentRadiation.class)) {
            FT_VentRadiation trait = type.getTrait(FT_VentRadiation.class);
            if(living != null) {
                ContaminationUtil.contaminate(living, ContaminationUtil.HazardType.RADIATION, ContaminationUtil.ContaminationType.CREATIVE, trait.getRadPerMB() * 5);
            }
            ChunkRadiationManager.proxy.incrementRad(world, this.getPosition(),trait.getRadPerMB() * 5);
        }

        if(type.hasTrait(FT_Poison.class)) {
            FT_Poison trait = type.getTrait(FT_Poison.class);

            if(living != null) {
                living.addPotionEffect(new PotionEffect(trait.isWithering() ? MobEffects.WITHER : MobEffects.POISON, (int) (5 * 20 * intensity)));
            }
        }

        if(type.hasTrait(FT_Toxin.class)) {
            FT_Toxin trait = type.getTrait(FT_Toxin.class);

            if(living != null) {
                trait.affect(living, intensity);
            }
        }

        if(type.hasTrait(FT_Pheromone.class)){

            FT_Pheromone pheromone = type.getTrait(FT_Pheromone.class);

            if(living != null) {
                living.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 2 * 60 * 20, 2));
                living.addPotionEffect(new PotionEffect(MobEffects.SPEED, 5 * 60 * 20, 1));
                living.addPotionEffect(new PotionEffect(MobEffects.HASTE, 2 * 60 * 20, 4));

                if (living instanceof EntityGlyphid && pheromone.getType() == 1) {
                    living.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 5 * 60 * 20, 4));
                    living.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE,  60 * 20, 0));
                    living.addPotionEffect(new PotionEffect(MobEffects.ABSORPTION,  60 * 20, 19));

                } else if (living instanceof EntityPlayer && pheromone.getType() == 2) {
                    living.addPotionEffect(new PotionEffect(MobEffects.STRENGTH, 2 * 60 * 20, 2));
                }
            }
        }

        if(type == Fluids.XPJUICE) {

            if(e instanceof EntityPlayer) {
                ((EntityPlayer) e).addExperience(1);
                this.setDead();
            }
        }

        if(type == Fluids.ENDERJUICE) {
            this.teleportRandomly(e);
        }
    }

    /* whether this type should extinguish entities */
    protected boolean isExtinguishing() {
        return this.getStyle() == ChemicalStyle.LIQUID && this.getType().temperature < 50 && !this.getType().hasTrait(FT_Flammable.class);
    }

    /* the extinguish type for burning multiblocks, roughly identical to the fire extinguisher */
    protected EnumExtinguishType getExtinguishingType(FluidType type) {
        return type == Fluids.CARBONDIOXIDE ? EnumExtinguishType.CO2 : type == Fluids.WATER || type == Fluids.HEAVYWATER || type == Fluids.COOLANT ? EnumExtinguishType.WATER : null;
    }

    protected DamageSource getDamage(String name) {

        if(thrower != null) {
            return new EntityDamageSourceIndirect(name, this, thrower);
        } else {
            return new DamageSource(name);
        }
    }

    //terribly copy-pasted from EntityEnderman.class
    public boolean teleportRandomly(Entity e) {
        double x = this.posX + (this.rand.nextDouble() - 0.5D) * 64.0D;
        double y = this.posY + (double) (this.rand.nextInt(64) - 32);
        double z = this.posZ + (this.rand.nextDouble() - 0.5D) * 64.0D;
        return this.teleportTo(e, x, y, z);
    }

    public boolean teleportTo(Entity entity, double x, double y, double z) {

        double targetX = entity.posX;
        double targetY = entity.posY;
        double targetZ = entity.posZ;
        entity.posX = x;
        entity.posY = y;
        entity.posZ = z;
        boolean flag = false;
        BlockPos pos = new BlockPos(entity.posX, entity.posY, entity.posZ);


        if(entity.world.isBlockLoaded(pos)) {
            boolean flag1 = false;

            while(!flag1 && pos.getY() > 0) {
                IBlockState block = entity.world.getBlockState(pos.down());

                if(block.getMaterial().blocksMovement()) {
                    flag1 = true;
                } else {
                    --entity.posY;
                    pos.down();
                }
            }

            if(flag1) {
                entity.setPosition(entity.posX, entity.posY, entity.posZ);

                if(entity.world.getCollisionBoxes(entity, entity.getCollisionBoundingBox()).isEmpty() && !entity.world.containsAnyLiquid(entity.getCollisionBoundingBox())) {
                    flag = true;
                }
            }
        }

        if(!flag) {
            entity.setPosition(targetX, targetY, targetZ);
            return false;
        } else {
            short short1 = 128;

            for(int l = 0; l < short1; ++l) {
                double d6 = (double) l / ((double) short1 - 1.0D);
                float f = (this.rand.nextFloat() - 0.5F) * 0.2F;
                float f1 = (this.rand.nextFloat() - 0.5F) * 0.2F;
                float f2 = (this.rand.nextFloat() - 0.5F) * 0.2F;
                double d7 = targetX + (entity.posX - targetX) * d6 + (this.rand.nextDouble() - 0.5D) * (double) entity.width * 2.0D;
                double d8 = targetY + (entity.posY - targetY) * d6 + this.rand.nextDouble() * (double) entity.height;
                double d9 = targetZ + (entity.posZ - targetZ) * d6 + (this.rand.nextDouble() - 0.5D) * (double) entity.width * 2.0D;
                entity.world.spawnParticle(EnumParticleTypes.PORTAL, d7, d8, d9, f, f1, f2);
            }

            entity.world.playSound(null, targetX, targetY, targetZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.NEUTRAL, 1.0F, 1.0F);
            entity.playSound(SoundEvents.ENTITY_ENDERMEN_TELEPORT, 1.0F, 1.0F);
            return true;

        }

    }

    @Override
    protected void onImpact(RayTraceResult mop) {
        if (!world.isRemote) {
            if (mop.typeOfHit == RayTraceResult.Type.ENTITY) {
                this.affect(mop.entityHit, 1D - (double) this.ticksExisted / (double) this.getMaxAge());
            }

            if (mop.typeOfHit == RayTraceResult.Type.BLOCK) {
                FluidType type = getType();
                BlockPos pos = mop.getBlockPos();
                IBlockState state = world.getBlockState(pos);

                if (type.hasTrait(FT_VentRadiation.class)) {
                    FT_VentRadiation trait = type.getTrait(FT_VentRadiation.class);
                    ChunkRadiationManager.proxy.incrementRad(world, pos, trait.getRadPerMB() * 5);
                }

                ChemicalStyle style = getStyle();

                if (style == ChemicalStyle.BURNING || style == ChemicalStyle.GASFLAME) {
                    for (EnumFacing dir : EnumFacing.VALUES) {
                        BlockPos offsetPos = pos.offset(dir);
                        if (world.isAirBlock(offsetPos)) {
                            Block fire = (type == Fluids.BALEFIRE) ? ModBlocks.balefire : Blocks.FIRE;
                            world.setBlockState(offsetPos, fire.getDefaultState());
                        }
                    }
                }

                if (this.isExtinguishing()) {
                    for (EnumFacing dir : EnumFacing.VALUES) {
                        BlockPos offsetPos = pos.offset(dir);
                        if (world.getBlockState(offsetPos).getBlock() == Blocks.FIRE) {
                            world.setBlockToAir(offsetPos);
                        }
                    }
                }

                EnumExtinguishType fext = this.getExtinguishingType(type);
                if (fext != null) {
                    TileEntity core = CompatExternal.getCoreFromPos(world, pos);
                    if (core instanceof IRepairable) {
                        ((IRepairable) core).tryExtinguish(world, pos.getX(),  pos.getY(), pos.getZ(), fext);
                    }

                    if (fext == EnumExtinguishType.WATER && style == ChemicalStyle.LIQUID) {
                        for (int i = -2; i <= 2; i++) {
                            for (int j = 0; j <= 1; j++) {
                                for (int k = -2; k <= 2; k++) {
                                    BlockPos checkPos = pos.add(i, j, k);
                                    if (world.getBlockState(checkPos).getBlock() == ModBlocks.fallout) {
                                        world.setBlockToAir(checkPos);
                                    }
                                }
                            }
                        }
                    }
                }

                if (type == Fluids.SEEDSLURRY) {
                    if (state.getBlock() == Blocks.DIRT || state.getBlock() == ModBlocks.waste_earth ||
                            state.getBlock() == ModBlocks.dirt_dead || state.getBlock() == ModBlocks.dirt_oily) {

                        if (world.getLight(pos.up()) >= 9 && world.getBlockState(pos.up()).getLightOpacity(world, pos.up()) <= 2) {
                            world.setBlockState(pos, Blocks.GRASS.getDefaultState());
                        }
                    }
                    int meta = state.getBlock().getMetaFromState(state);
                    if (state.getBlock() == Blocks.COBBLESTONE) world.setBlockState(pos, Blocks.MOSSY_COBBLESTONE.getDefaultState());
                    if (state.getBlock() == Blocks.STONEBRICK && meta == 0)
                        world.setBlockState(pos, Blocks.STONEBRICK.getStateFromMeta(1));
                    if (state.getBlock() == ModBlocks.waste_earth) world.setBlockState(pos, Blocks.GRASS.getDefaultState());
                    if (state.getBlock() == ModBlocks.brick_concrete)
                        world.setBlockState(pos, ModBlocks.brick_concrete_mossy.getDefaultState());
//                    if (state.getBlock() == ModBlocks.concrete_brick_slab && meta % 8 == 0)
//                        world.setBlockState(pos, ModBlocks.concrete_brick_slab.getStateFromMeta(meta + 1));
                    if (state.getBlock() == ModBlocks.brick_concrete_stairs)
                        world.setBlockState(pos, ModBlocks.brick_concrete_mossy_stairs.getStateFromMeta(meta));
                }

                this.setDead();
            }
        }
    }

    @Override
    protected float getAirDrag() {

        ChemicalStyle type = getStyle();

        if(type == ChemicalStyle.AMAT) return 1F;
        if(type == ChemicalStyle.LIGHTNING) return 1F;
        if(type == ChemicalStyle.GAS) return 0.95F;

        return 0.99F;
    }

    @Override
    protected float getWaterDrag() {

        ChemicalStyle type = getStyle();

        if(type == ChemicalStyle.AMAT) return 1F;
        if(type == ChemicalStyle.LIGHTNING) return 1F;
        if(type == ChemicalStyle.GAS) return 1F;

        return 0.8F;
    }

    public int getMaxAge() {

        switch(this.getStyle()) {
            case AMAT: return 100;
            case LIGHTNING: return 5;
            case BURNING:return 600;
            case GAS: return 60;
            case GASFLAME: return 20;
            case LIQUID: return 600;
            default: return 100;
        }
    }

    @Override
    public float getGravityVelocity() {

        ChemicalStyle type = getStyle();

        if(type == ChemicalStyle.AMAT) return 0F;
        if(type == ChemicalStyle.LIGHTNING) return 0F;
        if(type == ChemicalStyle.GAS) return 0f;
        if(type == ChemicalStyle.GASFLAME) return -0.01F;

        return 0.03F;
    }

    public ChemicalStyle getStyle() {
        return getStyleFromType(this.getType());
    }

    public static ChemicalStyle getStyleFromType(FluidType type) {

        if(type == Fluids.IONGEL) {
            return ChemicalStyle.LIGHTNING;
        }

        if(type.isAntimatter()) {
            return ChemicalStyle.AMAT;
        }

        if(type.hasTrait(Fluids.GASEOUS.getClass()) || type.hasTrait(Fluids.EVAP.getClass())) {

            if(type.hasTrait(FT_Flammable.class) || type.hasTrait(FT_Combustible.class)) {
                return ChemicalStyle.GASFLAME;
            } else {
                return ChemicalStyle.GAS;
            }
        }

        if(type.hasTrait(Fluids.LIQUID.getClass())) {

            if(type.hasTrait(FT_Combustible.class)) {
                return ChemicalStyle.BURNING;
            } else {
                return ChemicalStyle.LIQUID;
            }
        }

        return ChemicalStyle.NULL;
    }

    /**
     * The general type of the chemical, determines rendering and movement
     */
    public static enum ChemicalStyle {
        AMAT,		//renders as beam
        LIGHTNING,	//renders as beam
        LIQUID,		//no renderer, fluid particles
        GAS,		//renders as particles
        GASFLAME,	//renders as fire particles
        BURNING,	//no renderer, fire particles
        NULL
    }
}
