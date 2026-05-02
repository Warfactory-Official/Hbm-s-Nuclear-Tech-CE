package com.hbm.main;

import com.hbm.Tags;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.bomb.DigammaMatter;
import com.hbm.blocks.fluid.FluidFogHandler;
import com.hbm.blocks.generic.BMPowerBox;
import com.hbm.blocks.generic.BlockModDoor;
import com.hbm.blocks.generic.TrappedBrick;
import com.hbm.blocks.machine.BlockSeal;
import com.hbm.blocks.machine.rbmk.RBMKDebrisRadiating;
import com.hbm.command.CommandRadVisClient;
import com.hbm.config.GeneralConfig;
import com.hbm.entity.grenade.EntityDisperserCanister;
import com.hbm.entity.grenade.EntityGrenadeBouncyGeneric;
import com.hbm.entity.grenade.EntityGrenadeImpactGeneric;
import com.hbm.entity.particle.ParticleContrail;
import com.hbm.entity.projectile.EntityAcidBomb;
import com.hbm.entity.projectile.EntityDischarge;
import com.hbm.handler.*;
import com.hbm.handler.HbmKeybinds.EnumKeybind;
import com.hbm.items.ModItems;
import com.hbm.items.weapon.sedna.factory.GunFactoryClient;
import com.hbm.lib.RecoilHandler;
import com.hbm.main.client.DynamicPlaceholderModelLoader;
import com.hbm.main.client.NTMClientRegistry;
import com.hbm.particle.ParticleRadiationFog;
import com.hbm.particle.ParticleRenderLayer;
import com.hbm.particle.helper.EffectNTLegacyAdapter;
import com.hbm.particle.helper.HbmEffectNT;
import com.hbm.particle_instanced.InstancedParticleRenderer;
import com.hbm.particle_instanced.ParticleContrailInstanced;
import com.hbm.particle_instanced.ParticleRadiationFogInstanced;
import com.hbm.qmaw.QMAWLoader;
import com.hbm.render.GLCompat;
import com.hbm.render.entity.ElectricityRenderer;
import com.hbm.render.entity.RenderMetaSensitiveItem;
import com.hbm.render.item.ItemRenderMissile;
import com.hbm.render.item.ItemRenderMissileGeneric;
import com.hbm.render.item.ItemRenderMissileGeneric.RenderMissileType;
import com.hbm.render.item.ItemRenderMissilePart;
import com.hbm.render.item.weapon.ItemRenderGunAnim;
import com.hbm.render.item.weapon.sedna.*;
import com.hbm.render.misc.MissilePart;
import com.hbm.render.modelrenderer.EgonBackpackRenderer;
import com.hbm.render.util.RenderInfoSystemLegacy;
import com.hbm.sound.AudioWrapper;
import com.hbm.sound.AudioWrapperClient;
import com.hbm.sound.AudioWrapperClientStartStop;
import com.hbm.util.ColorUtil;
import com.hbm.util.Compat;
import com.hbm.util.I18nUtil;
import com.hbm.util.Vec3dUtil;
import com.hbm.wiaj.cannery.Jars;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockStone;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleCloud;
import net.minecraft.client.particle.ParticleFirework.Spark;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderSnowball;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.resources.IReloadableResourceManager;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.registry.IRegistry;
import net.minecraft.world.World;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.obj.OBJLoader;
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
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import paulscode.sound.SoundSystemConfig;

