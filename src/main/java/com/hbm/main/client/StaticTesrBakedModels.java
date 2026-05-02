package com.hbm.main.client;

import com.hbm.Tags;
import com.hbm.blocks.ModBlocks;
import com.hbm.render.chunk.IRenderExtentsOverride;
import com.hbm.render.icon.PaddedSpriteUtil;
import com.hbm.render.icon.PaddedSpriteUtil.TextureInfo;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.render.model.*;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.IntFunction;

/**
 * Registry for static TESR replacements baked into normal block/item models.
 *
 * <p>Standard port procedure for maintainers:
 * <ol>
 *     <li>Confirm the TESR is actually static enough to bake. Good candidates only depend on blockstate or metadata,
 *     plus fixed transforms. Bad candidates still need live tile state, fluids, text, time-based animation, or dynamic
 *     world connections.</li>
 *     <li>Identify the source geometry path and texture path from the old renderer. For OBJ/HFR models, add a
 *     {@link Spec} entry here using one of:
 *     <ul>
 *         <li>{@code normalSpec(...)} for blocks that render through a single model location such as {@code normal}.</li>
 *         <li>{@code facingSpec(...)} for blocks whose world state is exposed as horizontal facing properties.</li>
 *         <li>{@code metaFacingSpec(...)} for blocks that still key their world variants on legacy numeric facing values.</li>
 *         <li>{@code variantSpec(...)} when the blockstate exposes named property variants that are not one of the
 *         common helpers above.</li>
 *     </ul>
 *     </li>
 *     <li>Translate the old GL transform stack into {@code Spec} fields:
 *     {@code yawsByMeta}, {@code preTranslate}, {@code translate}, {@code worldAngles}, {@code item(...)},
 *     {@code itemAngles(...)} and {@code gui(...)}.</li>
 *     <li>If the old TESR rendered only selected OBJ groups, use {@code parts(...)}. If it rendered multiple static
 *     layers, either wrap the baked model in {@link CompositeBakedModel} or extend the baked helper for that pattern.</li>
 *     <li>If the source texture is non-square or not mip-safe for the current atlas settings, the runtime stitch path
 *     will pad it automatically. Do not check in replacement atlas PNGs unless there is some external reason.</li>
 *     <li>If the block still returns {@code ENTITYBLOCK_ANIMATED}, switch it to {@code MODEL} when
 *     {@link #isManagedBlock(Block)} is true.</li>
 *     <li>Delete the old TESR class so the generated registrar stops binding it, then remove any now-dead
 *     {@code ResourceManager} model or texture entries.</li>
 *     <li>Build and verify in game. Look specifically for wrong facing, wrong origin, inverted normals, missing parts,
 *     and mip warnings caused by source texture dimensions.</li>
 * </ol>
 *
 * <p>If the source renderer is not using HFR/OBJ geometry, do not force it into {@link Spec}. Add a dedicated baked
 * adapter in this class or in {@code com.hbm.render.model} instead, as done for legacy {@code ModelRenderer}-based
 * models.
 */
public final class StaticTesrBakedModels {

    private static final float RAD_90 = (float) Math.toRadians(90);
    private static final float RAD_180 = (float) Math.toRadians(180);
    private static final float RAD_270 = (float) Math.toRadians(270);

