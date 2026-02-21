package com.hbm.blocks.bomb;

import com.hbm.blocks.BlockEnumMeta;
import com.hbm.config.BombConfig;
import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.entity.logic.EntityBalefire;
import com.hbm.entity.logic.EntityNukeExplosionMK5;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.BlockAllocatorStandard;
import com.hbm.explosion.vanillant.standard.BlockProcessorStandard;
import com.hbm.explosion.vanillant.standard.EntityProcessorCross;
import com.hbm.explosion.vanillant.standard.PlayerProcessorStandard;
import com.hbm.interfaces.IBomb;
import com.hbm.items.ModItems;
import com.hbm.particle.helper.ExplosionCreator;
import com.hbm.tileentity.bomb.TileEntityCrashedBomb;
import com.hbm.util.EnumUtil;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class BlockCrashedBomb extends BlockEnumMeta<BlockCrashedBomb.EnumDudType> implements IBomb, ITileEntityProvider {

    public BlockCrashedBomb(Material mat, SoundType type, String registryName) {
        super(mat, type, registryName, EnumDudType.VALUES, true, true);
    }

    @Override
    public @Nullable TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityCrashedBomb();
    }

    @Override
    protected boolean useSpecialRenderer() {
        return true;
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX
            , float hitY, float hitZ) {
        if (world.isRemote) return true;
        Item tool = player.getHeldItem(hand).getItem();
        if (tool == ModItems.defuser || tool == ModItems.defuser_desh) {
            if (tool.getMaxDamage(player.getHeldItem(hand)) > 0) player.getHeldItem(hand).damageItem(1, player);


            EnumDudType type = EnumUtil.grabEnumSafely(EnumDudType.VALUES, getMetaFromState(world.getBlockState(pos)));

            //TODO: make this less scummy
            switch (type) {
                case BALEFIRE -> world.spawnEntity(
                        new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(ModItems.egg_balefire_shard)));
                case CONVENTIONAL -> world.spawnEntity(
                        new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(ModItems.ball_tnt, 16)));
                case NUKE -> {
                    world.spawnEntity(
                            new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(ModItems.ball_tnt, 8)));
                    world.spawnEntity(
                            new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(ModItems.billet_plutonium, 4)));
                }
                case SALTED -> {
                    world.spawnEntity(
                            new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(ModItems.ball_tnt, 8)));
                    world.spawnEntity(
                            new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, new ItemStack(ModItems.billet_plutonium, 2)));
                    world.spawnEntity(new EntityItem(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5 + 0.5,
                            new ItemStack(ModItems.ingot_cobalt, 12)));
                }
            }
            world.destroyBlock(pos, false);
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
    public BombReturnCode explode(World world, BlockPos pos, Entity detonator) {
        if (!world.isRemote) {
            EnumDudType type = EnumUtil.grabEnumSafely(EnumDudType.VALUES, getMetaFromState(world.getBlockState(pos)));
            world.setBlockToAir(pos);

            switch (type) {
                case BALEFIRE -> {
                    EntityBalefire bf = new EntityBalefire(world);
                    bf.setPosition(pos.getX(), pos.getY(), pos.getZ());
                    bf.destructionRange = (int) (BombConfig.fatmanRadius * 1.25);
                    world.spawnEntity(bf);
                    if (BombConfig.enableNukeClouds) {
                        EntityNukeTorex.statFacBale(world, pos.getX() + 0.5, pos.getY() + 5, pos.getZ() + 0.5,
                                (int) (BombConfig.fatmanRadius * 1.25));
                    }
                }
                case CONVENTIONAL -> {
                    ExplosionVNT xnt = new ExplosionVNT(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 35F);
                    xnt.setBlockAllocator(new BlockAllocatorStandard(24));
                    xnt.setBlockProcessor(new BlockProcessorStandard().setNoDrop());
                    xnt.setEntityProcessor(new EntityProcessorCross(5D).withRangeMod(1.5F));
                    xnt.setPlayerProcessor(new PlayerProcessorStandard());
                    xnt.explode();
                    ExplosionCreator.composeEffectLarge(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                }
                case NUKE -> {
                    world.spawnEntity(EntityNukeExplosionMK5.statFac(world, 35, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5));
                    if (BombConfig.enableNukeClouds) {
                        EntityNukeTorex.statFac(world, pos.getX() + 0.5, pos.getY() + 5, pos.getZ() + 0.5, 35);
                    }
                }
                case SALTED -> {
                    world.spawnEntity(
                            EntityNukeExplosionMK5.statFac(world, 25, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5).moreFallout(25));
                    if (BombConfig.enableNukeClouds) {
                        EntityNukeTorex.statFac(world, pos.getX() + 0.5, pos.getY() + 5, pos.getZ() + 0.5, 25);
                    }
                }
            }
        }

        return BombReturnCode.DETONATED;
    }

    public enum EnumDudType {
        BALEFIRE,
        CONVENTIONAL,
        NUKE,
        SALTED;

        public static final EnumDudType[] VALUES = values();
    }
}