import java.io.File;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class ClientProxy extends ServerProxy {
    public static final FloatBuffer AUX_GL_BUFFER = GLAllocation.createDirectFloatBuffer(16);
    public static final FloatBuffer AUX_GL_BUFFER2 = GLAllocation.createDirectFloatBuffer(16);
    //Drillgon200: Will I ever figure out how to write better code than this?
    public static final List<Runnable> deferredRenderers = new ArrayList<>();
    public static KeyBinding jetpackActivate;
    public static KeyBinding jetpackHover;
    public static KeyBinding jetpackHud;
    public static KeyBinding fsbFlashlight;
    //Drillgon200: This is stupid, but I'm lazy
    public static boolean renderingConstant = false;
    private static final Int2LongOpenHashMap vanished = new Int2LongOpenHashMap();

    public static void registerItemRenderer(Item i, TileEntityItemStackRenderer render) {
        NTMClientRegistry.bindTeisr(i, render);
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
    @SuppressWarnings("DataFlowIssue")
    public void registerRenderInfo() {
        if (!Minecraft.getMinecraft().getFramebuffer().isStencilEnabled())
            Minecraft.getMinecraft().getFramebuffer().enableStencil();

        MinecraftForge.EVENT_BUS.register(new ModEventHandlerClient());
        MinecraftForge.EVENT_BUS.register(new NTMClientRegistry());
        MinecraftForge.EVENT_BUS.register(new ModEventHandlerRenderer());
        MinecraftForge.EVENT_BUS.register(new PlacementPreviewHandler());
        MinecraftForge.EVENT_BUS.register(new RenderInfoSystemLegacy());
        ClientCommandHandler.instance.registerCommand(new CommandRadVisClient());

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
//        ((IReloadableResourceManager) Minecraft.getMinecraft().getResourceManager()).registerReloadListener(new HFRModelReloader());

//        ClientRegistry.bindTileEntitySpecialRenderer(TileEntityMachineAssembler.class, new RenderAssembler());
        // TODO: replace it with EntityCombineBallNT
        /*RenderingRegistry.registerEntityRenderingHandler(EntityCombineBall.class, (RenderManager man) -> {
        });*/
        RenderingRegistry.registerEntityRenderingHandler(EntityDischarge.class, ElectricityRenderer.FACTORY);
        registerGrenadeRenderer(EntityAcidBomb.class, Items.SLIME_BALL);
        registerGrenadeRenderer(EntityGrenadeBouncyGeneric.class, ModItems.stick_dynamite_fishing);
        registerGrenadeRenderer(EntityGrenadeImpactGeneric.class, ModItems.stick_dynamite);
        registerMetaSensitiveGrenade(EntityDisperserCanister.class, ModItems.disperser_canister);
        registerMetaSensitiveGrenade(EntityDisperserCanister.class, ModItems.glyphid_gland);

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
        ModelLoader.setCustomStateMapper(ModBlocks.floodlight, new StateMap.Builder().ignore(com.hbm.blocks.machine.Floodlight.META).build());
        ModelLoader.setCustomStateMapper(ModBlocks.spotlight_beam, new StateMap.Builder().ignore(com.hbm.blocks.machine.SpotlightBeam.META).build());
        ModelLoader.setCustomStateMapper(ModBlocks.frozen_grass, new StateMap.Builder().ignore(com.hbm.blocks.generic.WasteEarth.META).build());
        ModelLoader.setCustomStateMapper(ModBlocks.red_connector, new StateMap.Builder().ignore(com.hbm.blocks.network.ConnectorRedWire.FACING).build());
        ModelLoader.setCustomStateMapper(ModBlocks.silo_hatch_drillgon, new StateMap.Builder().ignore(com.hbm.blocks.machine.BlockSiloHatch.FACING).build());
        ModelLoader.setCustomStateMapper(ModBlocks.machine_diesel, new StateMap.Builder().ignore(BlockHorizontal.FACING).build());
        ModelLoader.setCustomStateMapper(ModBlocks.turret_sentry, fixedModelStateMapper(new ModelResourceLocation(ModBlocks.machine_autosaw.getRegistryName(), "normal")));
        ModelLoader.setCustomStateMapper(ModBlocks.turret_sentry_damaged, fixedModelStateMapper(new ModelResourceLocation(ModBlocks.machine_autosaw.getRegistryName(), "normal")));
        //Drillgon200: This can't be efficient, but eh.
        for (Block b : ModBlocks.ALL_BLOCKS) {
            if (b instanceof BlockDummyable || b instanceof RBMKDebrisRadiating || b instanceof DigammaMatter)
                ModelLoader.setCustomStateMapper(b, new StateMap.Builder().ignore(BlockDummyable.META).build());
        }
    }

    private <E extends Entity> void registerGrenadeRenderer(Class<E> clazz, Item grenade) {
        RenderingRegistry.registerEntityRenderingHandler(clazz, (RenderManager man) -> new RenderSnowball<>(man,
                grenade, Minecraft.getMinecraft().getRenderItem()));
    }

    private <E extends Entity & RenderMetaSensitiveItem.IHasMetaSensitiveRenderer<E>> void registerMetaSensitiveGrenade(Class<E> clazz, Item grenade) {
        RenderingRegistry.registerEntityRenderingHandler(clazz, (RenderManager man) ->
                new RenderMetaSensitiveItem<>(man, grenade, Minecraft.getMinecraft().getRenderItem()));
    }

    private static StateMapperBase fixedModelStateMapper(ModelResourceLocation location) {
        return new StateMapperBase() {
            @Override
            protected @NotNull ModelResourceLocation getModelResourceLocation(@NotNull IBlockState state) {
                return location;
            }
        };
    }

    @Override
    public void registerSpecialItemRenderers(IRegistry<ModelResourceLocation, IBakedModel> reg) {
        MissilePart.registerAllParts();

        MissilePart.parts.values().forEach(part -> {
            registerItemRenderer(part.part, new ItemRenderMissilePart(part));
        });

        registerItemRenderer(ModItems.missile_custom, new ItemRenderMissile());

        ItemRenderMissileGeneric.init();

        // GUNS
        registerItemRenderer(ModItems.gun_light_revolver, new ItemRenderAtlas(ResourceManager.bio_revolver_tex));
        registerItemRenderer(ModItems.gun_light_revolver_atlas, new ItemRenderAtlas(ResourceManager.bio_revolver_atlas_tex));
        registerItemRenderer(ModItems.gun_double_barrel, new ItemRenderDoubleBarrel(ResourceManager.double_barrel_tex));
        registerItemRenderer(ModItems.gun_double_barrel_sacred_dragon, new ItemRenderDoubleBarrel(ResourceManager.double_barrel_sacred_dragon_tex));
        registerItemRenderer(ModItems.gun_flamer, new ItemRenderFlamer(ResourceManager.flamethrower_tex));
        registerItemRenderer(ModItems.gun_flamer_topaz, new ItemRenderFlamer(ResourceManager.flamethrower_topaz_tex));
        registerItemRenderer(ModItems.gun_flamer_daybreaker, new ItemRenderFlamer(ResourceManager.flamethrower_daybreaker_tex));
        registerItemRenderer(ModItems.gun_heavy_revolver, new ItemRenderHeavyRevolver(ResourceManager.heavy_revolver_tex));
        registerItemRenderer(ModItems.gun_heavy_revolver_lilmac, new ItemRenderHeavyRevolver(ResourceManager.lilmac_tex));
        registerItemRenderer(ModItems.gun_heavy_revolver_protege, new ItemRenderHeavyRevolver(ResourceManager.heavy_revolver_protege_tex));
        registerItemRenderer(ModItems.gun_maresleg, new ItemRenderMaresleg(ResourceManager.maresleg_tex));
        registerItemRenderer(ModItems.gun_maresleg_broken, new ItemRenderMaresleg(ResourceManager.maresleg_broken_tex));
        registerItemRenderer(ModItems.gun_minigun, new ItemRenderMinigun(ResourceManager.minigun_tex));
        registerItemRenderer(ModItems.gun_minigun_lacunae, new ItemRenderMinigun(ResourceManager.minigun_lacunae_tex));
        registerItemRenderer(ModItems.gun_autoshotgun, new ItemRenderShredder(ResourceManager.shredder_tex));
        registerItemRenderer(ModItems.gun_autoshotgun_shredder, new ItemRenderShredder(ResourceManager.shredder_orig_tex));
        registerItemRenderer(ModItems.gun_amat, new ItemRenderAmat(ResourceManager.amat_tex));
        registerItemRenderer(ModItems.gun_amat_penance, new ItemRenderAmat(ResourceManager.amat_penance_tex));
        registerItemRenderer(ModItems.gun_amat_subtlety, new ItemRenderAmat(ResourceManager.amat_subtlety_tex));
        registerItemRenderer(ModItems.gun_g3, new ItemRenderG3(ResourceManager.g3_tex));
        registerItemRenderer(ModItems.gun_g3_zebra, new ItemRenderG3(ResourceManager.g3_zebra_tex));
        registerItemRenderer(ModItems.gun_henry, new ItemRenderHenry(ResourceManager.henry_tex));
        registerItemRenderer(ModItems.gun_henry_lincoln, new ItemRenderHenry(ResourceManager.henry_lincoln_tex));
        registerItemRenderer(ModItems.gun_laser_pistol, new ItemRenderLaserPistol(ResourceManager.laser_pistol_tex));
        registerItemRenderer(ModItems.gun_laser_pistol_pew_pew, new ItemRenderLaserPistol(ResourceManager.laser_pistol_pew_pew_tex));
        registerItemRenderer(ModItems.gun_laser_pistol_morning_glory, new ItemRenderLaserPistol(ResourceManager.laser_pistol_morning_glory_tex));
        registerItemRenderer(ModItems.gun_autoshotgun_sexy, new ItemRenderSexy(ResourceManager.sexy_tex));
        registerItemRenderer(ModItems.gun_autoshotgun_heretic, new ItemRenderSexy(ResourceManager.heretic_tex));
        // MISSILES
        registerItemRenderer(ModItems.missile_taint, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER0));
        registerItemRenderer(ModItems.missile_micro, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER0));
        registerItemRenderer(ModItems.missile_bhole, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER0));
        registerItemRenderer(ModItems.missile_schrabidium, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER0));
        registerItemRenderer(ModItems.missile_emp, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER0));
        registerItemRenderer(ModItems.missile_generic, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER1));
        registerItemRenderer(ModItems.missile_decoy, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER1));
        registerItemRenderer(ModItems.missile_stealth, new ItemRenderMissileGeneric(RenderMissileType.TYPE_STEALTH));
        registerItemRenderer(ModItems.missile_incendiary, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER1));
        registerItemRenderer(ModItems.missile_cluster, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER1));
        registerItemRenderer(ModItems.missile_buster, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER1));
        registerItemRenderer(ModItems.missile_anti_ballistic, new ItemRenderMissileGeneric(RenderMissileType.TYPE_ABM));
        registerItemRenderer(ModItems.missile_strong, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER2));
        registerItemRenderer(ModItems.missile_incendiary_strong, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER2));
        registerItemRenderer(ModItems.missile_cluster_strong, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER2));
        registerItemRenderer(ModItems.missile_buster_strong, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER2));
        registerItemRenderer(ModItems.missile_emp_strong, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER2));
        registerItemRenderer(ModItems.missile_burst, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER3));
        registerItemRenderer(ModItems.missile_inferno, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER3));
        registerItemRenderer(ModItems.missile_rain, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER3));
        registerItemRenderer(ModItems.missile_drill, new ItemRenderMissileGeneric(RenderMissileType.TYPE_TIER3));
        registerItemRenderer(ModItems.missile_nuclear, new ItemRenderMissileGeneric(RenderMissileType.TYPE_NUCLEAR));
        registerItemRenderer(ModItems.missile_nuclear_cluster, new ItemRenderMissileGeneric(RenderMissileType.TYPE_NUCLEAR));
        registerItemRenderer(ModItems.missile_volcano, new ItemRenderMissileGeneric(RenderMissileType.TYPE_NUCLEAR));
        registerItemRenderer(ModItems.missile_n2, new ItemRenderMissileGeneric(RenderMissileType.TYPE_NUCLEAR));
        registerItemRenderer(ModItems.missile_endo, new ItemRenderMissileGeneric(RenderMissileType.TYPE_THERMAL));
        registerItemRenderer(ModItems.missile_exo, new ItemRenderMissileGeneric(RenderMissileType.TYPE_THERMAL));
        registerItemRenderer(ModItems.missile_shuttle, new ItemRenderMissileGeneric(RenderMissileType.TYPE_ROBIN));
        registerItemRenderer(ModItems.missile_doomsday, new ItemRenderMissileGeneric(RenderMissileType.TYPE_DOOMSDAY));
        registerItemRenderer(ModItems.missile_doomsday_rusted, new ItemRenderMissileGeneric(RenderMissileType.TYPE_DOOMSDAY));
        registerItemRenderer(ModItems.missile_carrier, new ItemRenderMissileGeneric(RenderMissileType.TYPE_CARRIER));
        registerItemRenderer(ModItems.gun_b92, ItemRenderGunAnim.INSTANCE);
    }

    @Override
    public void registerGunCfg() {
        GunFactoryClient.init();
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
                if (GeneralConfig.instancedParticles) {
                    InstancedParticleRenderer.addParticle(new ParticleRadiationFogInstanced(world, x, y, z));
                } else {
                    ParticleRadiationFog fog = new ParticleRadiationFog(world, x, y, z);
                    Minecraft.getMinecraft().effectRenderer.addEffect(fog);
                }
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

    @Deprecated
    public void effectNT(NBTTagCompound data) {
        EffectNTLegacyAdapter.runLegacyEffect(data);
    }

    //mk4!
    public void effectNT(HbmEffectNT type, double x, double y, double z, @Nullable NBTTagCompound data) {
        type.summonParticle(Minecraft.getMinecraft().world, x, y, z, data);
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
            case TOGGLE_MAGNET -> HbmKeybinds.magnetKey.isKeyDown();
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
            case TOOL_CTRL -> HbmKeybinds.copyToolCtrl.isKeyDown();
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
    public void spawnSpark(World world, double posX, double posY, double posZ, Vec3d payload) {
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
    public void checkGLCaps() {
        // Reading GLCompat.error triggers its <clinit>, which runs the caps probe.
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
        ModelLoaderRegistry.registerLoader(DynamicPlaceholderModelLoader.INSTANCE);

        AutoRegistry.registerRenderInfo();

        HbmEffectNT.registerClientHandlers();

        ClientHttpHandler.preinit();
    }

    @Deprecated
    @Override
    public AudioWrapper getLoopedSound(SoundEvent sound, SoundCategory cat, float x, float y, float z, float volume, float pitch) {
        AudioWrapperClient audio = new AudioWrapperClient(sound, cat, false);
        audio.updatePosition(x, y, z);
        audio.updateVolume(volume);
        audio.updatePitch(pitch);
        return audio;
    }

    @Override
    public AudioWrapper getLoopedSound(SoundEvent sound, SoundCategory cat, float x, float y, float z, float volume, float range, float pitch, int keepAlive) {
        AudioWrapperClient audio = new AudioWrapperClient(sound, cat, true);
        audio.updatePosition(x, y, z);
        audio.updateVolume(volume);
        audio.updateRange(range);
        audio.updatePitch(pitch);
        audio.setKeepAlive(keepAlive);
        return audio;
    }

    @Override
    public AudioWrapper getLoopedSound(SoundEvent sound, SoundCategory cat, float x, float y, float z, float volume, float range, float pitch) {
        AudioWrapperClient audio = new AudioWrapperClient(sound, cat, true);
        audio.updatePosition(x, y, z);
        audio.updateVolume(volume);
        audio.updateRange(range);
        audio.updatePitch(pitch);
        return audio;
    }

    @Override
    public AudioWrapper getLoopedSoundStartStop(World world, SoundEvent sound, SoundEvent start, SoundEvent stop, SoundCategory cat, float x, float y, float z, float volume, float pitch) {
        AudioWrapperClientStartStop audio = new AudioWrapperClientStartStop(world, sound, start, stop, volume, cat);
        audio.updatePosition(x, y, z);
        audio.updatePitch(pitch);
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
    @SuppressWarnings("DataFlowIssue")
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
    @SuppressWarnings({"deprecation", "DataFlowIssue"})
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
