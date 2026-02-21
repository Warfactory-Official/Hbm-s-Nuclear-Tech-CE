package com.hbm.world.gen;

import com.hbm.config.GeneralConfig;
import com.hbm.config.StructureConfig;
import com.hbm.world.gen.component.BunkerComponents;
import com.hbm.world.gen.component.CivilianFeatures;
import com.hbm.world.gen.component.OfficeFeatures;
import com.hbm.world.gen.component.SiloComponent;
import net.minecraft.init.Biomes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeMesa;
import net.minecraft.world.gen.structure.MapGenStructure;
import net.minecraft.world.gen.structure.StructureStart;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MapGenNTMFeatures extends MapGenStructure {

	private static List<Biome> biomelist;
	private int maxDistanceBetweenScatteredFeatures;
	private int minDistanceBetweenScatteredFeatures;

	public MapGenNTMFeatures() {
		this.maxDistanceBetweenScatteredFeatures = StructureConfig.structureMaxChunks;
		this.minDistanceBetweenScatteredFeatures = StructureConfig.structureMinChunks;
	}

	@Override
	public String getStructureName() {
		return "NTMFeatures";
	}

	@Nullable
	@Override
	public BlockPos getNearestStructurePos(World worldIn, BlockPos pos, boolean findUnexplored) {
		return null;
	}

	@Override
	protected boolean canSpawnStructureAtCoords(int chunkX, int chunkZ) {
		int k = chunkX;
		int l = chunkZ;

		if (chunkX < 0) {
			chunkX -= this.maxDistanceBetweenScatteredFeatures - 1;
		}
		if (chunkZ < 0) {
			chunkZ -= this.maxDistanceBetweenScatteredFeatures - 1;
		}

		int i1 = chunkX / this.maxDistanceBetweenScatteredFeatures;
		int j1 = chunkZ / this.maxDistanceBetweenScatteredFeatures;
		Random random = this.world.setRandomSeed(i1, j1, 14357617);
		i1 *= this.maxDistanceBetweenScatteredFeatures;
		j1 *= this.maxDistanceBetweenScatteredFeatures;
		i1 += random.nextInt(this.maxDistanceBetweenScatteredFeatures - this.minDistanceBetweenScatteredFeatures);
		j1 += random.nextInt(this.maxDistanceBetweenScatteredFeatures - this.minDistanceBetweenScatteredFeatures);

		if (k == i1 && l == j1) {
			Biome biome = this.world.getBiome(new BlockPos(k * 16 + 8, 0, l * 16 + 8));

			if (biomelist == null) {
				biomelist = Arrays.asList(Biomes.OCEAN, Biomes.RIVER, Biomes.FROZEN_OCEAN, Biomes.FROZEN_RIVER, Biomes.DEEP_OCEAN);
			}

			for (Biome disallowed : biomelist) {
				if (biome == disallowed) {
					return false;
				}
			}
			return true;
		}

		return false;
	}

	@Override
	protected StructureStart getStructureStart(int chunkX, int chunkZ) {
		if (this.rand.nextInt(15) == 0) {
			return new BunkerComponents.BunkerStart(this.world, this.rand, chunkX, chunkZ);
		}
		return new MapGenNTMFeatures.Start(this.world, this.rand, chunkX, chunkZ);
	}

	public static class Start extends StructureStart {

		public Start() {}

		public Start(World world, Random rand, int chunkX, int chunkZ) {
			super(chunkX, chunkZ);

			int i = (chunkX << 4) + 8;
			int j = (chunkZ << 4) + 8;

			Biome biome = world.getBiome(new BlockPos(i, 0, j));
			// Th3_Sl1ze: I'm a degenerate forgetting to add things when I port them, don't mind me!
			if (biome.getHeightVariation() <= 0.25F && rand.nextInt(10) == 0) {
				SiloComponent silo = new SiloComponent(rand, i, j);
				this.components.add(silo);
			} else if (biome.getDefaultTemperature() >= 1.0F && biome.getRainfall() == 0.0F && !(biome instanceof BiomeMesa)) {
				if (rand.nextBoolean()) {
					CivilianFeatures.NTMHouse1 house1 = new CivilianFeatures.NTMHouse1(rand, i, j);
					this.components.add(house1);
				} else {
					CivilianFeatures.NTMHouse2 house2 = new CivilianFeatures.NTMHouse2(rand, i, j);
					this.components.add(house2);
				}
			} else {
				switch (rand.nextInt(6)) {
					case 0 -> {
						CivilianFeatures.NTMLab2 lab2 = new CivilianFeatures.NTMLab2(rand, i, j);
						this.components.add(lab2);
					}
					case 1 -> {
						CivilianFeatures.NTMLab1 lab1 = new CivilianFeatures.NTMLab1(rand, i, j);
						this.components.add(lab1);
					}
					case 2 -> {
						OfficeFeatures.LargeOffice office = new OfficeFeatures.LargeOffice(rand, i, j);
						this.components.add(office);
					}
					case 3 -> {
						OfficeFeatures.LargeOfficeCorner officeCorner = new OfficeFeatures.LargeOfficeCorner(rand, i, j);
						this.components.add(officeCorner);
					}
					case 4, 5 -> {
						CivilianFeatures.RuralHouse1 ruralHouse = new CivilianFeatures.RuralHouse1(rand, i, j);
						this.components.add(ruralHouse);
					}
				}
			}

			if (GeneralConfig.enableDebugMode) {
				System.out.print("[Debug] StructureStart at " + i + ", 64, " + j + "\n[Debug] Components: ");
				this.components.forEach((component) -> System.out.print(component.getClass().getSimpleName() + " "));
				System.out.print("\n");
			}

			this.updateBoundingBox();
		}
	}
}
