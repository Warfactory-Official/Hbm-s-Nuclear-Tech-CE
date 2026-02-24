package com.hbm.blocks.machine;

import com.hbm.Tags;
import com.hbm.blocks.BlockEnums.LightType;
import com.hbm.blocks.ISpotlight;
import com.hbm.blocks.ModBlocks;
import com.hbm.items.IDynamicModels;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.render.model.BlockSpotlightBakedModel;
import com.hbm.world.gen.nbt.INBTBlockTransformable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockSlab.EnumBlockHalf;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Random;

public class Spotlight extends Block implements ISpotlight, INBTBlockTransformable, IDynamicModels {
  @SideOnly(Side.CLIENT)
  public TextureAtlasSprite sprite;

  public static final PropertyDirection FACING = PropertyDirection.create("facing");
  public static final PropertyBool BROKEN = PropertyBool.create("broken");

  public static final PropertyBool CONN_NORTH = PropertyBool.create("conn_north");
  public static final PropertyBool CONN_SOUTH = PropertyBool.create("conn_south");
  public static final PropertyBool CONN_WEST = PropertyBool.create("conn_west");
  public static final PropertyBool CONN_EAST = PropertyBool.create("conn_east");
  public static final PropertyBool CONN_UP = PropertyBool.create("conn_up");
  public static final PropertyBool CONN_DOWN = PropertyBool.create("conn_down");

  public static final int META_YELLOW = 0;
  public static final int META_GREEN = 1;
  public static final int META_BLUE = 2;

  public static boolean disableOnGeneration = true;

  // I'd be extending the ReinforcedLamp class if it wasn't for the inverted behaviour of these
  // specific lights
  // I want these blocks to be eminently useful, so removing the need for redstone by default is
  // desired,
  // these act more like redstone torches, in that applying a signal turns them off
  public boolean isOn;

  public int beamLength;
  public LightType type;

  public Spotlight(String name, Material mat, int beamLength, LightType type, boolean isOn) {
    super(mat);

    this.setRegistryName(name);
    this.setTranslationKey(name);

    this.beamLength = beamLength;
    this.type = type;
    this.isOn = isOn;

    this.setHardness(0.5F);

    if (isOn) setLightLevel(1.0F);

    setDefaultState(
        this.blockState
            .getBaseState()
            .withProperty(FACING, EnumFacing.UP)
            .withProperty(BROKEN, false)
            .withProperty(CONN_UP, false)
            .withProperty(CONN_DOWN, false)
            .withProperty(CONN_NORTH, false)
            .withProperty(CONN_SOUTH, false)
            .withProperty(CONN_WEST, false)
            .withProperty(CONN_EAST, false));

    ModBlocks.ALL_BLOCKS.add(this);
    IDynamicModels.INSTANCES.add(this);
  }

  public String getPartName(int connectionCount) {
    if (Objects.requireNonNull(type) == LightType.HALOGEN) {
      return "FloodLamp";
    }
    return "CageLamp";
  }

  @Override
  public BlockFaceShape getBlockFaceShape(
      IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
    return BlockFaceShape.UNDEFINED;
  }

  @Override
  public EnumBlockRenderType getRenderType(@NotNull IBlockState state) {
    return EnumBlockRenderType.MODEL;
  }

  public String getModel() {
    return switch (type) {
      case FLUORESCENT -> "fluorescent_lamp";
      case HALOGEN -> "flood_lamp";
      default -> "cage_lamp";
    };
  }

  @Override
  public @NotNull IBlockState getStateForPlacement(
      @NotNull World worldIn,
      @NotNull BlockPos pos,
      @NotNull EnumFacing facing,
      float hitX,
      float hitY,
      float hitZ,
      int meta,
      @NotNull EntityLivingBase placer) {
    return this.getDefaultState().withProperty(FACING, facing).withProperty(BROKEN, false);
  }

  @Override
  protected @NotNull BlockStateContainer createBlockState() {
    return new BlockStateContainer(
        this, FACING, BROKEN, CONN_UP, CONN_DOWN, CONN_NORTH, CONN_SOUTH, CONN_WEST, CONN_EAST);
  }

  @Override
  public @NotNull IBlockState getStateFromMeta(int meta) {

    boolean broken = (meta & 1) == 1;

    int faceBits = (meta >> 1) & 7;
    if (faceBits > 5) faceBits = 0;

    EnumFacing facing = EnumFacing.byIndex(faceBits);

    return getDefaultState().withProperty(BROKEN, broken).withProperty(FACING, facing);
  }

