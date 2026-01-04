package com.hbm.blocks.generic;

import com.hbm.blocks.ModBlocks;
import com.hbm.handler.WeightedRandomChestContentFrom1710;
import com.hbm.handler.WeightedRandomFrom1710;
import com.hbm.itempool.ItemPool;
import com.hbm.itempool.ItemPoolsRedRoom;
import com.hbm.items.ModItems;
import com.hbm.items.tool.ItemModDoor;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.main.AdvancementManager;
import net.minecraft.advancements.PlayerAdvancements;
import net.minecraft.block.Block;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
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

public class BlockKeyhole extends Block {
    public BlockKeyhole(String s) {
        super(Material.ROCK);
        this.setRegistryName(s);
        this.setTranslationKey(s);

        ModBlocks.ALL_BLOCKS.add(this);
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return new ItemStack(Blocks.STONE);
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Item.getItemFromBlock(Blocks.COBBLESTONE);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if(!player.getHeldItem(hand).isEmpty()) {
            boolean cracked = player.getHeldItem(hand).getItem() == ModItems.key_red_cracked;
            if((player.getHeldItem(hand).getItem() == ModItems.key_red || cracked) && facing != EnumFacing.UP && facing != EnumFacing.DOWN) {
                if(cracked) player.getHeldItem(hand).shrink(1);
                if(world.isRemote) return true;
                ForgeDirection dir = ForgeDirection.getOrientation(facing);
                generateRoom(world, pos.getX() - dir.offsetX * 4, pos.getY() - 2, pos.getZ() - dir.offsetZ * 4);
                ItemModDoor.placeDoor(world, pos.down(), facing.getOpposite(), ModBlocks.door_red, false);
                world.playSound(player, pos, HBMSoundHandler.lockOpen, SoundCategory.BLOCKS, 1.0F, 1.0F);
                PlayerAdvancements advancements = ((EntityPlayerMP) player).getAdvancements();
                advancements.grantCriterion(AdvancementManager.achRedRoom, "impossible");
                return true;
            }
        }

        return false;
    }

