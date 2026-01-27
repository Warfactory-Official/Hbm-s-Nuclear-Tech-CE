package com.hbm.main;

import com.hbm.Tags;
import net.minecraft.advancements.Advancement;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Level;

import java.util.Objects;

public class AdvancementManager {

    public static Advancement achSacrifice;
    public static Advancement achImpossible;
    public static Advancement achTOB;
    public static Advancement achPotato;
    public static Advancement achC20_5;
    public static Advancement achFiend;
    public static Advancement achFiend2;
    public static Advancement achRadPoison;
    public static Advancement achRadDeath;
    public static Advancement achStratum;
    public static Advancement achOmega12;
    public static Advancement achSomeWounds;
    public static Advancement achSlimeball;
    public static Advancement achSulfuric;
    public static Advancement achGoFish;
    public static Advancement achNo9;
    public static Advancement achInferno;
    public static Advancement achRedRoom;
    public static Advancement bobHidden;
    public static Advancement horizonsStart;
    public static Advancement horizonsEnd;
    public static Advancement horizonsBonus;
    public static Advancement bossCreeper;
    public static Advancement bossMeltdown;
    public static Advancement bossMaskman;
    public static Advancement bossWorm;
    public static Advancement bossUFO;
    public static Advancement digammaSee;
    public static Advancement digammaFeel;
    public static Advancement digammaKnow;
    public static Advancement digammaKauaiMoho;
    public static Advancement digammaUpOnTop;

    public static Advancement achBurnerPress;
    public static Advancement achBlastFurnace;
    public static Advancement achAssembly;
    public static Advancement achSelenium;
    public static Advancement achChemplant;
    public static Advancement achConcrete;
    public static Advancement achPolymer;
    public static Advancement achDesh;
    public static Advancement achTantalum;
    public static Advancement achRedBalloons;
    public static Advancement achManhattan;
    public static Advancement achGasCent;
    public static Advancement achCentrifuge;
    public static Advancement achFOEQ;
    public static Advancement achSoyuz;
    public static Advancement achSpace;
    public static Advancement achSchrab;
    public static Advancement achAcidizer;
    public static Advancement achRadium;
    public static Advancement achTechnetium;
    public static Advancement achZIRNOXBoom;
    public static Advancement achChicagoPile;
    public static Advancement achSILEX;
    public static Advancement achWatz;
    public static Advancement achWatzBoom;
    public static Advancement achRBMK;
    public static Advancement achRBMKBoom;
    public static Advancement achBismuth;
    public static Advancement achBreeding;
    public static Advancement achFusion;
    public static Advancement achMeltdown;

    public static Advancement progress_dfc;
    public static Advancement root;

    private static Advancement load(net.minecraft.advancements.AdvancementManager adv, String path) {
        ResourceLocation id = new ResourceLocation(Tags.MODID, path);
        return Objects.requireNonNull(adv.getAdvancement(id), "Missing advancement: " + id);
    }

    public static void init(MinecraftServer serv) {
        net.minecraft.advancements.AdvancementManager adv = serv.getAdvancementManager();

        achSacrifice  = load(adv, "achsacrifice");
        achImpossible = load(adv, "achimpossible");
        achTOB        = load(adv, "achtob");
        achGoFish     = load(adv, "achgofish");
        achPotato     = load(adv, "achpotato");
        achC20_5      = load(adv, "achc20_5");
        achFiend      = load(adv, "achfiend");
        achFiend2     = load(adv, "achfiend2");
        achStratum    = load(adv, "achstratum");
        achOmega12    = load(adv, "achomega12");

        achNo9        = load(adv, "achno9");
        achSlimeball  = load(adv, "achslimeball");
        achSulfuric   = load(adv, "achsulfuric");
        achInferno    = load(adv, "achinferno");
        achRedRoom    = load(adv, "achredroom");

        bobHidden     = load(adv, "bobhidden");

        horizonsStart = load(adv, "horizonsstart");
        horizonsEnd   = load(adv, "horizonsend");
        horizonsBonus = load(adv, "horizonsbonus");

        bossCreeper   = load(adv, "bosscreeper");
        bossMeltdown  = load(adv, "bossmeltdown");
        bossMaskman   = load(adv, "bossmaskman");
        bossWorm      = load(adv, "bossworm");
        bossUFO       = load(adv, "bossufo");

        achRadPoison  = load(adv, "achradpoison");
        achRadDeath   = load(adv, "achraddeath");

        achSomeWounds = load(adv, "achsomewounds");

        digammaSee       = load(adv, "digammasee");
        digammaFeel      = load(adv, "digammafeel");
        digammaKnow      = load(adv, "digammaknow");
        digammaKauaiMoho = load(adv, "digammakauaimoho");
        digammaUpOnTop   = load(adv, "digammaupontop");

        // Progression
        achBurnerPress  = load(adv, "achburnerpress");
        achBlastFurnace = load(adv, "achblastfurnace");
        achAssembly     = load(adv, "achassembly");
        achSelenium     = load(adv, "achselenium");
        achChemplant    = load(adv, "achchemplant");
        achConcrete     = load(adv, "achconcrete");
        achPolymer      = load(adv, "achpolymer");
        achDesh         = load(adv, "achdesh");
        achTantalum     = load(adv, "achtantalum");
        achGasCent      = load(adv, "achgascent");
        achCentrifuge   = load(adv, "achcentrifuge");
        achFOEQ         = load(adv, "achfoeq");
        achSoyuz        = load(adv, "achsoyuz");
        achSpace        = load(adv, "achspace");
        achSchrab       = load(adv, "achschrab");
        achAcidizer     = load(adv, "achacidizer");
        achRadium       = load(adv, "achradium");
        achTechnetium   = load(adv, "achtechnetium");
        achZIRNOXBoom   = load(adv, "achzirnoxboom");
        achChicagoPile  = load(adv, "achchicagopile");
        achSILEX        = load(adv, "achsilex");
        achWatz         = load(adv, "achwatz");
        achWatzBoom     = load(adv, "achwatzboom");
        achRBMK         = load(adv, "achrbmk");
        achRBMKBoom     = load(adv, "achrbmkboom");
        achBismuth      = load(adv, "achbismuth");
        achBreeding     = load(adv, "achbreeding");
        achFusion       = load(adv, "achfusion");
        achMeltdown     = load(adv, "achmeltdown");
        achRedBalloons  = load(adv, "achredballoons");
        achManhattan    = load(adv, "achmanhattan");

        progress_dfc = load(adv, "progress_dfc"); // 1.12.2 exclusive, kept because why not?
        // TODO: Maybe add an achievement for SAFE
        // not really, it got removed
        root = load(adv, "root"); // 1.12.2 Root advancement
    }

    public static void grantAchievement(EntityPlayerMP player, Advancement a) {
        if (a == null) {
            MainRegistry.logger.log(Level.ERROR, "Failed to grant null advancement! This should never happen.");
            return;
        }
        for (String s : player.getAdvancements().getProgress(a).getRemaningCriteria()) {
            player.getAdvancements().grantCriterion(a, s);
        }
    }

    @Deprecated
    public static void grantAchievement(EntityPlayer player, Advancement a) {
        if (player instanceof EntityPlayerMP) grantAchievement((EntityPlayerMP) player, a);
    }

    public static boolean hasAdvancement(EntityPlayerMP playerMP, Advancement a) {
        if (a == null) {
            MainRegistry.logger.log(Level.ERROR, "Failed to test null advancement! This should never happen.");
            return false;
        }
        return playerMP.getAdvancements().getProgress(a).isDone();
    }
}
