package com.hbm.items.armor;

import com.hbm.handler.ArmorModHandler;
import com.hbm.main.MainRegistry;
import com.hbm.render.model.ModelBackTesla;
import com.hbm.tileentity.machine.TileEntityTesla;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderPlayerEvent.Pre;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11; import net.minecraft.client.renderer.GlStateManager;

import java.util.ArrayList;
import java.util.List;

public class ItemModTesla extends ItemArmorMod {

	private ModelBackTesla modelTesla;
	public List<double[]> targets = new ArrayList<>();
	
	public ItemModTesla(String s) {
		super(ArmorModHandler.plate_only, false, true, false, false, s);
	}
    
	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> list, ITooltipFlag flagIn){
		list.add(TextFormatting.YELLOW + "Zaps nearby entities (requires full electric set)");
		list.add("");
		super.addInformation(stack, worldIn, list, flagIn);
	}

	@SideOnly(Side.CLIENT)
	public void addDesc(List<String> list, ItemStack stack, ItemStack armor) {
		list.add(TextFormatting.YELLOW + stack.getDisplayName() + " (zaps nearby entities)");
	}
	
	@Override
	public void modUpdate(EntityLivingBase entity, ItemStack armor) {
		
		if(!entity.world.isRemote && entity instanceof EntityPlayer && armor.getItem() instanceof ArmorFSBPowered && ArmorFSBPowered.hasFSBArmor((EntityPlayer)entity)) {
			targets = TileEntityTesla.zap(entity.world, entity.posX, entity.posY + 1.25, entity.posZ, 5, entity);
			
			if(targets != null && !targets.isEmpty() && entity.getRNG().nextInt(5) == 0) {
				armor.damageItem(1, entity);
			}
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
		float yaw = player.prevRenderYawOffset + (player.renderYawOffset - player.prevRenderYawOffset) * interp;
		float pitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * interp;

		EntityPlayer me = MainRegistry.proxy.me();
		boolean isMe = player == me;

		GlStateManager.pushMatrix();

		if(!isMe) {
			offset(player, me, interp);
		}

		GlStateManager.rotate(yaw + 180.0F, 0.0F, 1.0F, 0.0F);

		modelTesla.render(player, 0.0F, 0.0F, 0.0F, 0.0F, pitch, 0.0625F);

		GlStateManager.popMatrix();
	}



}