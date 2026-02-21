package com.hbm.render.tileentity;

import com.hbm.blocks.ModBlocks;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.item.ItemRenderBase;
import com.hbm.tileentity.machine.TileEntityMachinePress;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.ForgeHooksClient;
@AutoRegister
public class RenderPress extends TileEntitySpecialRenderer<TileEntityMachinePress> implements IItemRendererProvider {

	/*private static final float[] IDENTITY_MATRIX =
			new float[] {
				1.0f, 0.0f, 0.0f, 0.0f,
				0.0f, 1.0f, 0.0f, 0.0f,
				0.0f, 0.0f, 1.0f, 0.0f,
				0.0f, 0.0f, 0.0f, 1.0f };
	
	private static final FloatBuffer matrix = BufferUtils.createFloatBuffer(16);*/
	public static Vec3d pos = new Vec3d(0, 0, 0);
	
	public RenderPress() {
		super();
	}

	// mlbv: What happened to this?
	@Override
	public void render(TileEntityMachinePress te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		pos = new Vec3d(x, y, z);
		/*if(MainRegistry.useShaders) {
			if(!ModEventHandlerClient.tester.containsKey(te))
				ModEventHandlerClient.tester.put(te, new Flashlight(new Vec3d(te.getPos().getX() + 0.5, te.getPos().getY() + 0.5, te.getPos().getZ() + 0.5), new Vec3d(1, 0, 0), 10));
		}*/
		/* GL11.glEnable(GL11.GL_STENCIL_TEST);
		    
		    
		    GL11.glStencilMask(0xFF);
		    GL11.glClearStencil(0);
		    GlStateManager.clear(GL11.GL_STENCIL_BUFFER_BIT );
		    GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xFF);
		    GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_REPLACE);
		    GL11.glColorMask(false, false, false, false);
		    GL11.glDepthMask(false);
		    GlStateManager.disableTexture2D();
		    GL11.glBegin(GL11.GL_QUADS);
		    GL11.glVertex3d(x, y, z);
		    GL11.glVertex3d(x, y, z+1);
		    GL11.glVertex3d(x, y+1, z+1);
		    GL11.glVertex3d(x, y+1, z);
		    GL11.glEnd();
		    GlStateManager.enableTexture2D();
		    GL11.glDepthMask(true);
		    GL11.glColorMask(true, true, true, true);
		    GL11.glStencilMask(0x00);
		    GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF);
		    GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
		
		FloatBuffer oldMatrix = GLAllocation.createDirectFloatBuffer(16);
		GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, oldMatrix);
		float[] duck = RenderHelper.getScreenAreaFromQuad(new Vec3d(x, y, z), new Vec3d(x, y, z + 1),
		        new Vec3d(x, y + 1, z + 1), new Vec3d(x, y + 1, z));
		if (duck != null) {
		    
			
			GlStateManager.matrixMode(GL11.GL_PROJECTION);
		    GlStateManager.loadIdentity();
		    float rightFactor = Minecraft.getMinecraft().displayWidth/duck[2];
		    float leftFactor = Minecraft.getMinecraft().displayWidth/duck[0];
		    float topFactor = Minecraft.getMinecraft().displayHeight/duck[3];
		    float bottomFactor = Minecraft.getMinecraft().displayHeight/duck[1];
		    
		    float testl = (float) (((Math.floor(Minecraft.getMinecraft().displayWidth/leftFactor)*leftFactor)/Minecraft.getMinecraft().displayWidth)*leftFactor);
		    
		    this.setupProjMatrix(rightFactor, leftFactor, topFactor, bottomFactor, 0.05F, (Minecraft.getMinecraft().gameSettings.renderDistanceChunks * 16) * MathHelper.SQRT_2);
		    //this.setupProjMatrix(testr, testl, testt, testb, 0.05F, (Minecraft.getMinecraft().gameSettings.renderDistanceChunks * 16) * MathHelper.SQRT_2);
		    
		    IntBuffer vp = GLAllocation.createDirectIntBuffer(16);
			
		    GlStateManager.viewport((int) (Minecraft.getMinecraft().displayWidth/leftFactor), (int) (Minecraft.getMinecraft().displayHeight/bottomFactor), (int) (Minecraft.getMinecraft().displayWidth/rightFactor-Minecraft.getMinecraft().displayWidth/leftFactor), (int) (Minecraft.getMinecraft().displayHeight/topFactor-Minecraft.getMinecraft().displayHeight/bottomFactor));
		    GL11.glGetInteger(GL11.GL_VIEWPORT, vp);
		
		    
		    GlStateManager.matrixMode(GL11.GL_MODELVIEW);
		}*/
		
		/*if(te.getPos().getX() % 2 == 0){
			HbmShaderManager.useDissolveShader(Library.remap((float)Math.sin((te.getWorld().getTotalWorldTime() + partialTicks)/10), -1F, 1F, 0F, 1F));
			GlStateManager.enableBlend();
			GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		}*/
		
		/*GL11.glEnable(GL11.GL_CLIP_PLANE0);
		DoubleBuffer buf = GLAllocation.createDirectByteBuffer(8*4).asDoubleBuffer();
		buf.put(new double[]{1, 0, 1, 0});
		buf.rewind();
		GlStateManager.pushMatrix();
		GlStateManager.translate(x + 0.5D, y, z + 0.5D);
		GL11.glClipPlane(GL11.GL_CLIP_PLANE0, buf);
		GlStateManager.popMatrix();*/
		
		GlStateManager.pushMatrix();

		GlStateManager.translate(x + 0.5D, y, z + 0.5D);
		GlStateManager.enableLighting();
		GlStateManager.rotate(180, 0F, 1F, 0F);
		
		
		
		//ColladaLoader.test.apply(2000);
		//test.applyAll();
		this.bindTexture(ResourceManager.press_body_tex);
		ResourceManager.press_body.renderAll();

		
		renderTileEntityAt2(te, x, y, z, partialTicks);
		GlStateManager.popMatrix();
		//GL11.glDisable(GL11.GL_CLIP_PLANE0);
		//if(model.controller.getAnim() == AnimationWrapper.EMPTY)
		//	model.controller.setAnim(new AnimationWrapper(anim_test).onEnd(new EndResult(EndType.REPEAT_REVERSE, null)));
		/*GlStateManager.pushMatrix();
		if(ResourceManager.model.controller.getAnim() == AnimationWrapper.EMPTY)
			ResourceManager.model.controller.setAnim(new AnimationWrapper(ResourceManager.anim_test).onEnd(new EndResult(EndType.REPEAT, null)));
		GlStateManager.translate(x+0.5, y+4, z+0.5);
		this.bindTexture(ResourceManager.turbofan_blades_tex);
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		GlStateManager.disableCull();
		ResourceManager.model.renderAnimated(System.currentTimeMillis());
		GlStateManager.enableCull();
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.popMatrix();*/
		/*if(te.getPos().getX() % 2 == 0){
			GlStateManager.disableBlend();
			HbmShaderManager.releaseShader2();
		}*/
		
		// GlStateManager.matrixMode(GL11.GL_PROJECTION);
		// GlStateManager.loadIdentity();
		// GL11.glLoadMatrix(oldMatrix);
		// GlStateManager.matrixMode(GL11.GL_MODELVIEW);
		// GlStateManager.viewport(0, 0, Minecraft.getMinecraft().displayWidth,
		// Minecraft.getMinecraft().displayHeight);
		// GL11.glDisable(GL11.GL_STENCIL_TEST);
	}

