package com.hbm.items.weapon.sedna.mods;

import com.hbm.items.weapon.sedna.Receiver;
import com.hbm.items.weapon.sedna.mags.MagazineEnergy;
import com.hbm.items.weapon.sedna.mags.MagazineFluid;
import net.minecraft.item.ItemStack;

public class WeaponModCanisters extends WeaponModBase {

    protected static MagazineFluid DUMMY_LIQUID = new MagazineFluid(0, 0);
    protected static MagazineEnergy DUMMY_ELECTRIC = new MagazineEnergy(0, 0);

    public WeaponModCanisters(int id) {
        super(id, "CANISTERS");
        this.setPriority(PRIORITY_MULT_FINAL);
    }

    @Override
    public <T> T eval(T base, ItemStack gun, String key, Object parent) {
        if(key == Receiver.O_MAGAZINE) {
            if(base instanceof MagazineFluid) {
                MagazineFluid original = (MagazineFluid) base;
                DUMMY_LIQUID.acceptedTypes = original.acceptedTypes;
                DUMMY_LIQUID.capacity = original.capacity * 3;
                DUMMY_LIQUID.index = original.index;
                return (T) DUMMY_LIQUID;
            }

            if(base instanceof MagazineEnergy) {
                MagazineEnergy original = (MagazineEnergy) base;
                DUMMY_ELECTRIC.capacity = original.capacity * 3;
                DUMMY_ELECTRIC.index = original.index;
                return (T) DUMMY_ELECTRIC;
            }
        }
        return base;
    }

    @Override public void onInstall(ItemStack gun, ItemStack mod, int index) { WeaponModManager.changedMagState(); }
    @Override public void onUninstall(ItemStack gun, ItemStack mod, int index) { WeaponModManager.changedMagState(); }
}