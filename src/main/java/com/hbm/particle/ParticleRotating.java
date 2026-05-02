package com.hbm.particle;

import com.hbm.render.util.NTMBufferBuilder;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class ParticleRotating extends Particle {

  protected ParticleRotating(World world, double x, double y, double z) {
    super(world, x, y, z);
  }

  @Override
  public int getFXLayer() {
    return 1;
  }

  public void renderParticleRotated(
      BufferBuilder buffer,
      float partialTicks,
      float sX,
      float sY,
      float sZ,
      float dX,
      float dZ,
      float scale) {

    float pX = (float) (this.prevPosX + (this.posX - this.prevPosX) * (double) partialTicks - interpPosX);
    float pY = (float) (this.prevPosY + (this.posY - this.prevPosY) * (double) partialTicks - interpPosY);
    float pZ = (float) (this.prevPosZ + (this.posZ - this.prevPosZ) * (double) partialTicks - interpPosZ);
    float rotation =
        this.prevParticleAngle + (this.particleAngle - this.prevParticleAngle) * partialTicks;

    float x1 = -sX * scale - dX * scale;
    float y1 = -sY * scale;
    float z1 = -sZ * scale - dZ * scale;
    float x2 = -sX * scale + dX * scale;
    float y2 = sY * scale;
    float z2 = -sZ * scale + dZ * scale;
    float x3 = sX * scale + dX * scale;
    float y3 = sY * scale;
    float z3 = sZ * scale + dZ * scale;
    float x4 = sX * scale - dX * scale;
    float y4 = -sY * scale;
    float z4 = sZ * scale - dZ * scale;

    float nX = (y2 - y1) * (z3 - z1) - (z2 - z1) * (y3 - y1);
    float nY = (z2 - z1) * (x3 - x1) - (x2 - x1) * (z3 - z1);
    float nZ = (x2 - x1) * (y3 - y1) - (y2 - y1) * (x3 - x1);

    Vec3d vec = new Vec3d(nX, nY, nZ).normalize();
    nX = (float) vec.x;
    nY = (float) vec.y;
    nZ = (float) vec.z;

    float cosTh = (float) Math.cos(rotation * Math.PI / 180D);
    float sinTh = (float) Math.sin(rotation * Math.PI / 180D);

    float x01 = x1 * cosTh + (nY * z1 - nZ * y1) * sinTh;
    float y01 = y1 * cosTh + (nZ * x1 - nX * z1) * sinTh;
    float z01 = z1 * cosTh + (nX * y1 - nY * x1) * sinTh;
    float x02 = x2 * cosTh + (nY * z2 - nZ * y2) * sinTh;
    float y02 = y2 * cosTh + (nZ * x2 - nX * z2) * sinTh;
    float z02 = z2 * cosTh + (nX * y2 - nY * x2) * sinTh;
    float x03 = x3 * cosTh + (nY * z3 - nZ * y3) * sinTh;
    float y03 = y3 * cosTh + (nZ * x3 - nX * z3) * sinTh;
    float z03 = z3 * cosTh + (nX * y3 - nY * x3) * sinTh;
    float x04 = x4 * cosTh + (nY * z4 - nZ * y4) * sinTh;
    float y04 = y4 * cosTh + (nZ * x4 - nX * z4) * sinTh;
    float z04 = z4 * cosTh + (nX * y4 - nY * x4) * sinTh;

    NTMBufferBuilder fastBuffer = (NTMBufferBuilder) buffer;
    int packedColor = NTMBufferBuilder.packColor(particleRed, particleGreen, particleBlue, particleAlpha);
    int packedLightmap = NTMBufferBuilder.packLightmap(240, 0);
    fastBuffer.appendParticlePositionTexColorLmap(
        pX + x01, pY + y01, pZ + z01,
        particleTexture.getMaxU(), particleTexture.getMaxV(),
        packedColor, packedLightmap);
    fastBuffer.appendParticlePositionTexColorLmap(
        pX + x02, pY + y02, pZ + z02,
        particleTexture.getMaxU(), particleTexture.getMinV(),
        packedColor, packedLightmap);
    fastBuffer.appendParticlePositionTexColorLmap(
        pX + x03, pY + y03, pZ + z03,
        particleTexture.getMinU(), particleTexture.getMinV(),
        packedColor, packedLightmap);
    fastBuffer.appendParticlePositionTexColorLmap(
        pX + x04, pY + y04, pZ + z04,
        particleTexture.getMinU(), particleTexture.getMaxV(),
        packedColor, packedLightmap);
  }

  public void renderParticleRotated(
      BufferBuilder buffer,
      float partialTicks,
      float sX,
      float sY,
      float sZ,
      float dX,
      float dZ,
      double scale) {
    renderParticleRotated(buffer, partialTicks, sX, sY, sZ, dX, dZ, (float) scale);
  }

  public double getPosX(){
    return this.posX;
  }

  public double getPosY(){
    return this.posY;
  }

  public double getPosZ(){
    return this.posZ;
  }
}
