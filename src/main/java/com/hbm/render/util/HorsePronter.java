package com.hbm.render.util;

import com.hbm.Tags;
import com.hbm.main.ResourceManager;
import com.hbm.util.Vec3NT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.client.renderer.GlStateManager;

public class HorsePronter {


    public static final ResourceLocation tex_demohorse = new ResourceLocation(Tags.MODID, "textures/models/horse/horse_demo.png");

    private static final Vec3NT[] pose = new Vec3NT[] {
            new Vec3NT(0, 0, 0), //head
            new Vec3NT(0, 0, 0), //left front leg
            new Vec3NT(0, 0, 0), //right front leg
            new Vec3NT(0, 0, 0), //left back leg
            new Vec3NT(0, 0, 0), //right back leg
            new Vec3NT(0, 0, 0), //tail
            new Vec3NT(0, 0, 0), //body
            new Vec3NT(0, 0, 0) //body offset
    };

    private static final Vec3NT[] offsets = new Vec3NT[] {
            new Vec3NT(0, 1.125, 0.375), //head
            new Vec3NT(0.125, 0.75, 0.3125), //left front leg
            new Vec3NT(-0.125, 0.75, 0.3125), //right front leg
            new Vec3NT(0.125, 0.75, -0.25), //left back leg
            new Vec3NT(-0.125, 0.75, -0.25), //right back leg
            new Vec3NT(0, 1.125, -0.4375), //tail
            new Vec3NT(0, 0, 0), //body
            new Vec3NT(0, 0, 0) //body offset
    };

    public static final int id_head = 0;
    public static final int id_lfl = 1;
    public static final int id_rfl = 2;
    public static final int id_lbl = 3;
    public static final int id_rbl = 4;
    public static final int id_tail = 5;
    public static final int id_body = 6;
    public static final int id_position = 7;

    private static boolean wings = false;
    private static boolean horn = false;
    private static boolean maleSnoot = false;

    public static void reset() {

        wings = false;
        horn = false;

        for(Vec3NT angles : pose) {
            angles.set(0, 0, 0);
        }
    }

    public static void enableHorn() { horn = true; }
    public static void enableWings() { wings = true; }
    public static void setMaleSnoot() { maleSnoot = true; }

    public static void setAlicorn() {
        enableHorn();
        enableWings();
    }

    public static void poseStandardSit() {
        double r = 60;
        pose(HorsePronter.id_body, 0, -r, 0);
        pose(HorsePronter.id_tail, 0, 45, 90);
        pose(HorsePronter.id_lbl, 0, -90 + r, 35);
        pose(HorsePronter.id_rbl, 0, -90 + r, -35);
        pose(HorsePronter.id_lfl, 0, r - 10, 5);
        pose(HorsePronter.id_rfl, 0, r - 10, -5);
        pose(HorsePronter.id_head, 0, r, 0);
    }

    public static void pose(int id, double yaw, double pitch, double roll) {
        pose[id].set(yaw, pitch, roll);
    }

    public static void pront() {

        GlStateManager.pushMatrix();
        GlStateManager.disableCull();
        doTransforms(id_body);

        ResourceManager.horse.renderPart("Body");

        if(horn) {
            renderWithTransform(id_head, "Head", "Mane", maleSnoot ? "NoseMale" : "NoseFemale", "HornPointy");
        } else {
            renderWithTransform(id_head, "Head", "Mane", maleSnoot ? "NoseMale" : "NoseFemale");
        }

        renderWithTransform(id_lfl, "LeftFrontLeg");
        renderWithTransform(id_rfl, "RightFrontLeg");
        renderWithTransform(id_lbl, "LeftBackLeg");
        renderWithTransform(id_rbl, "RightBackLeg");
        renderWithTransform(id_tail, "Tail");

        if(wings) {
            ResourceManager.horse.renderPart("LeftWing");
            ResourceManager.horse.renderPart("RightWing");
        }

        GlStateManager.enableCull();
        GlStateManager.popMatrix();
    }

    private static void doTransforms(int id) {
        Vec3NT rotation = pose[id];
        Vec3NT offset = offsets[id];
        GlStateManager.translate(offset.x, offset.y, offset.z);
        GlStateManager.rotate(rotation.x, 0, 1, 0);
        GlStateManager.rotate(rotation.y, 1, 0, 0);
        GlStateManager.rotate(rotation.z, 0, 0, 1); //TODO: check pitch and roll axis
        GlStateManager.translate(-offset.x, -offset.y, -offset.z);
    }

    private static void renderWithTransform(int id, String... parts) {
        GlStateManager.pushMatrix();
        doTransforms(id);
        for(String part : parts) ResourceManager.horse.renderPart(part);
        GlStateManager.popMatrix();
    }
}
