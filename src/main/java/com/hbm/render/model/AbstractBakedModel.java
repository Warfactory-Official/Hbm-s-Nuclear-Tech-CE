package com.hbm.render.model;

import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.client.model.pipeline.UnpackedBakedQuad;
import net.minecraftforge.common.model.TRSRTransformation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Vector3f;

import java.util.Arrays;
import java.util.List;

@SideOnly(Side.CLIENT)
public abstract class AbstractBakedModel implements IBakedModel {

    protected static final boolean[] LEGACY_ALL_FACES = new boolean[]{true, true, true, true, true, true};
    protected static final int[] LEGACY_NO_ROTATION = new int[6];
    private static final VertexFormat LEGACY_FORMAT = DefaultVertexFormats.ITEM;

    private final boolean ambientOcclusion;
    private final boolean gui3d;
    private final boolean builtInRenderer;
    private final ItemCameraTransforms transforms;
    private final ItemOverrideList overrides;

    protected AbstractBakedModel(ItemCameraTransforms transforms) {
        this(true, true, false, transforms, ItemOverrideList.NONE);
    }

    protected AbstractBakedModel(boolean ambientOcclusion, boolean gui3d, boolean builtInRenderer, ItemCameraTransforms transforms) {
        this(ambientOcclusion, gui3d, builtInRenderer, transforms, ItemOverrideList.NONE);
    }

    protected AbstractBakedModel(boolean ambientOcclusion, boolean gui3d, boolean builtInRenderer, ItemCameraTransforms transforms, ItemOverrideList overrides) {
        this.ambientOcclusion = ambientOcclusion;
        this.gui3d = gui3d;
        this.builtInRenderer = builtInRenderer;
        this.transforms = transforms != null ? transforms : ItemCameraTransforms.DEFAULT;
        this.overrides = overrides != null ? overrides : ItemOverrideList.NONE;
    }

    @Override
    public final boolean isAmbientOcclusion() {
        return ambientOcclusion;
    }

    @Override
    public final boolean isGui3d() {
        return gui3d;
    }

    @Override
    public final boolean isBuiltInRenderer() {
        return builtInRenderer;
    }

    @Override
    public final ItemCameraTransforms getItemCameraTransforms() {
        return transforms;
    }

    @Override
    public final ItemOverrideList getOverrides() {
        return overrides;
    }

    public static void addBox(List<BakedQuad> quads, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, TextureAtlasSprite sprite) {
        FaceBakery bakery = new FaceBakery();

        Vector3f from = new Vector3f(minX * 16.0f, minY * 16.0f, minZ * 16.0f);
        Vector3f to = new Vector3f(maxX * 16.0f, maxY * 16.0f, maxZ * 16.0f);

        for (EnumFacing face : EnumFacing.VALUES) {
            BlockFaceUV uv = makeFaceUV(face, from, to);
            BlockPartFace partFace = new BlockPartFace(face, -1, "", uv);
            BakedQuad quad = bakery.makeBakedQuad(from, to, partFace, sprite, face, TRSRTransformation.identity(), null, true, true);
            quads.add(quad);
        }
    }

    public static BlockFaceUV makeFaceUV(EnumFacing face, Vector3f from, Vector3f to) {
        float u1, v1, u2, v2;
        switch (face) {
            case DOWN -> {
                u1 = from.x;
                v1 = 16f - to.z;
                u2 = to.x;
                v2 = 16f - from.z;
            }
            case UP -> {
                u1 = from.x;
                v1 = from.z;
                u2 = to.x;
                v2 = to.z;
            }
            case NORTH -> { // Z-
                u1 = 16f - to.x;
                v1 = 16f - to.y;
                u2 = 16f - from.x;
                v2 = 16f - from.y;
            }
            case SOUTH -> { // Z+
                u1 = from.x;
                v1 = 16f - to.y;
                u2 = to.x;
                v2 = 16f - from.y;
            }
            case WEST -> { // X-
                u1 = from.z;
                v1 = 16f - to.y;
                u2 = to.z;
                v2 = 16f - from.y;
            }
            case EAST -> { // X+
                u1 = 16f - to.z;
                v1 = 16f - to.y;
                u2 = 16f - from.z;
                v2 = 16f - from.y;
            }
            default -> {
                u1 = 0f;
                v1 = 0f;
                u2 = 16f;
                v2 = 16f;
            }
        }
        return new BlockFaceUV(new float[]{u1, v1, u2, v2}, 0);
    }

