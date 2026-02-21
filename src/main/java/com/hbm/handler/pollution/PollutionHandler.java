package com.hbm.handler.pollution;

import com.hbm.config.MobConfig;
import com.hbm.config.RadiationConfig;
import com.hbm.entity.mob.glyphid.EntityGlyphidDigger;
import com.hbm.entity.mob.glyphid.EntityGlyphidScout;
import com.hbm.render.amlfrom1710.Vec3;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class PollutionHandler {

    public static final String fileName = "hbmpollution.dat";
    public static HashMap<World, PollutionPerWorld> perWorld = new HashMap();

    /** Baserate of soot generation for a furnace-equivalent machine per second */
    public static final float SOOT_PER_SECOND = 1F / 25F;
    /** Baserate of heavy metal generation, balanced around the soot values of combustion engines */
    public static final float HEAVY_METAL_PER_SECOND = 1F / 50F;
    /** Baserate for poison when spilled */
    public static final float POISON_PER_SECOND = 1F / 50F;
    public static Vec3 targetCoords;

    ///////////////////////
    /// UTILITY METHODS ///
    ///////////////////////
    public static void incrementPollution(World world, BlockPos pos, PollutionType type, float amount) {

        if(!RadiationConfig.enablePollution || pos == null) return; //FIXME: This is a temporary fix for a crash, pos should never be null

        PollutionPerWorld ppw = perWorld.get(world);
        if(ppw == null) return;
        ChunkPos chPos = new ChunkPos(pos.getX() >> 6, pos.getZ() >> 6);
        PollutionData data = ppw.pollution.get(chPos);
        if(data == null) {
            data = new PollutionData();
            ppw.pollution.put(chPos, data);
        }
        data.pollution[type.ordinal()] = MathHelper.clamp((float) (data.pollution[type.ordinal()] + amount * MobConfig.pollutionMult), 0F, 10_000F);
    }

    public static void decrementPollution(World world, BlockPos pos, PollutionType type, float amount) {
        incrementPollution(world, pos, type, -amount);
    }

    public static void setPollution(World world, BlockPos pos, PollutionType type, float amount) {

        if(!RadiationConfig.enablePollution) return;

        PollutionPerWorld ppw = perWorld.get(world);
        if(ppw == null) return;
        ChunkPos chPos = new ChunkPos(pos.getX() >> 6, pos.getZ() >> 6);
        PollutionData data = ppw.pollution.get(chPos);
        if(data == null) {
            data = new PollutionData();
            ppw.pollution.put(chPos, data);
        }
        data.pollution[type.ordinal()] = amount;
    }

    public static float getPollution(World world, BlockPos pos, PollutionType type) {

        if(!RadiationConfig.enablePollution) return 0;

        PollutionPerWorld ppw = perWorld.get(world);
        if(ppw == null) return 0F;
        ChunkPos chPos = new ChunkPos(pos.getX() >> 6, pos.getZ() >> 6);
        PollutionData data = ppw.pollution.get(chPos);
        if(data == null) return 0F;
        return data.pollution[type.ordinal()];
    }

    public static PollutionData getPollutionData(World world, BlockPos pos) {

        if(!RadiationConfig.enablePollution) return null;

        PollutionPerWorld ppw = perWorld.get(world);
        if(ppw == null) return null;
        ChunkPos chPos = new ChunkPos(pos.getX() >> 6, pos.getZ() >> 6);
        PollutionData data = ppw.pollution.get(chPos);
        return data;
    }

    //////////////////////
    /// EVENT HANDLING ///
    //////////////////////
    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        if(!event.getWorld().isRemote && RadiationConfig.enablePollution) {
            WorldServer world = (WorldServer) event.getWorld();
            String dirPath = getDataDir(world);

            try {
                File pollutionFile = new File(dirPath, fileName);

                if(pollutionFile != null) {

                    if(pollutionFile.exists()) {
                        FileInputStream io = new FileInputStream(pollutionFile);
                        NBTTagCompound data = CompressedStreamTools.readCompressed(io);
                        io.close();
                        perWorld.put(event.getWorld(), new PollutionPerWorld(data));
                    } else {
                        perWorld.put(event.getWorld(), new PollutionPerWorld());
                    }
                }
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        if(!event.getWorld().isRemote) perWorld.remove(event.getWorld());
    }

    @SubscribeEvent
    public void onWorldSave(WorldEvent.Save event) {
        if(!event.getWorld().isRemote) {
            WorldServer world = (WorldServer) event.getWorld();
            String dirPath = getDataDir(world);
            File pollutionFile = new File(dirPath, fileName);

            try {
                if(!pollutionFile.getParentFile().exists()) pollutionFile.getParentFile().mkdirs();
                if(!pollutionFile.exists()) pollutionFile.createNewFile();
                PollutionPerWorld ppw = perWorld.get(world);
                if(ppw != null) {
                    NBTTagCompound data = ppw.writeToNBT();
                    CompressedStreamTools.writeCompressed(data, new FileOutputStream(pollutionFile));
                }
            } catch(Exception ex) {
                System.out.println("Failed to write " + pollutionFile.getAbsolutePath());
                ex.printStackTrace();
            }
        }
    }

    public String getDataDir(WorldServer world) {
        String dir = world.getSaveHandler().getWorldDirectory().getAbsolutePath();
        // Crucible and probably Thermos provide dimId by themselves
        String dimId = File.separator + "DIM" + world.provider.getDimension();
        if(world.provider.getDimension() != 0 && !dir.endsWith(dimId)) {
            dir += dimId;
        }
        dir += File.separator + "data";
        return dir;
    }

    //////////////////////////
    /// SYSTEM UPDATE LOOP ///
    //////////////////////////
    int eggTimer = 0;
    @SubscribeEvent
    public void updateSystem(TickEvent.ServerTickEvent event) {

        if(event.side == Side.SERVER && event.phase == TickEvent.Phase.END) {

            handleWorldDestruction();

            eggTimer++;
            if(eggTimer < 60) return;
            eggTimer = 0;

            for(Map.Entry<World, PollutionPerWorld> entry : perWorld.entrySet()) {
                HashMap<ChunkPos, PollutionData> newPollution = new HashMap();

                for(Map.Entry<ChunkPos, PollutionData> chunk : entry.getValue().pollution.entrySet()) {
                    int x = chunk.getKey().x;
                    int z = chunk.getKey().z;
                    PollutionData data = chunk.getValue();

                    float[] pollutionForNeightbors = new float[PollutionType.VALUES.length];
                    int S = PollutionType.SOOT.ordinal();
                    int H = PollutionType.HEAVYMETAL.ordinal();
                    int P = PollutionType.POISON.ordinal();

                    /* CALCULATION */
                    if(data.pollution[S] > 10) {
                        pollutionForNeightbors[S] = (float) (data.pollution[S] * 0.05F);
                        data.pollution[S] *= 0.8F;
                    }

                    data.pollution[S] *= 0.99F;
                    data.pollution[H] *= 0.9995F;

                    if(data.pollution[P] > 10) {
                        pollutionForNeightbors[P] = data.pollution[P] * 0.025F;
                        data.pollution[P] *= 0.9F;
                    } else {
                        data.pollution[P] *= 0.995F;
                    }

                    /* SPREADING */
                    //apply new data to self
                    PollutionData newData = newPollution.get(chunk.getKey());
                    if(newData == null) newData = new PollutionData();

                    boolean shouldPut = false;
                    for(int i = 0; i < newData.pollution.length; i++) {
                        newData.pollution[i] += data.pollution[i];
                        if(newData.pollution[i] > 0) shouldPut = true;
                    }
                    if(shouldPut) newPollution.put(chunk.getKey(), newData);

                    //apply neighbor data to neighboring chunks
                    int[][] offsets = new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
                    for(int[] offset : offsets) {
                        ChunkPos offPos = new ChunkPos(x + offset[0], z + offset[1]);
                        PollutionData offsetData = newPollution.get(offPos);
                        if(offsetData == null) offsetData = new PollutionData();

                        shouldPut = false;
                        for(int i = 0; i < offsetData.pollution.length; i++) {
                            offsetData.pollution[i] += pollutionForNeightbors[i];
                            if(offsetData.pollution[i] > 0) shouldPut = true;
                        }
                        if(shouldPut) newPollution.put(offPos, offsetData);
                    }
                }

                entry.getValue().pollution.clear();
                entry.getValue().pollution.putAll(newPollution);
            }
        }
    }

    protected static final float DESTRUCTION_THRESHOLD = 15F;
    protected static final int DESTRUCTION_COUNT = 5;

    protected static void handleWorldDestruction() {

        for(Map.Entry<World, PollutionPerWorld> entry : perWorld.entrySet()) {

            World world = entry.getKey();
            WorldServer serv = (WorldServer) world;
            ChunkProviderServer provider = (ChunkProviderServer) serv.getChunkProvider();

            for(Map.Entry<ChunkPos, PollutionData> pollution : entry.getValue().pollution.entrySet()) {

                float poison = pollution.getValue().pollution[PollutionType.POISON.ordinal()];
                if(poison < DESTRUCTION_THRESHOLD) continue;

                ChunkPos entryPos = pollution.getKey();

                for(int i = 0; i < DESTRUCTION_COUNT; i++) {
                    int x = (entryPos.x << 6) + world.rand.nextInt(64);
                    int z = (entryPos.z << 6) + world.rand.nextInt(64);

                    if(provider.chunkExists(x >> 4, z >> 4)) {
                        int y = world.getHeight(x, z) - world.rand.nextInt(3) + 1;
                        BlockPos bPos = new BlockPos(x, y, z);
                        IBlockState state = world.getBlockState(bPos);
                        Block b = state.getBlock();

                        if(b == Blocks.GRASS || (b == Blocks.DIRT && b.getMetaFromState(state) == 0)) {
                            world.setBlockState(bPos, Blocks.DIRT.getDefaultState(), 3);
                        } else if(b == Blocks.TALLGRASS || b.getMaterial(state) == Material.LEAVES || b.getMaterial(state) == Material.PLANTS) {
                            world.setBlockToAir(bPos);
                        }
                    }
                }
            }
        }
    }

    //////////////////////
    /// DATA STRUCTURE ///
    //////////////////////
    public static class PollutionPerWorld {
        public HashMap<ChunkPos, PollutionData> pollution = new HashMap();

        public PollutionPerWorld() { }

        public PollutionPerWorld(NBTTagCompound data) {

            NBTTagList list = data.getTagList("entries", 10);

            for(int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound nbt = list.getCompoundTagAt(i);
                int chunkX = nbt.getInteger("chunkX");
                int chunkZ = nbt.getInteger("chunkZ");
                pollution.put(new ChunkPos(chunkX, chunkZ), PollutionData.fromNBT(nbt));
            }
        }

        public NBTTagCompound writeToNBT() {

            NBTTagCompound data = new NBTTagCompound();

            NBTTagList list = new NBTTagList();

            for(Map.Entry<ChunkPos, PollutionData> entry : pollution.entrySet()) {
                NBTTagCompound nbt = new NBTTagCompound();
                nbt.setInteger("chunkX", entry.getKey().x);
                nbt.setInteger("chunkZ", entry.getKey().z);
                entry.getValue().toNBT(nbt);
                list.appendTag(nbt);
            }

            data.setTag("entries", list);

            return data;
        }
    }

    public static class PollutionData {
        public float[] pollution = new float[PollutionType.VALUES.length];

        public static PollutionData fromNBT(NBTTagCompound nbt) {
            PollutionData data = new PollutionData();

            for(int i = 0; i < PollutionType.VALUES.length; i++) {
                data.pollution[i] = nbt.getFloat(PollutionType.VALUES[i].name().toLowerCase(Locale.US));
            }

            return data;
        }

        public void toNBT(NBTTagCompound nbt) {
            for(int i = 0; i < PollutionType.VALUES.length; i++) {
                nbt.setFloat(PollutionType.VALUES[i].name().toLowerCase(Locale.US), pollution[i]);
            }
        }
    }

    public static enum PollutionType {
        SOOT("trait.ptype.soot"),
        POISON("trait.ptype.poison"),
        HEAVYMETAL("trait.ptype.heavymetal"),
        FALLOUT("trait.ptype.fallout");

        public static final PollutionType[] VALUES = values();

        public String name;

        private PollutionType(String name) {
            this.name = name;
        }
    }

    @SubscribeEvent
    public void rampantTargetSetter(PlayerSleepInBedEvent event){
        if (MobConfig.rampantGlyphidGuidance) targetCoords = Vec3.createVectorHelper(event.getPos().getX(), event.getPos().getY(), event.getPos().getZ());
    }

    @SubscribeEvent
    public void rampantScoutPopulator(WorldEvent.PotentialSpawns event){
        BlockPos pos = event.getPos();
        if(MobConfig.rampantNaturalScoutSpawn && !event.getWorld().isRemote && event.getWorld().provider.getDimension() == 0 && event.getWorld().canSeeSky(pos) && !event.isCanceled()) {
            if (event.getWorld().rand.nextInt(MobConfig.rampantScoutSpawnChance) == 0) {
                int x = pos.getX(), y = pos.getY(), z = pos.getZ();

                float soot = PollutionHandler.getPollution(event.getWorld(), pos, PollutionType.SOOT);

                if (soot >= MobConfig.rampantScoutSpawnThresh) {
                    EntityGlyphidScout scout = new EntityGlyphidScout(event.getWorld());
                    scout.setLocationAndAngles(x, y, z, event.getWorld().rand.nextFloat() * 360.0F, 0.0F);
                    if(scout.isValidLightLevel()) {
                        //escort for the scout, which can also deal with obstacles
                        EntityGlyphidDigger digger = new EntityGlyphidDigger(event.getWorld());
                        scout.setLocationAndAngles(x, y, z, event.getWorld().rand.nextFloat() * 360.0F, 0.0F);
                        digger.setLocationAndAngles(x, y, z, event.getWorld().rand.nextFloat() * 360.0F, 0.0F);
                        if(scout.getCanSpawnHere()) event.getWorld().spawnEntity(scout);
                        if(digger.getCanSpawnHere()) event.getWorld().spawnEntity(digger);
                    }
                }
            }
        }

    }
}
