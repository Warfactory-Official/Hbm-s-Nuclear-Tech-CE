package com.hbm.handler.ability;

import com.hbm.config.ToolConfig;
import com.hbm.explosion.ExplosionNT;
import com.hbm.handler.ThreeInts;
import com.hbm.items.tool.ItemToolAbility;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.*;

public interface IToolAreaAbility extends IBaseAbility {
    // Should call tool.breakExtraBlock on a bunch of blocks.
    // The initial block is implicitly broken, so don't call breakExtraBlock on it.
    // Returning true skips the reference block from being broken
    boolean onDig(int level, World world, BlockPos pos, EntityPlayer player, ItemToolAbility tool);

    // Whether breakExtraBlock is called at all. Currently only false for explosion
    default boolean allowsHarvest(int level) {
        return true;
    }

    int SORT_ORDER_BASE = 0;

    // region handlers
    IToolAreaAbility NONE = new IToolAreaAbility() {
        @Override
        public String getName() {
            return "";
        }

        @Override
        public int sortOrder() {
            return SORT_ORDER_BASE + 0;
        }

        @Override
        public boolean onDig(int level, World world, BlockPos pos, EntityPlayer player, ItemToolAbility tool) {
            return false;
        }
    };

    IToolAreaAbility RECURSION = new IToolAreaAbility() {
        @Override
        public String getName() {
            return "tool.ability.recursion";
        }

        @Override
        public boolean isAllowed() {
            return ToolConfig.abilityVein;
        }

        public final int[] radiusAtLevel = { 3, 4, 5, 6, 7, 9, 10 };

        @Override
        public int levels() {
            return radiusAtLevel.length;
        }

        @Override
        public String getExtension(int level) {
            return " (" + radiusAtLevel[level] + ")";
        }

        @Override
        public int sortOrder() {
            return SORT_ORDER_BASE + 1;
        }

        // Note: if reusing it across different instatces of a tool
        // is a problem here, then it had already been one before
        // the refactor! The solution is to simply make this a local
        // of the onDig method and pass it around as a parameter.
        private final Set<ThreeInts> visited = new HashSet<>();

        @Override
        public boolean onDig(int level, World world, BlockPos pos, EntityPlayer player, ItemToolAbility tool) {
            Block b = world.getBlockState(pos).getBlock();

            if (b == Blocks.STONE && !ToolConfig.recursiveStone) {
                return false;
            }

            if (b == Blocks.NETHERRACK && !ToolConfig.recursiveNetherrack) {
                return false;
            }

            visited.clear();

            recurse(world, pos.getX(), pos.getY(), pos.getZ(), pos.getX(), pos.getY(), pos.getZ(), player, tool, 0, radiusAtLevel[level]);

            return false;
        }

        private final List<ThreeInts> offsets = new ArrayList<>(3 * 3 * 3 - 1) {
            {
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            if (dx != 0 || dy != 0 || dz != 0) {
                                add(new ThreeInts(dx, dy, dz));
                            }
                        }
                    }
                }
            }
        };

        private void recurse(World world, int x, int y, int z, int refX, int refY, int refZ, EntityPlayer player, ItemToolAbility tool, int depth, int radius) {
            List<ThreeInts> shuffledOffsets = new ArrayList<>(offsets);
            Collections.shuffle(shuffledOffsets);

            for (ThreeInts offset : shuffledOffsets) {
                breakExtra(world, x + offset.x, y + offset.y, z + offset.z, refX, refY, refZ, player, tool, depth, radius);
            }
        }

        private void breakExtra(World world, int x, int y, int z, int refX, int refY, int refZ, EntityPlayer player, ItemToolAbility tool, int depth, int radius) {
            if (visited.contains(new ThreeInts(x, y, z)))
                return;

            depth += 1;

            if (depth > ToolConfig.recursionDepth)
                return;

            visited.add(new ThreeInts(x, y, z));

            // don't lose the ref block just yet
            if (x == refX && y == refY && z == refZ)
                return;

            if (new Vec3d(x - refX, y - refY, z - refZ).length() > radius)
                return;

            BlockPos pos1 = new BlockPos(x, y, z);
            BlockPos refPos = new BlockPos(refX, refY, refZ);
            Block b = world.getBlockState(pos1).getBlock();
            Block ref = world.getBlockState(refPos).getBlock();
            int meta = world.getBlockState(pos1).getBlock().getMetaFromState(world.getBlockState(pos1));
            int refMeta = world.getBlockState(refPos).getBlock().getMetaFromState(world.getBlockState(refPos));

            if (!isSameBlock(b, ref))
                return;

            if (meta != refMeta)
                return;

            if (player.getHeldItemMainhand().isEmpty())
                return;

            tool.breakExtraBlock(world, x, y, z, player, refX, refY, refZ);

            recurse(world, x, y, z, refX, refY, refZ, player, tool, depth, radius);
        }

        private boolean isSameBlock(Block b1, Block b2) {
            if ((b1 == Blocks.REDSTONE_ORE && b2 == Blocks.LIT_REDSTONE_ORE) || (b1 == Blocks.LIT_REDSTONE_ORE && b2 == Blocks.REDSTONE_ORE))
                return true;
            return b1 == b2;
        }
    };

    IToolAreaAbility HAMMER = new IToolAreaAbility() {
        @Override
        public String getName() {
            return "tool.ability.hammer";
        }

        @Override
        public boolean isAllowed() {
            return ToolConfig.abilityHammer;
        }

        public final int[] rangeAtLevel = { 1, 2, 3, 4 };

        @Override
        public int levels() {
            return rangeAtLevel.length;
        }

        @Override
        public String getExtension(int level) {
            return " (" + rangeAtLevel[level] + ")";
        }

        @Override
        public int sortOrder() {
            return SORT_ORDER_BASE + 2;
        }

        @Override
        public boolean onDig(int level, World world, BlockPos pos, EntityPlayer player, ItemToolAbility tool) {
            int range = rangeAtLevel[level];
            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();

            for (int a = x - range; a <= x + range; a++) {
                for (int b = y - range; b <= y + range; b++) {
                    for (int c = z - range; c <= z + range; c++) {
                        if (a == x && b == y && c == z)
                            continue;

                        tool.breakExtraBlock(world, a, b, c, player, x, y, z);
                    }
                }
            }

            return false;
        }
    };

    IToolAreaAbility HAMMER_FLAT = new IToolAreaAbility() {
        @Override
        public String getName() {
            return "tool.ability.hammer_flat";
        }

        @Override
        public boolean isAllowed() {
            return ToolConfig.abilityHammer;
        }

        public final int[] rangeAtLevel = { 1, 2, 3, 4 };

        @Override
        public int levels() {
            return rangeAtLevel.length;
        }

        @Override
        public String getExtension(int level) {
            return " (" + rangeAtLevel[level] + ")";
        }

        @Override
        public int sortOrder() {
            return SORT_ORDER_BASE + 3;
        }

        @Override
        public boolean onDig(int level, World world, BlockPos pos, EntityPlayer player, ItemToolAbility tool) {
            int range = rangeAtLevel[level];

            RayTraceResult hit = raytraceFromEntity(world, player, false, 4.5d);
            if (hit == null) return true;
            int sideHit = hit.sideHit.getIndex();

            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();

            int xRange = range;
            int yRange = range;
            int zRange = 0;
            switch (sideHit) {
                case 0:
                case 1:
                    yRange = 0;
                    zRange = range;
                    break;
                case 2:
                case 3:
                    break;
                case 4:
                case 5:
                    xRange = 0;
                    zRange = range;
                    break;
            }

            for (int a = x - xRange; a <= x + xRange; a++) {
                for (int b = y - yRange; b <= y + yRange; b++) {
                    for (int c = z - zRange; c <= z + zRange; c++) {
                        if (a == x && b == y && c == z)
                            continue;

                        tool.breakExtraBlock(world, a, b, c, player, x, y, z);
                    }
                }
            }

            return false;
        }

        private RayTraceResult raytraceFromEntity(World world, EntityPlayer player, boolean stopOnLiquid, double range) {
            float f = 1.0F;
            float pitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * f;
            float yaw = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * f;
            double x = player.prevPosX + (player.posX - player.prevPosX) * (double) f;
            double y = player.prevPosY + (player.posY - player.prevPosY) * (double) f + player.getEyeHeight();
            double z = player.prevPosZ + (player.posZ - player.prevPosZ) * (double) f;
            Vec3d start = new Vec3d(x, y, z);

            float cosYaw = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
            float sinYaw = MathHelper.sin(-yaw * 0.017453292F - (float) Math.PI);
            float cosPitch = -MathHelper.cos(-pitch * 0.017453292F);
            float sinPitch = MathHelper.sin(-pitch * 0.017453292F);
            float lookX = sinYaw * cosPitch;
            float lookZ = cosYaw * cosPitch;

            double reach = range;
            if (player instanceof EntityPlayerMP) {
                reach = ((EntityPlayerMP) player).interactionManager.getBlockReachDistance();
            }

            Vec3d end = start.add((double) lookX * reach, (double) sinPitch * reach, (double) lookZ * reach);
            return world.rayTraceBlocks(start, end, stopOnLiquid, !stopOnLiquid, stopOnLiquid);
        }
    };

    IToolAreaAbility EXPLOSION = new IToolAreaAbility() {
        @Override
        public String getName() {
            return "tool.ability.explosion";
        }

        @Override
        public boolean isAllowed() {
            return ToolConfig.abilityExplosion;
        }

        public final float[] strengthAtLevel = { 2.5F, 5F, 10F, 15F };

        @Override
        public int levels() {
            return strengthAtLevel.length;
        }

        @Override
        public String getExtension(int level) {
            return " (" + strengthAtLevel[level] + ")";
        }

        @Override
        public boolean allowsHarvest(int level) {
            return false;
        }

        @Override
        public int sortOrder() {
            return SORT_ORDER_BASE + 4;
        }

        @Override
        public boolean onDig(int level, World world, BlockPos pos, EntityPlayer player, ItemToolAbility tool) {
            float strength = strengthAtLevel[level];

            int x = pos.getX();
            int y = pos.getY();
            int z = pos.getZ();

            ExplosionNT ex = new ExplosionNT(player.world, player, x + 0.5D, y + 0.5D, z + 0.5D, strength);
            ex.addAttrib(ExplosionNT.ExAttrib.ALLDROP);
            ex.addAttrib(ExplosionNT.ExAttrib.NOHURT);
            ex.addAttrib(ExplosionNT.ExAttrib.NOPARTICLE);
            ex.doExplosionA();
            ex.doExplosionB(false);

            player.world.createExplosion(player, x + 0.5D, y + 0.5D, z + 0.5D, 0.1F, false);

            return true;
        }
    };
    // endregion handlers

    IToolAreaAbility[] abilities = { NONE, RECURSION, HAMMER, HAMMER_FLAT, EXPLOSION };

    static IToolAreaAbility getByName(String name) {
        for(IToolAreaAbility ability : abilities) {
            if(ability.getName().equals(name))
                return ability;
        }

        return NONE;
    }
}
