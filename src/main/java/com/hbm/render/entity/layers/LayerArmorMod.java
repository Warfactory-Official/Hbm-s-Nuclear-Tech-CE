package com.hbm.render.entity.layers;

import com.hbm.handler.ArmorModHandler;
import com.hbm.items.armor.ItemArmorMod;
import com.hbm.items.armor.JetpackBase;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderPlayerEvent;

public class LayerArmorMod implements LayerRenderer<AbstractClientPlayer> {

    private final RenderPlayer renderer;

    public LayerArmorMod(RenderPlayer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void doRenderLayer(AbstractClientPlayer player, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {

        //bridge so every existing modRender(Pre, ItemStack) keeps compiling; x/y/z are unused by them
        RenderPlayerEvent.Pre ctx = new RenderPlayerEvent.Pre(player, this.renderer, partialTicks, 0, 0, 0);

        for(int i = 0; i < 4; i++) {

            ItemStack armor = player.inventory.armorItemInSlot(i);
            if(armor.isEmpty()) continue;

            if(ArmorModHandler.hasMods(armor)) {

                for(ItemStack mod : ArmorModHandler.pryMods(armor)) {

                    if(mod != null && mod.getItem() instanceof ItemArmorMod) {
                        ((ItemArmorMod) mod.getItem()).modRender(ctx, armor);
                    }
                }
            }

            //because armor that isn't ItemArmor doesn't render at all
            if(armor.getItem() instanceof JetpackBase) {
                ((ItemArmorMod) armor.getItem()).modRender(ctx, armor);
            }
        }
    }

    @Override
    public boolean shouldCombineTextures() {
        return false;
    }
}
