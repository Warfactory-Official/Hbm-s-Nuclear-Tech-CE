package com.hbm.render.entity.item;

import com.hbm.entity.cart.EntityMinecartCrate;
import com.hbm.interfaces.AutoRegister;

import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderMinecart;
import net.minecraftforge.fml.client.registry.IRenderFactory;

@AutoRegister(entity = EntityMinecartCrate.class, factory = "FACTORY")
public class RenderCartCrate extends RenderMinecart<EntityMinecartCrate> {

    public static final IRenderFactory<EntityMinecartCrate> FACTORY = RenderCartCrate::new;

    public RenderCartCrate(RenderManager renderManager) {
        super(renderManager);
    }
}
