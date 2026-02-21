package com.hbm.blocks.bomb;

import com.hbm.Tags;
import com.hbm.api.block.IExploder;
import com.hbm.api.block.IToolable;
import com.hbm.blocks.ModBlocks;
import com.hbm.entity.item.EntityTNTPrimedBase;
import com.hbm.interfaces.IBomb;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.tileentity.bomb.TileEntityCharge;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;


public abstract class BlockChargeBase extends BlockContainer implements IBomb, IToolable, IExploder {

    public static final PropertyDirection FACING = PropertyDirection.create("facing");

    private static final AxisAlignedBB AABB_UP = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 6.0D / 16.0D, 1.0D);
    private static final AxisAlignedBB AABB_DOWN = new AxisAlignedBB(0.0D, 10.0D / 16.0D, 0.0D, 1.0D, 1.0D, 1.0D);
    private static final AxisAlignedBB AABB_NORTH = new AxisAlignedBB(0.0D, 0.0D, 10.0D / 16.0D, 1.0D, 1.0D, 1.0D);
    private static final AxisAlignedBB AABB_SOUTH = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 6.0D / 16.0D);
    private static final AxisAlignedBB AABB_WEST = new AxisAlignedBB(10.0D / 16.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);
    private static final AxisAlignedBB AABB_EAST = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 6.0D / 16.0D, 1.0D, 1.0D);
    public static boolean safe = false;
    protected final ResourceLocation texture;

    protected BlockChargeBase(String registryName) {
        super(Material.TNT);
        this.texture = new ResourceLocation(Tags.MODID, "blocks/" + registryName);

        setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.UP));
        setTranslationKey(registryName);
        setRegistryName(registryName);
        setLightOpacity(0);
        ModBlocks.ALL_BLOCKS.add(this);
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityCharge();
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state){
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
        if (worldIn.getTileEntity(pos) instanceof TileEntityCharge charge && placer instanceof EntityPlayerMP playerMP)
            charge.placerID = playerMP.getUniqueID();
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        EnumFacing facing = EnumFacing.byIndex(meta & 7);
        return this.getDefaultState().withProperty(FACING, facing);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getIndex();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta,
                                            EntityLivingBase placer) {
        return this.getDefaultState().withProperty(FACING, facing);
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
        return NULL_AABB;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return switch (state.getValue(FACING)) {
            case DOWN -> AABB_DOWN;
            case UP -> AABB_UP;
            case NORTH -> AABB_NORTH;
            case SOUTH -> AABB_SOUTH;
            case WEST -> AABB_WEST;
            case EAST -> AABB_EAST;
        };
    }

    @Override
    public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side) {
        BlockPos support = pos.offset(side.getOpposite());
        return worldIn.isSideSolid(support, side, true);
    }

    @Override
    public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, net.minecraft.block.Block blockIn, BlockPos fromPos) {
        EnumFacing dir = state.getValue(FACING);
        BlockPos support = pos.offset(dir.getOpposite());
        if (!worldIn.isSideSolid(support, dir, true)) {
            worldIn.setBlockToAir(pos);
        }
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Items.AIR;
    }

    @Override
    public int quantityDropped(Random random) {
        return 0;
    }

    @Override
    public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, EnumFacing side, float fX, float fY, float fZ, EnumHand hand,
                           ToolType tool) {
        if (tool != ToolType.DEFUSER) return false;
        BlockPos pos = new BlockPos(x, y, z);
        TileEntity te = world.getTileEntity(pos);
        if (!(te instanceof TileEntityCharge charge)) return false;

        if (charge.started) {
            charge.started = false;
            world.playSound(null, pos, HBMSoundHandler.fstbmbStart, SoundCategory.BLOCKS, 10.0F, 1.0F);
            charge.markDirty();
        } else {
            safe = true;
            this.dismantle(world, pos);
            safe = false;
        }
        return true;
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        super.breakBlock(worldIn, pos, state);
        if (!safe) {
            explode(worldIn, pos, null);
        }
    }

    @Override
    public void onBlockExploded(World world, BlockPos pos, Explosion explosion) {
        if (!world.isRemote) {
            EntityTNTPrimedBase tntPrimed = new EntityTNTPrimedBase(world, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D,
                    explosion.getExplosivePlacedBy(), world.getBlockState(pos));
            tntPrimed.fuse = 0;
            tntPrimed.detonateOnCollision = false;
            world.spawnEntity(tntPrimed);
        }
        super.onBlockExploded(world, pos, explosion);
    }

    @Override
    public void explodeEntity(World world, double x, double y, double z, EntityTNTPrimedBase entity) {
        explode(world, new BlockPos(MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z)), null);
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                    float hitX, float hitY, float hitZ) {
        if (worldIn.isRemote) return true;

        TileEntity te = worldIn.getTileEntity(pos);
        if (!(te instanceof TileEntityCharge charge)) return false;

        if (!charge.started) {
            if (playerIn.isSneaking()) {
                if (charge.timer > 0) {
                    charge.started = true;
                    worldIn.playSound(null, pos, HBMSoundHandler.fstbmbStart, SoundCategory.BLOCKS, 1.0F, 1.0F);
                }
            } else {
                switch (charge.timer) {
                    case 0 -> charge.timer = 100;
                    case 100 -> charge.timer = 200;
                    case 200 -> charge.timer = 300;
                    case 300 -> charge.timer = 600;
                    case 600 -> charge.timer = 1200;
                    case 1200 -> charge.timer = 3600;
                    case 3600 -> charge.timer = 6000;
                    default -> charge.timer = 0;
                }
                worldIn.playSound(null, pos, HBMSoundHandler.techBoop, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }
            charge.markDirty();
        }
        return true;
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
        tooltip.add(TextFormatting.YELLOW + "Right-click to change timer.");
        tooltip.add(TextFormatting.YELLOW + "Sneak-click to arm.");
        tooltip.add(TextFormatting.RED + "Can only be disarmed and removed with defuser.");
    }

    protected void dismantle(World world, BlockPos pos) {
        world.setBlockToAir(pos);
        Item item = Item.getItemFromBlock(this);
        if (item == Items.AIR) return;

        ItemStack stack = new ItemStack(item, 1, this.damageDropped(world.getBlockState(pos)));
        Random rand = world.rand;

        double ox = rand.nextFloat() * 0.6F + 0.2F;
        double oy = rand.nextFloat() * 0.2F + 1.0F;
        double oz = rand.nextFloat() * 0.6F + 0.2F;

        EntityItem entityitem = new EntityItem(world, pos.getX() + ox, pos.getY() + oy, pos.getZ() + oz, stack);

        double v = 0.05D;
        entityitem.motionX = rand.nextGaussian() * v;
        entityitem.motionY = rand.nextGaussian() * v + 0.2D;
        entityitem.motionZ = rand.nextGaussian() * v;

        if (!world.isRemote) world.spawnEntity(entityitem);
    }
}
