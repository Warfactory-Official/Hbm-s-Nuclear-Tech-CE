package com.hbm.render.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;

import java.util.HashMap;
import java.util.Map;

//Modified vesion of LeafiaGripOffsetHelper.java by abysschroma
//https://github.com/abysschroma/NTM-but-uncomfortable/blob/main/src/main/java/com/leafia/dev/items/LeafiaGripOffsetHelper.java
@SideOnly(Side.CLIENT)
public class ViewModelPositonDebugger {
    static boolean debug = false;
    static boolean blockInput = false;
    public Map<TransformType, offset> offsetMap;
    protected int debugIndex = 0;
    public static class offset {
        public double scale;
        public Vec3d rotation;
        public Vec3d position;
        protected ViewModelPositonDebugger helper;
        public offset(ViewModelPositonDebugger helper) {
            scale = 1;
            rotation = new Vec3d(0,0,0);
            position = new Vec3d(0,0,0);
            this.helper = helper;
        }
        public offset setPosition(double x, double y, double z) {
            this.position = new Vec3d(x,y,z);
            return this;
        }
        public offset setRotation(double x, double y, double z) {
            this.rotation = new Vec3d(x,y,z);
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
    public ViewModelPositonDebugger() {
        offsetMap = new HashMap<>();
        debugIndex = 0;
        for (TransformType transform : TransformType.values()) {
            offset offset = new offset(this);
            offset.scale = 0.25;
            offsetMap.put(transform,offset);
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
    public offset get(TransformType transform) {
        return offsetMap.get(transform);
    }
    public void applyCustomOffset(offset offset) {
        GlStateManager.scale(offset.scale,offset.scale,offset.scale);
        GlStateManager.translate(-offset.position.x,offset.position.y,offset.position.z);
        GlStateManager.rotate((float) -offset.rotation.y, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate((float) offset.rotation.x, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate((float) -offset.rotation.z, 0.0F, 0.0F, 1.0F);

    }
    protected void render(TransformType type) {
        offset offset = this.offsetMap.get(type);
        applyCustomOffset(offset);
    }
    public void apply(TransformType type) {
        GlStateManager.rotate(-90f,0,1,0);
        switch(type) {
            case FIRST_PERSON_LEFT_HAND: render(TransformType.FIRST_PERSON_RIGHT_HAND); break;
            case THIRD_PERSON_LEFT_HAND: render(TransformType.THIRD_PERSON_RIGHT_HAND); break;
            case FIXED: render(TransformType.GUI); break;
        }
        render(type);
        render(TransformType.NONE);
        if (debug)
            tickDebug();
    }
    protected void tickDebug() {
        boolean[] inputs = new boolean[]{
                Keyboard.isKeyDown(Keyboard.KEY_UP),Keyboard.isKeyDown(Keyboard.KEY_LEFT),Keyboard.isKeyDown(Keyboard.KEY_DOWN),
                Keyboard.isKeyDown(Keyboard.KEY_RIGHT),Keyboard.isKeyDown(Keyboard.KEY_I),Keyboard.isKeyDown(Keyboard.KEY_J),
                Keyboard.isKeyDown(Keyboard.KEY_K),Keyboard.isKeyDown(Keyboard.KEY_L),Keyboard.isKeyDown(Keyboard.KEY_SPACE),
                Keyboard.isKeyDown(Keyboard.KEY_RSHIFT),Keyboard.isKeyDown(Keyboard.KEY_RCONTROL),Keyboard.isKeyDown(Keyboard.KEY_LBRACKET),
                Keyboard.isKeyDown(Keyboard.KEY_RBRACKET),Keyboard.isKeyDown(Keyboard.KEY_COMMA),Keyboard.isKeyDown(Keyboard.KEY_PERIOD),
                Keyboard.isKeyDown(Keyboard.KEY_O), Keyboard.isKeyDown(Keyboard.KEY_P)};
        boolean doUnblock = true;
        for (boolean input : inputs) {
            if (input) {
                doUnblock = false;
                break;
            }
        }
        if (doUnblock) blockInput = false;
        if (!blockInput) {
            if (!doUnblock) blockInput = true;
            if (inputs[9])
                debugIndex++;
            if (inputs[10])
                debugIndex--;
            debugIndex = Math.floorMod(debugIndex, TransformType.values().length);
            TransformType curTransform = TransformType.values()[debugIndex];
            offset offset = this.offsetMap.get(curTransform);
            double increment = 0.25;
            double incrementAngle = 5;
            double incrementScale = 0.05;
            boolean damn = Keyboard.isKeyDown(Keyboard.KEY_LCONTROL);
            if (Keyboard.isKeyDown(Keyboard.KEY_TAB)) {
                increment=0.05;
                incrementAngle=1;
                incrementScale=0.01;
            }
            if (inputs[8] || inputs[9] || inputs[10]) {
                EntityPlayer player = Minecraft.getMinecraft().player;
                Minecraft.getMinecraft().ingameGUI.getChatGUI().clearChatMessages(false);
                player.sendMessage(new TextComponentString("-- Current Grip --").setStyle(new Style().setColor(TextFormatting.LIGHT_PURPLE)));
                player.sendMessage(new TextComponentString(String.format(" Scale: %01.2f",offset.scale)));
                player.sendMessage(new TextComponentString(String.format(" Position: %01.2f, %01.2f, %01.2f",offset.position.x,offset.position.y,offset.position.z)));
                player.sendMessage(new TextComponentString(String.format(" Rotation: %01.0f, %01.0f, %01.0f",offset.rotation.x,offset.rotation.y,offset.rotation.z)));
                player.sendMessage(new TextComponentString(" Type: "+(curTransform == TransformType.NONE ? "CUSTOM" : curTransform.name())).setStyle(new Style().setColor(TextFormatting.GRAY)));
            }
            if (inputs[11])
                offset.scale+=incrementScale;
            if (inputs[12])
                offset.scale-=incrementScale;
            {
                if (inputs[0])
                    offset.position = offset.position.add(0,damn ? 0 : increment,damn ? increment : 0);
                if (inputs[2])
                    offset.position = offset.position.add(0,damn ? 0 : -increment,damn ? -increment : 0);
                if (inputs[1])
                    offset.position = offset.position.add(-increment,0,0);
                if (inputs[3])
                    offset.position = offset.position.add(increment,0,0);
            }
            {
                // I've changed this fucking mess of the debug rotation system
                // I-O-P changes X-Y-Z rotation positively, and J-K-L - negatively
                // pretty simple, huh?
                if (inputs[4])
                    offset.rotation = offset.rotation.add(incrementAngle,0,0); // I
                if (inputs[5])
                    offset.rotation = offset.rotation.add(-incrementAngle,0,0); // J
                if (inputs[15])
                    offset.rotation = offset.rotation.add(0,incrementAngle,0); // O
                if (inputs[6])
                    offset.rotation = offset.rotation.add(0,-incrementAngle,0); // K
                if (inputs[16])
                    offset.rotation = offset.rotation.add(0,0,incrementAngle); // P
                if (inputs[7])
                    offset.rotation = offset.rotation.add(0,0,-incrementAngle); // L
            }
            {
                if (inputs[13])
                    offset.position = offset.position.add(0,0,-increment);
                if (inputs[14])
                    offset.position = offset.position.add(0,0,increment);
            }
        }
    }
}
