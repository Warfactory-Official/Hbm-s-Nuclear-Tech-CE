package com.hbm.crafting;

import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ItemEnums;
import com.hbm.items.ModItems;
import com.hbm.items.machine.*;
import com.hbm.main.CraftingManager;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.oredict.OreDictionary;

import static com.hbm.inventory.OreDictManager.*;

/**
 * For the loading and unloading of fuel rods
 * @author hbm
 */
public class RodRecipes {

    public static void register() {

        //Zirnox Fuel
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.rod_zirnox_empty, 4), "Z Z", "ZBZ", "Z Z", 'Z', ZR.nugget(), 'B', BE.ingot() );
        addZIRNOXRod(U, ItemZirnoxRod.EnumZirnoxType.NATURAL_URANIUM_FUEL);
        addZIRNOXRod(ModItems.billet_uranium_fuel, ItemZirnoxRod.EnumZirnoxType.URANIUM_FUEL);
        addZIRNOXRod(TH232, ItemZirnoxRod.EnumZirnoxType.TH232_FUEL);
        addZIRNOXRod(ModItems.billet_thorium_fuel, ItemZirnoxRod.EnumZirnoxType.THORIUM_FUEL);
        addZIRNOXRod(ModItems.billet_mox_fuel, ItemZirnoxRod.EnumZirnoxType.MOX_FUEL);
        addZIRNOXRod(ModItems.billet_plutonium_fuel, ItemZirnoxRod.EnumZirnoxType.PLUTONIUM_FUEL);
        addZIRNOXRod(U233, ItemZirnoxRod.EnumZirnoxType.U233_FUEL);
        addZIRNOXRod(U235, ItemZirnoxRod.EnumZirnoxType.U235_FUEL);
        addZIRNOXRod(ModItems.billet_les, ItemZirnoxRod.EnumZirnoxType.LES_FUEL);
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.rod_zirnox, 1, ItemZirnoxRod.EnumZirnoxType.LITHIUM_FUEL.ordinal()), ModItems.rod_zirnox_empty, LI.ingot(), LI.ingot() );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.rod_zirnox, 1, ItemZirnoxRod.EnumZirnoxType.ZFB_MOX_FUEL.ordinal()), ModItems.rod_zirnox_empty, ModItems.billet_mox_fuel, ZR.billet() );

        CraftingManager.addShapelessAuto(new ItemStack(ModItems.waste_natural_uranium, 2, 1), new ItemStack(ModItems.rod_zirnox_depleted, 1, ItemZirnoxRodDepleted.EnumZirnoxTypeDepleted.NATURAL_URANIUM_FUEL.ordinal()) );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.waste_uranium, 2, 1), new ItemStack(ModItems.rod_zirnox_depleted, 1, ItemZirnoxRodDepleted.EnumZirnoxTypeDepleted.URANIUM_FUEL.ordinal()) );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.waste_thorium, 2, 1), new ItemStack(ModItems.rod_zirnox_depleted, 1, ItemZirnoxRodDepleted.EnumZirnoxTypeDepleted.THORIUM_FUEL.ordinal()) );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.waste_mox, 2, 1), new ItemStack(ModItems.rod_zirnox_depleted, 1, ItemZirnoxRodDepleted.EnumZirnoxTypeDepleted.MOX_FUEL.ordinal()) );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.waste_plutonium, 2, 1), new ItemStack(ModItems.rod_zirnox_depleted, 1, ItemZirnoxRodDepleted.EnumZirnoxTypeDepleted.PLUTONIUM_FUEL.ordinal()) );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.waste_u233, 2, 1), new ItemStack(ModItems.rod_zirnox_depleted, 1, ItemZirnoxRodDepleted.EnumZirnoxTypeDepleted.U233_FUEL.ordinal()) );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.waste_u235, 2, 1), new ItemStack(ModItems.rod_zirnox_depleted, 1, ItemZirnoxRodDepleted.EnumZirnoxTypeDepleted.U235_FUEL.ordinal()) );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.waste_schrabidium, 2, 1), new ItemStack(ModItems.rod_zirnox_depleted, 1, ItemZirnoxRodDepleted.EnumZirnoxTypeDepleted.LES_FUEL.ordinal()) );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.waste_zfb_mox, 2, 1), new ItemStack(ModItems.rod_zirnox_depleted, 1, ItemZirnoxRodDepleted.EnumZirnoxTypeDepleted.ZFB_MOX_FUEL.ordinal()) );

        //Breeding Rods
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.rod_empty, 16), "SSS", "L L", "SSS", 'S', STEEL.plate528(), 'L', PB.plate528() );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.rod_empty, 2), ModItems.rod_dual_empty );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.rod_dual_empty, 1), ModItems.rod_empty, ModItems.rod_empty );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.rod_empty, 4), ModItems.rod_quad_empty );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.rod_quad_empty, 1), ModItems.rod_empty, ModItems.rod_empty, ModItems.rod_empty, ModItems.rod_empty );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.rod_quad_empty, 1), ModItems.rod_dual_empty, ModItems.rod_dual_empty );

        CraftingManager.addShapelessAuto(new ItemStack(ModItems.rod, 1, ItemBreedingRod.BreedingRodType.LITHIUM.ordinal()), ModItems.rod_empty, LI.ingot() );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.lithium, 1), new ItemStack(ModItems.rod, 1, ItemBreedingRod.BreedingRodType.LITHIUM.ordinal()) );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.rod_dual, 1, ItemBreedingRod.BreedingRodType.LITHIUM.ordinal()), ModItems.rod_dual_empty, LI.ingot(), LI.ingot() );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.lithium, 2), new ItemStack(ModItems.rod_dual, 1, ItemBreedingRod.BreedingRodType.LITHIUM.ordinal()) );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.rod_quad, 1, ItemBreedingRod.BreedingRodType.LITHIUM.ordinal()), ModItems.rod_quad_empty, LI.ingot(), LI.ingot(), LI.ingot(), LI.ingot() );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.lithium, 4), new ItemStack(ModItems.rod_quad, 1, ItemBreedingRod.BreedingRodType.LITHIUM.ordinal()) );

        CraftingManager.addShapelessAuto(new ItemStack(ModItems.cell, 1, Fluids.TRITIUM.getID()), new ItemStack(ModItems.rod, 1, ItemBreedingRod.BreedingRodType.TRITIUM.ordinal()), new ItemStack(ModItems.cell));
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.cell, 2, Fluids.TRITIUM.getID()), new ItemStack(ModItems.rod_dual, 1, ItemBreedingRod.BreedingRodType.TRITIUM.ordinal()), new ItemStack(ModItems.cell), new ItemStack(ModItems.cell) );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.cell, 4, Fluids.TRITIUM.getID()), new ItemStack(ModItems.rod_quad, 1, ItemBreedingRod.BreedingRodType.TRITIUM.ordinal()), new ItemStack(ModItems.cell), new ItemStack(ModItems.cell), new ItemStack(ModItems.cell), new ItemStack(ModItems.cell) );

        addBreedingRod(CO, ModItems.billet_cobalt, ItemBreedingRod.BreedingRodType.CO);
        addBreedingRod(CO60, ModItems.billet_co60, ItemBreedingRod.BreedingRodType.CO60);
        addBreedingRod(RA226, ModItems.billet_ra226, ItemBreedingRod.BreedingRodType.RA226);
        addBreedingRod(AC227, ModItems.billet_actinium, ItemBreedingRod.BreedingRodType.AC227);
        addBreedingRod(TH232, ModItems.billet_th232, ItemBreedingRod.BreedingRodType.TH232);
        addBreedingRod(ModItems.billet_thorium_fuel, ItemBreedingRod.BreedingRodType.THF);
        addBreedingRod(U235, ModItems.billet_u235, ItemBreedingRod.BreedingRodType.U235);
        addBreedingRod(NP237, ModItems.billet_neptunium, ItemBreedingRod.BreedingRodType.NP237);
        addBreedingRod(U238, ModItems.billet_u238, ItemBreedingRod.BreedingRodType.U238);
        addBreedingRod(PU238, ModItems.billet_pu238, ItemBreedingRod.BreedingRodType.PU238);
        addBreedingRod(PU239, ModItems.billet_pu239, ItemBreedingRod.BreedingRodType.PU239);
        addBreedingRod(ModItems.billet_pu_mix, ItemBreedingRod.BreedingRodType.RGP);
        addBreedingRod(ModItems.billet_nuclear_waste, ItemBreedingRod.BreedingRodType.WASTE);
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.rod, 1, ItemBreedingRod.BreedingRodType.LEAD.ordinal()), ModItems.rod_empty, PB.nugget(), PB.nugget(), PB.nugget(), PB.nugget(), PB.nugget(), PB.nugget() );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.nugget_lead, 6), new ItemStack(ModItems.rod, 1, ItemBreedingRod.BreedingRodType.LEAD.ordinal()) );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.rod_dual, 1, ItemBreedingRod.BreedingRodType.LEAD.ordinal()), ModItems.rod_dual_empty, PB.ingot(), PB.nugget(), PB.nugget(), PB.nugget() );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.nugget_lead, 12), new ItemStack(ModItems.rod_dual, 1, ItemBreedingRod.BreedingRodType.LEAD.ordinal()) );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.rod_quad, 1, ItemBreedingRod.BreedingRodType.LEAD.ordinal()), ModItems.rod_quad_empty, PB.ingot(), PB.ingot(), PB.nugget(), PB.nugget(), PB.nugget(), PB.nugget(), PB.nugget(), PB.nugget() );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.nugget_lead, 24), new ItemStack(ModItems.rod_quad, 1, ItemBreedingRod.BreedingRodType.LEAD.ordinal()) );
        addBreedingRod(U, ModItems.billet_uranium, ItemBreedingRod.BreedingRodType.URANIUM);


        //Pile fuel
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.pile_rod_uranium, 1), " U ", "PUP", " U ", 'P', IRON.plate(), 'U', U.billet() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.pile_rod_source, 1), " U ", "PUP", " U ", 'P', IRON.plate(), 'U', ModItems.billet_ra226be );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.pile_rod_boron, 1), " B ", " W ", " B ", 'B', B.ingot(), 'W', KEY_PLANKS );
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.pile_rod_lithium, 1), new ItemStack(ModItems.cell), LI.ingot() );
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.pile_rod_detector, 1), " B ", "CM ", " B ", 'B', B.ingot(), 'C', DictFrame.fromOne(ModItems.circuit, ItemEnums.EnumCircuitType.VACUUM_TUBE), 'M', ModItems.motor );

        CraftingManager.addRecipeAuto(new ItemStack(ModItems.rbmk_fuel_empty, 1), "ZRZ", "Z Z", "ZRZ", 'Z', ZR.ingot(), 'R', ModItems.rod_quad_empty );
        addRBMKRod(U, ModItems.rbmk_fuel_ueu);
        addRBMKRod(ModItems.billet_uranium_fuel, ModItems.rbmk_fuel_meu);
        addRBMKRod(U233, ModItems.rbmk_fuel_heu233);
        addRBMKRod(U235, ModItems.rbmk_fuel_heu235);
        addRBMKRod(ModItems.billet_thorium_fuel, ModItems.rbmk_fuel_thmeu);
        addRBMKRod(ModItems.billet_mox_fuel, ModItems.rbmk_fuel_mox);
        addRBMKRod(ModItems.billet_plutonium_fuel, ModItems.rbmk_fuel_lep);
        addRBMKRod(PURG, ModItems.rbmk_fuel_mep);
        addRBMKRod(PU239, ModItems.rbmk_fuel_hep239);
        addRBMKRod(PU241, ModItems.rbmk_fuel_hep241);
        addRBMKRod(ModItems.billet_americium_fuel, ModItems.rbmk_fuel_lea);
        addRBMKRod(AMRG, ModItems.rbmk_fuel_mea);
        addRBMKRod(AM241, ModItems.rbmk_fuel_hea241);
        addRBMKRod(AM242, ModItems.rbmk_fuel_hea242);
        addRBMKRod(ModItems.billet_neptunium_fuel, ModItems.rbmk_fuel_men);
        addRBMKRod(NP237, ModItems.rbmk_fuel_hen);
        addRBMKRod(ModItems.billet_po210be, ModItems.rbmk_fuel_po210be);
        addRBMKRod(ModItems.billet_ra226be, ModItems.rbmk_fuel_ra226be);
        addRBMKRod(ModItems.billet_pu238be, ModItems.rbmk_fuel_pu238be);
        addRBMKRod(ModItems.billet_australium_lesser, ModItems.rbmk_fuel_leaus);
        addRBMKRod(ModItems.billet_australium_greater, ModItems.rbmk_fuel_heaus);
        addRBMKRod(ModItems.egg_balefire_shard, ModItems.rbmk_fuel_balefire);
        addRBMKRod(ModItems.billet_les, ModItems.rbmk_fuel_les);
        addRBMKRod(ModItems.billet_schrabidium_fuel, ModItems.rbmk_fuel_mes);
        addRBMKRod(ModItems.billet_hes, ModItems.rbmk_fuel_hes);
        addRBMKRod(ModItems.billet_balefire_gold, ModItems.rbmk_fuel_balefire_gold);
        addRBMKRod(ModItems.billet_flashlead, ModItems.rbmk_fuel_flashlead);
        addRBMKRod(ModItems.billet_zfb_bismuth, ModItems.rbmk_fuel_zfb_bismuth);
        addRBMKRod(ModItems.billet_zfb_pu241, ModItems.rbmk_fuel_zfb_pu241);
        addRBMKRod(ModItems.billet_zfb_am_mix, ModItems.rbmk_fuel_zfb_am_mix);
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.rbmk_fuel_drx, 1), ModItems.rbmk_fuel_balefire, ModItems.particle_digamma );

        addPellet(SA326,							ItemWatzPellet.EnumWatzType.SCHRABIDIUM);
        addPellet(ModItems.ingot_hes,				ItemWatzPellet.EnumWatzType.HES);
        addPellet(ModItems.ingot_schrabidium_fuel,	ItemWatzPellet.EnumWatzType.MES);
        addPellet(ModItems.ingot_les,				ItemWatzPellet.EnumWatzType.LES);
        addPellet(NP237,							ItemWatzPellet.EnumWatzType.HEN);
        addPellet(ModItems.ingot_uranium_fuel,		ItemWatzPellet.EnumWatzType.MEU);
        addPellet(ModItems.ingot_pu_mix,			ItemWatzPellet.EnumWatzType.MEP);
        addPellet(PB,								ItemWatzPellet.EnumWatzType.LEAD);
        addPellet(B,								ItemWatzPellet.EnumWatzType.BORON);
        addPellet(U238,								ItemWatzPellet.EnumWatzType.DU);

        //PWR fuel
        CraftingManager.addRecipeAuto(DictFrame.fromOne(ModItems.pwr_fuel, ItemPWRFuel.EnumPWRFuel.MEU), "F", "I", "F", 'F', ModItems.billet_uranium_fuel, 'I', ModItems.plate_polymer );
        CraftingManager.addRecipeAuto(DictFrame.fromOne(ModItems.pwr_fuel, ItemPWRFuel.EnumPWRFuel.HEU233), "F", "I", "F", 'F', U233.billet(), 'I', ModItems.plate_polymer );
        CraftingManager.addRecipeAuto(DictFrame.fromOne(ModItems.pwr_fuel, ItemPWRFuel.EnumPWRFuel.HEU235), "F", "I", "F", 'F', U235.billet(), 'I', ModItems.plate_polymer );
        CraftingManager.addRecipeAuto(DictFrame.fromOne(ModItems.pwr_fuel, ItemPWRFuel.EnumPWRFuel.MEN), "F", "I", "F", 'F', ModItems.billet_neptunium_fuel, 'I', ModItems.plate_polymer );
        CraftingManager.addRecipeAuto(DictFrame.fromOne(ModItems.pwr_fuel, ItemPWRFuel.EnumPWRFuel.HEN237), "F", "I", "F", 'F', NP237.billet(), 'I', ModItems.plate_polymer );
        CraftingManager.addRecipeAuto(DictFrame.fromOne(ModItems.pwr_fuel, ItemPWRFuel.EnumPWRFuel.MOX), "F", "I", "F", 'F', ModItems.billet_mox_fuel, 'I', ModItems.plate_polymer );
        CraftingManager.addRecipeAuto(DictFrame.fromOne(ModItems.pwr_fuel, ItemPWRFuel.EnumPWRFuel.MEP), "F", "I", "F", 'F', ModItems.billet_pu_mix, 'I', ModItems.plate_polymer );
        CraftingManager.addRecipeAuto(DictFrame.fromOne(ModItems.pwr_fuel, ItemPWRFuel.EnumPWRFuel.HEP239), "F", "I", "F", 'F', PU239.billet(), 'I', ModItems.plate_polymer );
        CraftingManager.addRecipeAuto(DictFrame.fromOne(ModItems.pwr_fuel, ItemPWRFuel.EnumPWRFuel.HEP241), "F", "I", "F", 'F', PU241.billet(), 'I', ModItems.plate_polymer );
        CraftingManager.addRecipeAuto(DictFrame.fromOne(ModItems.pwr_fuel, ItemPWRFuel.EnumPWRFuel.MEA), "F", "I", "F", 'F', ModItems.billet_am_mix, 'I', ModItems.plate_polymer );
        CraftingManager.addRecipeAuto(DictFrame.fromOne(ModItems.pwr_fuel, ItemPWRFuel.EnumPWRFuel.HEA242), "F", "I", "F", 'F', AM242.billet(), 'I', ModItems.plate_polymer );
        CraftingManager.addRecipeAuto(DictFrame.fromOne(ModItems.pwr_fuel, ItemPWRFuel.EnumPWRFuel.HES326), "F", "I", "F", 'F', SA326.billet(), 'I', ModItems.plate_polymer );
        CraftingManager.addRecipeAuto(DictFrame.fromOne(ModItems.pwr_fuel, ItemPWRFuel.EnumPWRFuel.HES327), "F", "I", "F", 'F', SA327.billet(), 'I', ModItems.plate_polymer );
        CraftingManager.addRecipeAuto(DictFrame.fromOne(ModItems.pwr_fuel, ItemPWRFuel.EnumPWRFuel.BFB_AM_MIX), "NFN", "NIN", "NBN", 'F', ModItems.billet_am_mix, 'I', ModItems.plate_polymer, 'B', BI.billet(), 'N', ModItems.nugget_plutonium_fuel );
        CraftingManager.addRecipeAuto(DictFrame.fromOne(ModItems.pwr_fuel, ItemPWRFuel.EnumPWRFuel.BFB_PU241), "NFN", "NIN", "NBN", 'F', PU241.billet(), 'I', ModItems.plate_polymer, 'B', BI.billet(), 'N', ModItems.nugget_uranium_fuel );

        CraftingManager.addRecipeAuto(new ItemStack(ModItems.icf_pellet_empty), "ZLZ", "L L", "ZLZ", 'Z', ZR.wireFine(), 'L', PB.wireFine() );
    }
    // TODO ? ids are not correct, resulting in NPE
    public static void registerInit() {
        /* GT6 */
        if(OreDictionary.doesOreNameExist("ingotNaquadah-Enriched"))	addPellet(new DictFrame("Naquadah-Enriched"),	ItemWatzPellet.EnumWatzType.NQD);
        if(OreDictionary.doesOreNameExist("ingotNaquadria"))			addPellet(new DictFrame("Naquadria"),			ItemWatzPellet.EnumWatzType.NQR);
    }

    //Fill rods with one billet. For fuels only, therefore no unloading or ore dict
    public static void addFuelRodBillet(Item billet, Item out) {
        CraftingManager.addShapelessAuto(new ItemStack(out), ModItems.rod_empty, billet );
    }

    //Fill rods with two billets
    public static void addDualFuelRodBillet(Item billet, Item out) {
        CraftingManager.addShapelessAuto(new ItemStack(out), ModItems.rod_dual_empty, billet, billet );
    }

    //Fill rods with three billets
    public static void addQuadFuelRodBillet(Item billet, Item out) {
        CraftingManager.addShapelessAuto(new ItemStack(out), ModItems.rod_quad_empty, billet, billet, billet, billet );
    }

    //Fill rods with one billet + unload
    public static void addRodBilletUnload(Item billet, Item out) {
        CraftingManager.addShapelessAuto(new ItemStack(out), ModItems.rod_empty, billet );
        CraftingManager.addShapelessAuto(new ItemStack(billet, 1), out );
    }
    public static void addRodBilletUnload(DictFrame mat, Item billet, Item out) {
        CraftingManager.addShapelessAuto(new ItemStack(out), ModItems.rod_empty, mat.billet() );
        CraftingManager.addShapelessAuto(new ItemStack(billet, 1), out );
    }

    //Fill rods with two billets + unload
    public static void addDualRodBilletUnload(Item billet, Item out) {
        CraftingManager.addShapelessAuto(new ItemStack(out), ModItems.rod_dual_empty, billet, billet );
        CraftingManager.addShapelessAuto(new ItemStack(billet, 2), out );
    }
    public static void addDualRodBilletUnload(DictFrame mat, Item billet, Item out) {
        CraftingManager.addShapelessAuto(new ItemStack(out), ModItems.rod_dual_empty, mat.billet(), mat.billet() );
        CraftingManager.addShapelessAuto(new ItemStack(billet, 2), out );
    }

    //Fill rods with three billets + unload
    public static void addQuadRodBilletUnload(Item billet, Item out) {
        CraftingManager.addShapelessAuto(new ItemStack(out), ModItems.rod_quad_empty, billet, billet, billet, billet );
        CraftingManager.addShapelessAuto(new ItemStack(billet, 4), out );
    }
    public static void addQuadRodBilletUnload(DictFrame mat, Item billet, Item out) {
        CraftingManager.addShapelessAuto(new ItemStack(out), ModItems.rod_quad_empty, mat.billet(), mat.billet(), mat.billet(), mat.billet() );
        CraftingManager.addShapelessAuto(new ItemStack(billet, 4), out );
    }

    /** Single, dual, quad rod loading + unloading **/
    public static void addBreedingRod(Item billet, ItemBreedingRod.BreedingRodType type) {
        addBreedingRodLoad(billet, type);
        addBreedingRodUnload(billet, type);
    }
    /** Single, dual, quad rod loading + unloading + oredict **/
    public static void addBreedingRod(DictFrame mat, Item billet, ItemBreedingRod.BreedingRodType type) {
        addBreedingRodLoad(mat, billet, type);
        addBreedingRodUnload(mat, billet, type);
    }

    /** Single, dual, quad rod loading **/
    public static void addBreedingRodLoad(Item billet, ItemBreedingRod.BreedingRodType type) {
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.rod, 1, type.ordinal()), ModItems.rod_empty, billet);
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.rod_dual, 1, type.ordinal()), ModItems.rod_dual_empty, billet, billet);
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.rod_quad, 1, type.ordinal()), ModItems.rod_quad_empty, billet, billet, billet, billet);
    }
    /** Single, dual, quad rod unloading **/
    public static void addBreedingRodUnload(Item billet, ItemBreedingRod.BreedingRodType type) {
        CraftingManager.addShapelessAuto(new ItemStack(billet, 1), new ItemStack(ModItems.rod, 1, type.ordinal()) );
        CraftingManager.addShapelessAuto(new ItemStack(billet, 2), new ItemStack(ModItems.rod_dual, 1, type.ordinal()) );
        CraftingManager.addShapelessAuto(new ItemStack(billet, 4), new ItemStack(ModItems.rod_quad, 1, type.ordinal()) );
    }
    /** Single, dual, quad rod loading with OreDict **/
    public static void addBreedingRodLoad(DictFrame mat, Item billet, ItemBreedingRod.BreedingRodType type) {
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.rod, 1, type.ordinal()), ModItems.rod_empty, mat.billet());
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.rod_dual, 1, type.ordinal()), ModItems.rod_dual_empty, mat.billet(), mat.billet());
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.rod_quad, 1, type.ordinal()), ModItems.rod_quad_empty, mat.billet(), mat.billet(), mat.billet(), mat.billet());
    }
    /** Single, dual, quad rod unloading with OreDict **/
    public static void addBreedingRodUnload(DictFrame mat, Item billet, ItemBreedingRod.BreedingRodType type) {
        CraftingManager.addShapelessAuto(new ItemStack(billet, 1), new ItemStack(ModItems.rod, 1, type.ordinal()) );
        CraftingManager.addShapelessAuto(new ItemStack(billet, 2), new ItemStack(ModItems.rod_dual, 1, type.ordinal()) );
        CraftingManager.addShapelessAuto(new ItemStack(billet, 4), new ItemStack(ModItems.rod_quad, 1, type.ordinal()) );
    }

    //Fill rods with 8 billets
    public static void addRBMKRod(DictFrame mat, Item out) {
        CraftingManager.addShapelessAuto(new ItemStack(out), ModItems.rbmk_fuel_empty, mat.billet(), mat.billet(), mat.billet(), mat.billet(), mat.billet(), mat.billet(), mat.billet(), mat.billet() );
    }
    public static void addRBMKRod(Item billet, Item out) {
        CraftingManager.addShapelessAuto(new ItemStack(out), ModItems.rbmk_fuel_empty, billet, billet, billet, billet, billet, billet, billet, billet );
    }

    /** Fill ZIRNOX rod with two billets **/
    public static void addZIRNOXRod(Item billet, ItemZirnoxRod.EnumZirnoxType num) {
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.rod_zirnox, 1, num.ordinal()), ModItems.rod_zirnox_empty, billet, billet );
    }

    /** Fill ZIRNOX rod with two billets with OreDict **/
    public static void addZIRNOXRod(DictFrame mat, ItemZirnoxRod.EnumZirnoxType num) {
        CraftingManager.addShapelessAuto(new ItemStack(ModItems.rod_zirnox, 1, num.ordinal()), ModItems.rod_zirnox_empty, mat.billet(), mat.billet() );
    }

    /** Watz pellet crafting **/
    public static void addPellet(DictFrame mat, ItemWatzPellet.EnumWatzType num) {
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.watz_pellet, 1, num.ordinal()), " I ", "IGI", " I ", 'I', mat.ingot(), 'G', GRAPHITE.ingot() );
    }
    public static void addPellet(Item item, ItemWatzPellet.EnumWatzType num) {
        CraftingManager.addRecipeAuto(new ItemStack(ModItems.watz_pellet, 1, num.ordinal()), " I ", "IGI", " I ", 'I', item, 'G', GRAPHITE.ingot() );
    }
}
