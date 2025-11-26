package com.hbm.items;

import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.color.IBlockColor;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Set;

/**
 * <p>
 * Interface for blocks or items that require dynamic model support.
 * Implementors register themselves in {@link #INSTANCES} and receive callbacks
 * during the various client-side model lifecycle events. Allows for model management
 * without boilerplate json
 * </p>
 *
 * <p>
 * The interface centralizes model baking, sprite registration, color handlers,
 * and custom state mappers. Any object implementing this interface should add
 * itself to {@link #INSTANCES} in its constructor so that the static
 * registration hooks can discover and process it automatically. I suggest pairing
 * it with {@link ItemBakedBase} or {@link com.hbm.blocks.generic.BlockBakeBase}
 * or any child classes.
 * </p>
 *
 * <p>
 * Typical usage pattern:
 * <ul>
 * <li>An implementing class adds itself to {@link #INSTANCES}.</li>
 * <li>During client initialization, Forge fires model-related events.</li>
 * <li>The corresponding static methods on this interface forward those events
 * to all registered implementors.</li>
 * <li>Implementors override the relevant methods to bake models, register sprites,
 * or supply color handlers.</li>
 * </ul>
 * </p>
 *
 * <p>
 * Model Handling Responsibilities:
 * <ul>
 * <li>{@link #bakeModel(ModelBakeEvent)} — Perform model baking and register baked models.</li>
 * <li>{@link #registerModel()} — Register the unbaked model resource locations.</li>
 * <li>{@link #registerSprite(TextureMap)} — Register any custom textures required by the model.</li>
 * <li>{@link #getStateMapper(ResourceLocation)} — Optionally return a custom state mapper.</li>
 * <li>{@link #getItemColorHandler()} and {@link #getBlockColorHandler()} — Optionally supply color handlers.</li>
 * </ul>
 * </p>
 *
 * <p>
 * All model-related methods are client-only and invoked automatically by the
 * static dispatcher methods. Server-side code will never touch these.
 * </p>
 *
 * <p>
 * Author: MrNorwood
 * </p>
 **/
public interface IDynamicModels {

    /**
     * Should be populated by implementors in constructors.
     */
    //mlbv: measured size = 1106, using ReferenceOpenHashSet for a faster contains()
    Set<IDynamicModels> INSTANCES = new ReferenceOpenHashSet<>(2048);

    @SideOnly(Side.CLIENT)
    static void bakeModels(ModelBakeEvent event) {
        INSTANCES.forEach(blockMeta -> blockMeta.bakeModel(event));
    }

    @SideOnly(Side.CLIENT)
    static void registerModels() {
        INSTANCES.forEach(IDynamicModels::registerModel);
    }

    @SideOnly(Side.CLIENT)
    static void registerSprites(TextureMap map) {
        INSTANCES.forEach(dynamicSprite -> dynamicSprite.registerSprite(map));
    }

    @SideOnly(Side.CLIENT)
    static void registerCustomStateMappers() {
        for (IDynamicModels model : INSTANCES) {
            if (model.getSelf() == null || !(model.getSelf() instanceof Block block)) continue;
            StateMapperBase mapper = model.getStateMapper(block.getRegistryName());
            if (mapper != null)
                ModelLoader.setCustomStateMapper(
                        block,
                        mapper
                );
        }

    }

    @SideOnly(Side.CLIENT)
    static void registerItemColorHandlers(ColorHandlerEvent.Item evt) {
        for (IDynamicModels model : INSTANCES) {
            IItemColor colorHandler = model.getItemColorHandler();
            Object self = model.getSelf();

            if (colorHandler == null || !(self instanceof Item item)) continue;

            evt.getItemColors().registerItemColorHandler(colorHandler, item);
        }
    }

    @SideOnly(Side.CLIENT)
    static void registerBlockColorHandlers(ColorHandlerEvent.Block evt) {
        for (IDynamicModels model : INSTANCES) {
            IBlockColor colorHandler = model.getBlockColorHandler();
            Object self = model.getSelf();

            if (colorHandler == null || !(self instanceof Block item)) continue;

            evt.getBlockColors().registerBlockColorHandler(colorHandler, item);
        }
    }


    @SideOnly(Side.CLIENT)
    default IItemColor getItemColorHandler() {
        return null;
    }

    @SideOnly(Side.CLIENT)
    default IBlockColor getBlockColorHandler() {
        return null;
    }


    @SideOnly(Side.CLIENT)
    default StateMapperBase getStateMapper(ResourceLocation loc) {
        return null;
    }

    @SideOnly(Side.CLIENT)
    void bakeModel(ModelBakeEvent event);

    default Object getSelf() {
        return this;
    }


    @SideOnly(Side.CLIENT)
    void registerModel();

    @SideOnly(Side.CLIENT)
    void registerSprite(TextureMap map);

}