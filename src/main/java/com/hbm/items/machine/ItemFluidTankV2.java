package com.hbm.items.machine;

import com.google.common.collect.ImmutableMap;
import com.hbm.Tags;
import com.hbm.config.GeneralConfig;
import com.hbm.handler.FluidTankV2Handler;
import com.hbm.inventory.FluidContainerRegistry;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ItemBakedBase;
import com.hbm.items.ModItems;
import com.hbm.util.I18nUtil;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.hbm.items.ItemEnumMulti.ROOT_PATH;

public class ItemFluidTankV2 extends ItemBakedBase {

	private final ResourceLocation baseTextureLocation;
	protected ResourceLocation overlayTextureLocation;
	private final ModelResourceLocation modelLocation;

	private final int cap;

	public ItemFluidTankV2(String name, int cap) {
		super(name);
		this.setHasSubtypes(true);
		this.setMaxDamage(0);
		name = name.replace("_v2", "");

		this.baseTextureLocation = new ResourceLocation(Tags.MODID, ROOT_PATH + name);
		this.overlayTextureLocation = new ResourceLocation(Tags.MODID, ROOT_PATH + name + "_overlay");
		this.modelLocation = new ModelResourceLocation(this.baseTextureLocation, "inventory");
		this.cap = cap;
	}

	@Override
	public int getMetadata(int damage) {
		return damage;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void bakeModel(ModelBakeEvent event) {
		try {
			IModel baseModel = ModelLoaderRegistry.getModel(new ResourceLocation("minecraft", "item/generated"));

			ImmutableMap.Builder<String, String> textures = ImmutableMap.builder();
			textures.put("layer0", this.baseTextureLocation.toString());

			if (this.overlayTextureLocation != null) {
				textures.put("layer1", this.overlayTextureLocation.toString());
			}

			IModel retexturedModel = baseModel.retexture(textures.build());
			IBakedModel bakedModel = retexturedModel.bake(ModelRotation.X0_Y0, DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
			event.getModelRegistry().putObject(this.modelLocation, bakedModel);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerModel() {
		FluidType[] order = Fluids.getInNiceOrder();
		for (FluidType fluidType : order) {
			ModelLoader.setCustomModelResourceLocation(this, fluidType.getID(), this.modelLocation);
		}
	}

	@Override
	public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
		return new FluidTankV2Handler(stack, this.cap);
	}

	@Override
	public boolean showDurabilityBar(ItemStack stack) {
		FluidType type = Fluids.fromID(stack.getMetadata());
		int amount = FluidContainerRegistry.getFluidContent(stack, type);
        return amount != cap && amount != 0;
    }

	@Override
	public double getDurabilityForDisplay(ItemStack stack) {
		FluidType type = Fluids.fromID(stack.getMetadata());
		int amount = FluidContainerRegistry.getFluidContent(stack, type);

		double fillRatio = (double) amount / (double) cap;
		return Math.min(Math.max(1.0 - fillRatio, 0), 1.0);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerSprite(TextureMap map) {
		map.registerSprite(this.baseTextureLocation);
		if (this.overlayTextureLocation != null) {
			map.registerSprite(this.overlayTextureLocation);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IItemColor getItemColorHandler() {
		return (stack, tintIndex) -> {
			if (tintIndex == 0) {
				return 0xFFFFFF;
			}

			int color = Fluids.fromID(stack.getMetadata()).getColor();
			return color >= 0 ? color : 0xFFFFFF;
		};
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		if(!GeneralConfig.registerTanks) return;
		if (!GeneralConfig.enableFluidContainersV2) return;
		if (tab != this.getCreativeTab() && tab != CreativeTabs.SEARCH) return;

		FluidType[] order = Fluids.getInNiceOrder();
		for(int i = 1; i < order.length; ++i) {
			FluidType type = order[i];

			if(type.hasNoContainer() || type.getFF() == null) continue;

			int id = type.getID();

			ItemStack stack = new ItemStack(this, 1, id);
			IFluidHandlerItem handler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
			if (handler != null) {
				FluidStack fluidStack = new FluidStack(type.getFF(), cap);
				handler.fill(fluidStack, true);
				stack.setTagCompound(handler.getContainer().getTagCompound());
			}
			items.add(stack);
		}
		items.add(new ItemStack(this, 1, 0));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getItemStackDisplayName(ItemStack stack) {
		FluidType type = Fluids.fromID(stack.getMetadata());
		int amount = FluidContainerRegistry.getFluidContent(stack, type);
		if (amount == 0) return I18nUtil.resolveKey(getTranslationKey() + ".empty");
		else return I18nUtil.resolveKey(getTranslationKey() + ".not_empty", type.getLocalizedName());
	}

	@Override
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		if(stack.getItem() == ModItems.fluid_pack_full) tooltip.add(stack.getCount() + "x " + "32000 mB");
		else {
			FluidType f = Fluids.fromID(stack.getMetadata());
			int fill = FluidContainerRegistry.getFluidContent(stack, f);
			String s = (f == null ? "0" : fill) + "/" + cap + " mB";
			if (stack.getCount() > 1)
				s = stack.getCount() + "x " + s;
			tooltip.add(s);
		}
		Fluids.fromID(stack.getMetadata()).addInfoItemTanks(tooltip);
	}

    @Override
    public boolean hasContainerItem(@NotNull ItemStack item) {
        return true;
    }

    @Override
    public @NotNull ItemStack getContainerItem(@NotNull ItemStack item) {
        return FluidContainerRegistry.getEmptyContainer(item);
    }
}
