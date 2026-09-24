package com.hbm.entity.cart;

import com.hbm.interfaces.AutoRegister;
import com.hbm.items.tool.ItemModMinecart;
import com.hbm.items.tool.ItemModMinecart.EnumCartBase;
import com.hbm.items.tool.ItemModMinecart.EnumMinecart;
import com.hbm.main.ResourceManager;
import com.hbm.render.entity.item.RenderNeoCart;

import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

@AutoRegister(name = "entity_ntm_cart_powder", trackingRange = 250, sendVelocityUpdates = false)
public class EntityMinecartPowder extends EntityMinecartNTM {

    public EntityMinecartPowder(World world) {
        super(world);
    }

    public EntityMinecartPowder(World world, double x, double y, double z, EnumCartBase type) {
        super(world, x, y, z, type);
    }

    @Override
    public @NotNull ItemStack getCartItem() {
        return ItemModMinecart.createCartItem(this.getBase(), EnumMinecart.POWDER);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void renderSpecialContent(RenderNeoCart renderer) {
        renderer.bindTexture(ResourceManager.cart_powder_tex);
        ResourceManager.cart_powder.renderPart("Powder");
    }
}
