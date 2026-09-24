package com.hbm.handler;

import com.hbm.blocks.generic.BlockBobble.BobbleType;
import com.hbm.render.skinlayer.MojangSkinLoader;

public final class ClientHttpHandler {

    private ClientHttpHandler() {
    }

    public static void preinit() {
        for (BobbleType type : BobbleType.VALUES) {
            if (type.skinUuid != null) {
                MojangSkinLoader.preload(type.skinUuid);
            }
        }
    }
}
