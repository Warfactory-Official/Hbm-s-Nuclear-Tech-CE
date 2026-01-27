package com.hbm.items.weapon.sedna.factory;

import com.hbm.Tags;
import com.hbm.blocks.ModBlocks;
import com.hbm.entity.projectile.EntityBulletBaseMK4;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.*;
import com.hbm.items.ModItems;
import com.hbm.items.weapon.sedna.BulletConfig;
import com.hbm.items.weapon.sedna.GunConfig;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.items.weapon.sedna.Receiver;
import com.hbm.items.weapon.sedna.impl.ItemGunChargeThrower;
import com.hbm.items.weapon.sedna.mags.MagazineFullReload;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.main.MainRegistry;
import com.hbm.particle.helper.ExplosionCreator;
import com.hbm.render.anim.sedna.BusAnimationKeyframeSedna.IType;
import com.hbm.render.anim.sedna.BusAnimationSedna;
import com.hbm.render.anim.sedna.BusAnimationSequenceSedna;
import com.hbm.render.anim.sedna.HbmAnimationsSedna;
import com.hbm.render.misc.RenderScreenOverlay;
import com.hbm.tileentity.IRepairable;
import com.hbm.util.CompatExternal;
import com.hbm.util.Vec3NT;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class XFactoryTool {

    public static final ResourceLocation scope = new ResourceLocation(Tags.MODID, "textures/misc/scope_tool.png");

    public static BulletConfig fext_water;
    public static BulletConfig fext_foam;
    public static BulletConfig fext_sand;

    public static BulletConfig ct_hook;
    public static BulletConfig ct_mortar;
    public static BulletConfig ct_mortar_charge;

    public static BiConsumer<EntityBulletBaseMK4, RayTraceResult> LAMBDA_WATER_HIT = (bullet, mop) -> {
        if (!bullet.world.isRemote && mop.typeOfHit == mop.typeOfHit.BLOCK) {
            BlockPos base = mop.getBlockPos();
            int ix = base.getX();
            int iy = base.getY();
            int iz = base.getZ();

            boolean fizz = false;

            for (int i = -1; i <= 1; i++) for (int j = -1; j <= 1; j++) for (int k = -1; k <= 1; k++) {
                BlockPos p = base.add(i, j, k);
                IBlockState st = bullet.world.getBlockState(p);
                Block block = st.getBlock();
                if (block == Blocks.FIRE || block == ModBlocks.foam_layer || block == ModBlocks.block_foam) {
                    bullet.world.setBlockState(p, Blocks.AIR.getDefaultState(), 3);
                    fizz = true;
                }
            }

            TileEntity core = CompatExternal.getCoreFromPos(bullet.world, new BlockPos(ix, iy, iz));
            if (core instanceof IRepairable) {
                ((IRepairable) core).tryExtinguish(bullet.world, ix, iy, iz, IRepairable.EnumExtinguishType.WATER);
            }

            if (fizz) {
                bullet.world.playSound(null, bullet.posX, bullet.posY, bullet.posZ, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 1.0F, 1.5F + bullet.world.rand.nextFloat() * 0.5F);
            }

            bullet.setDead();
        }
    };

    public static Consumer<Entity> LAMBDA_WATER_UPDATE = (bullet) -> {
        if(bullet.world.isRemote) {
            NBTTagCompound data = new NBTTagCompound();
            data.setString("type", "vanillaExt");
            data.setString("mode", "blockdust");
            data.setInteger("block", Block.getIdFromBlock(Blocks.WATER));
            data.setDouble("posX", bullet.posX); data.setDouble("posY", bullet.posY); data.setDouble("posZ", bullet.posZ);
            data.setDouble("mX", bullet.motionX + bullet.world.rand.nextGaussian() * 0.05);
            data.setDouble("mY", bullet.motionY - 0.2 + bullet.world.rand.nextGaussian() * 0.05);
            data.setDouble("mZ", bullet.motionZ + bullet.world.rand.nextGaussian() * 0.05);
            MainRegistry.proxy.effectNT(data);
        } else {
            int x = (int)Math.floor(bullet.posX);
            int y = (int)Math.floor(bullet.posY);
            int z = (int)Math.floor(bullet.posZ);
            BlockPos pos = new BlockPos(x, y, z);
            IBlockState state = bullet.world.getBlockState(pos);
            Block block = state.getBlock();
            if(block == ModBlocks.volcanic_lava_block && block.getMetaFromState(state) == 0) {
                bullet.world.setBlockState(pos, Blocks.OBSIDIAN.getDefaultState());
                bullet.setDead();
            }
        }
    };

    public static BiConsumer<EntityBulletBaseMK4, RayTraceResult> LAMBDA_FOAM_HIT = (bullet, mop) -> {
        if (!bullet.world.isRemote && mop.typeOfHit == mop.typeOfHit.BLOCK) {
            BlockPos base = mop.getBlockPos();
            int ix = base.getX();
            int iy = base.getY();
            int iz = base.getZ();

            boolean fizz = false;

            for (int i = -1; i <= 1; i++) for (int j = -1; j <= 1; j++) for (int k = -1; k <= 1; k++) {
                BlockPos p = base.add(i, j, k);
                IBlockState st = bullet.world.getBlockState(p);
                if (st.getMaterial() == Material.FIRE) {
                    bullet.world.setBlockState(p, Blocks.AIR.getDefaultState(), 3);
                    fizz = true;
                }
            }

            TileEntity core = CompatExternal.getCoreFromPos(bullet.world, new BlockPos(ix, iy, iz));
            if (core instanceof IRepairable) {
                ((IRepairable) core).tryExtinguish(bullet.world, ix, iy, iz, IRepairable.EnumExtinguishType.FOAM);
                return;
            }

            if (bullet.world.rand.nextBoolean()) {
                EnumFacing dir = mop.sideHit;
                base = base.offset(dir);
            }

            IBlockState state = bullet.world.getBlockState(base);
            Block b = state.getBlock();

            if (b.isReplaceable(bullet.world, base) && ModBlocks.foam_layer.canPlaceBlockAt(bullet.world, base)) {
                if (b != ModBlocks.foam_layer) {
                    bullet.world.setBlockState(base, ModBlocks.foam_layer.getDefaultState(), 3);
                } else {
                    int meta = b.getMetaFromState(state);
                    if (meta < 6) {
                        bullet.world.setBlockState(base, b.getStateFromMeta(meta + 1), 3);
                    } else {
                        bullet.world.setBlockState(base, ModBlocks.block_foam.getDefaultState(), 3);
                    }
                }
            }

            if (fizz) {
                bullet.world.playSound(null, bullet.posX, bullet.posY, bullet.posZ, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 1.0F, 1.5F + bullet.world.rand.nextFloat() * 0.5F);
            }
        }
    };

    public static Consumer<Entity> LAMBDA_FOAM_UPDATE = (bullet) -> {
        if(bullet.world.isRemote) {
            NBTTagCompound data = new NBTTagCompound();
            data.setString("type", "vanillaExt");
            data.setString("mode", "blockdust");
            data.setInteger("block", Block.getIdFromBlock(ModBlocks.block_foam));
            data.setDouble("posX", bullet.posX); data.setDouble("posY", bullet.posY); data.setDouble("posZ", bullet.posZ);
            data.setDouble("mX", bullet.motionX + bullet.world.rand.nextGaussian() * 0.1);
            data.setDouble("mY", bullet.motionY - 0.2 + bullet.world.rand.nextGaussian() * 0.1);
            data.setDouble("mZ", bullet.motionZ + bullet.world.rand.nextGaussian() * 0.1);
            MainRegistry.proxy.effectNT(data);
        }
    };

    public static BiConsumer<EntityBulletBaseMK4, RayTraceResult> LAMBDA_SAND_HIT = (bullet, mop) -> {
        if (!bullet.world.isRemote && mop.typeOfHit == mop.typeOfHit.BLOCK) {
            BlockPos pos = mop.getBlockPos();
            int ix = pos.getX();
            int iy = pos.getY();
            int iz = pos.getZ();

            TileEntity core = CompatExternal.getCoreFromPos(bullet.world, new BlockPos(ix, iy, iz));
            if (core instanceof IRepairable) {
                ((IRepairable) core).tryExtinguish(bullet.world, ix, iy, iz, IRepairable.EnumExtinguishType.SAND);
                return;
            }

            if (bullet.world.rand.nextBoolean()) {
                EnumFacing dir = mop.sideHit;
                pos = pos.offset(dir);
            }

            IBlockState state = bullet.world.getBlockState(pos);
            Block b = state.getBlock();

            if ((b.isReplaceable(bullet.world, pos) || b == ModBlocks.sand_boron_layer) && ModBlocks.sand_boron_layer.canPlaceBlockAt(bullet.world, pos)) {
                if (b != ModBlocks.sand_boron_layer) {
                    bullet.world.setBlockState(pos, ModBlocks.sand_boron_layer.getDefaultState(), 3);
                } else {
                    int meta = b.getMetaFromState(state);
                    if (meta < 6) {
                        bullet.world.setBlockState(pos, b.getStateFromMeta(meta + 1), 3);
                    } else {
                        bullet.world.setBlockState(pos, ModBlocks.sand_boron.getDefaultState(), 3);
                    }
                }
                if (state.getMaterial() == Material.FIRE) {
                    bullet.world.playSound(null, bullet.posX, bullet.posY, bullet.posZ, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 1.0F, 1.5F + bullet.world.rand.nextFloat() * 0.5F);
                }
            }
        }
    };

    public static Consumer<Entity> LAMBDA_SAND_UPDATE = (bullet) -> {
        if(bullet.world.isRemote) {
            NBTTagCompound data = new NBTTagCompound();
            data.setString("type", "vanillaExt");
            data.setString("mode", "blockdust");
            data.setInteger("block", Block.getIdFromBlock(ModBlocks.sand_boron));
            data.setDouble("posX", bullet.posX); data.setDouble("posY", bullet.posY); data.setDouble("posZ", bullet.posZ);
            data.setDouble("mX", bullet.motionX + bullet.world.rand.nextGaussian() * 0.1);
            data.setDouble("mY", bullet.motionY - 0.2 + bullet.world.rand.nextGaussian() * 0.1);
            data.setDouble("mZ", bullet.motionZ + bullet.world.rand.nextGaussian() * 0.1);
            MainRegistry.proxy.effectNT(data);
        }
    };

    public static Consumer<Entity> LAMBDA_SET_HOOK = (entity) -> {
        EntityBulletBaseMK4 bullet = (EntityBulletBaseMK4) entity;
        if(!bullet.world.isRemote && bullet.ticksExisted < 2 && bullet.getThrower() instanceof EntityPlayer player) {
            if(!player.getHeldItemMainhand().isEmpty() && player.getHeldItemMainhand().getItem() == ModItems.gun_charge_thrower) {
                ItemGunChargeThrower.setLastHook(player.getHeldItemMainhand(), bullet.getEntityId());
            }
        }
        bullet.ignoreFrustumCheck = true;
    };

    public static BiConsumer<EntityBulletBaseMK4, RayTraceResult> LAMBDA_HOOK = (bullet, mop) -> {
        if (mop.typeOfHit == RayTraceResult.Type.BLOCK) {
            Vec3NT vec = new Vec3NT(-bullet.motionX, -bullet.motionY, -bullet.motionZ).normalizeSelf().multiply(0.05);
            bullet.setPosition(mop.hitVec.x + vec.x, mop.hitVec.y + vec.y, mop.hitVec.z + vec.z);
            BlockPos pos = mop.getBlockPos();
            bullet.getStuck(pos, mop.sideHit.getIndex());
        }
    };

    public static BiConsumer<EntityBulletBaseMK4, RayTraceResult> LAMBDA_MORTAR = (bullet, mop) -> {
        if (mop.typeOfHit == RayTraceResult.Type.ENTITY && bullet.ticksExisted < 3 && mop.entityHit == bullet.getThrower()) return;
        ExplosionVNT vnt = new ExplosionVNT(bullet.world, mop.hitVec.x, mop.hitVec.y, mop.hitVec.z, 5, bullet.getThrower());
        vnt.setBlockAllocator(new BlockAllocatorBulkie(60, 8));
        vnt.setBlockProcessor(new BlockProcessorStandard());
        vnt.setEntityProcessor(new EntityProcessorCrossSmooth(1, bullet.damage).setupPiercing(bullet.config.armorThresholdNegation, bullet.config.armorPiercingPercent));
        vnt.setPlayerProcessor(new PlayerProcessorStandard());
        vnt.setSFX(new ExplosionEffectWeapon(10, 2.5F, 1F));
        vnt.explode();
        bullet.setDead();
    };

    public static BiConsumer<EntityBulletBaseMK4, RayTraceResult> LAMBDA_MORTAR_CHARGE = (bullet, mop) -> {
        if (mop.typeOfHit == RayTraceResult.Type.ENTITY && bullet.ticksExisted < 3 && mop.entityHit == bullet.getThrower()) return;
        ExplosionVNT vnt = new ExplosionVNT(bullet.world, mop.hitVec.x, mop.hitVec.y, mop.hitVec.z, 15, bullet.getThrower());
        vnt.setBlockAllocator(new BlockAllocatorStandard());
        vnt.setBlockProcessor(new BlockProcessorStandard().setNoDrop().withBlockEffect(new BlockMutatorDebris(ModBlocks.block_slag, 1)));
        vnt.setEntityProcessor(new EntityProcessorCrossSmooth(1, bullet.damage).setupPiercing(bullet.config.armorThresholdNegation, bullet.config.armorPiercingPercent));
        vnt.setPlayerProcessor(new PlayerProcessorStandard());
        ExplosionCreator.composeEffectSmall(bullet.world, mop.hitVec.x, mop.hitVec.y + 0.5, mop.hitVec.z);
        vnt.explode();
        bullet.setDead();
    };

    public static void init() {

        fext_water = new BulletConfig().setItem(new ItemStack(ModItems.ammo_fireext, 1, 0)).setReloadCount(300).setLife(100).setVel(0.75F).setGrav(0.04F).setSpread(0.025F)
                .setOnUpdate(LAMBDA_WATER_UPDATE)
                .setOnEntityHit((bulletEntity, target) -> { if(target.entityHit != null) target.entityHit.extinguish(); })
                .setOnRicochet(LAMBDA_WATER_HIT);
        fext_foam = new BulletConfig().setItem(new ItemStack(ModItems.ammo_fireext, 1, 1)).setReloadCount(300).setLife(100).setVel(0.75F).setGrav(0.04F).setSpread(0.05F)
                .setOnUpdate(LAMBDA_FOAM_UPDATE)
                .setOnEntityHit((bulletEntity, target) -> { if(target.entityHit != null) target.entityHit.extinguish(); })
                .setOnRicochet(LAMBDA_FOAM_HIT);
        fext_sand = new BulletConfig().setItem(new ItemStack(ModItems.ammo_fireext, 1, 2)).setReloadCount(300).setLife(100).setVel(0.75F).setGrav(0.04F).setSpread(0.05F)
                .setOnUpdate(LAMBDA_SAND_UPDATE)
                .setOnEntityHit((bulletEntity, target) -> { if(target.entityHit != null) target.entityHit.extinguish(); })
                .setOnRicochet(LAMBDA_SAND_HIT);

        ct_hook = new BulletConfig().setItem(GunFactory.EnumAmmo.CT_HOOK).setRenderRotations(false).setLife(6_000).setVel(3F).setGrav(0.035F).setDoesPenetrate(true).setDamageFalloffByPen(false)
                .setOnUpdate(LAMBDA_SET_HOOK).setOnImpact(LAMBDA_HOOK);
        ct_mortar = new BulletConfig().setItem(GunFactory.EnumAmmo.CT_MORTAR).setDamage(2.5F).setLife(200).setVel(3F).setGrav(0.035F)
                .setOnImpact(LAMBDA_MORTAR);
        ct_mortar_charge = new BulletConfig().setItem(GunFactory.EnumAmmo.CT_MORTAR_CHARGE).setDamage(5F).setLife(200).setVel(3F).setGrav(0.035F)
                .setOnImpact(LAMBDA_MORTAR_CHARGE);

        ModItems.gun_fireext = new ItemGunBaseNT(ItemGunBaseNT.WeaponQuality.UTILITY, "gun_fireext", new GunConfig()
                .dura(5_000).draw(10).inspect(55).reloadChangeType(true).hideCrosshair(false).crosshair(RenderScreenOverlay.Crosshair.L_CIRCLE)
                .rec(new Receiver(0)
                        .dmg(0F).delay(1).dry(0).auto(true).spread(0F).spreadHipfire(0F).reload(20).jam(0).sound(HBMSoundHandler.fireExtinguisher, 1.0F, 1.0F)
                        .mag(new MagazineFullReload(0, 300).addConfigs(fext_water, fext_foam, fext_sand))
                        .offset(1, -0.0625 * 2.5, -0.25D)
                        .setupStandardFire())
                .setupStandardConfiguration()
                .orchestra(Orchestras.ORCHESTRA_FIREEXT)
        );

        ModItems.gun_charge_thrower = new ItemGunChargeThrower(ItemGunBaseNT.WeaponQuality.UTILITY, "gun_charge_thrower", new GunConfig()
                .dura(3_000).draw(10).inspect(55).reloadChangeType(true).hideCrosshair(false).crosshair(RenderScreenOverlay.Crosshair.L_CIRCUMFLEX)
                .rec(new Receiver(0)
                        .dmg(10F).delay(4).dry(10).auto(true).spread(0F).spreadHipfire(0F).reload(60).jam(0).sound(HBMSoundHandler.fireGrenade, 1.0F, 1.0F)
                        .mag(new MagazineFullReload(0, 1).addConfigs(ct_hook, ct_mortar, ct_mortar_charge))
                        .offset(1, -0.0625 * 2.5, -0.25D)
                        .setupStandardFire().recoil(LAMBDA_RECOIL_CT))
                .setupStandardConfiguration()
                .anim(LAMBDA_CT_ANIMS).orchestra(Orchestras.ORCHESTRA_CHARGE_THROWER)
        );
    }

    public static BiConsumer<ItemStack, ItemGunBaseNT.LambdaContext> LAMBDA_RECOIL_CT = (stack, ctx) -> ItemGunBaseNT.setupRecoil(10, (float) (ctx.getPlayer().getRNG().nextGaussian() * 1.5));

    @SuppressWarnings("incomplete-switch") public static BiFunction<ItemStack, HbmAnimationsSedna.AnimType, BusAnimationSedna> LAMBDA_CT_ANIMS = (stack, type) -> switch (type) {
        case EQUIP -> new BusAnimationSedna()
                .addBus("EQUIP", new BusAnimationSequenceSedna().addPos(-45, 0, 0, 0).addPos(0, 0, 0, 500, IType.SIN_DOWN));
        case CYCLE -> new BusAnimationSedna()
                .addBus("RECOIL", new BusAnimationSequenceSedna().addPos(0, 0, -1, 100, IType.SIN_DOWN).addPos(0, 0, 0, 250, IType.SIN_FULL));
        case RELOAD -> new BusAnimationSedna()
                .addBus("RAISE", new BusAnimationSequenceSedna().addPos(-45, 0, 0, 500, IType.SIN_FULL).hold(2000).addPos(0, 0, 0, 500, IType.SIN_FULL))
                .addBus("AMMO", new BusAnimationSequenceSedna().setPos(0, -10, -5).hold(500).addPos(0, 0, 5, 750, IType.SIN_FULL).addPos(0, 0, 0, 500, IType.SIN_UP).hold(4000))
                .addBus("TWIST", new BusAnimationSequenceSedna().setPos(0, 0, 25).hold(2000).addPos(0, 0, 0, 150));
        case INSPECT -> new BusAnimationSedna()
                .addBus("TURN", new BusAnimationSequenceSedna().addPos(0, 60, 0, 500, IType.SIN_FULL).hold(1750).addPos(0, 0, 0, 500, IType.SIN_FULL))
                .addBus("ROLL", new BusAnimationSequenceSedna().hold(750).addPos(0, 0, -90, 500, IType.SIN_FULL).hold(1000).addPos(0, 0, 0, 500, IType.SIN_FULL));
        default -> null;
    };
}
