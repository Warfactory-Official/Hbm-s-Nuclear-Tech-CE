package com.hbm.wiaj.cannery;

import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;
import com.hbm.main.ResourceManager;
import com.hbm.tileentity.machine.TileEntityStirling;
import com.hbm.util.I18nUtil;
import com.hbm.util.RenderUtil;
import com.hbm.wiaj.JarScene;
import com.hbm.wiaj.JarScript;
import com.hbm.wiaj.WorldInAJar;
import com.hbm.wiaj.actions.*;
import com.hbm.wiaj.actors.ActorFancyPanel;
import com.hbm.wiaj.actors.ActorFancyPanel.Orientation;
import com.hbm.wiaj.actors.ActorTileEntity;
import com.hbm.wiaj.actors.ITileActorRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;


public class CanneryFirebox extends CanneryBase {

    @Override
    public ItemStack getIcon() {
        return new ItemStack(ModBlocks.heater_firebox);
    }

    @Override
    public String getName() {
        return "cannery.firebox";
    }

    public JarScript createScript() {

        WorldInAJar world = new WorldInAJar(5, 5, 5);
        JarScript script = new JarScript(world);

        JarScene scene0 = new JarScene(script);

        scene0.add(new ActionSetZoom(3, 0));

        for (int x = world.sizeX - 1; x >= 0; x--) {
            for (int z = 0; z < world.sizeZ; z++) {
                scene0.add(new ActionSetBlock(x, 0, z, Blocks.BRICK_BLOCK));
            }
            scene0.add(new ActionWait(2));
        }

        scene0.add(new ActionWait(8));

        NBTTagCompound firebox = new NBTTagCompound();
        firebox.setDouble("x", 2);
        firebox.setDouble("y", 1);
        firebox.setDouble("z", 2);
        firebox.setInteger("rotation", 5);
        scene0.add(new ActionCreateActor(0, new ActorTileEntity(new ActorFirebox(), firebox)));

        scene0.add(new ActionWait(10));

        scene0.add(new ActionCreateActor(1, new ActorFancyPanel(Minecraft.getMinecraft().fontRenderer, 0, -10, new Object[][]{{I18nUtil.resolveKey("cannery.firebox.0")}}, 150)
                .setColors(colorCopper).setOrientation(Orientation.BOTTOM)));

        scene0.add(new ActionWait(60));

        scene0.add(new ActionCreateActor(1, new ActorFancyPanel(Minecraft.getMinecraft().fontRenderer, 0, -10, new Object[][]{{I18nUtil.resolveKey("cannery.firebox.1")}}, 250)
                .setColors(colorCopper).setOrientation(Orientation.BOTTOM)));

        scene0.add(new ActionWait(60));
        scene0.add(new ActionRemoveActor(1));
        scene0.add(new ActionWait(5));
        scene0.add(new ActionUpdateActor(0, "open", true));
        scene0.add(new ActionWait(30));

        scene0.add(new ActionUpdateActor(0, "isOn", true));

        scene0.add(new ActionCreateActor(1, new ActorFancyPanel(Minecraft.getMinecraft().fontRenderer, -50, 40, new Object[][]{{new ItemStack(Items.COAL)}}, 0)
                .setColors(colorCopper).setOrientation(Orientation.RIGHT)));
        scene0.add(new ActionWait(10));
        scene0.add(new ActionCreateActor(1, new ActorFancyPanel(Minecraft.getMinecraft().fontRenderer, -50, 40, new Object[][]{{new ItemStack(ModItems.coke)}}, 0)
                .setColors(colorCopper).setOrientation(Orientation.RIGHT)));
        scene0.add(new ActionWait(10));
        scene0.add(new ActionCreateActor(1, new ActorFancyPanel(Minecraft.getMinecraft().fontRenderer, -50, 40, new Object[][]{{new ItemStack(ModItems.solid_fuel)}}, 0)
                .setColors(colorCopper).setOrientation(Orientation.RIGHT)));
        scene0.add(new ActionWait(10));
        scene0.add(new ActionCreateActor(1, new ActorFancyPanel(Minecraft.getMinecraft().fontRenderer, -50, 40, new Object[][]{{new ItemStack(ModItems.rocket_fuel)}}, 0)
                .setColors(colorCopper).setOrientation(Orientation.RIGHT)));
        scene0.add(new ActionWait(10));
        scene0.add(new ActionCreateActor(1, new ActorFancyPanel(Minecraft.getMinecraft().fontRenderer, -50, 40, new Object[][]{{new ItemStack(ModItems.solid_fuel)}}, 0) //TODO: add solid fuel balefire
                .setColors(colorCopper).setOrientation(Orientation.RIGHT)));
        scene0.add(new ActionWait(10));
        scene0.add(new ActionRemoveActor(1));
        scene0.add(new ActionWait(10));
        scene0.add(new ActionUpdateActor(0, "open", false));
        scene0.add(new ActionWait(30));

        scene0.add(new ActionCreateActor(1, new ActorFancyPanel(Minecraft.getMinecraft().fontRenderer, 0, -10, new Object[][]{{I18nUtil.resolveKey("cannery.firebox.2")}}, 250)
                .setColors(colorCopper).setOrientation(Orientation.BOTTOM)));

        scene0.add(new ActionWait(80));

        scene0.add(new ActionCreateActor(1, new ActorFancyPanel(Minecraft.getMinecraft().fontRenderer, 0, -10, new Object[][]{{I18nUtil.resolveKey("cannery.firebox.3")}}, 250)
                .setColors(colorCopper).setOrientation(Orientation.BOTTOM)));

        scene0.add(new ActionWait(60));
        scene0.add(new ActionRemoveActor(1));
        scene0.add(new ActionWait(10));

        JarScene scene1 = new JarScene(script);

        NBTTagCompound stirling = new NBTTagCompound();
        stirling.setDouble("x", 2);
        stirling.setDouble("y", 2);
        stirling.setDouble("z", 2);
        stirling.setInteger("rotation", 2);
        stirling.setBoolean("hasCog", true);
        ITileActorRenderer stirlingRenderer = (ITileActorRenderer) TileEntityRendererDispatcher.instance.getRenderer(TileEntityStirling.class);
        scene1.add(new ActionCreateActor(1, new ActorTileEntity(stirlingRenderer, stirling)));
        scene1.add(new ActionUpdateActor(1, "speed", 0F));

        scene1.add(new ActionWait(10));
        scene1.add(new ActionCreateActor(2, new ActorFancyPanel(Minecraft.getMinecraft().fontRenderer, 0, -45, new Object[][]{{I18nUtil.resolveKey("cannery.firebox.4")}}, 250)
                .setColors(colorCopper).setOrientation(Orientation.BOTTOM)));
        scene1.add(new ActionWait(60));
        scene1.add(new ActionRemoveActor(2));
        scene1.add(new ActionWait(10));

        for (int i = 0; i < 60; i++) {
            scene1.add(new ActionUpdateActor(1, "speed", i / 5F));
            scene1.add(new ActionWait(1));
        }

        scene1.add(new ActionSetTile(1, 2, 2, new Dummies.JarDummyConnector()));
        scene1.add(new ActionSetTile(0, 2, 2, new Dummies.JarDummyConnector()));
        scene1.add(new ActionSetTile(0, 1, 2, new Dummies.JarDummyConnector()));
        scene1.add(new ActionSetTile(0, 1, 3, new Dummies.JarDummyConnector()));
        scene1.add(new ActionSetBlock(0, 2, 2, ModBlocks.red_cable));
        scene1.add(new ActionSetBlock(0, 1, 2, ModBlocks.red_cable));
        scene1.add(new ActionSetBlock(0, 1, 3, ModBlocks.machine_detector, 0));
        scene1.add(new ActionWait(10));
        scene1.add(new ActionSetBlock(0, 1, 3, ModBlocks.machine_detector, 1));
        scene1.add(new ActionWait(100));

        script.addScene(scene0).addScene(scene1);
        return script;
    }

