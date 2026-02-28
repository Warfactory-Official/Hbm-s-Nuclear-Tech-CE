package com.hbm.blocks.machine;

import com.hbm.api.block.IToolable;
import com.hbm.api.energymk2.IEnergyReceiverMK2;
import com.hbm.blocks.ModBlocks;
import com.hbm.blocks.machine.FloodlightBeam.TileEntityFloodlightBeam;
import com.hbm.interfaces.AutoRegister;
import com.hbm.lib.ForgeDirection;
import com.hbm.util.Compat;
import com.hbm.util.Vec3NT;
import com.hbm.world.gen.nbt.INBTBlockTransformable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

public class Floodlight extends BlockContainer implements IToolable, INBTBlockTransformable {
  public static final PropertyInteger META = PropertyInteger.create("meta", 0, 11);

  public Floodlight(String name, Material mat) {
    super(mat);

    this.setRegistryName(name);
    this.setTranslationKey(name);

    setDefaultState(blockState.getBaseState().withProperty(META, 0));

    ModBlocks.ALL_BLOCKS.add(this);
  }

  @Override
  protected @NotNull BlockStateContainer createBlockState() {
    return new BlockStateContainer(this, META);
  }

  @Override
  public @NotNull IBlockState getStateFromMeta(int meta) {
    return this.getDefaultState().withProperty(META, meta);
  }

  @Override
  public int getMetaFromState(IBlockState state) {
    return state.getValue(META);
  }

  @Override
  public @NotNull IBlockState getStateForPlacement(
      @NotNull World world,
      @NotNull BlockPos pos,
      EnumFacing facing,
      float hitX,
      float hitY,
      float hitZ,
      int meta,
      @NotNull EntityLivingBase placer,
      @NotNull EnumHand hand) {
    return getDefaultState().withProperty(META, facing.getIndex());
  }

  @Override
  public TileEntity createNewTileEntity(World world, int meta) {
    return new TileEntityFloodlight();
  }

