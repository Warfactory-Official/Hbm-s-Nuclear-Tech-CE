package com.hbm.items.gear;

import com.hbm.Tags;
import com.hbm.api.item.IGasMask;
import com.hbm.handler.ArmorUtil;
import com.hbm.items.ModItems;
import com.hbm.render.model.ModelM65;
import com.hbm.util.ArmorRegistry.HazardClass;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class ArmorHazmatMask extends ArmorHazmat implements IGasMask {

	@SideOnly(Side.CLIENT)
	private ModelM65 modelM65;

	public ArmorHazmatMask(ArmorMaterial materialIn, int renderIndexIn, EntityEquipmentSlot equipmentSlotIn, String s) {
		super(materialIn, renderIndexIn, equipmentSlotIn, s);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public ModelBiped getArmorModel(@NotNull EntityLivingBase entityLiving, @NotNull ItemStack itemStack, @NotNull EntityEquipmentSlot armorSlot, @NotNull ModelBiped _default) {
		if ((this == ModItems.hazmat_helmet_red || this == ModItems.hazmat_helmet_grey) && armorSlot == EntityEquipmentSlot.HEAD) {
			if (this.modelM65 == null) {
				this.modelM65 = new ModelM65();
			}
			return this.modelM65;
		}

		return null;
	}

	@Override
	public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
		if (stack.getItem() == ModItems.hazmat_helmet) {
			return Tags.MODID + ":textures/armor/hazmat_1.png";
		}
		if (stack.getItem() == ModItems.hazmat_helmet_red) {
			return Tags.MODID + ":textures/armor/ModelHazRed.png";
		}
		if (stack.getItem() == ModItems.hazmat_helmet_grey) {
			return Tags.MODID + ":textures/armor/ModelHazGrey.png";
		}
		if (stack.getItem() == ModItems.hazmat_paa_helmet) {
			return Tags.MODID + ":textures/armor/hazmat_paa_1.png";
		}

		return super.getArmorTexture(stack, entity, slot, type);
	}

	@Override
	public void addInformation(@NotNull ItemStack stack, World worldIn, @NotNull List<String> list, @NotNull ITooltipFlag flagIn) {
		super.addInformation(stack, worldIn, list, flagIn);
		ArmorUtil.addGasMaskTooltip(stack, worldIn, list, flagIn);
	}

	@Override
	public List<HazardClass> getBlacklist(ItemStack stack) {
		return Collections.emptyList();
	}

	@Override
	public @NotNull ItemStack getFilter(ItemStack stack) {
		return ArmorUtil.getGasMaskFilter(stack);
	}

	@Override
	public boolean isFilterApplicable(ItemStack stack, ItemStack filter) {
		return true;
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
	public @NotNull ActionResult<ItemStack> onItemRightClick(@NotNull World world, EntityPlayer player, @NotNull EnumHand hand) {
		if (player.isSneaking()) {
			ItemStack stack = player.getHeldItem(hand);
			ItemStack filter = this.getFilter(stack);

			if (!filter.isEmpty()) {
				ArmorUtil.removeFilter(stack);

				if (!player.inventory.addItemStackToInventory(filter)) {
					player.dropItem(filter, true, false);
				}
			}
		}
		return super.onItemRightClick(world, player, hand);
	}
}
