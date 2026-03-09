package com.hbm.render.util;

import com.hbm.config.ClientConfig;
import com.hbm.config.GeneralConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class RenderInfoSystemLegacy {

    private static int nextID = 1000;
    private static Map<Integer, InfoEntry> inbox = new ConcurrentHashMap<>();
    private static Map<Integer, InfoEntry> messages = new ConcurrentHashMap<>();

    @SubscribeEvent
    public void clentTick(TickEvent.ClientTickEvent event) {
        synchronized (inbox) {
            messages.putAll(inbox);
            inbox.clear();
        }

        Iterator<Map.Entry<Integer, InfoEntry>> iterator = new HashMap<>(messages).entrySet().iterator();

        long currentTime = System.currentTimeMillis();
        while (iterator.hasNext()) {
            Map.Entry<Integer, InfoEntry> entry = iterator.next();
            InfoEntry info = entry.getValue();

            if (info.start + info.millis < currentTime) {
                messages.remove(entry.getKey());
            }
        }
    }

    @SubscribeEvent(receiveCanceled = true)
    public void onOverlayRender(RenderGameOverlayEvent.Pre event) {

        if(event.getType() != RenderGameOverlayEvent.ElementType.CROSSHAIRS)
            return;

        if(this.messages.isEmpty())
            return;

        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution resolution = event.getResolution();

        List<InfoEntry> entries = new ArrayList(messages.values());
        Collections.sort(entries);

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);

        int longest = 0;

        for(InfoEntry entry : entries) {
            int length = mc.fontRenderer.getStringWidth(entry.text);

            if(length > longest)
                longest = length;
        }

        int mode = ClientConfig.INFO_POSITION.get();

        int pX = mode == 0 ? 15 : mode == 1 ? (resolution.getScaledWidth() - longest - 15) : mode == 2 ? (resolution.getScaledWidth() / 2 + 7) : (resolution.getScaledWidth() / 2 - longest - 6);
        int pZ = mode == 0 ? 15 : mode == 1 ? 15 : resolution.getScaledHeight() / 2 + 7;

        pX += ClientConfig.INFO_OFFSET_HORIZONTAL.get();
        pZ += ClientConfig.INFO_OFFSET_VERTICAL.get();

        int side = pX + 5 + longest;
        int height = messages.size() * 10 + pZ + 2;
        int z = 0;

        GlStateManager.disableTexture2D();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();

        bufferbuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(pX - 5, pZ - 5, z).color(0.25F, 0.25F, 0.25F, 0.5F).endVertex();
        bufferbuilder.pos(pX - 5, height, z).color(0.25F, 0.25F, 0.25F, 0.5F).endVertex();
        bufferbuilder.pos(side, height, z).color(0.25F, 0.25F, 0.25F, 0.5F).endVertex();
        bufferbuilder.pos(side, pZ - 5, z).color(0.25F, 0.25F, 0.25F, 0.5F).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();

        int off = 0;
        long now = System.currentTimeMillis();

        for(InfoEntry entry : entries) {

            int elapsed = (int) (now - entry.start);

            int alpha = Math.max(Math.min(510 * (entry.millis - elapsed) / entry.millis, 255), 5); //smoothly scales down from 510 to 0, then caps at 255
            int color = entry.color + (alpha << 24 & -0xffffff);
            mc.fontRenderer.drawString(entry.text, pX, pZ + off, color);

            off += 10;
        }

        GlStateManager.color(1F, 1F, 1F, 1F);

        GlStateManager.popMatrix();
        Minecraft.getMinecraft().renderEngine.bindTexture(Gui.ICONS);
    }

    public static void push(InfoEntry entry) {
        push(entry, nextID++); //range is so large, collisions are unlikely and if they do occur, not a big deal
    }

    public static void push(InfoEntry entry, int id) {
        inbox.put(id, entry);
    }

    public static class InfoEntry implements Comparable {

        String text;
        int color = 0xffffff;
        long start;
        int millis;

        public InfoEntry(String text) {
            this(text, 3000);
        }

        public InfoEntry(String text, int millis) {
            this.text = text;
            this.millis = millis;
            this.start = System.currentTimeMillis();
        }

        public InfoEntry withColor(int color) {
            this.color = color;
            return this;
        }

        @Override
        public int compareTo(Object o) {

            if(!(o instanceof InfoEntry)) {
                return 0;
            }

            InfoEntry other = (InfoEntry) o;

            return this.millis < other.millis ? -1 : this.millis > other.millis ? 1 : 0;
        }
    }
}
