package com.hbm.blocks.generic;

import com.hbm.items.IDynamicModels;
import com.hbm.render.model.BlockBarrierBakedModel;
import com.hbm.util.UnlistedPropertyBoolean;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class BlockBarrier extends BlockBakeBase implements IDynamicModels {
  public static final PropertyDirection FACING = BlockHorizontal.FACING;
  public static final IUnlistedProperty<Boolean> CONN_NEG_X = new UnlistedPropertyBoolean("conn_neg_x");
  public static final IUnlistedProperty<Boolean> CONN_POS_X = new UnlistedPropertyBoolean("conn_pos_x");
  public static final IUnlistedProperty<Boolean> CONN_NEG_Z = new UnlistedPropertyBoolean("conn_neg_z");
  public static final IUnlistedProperty<Boolean> CONN_POS_Z = new UnlistedPropertyBoolean("conn_pos_z");
  public static final IUnlistedProperty<Boolean> CONN_POS_Y = new UnlistedPropertyBoolean("conn_pos_y");

  private static final AxisAlignedBB POS_X = new AxisAlignedBB(0, 0, 0, 0.125, 1, 1);
  private static final AxisAlignedBB NEG_X = new AxisAlignedBB(0.875, 0, 0, 1, 1, 1);
  private static final AxisAlignedBB POS_Z = new AxisAlignedBB(0, 0, 0, 1, 1, 0.125);
  private static final AxisAlignedBB NEG_Z = new AxisAlignedBB(0, 0, 0.875, 1, 1, 1);

  @SideOnly(Side.CLIENT)
  private TextureAtlasSprite sprite;

  public BlockBarrier(Material mat, String name) {
    super(mat, name);
    this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
  }

  @Override
  public BlockFaceShape getBlockFaceShape(
      IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
    return BlockFaceShape.UNDEFINED;
  }

  @Override
  protected BlockStateContainer createBlockState() {
    return new ExtendedBlockState(
            this,
            new IProperty<?>[] {FACING},
            new IUnlistedProperty<?>[] {CONN_NEG_X, CONN_POS_X, CONN_NEG_Z, CONN_POS_Z, CONN_POS_Y});
  }

  @Override
  public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
    if (!(state.getBlock() == this) || !(state.getPropertyKeys().contains(FACING))) return state;

    IExtendedBlockState ext = (IExtendedBlockState) state;

    EnumFacing facing = state.getValue(FACING);

    IBlockState nx = world.getBlockState(pos.west());
    IBlockState px = world.getBlockState(pos.east());
    IBlockState nz = world.getBlockState(pos.north());
    IBlockState pz = world.getBlockState(pos.south());
    IBlockState py = world.getBlockState(pos.up());

    boolean negX = nx.isOpaqueCube() || nx.isNormalCube() || facing == EnumFacing.EAST;
    boolean posX = px.isOpaqueCube() || px.isNormalCube() || facing == EnumFacing.WEST;
    boolean negZ = nz.isOpaqueCube() || nz.isNormalCube() || facing == EnumFacing.SOUTH;
    boolean posZ = pz.isOpaqueCube() || pz.isNormalCube() || facing == EnumFacing.NORTH;
    boolean posY = py.isOpaqueCube() || py.isNormalCube();

    return ext
            .withProperty(CONN_NEG_X, negX)
            .withProperty(CONN_POS_X, posX)
            .withProperty(CONN_NEG_Z, negZ)
            .withProperty(CONN_POS_Z, posZ)
            .withProperty(CONN_POS_Y, posY);
  }

  @Override
  public int getMetaFromState(IBlockState state) {
    return state.getValue(FACING).getHorizontalIndex();
  }

  @Override
  public IBlockState getStateFromMeta(int meta) {
    return getDefaultState().withProperty(FACING, EnumFacing.byHorizontalIndex(meta));
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
  public void onBlockPlacedBy(
      World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {

    EnumFacing facing = placer.getHorizontalFacing().getOpposite();
    worldIn.setBlockState(pos, state.withProperty(FACING, facing), 2);
  }

  @Override
  public IBlockState getStateForPlacement(
      World world,
      BlockPos pos,
      EnumFacing facing,
      float hitX,
      float hitY,
      float hitZ,
      int meta,
      EntityLivingBase placer) {
    return getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
  }

  @Override
  public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {

    EnumFacing facing = state.getValue(FACING);

    return switch (facing) {
      case EAST -> POS_X;
      case WEST -> NEG_X;
      case SOUTH -> POS_Z;
      case NORTH -> NEG_Z;
      default -> FULL_BLOCK_AABB;
    };
  }

  @Override
  public void addCollisionBoxToList(
      IBlockState state,
      World worldIn,
      BlockPos pos,
      AxisAlignedBB entityBox,
      List<AxisAlignedBB> collidingBoxes,
      Entity entityIn,
      boolean isActualState) {

    EnumFacing facing = state.getValue(FACING);

    IBlockState nx = worldIn.getBlockState(pos.west());
    IBlockState px = worldIn.getBlockState(pos.east());
    IBlockState nz = worldIn.getBlockState(pos.north());
    IBlockState pz = worldIn.getBlockState(pos.south());

    List<AxisAlignedBB> bbs = new ArrayList<>();

    if (nx.isOpaqueCube() || nx.isNormalCube() || facing == EnumFacing.EAST)
      bbs.add(new AxisAlignedBB(0, 0, 0, 0.125, 1, 1));
    if (nz.isOpaqueCube() || nz.isNormalCube() || facing == EnumFacing.SOUTH)
      bbs.add(new AxisAlignedBB(0, 0, 0, 1, 1, 0.125));
    if (px.isOpaqueCube() || px.isNormalCube() || facing == EnumFacing.WEST)
      bbs.add(new AxisAlignedBB(0.875, 0, 0, 1, 1, 1));
    if (pz.isOpaqueCube() || pz.isNormalCube() || facing == EnumFacing.NORTH)
      bbs.add(new AxisAlignedBB(0, 0, 0.875, 1, 1, 1));

    for (AxisAlignedBB bb : bbs) {
      AxisAlignedBB offsetBB = bb.offset(pos);
      if (entityBox.intersects(offsetBB)) {
        collidingBoxes.add(offsetBB);
      }
    }
  }

  @Override
  @SideOnly(Side.CLIENT)
  public boolean shouldSideBeRendered(
      @NotNull IBlockState blockState,
      @NotNull IBlockAccess blockAccess,
      @NotNull BlockPos pos,
      @NotNull EnumFacing side) {
    return true;
  }
  @Override
  @SideOnly(Side.CLIENT)
  public void registerModel() {
    // Item model: point to "inventory" variant
    Item item = Item.getItemFromBlock(this);
    ModelResourceLocation inv = new ModelResourceLocation(getRegistryName(), "inventory");
    ModelLoader.setCustomModelResourceLocation(item, 0, inv);
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
  public void registerSprite(TextureMap map) {
    ResourceLocation rl = getRegistryName();
    if (rl != null) {
      this.sprite = map.registerSprite(new ResourceLocation(rl.getNamespace(), "blocks/" + rl.getPath()));
    }
  }
  @Override
  @SideOnly(Side.CLIENT)
  public void bakeModel(ModelBakeEvent event) {
    if (this.sprite == null) return;

    ModelResourceLocation worldLoc = new ModelResourceLocation(getRegistryName(), "normal");
    ModelResourceLocation invLoc = new ModelResourceLocation(getRegistryName(), "inventory");

    IBakedModel worldModel = new BlockBarrierBakedModel(this.sprite, false);
    IBakedModel itemModel = new BlockBarrierBakedModel(this.sprite, true);

    event.getModelRegistry().putObject(worldLoc, worldModel);
    event.getModelRegistry().putObject(invLoc, itemModel);
  }
}
