package com.hbm.blocks.network;

import com.hbm.api.conveyor.IConveyorBelt;
import com.hbm.api.conveyor.IConveyorItem;
import com.hbm.api.conveyor.IConveyorPackage;
import com.hbm.api.conveyor.IEnterableBlock;
import com.hbm.blocks.ModBlocks;
import com.hbm.entity.item.EntityMovingItem;
import com.hbm.entity.item.EntityMovingPackage;
import com.hbm.items.ModItems;
import com.hbm.items.tool.ItemTooling;
import com.hbm.main.MainRegistry;
import com.hbm.modules.ModulePatternMatcher;
import com.hbm.tileentity.network.TileEntityCraneRouter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class CraneRouter extends BlockContainer implements IEnterableBlock {
    public CraneRouter(Material materialIn, String s) {
        super(materialIn);
        this.setTranslationKey(s);
        this.setRegistryName(s);
        ModBlocks.ALL_BLOCKS.add(this);
    }
    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityCraneRouter();
    }

    @Override
    public boolean canItemEnter(World world, int x, int y, int z, EnumFacing dir, IConveyorItem entity) {
        return true;
    }

    @Override
    public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (playerIn.getHeldItem(hand).getItem() instanceof ItemTooling ||
            playerIn.getHeldItem(hand).getItem() == ModItems.conveyor_wand) {
            return false;
        } else if(worldIn.isRemote) {
            return true;
        } else if(!playerIn.isSneaking()) {
            playerIn.openGui(MainRegistry.instance, 0, worldIn, pos.getX(), pos.getY(), pos.getZ());
            return true;
        } else {
            return false;
        }
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    private static EnumFacing[] customEnumOrder = new EnumFacing[]{
        EnumFacing.NORTH,
        EnumFacing.UP,
        EnumFacing.EAST,
        EnumFacing.SOUTH,
        EnumFacing.DOWN,
        EnumFacing.WEST
    };

    @Override
    public void onItemEnter(World world, int x, int y, int z, EnumFacing dir, IConveyorItem entity) {
        if (entity == null || entity.getItemStack().isEmpty()) return;

        EnumFacing route = getOutputDir(world, x, y, z, entity.getItemStack());
        if (route == null) {
            world.spawnEntity(new EntityItem(world, x + 0.5, y + 0.5, z + 0.5, entity.getItemStack().copy()));
            return;
        }

        sendOnRoute(world, x, y, z, entity.getItemStack().copy(), route);
    }

    protected void sendOnRoute(World world, int x, int y, int z, ItemStack stack, EnumFacing dir) {
        IConveyorBelt belt = null;
        BlockPos targetPos = new BlockPos(x + dir.getXOffset(), y + dir.getYOffset(), z + dir.getZOffset());
        Block block = world.getBlockState(targetPos).getBlock();

        if (block instanceof IConveyorBelt) {
            belt = (IConveyorBelt) block;
        }

        if (belt != null) {
            EntityMovingItem moving = new EntityMovingItem(world);
            Vec3d pos = new Vec3d(x + 0.5 + dir.getXOffset() * 0.55, y + 0.5 + dir.getYOffset() * 0.55, z + 0.5 + dir.getZOffset() * 0.55);
            Vec3d snap = belt.getClosestSnappingPosition(world, targetPos, pos);
            moving.setPosition(snap.x, snap.y, snap.z);
            moving.setItemStack(stack);
            world.spawnEntity(moving);
        } else {
            world.spawnEntity(new EntityItem(world, x + 0.5 + dir.getXOffset() * 0.55, y + 0.5 + dir.getYOffset() * 0.55, z + 0.5 + dir.getZOffset() * 0.55, stack));
        }
    }

    protected void sendPackageOnRoute(World world, int x, int y, int z, List<ItemStack> items, EnumFacing dir) {
        if (items.isEmpty()) return;

        IConveyorBelt belt = null;
        BlockPos targetPos = new BlockPos(x + dir.getXOffset(), y + dir.getYOffset(), z + dir.getZOffset());
        Block block = world.getBlockState(targetPos).getBlock();
        if (block instanceof IConveyorBelt) {
            belt = (IConveyorBelt) block;
        }

        if (belt != null) {
            EntityMovingPackage moving = new EntityMovingPackage(world);
            Vec3d pos = new Vec3d(x + 0.5 + dir.getXOffset() * 0.55, y + 0.5 + dir.getYOffset() * 0.55, z + 0.5 + dir.getZOffset() * 0.55);
            Vec3d snap = belt.getClosestSnappingPosition(world, targetPos, pos);
            moving.setPosition(snap.x, snap.y, snap.z);
            moving.setItemStacks(items.toArray(new ItemStack[0]));
            world.spawnEntity(moving);
        } else {
            for (ItemStack stack : items) {
                world.spawnEntity(new EntityItem(world, x + 0.5 + dir.getXOffset() * 0.55, y + 0.5 + dir.getYOffset() * 0.55, z + 0.5 + dir.getZOffset() * 0.55, stack));
            }
        }
    }

    @Override public boolean canPackageEnter(World world, int x, int y, int z, EnumFacing dir, IConveyorPackage entity) { return true; }

    @Override
    public void onPackageEnter(World world, int x, int y, int z, EnumFacing dir, IConveyorPackage entity) {
        if (entity == null || entity.getItemStacks() == null || entity.getItemStacks().length == 0) return;

        List<ItemStack>[] sorted = sort(world, x, y, z, entity.getItemStacks());

        for (int i = 0; i < sorted.length; i++) {
            List<ItemStack> items = sorted[i];
            if (items.isEmpty()) continue;

            if (i == 6) {
                for (ItemStack stack : items) {
                    world.spawnEntity(new EntityItem(world, x + 0.5, y + 0.5, z + 0.5, stack));
                }
            } else {
                sendPackageOnRoute(world, x, y, z, items, customEnumOrder[i]);
            }
        }
    }

    private static EnumFacing getOutputDir(World world, int x, int y, int z, ItemStack stack) {
        TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
        if (!(te instanceof TileEntityCraneRouter router)) return null;
        return getOutputDir(router, stack, world);
    }

    private static EnumFacing getOutputDir(TileEntityCraneRouter router, ItemStack stack, World world) {
        List<EnumFacing> validDirs = new ArrayList<>();

        //check filters for all sides
        for(int i = 0; i<6; i++) {

            ModulePatternMatcher matcher = router.patterns[i];
            int mode = router.modes[i];

            //if the side is disabled or wildcard, skip
            if(mode == router.MODE_NONE || mode == router.MODE_WILDCARD)
                continue;

            boolean matchesFilter = false;

            for(int slot = 0; slot < 5; slot++) {
                ItemStack filter = router.inventory.getStackInSlot(i * 5 + slot);

                if(filter.isEmpty())
                    continue;

                //the filter kicks in so long as one entry matches
                if(matcher.isValidForFilter(filter, slot, stack)) {
                    matchesFilter = true;
                    break;
                }
            }

            //add dir if matches with whitelist on or doesn't match with blacklist on
            if((mode == router.MODE_WHITELIST && matchesFilter) || (mode == router.MODE_BLACKLIST && !matchesFilter)) {
                validDirs.add(customEnumOrder[i]);
            }
        }

        //if no valid dirs have yet been found, use wildcard
        if(validDirs.isEmpty()) {
            for(int i = 0; i<6; i++) {
                if(router.modes[i] == router.MODE_WILDCARD) {
                    validDirs.add(customEnumOrder[i]);
                }
            }
        }

        if(validDirs.isEmpty()) {
            return null;
        }

        int i = world.rand.nextInt(validDirs.size());
        return validDirs.get(i);
    }

    private static int getOutputIndex(EnumFacing side) {
        for (int i = 0; i < 6; i++) {
            if (customEnumOrder[i] == side) return i;
        }
        return 6;
    }

    @SuppressWarnings("unchecked")
    public static List<ItemStack>[] sort(World world, int x, int y, int z, ItemStack... stacks) {
        List<ItemStack>[] output = new List[7];
        for (int i = 0; i < 7; i++) output[i] = new ArrayList<>();

        for (ItemStack stack : stacks) {
            if (stack == null || stack.isEmpty()) continue;

            EnumFacing route = getOutputDir(world, x, y, z, stack.copy());
            output[getOutputIndex(route)].add(stack.copy());
        }

        return output;
    }

}
