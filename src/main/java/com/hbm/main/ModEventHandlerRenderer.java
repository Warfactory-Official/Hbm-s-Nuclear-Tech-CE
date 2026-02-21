package com.hbm.main;

import com.hbm.blocks.ICustomBlockHighlight;
import com.hbm.config.RadiationConfig;
import com.hbm.handler.pollution.PollutionHandler.PollutionType;
import com.hbm.items.ModItems;
import com.hbm.items.armor.IArmorDisableModel;
import com.hbm.items.weapon.sedna.factory.XFactoryDrill;
import com.hbm.packet.PermaSyncHandler;
import com.hbm.render.item.weapon.sedna.ItemRenderWeaponBase;
import com.hbm.util.EnumUtil;
import com.hbm.util.ShaderHelper;
import com.hbm.util.Vec3NT;
import com.hbm.world.biome.BiomeGenCraterBase;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.EntityViewRenderEvent.FogColors;
import net.minecraftforge.client.event.EntityViewRenderEvent.FogDensity;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

public class ModEventHandlerRenderer {
	private static final boolean[] partsHidden = new boolean[7];
	
	@SubscribeEvent
	public void onDrawHighlight(DrawBlockHighlightEvent event) {
		RayTraceResult mop = event.getTarget();
		
		if(mop != null && mop.typeOfHit == RayTraceResult.Type.BLOCK) {
			Block b = event.getPlayer().world.getBlockState(mop.getBlockPos()).getBlock();
			if(b instanceof ICustomBlockHighlight cus) {

                if(cus.shouldDrawHighlight(event.getPlayer().world, mop.getBlockPos())) {
					cus.drawHighlight(event, event.getPlayer().world, mop.getBlockPos());
					event.setCanceled(true);
				}
			}
		}
		EntityPlayer player = MainRegistry.proxy.me();
		ItemStack held = player.getHeldItemMainhand();

		if (!held.isEmpty() && held.getItem() == ModItems.gun_drill) {
			XFactoryDrill.drawBlockHighlight(player, held, event.getPartialTicks());
			event.setCanceled(true);
        }
	}
	
	float renderSoot = 0;

