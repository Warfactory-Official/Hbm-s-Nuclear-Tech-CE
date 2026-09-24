package com.hbm.render.world;

import com.hbm.capability.HbmLivingProps;
import com.hbm.handler.ImpactWorldHandler;
import com.hbm.render.util.NTMBufferBuilder;
import com.hbm.render.util.NTMImmediate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.IRenderHandler;
import org.lwjgl.opengl.GL11;

import java.util.Random;

public class RenderNTMSkyboxImpact extends IRenderHandler {

    private static final ResourceLocation sunTexture = new ResourceLocation("textures/environment/sun.png");
    private static final ResourceLocation moonTexture = new ResourceLocation("textures/environment/moon_phases.png");
    private static final ResourceLocation digammaStar = new ResourceLocation("hbm:textures/misc/star_digamma.png");
    private static final ResourceLocation bobmazonSat = new ResourceLocation("hbm:textures/misc/sat_bobmazon.png");

    private final VertexBuffer skyVBO = new VertexBuffer(DefaultVertexFormats.POSITION);
    private final VertexBuffer sky2VBO = new VertexBuffer(DefaultVertexFormats.POSITION);
    private final VertexBuffer starVBO = new VertexBuffer(DefaultVertexFormats.POSITION);

    public RenderNTMSkyboxImpact() {
        this.generateSky();
        this.generateSky2();
        this.generateStars();
    }