    protected static void generateRoom(World world, int x, int y, int z) {

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
                world.setBlockState(new BlockPos(x + width, y + i, z + j), ModBlocks.brick_red.getDefaultState().withProperty(BlockRedBrick.META, 4));
                world.setBlockState(new BlockPos(x - width, y + i, z + j), ModBlocks.brick_red.getDefaultState().withProperty(BlockRedBrick.META, 5));
                world.setBlockState(new BlockPos(x + j, y + i, z + width), ModBlocks.brick_red.getDefaultState().withProperty(BlockRedBrick.META, 2));
                world.setBlockState(new BlockPos(x + j, y + i, z - width), ModBlocks.brick_red.getDefaultState().withProperty(BlockRedBrick.META, 3));
            }
        }

        int r = world.rand.nextInt(4);
        if(r == 0) world.setBlockState(new BlockPos(x + width, y + 2, z), ModBlocks.stone_keyhole_meta.getDefaultState().withProperty(BlockRedBrickKeyhole.META, 4));
        if(r == 1) world.setBlockState(new BlockPos(x - width, y + 2, z), ModBlocks.stone_keyhole_meta.getDefaultState().withProperty(BlockRedBrickKeyhole.META, 5));
        if(r == 2) world.setBlockState(new BlockPos(x, y + 2, z + width), ModBlocks.stone_keyhole_meta.getDefaultState().withProperty(BlockRedBrickKeyhole.META, 2));
        if(r == 3) world.setBlockState(new BlockPos(x, y + 2, z - width), ModBlocks.stone_keyhole_meta.getDefaultState().withProperty(BlockRedBrickKeyhole.META, 3));

        for(int i = -width + 1; i <= width - 1; i++) {
            for(int j = -width + 1; j <= width - 1; j++) {
                //Floor and ceiling
                world.setBlockState(new BlockPos(x + i, y, z + j), ModBlocks.brick_red.getDefaultState().withProperty(BlockRedBrick.META, 1));
                world.setBlockState(new BlockPos(x + i, y + height - 1, z + j), ModBlocks.brick_red.getDefaultState().withProperty(BlockRedBrick.META, 0));

                for(int k = 1; k <= height - 2; k++) {
                    world.setBlockToAir(new BlockPos(x + i, y + k, z + j));
                }
            }
        }

        //Torches
        int torchDist = width - 1;
        int torchOff = torchDist - 1;
        world.setBlockState(new BlockPos(x + torchDist, y + 2, z + torchOff), Blocks.TORCH.getDefaultState().withProperty(BlockTorch.FACING, EnumFacing.WEST));
        world.setBlockState(new BlockPos(x + torchDist, y + 2, z - torchOff), Blocks.TORCH.getDefaultState().withProperty(BlockTorch.FACING, EnumFacing.WEST));
        world.setBlockState(new BlockPos(x - torchDist, y + 2, z + torchOff), Blocks.TORCH.getDefaultState().withProperty(BlockTorch.FACING, EnumFacing.EAST));
        world.setBlockState(new BlockPos(x - torchDist, y + 2, z - torchOff), Blocks.TORCH.getDefaultState().withProperty(BlockTorch.FACING, EnumFacing.EAST));
        world.setBlockState(new BlockPos(x + torchOff, y + 2, z + torchDist), Blocks.TORCH.getDefaultState().withProperty(BlockTorch.FACING, EnumFacing.NORTH));
        world.setBlockState(new BlockPos(x - torchOff, y + 2, z + torchDist), Blocks.TORCH.getDefaultState().withProperty(BlockTorch.FACING, EnumFacing.NORTH));
        world.setBlockState(new BlockPos(x + torchOff, y + 2, z - torchDist), Blocks.TORCH.getDefaultState().withProperty(BlockTorch.FACING, EnumFacing.SOUTH));
        world.setBlockState(new BlockPos(x - torchOff, y + 2, z - torchDist), Blocks.TORCH.getDefaultState().withProperty(BlockTorch.FACING, EnumFacing.SOUTH));

        //Cobwebs
        if(world.rand.nextInt(4) == 0) {
            for(int i = -width + 1; i <= width - 1; i++) {
                for(int j = -width + 1; j <= width - 1; j++) {
                    if(world.rand.nextBoolean()) world.setBlockState(new BlockPos(x + i, y + height - 2, z + j), Blocks.WEB.getDefaultState());
                }
            }
        }

        //Pillars
        if(world.rand.nextInt(4) == 0) {
            for(int i = 1; i <= height - 2; i++) {
                world.setBlockState(new BlockPos(x + width - 2, y + i, z + width - 2), ModBlocks.concrete_colored.getDefaultState().withProperty(BlockMeta.META, 14));
                world.setBlockState(new BlockPos(x + width - 2, y + i, z - width + 2), ModBlocks.concrete_colored.getDefaultState().withProperty(BlockMeta.META, 14));
                world.setBlockState(new BlockPos(x - width + 2, y + i, z + width - 2), ModBlocks.concrete_colored.getDefaultState().withProperty(BlockMeta.META, 14));
                world.setBlockState(new BlockPos(x - width + 2, y + i, z - width + 2), ModBlocks.concrete_colored.getDefaultState().withProperty(BlockMeta.META, 14));
            }
        }

        //Fire
        if(world.rand.nextInt(4) == 0) {
            world.setBlockState(new BlockPos(x + width - 1, y, z + width - 1), Blocks.NETHERRACK.getDefaultState());
            world.setBlockState(new BlockPos(x + width - 1, y, z - width + 1), Blocks.NETHERRACK.getDefaultState());
            world.setBlockState(new BlockPos(x - width + 1, y, z + width - 1), Blocks.NETHERRACK.getDefaultState());
            world.setBlockState(new BlockPos(x - width + 1, y, z - width + 1), Blocks.NETHERRACK.getDefaultState());
            world.setBlockState(new BlockPos(x + width - 1, y + 1, z + width - 1), Blocks.FIRE.getDefaultState());
            world.setBlockState(new BlockPos(x + width - 1, y + 1, z - width + 1), Blocks.FIRE.getDefaultState());
            world.setBlockState(new BlockPos(x - width + 1, y + 1, z + width - 1), Blocks.FIRE.getDefaultState());
            world.setBlockState(new BlockPos(x - width + 1, y + 1, z - width + 1), Blocks.FIRE.getDefaultState());
        }

        //Circle
        if(world.rand.nextInt(4) == 0) {
            for(int i = -1; i <= 1; i++) {
                for(int j = -1; j <= 1; j++) {
                    if(i != 0 || j != 0) world.setBlockState(new BlockPos(x + i, y, z + j), ModBlocks.concrete_colored.getDefaultState().withProperty(BlockMeta.META, 14), 3);
                }
            }
        }

        //Lava
        if(world.rand.nextInt(4) == 0) {
            world.setBlockState(new BlockPos(x + width - 2, y, z + width - 1), Blocks.LAVA.getDefaultState());
            world.setBlockState(new BlockPos(x + width - 3, y, z + width - 1), Blocks.LAVA.getDefaultState());
            world.setBlockState(new BlockPos(x - width + 2, y, z + width - 1), Blocks.LAVA.getDefaultState());
            world.setBlockState(new BlockPos(x - width + 3, y, z + width - 1), Blocks.LAVA.getDefaultState());
            world.setBlockState(new BlockPos(x + width - 2, y, z - width + 1), Blocks.LAVA.getDefaultState());
            world.setBlockState(new BlockPos(x + width - 3, y, z - width + 1), Blocks.LAVA.getDefaultState());
            world.setBlockState(new BlockPos(x - width + 2, y, z - width + 1), Blocks.LAVA.getDefaultState());
            world.setBlockState(new BlockPos(x - width + 3, y, z - width + 1), Blocks.LAVA.getDefaultState());
            world.setBlockState(new BlockPos(x + width - 1, y, z + width - 2), Blocks.LAVA.getDefaultState());
            world.setBlockState(new BlockPos(x + width - 1, y, z + width - 3), Blocks.LAVA.getDefaultState());
            world.setBlockState(new BlockPos(x + width - 1, y, z - width + 2), Blocks.LAVA.getDefaultState());
            world.setBlockState(new BlockPos(x + width - 1, y, z - width + 3), Blocks.LAVA.getDefaultState());
            world.setBlockState(new BlockPos(x - width + 1, y, z + width - 2), Blocks.LAVA.getDefaultState());
            world.setBlockState(new BlockPos(x - width + 1, y, z + width - 3), Blocks.LAVA.getDefaultState());
            world.setBlockState(new BlockPos(x - width + 1, y, z - width + 2), Blocks.LAVA.getDefaultState());
            world.setBlockState(new BlockPos(x - width + 1, y, z - width + 3), Blocks.LAVA.getDefaultState());
        }

        int rand = world.rand.nextInt(20);

        if(rand == 0) {
            world.setBlockState(new BlockPos(x, y + 1, z), ModBlocks.deco_loot.getDefaultState());
            BlockLoot.TileEntityLoot loot = (BlockLoot.TileEntityLoot) world.getTileEntity(new BlockPos(x, y + 1, z));
            loot.addItem(new ItemStack(ModItems.trenchmaster_helmet), 0, 0, 0);
            loot.addItem(new ItemStack(ModItems.trenchmaster_plate), 0, 0, 0);
            loot.addItem(new ItemStack(ModItems.trenchmaster_legs), 0, 0, 0);
            loot.addItem(new ItemStack(ModItems.trenchmaster_boots), 0, 0, 0);
        } else {
            spawnPedestalItem(world, x, y + 1, z);
        }

        //Clear dropped items
        List<EntityItem> items = world.getEntitiesWithinAABB(EntityItem.class, new AxisAlignedBB(x + 0.5, y, z + 0.5, x + 0.5, y + height, z + 0.5).expand(size / 2D, 0, size / 2D));
        for(EntityItem item : items) item.setDead();
    }

    public static void spawnPedestalItem(World world, int x, int y, int z) {
        world.setBlockState(new BlockPos(x, y, z), ModBlocks.pedestal.getDefaultState());

        TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
        if (te instanceof BlockPedestal.TileEntityPedestal pedestal) {
            WeightedRandomChestContentFrom1710 content = (WeightedRandomChestContentFrom1710) WeightedRandomFrom1710.getRandomItem(world.rand, ItemPool.getPool(ItemPoolsRedRoom.POOL_RED_PEDESTAL));
            pedestal.item = content.theItemId.copy();
            pedestal.markDirty();
            IBlockState state = world.getBlockState(new BlockPos(x, y, z));
            world.notifyBlockUpdate(new BlockPos(x, y, z), state, state, 3);
        }
    }
}
