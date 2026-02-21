package com.hbm.explosion;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.CompatibilityConfig;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraft.world.World;

public class ExplosionTom {
	public int posX;
	public int posY;
	public int posZ;
	public int lastposX = 0;
	public int lastposZ = 0;
	public int radius;
	public int radius2;
	public World world;
	private int n = 1;
	private int nlimit;
	private int shell;
	private int leg;
	private int element;
	
	public void saveToNbt(NBTTagCompound nbt, String name) {
		nbt.setInteger(name + "posX", posX);
		nbt.setInteger(name + "posY", posY);
		nbt.setInteger(name + "posZ", posZ);
		nbt.setInteger(name + "lastposX", lastposX);
		nbt.setInteger(name + "lastposZ", lastposZ);
		nbt.setInteger(name + "radius", radius);
		nbt.setInteger(name + "radius2", radius2);
		nbt.setInteger(name + "n", n);
		nbt.setInteger(name + "nlimit", nlimit);
		nbt.setInteger(name + "shell", shell);
		nbt.setInteger(name + "leg", leg);
		nbt.setInteger(name + "element", element);
	}
	
	public void readFromNbt(NBTTagCompound nbt, String name) {
		posX = nbt.getInteger(name + "posX");
		posY = nbt.getInteger(name + "posY");
		posZ = nbt.getInteger(name + "posZ");
		lastposX = nbt.getInteger(name + "lastposX");
		lastposZ = nbt.getInteger(name + "lastposZ");
		radius = nbt.getInteger(name + "radius");
		radius2 = nbt.getInteger(name + "radius2");
		n = nbt.getInteger(name + "n");
		nlimit = nbt.getInteger(name + "nlimit");
		shell = nbt.getInteger(name + "shell");
		leg = nbt.getInteger(name + "leg");
		element = nbt.getInteger(name + "element");
	}
	
	public ExplosionTom(int x, int y, int z, World world, int rad) {
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		
		this.world = world;
		
		this.radius = rad;
		this.radius2 = this.radius * this.radius;

		this.nlimit = this.radius2 * 4;
	}
	
	public boolean update() {
		if(!CompatibilityConfig.isWarDim(world)){
			return true;
		}
		breakColumn(this.lastposX, this.lastposZ);
		this.shell = (int) Math.floor((Math.sqrt(n) + 1) / 2);
		int shell2 = this.shell * 2;
		this.leg = (int) Math.floor((this.n - (shell2 - 1) * (shell2 - 1)) / shell2);
		this.element = (this.n - (shell2 - 1) * (shell2 - 1)) - shell2 * this.leg - this.shell + 1;
		this.lastposX = this.leg == 0 ? this.shell : this.leg == 1 ? -this.element : this.leg == 2 ? -this.shell : this.element;
		this.lastposZ = this.leg == 0 ? this.element : this.leg == 1 ? this.shell : this.leg == 2 ? -this.element : -this.shell;
		this.n++;
		return this.n > this.nlimit;
	}

    // mlbv: 100% parity as of Oct 30, 2025; I made some changes to avoid redundant recalculations
    private void breakColumn(final int x, final int z) {
        final int r2 = x * x + z * z;
        final int dist = this.radius2 - r2;
        if (dist <= 0) return;
        final int pX = posX + x;
        final int pZ = posZ + z;
        final double r = Math.sqrt(r2);
        final boolean insideRim = r < 500.0;

        int y = 256;
        final int terrain = 63;

        final double cA = (terrain - Math.exp(-(r2) / 40000.0) * 13.0) + world.rand.nextInt(2); // bowl
        final double rMinus200 = r - 200.0;
        final double cB = cA + Math.exp(-(rMinus200 * rMinus200) / 400.0) * 13.0;               // peak ring
        final double rMinus500 = r - 500.0;
        final int craterFloor = (int) (cB + Math.exp(-(rMinus500 * rMinus500) / 2000.0) * 37.0);// rim

        final MutableBlockPos pos = new MutableBlockPos();

        for (int i = 256; i > 0; i--) {
            pos.setPos(pX, i, pZ);
            if (i == craterFloor || !world.isAirBlock(pos)) {
                y = i;
                break;
            }
        }

        final int height = terrain - 14;
        final int offset = 20;
        final int threshold = (int) (r * (height + offset) / (double) this.radius) + world.rand.nextInt(2) - offset;

        Material m;
        while (y > threshold) {
            if (y == 0) break;

            if (y <= craterFloor) {
                pos.setPos(pX, y, pZ);
                if (world.rand.nextInt(499) < 1) {
                    world.setBlockState(pos, ModBlocks.ore_tektite_osmiridium.getDefaultState(), 2);
                } else {
                    world.setBlockState(pos, ModBlocks.tektite.getDefaultState(), 2);
                }
            } else {
                if (y > terrain + 1) {
                    if (insideRim) {
                        for (int i = -2; i < 3; i++) {
                            for (int j = -2; j < 3; j++) {
                                for (int k = -2; k < 3; k++) {
                                    pos.setPos(pX + i, y + j, pZ + k);
                                    m = world.getBlockState(pos).getMaterial();
                                    if (m == Material.WATER || m == Material.ICE || m == Material.SNOW || m.getCanBurn()) {
                                        world.setBlockToAir(pos);
                                        world.setBlockToAir(pos.setPos(pX, y, pZ));
                                    }
                                }
                            }
                        }
                        world.setBlockState(pos.setPos(pX, y, pZ), Blocks.AIR.getDefaultState(), 2);
                    }
                } else {
                    for (int i = -2; i < 3; i++) {
                        for (int j = -2; j < 3; j++) {
                            for (int k = -2; k < 3; k++) {
                                pos.setPos(pX + i, y + j, pZ + k);
                                m = world.getBlockState(pos).getMaterial();
                                if (m == Material.WATER || m == Material.ICE || world.isAirBlock(pos.setPos(pX + i, y, pZ + k))) {
                                    world.setBlockState(pos.setPos(pX + i, y, pZ + k), Blocks.LAVA.getDefaultState(), 2);
                                    world.setBlockState(pos.setPos(pX, y, pZ), Blocks.LAVA.getDefaultState(), 2);
                                }
                            }
                        }
                    }
                    world.setBlockState(pos.setPos(pX, y, pZ), Blocks.LAVA.getDefaultState(), 2);
                }
            }
            y--;
        }
    }
}
