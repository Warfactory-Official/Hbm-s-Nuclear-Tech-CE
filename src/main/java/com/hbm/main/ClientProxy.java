package com.hbm.main;

import com.hbm.Tags;
import com.hbm.animloader.AnimationWrapper.EndResult;
import com.hbm.animloader.AnimationWrapper.EndType;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.bomb.DigammaMatter;
import com.hbm.blocks.fluid.FluidFogHandler;
import com.hbm.blocks.generic.BMPowerBox;
import com.hbm.blocks.generic.BlockModDoor;
import com.hbm.blocks.generic.TrappedBrick;
import com.hbm.blocks.machine.BlockSeal;
import com.hbm.blocks.machine.rbmk.RBMKDebrisRadiating;
import com.hbm.config.GeneralConfig;
import com.hbm.entity.grenade.*;
import com.hbm.entity.particle.*;
import com.hbm.entity.projectile.EntityAcidBomb;
import com.hbm.entity.projectile.EntityDischarge;
import com.hbm.handler.*;
import com.hbm.handler.HbmKeybinds.EnumKeybind;
import com.hbm.items.IAnimatedItem;
import com.hbm.items.ModItems;
import com.hbm.items.RBMKItemRenderers;
import com.hbm.items.weapon.sedna.factory.GunFactoryClient;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.RecoilHandler;
import com.hbm.main.client.NTMClientRegistry;
import com.hbm.command.CommandRadVisClient;
import com.hbm.particle.*;
import com.hbm.particle.bfg.*;
import com.hbm.particle.bullet_hit.ParticleBloodParticle;
import com.hbm.particle.bullet_hit.ParticleBulletImpact;
import com.hbm.particle.bullet_hit.ParticleHitDebris;
import com.hbm.particle.bullet_hit.ParticleSmokeAnim;
import com.hbm.particle.helper.ParticleCreators;
import com.hbm.particle_instanced.InstancedParticleRenderer;
import com.hbm.particle_instanced.ParticleContrailInstanced;
import com.hbm.particle_instanced.ParticleExSmokeInstanced;
import com.hbm.particle_instanced.ParticleRocketFlameInstanced;
import com.hbm.qmaw.QMAWLoader;
import com.hbm.render.GLCompat;
import com.hbm.render.anim.BusAnimation;
import com.hbm.render.anim.BusAnimationKeyframe;
import com.hbm.render.anim.BusAnimationSequence;
import com.hbm.render.anim.HbmAnimations;
import com.hbm.render.anim.HbmAnimations.Animation;
import com.hbm.render.anim.HbmAnimations.BlenderAnimation;
import com.hbm.render.anim.sedna.BusAnimationSedna;
import com.hbm.render.anim.sedna.BusAnimationSequenceSedna;
import com.hbm.render.anim.sedna.HbmAnimationsSedna;
import com.hbm.render.entity.ElectricityRenderer;
import com.hbm.render.entity.RenderBoat;
import com.hbm.render.entity.RenderMetaSensitiveItem;
import com.hbm.render.item.ItemRenderMissile;
import com.hbm.render.item.ItemRenderMissileGeneric;
import com.hbm.render.item.ItemRenderMissileGeneric.RenderMissileType;
import com.hbm.render.item.ItemRenderMissilePart;
import com.hbm.render.item.weapon.ItemRenderGunAnim;
import com.hbm.render.item.weapon.sedna.*;
import com.hbm.render.misc.MissilePart;
import com.hbm.render.modelrenderer.EgonBackpackRenderer;
import com.hbm.render.tileentity.IItemRendererProvider;
import com.hbm.render.util.RenderInfoSystemLegacy;
import com.hbm.render.util.RenderOverhead;
import com.hbm.sound.AudioWrapper;
import com.hbm.sound.AudioWrapperClient;
import com.hbm.sound.AudioWrapperClientStartStop;
import com.hbm.sound.SoundLoopCrucible;
import com.hbm.util.*;
import com.hbm.wiaj.cannery.Jars;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockStainedHardenedClay;
import net.minecraft.block.BlockStone;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.particle.*;
import net.minecraft.client.particle.ParticleFirework.Spark;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GLContext;
import paulscode.sound.SoundSystemConfig;