  @Override
  public int getMetaFromState(IBlockState state) {
    int m = state.getValue(BROKEN) ? 1 : 0;
    m |= (state.getValue(FACING).getIndex() << 1);
    return m;
  }

  @Override
  public @NotNull IBlockState withRotation(IBlockState state, Rotation rot) {
    return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
  }

  @Override
  public @NotNull IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
    return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
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
  public @NotNull MapColor getMapColor(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
    return MapColor.AIR;
  }

  @Nullable
  @Override
  public AxisAlignedBB getCollisionBoundingBox(
      @NotNull IBlockState blockState, @NotNull IBlockAccess worldIn, @NotNull BlockPos pos) {
    return NULL_AABB;
  }

  @Override
  public @NotNull AxisAlignedBB getBoundingBox(
      @NotNull IBlockState state, @NotNull IBlockAccess source, @NotNull BlockPos pos) {
    EnumFacing facing = state.getValue(FACING);
    float[] bounds = swizzleBounds(facing);
    float[] offset =
        new float[] {
          0.5F - facing.getXOffset() * (0.5F - bounds[0]),
          0.5F - facing.getYOffset() * (0.5F - bounds[1]),
          0.5F - facing.getZOffset() * (0.5F - bounds[2])
        };

    return new AxisAlignedBB(
        offset[0] - bounds[0],
        offset[1] - bounds[1],
        offset[2] - bounds[2],
        offset[0] + bounds[0],
        offset[1] + bounds[1],
        offset[2] + bounds[2]);
  }

  private float[] swizzleBounds(EnumFacing facing) {
    float[] bounds = getBounds();
    return switch (facing) {
      case EAST, WEST -> new float[] {bounds[2], bounds[1], bounds[0]};
      case UP, DOWN -> new float[] {bounds[1], bounds[2], bounds[0]};
      default -> bounds;
    };
  }

  // Returns an xyz (half-)size for a given object type
  private float[] getBounds() {
    return switch (type) {
      case FLUORESCENT -> new float[] {0.5F, 0.5F, 0.1F};
      case HALOGEN -> new float[] {0.35F, 0.25F, 0.2F};
      default -> new float[] {0.25F, 0.2F, 0.15F};
    };
  }

  @Override
  public void onBlockAdded(World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state) {
    if (worldIn.isRemote) return;
    if (updatePower(worldIn, pos)) return;
    updateBeam(worldIn, pos, state);
  }

  private boolean canConnectTo(IBlockAccess world, BlockPos pos) {
    return world.getBlockState(pos).getBlock() == this;
  }

  @Override
  public IBlockState getActualState(IBlockState state, IBlockAccess world, BlockPos pos) {
    boolean cu = canConnectTo(world, pos.up());
    boolean cd = canConnectTo(world, pos.down());
    boolean cn = canConnectTo(world, pos.north());
    boolean cs = canConnectTo(world, pos.south());
    boolean cw = canConnectTo(world, pos.west());
    boolean ce = canConnectTo(world, pos.east());
    return state
        .withProperty(CONN_UP, cu)
        .withProperty(CONN_DOWN, cd)
        .withProperty(CONN_NORTH, cn)
        .withProperty(CONN_SOUTH, cs)
        .withProperty(CONN_WEST, cw)
        .withProperty(CONN_EAST, ce);
  }

  private boolean updatePower(World world, BlockPos pos) {
    IBlockState state = world.getBlockState(pos);
    if (isBroken(state)) return false;

    boolean isPowered = world.isBlockPowered(pos);
    if (isOn && isPowered) {
      world.scheduleBlockUpdate(pos, this, 4, 0);
      return true;
    } else if (!isOn && !isPowered) {
      world.setBlockState(
          pos,
          getOn()
              .getDefaultState()
              .withProperty(FACING, state.getValue(FACING))
              .withProperty(BROKEN, state.getValue(BROKEN)),
          2);
      return true;
    }

    return false;
  }

  @Override
  public void breakBlock(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState state) {
    super.breakBlock(world, pos, state);

    if (world.isRemote) return;

    unpropagateBeam(world, pos, state.getValue(FACING));
  }