    protected static boolean[] legacyVisibility(boolean down, boolean up, boolean north, boolean south, boolean west, boolean east) {
        return new boolean[]{down, up, north, south, west, east};
    }

    protected static void addLegacyBox(List<BakedQuad> quads, float minX, float minY, float minZ, float maxX, float maxY, float maxZ,
                                      TextureAtlasSprite sprite, boolean[] visibleFaces, int[] rotations) {
        TextureAtlasSprite[] sprites = new TextureAtlasSprite[6];
        Arrays.fill(sprites, sprite);
        addLegacyBox(quads, minX, minY, minZ, maxX, maxY, maxZ, sprites, visibleFaces, rotations);
    }

    protected static void addLegacyBox(List<BakedQuad> quads, float minX, float minY, float minZ, float maxX, float maxY, float maxZ,
                                      TextureAtlasSprite[] sprites, boolean[] visibleFaces, int[] rotations) {
        if (minX == maxX || minY == maxY || minZ == maxZ) return;

        for (EnumFacing face : EnumFacing.VALUES) {
            int index = face.getIndex();
            if (!visibleFaces[index]) continue;
            TextureAtlasSprite sprite = sprites[index];
            if (sprite == null) continue;
            quads.add(buildLegacyQuad(face, minX, minY, minZ, maxX, maxY, maxZ, sprite, rotations[index]));
        }
    }

