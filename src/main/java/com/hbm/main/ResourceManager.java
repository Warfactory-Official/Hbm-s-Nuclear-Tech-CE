package com.hbm.main;

import com.hbm.Tags;
import com.hbm.animloader.AnimatedModel;
import com.hbm.animloader.Animation;
import com.hbm.animloader.ColladaLoader;
import com.hbm.handler.HbmShaderManager2;
import com.hbm.handler.HbmShaderManager2.Shader;
import com.hbm.lib.internal.MethodHandleHelper;
import com.hbm.main.client.NTMClientRegistry;
import com.hbm.render.GLCompat;
import com.hbm.render.WavefrontObjDisplayList;
import com.hbm.render.anim.sedna.AnimationLoader;
import com.hbm.render.anim.sedna.BusAnimationSedna;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.render.loader.IModelCustom;
import com.hbm.render.loader.IModelCustomNamed;
import com.hbm.render.loader.WaveFrontObjectVAO;
import com.hbm.render.misc.LensVisibilityHandler;
import com.hbm.util.Compat;
import com.hbm.util.KeypadClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.SplashProgress;
import net.minecraftforge.fml.common.Loader;
import org.lwjgl.opengl.GL11;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.util.HashMap;

public class ResourceManager {

    //God
    public static final IModelCustom error = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/error.obj"));

    public static final IModelCustom cat = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/cat.obj"));

    //Press
    public static final IModelCustom press_body = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/press_body.obj")).asVBO();
    public static final IModelCustom press_head = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/press_head.obj")).asVBO();
    public static final IModelCustom epress_body = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/epress_body.obj")).asVBO();
    public static final IModelCustom epress_head = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/epress_head.obj")).asVBO();
    public static final IModelCustom conveyor_press = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/conveyor_press.obj")).asVBO();
    public static final IModelCustom ammo_press = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/ammo_press.obj")).asVBO();

    public static final IModelCustom bm_box_lever = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/bm_box_lever.obj")).asVBO();

    //Assembler
    public static final IModelCustom assembler_body = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/assembler_new_body.obj")).asVBO();
    public static final IModelCustom assembler_cog = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/assembler_new_cog.obj")).asVBO();
    public static final IModelCustom assembler_slider = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/assembler_new_slider.obj")).asVBO();
    public static final IModelCustom assembler_arm = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/assembler_new_arm.obj")).asVBO();
    public static final IModelCustom assembly_machine = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/assembly_machine.obj")).asVBO();
    public static final IModelCustom assemfac = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/assemfac.obj")).asVBO();
    public static final IModelCustom assembly_factory = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/assembly_factory.obj")).asVBO();

    //Chemplant
    public static final IModelCustom chemplant_new = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/chemplant_main_new.obj")).asVBO();
    public static final IModelCustom chemplant_body = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/chemplant_new_body.obj")).asVBO();
    public static final IModelCustom chemplant_spinner = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/chemplant_new_spinner.obj")).asVBO();
    public static final IModelCustom chemplant_piston = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/chemplant_new_piston.obj")).asVBO();
    public static final IModelCustom chemplant_fluid = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/chemplant_new_fluid.hmf")).asVBO();
    public static final IModelCustom chemplant_fluidcap = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/chemplant_new_fluidcap.hmf")).asVBO();
    public static final IModelCustom chemical_plant = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/chemical_plant.obj")).asVBO();

    public static final IModelCustom chemical_factory = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/chemical_factory.obj")).asVBO();
    public static final IModelCustom purex = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/purex.obj")).asVBO();
    public static final IModelCustom chemfac = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/chemfac.obj")).asVBO();

    //Mixer
    public static final IModelCustom mixer = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/mixer.obj")).asVBO();

    //F6 TANKS
    public static final IModelCustom tank = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/tank.obj")).asVBO();

    //Small Reactor
    public static final IModelCustom reactor_small_base = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/reactors/reactor_small_base.obj")).asVBO();
    public static final IModelCustom reactor_small_rods = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/reactors/reactor_small_rods.obj")).asVBO();

    //Breeder
    public static final IModelCustom breeder = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/reactors/breeder.obj")).asVBO();

    //ITER
    public static final IModelCustom iter = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/reactors/iter.obj")).asVBO();
    public static final IModelCustom fusion_torus = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/fusion/torus.obj")).asVBO();
    public static final IModelCustom fusion_klystron = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/fusion/klystron.obj")).asVBO();
    public static final IModelCustom fusion_breeder = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/fusion/breeder.obj")).asVBO();
    public static final IModelCustom fusion_collector = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/fusion/collector.obj")).asVBO();
    public static final IModelCustom fusion_boiler = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/fusion/boiler.obj")).asVBO();
    public static final IModelCustom fusion_mhdt = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/fusion/mhdt.obj")).asVBO();
    public static final IModelCustom fusion_coupler = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/fusion/coupler.obj")).asVBO();

    //ICF
    public static final IModelCustom icf = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/reactors/icf.obj")).asVBO();

    //Watz
    public static final IModelCustom watz = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/reactors/watz.obj")).asVBO();
    public static final IModelCustom watz_pump = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/watz_pump.obj")).asVBO();

    //FENSU
    public static final IModelCustom battery_socket = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/battery.obj")).asVBO();
    public static final IModelCustom battery_redd = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/fensu2.obj")).asVBO();
    public static final IModelCustom fensu = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/fensu.obj")).asVBO();

    //New Turrets made by Freon
    public static final IModelCustom turret_flamethower = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/turrets/freons/turret_flamethower.obj")).asVBO();
    public static final IModelCustom turret_cannon = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/turrets/freons/turret_cannon.obj")).asVBO();
    public static final IModelCustom turret_tau = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/turrets/freons/turret_tau.obj")).asVBO();
    public static final IModelCustom turret_mg = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/turrets/freons/turret_mg.obj")).asVBO();
    public static final IModelCustom turret_rocket_launcher = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/turrets/freons/turret_rocket_launcher.obj")).asVBO();


    //Turrets
    public static final IModelCustom turret_spitfire_base = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/turret_spitfire_base.obj")).asVBO();
    public static final IModelCustom turret_spitfire_rotor = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/turret_spitfire_rotor.obj")).asVBO();

    public static final IModelCustom turret_cwis_base = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/cwis_base.obj")).asVBO();
    public static final IModelCustom turret_cwis_rotor = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/cwis_rotor.obj")).asVBO();

    public static final IModelCustom turret_cheapo_base = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/turret_cheapo_base.obj")).asVBO();
    public static final IModelCustom turret_cheapo_rotor = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/turret_cheapo_rotor.obj")).asVBO();

    public static final IModelCustom turret_spitfire_gun = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/turret_spitfire_gun.obj")).asVBO();
    public static final IModelCustom turret_cwis_head = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/cwis_head.obj")).asVBO();
    public static final IModelCustom turret_cwis_gun = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/cwis_gun.obj")).asVBO();
    public static final IModelCustom turret_cheapo_head = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/turret_cheapo_head.obj")).asVBO();
    public static final IModelCustom turret_cheapo_gun = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/turret_cheapo_gun.obj")).asVBO();

    public static final IModelCustom turret_chekhov = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/turrets/turret_chekhov.obj")).asVBO();
    public static final IModelCustom turret_jeremy = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/turrets/turret_jeremy.obj")).asVBO();
    public static final IModelCustom turret_tauon = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/turrets/turret_tauon.obj")).asVBO();
    public static final IModelCustom turret_richard = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/turrets/turret_richard.obj")).asVBO();
    public static final IModelCustom turret_howard = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/turrets/turret_howard.obj")).asVBO();
    public static final IModelCustom turret_maxwell = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/turrets/turret_microwave.obj")).asVBO();
    public static final IModelCustom turret_fritz = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/turrets/turret_fritz.obj")).asVBO();
    public static final IModelCustom turret_himars = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/turrets/turret_himars.obj")).asVBO();
    public static final IModelCustom turret_sentry = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/turrets/turret_sentry.obj")).asVBO();
    public static final IModelCustom turret_arty = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/turrets/turret_arty.obj")).asVBO();

    public static final IModelCustom turret_howard_damaged = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/turrets/turret_howard_damaged.obj")).asVBO();

    //Satellites
    public static final IModelCustom sat_base = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/sat_base.obj")).asVBO();
    public static final IModelCustom sat_radar = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/sat_radar.obj")).asVBO();
    public static final IModelCustom sat_resonator = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/sat_resonator.obj")).asVBO();
    public static final IModelCustom sat_scanner = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/sat_scanner.obj")).asVBO();
    public static final IModelCustom sat_mapper = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/sat_mapper.obj")).asVBO();
    public static final IModelCustom sat_laser = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/sat_laser.obj")).asVBO();
    public static final IModelCustom sat_foeq = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/sat_foeq.obj")).asVBO();
    public static final IModelCustom sat_foeq_burning = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/sat_foeq_burning.obj")).asVBO();
    public static final IModelCustom sat_foeq_fire = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/sat_foeq_fire.obj")).asVBO();

    //Bomber
    public static final IModelCustom dornier = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/dornier.obj")).asVBO();
    public static final IModelCustom b29 = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/b29.obj")).asVBO();
    public static final IModelCustom c130 = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/c130.obj")).asVBO();

