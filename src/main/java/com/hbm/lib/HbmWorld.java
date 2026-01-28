package com.hbm.lib;

import com.hbm.world.gen.MapGenNTMFeatures;
import com.hbm.world.gen.NTMWorldGenerator;
import com.hbm.world.gen.component.BunkerComponents;
import com.hbm.world.gen.component.CivilianFeatures;
import com.hbm.world.gen.component.OfficeFeatures;
import com.hbm.world.gen.component.SiloComponent;
import com.hbm.world.gen.nbt.NBTStructure;
import com.hbm.world.phased.PhasedEventHandler;
import com.hbm.world.phased.PhasedStructureGenerator;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.registry.GameRegistry;

public class HbmWorld {

	public static void mainRegistry() {
		initWorldGen();
	}

	public static NTMWorldGenerator worldGenerator;

	public static void initWorldGen() {
		MapGenStructureIO.registerStructure(MapGenNTMFeatures.Start.class, "NTMFeatures");
		MapGenStructureIO.registerStructure(BunkerComponents.BunkerStart.class, "NTMBunker");
		registerNTMFeatures();

		registerWorldGen(new HbmWorldGen(), 1);

        worldGenerator = new NTMWorldGenerator();
        registerWorldGen(worldGenerator, 1);
        registerWorldGen(PhasedStructureGenerator.INSTANCE, 1);
        MinecraftForge.EVENT_BUS.register(PhasedEventHandler.INSTANCE);
		MinecraftForge.EVENT_BUS.register(worldGenerator);
		NBTStructure.register();
	}

	private static void registerWorldGen(IWorldGenerator nukerWorldGen, int weightedProbability) {
		GameRegistry.registerWorldGenerator(nukerWorldGen, weightedProbability);
	}

	private static void registerNTMFeatures() {
		CivilianFeatures.registerComponents();
		OfficeFeatures.registerComponents();
		BunkerComponents.registerComponents();
		MapGenStructureIO.registerStructureComponent(SiloComponent.class, "NTMSiloComponent");
	}
}
