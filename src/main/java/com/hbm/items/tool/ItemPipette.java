package com.hbm.items.tool;

import com.google.common.collect.ImmutableMap;
import com.hbm.Tags;
import com.hbm.api.fluidmk2.IFillableItem;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ItemBakedBase;
import com.hbm.items.ModItems;
import com.hbm.util.I18nUtil;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelBakery;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.hbm.items.ItemEnumMulti.ROOT_PATH;

public class ItemPipette extends ItemBakedBase implements IFillableItem {

    private static final String EMPTY_OVERLAY_PATH = "pipette_empty";
    private final String baseTexturePath;

    public ItemPipette(String name) {
        super(name, name);
        this.baseTexturePath = name;
        this.setNoRepair();
        this.setMaxDamage(1);
    }

    public short getMaxFill() {
        return this == ModItems.pipette_laboratory ? (short) 50 : (short) 1000;
    }

    public void initNBT(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        NBTTagCompound tag = new NBTTagCompound();
        tag.setShort("type", (short) Fluids.NONE.getID());
        tag.setShort("fill", (short) 0);
        tag.setShort("capacity", getMaxFill());
        stack.setTagCompound(tag);
    }

    public FluidType getType(ItemStack stack) {
        if (stack.isEmpty()) {
            return Fluids.NONE;
        }
        NBTTagCompound tag = getOrCreateTag(stack);
        return Fluids.fromID(tag.getShort("type"));
    }

    public short getCapacity(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        NBTTagCompound tag = getOrCreateTag(stack);
        return tag.getShort("capacity");
    }

    public void setFill(ItemStack stack, FluidType type, short fill) {
        if (stack.isEmpty()) {
            return;
        }
        NBTTagCompound tag = getOrCreateTag(stack);
        tag.setShort("type", (short) type.getID());
        tag.setShort("fill", fill);
    }

    @Override
    public int getFill(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        NBTTagCompound tag = getOrCreateTag(stack);
        return tag.getShort("fill");
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);

        if (stack.isEmpty()) {
            return new ActionResult<>(EnumActionResult.PASS, stack);
        }

        getOrCreateTag(stack);

        if (!world.isRemote) {
            if (getFill(stack) == 0) {
                int capacity = getCapacity(stack);
                int max = getMaxFill();
                int updated;

                if (this == ModItems.pipette_laboratory) {
                    updated = player.isSneaking() ? Math.max(capacity - 1, 1) : Math.min(capacity + 1, 50);
                } else {
                    updated = player.isSneaking() ? Math.max(capacity - 50, 50) : Math.min(capacity + 50, 1000);
                }

                stack.getTagCompound().setShort("capacity", (short) updated);
                player.sendMessage(new TextComponentString(updated + "/" + max + "mB"));
            } else {
                player.sendMessage(new TextComponentTranslation("desc.item.pipette.noEmpty"));
            }
        }

        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        if (this == ModItems.pipette_laboratory) {
            tooltip.add(I18nUtil.resolveKey("desc.item.pipette.corrosive"));
            tooltip.add(I18nUtil.resolveKey("desc.item.pipette.laboratory"));
        }
        if (this == ModItems.pipette_boron) {
            tooltip.add(I18nUtil.resolveKey("desc.item.pipette.corrosive"));
        }
        if (this == ModItems.pipette) {
            tooltip.add(I18nUtil.resolveKey("desc.item.pipette.noCorrosive"));
        }