    //Missiles
    public static final IModelCustom missileV2 = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missile_v2.obj")).asVBO();
    public static final IModelCustom missileABM = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missile_abm.obj")).asVBO();
    public static final IModelCustom missileStrong = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missile_strong.obj")).asVBO();
    public static final IModelCustom missileMicro = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missile_micro.obj")).asVBO();
    public static final IModelCustom missileHuge = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missile_huge.obj")).asVBO();
    public static final IModelCustom missileNuclear = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missile_atlas.obj")).asVBO();
    public static final IModelCustom missileN2 = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missileneon.obj")).asVBO();
    public static final IModelCustom missileThermo = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missileThermo.obj")).asVBO();
    public static final IModelCustom missileDoomsday = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missileDoomsday.obj"));
    public static final IModelCustom missileTaint = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missileTaint.obj")).asVBO();
    public static final IModelCustom missileStealth = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missile_stealth.obj")).asVBO();
    public static final IModelCustom missileCarrier = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missileCarrier.obj"));
    public static final IModelCustom missileBooster = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missileBooster.obj"));
    public static final IModelCustom minerRocket = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/minerRocket.obj"));
    public static final IModelCustom soyuz_lander = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/soyuz_lander.obj")).asVBO();
    public static final IModelCustom soyuz_module = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/soyuz_module.obj")).asVBO();
    //Boxcar
    public static final IModelCustom boxcar = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/boxcar.obj")).asVBO();
    public static final IModelCustom torpedo = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/torpedo.obj")).asVBO();
    public static final IModelCustom duchessgambit = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/duchessgambit.obj")).asVBO();
    public static final IModelCustom building = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/building.obj")).asVBO();
    public static final IModelCustom rpc = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/rpc.obj")).asVBO();
    public static final IModelCustom tom_main = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/tom_main.obj")).asVBO();
    public static final IModelCustom tom_flame = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/tom_flame.hmf")).asVBO();
    public static final IModelCustom nikonium = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/nikonium.obj")).asVBO();
    public static final IModelCustom BFG10K = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/bfg337.obj")).asVBO();
    public static final IModelCustom hemisphere_uv = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/sphere_half.obj")).asVBO();
    //Dark Matter Core
    public static final IModelCustom dfc_emitter = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/core_emitter.obj")).asVBO();
    public static final IModelCustom dfc_receiver = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/core_receiver.obj")).asVBO();
    public static final IModelCustom dfc_injector = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/core_injector.obj")).asVBO();
    //Fan
    public static final IModelCustom fan = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/fan.obj")).asVBO();
    //Sphere
    public static final IModelCustom sphere_ruv = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/sphere_ruv.obj")).asVBO();
    public static final IModelCustom sphere_iuv = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/sphere_iuv.obj")).asVBO();
    public static final IModelCustom sphere_uv_anim = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/sphere_uv.hmf")).asVBO();
    //Meteor
    public static final IModelCustom meteor = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/meteor.obj")).asVBO();
    //Guns
    public static final IModelCustom brimstone = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/brimstone.obj")).asVBO();
    public static final IModelCustom hk69 = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/hk69.obj")).asVBO();
    public static final IModelCustom deagle = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/deagle.obj")).asVBO();
    public static final IModelCustom shotty = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/supershotty.obj")).asVBO();
    public static final IModelCustom ks23 = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/ks23.obj")).asVBO();
    public static final IModelCustom flamer = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/flamer.obj")).asVBO();
    public static final IModelCustom flechette = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/flechette.obj")).asVBO();
    public static final IModelCustom sauergun = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/sauergun.obj")).asVBO();
    public static final IModelCustom vortex = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/vortex.obj")).asVBO();
    public static final IModelCustom thompson = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/thompson.obj")).asVBO();
    public static final IModelCustom bolter = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/bolter.obj")).asVBO();
    public static final IModelCustom ff_python = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/python.obj")).asVBO();
    public static final IModelCustom ff_maresleg = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/maresleg.obj")).asVBO();
    public static final IModelCustom ff_nightmare = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/nightmare.obj")).asVBO();
    public static final IModelCustom fireext = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/fireext.obj")).asVBO();
    public static final IModelCustom ar15 = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/ar15.obj")).asVBO();
    public static final IModelCustom mg42 = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/mg42.obj")).asVBO();
    public static final IModelCustom rem700 = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/rem700.obj")).asVBO();
    public static final IModelCustom rem700poly = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/rem700poly.obj")).asVBO();
    public static final IModelCustom rem700sat = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/rem700sat.obj")).asVBO();
    public static final IModelCustom cursed_revolver = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/cursed.obj"));
    public static final IModelCustom detonator_laser = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/detonator_laser.obj")).asVBO();
    public static final IModelCustom remington = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/remington.obj")).asVBO();
    public static final IModelCustom nightmare_dark = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/nightmare_dark.obj")).asVBO();
    public static final IModelCustom glass_cannon = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/glass_cannon.obj")).asVBO();
    public static final IModelCustom novac = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/novac.obj")).asVBO();
    public static final IModelCustom lunatic_sniper = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/lunatic_sniper.obj")).asVBO();
    public static final IModelCustom benelli = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/benelli_new.obj")).asVBO();
    public static final IModelCustom coilgun = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/coilgun.obj")).asVBO();
    public static final IModelCustom cryocannon = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/cryo_cannon.obj")).asVBO();
    public static final IModelCustom uac_pistol = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/uac_pistol.obj")).asVBO();
    public static final IModelCustom pepperbox = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/pepperbox.obj")).asVBO();
    public static final IModelCustom bio_revolver = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/bio_revolver.obj")).asVBO();
    public static final IModelCustom henry = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/henry.obj")).asVBO();
    public static final IModelCustom greasegun = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/greasegun.obj")).asVBO();
    public static final IModelCustom maresleg = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/maresleg.obj")).asVBO();
    public static final IModelCustom flaregun = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/flaregun.obj")).asVBO();
    public static final IModelCustom am180 = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/am180.obj")).asVBO();
    public static final IModelCustom liberator = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/liberator.obj")).asVBO();
    public static final IModelCustom congolake = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/congolake.obj")).asVBO();
    public static final IModelCustom flamethrower = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/flamethrower.obj")).asVBO();
    public static final IModelCustom lilmac = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/lilmac.obj")).asVBO();
    public static final IModelCustom carbine = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/carbine.obj")).asVBO();
    public static final IModelCustom uzi = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/uzi.obj")).asVBO();
    public static final IModelCustom spas_12 = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/spas-12.obj")).asVBO();
    public static final IModelCustom panzerschreck = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/panzerschreck.obj")).asVBO();
    public static final IModelCustom g3 = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/g3.obj")).asVBO();
    public static final IModelCustom stinger = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/stinger.obj")).asVBO();
    public static final IModelCustom chemthrower = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/chemthrower.obj")).asVBO();
    public static final IModelCustom amat = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/amat.obj")).asVBO();
    public static final IModelCustom m2 = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/m2_browning.obj")).asVBO();
    public static final IModelCustom shredder = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/shredder.obj")).asVBO();
    public static final IModelCustom sexy = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/sexy.obj")).asVBO();
    public static final IModelCustom whiskey = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/whiskey.obj")).asVBO();
    public static final IModelCustom quadro = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/quadro.obj")).asVBO();
    public static final IModelCustom mike_hawk = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/mike_hawk.obj")).asVBO();
    public static final IModelCustom minigun = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/minigun.obj")).asVBO();
    public static final IModelCustom missile_launcher = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/missile_launcher.obj")).asVBO();
    public static final IModelCustom tesla_cannon = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/tesla_cannon.obj")).asVBO();
    public static final IModelCustom laser_pistol = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/laser_pistol.obj")).asVBO();
    public static final IModelCustom stg77 = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/stg77.obj")).asVBO();
    public static final IModelCustom tau = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/tau.obj")).asVBO();
    public static final IModelCustom fatman = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/fatman.obj")).asVBO();
    public static final IModelCustom lasrifle = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/lasrifle.obj")).asVBO();
    public static final IModelCustom lasrifle_mods = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/lasrifle_mods.obj")).asVBO();
    public static final IModelCustom hangman = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/hangman.obj")).asVBO();
    public static final IModelCustom folly = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/folly.obj")).asVBO();
    public static final IModelCustom double_barrel = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/sacred_dragon.obj")).asVBO();
    public static final IModelCustom aberrator = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/aberrator.obj")).asVBO();
    public static final IModelCustom mas36 = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/mas36.obj")).asVBO();
    public static final IModelCustom charge_thrower = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/charge_thrower.obj")).asVBO();
    public static final IModelCustom drill = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/drill.obj")).asVBO();
    public static final IModelCustom lance = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/lance.obj")).asVBO();
    public static final IModelCustom grenade_frag = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/grenade_frag.obj")).asVBO();
    public static final IModelCustom grenade_aschrab = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/grenade_aschrab.obj")).asVBO();
    public static final IModelCustom armor_bj = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/armor/BJ.obj")).asVBO();
    public static final IModelCustom armor_hev = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/armor/hev.obj")).asVBO();
    public static final IModelCustom armor_hat = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/armor/hat.obj")).asVBO();
    public static final IModelCustom armor_no9 = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/armor/no9.obj")).asVBO();
    public static final IModelCustom armor_goggles = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/armor/goggles.obj")).asVBO();
    public static final IModelCustom armor_ajr = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/armor/AJR.obj")).asVBO();
    public static final IModelCustom armor_steamsuit = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/armor/steamsuit.obj")).asVBO();
    public static final IModelCustom armor_dieselsuit = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/armor/bnuuy.obj")).asVBO();
    public static final IModelCustom armor_trenchmaster = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/armor/trenchmaster.obj")).asVBO();
    public static final IModelCustom armor_t51 = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/armor/t51.obj")).asVBO();
    public static final IModelCustom armor_taurun = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/armor/taurun.obj")).asVBO();
    public static final IModelCustom armor_bismuth = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/armor/bismuth.obj")).asVBO();
    public static final IModelCustom armor_envsuit = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/armor/envsuit.obj")).asVBO();
    public static final IModelCustom armor_RPA = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/armor/rpa.obj")).asVBO();
    public static final IModelCustom armor_fau = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/armor/fau.obj")).asVBO();
    public static final IModelCustom armor_dnt = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/armor/dnt.obj")).asVBO();
    public static final IModelCustom armor_mod_tesla = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/armor/mod_tesla.obj")).asVBO();
    public static final IModelCustom armor_wings = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/armor/murk.obj")).asVBO();
    //Centrifuge
    public static final IModelCustom centrifuge = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/centrifuge.obj")).asVBO();
    public static final IModelCustom centrifuge_gas = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/centrifuge_gas.obj")).asVBO();
    public static final IModelCustom silex = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/silex.obj")).asVBO();
    public static final IModelCustom fel = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/fel.obj")).asVBO();
    //Arc Welder
    public static final IModelCustom arc_welder = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/arc_welder.obj")).asVBO();
    //Arc Furnace
    public static final IModelCustom arc_furnace = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/arc_furnace.obj")).asVBO();
    //Soldering Station
    public static final IModelCustom soldering_station = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/soldering_station.obj")).asVBO();
    //Magnusson Device
    public static final IModelCustom microwave = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/microwave.obj")).asVBO();
    // Autosaw
    public static final IModelCustom autosaw = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/autosaw.obj"));
    //Cables
    public static final IModelCustom cable_neo = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/blocks/cable_neo.obj")).asVBO();
    //Big Cables
    public static final IModelCustom connector = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/network/connector.obj")).asVBO();
    public static final IModelCustom pylon_large = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/network/pylon_large.obj")).asVBO();
    public static final IModelCustom pylon_medium = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/network/pylon_medium.obj")).asVBO();
    public static final IModelCustom substation = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/network/substation.obj")).asVBO();
    public static final IModelCustom pipe_anchor = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/network/pipe_anchor.obj")).asVBO();
    public static final IModelCustom fluid_pump = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/network/fluid_diode.obj")).asVBO();
    public static final IModelCustom conservecrate = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/blocks/conservecrate.obj")).asVBO();
    //Pipe
    public static final IModelCustom pipe_neo = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/blocks/pipe_neo.obj")).asVBO();
    //Conveyor blocks/cranes //FIXME: Doesnt work with VBO
    public static final IModelCustom crane_splitter = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/blocks/crane_splitter.obj"));
    public static final IModelCustom crane_buffer = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/blocks/crane_buffer.obj"));
    //B.O.P
    public static final IModelCustom ore_slopper = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/ore_slopper.obj")).asVBO();
    //Laser Miner
    public static final IModelCustom mining_laser = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/mining_laser.obj")).asVBO();
    //Excavator
    public static final IModelCustom excavator = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/excavator.obj")).asVBO();
    //Crystallizer
    public static final IModelCustom crystallizer = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/crystallizer.obj")).asVBO();
    //Cyclotron
    public static final IModelCustom cyclotron = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/cyclotron.obj")).asVBO();
    //Albion Particle Accelerator
    public static final IModelCustom pa_source = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/particleaccelerator/source.obj")).asVBO();
    public static final IModelCustom pa_beamline = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/particleaccelerator/beamline.obj")).asVBO();
    public static final IModelCustom pa_rfc = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/particleaccelerator/rfc.obj")).asVBO();
    public static final IModelCustom pa_quadrupole = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/particleaccelerator/quadrupole.obj")).asVBO();
    public static final IModelCustom pa_dipole = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/particleaccelerator/dipole.obj")).asVBO();
    public static final IModelCustom pa_detector = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/particleaccelerator/detector.obj")).asVBO();
    //Exposure Chamber
    public static final IModelCustom exposure_chamber = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/exposure_chamber.obj")).asVBO();
    //RTG
    public static final IModelCustom rtg = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/rtg.obj")).asVBO();
    public static final IModelCustom rtg_connector = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/rtg_connector.obj")).asVBO();
    //Waste Drum
    public static final IModelCustom waste_drum = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/drum.obj")).asVBO();
    //Deuterium Tower
    public static final IModelCustom deuterium_tower = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/machine_deuterium_tower.obj")).asVBO();
    //Vault Door
    public static final IModelCustom vault_cog = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/vault_cog.obj")).asVBO();
    public static final IModelCustom vault_frame = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/vault_frame.obj")).asVBO();
    public static final IModelCustom vault_teeth = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/vault_teeth.obj")).asVBO();
    public static final IModelCustom vault_label = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/vault_label.obj")).asVBO();
    //Blast Door
    public static final IModelCustom blast_door_base = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/blast_door_base.obj")).asVBO();
    public static final IModelCustom blast_door_tooth = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/blast_door_tooth.obj")).asVBO();
    public static final IModelCustom blast_door_slider = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/blast_door_slider.obj")).asVBO();
    public static final IModelCustom blast_door_block = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/blast_door_block.obj")).asVBO();
    //Tesla Coil
    public static final IModelCustom tesla = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/tesla.obj")).asVBO();
    public static final IModelCustom teslacrab = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/mobs/teslacrab.obj")).asVBO();
    public static final IModelCustom taintcrab = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/mobs/taintcrab.obj")).asVBO();
    public static final IModelCustom maskman = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/mobs/maskman.obj")).asVBO();
    public static final IModelCustom ufo = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/mobs/ufo.obj")).asVBO();
    public static final IModelCustom glyphid = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/mobs/glyphid.obj")).asVBO();
    public static final IModelCustom drone = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/mobs/quadcopter.obj")).asVBO();
    //Lights
    public static final IModelCustom lantern = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/trinkets/lantern.obj")).asVBO();
    //ZIRNOX
    public static final IModelCustom zirnox = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/zirnox.obj")).asVBO();
    public static final IModelCustom zirnox_destroyed = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/zirnox_destroyed.obj")).asVBO();
    //Belt
    public static final IModelCustom arrow = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/blocks/arrow.obj")).asVBO();
    //Radiolysis
    public static final IModelCustom radiolysis = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/radiolysis.obj"));
    //RotaryFurnace
    public static final IModelCustom rotary_furnace = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/rotary_furnace.obj")).asVBO();
    //Electrolyser
    public static final IModelCustom electrolyser = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/electrolyser.obj")).asVBO();
    //Charging Station
    public static final IModelCustom charger = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/blocks/charger.obj")).asVBO();
    public static final IModelCustom refueler = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/blocks/refueler.obj")).asVBO();
    //DecoContainer (File Cabinet for now)
    public static final IModelCustom file_cabinet = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/file_cabinet.obj")).asVBO();
    //Selenium Engine
    public static final IModelCustom selenium_body = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/selenium_engine_body.obj")).asVBO();
    public static final IModelCustom selenium_rotor = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/selenium_engine_rotor.obj")).asVBO();
    public static final IModelCustom selenium_piston = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/selenium_engine_piston.obj")).asVBO();
    //Radgen
    public static final IModelCustom radgen_body = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/radgen.obj")).asVBO();
    public static final IModelCustom geiger = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/blocks/geiger_counter.obj")).asVBO();
    //Pumpjack
    public static final IModelCustom pumpjack = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/pumpjack.obj")).asVBO();
    // Fracking Tower
    public static final IModelCustom fracking_tower = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/fracking_tower.obj")).asVBO();
    //TELEX
    public static final IModelCustom telex = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/telex.obj"));
    //Refinery
    public static final IModelCustom refinery = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/refinery.obj")).asVBO();
    public static final IModelCustom vacuum_distill = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/vacuum_distill.obj")).asVBO();
    public static final IModelCustom fraction_tower = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/fraction_tower.obj")).asVBO();
    public static final IModelCustom fraction_spacer = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/fraction_spacer.obj")).asVBO();
    public static final IModelCustom cracking_tower = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/cracking_tower.obj")).asVBO();
    public static final IModelCustom catalytic_reformer = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/catalytic_reformer.obj")).asVBO();
    public static final IModelCustom hydrotreater = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/hydrotreater.obj")).asVBO();
    public static final IModelCustom liquefactor = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/liquefactor.obj")).asVBO();
    public static final IModelCustom solidifier = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/solidifier.obj")).asVBO();
    public static final IModelCustom coker = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/coker.obj")).asVBO();
    public static final IModelCustom compressor = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/compressor.obj")).asVBO();
    public static final IModelCustom pyrooven = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/pyrooven.obj")).asVBO();
    //Flare Stack
    public static final IModelCustom oilflare = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/flare_stack.obj")).asVBO();
    public static final IModelCustom chimney_brick = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/chimney_brick.obj")).asVBO();
    public static final IModelCustom chimney_industrial = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/chimney_industrial.obj")).asVBO();
    //Tank
    public static final IModelCustom fluidtank = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/fluidtank.obj")).asVBO();
    public static final IModelCustom fluidtank_exploded = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/fluidtank_exploded.obj")).asVBO();
    public static final IModelCustom bat9000 = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/bat9000.obj")).asVBO();
    public static final IModelCustom orbus = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/orbus.obj")).asVBO();
    //Turbofan
    public static final IModelCustom turbofan = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/turbofan.obj")).asVBO();
    //Pumps
    public static final IModelCustom pump = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/pump.obj")).asVBO();
    //Large Turbine
    public static final IModelCustom steam_engine = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/steam_engine.obj")).asVBO();
    public static final IModelCustom turbine = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/turbine.obj")).asVBO();
    public static final IModelCustom chungus = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/chungus.obj")).asVBO();
    //Cooling Tower
    public static final IModelCustom tower_small = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/tower_small.obj")).asVBO();
    public static final IModelCustom tower_large = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/tower_large.obj")).asVBO();
    public static final IModelCustom condenser = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/condenser.obj")).asVBO();
    //Air stuff
    public static final IModelCustom intake = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/intake.obj")).asVBO();
    //Deuterium Tower
    public static final ResourceLocation deuterium_tower_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/machine_deuterium_tower.png");
    //Strand Caster
    public static final IModelCustom strand_caster = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/strand_caster.obj")).asVBO();
    //Industrial Boiler
    public static final IModelCustom boiler_industrial = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/industrial_boiler.obj")).asVBO();
    public static final IModelCustom hephaestus = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/hephaestus.obj")).asVBO();
    //IGen
    public static final IModelCustom igen = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/igen.obj")).asVBO();
    //Combustion Engine
    public static final IModelCustom combustion_engine = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/combustion_engine.obj")).asVBO();
    //Firebox, and the lot
    public static final IModelCustom sawmill = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/sawmill.obj")).asVBO();
    public static final IModelCustom heater_firebox = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/firebox.obj")).asVBO();
    public static final IModelCustom heater_oven = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/heating_oven.obj")).asVBO();
    public static final IModelCustom heater_oilburner = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/oilburner.obj")).asVBO();
    public static final IModelCustom heater_electric = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/electric_heater.obj")).asVBO();
    public static final IModelCustom heater_heatex = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/heatex.obj")).asVBO();
    //Furnaces
    public static final IModelCustom furnace_iron = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/furnace_iron.obj")).asVBO();
    public static final IModelCustom furnace_steel = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/furnace_steel.obj")).asVBO();
    public static final IModelCustom combination_oven = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/combination_oven.obj")).asVBO();
    public static final IModelCustom crucible_heat = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/crucible.obj")).asVBO();
    public static final IModelCustom boiler = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/boiler.obj")).asVBO();
    public static final IModelCustom boiler_burst = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/boiler_burst.obj")).asVBO();
    //Heat-Based Machines
    public static final IModelCustom stirling = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/stirling.obj")).asVBO();
    //Wood Burner gen
    public static final IModelCustom wood_burner = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/wood_burner.obj")).asVBO();
    //Not wood burner gen
    public static final IModelCustom turbine_gas = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/turbinegas.obj")).asVBO();
    //Bombs
    public static final IModelCustom bomb_solinium = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/bombs/ufp.obj")).asVBO();
    public static final IModelCustom n2 = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/bombs/n2.obj")).asVBO();
    public static final IModelCustom n45_chain = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/bombs/n45_chain.obj")).asVBO();
    public static final IModelCustom fstbmb = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/bombs/fstbmb.obj")).asVBO();
    public static final IModelCustom bomb_gadget = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/bombs/gadget.obj")).asVBO();
    public static final IModelCustom bomb_boy = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/bombs/LilBoy.obj")).asVBO();
    public static final IModelCustom bomb_man = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/bombs/FatMan.obj")).asVBO();
    public static final IModelCustom bomb_mike = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/bombs/IvyMike.obj")).asVBO();
    public static final IModelCustom bomb_tsar = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/bombs/tsar.obj")).asVBO();
    public static final IModelCustom bomb_prototype = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/bombs/Prototype.obj")).asVBO();
    public static final IModelCustom bomb_fleija = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/bombs/Fleija.obj")).asVBO();
    public static final IModelCustom bomb_multi = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/bombs/BombGeneric.obj")).asVBO();
    //Landmines
    public static final IModelCustom mine_ap = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/bombs/ap_mine.obj")).asVBO();
    public static final IModelCustom mine_he = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/mine_he.obj")).asVBO();
    public static final IModelCustom mine_marelet = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/bombs/marelet.obj")).asVBO();
    public static final IModelCustom mine_fat = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/mine_fat.obj")).asVBO();
    public static final IModelCustom mine_naval = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/bombs/naval_mine.obj")).asVBO();
    //Derrick
    public static final IModelCustom derrick = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/derrick.obj")).asVBO();
    //Missile Parts
    public static final IModelCustom missile_pad = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/launch_pad_silo.obj")).asVBO();
    public static final IModelCustom missile_erector = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/launch_pad_erector.obj")).asVBO();
    public static final IModelCustom missile_assembly = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missile_assembly.obj")).asVBO();
    public static final IModelCustom strut = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/strut.obj")).asVBO();
    public static final IModelCustom compact_launcher = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/compact_launcher.obj")).asVBO();
    public static final IModelCustom launch_table_base = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/launch_table/launch_table_base.obj")).asVBO();
    public static final IModelCustom launch_table_large_pad = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/launch_table/launch_table_large_pad.obj")).asVBO();
    public static final IModelCustom launch_table_small_pad = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/launch_table/launch_table_small_pad.obj")).asVBO();
    public static final IModelCustom launch_table_large_scaffold_base = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/launch_table/launch_table_large_scaffold_base.obj")).asVBO();
    public static final IModelCustom launch_table_large_scaffold_connector = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/launch_table/launch_table_large_scaffold_connector.obj")).asVBO();
    public static final IModelCustom launch_table_large_scaffold_empty = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/launch_table/launch_table_large_scaffold_empty.obj")).asVBO();
    public static final IModelCustom launch_table_small_scaffold_base = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/launch_table/launch_table_small_scaffold_base.obj")).asVBO();
    public static final IModelCustom launch_table_small_scaffold_connector = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/launch_table/launch_table_small_scaffold_connector.obj")).asVBO();
    public static final IModelCustom launch_table_small_scaffold_empty = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/launch_table/launch_table_small_scaffold_empty.obj")).asVBO();
    public static final IModelCustom mp_t_10_kerosene = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missile_parts/mp_t_10_kerosene.obj")).asVBO();
    public static final IModelCustom mp_t_10_solid = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missile_parts/mp_t_10_solid.obj")).asVBO();
    public static final IModelCustom mp_t_10_xenon = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missile_parts/mp_t_10_xenon.obj")).asVBO();
    public static final IModelCustom mp_t_15_kerosene = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missile_parts/mp_t_15_kerosene.obj")).asVBO();
    public static final IModelCustom mp_t_15_kerosene_dual = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missile_parts/mp_t_15_kerosene_dual.obj")).asVBO();
    public static final IModelCustom mp_t_15_kerosene_triple = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missile_parts/mp_t_15_kerosene_triple.obj")).asVBO();
    public static final IModelCustom mp_t_15_solid = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missile_parts/mp_t_15_solid.obj")).asVBO();
    public static final IModelCustom mp_t_15_solid_hexdecuple = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missile_parts/mp_t_15_solid_hexdecuple.obj")).asVBO();
    public static final IModelCustom mp_t_15_balefire_short = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missile_parts/mp_t_15_balefire_short.obj")).asVBO();
    public static final IModelCustom mp_t_15_balefire = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missile_parts/mp_t_15_balefire.obj")).asVBO();
    public static final IModelCustom mp_t_15_balefire_large = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missile_parts/mp_t_15_balefire_large.obj")).asVBO();
    public static final IModelCustom mp_t_20_kerosene = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missile_parts/mp_t_20_kerosene.obj")).asVBO();
    public static final IModelCustom mp_t_20_kerosene_dual = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missile_parts/mp_t_20_kerosene_dual.obj")).asVBO();
    public static final IModelCustom mp_t_20_kerosene_triple = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missile_parts/mp_t_20_kerosene_triple.obj")).asVBO();
    public static final IModelCustom mp_t_20_solid = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missile_parts/mp_t_20_solid.obj")).asVBO();
    public static final IModelCustom mp_t_20_solid_multi = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missile_parts/mp_t_20_solid_multi.obj")).asVBO();
    public static final IModelCustom mp_s_10_flat = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missile_parts/mp_s_10_flat.obj")).asVBO();
    public static final IModelCustom mp_s_10_cruise = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missile_parts/mp_s_10_cruise.obj")).asVBO();
    public static final IModelCustom mp_s_10_space = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missile_parts/mp_s_10_space.obj")).asVBO();
    public static final IModelCustom mp_s_15_flat = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missile_parts/mp_s_15_flat.obj")).asVBO();
    public static final IModelCustom mp_s_15_thin = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missile_parts/mp_s_15_thin.obj")).asVBO();
    public static final IModelCustom mp_s_15_soyuz = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missile_parts/mp_s_15_soyuz.obj")).asVBO();
    public static final IModelCustom mp_f_10_kerosene = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missile_parts/mp_f_10_kerosene.obj")).asVBO();
    public static final IModelCustom mp_f_10_long_kerosene = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missile_parts/mp_f_10_long_kerosene.obj")).asVBO();
    public static final IModelCustom mp_f_10_15_kerosene = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missile_parts/mp_f_10_15_kerosene.obj")).asVBO();
    public static final IModelCustom mp_f_15_kerosene = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missile_parts/mp_f_15_kerosene.obj")).asVBO();
    public static final IModelCustom mp_f_15_hydrogen = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missile_parts/mp_f_15_hydrogen.obj")).asVBO();
    public static final IModelCustom mp_f_15_20_kerosene = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missile_parts/mp_f_15_20_kerosene.obj")).asVBO();
    public static final IModelCustom mp_w_10_he = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missile_parts/mp_w_10_he.obj")).asVBO();
    public static final IModelCustom mp_w_10_incendiary = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missile_parts/mp_w_10_incendiary.obj")).asVBO();
    public static final IModelCustom mp_w_10_buster = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missile_parts/mp_w_10_buster.obj")).asVBO();
    public static final IModelCustom mp_w_10_nuclear = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missile_parts/mp_w_10_nuclear.obj")).asVBO();
    public static final IModelCustom mp_w_10_nuclear_large = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missile_parts/mp_w_10_nuclear_large.obj")).asVBO();
    public static final IModelCustom mp_w_10_taint = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missile_parts/mp_w_10_taint.obj")).asVBO();
    public static final IModelCustom mp_w_15_he = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missile_parts/mp_w_15_he.obj")).asVBO();
    public static final IModelCustom mp_w_15_incendiary = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missile_parts/mp_w_15_incendiary.obj")).asVBO();
    public static final IModelCustom mp_w_15_nuclear = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missile_parts/mp_w_15_nuclear.obj")).asVBO();
    public static final IModelCustom mp_w_15_boxcar = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missile_parts/mp_w_15_boxcar.obj")).asVBO();
    public static final IModelCustom mp_w_15_n2 = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missile_parts/mp_w_15_n2.obj")).asVBO();
    public static final IModelCustom mp_w_15_balefire = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missile_parts/mp_w_15_balefire.obj")).asVBO();
    public static final IModelCustom mp_w_15_turbine = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missile_parts/mp_w_15_turbine.obj")).asVBO();
    public static final IModelCustom mp_w_15_mirv = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missile_parts/mp_w_15_mirv.obj")).asVBO();
    public static final IModelCustom mp_w_fairing = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/missile_parts/mp_w_fairing.obj")).asVBO();
    //Anti Mass Spectrometer
    public static final IModelCustom ams_base = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/ams_base.obj")).asVBO();
    public static final IModelCustom ams_emitter = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/ams_emitter.obj")).asVBO();
    public static final IModelCustom ams_emitter_destroyed = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/ams_emitter_destroyed.obj")).asVBO();
    public static final IModelCustom ams_limiter = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/ams_limiter.obj")).asVBO();
    public static final IModelCustom ams_limiter_destroyed = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/ams_limiter_destroyed.obj")).asVBO();
    //Projectiles
    public static final IModelCustom projectiles = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/projectiles/projectiles.obj")).asVBO();
    public static final IModelCustom casings = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/effect/casings.obj")).asVBO();
    public static final IModelCustom rbmk_crane_console = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/rbmk/crane_console.obj")).asVBO();
    public static final IModelCustom rbmk_crane = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/rbmk/crane.obj")).asVBO();
    public static final IModelCustom rbmk_autoloader = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/rbmk/autoloader.obj")).asVBO();
    public static final ResourceLocation rbmk_crane_console_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/crane_console.png");
    public static final ResourceLocation rbmk_crane_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/rbmk_crane.png");
    public static final ResourceLocation rbmk_autoloader_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/rbmk_autoloader.png");
    public static final ResourceLocation mini_nuke_tex = new ResourceLocation(Tags.MODID, "textures/models/projectiles/mini_nuke.png");
    public static final IModelCustom rbmk_element = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/rbmk/rbmk_element.obj")).asVBO();
    public static final IModelCustom rbmk_reflector = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/rbmk/rbmk_reflector.obj")).asVBO();
    public static final IModelCustom rbmk_rods = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/rbmk/rbmk_rods.obj")).asVBO();
    public static final IModelCustom rbmk_console = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/rbmk/rbmk_console.obj")).asVBO();
    public static final IModelCustom rbmk_debris = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/rbmk/debris.obj")).asVBO();
    public static final ResourceLocation rbmk_console_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/rbmk_control.png");
    public static final IModelCustom hev_battery = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/blocks/battery.obj")).asVBO();
    public static final IModelCustom anvil = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/blocks/anvil.obj")).asVBO();
    public static final IModelCustom charge_dynamite = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/blocks/charge_dynamite.obj")).asVBO();
    public static final IModelCustom charge_c4 = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/blocks/charge_c4.obj")).asVBO();
    //RBMK DEBRIS
    public static final IModelCustom deb_blank = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/projectiles/deb_blank.obj")).asVBO();
    public static final IModelCustom deb_element = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/projectiles/deb_element.obj")).asVBO();
    public static final IModelCustom deb_fuel = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/projectiles/deb_fuel.obj")).asVBO();
    public static final IModelCustom deb_rod = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/projectiles/deb_rod.obj")).asVBO();
    public static final IModelCustom deb_lid = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/projectiles/deb_lid.obj")).asVBO();
    public static final IModelCustom deb_graphite = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/projectiles/deb_graphite.obj")).asVBO();
    //Zirnox debris
    public static final IModelCustom deb_zirnox_blank = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/zirnox/deb_blank.obj")).asVBO();
    public static final IModelCustom deb_zirnox_concrete = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/zirnox/deb_concrete.obj")).asVBO();
    public static final IModelCustom deb_zirnox_element = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/zirnox/deb_element.obj")).asVBO();
    public static final IModelCustom deb_zirnox_exchanger = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/zirnox/deb_exchanger.obj")).asVBO();
    public static final IModelCustom deb_zirnox_shrapnel = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/zirnox/deb_shrapnel.obj")).asVBO();
    //SatDock
    public static final IModelCustom satDock = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/sat_dock.obj")).asVBO();
    //Solar Tower
    public static final IModelCustom solar_boiler = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/solar_boiler.obj")).asVBO();
    public static final IModelCustom solar_mirror = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/solar_mirror.obj")).asVBO();
    //Drain
    public static final IModelCustom drain = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/drain.obj")).asVBO();
    //Watz
    public static final ResourceLocation watz_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/watz.png");
    public static final ResourceLocation watz_pump_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/watz_pump.png");
    //Radar
    public static final IModelCustom radar_body = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/radar_base.obj")).asVBO();
    public static final IModelCustom radar = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/radar.obj")).asVBO();
    public static final IModelCustom radar_large = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/radar_large.obj")).asVBO();
    public static final IModelCustom radar_screen = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/radar_screen.obj")).asVBO();
    //ITER
    public static final ResourceLocation iter_glass = new ResourceLocation(Tags.MODID, "textures/models/iter/glass.png");
    public static final ResourceLocation iter_microwave = new ResourceLocation(Tags.MODID, "textures/models/iter/microwave.png");
    public static final ResourceLocation iter_motor = new ResourceLocation(Tags.MODID, "textures/models/iter/motor.png");
    public static final ResourceLocation iter_plasma = new ResourceLocation(Tags.MODID, "textures/models/iter/plasma.png");
    public static final ResourceLocation iter_rails = new ResourceLocation(Tags.MODID, "textures/models/iter/rails.png");
    public static final ResourceLocation iter_solenoid = new ResourceLocation(Tags.MODID, "textures/models/iter/solenoid.png");
    public static final ResourceLocation iter_toroidal = new ResourceLocation(Tags.MODID, "textures/models/iter/toroidal.png");
    public static final ResourceLocation iter_torus = new ResourceLocation(Tags.MODID, "textures/models/iter/torus.png");
    public static final ResourceLocation iter_torus_tungsten = new ResourceLocation(Tags.MODID, "textures/models/iter/torus_tungsten.png");
    public static final ResourceLocation iter_torus_desh = new ResourceLocation(Tags.MODID, "textures/models/iter/torus_desh.png");
    public static final ResourceLocation iter_torus_chlorophyte = new ResourceLocation(Tags.MODID, "textures/models/iter/torus_chlorophyte.png");
    public static final ResourceLocation iter_torus_vaporwave = new ResourceLocation(Tags.MODID, "textures/models/iter/torus_vaporwave.png");
    public static final ResourceLocation fusion_torus_tex = new ResourceLocation(Tags.MODID, "textures/models/fusion/torus.png");
    public static final ResourceLocation fusion_plasma_tex = new ResourceLocation(Tags.MODID, "textures/models/fusion/plasma.png");
    public static final ResourceLocation fusion_plasma_glow_tex = new ResourceLocation(Tags.MODID, "textures/models/fusion/plasma_glow.png");
    public static final ResourceLocation fusion_plasma_sparkle_tex = new ResourceLocation(Tags.MODID, "textures/models/fusion/plasma_sparkle.png");
    public static final ResourceLocation fusion_klystron_tex = new ResourceLocation(Tags.MODID, "textures/models/fusion/klystron.png");
    public static final ResourceLocation fusion_breeder_tex = new ResourceLocation(Tags.MODID, "textures/models/fusion/breeder.png");
    public static final ResourceLocation fusion_collector_tex = new ResourceLocation(Tags.MODID, "textures/models/fusion/collector.png");
    public static final ResourceLocation fusion_boiler_tex = new ResourceLocation(Tags.MODID, "textures/models/fusion/boiler.png");
    public static final ResourceLocation fusion_mhdt_tex = new ResourceLocation(Tags.MODID, "textures/models/fusion/mhdt.png");
    public static final ResourceLocation fusion_coupler_tex = new ResourceLocation(Tags.MODID, "textures/models/fusion/coupler.png");

