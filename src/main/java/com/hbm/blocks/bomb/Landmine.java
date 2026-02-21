package com.hbm.blocks.bomb;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.BombConfig;
import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.entity.logic.EntityNukeExplosionMK5;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.*;
import com.hbm.interfaces.IBomb;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.bomb.TileEntityLandmine;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockFence;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class Landmine extends BlockContainer implements IBomb {

    public static final float f = 0.0625F;
    public static final AxisAlignedBB AP_BOX = new AxisAlignedBB(5 * f, 0.0F, 5 * f, 11 * f, 1 * f, 11 * f);
    public static final AxisAlignedBB HE_BOX = new AxisAlignedBB(4 * f, 0.0F, 4 * f, 12 * f, 2 * f, 12 * f);
    public static final AxisAlignedBB SHRAP_BOX = new AxisAlignedBB(5 * f, 0.0F, 5 * f, 11 * f, 1 * f, 11 * f);
    public static final AxisAlignedBB FAT_BOX = new AxisAlignedBB(5 * f, 0.0F, 4 * f, 11 * f, 6 * f, 12 * f);
    private static final Random rand = new Random();
    public static boolean safeMode = false;
    public double range;
    public double height;

    public Landmine(Material materialIn, String s, double range, double height) {
        super(materialIn);
        this.setTranslationKey(s);
        this.setRegistryName(s);
        this.range = range;
        this.height = height;
        ModBlocks.ALL_BLOCKS.add(this);
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityLandmine();
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Items.AIR;
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        String name = this.getRegistryName().getPath();
        return switch (name) {
            case "mine_ap" -> AP_BOX;
            case "mine_he" -> HE_BOX;
            case "mine_shrap" -> SHRAP_BOX;
            case "mine_fat" -> FAT_BOX;
            default -> FULL_BLOCK_AABB;
        };
    }

    @Override
    public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
        boolean solidBelow = worldIn.getBlockState(pos.down()).isSideSolid(worldIn, pos.down(), EnumFacing.UP);
        boolean fenceBelow = worldIn.getBlockState(pos.down()).getBlock() instanceof BlockFence;
        return solidBelow || fenceBelow;
    }

    @Override
    public void neighborChanged(IBlockState state, World world, BlockPos pos, Block blockIn, BlockPos fromPos) {
        if (world.isBlockPowered(pos)) {
            explode(world, pos, null);
        }

        boolean unsupported =
                !world.getBlockState(pos.down()).isSideSolid(world, pos.down(), EnumFacing.UP) && !(world.getBlockState(pos.down()).getBlock() instanceof BlockFence);

        if (unsupported) {
            this.dropBlockAsItem(world, pos, world.getBlockState(pos), 0);
            world.setBlockToAir(pos);
        }
    }

    @Override
    public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
        if (!safeMode) {
            explode(worldIn, pos, null);
        }
        super.breakBlock(worldIn, pos, state);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX
            , float hitY, float hitZ) {
        Item heldMain = player.getHeldItemMainhand().getItem();
        Item heldOff = player.getHeldItemOffhand().getItem();
        boolean hasDefuser =
                heldMain == ModItems.defuser || heldOff == ModItems.defuser || heldMain == ModItems.defuser_desh || heldOff == ModItems.defuser_desh;

        if (hasDefuser) {
            safeMode = true;
            world.setBlockToAir(pos);

            ItemStack stack = new ItemStack(this, 1);
            float fx = world.rand.nextFloat() * 0.6F + 0.2F;
            float fy = world.rand.nextFloat() * 0.2F;
            float fz = world.rand.nextFloat() * 0.6F + 0.2F;

            EntityItem drop = new EntityItem(world, pos.getX() + fx, pos.getY() + fy + 1, pos.getZ() + fz, stack);

            float velocity = 0.05F;
            drop.motionX = world.rand.nextGaussian() * velocity;
            drop.motionY = world.rand.nextGaussian() * velocity + 0.2F;
            drop.motionZ = world.rand.nextGaussian() * velocity;

            if (!world.isRemote)
                world.spawnEntity(drop);
            safeMode = false;
            return true;
        }
        return false;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public boolean isOpaqueCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isBlockNormalCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isNormalCube(IBlockState state) {
        return false;
    }

    @Override
    public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {
        return false;
    }

    @Override
    public boolean isFullCube(IBlockState state) {
        return false;
    }

    @Override
    public void addInformation(ItemStack stack, World player, List<String> tooltip, ITooltipFlag advanced) {
        if (this == ModBlocks.mine_fat) {
            tooltip.add("§2[Nuclear Mine]§r");
            tooltip.add(" §eRadius: " + BombConfig.fatmanRadius + "m§r");
            tooltip.add("§2[Fallout]§r");
            tooltip.add(" §aRadius: " + (int) BombConfig.fatmanRadius * (1 + BombConfig.falloutRange / 100) + "m§r");
        }
    }

    public boolean isWaterAbove(World world, int x, int y, int z) {
        for(int xo = -1; xo <= 1; xo++) {
            for(int zo = -1; zo <= 1; zo++) {
                Block blockAbove = world.getBlockState(new BlockPos(x + xo, y + 1, z + zo)).getBlock();
                if(blockAbove == Blocks.WATER || blockAbove == Blocks.FLOWING_WATER) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public BombReturnCode explode(World world, BlockPos pos, Entity detonator) {
        if (world.isRemote) return BombReturnCode.DETONATED;
        int x = pos.getX(), y = pos.getY(), z = pos.getZ();
        safeMode = true;
        world.destroyBlock(pos, false);
        safeMode = false;
        if (this.getRegistryName() == null) return BombReturnCode.UNDEFINED;
        String name = this.getRegistryName().getPath();
        switch (name) {
            case "mine_ap" -> {
                ExplosionVNT vnt = new ExplosionVNT(world, x + .5, y + .5, z + .5, 3F, detonator);
                vnt.setEntityProcessor(new EntityProcessorCrossSmooth(0.5, 10F).setupPiercing(5F, 0.2F));
                vnt.setPlayerProcessor(new PlayerProcessorStandard());
                vnt.setSFX(new ExplosionEffectWeapon(5, 1F, .5F));
                vnt.explode();
            }
            case "mine_he" -> {
                ExplosionVNT vnt = new ExplosionVNT(world, x + .5, y + .5, z + .5, 4F, detonator);
                vnt.setBlockAllocator(new BlockAllocatorStandard());
                vnt.setBlockProcessor(new BlockProcessorStandard());
                vnt.setEntityProcessor(new EntityProcessorCrossSmooth(1, 35).setupPiercing(15F, 0.2F));
                vnt.setPlayerProcessor(new PlayerProcessorStandard());
                vnt.setSFX(new ExplosionEffectWeapon(15, 3.5F, 1.25F));
                vnt.explode();
            }
            case "mine_shrap" -> {
                ExplosionVNT vnt = new ExplosionVNT(world, x + 0.5, y + 0.5, z + 0.5, 3F, detonator);
                vnt.setEntityProcessor(new EntityProcessorCrossSmooth(0.5, 7.5F));
                vnt.setPlayerProcessor(new PlayerProcessorStandard());
                vnt.setSFX(new ExplosionEffectWeapon(5, 1F, 0.5F));
                vnt.explode();
                ExplosionLarge.spawnShrapnelShower(world, x + 0.5, y + 0.5, z + 0.5, 0, 1D, 0, 45, .2D);
                ExplosionLarge.spawnShrapnels(world, x + 0.5, y + 0.5, z + 0.5, 5);
            }
            case "mine_fat" -> {
                world.spawnEntity(EntityNukeExplosionMK5.statFac(world, BombConfig.fatmanRadius, x + 0.5, y + 0.5, z + 0.5).setDetonator(detonator));
                if (rand.nextInt(100) == 0 || MainRegistry.polaroidID == 11) {
                    EntityNukeTorex.statFacBale(world, x + 0.5, y + 0.5, z + 0.5, BombConfig.fatmanRadius);
                } else {
                    EntityNukeTorex.statFac(world, x + 0.5, y + 0.5, z + 0.5, BombConfig.fatmanRadius);
                }
            }
            case "mine_naval" -> {
                ExplosionVNT vnt = new ExplosionVNT(world, x + 5, y + 5, z + 5, 25F);
                vnt.setBlockAllocator(new BlockAllocatorWater(32));
                vnt.setBlockProcessor(new BlockProcessorStandard());
                vnt.setEntityProcessor(new EntityProcessorCrossSmooth(0.5, 100F /*ServerConfig.MINE_NAVAL_DAMAGE.get()*/ ).setupPiercing(5F, 0.2F)); // TODO
                vnt.setPlayerProcessor(new PlayerProcessorStandard());
                vnt.setSFX(new ExplosionEffectWeapon(10, 1F, 0.5F));
                vnt.explode();

                ExplosionLarge.spawnParticlesRadial(world, x + 0.5, y + 2, z + 0.5, 30);
                ExplosionLarge.spawnRubble(world,x + 0.5, y + 0.5, z + 0.5, 5 );

                // Only spawn water effects if there's water above the mine
                if (isWaterAbove(world, x, y, z)) {
                    ExplosionLarge.spawnFoam(world, x + 0.5, y + 0.5, z + 0.5, 60);
                }
            }
        }
        return BombReturnCode.DETONATED;
    }
}