	public void renderTileEntityAt2(TileEntity tileEntity, double x, double y, double z, float f) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(0, 1 - 0.125D, 0);
		GlStateManager.scale(0.95F, 1, 0.95F);

		TileEntityMachinePress press = (TileEntityMachinePress) tileEntity;
		float f1 = press.progress * (1 - 0.125F) / TileEntityMachinePress.maxProgress;
		GlStateManager.translate(0, -f1, 0);
		this.bindTexture(ResourceManager.press_head_tex);

		ResourceManager.press_head.renderAll();

		GlStateManager.popMatrix();
		renderTileEntityAt3(tileEntity, x, y, z, f);
	}

	public void renderTileEntityAt3(TileEntity tileEntity, double x, double y, double z, float f) {
		GlStateManager.translate(0, 1, -1);
		GlStateManager.enableLighting();
		GlStateManager.rotate(180, 0F, 1F, 0F);
		GlStateManager.rotate(-90, 1F, 0F, 0F);
		
		TileEntityMachinePress press = (TileEntityMachinePress) tileEntity;
		ItemStack stack = press.syncStack;

		if(stack != null && !(stack.getItem() instanceof ItemBlock) && !stack.isEmpty()) {
			IBakedModel model = Minecraft.getMinecraft().getRenderItem().getItemModelWithOverrides(stack, tileEntity.getWorld(), null);
			model = ForgeHooksClient.handleCameraTransforms(model, TransformType.FIXED, false);
			Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			GlStateManager.translate(0.0F, 1.0F, 0.0F);
			GlStateManager.rotate(180, 0F, 1F, 0F);
			GlStateManager.scale(0.5F, 0.5F, 0.5F);
			
			
			Minecraft.getMinecraft().getRenderItem().renderItem(stack, model);
		}

	}

	/**
	 *
	 * @param r
	 *            -
	 * @param l
	 * @param t
	 * @param b
	 * @param zNear
	 * @param zFar
	 */
	/*public void setupProjMatrix(float r, float l, float t, float b, float zNear, float zFar){
		
		float r2, l2, t2, b2, r3, l3, t3, b3;
		float sine, cotangent, deltaZ;
		float radians = Minecraft.getMinecraft().gameSettings.fovSetting*fov / 2 * (float)Math.PI / 180;
	
		deltaZ = zFar - zNear;
		sine = (float) Math.sin(radians);
	
		if ((deltaZ == 0) || (sine == 0) || (1 == 0)) {
			return;
		}
	
		cotangent = (float) Math.cos(radians) / sine;
	
		float aspect = (float)Minecraft.getMinecraft().displayWidth/(float)Minecraft.getMinecraft().displayHeight;
		float duck = cotangent/aspect;
		//System.out.println(cotangent/aspect + " " + cotangent + " " + cotangent/(cotangent/aspect) + " " + Minecraft.getMinecraft().displayWidth/(float)Minecraft.getMinecraft().displayHeight);
		
		
		
		
		r2 = (float) (((2.0F*zNear)/duck)/2);
		l2 =  -r2;
		t2 = (float) (((2.0F*zNear)/cotangent)/2);
		b2 = -t2;
		
		r3 = (r2+r2)/r-r2;
		l3 = -((l2+l2)/l-l2);
		t3 = (t2+t2)/t-t2;
		b3 = -((b2+b2)/b-b2);
		this.setupProjMatrixDirect(r3, l3, t3, b3, zNear, zFar);
	}
	
	public void setupProjMatrixDirect(float r, float l, float t, float b, float zNear, float zFar){
	
		
		
		
		__gluMakeIdentityf(matrix);
	//2*zNear / (r-l))
		//2*zNear / (t-b)
		matrix.put(0 * 4 + 0, 2*zNear / (r-l));
		matrix.put(1 * 4 + 1, 2*zNear / (t-b));
		matrix.put(2*4 + 0, (r+l)/(r-l));
		matrix.put(2*4 + 1, (t+b)/(t-b));
		matrix.put(2 * 4 + 2, - (zFar + zNear) / (zFar-zNear));
		matrix.put(2 * 4 + 3, -1);
		matrix.put(3 * 4 + 2, -2 * zNear * zFar / (zFar-zNear));
		matrix.put(3 * 4 + 3, 0);
	
		//glMultMatrix(matrix);
		GL11.glLoadMatrix(matrix);
	}
	
	private static void __gluMakeIdentityf(FloatBuffer m) {
		int oldPos = m.position();
		m.put(IDENTITY_MATRIX);
		m.position(oldPos);
	}
	
	public float normalize(float value, float min, float max, float mult) {
		float normalized = (value - min) / (max - min);
		return normalized*mult;
	}
	public float normalize(float value, float min, float max){
		return this.normalize(value, min, max, 1.0f);
	}*/

	@Override
	public Item getItemForRenderer() {
		return Item.getItemFromBlock(ModBlocks.machine_press);
	}

	@Override
	public ItemRenderBase getRenderer(Item item) {
		return new ItemRenderBase() {
			public void renderInventory() {
                GlStateManager.translate(0, -4, 0);
				GlStateManager.scale(4.5, 4.5, 4.5);
            }

            public void renderCommon() {
                bindTexture(ResourceManager.press_body_tex);
                ResourceManager.press_body.renderAll();
				GlStateManager.translate(0, 0.5, 0);
                bindTexture(ResourceManager.press_head_tex);
                ResourceManager.press_head.renderAll();
            }
		};
	}

}