    protected static BakedQuad buildLegacyQuad(EnumFacing face, float minX, float minY, float minZ, float maxX, float maxY, float maxZ,
                                              TextureAtlasSprite sprite, int rotation) {
        float uMin;
        float uMax;
        float vMin;
        float vMax;
        float uBottomRight;
        float uTopLeft;
        float vTopLeft;
        float vBottomRight;
        float[] px = new float[4];
        float[] py = new float[4];
        float[] pz = new float[4];
        float[] uu = new float[4];
        float[] vv = new float[4];

        switch (face) {
            case DOWN:
                uMin = minX;
                uMax = maxX;
                vMin = minZ;
                vMax = maxZ;
                uBottomRight = uMax;
                uTopLeft = uMin;
                vTopLeft = vMin;
                vBottomRight = vMax;
                if (rotation == 2) {
                    uMin = minZ;
                    vMin = 16.0F - maxX;
                    uMax = maxZ;
                    vMax = 16.0F - minX;
                    vTopLeft = vMin;
                    vBottomRight = vMax;
                    uBottomRight = uMin;
                    uTopLeft = uMax;
                    vMin = vMax;
                    vMax = vTopLeft;
                } else if (rotation == 1) {
                    uMin = 16.0F - maxZ;
                    vMin = minX;
                    uMax = 16.0F - minZ;
                    vMax = maxX;
                    uBottomRight = uMax;
                    uTopLeft = uMin;
                    uMin = uMax;
                    uMax = uTopLeft;
                    vTopLeft = vMax;
                    vBottomRight = vMin;
                } else if (rotation == 3) {
                    uMin = 16.0F - minX;
                    uMax = 16.0F - maxX;
                    vMin = 16.0F - minZ;
                    vMax = 16.0F - maxZ;
                    uBottomRight = uMax;
                    uTopLeft = uMin;
                    vTopLeft = vMin;
                    vBottomRight = vMax;
                }
                setLegacyVertex(px, py, pz, uu, vv, 0, minX, minY, maxZ, uTopLeft, vBottomRight);
                setLegacyVertex(px, py, pz, uu, vv, 1, minX, minY, minZ, uMin, vMin);
                setLegacyVertex(px, py, pz, uu, vv, 2, maxX, minY, minZ, uBottomRight, vTopLeft);
                setLegacyVertex(px, py, pz, uu, vv, 3, maxX, minY, maxZ, uMax, vMax);
                break;
            case UP:
                uMin = minX;
                uMax = maxX;
                vMin = minZ;
                vMax = maxZ;
                uBottomRight = uMax;
                uTopLeft = uMin;
                vTopLeft = vMin;
                vBottomRight = vMax;
                if (rotation == 1) {
                    uMin = minZ;
                    vMin = 16.0F - maxX;
                    uMax = maxZ;
                    vMax = 16.0F - minX;
                    vTopLeft = vMin;
                    vBottomRight = vMax;
                    uBottomRight = uMin;
                    uTopLeft = uMax;
                    vMin = vMax;
                    vMax = vTopLeft;
                } else if (rotation == 2) {
                    uMin = 16.0F - maxZ;
                    vMin = minX;
                    uMax = 16.0F - minZ;
                    vMax = maxX;
                    uBottomRight = uMax;
                    uTopLeft = uMin;
                    uMin = uMax;
                    uMax = uTopLeft;
                    vTopLeft = vMax;
                    vBottomRight = vMin;
                } else if (rotation == 3) {
                    uMin = 16.0F - minX;
                    uMax = 16.0F - maxX;
                    vMin = 16.0F - minZ;
                    vMax = 16.0F - maxZ;
                    uBottomRight = uMax;
                    uTopLeft = uMin;
                    vTopLeft = vMin;
                    vBottomRight = vMax;
                }
                setLegacyVertex(px, py, pz, uu, vv, 0, maxX, maxY, maxZ, uMax, vMax);
                setLegacyVertex(px, py, pz, uu, vv, 1, maxX, maxY, minZ, uBottomRight, vTopLeft);
                setLegacyVertex(px, py, pz, uu, vv, 2, minX, maxY, minZ, uMin, vMin);
                setLegacyVertex(px, py, pz, uu, vv, 3, minX, maxY, maxZ, uTopLeft, vBottomRight);
                break;
            case NORTH:
                uMin = minX;
                uMax = maxX;
                vMin = 16.0F - maxY;
                vMax = 16.0F - minY;
                uBottomRight = uMax;
                uTopLeft = uMin;
                vTopLeft = vMin;
                vBottomRight = vMax;
                if (rotation == 2) {
                    uMin = minY;
                    uMax = maxY;
                    vMin = 16.0F - minX;
                    vMax = 16.0F - maxX;
                    vTopLeft = vMin;
                    vBottomRight = vMax;
                    uBottomRight = uMin;
                    uTopLeft = uMax;
                    vMin = vMax;
                    vMax = vTopLeft;
                } else if (rotation == 1) {
                    uMin = 16.0F - maxY;
                    uMax = 16.0F - minY;
                    vMin = maxX;
                    vMax = minX;
                    uBottomRight = uMax;
                    uTopLeft = uMin;
                    uMin = uMax;
                    uMax = uTopLeft;
                    vTopLeft = vMax;
                    vBottomRight = vMin;
                } else if (rotation == 3) {
                    uMin = 16.0F - minX;
                    uMax = 16.0F - maxX;
                    vMin = maxY;
                    vMax = minY;
                    uBottomRight = uMax;
                    uTopLeft = uMin;
                    vTopLeft = vMin;
                    vBottomRight = vMax;
                }
                setLegacyVertex(px, py, pz, uu, vv, 0, minX, maxY, minZ, uBottomRight, vTopLeft);
                setLegacyVertex(px, py, pz, uu, vv, 1, maxX, maxY, minZ, uMin, vMin);
                setLegacyVertex(px, py, pz, uu, vv, 2, maxX, minY, minZ, uTopLeft, vBottomRight);
                setLegacyVertex(px, py, pz, uu, vv, 3, minX, minY, minZ, uMax, vMax);
                break;
            case SOUTH:
                uMin = minX;
                uMax = maxX;
                vMin = 16.0F - maxY;
                vMax = 16.0F - minY;
                uBottomRight = uMax;
                uTopLeft = uMin;
                vTopLeft = vMin;
                vBottomRight = vMax;
                if (rotation == 1) {
                    uMin = minY;
                    vMax = 16.0F - minX;
                    uMax = maxY;
                    vMin = 16.0F - maxX;
                    vTopLeft = vMin;
                    vBottomRight = vMax;
                    uBottomRight = uMin;
                    uTopLeft = uMax;
                    vMin = vMax;
                    vMax = vTopLeft;
                } else if (rotation == 2) {
                    uMin = 16.0F - maxY;
                    vMin = minX;
                    uMax = 16.0F - minY;
                    vMax = maxX;
                    uBottomRight = uMax;
                    uTopLeft = uMin;
                    uMin = uMax;
                    uMax = uTopLeft;
                    vTopLeft = vMax;
                    vBottomRight = vMin;
                } else if (rotation == 3) {
                    uMin = 16.0F - minX;
                    uMax = 16.0F - maxX;
                    vMin = maxY;
                    vMax = minY;
                    uBottomRight = uMax;
                    uTopLeft = uMin;
                    vTopLeft = vMin;
                    vBottomRight = vMax;
                }
                setLegacyVertex(px, py, pz, uu, vv, 0, minX, maxY, maxZ, uMin, vMin);
                setLegacyVertex(px, py, pz, uu, vv, 1, minX, minY, maxZ, uTopLeft, vBottomRight);
                setLegacyVertex(px, py, pz, uu, vv, 2, maxX, minY, maxZ, uMax, vMax);
                setLegacyVertex(px, py, pz, uu, vv, 3, maxX, maxY, maxZ, uBottomRight, vTopLeft);
                break;
            case WEST:
                uMin = minZ;
                uMax = maxZ;
                vMin = 16.0F - maxY;
                vMax = 16.0F - minY;
                uBottomRight = uMax;
                uTopLeft = uMin;
                vTopLeft = vMin;
                vBottomRight = vMax;
                if (rotation == 1) {
                    uMin = minY;
                    vMin = 16.0F - maxZ;
                    uMax = maxY;
                    vMax = 16.0F - minZ;
                    vTopLeft = vMin;
                    vBottomRight = vMax;
                    uBottomRight = uMin;
                    uTopLeft = uMax;
                    vMin = vMax;
                    vMax = vTopLeft;
                } else if (rotation == 2) {
                    uMin = 16.0F - maxY;
                    vMin = minZ;
                    uMax = 16.0F - minY;
                    vMax = maxZ;
                    uBottomRight = uMax;
                    uTopLeft = uMin;
                    uMin = uMax;
                    uMax = uTopLeft;
                    vTopLeft = vMax;
                    vBottomRight = vMin;
                } else if (rotation == 3) {
                    uMin = 16.0F - minZ;
                    uMax = 16.0F - maxZ;
                    vMin = maxY;
                    vMax = minY;
                    uBottomRight = uMax;
                    uTopLeft = uMin;
                    vTopLeft = vMin;
                    vBottomRight = vMax;
                }
                setLegacyVertex(px, py, pz, uu, vv, 0, minX, maxY, maxZ, uBottomRight, vTopLeft);
                setLegacyVertex(px, py, pz, uu, vv, 1, minX, maxY, minZ, uMin, vMin);
                setLegacyVertex(px, py, pz, uu, vv, 2, minX, minY, minZ, uTopLeft, vBottomRight);
                setLegacyVertex(px, py, pz, uu, vv, 3, minX, minY, maxZ, uMax, vMax);
                break;
            case EAST:
            default:
                uMin = minZ;
                uMax = maxZ;
                vMin = 16.0F - maxY;
                vMax = 16.0F - minY;
                uBottomRight = uMax;
                uTopLeft = uMin;
                vTopLeft = vMin;
                vBottomRight = vMax;
                if (rotation == 2) {
                    uMin = minY;
                    vMin = 16.0F - minZ;
                    uMax = maxY;
                    vMax = 16.0F - maxZ;
                    vTopLeft = vMin;
                    vBottomRight = vMax;
                    uBottomRight = uMin;
                    uTopLeft = uMax;
                    vMin = vMax;
                    vMax = vTopLeft;
                } else if (rotation == 1) {
                    uMin = 16.0F - maxY;
                    vMin = maxZ;
                    uMax = 16.0F - minY;
                    vMax = minZ;
                    uBottomRight = uMax;
                    uTopLeft = uMin;
                    uMin = uMax;
                    uMax = uTopLeft;
                    vTopLeft = vMax;
                    vBottomRight = vMin;
                } else if (rotation == 3) {
                    uMin = 16.0F - minZ;
                    uMax = 16.0F - maxZ;
                    vMin = maxY;
                    vMax = minY;
                    uBottomRight = uMax;
                    uTopLeft = uMin;
                    vTopLeft = vMin;
                    vBottomRight = vMax;
                }
                setLegacyVertex(px, py, pz, uu, vv, 0, maxX, minY, maxZ, uTopLeft, vBottomRight);
                setLegacyVertex(px, py, pz, uu, vv, 1, maxX, minY, minZ, uMax, vMax);
                setLegacyVertex(px, py, pz, uu, vv, 2, maxX, maxY, minZ, uBottomRight, vTopLeft);
                setLegacyVertex(px, py, pz, uu, vv, 3, maxX, maxY, maxZ, uMin, vMin);
                break;
        }

        UnpackedBakedQuad.Builder builder = new UnpackedBakedQuad.Builder(LEGACY_FORMAT);
        builder.setQuadOrientation(face);
        builder.setTexture(sprite);
        builder.setApplyDiffuseLighting(true);

        Vector3f normal = new Vector3f((float) face.getXOffset(), (float) face.getYOffset(), (float) face.getZOffset());
        for (int i = 0; i < 4; i++) {
            putLegacyVertex(builder, px[i] / 16.0F, py[i] / 16.0F, pz[i] / 16.0F, uu[i], vv[i], normal, sprite);
        }

        return builder.build();
    }

