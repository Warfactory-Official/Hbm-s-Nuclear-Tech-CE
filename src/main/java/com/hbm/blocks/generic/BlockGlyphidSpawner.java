package com.hbm.blocks.generic;

import com.hbm.blocks.IBlockMulti;
import com.hbm.blocks.ModBlocks;
import com.hbm.config.MobConfig;
import com.hbm.entity.mob.glyphid.*;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.interfaces.AutoRegister;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.util.Tuple.Pair;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.function.Function;

public class BlockGlyphidSpawner extends BlockContainer implements IBlockMulti {
    public static final PropertyEnum<Type> TYPE = PropertyEnum.create("type", Type.class);

    public BlockGlyphidSpawner(Material m, String s) {
        super(m);
        this.setRegistryName(s);
        this.setTranslationKey(s);
        this.setCreativeTab(MainRegistry.blockTab);

        ModBlocks.ALL_BLOCKS.add(this);
    }

    @Override
    public EnumBlockRenderType getRenderType(IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public int getSubCount() {
        return 3;
    }

    @Override
    public Item getItemDropped(IBlockState state, Random rand, int fortune) {
        return ModItems.egg_glyphid;
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(TYPE).ordinal();
    }

    @Override
    public @NotNull IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(TYPE, Type.VALUES[meta]);
    }

    @Override
    protected @NotNull BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, TYPE);
    }

    public enum Type implements IStringSerializable {
        BASE, INFESTED, RAD;

        public static final Type[] VALUES = values();

        @Override
        public @NotNull String getName() {
            return name().toLowerCase(Locale.US);
        }
    }

    private static final List<Pair<Function<World, EntityGlyphid>, int[]>> spawnMap = new ArrayList<>();

    static {
        // big thanks to martin for the suggestion of using functions
        spawnMap.add(new Pair<>(EntityGlyphid::new,				MobConfig.glyphidChance));
        spawnMap.add(new Pair<>(EntityGlyphidBombardier::new,	MobConfig.bombardierChance));
        spawnMap.add(new Pair<>(EntityGlyphidBrawler::new,		MobConfig.brawlerChance));
        spawnMap.add(new Pair<>(EntityGlyphidDigger::new,		MobConfig.diggerChance));
        spawnMap.add(new Pair<>(EntityGlyphidBlaster::new,		MobConfig.blasterChance));
        spawnMap.add(new Pair<>(EntityGlyphidBehemoth::new,		MobConfig.behemothChance));
        spawnMap.add(new Pair<>(EntityGlyphidBrenda::new,		MobConfig.brendaChance));
        spawnMap.add(new Pair<>(EntityGlyphidNuclear::new,		MobConfig.johnsonChance));
    }

    @Override
    public int quantityDropped(IBlockState state, int fortune, Random random) {
        return 1 + random.nextInt(3) + fortune;
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityGlyphidSpawner();
    }

    @AutoRegister
    public static class TileEntityGlyphidSpawner extends TileEntity implements ITickable {

        boolean initialSpawn = true;

        @Override
        public void update() {

            if(!world.isRemote && this.world.getDifficulty() != EnumDifficulty.PEACEFUL) {

                if(initialSpawn || world.getTotalWorldTime() % MobConfig.swarmCooldown == 0) {

                    initialSpawn = false;
                    int count = 0;

                    for(Object e : world.loadedEntityList) {
                        if(e instanceof EntityGlyphid) {
                            count++;
                            if(count >= MobConfig.spawnMax) return;
                        }
                    }

                    List<EntityGlyphid> list = world.getEntitiesWithinAABB(EntityGlyphid.class, new AxisAlignedBB(pos.getX() - 5, pos.getY() + 1, pos.getZ() - 5, pos.getX() + 6, pos.getY() + 7, pos.getZ() + 6));
                    float soot = PollutionHandler.getPollution(world, pos, PollutionHandler.PollutionType.SOOT);

                    int subtype = this.getBlockMetadata();
                    if(list.size() <= 3 || subtype == EntityGlyphid.TYPE_RADIOACTIVE) {

                        ArrayList<EntityGlyphid> currentSwarm = createSwarm(soot, subtype);

                        for(EntityGlyphid glyphid : currentSwarm) {
                            trySpawnEntity(glyphid);
                        }

                        if(!initialSpawn && world.rand.nextInt(MobConfig.scoutSwarmSpawnChance + 1) == 0 && soot >= MobConfig.scoutThreshold && subtype != EntityGlyphid.TYPE_RADIOACTIVE) {
                            EntityGlyphidScout scout = new EntityGlyphidScout(world);
                            if(this.getBlockMetadata() == 1) scout.getDataManager().set(EntityGlyphid.SUBTYPE, EntityGlyphid.TYPE_INFECTED);
                            trySpawnEntity(scout);
                        }
                    }
                }
            }
        }

        public void trySpawnEntity(EntityGlyphid glyphid) {
            double offsetX = glyphid.getRNG().nextGaussian() * 3;
            double offsetZ = glyphid.getRNG().nextGaussian() * 3;

            for(int i = 0; i < 7; i++) {
                glyphid.setLocationAndAngles(pos.getX() + 0.5 + offsetX, pos.getY() - 2 + i, pos.getZ() + 0.5 + offsetZ, world.rand.nextFloat() * 360.0F, 0.0F);
                if(glyphid.getCanSpawnHere()) {
                    world.spawnEntity(glyphid);
                    return;
                }
            }
        }

        public ArrayList<EntityGlyphid> createSwarm(float soot, int meta) {

            Random rand = new Random();
            ArrayList<EntityGlyphid> currentSpawns = new ArrayList<>();
            int swarmAmount = (int) Math.min(MobConfig.baseSwarmSize * Math.max(MobConfig.swarmScalingMult * (soot / MobConfig.sootStep), 1), 10);
            int cap = 100;

            while(currentSpawns.size() <= swarmAmount && cap >= 0) {
                // (dys)functional programing
                for(Pair<Function<World, EntityGlyphid>, int[]> glyphid : spawnMap) {
                    int[] chance = glyphid.getValue();
                    int adjustedChance = (int) (chance[0] + (chance[1] - chance[1] / Math.max(((soot + 1) / 3), 1)));
                    if(soot >= chance[2] && rand.nextInt(100) <= adjustedChance) {
                        EntityGlyphid entity = glyphid.getKey().apply(world);
                        if(meta == 1) entity.getDataManager().set(EntityGlyphid.SUBTYPE, EntityGlyphid.TYPE_INFECTED);
                        if(meta == 2) entity.getDataManager().set(EntityGlyphid.SUBTYPE, EntityGlyphid.TYPE_RADIOACTIVE);
                        currentSpawns.add(entity);
                    }
                }

                cap--;
            }
            return currentSpawns;
        }

        @Override
        public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
            super.writeToNBT(nbt);
            nbt.setBoolean("initialSpawn", initialSpawn);
            return nbt;
        }

        @Override
        public void readFromNBT(NBTTagCompound nbt) {
            super.readFromNBT(nbt);
            this.initialSpawn = nbt.getBoolean("initialSpawn");
        }
    }
}
