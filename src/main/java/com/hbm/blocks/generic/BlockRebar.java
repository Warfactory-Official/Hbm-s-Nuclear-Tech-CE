package com.hbm.blocks.generic;

import com.hbm.Tags;
import com.hbm.api.fluidmk2.IFluidReceiverMK2;
import com.hbm.blocks.ModBlocks;
import com.hbm.capability.NTMFluidHandlerWrapper;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.RecipesCommon.ComparableStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.items.IDynamicModels;
import com.hbm.items.ModItems;
import com.hbm.items.tool.ItemRebarPlacer;
import com.hbm.lib.DirPos;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.render.model.BlockRebarBakedModel;
import com.hbm.tileentity.IBufPacketReceiver;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.tileentity.network.TileEntityPipeBaseNT;
import com.hbm.uninos.UniNodespace;
import com.hbm.uninos.networkproviders.RebarNetwork;
import com.hbm.util.BobMathUtil;
import com.hbm.util.Compat;
import com.hbm.util.InventoryUtil;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BlockRebar extends BlockContainer implements IDynamicModels {

    @SideOnly(Side.CLIENT)
    private TextureAtlasSprite rebarSprite;
    @SideOnly(Side.CLIENT)
    private TextureAtlasSprite concreteSprite;

    public BlockRebar() {
        super(Material.IRON);
        setRegistryName("rebar");
        setTranslationKey("rebar");
        setHarvestLevel("pickaxe", 0);
        setLightOpacity(0);
        fullBlock = false;
        translucent = true;
        IDynamicModels.INSTANCES.add(this);
        ModBlocks.ALL_BLOCKS.add(this);
    }

    @SideOnly(Side.CLIENT)
    public static void renderRebar(float partialTicks) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer player = mc.player;
        World world = mc.world;
        if (player == null || world == null) {
            return;
        }
        double dx = player.prevPosX + (player.posX - player.prevPosX) * partialTicks;
        double dy = player.prevPosY + (player.posY - player.prevPosY) * partialTicks;
        double dz = player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks;
        renderWorldModels(mc, dx, dy, dz, world);
        renderPreview(player, mc, dx, dy, dz);
    }

    private static void renderWorldModels(Minecraft mc, double dx, double dy, double dz, World world) {
        TextureAtlasSprite sprite = ((BlockRebar) ModBlocks.rebar).concreteSprite;
        RebarFillRenderer.render(mc, dx, dy, dz, world, sprite);
    }

    private static void renderPreview(EntityPlayer player, Minecraft mc, double dx, double dy, double dz) {
        ItemStack held = player.getHeldItemMainhand();
        if (!held.isEmpty() && held.getItem() == ModItems.rebar_placer && held.hasTagCompound() && held.getTagCompound().hasKey("pos")) {
            RayTraceResult mop = mc.objectMouseOver;
            if (mop != null && mop.typeOfHit == RayTraceResult.Type.BLOCK) {
                int[] pos = held.getTagCompound().getIntArray("pos");
                BlockPos hitPos = mop.getBlockPos();
                EnumFacing side = mop.sideHit;
                BlockPos target = hitPos.offset(side);

                int minX = Math.min(pos[0], target.getX());
                int maxX = Math.max(pos[0], target.getX());
                int minY = Math.min(pos[1], target.getY());
                int maxY = Math.max(pos[1], target.getY());
                int minZ = Math.min(pos[2], target.getZ());
                int maxZ = Math.max(pos[2], target.getZ());

                AxisAlignedBB box = new AxisAlignedBB(minX + 0.125D, minY + 0.125D, minZ + 0.125D, maxX + 0.875D, maxY + 0.875D, maxZ + 0.875D);

                GlStateManager.pushMatrix();
                GlStateManager.disableLighting();
                GlStateManager.enableBlend();
                GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
                GlStateManager.glLineWidth(2.0F);
                GlStateManager.disableTexture2D();
                GlStateManager.depthMask(false);
                RenderGlobal.drawSelectionBoundingBox(box.offset(-dx, -dy, -dz), 1F, 1F, 1F, 1F);
                GlStateManager.depthMask(true);
                GlStateManager.enableTexture2D();
                GlStateManager.disableBlend();
                GlStateManager.enableLighting();
                GlStateManager.popMatrix();

                int rebarLeft = player.isCreative() ? Integer.MAX_VALUE : InventoryUtil.countAStackMatches(player, new ComparableStack(ModBlocks.rebar), true);
                int rebarRequired = (maxX - minX + 1) * (maxY - minY + 1) * (maxZ - minZ + 1);
                TextFormatting color = rebarRequired > rebarLeft ? TextFormatting.RED : TextFormatting.GREEN;
                MainRegistry.proxy.displayTooltip(color + ((rebarLeft == Integer.MAX_VALUE ? "∞" : rebarLeft) + " / " + rebarRequired));
            }
        }
    }

    @Override
    public @Nullable TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityRebar();
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
    public boolean isNormalCube(IBlockState state) {
        return false;
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public boolean shouldSideBeRendered(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        return true;
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof TileEntityRebar rebar)) return;

        rebar.hasConnection = false;

        for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
            TileEntity neighbor = Compat.getTileStandard(world, pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ);
            if (neighbor instanceof TileEntityPipeBaseNT) {
                rebar.hasConnection = true;
                return;
            }
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerSprite(TextureMap map) {
        ResourceLocation rl = getRegistryName();
        if (rl != null) {
            rebarSprite = map.registerSprite(new ResourceLocation(rl.getNamespace(), "blocks/" + rl.getPath()));
        }
        concreteSprite = map.registerSprite(new ResourceLocation(Tags.MODID, "blocks/concrete_liquid"));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void bakeModel(ModelBakeEvent event) {
        RebarFillRenderer.reset();
        ModelResourceLocation worldLoc = new ModelResourceLocation(getRegistryName(), "normal");
        ModelResourceLocation invLoc = new ModelResourceLocation(getRegistryName(), "inventory");

        IBakedModel worldModel = new BlockRebarBakedModel(rebarSprite);
        IBakedModel itemModel = new BlockRebarBakedModel(rebarSprite);

        event.getModelRegistry().putObject(worldLoc, worldModel);
        event.getModelRegistry().putObject(invLoc, itemModel);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public StateMapperBase getStateMapper(ResourceLocation loc) {
        return new StateMapperBase() {
            @Override
            protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
                return new ModelResourceLocation(loc, "normal");
            }
        };
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModel() {
        ModelResourceLocation inv = new ModelResourceLocation(getRegistryName(), "inventory");
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, inv);
    }

    @AutoRegister
    public static class TileEntityRebar extends TileEntityLoadedBase implements ITickable, IFluidReceiverMK2, IBufPacketReceiver {
        static final Set<TileEntityRebar> ACTIVE; // only initialized on client
        public Block concrete;
        public int concreteMeta;
        public int progress;
        public int prevProgress;
        public boolean hasConnection = false;
        protected RebarNetwork.RebarNode node;

        static {
            if (FMLCommonHandler.instance().getSide() == Side.CLIENT) {
                ACTIVE = new ReferenceLinkedOpenHashSet<>(); // linked for insertion order
            } else ACTIVE = null;
        }

        public TileEntityRebar setup(Block block, int meta) {
            concrete = block;
            concreteMeta = meta;
            markDirty();
            return this;
        }

        @Override
        public void update() {
            if (world == null) return;

            long time = world.getTotalWorldTime();

            if (!world.isRemote) {

                if (prevProgress != progress) {
                    markChanged();
                    prevProgress = progress;
                }

                if (progress >= 1_000) {
                    if (concrete != null && ItemRebarPlacer.isValidConk(Item.getItemFromBlock(concrete), concreteMeta)) {
                        world.setBlockState(pos, concrete.getStateFromMeta(concreteMeta), 3);
                    } else {
                        world.setBlockState(pos, ModBlocks.concrete_rebar.getDefaultState(), 3);
                    }
                    return;
                }

                if (time % 60 == 0) {
                    for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                        trySubscribe(Fluids.CONCRETE, world, pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ, dir);
                    }
                }

                if (node == null || node.expired) {
                    node = UniNodespace.getNode(world, pos, RebarNetwork.THE_PROVIDER);

                    if (node == null || node.expired) {
                        node = createNode();
                        UniNodespace.createNode(world, node);
                    }
                }

                networkPackNT(100);
            }
        }

        @Override
        public void onLoad() {
            super.onLoad();
            if (world.isRemote) {
                onClientRenderLoad();
            }
        }

        @Override
        public void onChunkUnload() {
            super.onChunkUnload();
            if (world.isRemote) {
                onClientRenderUnload();
            }
        }

        @Override
        public void invalidate() {
            super.invalidate();
            if (world != null) {
                if (world.isRemote) {
                    onClientRenderUnload();
                } else {
                    if (node != null) {
                        UniNodespace.destroyNode(world, pos, RebarNetwork.THE_PROVIDER);
                    }
                }
            }
        }

        @Override
        public void serialize(ByteBuf buf) {
            buf.writeInt(progress);
        }

        @Override
        public void deserialize(ByteBuf buf) {
            int newProgress = buf.readInt();
            Minecraft.getMinecraft().addScheduledTask(() -> {
                int oldProgress = this.progress;
                this.progress = newProgress;

                if (world == null || !world.isRemote || isInvalid()) {
                    onClientRenderUnload();
                    return;
                }

                syncClientRenderTracking(oldProgress, newProgress);
            });
        }

        @Override
        public void readFromNBT(NBTTagCompound nbt) {
            super.readFromNBT(nbt);
            progress = nbt.getInteger("progress");
            hasConnection = nbt.getBoolean("hasConnection");

            if (nbt.hasKey("block")) {
                concrete = Block.getBlockById(nbt.getInteger("block"));
                concreteMeta = nbt.getInteger("meta");
            }
        }

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
            super.writeToNBT(nbt);
            nbt.setInteger("progress", progress);
            nbt.setBoolean("hasConnection", hasConnection);

            if (concrete != null) {
                nbt.setInteger("block", Block.getIdFromBlock(concrete));
                nbt.setInteger("meta", concreteMeta);
            }

            return nbt;
        }

        public RebarNetwork.RebarNode createNode() {
            return new RebarNetwork.RebarNode(RebarNetwork.THE_PROVIDER, pos).setConnections(new DirPos(pos.add(1, 0, 0), Library.POS_X), new DirPos(pos.add(-1, 0, 0), Library.NEG_X), new DirPos(pos.add(0, 1, 0), Library.POS_Y), new DirPos(pos.add(0, -1, 0), Library.NEG_Y), new DirPos(pos.add(0, 0, 1), Library.POS_Z), new DirPos(pos.add(0, 0, -1), Library.NEG_Z));
        }

        private boolean shouldTrackClientFill(int progressValue) {
            return progressValue > 0 && !isInvalid();
        }

        private void onClientRenderLoad() {
            if (!shouldTrackClientFill(progress)) return;
            ACTIVE.add(this);
            RebarFillRenderer.onLoad(this);
        }

        private void onClientRenderUnload() {
            ACTIVE.remove(this);
            RebarFillRenderer.onUnload(this);
        }

        private void syncClientRenderTracking(int oldProgress, int newProgress) {
            boolean wasTracked = shouldTrackClientFill(oldProgress);
            boolean shouldTrack = shouldTrackClientFill(newProgress);

            if (shouldTrack) {
                ACTIVE.add(this);
            } else {
                ACTIVE.remove(this);
            }

            RebarFillRenderer.onProgressUpdate(this, wasTracked ? oldProgress : 0, shouldTrack ? newProgress : 0);
        }

        @Override
        public FluidTankNTM[] getAllTanks() {
            FluidTankNTM tank = new FluidTankNTM(Fluids.CONCRETE, 1_000);
            tank.setFill(progress);
            return new FluidTankNTM[]{tank};
        }

        @Override
        public long transferFluid(FluidType type, int pressure, long amount) {
            if (type != Fluids.CONCRETE) return amount;
            if (node == null || node.expired || !node.hasValidNet()) return amount;

            List<TileEntityRebar> lowestLinks = new ArrayList<>();
            int lowestY = Integer.MAX_VALUE;
            int progress = 0;
            int capacity = 0;

            for (RebarNetwork.RebarNode link : node.net.links) {
                BlockPos linkPos = link.positions[0];
                int y = linkPos.getY();

                if (y < lowestY) {
                    lowestY = y;
                    progress = 0;
                    capacity = 0;
                    lowestLinks.clear();
                }

                if (y == lowestY) {
                    TileEntity tile = world.getTileEntity(linkPos);
                    if (!(tile instanceof TileEntityRebar rebar)) continue;

                    progress += rebar.progress;
                    capacity += 1_000;
                    lowestLinks.add(rebar);
                }
            }

            if (capacity > 0 && !lowestLinks.isEmpty()) {
                int maxSpeed = 50;
                int maxAccept = (int) BobMathUtil.min(capacity - progress, amount, maxSpeed * lowestLinks.size());
                int target = Math.min((progress + maxAccept) / lowestLinks.size(), 1_000);

                for (TileEntityRebar rebar : lowestLinks) {
                    if (rebar.progress >= target) continue;
                    int delta = target - rebar.progress;
                    if (delta > amount) continue;

                    rebar.progress += delta;
                    amount -= delta;
                }
            }

            return amount;
        }

        @Override
        public long getDemand(FluidType type, int pressure) {
            return 10_000;
        }

        @Override
        public boolean canConnect(FluidType type, ForgeDirection dir) {
            return dir != ForgeDirection.UNKNOWN && type == Fluids.CONCRETE;
        }

        @Override
        public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
            if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
                return true;
            }
            return super.hasCapability(capability, facing);
        }

        @Override
        public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
            if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
                return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(new NTMFluidHandlerWrapper(this));
            }
            return super.getCapability(capability, facing);
        }
    }
}
