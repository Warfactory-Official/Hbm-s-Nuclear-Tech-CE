package com.hbm.render.tileentity;

import com.hbm.Tags;
import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemBatteryPack;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.render.util.BeamPronter;
import com.hbm.render.util.HorsePronter;
import com.hbm.tileentity.machine.storage.TileEntityBatterySocket;
import com.hbm.util.EnumUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.util.Random;

@AutoRegister
public class RenderBatterySocket extends TileEntitySpecialRenderer<TileEntityBatterySocket> implements IItemRendererProvider {

    private static ResourceLocation blorbo = new ResourceLocation(Tags.MODID, "textures/models/horse/sunburst.png");

    @Override
    public void render(TileEntityBatterySocket tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + 0.5D, y, z + 0.5D);

        GlStateManager.enableLighting();
        GlStateManager.enableCull();

        switch(tile.getBlockMetadata() - 10) {
            case 2: GL11.glRotatef(90, 0F, 1F, 0F); break;
            case 4: GL11.glRotatef(180, 0F, 1F, 0F); break;
            case 3: GL11.glRotatef(270, 0F, 1F, 0F); break;
            case 5: GL11.glRotatef(0, 0F, 1F, 0F); break;
        }

        GlStateManager.translate(-0.5D, 0D, 0.5D);

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        bindTexture(ResourceManager.battery_socket_tex);
        ResourceManager.battery_socket.renderPart("Socket");

        ItemStack render = tile.syncStack;
        if(render != null) {

            if(render.getItem() == ModItems.battery_pack) {
                ItemBatteryPack.EnumBatteryPack pack = EnumUtil.grabEnumSafely(ItemBatteryPack.EnumBatteryPack.VALUES, render.getItemDamage());
                bindTexture(pack.texture);
                ResourceManager.battery_socket.renderPart(pack.isCapacitor() ? "Capacitor" : "Battery");
            } else if(render.getItem() == ModItems.battery_sc) {
                bindTexture(ResourceManager.battery_sc_tex);
                ResourceManager.battery_socket.renderPart("Battery");
            } else if(render.getItem() == ModItems.battery_creative) {
                GL11.glPushMatrix();
                GL11.glScaled(0.75, 0.75, 0.75);
                GL11.glRotated((tile.getWorld().getTotalWorldTime() % 360 + partialTicks) * 25D, 0, -1, 0);
                this.bindTexture(blorbo);
                HorsePronter.reset();
                HorsePronter.enableHorn();
                HorsePronter.pront();
                GL11.glPopMatrix();

                Random rand = new Random(tile.getWorld().getTotalWorldTime() / 5);
                rand.nextBoolean();

                for(int i = -1; i <= 1; i += 2) for(int j = -1; j <= 1; j += 2) if(rand.nextInt(4) == 0) {
                    GL11.glPushMatrix();
                    GL11.glTranslated(0, 0.75, 0);
                    BeamPronter.prontBeam(new Vec3d(0.4375 * i, 1.1875, 0.4375 * j), BeamPronter.EnumWaveType.RANDOM, BeamPronter.EnumBeamType.SOLID, 0x404040, 0x002040, (int)(System.currentTimeMillis() % 1000) / 50, 15, 0.0625F, 3, 0.025F);
                    BeamPronter.prontBeam(new Vec3d(0.4375 * i, 1.1875, 0.4375 * j), BeamPronter.EnumWaveType.RANDOM, BeamPronter.EnumBeamType.SOLID, 0x404040, 0x002040, (int)(System.currentTimeMillis() % 1000) / 50, 1, 0, 3, 0.025F);
                    GL11.glPopMatrix();
                }
            }
        }

        GlStateManager.shadeModel(GL11.GL_FLAT);

        GlStateManager.popMatrix();
    }

    @Override
    public Item getItemForRenderer() {
        return Item.getItemFromBlock(ModBlocks.machine_battery_socket);
    }

    @Override
    public ItemRenderBase getRenderer(Item item) {
        return new ItemRenderBase() {
            @Override
            public void renderInventory() {
                GlStateManager.translate(0D, -2D, 0D);
                GlStateManager.scale(5D, 5D, 5D);
            }

            @Override
            public void renderCommon() {
                GlStateManager.shadeModel(GL11.GL_SMOOTH);
                bindTexture(ResourceManager.battery_socket_tex);
                ResourceManager.battery_socket.renderPart("Socket");
                GlStateManager.shadeModel(GL11.GL_FLAT);
            }
        };
    }
}
