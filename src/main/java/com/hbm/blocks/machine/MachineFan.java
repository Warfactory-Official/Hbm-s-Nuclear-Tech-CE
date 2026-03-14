package com.hbm.blocks.machine;

import com.google.common.collect.ImmutableMap;
import com.hbm.api.block.IBlowable;
import com.hbm.api.block.IToolable;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.interfaces.AutoRegister;
import com.hbm.items.ClaimedModelLocationRegistry;
import com.hbm.lib.ForgeDirection;
import com.hbm.main.client.NTMClientRegistry;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.PlayerInformPacketLegacy;
import com.hbm.tileentity.TileEntityLoadedBase;
import com.hbm.util.ChatBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public class MachineFan extends BlockContainerBakeable implements IToolable, ITooltipProvider {

    public static final PropertyDirection FACING = BlockDirectional.FACING;

    public MachineFan(String s) {
        super(Material.IRON, s, null);
        this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityFan();
    }

    @Override
    public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack stack) {
        EnumFacing facing = EnumFacing.getDirectionFromEntityLiving(pos, player);
        world.setBlockState(pos, state.withProperty(FACING, facing), 2);
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state){
        return EnumBlockRenderType.INVISIBLE;
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
    public boolean isSideSolid(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
        EnumFacing facing = state.getValue(FACING);
        return facing.getAxis() != side.getAxis();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(FACING, EnumFacing.byIndex(meta & 7));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getIndex();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }
    @AutoRegister
    public static class TileEntityFan extends TileEntityLoadedBase implements ITickable {

        public float spin;
        public float prevSpin;
        public boolean falloff = true;

        @Override
        public void update() {
            this.prevSpin = this.spin;

            if (world.isBlockPowered(pos)) {
                EnumFacing dir = world.getBlockState(pos).getValue(MachineFan.FACING);

                int range = 10;
                int effRange = 0;
                double push = 0.1;

                for (int i = 1; i <= range; i++) {
                    BlockPos p = pos.offset(dir, i);
                    IBlockState s = world.getBlockState(p);
                    Block b = s.getBlock();
                    boolean blowable = b instanceof IBlowable;

                    if (s.isFullCube() || blowable) {
                        if (!world.isRemote && blowable) {
                            ((IBlowable) b).applyFan(world, p, ForgeDirection.getOrientation(dir), i);
                        }
                        break;
                    }

                    effRange = i;
                }

                int x = dir.getXOffset() * effRange;
                int y = dir.getYOffset() * effRange;
                int z = dir.getZOffset() * effRange;

                AxisAlignedBB aabb = new AxisAlignedBB(
                        pos.getX() + 0.5 + Math.min(x, 0),
                        pos.getY() + 0.5 + Math.min(y, 0),
                        pos.getZ() + 0.5 + Math.min(z, 0),
                        pos.getX() + 0.5 + Math.max(x, 0),
                        pos.getY() + 0.5 + Math.max(y, 0),
                        pos.getZ() + 0.5 + Math.max(z, 0)
                ).grow(0.5, 0.5, 0.5);

                List<Entity> affected = world.getEntitiesWithinAABB(Entity.class, aabb);

                for (Entity e : affected) {
                    double coeff = push;

                    if (falloff) {
                        double dist = e.getDistance(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                        coeff *= 1.5 * (1 - dist / range / 2);
                    }

                    e.motionX += dir.getXOffset() * coeff;
                    e.motionY += dir.getYOffset() * coeff;
                    e.motionZ += dir.getZOffset() * coeff;
                }

                if (world.isRemote && world.rand.nextInt(30) == 0) {
                    double speed = 0.2;
                    world.spawnParticle(EnumParticleTypes.CLOUD,
                            pos.getX() + 0.5 + dir.getXOffset() * 0.5,
                            pos.getY() + 0.5 + dir.getYOffset() * 0.5,
                            pos.getZ() + 0.5 + dir.getZOffset() * 0.5,
                            dir.getXOffset() * speed,
                            dir.getYOffset() * speed,
                            dir.getZOffset() * speed);
                }

                this.spin += 30;
            }

            if (this.spin >= 360) {
                this.prevSpin -= 360;
                this.spin -= 360;
            }

            if (!world.isRemote) {
                networkPackNT(150);
            }
        }

        @Override
        @SideOnly(Side.CLIENT)
        public double getMaxRenderDistanceSquared() {
            return 65536.0D;
        }

        @Override
        public void readFromNBT(NBTTagCompound nbt) {
            super.readFromNBT(nbt);
            this.falloff = nbt.getBoolean("falloff");
        }

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
            super.writeToNBT(nbt);
            nbt.setBoolean("falloff", falloff);
            return nbt;
        }

        @Override
        public void serialize(ByteBuf buf) {
            buf.writeBoolean(falloff);
        }

        @Override
        public void deserialize(ByteBuf buf) {
            falloff = buf.readBoolean();
        }
    }

    @Override
    public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, EnumFacing side, float fX, float fY, float fZ, EnumHand hand, ToolType tool) {
        BlockPos pos = new BlockPos(x, y, z);

        if (tool == ToolType.SCREWDRIVER) {
            IBlockState state = world.getBlockState(pos);
            EnumFacing facing = state.getValue(FACING);
            world.setBlockState(pos, state.withProperty(FACING, facing.getOpposite()), 3);
            return true;
        }

        if (tool == ToolType.HAND_DRILL) {
            TileEntity te = world.getTileEntity(pos);

            if (te instanceof TileEntityFan tile) {
                tile.falloff = !tile.falloff;
                tile.markDirty();

                if (!world.isRemote) {
                    PacketDispatcher.wrapper.sendTo(
                            new PlayerInformPacketLegacy(
                                    ChatBuilder.start("")
                                            .nextTranslation(this.getTranslationKey() + (tile.falloff ? ".falloffOn" : ".falloffOff"))
                                            .color(TextFormatting.GOLD)
                                            .flush(),
                                    10),
                            (EntityPlayerMP) player
                    );

                    world.playSound(null, pos, SoundEvents.BLOCK_LEVER_CLICK, SoundCategory.BLOCKS, 0.5F, 0.5F);
                }
            }

            return true;
        }

        return false;
    }

    @Override
    public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn) {
        this.addStandardInfo(list);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void bakeModel(ModelBakeEvent event) {
        try {
            IModel blockBaseModel = ModelLoaderRegistry.getModel(new ResourceLocation("block/cube_all"));
            ImmutableMap<String, String> blockTextures = ImmutableMap.of("all", "hbm:blocks/block_steel");
            IModel blockRetextured = blockBaseModel.retexture(blockTextures);
            IBakedModel blockBaked = blockRetextured.bake(
                    ModelRotation.X0_Y0,
                    DefaultVertexFormats.BLOCK,
                    ModelLoader.defaultTextureGetter()
            );
            ModelResourceLocation worldLocation = new ModelResourceLocation(getRegistryName(), "normal");
            event.getModelRegistry().putObject(worldLocation, blockBaked);
            if (!ClaimedModelLocationRegistry.hasSyntheticTeisrBinding(Item.getItemFromBlock(this))) {
                IModel itemBaseModel = ModelLoaderRegistry.getModel(new ResourceLocation("item/generated"));
                ImmutableMap<String, String> itemTextures = ImmutableMap.of("layer0", "hbm:blocks/" + getRegistryName().getPath());
                IModel itemRetextured = itemBaseModel.retexture(itemTextures);
                IBakedModel itemBaked = itemRetextured.bake(
                        ModelRotation.X0_Y0,
                        DefaultVertexFormats.ITEM,
                        ModelLoader.defaultTextureGetter()
                );
                ModelResourceLocation inventoryLocation = new ModelResourceLocation(getRegistryName(), "inventory");
                event.getModelRegistry().putObject(inventoryLocation, itemBaked);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    @SideOnly(Side.CLIENT)
    public void registerModel() {
        Item item = Item.getItemFromBlock(this);
        ModelResourceLocation syntheticLocation = NTMClientRegistry.getSyntheticTeisrModelLocation(item);
        ModelLoader.setCustomModelResourceLocation(item, 0, syntheticLocation != null ? syntheticLocation : new ModelResourceLocation(this.getRegistryName(), "inventory"));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerSprite(TextureMap map) {
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


}