    //ICF
    public static final ResourceLocation icf_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/icf.png");
    //Strand Caster
    public static final ResourceLocation strand_caster_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/strand_caster.png");
    //Industrial Boiler
    public static final ResourceLocation boiler_industrial_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/industrial_boiler.png");
    public static final ResourceLocation hephaestus_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/hephaestus.png");
    //FENSU
    public static final ResourceLocation battery_socket_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/battery_socket.png");
    public static final ResourceLocation battery_sc_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/battery_sc.png");
    public static final ResourceLocation battery_redd_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/fensu2.png");
    public static final ResourceLocation[] fensu_tex = new ResourceLocation[]{
            new ResourceLocation(Tags.MODID, "textures/models/machines/fensus/fensu_white.png"),
            new ResourceLocation(Tags.MODID, "textures/models/machines/fensus/fensu_orange.png"),
            new ResourceLocation(Tags.MODID, "textures/models/machines/fensus/fensu_magenta.png"),
            new ResourceLocation(Tags.MODID, "textures/models/machines/fensus/fensu_light_blue.png"),
            new ResourceLocation(Tags.MODID, "textures/models/machines/fensus/fensu_yellow.png"),
            new ResourceLocation(Tags.MODID, "textures/models/machines/fensus/fensu_lime.png"),
            new ResourceLocation(Tags.MODID, "textures/models/machines/fensus/fensu_pink.png"),
            new ResourceLocation(Tags.MODID, "textures/models/machines/fensus/fensu_gray.png"),
            new ResourceLocation(Tags.MODID, "textures/models/machines/fensus/fensu_light_gray.png"),
            new ResourceLocation(Tags.MODID, "textures/models/machines/fensus/fensu_cyan.png"),
            new ResourceLocation(Tags.MODID, "textures/models/machines/fensus/fensu_purple.png"),
            new ResourceLocation(Tags.MODID, "textures/models/machines/fensus/fensu_blue.png"),
            new ResourceLocation(Tags.MODID, "textures/models/machines/fensus/fensu_brown.png"),
            new ResourceLocation(Tags.MODID, "textures/models/machines/fensus/fensu_green.png"),
            new ResourceLocation(Tags.MODID, "textures/models/machines/fensus/fensu_red.png"),
            new ResourceLocation(Tags.MODID, "textures/models/machines/fensus/fensu_black.png")
    };
    public static final ResourceLocation jshotgun_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/jade_shotgun.png");
    public static final ResourceLocation jshotgun_lmap = new ResourceLocation(Tags.MODID, "textures/models/weapons/jade_shotgun_lmap.png");
    //Forcefield
    public static final IModelCustom forcefield_top = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/forcefield_top.obj")).asVBO();
    //Shimmer Sledge
    public static final IModelCustom shimmer_sledge = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/shimmer_sledge.obj")).asVBO();
    public static final IModelCustom shimmer_axe = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/shimmer_axe.obj")).asVBO();
    public static final IModelCustom stopsign = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/stopsign.obj")).asVBO();
    public static final IModelCustom gavel = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/gavel.obj")).asVBO();
    public static final IModelCustom crucible = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/crucible.obj")).asVBO();
    public static final IModelCustom boltgun = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/boltgun.obj")).asVBO();
    public static final IModelCustom n_i_4_n_i = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/n_i_4_n_i.obj")).asVBO();
    public static final HashMap<String, BusAnimationSedna> python_anim = AnimationLoader.load(new ResourceLocation(Tags.MODID, "models/weapons/animations/python.json"));
    public static final HashMap<String, BusAnimationSedna> cursed_anim = AnimationLoader.load(new ResourceLocation(Tags.MODID, "models/weapons/animations/cursed.json"));
    public static final HashMap<String, BusAnimationSedna> novac_anim = AnimationLoader.load(new ResourceLocation(Tags.MODID, "models/weapons/animations/novac.json"));
    public static final HashMap<String, BusAnimationSedna> ks23_anim = AnimationLoader.load(new ResourceLocation(Tags.MODID, "models/weapons/animations/ks23.json"));
    public static final HashMap<String, BusAnimationSedna> spas_12_anim = AnimationLoader.load(new ResourceLocation(Tags.MODID, "models/weapons/animations/spas12.json"));
    public static final HashMap<String, BusAnimationSedna> supershotty_anim = AnimationLoader.load(new ResourceLocation(Tags.MODID, "models/weapons/animations/supershotty.json"));
    public static final HashMap<String, BusAnimationSedna> benelli_anim = AnimationLoader.load(new ResourceLocation(Tags.MODID, "models/weapons/animations/benelli.json"));
    public static final HashMap<String, BusAnimationSedna> congolake_anim = AnimationLoader.load(new ResourceLocation(Tags.MODID, "models/weapons/animations/congolake.json"));
    public static final HashMap<String, BusAnimationSedna> am180_anim = AnimationLoader.load(new ResourceLocation(Tags.MODID, "models/weapons/animations/am180.json"));
    public static final HashMap<String, BusAnimationSedna> flamethrower_anim = AnimationLoader.load(new ResourceLocation(Tags.MODID, "models/weapons/animations/flamethrower.json"));
    public static final HashMap<String, BusAnimationSedna> stg77_anim = AnimationLoader.load(new ResourceLocation(Tags.MODID, "models/weapons/animations/stg77.json"));
    public static final HashMap<String, BusAnimationSedna> lag_anim = AnimationLoader.load(new ResourceLocation(Tags.MODID, "models/weapons/animations/lag.json"));
    // Control panel
    public static final IModelCustom control_panel_custom = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/control_panel/control_panel_custom.obj")).asVBO();
    public static final IModelCustom control_panel_front = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/control_panel/control_panel_front.obj")).asVBO();
    public static final ResourceLocation control_panel_custom_tex = new ResourceLocation(Tags.MODID, "textures/models/misc/control_panel.png");
    // Controls
    public static final IModelCustom ctrl_button_push = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/control_panel/button_push.obj")).asVBO();
    public static final IModelCustom ctrl_button_emergency_push = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/control_panel/button_emergency_push.obj")).asVBO();
    public static final IModelCustom ctrl_button_encased_push = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/control_panel/button_encased_push.obj")).asVBO();
    public static final IModelCustom ctrl_switch_toggle = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/control_panel/switch_toggle.obj")).asVBO();
    public static final IModelCustom ctrl_switch_rotary_toggle = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/control_panel/switch_rotary_toggle.obj")).asVBO();
    public static final IModelCustom ctrl_display_seven_seg = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/control_panel/display_seven_seg.obj")).asVBO();
    public static final IModelCustom ctrl_indicator_lamp = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/control_panel/indicator_lamp.obj")).asVBO();
    public static final IModelCustom ctrl_slider_vertical = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/control_panel/slider_vertical.obj")).asVBO();
    public static final IModelCustom ctrl_knob_control = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/control_panel/knob_control.obj")).asVBO();
    public static final IModelCustom ctrl_dial_square = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/control_panel/dial_square.obj")).asVBO();
    public static final IModelCustom ctrl_dial_large = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/control_panel/dial_large.obj")).asVBO();
    public static final ResourceLocation ctrl_button_push_tex = new ResourceLocation(Tags.MODID, "textures/models/control_panel/button_push.png");
    public static final ResourceLocation ctrl_button_emergency_push_tex = new ResourceLocation(Tags.MODID, "textures/models/control_panel/button_emergency_push.png");
    public static final ResourceLocation ctrl_button_encased_push_tex = new ResourceLocation(Tags.MODID, "textures/models/control_panel/button_encased_push.png");
    public static final ResourceLocation ctrl_switch_toggle_tex = new ResourceLocation(Tags.MODID, "textures/models/control_panel/switch_toggle.png");
    public static final ResourceLocation ctrl_switch_rotary_toggle_tex = new ResourceLocation(Tags.MODID, "textures/models/control_panel/switch_rotary_toggle.png");
    public static final ResourceLocation ctrl_display_seven_seg_tex = new ResourceLocation(Tags.MODID, "textures/models/control_panel/display_7seg.png");
    public static final ResourceLocation ctrl_slider_vertical_tex = new ResourceLocation(Tags.MODID, "textures/models/control_panel/slider_vertical.png");
    public static final ResourceLocation ctrl_knob_control_tex = new ResourceLocation(Tags.MODID, "textures/models/control_panel/knob_control.png");
    public static final ResourceLocation ctrl_dial_square_tex = new ResourceLocation(Tags.MODID, "textures/models/control_panel/dial_square.png");
    public static final ResourceLocation ctrl_dial_large_tex = new ResourceLocation(Tags.MODID, "textures/models/control_panel/dial_large.png");
    public static final ResourceLocation ctrl_button_push_gui_tex = new ResourceLocation(Tags.MODID, "textures/models/control_panel/button_push_gui.png");
    public static final ResourceLocation ctrl_button_emergency_push_gui_tex = new ResourceLocation(Tags.MODID, "textures/models/control_panel/button_emergency_push_gui.png");
    public static final ResourceLocation ctrl_button_encased_push_gui_tex = new ResourceLocation(Tags.MODID, "textures/models/control_panel/button_encased_push_gui.png");
    public static final ResourceLocation ctrl_switch_toggle_gui_tex = new ResourceLocation(Tags.MODID, "textures/models/control_panel/switch_toggle_gui.png");
    public static final ResourceLocation ctrl_switch_rotary_toggle_gui_tex = new ResourceLocation(Tags.MODID, "textures/models/control_panel/switch_rotary_toggle_gui.png");
    public static final ResourceLocation ctrl_display_seven_seg_gui_tex = new ResourceLocation(Tags.MODID, "textures/models/control_panel/display_7seg_gui.png");
    public static final ResourceLocation ctrl_indicator_lamp_gui_tex = new ResourceLocation(Tags.MODID, "textures/models/control_panel/indicator_lamp_gui.png");
    public static final ResourceLocation ctrl_slider_vertical_gui_tex = new ResourceLocation(Tags.MODID, "textures/models/control_panel/slider_vertical_gui.png");
    public static final ResourceLocation ctrl_knob_control_gui_tex = new ResourceLocation(Tags.MODID, "textures/models/control_panel/knob_control_gui.png");
    public static final ResourceLocation ctrl_dial_square_gui_tex = new ResourceLocation(Tags.MODID, "textures/models/control_panel/dial_square_gui.png");
    public static final ResourceLocation ctrl_dial_large_gui_tex = new ResourceLocation(Tags.MODID, "textures/models/control_panel/dial_large_gui.png");
    //Textures for conveyors/cranes custom models
    public static final ResourceLocation splitter_tex = new ResourceLocation(Tags.MODID, "textures/models/network/splitter.png");
    public static final ResourceLocation universal = new ResourceLocation(Tags.MODID, "textures/models/misc/universaldark.png");
    //Freons Turrets Textures
    public static final ResourceLocation turret_flamethower_tex = new ResourceLocation(Tags.MODID, "textures/models/turrets/freons/turret_flamethower.png");
    public static final ResourceLocation turret_cannon_tex = new ResourceLocation(Tags.MODID, "textures/models/turrets/freons/turret_cannon.png");
    public static final ResourceLocation turret_tau_tex = new ResourceLocation(Tags.MODID, "textures/models/turrets/freons/turret_tau.png");
    public static final ResourceLocation turret_mg_tex = new ResourceLocation(Tags.MODID, "textures/models/turrets/freons/turret_mg.png");
    public static final ResourceLocation turret_rocket_launcher_tex = new ResourceLocation(Tags.MODID, "textures/models/turrets/freons/turret_rocket_launcher.png");
    //Old turrets
    public static final ResourceLocation turret_arty_tex = new ResourceLocation(Tags.MODID, "textures/models/turrets/arty.png");
    public static final ResourceLocation turret_ciws_base_tex = new ResourceLocation(Tags.MODID, "textures/models/turrets/cwis_base.png");
    public static final ResourceLocation turret_ciws_rotor_tex = new ResourceLocation(Tags.MODID, "textures/models/turrets/cwis_rotor.png");
    public static final ResourceLocation turret_ciws_head_tex = new ResourceLocation(Tags.MODID, "textures/models/turrets/cwis_head.png");
    public static final ResourceLocation turret_ciws_gun_tex = new ResourceLocation(Tags.MODID, "textures/models/turrets/cwis_gun.png");
    public static final ResourceLocation turret_cheapo_base_tex = new ResourceLocation(Tags.MODID, "textures/models/turrets/turret_cheapo_base.png");
    public static final ResourceLocation turret_cheapo_rotor_tex = new ResourceLocation(Tags.MODID, "textures/models/turrets/turret_cheapo_rotor.png");
    public static final ResourceLocation turret_cheapo_head_tex = new ResourceLocation(Tags.MODID, "textures/models/turrets/turret_cheapo_head.png");
    public static final ResourceLocation turret_cheapo_gun_tex = new ResourceLocation(Tags.MODID, "textures/models/turrets/turret_cheapo_gun.png");
    public static final ResourceLocation turret_base_tex = new ResourceLocation(Tags.MODID, "textures/models/turrets/base.png");
    public static final ResourceLocation turret_base_friendly_tex = new ResourceLocation(Tags.MODID, "textures/models/turrets/base_friendly.png");
    public static final ResourceLocation turret_carriage_tex = new ResourceLocation(Tags.MODID, "textures/models/turrets/carriage.png");
    public static final ResourceLocation turret_carriage_ciws_tex = new ResourceLocation(Tags.MODID, "textures/models/turrets/carriage_ciws.png");
    public static final ResourceLocation turret_carriage_friendly_tex = new ResourceLocation(Tags.MODID, "textures/models/turrets/carriage_friendly.png");
    public static final ResourceLocation turret_connector_tex = new ResourceLocation(Tags.MODID, "textures/models/turrets/connector.png");
    public static final ResourceLocation turret_chekhov_tex = new ResourceLocation(Tags.MODID, "textures/models/turrets/chekhov.png");
    public static final ResourceLocation turret_chekhov_barrels_tex = new ResourceLocation(Tags.MODID, "textures/models/turrets/chekhov_barrels.png");

    //Textures TEs
    public static final ResourceLocation turret_jeremy_tex = new ResourceLocation(Tags.MODID, "textures/models/turrets/jeremy.png");
    public static final ResourceLocation turret_tauon_tex = new ResourceLocation(Tags.MODID, "textures/models/turrets/tauon.png");
    public static final ResourceLocation turret_richard_tex = new ResourceLocation(Tags.MODID, "textures/models/turrets/richard.png");
    public static final ResourceLocation turret_howard_tex = new ResourceLocation(Tags.MODID, "textures/models/turrets/howard.png");
    public static final ResourceLocation turret_howard_barrels_tex = new ResourceLocation(Tags.MODID, "textures/models/turrets/howard_barrels.png");
    public static final ResourceLocation turret_maxwell_tex = new ResourceLocation(Tags.MODID, "textures/models/turrets/maxwell.png");
    public static final ResourceLocation turret_fritz_tex = new ResourceLocation(Tags.MODID, "textures/models/turrets/fritz.png");
    public static final ResourceLocation turret_himars_tex = new ResourceLocation(Tags.MODID, "textures/models/turrets/himars.png");
    public static final ResourceLocation turret_sentry_tex = new ResourceLocation(Tags.MODID, "textures/models/turrets/sentry.png");
    public static final ResourceLocation turret_sentry_damaged_tex = new ResourceLocation(Tags.MODID, "textures/models/turrets/sentry_damaged.png");
    public static final ResourceLocation himars_standard_tex = new ResourceLocation(Tags.MODID, "textures/models/projectiles/himars_standard.png");
    public static final ResourceLocation turret_base_rusted = new ResourceLocation(Tags.MODID, "textures/models/turrets/rusted/base.png");
    public static final ResourceLocation turret_carriage_ciws_rusted = new ResourceLocation(Tags.MODID, "textures/models/turrets/rusted/carriage_ciws.png");
    public static final ResourceLocation turret_howard_rusted = new ResourceLocation(Tags.MODID, "textures/models/turrets/rusted/howard.png");
    public static final ResourceLocation turret_howard_barrels_rusted = new ResourceLocation(Tags.MODID, "textures/models/turrets/rusted/howard_barrels.png");
    public static final ResourceLocation mine_ap_grass_tex = new ResourceLocation(Tags.MODID, "textures/models/bombs/mine_ap_grass.png");
    public static final ResourceLocation mine_ap_desert_tex = new ResourceLocation(Tags.MODID, "textures/models/bombs/mine_ap_desert.png");
    public static final ResourceLocation mine_ap_snow_tex = new ResourceLocation(Tags.MODID, "textures/models/bombs/mine_ap_snow.png");
    public static final ResourceLocation mine_ap_stone_tex = new ResourceLocation(Tags.MODID, "textures/models/bombs/mine_ap_stone.png");
    public static final ResourceLocation mine_marelet_tex = new ResourceLocation(Tags.MODID, "textures/models/explosion/mine_marelet.png");
    public static final ResourceLocation mine_shrap_tex = new ResourceLocation(Tags.MODID, "textures/models/bombs/mine_shrapnel.png");
    public static final ResourceLocation mine_fat_tex = new ResourceLocation(Tags.MODID, "textures/models/explosion/mine_fat.png");
    public static final ResourceLocation mine_naval_tex = new ResourceLocation(Tags.MODID, "textures/models/bombs/nmine.png");
    //Derrick
    public static final ResourceLocation derrick_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/derrick.png");
    //Pumpjack
    public static final ResourceLocation pumpjack_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/pumpjack.png");
    public static final ResourceLocation fracking_tower_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/fracking_tower.png");
    //TELEX
    public static final ResourceLocation telex_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/telex.png");
    //Refinery
    public static final ResourceLocation refinery_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/refinery.png");
    public static final ResourceLocation vacuum_distill_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/vacuum_distill.png");
    public static final ResourceLocation fraction_tower_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/fraction_tower.png");
    public static final ResourceLocation fraction_spacer_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/fraction_spacer.png");
    public static final ResourceLocation cracking_tower_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/cracking_tower.png");
    public static final ResourceLocation catalytic_reformer_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/catalytic_reformer.png");
    public static final ResourceLocation hydrotreater_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/hydrotreater.png");
    public static final ResourceLocation liquefactor_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/liquefactor.png");
    public static final ResourceLocation solidifier_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/solidifier.png");
    public static final ResourceLocation coker_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/coker.png");
    public static final ResourceLocation compressor_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/compressor.png");

