package com.hbm.render.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.joml.Quaternionf;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Quaternion;

import java.util.HashMap;
import java.util.Map;

//Modified vesion of LeafiaGripOffsetHelper.java by abysschroma
//https://github.com/abysschroma/NTM-but-uncomfortable/blob/main/src/main/java/com/leafia/dev/items/LeafiaGripOffsetHelper.java
@SideOnly(Side.CLIENT)
public class ViewModelPositonDebugger {
    static boolean debug = true;
    static boolean blockInput = false;
    public Map<TransformType, offset> offsetMap;
    protected int debugIndex = 0;

    public ViewModelPositonDebugger() {
        offsetMap = new HashMap<>();
        debugIndex = 0;
        for (TransformType transform : TransformType.values()) {
            offset offset = new offset(this);
            offset.scale = 0.25;
            offsetMap.put(transform, offset);
            if (transform == TransformType.NONE)
                offset.setScale(1);
            if (transform == TransformType.FIRST_PERSON_LEFT_HAND)
                offset.setScale(1);
            if (transform == TransformType.THIRD_PERSON_LEFT_HAND)
                offset.setScale(1);
            if (transform == TransformType.FIXED)
                offset.setScale(1);
        }
    }

    public static void renderGizmo(float radius, float thickness) {
        if (!debug) return;

        GlStateManager.pushMatrix();

        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.disableBlend();

        GL11.glLineWidth(thickness);

        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();

        buf.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

        // X
        buf.pos(0, 0, 0).color(1, 0, 0, 1).endVertex();
        buf.pos(radius, 0, 0).color(1, 0, 0, 1).endVertex();
        buf.pos(0, 0, 0).color(0.4f, 0, 0, 1).endVertex();
        buf.pos(-radius, 0, 0).color(0.4f, 0, 0, 1).endVertex();

        // Y
        buf.pos(0, 0, 0).color(0, 1, 0, 1).endVertex();
        buf.pos(0, radius, 0).color(0, 1, 0, 1).endVertex();
        buf.pos(0, 0, 0).color(0, 0.4f, 0, 1).endVertex();
        buf.pos(0, -radius, 0).color(0, 0.4f, 0, 1).endVertex();

        // Z
        buf.pos(0, 0, 0).color(0, 0, 1, 1).endVertex();
        buf.pos(0, 0, radius).color(0, 0, 1, 1).endVertex();
        buf.pos(0, 0, 0).color(0, 0, 0.4f, 1).endVertex();
        buf.pos(0, 0, -radius).color(0, 0, 0.4f, 1).endVertex();

        tess.draw();


        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.enableCull();

        GlStateManager.popMatrix();
    }


    public offset get(TransformType transform) {
        return offsetMap.get(transform);
    }

    public void applyCustomOffset(offset offset) {
        GlStateManager.scale(offset.scale, offset.scale, offset.scale);
        GlStateManager.translate(-offset.position.x, offset.position.y, offset.position.z);
        GlStateManager.rotate(new Quaternion(offset.rotation.x, offset.rotation.y, offset.rotation.z, offset.rotation.w));
    }

    protected void render(TransformType type) {
        offset offset = this.offsetMap.get(type);
        applyCustomOffset(offset);
    }

    public void apply(TransformType type) {
//        GlStateManager.rotate(-90f, 0, 1, 0);
        switch (type) {
            case FIRST_PERSON_LEFT_HAND:
                render(TransformType.FIRST_PERSON_RIGHT_HAND);
                break;
            case THIRD_PERSON_LEFT_HAND:
                render(TransformType.THIRD_PERSON_RIGHT_HAND);
                break;
            case FIXED:
                render(TransformType.GUI);
                break;
        }
        render(type);
        render(TransformType.NONE);
        if (debug)
            tickDebug();
    }

    private Map<String, Boolean> posInputs() {
        return Map.of(
                "up", Keyboard.isKeyDown(Keyboard.KEY_UP),
                "left", Keyboard.isKeyDown(Keyboard.KEY_LEFT),
                "down", Keyboard.isKeyDown(Keyboard.KEY_DOWN),
                "right", Keyboard.isKeyDown(Keyboard.KEY_RIGHT),
                "+", Keyboard.isKeyDown(Keyboard.KEY_EQUALS),
                "-", Keyboard.isKeyDown(Keyboard.KEY_MINUS),
                "comma", Keyboard.isKeyDown(Keyboard.KEY_COMMA),
                "period", Keyboard.isKeyDown(Keyboard.KEY_PERIOD)
        );
    }