  @Override
  public void updateTick(
      @NotNull World world,
      @NotNull BlockPos pos,
      @NotNull IBlockState state,
      @NotNull Random rand) {
    if (world.isRemote) return;

    if (isOn && world.isBlockPowered(pos)) {
      IBlockState newState =
          getOff()
              .getDefaultState()
              .withProperty(FACING, state.getValue(FACING))
              .withProperty(BROKEN, state.getValue(BROKEN));

      if (state != newState) {
        world.setBlockState(pos, newState, 2);
      }
    }
  }

  // Repropagate the beam if we've become unblocked
  @Override
  public void neighborChanged(
      @NotNull IBlockState state,
      World world,
      @NotNull BlockPos pos,
      @NotNull Block blockIn,
      @NotNull BlockPos fromPos) {
    if (world.isRemote) return;
    if (blockIn instanceof SpotlightBeam) return;
    if (blockIn == Blocks.AIR) return;

    EnumFacing facing = state.getValue(FACING);
    BlockPos checkPos = pos.offset(facing.getOpposite());
    IBlockState checkState = world.getBlockState(checkPos);

    if (!canPlace(world, checkPos, checkState, facing)) {
      world.destroyBlock(pos, true);
      return;
    }

    if (updatePower(world, pos)) return;

    updateBeam(world, pos, state);
  }

  @Override
  public boolean canPlaceBlockOnSide(
      @NotNull World worldIn, @NotNull BlockPos pos, @NotNull EnumFacing side) {
    if (!super.canPlaceBlockOnSide(worldIn, pos, side)) return false;
    BlockPos checkPos = pos.offset(side.getOpposite());
    IBlockState checkState = worldIn.getBlockState(checkPos);

    return canPlace(worldIn, pos, checkState, side);
  }

  // BlockSlab doesn't actually properly return isSideSolid,
  // probably because MOJANK thought this would only ever be used for torches,
  // which can't be placed on ceilings...
  private boolean canPlace(World world, BlockPos pos, IBlockState state, EnumFacing side) {
    if (state.getBlock() instanceof BlockSlab) {
      EnumBlockHalf half = state.getValue(BlockSlab.HALF);
      return (half == EnumBlockHalf.BOTTOM && side == EnumFacing.DOWN) || state.isOpaqueCube();
    }

    return state.isSideSolid(world, pos, side);
  }

  private void updateBeam(World world, BlockPos pos, IBlockState state) {
    if (!isOn) return;

    propagateBeam(world, pos, state.getValue(FACING), beamLength); // META_YELLOW
  }

  // Replace bulbs on broken lights with a click
  @Override
  public boolean onBlockActivated(
      World world,
      BlockPos pos,
      IBlockState state,
      EntityPlayer player,
      EnumHand hand,
      EnumFacing facing,
      float hitX,
      float hitY,
      float hitZ) {
    if (!isBroken(state)) return false;

    repair(world, pos);
    return true;
  }

  private void repair(World world, BlockPos pos) {
    if (!isBroken(world.getBlockState(pos))) return;

    world.setBlockState(
        pos,
        getOn()
            .getDefaultState()
            .withProperty(FACING, world.getBlockState(pos).getValue(FACING))
            .withProperty(BROKEN, false),
        2);

    for (EnumFacing facing : EnumFacing.VALUES) {
      int ox = pos.getX() + facing.getXOffset();
      int oy = pos.getY() + facing.getYOffset();
      int oz = pos.getZ() + facing.getZOffset();
      IBlockState state = world.getBlockState(new BlockPos(ox, oy, oz));

      if (state.getBlock() == this) repair(world, new BlockPos(ox, oy, oz));
    }
  }

  public boolean isBroken(IBlockState state) {
    return state.getValue(BROKEN);
  }

  @Override
  public @NotNull Item getItemDropped(
      @NotNull IBlockState state, @NotNull Random rand, int fortune) {
    return Item.getItemFromBlock(getOn());
  }

  @Override
  public @NotNull ItemStack getPickBlock(
      @NotNull IBlockState state,
      @NotNull RayTraceResult target,
      @NotNull World world,
      @NotNull BlockPos pos,
      @NotNull EntityPlayer player) {
    return new ItemStack(getOn());
  }

