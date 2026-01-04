package com.hbm.blocks.network;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.hbm.Tags;
import com.hbm.api.block.IToolable;
import com.hbm.blocks.ModSoundTypes;
import com.hbm.blocks.generic.BlockBakeBase;
import com.hbm.interfaces.AutoRegister;
import com.hbm.interfaces.ICopiable;
import com.hbm.lib.ForgeDirection;
import com.hbm.main.MainRegistry;
import com.hbm.render.block.BlockBakeFrame;
import com.hbm.render.model.BakedModelTransforms;
import com.hbm.tileentity.network.TileEntityPneumoTube;
import com.hbm.util.Compat;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
// there were some fucking mumbo jumbo conversions with forgedirection <-> enumfacing, don't mind me
public class PneumoTubePaintableBlock extends BlockBakeBase implements IToolable {

    public static final IUnlistedProperty<IBlockState> DISGUISED_STATE = new SimpleUnlistedProperty<>("disguised_state", IBlockState.class);
    public static final IUnlistedProperty<EnumFacing> INSERTION_DIR = new SimpleUnlistedProperty<>("insertion_dir", EnumFacing.class);
    public static final IUnlistedProperty<EnumFacing> EJECTION_DIR = new SimpleUnlistedProperty<>("ejection_dir", EnumFacing.class);

    @SideOnly(Side.CLIENT)
    private static TextureAtlasSprite baseSprite;
    @SideOnly(Side.CLIENT)
    private static TextureAtlasSprite overlaySprite;
    @SideOnly(Side.CLIENT)
    private static TextureAtlasSprite overlayInSprite;
    @SideOnly(Side.CLIENT)
    private static TextureAtlasSprite overlayOutSprite;

