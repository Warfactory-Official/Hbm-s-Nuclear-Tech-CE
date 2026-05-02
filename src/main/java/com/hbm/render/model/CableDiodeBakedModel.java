package com.hbm.render.model;

import com.hbm.Tags;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.network.energy.CableDiode;
import com.hbm.main.ResourceManager;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemTransformVec3f;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.util.vector.Vector3f;

import java.util.*;

import net.minecraftforge.common.property.IExtendedBlockState;

import static com.hbm.blocks.network.energy.CableDiode.FACING;

@SideOnly(Side.CLIENT)
public class CableDiodeBakedModel extends AbstractWavefrontBakedModel {

    private static final float WIDTH = 0.875f;
    private static final float MIN_XYZ = 0.5f - 0.375f;
    private static final float MAX_XYZ = 0.5f + 0.375f;

    private final TextureAtlasSprite diodeSprite;
    private final TextureAtlasSprite cableSprite;
    private final TextureAtlasSprite padSprite;
    private final boolean isInventory;
    @SuppressWarnings("unchecked")
    private final List<BakedQuad>[] cache = new List[64 * 6];
    private List<BakedQuad> inventoryCache;

    public CableDiodeBakedModel(TextureAtlasSprite diodeSprite, TextureAtlasSprite cableSprite, boolean isInventory) {
        super(ResourceManager.cable_neo_obj, isInventory ? DefaultVertexFormats.ITEM : DefaultVertexFormats.BLOCK, 1.0F, 0.0F, 0.0F, 0.0F, makeItemTransforms());
        this.diodeSprite = diodeSprite;
        this.cableSprite = cableSprite;
        this.isInventory = isInventory;
        this.padSprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(new ResourceLocation(Tags.MODID, "blocks/" + ModBlocks.hadron_coil_alloy.getRegistryName().getPath()).toString());
    }

    @SuppressWarnings("deprecation")
    private static ItemCameraTransforms makeItemTransforms() {
        ItemTransformVec3f gui = new ItemTransformVec3f(new Vector3f(30, -135, 0), new Vector3f(0, -0.1f, 0), new Vector3f(0.575f, 0.575f, 0.575f));
        ItemTransformVec3f thirdPerson = new ItemTransformVec3f(new Vector3f(75, 45, 0), new Vector3f(0, 1.5f / 16f, -2.5f / 16f), new Vector3f(0.4f, 0.4f, 0.4f));
        ItemTransformVec3f firstPerson = new ItemTransformVec3f(new Vector3f(0, 45, 0), new Vector3f(0, 0, 0), new Vector3f(0.4f, 0.4f, 0.4f));
        ItemTransformVec3f ground = new ItemTransformVec3f(new Vector3f(0, 0, 0), new Vector3f(0, 2f / 16f, 0), new Vector3f(0.4f, 0.4f, 0.4f));

        return new ItemCameraTransforms(thirdPerson, thirdPerson, firstPerson, firstPerson, BakedModelTransforms.standardBlock().head, gui, ground, BakedModelTransforms.standardBlock().fixed);
    }

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        if (side != null) return Collections.emptyList();

        if (isInventory) {
            if (inventoryCache != null) return inventoryCache;
            return inventoryCache = Collections.unmodifiableList(buildItemQuads());
        }

        if (state == null) {
            return Collections.emptyList();
        }

        EnumFacing facing = state.getValue(FACING);
        int mask = 0;
        if (state instanceof IExtendedBlockState ext) {
            try {
                Integer value = ext.getValue(CableDiode.CONNECTION_MASK);
                mask = value != null ? value : 0;
            } catch (Exception ignored) {
            }
        }

        int cacheIndex = mask * 6 + facing.getIndex();
        if (cache[cacheIndex] != null) return cache[cacheIndex];
        return cache[cacheIndex] = Collections.unmodifiableList(buildWorldQuads(facing, mask));
    }

    private List<BakedQuad> buildItemQuads() {
        List<BakedQuad> quads = new ArrayList<>();
        addLegacyBox(quads, 0.0F, 14.0F, 0.0F, 16.0F, 16.0F, 16.0F, diodeSprite, LEGACY_ALL_FACES, LEGACY_NO_ROTATION);
        addLegacyBox(quads, MIN_XYZ * 16.0F, MIN_XYZ * 16.0F, MIN_XYZ * 16.0F, MAX_XYZ * 16.0F, MAX_XYZ * 16.0F, MAX_XYZ * 16.0F, padSprite, LEGACY_ALL_FACES, LEGACY_NO_ROTATION);
        quads.addAll(bakeSimpleQuads(Set.of("posX", "negX", "negY", "posZ", "negZ"), 0.0F, 0.0F, (float) Math.PI, true, true, cableSprite));
        return quads;
    }

    private List<BakedQuad> buildWorldQuads(EnumFacing facing, int mask) {
        List<BakedQuad> quads = new ArrayList<>();
        boolean pX = (mask & 1) != 0;
        boolean nX = (mask & (1 << 1)) != 0;
        boolean pY = (mask & (1 << 2)) != 0;
        boolean nY = (mask & (1 << 3)) != 0;
        boolean pZ = (mask & (1 << 4)) != 0;
        boolean nZ = (mask & (1 << 5)) != 0;

        addLegacyBox(
                quads,
                (facing == EnumFacing.WEST ? WIDTH : 0.0F) * 16.0F,
                (facing == EnumFacing.DOWN ? WIDTH : 0.0F) * 16.0F,
                (facing == EnumFacing.NORTH ? WIDTH : 0.0F) * 16.0F,
                (1.0F - (facing == EnumFacing.EAST ? WIDTH : 0.0F)) * 16.0F,
                (1.0F - (facing == EnumFacing.UP ? WIDTH : 0.0F)) * 16.0F,
                (1.0F - (facing == EnumFacing.SOUTH ? WIDTH : 0.0F)) * 16.0F,
                diodeSprite,
                LEGACY_ALL_FACES,
                LEGACY_NO_ROTATION);
        addLegacyBox(quads, MIN_XYZ * 16.0F, MIN_XYZ * 16.0F, MIN_XYZ * 16.0F, MAX_XYZ * 16.0F, MAX_XYZ * 16.0F, MAX_XYZ * 16.0F, padSprite, LEGACY_ALL_FACES, LEGACY_NO_ROTATION);

        if (pX) quads.addAll(bakeSimpleQuads(Collections.singleton("posX"), 0.0F, 0.0F, 0.0F, true, true, cableSprite));
        if (nX) quads.addAll(bakeSimpleQuads(Collections.singleton("negX"), 0.0F, 0.0F, 0.0F, true, true, cableSprite));
        if (pY) quads.addAll(bakeSimpleQuads(Collections.singleton("posY"), 0.0F, 0.0F, 0.0F, true, true, cableSprite));
        if (nY) quads.addAll(bakeSimpleQuads(Collections.singleton("negY"), 0.0F, 0.0F, 0.0F, true, true, cableSprite));
        if (nZ) quads.addAll(bakeSimpleQuads(Collections.singleton("posZ"), 0.0F, 0.0F, 0.0F, true, true, cableSprite));
        if (pZ) quads.addAll(bakeSimpleQuads(Collections.singleton("negZ"), 0.0F, 0.0F, 0.0F, true, true, cableSprite));

        return quads;
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleTexture() {
        return diodeSprite;
    }
}