        FluidType type = getType(stack);
        tooltip.add("Fluid: " + type.getLocalizedName());
        tooltip.add("Amount: " + getFill(stack) + "/" + getCapacity(stack) + "mB (" + getMaxFill() + "mB)");
    }

    @Override
    public boolean acceptsFluid(FluidType type, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return (type == getType(stack) || getFill(stack) == 0) && !type.isAntimatter();
    }

    @Override
    public int tryFill(FluidType type, int amount, ItemStack stack) {
        if (stack.isEmpty() || amount <= 0) {
            return amount;
        }

        if (!acceptsFluid(type, stack)) {
            return amount;
        }

        if (getFill(stack) == 0) {
            setFill(stack, type, (short) 0);
        }

        int capacity = getCapacity(stack);
        int fill = getFill(stack);
        int required = Math.max(0, capacity - fill);
        int toFill = Math.min(required, amount);

        if (toFill > 0) {
            setFill(stack, type, (short) (fill + toFill));
        }

        if (getFill(stack) > 0 && willFizzle(type)) {
            stack.shrink(stack.getCount());
        }

        return amount - toFill;
    }

    public boolean willFizzle(FluidType type) {
        if (this != ModItems.pipette) {
            return false;
        }
        return type.isCorrosive() && type != Fluids.PEROXIDE;
    }

    @Override
    public boolean providesFluid(FluidType type, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return getType(stack) == type;
    }

    @Override
    public int tryEmpty(FluidType type, int amount, ItemStack stack) {
        if (stack.isEmpty() || amount <= 0) {
            return amount;
        }

        if (providesFluid(type, stack)) {
            int stored = getFill(stack);
            int toDrain = Math.min(amount, stored);
            setFill(stack, type, (short) (stored - toDrain));

            if (getFill(stack) == 0) {
                setFill(stack, Fluids.NONE, (short) 0);
            }

            return toDrain;
        }

        return amount;
    }

    @Override
    public FluidType getFirstFluidType(ItemStack stack) {
        return getType(stack);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void bakeModel(ModelBakeEvent event) {
        ResourceLocation baseTexture = new ResourceLocation(Tags.MODID, ROOT_PATH + baseTexturePath);
        ResourceLocation overlayTexture = new ResourceLocation(Tags.MODID, ROOT_PATH + getOverlayTexturePath());
        ResourceLocation emptyTexture = new ResourceLocation(Tags.MODID, ROOT_PATH + EMPTY_OVERLAY_PATH);

        try {
            IModel parent = ModelLoaderRegistry.getModel(new ResourceLocation("minecraft", "item/generated"));

            ImmutableMap<String, String> filledTextures = ImmutableMap.of(
                    "layer0", baseTexture.toString(),
                    "layer1", overlayTexture.toString()
            );

            ImmutableMap<String, String> emptyTextures = ImmutableMap.of(
                    "layer0", baseTexture.toString(),
                    "layer1", emptyTexture.toString()
            );

            IBakedModel filledModel = parent.retexture(filledTextures)
                    .bake(ModelRotation.X0_Y0, DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());
            IBakedModel emptyModel = parent.retexture(emptyTextures)
                    .bake(ModelRotation.X0_Y0, DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter());

            event.getModelRegistry().putObject(getFilledModelLocation(), filledModel);
            event.getModelRegistry().putObject(getEmptyModelLocation(), emptyModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerModel() {
        ModelResourceLocation filled = getFilledModelLocation();
        ModelResourceLocation empty = getEmptyModelLocation();

        ModelBakery.registerItemVariants(this, filled, empty);
        ModelLoader.setCustomMeshDefinition(this, stack -> getFill(stack) > 0 ? filled : empty);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerSprite(TextureMap map) {
        super.registerSprite(map);
        map.registerSprite(new ResourceLocation(Tags.MODID, ROOT_PATH + getOverlayTexturePath()));
        map.registerSprite(new ResourceLocation(Tags.MODID, ROOT_PATH + EMPTY_OVERLAY_PATH));
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IItemColor getItemColorHandler() {
        return (stack, tintIndex) -> {
            if (tintIndex == 1 && !stack.isEmpty() && getFill(stack) > 0) {
                FluidType type = getType(stack);
                int color = type.getColor();
                if (color < 0) {
                    color = 0xFFFFFF;
                }
                return color;
            }
            return 0xFFFFFF;
        };
    }

    private NBTTagCompound getOrCreateTag(ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null) {
            initNBT(stack);
            tag = stack.getTagCompound();
        }
        return tag == null ? new NBTTagCompound() : tag;
    }

    private String getOverlayTexturePath() {
        return this == ModItems.pipette_laboratory ? "pipette_laboratory_overlay" : "pipette_overlay";
    }

    @SideOnly(Side.CLIENT)
    private ModelResourceLocation getFilledModelLocation() {
        return new ModelResourceLocation(new ResourceLocation(Tags.MODID, ROOT_PATH + baseTexturePath + "_filled"), "inventory");
    }

    @SideOnly(Side.CLIENT)
    private ModelResourceLocation getEmptyModelLocation() {
        return new ModelResourceLocation(new ResourceLocation(Tags.MODID, ROOT_PATH + baseTexturePath + "_empty"), "inventory");
    }
}