    //Landmines
    public static final ResourceLocation pyrooven_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/pyrooven.png");
    //Flare Stack
    public static final ResourceLocation oilflare_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/flare_stack.png");
    public static final ResourceLocation chimney_brick_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/chimney_brick.png");
    public static final ResourceLocation chimney_industrial_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/chimney_industrial.png");
    //Tank
    public static final ResourceLocation tank_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/tank.png");
    public static final ResourceLocation tank_inner_tex = new ResourceLocation(Tags.MODID, "textures/models/tank/tank_inner.png");
    public static final ResourceLocation tank_label_tex = new ResourceLocation(Tags.MODID, "textures/models/tank/tank_NONE.png");
    public static final ResourceLocation bat9000_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/bat9000.png");
    public static final ResourceLocation orbus_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/orbus.png");
    //Cable
    public static final ResourceLocation cable_neo_tex = new ResourceLocation(Tags.MODID, "textures/blocks/cable_neo.png");
    //ZIRNOX
    public static final ResourceLocation zirnox_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/zirnox.png");
    public static final ResourceLocation zirnox_destroyed_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/zirnox_destroyed.png");
    //Radiolysis
    public static final ResourceLocation radiolysis_tex = new ResourceLocation(Tags.MODID, "textures/models/radiolysis.png");
    //Rotary Furnace
    public static final ResourceLocation rotary_furnace_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/rotary_furnace.png");
    //Electrolyser
    public static final ResourceLocation electrolyser_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/electrolyser.png");
    //Charger
    public static final ResourceLocation charger_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/charger.png");
    public static final ResourceLocation refueler_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/refueler.png");
    //DecoContainer
    public static final ResourceLocation file_cabinet_tex = new ResourceLocation(Tags.MODID, "textures/models/file_cabinet.png");
    public static final ResourceLocation file_cabinet_steel_tex = new ResourceLocation(Tags.MODID, "textures/models/file_cabinet_steel.png");
    //Large Cable
    public static final ResourceLocation pylon_large_tex = new ResourceLocation(Tags.MODID, "textures/models/network/pylon_large.png");
    public static final ResourceLocation substation_tex = new ResourceLocation(Tags.MODID, "textures/models/network/substation.png");
    public static final ResourceLocation wire_tex = new ResourceLocation(Tags.MODID, "textures/models/network/wire.png");
    public static final ResourceLocation wire_greyscale_tex = new ResourceLocation(Tags.MODID, "textures/models/network/wire_greyscale.png");
    //Pipes
    public static final ResourceLocation pipe_anchor_tex = new ResourceLocation(Tags.MODID, "textures/models/network/pipe_anchor.png");
    public static final ResourceLocation fluid_pump_tex = new ResourceLocation(Tags.MODID, "textures/models/network/fluid_diode.png");

    public static final ResourceLocation pylon_medium_tex = new ResourceLocation(Tags.MODID, "textures/models/network/pylon_medium.png");
    public static final ResourceLocation pylon_medium_steel_tex = new ResourceLocation(Tags.MODID, "textures/models/network/pylon_medium_steel.png");
    public static final ResourceLocation connector_tex = new ResourceLocation(Tags.MODID, "textures/models/network/connector.png");
    //Pipe
    public static final ResourceLocation pipe_neo_tex = new ResourceLocation(Tags.MODID, "textures/blocks/pipe_neo.png");
    public static final ResourceLocation pipe_neo_succ_tex = new ResourceLocation(Tags.MODID, "textures/blocks/pipe_neo_succ.png");
    //Turbofan
    public static final ResourceLocation turbofan_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/turbofan.png");
    public static final ResourceLocation turbofan_back_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/turbofan_back.png");
    public static final ResourceLocation turbofan_afterburner_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/turbofan_afterburner.png");
    public static final ResourceLocation turbofan_blades_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/turbofan_blades.png");
    //Pumps
    public static final ResourceLocation pump_steam_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/pump_steam.png");
    public static final ResourceLocation pump_electric_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/pump_electric.png");
    //Large Turbine
    public static final ResourceLocation steam_engine_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/steam_engine.png");
    public static final ResourceLocation turbine_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/turbine.png");
    public static final ResourceLocation chungus_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/chungus.png");
    //Cooling Tower
    public static final ResourceLocation tower_small_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/tower_small.png");
    public static final ResourceLocation tower_large_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/tower_large.png");
    public static final ResourceLocation condenser_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/condenser.png");
    public static final ResourceLocation intake_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/intake.png");
    public static final ResourceLocation compressor_compact_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/compressor_compact.png");
    //IGen
    public static final ResourceLocation igen_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/igen.png");
    public static final ResourceLocation igen_rotor = new ResourceLocation(Tags.MODID, "textures/models/machines/igen_rotor.png");
    public static final ResourceLocation igen_cog = new ResourceLocation(Tags.MODID, "textures/models/machines/igen_cog.png");
    public static final ResourceLocation igen_arm = new ResourceLocation(Tags.MODID, "textures/models/machines/igen_arm.png");
    public static final ResourceLocation igen_pistons = new ResourceLocation(Tags.MODID, "textures/models/machines/igen_pistons.png");
    //Combustion Engine
    public static final ResourceLocation combustion_engine_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/combustion_engine.png");
    //Firebox and the lot
    public static final ResourceLocation sawmill_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/sawmill.png");
    public static final ResourceLocation crucible_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/crucible_heat.png");
    public static final ResourceLocation heater_firebox_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/firebox.png");
    public static final ResourceLocation heater_oven_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/heating_oven.png");
    public static final ResourceLocation ashpit_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/ashpit.png");
    public static final ResourceLocation heater_oilburner_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/oilburner.png");
    public static final ResourceLocation boiler_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/boiler.png");
    public static final ResourceLocation heater_electric_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/electric_heater.png");
    public static final ResourceLocation heater_heatex_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/heater_heatex.png");
    //Furnaces
    public static final ResourceLocation furnace_iron_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/furnace_iron.png");
    public static final ResourceLocation furnace_steel_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/furnace_steel.png");
    public static final ResourceLocation combination_oven_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/combination_oven.png");
    //Selenium Engine
    public static final ResourceLocation selenium_body_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/selenium_engine_body.png");
    public static final ResourceLocation selenium_piston_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/selenium_engine_piston.png");
    public static final ResourceLocation selenium_rotor_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/selenium_engine_rotor.png");
    //Press
    public static final ResourceLocation press_body_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/press_body.png");
    public static final ResourceLocation press_head_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/press_head.png");
    public static final ResourceLocation epress_body_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/epress_body.png");
    public static final ResourceLocation epress_head_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/epress_head.png");
    public static final ResourceLocation conveyor_press_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/conveyor_press.png");
    public static final ResourceLocation conveyor_press_belt_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/conveyor_press_belt.png");
    public static final ResourceLocation ammo_press_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/ammo_press.png");
    public static final ResourceLocation bm_box_lever_tex = new ResourceLocation(Tags.MODID, "textures/models/turrets/bm_box_lever.png");
    //Assembler
    public static final ResourceLocation assembler_body_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/assembler_base_new.png");
    public static final ResourceLocation assembler_cog_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/assembler_cog_new.png");
    public static final ResourceLocation assembler_slider_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/assembler_slider_new.png");
    public static final ResourceLocation assembler_arm_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/assembler_arm_new.png");
    public static final ResourceLocation assembly_machine_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/assembly_machine.png");
    public static final ResourceLocation assemfac_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/assemfac.png");
    public static final ResourceLocation assembly_factory_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/assembly_factory.png");
    public static final ResourceLocation assembly_factory_sparks_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/assembly_factory_sparks.png");
    //Chemplant
    public static final ResourceLocation chemplant_body_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/chemplant_base_new.png");
    public static final ResourceLocation chemplant_spinner_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/chemplant_spinner_new.png");
    public static final ResourceLocation chemplant_piston_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/chemplant_piston_new.png");
    public static final ResourceLocation chemplant_fluid_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/lavabase_small.png");
    public static final ResourceLocation chemical_plant_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/chemical_plant.png");
    public static final ResourceLocation chemical_plant_fluid_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/chemical_plant_fluid.png");
    public static final ResourceLocation chemfac_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/chemfac.png");
    public static final ResourceLocation chemical_factory_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/chemical_factory.png");
    public static final ResourceLocation purex_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/purex.png");
    //Arc Welder
    public static final ResourceLocation arc_welder_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/arc_welder.png");
    //Soldering Station
    public static final ResourceLocation soldering_station_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/soldering_station.png");
    //Arc Furnace
    public static final ResourceLocation arc_furnace_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/arc_furnace.png");
    //Mixer
    public static final ResourceLocation mixer_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/mixer.png");
    public static final ResourceLocation mixer_uu_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/mixer_uu.png");
    //F6 TANKS
    public static final ResourceLocation uf6_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/UF6Tank.png");
    public static final ResourceLocation puf6_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/PUF6Tank.png");
    //Centrifuge
    public static final ResourceLocation centrifuge_new_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/centrifuge_new.png");
    public static final ResourceLocation centrifuge_gas_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/centrifuge_gas.png");
    public static final ResourceLocation silex_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/silex.png");
    public static final ResourceLocation fel_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/fel.png");
    //Magnusson Device
    public static final ResourceLocation microwave_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/microwave.png");
    //Big Man Johnson
    public static final ResourceLocation autosaw_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/autosaw.png");
    //Heat engines
    public static final ResourceLocation stirling_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/stirling.png");
    public static final ResourceLocation stirling_steel_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/stirling_steel.png");
    public static final ResourceLocation stirling_creative_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/stirling_creative.png");
    //B.O.P
    public static final ResourceLocation ore_slopper_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/ore_slopper.png");
    //Wood burner gen
    public static final ResourceLocation wood_burner_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/wood_burner.png");
    //Not wood burner gen
    public static final ResourceLocation turbine_gas_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/turbinegas.png");
    //Laser Miner
    public static final ResourceLocation mining_laser_base_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/mining_laser_base.png");
    public static final ResourceLocation mining_laser_pivot_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/mining_laser_pivot.png");
    public static final ResourceLocation mining_laser_laser_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/mining_laser_laser.png");
    //Excavator
    public static final ResourceLocation excavator_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/excavator.png");
    //Crystallizer
    public static final ResourceLocation crystallizer_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/crystallizer.png");
    //Cyclotron
    public static final ResourceLocation cyclotron_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/cyclotron.png");
    public static final ResourceLocation cyclotron_ashes = new ResourceLocation(Tags.MODID, "textures/models/machines/cyclotron_ashes.png");
    public static final ResourceLocation cyclotron_ashes_filled = new ResourceLocation(Tags.MODID, "textures/models/machines/cyclotron_ashes_filled.png");
    public static final ResourceLocation cyclotron_book = new ResourceLocation(Tags.MODID, "textures/models/machines/cyclotron_book.png");
    public static final ResourceLocation cyclotron_book_filled = new ResourceLocation(Tags.MODID, "textures/models/machines/cyclotron_book_filled.png");
    public static final ResourceLocation cyclotron_gavel = new ResourceLocation(Tags.MODID, "textures/models/machines/cyclotron_gavel.png");
    public static final ResourceLocation cyclotron_gavel_filled = new ResourceLocation(Tags.MODID, "textures/models/machines/cyclotron_gavel_filled.png");
    public static final ResourceLocation cyclotron_coin = new ResourceLocation(Tags.MODID, "textures/models/machines/cyclotron_coin.png");
    public static final ResourceLocation cyclotron_coin_filled = new ResourceLocation(Tags.MODID, "textures/models/machines/cyclotron_coin_filled.png");
    //Albion Particle Accelerator
    public static final ResourceLocation pa_source_tex = new ResourceLocation(Tags.MODID, "textures/models/particleaccelerator/source.png");
    public static final ResourceLocation pa_beamline_tex = new ResourceLocation(Tags.MODID, "textures/models/particleaccelerator/beamline.png");
    public static final ResourceLocation pa_rfc_tex = new ResourceLocation(Tags.MODID, "textures/models/particleaccelerator/rfc.png");
    public static final ResourceLocation pa_quadrupole_tex = new ResourceLocation(Tags.MODID, "textures/models/particleaccelerator/quadrupole.png");
    public static final ResourceLocation pa_dipole_tex = new ResourceLocation(Tags.MODID, "textures/models/particleaccelerator/dipole.png");
    public static final ResourceLocation pa_detector_tex = new ResourceLocation(Tags.MODID, "textures/models/particleaccelerator/detector.png");
    //Exposure Chamber
    public static final ResourceLocation exposure_chamber_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/exposure_chamber.png");
    //Waste Drum
    public static final ResourceLocation waste_drum_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/drum_gray.png");
    //RTG
    public static final ResourceLocation rtg_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/rtg.png");
    public static final ResourceLocation rtg_cell_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/rtg_cell.png");
    public static final ResourceLocation rtg_polonium_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/rtg_polonium.png");
    //Dark Matter Core
    public static final ResourceLocation dfc_emitter_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/core_emitter.png");
    public static final ResourceLocation dfc_receiver_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/core_receiver.png");
    public static final ResourceLocation dfc_injector_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/core_injector.png");
    public static final ResourceLocation dfc_stabilizer_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/core_stabilizer.png");
    //Fan
    public static final ResourceLocation fan_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/fan.png");
    //Radgen
    public static final ResourceLocation radgen_body_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/rad_gen_body.png");
    public static final ResourceLocation geiger_tex = new ResourceLocation(Tags.MODID, "textures/blocks/geiger.png");
    //Small Reactor
    public static final ResourceLocation reactor_small_base_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/reactor_small_base.png");
    public static final ResourceLocation reactor_small_rods_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/reactor_small_rods.png");
    //Breeder
    public static final ResourceLocation breeder_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/breeder.png");
    //Radar
    public static final ResourceLocation radar_body_tex = new ResourceLocation(Tags.MODID, "textures/models/radar_base.png");
    public static final ResourceLocation radar_head_tex = new ResourceLocation(Tags.MODID, "textures/models/radar_head.png");
    public static final ResourceLocation radar_base_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/radar_base.png");
    public static final ResourceLocation radar_dish_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/radar_dish.png");
    public static final ResourceLocation radar_large_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/radar_large.png");
    public static final ResourceLocation radar_screen_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/radar_screen.png");
    //Forcefield
    public static final ResourceLocation forcefield_base_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/forcefield_base.png");
    public static final ResourceLocation forcefield_top_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/forcefield_top.png");
    //Bombs
    public static final ResourceLocation bomb_solinium_tex = new ResourceLocation(Tags.MODID, "textures/models/bombs/ufp.png");
    public static final ResourceLocation n2_tex = new ResourceLocation(Tags.MODID, "textures/models/bombs/n2.png");
    public static final ResourceLocation fstbmb_tex = new ResourceLocation(Tags.MODID, "textures/models/bombs/fstbmb.png");
    public static final ResourceLocation bomb_gadget_tex = new ResourceLocation(Tags.MODID, "textures/models/bombs/gadget.png");
    public static final ResourceLocation bomb_boy_tex = new ResourceLocation(Tags.MODID, "textures/models/bombs/lilboy.png");
    public static final ResourceLocation bomb_man_tex = new ResourceLocation(Tags.MODID, "textures/models/bombs/FatMan.png");
    public static final ResourceLocation bomb_mike_tex = new ResourceLocation(Tags.MODID, "textures/models/bombs/IvyMike.png");
    public static final ResourceLocation bomb_tsar_tex = new ResourceLocation(Tags.MODID, "textures/models/bombs/Tsar.png");
    public static final ResourceLocation bomb_prototype_tex = new ResourceLocation(Tags.MODID, "textures/models/bombs/Prototype.png");
    public static final ResourceLocation bomb_fleija_tex = new ResourceLocation(Tags.MODID, "textures/models/bombs/Fleija.png");
    public static final ResourceLocation bomb_custom_tex = new ResourceLocation(Tags.MODID, "textures/models/bombs/CustomNuke.png");
    public static final ResourceLocation bomb_multi_tex = new ResourceLocation(Tags.MODID, "textures/models/bombs/BombGeneric.png");
    //Satellites
    public static final ResourceLocation sat_base_tex = new ResourceLocation(Tags.MODID, "textures/models/sat/sat_base.png");
    public static final ResourceLocation sat_radar_tex = new ResourceLocation(Tags.MODID, "textures/models/sat/sat_radar.png");
    public static final ResourceLocation sat_resonator_tex = new ResourceLocation(Tags.MODID, "textures/models/sat/sat_resonator.png");
    public static final ResourceLocation sat_scanner_tex = new ResourceLocation(Tags.MODID, "textures/models/sat/sat_scanner.png");
    public static final ResourceLocation sat_mapper_tex = new ResourceLocation(Tags.MODID, "textures/models/sat/sat_mapper.png");
    public static final ResourceLocation sat_laser_tex = new ResourceLocation(Tags.MODID, "textures/models/sat/sat_laser.png");
    public static final ResourceLocation sat_foeq_tex = new ResourceLocation(Tags.MODID, "textures/models/sat/sat_foeq.png");
    public static final ResourceLocation sat_foeq_burning_tex = new ResourceLocation(Tags.MODID, "textures/sat/models/sat_foeq_burning.png");
    //SatDock
    public static final ResourceLocation satdock_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/sat_dock.png");
    //Vault Door
    public static final ResourceLocation vault_cog_1_tex = new ResourceLocation(Tags.MODID, "textures/models/doors/vault/vault_cog_1.png");
    public static final ResourceLocation vault_cog_2_tex = new ResourceLocation(Tags.MODID, "textures/models/doors/vault/vault_cog_2.png");
    public static final ResourceLocation vault_cog_3_tex = new ResourceLocation(Tags.MODID, "textures/models/doors/vault/vault_cog_3.png");
    public static final ResourceLocation vault_cog_4_tex = new ResourceLocation(Tags.MODID, "textures/models/doors/vault/vault_cog_4.png");
    public static final ResourceLocation vault_frame_tex = new ResourceLocation(Tags.MODID, "textures/models/doors/vault/vault_frame.png");
    public static final ResourceLocation vault_label_1_tex = new ResourceLocation(Tags.MODID, "textures/models/doors/vault/vault_label_1.png");
    public static final ResourceLocation vault_label_2_tex = new ResourceLocation(Tags.MODID, "textures/models/doors/vault/vault_label_2.png");
    public static final ResourceLocation vault_label_3_tex = new ResourceLocation(Tags.MODID, "textures/models/doors/vault/vault_label_3.png");
    public static final ResourceLocation vault_label_4_tex = new ResourceLocation(Tags.MODID, "textures/models/doors/vault/vault_label_4.png");
    public static final ResourceLocation vault_label_5_tex = new ResourceLocation(Tags.MODID, "textures/models/doors/vault/vault_label_5.png");
    public static final ResourceLocation vault_label_6_tex = new ResourceLocation(Tags.MODID, "textures/models/doors/vault/vault_label_6.png");
    public static final ResourceLocation vault_label_7_tex = new ResourceLocation(Tags.MODID, "textures/models/doors/vault/vault_label_7.png");
    public static final ResourceLocation vault_label_8_tex = new ResourceLocation(Tags.MODID, "textures/models/doors/vault/vault_label_8.png");
    //Solar Tower
    public static final ResourceLocation solar_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/solar_boiler.png");
    public static final ResourceLocation solar_mirror_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/solar_mirror.png");
    //Drain
    public static final ResourceLocation drain_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/drain.png");
    //Blast Door
    public static final ResourceLocation blast_door_base_tex = new ResourceLocation(Tags.MODID, "textures/models/doors/blast/blast_door_base.png");
    public static final ResourceLocation blast_door_tooth_tex = new ResourceLocation(Tags.MODID, "textures/models/doors/blast/blast_door_tooth.png");
    public static final ResourceLocation blast_door_slider_tex = new ResourceLocation(Tags.MODID, "textures/models/doors/blast/blast_door_slider.png");
    public static final ResourceLocation blast_door_block_tex = new ResourceLocation(Tags.MODID, "textures/models/doors/blast/blast_door_block.png");
    //Sliding Blast Door
    public static final ResourceLocation sliding_blast_door_tex = new ResourceLocation(Tags.MODID, "textures/models/doors/slidingblast/sliding_blast_door.png");
    public static final ResourceLocation sliding_blast_door_variant1_tex = new ResourceLocation(Tags.MODID, "textures/models/doors/slidingblast/sliding_blast_door_variant1.png");
    public static final ResourceLocation sliding_blast_door_variant2_tex = new ResourceLocation(Tags.MODID, "textures/models/doors/slidingblast/sliding_blast_door_variant2.png");
    public static final ResourceLocation sliding_blast_door_keypad_tex = new ResourceLocation(Tags.MODID, "textures/models/doors/slidingblast/sliding_blast_door_keypad.png");
    //Doors
    public static final ResourceLocation transition_seal_tex = new ResourceLocation(Tags.MODID, "textures/models/doors/transition_seal.png");
    public static final ResourceLocation water_door_tex = new ResourceLocation(Tags.MODID, "textures/models/doors/water_door.png");
    public static final ResourceLocation large_vehicle_door_tex = new ResourceLocation(Tags.MODID, "textures/models/doors/large_vehicle_door.png");
    public static final ResourceLocation qe_containment_tex = new ResourceLocation(Tags.MODID, "textures/models/doors/qe_containment.png");
    public static final ResourceLocation qe_containment_decal = new ResourceLocation(Tags.MODID, "textures/models/doors/qe_containment_decal.png");
    public static final ResourceLocation qe_sliding_door_tex = new ResourceLocation(Tags.MODID, "textures/models/doors/qe_sliding_door.png");
    public static final ResourceLocation small_hatch_tex = new ResourceLocation(Tags.MODID, "textures/models/doors/hatch.png");
    public static final ResourceLocation fire_door_tex = new ResourceLocation(Tags.MODID, "textures/models/doors/fire_door.png");
    public static final ResourceLocation round_airlock_door_tex = new ResourceLocation(Tags.MODID, "textures/models/doors/round_airlock_door.png");
    public static final ResourceLocation secure_access_door_tex = new ResourceLocation(Tags.MODID, "textures/models/doors/secure_access_door.png");
    public static final ResourceLocation sliding_seal_door_tex = new ResourceLocation(Tags.MODID, "textures/models/doors/sliding_seal_door.png");
    public static final ResourceLocation sliding_gate_door_tex = new ResourceLocation(Tags.MODID, "textures/models/doors/sliding_gate_door.png");
    //Silo hatch
    public static final ResourceLocation hatch_tex = new ResourceLocation(Tags.MODID, "textures/models/doors/hatchtexture.png");
    //Lantern
    public static final ResourceLocation lantern_tex = new ResourceLocation(Tags.MODID, "textures/models/trinkets/lantern.png");
    public static final ResourceLocation lantern_rusty_tex = new ResourceLocation(Tags.MODID, "textures/models/trinkets/lantern_rusty.png");
    //Tesla Coil
    public static final ResourceLocation tesla_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/tesla.png");
    public static final ResourceLocation teslacrab_tex = new ResourceLocation(Tags.MODID, "textures/entity/teslacrab.png");
    public static final ResourceLocation taintcrab_tex = new ResourceLocation(Tags.MODID, "textures/entity/taintcrab.png");
    public static final ResourceLocation maskman_tex = new ResourceLocation(Tags.MODID, "textures/entity/maskman.png");
    public static final ResourceLocation iou = new ResourceLocation(Tags.MODID, "textures/entity/iou.png");
    public static final ResourceLocation ufo_tex = new ResourceLocation(Tags.MODID, "textures/entity/ufo.png");
    //Glyphids
    public static final ResourceLocation glyphid_tex = new ResourceLocation(Tags.MODID, "textures/entity/glyphid.png");
    public static final ResourceLocation glyphid_brawler_tex = new ResourceLocation(Tags.MODID, "textures/entity/glyphid_brawler.png");
    public static final ResourceLocation glyphid_behemoth_tex = new ResourceLocation(Tags.MODID, "textures/entity/glyphid_behemoth.png");
    public static final ResourceLocation glyphid_brenda_tex = new ResourceLocation(Tags.MODID, "textures/entity/glyphid_brenda.png");
    public static final ResourceLocation glyphid_bombardier_tex = new ResourceLocation(Tags.MODID, "textures/entity/glyphid_bombardier.png");
    public static final ResourceLocation glyphid_blaster_tex = new ResourceLocation(Tags.MODID, "textures/entity/glyphid_blaster.png");
    public static final ResourceLocation glyphid_scout_tex = new ResourceLocation(Tags.MODID, "textures/entity/glyphid_scout.png");
    public static final ResourceLocation glyphid_nuclear_tex = new ResourceLocation(Tags.MODID, "textures/entity/glyphid_nuclear.png");
    public static final ResourceLocation glyphid_digger_tex = new ResourceLocation(Tags.MODID, "textures/entity/glyphid_digger.png");
    /// /Obj Items