    private static void putLegacyVertex(UnpackedBakedQuad.Builder builder, float x, float y, float z, float u16, float v16,
                                 Vector3f normal, TextureAtlasSprite sprite) {
        for (int elementIndex = 0; elementIndex < LEGACY_FORMAT.getElementCount(); elementIndex++) {
            VertexFormatElement element = LEGACY_FORMAT.getElement(elementIndex);
            switch (element.getUsage()) {
                case POSITION:
                    builder.put(elementIndex, x, y, z);
                    break;
                case COLOR:
                    builder.put(elementIndex, 1.0F, 1.0F, 1.0F, 1.0F);
                    break;
                case UV:
                    if (element.getIndex() == 0) {
                        builder.put(elementIndex, sprite.getInterpolatedU(u16), sprite.getInterpolatedV(v16));
                    } else {
                        builder.put(elementIndex, 0.0F, 0.0F);
                    }
                    break;
                case NORMAL:
                    builder.put(elementIndex, normal.x, normal.y, normal.z);
                    break;
                case PADDING:
                    builder.put(elementIndex, 0.0F);
                    break;
                default:
                    builder.put(elementIndex);
                    break;
            }
        }
    }

    private static void setLegacyVertex(float[] px, float[] py, float[] pz, float[] uu, float[] vv, int index,
                                        float x, float y, float z, float u, float v) {
        px[index] = x;
        py[index] = y;
        pz[index] = z;
        uu[index] = u;
        vv[index] = v;
    }
}
