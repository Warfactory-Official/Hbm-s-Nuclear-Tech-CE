package com.hbm.blocks.generic;

import com.hbm.items.IDynamicModels;
import com.hbm.render.model.BlockSandbagsBakedModel;
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
import net.minecraft.item.Item;
import net.minecraft.util.EnumBlockRenderType;
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

import java.util.List;

public class BlockSandbags extends BlockBakeBase implements IDynamicModels {

  public static final PropertyDirection FACING = BlockHorizontal.FACING;

  public static final IUnlistedProperty<Boolean> CONN_NEG_X =
      new UnlistedPropertyBoolean("conn_neg_x");
  public static final IUnlistedProperty<Boolean> CONN_POS_X =
      new UnlistedPropertyBoolean("conn_pos_x");
  public static final IUnlistedProperty<Boolean> CONN_NEG_Z =
      new UnlistedPropertyBoolean("conn_neg_z");
  public static final IUnlistedProperty<Boolean> CONN_POS_Z =
      new UnlistedPropertyBoolean("conn_pos_z");

  @SideOnly(Side.CLIENT)
  private TextureAtlasSprite sprite;

  public BlockSandbags(Material mat, String name) {
    super(mat, name);
    this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
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
  public BlockFaceShape getBlockFaceShape(
      IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
    return BlockFaceShape.UNDEFINED;
  }

  @Override
  protected BlockStateContainer createBlockState() {
    return new ExtendedBlockState(
        this,
        new IProperty[] {FACING},
        new IUnlistedProperty[] {CONN_NEG_X, CONN_POS_X, CONN_NEG_Z, CONN_POS_Z});
  }

  @Override
  public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
    if (!(state.getBlock() == this) || !(state.getPropertyKeys().contains(FACING))) return state;

    IExtendedBlockState ext = (IExtendedBlockState) state;

    IBlockState nx = world.getBlockState(pos.west());
    IBlockState px = world.getBlockState(pos.east());
    IBlockState nz = world.getBlockState(pos.north());
    IBlockState pz = world.getBlockState(pos.south());

    boolean negX = nx.getBlock() == this || nx.isOpaqueCube() || nx.isFullCube();
    boolean posX = px.getBlock() == this || px.isOpaqueCube() || px.isFullCube();
    boolean negZ = nz.getBlock() == this || nz.isOpaqueCube() || nz.isFullCube();
    boolean posZ = pz.getBlock() == this || pz.isOpaqueCube() || pz.isFullCube();

    return ext.withProperty(CONN_NEG_X, negX)
        .withProperty(CONN_POS_X, posX)
        .withProperty(CONN_NEG_Z, negZ)
        .withProperty(CONN_POS_Z, posZ);
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
  @SideOnly(Side.CLIENT)
  public boolean shouldSideBeRendered(
      @NotNull IBlockState blockState,
      @NotNull IBlockAccess blockAccess,
      @NotNull BlockPos pos,
      @NotNull EnumFacing side) {
    return true;
  }

  @Override
  public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {

    float min = 0.25F;
    float max = 0.75F;

    BlockPos nx = pos.west();
    BlockPos px = pos.east();
    BlockPos nz = pos.north();
    BlockPos pz = pos.south();

    IBlockState sNx = world.getBlockState(nx);
    IBlockState sPx = world.getBlockState(px);
    IBlockState sNz = world.getBlockState(nz);
    IBlockState sPz = world.getBlockState(pz);

    float minX = (sNx.isOpaqueCube() || sNx.isFullCube() || sNx.getBlock() == this) ? 0F : min;
    float minZ = (sNz.isOpaqueCube() || sNz.isFullCube() || sNz.getBlock() == this) ? 0F : min;
    float maxX = (sPx.isOpaqueCube() || sPx.isFullCube() || sPx.getBlock() == this) ? 1F : max;
    float maxZ = (sPz.isOpaqueCube() || sPz.isFullCube() || sPz.getBlock() == this) ? 1F : max;

    return new AxisAlignedBB(minX, 0, minZ, maxX, 1, maxZ);
  }

  @Override
  public void addCollisionBoxToList(
      IBlockState state,
      World world,
      BlockPos pos,
      AxisAlignedBB entityBox,
      List<AxisAlignedBB> collidingBoxes,
      Entity entity,
      boolean isActualState) {

    AxisAlignedBB bb = getBoundingBox(state, world, pos).offset(pos);

    if (bb.intersects(entityBox)) {
      collidingBoxes.add(bb);
    }
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
      this.sprite =
          map.registerSprite(new ResourceLocation(rl.getNamespace(), "blocks/" + rl.getPath()));
    }
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void bakeModel(ModelBakeEvent event) {
    if (this.sprite == null) return;

    ModelResourceLocation worldLoc = new ModelResourceLocation(getRegistryName(), "normal");
    ModelResourceLocation invLoc = new ModelResourceLocation(getRegistryName(), "inventory");

    IBakedModel worldModel = new BlockSandbagsBakedModel(this.sprite, false);
    IBakedModel itemModel = new BlockSandbagsBakedModel(this.sprite, true);

    event.getModelRegistry().putObject(worldLoc, worldModel);
    event.getModelRegistry().putObject(invLoc, itemModel);
  }
}