    private Map<String, Boolean> rotInputs() {
        return Map.of(
                "I", Keyboard.isKeyDown(Keyboard.KEY_I),
                "J", Keyboard.isKeyDown(Keyboard.KEY_J),
                "O", Keyboard.isKeyDown(Keyboard.KEY_O),
                "K", Keyboard.isKeyDown(Keyboard.KEY_K),
                "P", Keyboard.isKeyDown(Keyboard.KEY_P),
                "L", Keyboard.isKeyDown(Keyboard.KEY_L)
        );
    }

    private Map<String, Boolean> collectInputs() {
        var inputs = new HashMap<String, Boolean>();
        inputs.putAll(posInputs());
        inputs.putAll(rotInputs());
        inputs.putAll(miscInputs());
        return inputs;
    }


    private Map<String, Boolean> miscInputs() {
        return Map.of(
                "space", Keyboard.isKeyDown(Keyboard.KEY_SPACE),
                "rshift", Keyboard.isKeyDown(Keyboard.KEY_RSHIFT),
                "rctrl", Keyboard.isKeyDown(Keyboard.KEY_RCONTROL),
                "tab", Keyboard.isKeyDown(Keyboard.KEY_TAB),
                "ctrl", Keyboard.isKeyDown(Keyboard.KEY_LCONTROL),
                "backspace", Keyboard.isKeyDown(Keyboard.KEY_BACK)
        );
    }
    /**
     * Handles interactive real-time debugging of item transform offsets for all {@link TransformType}s.
     * <p>
     * This method allows the developer to interactively tune position, scale, and rotation
     * (using quaternions) for any item transform type. It is designed for TEISR debugging.
     * Provides a full suite of movement, rotation, scaling, and reset functions.
     *
     * <h3>Input Mapping</h3>
     * <ul>
     *     <li><b>Arrow Keys</b>: Move offset on X/Y axes</li>
     *     <li><b>Comma / Period</b>: Move offset along Z axis</li>
     *     <li><b>[ / ]</b>: Increase / decrease scale</li>
     *
     *     <li><b>I / J</b>: Rotate around +X / -X</li>
     *     <li><b>O / K</b>: Rotate around +Y / -Y</li>
     *     <li><b>P / L</b>: Rotate around +Z / -Z</li>
     *
     *     <li><b>BackSpace / RShift</b>: Cycle active TransformType forward / backward</li>
     *     <li><b>Space</b>: View current rotations</li>
     *     <li><b>LCtrl</b>: Modifies arrow key behavior (vertical ⇄ depth axis)</li>
     *     <li><b>Tab</b>: Precision mode (smaller increments)</li>
     *
     *     <li><b>Pause / Break</b>: Reset rotation to identity quaternion</li>
     *     <li><b>Home</b>: Reset position to (0,0,0)</li>
     *     <li><b>End</b>: Reset scale to 1.0</li>
     * </ul>
     *
     * <h3>Rotation Handling</h3>
     * Rotation uses quaternion multiplication rather than Euler angle accumulation.
     * This eliminates gimbal lock and ensures smooth continuous rotation. Each rotation key
     * applies a small axis-angle quaternion and normalizes the result.
     * This debug method is meant only for development and should not run in production.
     *
     * @author: MrNorwood
     */
    protected void tickDebug() {
        //It's not speed optimal, but who cares its debug
        Map<String, Boolean> input = collectInputs();
        boolean doUnblock = input.values().stream().noneMatch(Boolean::booleanValue);

        if (doUnblock) blockInput = false;
        if (!blockInput) {
            if (!doUnblock) blockInput = true;

            if (input.get("backspace")) debugIndex++;
            if (input.get("rshift")) debugIndex--;

            debugIndex = Math.floorMod(debugIndex, TransformType.values().length);

            TransformType curTransform = TransformType.values()[debugIndex];
            offset offset = this.offsetMap.get(curTransform);

            double increment = 0.25;
            float incrementAngle = 5f;
            double incrementScale = 0.05;

            boolean damn = input.get("rctrl");

            if (input.get("tab")) {
                increment = 0.05;
                incrementAngle = 1;
                incrementScale = 0.01;
            }

            if (input.get("space") || input.get("rshift") || input.get("backspace")) {
                EntityPlayer player = Minecraft.getMinecraft().player;
                Minecraft.getMinecraft().ingameGUI.getChatGUI().clearChatMessages(false);

                player.sendMessage(new TextComponentString("-- Current Grip --")
                        .setStyle(new Style().setColor(TextFormatting.LIGHT_PURPLE)));

                player.sendMessage(new TextComponentString(
                        String.format(" Scale: %01.2f", offset.scale)));

                player.sendMessage(new TextComponentString(
                        String.format(" Position: %01.2f, %01.2f, %01.2f",
                                offset.position.x, offset.position.y, offset.position.z)));

                player.sendMessage(new TextComponentString(
                        String.format(" Rotation: %01.0f, %01.0f, %01.0f",
                                offset.rotation.x, offset.rotation.y, offset.rotation.z)));

                player.sendMessage(new TextComponentString(
                        " Type: " + (curTransform == TransformType.NONE ? "CUSTOM" : curTransform.name()))
                        .setStyle(new Style().setColor(TextFormatting.GRAY)));
            }

            //Scale
            if (input.get("+")) offset.scale += incrementScale;
            if (input.get("-")) offset.scale -= incrementScale;

            //Position
            if (input.get("up"))
                offset.position = offset.position.add(0, damn ? 0 : increment, damn ? increment : 0);
            if (input.get("down"))
                offset.position = offset.position.add(0, damn ? 0 : -increment, damn ? -increment : 0);
            if (input.get("right"))
                offset.position = offset.position.add(-increment, 0, 0);
            if (input.get("left"))
                offset.position = offset.position.add(increment, 0, 0);

            if (input.get("comma"))
                offset.position = offset.position.add(0, 0, -increment);
            if (input.get("period"))
                offset.position = offset.position.add(0, 0, increment);

            //Rotation
            Quaternionf qxPlus = new Quaternionf().fromAxisAngleDeg(1, 0, 0, incrementAngle);
            Quaternionf qxMinus = new Quaternionf().fromAxisAngleDeg(1, 0, 0, -incrementAngle);
            Quaternionf qyPlus = new Quaternionf().fromAxisAngleDeg(0, 1, 0, incrementAngle);
            Quaternionf qyMinus = new Quaternionf().fromAxisAngleDeg(0, 1, 0, -incrementAngle);
            Quaternionf qzPlus = new Quaternionf().fromAxisAngleDeg(0, 0, 1, incrementAngle);
            Quaternionf qzMinus = new Quaternionf().fromAxisAngleDeg(0, 0, 1, -incrementAngle);

            if (input.get("I")) offset.rotation.mul(qxPlus);
            if (input.get("J")) offset.rotation.mul(qxMinus);
            if (input.get("O")) offset.rotation.mul(qyPlus);
            if (input.get("K")) offset.rotation.mul(qyMinus);
            if (input.get("P")) offset.rotation.mul(qzPlus);
            if (input.get("L")) offset.rotation.mul(qzMinus);

            offset.rotation.normalize();
        }
    }

    public static class offset {
        public double scale;
        public Quaternionf rotation;
        public Vec3d position;
        protected ViewModelPositonDebugger helper;

        public offset(ViewModelPositonDebugger helper) {
            scale = 1;
            rotation = new Quaternionf();
            position = new Vec3d(0, 0, 0);
            this.helper = helper;
        }

        public offset setPosition(double x, double y, double z) {
            this.position = new Vec3d(x, y, z);
            return this;
        }

        public offset setRotation(float x, float y, float z, float w) {
            this.rotation = new Quaternionf(x, y, z, w);
            return this;
        }


        public offset setRotation(float x, float y, float z) {
            Quaternionf qx = new Quaternionf().fromAxisAngleDeg(1, 0, 0, x);
            Quaternionf qy = new Quaternionf().fromAxisAngleDeg(0, 1, 0, y);
            Quaternionf qz = new Quaternionf().fromAxisAngleDeg(0, 0, 1, z);
            this.rotation = new Quaternionf().mul(qy).mul(qx).mul(qz);
            return this;
        }

        public offset setScale(double scale) {
            this.scale = scale;
            return this;
        }

        public ViewModelPositonDebugger getHelper() {
            return this.helper;
        }
    }


}