    public static class ActorFirebox implements ITileActorRenderer {

        @Override
        public void renderActor(WorldInAJar world, int ticks, float interp, NBTTagCompound data) {
            double x = data.getDouble("x");
            double y = data.getDouble("y");
            double z = data.getDouble("z");
            int rotation = data.getInteger("rotation");
            boolean isOn = data.getBoolean("isOn");
            float doorAngle = data.getFloat("angle");
            float prevDoorAngle = data.getFloat("lastAngle");

            GlStateManager.translate(x + 0.5D, y, z + 0.5D);
            GlStateManager.enableLighting();
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
            GlStateManager.enableCull();

            switch (rotation) {
                case 3 -> GlStateManager.rotate(0, 0F, 1F, 0F);
                case 5 -> GlStateManager.rotate(90, 0F, 1F, 0F);
                case 2 -> GlStateManager.rotate(180, 0F, 1F, 0F);
                case 4 -> GlStateManager.rotate(270, 0F, 1F, 0F);
            }

            ITileActorRenderer.bindTexture(ResourceManager.heater_firebox_tex);
            ResourceManager.heater_firebox.renderPart("Main");

            GlStateManager.pushMatrix();

            float door = prevDoorAngle + (doorAngle - prevDoorAngle) * interp;
            GlStateManager.translate(1.375, 0, 0.375);
            GlStateManager.rotate(door, 0F, -1F, 0F);
            GlStateManager.translate(-1.375, 0, -0.375);
            ResourceManager.heater_firebox.renderPart("Door");
            GlStateManager.popMatrix();

            if (isOn) {
                GlStateManager.pushMatrix();
                boolean prevLighting = RenderUtil.isLightingEnabled();
                GlStateManager.disableLighting();
                GlStateManager.disableCull();
                OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
                ResourceManager.heater_firebox.renderPart("InnerBurning");
                if (prevLighting) GlStateManager.enableLighting();
                GlStateManager.popMatrix();
            } else {
                ResourceManager.heater_firebox.renderPart("InnerEmpty");
            }
        }

        @Override
        public void updateActor(int ticks, NBTTagCompound data) {

            boolean open = data.getBoolean("open");
            float doorAngle = data.getFloat("angle");
            data.setFloat("lastAngle", doorAngle);

            float swingSpeed = (doorAngle / 10F) + 3;

            if (open) {
                doorAngle += swingSpeed;
            } else {
                doorAngle -= swingSpeed;
            }

            doorAngle = MathHelper.clamp(doorAngle, 0F, 135F);
            data.setFloat("angle", doorAngle);
        }
    }
}
