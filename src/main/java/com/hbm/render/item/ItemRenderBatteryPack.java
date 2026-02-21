package com.hbm.render.item;

import com.hbm.interfaces.AutoRegister;
import com.hbm.items.machine.ItemBatteryPack;
import com.hbm.main.ResourceManager;
import com.hbm.util.EnumUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

@AutoRegister(item = "battery_pack")
public class ItemRenderBatteryPack extends ItemRenderBase {

    @Override
    public void renderInventory() {
        GlStateManager.translate(0, -3, 0);
        GlStateManager.scale(5, 5, 5);
    }

    @Override
    public void renderCommon(ItemStack item) {
        ItemBatteryPack.EnumBatteryPack pack = EnumUtil.grabEnumSafely(ItemBatteryPack.EnumBatteryPack.VALUES, item.getItemDamage());
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        Minecraft.getMinecraft().getTextureManager().bindTexture(pack.texture);
        ResourceManager.battery_socket.renderPart(pack.isCapacitor() ? "Capacitor" : "Battery");
        GlStateManager.shadeModel(GL11.GL_FLAT);
    }
}
