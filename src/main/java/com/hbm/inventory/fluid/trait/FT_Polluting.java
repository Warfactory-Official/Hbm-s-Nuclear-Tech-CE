package com.hbm.inventory.fluid.trait;

import com.google.gson.JsonObject;
import com.google.gson.stream.JsonWriter;
import com.hbm.handler.pollution.PollutionHandler;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.tank.FluidTankNTM;
import com.hbm.util.I18nUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FT_Polluting extends FluidTrait {

    //original draft had both of them inside a hashmap for the release type but honestly handling hash maps in hash maps adds more complexity than it removes
    public HashMap<PollutionHandler.PollutionType, Float> releaseMap = new HashMap();
    public HashMap<PollutionHandler.PollutionType, Float> burnMap = new HashMap();

    public FT_Polluting release(PollutionHandler.PollutionType type, float amount) {
        releaseMap.put(type, amount);
        return this;
    }

    public FT_Polluting burn(PollutionHandler.PollutionType type, float amount) {
        burnMap.put(type, amount);
        return this;
    }

    @Override
    public void addInfo(List<String> info) {
        info.add(TextFormatting.GOLD + "[" + I18nUtil.resolveKey("trait.polluting") + "]");
    }

    @Override
    public void addInfoHidden(List<String> info) {

        if(!this.releaseMap.isEmpty()) {
            info.add(I18nUtil.resolveKey("trait.polluspill"));
            for(Map.Entry<PollutionHandler.PollutionType, Float> entry : releaseMap.entrySet()) info.add(TextFormatting.GREEN + " - " + entry.getValue() + " " + I18nUtil.resolveKey(entry.getKey().name) + " " + I18nUtil.resolveKey("desc.permb"));
        }

        if(!this.burnMap.isEmpty()) {
            info.add(I18nUtil.resolveKey("trait.polluburn"));
            for(Map.Entry<PollutionHandler.PollutionType, Float> entry : burnMap.entrySet()) info.add(TextFormatting.RED + " - " + entry.getValue() + " " + I18nUtil.resolveKey(entry.getKey().name) + " " + I18nUtil.resolveKey("desc.permb"));
        }
    }

    @Override
    public void onFluidRelease(World world, int x, int y, int z, FluidTankNTM tank, int overflowAmount, FluidReleaseType type) {
        if(type == FluidReleaseType.SPILL) for(Map.Entry<PollutionHandler.PollutionType, Float> entry : releaseMap.entrySet()) PollutionHandler.incrementPollution(world, new BlockPos(x, y, z), entry.getKey(), entry.getValue());
        if(type == FluidReleaseType.BURN) for(Map.Entry<PollutionHandler.PollutionType, Float> entry : burnMap.entrySet()) PollutionHandler.incrementPollution(world, new BlockPos(x, y, z), entry.getKey(), entry.getValue());
    }

    @Override
    public void serializeJSON(JsonWriter writer) throws IOException {
        writer.name("release").beginObject();
        for(Map.Entry<PollutionHandler.PollutionType, Float> entry : releaseMap.entrySet()) {
            writer.name(entry.toString()).value(entry.getValue());
        }
        writer.endObject();
        writer.name("burn").beginObject();
        for(Map.Entry<PollutionHandler.PollutionType, Float> entry : burnMap.entrySet()) {
            writer.name(entry.toString()).value(entry.getValue());
        }
        writer.endObject();
    }

    @Override
    public void deserializeJSON(JsonObject obj) {
        if(obj.has("release")) {
            JsonObject release = obj.get("release").getAsJsonObject();
            for(PollutionHandler.PollutionType type : PollutionHandler.PollutionType.VALUES) {
                if(release.has(type.name())) {
                    releaseMap.put(type, release.get(type.name()).getAsFloat());
                }
            }
        }
        if(obj.has("burn")) {
            JsonObject release = obj.get("burn").getAsJsonObject();
            for(PollutionHandler.PollutionType type : PollutionHandler.PollutionType.VALUES) {
                if(release.has(type.name())) {
                    burnMap.put(type, release.get(type.name()).getAsFloat());
                }
            }
        }
    }

    public static void pollute(World world, int x, int y, int z, FluidType type, FluidReleaseType release, float mB) {
        FT_Polluting trait = type.getTrait(FT_Polluting.class);
        if(trait == null) return;
        if(release == FluidReleaseType.VOID) return;

        HashMap<PollutionHandler.PollutionType, Float> map = release == FluidReleaseType.BURN ? trait.burnMap : trait.releaseMap;

        for(Map.Entry<PollutionHandler.PollutionType, Float> entry : map.entrySet()) {
            PollutionHandler.incrementPollution(world, new BlockPos(x, y, z), entry.getKey(), entry.getValue() * mB);
        }
    }
}