  @Override
  public EnumBlockRenderType getRenderType(IBlockState state) {
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

  // only method with player param, called second for variable rotation
  @Override
  public void onBlockPlacedBy(
      World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
    setAngle(worldIn, pos, placer, true);
  }

  @Override
  public boolean onScrew(
      World world,
      EntityPlayer player,
      int x,
      int y,
      int z,
      EnumFacing side,
      float fX,
      float fY,
      float fZ,
      EnumHand hand,
      ToolType tool) {
    if (tool != ToolType.SCREWDRIVER) return false;
    setAngle(world, new BlockPos(x, y, z), player, false);
    return true;
  }

  public void setAngle(World world, BlockPos pos, EntityLivingBase player, boolean updateMeta) {
    int i = MathHelper.floor(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
    float rotation = player.rotationPitch;

    TileEntity tile = world.getTileEntity(pos);

    if (tile instanceof TileEntityFloodlight floodlight) {
      IBlockState state = world.getBlockState(pos);
      int meta = state.getValue(META) % 6;

      if (meta == 0 || meta == 1) {
        if (i == 0 || i == 2)
          if (updateMeta) {
            world.setBlockState(pos, state.withProperty(META, meta + 6), 3);
          }
        if (meta == 1) if (i == 0 || i == 1) rotation = 180F - rotation;
        if (meta == 0) if (i == 0 || i == 3) rotation = 180F - rotation;
      }

      floodlight.rotation = -Math.round(rotation / 5F) * 5F;
      if (floodlight.isOn) floodlight.destroyLights();
      tile.markDirty();
    }
  }

  @Override
  public int transformMeta(int meta, int coordBaseMode) {
    if (meta < 6) {
      switch (coordBaseMode) {
        case 1: // West
          switch (meta) {
            case 2:
              return 5;
            case 3:
              return 4;
            case 4:
              return 2;
            case 5:
              return 3;
          }
          break;
        case 2: // North
          switch (meta) {
            case 2:
              return 3;
            case 3:
              return 2;
            case 4:
              return 5;
            case 5:
              return 4;
          }
          break;
        case 3: // East
          switch (meta) {
            case 2:
              return 4;
            case 3:
              return 5;
            case 4:
              return 3;
            case 5:
              return 2;
          }
          break;
      }
    }

    // Also rotate the upper bits that store additional state (6-11)
    if (meta >= 6) {
      return transformMeta(meta - 6, coordBaseMode) + 6;
    }

    return meta;
  }

  @Override
  public Block transformBlock(Block block) {
    return block; // No block transformation needed
  }

  @AutoRegister
  public static class TileEntityFloodlight extends TileEntity
      implements IEnergyReceiverMK2, ITickable {

    public float rotation;
    protected BlockPos[] lightPos = new BlockPos[15];
    public static final long maxPower = 5_000;
    public long power;

    public int delay;
    public boolean isOn;

    @Override
    public void update() {
      if (!world.isRemote) {

        ForgeDirection dir =
            ForgeDirection.getOrientation(this.getBlockMetadata() % 6).getOpposite();
        this.trySubscribe(
            world,
            getPos().getX() + dir.offsetX,
            getPos().getY() + dir.offsetY,
            getPos().getZ() + dir.offsetZ,
            dir);

        if (delay > 0) {
          delay--;
          return;
        }

        if (power >= 100) {
          power -= 100;

          if (!isOn) {
            IBlockState state = world.getBlockState(pos);

            this.isOn = true;
            this.castLights();
            markDirty();
            world.notifyBlockUpdate(pos, state, state, 3);
          } else {

            long timer = world.getTotalWorldTime();
            if (timer % 5 == 0) {
              timer = timer / 5;
              this.castLight((int) Math.abs(timer % this.lightPos.length));
            }
          }

        } else {
          if (isOn) {
            IBlockState state = world.getBlockState(pos);

            this.isOn = false;
            this.delay = 60;
            this.destroyLights();
            markDirty();
            world.notifyBlockUpdate(pos, state, state, 3);
          }
        }
      }
    }

    private void castLight(int index) {
      BlockPos newPos = this.getRayEndpoint(index);
      BlockPos oldPos = this.lightPos[index];
      this.lightPos[index] = null;

      if (newPos == null
          || !newPos.equals(
              oldPos)) { // if the new end point is null or not equal to the previous, delete the
        // previous spot
        if (oldPos != null) {
          TileEntity tile =
              Compat.getTileStandard(world, oldPos.getX(), oldPos.getY(), oldPos.getZ());
          if (tile instanceof TileEntityFloodlightBeam beam) {
            if (beam.cache == this) {
              world.setBlockState(oldPos, Blocks.AIR.getDefaultState(), 2);
            }
          }
        }
      }

      if (newPos == null) return;

      if (world.getBlockState(newPos).getBlock() == Blocks.AIR) {
        world.setBlockState(newPos, ModBlocks.floodlight_beam.getDefaultState(), 2);
        TileEntity tile =
            Compat.getTileStandard(world, newPos.getX(), newPos.getY(), newPos.getZ());
        if (tile instanceof TileEntityFloodlightBeam)
          ((TileEntityFloodlightBeam) tile)
              .setSource(this, newPos.getX(), newPos.getY(), newPos.getZ(), index);
        this.lightPos[index] = newPos;
      }

      if (world.getBlockState(newPos).getBlock() == ModBlocks.floodlight_beam) {
        this.lightPos[index] = newPos;
      }
    }

    public BlockPos getRayEndpoint(int index) {

      if (index < 0 || index >= lightPos.length) return null;

      int meta = this.getBlockMetadata();
      Vec3NT dir = new Vec3NT(1, 0, 0);

      float[] angles = getVariation(index);

      float rotation = this.rotation;
      if (meta == 1 || meta == 7) rotation = 180 - rotation;
      if (meta == 6) rotation = 180 - rotation;
      dir.rotateRollSelf((float) (rotation / 180D * Math.PI) + angles[0]);

      if (meta == 6) dir.rotateYawSelf((float) (Math.PI / 2D));
      if (meta == 7) dir.rotateYawSelf((float) (Math.PI / 2D));
      if (meta == 2) dir.rotateYawSelf((float) (Math.PI / 2D));
      if (meta == 3) dir.rotateYawSelf((float) -(Math.PI / 2D));
      if (meta == 4) dir.rotateYawSelf((float) (Math.PI));
      dir.rotateYawSelf(angles[1]);

      for (int i = 1; i < 64; i++) {
        int x = getPos().getX();
        int y = getPos().getY();
        int z = getPos().getZ();

        int iX = (int) Math.floor(x + 0.5 + dir.x * i);
        int iY = (int) Math.floor(y + 0.5 + dir.y * i);
        int iZ = (int) Math.floor(z + 0.5 + dir.z * i);

        if (iX == x && iY == y && iZ == z) continue;

        IBlockState state = world.getBlockState(new BlockPos(iX, iY, iZ));
        if (state.getLightOpacity(world, new BlockPos(iX, iY, iZ)) < 127) continue;

        int fX = (int) Math.floor(x + 0.5 + dir.x * (i - 1));
        int fY = (int) Math.floor(y + 0.5 + dir.y * (i - 1));
        int fZ = (int) Math.floor(z + 0.5 + dir.z * (i - 1));

        if (i > 1) return new BlockPos(fX, fY, fZ);
      }

      return null;
    }

    private void castLights() {
      for (int i = 0; i < this.lightPos.length; i++) this.castLight(i);
    }

    private void destroyLight(int index) {
      BlockPos pos = lightPos[index];
      if (pos != null && world.getBlockState(pos).getBlock() == ModBlocks.floodlight_beam) {
        world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
      }
    }

    private void destroyLights() {
      for (int i = 0; i < this.lightPos.length; i++) destroyLight(i);
    }

    private float[] getVariation(int index) {
      return new float[] {
        ((((float) index / 3) - 2) * 7.5F) / 180F * (float) Math.PI,
        (((index % 3) - 1) * 15F) / 180F * (float) Math.PI
      };
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
      NBTTagCompound nbt = new NBTTagCompound();
      this.writeToNBT(nbt);
      return new SPacketUpdateTileEntity(getPos(), 0, nbt);
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

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
      super.readFromNBT(nbt);
      this.rotation = nbt.getFloat("rotation");
      this.power = nbt.getLong("power");
      this.isOn = nbt.getBoolean("isOn");
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
      super.writeToNBT(nbt);
      nbt.setFloat("rotation", rotation);
      nbt.setLong("power", power);
      nbt.setBoolean("isOn", isOn);
      return nbt;
    }

    @Override
    public long getPower() {
      return power;
    }

    @Override
    public void setPower(long power) {
      this.power = power;
    }

    @Override
    public long getMaxPower() {
      return maxPower;
    }

    private boolean isLoaded = true;

    @Override
    public boolean isLoaded() {
      return isLoaded;
    }

    @Override
    public void onChunkUnload() {
      this.isLoaded = false;
    }

    AxisAlignedBB bb = null;

    @Override
    public AxisAlignedBB getRenderBoundingBox() {

      if (bb == null) {
        int x = getPos().getX();
        int y = getPos().getY();
        int z = getPos().getZ();
        bb = new AxisAlignedBB(x - 1, y - 1, z - 1, x + 2, y + 2, z + 2);
      }

      return bb;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
      return 65536.0D;
    }
  }
}
