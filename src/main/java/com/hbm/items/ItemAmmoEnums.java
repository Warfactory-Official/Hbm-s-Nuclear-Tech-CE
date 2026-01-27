package com.hbm.items;

import com.google.common.collect.ImmutableSet;
import com.hbm.items.weapon.ItemAmmo;

import java.util.Set;

public class ItemAmmoEnums {

    public enum AmmoFireExt implements IAmmoItemEnum {
        WATER("ammo_fireext"),
        FOAM("ammo_fireext_foam"),
        SAND("ammo_fireext_sand");

        public static final AmmoFireExt[] VALUES = values();

        private final Set<ItemAmmo.AmmoItemTrait> traits;
        private final String unloc;

        private AmmoFireExt(String unloc, ItemAmmo.AmmoItemTrait... traits) {
            this.traits = safeAssign(traits);
            this.unloc = unloc;
        }

        @Override
        public Set<ItemAmmo.AmmoItemTrait> getTraits() {
            return traits;
        }

        @Override
        public String getInternalName() {
            return unloc;
        }
    }

    public enum AmmoMisc implements IAmmoItemEnum {
        //LUNA_SNIPER("ammo_lunar", Gun50BMGFactory.getLunaticSabotRound(), AmmoItemTrait.PRO_HEAVY_DAMAGE, AmmoItemTrait.PRO_ACCURATE2, AmmoItemTrait.NEU_HEAVY_METAL),
        DGK("ammo_dkg");

        public static final AmmoMisc[] VALUES = values();

        private final Set<ItemAmmo.AmmoItemTrait> traits;
        private final String unloc;

        private AmmoMisc(String unloc, ItemAmmo.AmmoItemTrait... traits) {
            this.traits = safeAssign(traits);
            this.unloc = unloc;
        }

        @Override
        public Set<ItemAmmo.AmmoItemTrait> getTraits() {
            return traits;
        }

        @Override
        public String getInternalName() {
            return unloc;
        }
    }

    public enum Ammo240Shell implements IAmmoItemEnum {
        STOCK("ammo_shell"),
        EXPLOSIVE("ammo_shell_explosive"),
        APFSDS_T("ammo_shell_apfsds_t"),
        APFSDS_DU("ammo_shell_apfsds_du"),
        W9("ammo_shell_w9");

        public static final Ammo240Shell[] VALUES = values();

        private final Set<ItemAmmo.AmmoItemTrait> traits;
        private final String unloc;

        private Ammo240Shell(String unloc, ItemAmmo.AmmoItemTrait... traits) {
            this.traits = safeAssign(traits);
            this.unloc = unloc;
        }

        @Override
        public Set<ItemAmmo.AmmoItemTrait> getTraits() {
            return traits;
        }

        @Override
        public String getInternalName() {
            return unloc;
        }
    }

    public interface IAmmoItemEnum {
        public Set<ItemAmmo.AmmoItemTrait> getTraits();
        public String getInternalName();
    }

    static Set<ItemAmmo.AmmoItemTrait> safeAssign(ItemAmmo.AmmoItemTrait[] traits) {
        return traits == null ? ImmutableSet.of() : ImmutableSet.copyOf(traits);
    }

}