    @Override
    public void render(float partialTicks, WorldClient world, Minecraft mc) {
        float atmosphericDust = ImpactWorldHandler.getDustForClient(world);

        GlStateManager.disableTexture2D();
        Vec3d skyColor = world.getSkyColor(mc.getRenderViewEntity(), partialTicks);
        float red = (float) skyColor.x;
        float green = (float) skyColor.y;
        float blue = (float) skyColor.z;
        float dust = Math.max(1.0F - atmosphericDust * 2.0F, 0.0F);
        float rain = dust * (1.0F - world.getRainStrength(partialTicks));

        if (mc.gameSettings.anaglyph) {
            float anaglyphRed = (red * 30.0F + green * 59.0F + blue * 11.0F) / 100.0F;
            float anaglyphGreen = (red * 30.0F + green * 70.0F) / 100.0F;
            float anaglyphBlue = (red * 30.0F + blue * 70.0F) / 100.0F;
            red = anaglyphRed;
            green = anaglyphGreen;
            blue = anaglyphBlue;
        }

        GlStateManager.color(red, green, blue);
        GlStateManager.depthMask(false);
        GlStateManager.enableFog();
        GlStateManager.color(red, green, blue);
        this.drawPositionVbo(this.skyVBO);
        GlStateManager.disableFog();
        GlStateManager.disableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderHelper.disableStandardItemLighting();

        float starBrightness = world.getStarBrightness(partialTicks);
        if (starBrightness > 0.0F) {
            GlStateManager.pushMatrix();
            GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(world.getCelestialAngle(partialTicks) * 360.0F, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotate(-19.0F, 0.0F, 1.0F, 0.0F);
            GlStateManager.color(starBrightness, starBrightness, starBrightness, starBrightness * rain);
            this.drawPositionVbo(this.starVBO);
            GlStateManager.popMatrix();
        }

        GlStateManager.shadeModel(GL11.GL_FLAT);

        GlStateManager.enableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.pushMatrix();
        GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(world.getCelestialAngle(partialTicks) * 360.0F, 1.0F, 0.0F, 0.0F);

        float size = 30.0F;
        GlStateManager.disableTexture2D();
        GlStateManager.color(0.0F, 0.0F, 0.0F, 1.0F);
        NTMBufferBuilder buf = NTMImmediate.INSTANCE.beginPositionQuads(1);
        buf.appendPositionQuadUnchecked(
                -size, 99.9F, -size,
                 size, 99.9F, -size,
                 size, 99.9F,  size,
                -size, 99.9F,  size
        );
        NTMImmediate.INSTANCE.draw();

        GlStateManager.enableTexture2D();
        GlStateManager.color(1.0F, 1.0F, 1.0F, rain);
        mc.getTextureManager().bindTexture(sunTexture);
        buf = NTMImmediate.INSTANCE.beginPositionTexQuads(1);
        buf.appendPositionTexQuadUnchecked(
                -size, 100.0F, -size, 0.0F, 0.0F,
                 size, 100.0F, -size, 1.0F, 0.0F,
                 size, 100.0F,  size, 1.0F, 1.0F,
                -size, 100.0F,  size, 0.0F, 1.0F
        );
        NTMImmediate.INSTANCE.draw();

        GlStateManager.color(1.0F, 1.0F, 1.0F, rain);
        size = 20.0F;
        mc.getTextureManager().bindTexture(moonTexture);
        int moonPhase = world.getMoonPhase();
        int moonColumn = moonPhase % 4;
        int moonRow = moonPhase / 4 % 2;
        float minU = moonColumn / 4.0F;
        float minV = moonRow / 2.0F;
        float maxU = (moonColumn + 1) / 4.0F;
        float maxV = (moonRow + 1) / 2.0F;
        buf = NTMImmediate.INSTANCE.beginPositionTexQuads(1);
        buf.appendPositionTexQuadUnchecked(
                -size, -100.0F,  size, maxU, maxV,
                 size, -100.0F,  size, minU, maxV,
                 size, -100.0F, -size, minU, minV,
                -size, -100.0F, -size, maxU, minV
        );
        NTMImmediate.INSTANCE.draw();

        float brightness = (float) Math.sin(world.getCelestialAngle(partialTicks) * Math.PI);
        brightness *= brightness;

        GlStateManager.pushMatrix();
        GlStateManager.color(brightness, brightness, brightness, dust);
        GlStateManager.rotate(-90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(world.getCelestialAngle(partialTicks) * 360.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(140.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(-40.0F, 0.0F, 0.0F, 1.0F);
        mc.getTextureManager().bindTexture(digammaStar);

        double digamma = HbmLivingProps.getDigamma(mc.player);
        float digammaSize = (float) (1.0D + digamma * 0.25D);
        float digammaDistance = (float) (100.0D - digamma * 2.5D);

        buf = NTMImmediate.INSTANCE.beginPositionTexQuads(1);
        buf.appendPositionTexQuadUnchecked(
                -digammaSize, digammaDistance, -digammaSize, 0.0F, 0.0F,
                 digammaSize, digammaDistance, -digammaSize, 0.0F, 1.0F,
                 digammaSize, digammaDistance,  digammaSize, 1.0F, 1.0F,
                -digammaSize, digammaDistance,  digammaSize, 1.0F, 0.0F
        );
        NTMImmediate.INSTANCE.draw();
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.color(brightness, brightness, brightness, rain);
        GlStateManager.rotate(-40.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate((System.currentTimeMillis() % (360 * 1000)) / 1000.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate((System.currentTimeMillis() % (360 * 100)) / 100.0F, 1.0F, 0.0F, 0.0F);
        mc.getTextureManager().bindTexture(bobmazonSat);

        float satelliteSize = 0.5F;
        float satelliteDistance = 100.0F;
        buf = NTMImmediate.INSTANCE.beginPositionTexQuads(1);
        buf.appendPositionTexQuadUnchecked(
                -satelliteSize, satelliteDistance, -satelliteSize, 0.0F, 0.0F,
                 satelliteSize, satelliteDistance, -satelliteSize, 0.0F, 1.0F,
                 satelliteSize, satelliteDistance,  satelliteSize, 1.0F, 1.0F,
                -satelliteSize, satelliteDistance,  satelliteSize, 1.0F, 0.0F
        );
        NTMImmediate.INSTANCE.draw();
        GlStateManager.popMatrix();

        GlStateManager.disableTexture2D();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableFog();
        GlStateManager.popMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.color(0.0F, 0.0F, 0.0F);

        double horizonOffset = mc.player.getPositionEyes(partialTicks).y - world.getHorizon();
        if (horizonOffset < 0.0D) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0F, 12.0F, 0.0F);
            this.drawPositionVbo(this.sky2VBO);
            GlStateManager.popMatrix();

            int black = NTMBufferBuilder.packColor(0, 0, 0, 255);
            float skyBoxSize = 1.0F;
            float top = -((float) (horizonOffset + 65.0D));
            float bottom = -skyBoxSize;
            buf = NTMImmediate.INSTANCE.beginPositionColorQuads(5);
            buf.appendPositionColorQuadUnchecked(-skyBoxSize, top,    skyBoxSize,  skyBoxSize, top,    skyBoxSize,  skyBoxSize, bottom, skyBoxSize, -skyBoxSize, bottom, skyBoxSize, black);
            buf.appendPositionColorQuadUnchecked(-skyBoxSize, bottom, -skyBoxSize, skyBoxSize, bottom, -skyBoxSize, skyBoxSize, top,    -skyBoxSize, -skyBoxSize, top,    -skyBoxSize, black);
            buf.appendPositionColorQuadUnchecked(skyBoxSize,  bottom, -skyBoxSize, skyBoxSize, bottom, skyBoxSize,  skyBoxSize, top,    skyBoxSize,  skyBoxSize, top,    -skyBoxSize, black);
            buf.appendPositionColorQuadUnchecked(-skyBoxSize, top,    -skyBoxSize, -skyBoxSize, top,    skyBoxSize, -skyBoxSize, bottom, skyBoxSize, -skyBoxSize, bottom, -skyBoxSize, black);
            buf.appendPositionColorQuadUnchecked(-skyBoxSize, bottom, -skyBoxSize, -skyBoxSize, bottom, skyBoxSize,  skyBoxSize, bottom, skyBoxSize,  skyBoxSize, bottom, -skyBoxSize, black);
            NTMImmediate.INSTANCE.draw();
        }

        if (world.provider.isSkyColored()) {
            GlStateManager.color(red * 0.2F + 0.04F, green * 0.2F + 0.04F, blue * 0.6F + 0.1F);
        } else {
            GlStateManager.color(red, green, blue);
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0F, -((float) (horizonOffset - 16.0D)), 0.0F);
        this.drawPositionVbo(this.sky2VBO);
        GlStateManager.popMatrix();
        GlStateManager.enableTexture2D();
        GlStateManager.depthMask(true);
    }

    private void generateSky() {
        NTMBufferBuilder buffer = NTMImmediate.INSTANCE.beginPositionQuads(169);
        this.renderSky(buffer, 16.0F, false);
        BufferBuilder vanilla = buffer.vanilla();
        vanilla.finishDrawing();
        vanilla.reset();
        this.skyVBO.bufferData(vanilla.getByteBuffer());
    }

    private void generateSky2() {
        NTMBufferBuilder buffer = NTMImmediate.INSTANCE.beginPositionQuads(169);
        this.renderSky(buffer, -16.0F, true);
        BufferBuilder vanilla = buffer.vanilla();
        vanilla.finishDrawing();
        vanilla.reset();
        this.sky2VBO.bufferData(vanilla.getByteBuffer());
    }

    private void renderSky(NTMBufferBuilder bufferBuilder, float posY, boolean reverseX) {
        for (int x = -384; x <= 384; x += 64) {
            for (int z = -384; z <= 384; z += 64) {
                float minX = x;
                float maxX = x + 64;

                if (reverseX) {
                    minX = x + 64;
                    maxX = x;
                }

                bufferBuilder.appendPositionQuadUnchecked(
                        minX, posY, z,
                        maxX, posY, z,
                        maxX, posY, z + 64,
                        minX, posY, z + 64
                );
            }
        }
    }

    private void generateStars() {
        NTMBufferBuilder buffer = NTMImmediate.INSTANCE.beginPositionQuads(1500);
        this.renderStars(buffer);
        BufferBuilder vanilla = buffer.vanilla();
        vanilla.finishDrawing();
        vanilla.reset();
        this.starVBO.bufferData(vanilla.getByteBuffer());
    }

    private void renderStars(NTMBufferBuilder buffer) {
        Random random = new Random(10842L);

        for (int i = 0; i < 1500; ++i) {
            double x = random.nextFloat() * 2.0F - 1.0F;
            double y = random.nextFloat() * 2.0F - 1.0F;
            double z = random.nextFloat() * 2.0F - 1.0F;
            double size = 0.15F + random.nextFloat() * 0.1F;
            double length = x * x + y * y + z * z;

            if (length < 1.0D && length > 0.01D) {
                length = 1.0D / Math.sqrt(length);
                x *= length;
                y *= length;
                z *= length;
                double pointX = x * 100.0D;
                double pointY = y * 100.0D;
                double pointZ = z * 100.0D;
                double yaw = Math.atan2(x, z);
                double sinYaw = Math.sin(yaw);
                double cosYaw = Math.cos(yaw);
                double pitch = Math.atan2(Math.sqrt(x * x + z * z), y);
                double sinPitch = Math.sin(pitch);
                double cosPitch = Math.cos(pitch);
                double roll = random.nextDouble() * Math.PI * 2.0D;
                double sinRoll = Math.sin(roll);
                double cosRoll = Math.cos(roll);

                for (int corner = 0; corner < 4; ++corner) {
                    double offsetY = ((corner & 2) - 1) * size;
                    double offsetZ = ((corner + 1 & 2) - 1) * size;
                    double rotatedY = offsetY * cosRoll - offsetZ * sinRoll;
                    double rotatedZ = offsetZ * cosRoll + offsetY * sinRoll;
                    double pitchedY = rotatedY * sinPitch;
                    double pitchedZ = -rotatedY * cosPitch;
                    double finalX = pitchedZ * sinYaw - rotatedZ * cosYaw;
                    double finalZ = rotatedZ * sinYaw + pitchedZ * cosYaw;
                    buffer.appendPositionUnchecked((float) (pointX + finalX), (float) (pointY + pitchedY), (float) (pointZ + finalZ));
                }
            }
        }
    }

    private void drawPositionVbo(VertexBuffer vertexBuffer) {
        vertexBuffer.bindBuffer();
        GlStateManager.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        GlStateManager.glVertexPointer(3, GL11.GL_FLOAT, 12, 0);
        vertexBuffer.drawArrays(GL11.GL_QUADS);
        vertexBuffer.unbindBuffer();
        GlStateManager.glDisableClientState(GL11.GL_VERTEX_ARRAY);
    }
}
