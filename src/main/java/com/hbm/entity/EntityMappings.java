package com.hbm.entity;

import com.hbm.entity.mob.EntityCreeperGold;
import com.hbm.entity.mob.EntityCreeperPhosgene;
import com.hbm.entity.mob.EntityCreeperVolatile;
import com.hbm.entity.mob.EntityPigeon;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeMushroomIsland;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import java.util.Collection;
import java.util.List;

// Th3_Sl1ze: if you'll figure out auto-registering mob spawn, you're more than welcome to delete this mess
// mlbv: nah, i'll just keep it here for now
public class EntityMappings {

    public static void writeSpawns() {
        Collection<Biome> all = ForgeRegistries.BIOMES.getValuesCollection();
        addSpawn(EntityCreeperPhosgene.class, 5, 1, 1, EnumCreatureType.MONSTER, all);
        addSpawn(EntityCreeperVolatile.class, 10, 1, 1, EnumCreatureType.MONSTER, all);
        addSpawn(EntityCreeperGold.class, 1, 1, 1, EnumCreatureType.MONSTER, all);
//        addSpawn(EntityPlasticBag.class, 1, 1, 3, EnumCreatureType.WATER_CREATURE, BiomeDictionary.getBiomes(BiomeDictionary.Type.OCEAN));
        addSpawn(EntityPigeon.class, 1, 5, 10, EnumCreatureType.CREATURE, BiomeDictionary.getBiomes(BiomeDictionary.Type.PLAINS));
    }

    public static void addSpawn(Class<? extends EntityLiving> entityClass, int weightedProb, int min, int max, EnumCreatureType typeOfCreature, Collection<Biome> biomes) {
        for (Biome biome : biomes) {
            if (biome == null) {
                continue;
            }
            if (biome instanceof BiomeMushroomIsland) {
                continue;
            }

            List<Biome.SpawnListEntry> spawns = biome.getSpawnableList(typeOfCreature);

            for (Biome.SpawnListEntry entry : spawns) {
                if (entry.entityClass == entityClass) {
                    entry.itemWeight = weightedProb;
                    entry.minGroupCount = min;
                    entry.maxGroupCount = max;
                    break;
                }
            }

            spawns.add(new Biome.SpawnListEntry(entityClass, weightedProb, min, max));
        }
    }
}
