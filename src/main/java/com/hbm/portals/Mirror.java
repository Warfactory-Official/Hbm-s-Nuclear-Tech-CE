package com.hbm.portals;


import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11; import net.minecraft.client.renderer.GlStateManager;

public class Mirror extends Portal {

	public Mirror(World world, Vec3d blc, Vec3d brc, Vec3d tlc, Vec3d trc, EnumFacing dir) {
		super(world, blc, brc, tlc, trc, dir);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void render() {
		if (Minecraft.getMinecraft().getRenderViewEntity() == null)
			return;
		Entity oldRenderViewEntity = Minecraft.getMinecraft().getRenderViewEntity();
		// Drillgon200: simple triangle normal formula. The cross product of two
		// vectors will always return a vector perpendicular to both of them.
		// So, we get the the vector from bottom left to bottom right crossed
		// with the vector from bottom left to top left, then normalize it to
		// get our normal
		Vec3d portalCenter = new Vec3d(bottomLeftCorner.x + (bottomRightCorner.x - bottomLeftCorner.x) / 2, bottomLeftCorner.y + (bottomRightCorner.y - bottomLeftCorner.y) / 2, bottomLeftCorner.z + (bottomRightCorner.z - bottomLeftCorner.z) / 2);
		Vec3d portalNormal = new Vec3d(bottomRightCorner.x - bottomLeftCorner.x, bottomRightCorner.y - bottomLeftCorner.y, bottomRightCorner.z - bottomLeftCorner.z).crossProduct(new Vec3d(topLeftCorner.x - bottomLeftCorner.x, topLeftCorner.y - bottomLeftCorner.y, topLeftCorner.z - bottomLeftCorner.z)).normalize();
		Vec3d oldRenderPos = oldRenderViewEntity.getPositionEyes(Minecraft.getMinecraft().getRenderPartialTicks()).subtract(portalCenter);

		// Drillgon200: Reflected ray (the position of the mirror's reflection
		// view entity) is I+V,
		// where I is the original position and V is 2 times the
		// reflection plane's normal multiplied by the negative plane normal's
		// dot product with the original position. So, R = I + 2*N*(-N dot I).
		Vec3d newRenderPos = oldRenderPos.add(multVector(multVector(portalNormal, 2), multVector(portalNormal, -1).dotProduct(oldRenderPos)));
		newRenderPos = newRenderPos.add(portalCenter);
		// Drillgon200: Do the same thing except with the look vector
		Vec3d originalLook = oldRenderViewEntity.getLook(Minecraft.getMinecraft().getRenderPartialTicks());
		Vec3d newLook = originalLook.add(multVector(multVector(portalNormal, 2), multVector(portalNormal, -1).dotProduct(originalLook)));
		double mirroredYaw = (float) MathHelper.atan2(newLook.x, newLook.z);
		float mirroredPitch = (float) Math.asin(newLook.y);
		this.dummyRenderEntity.setPositionAndRotationDirect(newRenderPos.x, newRenderPos.y, newRenderPos.z, (float) -Math.toDegrees(mirroredYaw), (float) Math.toDegrees(mirroredPitch), 0, true);
		dummyRenderEntity.lastTickPosX = newRenderPos.x;
		dummyRenderEntity.lastTickPosY = newRenderPos.y;
		dummyRenderEntity.lastTickPosZ = newRenderPos.z;
		dummyRenderEntity.prevPosX = newRenderPos.x;
		dummyRenderEntity.prevPosY = newRenderPos.y;
		dummyRenderEntity.prevPosZ = newRenderPos.z;
		dummyRenderEntity.prevRotationYaw = dummyRenderEntity.rotationYaw;
		dummyRenderEntity.prevRotationPitch = dummyRenderEntity.rotationPitch;
		Minecraft.getMinecraft().setRenderViewEntity(dummyRenderEntity);
		int i2 = Minecraft.getMinecraft().gameSettings.limitFramerate;
		int j = Math.min(Minecraft.getDebugFPS(), i2);
        j = Math.max(j, 60);
        long k = System.nanoTime() - System.nanoTime();
        long l = Math.max((long)(1000000000 / j / 4) - k, 0L);
        Minecraft.getMinecraft().entityRenderer.renderWorld(Minecraft.getMinecraft().getRenderPartialTicks(), System.nanoTime() + l);
        GlStateManager.rotate(180, 0, 1, 0);
        GlStateManager.rotate((float) (mirroredYaw), 0, 1, 0);
        Minecraft.getMinecraft().getRenderManager().renderViewEntity = oldRenderViewEntity;
		Minecraft.getMinecraft().getRenderManager().renderEntityStatic(oldRenderViewEntity, Minecraft.getMinecraft().getRenderPartialTicks(), false);
		Minecraft.getMinecraft().setRenderViewEntity(oldRenderViewEntity);
		
	}

	private Vec3d multVector(Vec3d vec, double factor) {
		return new Vec3d(vec.x * factor, vec.y * factor, vec.z * factor);
	}
}