    //Shimmer Sledge
    public static final ResourceLocation detonator_laser_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/detonator_laser.png");
    /// /Texture Items

    //Shimmer Sledge
    public static final ResourceLocation shimmer_sledge_tex = new ResourceLocation(Tags.MODID, "textures/models/tools/shimmer_sledge.png");
    public static final ResourceLocation shimmer_axe_tex = new ResourceLocation(Tags.MODID, "textures/models/tools/shimmer_axe.png");
    public static final ResourceLocation stopsign_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/stopsign.png");
    public static final ResourceLocation sopsign_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/sopsign.png");
    public static final ResourceLocation chernobylsign_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/chernobylsign.png");
    public static final ResourceLocation gavel_wood = new ResourceLocation(Tags.MODID, "textures/models/weapons/gavel_wood.png");
    public static final ResourceLocation gavel_lead = new ResourceLocation(Tags.MODID, "textures/models/weapons/gavel_lead.png");
    public static final ResourceLocation gavel_diamond = new ResourceLocation(Tags.MODID, "textures/models/weapons/gavel_diamond.png");
    public static final ResourceLocation gavel_mese = new ResourceLocation(Tags.MODID, "textures/models/weapons/gavel_mese.png");
    public static final ResourceLocation crucible_hilt = new ResourceLocation(Tags.MODID, "textures/models/weapons/crucible_hilt.png");
    public static final ResourceLocation crucible_guard = new ResourceLocation(Tags.MODID, "textures/models/weapons/crucible_guard.png");
    public static final ResourceLocation crucible_blade = new ResourceLocation(Tags.MODID, "textures/models/weapons/crucible_blade.png");
    public static final ResourceLocation crucible_blade_bloom = new ResourceLocation(Tags.MODID, "textures/models/weapons/crucible_blade_bloom.png");
    public static final ResourceLocation boltgun_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/boltgun.png");
    public static final ResourceLocation hs_sword_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/hs_sword.png");
    public static final ResourceLocation hf_sword_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/hf_sword.png");
    public static final ResourceLocation stinger_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/stinger.png");
    public static final ResourceLocation remington_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/remington.png");
    public static final ResourceLocation spas_12_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/spas-12.png");
    public static final ResourceLocation glass_cannon_panel_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/glass_cannon_panel.png");
    public static final ResourceLocation chemthrower_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/chemthrower.png");
    public static final ResourceLocation novac_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/novac.png");
    public static final ResourceLocation blackjack_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/blackjack.png");
    public static final ResourceLocation lent_gun_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/lent_gun.png");
    public static final ResourceLocation red_key_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/red_key.png");
    public static final ResourceLocation m2_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/m2_browning.png");
    public static final ResourceLocation lunatic_sniper_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/lunatic_sniper.png");
    public static final ResourceLocation benelli_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/benelli_tex.png");
    public static final ResourceLocation coilgun_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/coilgun.png");
    public static final ResourceLocation cryocannon_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/cryo_cannon.png");
    public static final ResourceLocation uac_pistol_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/pistol_texture.png");
    public static final ResourceLocation congolake_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/congolake.png");
    public static final ResourceLocation debug_gun_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/debug_gun.png");
    public static final ResourceLocation pepperbox_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/pepperbox.png");
    public static final ResourceLocation bio_revolver_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/bio_revolver.png");
    public static final ResourceLocation bio_revolver_atlas_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/bio_revolver_atlas.png");
    public static final ResourceLocation dani_celestial_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/dani_celestial.png");
    public static final ResourceLocation dani_lunar_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/dani_lunar.png");
    public static final ResourceLocation henry_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/henry.png");
    public static final ResourceLocation henry_lincoln_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/henry_lincoln.png");
    public static final ResourceLocation greasegun_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/greasegun.png");
    public static final ResourceLocation greasegun_clean_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/greasegun_clean.png");
    public static final ResourceLocation maresleg_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/maresleg.png");
    public static final ResourceLocation maresleg_broken_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/maresleg_broken.png");
    public static final ResourceLocation flaregun_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/flaregun.png");
    public static final ResourceLocation heavy_revolver_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/heavy_revolver.png");
    public static final ResourceLocation heavy_revolver_protege_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/protege.png");
    public static final ResourceLocation lilmac_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/lilmac.png");
    public static final ResourceLocation lilmac_scope_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/lilmac_scope.png");
    public static final ResourceLocation carbine_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/huntsman.png");
    public static final ResourceLocation carbine_bayonet_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/carbine_bayonet.png");
    public static final ResourceLocation am180_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/am180.png");
    public static final ResourceLocation liberator_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/liberator.png");
    public static final ResourceLocation flamethrower_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/flamethrower.png");
    public static final ResourceLocation flamethrower_topaz_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/flamethrower_topaz.png");
    public static final ResourceLocation flamethrower_daybreaker_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/flamethrower_daybreaker.png");
    public static final ResourceLocation mike_hawk_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/lag.png");
    public static final ResourceLocation uzi_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/uzi.png");
    public static final ResourceLocation uzi_saturnite_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/uzi_saturnite.png");
    public static final ResourceLocation panzerschreck_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/panzerschreck.png");
    public static final ResourceLocation g3_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/g3.png");
    public static final ResourceLocation g3_zebra_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/g3_zebra.png");
    public static final ResourceLocation g3_green_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/g3_polymer_green.png");
    public static final ResourceLocation g3_black_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/g3_polymer_black.png");
    public static final ResourceLocation g3_attachments = new ResourceLocation(Tags.MODID, "textures/models/weapons/g3_attachments.png");
    public static final ResourceLocation amat_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/amat.png");
    public static final ResourceLocation amat_subtlety_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/amat_subtlety.png");
    public static final ResourceLocation amat_penance_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/amat_penance.png");
    public static final ResourceLocation shredder_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/shredder.png");
    public static final ResourceLocation shredder_orig_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/shredder_orig.png");
    public static final ResourceLocation sexy_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/sexy_real_no_fake.png");
    public static final ResourceLocation heretic_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/sexy_heretic.png");
    public static final ResourceLocation whiskey_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/whiskey.png");
    public static final ResourceLocation quadro_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/quadro.png");
    public static final ResourceLocation quadro_rocket_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/quadro_rocket.png");
    public static final ResourceLocation minigun_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/minigun.png");
    public static final ResourceLocation minigun_lacunae_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/minigun_lacunae.png");
    public static final ResourceLocation minigun_dual_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/minigun_dual.png");
    public static final ResourceLocation missile_launcher_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/missile_launcher.png");
    public static final ResourceLocation tesla_cannon_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/tesla_cannon.png");
    public static final ResourceLocation laser_pistol_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/laser_pistol.png");
    public static final ResourceLocation laser_pistol_pew_pew_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/laser_pistol_pew_pew.png");
    public static final ResourceLocation laser_pistol_morning_glory_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/laser_pistol_morning_glory.png");
    public static final ResourceLocation stg77_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/stg77.png");
    public static final ResourceLocation tau_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/tau.png");
    public static final ResourceLocation fatman_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/fatman.png");
    public static final ResourceLocation fatman_mininuke_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/fatman_mininuke.png");
    public static final ResourceLocation fatman_balefire_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/fatman_balefire.png");
    public static final ResourceLocation lasrifle_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/lasrifle.png");
    public static final ResourceLocation lasrifle_mods_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/lasrifle_mods.png");
    public static final ResourceLocation hangman_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/hangman.png");
    public static final ResourceLocation folly_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/moonlight.png");
    public static final ResourceLocation double_barrel_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/double_barrel.png");
    public static final ResourceLocation double_barrel_sacred_dragon_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/double_barrel_sacred_dragon.png");
    public static final ResourceLocation charge_thrower_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/charge_thrower.png");
    public static final ResourceLocation drill_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/drill.png");
    public static final ResourceLocation charge_thrower_hook_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/charge_thrower_hook.png");
    public static final ResourceLocation charge_thrower_mortar_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/charge_thrower_mortar.png");
    public static final ResourceLocation charge_thrower_rocket_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/charge_thrower_rocket.png");
    public static final ResourceLocation n_i_4_n_i_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/n_i_4_n_i.png");
    public static final ResourceLocation n_i_4_n_i_greyscale_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/n_i_4_n_i_greyscale.png");
    public static final ResourceLocation aberrator_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/aberrator.png");
    public static final ResourceLocation eott_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/eott.png");
    public static final ResourceLocation mas36_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/mas36.png");
    public static final ResourceLocation brimstone_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/brimstone.png");
    public static final ResourceLocation hk69_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/hk69.png");
    public static final ResourceLocation deagle_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/deagle.png");
    public static final ResourceLocation ks23_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/ks23.png");
    public static final ResourceLocation flamer_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/flamer.png");
    public static final ResourceLocation flechette_body = new ResourceLocation(Tags.MODID, "textures/models/weapons/flechette_body.png");
    public static final ResourceLocation flechette_barrel = new ResourceLocation(Tags.MODID, "textures/models/weapons/flechette_barrel.png");
    public static final ResourceLocation flechette_gren_tube = new ResourceLocation(Tags.MODID, "textures/models/weapons/flechette_gren_tube.png");
    public static final ResourceLocation flechette_grenades = new ResourceLocation(Tags.MODID, "textures/models/weapons/flechette_grenades.png");
    public static final ResourceLocation flechette_pivot = new ResourceLocation(Tags.MODID, "textures/models/weapons/flechette_pivot.png");
    public static final ResourceLocation flechette_top = new ResourceLocation(Tags.MODID, "textures/models/weapons/flechette_top.png");
    public static final ResourceLocation flechette_chamber = new ResourceLocation(Tags.MODID, "textures/models/weapons/flechette_chamber.png");
    public static final ResourceLocation flechette_base = new ResourceLocation(Tags.MODID, "textures/models/weapons/flechette_base.png");
    public static final ResourceLocation flechette_drum = new ResourceLocation(Tags.MODID, "textures/models/weapons/flechette_drum.png");
    public static final ResourceLocation flechette_trigger = new ResourceLocation(Tags.MODID, "textures/models/weapons/flechette_trigger.png");
    public static final ResourceLocation flechette_stock = new ResourceLocation(Tags.MODID, "textures/models/weapons/flechette_stock.png");
    public static final ResourceLocation sauergun_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/sauergun.png");
    public static final ResourceLocation thompson_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/thompson.png");
    public static final ResourceLocation grenade_mk2 = new ResourceLocation(Tags.MODID, "textures/models/weapons/grenade_mk2.png");
    public static final ResourceLocation grenade_aschrab_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/grenade_aschrab.png");
    public static final ResourceLocation bolter_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/bolter.png");
    public static final ResourceLocation fireext_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/fireext_normal.png");
    public static final ResourceLocation fireext_foam_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/fireext_foam.png");
    public static final ResourceLocation fireext_sand_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/fireext_sand.png");
    public static final ResourceLocation ar15_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/carbine.png");
    public static final ResourceLocation lance_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/lance.png");
    public static final ResourceLocation bj_eyepatch = new ResourceLocation(Tags.MODID, "textures/armor/bj_eyepatch.png");
    public static final ResourceLocation bj_leg = new ResourceLocation(Tags.MODID, "textures/armor/bj_leg.png");
    public static final ResourceLocation bj_chest = new ResourceLocation(Tags.MODID, "textures/armor/bj_chest.png");
    public static final ResourceLocation bj_jetpack = new ResourceLocation(Tags.MODID, "textures/armor/bj_jetpack.png");
    public static final ResourceLocation bj_arm = new ResourceLocation(Tags.MODID, "textures/armor/bj_arm.png");
    public static final ResourceLocation hev_helmet = new ResourceLocation(Tags.MODID, "textures/armor/hev_helmet.png");
    public static final ResourceLocation hev_leg = new ResourceLocation(Tags.MODID, "textures/armor/hev_leg.png");
    public static final ResourceLocation hev_chest = new ResourceLocation(Tags.MODID, "textures/armor/hev_chest.png");
    public static final ResourceLocation hev_arm = new ResourceLocation(Tags.MODID, "textures/armor/hev_arm.png");
    public static final ResourceLocation zirconium_leg = new ResourceLocation(Tags.MODID, "textures/armor/zirconium_2.png");
    public static final ResourceLocation steamsuit_helmet = new ResourceLocation(Tags.MODID, "textures/armor/steamsuit_helmet.png");
    public static final ResourceLocation steamsuit_leg = new ResourceLocation(Tags.MODID, "textures/armor/steamsuit_leg.png");
    public static final ResourceLocation steamsuit_chest = new ResourceLocation(Tags.MODID, "textures/armor/steamsuit_chest.png");
    public static final ResourceLocation steamsuit_arm = new ResourceLocation(Tags.MODID, "textures/armor/steamsuit_arm.png");
    public static final ResourceLocation dieselsuit_helmet = new ResourceLocation(Tags.MODID, "textures/armor/bnuuy_helmet.png");
    public static final ResourceLocation dieselsuit_leg = new ResourceLocation(Tags.MODID, "textures/armor/bnuuy_leg.png");
    public static final ResourceLocation dieselsuit_chest = new ResourceLocation(Tags.MODID, "textures/armor/bnuuy_chest.png");
    public static final ResourceLocation dieselsuit_arm = new ResourceLocation(Tags.MODID, "textures/armor/bnuuy_arm.png");
    public static final ResourceLocation trenchmaster_helmet = new ResourceLocation(Tags.MODID, "textures/armor/trenchmaster_helmet.png");
    public static final ResourceLocation trenchmaster_leg = new ResourceLocation(Tags.MODID, "textures/armor/trenchmaster_leg.png");
    public static final ResourceLocation trenchmaster_chest = new ResourceLocation(Tags.MODID, "textures/armor/trenchmaster_chest.png");
    public static final ResourceLocation trenchmaster_arm = new ResourceLocation(Tags.MODID, "textures/armor/trenchmaster_arm.png");
    public static final ResourceLocation t51_helmet = new ResourceLocation(Tags.MODID, "textures/armor/t51_helmet.png");
    public static final ResourceLocation t51_leg = new ResourceLocation(Tags.MODID, "textures/armor/t51_leg.png");
    public static final ResourceLocation t51_chest = new ResourceLocation(Tags.MODID, "textures/armor/t51_chest.png");
    public static final ResourceLocation t51_arm = new ResourceLocation(Tags.MODID, "textures/armor/t51_arm.png");
    public static final ResourceLocation taurun_helmet = new ResourceLocation(Tags.MODID, "textures/armor/taurun_helmet.png");
    public static final ResourceLocation taurun_leg = new ResourceLocation(Tags.MODID, "textures/armor/taurun_leg.png");
    public static final ResourceLocation taurun_chest = new ResourceLocation(Tags.MODID, "textures/armor/taurun_chest.png");
    public static final ResourceLocation taurun_arm = new ResourceLocation(Tags.MODID, "textures/armor/taurun_arm.png");
    public static final ResourceLocation bismuth_helmet = new ResourceLocation(Tags.MODID, "textures/armor/bismuth.png");
    public static final ResourceLocation bismuth_leg = new ResourceLocation(Tags.MODID, "textures/armor/bismuth.png");
    public static final ResourceLocation bismuth_chest = new ResourceLocation(Tags.MODID, "textures/armor/bismuth.png");
    public static final ResourceLocation bismuth_arm = new ResourceLocation(Tags.MODID, "textures/armor/bismuth.png");
    public static final ResourceLocation envsuit_helmet = new ResourceLocation(Tags.MODID, "textures/armor/envsuit_helmet.png");
    public static final ResourceLocation envsuit_leg = new ResourceLocation(Tags.MODID, "textures/armor/envsuit_leg.png");
    public static final ResourceLocation envsuit_chest = new ResourceLocation(Tags.MODID, "textures/armor/envsuit_chest.png");
    public static final ResourceLocation envsuit_arm = new ResourceLocation(Tags.MODID, "textures/armor/envsuit_arm.png");
    public static final ResourceLocation ajr_helmet = new ResourceLocation(Tags.MODID, "textures/armor/ajr_helmet.png");
    public static final ResourceLocation ajr_leg = new ResourceLocation(Tags.MODID, "textures/armor/ajr_leg.png");
    public static final ResourceLocation ajr_chest = new ResourceLocation(Tags.MODID, "textures/armor/ajr_chest.png");
    public static final ResourceLocation ajr_arm = new ResourceLocation(Tags.MODID, "textures/armor/ajr_arm.png");
    public static final ResourceLocation ajro_helmet = new ResourceLocation(Tags.MODID, "textures/armor/ajro_helmet.png");
    public static final ResourceLocation ajro_leg = new ResourceLocation(Tags.MODID, "textures/armor/ajro_leg.png");
    public static final ResourceLocation ajro_chest = new ResourceLocation(Tags.MODID, "textures/armor/ajro_chest.png");
    public static final ResourceLocation ajro_arm = new ResourceLocation(Tags.MODID, "textures/armor/ajro_arm.png");
    public static final ResourceLocation rpa_helmet = new ResourceLocation(Tags.MODID, "textures/armor/rpa_helmet.png");
    public static final ResourceLocation rpa_leg = new ResourceLocation(Tags.MODID, "textures/armor/rpa_leg.png");
    public static final ResourceLocation rpa_chest = new ResourceLocation(Tags.MODID, "textures/armor/rpa_chest.png");
    public static final ResourceLocation rpa_arm = new ResourceLocation(Tags.MODID, "textures/armor/rpa_arm.png");
    public static final ResourceLocation fau_helmet = new ResourceLocation(Tags.MODID, "textures/armor/fau_helmet.png");
    public static final ResourceLocation fau_leg = new ResourceLocation(Tags.MODID, "textures/armor/fau_leg.png");
    public static final ResourceLocation fau_chest = new ResourceLocation(Tags.MODID, "textures/armor/fau_chest.png");
    public static final ResourceLocation fau_cassette = new ResourceLocation(Tags.MODID, "textures/armor/fau_cassette.png");
    public static final ResourceLocation fau_arm = new ResourceLocation(Tags.MODID, "textures/armor/fau_arm.png");
    public static final ResourceLocation dnt_helmet = new ResourceLocation(Tags.MODID, "textures/armor/dnt_helmet.png");
    public static final ResourceLocation dnt_leg = new ResourceLocation(Tags.MODID, "textures/armor/dnt_leg.png");
    public static final ResourceLocation dnt_chest = new ResourceLocation(Tags.MODID, "textures/armor/dnt_chest.png");
    public static final ResourceLocation dnt_arm = new ResourceLocation(Tags.MODID, "textures/armor/dnt_arm.png");
    public static final ResourceLocation hat = new ResourceLocation(Tags.MODID, "textures/armor/hat.png");
    public static final ResourceLocation no9 = new ResourceLocation(Tags.MODID, "textures/armor/no9.png");
    public static final ResourceLocation no9_insignia = new ResourceLocation(Tags.MODID, "textures/armor/no9_insignia.png");
    public static final ResourceLocation goggles = new ResourceLocation(Tags.MODID, "textures/armor/goggle_glasses.png");
    public static final ResourceLocation mod_tesla = new ResourceLocation(Tags.MODID, "textures/armor/mod_tesla.png");
    public static final ResourceLocation wings_murk = new ResourceLocation(Tags.MODID, "textures/armor/wings_murk.png");
    public static final ResourceLocation wings_bob = new ResourceLocation(Tags.MODID, "textures/armor/wings_bob.png");
    /// /Texture Entities

