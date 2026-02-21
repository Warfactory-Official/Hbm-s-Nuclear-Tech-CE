package com.hbm.entity;

import com.hbm.render.amlfrom1710.Vec3;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathFinder;
import net.minecraft.pathfinding.WalkNodeProcessor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PathFinderUtils {
    public static Path getPathEntityToEntityPartial(World world, EntityLiving fromEntity, Entity toEntity, float maxDist, boolean allowDoors, boolean allowWater, boolean allowOpenDoors) {
        world.profiler.startSection("pathfind");

        Vec3 vec = Vec3.createVectorHelper(toEntity.posX - fromEntity.posX, toEntity.posY - fromEntity.posY, toEntity.posZ - fromEntity.posZ);
        vec = vec.normalize();
        vec.xCoord *= maxDist;
        vec.yCoord *= maxDist;
        vec.zCoord *= maxDist;

        int x = (int) Math.floor(fromEntity.posX + vec.xCoord);
        int y = (int) Math.floor(fromEntity.posY + vec.yCoord);
        int z = (int) Math.floor(fromEntity.posZ + vec.zCoord);

        //this part will adjust the end of the path so it's actually on the ground, it being unreachable causes mobs to slow down
        boolean solid = false;

        for(int i = y; i > y - 10; i--) {
            if(!world.getBlockState(new BlockPos(x, i, z)).getMaterial().blocksMovement() && world.getBlockState(new BlockPos(x, i - 1, z)).isNormalCube()) {
                solid = true;
                y = i;
                break;
            }

        }

        if(!solid) for(int i = y + 10; i > y; i--) {
            if(!world.getBlockState(new BlockPos(x, i, z)).getMaterial().blocksMovement() && world.getBlockState(new BlockPos(x, i - 1, z)).isNormalCube()) {
                y = i;
                break;
            }
        }

        WalkNodeProcessor nodeProcessor = new WalkNodeProcessor();
        nodeProcessor.setCanEnterDoors(allowDoors);
        nodeProcessor.setCanOpenDoors(allowOpenDoors);
        nodeProcessor.setCanSwim(allowWater);

        PathFinder pathFinder = new PathFinder(nodeProcessor);

        Path path = pathFinder.findPath(world, fromEntity, new BlockPos(x, y, z), maxDist);
        world.profiler.endSection();
        return path;
    }

    public static Path getPathEntityToCoordPartial(World world, EntityLiving fromEntity, int posX, int posY, int posZ, float maxDist, boolean allowDoors, boolean allowOpenDoors, boolean allowWater) {
        world.profiler.startSection("pathfind");

        Vec3 vec = Vec3.createVectorHelper(posX - fromEntity.posX, posY - fromEntity.posY, posZ - fromEntity.posZ);
        vec = vec.normalize();
        vec.xCoord *= maxDist;
        vec.yCoord *= maxDist;
        vec.zCoord *= maxDist;

        int x = (int) Math.floor(fromEntity.posX + vec.xCoord);
        int y = (int) Math.floor(fromEntity.posY + vec.yCoord);
        int z = (int) Math.floor(fromEntity.posZ + vec.zCoord);

        //this part will adjust the end of the path so it's actually on the ground, it being unreachable causes mobs to slow down
        boolean solid = false;

        for(int i = y; i > y - 10; i--) {
            if(!world.getBlockState(new BlockPos(x, i, z)).getMaterial().blocksMovement() && world.getBlockState(new BlockPos(x, i - 1, z)).isNormalCube()) {
                solid = true;
                y = i;
                break;
            }

        }

        if(!solid) for(int i = y + 10; i > y; i--) {
            if(!world.getBlockState(new BlockPos(x, i, z)).getMaterial().blocksMovement() && world.getBlockState(new BlockPos(x, i - 1, z)).isNormalCube()) {
                y = i;
                break;
            }
        }

        WalkNodeProcessor nodeProcessor = new WalkNodeProcessor();
        nodeProcessor.setCanEnterDoors(allowDoors);
        nodeProcessor.setCanOpenDoors(allowOpenDoors);
        nodeProcessor.setCanSwim(allowWater);

        PathFinder pathFinder = new PathFinder(nodeProcessor);

        Path path = pathFinder.findPath(world, fromEntity, new BlockPos(x, y, z), maxDist);
        world.profiler.endSection();
        return path;
    }
}