  // Recursively add beam blocks, updating any that already exist with new incoming light directions
  public static void propagateBeam(World world, BlockPos pos, EnumFacing facing, int distance) {
    distance--;
    if (distance <= 0) return;

    pos = pos.offset(facing);

    IBlockState state = world.getBlockState(pos);
    if (!state.getBlock().isAir(state, world, pos)) return;

    if (!(state.getBlock() instanceof SpotlightBeam)) {
      world.setBlockState(pos, ModBlocks.spotlight_beam.getDefaultState(), 3);
    }

    // If we encounter an existing beam, add a new INCOMING direction to the
    // metadata, and cancel propagation if something goes wrong
    if (SpotlightBeam.setDirection(world, pos, facing, true) == 0) return;

    propagateBeam(world, pos, facing, distance);
  }

  // Recursively delete beam blocks, if they aren't still illuminated from a different direction
  public static void unpropagateBeam(World world, BlockPos pos, EnumFacing facing) {
    pos = pos.offset(facing);

    Block block = world.getBlockState(pos).getBlock();
    if (!(block instanceof SpotlightBeam)) return;

    // Remove the metadata associated with this direction
    // If all directions are set to zero, delete the beam
    if (SpotlightBeam.setDirection(world, pos, facing, false) == 0) {
      world.setBlockToAir(pos);
    }

    unpropagateBeam(world, pos, facing);
  }

  // Travels back through a beam to the source, and if found, repropagates the beam
  public static void backPropagate(World world, BlockPos pos, EnumFacing facing) {
    pos = pos.offset(facing.getOpposite());

    IBlockState block = world.getBlockState(pos);
    if (block instanceof ISpotlight spot) {
      propagateBeam(world, pos, facing, spot.getBeamLength());
    } else if (!(block instanceof SpotlightBeam)) {
      return;
    }

    backPropagate(world, pos, facing);
  }

  protected Block getOff() {
    if (this == ModBlocks.spotlight_incandescent) return ModBlocks.spotlight_incandescent_off;
    if (this == ModBlocks.spotlight_fluoro) return ModBlocks.spotlight_fluoro_off;
    if (this == ModBlocks.spotlight_halogen) return ModBlocks.spotlight_halogen_off;

    return this;
  }

  protected Block getOn() {
    if (this == ModBlocks.spotlight_incandescent_off) return ModBlocks.spotlight_incandescent;
    if (this == ModBlocks.spotlight_fluoro_off) return ModBlocks.spotlight_fluoro;
    if (this == ModBlocks.spotlight_halogen_off) return ModBlocks.spotlight_halogen;

    return this;
  }

  @Override
  public int getBeamLength() {
    return this.beamLength;
  }

  @Override
  public int transformMeta(int meta, int coordBaseMode) {
    // +1 to set as broken, won't turn on until broken and replaced
    int disabled = disableOnGeneration ? 1 : 0;
    return (INBTBlockTransformable.transformMetaDeco(meta >> 1, coordBaseMode) << 1) + disabled;
  }

  @Override
  public Block transformBlock(Block block) {
    if (!disableOnGeneration) return block;
    if (block == ModBlocks.spotlight_incandescent) return ModBlocks.spotlight_incandescent_off;
    if (block == ModBlocks.spotlight_fluoro) return ModBlocks.spotlight_fluoro_off;
    if (block == ModBlocks.spotlight_halogen) return ModBlocks.spotlight_halogen_off;
    return block;
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void registerModel() {
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
    if (rl == null) {
      return;
    }

    String sprite = getModel();

    if (!isOn) {
      sprite += "_off";
    }

    this.sprite = map.registerSprite(new ResourceLocation(rl.getNamespace(), "blocks/" + sprite));
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void bakeModel(ModelBakeEvent event) {
    ResourceLocation rl = getRegistryName();

    if (rl == null) {
      return;
    }

    HFRWavefrontObject model;
    try {
      model =
          new HFRWavefrontObject(
              new ResourceLocation(Tags.MODID, String.format("models/lights/%s.obj", getModel())));
    } catch (Exception _) {
      return;
    }

    IBakedModel blockModel = BlockSpotlightBakedModel.forBlock(model, sprite, type);
    IBakedModel itemModel = BlockSpotlightBakedModel.forItem(model, sprite, type);

    ModelResourceLocation mrlBlock = new ModelResourceLocation(rl, "normal");
    ModelResourceLocation mrlItem = new ModelResourceLocation(rl, "inventory");

    event.getModelRegistry().putObject(mrlBlock, blockModel);
    event.getModelRegistry().putObject(mrlItem, itemModel);
  }
}
