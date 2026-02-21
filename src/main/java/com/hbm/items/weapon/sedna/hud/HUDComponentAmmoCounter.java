package com.hbm.items.weapon.sedna.hud;

import com.hbm.Tags;
import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.items.weapon.sedna.mags.IMagazine;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;

public class HUDComponentAmmoCounter implements IHUDComponent {

    private static final ResourceLocation misc = new ResourceLocation(Tags.MODID + ":textures/misc/overlay_misc.png");
    protected static final RenderItem itemRenderer = Minecraft.getMinecraft().getRenderItem();
    protected int receiver;
    protected boolean mirrored;
    protected boolean noCounter;

    public HUDComponentAmmoCounter(int receiver) {
        this.receiver = receiver;
    }

    public HUDComponentAmmoCounter mirror() {
        this.mirrored = true;
        return this;
    }

    public HUDComponentAmmoCounter noCounter() {
        this.noCounter = true;
        return this;
    }

    @Override
    public int getComponentHeight(EntityPlayer player, ItemStack stack){
        return 24;
    }

    @Override
    public void renderHUDComponent(RenderGameOverlayEvent.Pre event, RenderGameOverlayEvent.ElementType type, EntityPlayer player, ItemStack stack, int bottomOffset, int gunIndex) {

        if(type != type.HOTBAR) return;
        ScaledResolution resolution = event.getResolution();
        Minecraft mc = Minecraft.getMinecraft();
        boolean left = !player.getHeldItemOffhand().isEmpty();

        int pX = resolution.getScaledWidth() / 2 + (mirrored ? -(62 + 36 + 52 + (left ? 29 : 0)) : (62 + 36)) + (noCounter ? 14 : 0);
        int pZ = resolution.getScaledHeight() - bottomOffset - 23;

        ItemGunBaseNT gun = (ItemGunBaseNT) stack.getItem();
        IMagazine mag = gun.getConfig(stack, gunIndex).getReceivers(stack)[this.receiver].getMagazine(stack);

        if (!noCounter) {
            mc.fontRenderer.drawString(mag.reportAmmoStateForHUD(stack, player), pX + 17, pZ + 6, 0xFFFFFF);
        }

        ItemStack icon = mag.getIconForHUD(stack, player);
        if (!icon.isEmpty()) {
            GlStateManager.enableBlend();
            GlStateManager.color(1F, 1F, 1F, 1F);
            RenderHelper.enableGUIStandardItemLighting();
            mc.getRenderItem().renderItemAndEffectIntoGUI(icon, pX, pZ);
            RenderHelper.disableStandardItemLighting();
            mc.getTextureManager().bindTexture(misc);
        }
    }
}
