package com.hbm.world.generator;

import com.hbm.world.phased.AbstractPhasedStructure;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CellularDungeon {

	// a buffer "map" of the rooms being generated before being spawned in
	CellularDungeonRoom[][] cells;
	EnumFacing[][] doors;
	// the order in which the buffer should be processed
	private List<int[]> order = new ArrayList<int[]>();

	// the size of the cell array x
	int dimX;
	// the size of the cell array z
	int dimZ;
	// the base width (and length) of a room
	public int width;
	// the height of a room
	public int height;
	// list of random floor blocks with equal weight
	public List<IBlockState> floor = new ArrayList<>();
	// list of random ceiling blocks with equal weight
	public List<IBlockState> ceiling = new ArrayList<>();
	// list of random wall blocks with equal weight
	public List<IBlockState> wall = new ArrayList<>();
	// the rooms that the dungeon can use
	public List<CellularDungeonRoom> rooms = new ArrayList<CellularDungeonRoom>();
	int tries;
	int branches;

	public CellularDungeon(int width, int height, int dimX, int dimZ, int tries, int branches) {

		this.dimX = dimX;
		this.dimZ = dimZ;
		this.width = width;
		this.height = height;
		this.tries = tries;
		this.branches = branches;
	}

	public CellularDungeon(int width, int height, int dimX, int dimZ, int tries, int branches, Block floor, Block ceiling, Block wall) {

		this.dimX = dimX;
		this.dimZ = dimZ;
		this.width = width;
		this.height = height;
		this.tries = tries;
		this.branches = branches;
		this.floor.add(floor.getDefaultState());
		this.ceiling.add(ceiling.getDefaultState());
		this.wall.add(wall.getDefaultState());
	}

	public void generate(AbstractPhasedStructure.LegacyBuilder world, int x, int y, int z, Random rand) {
		x -= dimX * width / 2;
		z -= dimZ * width / 2;

		compose(rand);
		for(int[] coord : order) {

			if(coord == null || coord.length != 2)
				continue;

			int dx = coord[0];
			int dz = coord[1];

			if(cells[dx][dz] != null) {
				cells[dx][dz].generate(world, x + dx * (width - 1), y, z + dz * (width - 1), doors[dx][dz]);
			}
		}
	}

	int rec = 0;

	public void compose(Random rand) {

		cells = new CellularDungeonRoom[dimX][dimZ];
		doors = new EnumFacing[dimX][dimZ];
		order.clear();

		int startX = dimX / 2;
		int startZ = dimZ / 2;

		cells[startX][startZ] = DungeonToolbox.getRandom(rooms, rand);
		doors[startX][startZ] = null;
		order.add(new int[] { startX, startZ });

		rec = 0;
		addRoom(startX, startZ, rand, null, DungeonToolbox.getRandom(rooms, rand));
	}

	// if x and z are occupied, it will just use the next nearby random space
	private boolean addRoom(int x, int z, Random rand, EnumFacing door, CellularDungeonRoom room) {

		rec++;
		if(rec > tries)
			return false;

		if(x < 0 || z < 0 || x >= dimX || z >= dimZ)
			return false;

		if(cells[x][z] != null) {

			EnumFacing dir = getRandomDir(rand);
			addRoom(x + dir.getXOffset(), z + dir.getZOffset(), rand, dir.getOpposite(), DungeonToolbox.getRandom(rooms, rand));
			return false;
		}


		if(room.daisyChain == null || addRoom(x + room.daisyDirection.getXOffset(), z + room.daisyDirection.getZOffset(), rand, null, room.daisyChain)) {
			cells[x][z] = room;
			doors[x][z] = door;
			order.add(new int[] { x, z });
		}

		for(int i = 0; i < branches; i++) {
			EnumFacing dir = getRandomDir(rand);
			addRoom(x + dir.getXOffset(), z + dir.getZOffset(), rand, dir.getOpposite(), DungeonToolbox.getRandom(rooms, rand));
		}

		return true;
	}

    public int getRadius() {
        int radiusX = (dimX * width) / 2;
        int radiusZ = (dimZ * width) / 2;
        return Math.max(radiusX, radiusZ);
    }

	public static EnumFacing getRandomDir(Random rand) {

		return EnumFacing.byIndex(rand.nextInt(4) + 2);
	}
}