	@SubscribeEvent
	public void worldTick(TickEvent.WorldTickEvent event) {

		if(event.phase == TickEvent.WorldTickEvent.Phase.START && RadiationConfig.enableSootFog) {

			float step = 0.05F;
			float soot = PermaSyncHandler.pollution[PollutionType.SOOT.ordinal()];

			if(Math.abs(renderSoot - soot) < step) {
				renderSoot = soot;
			} else if(renderSoot < soot) {
				renderSoot += step;
			} else if(renderSoot > soot) {
				renderSoot -= step;
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void thickenFog(FogDensity event) {

		float soot = (float) (renderSoot - RadiationConfig.sootFogThreshold);

		if(soot > 0 && RadiationConfig.enableSootFog) {
			
			float farPlaneDistance = (float) (Minecraft.getMinecraft().gameSettings.renderDistanceChunks * 16);
			float fogDist = farPlaneDistance / (1 + soot * 5F / (float) RadiationConfig.sootFogDivisor);
			GL11.glFogf(GL11.GL_FOG_START, 0);
			GL11.glFogf(GL11.GL_FOG_END, fogDist);

			if(GLContext.getCapabilities().GL_NV_fog_distance) {
				GL11.glFogi(34138, 34139);
			}
			
			event.setCanceled(true);
		}
	}
	
	@SubscribeEvent(priority = EventPriority.LOW)
	public void tintFog(FogColors event) {
		
		float soot = (float) (renderSoot - RadiationConfig.sootFogThreshold);
		float sootColor = 0.15F;
		float sootReq = (float) RadiationConfig.sootFogDivisor;
		if(soot > 0 && RadiationConfig.enableSootFog) {
			float interp = Math.min(soot / sootReq, 1F);
			event.setRed(event.getRed() * (1 - interp) + sootColor * interp);
			event.setGreen(event.getGreen() * (1 - interp) + sootColor * interp);
			event.setBlue(event.getBlue() * (1 - interp) + sootColor * interp);
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void onRenderHand(RenderHandEvent event) {
		if (ShaderHelper.isShadowPass()) {
			return;
		}
		//can't use plaxer.getHeldItem() here because the item rendering persists for a few frames after hitting the switch key
		ItemRenderer itemRenderer = Minecraft.getMinecraft().entityRenderer.itemRenderer;
		ItemStack toRender = itemRenderer.itemStackMainHand;

		if(toRender != null) {
			TileEntityItemStackRenderer render = toRender.getItem().getTileEntityItemStackRenderer();

			if(render instanceof ItemRenderWeaponBase) {
				((ItemRenderWeaponBase) render).setPerspectiveAndRender(toRender, event.getPartialTicks());
				event.setCanceled(true);
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.LOWEST, receiveCanceled = true)
	public void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
		EntityPlayer player = event.getEntityPlayer();
		RenderPlayer renderer = event.getRenderer();

		for (EntityEquipmentSlot slot : EnumUtil.ENTITY_EQUIPMENT_SLOTS) {
			if (slot.getSlotType() != EntityEquipmentSlot.Type.ARMOR)
				continue;

			ItemStack stack = player.getItemStackFromSlot(slot);

			if (!stack.isEmpty() && stack.getItem() instanceof IArmorDisableModel disable) {

                for (int j = 0; j < IArmorDisableModel.EnumPlayerPart.VALUES.length; j++) {
					IArmorDisableModel.EnumPlayerPart type = IArmorDisableModel.EnumPlayerPart.VALUES[j];
					ModelRenderer box = getBoxFromType(renderer, type);

					if (box != null && disable.disablesPart(player, stack, type) && !box.isHidden) {
						partsHidden[j] = true;
						box.isHidden = true;
					}
				}
			}
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
	public void onRenderPlayerPost(RenderPlayerEvent.Post event) {

		RenderPlayer renderer = event.getRenderer();

		for (int j = 0; j < IArmorDisableModel.EnumPlayerPart.VALUES.length; j++) {
			IArmorDisableModel.EnumPlayerPart type = IArmorDisableModel.EnumPlayerPart.VALUES[j];
			if(partsHidden[j]) {
				getBoxFromType(renderer, type).isHidden = false;
			}
		}
	}

	private static ModelRenderer getBoxFromType(RenderPlayer renderer, IArmorDisableModel.EnumPlayerPart part) {

        return switch (part) {
            case BODY -> renderer.getMainModel().bipedBody;
            case HAT -> renderer.getMainModel().bipedHeadwear;
            case HEAD -> renderer.getMainModel().bipedHead;
            case LEFT_ARM -> renderer.getMainModel().bipedLeftArm;
            case LEFT_LEG -> renderer.getMainModel().bipedLeftLeg;
            case RIGHT_ARM -> renderer.getMainModel().bipedRightArm;
            case RIGHT_LEG -> renderer.getMainModel().bipedRightLeg;
        };
	}


	private static boolean fogInit = false;
	private static int fogX;
	private static int fogZ;
	private static Vec3NT fogRGBMultiplier;
	private static boolean doesBiomeApply = false;
	private static long fogTimer = 0;
	
	/** Same procedure as getting the blended sky color but for fog */
	public static Vec3NT getFogBlendColor(World world, int playerX, int playerZ, float red, float green, float blue, double partialTicks) {
		
		long millis = System.currentTimeMillis() - fogTimer;
		if(playerX == fogX && playerZ == fogZ && fogInit && millis < 3000) return fogRGBMultiplier;

		fogInit = true;
		fogTimer = System.currentTimeMillis();
		GameSettings settings = Minecraft.getMinecraft().gameSettings;
		int[] ranges = ForgeModContainer.blendRanges;
		int distance = 0;
		
		if(settings.fancyGraphics && settings.renderDistanceChunks >= 0) {
			distance = ranges[Math.min(settings.renderDistanceChunks, ranges.length - 1)];
		}

		float r = 0F;
		float g = 0F;
		float b = 0F;
		
		int divider = 0;
		doesBiomeApply = false;
		
		for(int x = -distance; x <= distance; x++) {
			for(int z = -distance; z <= distance; z++) {
				BlockPos pos = new BlockPos(playerX + x, 150, playerZ + z);
				Biome biome = world.getBiomeForCoordsBody(pos);
				Vec3NT color = getBiomeFogColors(world, biome, red, green, blue, pos, partialTicks);
				r += (float) color.x;
				g += (float) color.y;
				b += (float) color.z;
				divider++;
			}
		}

		fogX = playerX;
		fogZ = playerZ;
		
		if(doesBiomeApply) {
			fogRGBMultiplier = new Vec3NT(r / divider, g / divider, b / divider);
		} else {
			fogRGBMultiplier = null;
		}

		return fogRGBMultiplier;
	}
	
	/** Returns the current biome's fog color adjusted for brightness if in a crater, or the world's cached fog color if not */
	public static Vec3NT getBiomeFogColors(World world, Biome biome, float r, float g, float b, BlockPos pos, double partialTicks) {
		
		if(biome instanceof BiomeGenCraterBase) {
			int color = biome.getSkyColorByTemp(biome.getTemperature(pos));
			r = ((color & 0xff0000) >> 16) / 255F;
			g = ((color & 0x00ff00) >> 8) / 255F;
			b = (color & 0x0000ff) / 255F;
			
			float celestialAngle = world.getCelestialAngle((float) partialTicks);
			float skyBrightness = MathHelper.clamp(MathHelper.cos(celestialAngle * (float) Math.PI * 2.0F) * 2.0F + 0.5F, 0F, 1F);
			r *= skyBrightness;
			g *= skyBrightness;
			b *= skyBrightness;
			
			doesBiomeApply = true;
		}
		
		return new Vec3NT(r, g, b);
	}
}
