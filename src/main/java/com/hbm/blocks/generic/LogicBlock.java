package com.hbm.blocks.generic;

import com.google.common.collect.ImmutableMap;
import com.hbm.blocks.machine.BlockContainerBakeable;
import com.hbm.blocks.network.SimpleUnlistedProperty;
import com.hbm.interfaces.AutoRegister;
import com.hbm.render.block.BlockBakeFrame;
import com.hbm.world.gen.util.LogicBlockActions;
import com.hbm.world.gen.util.LogicBlockConditions;
import com.hbm.world.gen.util.LogicBlockInteractions;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class LogicBlock extends BlockContainerBakeable {

    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
    public static final IUnlistedProperty<IBlockState> DISGUISED_STATE = new SimpleUnlistedProperty<>("disguised_state", IBlockState.class);

    public LogicBlock(String regName) {
        super(Material.ROCK, regName, new BlockBakeFrame("logic_block"));
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new LogicBlock.TileEntityLogicBlock();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing facing = EnumFacing.byHorizontalIndex(meta & 3);
        return this.getDefaultState().withProperty(FACING, facing);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        EnumFacing facing = state.getValue(FACING);
        return facing.getHorizontalIndex();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new ExtendedBlockState(this, new IProperty[]{FACING}, new IUnlistedProperty[]{DISGUISED_STATE});
    }

    @Override
    @SuppressWarnings("deprecation")
    public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
        if (state instanceof IExtendedBlockState) {
            IExtendedBlockState ext = (IExtendedBlockState) state;
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof LogicBlock.TileEntityLogicBlock) {
                LogicBlock.TileEntityLogicBlock logic = (LogicBlock.TileEntityLogicBlock) te;
                if (logic.disguise != null) {
                    IBlockState disguiseState = logic.disguise.getStateFromMeta(logic.disguiseMeta);
                    return ext.withProperty(DISGUISED_STATE, disguiseState);
                }
            }
            return ext.withProperty(DISGUISED_STATE, null);
        }
        return state;
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
        EnumFacing horizontal = placer.getHorizontalFacing().getOpposite();
        return this.getDefaultState().withProperty(FACING, horizontal);
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof LogicBlock.TileEntityLogicBlock) {
            ((LogicBlock.TileEntityLogicBlock) te).direction = state.getValue(FACING);
        }
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float subX, float subY, float subZ) {
        TileEntity te = worldIn.getTileEntity(pos);
        if (te instanceof LogicBlock.TileEntityLogicBlock && ((LogicBlock.TileEntityLogicBlock) te).interaction != null) {
            ((LogicBlock.TileEntityLogicBlock) te).interaction.accept(new Object[]{
                    worldIn, te, pos.getX(), pos.getY(), pos.getZ(), player, side.getIndex(), subX, subY, subZ
            });
            return true;
        }
        return super.onBlockActivated(worldIn, pos, state, player, hand, side, subX, subY, subZ);
    }

    @Override
    public void bakeModel(ModelBakeEvent event) {
        try {
            IModel baseModel = ModelLoaderRegistry.getModel(new ResourceLocation(blockFrame.getBaseModel()));
            ImmutableMap.Builder<String, String> textureMap = ImmutableMap.builder();

            blockFrame.putTextures(textureMap);
            IModel retexturedModel = baseModel.retexture(textureMap.build());
            IBakedModel[] models = new IBakedModel[4];
            for (int i = 0; i < EnumFacing.HORIZONTALS.length; i++) {
                EnumFacing facing = EnumFacing.HORIZONTALS[i];
                IBakedModel baked = retexturedModel.bake(
                        ModelRotation.getModelRotation(0, BlockBakeFrame.getYRotationForFacing(facing)),
                        DefaultVertexFormats.BLOCK,
                        ModelLoader.defaultTextureGetter()
                );
                models[i] = new DisguisedDelegatingModel(baked);
            }
            ModelResourceLocation modelLocation = new ModelResourceLocation(Objects.requireNonNull(getRegistryName()), "inventory");
            event.getModelRegistry().putObject(modelLocation, models[2]);
            for (int index = 0; index < models.length; index++) {
                ModelResourceLocation worldLocation = new ModelResourceLocation(Objects.requireNonNull(getRegistryName()), "facing=" + EnumFacing.HORIZONTALS[index].getName());
                event.getModelRegistry().putObject(worldLocation, models[index]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @AutoRegister
    public static class TileEntityLogicBlock extends TileEntity implements ITickable {

        public int phase = 0;
        public int timer = 0;

        public Block disguise;
        public int disguiseMeta;

        public String conditionID = "PLAYER_CUBE_5";
        public String actionID = "FODDER_WAVE";
        public String interactionID;

        public Function<TileEntityLogicBlock, Boolean> condition;
        public Consumer<TileEntityLogicBlock> action;
        public Consumer<Object[]> interaction;

        public EntityPlayer player;

        public EnumFacing direction = EnumFacing.NORTH;

        @Override
        public void update() {
            if (!world.isRemote) {
                if (action == null) {
                    action = LogicBlockActions.actions.get(actionID);
                }
                if (condition == null) {
                    condition = LogicBlockConditions.conditions.get(conditionID);
                }
                if (interaction == null && interactionID != null) {
                    interaction = LogicBlockInteractions.interactions.get(interactionID);
                }

                if (action == null || condition == null) {
                    world.setBlockToAir(pos);
                    return;
                }
                action.accept(this);
                if (condition.apply(this)) {
                    phase++;
                    timer = 0;
                } else {
                    timer++;
                }
            }
        }

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
            super.writeToNBT(nbt);
            nbt.setInteger("phase", phase);

            nbt.setString("actionID", actionID);
            nbt.setString("conditionID", conditionID);
            if (interactionID != null)
                nbt.setString("interactionID", interactionID);

            nbt.setInteger("direction", direction.getIndex());
            if (disguise != null) {
                nbt.setInteger("disguiseMeta", disguiseMeta);
                ResourceLocation key = ForgeRegistries.BLOCKS.getKey(disguise);
                if (key != null) {
                    nbt.setString("disguise", key.toString());
                }
            }
            return nbt;
        }

        @Override
        public void readFromNBT(NBTTagCompound nbt) {
            super.readFromNBT(nbt);
            this.phase = nbt.getInteger("phase");

            this.actionID = nbt.getString("actionID");
            this.conditionID = nbt.getString("conditionID");
            if (nbt.hasKey("interactionID")) this.interactionID = nbt.getString("interactionID");

            this.direction = EnumFacing.byIndex(nbt.getInteger("direction"));

            if (nbt.hasKey("disguise")) {
                disguiseMeta = nbt.getInteger("disguiseMeta");
                String str = nbt.getString("disguise");
                Block b = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(str));
                this.disguise = b;
            }
        }

        @Override
        public SPacketUpdateTileEntity getUpdatePacket() {
            NBTTagCompound nbt = new NBTTagCompound();
            this.writeToNBT(nbt);
            return new SPacketUpdateTileEntity(this.pos, 0, nbt);
        }

        @Override
        public NBTTagCompound getUpdateTag() {
            NBTTagCompound nbt = super.getUpdateTag();
            this.writeToNBT(nbt);
            return nbt;
        }

        @Override
        public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
            this.readFromNBT(pkt.getNbtCompound());
        }
    }

    public static class DisguisedDelegatingModel implements IBakedModel {
        private final IBakedModel base;

        public DisguisedDelegatingModel(IBakedModel base) {
            this.base = base;
        }

        @Override
        public List<BakedQuad> getQuads(IBlockState state, EnumFacing side, long rand) {
            if (state instanceof IExtendedBlockState) {
                IExtendedBlockState ext = (IExtendedBlockState) state;
                IBlockState disguise = ext.getValue(DISGUISED_STATE);
                if (disguise != null) {
                    IBakedModel other = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(disguise);
                    return other.getQuads(disguise, side, rand);
                }
            }
            return base.getQuads(state, side, rand);
        }

        @Override
        public boolean isAmbientOcclusion() {
            return base.isAmbientOcclusion();
        }

        @Override
        public boolean isGui3d() {
            return base.isGui3d();
        }

        @Override
        public boolean isBuiltInRenderer() {
            return base.isBuiltInRenderer();
        }

        @Override
        public TextureAtlasSprite getParticleTexture() {
            return base.getParticleTexture();
        }

        @Override
        public ItemCameraTransforms getItemCameraTransforms() {
            return base.getItemCameraTransforms();
        }

        @Override
        public ItemOverrideList getOverrides() {
            return base.getOverrides();
        }
    }
}
