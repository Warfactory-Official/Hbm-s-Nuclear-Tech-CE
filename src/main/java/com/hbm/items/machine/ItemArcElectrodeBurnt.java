package com.hbm.items.machine;

import com.hbm.items.ItemEnumMulti;

public class ItemArcElectrodeBurnt extends ItemEnumMulti<ItemArcElectrode.EnumElectrodeType> {

    public ItemArcElectrodeBurnt(String s) {
        super(s, ItemArcElectrode.EnumElectrodeType.VALUES, true, true);
        this.setFull3D();
    }
}