    public PneumoTubePaintableBlock(String name) {
        super(Material.IRON, name, new BlockBakeFrame("pneumatic_tube_paintable"));
        this.setDefaultState(this.blockState.getBaseState());
        this.setSoundType(ModSoundTypes.pipe);
        this.useNeighborBrightness = true;
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, new IProperty[0], new IUnlistedProperty[]{DISGUISED_STATE, INSERTION_DIR, EJECTION_DIR});
    }

    @Override
    public boolean hasTileEntity(IBlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(World world, IBlockState state) {
        return new TileEntityPneumoTubePaintable();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState();
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return 0;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean causesSuffocation(IBlockState state) {
        return false;
    }

    @Override
    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
        return true;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
                                    EnumFacing facing, float hitX, float hitY, float hitZ) {

        ItemStack stack = player.getHeldItem(hand);
        if(!stack.isEmpty() && stack.getItem() instanceof ItemBlock ib) {
            Block disguise = ib.getBlock();
            if(disguise != this) {
                IBlockState disguiseState = disguise.getStateFromMeta(stack.getMetadata());
                if(disguiseState.isFullCube() && disguiseState.isOpaqueCube()) {
                    TileEntity tile = world.getTileEntity(pos);
                    if(tile instanceof TileEntityPneumoTubePaintable tube && tube.block == null) {
                        if(!world.isRemote) {
                            tube.block = disguise;
                            tube.meta = stack.getMetadata() & 15;
                            tube.markDirty();
                            world.markChunkDirty(pos, tube);
                            world.notifyBlockUpdate(pos, state, state, 3);
                        }
                        return true;
                    }
                }
            }
        } else if(!stack.isEmpty()) {
            ToolType type = ToolType.getType(stack);
            if(type == ToolType.SCREWDRIVER || type == ToolType.HAND_DRILL) return false;
        }

        if(!player.isSneaking()) {
            TileEntity tile = world.getTileEntity(pos);
            if(tile instanceof TileEntityPneumoTube tube) {
                if(tube.isCompressor()) {
                    if (!world.isRemote) FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
                    return true;
                } else if(tube.isEndpoint()) {
                    if (!world.isRemote) FMLNetworkHandler.openGui(player, MainRegistry.instance, 1, world, pos.getX(), pos.getY(), pos.getZ());
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, EnumFacing side, float fX, float fY, float fZ, EnumHand hand, ToolType tool) {
        BlockPos pos = new BlockPos(x, y, z);
        TileEntity tile = world.getTileEntity(pos);

        if (tool == ToolType.HAND_DRILL) {
            if (tile instanceof TileEntityPneumoTubePaintable tube && tube.block != null) {
                if (!world.isRemote) {
                    tube.block = null;
                    tube.meta = 0;
                    tube.markDirty();
                    world.markChunkDirty(pos, tube);
                    world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
                }
                return true;
            }
            return false;
        } else if (tool == ToolType.SCREWDRIVER) {
            if (!(tile instanceof TileEntityPneumoTube tube)) {
                return false;
            }
            if (world.isRemote) {
                return true;
            }

            ForgeDirection rot = player.isSneaking() ? tube.ejectionDir : tube.insertionDir;
            ForgeDirection oth = player.isSneaking() ? tube.insertionDir : tube.ejectionDir;

            for (int i = 0; i < 7; i++) {
                rot = ForgeDirection.getOrientation((rot.ordinal() + 1) % 7);
                if (rot == ForgeDirection.UNKNOWN) break; //unknown is always valid, simply disables this part
                if (rot == oth) continue; //skip if both positions collide
                TileEntity neighbor = Compat.getTileStandard(world, x + rot.offsetX, y + rot.offsetY, z + rot.offsetZ);
                if (neighbor == null || neighbor instanceof TileEntityPneumoTube) continue;
                if(PneumoTube.hasItemHandler(neighbor, rot)) break;
                if(neighbor instanceof IInventory) break; //fallback for legacy inventories
            }

            if(player.isSneaking()) tube.ejectionDir = rot; else tube.insertionDir = rot;

            tube.markDirty();
            world.markChunkDirty(pos, tube);
            world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
            return true;
        }

        return false;
    }

    @Override
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        if(!(state instanceof IExtendedBlockState ext)) return state;
        TileEntity tile = world.getTileEntity(pos);
        if(tile instanceof TileEntityPneumoTubePaintable tube) {
            IBlockState disguiseState = tube.block != null ? tube.block.getStateFromMeta(tube.meta) : null;
            EnumFacing insertion = tube.insertionDir != null ? tube.insertionDir.toEnumFacing() : null;
            EnumFacing ejection = tube.ejectionDir != null ? tube.ejectionDir.toEnumFacing() : null;
            return ext.withProperty(DISGUISED_STATE, disguiseState).withProperty(INSERTION_DIR, insertion).withProperty(EJECTION_DIR, ejection);
        }
        return ext.withProperty(DISGUISED_STATE, null).withProperty(INSERTION_DIR, null).withProperty(EJECTION_DIR, null);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerSprite(TextureMap map) {
        super.registerSprite(map);
        baseSprite = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/pneumatic_tube_paintable"));
        overlaySprite = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/pneumatic_tube_paintable_overlay"));
        overlayInSprite = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/pneumatic_tube_paintable_overlay_in"));
        overlayOutSprite = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/pneumatic_tube_paintable_overlay_out"));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void bakeModel(ModelBakeEvent event) {
        TextureMap textureMapBlocks = Minecraft.getMinecraft().getTextureMapBlocks();
        TextureAtlasSprite base = baseSprite != null ? baseSprite : textureMapBlocks.getAtlasSprite(new ResourceLocation(Tags.MODID, "blocks/pneumatic_tube_paintable").toString());
        TextureAtlasSprite overlay = overlaySprite != null ? overlaySprite : textureMapBlocks.getAtlasSprite(new ResourceLocation(Tags.MODID, "blocks/pneumatic_tube_paintable_overlay").toString());
        TextureAtlasSprite overlayIn = overlayInSprite != null ? overlayInSprite : textureMapBlocks.getAtlasSprite(new ResourceLocation(Tags.MODID, "blocks/pneumatic_tube_paintable_overlay_in").toString());
        TextureAtlasSprite overlayOut = overlayOutSprite != null ? overlayOutSprite : textureMapBlocks.getAtlasSprite(new ResourceLocation(Tags.MODID, "blocks/pneumatic_tube_paintable_overlay_out").toString());

        PneumoTubePaintableModel model = new PneumoTubePaintableModel(base, overlay, overlayIn, overlayOut);
        ModelResourceLocation inventory = new ModelResourceLocation(Objects.requireNonNull(getRegistryName()), "inventory");
        ModelResourceLocation normal = new ModelResourceLocation(Objects.requireNonNull(getRegistryName()), "normal");

        event.getModelRegistry().putObject(inventory, model);
        event.getModelRegistry().putObject(normal, model);
    }
    @AutoRegister
    public static class TileEntityPneumoTubePaintable extends TileEntityPneumoTube implements ICopiable {

        public Block block;
        public int meta;
        private Block lastBlock;
        private int lastMeta;

        @Override
        public void update() {
            super.update();
            if (world != null && world.isRemote) {
                if (block != lastBlock || meta != lastMeta) {
                    world.markBlockRangeForRenderUpdate(pos, pos);
                    lastBlock = block;
                    lastMeta = meta;
                }
            }
        }

        @Override
        public void readFromNBT(NBTTagCompound nbt) {
            super.readFromNBT(nbt);
            if (nbt.hasKey("block", Constants.NBT.TAG_STRING)) {
                ResourceLocation loc = new ResourceLocation(nbt.getString("block"));
                this.block = ForgeRegistries.BLOCKS.getValue(loc);
            } else {
                this.block = null;
            }
            this.meta = nbt.getInteger("meta");
        }

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
            super.writeToNBT(nbt);
            if (block != null) {
                ResourceLocation key = ForgeRegistries.BLOCKS.getKey(block);
                if (key != null) {
                    nbt.setString("block", key.toString());
                }
            }
            nbt.setInteger("meta", meta);
            return nbt;
        }

        @Override
        public SPacketUpdateTileEntity getUpdatePacket() {
            NBTTagCompound nbt = new NBTTagCompound();
            this.writeToNBT(nbt);
            nbt.setInteger("insertionDir", this.insertionDir != ForgeDirection.UNKNOWN ? this.insertionDir.ordinal() : -1);
            nbt.setInteger("ejectionDir", this.ejectionDir != ForgeDirection.UNKNOWN ? this.ejectionDir.ordinal() : -1);
            return new SPacketUpdateTileEntity(this.pos, 0, nbt);
        }

        @Override
        public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
            NBTTagCompound nbt = pkt.getNbtCompound();
            this.readFromNBT(nbt);
            int insertion = nbt.getInteger("insertionDir");
            int ejection = nbt.getInteger("ejectionDir");
            this.insertionDir = insertion >= 0 ? ForgeDirection.getOrientation(insertion) : ForgeDirection.UNKNOWN;
            this.ejectionDir = ejection >= 0 ? ForgeDirection.getOrientation(ejection) : ForgeDirection.UNKNOWN;
        }

        @Override
        public NBTTagCompound getUpdateTag() {
            NBTTagCompound nbt = super.getUpdateTag();
            this.writeToNBT(nbt);
            return nbt;
        }

        @Override
        public NBTTagCompound getSettings(World world, int x, int y, int z) {
            NBTTagCompound nbt = new NBTTagCompound();
            if (block != null) {
                ResourceLocation key = ForgeRegistries.BLOCKS.getKey(block);
                if (key != null) {
                    nbt.setString("paintblock", key.toString());
                    nbt.setInteger("paintmeta", meta);
                }
            }
            return nbt;
        }

        @Override
        public void pasteSettings(NBTTagCompound nbt, int index, World world, EntityPlayer player, int x, int y, int z) {
            if (nbt.hasKey("paintblock", Constants.NBT.TAG_STRING)) {
                ResourceLocation key = new ResourceLocation(nbt.getString("paintblock"));
                this.block = ForgeRegistries.BLOCKS.getValue(key);
                this.meta = nbt.getInteger("paintmeta");
                this.markDirty();
                if (world != null) {
                    world.markChunkDirty(pos, this);
                    IBlockState state = world.getBlockState(pos);
                    world.notifyBlockUpdate(pos, state, state, 3);
                }
                if (world != null && world.isRemote) {
                    this.lastBlock = null;
                }
            }
        }
    }

    @SideOnly(Side.CLIENT)
    public static class PneumoTubePaintableModel implements IBakedModel {

        private static final FaceBakery FACE_BAKERY = new FaceBakery();
        private final TextureAtlasSprite particle;
        private final ImmutableMap<EnumFacing, ImmutableList<BakedQuad>> baseFaces;
        private final ImmutableList<BakedQuad> baseGeneral;
        private final ImmutableMap<EnumFacing, BakedQuad> overlayGeneral;
        private final ImmutableMap<EnumFacing, BakedQuad> overlayInsertion;
        private final ImmutableMap<EnumFacing, BakedQuad> overlayEjection;

        public PneumoTubePaintableModel(TextureAtlasSprite base, TextureAtlasSprite overlay, TextureAtlasSprite overlayIn, TextureAtlasSprite overlayOut) {
            this.particle = base;
            this.baseFaces = buildFaceMap(base, -1, false);
            this.baseGeneral = flatten(this.baseFaces);
            this.overlayGeneral = buildOverlayMap(overlay, -1);
            this.overlayInsertion = buildOverlayMap(overlayIn, -1);
            this.overlayEjection = buildOverlayMap(overlayOut, -1);
        }

        @Override
        public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
            if (state == null) {
                if (side != null) return ImmutableList.of();
                ImmutableList.Builder<BakedQuad> b = ImmutableList.builder();
                for (EnumFacing f : EnumFacing.VALUES) b.addAll(baseFaces.get(f));
                for (EnumFacing f : EnumFacing.VALUES) b.add(overlayGeneral.get(f));
                return b.build();
            }
            if (side == null) return ImmutableList.of();
            BlockRenderLayer layer = MinecraftForgeClient.getRenderLayer();
            boolean tubeLayer = (layer == null || layer == BlockRenderLayer.CUTOUT_MIPPED);
            IBlockState disguiseState = null;
            EnumFacing insertion = null;
            EnumFacing ejection = null;

            if (state instanceof IExtendedBlockState ext) {
                disguiseState = ext.getValue(DISGUISED_STATE);
                insertion = ext.getValue(INSERTION_DIR);
                ejection = ext.getValue(EJECTION_DIR);
            }
            List<BakedQuad> base = ImmutableList.of();
            if (disguiseState != null) {
                if (layer == null || disguiseState.getBlock().canRenderInLayer(disguiseState, layer)) {
                    IBakedModel disguiseModel = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(disguiseState);
                    base = disguiseModel.getQuads(disguiseState, side, rand);
                }
            } else if (tubeLayer) {
                base = baseFaces.get(side);
            }
            if (!tubeLayer) return base;
            BakedQuad overlayQuad = selectOverlay(side, insertion, ejection);
            if (base.isEmpty()) return ImmutableList.of(overlayQuad);
            ArrayList<BakedQuad> out = new ArrayList<>(base.size() + 1);
            out.addAll(base);
            out.add(overlayQuad);
            return out;
        }

        private BakedQuad selectOverlay(EnumFacing face, EnumFacing insertion, EnumFacing ejection) {
            if (ejection != null && ejection == face) {
                return overlayEjection.get(face);
            } else if (insertion != null && insertion == face) {
                return overlayInsertion.get(face);
            }
            return overlayGeneral.get(face);
        }

        @Override
        public boolean isAmbientOcclusion() {
            return true;
        }

        @Override
        public boolean isGui3d() {
            return true;
        }

        @Override
        public boolean isBuiltInRenderer() {
            return false;
        }

        @Override
        public TextureAtlasSprite getParticleTexture() {
            return particle;
        }

        @Override
        public ItemCameraTransforms getItemCameraTransforms() {
            return BakedModelTransforms.standardBlock();
        }

        @Override
        public ItemOverrideList getOverrides() {
            return ItemOverrideList.NONE;
        }

        private static ImmutableMap<EnumFacing, ImmutableList<BakedQuad>> buildFaceMap(TextureAtlasSprite sprite, int tintIndex, boolean offset) {
            ImmutableMap.Builder<EnumFacing, ImmutableList<BakedQuad>> builder = ImmutableMap.builder();
            for (EnumFacing face : EnumFacing.VALUES) {
                builder.put(face, ImmutableList.of(createQuad(face, sprite, tintIndex, offset)));
            }
            return builder.build();
        }

        private static ImmutableList<BakedQuad> flatten(Map<EnumFacing, ImmutableList<BakedQuad>> map) {
            ImmutableList.Builder<BakedQuad> builder = ImmutableList.builder();
            for (EnumFacing face : EnumFacing.VALUES) {
                builder.addAll(map.get(face));
            }
            return builder.build();
        }

        private static ImmutableMap<EnumFacing, BakedQuad> buildOverlayMap(TextureAtlasSprite sprite, int tintIndex) {
            ImmutableMap.Builder<EnumFacing, BakedQuad> builder = ImmutableMap.builder();
            for (EnumFacing face : EnumFacing.VALUES) {
                builder.put(face, createQuad(face, sprite, tintIndex, true));
            }
            return builder.build();
        }

        private static BakedQuad createQuad(EnumFacing face, TextureAtlasSprite sprite, int tintIndex, boolean offset) {
            float eps = 0.001F;
            Vector3f from = new Vector3f(0F, 0F, 0F);
            Vector3f to = new Vector3f(16F, 16F, 16F);

            if (offset) {
                switch (face) {
                    case DOWN -> from.setY(-eps);
                    case UP -> to.setY(16F + eps);
                    case NORTH -> from.setZ(-eps);
                    case SOUTH -> to.setZ(16F + eps);
                    case WEST -> from.setX(-eps);
                    case EAST -> to.setX(16F + eps);
                }
            }

            BlockFaceUV uv = new BlockFaceUV(new float[]{0F, 0F, 16F, 16F}, 0);
            BlockPartFace partFace = new BlockPartFace(null, tintIndex, "", uv);
            return FACE_BAKERY.makeBakedQuad(from, to, partFace, sprite, face, ModelRotation.X0_Y0, null, false, true);
        }
    }
}