    //Vortex
    public static final ResourceLocation vortex_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/vortex.png");
    public static final ResourceLocation vortex_hud_circle = new ResourceLocation(Tags.MODID, "textures/misc/vortex_circle.png");
    public static final ResourceLocation vortex_hud_reticle = new ResourceLocation(Tags.MODID, "textures/misc/vortex_target.png");
    public static final ResourceLocation vortex_beam_circle_2 = new ResourceLocation(Tags.MODID, "textures/particle/vortex_beam_circle_2.png");
    public static final ResourceLocation vortex_hit = new ResourceLocation(Tags.MODID, "textures/particle/vortex_hit.png");
    public static final ResourceLocation vortex_beam2 = new ResourceLocation(Tags.MODID, "textures/particle/vortex_beam2.png");
    public static final ResourceLocation vortex_flash = new ResourceLocation(Tags.MODID, "textures/particle/vortex_flash.png");
    public static final ResourceLocation white = new ResourceLocation(Tags.MODID, "textures/misc/white.png");
    //ChickenCom plasma gun
    public static final ResourceLocation cc_plasma_cannon_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/cc_assault_rifle.png");
    //Gluon gun
    public static final ResourceLocation egon_hose_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/egon_hose.png");
    public static final ResourceLocation egon_display_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/egon_display.png");
    public static final ResourceLocation egon_backpack_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/egon.png");
    public static final ResourceLocation crucible_spark = new ResourceLocation(Tags.MODID, "textures/misc/crucible_spark.png");
    public static final ResourceLocation lut = new ResourceLocation(Tags.MODID, "textures/misc/neutrallut.png");
    public static final ResourceLocation spinny_light_tex = new ResourceLocation(Tags.MODID, "textures/blocks/spinnylight.png");
    //Drones
    public static final IModelCustom delivery_drone = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/machines/drone.obj"));
    //Blast
    public static final ResourceLocation tomblast = new ResourceLocation(Tags.MODID, "textures/models/explosion/tomblast.png");
    public static final ResourceLocation dust = new ResourceLocation(Tags.MODID, "textures/models/explosion/dust.png");
    //Boxcar
    public static final ResourceLocation boxcar_tex = new ResourceLocation(Tags.MODID, "textures/models/misc/boxcar.png");
    public static final ResourceLocation boxcar_tex_flipv = new ResourceLocation(Tags.MODID, "textures/models/misc/boxcarflipv.png");
    public static final ResourceLocation duchessgambit_tex = new ResourceLocation(Tags.MODID, "textures/models/misc/duchessgambit.png");
    public static final ResourceLocation building_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/building.png");
    public static final ResourceLocation torpedo_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/torpedo.png");
    //public static final ResourceLocation wings_solstice = new ResourceLocation(RefStrings.MODID, "textures/armor/wings_solstice.png");
    public static final ResourceLocation rpc_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/rpc.png");
    public static final ResourceLocation tom_main_tex = new ResourceLocation(Tags.MODID, "textures/models/explosion/tom_main.png");
    public static final ResourceLocation tom_flame_tex = new ResourceLocation(Tags.MODID, "textures/models/explosion/tom_flame.png");
    public static final ResourceLocation tom_flame_o_tex = new ResourceLocation(Tags.MODID, "textures/models/explosion/tom_flame_o.png");
    public static final ResourceLocation bobkotium_tex = new ResourceLocation(Tags.MODID, "textures/models/misc/bobkotium.png");
    public static final ResourceLocation bfg_ring_4 = new ResourceLocation(Tags.MODID, "textures/models/bfg/ring3_lighter.png");
    public static final ResourceLocation bfg_lightning_1 = new ResourceLocation(Tags.MODID, "textures/models/bfg/lightning_isolated.png");
    public static final ResourceLocation bfg_lightning_2 = new ResourceLocation(Tags.MODID, "textures/models/bfg/multi_tester.png");
    public static final ResourceLocation bfg_core_lightning = new ResourceLocation(Tags.MODID, "textures/models/bfg/additivebeam.png");
    public static final ResourceLocation bfg_beam = new ResourceLocation(Tags.MODID, "textures/models/bfg/why.png");
    public static final ResourceLocation bfg_beam1 = new ResourceLocation(Tags.MODID, "textures/models/bfg/why2.png");
    public static final ResourceLocation bfg_beam2 = new ResourceLocation(Tags.MODID, "textures/models/bfg/beam_test0.png");
    public static final ResourceLocation bfg_prefire = new ResourceLocation(Tags.MODID, "textures/models/bfg/perlin_fresnel.png");
    public static final ResourceLocation bfg_particle = new ResourceLocation(Tags.MODID, "textures/models/bfg/particle.png");
    public static final ResourceLocation bfg_smoke = new ResourceLocation(Tags.MODID, "textures/models/bfg/smoke3_bright2.png");
    //Bullet VFX
    public static final ResourceLocation bullet_impact = new ResourceLocation(Tags.MODID, "textures/misc/impact.png");
    public static final ResourceLocation bullet_impact_occlusion = new ResourceLocation(Tags.MODID, "textures/misc/impact_occlusion.png");
    public static final ResourceLocation bullet_impact_normal = new ResourceLocation(Tags.MODID, "textures/misc/impact_normal.png");
    public static final ResourceLocation rock_fragments = new ResourceLocation(Tags.MODID, "textures/misc/rock_fragments.png");
    public static final ResourceLocation twigs_and_leaves = new ResourceLocation(Tags.MODID, "textures/misc/twigs_and_leaves.png");
    public static final ResourceLocation wood_fragments = new ResourceLocation(Tags.MODID, "textures/misc/wood_shards.png");
    public static final ResourceLocation smoke_anim0 = new ResourceLocation(Tags.MODID, "textures/misc/smo0_blur4.png");
    public static final ResourceLocation fresnel_l = new ResourceLocation(Tags.MODID, "textures/models/bfg/fresnel.png");
    public static final ResourceLocation fresnel_m = new ResourceLocation(Tags.MODID, "textures/models/bfg/fresnel_m.png");
    public static final ResourceLocation fresnel_ms = new ResourceLocation(Tags.MODID, "textures/models/bfg/fresnel_ms.png");
    public static final ResourceLocation fresnel_s = new ResourceLocation(Tags.MODID, "textures/models/bfg/fresnel_s.png");
    //Projectiles
    public static final ResourceLocation flechette_tex = new ResourceLocation(Tags.MODID, "textures/models/projectiles/flechette.png");
    public static final ResourceLocation casings_tex = new ResourceLocation(Tags.MODID, "textures/particle/casings.png");
    public static final ResourceLocation grenade_tex = new ResourceLocation(Tags.MODID, "textures/models/projectiles/grenade.png");
    public static final ResourceLocation rocket_tex = new ResourceLocation(Tags.MODID, "textures/models/projectiles/rocket.png");
    public static final ResourceLocation rocket_mirv_tex = new ResourceLocation(Tags.MODID, "textures/models/projectiles/rocket_mirv.png");
    public static final ResourceLocation mini_mirv_tex = new ResourceLocation(Tags.MODID, "textures/models/projectiles/mini_mirv.png");
    //Bomber
    public static final ResourceLocation dornier_0_tex = new ResourceLocation(Tags.MODID, "textures/models/planes/dornier_0.png");
    public static final ResourceLocation dornier_1_tex = new ResourceLocation(Tags.MODID, "textures/models/planes/dornier_1.png");
    public static final ResourceLocation dornier_2_tex = new ResourceLocation(Tags.MODID, "textures/models/planes/dornier_2.png");
    public static final ResourceLocation dornier_3_tex = new ResourceLocation(Tags.MODID, "textures/models/planes/dornier_3.png");
    public static final ResourceLocation dornier_4_tex = new ResourceLocation(Tags.MODID, "textures/models/planes/dornier_4.png");
    public static final ResourceLocation b29_0_tex = new ResourceLocation(Tags.MODID, "textures/models/planes/b29_0.png");
    public static final ResourceLocation b29_1_tex = new ResourceLocation(Tags.MODID, "textures/models/planes/b29_1.png");
    public static final ResourceLocation b29_2_tex = new ResourceLocation(Tags.MODID, "textures/models/planes/b29_2.png");
    public static final ResourceLocation b29_3_tex = new ResourceLocation(Tags.MODID, "textures/models/planes/b29_3.png");
    public static final ResourceLocation c130_0_tex = new ResourceLocation(Tags.MODID, "textures/models/weapons/c130_0.png");
    //Missiles
    public static final ResourceLocation missileV2_HE_tex = new ResourceLocation(Tags.MODID, "textures/models/missiles/missile_v2.png");
    public static final ResourceLocation missileV2_IN_tex = new ResourceLocation(Tags.MODID, "textures/models/missiles/missile_v2_inc.png");
    public static final ResourceLocation missileV2_CL_tex = new ResourceLocation(Tags.MODID, "textures/models/missiles/missile_v2_cl.png");
    public static final ResourceLocation missileV2_BU_tex = new ResourceLocation(Tags.MODID, "textures/models/missiles/missile_v2_bu.png");
    public static final ResourceLocation missileV2_decoy_tex = new ResourceLocation(Tags.MODID, "textures/models/missiles/missile_v2_decoy.png");
    public static final ResourceLocation missileAA_tex = new ResourceLocation(Tags.MODID, "textures/models/missiles/missile_abm.png");
    public static final ResourceLocation missileStealth_tex = new ResourceLocation(Tags.MODID, "textures/models/missiles/missile_stealth.png");
    public static final ResourceLocation missileStrong_HE_tex = new ResourceLocation(Tags.MODID, "textures/models/missiles/missile_strong.png");
    public static final ResourceLocation missileStrong_EMP_tex = new ResourceLocation(Tags.MODID, "textures/models/missiles/missile_strong_emp.png");
    public static final ResourceLocation missileStrong_IN_tex = new ResourceLocation(Tags.MODID, "textures/models/missiles/missile_strong_inc.png");
    public static final ResourceLocation missileStrong_CL_tex = new ResourceLocation(Tags.MODID, "textures/models/missiles/missile_strong_cl.png");
    public static final ResourceLocation missileStrong_BU_tex = new ResourceLocation(Tags.MODID, "textures/models/missiles/missile_strong_bu.png");
    public static final ResourceLocation missileHuge_HE_tex = new ResourceLocation(Tags.MODID, "textures/models/missiles/missile_huge.png");
    public static final ResourceLocation missileHuge_IN_tex = new ResourceLocation(Tags.MODID, "textures/models/missiles/missile_huge_inc.png");
    public static final ResourceLocation missileHuge_CL_tex = new ResourceLocation(Tags.MODID, "textures/models/missiles/missile_huge_cl.png");
    public static final ResourceLocation missileHuge_BU_tex = new ResourceLocation(Tags.MODID, "textures/models/missiles/missile_huge_bu.png");
    public static final ResourceLocation missileNuclear_tex = new ResourceLocation(Tags.MODID, "textures/models/missiles/missile_atlas_nuclear.png");
    public static final ResourceLocation missileMIRV_tex = new ResourceLocation(Tags.MODID, "textures/models/missiles/missile_atlas_thermo.png");
    public static final ResourceLocation missileVolcano_tex = new ResourceLocation(Tags.MODID, "textures/models/missiles/missile_atlas_tectonic.png");
    public static final ResourceLocation missileDoomsday_tex = new ResourceLocation(Tags.MODID, "textures/models/missiles/missile_atlas_doomsday.png");
    public static final ResourceLocation missileDoomsdayRusted_tex = new ResourceLocation(Tags.MODID, "textures/models/missiles/missile_atlas_doomsday_weathered.png");
    public static final ResourceLocation missileN2_tex = new ResourceLocation(Tags.MODID, "textures/models/missiles/missileN2.png");
    public static final ResourceLocation missileEndo_tex = new ResourceLocation(Tags.MODID, "textures/models/missiles/missileEndo.png");
    public static final ResourceLocation missileExo_tex = new ResourceLocation(Tags.MODID, "textures/models/missiles/missileExo.png");
    public static final ResourceLocation missileTaint_tex = new ResourceLocation(Tags.MODID, "textures/models/missiles/missile_micro_taint.png");
    public static final ResourceLocation missileMicro_tex = new ResourceLocation(Tags.MODID, "textures/models/missiles/missile_micro.png");
    public static final ResourceLocation missileCarrier_tex = new ResourceLocation(Tags.MODID, "textures/models/missiles/missileCarrier.png");
    public static final ResourceLocation missileBooster_tex = new ResourceLocation(Tags.MODID, "textures/models/missiles/missileBooster.png");
    public static final ResourceLocation minerRocket_tex = new ResourceLocation(Tags.MODID, "textures/models/missiles/minerRocket.png");
    public static final ResourceLocation minerRocketGerald_tex = new ResourceLocation(Tags.MODID, "textures/models/missiles/minerRocket_gerald.png");
    public static final ResourceLocation bobmazon_tex = new ResourceLocation(Tags.MODID, "textures/models/missiles/bobmazon.png");
    public static final ResourceLocation missileMicroBHole_tex = new ResourceLocation(Tags.MODID, "textures/models/missiles/missile_micro_bhole.png");
    public static final ResourceLocation missileMicroSchrab_tex = new ResourceLocation(Tags.MODID, "textures/models/missiles/missile_micro_schrab.png");
    public static final ResourceLocation missileMicroEMP_tex = new ResourceLocation(Tags.MODID, "textures/models/missiles/missile_micro_emp.png");
    public static final ResourceLocation soyuz_engineblock = new ResourceLocation(Tags.MODID, "textures/models/soyuz/engineblock.png");
    public static final ResourceLocation soyuz_bottomstage = new ResourceLocation(Tags.MODID, "textures/models/soyuz/bottomstage.png");
    public static final ResourceLocation soyuz_topstage = new ResourceLocation(Tags.MODID, "textures/models/soyuz/topstage.png");
    public static final ResourceLocation soyuz_payload = new ResourceLocation(Tags.MODID, "textures/models/soyuz/payload.png");
    public static final ResourceLocation soyuz_payloadblocks = new ResourceLocation(Tags.MODID, "textures/models/soyuz/payloadblocks.png");
    public static final ResourceLocation soyuz_les = new ResourceLocation(Tags.MODID, "textures/models/soyuz/les.png");
    public static final ResourceLocation soyuz_lesthrusters = new ResourceLocation(Tags.MODID, "textures/models/soyuz/lesthrusters.png");
    public static final ResourceLocation soyuz_mainengines = new ResourceLocation(Tags.MODID, "textures/models/soyuz/mainengines.png");
    public static final ResourceLocation soyuz_sideengines = new ResourceLocation(Tags.MODID, "textures/models/soyuz/sideengines.png");
    public static final ResourceLocation soyuz_booster = new ResourceLocation(Tags.MODID, "textures/models/soyuz/booster.png");
    public static final ResourceLocation soyuz_boosterside = new ResourceLocation(Tags.MODID, "textures/models/soyuz/boosterside.png");
    public static final ResourceLocation soyuz_luna_engineblock = new ResourceLocation(Tags.MODID, "textures/models/soyuz_luna/engineblock.png");
    public static final ResourceLocation soyuz_luna_bottomstage = new ResourceLocation(Tags.MODID, "textures/models/soyuz_luna/bottomstage.png");
    public static final ResourceLocation soyuz_luna_topstage = new ResourceLocation(Tags.MODID, "textures/models/soyuz_luna/topstage.png");
    public static final ResourceLocation soyuz_luna_payload = new ResourceLocation(Tags.MODID, "textures/models/soyuz_luna/payload.png");
    public static final ResourceLocation soyuz_luna_payloadblocks = new ResourceLocation(Tags.MODID, "textures/models/soyuz_luna/payloadblocks.png");
    public static final ResourceLocation soyuz_luna_les = new ResourceLocation(Tags.MODID, "textures/models/soyuz_luna/les.png");
    public static final ResourceLocation soyuz_luna_lesthrusters = new ResourceLocation(Tags.MODID, "textures/models/soyuz_luna/lesthrusters.png");
    public static final ResourceLocation soyuz_luna_mainengines = new ResourceLocation(Tags.MODID, "textures/models/soyuz_luna/mainengines.png");
    public static final ResourceLocation soyuz_luna_sideengines = new ResourceLocation(Tags.MODID, "textures/models/soyuz_luna/sideengines.png");
    public static final ResourceLocation soyuz_luna_booster = new ResourceLocation(Tags.MODID, "textures/models/soyuz_luna/booster.png");
    public static final ResourceLocation soyuz_luna_boosterside = new ResourceLocation(Tags.MODID, "textures/models/soyuz_luna/boosterside.png");
    public static final ResourceLocation soyuz_authentic_engineblock = new ResourceLocation(Tags.MODID, "textures/models/soyuz_authentic/engineblock.png");
    public static final ResourceLocation soyuz_authentic_bottomstage = new ResourceLocation(Tags.MODID, "textures/models/soyuz_authentic/bottomstage.png");
    public static final ResourceLocation soyuz_authentic_topstage = new ResourceLocation(Tags.MODID, "textures/models/soyuz_authentic/topstage.png");
    public static final ResourceLocation soyuz_authentic_payload = new ResourceLocation(Tags.MODID, "textures/models/soyuz_authentic/payload.png");
    public static final ResourceLocation soyuz_authentic_payloadblocks = new ResourceLocation(Tags.MODID, "textures/models/soyuz_authentic/payloadblocks.png");
    public static final ResourceLocation soyuz_authentic_les = new ResourceLocation(Tags.MODID, "textures/models/soyuz_authentic/les.png");
    public static final ResourceLocation soyuz_authentic_lesthrusters = new ResourceLocation(Tags.MODID, "textures/models/soyuz_authentic/lesthrusters.png");
    public static final ResourceLocation soyuz_authentic_mainengines = new ResourceLocation(Tags.MODID, "textures/models/soyuz_authentic/mainengines.png");
    public static final ResourceLocation soyuz_authentic_sideengines = new ResourceLocation(Tags.MODID, "textures/models/soyuz_authentic/sideengines.png");
    public static final ResourceLocation soyuz_authentic_booster = new ResourceLocation(Tags.MODID, "textures/models/soyuz_authentic/booster.png");
    public static final ResourceLocation soyuz_authentic_boosterside = new ResourceLocation(Tags.MODID, "textures/models/soyuz_authentic/boosterside.png");
    public static final ResourceLocation soyuz_memento = new ResourceLocation(Tags.MODID, "textures/items/polaroid_memento.png");
    public static final ResourceLocation soyuz_lander_tex = new ResourceLocation(Tags.MODID, "textures/models/soyuz_capsule/soyuz_lander.png");
    public static final ResourceLocation soyuz_lander_rust_tex = new ResourceLocation(Tags.MODID, "textures/models/soyuz_capsule/soyuz_lander_rust.png");
    public static final ResourceLocation soyuz_chute_tex = new ResourceLocation(Tags.MODID, "textures/models/soyuz_capsule/soyuz_chute.png");
    public static final ResourceLocation supply_crate = new ResourceLocation(Tags.MODID, "textures/blocks/crate_can.png");
    public static final ResourceLocation module_lander_tex = new ResourceLocation(Tags.MODID, "textures/models/soyuz_capsule/module_lander_space_elon.png");
    public static final ResourceLocation soyuz_module_dome_tex = new ResourceLocation(Tags.MODID, "textures/models/soyuz_capsule/module_dome.png");
    public static final ResourceLocation soyuz_module_lander_tex = new ResourceLocation(Tags.MODID, "textures/models/soyuz_capsule/module_lander.png");
    public static final ResourceLocation soyuz_module_propulsion_tex = new ResourceLocation(Tags.MODID, "textures/models/soyuz_capsule/module_propulsion.png");
    public static final ResourceLocation soyuz_module_solar_tex = new ResourceLocation(Tags.MODID, "textures/models/soyuz_capsule/module_solar.png");
    public static final ResourceLocation soyuz_launcher_legs_tex = new ResourceLocation(Tags.MODID, "textures/models/soyuz_launcher/launcher_leg.png");
    public static final ResourceLocation soyuz_launcher_table_tex = new ResourceLocation(Tags.MODID, "textures/models/soyuz_launcher/launcher_table.png");
    public static final ResourceLocation soyuz_launcher_tower_base_tex = new ResourceLocation(Tags.MODID, "textures/models/soyuz_launcher/launcher_tower_base.png");
    public static final ResourceLocation soyuz_launcher_tower_tex = new ResourceLocation(Tags.MODID, "textures/models/soyuz_launcher/launcher_tower.png");
    public static final ResourceLocation soyuz_launcher_support_base_tex = new ResourceLocation(Tags.MODID, "textures/models/soyuz_launcher/launcher_support_base.png");
    public static final ResourceLocation soyuz_launcher_support_tex = new ResourceLocation(Tags.MODID, "textures/models/soyuz_launcher/launcher_support.png");
    //Missile Parts
    public static final ResourceLocation missile_pad_tex = new ResourceLocation(Tags.MODID, "textures/models/launchpad/silo.png");
    public static final ResourceLocation missile_pad_rusted_tex = new ResourceLocation(Tags.MODID, "textures/models/launchpad/silo_rusted.png");
    public static final ResourceLocation missile_erector_tex = new ResourceLocation(Tags.MODID, "textures/models/launchpad/pad.png");
    public static final ResourceLocation missile_erector_micro_tex = new ResourceLocation(Tags.MODID, "textures/models/launchpad/erector_micro.png");
    public static final ResourceLocation missile_erector_v2_tex = new ResourceLocation(Tags.MODID, "textures/models/launchpad/erector_v2.png");
    public static final ResourceLocation missile_erector_strong_tex = new ResourceLocation(Tags.MODID, "textures/models/launchpad/erector_strong.png");
    public static final ResourceLocation missile_erector_huge_tex = new ResourceLocation(Tags.MODID, "textures/models/launchpad/erector_huge.png");
    public static final ResourceLocation missile_erector_atlas_tex = new ResourceLocation(Tags.MODID, "textures/models/launchpad/erector_atlas.png");
    public static final ResourceLocation missile_erector_abm_tex = new ResourceLocation(Tags.MODID, "textures/models/launchpad/erector_abm.png");
    public static final ResourceLocation missile_assembly_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/missile_assembly.png");
    public static final ResourceLocation strut_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/strut.png");
    public static final ResourceLocation compact_launcher_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/compact_launcher.png");
    public static final ResourceLocation launch_table_base_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/launch_table.png");
    public static final ResourceLocation launch_table_large_pad_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/launch_table_large_pad.png");
    public static final ResourceLocation launch_table_small_pad_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/launch_table_small_pad.png");
    public static final ResourceLocation launch_table_large_scaffold_base_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/launch_table_large_scaffold_base.png");
    public static final ResourceLocation launch_table_large_scaffold_connector_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/launch_table_large_scaffold_connector.png");
    public static final ResourceLocation launch_table_small_scaffold_base_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/launch_table_small_scaffold_base.png");
    public static final ResourceLocation launch_table_small_scaffold_connector_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/launch_table_small_scaffold_connector.png");
    public static final ResourceLocation mp_t_10_kerosene_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/thrusters/mp_t_10_kerosene.png");
    public static final ResourceLocation mp_t_10_solid_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/thrusters/mp_t_10_solid.png");
    public static final ResourceLocation mp_t_10_xenon_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/thrusters/mp_t_10_xenon.png");
    public static final ResourceLocation mp_t_15_kerosene_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/thrusters/mp_t_15_kerosene.png");
    public static final ResourceLocation mp_t_15_kerosene_dual_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/thrusters/mp_t_15_kerosene_dual.png");
    public static final ResourceLocation mp_t_15_solid_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/thrusters/mp_t_15_solid.png");
    public static final ResourceLocation mp_t_15_solid_hexdecuple_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/thrusters/mp_t_15_solid_hexdecuple.png");
    public static final ResourceLocation mp_t_15_hydrogen_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/thrusters/mp_t_15_hydrogen.png");
    public static final ResourceLocation mp_t_15_hydrogen_dual_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/thrusters/mp_t_15_hydrogen_dual.png");
    public static final ResourceLocation mp_t_15_balefire_short_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/thrusters/mp_t_15_balefire_short.png");
    public static final ResourceLocation mp_t_15_balefire_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/thrusters/mp_t_15_balefire.png");
    public static final ResourceLocation mp_t_15_balefire_large_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/thrusters/mp_t_15_balefire_large.png");
    public static final ResourceLocation mp_t_15_balefire_large_rad_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/thrusters/mp_t_15_balefire_large_rad.png");
    public static final ResourceLocation mp_t_20_kerosene_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/thrusters/mp_t_20_kerosene.png");
    public static final ResourceLocation mp_t_20_kerosene_dual_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/thrusters/mp_t_20_kerosene_dual.png");
    public static final ResourceLocation mp_t_20_solid_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/thrusters/mp_t_20_solid.png");
    public static final ResourceLocation mp_t_20_solid_multi_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/thrusters/mp_t_20_solid_multi.png");
    public static final ResourceLocation mp_t_20_solid_multier_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/thrusters/mp_t_20_solid_multier.png");
    public static final ResourceLocation mp_s_10_flat_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/stability/mp_s_10_flat.png");
    public static final ResourceLocation mp_s_10_cruise_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/stability/mp_s_10_cruise.png");
    public static final ResourceLocation mp_s_10_space_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/stability/mp_s_10_space.png");
    public static final ResourceLocation mp_s_15_flat_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/stability/mp_s_15_flat.png");
    public static final ResourceLocation mp_s_15_thin_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/stability/mp_s_15_thin.png");
    public static final ResourceLocation mp_s_15_soyuz_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/stability/mp_s_15_soyuz.png");
    public static final ResourceLocation mp_f_10_kerosene_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_10_kerosene.png");
    public static final ResourceLocation mp_f_10_kerosene_camo_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_10_kerosene_camo.png");
    public static final ResourceLocation mp_f_10_kerosene_desert_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_10_kerosene_desert.png");
    public static final ResourceLocation mp_f_10_kerosene_sky_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_10_kerosene_sky.png");
    public static final ResourceLocation mp_f_10_kerosene_flames_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_10_kerosene_flames.png");
    public static final ResourceLocation mp_f_10_kerosene_insulation_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_10_kerosene_insulation.png");
    public static final ResourceLocation mp_f_10_kerosene_sleek_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_10_kerosene_sleek.png");
    public static final ResourceLocation mp_f_10_kerosene_metal_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_10_kerosene_metal.png");
    public static final ResourceLocation mp_f_10_kerosene_taint_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/contest/mp_f_10_kerosene_taint.png");
    public static final ResourceLocation mp_f_10_solid_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_10_solid.png");
    public static final ResourceLocation mp_f_10_solid_flames_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_10_solid_flames.png");
    public static final ResourceLocation mp_f_10_solid_insulation_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_10_solid_insulation.png");
    public static final ResourceLocation mp_f_10_solid_sleek_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_10_solid_sleek.png");
    public static final ResourceLocation mp_f_10_solid_soviet_glory_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_10_solid_soviet_glory.png");
    public static final ResourceLocation mp_f_10_solid_moonlit_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/contest/mp_f_10_solid_moonlit.png");
    public static final ResourceLocation mp_f_10_solid_cathedral_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/contest/mp_f_10_solid_cathedral.png");
    public static final ResourceLocation mp_f_10_solid_battery_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/contest/mp_f_10_solid_battery.png");
    public static final ResourceLocation mp_f_10_solid_duracell_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_10_solid_duracell.png");
    public static final ResourceLocation mp_f_10_xenon_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_10_xenon.png");
    public static final ResourceLocation mp_f_10_xenon_bhole_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/contest/mp_f_10_xenon_bhole.png");
    public static final ResourceLocation mp_f_10_long_kerosene_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_10_long_kerosene.png");
    public static final ResourceLocation mp_f_10_long_kerosene_camo_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_10_long_kerosene_camo.png");
    public static final ResourceLocation mp_f_10_long_kerosene_desert_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_10_long_kerosene_desert.png");
    public static final ResourceLocation mp_f_10_long_kerosene_sky_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_10_long_kerosene_sky.png");
    public static final ResourceLocation mp_f_10_long_kerosene_flames_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_10_long_kerosene_flames.png");
    public static final ResourceLocation mp_f_10_long_kerosene_insulation_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_10_long_kerosene_insulation.png");
    public static final ResourceLocation mp_f_10_long_kerosene_sleek_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_10_long_kerosene_sleek.png");
    public static final ResourceLocation mp_f_10_long_kerosene_metal_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_10_long_kerosene_metal.png");
    public static final ResourceLocation mp_f_10_long_kerosene_dash_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/contest/mp_f_10_long_kerosene_dash.png");
    public static final ResourceLocation mp_f_10_long_kerosene_taint_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/contest/mp_f_10_long_kerosene_taint.png");
    public static final ResourceLocation mp_f_10_long_kerosene_vap_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/contest/mp_f_10_long_kerosene_vap.png");
    public static final ResourceLocation mp_f_10_long_solid_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_10_long_solid.png");
    public static final ResourceLocation mp_f_10_long_solid_flames_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_10_long_solid_flames.png");
    public static final ResourceLocation mp_f_10_long_solid_insulation_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_10_long_solid_insulation.png");
    public static final ResourceLocation mp_f_10_long_solid_sleek_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_10_long_solid_sleek.png");
    public static final ResourceLocation mp_f_10_long_solid_soviet_glory_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_10_long_solid_soviet_glory.png");
    public static final ResourceLocation mp_f_10_long_solid_bullet_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/contest/mp_f_10_long_solid_bullet.png");
    public static final ResourceLocation mp_f_10_long_solid_silvermoonlight_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/contest/mp_f_10_long_solid_silvermoonlight.png");
    public static final ResourceLocation mp_f_10_15_kerosene_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_10_15_kerosene.png");
    public static final ResourceLocation mp_f_10_15_solid_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_10_15_solid.png");
    public static final ResourceLocation mp_f_10_15_hydrogen_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_10_15_hydrogen.png");
    public static final ResourceLocation mp_f_10_15_balefire_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_10_15_balefire.png");
    public static final ResourceLocation mp_f_15_kerosene_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_15_kerosene.png");
    public static final ResourceLocation mp_f_15_kerosene_camo_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_15_kerosene_camo.png");
    public static final ResourceLocation mp_f_15_kerosene_desert_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_15_kerosene_desert.png");
    public static final ResourceLocation mp_f_15_kerosene_sky_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_15_kerosene_sky.png");
    public static final ResourceLocation mp_f_15_kerosene_insulation_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_15_kerosene_insulation.png");
    public static final ResourceLocation mp_f_15_kerosene_metal_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_15_kerosene_metal.png");
    public static final ResourceLocation mp_f_15_kerosene_decorated_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_15_kerosene_decorated.png");
    public static final ResourceLocation mp_f_15_kerosene_steampunk_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_15_kerosene_steampunk.png");
    public static final ResourceLocation mp_f_15_kerosene_polite_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_15_kerosene_polite.png");
    public static final ResourceLocation mp_f_15_kerosene_blackjack_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/base/mp_f_15_kerosene_blackjack.png");
    public static final ResourceLocation mp_f_15_kerosene_lambda_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/contest/mp_f_15_kerosene_lambda.png");
    public static final ResourceLocation mp_f_15_kerosene_minuteman_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/contest/mp_f_15_kerosene_minuteman.png");
    public static final ResourceLocation mp_f_15_kerosene_pip_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/contest/mp_f_15_kerosene_pip.png");
    public static final ResourceLocation mp_f_15_kerosene_taint_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/contest/mp_f_15_kerosene_taint.png");
    public static final ResourceLocation mp_f_15_kerosene_yuck_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_15_kerosene_yuck.png");
    public static final ResourceLocation mp_f_15_solid_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_15_solid.png");
    public static final ResourceLocation mp_f_15_solid_insulation_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_15_solid_insulation.png");
    public static final ResourceLocation mp_f_15_solid_desh_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_15_solid_desh.png");
    public static final ResourceLocation mp_f_15_solid_soviet_glory_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_15_solid_soviet_glory.png");
    public static final ResourceLocation mp_f_15_solid_soviet_stank_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_15_solid_soviet_stank.png");
    public static final ResourceLocation mp_f_15_solid_faust_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/contest/mp_f_15_solid_faust.png");
    public static final ResourceLocation mp_f_15_solid_silvermoonlight_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/contest/mp_f_15_solid_silvermoonlight.png");
    public static final ResourceLocation mp_f_15_solid_snowy_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/contest/mp_f_15_solid_snowy.png");
    public static final ResourceLocation mp_f_15_solid_panorama_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_15_solid_panorama.png");
    public static final ResourceLocation mp_f_15_solid_roses_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_15_solid_roses.png");
    public static final ResourceLocation mp_f_15_solid_mimi_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_15_solid_mimi.png");
    public static final ResourceLocation mp_f_20_kerolox = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_20_kerosene.png");
    public static final ResourceLocation mp_f_20_kerolox_usa = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_20_kerosene_usa.png");
    public static final ResourceLocation mp_f_15_hydrogen_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_15_hydrogen.png");
    public static final ResourceLocation mp_f_15_hydrogen_cathedral_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/contest/mp_f_15_hydrogen_cathedral.png");
    public static final ResourceLocation mp_f_15_balefire_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_15_balefire.png");
    public static final ResourceLocation mp_f_15_20_kerosene_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_15_20_kerosene.png");
    public static final ResourceLocation mp_f_15_20_kerosene_magnusson_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_15_20_kerosene_magnusson.png");
    public static final ResourceLocation mp_f_15_20_solid_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/fuselages/mp_f_15_20_solid.png");
    public static final ResourceLocation mp_w_10_he_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/warheads/mp_w_10_he.png");
    public static final ResourceLocation mp_w_10_incendiary_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/warheads/mp_w_10_incendiary.png");
    public static final ResourceLocation mp_w_10_buster_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/warheads/mp_w_10_buster.png");
    public static final ResourceLocation mp_w_10_nuclear_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/warheads/mp_w_10_nuclear.png");
    public static final ResourceLocation mp_w_10_nuclear_large_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/warheads/mp_w_10_nuclear_large.png");
    public static final ResourceLocation mp_w_10_taint_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/warheads/mp_w_10_taint.png");
    public static final ResourceLocation mp_w_10_cloud_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/warheads/mp_w_10_cloud.png");
    public static final ResourceLocation mp_w_15_he_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/warheads/mp_w_15_he.png");
    public static final ResourceLocation mp_w_15_incendiary_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/warheads/mp_w_15_incendiary.png");
    public static final ResourceLocation mp_w_15_nuclear_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/warheads/mp_w_15_nuclear.png");
    public static final ResourceLocation mp_w_15_nuclear_shark_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/warheads/mp_w_15_nuclear_shark.png");
    public static final ResourceLocation mp_w_15_thermo_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/warheads/mp_w_15_thermo.png");
    public static final ResourceLocation mp_w_15_n2_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/warheads/mp_w_15_n2.png");
    public static final ResourceLocation mp_w_15_balefire_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/warheads/mp_w_15_balefire.png");
    public static final ResourceLocation mp_w_15_turbine_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/warheads/mp_w_15_turbine.png");
    public static final ResourceLocation mp_w_15_volcano_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/warheads/mp_w_15_volcano.png");
    public static final ResourceLocation mp_w_15_mirv_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/warheads/mp_w_15_mirv.png");
    public static final ResourceLocation mp_w_fairing_tex = new ResourceLocation(Tags.MODID, "textures/models/missile_parts/warheads/mp_w_fairing.png");
    //Drone
    public static final ResourceLocation delivery_drone_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/drone.png");
    public static final ResourceLocation delivery_drone_express_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/drone_express.png");
    public static final ResourceLocation delivery_drone_request_tex = new ResourceLocation(Tags.MODID, "textures/models/machines/drone_request.png");
    //Keypad
    public static final ResourceLocation keypad_tex = new ResourceLocation(Tags.MODID, "textures/models/doors/slidingblast/keypad.png");
    public static final ResourceLocation keypad_error_tex = new ResourceLocation(Tags.MODID, "textures/models/doors/slidingblast/keypad_error.png");
    public static final ResourceLocation keypad_success_tex = new ResourceLocation(Tags.MODID, "textures/models/doors/slidingblast/keypad_success.png");
    //SSG
    public static final ResourceLocation x_marker = new ResourceLocation(Tags.MODID, "textures/misc/x.png");
    public static final ResourceLocation meathook_marker = new ResourceLocation(Tags.MODID, "textures/misc/meathook.png");
    //PLASMA RAILGUN
    public static final ResourceLocation railgun_base_tex = new ResourceLocation(Tags.MODID, "textures/models/turrets/railgun_base.png");
    public static final ResourceLocation railgun_rotor_tex = new ResourceLocation(Tags.MODID, "textures/models/turrets/railgun_rotor.png");
    public static final ResourceLocation railgun_main_tex = new ResourceLocation(Tags.MODID, "textures/models/turrets/railgun_main.png");
    public static final IModelCustom railgun_base = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/railgun_base.obj")).asVBO();
    public static final IModelCustom railgun_rotor = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/railgun_rotor.obj")).asVBO();
    public static final IModelCustom railgun_main = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/railgun_main.obj")).asVBO();
    // Dud
    public static final IModelCustom dud_balefire = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/bombs/dud_balefire.obj")).asVBO();
    public static final IModelCustom dud_conventional = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/bombs/dud_conventional.obj")).asVBO();
    public static final IModelCustom dud_nuke = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/bombs/dud_nuke.obj")).asVBO();
    public static final IModelCustom dud_salted = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/bombs/dud_salted.obj")).asVBO();
    public static final ResourceLocation dud_balefire_tex = new ResourceLocation(Tags.MODID, "textures/models/bombs/dud_balefire.png");
    public static final ResourceLocation dud_conventional_tex = new ResourceLocation(Tags.MODID, "textures/models/bombs/dud_conventional.png");
    public static final ResourceLocation dud_nuke_tex = new ResourceLocation(Tags.MODID, "textures/models/bombs/dud_nuke.png");
    public static final ResourceLocation dud_salted_tex = new ResourceLocation(Tags.MODID, "textures/models/bombs/dud_salted.png");
    //Blood
    public static final ResourceLocation blood0 = new ResourceLocation(Tags.MODID, "textures/misc/blood0.png");
    public static final ResourceLocation blood_dec0 = new ResourceLocation(Tags.MODID, "textures/misc/blood_dec0.png");
    public static final ResourceLocation blood_dec1 = new ResourceLocation(Tags.MODID, "textures/misc/blood_dec1.png");
    public static final ResourceLocation blood_dec2 = new ResourceLocation(Tags.MODID, "textures/misc/blood_dec2.png");
    public static final ResourceLocation blood_dec3 = new ResourceLocation(Tags.MODID, "textures/misc/blood_dec3.png");
    public static final ResourceLocation blood_dec4 = new ResourceLocation(Tags.MODID, "textures/misc/blood_dec4.png");
    public static final ResourceLocation[] blood_decals = {blood_dec0, blood_dec1, blood_dec2, blood_dec3, blood_dec4};
    public static final ResourceLocation blood_particles = new ResourceLocation(Tags.MODID, "textures/misc/blood_particles.png");
    public static final ResourceLocation gore_generic = new ResourceLocation(Tags.MODID, "textures/misc/gore_generic.png");
    public static final ResourceLocation crucible_cap = new ResourceLocation(Tags.MODID, "textures/misc/crucible_cap.png");
    public static final ResourceLocation shotgun_crosshair = new ResourceLocation(Tags.MODID, "textures/misc/shotgun_crosshair.png");
    //Debug
    public static final ResourceLocation uv_debug = new ResourceLocation(Tags.MODID, "textures/misc/uv_debug.png");
    public static final ResourceLocation noise_1 = new ResourceLocation(Tags.MODID, "textures/misc/noise_1.png");
    public static final ResourceLocation noise_2 = new ResourceLocation(Tags.MODID, "textures/misc/noise_2.png");
    public static final ResourceLocation noise_3 = new ResourceLocation(Tags.MODID, "textures/misc/fract_noise.png");
    public static final ResourceLocation fl_cookie = new ResourceLocation(Tags.MODID, "textures/misc/fl_cookie.png");
    //Revolvers
    public static final ResourceLocation ff_gold = new ResourceLocation(Tags.MODID, "textures/models/weapons/ff/gold.png");
    public static final ResourceLocation ff_gun_bright = new ResourceLocation(Tags.MODID, "textures/models/weapons/ff/gun_bright.png");
    public static final ResourceLocation ff_gun_dark = new ResourceLocation(Tags.MODID, "textures/models/weapons/ff/gun_dark.png");
    public static final ResourceLocation ff_iron = new ResourceLocation(Tags.MODID, "textures/models/weapons/ff/iron.png");
    public static final ResourceLocation ff_lead = new ResourceLocation(Tags.MODID, "textures/models/weapons/ff/lead.png");
    public static final ResourceLocation ff_saturnite = new ResourceLocation(Tags.MODID, "textures/models/weapons/ff/saturnite.png");
    public static final ResourceLocation ff_schrabidium = new ResourceLocation(Tags.MODID, "textures/models/weapons/ff/schrabidium.png");
    public static final ResourceLocation ff_wood = new ResourceLocation(Tags.MODID, "textures/models/weapons/ff/wood.png");
    public static final ResourceLocation ff_wood_red = new ResourceLocation(Tags.MODID, "textures/models/weapons/ff/wood_red.png");
    public static final IModelCustomNamed water_door = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/doors/water_door.obj")).asVBO();
    public static final IModelCustomNamed large_vehicle_door = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/doors/large_vehicle_door.obj")).asVBO();
    public static final IModelCustomNamed qe_containment_door = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/doors/qe_containment.obj")).asVBO();
    public static final IModelCustomNamed qe_sliding_door = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/doors/qe_sliding_door.obj")).asVBO();
    public static final IModelCustomNamed fire_door = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/doors/fire_door.obj")).asVBO();
    public static final IModelCustomNamed small_hatch = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/doors/hatch.obj")).asVBO();
    public static final IModelCustomNamed round_airlock_door = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/doors/round_airlock_door.obj")).asVBO();
    public static final IModelCustomNamed secure_access_door = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/doors/secure_access_door.obj")).asVBO();
    public static final IModelCustomNamed sliding_seal_door = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/doors/sliding_seal_door.obj")).asVBO();
    public static final IModelCustom spinny_light = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/spinny_light.obj")).asVBO();
    public static final ResourceLocation silo_hatch_tex = new ResourceLocation(Tags.MODID, "textures/models/doors/silo_hatch.png");
    public static final IModelCustomNamed silo_hatch = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/doors/silo_hatch.obj")).asVBO();
    public static final ResourceLocation silo_hatch_large_tex = new ResourceLocation(Tags.MODID, "textures/models/doors/silo_hatch_large.png");
    public static final IModelCustomNamed silo_hatch_large = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/doors/silo_hatch_large.obj")).asVBO();
    private static final MethodHandle splashThreadGetter;
    private static final MethodHandle splashEnabledGetter;
    private static final MethodHandle splashPauseHandle;
    private static final MethodHandle splashResumeHandle;
    public static IModelCustom soyuz = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/soyuz.obj")).asVBO();
    public static IModelCustom soyuz_launcher_legs = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/launch_table/soyuz_launcher_legs.obj")).asVBO();
    public static IModelCustom soyuz_launcher_table = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/launch_table/soyuz_launcher_table.obj")).asVBO();
    public static IModelCustom soyuz_launcher_tower_base = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/launch_table/soyuz_launcher_tower_base.obj")).asVBO();
    public static IModelCustom soyuz_launcher_tower = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/launch_table/soyuz_launcher_tower.obj")).asVBO();
    public static IModelCustom soyuz_launcher_support_base = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/launch_table/soyuz_launcher_support_base.obj")).asVBO();
    public static IModelCustom soyuz_launcher_support = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/launch_table/soyuz_launcher_support.obj")).asVBO();
    public static IModelCustom sphere_uv = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/sphere_uv.obj")).asVBO();
    public static IModelCustom sphere_hq = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/sphere_hq.obj")).asVBO();
    public static IModelCustom egon_hose = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/egon_hose.obj")).asVBO();
    public static IModelCustom egon_backpack = new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/weapons/egon.obj")).asVBO();
    //Doors
    public static AnimatedModel transition_seal;
    public static Animation transition_seal_anim;
    //Gluon gun and tau cannon
    public static ResourceLocation flare = new ResourceLocation(Tags.MODID, "textures/misc/flare.png");
    public static ResourceLocation flare2 = new ResourceLocation(Tags.MODID, "textures/misc/flare2.png");
    public static ResourceLocation flare3 = new ResourceLocation(Tags.MODID, "textures/misc/flare3.png");
    public static ResourceLocation flare3b = new ResourceLocation(Tags.MODID, "textures/misc/flare3b.png");
    public static ResourceLocation gluon_beam_tex = new ResourceLocation(Tags.MODID, "textures/misc/gluonbeam.png");
    public static ResourceLocation gluon_muzzle_smoke = new ResourceLocation(Tags.MODID, "textures/misc/gluon_muzzle_smoke.png");
    public static ResourceLocation gluon_muzzle_glow = new ResourceLocation(Tags.MODID, "textures/misc/gluon_muzzle_glow.png");
    public static ResourceLocation gluon_burn = new ResourceLocation(Tags.MODID, "textures/misc/gluon_burn.png");
    public static ResourceLocation tau_beam_tex = new ResourceLocation(Tags.MODID, "textures/misc/tau_beam.png");
    public static ResourceLocation tau_lightning = new ResourceLocation(Tags.MODID, "textures/misc/tau_lightning.png");
    public static ResourceLocation gluontau_hud = new ResourceLocation(Tags.MODID, "textures/misc/gluontau_hud.png");
    public static ResourceLocation mflash = new ResourceLocation(Tags.MODID, "textures/misc/mflash_4.png");
    public static ResourceLocation beam_generic = new ResourceLocation(Tags.MODID, "textures/misc/beam_generic.png");
    //Book
    public static ResourceLocation circle_big = new ResourceLocation(Tags.MODID, "textures/misc/circle_big.png");
    public static ResourceLocation jetpack_tex = new ResourceLocation(Tags.MODID, "textures/armor/jetpack_anim.png");
    public static ResourceLocation jetpack_hud_large = new ResourceLocation(Tags.MODID, "textures/gui/hud/jetpack_hud_large.png");
    public static ResourceLocation jetpack_hud_small = new ResourceLocation(Tags.MODID, "textures/gui/hud/jetpack_hud_small.png");
    public static ResourceLocation jetpack_hud_small_text = new ResourceLocation(Tags.MODID, "textures/gui/hud/jetpack_hud_small_text.png");
    //ANIMATIONS
    public static AnimatedModel supershotgun;
    public static Animation ssg_reload;
    public static AnimatedModel door0;
    public static AnimatedModel door0_1;
    public static Animation door0_open;
    public static AnimatedModel silo_hatch_drillgon;
    public static Animation silo_hatch_open;
    public static AnimatedModel jetpack;
    public static Animation jetpack_activate;
    public static AnimatedModel lightning_fp;
    public static Animation lightning_fp_anim;
    public static AnimatedModel arm_rig;
    public static AnimatedModel jshotgun;
    public static Animation jshotgun_anim0;
    public static Animation jshotgun_anim1;
    public static AnimatedModel crucible_anim;
    public static Animation crucible_equip;
    public static AnimatedModel hs_sword;
    public static Animation hs_sword_equip;
    public static AnimatedModel hf_sword;
    public static Animation hf_sword_equip;
    //SHADERS
    public static Shader lit_particles = HbmShaderManager2.loadShader(new ResourceLocation(Tags.MODID, "shaders/lit_particles"), shader -> {
        GLCompat.bindAttribLocation(shader, 0, "pos");
        GLCompat.bindAttribLocation(shader, 1, "offsetPos");
        GLCompat.bindAttribLocation(shader, 2, "scale");
        GLCompat.bindAttribLocation(shader, 3, "texData");
        GLCompat.bindAttribLocation(shader, 4, "color");
        GLCompat.bindAttribLocation(shader, 5, "lightmap");
    }).withUniforms(HbmShaderManager2.MODELVIEW_MATRIX, HbmShaderManager2.PROJECTION_MATRIX, HbmShaderManager2.INV_PLAYER_ROT_MATRIX, HbmShaderManager2.LIGHTMAP);
    public static Shader gluon_beam = HbmShaderManager2.loadShader(new ResourceLocation(Tags.MODID, "shaders/gluon_beam"))
            .withUniforms(shader -> {
                GLCompat.activeTexture(GLCompat.GL_TEXTURE0 + 3);
                Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.noise_1);
                shader.uniform1i("noise_1", 3);
                GLCompat.activeTexture(GLCompat.GL_TEXTURE0 + 4);
                Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.noise_2);
                shader.uniform1i("noise_1", 4);
                GLCompat.activeTexture(GLCompat.GL_TEXTURE0);

