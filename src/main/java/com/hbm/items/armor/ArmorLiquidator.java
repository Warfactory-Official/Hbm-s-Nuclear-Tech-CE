package com.hbm.items.armor;

import com.google.common.collect.Multimap;
import com.hbm.Tags;
import com.hbm.api.item.IGasMask;
import com.hbm.handler.ArmorModHandler;
import com.hbm.handler.ArmorUtil;
import com.hbm.items.ModItems;
import com.hbm.items.gear.ArmorFSB;
import com.hbm.render.model.ModelM65;
import com.hbm.util.ArmorRegistry.HazardClass;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import java.util.Collections;
import java.util.List;

public class ArmorLiquidator extends ArmorFSB implements IGasMask {

	@SideOnly(Side.CLIENT)
	private ModelM65 model;
	private ResourceLocation hazmatBlur = new ResourceLocation(Tags.MODID + ":textures/misc/overlay_dark.png");
	
	public ArmorLiquidator(ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot equipmentSlotIn, String texture, String name) {
		super(materialIn, renderIndexIn, equipmentSlotIn, texture, name);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ModelBiped getArmorModel(EntityLivingBase entityLiving, ItemStack itemStack, EntityEquipmentSlot armorSlot, ModelBiped _default) {
		if (this == ModItems.liquidator_helmet) {
			if (armorSlot == EntityEquipmentSlot.HEAD) {
				if (this.model == null) {
					this.model = new ModelM65();
				}
				return this.model;
			}
		}
		return super.getArmorModel(entityLiving, itemStack, armorSlot, _default);
	}
	
	@Override
	public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot equipmentSlot) {
		Multimap<String, AttributeModifier> map = super.getItemAttributeModifiers(equipmentSlot);
		if(equipmentSlot == this.armorType){
			map.put(SharedMonsterAttributes.KNOCKBACK_RESISTANCE.getName(), new AttributeModifier(ArmorModHandler.fixedUUIDs[this.armorType.getIndex()], "Armor modifier", 100D, 0));
			map.put(SharedMonsterAttributes.MOVEMENT_SPEED.getName(), new AttributeModifier(ArmorModHandler.fixedUUIDs[this.armorType.getIndex()], "Armor modifier", (double) -0.1D, 1));
		}
		return map;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void renderHelmetOverlay(ItemStack stack, EntityPlayer player, ScaledResolution resolution, float partialTicks) {
		GlStateManager.disableDepth();
		GlStateManager.depthMask(false);
		GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.color(1, 1, 1, 1);
		GlStateManager.disableAlpha();
		Minecraft.getMinecraft().getTextureManager().bindTexture(hazmatBlur);
		Tessellator tes = Tessellator.getInstance();
		BufferBuilder buf = tes.getBuffer();
		buf.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
		buf.pos(0.0D, (double) resolution.getScaledHeight(), -90.0D).tex(0, 1).endVertex();
		buf.pos((double) resolution.getScaledWidth(), (double) resolution.getScaledHeight(), -90.0D).tex(1, 1).endVertex();
		buf.pos((double) resolution.getScaledWidth(), 0.0D, -90.0D).tex(1, 0).endVertex();
		buf.pos(0.0D, 0.0D, -90.0D).tex(0, 0).endVertex();
		tes.draw();
		GlStateManager.depthMask(true);
		GlStateManager.enableDepth();
		GlStateManager.enableAlpha();
		GlStateManager.color(1, 1, 1, 1);
	}
	
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn){
		super.addInformation(stack, worldIn, list, flagIn);
		if (this == ModItems.liquidator_helmet)
			ArmorUtil.addGasMaskTooltip(stack, worldIn, list, flagIn);
	}

	@Override
	public List<HazardClass> getBlacklist(ItemStack stack) {
		return Collections.emptyList(); // full hood has no restrictions
	}

	@Override
	public @NotNull ItemStack getFilter(ItemStack stack) {
		return ArmorUtil.getGasMaskFilter(stack);
	}

	@Override
	public void installFilter(ItemStack stack, ItemStack filter) {
		ArmorUtil.installGasMaskFilter(stack, filter);
	}

	@Override
	public void damageFilter(ItemStack stack, int damage) {
		ArmorUtil.damageGasMaskFilter(stack, damage);
	}

	@Override
	public boolean isFilterApplicable(ItemStack stack, ItemStack filter) {
		return true;
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		if (this == ModItems.liquidator_helmet){
			if(player.isSneaking()) {
				ItemStack stack = player.getHeldItem(hand);
				ItemStack filter = this.getFilter(stack);
				
				if(filter != null) {
					ArmorUtil.removeFilter(stack);
					
					if(!player.inventory.addItemStackToInventory(filter)) {
						player.dropItem(filter, true, false);
					}
				}
			}
		}
		return super.onItemRightClick(world, player, hand);
	}
}
