package com.hbm.physics;

import com.hbm.render.amlfrom1710.Vec3;
import com.hbm.render.util.NTMImmediate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;

public class ParticlePhysicsBlocks extends Particle {

	public RigidBody body;
	public AxisAlignedBB[] boxes;
	public BlockPos[] blocks;
	public BlockPos createPos;
	public boolean didInit = false;
	int callListId;

	public ParticlePhysicsBlocks(World world, double posXIn, double posYIn, double posZIn, BlockPos createPos, BlockPos[] blocks) {
		super(world, posXIn, posYIn, posZIn);
		this.blocks = blocks;
		this.createPos = createPos;
		init();
	}
	
	public void init(){
		int offsetY = 0;
		body = new RigidBody(world, posX, posY, posZ);
		ArrayList<AABBCollider> boxs = new ArrayList<>();
		for(BlockPos pos : blocks){
			ArrayList<AxisAlignedBB> boxes2 = new ArrayList<>();
			IBlockState state = world.getBlockState(pos);
			state.addCollisionBoxToList(world, pos, TileEntity.INFINITE_EXTENT_AABB, boxes2, null, false);
			for(AxisAlignedBB box : boxes2){
				boolean light = state.getBlock() == Blocks.LEAVES || state.getBlock() == Blocks.LEAVES2;
				float mass = light ? 0.25F : 1F;
				boxs.add(new AABBCollider(box.offset(-posX, -posY + offsetY, -posZ), mass));
			}
		}
		boxes = new AxisAlignedBB[boxs.size()];
		for(int i = 0; i < boxes.length; i ++){
			boxes[i] = boxs.get(i).box;
		}
		body.addColliders(boxs.toArray(new AABBCollider[0]));
		Vec3d impulse = Minecraft.getMinecraft().player.getLookVec().scale(0.6*body.mass);
		body.impulseVelocity(new Vec3(impulse.x, 0, impulse.z), new Vec3(posX + 0.5, posY, posZ + 0.5));
		body.friction = 0.8F;
		particleMaxAge = 1000;
		
		
		callListId = GL11.glGenLists(1);
		GL11.glNewList(callListId, GL11.GL_COMPILE);
		BufferBuilder buf = NTMImmediate.INSTANCE.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
		for(BlockPos pos : blocks){
			IBlockState state = world.getBlockState(pos);
			IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(state);
			Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer().renderModelSmooth(world, model, state, pos.add(0, offsetY, 0), buf, true, MathHelper.getPositionRandom(pos));
		}
		NTMImmediate.INSTANCE.draw();
		GL11.glEndList();
	}
	
	@Override
	public void onUpdate() {
		body.minecraftTimestep();
		this.particleAge ++;
		if(particleAge >= particleMaxAge){
			setExpired();
			GL11.glDeleteLists(callListId, 1);
		}
	}
	
	@Override
	public int getFXLayer() {
		return 3;
	}
	
	@Override
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ) {
		GlStateManager.pushMatrix();
		double entPosX = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX)*partialTicks;
        double entPosY = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY)*partialTicks;
        double entPosZ = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ)*partialTicks;
        
        interpPosX = entPosX;
        interpPosY = entPosY;
        interpPosZ = entPosZ;
        GlStateManager.pushMatrix();
        //OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240, 240);
        
        GlStateManager.disableTexture2D();
		GlStateManager.glLineWidth(4);
		//RenderGlobal.drawSelectionBoundingBox(body.boundingBox.offset(-interpPosX, -interpPosY, -interpPosZ), 0, 1, 0, 1);
		GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
        for(Contact c : body.contacts.contacts){
			if(c != null && false){
				Tessellator tes = Tessellator.getInstance();
				BufferBuilder buf = tes.getBuffer();
				buf.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
				Vec3 normal = c.normal.mult(0.5F);
				Vec3 globalA = c.globalA.subtract(interpPosX, interpPosY, interpPosZ);
				Vec3 globalB = c.globalB.subtract(interpPosX, interpPosY, interpPosZ);
				buf.pos(globalA.xCoord, globalA.yCoord, globalA.zCoord).color(0F, 0F, 1F, 1F).endVertex();
				buf.pos(globalA.xCoord-normal.xCoord, globalA.yCoord-normal.yCoord, globalA.zCoord-normal.xCoord).color(0F, 0F, 1F, 1F).endVertex();
				
				//buf.pos(globalB.xCoord, globalB.yCoord, globalB.zCoord).color(0F, 0F, 1F, 1F).endVertex();
				//buf.pos(globalB.xCoord+normal.xCoord, globalB.yCoord+normal.yCoord, globalB.zCoord+normal.xCoord).color(0F, 0F, 1F, 1F).endVertex();
				tes.draw();
				
				GL11.glPointSize(16);
				buf.begin(GL11.GL_POINTS, DefaultVertexFormats.POSITION_COLOR);
				buf.pos(globalA.xCoord, globalA.yCoord, globalA.zCoord).color(0F, 0F, 1F, 1F).endVertex();
				//buf.pos(globalB.xCoord, globalB.yCoord, globalB.zCoord).color(0F, 0F, 1F, 1F).endVertex();
				tes.draw();
			}
		}
        
		body.doGlTransform(new Vec3(interpPosX, interpPosY, interpPosZ), partialTicks);
		GlStateManager.translate(-createPos.getX(), -createPos.getY(), -createPos.getZ());
		
		GlStateManager.disableTexture2D();
		GlStateManager.glLineWidth(4);
		for(AxisAlignedBB box : boxes){
			//RenderGlobal.drawSelectionBoundingBox(box, 1, collided ? 0 : 1, collided ? 0 : 1, 1);
		}
		GlStateManager.enableTexture2D();
		Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		GlStateManager.enableCull();
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		GL11.glCallList(callListId);
		GlStateManager.shadeModel(GL11.GL_FLAT);
		
		GlStateManager.popMatrix();
	}
}