                float time = (System.currentTimeMillis() % 10000000) / 1000F;
                shader.uniform1f("time", time);
            });
    public static Shader gluon_spiral = HbmShaderManager2.loadShader(new ResourceLocation(Tags.MODID, "shaders/gluon_spiral"))
            .withUniforms(shader -> {
                //Well, I accidentally uniformed the same noise sampler twice. That explains why the second noise didn't work.
                GLCompat.activeTexture(GLCompat.GL_TEXTURE0 + 3);
                Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.noise_1);
                shader.uniform1i("noise_1", 3);
                GLCompat.activeTexture(GLCompat.GL_TEXTURE0 + 4);
                Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.noise_2);
                shader.uniform1i("noise_1", 4);
                GLCompat.activeTexture(GLCompat.GL_TEXTURE0);

                float time = (System.currentTimeMillis() % 10000000) / 1000F;
                shader.uniform1f("time", time);
            });
    //Drillgon200: Did I need a shader for this? No, not really, but it's somewhat easier to create a sin wave pattern programmatically than to do it in paint.net.
    public static Shader tau_ray = HbmShaderManager2.loadShader(new ResourceLocation(Tags.MODID, "shaders/tau_ray"));
    public static Shader book_circle = HbmShaderManager2.loadShader(new ResourceLocation(Tags.MODID, "shaders/book/circle"));
    public static Shader normal_fadeout = HbmShaderManager2.loadShader(new ResourceLocation(Tags.MODID, "shaders/normal_fadeout"));
    public static Shader heat_distortion = HbmShaderManager2.loadShader(new ResourceLocation(Tags.MODID, "shaders/heat_distortion"))
            .withUniforms(shader -> {
                Framebuffer buffer = Minecraft.getMinecraft().getFramebuffer();
                GLCompat.activeTexture(GLCompat.GL_TEXTURE0 + 3);
                GlStateManager.bindTexture(buffer.framebufferTexture);
                shader.uniform1i("fbo_tex", 3);
                GLCompat.activeTexture(GLCompat.GL_TEXTURE0 + 4);
                Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.noise_2);
                shader.uniform1i("noise", 4);
                GLCompat.activeTexture(GLCompat.GL_TEXTURE0);

                float time = (System.currentTimeMillis() % 10000000) / 1000F;
                shader.uniform1f("time", time);
                shader.uniform2f("windowSize", Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
            });
    public static Shader desaturate = HbmShaderManager2.loadShader(new ResourceLocation(Tags.MODID, "shaders/desaturate"));
    public static Shader test_trail = HbmShaderManager2.loadShader(new ResourceLocation(Tags.MODID, "shaders/trail"), shader -> {
        GLCompat.bindAttribLocation(shader, 0, "pos");
        GLCompat.bindAttribLocation(shader, 1, "tex");
        GLCompat.bindAttribLocation(shader, 2, "color");
    });
    public static Shader blit = HbmShaderManager2.loadShader(new ResourceLocation(Tags.MODID, "shaders/blit"));
    public static Shader downsample = HbmShaderManager2.loadShader(new ResourceLocation(Tags.MODID, "shaders/downsample"));
    public static Shader bloom_h = HbmShaderManager2.loadShader(new ResourceLocation(Tags.MODID, "shaders/bloom_h"));
    public static Shader bloom_v = HbmShaderManager2.loadShader(new ResourceLocation(Tags.MODID, "shaders/bloom_v"));
    public static Shader bloom_test = HbmShaderManager2.loadShader(new ResourceLocation(Tags.MODID, "shaders/bloom_test"));
    public static Shader lightning = HbmShaderManager2.loadShader(new ResourceLocation(Tags.MODID, "shaders/lightning"), shader -> {
        GLCompat.bindAttribLocation(shader, 0, "pos");
        GLCompat.bindAttribLocation(shader, 1, "tex");
        GLCompat.bindAttribLocation(shader, 2, "color");
    }).withUniforms(shader -> {
        GLCompat.activeTexture(GLCompat.GL_TEXTURE0 + 4);
        Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.noise_2);
        shader.uniform1i("noise", 4);
        GLCompat.activeTexture(GLCompat.GL_TEXTURE0);
    });
    public static Shader maxdepth = HbmShaderManager2.loadShader(new ResourceLocation(Tags.MODID, "shaders/maxdepth"));
    public static Shader lightning_gib = HbmShaderManager2.loadShader(new ResourceLocation(Tags.MODID, "shaders/lightning_gib")).withUniforms(HbmShaderManager2.LIGHTMAP, shader -> {
        GLCompat.activeTexture(GLCompat.GL_TEXTURE0 + 4);
        Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.noise_2);
        shader.uniform1i("noise", 4);
        GLCompat.activeTexture(GLCompat.GL_TEXTURE0);
    });
    public static Shader testlut = HbmShaderManager2.loadShader(new ResourceLocation(Tags.MODID, "shaders/testlut"));
    public static Shader flashlight_nogeo = HbmShaderManager2.loadShader(new ResourceLocation(Tags.MODID, "shaders/flashlight_nogeo"));
    public static Shader flashlight_deferred = HbmShaderManager2.loadShader(new ResourceLocation(Tags.MODID, "shaders/flashlight_deferred")).withUniforms(shader -> {
        shader.uniform2f("windowSize", Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
    });
    //The actual shaders used in flashlight rendering, not experimental
    public static Shader albedo = HbmShaderManager2.loadShader(new ResourceLocation(Tags.MODID, "shaders/lighting/albedo"));
    public static Shader flashlight_depth = HbmShaderManager2.loadShader(new ResourceLocation(Tags.MODID, "shaders/lighting/flashlight_depth"));
    public static Shader flashlight_post = HbmShaderManager2.loadShader(new ResourceLocation(Tags.MODID, "shaders/lighting/flashlight_post")).withUniforms(shader -> {
        shader.uniform2f("windowSize", Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
    });
    public static Shader pointlight_post = HbmShaderManager2.loadShader(new ResourceLocation(Tags.MODID, "shaders/lighting/pointlight_post")).withUniforms(shader -> {
        shader.uniform2f("windowSize", Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
    });
    public static Shader cone_volume = HbmShaderManager2.loadShader(new ResourceLocation(Tags.MODID, "shaders/lighting/cone_volume")).withUniforms(shader -> {
        shader.uniform2f("windowSize", Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
    });
    public static Shader flashlight_blit = HbmShaderManager2.loadShader(new ResourceLocation(Tags.MODID, "shaders/lighting/blit"));
    public static Shader volume_upscale = HbmShaderManager2.loadShader(new ResourceLocation(Tags.MODID, "shaders/lighting/volume_upscale")).withUniforms(shader -> {
        shader.uniform2f("windowSize", Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
    });
    public static Shader heat_distortion_post = HbmShaderManager2.loadShader(new ResourceLocation(Tags.MODID, "shaders/heat_distortion_post")).withUniforms(shader -> {
        shader.uniform2f("windowSize", Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight);
        GlStateManager.setActiveTexture(GLCompat.GL_TEXTURE0 + 4);
        Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.noise_2);
        shader.uniform1i("noise", 4);
        GlStateManager.setActiveTexture(GLCompat.GL_TEXTURE0);
        float time = (System.currentTimeMillis() % 10000000) / 1000F;
        shader.uniform1f("time", time);
    });
    public static Shader heat_distortion_new = HbmShaderManager2.loadShader(new ResourceLocation(Tags.MODID, "shaders/heat_distortion_new"));
    public static Shader crucible_lightning = HbmShaderManager2.loadShader(new ResourceLocation(Tags.MODID, "shaders/crucible_lightning"), shader -> {
        GLCompat.bindAttribLocation(shader, 0, "pos");
        GLCompat.bindAttribLocation(shader, 1, "tex");
        GLCompat.bindAttribLocation(shader, 2, "in_color");
    }).withUniforms(shader -> {
        GLCompat.activeTexture(GLCompat.GL_TEXTURE0 + 4);
        Minecraft.getMinecraft().getTextureManager().bindTexture(ResourceManager.noise_2);
        shader.uniform1i("noise", 4);
        GLCompat.activeTexture(GLCompat.GL_TEXTURE0);
    });
    public static Shader flash_lmap = HbmShaderManager2.loadShader(new ResourceLocation(Tags.MODID, "shaders/flash_lmap")).withUniforms(HbmShaderManager2.LIGHTMAP);
    public static Shader bimpact = HbmShaderManager2.loadShader(new ResourceLocation(Tags.MODID, "shaders/bimpact"), shader -> {
        GLCompat.bindAttribLocation(shader, 0, "pos");
        GLCompat.bindAttribLocation(shader, 1, "vColor");
        GLCompat.bindAttribLocation(shader, 3, "tex");
        GLCompat.bindAttribLocation(shader, 4, "lightTex");
        GLCompat.bindAttribLocation(shader, 5, "projTex");
    }).withUniforms(HbmShaderManager2.LIGHTMAP, HbmShaderManager2.WINDOW_SIZE);
    public static Shader blood_dissolve = HbmShaderManager2.loadShader(new ResourceLocation(Tags.MODID, "shaders/blood/blood")).withUniforms(HbmShaderManager2.LIGHTMAP);
    public static Shader gravitymap_render = HbmShaderManager2.loadShader(new ResourceLocation(Tags.MODID, "shaders/blood/gravitymap"));
    public static Shader blood_flow_update = HbmShaderManager2.loadShader(new ResourceLocation(Tags.MODID, "shaders/blood/blood_flow_update"));
    public static Shader gpu_particle_render = HbmShaderManager2.loadShader(new ResourceLocation(Tags.MODID, "shaders/gpu_particle_render")).withUniforms(HbmShaderManager2.MODELVIEW_MATRIX, HbmShaderManager2.PROJECTION_MATRIX, HbmShaderManager2.INV_PLAYER_ROT_MATRIX, shader -> {
        shader.uniform1i("lightmap", 1);
        shader.uniform1i("particleData0", 2);
        shader.uniform1i("particleData1", 3);
        shader.uniform1i("particleData2", 4);
        shader.uniform4f("particleTypeTexCoords[0]", NTMClientRegistry.contrail.getMinU(), NTMClientRegistry.contrail.getMinV(), NTMClientRegistry.contrail.getMaxU() - NTMClientRegistry.contrail.getMinU(), NTMClientRegistry.contrail.getMaxV() - NTMClientRegistry.contrail.getMinV());
    });
    public static Shader gpu_particle_udpate = HbmShaderManager2.loadShader(new ResourceLocation(Tags.MODID, "shaders/gpu_particle_update")).withUniforms(shader -> {
        shader.uniform1i("particleData0", 2);
        shader.uniform1i("particleData1", 3);
        shader.uniform1i("particleData2", 4);
    });

    static {
        Class<?> splash;
        if (Loader.isModLoaded(Compat.ModIds.MODERN_SPLASH)) {
            try {
                splash = Class.forName("gkappa.modernsplash.CustomSplash");
            } catch (ClassNotFoundException e) {
                throw new RuntimeException("ModernSplash loaded but failed to find gkappa.modernsplash.CustomSplash", e);
            }
        } else {
            splash = SplashProgress.class;
        }
        splashThreadGetter = MethodHandleHelper.findStaticGetter(splash, "thread", Thread.class);
        splashEnabledGetter = MethodHandleHelper.findStaticGetter(splash, "enabled", boolean.class);
        splashPauseHandle = MethodHandleHelper.findStatic(splash, "pause", MethodType.methodType(void.class));
        splashResumeHandle = MethodHandleHelper.findStatic(splash, "resume", MethodType.methodType(void.class));
    }

    public static void loadAnimatedModels() {
        supershotgun = ColladaLoader.load(new ResourceLocation(Tags.MODID, "models/anim/ssg_reload_mk2_2_newmodel.dae"));
        ssg_reload = ColladaLoader.loadAnim(1300, new ResourceLocation(Tags.MODID, "models/anim/ssg_reload_mk2_2_newmodel.dae"));

        door0 = ColladaLoader.load(new ResourceLocation(Tags.MODID, "models/anim/door0.dae"));
        door0_1 = ColladaLoader.load(new ResourceLocation(Tags.MODID, "models/anim/door0_1.dae"));
        door0_open = ColladaLoader.loadAnim(1200, new ResourceLocation(Tags.MODID, "models/anim/door0.dae"));

        silo_hatch_drillgon = ColladaLoader.load(new ResourceLocation(Tags.MODID, "models/anim/hatch.dae"));
        silo_hatch_open = ColladaLoader.loadAnim(5000, new ResourceLocation(Tags.MODID, "models/anim/hatch.dae"));

        jetpack = ColladaLoader.load(new ResourceLocation(Tags.MODID, "models/anim/jetpack.dae"));
        jetpack_activate = ColladaLoader.loadAnim(1000, new ResourceLocation(Tags.MODID, "models/anim/jetpack.dae"));

        lightning_fp = ColladaLoader.load(new ResourceLocation(Tags.MODID, "models/anim/lightning_fp_anim0.dae"));
        lightning_fp_anim = ColladaLoader.loadAnim(4160, new ResourceLocation(Tags.MODID, "models/anim/lightning_fp_anim0.dae"));

        arm_rig = ColladaLoader.load(new ResourceLocation(Tags.MODID, "models/anim/arm_rig.dae"));

        crucible_anim = ColladaLoader.load(new ResourceLocation(Tags.MODID, "models/anim/crucible_equip.dae"), true);
        crucible_equip = ColladaLoader.loadAnim(1060, new ResourceLocation(Tags.MODID, "models/anim/crucible_equip.dae"));
        hs_sword = ColladaLoader.load(new ResourceLocation(Tags.MODID, "models/anim/hs_sword_equip.dae"), true);
        hs_sword_equip = ColladaLoader.loadAnim(800, new ResourceLocation(Tags.MODID, "models/anim/hs_sword_equip.dae"));
        hf_sword = ColladaLoader.load(new ResourceLocation(Tags.MODID, "models/anim/hf_sword_equip.dae"), true);
        hf_sword_equip = ColladaLoader.loadAnim(900, new ResourceLocation(Tags.MODID, "models/anim/hf_sword_equip.dae"));

        jshotgun = ColladaLoader.load(new ResourceLocation(Tags.MODID, "models/anim/jshotgun_anim1.dae"), true);
        jshotgun_anim0 = ColladaLoader.loadAnim(1500, new ResourceLocation(Tags.MODID, "models/anim/jshotgun_anim0.dae"));
        jshotgun_anim1 = ColladaLoader.loadAnim(3000, new ResourceLocation(Tags.MODID, "models/anim/jshotgun_anim1.dae"));

        transition_seal = ColladaLoader.load(new ResourceLocation(Tags.MODID, "models/doors/seal.dae"), true);
        transition_seal_anim = ColladaLoader.loadAnim(24040, new ResourceLocation(Tags.MODID, "models/doors/seal.dae"));
    }

    // this method is called at ModelBakeEvent, which is guaranteed to be posted on client main thread!
    public static void init() {
        LensVisibilityHandler.checkSphere = new WavefrontObjDisplayList(new HFRWavefrontObject(new ResourceLocation(Tags.MODID, "models/diffractionspikechecker.obj"))).getListForName("sphere");
        Minecraft.getMinecraft().getTextureManager().bindTexture(fresnel_ms);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        Minecraft.getMinecraft().getTextureManager().bindTexture(noise_1);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
        Minecraft.getMinecraft().getTextureManager().bindTexture(noise_2);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);

        //Drillgon discovered that it messes with GL context
        pauseSplash();
        if (!WaveFrontObjectVAO.uploaded) {
            for (WaveFrontObjectVAO allVBO : WaveFrontObjectVAO.allVBOs) {
                allVBO.uploadModels();
            }
            WaveFrontObjectVAO.uploaded = true;
        }
        resumeSplash();
        KeypadClient.load();
    }

    private static void pauseSplash() {
        if (splashNotTerminated()) {
            try {
                splashPauseHandle.invokeExact();
                MainRegistry.logger.debug("[ResourceManager] successfully paused Splashscreen");
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
    }

    private static void resumeSplash() {
        if (splashNotTerminated()) {
            try {
                splashResumeHandle.invokeExact();
                MainRegistry.logger.debug("[ResourceManager] successfully resumed Splashscreen");
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
    }

    private static boolean splashNotTerminated() {
        try {
            boolean enabled = (boolean) splashEnabledGetter.invokeExact();
            if (!enabled) {
                MainRegistry.logger.debug("[ResourceManager] Splashscreen is currently disabled");
                return false;
            }
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
        Thread splashThread;
        try {
            splashThread = (Thread) splashThreadGetter.invokeExact();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
        // this line is expected throw NullPointerException
        return splashThread.getState() != Thread.State.TERMINATED;
    }


}
