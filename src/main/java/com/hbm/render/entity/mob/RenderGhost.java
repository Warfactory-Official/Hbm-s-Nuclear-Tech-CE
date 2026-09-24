package com.hbm.render.entity.mob;

import com.hbm.Tags;
import com.hbm.entity.mob.EntityGhost;
import com.hbm.interfaces.AutoRegister;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

@AutoRegister(entity = EntityGhost.class, factory = "FACTORY")
public class RenderGhost extends RenderLiving<EntityGhost> {

	public static final IRenderFactory<EntityGhost> FACTORY = RenderGhost::new;

	private static final ResourceLocation texture = new ResourceLocation(Tags.MODID, "textures/entity/ghost.png");

	public RenderGhost(RenderManager manager) {
		super(manager, new ModelBiped(0.0F), 0.5F);
	}

	@Override
	protected @NotNull ResourceLocation getEntityTexture(@NotNull EntityGhost entity) {
		return texture;
	}

	@Override
	public void doRender(@NotNull EntityGhost entity, double x, double y, double z, float yaw, float partialTicks) {
		GlStateManager.enableBlend();
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0);
		OpenGlHelper.glBlendFunc(770, 771, 1, 0);
		super.doRender(entity, x, y, z, yaw, partialTicks);
		GlStateManager.alphaFunc(GL11.GL_GREATER, 0.1F);
		GlStateManager.disableBlend();
	}
}
