package com.hbm.blocks.generic;

import com.hbm.blocks.ModBlocks;
import com.hbm.entity.mob.EntityUndeadSoldier;
import com.hbm.interfaces.AutoRegister;
import com.hbm.items.ItemEnums;
import com.hbm.items.ModItems;
import com.hbm.util.EnumUtil;
import com.hbm.util.Vec3NT;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;

import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class DungeonSpawner extends BlockContainer {

    public DungeonSpawner() {
        super(Material.ROCK);
        this.setTranslationKey("dungeon_spawner");
        this.setRegistryName("dungeon_spawner");
        this.setSoundType(SoundType.STONE);
        ModBlocks.ALL_BLOCKS.add(this);
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta) {
        return new TileEntityDungeonSpawner();
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return Items.AIR;
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @AutoRegister(name = "tileentity_dungeon_spawner")
    public static class TileEntityDungeonSpawner extends TileEntity implements ITickable {

        private int phase = 0;
        private int timer = 0;
        private EnumSpawnerType type = EnumSpawnerType.ABERRATOR;

        @Override
        public void update() {
            if (world == null || world.isRemote) return;

            type.phase.accept(this);
            if (type.phaseCondition.test(this)) {
                phase++;
                timer = 0;
            } else {
                timer++;
            }
        }

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
            super.writeToNBT(nbt);
            nbt.setInteger("phase", phase);
            nbt.setByte("type", (byte) type.ordinal());
            return nbt;
        }

        @Override
        public void readFromNBT(NBTTagCompound nbt) {
            super.readFromNBT(nbt);
            phase = nbt.getInteger("phase");
            type = EnumUtil.grabEnumSafely(EnumSpawnerType.VALUES, nbt.getByte("type"));
        }
    }

    public enum EnumSpawnerType {

        ABERRATOR(CON_ABERRATOR, PHASE_ABERRATOR);

        public static final EnumSpawnerType[] VALUES = values();

        private final Predicate<TileEntityDungeonSpawner> phaseCondition;
        private final Consumer<TileEntityDungeonSpawner> phase;

        EnumSpawnerType(Predicate<TileEntityDungeonSpawner> phaseCondition, Consumer<TileEntityDungeonSpawner> phase) {
            this.phaseCondition = phaseCondition;
            this.phase = phase;
        }
    }

    private static final Predicate<TileEntityDungeonSpawner> CON_ABERRATOR = tile -> {
        World world = tile.getWorld();
        if (world == null || world.getDifficulty() == EnumDifficulty.PEACEFUL) return false;

        BlockPos pos = tile.getPos();
        if (tile.phase == 0) {
            if (world.getTotalWorldTime() % 20L != 0L) return false;

            AxisAlignedBB triggerBox = new AxisAlignedBB(pos.getX(), pos.getY() - 2, pos.getZ(), pos.getX() + 1, pos.getY(), pos.getZ() + 1)
                    .grow(20.0D, 10.0D, 20.0D);
            return !world.getEntitiesWithinAABB(EntityPlayer.class, triggerBox).isEmpty();
        }

        if (tile.phase < 3) {
            if (world.getTotalWorldTime() % 20L != 0L || tile.timer < 60) return false;

            AxisAlignedBB mobBox = new AxisAlignedBB(pos.getX() - 2, pos.getY(), pos.getZ(), pos.getX(), pos.getY() + 1, pos.getZ() + 1)
                    .grow(50.0D, 20.0D, 50.0D);
            return world.getEntitiesWithinAABB(EntityUndeadSoldier.class, mobBox).isEmpty();
        }

        return false;
    };

    private static final Consumer<TileEntityDungeonSpawner> PHASE_ABERRATOR = tile -> {
        World world = tile.getWorld();
        if (world == null) return;

        BlockPos pos = tile.getPos();
        if (tile.phase == 1 || tile.phase == 2) {
            if (tile.timer == 0) {
                Vec3NT vec = new Vec3NT(10, 0, 0);
                for (int i = 0; i < 10; i++) {
                    EntityUndeadSoldier mob = new EntityUndeadSoldier(world);
                    for (int j = 0; j < 7; j++) {
                        mob.setPositionAndRotation(pos.getX() + 0.5D + vec.x, pos.getY() - 5, pos.getZ() + 0.5D + vec.z, i * 36F, 0);
                        if (mob.getCanSpawnHere()) {
                            mob.onInitialSpawn(world.getDifficultyForLocation(mob.getPosition()), null);
                            world.spawnEntity(mob);
                            break;
                        }
                    }

                    vec.rotateAroundYDeg(36D);
                }
            }
        }

        if (tile.phase > 2) {
            TileEntity te = world.getTileEntity(pos.up(18));
            if (te instanceof BlockSkeletonHolder.TileEntitySkeletonHolder skeleton) {
                if (world.rand.nextInt(5) == 0) {
                    skeleton.item = new ItemStack(ModItems.item_secret, 1, ItemEnums.EnumSecretType.ABERRATOR.ordinal());
                } else {
                    skeleton.item = new ItemStack(ModItems.clay_tablet, 1, 1);
                }
                skeleton.markDirty();

                IBlockState state = world.getBlockState(pos.up(18));
                world.notifyBlockUpdate(pos.up(18), state, state, 3);
            }

            world.setBlockState(pos, Blocks.OBSIDIAN.getDefaultState(), 3);
        }
    };
}
