package com.hbm.saveddata.satellites;

import com.hbm.itempool.ItemPoolsSatellite;
import com.hbm.items.machine.ItemSatellite;
import com.hbm.items.machine.ItemSatellite.EnumSatType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class SatelliteLunarMiner extends SatelliteMiner {

    @Override public String getType() { return "LUNAR_MINER"; }

    @Override
    public ITextComponent[] getInfo(World world) {
        return new ITextComponent[] {
                new TextComponentTranslation(ItemSatellite.make(EnumSatType.MINER_LUNAR).getTranslationKey() + ".name"),
                new TextComponentTranslation("satellite.minerprogress", (int) Math.round(this.progress * 100) + "%")
        };
    }

    static {
        registerCargo(SatelliteLunarMiner.class, ItemPoolsSatellite.POOL_SAT_LUNAR);
    }
}