import java.awt.*;
import java.io.File;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ClientProxy extends ServerProxy {

    public static final ModelResourceLocation IRRELEVANT_MRL = new ModelResourceLocation("hbm:placeholdermodel", "inventory");
    public static final FloatBuffer AUX_GL_BUFFER = GLAllocation.createDirectFloatBuffer(16);
    public static final FloatBuffer AUX_GL_BUFFER2 = GLAllocation.createDirectFloatBuffer(16);
    //Drillgon200: Will I ever figure out how to write better code than this?
    public static final List<Runnable> deferredRenderers = new ArrayList<>();
    public static KeyBinding jetpackActivate;
    public static KeyBinding jetpackHover;
    public static KeyBinding jetpackHud;
    public static KeyBinding fsbFlashlight;
    public static KeyBinding craneUpKey;
    public static KeyBinding craneDownKey;
    public static KeyBinding craneLeftKey;
    public static KeyBinding craneRightKey;
    public static KeyBinding craneLoadKey;
    //Drillgon200: This is stupid, but I'm lazy
    public static boolean renderingConstant = false;
    public RenderInfoSystemLegacy theInfoSystem = new RenderInfoSystemLegacy();
    private static final Int2LongOpenHashMap vanished = new Int2LongOpenHashMap();

    public static void registerItemRenderer(Item i, TileEntityItemStackRenderer render, IRegistry<ModelResourceLocation, IBakedModel> reg) {
        i.setTileEntityItemStackRenderer(render);
        NTMClientRegistry.swapModels(i, reg);
    }

    @Override
    public File getDataDir() {
        return Minecraft.getMinecraft().gameDir;
    }

    @Override
    public void init(FMLInitializationEvent evt) {
        FluidFogHandler.init();
        // All previous color handler registrations here have been moved to ModEventHandlerClient#itemColorsEvent
        // and ModEventHandlerClient#blockColorsEvent
    }

    @Override
    public void registerRenderInfo() {
        if (!Minecraft.getMinecraft().getFramebuffer().isStencilEnabled())
            Minecraft.getMinecraft().getFramebuffer().enableStencil();

        MinecraftForge.EVENT_BUS.register(new ModEventHandlerClient());
        MinecraftForge.EVENT_BUS.register(new NTMClientRegistry());
        MinecraftForge.EVENT_BUS.register(new ModEventHandlerRenderer());
        MinecraftForge.EVENT_BUS.register(new PlacementPreviewHandler());
        MinecraftForge.EVENT_BUS.register(theInfoSystem);
        ClientCommandHandler.instance.registerCommand(new CommandRadVisClient());

        HbmShaderManager.loadShaders();

        jetpackActivate = new KeyBinding("key.jetpack_activate", KeyConflictContext.IN_GAME, Keyboard.KEY_J, "key.categories.hbm");
        ClientRegistry.registerKeyBinding(jetpackActivate);
        jetpackHover = new KeyBinding("key.jetpack_hover", KeyConflictContext.IN_GAME, Keyboard.KEY_H, "key.categories.hbm");
        ClientRegistry.registerKeyBinding(jetpackHover);
        jetpackHud = new KeyBinding("key.jetpack_hud", KeyConflictContext.IN_GAME, Keyboard.KEY_U, "key.categories.hbm");
        ClientRegistry.registerKeyBinding(jetpackHud);
        fsbFlashlight = new KeyBinding("key.fsb_flashlight", KeyConflictContext.IN_GAME, Keyboard.KEY_NUMPAD6, "key.categories.hbm");
        ClientRegistry.registerKeyBinding(fsbFlashlight);

        HbmKeybinds.register();
        Jars.initJars();

        ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(new QMAWLoader());

//        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineAssembler.class, new RenderAssembler());
        // TODO: replace it with EntityCombineBallNT
        /*RenderingRegistry.registerEntityRenderingHandler(EntityCombineBall.class, (RenderManager man) -> {
        });*/
        RenderingRegistry.registerEntityRenderingHandler(EntityDischarge.class, ElectricityRenderer.FACTORY);
        RenderingRegistry.registerEntityRenderingHandler(EntityGrenadeGeneric.class, (RenderManager man) -> {
            return new RenderSnowball<EntityGrenadeGeneric>(man, ModItems.grenade_generic, Minecraft.getMinecraft().getRenderItem());
        });
        registerGrenadeRenderer(EntityGrenadeStrong.class, ModItems.grenade_strong);
        registerGrenadeRenderer(EntityGrenadeFrag.class, ModItems.grenade_frag);
        registerGrenadeRenderer(EntityGrenadeFire.class, ModItems.grenade_fire);
        registerGrenadeRenderer(EntityGrenadeCluster.class, ModItems.grenade_cluster);
        registerGrenadeRenderer(EntityGrenadeElectric.class, ModItems.grenade_electric);
        registerGrenadeRenderer(EntityGrenadePoison.class, ModItems.grenade_poison);
        registerGrenadeRenderer(EntityGrenadeGas.class, ModItems.grenade_gas);
        registerGrenadeRenderer(EntityGrenadeSchrabidium.class, ModItems.grenade_schrabidium);
        registerGrenadeRenderer(EntityGrenadePulse.class, ModItems.grenade_pulse);
        registerGrenadeRenderer(EntityGrenadePlasma.class, ModItems.grenade_plasma);
        registerGrenadeRenderer(EntityGrenadeTau.class, ModItems.grenade_tau);
        registerGrenadeRenderer(EntityGrenadeCloud.class, ModItems.grenade_cloud);
        registerGrenadeRenderer(EntityGrenadePC.class, ModItems.grenade_pink_cloud);
        registerGrenadeRenderer(EntityGrenadeSmart.class, ModItems.grenade_smart);
        registerGrenadeRenderer(EntityGrenadeMIRV.class, ModItems.grenade_mirv);
        registerGrenadeRenderer(EntityGrenadeBreach.class, ModItems.grenade_breach);
        registerGrenadeRenderer(EntityGrenadeBurst.class, ModItems.grenade_burst);
        registerGrenadeRenderer(EntityGrenadeLemon.class, ModItems.grenade_lemon);
        registerGrenadeRenderer(EntityGrenadeZOMG.class, ModItems.grenade_zomg);
        registerGrenadeRenderer(EntityGrenadeSolinium.class, ModItems.grenade_solinium);
        registerGrenadeRenderer(EntityGrenadeShrapnel.class, ModItems.grenade_shrapnel);
        registerGrenadeRenderer(EntityGrenadeBlackHole.class, ModItems.grenade_black_hole);
        registerGrenadeRenderer(EntityGrenadeGascan.class, ModItems.grenade_gascan);
        registerGrenadeRenderer(EntityGrenadeNuke.class, ModItems.grenade_nuke);
        registerGrenadeRenderer(EntityGrenadeNuclear.class, ModItems.grenade_nuclear);
        registerGrenadeRenderer(EntityGrenadeIFGeneric.class, ModItems.grenade_if_generic);
        registerGrenadeRenderer(EntityGrenadeIFHE.class, ModItems.grenade_if_he);
        registerGrenadeRenderer(EntityGrenadeIFBouncy.class, ModItems.grenade_if_bouncy);
        registerGrenadeRenderer(EntityGrenadeIFSticky.class, ModItems.grenade_if_sticky);
        registerGrenadeRenderer(EntityGrenadeIFImpact.class, ModItems.grenade_if_impact);
        registerGrenadeRenderer(EntityGrenadeIFIncendiary.class, ModItems.grenade_if_incendiary);
        registerGrenadeRenderer(EntityGrenadeIFToxic.class, ModItems.grenade_if_toxic);
        registerGrenadeRenderer(EntityGrenadeIFConcussion.class, ModItems.grenade_if_concussion);
        registerGrenadeRenderer(EntityGrenadeIFBrimstone.class, ModItems.grenade_if_brimstone);
        registerGrenadeRenderer(EntityGrenadeIFMystery.class, ModItems.grenade_if_mystery);
        registerGrenadeRenderer(EntityGrenadeIFSpark.class, ModItems.grenade_if_spark);
        registerGrenadeRenderer(EntityGrenadeIFHopwire.class, ModItems.grenade_if_hopwire);
        registerGrenadeRenderer(EntityGrenadeIFNull.class, ModItems.grenade_if_null);
        registerGrenadeRenderer(EntityGrenadeDynamite.class, ModItems.stick_dynamite);
        registerGrenadeRenderer(EntityAcidBomb.class, Items.SLIME_BALL);
        registerGrenadeRenderer(EntityGrenadeBouncyGeneric.class, ModItems.stick_dynamite_fishing);
        registerGrenadeRenderer(EntityGrenadeImpactGeneric.class, ModItems.grenade_kyiv);
        registerMetaSensitiveGrenade(EntityDisperserCanister.class, ModItems.disperser_canister);
        registerMetaSensitiveGrenade(EntityDisperserCanister.class, ModItems.glyphid_gland);

        AutoRegistry.registerRenderInfo();

        ModelLoader.setCustomStateMapper(ModBlocks.door_bunker, new StateMap.Builder().ignore(BlockModDoor.POWERED).build());
        ModelLoader.setCustomStateMapper(ModBlocks.door_metal, new StateMap.Builder().ignore(BlockModDoor.POWERED).build());
        ModelLoader.setCustomStateMapper(ModBlocks.door_office, new StateMap.Builder().ignore(BlockModDoor.POWERED).build());
        ModelLoader.setCustomStateMapper(ModBlocks.door_red, new StateMap.Builder().ignore(BlockModDoor.POWERED).build());

        ModelLoader.setCustomStateMapper(ModBlocks.toxic_block, new StateMap.Builder().ignore(BlockFluidClassic.LEVEL).build());
        ModelLoader.setCustomStateMapper(ModBlocks.mud_block, new StateMap.Builder().ignore(BlockFluidClassic.LEVEL).build());
        ModelLoader.setCustomStateMapper(ModBlocks.acid_block, new StateMap.Builder().ignore(BlockFluidClassic.LEVEL).build());
        ModelLoader.setCustomStateMapper(ModBlocks.schrabidic_block, new StateMap.Builder().ignore(BlockFluidClassic.LEVEL).build());
        ModelLoader.setCustomStateMapper(ModBlocks.corium_block, new StateMap.Builder().ignore(BlockFluidClassic.LEVEL).build());
        ModelLoader.setCustomStateMapper(ModBlocks.volcanic_lava_block, new StateMap.Builder().ignore(BlockFluidClassic.LEVEL).build());
        ModelLoader.setCustomStateMapper(ModBlocks.sulfuric_acid_block, new StateMap.Builder().ignore(BlockFluidClassic.LEVEL).build());

        ModelLoader.setCustomStateMapper(ModBlocks.seal_controller, new StateMap.Builder().ignore(BlockSeal.ACTIVATED).build());
        ModelLoader.setCustomStateMapper(ModBlocks.ntm_dirt, new StateMap.Builder().ignore(BlockDirt.SNOWY).ignore(BlockDirt.VARIANT).build());
        ModelLoader.setCustomStateMapper(ModBlocks.brick_jungle_trap, new StateMap.Builder().ignore(TrappedBrick.TYPE).build());
        ModelLoader.setCustomStateMapper(ModBlocks.stone_porous, new StateMap.Builder().ignore(BlockStone.VARIANT).build());
        ModelLoader.setCustomStateMapper(ModBlocks.volcano_core, new StateMap.Builder().ignore(BlockDummyable.META).build());
        ModelLoader.setCustomStateMapper(ModBlocks.bm_power_box, new StateMap.Builder().ignore(BMPowerBox.FACING, BMPowerBox.IS_ON).build());
        //Drillgon200: This can't be efficient, but eh.
        for (Block b : ModBlocks.ALL_BLOCKS) {
            if (b instanceof BlockDummyable || b instanceof RBMKDebrisRadiating || b instanceof DigammaMatter)
                ModelLoader.setCustomStateMapper(b, new StateMap.Builder().ignore(BlockDummyable.META).build());
        }
    }

    private <E extends Entity> void registerGrenadeRenderer(Class<E> clazz, Item grenade) {
        RenderingRegistry.registerEntityRenderingHandler(clazz, (RenderManager man) -> {
            return new RenderSnowball<E>(man, grenade, Minecraft.getMinecraft().getRenderItem());
        });
    }

    private <E extends Entity & RenderMetaSensitiveItem.IHasMetaSensitiveRenderer<E>> void registerMetaSensitiveGrenade(Class<E> clazz, Item grenade) {
        RenderingRegistry.registerEntityRenderingHandler(clazz, (RenderManager man) ->
                new RenderMetaSensitiveItem<>(man, grenade, Minecraft.getMinecraft().getRenderItem()));
    }

    @Override
    public void registerMissileItems(IRegistry<ModelResourceLocation, IBakedModel> reg) {
        MissilePart.registerAllParts();

        MissilePart.parts.values().forEach(part -> {
            registerItemRenderer(part.part, new ItemRenderMissilePart(part), reg);
        });

        registerItemRenderer(ModItems.missile_custom, new ItemRenderMissile(), reg);

        ItemRenderMissileGeneric.init();

        // GUNS
        registerItemRenderer(ModItems.gun_light_revolver, new ItemRenderAtlas(ResourceManager.bio_revolver_tex), reg);
        registerItemRenderer(ModItems.gun_light_revolver_atlas, new ItemRenderAtlas(ResourceManager.bio_revolver_atlas_tex), reg);
        registerItemRenderer(ModItems.gun_double_barrel, new ItemRenderDoubleBarrel(ResourceManager.double_barrel_tex), reg);
        registerItemRenderer(ModItems.gun_double_barrel_sacred_dragon, new ItemRenderDoubleBarrel(ResourceManager.double_barrel_sacred_dragon_tex), reg);
        registerItemRenderer(ModItems.gun_flamer, new ItemRenderFlamer(ResourceManager.flamethrower_tex), reg);
        registerItemRenderer(ModItems.gun_flamer_topaz, new ItemRenderFlamer(ResourceManager.flamethrower_topaz_tex), reg);
        registerItemRenderer(ModItems.gun_flamer_daybreaker, new ItemRenderFlamer(ResourceManager.flamethrower_daybreaker_tex), reg);
        registerItemRenderer(ModItems.gun_heavy_revolver, new ItemRenderHeavyRevolver(ResourceManager.heavy_revolver_tex), reg);
        registerItemRenderer(ModItems.gun_heavy_revolver_lilmac, new ItemRenderHeavyRevolver(ResourceManager.lilmac_tex), reg);
        registerItemRenderer(ModItems.gun_heavy_revolver_protege, new ItemRenderHeavyRevolver(ResourceManager.heavy_revolver_protege_tex), reg);
        registerItemRenderer(ModItems.gun_maresleg, new ItemRenderMaresleg(ResourceManager.maresleg_tex), reg);
        registerItemRenderer(ModItems.gun_maresleg_broken, new ItemRenderMaresleg(ResourceManager.maresleg_broken_tex), reg);
        registerItemRenderer(ModItems.gun_minigun, new ItemRenderMinigun(ResourceManager.minigun_tex), reg);
        registerItemRenderer(ModItems.gun_minigun_lacunae, new ItemRenderMinigun(ResourceManager.minigun_lacunae_tex), reg);
        registerItemRenderer(ModItems.gun_autoshotgun, new ItemRenderShredder(ResourceManager.shredder_tex), reg);
        registerItemRenderer(ModItems.gun_autoshotgun_shredder, new ItemRenderShredder(ResourceManager.shredder_orig_tex), reg);
        registerItemRenderer(ModItems.gun_amat, new ItemRenderAmat(ResourceManager.amat_tex), reg);
        registerItemRenderer(ModItems.gun_amat_penance, new ItemRenderAmat(ResourceManager.amat_penance_tex), reg);
        registerItemRenderer(ModItems.gun_amat_subtlety, new ItemRenderAmat(ResourceManager.amat_subtlety_tex), reg);
        registerItemRenderer(ModItems.gun_g3, new ItemRenderG3(ResourceManager.g3_tex), reg);
        registerItemRenderer(ModItems.gun_g3_zebra, new ItemRenderG3(ResourceManager.g3_zebra_tex), reg);
        registerItemRenderer(ModItems.gun_henry, new ItemRenderHenry(ResourceManager.henry_tex), reg);
        registerItemRenderer(ModItems.gun_henry_lincoln, new ItemRenderHenry(ResourceManager.henry_lincoln_tex), reg);
        registerItemRenderer(ModItems.gun_laser_pistol, new ItemRenderLaserPistol(ResourceManager.laser_pistol_tex), reg);
        registerItemRenderer(ModItems.gun_laser_pistol_pew_pew, new ItemRenderLaserPistol(ResourceManager.laser_pistol_pew_pew_tex), reg);
        registerItemRenderer(ModItems.gun_laser_pistol_morning_glory, new ItemRenderLaserPistol(ResourceManager.laser_pistol_morning_glory_tex), reg);
        registerItemRenderer(ModItems.gun_autoshotgun_sexy, new ItemRenderSexy(ResourceManager.sexy_tex), reg);
        registerItemRenderer(ModItems.gun_autoshotgun_heretic, new ItemRenderSexy(ResourceManager.heretic_tex), reg);
        //
        registerItemRenderer(ModItems.missile_taint, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER0), reg);
        registerItemRenderer(ModItems.missile_micro, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER0), reg);
        registerItemRenderer(ModItems.missile_bhole, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER0), reg);
        registerItemRenderer(ModItems.missile_schrabidium, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER0), reg);
        registerItemRenderer(ModItems.missile_emp, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER0), reg);
        registerItemRenderer(ModItems.missile_generic, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER1), reg);
        registerItemRenderer(ModItems.missile_decoy, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER1), reg);
        registerItemRenderer(ModItems.missile_stealth, new ItemRenderMissileGeneric(RenderMissileType.TYPE_STEALTH), reg);
        registerItemRenderer(ModItems.missile_incendiary, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER1), reg);
        registerItemRenderer(ModItems.missile_cluster, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER1), reg);
        registerItemRenderer(ModItems.missile_buster, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER1), reg);
        registerItemRenderer(ModItems.missile_anti_ballistic, new ItemRenderMissileGeneric(RenderMissileType.TYPE_ABM), reg);
        registerItemRenderer(ModItems.missile_strong, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER2), reg);
        registerItemRenderer(ModItems.missile_incendiary_strong, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER2), reg);
        registerItemRenderer(ModItems.missile_cluster_strong, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER2), reg);
        registerItemRenderer(ModItems.missile_buster_strong, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER2), reg);
        registerItemRenderer(ModItems.missile_emp_strong, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER2), reg);
        registerItemRenderer(ModItems.missile_burst, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER3), reg);
        registerItemRenderer(ModItems.missile_inferno, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER3), reg);
        registerItemRenderer(ModItems.missile_rain, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER3), reg);
        registerItemRenderer(ModItems.missile_drill, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER3), reg);
        registerItemRenderer(ModItems.missile_nuclear, new ItemRenderMissileGeneric(RenderMissileType.TYPE_NUCLEAR), reg);
        registerItemRenderer(ModItems.missile_nuclear_cluster, new ItemRenderMissileGeneric(RenderMissileType.TYPE_NUCLEAR), reg);
        registerItemRenderer(ModItems.missile_volcano, new ItemRenderMissileGeneric(RenderMissileType.TYPE_NUCLEAR), reg);
        registerItemRenderer(ModItems.missile_n2, new ItemRenderMissileGeneric(RenderMissileType.TYPE_NUCLEAR), reg);
        registerItemRenderer(ModItems.missile_endo, new ItemRenderMissileGeneric(RenderMissileType.TYPE_THERMAL), reg);
        registerItemRenderer(ModItems.missile_exo, new ItemRenderMissileGeneric(RenderMissileType.TYPE_THERMAL), reg);
        registerItemRenderer(ModItems.missile_doomsday, new ItemRenderMissileGeneric(RenderMissileType.TYPE_DOOMSDAY), reg);
        registerItemRenderer(ModItems.missile_doomsday_rusted, new ItemRenderMissileGeneric(RenderMissileType.TYPE_DOOMSDAY), reg);
        registerItemRenderer(ModItems.missile_carrier, new ItemRenderMissileGeneric(RenderMissileType.TYPE_CARRIER), reg);
        registerItemRenderer(ModItems.gun_b92, ItemRenderGunAnim.INSTANCE, reg);
    }

    @Override
    public void registerGunCfg() {
        GunFactoryClient.init();
    }

    @Override
    public void registerTileEntitySpecialRenderer() {

    }

    @Override
    public void particleControl(double x, double y, double z, int type) {
        World world = Minecraft.getMinecraft().world;

        switch (type) {
            case 0:

                for (int i = 0; i < 10; i++) {
                    Particle smoke = new ParticleCloud.Factory().createParticle(EnumParticleTypes.CLOUD.getParticleID(), world, x + world.rand.nextGaussian(), y + world.rand.nextGaussian(), z + world.rand.nextGaussian(), 0.0, 0.0, 0.0);
                    Minecraft.getMinecraft().effectRenderer.addEffect(smoke);
                }
                break;

            case 1:
                Particle s = new ParticleCloud.Factory().createParticle(EnumParticleTypes.CLOUD.getParticleID(), world, x, y, z, 0.0, 0.1, 0.0);
                Minecraft.getMinecraft().effectRenderer.addEffect(s);

                break;

            case 2:
                if (GeneralConfig.instancedParticles) {
                    ParticleContrailInstanced contrail2 = new ParticleContrailInstanced(world, x, y, z);
                    InstancedParticleRenderer.addParticle(contrail2);
                } else {
                    ParticleContrail contrail = new ParticleContrail(Minecraft.getMinecraft().renderEngine, world, x, y, z);
                    Minecraft.getMinecraft().effectRenderer.addEffect(contrail);
                }
                break;
            case 3: //Rad Fog

                ParticleRadiationFog fog = new ParticleRadiationFog(world, x, y, z);
                Minecraft.getMinecraft().effectRenderer.addEffect(fog);
                break;
            case 4:
                world.spawnParticle(EnumParticleTypes.FLAME, x + world.rand.nextDouble(), y + 1.1, z + world.rand.nextDouble(), 0.0, 0.0, 0.0);
                world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x + world.rand.nextDouble(), y + 1.1, z + world.rand.nextDouble(), 0.0, 0.0, 0.0);

                world.spawnParticle(EnumParticleTypes.FLAME, x - 0.1, y + world.rand.nextDouble(), z + world.rand.nextDouble(), 0.0, 0.0, 0.0);
                world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x - 0.1, y + world.rand.nextDouble(), z + world.rand.nextDouble(), 0.0, 0.0, 0.0);

                world.spawnParticle(EnumParticleTypes.FLAME, x + 1.1, y + world.rand.nextDouble(), z + world.rand.nextDouble(), 0.0, 0.0, 0.0);
                world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x + 1.1, y + world.rand.nextDouble(), z + world.rand.nextDouble(), 0.0, 0.0, 0.0);

                world.spawnParticle(EnumParticleTypes.FLAME, x + world.rand.nextDouble(), y + world.rand.nextDouble(), z - 0.1, 0.0, 0.0, 0.0);
                world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x + world.rand.nextDouble(), y + world.rand.nextDouble(), z - 0.1, 0.0, 0.0, 0.0);

                world.spawnParticle(EnumParticleTypes.FLAME, x + world.rand.nextDouble(), y + world.rand.nextDouble(), z + 1.1, 0.0, 0.0, 0.0);
                world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x + world.rand.nextDouble(), y + world.rand.nextDouble(), z + 1.1, 0.0, 0.0, 0.0);
                break;
        }
    }

    //version 2, now with strings!
    @Override
    public void spawnParticle(double x, double y, double z, String type, float[] args) {
        World world = Minecraft.getMinecraft().world;
        TextureManager man = Minecraft.getMinecraft().renderEngine;

        if ("launchsmoke".equals(type)) {
            ParticleSmokePlume contrail = new ParticleSmokePlume(man, world, x, y, z);
            Minecraft.getMinecraft().effectRenderer.addEffect(contrail);
            return;
        }
        if ("exKerosene".equals(type)) {
            ParticleContrail contrail = new ParticleContrailKerosene(man, world, x, y, z);
            if (args != null && args.length == 3)
                contrail.setMotion(args[0], args[1], args[2]);
            Minecraft.getMinecraft().effectRenderer.addEffect(contrail);
            return;
        }
        if ("exSolid".equals(type)) {
            ParticleContrail contrail = new ParticleContrailSolid(man, world, x, y, z);
            if (args != null && args.length == 3)
                contrail.setMotion(args[0], args[1], args[2]);
            Minecraft.getMinecraft().effectRenderer.addEffect(contrail);
            return;
        }
        if ("exHydrogen".equals(type)) {
            ParticleContrail contrail = new ParticleContrailHydrogen(man, world, x, y, z);
            if (args != null && args.length == 3)
                contrail.setMotion(args[0], args[1], args[2]);
            Minecraft.getMinecraft().effectRenderer.addEffect(contrail);
            return;
        }
        if ("exBalefire".equals(type)) {
            ParticleContrail contrail = new ParticleContrailBalefire(man, world, x, y, z);
            if (args != null && args.length == 3)
                contrail.setMotion(args[0], args[1], args[2]);
            Minecraft.getMinecraft().effectRenderer.addEffect(contrail);
            return;
        }
        if ("exDark".equals(type)) {
            ParticleContrail contrail = new ParticleContrailDark(man, world, x, y, z);
            if (args != null && args.length == 3)
                contrail.setMotion(args[0], args[1], args[2]);
            Minecraft.getMinecraft().effectRenderer.addEffect(contrail);
            return;
        }
        if ("bfg_fire".equals(type)) {
            BlockPos pos = new BlockPos(x, y, z);
            int fireAge = (int) args[0];
            if (fireAge >= 0) {
                if (fireAge >= 1 && fireAge <= 40) {
                    Vec3d attractionPoint = new Vec3d(pos.getX() + 0.5, pos.getY() + 24, pos.getZ() + 0.5 - 60);
                    for (int i = 0; i < world.rand.nextInt(6); i++) {
                        float randPosX = BobMathUtil.remap(world.rand.nextFloat(), 0, 1, -10, 10);
                        float randPosY = BobMathUtil.remap(world.rand.nextFloat(), 0, 1, -10, 10);
                        float randPosZ = BobMathUtil.remap(world.rand.nextFloat(), 0, 1, 0, 10);
                        float randMotionX = world.rand.nextFloat() * 0.4F - 0.2F;
                        float randMotionY = world.rand.nextFloat() * 0.4F - 0.2F;
                        float randMotionZ = world.rand.nextFloat() * 0.4F - 0.2F;
                        Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleBFGParticle(world, pos.getX() + 0.5 + randPosX, pos.getY() + 24 + randPosY, pos.getZ() + 0.5 - 74 + +randPosZ, randMotionX, randMotionY, randMotionZ, attractionPoint));
                    }
                }

                if (fireAge >= 1 && fireAge <= 12 && fireAge % 3 == 0) {
                    Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleBFGCoreLightning(world, pos.getX() + 0.5, pos.getY() + 24, pos.getZ() + 0.5 - 61));
                }
                if (fireAge >= 28 && fireAge <= 32 && fireAge % 2 == 0) {
                    Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleBFGCoreLightning(world, pos.getX() + 0.5, pos.getY() + 24, pos.getZ() + 0.5 - 61));
                }
                if (fireAge > 32 && fireAge <= 52) {
                    Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleBFGCoreLightning(world, pos.getX() + 0.5, pos.getY() + 24, pos.getZ() + 0.5 - 61));
                }

                if (fireAge == 10) {
                    Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleBFGPrefire(world, pos.getX() + 0.5, pos.getY() + 24, pos.getZ() + 0.5 - 21));
                }

                if (fireAge == 58) {
                    Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleBFGBeam(world, pos.getX() + 0.5, pos.getY() + 24, pos.getZ() + 0.5 - 25));
                }
                if (fireAge >= 58 && fireAge <= 70) {
                    for (int i = 0; i < 20; i++) {
                        float randPosX = BobMathUtil.remap(world.rand.nextFloat(), 0, 1, -5, 5);
                        float randPosY = BobMathUtil.remap(world.rand.nextFloat(), 0, 1, -5, 5);
                        float randPosZ = BobMathUtil.remap(world.rand.nextFloat(), 0, 1, 0, -200);
                        float randMotionX = world.rand.nextFloat() * 0.4F - 0.2F;
                        float randMotionY = world.rand.nextFloat() * 0.4F - 0.2F;
                        float randMotionZ = world.rand.nextFloat() - 5.4F - 4F;
                        Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleBFGParticle(world, pos.getX() + 0.5 + randPosX, pos.getY() + 24 + randPosY, pos.getZ() + 0.5 - 44 + +randPosZ, randMotionX, randMotionY, randMotionZ, null));
                    }
                }
                if (fireAge == 58 || fireAge == 64) {
                    Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleBFGSmoke(world, pos.getX() + 0.5, pos.getY() + 23, pos.getZ() + 0.5 - 55));
                }
                if (fireAge == 58 || fireAge == 68 || fireAge == 83) {
                    Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleBFGRing(world, pos.getX() + 0.5, pos.getY() + 25, pos.getZ() + 0.5 - 55));
                }
                if (fireAge == 60) {
                    Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleBFGShockwave(world, pos.getX() + 0.5, pos.getY() + 25, pos.getZ() + 0.5 - 55, 2, 30, 1, 0.95F));
                }
                if (fireAge == 65) {
                    Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleBFGShockwave(world, pos.getX() + 0.5, pos.getY() + 25, pos.getZ() + 0.5 - 65, 5, 25, 0.6F, 0.98F));
                }


            }
        }
    }

    //Drillgon200: Sending whole tag compounds to spawn particles can't be efficient...
    @SuppressWarnings("deprecation")
    //mk3, only use this one
    @Override
    public void effectNT(NBTTagCompound data) {
        World world = Minecraft.getMinecraft().world;
        if (world == null)
            return;
        TextureManager man = Minecraft.getMinecraft().renderEngine;
        EntityPlayer player = Minecraft.getMinecraft().player;
        int particleSetting = Minecraft.getMinecraft().gameSettings.particleSetting;
        Random rand = world.rand;
        String type = data.getString("type");
        double x = data.getDouble("posX");
        double y = data.getDouble("posY");
        double z = data.getDouble("posZ");

        if (ParticleCreators.particleCreators.containsKey(type)) {
            ParticleCreators.particleCreators.get(type).makeParticle(world, player,
                    Minecraft.getMinecraft().renderEngine, rand, x, y, z, data);
            return;
        }
        switch (type) {
            // Old MK1 system ported to MK3:
            case "waterSplash" -> {
                for (int i = 0; i < 10; i++) {
                    EntityCloudFX smoke = new EntityCloudFX(world, x + world.rand.nextGaussian(), y + world.rand.nextGaussian(), z + world.rand.nextGaussian(), 0.0, 0.0, 0.0);
                    Minecraft.getMinecraft().effectRenderer.addEffect(smoke);
                }
            }
            case "cloudFX2" -> { // i have genuinely no idea what used this
                EntityCloudFX smoke = new EntityCloudFX(world, x, y, z, 0.0, 0.1, 0.0);
                Minecraft.getMinecraft().effectRenderer.addEffect(smoke);
            }
            case "ABMContrail" -> {
                ParticleContrail contrail = new ParticleContrail(man, world, x, y, z);
                Minecraft.getMinecraft().effectRenderer.addEffect(contrail);
            }
            // End MK1 porting.

            // Old MK2 system ported to MK3:
            case "launchSmoke" -> {
                ParticleSmokePlume contrail = new ParticleSmokePlume(man, world, x, y, z);
                contrail.motionX = data.getDouble("moX");
                contrail.motionY = data.getDouble("moY");
                contrail.motionZ = data.getDouble("moZ");
                Minecraft.getMinecraft().effectRenderer.addEffect(contrail);
            }
            case "exKerosene" -> {
                ParticleContrail contrail = new ParticleContrail(man, world, x, y, z, 0F, 0F, 0F, 1F);
                Minecraft.getMinecraft().effectRenderer.addEffect(contrail);
            }
            case "exSolid" -> {
                ParticleContrail contrail = new ParticleContrail(man, world, x, y, z, 0.3F, 0.2F, 0.05F, 1F);
                Minecraft.getMinecraft().effectRenderer.addEffect(contrail);
            }
            case "exHydrogen" -> {
                ParticleContrail contrail = new ParticleContrail(man, world, x, y, z, 0.7F, 0.7F, 0.7F, 1F);
                Minecraft.getMinecraft().effectRenderer.addEffect(contrail);
            }
            case "exBalefire" -> {
                ParticleContrail contrail = new ParticleContrail(man, world, x, y, z, 0.2F, 0.7F, 0.2F, 1F);
                Minecraft.getMinecraft().effectRenderer.addEffect(contrail);
            }
            case "radFog", "radiationfog" -> {
                ParticleRadiationFog contrail = new ParticleRadiationFog(world, x, y, z);
                Minecraft.getMinecraft().effectRenderer.addEffect(contrail);
            }

            // End MK2 porting.

            case "missileContrail" -> {
                if (new Vec3d(player.posX - x, player.posY - y, player.posZ - z).length() > 350) return;

                float scale = data.hasKey("scale") ? data.getFloat("scale") : 1F;
                double mX = data.getDouble("moX");
                double mY = data.getDouble("moY");
                double mZ = data.getDouble("moZ");

                /*ParticleContrail contrail = new ParticleContrail(man, world, x, y, z, 0, 0, 0, scale);
                contrail.motionX = mX;
                contrail.motionY = mY;
                contrail.motionZ = mZ;
                Minecraft.getMinecraft().effectRenderer.addEffect(contrail);*/

                ParticleRocketFlame fx = new ParticleRocketFlame(world, x, y, z).setScale(scale);
                fx.setMotion(mX, mY, mZ);
                if (data.hasKey("maxAge")) fx.setMaxAge(data.getInteger("maxAge"));
                Minecraft.getMinecraft().effectRenderer.addEffect(fx);
            }
            case "smoke" -> {
                String mode = data.getString("mode");
                int count = Math.max(1, data.getInteger("count"));

                switch (mode) {
                    case "cloud" -> {
                        for (int i = 0; i < count; i++) {
                            if (GeneralConfig.instancedParticles) {
                                ParticleExSmokeInstanced fx = new ParticleExSmokeInstanced(world, x, y, z);
                                double motionY = rand.nextGaussian() * (1 + (count / 100D));
                                double motionX = rand.nextGaussian() * (1 + (count / 150D));
                                double motionZ = rand.nextGaussian() * (1 + (count / 150D));
                                if (rand.nextBoolean()) motionY = Math.abs(motionY);
                                fx.setMotion(motionX, motionY, motionZ);
                                InstancedParticleRenderer.addParticle(fx);
                            } else {
                                ParticleExSmoke fx = new ParticleExSmoke(world, x, y, z);
                                double motionY = rand.nextGaussian() * (1 + (count / 100D));
                                double motionX = rand.nextGaussian() * (1 + (count / 150D));
                                double motionZ = rand.nextGaussian() * (1 + (count / 150D));
                                if (rand.nextBoolean()) motionY = Math.abs(motionY);
                                fx.setMotion(motionX, motionY, motionZ);
                                Minecraft.getMinecraft().effectRenderer.addEffect(fx);
                            }
                        }
                    }
                    case "radial" -> {
                        for (int i = 0; i < count; i++) {
                            if (GeneralConfig.instancedParticles) {
                                ParticleExSmokeInstanced fx = new ParticleExSmokeInstanced(world, x, y, z);
                                fx.setMotion(rand.nextGaussian() * (1 + (count / 50D)),
                                        rand.nextGaussian() * (1 + (count / 50D)), rand.nextGaussian() * (1 + (count / 50D)));
                                InstancedParticleRenderer.addParticle(fx);
                            } else {
                                ParticleExSmoke fx = new ParticleExSmoke(world, x, y, z);
                                fx.setMotion(rand.nextGaussian() * (1 + (count / 50D)),
                                        rand.nextGaussian() * (1 + (count / 50D)), rand.nextGaussian() * (1 + (count / 50D)));
                                Minecraft.getMinecraft().effectRenderer.addEffect(fx);
                            }
                        }
                    }
                    case "radialDigamma" -> {
                        MutableVec3d vec = new MutableVec3d(2, 0, 0);
                        vec.rotateYawSelf(rand.nextFloat() * (float) Math.PI * 2F);

                        for (int i = 0; i < count; i++) {
                            ParticleDigammaSmoke fx = new ParticleDigammaSmoke(world, x, y, z);
                            fx.motion((float) vec.x, 0, (float) vec.z);
                            Minecraft.getMinecraft().effectRenderer.addEffect(fx);

                            vec.rotateYawSelf((float) Math.PI * 2F / (float) count);
                        }
                    }
                    case "shock" -> {
                        double strength = data.getDouble("strength");

                        MutableVec3d vec = new MutableVec3d(strength, 0, 0);
                        vec.rotateYawSelf(rand.nextInt(360));

                        for (int i = 0; i < count; i++) {
                            if (GeneralConfig.instancedParticles) {
                                ParticleExSmokeInstanced fx = new ParticleExSmokeInstanced(world, x, y, z);
                                fx.setMotion(vec.x, 0, vec.z);
                                InstancedParticleRenderer.addParticle(fx);
                            } else {
                                ParticleExSmoke fx = new ParticleExSmoke(world, x, y, z);
                                fx.setMotion(vec.x, 0, vec.z);
                                Minecraft.getMinecraft().effectRenderer.addEffect(fx);
                            }

                            vec.rotateYawSelf((float) Math.PI * 2F / count);
                        }
                    }
                    case "shockRand" -> {
                        double strength = data.getDouble("strength");

                        MutableVec3d vec = new MutableVec3d(strength, 0, 0);
                        vec.rotateYawSelf(rand.nextInt(360));
                        double r;

                        for (int i = 0; i < count; i++) {
                            r = rand.nextDouble();
                            if (GeneralConfig.instancedParticles) {
                                ParticleExSmokeInstanced fx = new ParticleExSmokeInstanced(world, x, y, z);
                                fx.setMotion(vec.x * r, 0, vec.z * r);
                                InstancedParticleRenderer.addParticle(fx);
                            } else {
                                ParticleExSmoke fx = new ParticleExSmoke(world, x, y, z);
                                fx.setMotion(vec.x * r, 0, vec.z * r);
                                Minecraft.getMinecraft().effectRenderer.addEffect(fx);
                            }

                            vec.rotateYawSelf(360F / count);
                        }
                    }
                    case "wave" -> {
                        double strength = data.getDouble("range");

                        MutableVec3d vec = new MutableVec3d(strength, 0, 0);

                        for (int i = 0; i < count; i++) {

                            vec.rotateYawSelf((float) Math.toRadians(rand.nextFloat() * 360F));

                            if (GeneralConfig.instancedParticles) {
                                ParticleExSmokeInstanced fx = new ParticleExSmokeInstanced(world, x + vec.x, y,
                                        z + vec.z);
                                fx.setMotion(0, 0, 0);
                                fx.setMaxAge(50);
                                InstancedParticleRenderer.addParticle(fx);
                            } else {
                                ParticleExSmoke fx = new ParticleExSmoke(world, x + vec.x, y, z + vec.z);
                                fx.setMotion(0, 0, 0);
                                fx.setMaxAge(50);
                                Minecraft.getMinecraft().effectRenderer.addEffect(fx);
                            }

                            vec.rotateYawSelf(360F / count);
                        }
                    }
                    case "foamSplash" -> {
                        double strength = data.getDouble("range");

                        MutableVec3d vec = new MutableVec3d(strength, 0, 0);

                        for (int i = 0; i < count; i++) {

                            vec.rotateYawSelf((float) Math.toRadians(rand.nextFloat() * 360F));
                            // TODO
                            /*ParticleFoam fx = new ParticleFoam(man, world, x + vec.xCoord, y, z + vec.zCoord);
                            fx.maxAge = 50;
                            fx.motionY = 0;
                            fx.motionX = 0;
                            fx.motionZ = 0;
                            Minecraft.getMinecraft().effectRenderer.addEffect(fx);

                            vec.rotateYawSelf(360 / count);*/
                        }
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + mode);
                }
            }
            case "debugdrone" -> {
                Item held = player.getHeldItem(EnumHand.MAIN_HAND).isEmpty() ? null : player.getHeldItem(EnumHand.MAIN_HAND).getItem();

                if (held == ModItems.drone ||
                        held == Item.getItemFromBlock(ModBlocks.drone_crate_provider) ||
                        held == Item.getItemFromBlock(ModBlocks.drone_crate_requester) ||
                        held == Item.getItemFromBlock(ModBlocks.drone_dock) ||
                        held == Item.getItemFromBlock(ModBlocks.drone_waypoint_request) ||
                        held == Item.getItemFromBlock(ModBlocks.drone_waypoint) ||
                        held == Item.getItemFromBlock(ModBlocks.drone_crate) ||
                        held == ModItems.drone_linker) {
                    double mX = data.getDouble("mX");
                    double mY = data.getDouble("mY");
                    double mZ = data.getDouble("mZ");
                    int color = data.getInteger("color");
                    ParticleDebugLine text = new ParticleDebugLine(world, x, y, z, mX, mY, mZ, color);
                    Minecraft.getMinecraft().effectRenderer.addEffect(text);
                }
            }
            case "network" -> {
                double mX = data.getDouble("mX");
                double mY = data.getDouble("mY");
                double mZ = data.getDouble("mZ");

                ParticleDebug debug = switch (data.getString("mode")) {
                    case "power" -> new ParticleDebug(world, x, y, z);
                    case "fluid" -> {
                        int color = data.getInteger("color");
                        yield new ParticleDebug(world, x, y, z, mX, mY, mZ, color);
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + data.getString("mode"));
                };
                Minecraft.getMinecraft().effectRenderer.addEffect(debug);
            }
            case "exhaust" -> {
                String mode = data.getString("mode");

                switch (mode) {
                    case "soyuz" -> {
                        if (new Vec3d(player.posX - x, player.posY - y, player.posZ - z).length() > 350)
                            return;

                        int count = Math.max(1, data.getInteger("count"));
                        double width = data.getDouble("width");

                        for (int i = 0; i < count; i++) {
                            if (GeneralConfig.instancedParticles) {
                                ParticleRocketFlameInstanced fx = new ParticleRocketFlameInstanced(world,
                                        x + rand.nextGaussian() * width, y, z + rand.nextGaussian() * width);
                                fx.setMotionY(-0.75 + rand.nextDouble() * 0.5);
                                InstancedParticleRenderer.addParticle(fx);
                            } else {
                                ParticleRocketFlame fx = new ParticleRocketFlame(world,
                                        x + rand.nextGaussian() * width, y, z + rand.nextGaussian() * width);
                                fx.setMotionY(-0.75 + rand.nextDouble() * 0.5);
                                Minecraft.getMinecraft().effectRenderer.addEffect(fx);
                            }
                        }
                    }
                    case "meteor" -> {
                        if (new Vec3d(player.posX - x, player.posY - y, player.posZ - z).length() > 350)
                            return;

                        int count = Math.max(1, data.getInteger("count"));
                        double width = data.getDouble("width");

                        for (int i = 0; i < count; i++) {
                            if (GeneralConfig.instancedParticles) {
                                ParticleRocketFlameInstanced fx = new ParticleRocketFlameInstanced(world,
                                        x + rand.nextGaussian() * width, y + rand.nextGaussian() * width,
                                        z + rand.nextGaussian() * width);
                                InstancedParticleRenderer.addParticle(fx);
                            } else {
                                ParticleRocketFlame fx = new ParticleRocketFlame(world,
                                        x + rand.nextGaussian() * width, y + rand.nextGaussian() * width,
                                        z + rand.nextGaussian() * width);
                                Minecraft.getMinecraft().effectRenderer.addEffect(fx);
                            }
                        }
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + mode);
                }
            }
            case "muke" -> { // mini nuke, without sound
                ParticleMukeWave wave = new ParticleMukeWave(world, x, y, z);
                ParticleMukeFlash flash = new ParticleMukeFlash(world, x, y, z, data.getBoolean("balefire"));

                Minecraft.getMinecraft().effectRenderer.addEffect(wave);
                Minecraft.getMinecraft().effectRenderer.addEffect(flash);

                player.hurtTime = 15;
                player.maxHurtTime = 15;
                player.attackedAtYaw = 0F;
            }
            case "ufo" -> {
                if (GeneralConfig.instancedParticles) {
                    ParticleRocketFlameInstanced fx = new ParticleRocketFlameInstanced(world, x, y, z);
                    InstancedParticleRenderer.addParticle(fx);
                } else {
                    ParticleRocketFlame fx = new ParticleRocketFlame(world, x, y, z);
                    Minecraft.getMinecraft().effectRenderer.addEffect(fx);
                }
            }
            case "haze" -> {
                ParticleHaze fog = new ParticleHaze(world, x, y, z);
                Minecraft.getMinecraft().effectRenderer.addEffect(fog);
            }
            case "plasmablast" -> {
                ParticlePlasmaBlast cloud = new ParticlePlasmaBlast(world, x, y, z, data.getFloat("r"),
                        data.getFloat("g"), data.getFloat("b"), data.getFloat("pitch"), data.getFloat("yaw"));
                cloud.setScale(data.getFloat("scale"));
                Minecraft.getMinecraft().effectRenderer.addEffect(cloud);
            }
            case "justTilt" -> {
                player.hurtTime = player.maxHurtTime = data.getInteger("time");
                player.attackedAtYaw = 0F;
            }
            case "properJolt" -> {
                player.hurtTime = data.getInteger("time");
                player.maxHurtTime = data.getInteger("maxTime");
                player.attackedAtYaw = 0F;
            }
            case "gasfire" -> {
                double mX = data.getDouble("mX");
                double mY = data.getDouble("mY");
                double mZ = data.getDouble("mZ");
                float scale = data.getFloat("scale");
                ParticleGasFlame text = new ParticleGasFlame(world, x, y, z, mX, mY, mZ, scale > 0 ? scale : 6.5F);
                Minecraft.getMinecraft().effectRenderer.addEffect(text);
            }
            case "marker" -> {
                int color = data.getInteger("color");
                String label = data.getString("label");
                int expires = data.getInteger("expires");
                double dist = data.getDouble("dist");

                RenderOverhead.queuedMarkers.put(new BlockPos(x, y, z),
                        new RenderOverhead.Marker(color).setDist(dist).setExpire(expires > 0 ?
                                System.currentTimeMillis() + expires : 0).withLabel(label.isEmpty() ? null : label));
            }
            case "casing" -> {
                CasingEjector ejector = CasingEjector.fromId(data.getInteger("ej"));
                if (ejector == null) return;
                SpentCasing casingConfig = SpentCasing.fromName((data.getString("name")));
                if (casingConfig == null) return;

                for (int i = 0; i < ejector.getAmount(); i++) {
                    ejector.spawnCasing(Minecraft.getMinecraft().renderEngine, casingConfig, world, x, y, z,
                            data.getFloat("pitch"), data.getFloat("yaw"), data.getBoolean("crouched"));
                }
            }
            case "foundry" -> {
                int color = data.getInteger("color");
                byte dir = data.getByte("dir");
                float length = data.getFloat("len");
                float base = data.getFloat("base");
                float offset = data.getFloat("off");

                ParticleFoundry sploosh = new ParticleFoundry(world, x, y, z, color, dir, length, base, offset);
                Minecraft.getMinecraft().effectRenderer.addEffect(sploosh);
            }
            case "fireworks" -> {
                int color = data.getInteger("color");
                char c = (char) data.getInteger("char");

                ParticleLetter fx = new ParticleLetter(world, x, y, z, color, c);
                Minecraft.getMinecraft().effectRenderer.addEffect(fx);

                for (int i = 0; i < 50; i++) {
                    Spark blast = new Spark(world, x, y, z,
                            0.4 * world.rand.nextGaussian(),
                            0.4 * world.rand.nextGaussian(),
                            0.4 * world.rand.nextGaussian(), Minecraft.getMinecraft().effectRenderer);
                    blast.setColor(color);
                    Minecraft.getMinecraft().effectRenderer.addEffect(blast);
                }
            }
            case "vomit" -> {
                Entity e = world.getEntityByID(data.getInteger("entity"));
                int count = data.getInteger("count");
                if (e instanceof EntityLivingBase) {

                    double ix = e.posX;
                    double iy = e.posY - e.getYOffset() + e.getEyeHeight() + (e instanceof EntityPlayer ? -0.5 : 0);
                    double iz = e.posZ;

                    Vec3d vec = e.getLookVec();

                    String mode = data.getString("mode");
                    for (int i = 0; i < count; i++) {
                        switch (mode) {
                            case "normal" -> {
                                int stateId =
                                        Block.getStateId(Blocks.STAINED_HARDENED_CLAY.getDefaultState().withProperty(BlockStainedHardenedClay.COLOR
                                                , rand.nextBoolean() ? EnumDyeColor.LIME : EnumDyeColor.GREEN));
                                Particle fx = new ParticleBlockDust.Factory().createParticle(-1, world, ix, iy, iz,
                                        (vec.x + rand.nextGaussian() * 0.2) * 0.2, (vec.y + rand.nextGaussian() * 0.2) * 0.2
                                        , (vec.z + rand.nextGaussian() * 0.2) * 0.2, stateId);
                                fx.setMaxAge(150 + rand.nextInt(50));
                                Minecraft.getMinecraft().effectRenderer.addEffect(fx);
                            }
                            case "blood" -> {
                                Particle fx = new ParticleBlockDust.Factory().createParticle(-1, world, ix, iy, iz,
                                        (vec.x + rand.nextGaussian() * 0.2) * 0.2, (vec.y + rand.nextGaussian() * 0.2) * 0.2
                                        , (vec.z + rand.nextGaussian() * 0.2) * 0.2,
                                        Block.getStateId(Blocks.REDSTONE_BLOCK.getDefaultState()));
                                fx.setMaxAge(150 + rand.nextInt(50));
                                Minecraft.getMinecraft().effectRenderer.addEffect(fx);
                            }
                            case "smoke" -> {
                                Particle fx = new ParticleSmokeNormal.Factory().createParticle(-1, world, ix, iy, iz,
                                        (vec.x + rand.nextGaussian() * 0.1) * 0.05,
                                        (vec.y + rand.nextGaussian() * 0.1) * 0.05,
                                        (vec.z + rand.nextGaussian() * 0.1) * 0.05);
                                fx.setMaxAge(10 + rand.nextInt(10));
                                fx.particleScale *= 0.2F;
                                ((ParticleSmokeNormal) fx).smokeParticleScale = fx.particleScale;
                                Minecraft.getMinecraft().effectRenderer.addEffect(fx);
                            }
                            default -> throw new IllegalStateException("Unexpected value: " + mode);
                        }
                    }
                }
            }
            case "sweat" -> {
                Entity e = world.getEntityByID(data.getInteger("entity"));
                Block b = Block.getBlockById(data.getInteger("block"));
                int meta = data.getInteger("meta");

                if (e instanceof EntityLivingBase) {

                    for (int i = 0; i < data.getInteger("count"); i++) {

                        double ix =
                                e.getEntityBoundingBox().minX - 0.2 + (e.getEntityBoundingBox().maxX - e.getEntityBoundingBox().minX + 0.4) * rand.nextDouble();
                        double iy =
                                e.getEntityBoundingBox().minY + (e.getEntityBoundingBox().maxY - e.getEntityBoundingBox().minY + 0.2) * rand.nextDouble();
                        double iz =
                                e.getEntityBoundingBox().minZ - 0.2 + (e.getEntityBoundingBox().maxZ - e.getEntityBoundingBox().minZ + 0.4) * rand.nextDouble();


                        Particle fx = new ParticleBlockDust.Factory().createParticle(-1, world, ix, iy, iz, 0, 0, 0,
                                Block.getStateId(b.getStateFromMeta(meta)));
                        fx.setMaxAge(150 + rand.nextInt(50));

                        Minecraft.getMinecraft().effectRenderer.addEffect(fx);
                    }
                }
            }
            case "splash" -> {
                if (particleSetting == 0 || (particleSetting == 1 && rand.nextBoolean())) {
                    ParticleLiquidSplash fx = new ParticleLiquidSplash(world, x, y, z);

                    if (data.hasKey("color")) {
                        Color color = new Color(data.getInteger("color"));
                        float f = 1F - rand.nextFloat() * 0.2F;
                        fx.setRBGColorF(color.getRed() / 255F * f, color.getGreen() / 255F * f, color.getBlue() / 255F * f);
                    }

                    Minecraft.getMinecraft().effectRenderer.addEffect(fx);
                }
            }
            case "fluidfill" -> {
                double mX = data.getDouble("mX");
                double mY = data.getDouble("mY");
                double mZ = data.getDouble("mZ");

                Particle fx = new ParticleCrit.Factory().createParticle(0, world, x, y, z, mX, mY, mZ);
                fx.nextTextureIndexX();

                if (data.hasKey("color")) {
                    Color color = new Color(data.getInteger("color"));
                    fx.setRBGColorF(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F);
                }

                Minecraft.getMinecraft().effectRenderer.addEffect(fx);
            }
            case "radiation" -> {
                for (int i = 0; i < data.getInteger("count"); i++) {

                    Particle flash = new ParticleSuspendedTown.Factory().createParticle(-1, world,
                            player.posX + rand.nextGaussian() * 4,
                            player.posY + rand.nextGaussian() * 2,
                            player.posZ + rand.nextGaussian() * 4,
                            0, 0, 0);

                    flash.setRBGColorF(0F, 0.75F, 1F);
                    flash.motionX = rand.nextGaussian();
                    flash.motionY = rand.nextGaussian();
                    flash.motionZ = rand.nextGaussian();
                    Minecraft.getMinecraft().effectRenderer.addEffect(flash);
                }
            }
            case "vanillaburst" -> {
                double motion = data.getDouble("motion");

                for (int i = 0; i < data.getInteger("count"); i++) {

                    double mX = rand.nextGaussian() * motion;
                    double mY = rand.nextGaussian() * motion;
                    double mZ = rand.nextGaussian() * motion;

                    String mode = data.getString("mode");
                    Particle fx = switch (mode) {
                        case "flame" -> new ParticleFlame.Factory().createParticle(-1, world, x, y, z, mX, mY, mZ);
                        case "cloud" -> new ParticleCloud.Factory().createParticle(-1, world, x, y, z, mX, mY, mZ);
                        case "reddust" -> new ParticleRedstone.Factory().createParticle(-1, world, x, y, z, 0.0F, 0.0F,
                                0.0F);
                        case "bluedust" ->
                                new ParticleRedstone.Factory().createParticle(-1, world, x, y, z, 0.01F, 0.01F,
                                        1F);
                        case "greendust" ->
                                new ParticleRedstone.Factory().createParticle(-1, world, x, y, z, 0.01F, 0.5F,
                                        0.1F);
                        case "blockdust" -> {
                            Block b = Block.getBlockById(data.getInteger("block"));
                            Particle particle = new ParticleBlockDust.Factory().createParticle(-1, world, x, y, z, mX, mY + 0.2, mZ,
                                    Block.getStateId(b.getDefaultState()));
                            particle.setMaxAge(50 + rand.nextInt(50));
                            yield particle;
                        }
                        default -> throw new IllegalStateException("Unexpected value: " + mode);
                    };

                    Minecraft.getMinecraft().effectRenderer.addEffect(fx);
                }
            }
            case "vanillaExt" -> {

                double mX = data.getDouble("mX");
                double mY = data.getDouble("mY");
                double mZ = data.getDouble("mZ");

                String mode = data.getString("mode");
                Particle fx = switch (mode) {
                    case "flame" -> new ParticleFlame.Factory().createParticle(-1, world, x, y, z, mX, mY, mZ);
                    case "smoke" -> new ParticleSmokeNormal.Factory().createParticle(-1, world, x, y, z, mX, mY, mZ);
                    case "volcano" -> {
                        Particle particle = new ParticleSmokeNormal.Factory().createParticle(-1, world, x, y, z, mX, mY, mZ);
                        ((ParticleSmokeNormal) particle).smokeParticleScale = 100f;
                        particle.setMaxAge(200 + rand.nextInt(50));
                        particle.canCollide = false;
                        particle.motionX = rand.nextGaussian() * 0.2;
                        particle.motionY = 2.5 + rand.nextDouble();
                        particle.motionZ = rand.nextGaussian() * 0.2;
                        yield particle;
                    }
                    case "cloud" -> new ParticleCloud.Factory().createParticle(-1, world, x, y, z, mX, mY, mZ);
                    case "reddust" -> new ParticleRedstone.Factory().createParticle(-1, world, x, y, z, (float) mX,
                            (float) mY, (float) mZ);
                    case "bluedust" ->
                            new ParticleRedstone.Factory().createParticle(-1, world, x, y, z, 0.01F, 0.01F, 1F);
                    case "greendust" ->
                            new ParticleRedstone.Factory().createParticle(-1, world, x, y, z, 0.01F, 0.5F, 0.1F);
                    case "fireworks" -> {
                        world.spawnParticle(EnumParticleTypes.FIREWORKS_SPARK, x, y, z, 0, 0, 0);
                        yield null;
                    }
                    case "largeexplode" -> {
                        Particle particle = new ParticleExplosionLarge.Factory().createParticle(-1, world, x, y, z, data.getFloat(
                                "size"), 0.0F, 0.0F);
                        float r = 1.0F - rand.nextFloat() * 0.2F;
                        particle.setRBGColorF(r, 0.9F * r, 0.5F * r);

                        for (int i = 0; i < data.getByte("count"); i++) {
                            ParticleExplosion sec =
                                    (ParticleExplosion) new ParticleExplosion.Factory().createParticle(-1, world, x, y, z,
                                            0.0F, 0.0F, 0.0F);
                            float r2 = 1.0F - rand.nextFloat() * 0.5F;
                            sec.setRBGColorF(0.5F * r2, 0.5F * r2, 0.5F * r2);
                            sec.multipleParticleScaleBy(i + 1);
                            Minecraft.getMinecraft().effectRenderer.addEffect(sec);
                        }
                        yield particle;
                    }
                    case "townaura" -> {
                        Particle particle = new ParticleSuspendedTown.Factory().createParticle(-1, world, x, y, z, 0, 0, 0);
                        float color = 0.5F + rand.nextFloat() * 0.5F;
                        particle.setRBGColorF(0.8F * color, 0.9F * color, color);
                        particle.motionX = mX;
                        particle.motionY = mY;
                        particle.motionZ = mZ;
                        yield particle;
                    }
                    case "blockdust" -> {
                        Block b = Block.getBlockById(data.getInteger("block"));
                        int id = Block.getStateId(b.getDefaultState());
                        Particle particle = new ParticleBlockDust.Factory().createParticle(-1, world, x, y, z, mX, mY + 0.2, mZ, id);
                        particle.setMaxAge(10 + rand.nextInt(20));
                        yield particle;
                    }
                    case "colordust" -> {
                        int id = Block.getStateId(Blocks.WOOL.getDefaultState());
                        Particle particle = new ParticleBlockDust.Factory().createParticle(-1, world, x, y, z, mX, mY + 0.2, mZ, id);
                        particle.setRBGColorF(data.getFloat("r"), data.getFloat("g"), data.getFloat("b"));
                        particle.setMaxAge(10 + rand.nextInt(20));
                        yield particle;
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + mode);
                };
                if (fx != null) {
                    fx.canCollide = !data.getBoolean("noclip");

                    if (data.getInteger("overrideAge") > 0) {
                        fx.setMaxAge(data.getInteger("overrideAge"));
                    }

                    Minecraft.getMinecraft().effectRenderer.addEffect(fx);
                }
            }
            case "spark" -> {
                String mode = data.getString("mode");
                double dirX = data.getDouble("dirX");
                double dirY = data.getDouble("dirY");
                double dirZ = data.getDouble("dirZ");
                float width = data.hasKey("width") ? data.getFloat("width") : 0.025F;
                float length = data.hasKey("length") ? data.getFloat("length") : 1.0F;
                float randLength = data.hasKey("randLength") ? data.getFloat("randLength") - length : 0;
                float gravity = data.hasKey("gravity") ? data.getFloat("gravity") : 9.81F * 0.01F;
                int lifetime = data.hasKey("lifetime") ? data.getInteger("lifetime") : 100;
                int randLifeTime = data.hasKey("randLifetime") ? data.getInteger("randLifetime") - lifetime : lifetime;
                float velocityRand = data.hasKey("randomVelocity") ? data.getFloat("randomVelocity") : 1.0F;
                float r = data.hasKey("r") ? data.getFloat("r") : 1.0F;
                float g = data.hasKey("g") ? data.getFloat("g") : 1.0F;
                float b = data.hasKey("b") ? data.getFloat("b") : 1.0F;
                float a = data.hasKey("a") ? data.getFloat("a") : 1.0F;
                if (mode.equals("coneBurst")) {
                    float angle = data.hasKey("angle") ? data.getFloat("angle") : 10;
                    float randAngle = data.hasKey("randAngle") ? data.getFloat("randAngle") - angle : 0;
                    int count = data.hasKey("count") ? data.getInteger("count") : 1;
                    for (int i = 0; i < count; i++) {
                        //Gets a random vector rotated within a cone and then rotates it to the particle data's direction
                        //Create a new vector and rotate it randomly about the x-axis within the angle specified, then rotate that by random degrees to get the random cone vector
                        Vec3d up = new Vec3d(0, 1, 0);
                        up = up.rotatePitch((float) Math.toRadians(rand.nextFloat() * (angle + rand.nextFloat() * randAngle)));
                        up = up.rotateYaw((float) Math.toRadians(rand.nextFloat() * 360));
                        //Finds the angles for the particle direction and rotate our random cone vector to it.
                        Vec3d direction = new Vec3d(dirX, dirY, dirZ);
                        Vec3d angles = BobMathUtil.getEulerAngles(direction);
                        Vec3d newDirection = new Vec3d(up.x, up.y, up.z);
                        newDirection = newDirection.rotatePitch((float) Math.toRadians(angles.y - 90));
                        newDirection = newDirection.rotateYaw((float) Math.toRadians(angles.x));
                        //Multiply it by the original vector's length to ensure it has the right magnitude
                        newDirection = newDirection.scale((float) direction.length() + rand.nextFloat() * velocityRand);
                        Particle fx = new ParticleSpark(world, x, y, z, length + rand.nextFloat() * randLength, width
                                , lifetime + rand.nextInt(randLifeTime), gravity).color(r, g, b, a).motion((float) newDirection.x, (float) newDirection.y, (float) newDirection.z);
                        Minecraft.getMinecraft().effectRenderer.addEffect(fx);
                    }
                }
            }
            case "hadron" -> Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleHadron(world, x, y, z));
            case "schrabfog" -> {
                ParticleSuspendedTown flash =
                        (ParticleSuspendedTown) new ParticleSuspendedTown.Factory().createParticle(-1, world, x, y, z, 0, 0,
                                0);
                flash.setRBGColorF(0F, 1F, 1F);
                Minecraft.getMinecraft().effectRenderer.addEffect(flash);
            }
            case "rift" -> Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleRift(world, x, y, z));
            case "rbmkflame" -> {
                int maxAge = data.getInteger("maxAge");
                Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleRBMKFlame(world, x, y, z, maxAge));
            }
            case "rbmkmush" -> {
                float scale = data.getFloat("scale");
                Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleRBMKMush(world, x, y, z, scale));
            }
            case "tower" -> {
                if (particleSetting == 0 || (particleSetting == 1 && rand.nextBoolean())) {
                    ParticleCoolingTower fx = new ParticleCoolingTower(world, x, y, z);
                    fx.setLift(data.getFloat("lift"));
                    fx.setBaseScale(data.getFloat("base"));
                    fx.setMaxScale(data.getFloat("max"));
                    fx.setLife(data.getInteger("life") / (particleSetting + 1));
                    if (data.hasKey("noWind")) fx.noWind();
                    if (data.hasKey("strafe")) fx.setStrafe(data.getFloat("strafe"));
                    if (data.hasKey("alpha")) fx.alphaMod(data.getFloat("alpha"));

                    if (data.hasKey("color")) {
                        Color color = new Color(data.getInteger("color"));
                        fx.setRBGColorF(color.getRed() / 255F, color.getGreen() / 255F, color.getBlue() / 255F);
                    }

                    Minecraft.getMinecraft().effectRenderer.addEffect(fx);
                }
            }
            case "jetpack" -> {
                Entity ent = world.getEntityByID(data.getInteger("player"));

                if (ent instanceof EntityPlayer p) {

                    Vec3d vec = new Vec3d(0, 0, -0.25);
                    Vec3d offset = new Vec3d(0.125, 0, 0);
                    float angle = (float) -Math.toRadians(p.rotationYawHead - (p.rotationYawHead - p.renderYawOffset));

                    vec = vec.rotateYaw(angle);
                    offset = offset.rotateYaw(angle);

                    double ix = p.posX + vec.x;
                    double iy = p.posY + p.eyeHeight - 1;
                    double iz = p.posZ + vec.z;
                    double ox = offset.x;
                    double oz = offset.z;

                    double moX = 0;
                    double moY = 0;
                    double moZ = 0;

                    int mode = data.getInteger("mode");

                    if (mode == 0) {
                        moY -= 0.2;
                    }

                    if (mode == 1) {
                        Vec3d look = p.getLookVec();

                        moX -= look.x * 0.1D;
                        moY -= look.y * 0.1D;
                        moZ -= look.z * 0.1D;
                    }

                    Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleFlame.Factory().createParticle(-1,
                            world, ix + ox, iy, iz + oz, p.motionX + moX * 2, p.motionY + moY * 2, p.motionZ + moZ * 2));
                    Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleFlame.Factory().createParticle(-1,
                            world, ix - ox, iy, iz - oz, p.motionX + moX * 2, p.motionY + moY * 2, p.motionZ + moZ * 2));
                    Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleSmokeNormal.Factory().createParticle(-1, world, ix + ox, iy, iz + oz, p.motionX + moX * 3, p.motionY + moY * 3, p.motionZ + moZ * 3));
                    Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleSmokeNormal.Factory().createParticle(-1, world, ix - ox, iy,
                            iz - oz, p.motionX + moX * 3, p.motionY + moY * 3, p.motionZ + moZ * 3));
                }
            }

            case "bf" -> {
                ParticleMukeCloud cloud = new ParticleMukeCloudBF(world, x, y, z, 0, 0, 0);
                Minecraft.getMinecraft().effectRenderer.addEffect(cloud);
            }
            case "chlorinefx" -> {
                double mX = data.getDouble("moX");
                double mY = data.getDouble("moY");
                double mZ = data.getDouble("moZ");
                EntityChlorineFX eff = new EntityChlorineFX(world, x, y, z, mX, mY, mZ);
                Minecraft.getMinecraft().effectRenderer.addEffect(eff);
            }
            case "cloudfx" -> {
                double mX = data.getDouble("moX");
                double mY = data.getDouble("moY");
                double mZ = data.getDouble("moZ");
                EntityCloudFX eff = new EntityCloudFX(world, x, y, z, mX, mY, mZ);
                Minecraft.getMinecraft().effectRenderer.addEffect(eff);
            }
            case "orangefx" -> {
                double mX = data.getDouble("moX");
                double mY = data.getDouble("moY");
                double mZ = data.getDouble("moZ");
                EntityOrangeFX eff = new EntityOrangeFX(world, x, y, z, mX, mY, mZ);
                Minecraft.getMinecraft().effectRenderer.addEffect(eff);
            }
            case "pinkcloudfx" -> {
                double mX = data.getDouble("moX");
                double mY = data.getDouble("moY");
                double mZ = data.getDouble("moZ");
                EntityPinkCloudFX eff = new EntityPinkCloudFX(world, x, y, z, mX, mY, mZ);
                Minecraft.getMinecraft().effectRenderer.addEffect(eff);
            }

            case "bimpact" -> {
                Type hitType = Type.values()[data.getByte("hitType")];
                Vec3d normal = new Vec3d(data.getFloat("nX"), data.getFloat("nY"), data.getFloat("nZ"));
                if (hitType == Type.BLOCK) {
                    IBlockState state = Block.getBlockById(data.getInteger("block")).getStateFromMeta(data.getByte(
                            "meta"));
                    Material mat = state.getMaterial();
                    float r = 1;
                    float g = 1;
                    float b = 1;
                    float scale = 1;
                    float randMotion = 0.2F;
                    int count = 10;
                    int smokeCount = 3;
                    int smokeScale = 5;
                    int smokeLife = 15;
                    if (mat == Material.IRON) {
                        world.playSound(x, y, z, HBMSoundHandler.hit_metal, SoundCategory.BLOCKS, 1,
                                0.9F + world.rand.nextFloat() * 0.2F, false);
                    } else {
                        world.playSound(x, y, z, HBMSoundHandler.hit_dirt, SoundCategory.BLOCKS, 1,
                                0.7F + world.rand.nextFloat() * 0.3F, false);
                    }
                    if (mat == Material.ROCK || mat == Material.GROUND || mat == Material.GRASS || mat == Material.WOOD || mat == Material.LEAVES || mat == Material.SAND) {
                        ResourceLocation tex = ResourceManager.rock_fragments;
                        if (mat == Material.WOOD) {
                            tex = ResourceManager.wood_fragments;
                        } else if (mat == Material.LEAVES) {
                            tex = ResourceManager.twigs_and_leaves;
                            smokeLife = 5;
                            smokeScale = 10;
                            smokeCount = 2;
                        }
                        if (mat == Material.GROUND || mat == Material.GRASS) {
                            r = 0.8F;
                            g = 0.5F;
                            b = 0.3F;
                            scale = 0.6F;
                            count = 40;
                        }
                        if (mat == Material.SAND) {
                            r = 1F;
                            g = 0.9F;
                            b = 0.6F;
                            scale = 0.1F;
                            randMotion = 0.5F;
                            count = 100;
                            smokeCount = 5;
                        }
                        for (int i = 0; i < count; i++) {
                            Vec3d dir = BobMathUtil.randVecInCone(normal, 45, world.rand);
                            dir = dir.scale(0.1F + world.rand.nextFloat() * randMotion);
                            Vec3d offset = normal.scale(0.2F);
                            ParticleHitDebris particle = new ParticleHitDebris(world, x + offset.x, y + offset.y,
                                    z + offset.z, tex, world.rand.nextInt(16), scale, 40 + world.rand.nextInt(20));
                            offset.scale(1);
                            particle.motion((float) dir.x, (float) dir.y, (float) dir.z);
                            particle.color(r, g, b);
                            ParticleBatchRenderer.addParticle(particle);
                        }
                        if (mat == Material.WOOD) {
                            r = 0.8F;
                            g = 0.5F;
                            b = 0.3F;
                        }
                        if (mat == Material.LEAVES) {
                            r = 0.2F;
                            g = 0.8F;
                            b = 0.4F;
                        }
                    }
                    if (mat != Material.LEAVES) {
                        ParticleBulletImpact impact = new ParticleBulletImpact(world, x + normal.x * 0.01F,
                                y + normal.y * 0.01F, z + normal.z * 0.01F, 0.1F, 60 + world.rand.nextInt(20), normal);
                        impact.color(r, g, b);
                        ParticleBatchRenderer.addParticle(impact);
                    }
                    if (mat == Material.SAND) {
                        r *= 1.5F;
                        g *= 1.5F;
                        b *= 1.5F;
                    }
                    if (mat == Material.IRON) {
                        NBTTagCompound nbt = new NBTTagCompound();
                        nbt.setString("type", "spark");
                        nbt.setString("mode", "coneBurst");
                        nbt.setDouble("posX", x);
                        nbt.setDouble("posY", y);
                        nbt.setDouble("posZ", z);
                        nbt.setDouble("dirX", normal.x * 0.6F);
                        nbt.setDouble("dirY", normal.y * 0.6F);
                        nbt.setDouble("dirZ", normal.z * 0.6F);
                        nbt.setFloat("r", 0.8F);
                        nbt.setFloat("g", 0.6F);
                        nbt.setFloat("b", 0.5F);
                        nbt.setFloat("a", 1.5F);
                        nbt.setInteger("lifetime", 1 + rand.nextInt(2));
                        nbt.setFloat("width", 0.03F);
                        nbt.setFloat("length", 0.3F);
                        nbt.setFloat("randLength", 0.6F);
                        nbt.setFloat("gravity", 0.1F);
                        nbt.setFloat("angle", 60F);
                        nbt.setInteger("count", 2 + rand.nextInt(2));
                        nbt.setFloat("randomVelocity", 0.3F);
                        effectNT(nbt);
                    } else {
                        for (int i = 0; i < smokeCount; i++) {
                            Vec3d dir = BobMathUtil.randVecInCone(normal, 30, world.rand);
                            dir = dir.scale(0.1 + world.rand.nextFloat() * 0.5);
                            ParticleSmokeAnim smoke = new ParticleSmokeAnim(world, x, y, z, 0.1F,
                                    smokeScale + world.rand.nextFloat() * smokeScale, 1, smokeLife);
                            smoke.color(r * 0.5F, g * 0.5F, b * 0.5F);
                            smoke.motion((float) dir.x, (float) dir.y, (float) dir.z);
                            ParticleBatchRenderer.addParticle(smoke);
                        }
                    }

                } else if (hitType == Type.ENTITY) {
                    world.playSound(x, y, z, HBMSoundHandler.hit_flesh, SoundCategory.BLOCKS, 1,
                            0.8F + world.rand.nextFloat() * 0.4F, false);
                    Vec3d bulletDirection = new Vec3d(data.getFloat("dirX"), data.getFloat("dirY"), data.getFloat(
                            "dirZ"));
                    if (GeneralConfig.bloodFX) {
                        for (int i = 0; i < 2; i++) {
                            int age = 10 + world.rand.nextInt(5);
                            ParticleBloodParticle blood = new ParticleBloodParticle(world, x, y, z,
                                    world.rand.nextInt(9), 1 + world.rand.nextFloat() * 3,
                                    0.5F + world.rand.nextFloat() * 0.5F, age);
                            blood.color(0.5F, 0F, 0F);
                            Vec3d dir = BobMathUtil.randVecInCone(normal, 70, world.rand);
                            dir = dir.scale(0.05F + world.rand.nextFloat() * 0.25);
                            if (i > 0) {
                                dir = BobMathUtil.randVecInCone(bulletDirection.normalize(), 20, world.rand);
                                dir = dir.scale(1F + world.rand.nextFloat());
                                blood.setMaxAge((int) (age * 0.75F));
                            }
                            blood.motion((float) dir.x, (float) dir.y + 0.1F, (float) dir.z);
                            ParticleBatchRenderer.addParticle(blood);
                        }
                        for (int i = 0; i < 3; i++) {
                            Vec3d dir = BobMathUtil.randVecInCone(normal, 30, world.rand);
                            dir = dir.scale(0.1 + world.rand.nextFloat() * 0.5);
                            ParticleSmokeAnim smoke = new ParticleSmokeAnim(world, x, y, z, 0.1F,
                                    3 + world.rand.nextFloat() * 3, 1, 10);
                            smoke.color(0.4F, 0, 0);
                            smoke.motion((float) dir.x, (float) dir.y, (float) dir.z);
                            ParticleBatchRenderer.addParticle(smoke);
                        }
                    }
                }
            }
            case "vanilla" -> {
                double mX = data.getDouble("mX");
                double mY = data.getDouble("mY");
                double mZ = data.getDouble("mZ");
                world.spawnParticle(EnumParticleTypes.getByName(data.getString("mode")), x, y, z, mX, mY, mZ);
            }
            case "anim" -> {
                EnumHand hand = EnumHand.values()[data.getInteger("hand")];
                int slot = player.inventory.currentItem;
                if (hand == EnumHand.OFF_HAND) {
                    slot = 9;
                }
                String name = data.getString("name");
                String mode = data.getString("mode");

                switch (name) {
                    case "crucible" -> {
                        switch (mode) {
                            case "equip" -> HbmAnimations.hotbar[slot] =
                                    new BlenderAnimation(player.getHeldItem(hand).getItem().getTranslationKey(),
                                            System.currentTimeMillis(), 1, ResourceManager.crucible_equip,
                                            new EndResult(EndType.STAY));
                            case "crucible" -> {
                                BusAnimation animation = new BusAnimation()
                                        .addBus("GUARD_ROT", new BusAnimationSequence()
                                                .addKeyframe(new BusAnimationKeyframe(90, 0, 1, 0))
                                                .addKeyframe(new BusAnimationKeyframe(90, 0, 1, 800))
                                                .addKeyframe(new BusAnimationKeyframe(0, 0, 1, 50)));

                                HbmAnimations.hotbar[slot] =
                                        new Animation(player.getHeldItem(hand).getItem().getTranslationKey(),
                                                System.currentTimeMillis(), animation);
                            }
                            case "swing" -> {
                                BusAnimation animation = new BusAnimation()
                                        .addBus("SWING", new BusAnimationSequence()
                                                .addKeyframe(new BusAnimationKeyframe(120, 0, 0, 150))
                                                .addKeyframe(new BusAnimationKeyframe(0, 0, 0, 500)));
                                if (HbmAnimations.hotbar[slot] instanceof BlenderAnimation) {
                                    HbmAnimations.hotbar[slot].animation = animation;
                                    HbmAnimations.hotbar[slot].startMillis = System.currentTimeMillis();
                                } else {
                                    HbmAnimations.hotbar[slot] =
                                            new Animation(player.getHeldItem(hand).getItem().getTranslationKey(),
                                                    System.currentTimeMillis(), animation);
                                }
                            }
                            case "cSwing" -> {
                                if (HbmAnimations.getRelevantTransformation("SWING_ROT", hand)[0] == 0) {

                                    int offset = rand.nextInt(80) - 20;

                                    BusAnimation animation = new BusAnimation()
                                            .addBus("SWING_ROT", new BusAnimationSequence()
                                                    .addKeyframe(new BusAnimationKeyframe(60 - offset, 60 - offset,
                                                            -55, 75))
                                                    .addKeyframe(new BusAnimationKeyframe(60 + offset, 60 - offset,
                                                            -45, 150))
                                                    .addKeyframe(new BusAnimationKeyframe(0, 0, 0, 500)))
                                            .addBus("SWING_TRANS", new BusAnimationSequence()
                                                    .addKeyframe(new BusAnimationKeyframe(-0, -10, 0, 75))
                                                    .addKeyframe(new BusAnimationKeyframe(0, -10, 0, 150))
                                                    .addKeyframe(new BusAnimationKeyframe(0, 0, 0, 500)));

                                    //Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord
                                    // .getMasterRecord(HBMSoundHandler.cSwing, 0.8F + player.getRNG().nextFloat() *
                                    // 0.2F));

                                    if (HbmAnimations.hotbar[slot] instanceof BlenderAnimation) {
                                        HbmAnimations.hotbar[slot].animation = animation;
                                        HbmAnimations.hotbar[slot].startMillis = System.currentTimeMillis();
                                    } else {
                                        HbmAnimations.hotbar[slot] =
                                                new Animation(player.getHeldItem(hand).getItem().getTranslationKey(),
                                                        System.currentTimeMillis(), animation);
                                    }
                                }
                            }
                            case "sSwing", "lSwing" -> { //temp for lance

                                int forward = 150;
                                int sideways = 100;
                                int retire = 200;

                                if (HbmAnimationsSedna.getRelevantAnim() == null) {

                                    BusAnimationSedna animation = new BusAnimationSedna()
                                            .addBus("SWING_ROT", new BusAnimationSequenceSedna()
                                                    .addPos(0, 0, 90, forward)
                                                    .addPos(45, 0, 90, sideways)
                                                    .addPos(0, 0, 0, retire))
                                            .addBus("SWING_TRANS", new BusAnimationSequenceSedna()
                                                    .addPos(0, 0, 3, forward)
                                                    .addPos(2, 0, 2, sideways)
                                                    .addPos(0, 0, 0, retire));


                                    HbmAnimationsSedna.hotbar[player.inventory.currentItem][0] = new HbmAnimationsSedna.Animation(player.getHeldItemMainhand().getItem().getTranslationKey(), System.currentTimeMillis(), animation, null);

                                } else {

                                    double[] rot = HbmAnimationsSedna.getRelevantTransformation("SWING_ROT");
                                    double[] trans = HbmAnimationsSedna.getRelevantTransformation("SWING_TRANS");

                                    if (System.currentTimeMillis() - HbmAnimationsSedna.getRelevantAnim().startMillis < 50)
                                        return;

                                    BusAnimationSedna animation = new BusAnimationSedna()
                                            .addBus("SWING_ROT", new BusAnimationSequenceSedna()
                                                    .addPos(rot[0], rot[1], rot[2], 0)
                                                    .addPos(0, 0, 90, forward)
                                                    .addPos(45, 0, 90, sideways)
                                                    .addPos(0, 0, 0, retire))
                                            .addBus("SWING_TRANS", new BusAnimationSequenceSedna()
                                                    .addPos(trans[0], trans[1], trans[2], 0)
                                                    .addPos(0, 0, 3, forward)
                                                    .addPos(2, 0, 2, sideways)
                                                    .addPos(0, 0, 0, retire));

                                    HbmAnimationsSedna.hotbar[player.inventory.currentItem][0] = new HbmAnimationsSedna.Animation(player.getHeldItemMainhand().getItem().getTranslationKey(), System.currentTimeMillis(), animation, null);
                                }
                            }
                            default -> throw new IllegalStateException("Unexpected value: " + mode);
                        }
                    }
                    case "hs_sword" -> {
                        switch (mode) {
                            case "equip" -> HbmAnimations.hotbar[slot] =
                                    new BlenderAnimation(player.getHeldItem(hand).getItem().getTranslationKey(),
                                            System.currentTimeMillis(), 1, ResourceManager.hs_sword_equip,
                                            new EndResult(EndType.STAY));
                            case "swing" -> {
                                BusAnimation animation = new BusAnimation()
                                        .addBus("SWING", new BusAnimationSequence()
                                                .addKeyframe(new BusAnimationKeyframe(120, 0, 0, 150))
                                                .addKeyframe(new BusAnimationKeyframe(0, 0, 0, 500)));
                                if (HbmAnimations.hotbar[slot] instanceof BlenderAnimation) {
                                    HbmAnimations.hotbar[slot].animation = animation;
                                    HbmAnimations.hotbar[slot].startMillis = System.currentTimeMillis();
                                } else {
                                    HbmAnimations.hotbar[slot] =
                                            new Animation(player.getHeldItem(hand).getItem().getTranslationKey(),
                                                    System.currentTimeMillis(), animation);
                                }
                            }
                            default -> throw new IllegalStateException("Unexpected value: " + mode);
                        }
                    }
                    case "hf_sword" -> {
                        switch (mode) {
                            case "equip" -> HbmAnimations.hotbar[slot] =
                                    new BlenderAnimation(player.getHeldItem(hand).getItem().getTranslationKey(),
                                            System.currentTimeMillis(), 1, ResourceManager.hf_sword_equip,
                                            new EndResult(EndType.STAY));
                            case "swing" -> {
                                BusAnimation animation = new BusAnimation()
                                        .addBus("SWING", new BusAnimationSequence()
                                                .addKeyframe(new BusAnimationKeyframe(120, 0, 0, 150))
                                                .addKeyframe(new BusAnimationKeyframe(0, 0, 0, 500)));
                                if (HbmAnimations.hotbar[slot] instanceof BlenderAnimation) {
                                    HbmAnimations.hotbar[slot].animation = animation;
                                    HbmAnimations.hotbar[slot].startMillis = System.currentTimeMillis();
                                } else {
                                    HbmAnimations.hotbar[slot] =
                                            new Animation(player.getHeldItem(hand).getItem().getTranslationKey(),
                                                    System.currentTimeMillis(), animation);
                                }
                            }
                            default -> throw new IllegalStateException("Unexpected value: " + mode);
                        }
                    }
                }

                if ("generic".equals(mode)) {
                    ItemStack stack = player.getHeldItem(hand);

                    if (!stack.isEmpty() && stack.getItem() instanceof IAnimatedItem item) {
                        BusAnimation anim = item.getAnimation(data, stack);

                        if (anim != null) {
                            HbmAnimations.hotbar[slot] =
                                    new Animation(player.getHeldItem(hand).getItem().getTranslationKey(),
                                            System.currentTimeMillis(), anim);
                        }
                    }
                }
            }
            case "tau" -> {
                for (int i = 0; i < data.getByte("count"); i++)
                    Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleHbmSpark(world, x, y, z,
                            rand.nextGaussian() * 0.05, 0.05, rand.nextGaussian() * 0.05));
                Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleHadron(world, x, y, z));
            }
            case "vanish" -> vanish(data.getInteger("ent"));
            case "giblets" -> {
                int ent = data.getInteger("ent");
                vanish(ent);
                Entity e = world.getEntityByID(ent);

                if (e == null)
                    return;

                float width = e.width;
                float height = e.height;
                int gW = (int) (width / 0.25F);
                int gH = (int) (height / 0.25F);

                boolean blowMeIntoTheGodDamnStratosphere = rand.nextInt(15) == 0;
                double mult = 1D;

                if (blowMeIntoTheGodDamnStratosphere)
                    mult *= 10;

                for (int i = -(gW / 2); i <= gW; i++) {
                    for (int j = 0; j <= gH; j++) {
                        Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleGiblet(world, x, y, z,
                                rand.nextGaussian() * 0.25 * mult, rand.nextDouble() * mult,
                                rand.nextGaussian() * 0.25 * mult));
                    }
                }
            }
            case "sound" -> {
                String mode = data.getString("mode");
                if (mode.equals("crucible_loop")) {
                    int id = data.getInteger("playerId");
                    Entity e = world.getEntityByID(id);
                    if (e instanceof EntityPlayer) {
                        Minecraft.getMinecraft().getSoundHandler().playSound(new SoundLoopCrucible((EntityPlayer) e));
                    }
                }
            }
            default -> throw new IllegalStateException("Unexpected value: " + type);
        }
    }

    public static void vanish(int ent) {
        vanished.put(ent, System.currentTimeMillis() + 2000);
    }

    @Override
    public boolean isVanished(Entity e) {

        if (e == null)
            return false;

        if (!vanished.containsKey(e.getEntityId()))
            return false;

        return vanished.get(e.getEntityId()) > System.currentTimeMillis();
    }

    @Override
    public boolean getIsKeyPressed(EnumKeybind key) {

        return switch (key) {
            case JETPACK -> Minecraft.getMinecraft().gameSettings.keyBindJump.isKeyDown();
            case TOGGLE_JETPACK -> HbmKeybinds.jetpackKey.isKeyDown();
            case TOGGLE_HEAD -> HbmKeybinds.hudKey.isKeyDown();
            case RELOAD -> HbmKeybinds.reloadKey.isKeyDown();
            case DASH -> HbmKeybinds.dashKey.isKeyDown();
            case CRANE_UP -> HbmKeybinds.craneUpKey.isKeyDown();
            case CRANE_DOWN -> HbmKeybinds.craneDownKey.isKeyDown();
            case CRANE_LEFT -> HbmKeybinds.craneLeftKey.isKeyDown();
            case CRANE_RIGHT -> HbmKeybinds.craneRightKey.isKeyDown();
            case CRANE_LOAD -> HbmKeybinds.craneLoadKey.isKeyDown();
            case ABILITY_CYCLE -> HbmKeybinds.abilityCycle.isKeyDown();
            case ABILITY_ALT -> HbmKeybinds.abilityAlt.isKeyDown();
            case TOOL_ALT -> HbmKeybinds.copyToolAlt.isKeyDown();
            case GUN_PRIMARY -> Mouse.isButtonDown(0);
            case GUN_SECONDARY -> HbmKeybinds.gunSecondaryKey.isKeyDown();
            case GUN_TERTIARY -> HbmKeybinds.gunTertiaryKey.isKeyDown();
        };

    }

    @Override
    public EntityPlayer me() {
        return Minecraft.getMinecraft().player;
    }

    @Override
    public void setRecoil(float rec) {
        RecoilHandler.verticalVelocity = rec;
    }

    @Override
    public void spawnSFX(World world, double posX, double posY, double posZ, int type, Vec3d payload) {
        int pow = 250;
        float angle = 25;
        float base = 0.5F;
        for (int i = 0; i < pow; i++) {

            float momentum = base * world.rand.nextFloat();
            float sway = (pow - i) / (float) pow;
            Vec3d vec = new Vec3d(payload.x, payload.y, payload.z);
            vec = Vec3dUtil.rotateRoll(vec, ((float) (angle * world.rand.nextGaussian() * sway * Math.PI / 180D)));
            vec = vec.rotateYaw((float) (angle * world.rand.nextGaussian() * sway * Math.PI / 180D));

            Spark blast = new Spark(world, posX, posY, posZ, vec.x * momentum, vec.y * momentum, vec.z * momentum, Minecraft.getMinecraft().effectRenderer);

            if (world.rand.nextBoolean())
                blast.setColor(0x0088EA);
            else
                blast.setColor(0x52A8E6);

            Minecraft.getMinecraft().effectRenderer.addEffect(blast);
        }
    }

    @Override
    public boolean opengl33() {
        return GLContext.getCapabilities().OpenGL33;
    }

    @Override
    public void checkGLCaps() {
        GLCompat.error = GLCompat.init();
        if (GLCompat.error.isEmpty()) {
            MainRegistry.logger.log(Level.INFO, "Advanced rendering fully supported");
        } else {
            MainRegistry.logger.log(Level.ERROR, "Advanced rendering not supported: " + GLCompat.error);
        }
    }

    @Override
    public void preInit(FMLPreInitializationEvent evt) {
        if (SoundSystemConfig.getNumberNormalChannels() < 128) {
            SoundSystemConfig.setNumberNormalChannels(128);
        }
        OBJLoader.INSTANCE.addDomain(Tags.MODID);

        AutoRegistry.preInitClient();
        for (Map.Entry<Item, TileEntityItemStackRenderer> entry : RBMKItemRenderers.itemRenderers.entrySet()) {
            entry.getKey().setTileEntityItemStackRenderer(entry.getValue());
        }

        for (TileEntitySpecialRenderer<? extends TileEntity> renderer : TileEntityRendererDispatcher.instance.renderers.values()) {
            if (renderer instanceof IItemRendererProvider prov) {
                for (Item item : prov.getItemsForRenderer()) {
                    item.setTileEntityItemStackRenderer(prov.getRenderer(item));
                }
            }
        }

        // same crap but for items directly because why invent a new solution when this shit works just fine
        for (Item renderer : Item.REGISTRY) {
            if (renderer instanceof IItemRendererProvider provider) {
                for (Item item : provider.getItemsForRenderer()) {
                    item.setTileEntityItemStackRenderer(provider.getRenderer(item));
                }
            }
        }

        // IItemRendererProvider is not applicable to Render<T extends Entity>
        Item.getItemFromBlock(ModBlocks.boat).setTileEntityItemStackRenderer(new RenderBoat.BoatItemRenderer());
    }

    @Deprecated
    @Override
    public AudioWrapper getLoopedSound(SoundEvent sound, SoundCategory cat, float x, float y, float z, float volume, float pitch) {
        AudioWrapperClient audio = new AudioWrapperClient(sound, cat, false);
        audio.updatePosition(x, y, z);
        return audio;
    }

    @Override
    public AudioWrapper getLoopedSound(SoundEvent sound, SoundCategory cat, float x, float y, float z, float volume, float range, float pitch, int keepAlive) {
        AudioWrapperClient audio = new AudioWrapperClient(sound, cat, true);
        audio.updatePosition(x, y, z);
        audio.updateVolume(volume);
        audio.updateRange(range);
        audio.setKeepAlive(keepAlive);
        return audio;
    }

    @Override
    public AudioWrapper getLoopedSound(SoundEvent sound, SoundCategory cat, float x, float y, float z, float volume, float range, float pitch) {
        AudioWrapperClient audio = new AudioWrapperClient(sound, cat, true);
        audio.updatePosition(x, y, z);
        audio.updateVolume(volume);
        audio.updateRange(range);
        return audio;
    }

    @Override
    public AudioWrapper getLoopedSoundStartStop(World world, SoundEvent sound, SoundEvent start, SoundEvent stop, SoundCategory cat, float x, float y, float z, float volume, float pitch) {
        AudioWrapperClientStartStop audio = new AudioWrapperClientStartStop(world, sound, start, stop, volume, cat);
        audio.updatePosition(x, y, z);
        return audio;
    }

    @Override
    public void displayTooltipLegacy(String msg, int time, int id) {
        if (id != 0)
            RenderInfoSystemLegacy.push(new RenderInfoSystemLegacy.InfoEntry(msg, time), id);
        else
            RenderInfoSystemLegacy.push(new RenderInfoSystemLegacy.InfoEntry(msg, time));
    }

    @Override
    public void playSoundClient(double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch) {
        Minecraft.getMinecraft().getSoundHandler().playSound(new PositionedSoundRecord(sound, category, volume, pitch, (float) x, (float) y, (float) z));
    }

    @Override
    public void postInit(FMLPostInitializationEvent e) {
        ResourceManager.loadAnimatedModels();
        Minecraft.getMinecraft().getRenderManager().getSkinMap().forEach((p, r) -> {
            r.addLayer(new JetpackHandler.JetpackLayer());
            r.getMainModel().bipedBody.addChild(new EgonBackpackRenderer(r.getMainModel()));
        });

        ParticleRenderLayer.register();
        BobmazonOfferFactory.init();
    }

    @Override
    public void playSound(String sound, Object data) {

    }

    @Override
    public void displayTooltip(String msg) {
        if (msg.startsWith("chat."))
            msg = I18nUtil.resolveKey(msg);
        Minecraft.getMinecraft().ingameGUI.setOverlayMessage(msg, false);
    }

    @Override
    public float partialTicks() {
        boolean paused = Minecraft.getMinecraft().isGamePaused();
        return paused ? Minecraft.getMinecraft().renderPartialTicksPaused : Minecraft.getMinecraft().getRenderPartialTicks();
    }

    @Override
    public List<ItemStack> getSubItems(ItemStack stack) {

        NonNullList<ItemStack> list = NonNullList.create();
        stack.getItem().getSubItems(stack.getItem().getCreativeTab(), list);
        for (ItemStack sta : list) {
            sta.setCount(stack.getCount());
        }
        return list;
    }

    @Override
    public float getImpactDust(World world) {
        return ImpactWorldHandler.getDustForClient(world);
    }

    @Override
    public float getImpactFire(World world) {
        return ImpactWorldHandler.getFireForClient(world);
    }

    @Override
    public boolean getImpact(World world) {
        return ImpactWorldHandler.getImpactForClient(world);
    }

    @Override
    public int getStackColor(@NotNull ItemStack stack, boolean amplify) {
        if (stack.isEmpty()) return 0x000000;
        int color;
        if (stack.getItem() instanceof ItemBlock) {
            try {
                Block block = ((ItemBlock) stack.getItem()).getBlock();
                IBlockState state = block.getStateFromMeta(stack.getMetadata());
                color = state.getMapColor(null, null).colorValue;
            } catch (Exception e) {
                color = 0xCCCCCC;
            }
        } else color = ColorUtil.getAverageColorFromStack(stack);
        if (amplify) color = ColorUtil.amplifyColor(color);
        return color;
    }


    public void onLoadComplete(FMLLoadCompleteEvent event) {
        if (!Loader.isModLoaded(Compat.ModIds.CTM)) NTMClientRegistry.ctmWarning = true;
    }
}
