package com.hbm.items.armor;

import com.hbm.handler.ArmorModHandler;
import com.hbm.lib.Library;
import com.hbm.render.misc.BeamPronter;
import com.hbm.render.misc.BeamPronter.EnumBeamType;
import com.hbm.render.misc.BeamPronter.EnumWaveType;
import com.hbm.render.model.ModelBackTesla;
import com.hbm.tileentity.machine.TileEntityTesla;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityOcelot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import com.hbm.util.Vec3NT;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderPlayerEvent.Pre;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.GlStateManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public class ItemModTesla extends ItemArmorMod {

	public static final String NBT_STATIC_GEN = "staticGen";
	private static final double COIL_OFFSET = 1.25D;
	private static final double RANGE = 5D;

	/** Client-side arcs per wearer, filled in modUpdate, drawn in renderArcs. */
	@SideOnly(Side.CLIENT)
	private static Map<Entity, List<double[]>> arcs;

	private ModelBackTesla modelTesla;

	public ItemModTesla(String s) {
		super(ArmorModHandler.battery, false, true, false, false, s);
	}

	public static boolean hasStaticGenerator(ItemStack mod) {
		return mod != null && !mod.isEmpty() && mod.hasTagCompound() && mod.getTagCompound().getBoolean(NBT_STATIC_GEN);
	}

	public static ItemStack withStaticGenerator(ItemStack stack) {
		if(!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
		stack.getTagCompound().setBoolean(NBT_STATIC_GEN, true);
		return stack;
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn){
		list.add(TextFormatting.YELLOW + "Zaps nearby entities (requires full electric set)");
		if(hasStaticGenerator(stack)) list.add(TextFormatting.YELLOW + "Static generator: visible arcs, uses no suit power");
		list.add("");
		super.addInformation(stack, worldIn, list, flagIn);
	}

	@SideOnly(Side.CLIENT)
	public void addDesc(List<String> list, ItemStack stack, ItemStack armor) {
		list.add(TextFormatting.YELLOW + stack.getDisplayName() + (hasStaticGenerator(stack) ? " (zaps nearby entities, self-powered)" : " (zaps nearby entities)"));
	}

	@Override
	public void modUpdate(EntityLivingBase entity, ItemStack armor) {

		if(!(entity instanceof EntityPlayer)) return;
		if(!(armor.getItem() instanceof ArmorFSBPowered) || !ArmorFSBPowered.hasFSBArmor((EntityPlayer) entity)) return;

		boolean staticGen = hasStaticGenerator(ArmorModHandler.pryMod(armor, ArmorModHandler.battery));

		// the client only needs the arc endpoints, so it scans for targets itself instead of running zap(), which deals damage
		// and, for player targets, shifts the returned point a full body height down
		if(entity.world.isRemote) {
			setArcs(entity, staticGen ? scanArcTargets(entity) : null);
			return;
		}

		List<double[]> targets = TileEntityTesla.zap(entity.world, entity.posX, entity.posY + COIL_OFFSET, entity.posZ, RANGE, entity);

		if(!staticGen && targets != null && !targets.isEmpty() && entity.getRNG().nextInt(5) == 0) {
			armor.damageItem(1, entity);
		}
	}

	/** Same filters as TileEntityTesla.zap, but without any of its effects - visuals only. */
	@SideOnly(Side.CLIENT)
	private static List<double[]> scanArcTargets(EntityLivingBase source) {

		double x = source.posX;
		double y = source.posY + COIL_OFFSET;
		double z = source.posZ;

		List<double[]> ret = new ArrayList<>();

		for(EntityLivingBase e : source.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(x - RANGE, y - RANGE, z - RANGE, x + RANGE, y + RANGE, z + RANGE))) {

			if(e instanceof EntityOcelot || e == source) continue;

			double ey = e.posY + e.height / 2;

			if(Vec3NT.createVectorHelper(e.posX - x, ey - y, e.posZ - z).length() > RANGE) continue;
			if(Library.isObstructed(source.world, x, y, z, e.posX, ey, e.posZ)) continue;

			ret.add(new double[] {e.posX, ey, e.posZ});
		}

		return ret;
	}

	@SideOnly(Side.CLIENT)
	private static void setArcs(Entity entity, List<double[]> targets) {
		if(arcs == null) arcs = new WeakHashMap<>();
		if(targets == null || targets.isEmpty()) arcs.remove(entity);
		else arcs.put(entity, new ArrayList<>(targets));
	}

	@SideOnly(Side.CLIENT)
	public static void renderArcs(RenderWorldLastEvent event) {

		if(arcs == null || arcs.isEmpty()) return;

		Minecraft mc = Minecraft.getMinecraft();
		Entity cam = mc.getRenderViewEntity();
		if(mc.world == null || cam == null) return;

		float interp = event.getPartialTicks();
		double camX = cam.lastTickPosX + (cam.posX - cam.lastTickPosX) * interp;
		double camY = cam.lastTickPosY + (cam.posY - cam.lastTickPosY) * interp;
		double camZ = cam.lastTickPosZ + (cam.posZ - cam.lastTickPosZ) * interp;

		for(EntityPlayer player : mc.world.playerEntities) {

			List<double[]> targets = arcs.get(player);
			if(targets == null || targets.isEmpty()) continue;

			double px = player.lastTickPosX + (player.posX - player.lastTickPosX) * interp;
			double py = player.lastTickPosY + (player.posY - player.lastTickPosY) * interp + COIL_OFFSET;
			double pz = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * interp;

			GlStateManager.pushMatrix();
			GlStateManager.translate(px - camX, py - camY, pz - camZ);

			for(double[] target : targets) {
				double length = Math.sqrt(Math.pow(target[0] - px, 2) + Math.pow(target[1] - py, 2) + Math.pow(target[2] - pz, 2));
				BeamPronter.prontBeam(new Vec3d(target[0] - px, target[1] - py, target[2] - pz), EnumWaveType.RANDOM, EnumBeamType.SOLID, 0x0051C4, 0x606060,
						(int) (mc.world.getTotalWorldTime() % 1000 + 1), (int) (length * 5), 0.125F, 2, 0.03125F);
			}

			GlStateManager.popMatrix();
		}
	}

	// Th3_Sl1ze: Shading is a bit fucked up btw. Too lazy to deal with it myself
	@Override
	public void modRender(Pre event, ItemStack armor) {
		if(this.modelTesla == null) {
			this.modelTesla = new ModelBackTesla();
		}

		EntityPlayer player = event.getEntityPlayer();

		float interp = event.getPartialRenderTick();
		float pitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * interp;

		GlStateManager.pushMatrix();
		modelTesla.render(player, 0.0F, 0.0F, 0.0F, 0.0F, pitch, 0.0625F);
		GlStateManager.popMatrix();
	}



}