    private static final List<Spec> SPECS = Arrays.asList(
            facingSpec(ModBlocks.nuke_boy, "models/bombs/lilboy.obj", "models/bombs/lilboy", yawMap().meta(2, 0).meta(3, 180).meta(4, 90).meta(5, 270).build())
                    .doubleSided()
                    .preTranslate(-2.0F, 0.0F, 0.0F)
                    .item(1.0F, 0.0F, -1.0F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(0.0D, 0.0D, 0.0D, 5.0D),
            facingSpec(ModBlocks.nuke_custom, "models/bombs/lilboy.obj", "models/bombs/customnuke", yawMap().meta(2, 0).meta(3, 180).meta(4, 90).meta(5, 270).build())
                    .doubleSided()
                    .preTranslate(-2.0F, 0.0F, 0.0F)
                    .item(1.0F, 0.0F, -1.0F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(0.0D, 0.0D, 0.0D, 5.0D),
            metaFacingSpec(ModBlocks.nuke_fleija, "models/bombs/fleija.obj", "models/bombs/fleija", yawMap().meta(2, 90).meta(3, 270).meta(4, 180).meta(5, 0).build())
                    .doubleSided()
                    .item(2.0F, RAD_90, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(0.0D, -2.0D, 0.0D, 4.5D),
            facingSpec(ModBlocks.nuke_gadget, "models/bombs/gadget.obj", "models/bombs/gadget", yawMap().meta(2, 0).meta(3, 180).meta(4, 90).meta(5, 270).build())
                    .doubleSided()
                    .item(1.0F, 0.0F, 0.25F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(0.0D, -3.0D, 0.0D, 5.0D),
            metaFacingSpec(ModBlocks.nuke_man, "models/bombs/fatman.obj", "models/bombs/fatman", yawMap().meta(2, 270).meta(3, 90).meta(4, 0).meta(5, 180).build())
                    .item(1.0F, RAD_180, 0.0F, 0.0F, 0.0F, -0.75F, 0.0F, 0.0F)
                    .gui(0.0D, -2.0D, 0.0D, 5.5D),
            facingSpec(ModBlocks.nuke_mike, "models/bombs/ivymike.obj", "models/bombs/ivymike", yawMap().meta(2, 180).meta(3, 0).meta(4, 270).meta(5, 90).build())
                    .doubleSided()
                    .item(1.0F, RAD_90, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(0.0D, -5.0D, 0.0D, 2.5D),
            facingSpec(ModBlocks.nuke_n2, "models/bombs/n2.obj", "models/bombs/n2", yawMap().meta(2, 180).meta(3, 0).meta(4, 270).meta(5, 90).build())
                    .doubleSided()
                    .item(1.0F, RAD_90, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(0.0D, -4.0D, 0.0D, 3.0D),
            facingSpec(ModBlocks.nuke_prototype, "models/bombs/prototype.obj", "models/bombs/prototype", yawMap().meta(2, 0).meta(3, 180).meta(4, 90).meta(5, 270).build())
                    .doubleSided()
                    .item(1.0F, RAD_90, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(0.0D, 0.0D, 0.0D, 2.25D),
            facingSpec(ModBlocks.nuke_solinium, "models/bombs/ufp.obj", "models/bombs/ufp", yawMap().meta(2, 90).meta(3, 270).meta(4, 180).meta(5, 0).build())
                    .doubleSided()
                    .item(1.0F, RAD_90, 0.5F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(0.0D, 0.0D, 0.0D, 4.0D),
            facingSpec(ModBlocks.nuke_tsar, "models/bombs/tsar.obj", "models/bombs/tsar", yawMap().meta(2, 0).meta(3, 180).meta(4, 90).meta(5, 270).build())
                    .doubleSided()
                    .item(1.0F, 0.0F, 1.5F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(0.0D, 0.0D, 0.0D, 2.25D),
            facingSpec(ModBlocks.bomb_multi, "models/bombs/BombGeneric.obj", "models/bombs/BombGeneric", yawMap().meta(2, 90).meta(3, 270).meta(4, 0).meta(5, 180).build())
                    .doubleSided()
                    .worldAngles(180.0D, 0.0D)
                    .translate(0.0F, 0.5F, 0.0F)
                    .item(3.0F, RAD_90, 0.75F, 0.5F, 0.0F, 0.0F, 0.0F)
                    .itemAngles(180.0D, 0.0D)
                    .gui(0.0D, -1.0D, 0.0D, 4.0D),
            normalSpec(ModBlocks.machine_storage_drum, "models/machines/drum.obj", "models/machines/drum_gray", yawMap().meta(0, 0).build())
                    .item(2.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(0.0D, -3.0D, 0.0D, 5.0D),
            normalSpec(ModBlocks.chimney_brick, "models/machines/chimney_brick.obj", "models/machines/chimney_brick", yawMap().meta(12, 0).meta(13, 0).meta(14, 0).meta(15, 0).build())
                    .doubleSided()
                    .item(0.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(0.0D, -5.0D, 0.0D, 2.25D),
            normalSpec(ModBlocks.chimney_industrial, "models/machines/chimney_industrial.obj", "models/machines/chimney_industrial", yawMap().meta(12, 180).meta(13, 180).meta(14, 180).meta(15, 180).build())
                    .doubleSided()
                    .item(0.25F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(0.0D, -5.0D, 0.0D, 2.75D),
            normalSpec(ModBlocks.machine_flare, "models/machines/flare_stack.obj", "models/machines/flare_stack", yawMap().meta(12, 180).meta(13, 180).meta(14, 180).meta(15, 180).build())
                    .doubleSided()
                    .item(0.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(0.0D, -4.0D, 0.0D, 2.5D),
            facingSpec(ModBlocks.geiger, "models/blocks/geiger_counter.obj", "blocks/geiger", yawMap().meta(2, 0).meta(3, 180).meta(4, 90).meta(5, 270).build())
                    .doubleSided()
                    .item(1.0F, RAD_90, 0.2F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(0.0D, 0.0D, 0.0D, 10.0D),
            normalSpec(ModBlocks.machine_refinery, "models/refinery.obj", "models/machines/refinery", yawMap().meta(12, 180).meta(13, 180).meta(14, 180).meta(15, 180).build())
                    .doubleSided()
                    .item(0.5F, RAD_180, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(0.0D, -4.0D, 0.0D, 3.0D),
            normalSpec(ModBlocks.machine_fraction_tower, "models/machines/fraction_tower.obj", "models/machines/fraction_tower", yawMap().meta(12, 0).meta(13, 0).meta(14, 0).meta(15, 0).build())
                    .doubleSided()
                    .item(1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(0.0D, -2.5D, 0.0D, 3.25D),
            normalSpec(ModBlocks.fraction_spacer, "models/machines/fraction_spacer.obj", "models/machines/fraction_spacer", yawMap().meta(12, 0).meta(13, 0).meta(14, 0).meta(15, 0).build())
                    .item(1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(0.0D, 0.0D, 0.0D, 3.25D),
            normalSpec(ModBlocks.machine_vacuum_distill, "models/machines/vacuum_distill.obj", "models/machines/vacuum_distill", yawMap().meta(12, 0).meta(13, 0).meta(14, 0).meta(15, 0).build())
                    .item(0.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(0.0D, -4.0D, 0.0D, 3.0D),
            normalSpec(ModBlocks.machine_hydrotreater, "models/machines/hydrotreater.obj", "models/machines/hydrotreater", yawMap().meta(12, 0).meta(13, 0).meta(14, 0).meta(15, 0).build())
                    .item(0.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(0.0D, -4.0D, 0.0D, 4.0D),
            normalSpec(ModBlocks.machine_coker, "models/machines/coker.obj", "models/machines/coker", yawMap().meta(12, 0).meta(13, 0).meta(14, 0).meta(15, 0).build())
                    .doubleSided()
                    .item(0.25F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(0.0D, -5.0D, 0.0D, 2.75D),
            normalSpec(ModBlocks.machine_industrial_boiler, "models/machines/industrial_boiler.obj", "models/machines/industrial_boiler", yawMap().meta(12, 0).meta(13, 0).meta(14, 0).meta(15, 0).build())
                    .item(1.0F, RAD_90, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(0.0D, -3.0D, 0.0D, 2.5D, 90.0D),
            normalSpec(ModBlocks.machine_deuterium_tower, "models/machines/machine_deuterium_tower.obj", "models/machines/machine_deuterium_tower", yawMap().meta(12, 180).meta(13, 0).meta(14, 270).meta(15, 90).build())
                    .preTranslate(0.5F, 0.0F, -0.5F)
                    .doubleSided()
                    .item(0.5F, RAD_180, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(0.0D, -5.0D, 0.0D, 3.0D, 180.0D),
            normalSpec(ModBlocks.machine_catalytic_cracker, "models/machines/cracking_tower.obj", "models/machines/cracking_tower", yawMap().meta(12, 90).meta(13, 270).meta(14, 180).meta(15, 0).build())
                    .doubleSided()
                    .item(0.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(0.0D, -3.5D, 0.0D, 1.8D),
            normalSpec(ModBlocks.machine_centrifuge, "models/centrifuge.obj", "models/machines/centrifuge_new", yawMap().meta(12, 0).meta(13, 180).meta(14, 90).meta(15, 270).build())
                    .item(1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(0.0D, -5.0D, 0.0D, 3.8D),
            normalSpec(ModBlocks.machine_drain, "models/machines/drain.obj", "models/machines/drain", yawMap().meta(12, 90).meta(13, 270).meta(14, 180).meta(15, 0).build())
                    .item(1.0F, RAD_180, 0.75F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(-1.0D, -1.0D, 0.0D, 5.0D, 180.0D),
            normalSpec(ModBlocks.machine_electrolyser, "models/machines/electrolyser.obj", "models/machines/electrolyser", yawMap().meta(12, 180).meta(13, 0).meta(14, 270).meta(15, 90).build())
                    .doubleSided()
                    .item(0.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(-1.0D, -1.0D, 0.0D, 2.5D),
            normalSpec(ModBlocks.heater_electric, "models/machines/electric_heater.obj", "models/machines/electric_heater", yawMap().meta(12, 90).meta(13, 270).meta(14, 180).meta(15, 0).build())
                    .item(1.9F, RAD_180, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(0.0D, -1.0D, 0.0D, 1.9D, 180.0D),
            normalSpec(ModBlocks.heater_heatex, "models/machines/heatex.obj", "models/machines/heater_heatex", yawMap().meta(12, 90).meta(13, 270).meta(14, 180).meta(15, 0).build())
                    .item(1.9F, RAD_180, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(0.0D, -1.0D, 0.0D, 1.9D, 180.0D),
            normalSpec(ModBlocks.heater_oilburner, "models/machines/oilburner.obj", "models/machines/oilburner", yawMap().meta(12, 0).meta(13, 0).meta(14, 0).meta(15, 0).build())
                    .gui(0.0D, -1.5D, 0.0D, 3.25D),
            normalSpec(ModBlocks.machine_silex, "models/machines/silex.obj", "models/machines/silex", yawMap().meta(12, 90).meta(13, 270).meta(14, 180).meta(15, 0).build())
                    .item(1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(0.0D, -2.5D, 0.0D, 3.25D),
            normalSpec(ModBlocks.machine_wood_burner, "models/machines/wood_burner.obj", "models/machines/wood_burner", yawMap().meta(12, 180).meta(13, 0).meta(14, 270).meta(15, 90).build())
                    .preTranslate(-0.5F, 0.0F, -0.5F)
                    .item(1.0F, RAD_90, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(0.0D, -4.1D, 0.0D, 3.5D, 90.0D),
            normalSpec(ModBlocks.watz, "models/reactors/watz.obj", "models/machines/watz", yawMap().meta(12, 0).meta(13, 0).meta(14, 0).meta(15, 0).build())
                    .doubleSided()
                    .item(1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(0.0D, -1.0D, 0.0D, 2.0D),
            normalSpec(ModBlocks.watz_pump, "models/machines/watz_pump.obj", "models/machines/watz_pump", yawMap().meta(12, 0).meta(13, 0).meta(14, 0).meta(15, 0).build())
                    .doubleSided()
                    .item(1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(0.0D, -1.5D, 0.0D, 5.0D),
            normalSpec(ModBlocks.machine_tower_small, "models/machines/tower_small.obj", "models/machines/tower_small", yawMap().meta(12, 0).meta(13, 0).meta(14, 0).meta(15, 0).build())
                    .doubleSided()
                    .item(0.25F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(0.0D, -4.0D, 0.0D, 3.0D),
            normalSpec(ModBlocks.machine_tower_large, "models/machines/tower_large.obj", "models/machines/tower_large", yawMap().meta(12, 0).meta(13, 0).meta(14, 0).meta(15, 0).build())
                    .doubleSided()
                    .item(0.25F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(0.0D, -3.0D, 0.0D, 3.8D),
            normalSpec(ModBlocks.sat_dock, "models/sat_dock.obj", "models/missile_parts/sat_dock", yawMap().meta(0, 0).build())
                    .item(1.0F, RAD_90, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(0.0D, 0.0D, 0.0D, 3.0D, 90.0D),
            variantSpec(ModBlocks.soyuz_capsule, "models/soyuz_lander.obj", "models/soyuz_capsule/soyuz_lander", yawMap().meta(0, -25).build(), "rusty=false")
                    .parts("Capsule")
                    .doubleSided()
                    .worldAngles(0.0D, 15.0D)
                    .translate(0.0F, -0.25F, 0.0F)
                    .withoutInventory(),
            variantSpec(ModBlocks.soyuz_capsule, "models/soyuz_lander.obj", "models/soyuz_capsule/soyuz_lander_rust", yawMap().meta(3, -25).build(), "rusty=true")
                    .parts("Capsule")
                    .doubleSided()
                    .worldAngles(0.0D, 15.0D)
                    .translate(0.0F, -0.25F, 0.0F)
                    .withoutInventory(),
            normalSpec(ModBlocks.radio_telex, "models/machines/telex.obj", "models/machines/telex", yawMap().meta(12, 90).meta(13, 270).meta(14, 180).meta(15, 0).build())
                    .doubleSided()
                    .item(1.0F, 0.0F, 0.0F, 0.0F, -0.5F, 0.0F, 0.0F)
                    .gui(0.0D, -2.0D, 0.0D, 6.0D),
            normalSpec(ModBlocks.fusion_coupler, "models/fusion/coupler.obj", "models/fusion/coupler", yawMap().meta(12, 180).meta(13, 0).meta(14, 270).meta(15, 90).build())
                    .item(0.5F, RAD_90, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(0.0D, -3.0D, 0.0D, 6.0D, 90.0D),
            normalSpec(ModBlocks.icf, "models/reactors/icf.obj", "models/machines/icf", yawMap().meta(12, 90).meta(13, 270).meta(14, 180).meta(15, 0).build())
                    .doubleSided()
                    .item(0.5F, RAD_90, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(0.0D, -1.5D, 0.0D, 2.125D, 90.0D),
            normalSpec(ModBlocks.fusion_boiler, "models/fusion/boiler.obj", "models/fusion/boiler", yawMap().meta(12, 90).meta(13, 270).meta(14, 180).meta(15, 0).build())
                    .item(0.5F, RAD_90, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(0.0D, -1.0D, 0.0D, 3.5D, 90.0D),
            normalSpec(ModBlocks.fusion_breeder, "models/fusion/breeder.obj", "models/fusion/breeder", yawMap().meta(12, 90).meta(13, 270).meta(14, 180).meta(15, 0).build())
                    .parts("Breeder")
                    .item(0.5F, RAD_90, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(0.0D, -3.0D, 0.0D, 5.0D, 90.0D),
            normalSpec(ModBlocks.fusion_collector, "models/fusion/collector.obj", "models/fusion/collector", yawMap().meta(12, 90).meta(13, 270).meta(14, 180).meta(15, 0).build())
                    .item(0.5F, RAD_90, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(0.0D, -2.0D, 0.0D, 5.0D, 90.0D),
            normalSpec(ModBlocks.plasma_heater, "models/reactors/iter.obj", "models/iter/microwave", yawMap().meta(12, 0).meta(13, 180).meta(14, 90).meta(15, 270).build())
                    .parts("Microwave")
                    .preTranslate(0.0F, 0.0F, 18.0F)
                    .item(0.5F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 14.0F)
                    .gui(0.0D, -1.0D, 0.0D, 2.5D, 90.0D),
            facingSpec(ModBlocks.fluid_pump, "models/network/fluid_diode.obj", "models/network/fluid_diode", yawMap().meta(2, 180).meta(3, 0).meta(4, 270).meta(5, 90).build())
                    .item(2.0F, RAD_90, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(0.0D, -2.0D, 0.0D, 5.0D),
            facingSpec(ModBlocks.machine_uf6_tank, "models/tank.obj", "models/machines/uf6tank", yawMap().meta(2, 0).meta(3, 180).meta(4, 90).meta(5, 270).build())
                    .item(1.0F, RAD_270, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(0.0D, -4.0D, 0.0D, 6.0D),
            facingSpec(ModBlocks.machine_puf6_tank, "models/tank.obj", "models/machines/puf6tank", yawMap().meta(2, 0).meta(3, 180).meta(4, 90).meta(5, 270).build())
                    .item(1.0F, RAD_270, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(0.0D, -4.0D, 0.0D, 6.0D),
            normalSpec(ModBlocks.pa_detector, "models/particleaccelerator/detector.obj", "models/particleaccelerator/detector", yawMap().meta(12, 90).meta(13, 270).meta(14, 180).meta(15, 0).build())
                    .translate(0.0F, -2.0F, 0.0F)
                    .item(0.5F, RAD_90, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(0.0D, -1.0D, 0.0D, 3.0D),
            normalSpec(ModBlocks.pa_source, "models/particleaccelerator/source.obj", "models/particleaccelerator/source", yawMap().meta(12, 90).meta(13, 270).meta(14, 180).meta(15, 0).build())
                    .translate(0.0F, -1.0F, 0.0F)
                    .item(0.5F, RAD_90, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(0.0D, -1.0D, 0.0D, 4.0D),
            normalSpec(ModBlocks.pa_rfc, "models/particleaccelerator/rfc.obj", "models/particleaccelerator/rfc", yawMap().meta(12, 90).meta(13, 270).meta(14, 180).meta(15, 0).build())
                    .translate(0.0F, -1.0F, 0.0F)
                    .item(0.5F, RAD_90, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(0.0D, -1.0D, 0.0D, 4.0D),
            normalSpec(ModBlocks.pa_beamline, "models/particleaccelerator/beamline.obj", "models/particleaccelerator/beamline", yawMap().meta(12, 90).meta(13, 270).meta(14, 180).meta(15, 0).build())
                    .item(1.0F, RAD_90, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(0.0D, 0.0D, 0.0D, 4.0D),
            normalSpec(ModBlocks.pa_dipole, "models/particleaccelerator/dipole.obj", "models/particleaccelerator/dipole", yawMap().meta(12, 0).meta(13, 0).meta(14, 0).meta(15, 0).build())
                    .translate(0.0F, -1.0F, 0.0F)
                    .item(1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(0.0D, -3.0D, 0.0D, 3.5D),
            normalSpec(ModBlocks.pa_quadrupole, "models/particleaccelerator/quadrupole.obj", "models/particleaccelerator/quadrupole", yawMap().meta(12, 90).meta(13, 270).meta(14, 180).meta(15, 0).build())
                    .translate(0.0F, -1.0F, 0.0F)
                    .item(1.0F, RAD_90, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(0.0D, -3.5D, 0.0D, 4.0D),
            normalSpec(ModBlocks.machine_gascent, "models/centrifuge_gas.obj", "models/machines/centrifuge_gas", yawMap().meta(12, 270).meta(13, 90).meta(14, 0).meta(15, 180).build())
                    .item(1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(0.0D, -5.0D, 0.0D, 3.8D),
            normalSpec(ModBlocks.machine_radiolysis, "models/radiolysis.obj", "models/radiolysis", yawMap().meta(12, 180).meta(13, 0).meta(14, 90).meta(15, 270).build())
                    .doubleSided()
                    .item(1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(0.0D, -2.5D, 0.0D, 3.0D),
            normalSpec(ModBlocks.machine_turbinegas, "models/machines/turbinegas.obj", "models/machines/turbinegas", yawMap().meta(12, 90).meta(13, 270).meta(14, 180).meta(15, 0).build())
                    .doubleSided()
                    .item(0.75F, RAD_90, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(-0.7D, -1.0D, 1.5D, 2.5D, 90.0D),
            normalSpec(ModBlocks.machine_fracking_tower, "models/fracking_tower.obj", "models/machines/fracking_tower", yawMap().meta(12, 180).meta(13, 180).meta(14, 180).meta(15, 180).build())
                    .doubleSided()
                    .extraWorldLayer("models/blocks/pipe_neo.obj", "blocks/pipe_silver", 0.0F, 0.5F, 0.0F, "pX", "nX", "pZ", "nZ")
                    .item(0.25F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(0.0D, -4.5D, 0.0D, 2.5D),
            normalSpec(ModBlocks.machine_solar_boiler, "models/machines/solar_boiler.obj", "models/machines/solar_boiler", yawMap().meta(12, 90).meta(13, 270).meta(14, 180).meta(15, 0).build())
                    .parts("Base")
                    .doubleSided()
                    .item(1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(0.0D, -2.5D, 0.0D, 3.25D),
            normalSpec(ModBlocks.crane_splitter, "models/blocks/crane_splitter.obj", "models/network/splitter", yawMap().meta(12, 0).meta(13, 180).meta(14, 90).meta(15, 270).build())
                    .preTranslate(-0.5F, 0.0F, 0.5F)
                    .item(1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(3.25D, 1.125D, 0.0D, 6.5D),
            normalSpec(ModBlocks.machine_well, "models/derrick.obj", "models/machines/derrick", yawMap().meta(12, 180).meta(13, 0).meta(14, 270).meta(15, 90).build())
                    .item(0.5F, RAD_90, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F)
                    .gui(0.0D, -4.0D, 0.0D, 3.0D, 90.0D),
            normalSpec(ModBlocks.reactor_zirnox, "models/zirnox.obj", "models/machines/zirnox", yawMap().meta(12, 90).meta(13, 270).meta(14, 180).meta(15, 0).build())
                    .doubleSided()
                    .item(0.75F, RAD_90, 0.5F, 1.0F, -0.3F, 0.0F, 0.0F)
                    .gui(0.5D, -1.55D, 0.0D, 2.7D, 90.0D),
            normalSpec(ModBlocks.zirnox_destroyed, "models/zirnox_destroyed.obj", "models/machines/zirnox_destroyed", yawMap().meta(12, 90).meta(13, 270).meta(14, 180).meta(15, 0).build())
                    .doubleSided()
                    .item(0.75F, RAD_90, 0.5F, 1.0F, -0.3F, 0.0F, 0.0F)
                    .gui(0.5D, -1.55D, 0.0D, 2.7D, 90.0D)
    );
    private static final Reference2ObjectOpenHashMap<Block, Spec> SPECS_BY_BLOCK = createSpecsByBlock();

    private StaticTesrBakedModels() {
    }

    public static void registerSprites(TextureMap map) {
        for (Spec spec : SPECS) {
            ResourceLocation itemTex = spec.itemTextureLocation();
            TextureInfo textureInfo = PaddedSpriteUtil.inspectTexture(itemTex, spec.textureLocation);
            PaddedSpriteUtil.register(map, textureInfo);
            for (LayerSpec layer : spec.extraWorldLayers) {
                TextureInfo layerTextureInfo = PaddedSpriteUtil.inspectTexture(layer.itemTextureLocation(),
                        layer.textureLocation);
                PaddedSpriteUtil.register(map, layerTextureInfo);
            }
        }
        registerLegacyModelRendererSprite(map, new ResourceLocation(Tags.MODID, "models/deco/ModelBroadcaster"));
        registerLegacyModelRendererSprite(map, new ResourceLocation(Tags.MODID, "models/deco/ModelRadioReceiver"));
        registerLegacyModelRendererSprite(map, new ResourceLocation(Tags.MODID, "models/deco/PoleSatelliteReceiver"));
        registerLegacyModelRendererSprite(map, new ResourceLocation(Tags.MODID, "models/turrets/ModelRadio"));
    }

    public static void bakeModels(IRegistry<ModelResourceLocation, IBakedModel> registry) {
        TextureMap atlas = Minecraft.getMinecraft().getTextureMapBlocks();

        for (Spec spec : SPECS) {
            ResourceLocation itemTex = spec.itemTextureLocation();
            TextureInfo textureInfo = PaddedSpriteUtil.inspectTexture(itemTex, spec.textureLocation);
            TextureAtlasSprite sprite = PaddedSpriteUtil.sprite(atlas, textureInfo);
            HFRWavefrontObject model = new HFRWavefrontObject(spec.modelLocation);

            StaticMetaWavefrontBakedModel worldModel = new StaticMetaWavefrontBakedModel(
                    model,
                    sprite,
                    spec.yawsByMeta,
                    spec.partNames,
                    spec.worldRoll,
                    spec.worldPitch,
                    spec.doubleSided,
                    textureInfo.uScale,
                    textureInfo.vScale,
                    spec.preTranslateX,
                    spec.preTranslateY,
                    spec.preTranslateZ,
                    spec.translateX,
                    spec.translateY,
                    spec.translateZ
            );
            int[][] autoRenderExtents = worldModel.captureRenderExtentsByMeta();
            List<IBakedModel> extraWorldModels = new ArrayList<>(spec.extraWorldLayers.size());
            for (LayerSpec layer : spec.extraWorldLayers) {
                StaticMetaWavefrontBakedModel layerModel = layer.createWorldModel(atlas, spec.yawsByMeta);
                autoRenderExtents = unionAutoRenderExtents(autoRenderExtents, layerModel.captureRenderExtentsByMeta());
                extraWorldModels.add(layerModel);
            }
            IBakedModel resolvedWorldModel = extraWorldModels.isEmpty() ? worldModel
                    : new CompositeBakedModel(worldModel, extraWorldModels.toArray(new IBakedModel[0]));
            spec.setAutoRenderExtents(autoRenderExtents);
            for (ModelResourceLocation worldLocation : spec.getWorldModelLocations()) {
                registry.putObject(worldLocation, resolvedWorldModel);
            }

            if (spec.bakeInventory) {
                StaticWavefrontItemBakedModel itemModel = new StaticWavefrontItemBakedModel(
                        model,
                        sprite,
                        spec.partNames,
                        spec.itemScale,
                        spec.itemYaw,
                        spec.doubleSided,
                        textureInfo.uScale,
                        textureInfo.vScale,
                        spec.itemRoll,
                        spec.itemPitch,
                        spec.guiTranslateX,
                        spec.guiTranslateY,
                        spec.guiTranslateZ,
                        spec.guiScale,
                        spec.guiYaw,
                        spec.itemPreTranslateX,
                        spec.itemPreTranslateY,
                        spec.itemPreTranslateZ,
                        spec.itemTranslateX,
                        spec.itemTranslateY,
                        spec.itemTranslateZ
                );
                registry.putObject(spec.getInventoryModelLocation(), itemModel);
            }
        }

        bakeLegacyModelRendererModels(registry, atlas);
    }

    public static boolean isManagedBlock(Block block) {
        return SPECS_BY_BLOCK.containsKey(block);
    }

    private static Spec facingSpec(Block block, String modelPath, String texturePath, float[] yawsByMeta) {
        ModelResourceLocation north = new ModelResourceLocation(block.getRegistryName(), "facing=north");
        ModelResourceLocation south = new ModelResourceLocation(block.getRegistryName(), "facing=south");
        ModelResourceLocation west = new ModelResourceLocation(block.getRegistryName(), "facing=west");
        ModelResourceLocation east = new ModelResourceLocation(block.getRegistryName(), "facing=east");
        return new Spec(block, modelPath, texturePath, yawsByMeta, north, south, west, east);
    }

    private static Spec normalSpec(Block block, String modelPath, String texturePath, float[] yawsByMeta) {
        ModelResourceLocation normal = new ModelResourceLocation(block.getRegistryName(), "normal");
        return new Spec(block, modelPath, texturePath, yawsByMeta, normal);
    }

    private static Spec variantSpec(Block block, String modelPath, String texturePath, float[] yawsByMeta, String... variants) {
        ModelResourceLocation[] locations = new ModelResourceLocation[variants.length];
        for (int i = 0; i < variants.length; i++) {
            locations[i] = new ModelResourceLocation(block.getRegistryName(), variants[i]);
        }
        return new Spec(block, modelPath, texturePath, yawsByMeta, locations);
    }

    private static Spec metaFacingSpec(Block block, String modelPath, String texturePath, float[] yawsByMeta) {
        ModelResourceLocation two = new ModelResourceLocation(block.getRegistryName(), "facing=2");
        ModelResourceLocation three = new ModelResourceLocation(block.getRegistryName(), "facing=3");
        ModelResourceLocation four = new ModelResourceLocation(block.getRegistryName(), "facing=4");
        ModelResourceLocation five = new ModelResourceLocation(block.getRegistryName(), "facing=5");
        return new Spec(block, modelPath, texturePath, yawsByMeta, two, three, four, five);
    }

    private static void registerLegacyModelRendererSprite(TextureMap map, ResourceLocation textureLocation) {
        ResourceLocation itemTex = new ResourceLocation(textureLocation.getNamespace(), "textures/" + textureLocation.getPath() + ".png");
        TextureInfo textureInfo = PaddedSpriteUtil.inspectTexture(itemTex, textureLocation);
        PaddedSpriteUtil.register(map, textureInfo);
    }

    private static void bakeLegacyModelRendererModels(IRegistry<ModelResourceLocation, IBakedModel> registry, TextureMap atlas) {
        bakeLegacyFacingModel(registry, atlas, ModBlocks.broadcaster_pc,
                _ -> new ModelBroadcaster(),
                new ResourceLocation(Tags.MODID, "models/deco/ModelBroadcaster"),
                yawMap().meta(2, 0).meta(3, 180).meta(4, 90).meta(5, 270).build(),
                0.0F, (float) Math.toRadians(180.0D),
                0.0F, 0.0F, 0.0F,
                0.5F, 1.5F, 0.5F);
        bakeLegacyFacingModel(registry, atlas, ModBlocks.radiorec,
                _ -> new ModelBroadcaster(),
                new ResourceLocation(Tags.MODID, "models/deco/ModelRadioReceiver"),
                yawMap().meta(2, 0).meta(3, 180).meta(4, 90).meta(5, 270).build(),
                0.0F, (float) Math.toRadians(180.0D),
                0.0F, 0.0F, 0.0F,
                0.5F, 1.5F, 0.5F);
        bakeLegacyFacingModel(registry, atlas, ModBlocks.pole_satellite_receiver,
                _ -> new ModelSatelliteReceiver(),
                new ResourceLocation(Tags.MODID, "models/deco/PoleSatelliteReceiver"),
                yawMap().meta(2, 0).meta(3, 180).meta(4, 90).meta(5, 270).build(),
                0.0F, (float) Math.toRadians(180.0D),
                0.0F, 0.0F, 0.0F,
                0.5F, 1.5F, 0.5F);

        ResourceLocation itemTex = new ResourceLocation(Tags.MODID, "textures/models/turrets/ModelRadio.png");
        TextureInfo radioboxTexture = PaddedSpriteUtil.inspectTexture(itemTex,
                new ResourceLocation(Tags.MODID, "models/turrets/ModelRadio"));
        TextureAtlasSprite radioboxSprite = atlas.getAtlasSprite(radioboxTexture.spriteLocation.toString());
        float[] radioboxYaws = yawMap()
                .meta(4, 0).meta(5, 0)
                .meta(6, 180).meta(7, 180)
                .meta(8, 270).meta(9, 270)
                .meta(10, 90).meta(11, 90)
                .build();
        StaticModelRendererBakedModel radioboxModel = new StaticModelRendererBakedModel(
                meta -> {
                    ModelRadio model = new ModelRadio();
                    model.setLeverDegrees((meta & 1) == 1 ? 160 : 20);
                    return model;
                },
                radioboxSprite,
                radioboxTexture.uScale,
                radioboxTexture.vScale,
                radioboxYaws,
                0.0F,
                (float) Math.toRadians(180.0D),
                0.0F, 0.0F, 1.0F,
                0.5F, 1.5F, 0.5F,
                0.0625F
        );
        registry.putObject(new ModelResourceLocation(ModBlocks.radiobox.getRegistryName(), "facing=north,state=false"), radioboxModel);
        registry.putObject(new ModelResourceLocation(ModBlocks.radiobox.getRegistryName(), "facing=south,state=false"), radioboxModel);
        registry.putObject(new ModelResourceLocation(ModBlocks.radiobox.getRegistryName(), "facing=west,state=false"), radioboxModel);
        registry.putObject(new ModelResourceLocation(ModBlocks.radiobox.getRegistryName(), "facing=east,state=false"), radioboxModel);
        registry.putObject(new ModelResourceLocation(ModBlocks.radiobox.getRegistryName(), "facing=north,state=true"), radioboxModel);
        registry.putObject(new ModelResourceLocation(ModBlocks.radiobox.getRegistryName(), "facing=south,state=true"), radioboxModel);
        registry.putObject(new ModelResourceLocation(ModBlocks.radiobox.getRegistryName(), "facing=west,state=true"), radioboxModel);
        registry.putObject(new ModelResourceLocation(ModBlocks.radiobox.getRegistryName(), "facing=east,state=true"), radioboxModel);
    }

    private static void bakeLegacyFacingModel(IRegistry<ModelResourceLocation, IBakedModel> registry, TextureMap atlas, Block block,
                                              IntFunction<net.minecraft.client.model.ModelBase> modelFactory, ResourceLocation textureLocation,
                                              float[] yaws, float roll, float pitch,
                                              float preTranslateX, float preTranslateY, float preTranslateZ,
                                              float tx, float ty, float tz) {
        ResourceLocation itemTex = new ResourceLocation(textureLocation.getNamespace(), "textures/" + textureLocation.getPath() + ".png");
        TextureInfo textureInfo = PaddedSpriteUtil.inspectTexture(itemTex, textureLocation);
        TextureAtlasSprite sprite = atlas.getAtlasSprite(textureInfo.spriteLocation.toString());
        IBakedModel model = new StaticModelRendererBakedModel(modelFactory, sprite, textureInfo.uScale, textureInfo.vScale, yaws, roll, pitch,
                preTranslateX, preTranslateY, preTranslateZ, tx, ty, tz, 0.0625F);
        registry.putObject(new ModelResourceLocation(block.getRegistryName(), "facing=north"), model);
        registry.putObject(new ModelResourceLocation(block.getRegistryName(), "facing=south"), model);
        registry.putObject(new ModelResourceLocation(block.getRegistryName(), "facing=west"), model);
        registry.putObject(new ModelResourceLocation(block.getRegistryName(), "facing=east"), model);
    }

    private static YawMapBuilder yawMap() {
        return new YawMapBuilder();
    }

    private static Reference2ObjectOpenHashMap<Block, Spec> createSpecsByBlock() {
        Reference2ObjectOpenHashMap<Block, Spec> specsByBlock = new Reference2ObjectOpenHashMap<>(SPECS.size());
        for (Spec spec : SPECS) {
            specsByBlock.put(spec.block, spec);
        }
        return specsByBlock;
    }

    public static int @Nullable [] getManagedRenderExtents(IBlockState state) {
        Block block = state.getBlock();
        Spec spec = SPECS_BY_BLOCK.get(block);
        if (spec == null) return null;

        if (block instanceof IRenderExtentsOverride override) {
            int[] manual = override.getRenderExtentsOverride(state);
            if (manual != null) return manual;
        }

        return spec.getAutoRenderExtents(state);
    }

    private static int[][] unionAutoRenderExtents(int[][] primary, int[][] secondary) {
        int maxLength = Math.max(primary.length, secondary.length);
        int[][] merged = new int[maxLength][];
        for (int i = 0; i < maxLength; i++) {
            int[] left = i < primary.length ? primary[i] : null;
            int[] right = i < secondary.length ? secondary[i] : null;
            merged[i] = unionRenderExtents(left, right);
        }
        return merged;
    }

    private static int[] unionRenderExtents(int[] left, int[] right) {
        if (left == null) {
            return right;
        }
        if (right == null) {
            return left;
        }
        return new int[]{
                Math.max(left[0], right[0]),
                Math.max(left[1], right[1]),
                Math.max(left[2], right[2]),
                Math.max(left[3], right[3]),
                Math.max(left[4], right[4]),
                Math.max(left[5], right[5])
        };
    }

    private static final class LayerSpec {
        private final ResourceLocation modelLocation;
        private final ResourceLocation textureLocation;
        private final String[] partNames;
        private final float translateX;
        private final float translateY;
        private final float translateZ;

        private LayerSpec(String modelPath, String texturePath, String[] partNames, float translateX, float translateY,
                          float translateZ) {
            modelLocation = new ResourceLocation(Tags.MODID, modelPath);
            textureLocation = new ResourceLocation(Tags.MODID, texturePath);
            this.partNames = partNames;
            this.translateX = translateX;
            this.translateY = translateY;
            this.translateZ = translateZ;
        }

        private ResourceLocation itemTextureLocation() {
            return new ResourceLocation(textureLocation.getNamespace(), "textures/" + textureLocation.getPath() + ".png");
        }

        private StaticMetaWavefrontBakedModel createWorldModel(TextureMap atlas, float[] yawsByMeta) {
            TextureInfo textureInfo = PaddedSpriteUtil.inspectTexture(itemTextureLocation(), textureLocation);
            TextureAtlasSprite sprite = PaddedSpriteUtil.sprite(atlas, textureInfo);
            return new StaticMetaWavefrontBakedModel(
                    new HFRWavefrontObject(modelLocation),
                    sprite,
                    yawsByMeta,
                    partNames,
                    0.0F,
                    0.0F,
                    false,
                    textureInfo.uScale,
                    textureInfo.vScale,
                    0.0F,
                    0.0F,
                    0.0F,
                    translateX,
                    translateY,
                    translateZ
            );
        }
    }

    private static final class YawMapBuilder {
        private final float[] yaws = new float[16];

        private YawMapBuilder() {
            Arrays.fill(yaws, Float.NaN);
        }

        private YawMapBuilder meta(int meta, float degrees) {
            yaws[meta] = (float) Math.toRadians(degrees);
            return this;
        }

        private float[] build() {
            return yaws;
        }
    }

    private static final class Spec {
        private final Block block;
        private final ResourceLocation modelLocation;
        private final ResourceLocation textureLocation;
        private final float[] yawsByMeta;
        private final ModelResourceLocation[] worldModelLocations;
        private String[] partNames;
        private final List<LayerSpec> extraWorldLayers = new ArrayList<>();
        private boolean bakeInventory = true;
        private boolean doubleSided;
        private int[][] autoRenderExtentsByMeta;
        private float worldRoll;
        private float worldPitch;
        private float preTranslateX;
        private float preTranslateY;
        private float preTranslateZ;
        private float translateX;
        private float translateY;
        private float translateZ;
        private float itemScale = 1.0F;
        private float itemYaw;
        private float itemRoll;
        private float itemPitch;
        private float itemPreTranslateX;
        private float itemPreTranslateY;
        private float itemPreTranslateZ;
        private float itemTranslateX;
        private float itemTranslateY;
        private float itemTranslateZ;
        private double guiTranslateX;
        private double guiTranslateY;
        private double guiTranslateZ;
        private double guiScale = 1.0D;
        private double guiYaw;

        private Spec(Block block, String modelPath, String texturePath, float[] yawsByMeta, ModelResourceLocation... worldModelLocations) {
            this.block = block;
            modelLocation = new ResourceLocation(Tags.MODID, modelPath);
            textureLocation = new ResourceLocation(Tags.MODID, texturePath);
            this.yawsByMeta = yawsByMeta;
            this.worldModelLocations = worldModelLocations;
        }

        private ResourceLocation spriteLocation() {
            ResourceLocation itemTex = itemTextureLocation();
            return PaddedSpriteUtil.inspectTexture(itemTex, textureLocation).spriteLocation;
        }

        private ResourceLocation itemTextureLocation() {
            return new ResourceLocation(textureLocation.getNamespace(), "textures/" + textureLocation.getPath() + ".png");
        }

        private Spec doubleSided() {
            doubleSided = true;
            return this;
        }

        private Spec withoutInventory() {
            bakeInventory = false;
            return this;
        }

        private Spec parts(String... names) {
            partNames = names;
            return this;
        }

        private Spec extraWorldLayer(String modelPath, String texturePath, float translateX, float translateY,
                                     float translateZ, String... partNames) {
            extraWorldLayers.add(new LayerSpec(modelPath, texturePath, partNames, translateX, translateY, translateZ));
            return this;
        }

        private Spec worldAngles(double roll, double pitch) {
            worldRoll = (float) Math.toRadians(roll);
            worldPitch = (float) Math.toRadians(pitch);
            return this;
        }

        private Spec itemAngles(double roll, double pitch) {
            itemRoll = (float) Math.toRadians(roll);
            itemPitch = (float) Math.toRadians(pitch);
            return this;
        }

        private Spec preTranslate(float x, float y, float z) {
            preTranslateX = x;
            preTranslateY = y;
            preTranslateZ = z;
            return this;
        }

        private Spec translate(float x, float y, float z) {
            translateX = x;
            translateY = y;
            translateZ = z;
            return this;
        }

        private Spec item(float scale, float yaw, float translateX, float translateY, float translateZ,
                          float preTranslateX, float preTranslateY, float preTranslateZ) {
            itemScale = scale;
            itemYaw = yaw;
            itemTranslateX = translateX;
            itemTranslateY = translateY;
            itemTranslateZ = translateZ;
            itemPreTranslateX = preTranslateX;
            itemPreTranslateY = preTranslateY;
            itemPreTranslateZ = preTranslateZ;
            return this;
        }

        private Spec item(float scale, float yaw, float translateX, float translateY, float translateZ,
                          float preTranslateX, float preTranslateY) {
            return item(scale, yaw, translateX, translateY, translateZ, preTranslateX, preTranslateY, 0.0F);
        }

        private Spec gui(double translateX, double translateY, double translateZ, double scale) {
            return gui(translateX, translateY, translateZ, scale, 0.0D);
        }

        private Spec gui(double translateX, double translateY, double translateZ, double scale, double yaw) {
            guiTranslateX = translateX;
            guiTranslateY = translateY;
            guiTranslateZ = translateZ;
            guiScale = scale;
            guiYaw = yaw;
            return this;
        }

        private ModelResourceLocation[] getWorldModelLocations() {
            return worldModelLocations;
        }

        private ModelResourceLocation getInventoryModelLocation() {
            return new ModelResourceLocation(block.getRegistryName(), "inventory");
        }

        private void setAutoRenderExtents(int[][] autoRenderExtentsByMeta) {
            this.autoRenderExtentsByMeta = autoRenderExtentsByMeta;
        }

        private int @Nullable [] getAutoRenderExtents(IBlockState state) {
            if (autoRenderExtentsByMeta == null) {
                return null;
            }

            int meta = block.getMetaFromState(state);
            if (meta < 0 || meta >= autoRenderExtentsByMeta.length) {
                return null;
            }
            return autoRenderExtentsByMeta[meta];
        }
    }
}
