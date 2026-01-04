package com.hbm.blocks.generic;

import com.hbm.blocks.ModBlocks;
import com.hbm.handler.WeightedRandomChestContentFrom1710;
import com.hbm.itempool.ItemPool;
import com.hbm.itempool.ItemPoolsRedRoom;
import com.hbm.items.ModItems;
import com.hbm.items.tool.ItemModDoor;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.lib.Library;
import com.hbm.main.AdvancementManager;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class BlockRedBrickKeyhole extends Block {
    public static final PropertyInteger META = PropertyInteger.create("meta", 0, 6);

    public BlockRedBrickKeyhole(Material m, String s) {
        super(m);
        this.setRegistryName(s);
        this.setTranslationKey(s);
        this.setDefaultState(this.blockState.getBaseState().withProperty(META, 0));

        ModBlocks.ALL_BLOCKS.add(this);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, META);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(META, meta);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(META);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Items.AIR;
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return new ItemStack(ModBlocks.brick_red);
    }

    @Override
    public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
        return getDefaultState().withProperty(META, EnumFacing.getDirectionFromEntityLiving(pos, placer).getIndex());
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if(!player.getHeldItem(hand).isEmpty()) {
            boolean cracked = player.getHeldItem(hand).getItem() == ModItems.key_red_cracked;
            if((player.getHeldItem(hand).getItem() == ModItems.key_red || cracked) && facing != EnumFacing.UP && facing != EnumFacing.DOWN) {
                if(cracked) player.getHeldItem(hand).shrink(1);
                if(world.isRemote) return true;
                ForgeDirection dir = ForgeDirection.getOrientation(facing);
                generateRoom(world, pos.getX() - dir.offsetX * 4, pos.getY() - 2, pos.getZ() - dir.offsetZ * 4, dir);
                ItemModDoor.placeDoor(world, pos.down(), facing.getOpposite(), ModBlocks.door_red, false);
                world.playSound(player, pos, HBMSoundHandler.lockOpen, SoundCategory.BLOCKS, 1.0F, 1.0F);
                PlayerAdvancements advancements = ((EntityPlayerMP) player).getAdvancements();
                advancements.grantCriterion(AdvancementManager.achRedRoom, "impossible");
                return true;
            }
        }

        return false;
    }

    protected static void generateRoom(World world, int x, int y, int z, ForgeDirection dir) {

        int size = 9;
        int height = 5;
        int width = size / 2;

        //Outer Edges, top and bottom
        for(int i = -width; i <= width; i++) {
            world.setBlockState(new BlockPos(x + i, y, z + width), ModBlocks.brick_red.getDefaultState().withProperty(BlockRedBrick.META, 6));
            world.setBlockState(new BlockPos(x + i, y, z - width), ModBlocks.brick_red.getDefaultState().withProperty(BlockRedBrick.META, 6));
            world.setBlockState(new BlockPos(x + width, y, z + i), ModBlocks.brick_red.getDefaultState().withProperty(BlockRedBrick.META, 6));
            world.setBlockState(new BlockPos(x - width, y, z + i), ModBlocks.brick_red.getDefaultState().withProperty(BlockRedBrick.META, 6));
            world.setBlockState(new BlockPos(x + i, y + height - 1, z + width), ModBlocks.brick_red.getDefaultState().withProperty(BlockRedBrick.META, 6));
            world.setBlockState(new BlockPos(x + i, y + height - 1, z - width), ModBlocks.brick_red.getDefaultState().withProperty(BlockRedBrick.META, 6));
            world.setBlockState(new BlockPos(x + width, y + height - 1, z + i), ModBlocks.brick_red.getDefaultState().withProperty(BlockRedBrick.META, 6));
            world.setBlockState(new BlockPos(x - width, y + height - 1, z + i), ModBlocks.brick_red.getDefaultState().withProperty(BlockRedBrick.META, 6));
        }

        for(int i = 1; i <= height - 2; i++) {
            //Outer edges, sides
            world.setBlockState(new BlockPos(x + width, y + i, z + width), ModBlocks.brick_red.getDefaultState().withProperty(BlockRedBrick.META, 6));
            world.setBlockState(new BlockPos(x + width, y + i, z - width), ModBlocks.brick_red.getDefaultState().withProperty(BlockRedBrick.META, 6));
            world.setBlockState(new BlockPos(x - width, y + i, z + width), ModBlocks.brick_red.getDefaultState().withProperty(BlockRedBrick.META, 6));
            world.setBlockState(new BlockPos(x - width, y + i, z - width), ModBlocks.brick_red.getDefaultState().withProperty(BlockRedBrick.META, 6));

            //Walls
            for(int j = -width + 1; j <= width - 1; j++) {
                if(dir != Library.POS_X) world.setBlockState(new BlockPos(x + width, y + i, z + j), ModBlocks.brick_red.getDefaultState().withProperty(BlockRedBrick.META, 6));
                if(dir != Library.NEG_X) world.setBlockState(new BlockPos(x - width, y + i, z + j), ModBlocks.brick_red.getDefaultState().withProperty(BlockRedBrick.META, 6));
                if(dir != Library.POS_Z) world.setBlockState(new BlockPos(x + j, y + i, z + width), ModBlocks.brick_red.getDefaultState().withProperty(BlockRedBrick.META, 6));
                if(dir != Library.NEG_Z) world.setBlockState(new BlockPos(x + j, y + i, z - width), ModBlocks.brick_red.getDefaultState().withProperty(BlockRedBrick.META, 6));
            }
        }

        for(int i = -width + 1; i <= width - 1; i++) {
            for(int j = -width + 1; j <= width - 1; j++) {
                //Floor and ceiling
                world.setBlockState(new BlockPos(x + i, y, z + j), ModBlocks.brick_red.getDefaultState().withProperty(BlockRedBrick.META, 6));
                world.setBlockState(new BlockPos(x + i, y + height - 1, z + j), ModBlocks.brick_red.getDefaultState().withProperty(BlockRedBrick.META, 6));

                for(int k = 1; k <= height - 2; k++) {
                    world.setBlockToAir(new BlockPos(x + i, y + k, z + j));
                }
            }
        }

        spawnPedestalItem(world, x, y + 1, z, ItemPool.getPool(ItemPoolsRedRoom.POOL_BLACK_SLAB));
        if(world.rand.nextBoolean()) spawnPedestalItem(world, x + 2, y + 1, z, ItemPool.getPool(ItemPoolsRedRoom.POOL_BLACK_PART));
        if(world.rand.nextBoolean()) spawnPedestalItem(world, x - 2, y + 1, z, ItemPool.getPool(ItemPoolsRedRoom.POOL_BLACK_PART));
        if(world.rand.nextBoolean()) spawnPedestalItem(world, x, y + 1, z + 2, ItemPool.getPool(ItemPoolsRedRoom.POOL_BLACK_PART));
        if(world.rand.nextBoolean()) spawnPedestalItem(world, x, y + 1, z - 2, ItemPool.getPool(ItemPoolsRedRoom.POOL_BLACK_PART));

        //Clear dropped items
        List<EntityItem> items = world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(x + 0.5, y, z + 0.5, x + 0.5, y + height, z + 0.5).expand(size / 2D, 0, size / 2D));
        for(EntityItem item : items) item.setDead();
    }

    public static void spawnPedestalItem(World world, int x, int y, int z, WeightedRandomChestContentFrom1710[] pool) {
        world.setBlockState(new BlockPos(x, y, z), ModBlocks.pedestal.getDefaultState());
        TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
        if (te instanceof BlockPedestal.TileEntityPedestal pedestal) {
            pedestal.item = ItemPool.getStack(pool, world.rand).copy();
            pedestal.markDirty();
            IBlockState state = world.getBlockState(new BlockPos(x, y, z));
            world.notifyBlockUpdate(new BlockPos(x, y, z), state, state, 3);
        }
    }
}
