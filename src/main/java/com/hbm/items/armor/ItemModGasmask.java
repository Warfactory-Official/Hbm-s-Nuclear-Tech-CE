package com.hbm.items.armor;

import com.hbm.api.item.IGasMask;
import com.hbm.handler.ArmorModHandler;
import com.hbm.handler.ArmorUtil;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.render.model.ModelM65;
import com.hbm.util.ArmorRegistry.HazardClass;
import com.hbm.util.I18nUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderPlayerEvent.Pre;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class ItemModGasmask extends ItemArmorMod implements IGasMask {

	@SideOnly(Side.CLIENT)
	private ModelM65 modelM65;
	
	private ResourceLocation tex = new ResourceLocation("hbm:textures/armor/ModelM65.png");
	private ResourceLocation tex_mono = new ResourceLocation("hbm:textures/armor/ModelM65Mono.png");
	
	public ItemModGasmask(String s) {
		super(ArmorModHandler.helmet_only, true, false, false, false, s);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn){
		if(this == ModItems.attachment_mask)
			list.add(TextFormatting.GREEN + "Gas protection");
		
		list.add("");
		super.addInformation(stack, worldIn, list, flagIn);

		ArmorUtil.addGasMaskTooltip(stack, worldIn, list, flagIn);

		List<HazardClass> haz = getBlacklist(stack);
		
		if(!haz.isEmpty()) {
			list.add("§c"+I18nUtil.resolveKey("hazard.neverProtects"));
			
			for(HazardClass clazz : haz) {
				list.add("§4 -" + I18nUtil.resolveKey(clazz.lang));
			}
		}
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addDesc(List<String> list, ItemStack stack, ItemStack armor){
		list.add("§a  " + stack.getDisplayName() + " (gas protection)");
		ArmorUtil.addGasMaskTooltip(stack, null, list, ITooltipFlag.TooltipFlags.NORMAL);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void modRender(Pre event, ItemStack armor){
		if(this.modelM65 == null) {
			this.modelM65 = new ModelM65();
		}
		RenderPlayer renderer = event.getRenderer();
		ModelBiped model = renderer.getMainModel();
		EntityPlayer player = event.getEntityPlayer();

		copyRot(modelM65, model);

		float interp = event.getPartialRenderTick();
		float yawWrapped = MathHelper.wrapDegrees(player.prevRotationYawHead + (player.rotationYawHead - player.prevRotationYawHead) * interp + 180);
		float pitch = player.rotationPitch;

		if(this == ModItems.attachment_mask)
			Minecraft.getMinecraft().renderEngine.bindTexture(tex);
		if(this == ModItems.attachment_mask_mono)
			Minecraft.getMinecraft().renderEngine.bindTexture(tex_mono);
		
		EntityPlayer me = MainRegistry.proxy.me();
		boolean isMe = player == me;
		if(!isMe){
			GlStateManager.pushMatrix();
			offset(player, me, interp);
		}
		if(model.isSneak) GlStateManager.translate(0, -0.1875F, 0);
		modelM65.render(player, 0F, 0F, 0, yawWrapped, pitch, 0.0625F);
		if(!isMe){
			GlStateManager.popMatrix();
		}
	}

	@Override
	public List<HazardClass> getBlacklist(ItemStack stack) {
		
		if(this == ModItems.attachment_mask_mono) {
			return Arrays.asList(HazardClass.GAS_LUNG, HazardClass.GAS_BLISTERING, HazardClass.BACTERIA,
                    HazardClass.BACTERIA);
		} else {
			return Arrays.asList(HazardClass.GAS_BLISTERING);
		}
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
		
		return super.onItemRightClick(world, player, hand);
	}
}
