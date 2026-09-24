package com.hbm.render.entity.mob;

import com.hbm.entity.mob.EntityBlockSpider;
import com.hbm.interfaces.AutoRegister;
import com.hbm.main.ResourceManager;
import com.hbm.render.model.ModelBlockSpider;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import org.jetbrains.annotations.NotNull;

@AutoRegister(entity = EntityBlockSpider.class, factory = "FACTORY")
public class RenderBlockSpider extends RenderLiving<EntityBlockSpider> {

	public static final IRenderFactory<EntityBlockSpider> FACTORY = RenderBlockSpider::new;

	public RenderBlockSpider(RenderManager manager) {
		super(manager, new ModelBlockSpider(), 1.0F);
	}

	@Override
	protected @NotNull ResourceLocation getEntityTexture(@NotNull EntityBlockSpider entity) {
		return ResourceManager.spider_tex;
	}
}
