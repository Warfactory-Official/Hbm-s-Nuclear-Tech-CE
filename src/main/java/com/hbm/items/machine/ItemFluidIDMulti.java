package com.hbm.items.machine;

import com.google.common.collect.ImmutableMap;
import com.hbm.Tags;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.gui.GUIScreenFluid;
import com.hbm.items.IItemControlReceiver;
import com.hbm.items.ItemBakedBase;
import com.hbm.main.MainRegistry;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.PlayerInformPacketLegacy;
import com.hbm.tileentity.IGUIProvider;
import com.hbm.tileentity.network.TileEntityPipeBaseNT;
import com.hbm.util.I18nUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.hbm.items.ItemEnumMulti.ROOT_PATH;

public class ItemFluidIDMulti extends ItemBakedBase implements IItemFluidIdentifier, IItemControlReceiver, IGUIProvider {
    private final ResourceLocation baseTextureLocation;
    private final ResourceLocation overlayTextureLocation;
    private final ModelResourceLocation modelLocation;
    public ItemFluidIDMulti(String s) {
        super(s);
        this.baseTextureLocation = new ResourceLocation(Tags.MODID, ROOT_PATH + s);
        String overlayName = s.substring(0, s.length() - 6) + "_overlay";
        this.overlayTextureLocation = new ResourceLocation(Tags.MODID, ROOT_PATH + overlayName);
        this.modelLocation = new ModelResourceLocation(this.baseTextureLocation, "inventory");
    }
    // this is only for display purposes, it doesn't need to have metadata at all
    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> list) {
        if (this.isInCreativeTab(tab)) {
            FluidType[] order = Fluids.getInNiceOrder();
            for (int i = 1; i < order.length; ++i) {
                FluidType type = order[i];
                if (!type.hasNoID()) {
                    ItemStack id = new ItemStack(this, 1, type.getID());
                    setType(id, type, true);
                    list.add(id);
                }
            }
        }
    }

    @Override
    public @NotNull ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, @NotNull EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);

        if (!world.isRemote && !player.isSneaking()) {
            FluidType primary = getType(stack, true);
            FluidType secondary = getType(stack, false);
            setType(stack, secondary, true);
            setType(stack, primary, false);
            world.playSound(null, player.posX, player.posY, player.posZ, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 0.25F, 1.25F);
            if (player instanceof EntityPlayerMP) {
                PacketDispatcher.wrapper.sendTo(new PlayerInformPacketLegacy(new TextComponentTranslation(secondary.getConditionalName()), 7, 3000), (EntityPlayerMP) player);
            }
        }

        if (world.isRemote && player.isSneaking()) {
            BlockPos pos = player.getPosition();
            FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
        }

        return super.onItemRightClick(world, player, hand);
    }

    @Override
    public void receiveControl(ItemStack stack, NBTTagCompound data) {
        if(data.hasKey("primary")) {
            setType(stack, Fluids.fromID(data.getInteger("primary")), true);
        }
        if(data.hasKey("secondary")) {
            setType(stack, Fluids.fromID(data.getInteger("secondary")), false);
        }
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, World worldIn, List<String> tooltip, @NotNull ITooltipFlag flagIn) {
        tooltip.add(I18nUtil.resolveKey(getTranslationKey() + ".info"));
        tooltip.add("   " + getType(stack, true).getLocalizedName());
        tooltip.add(I18nUtil.resolveKey(getTranslationKey() + ".info2"));
        tooltip.add("   " + getType(stack, false).getLocalizedName());
    }

    @Override
    public @NotNull ItemStack getContainerItem(ItemStack stack) {
        return stack.copy();
    }

    @Override
    public boolean hasContainerItem() {
        return true;
    }

    @Override
    public FluidType getType(World world, int x, int y, int z, ItemStack stack) {
        return getType(stack, true);
    }

    @Override
    public boolean doesSneakBypassUse(@NotNull ItemStack stack, net.minecraft.world.@NotNull IBlockAccess world, @NotNull BlockPos pos, @NotNull EntityPlayer player) {
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void bakeModel(ModelBakeEvent event) {
        try {
            IModel baseModel = ModelLoaderRegistry.getModel(new ResourceLocation("minecraft", "item/generated"));

            IModel retexturedModel = baseModel.retexture(
                    ImmutableMap.of(
                            "layer0", this.baseTextureLocation.toString(),
                            "layer1", this.overlayTextureLocation.toString()
                    )
            );

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
    @SideOnly(Side.CLIENT)
    public void registerSprite(TextureMap map) {
        map.registerSprite(this.baseTextureLocation);
        map.registerSprite(this.overlayTextureLocation);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IItemColor getItemColorHandler() {
        return (stack, tintIndex) -> {
            if (tintIndex == 0) {
                return 0xFFFFFF;
            }

            int color = getType(stack, true).getColor();
            return color >= 0 ? color : 0xFFFFFF;
        };
    }

    public static void setType(ItemStack stack, FluidType type, boolean primary) {
        if(!stack.hasTagCompound())
            stack.setTagCompound(new NBTTagCompound());

        int id = type.getID();
        stack.getTagCompound().setInteger("fluid" + (primary ? 1 : 2), id);
        if (primary) stack.setItemDamage(id);
    }

    public static FluidType getType(ItemStack stack, boolean primary) {
        if(!stack.hasTagCompound())
            return Fluids.NONE;

        int type = stack.getTagCompound().getInteger("fluid" + (primary ? 1 : 2));
        return Fluids.fromID(type);
    }

    @Override
    public @NotNull EnumActionResult onItemUse(@NotNull EntityPlayer player, World worldIn, @NotNull BlockPos pos, @NotNull EnumHand hand, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {
        TileEntity te = worldIn.getTileEntity(pos);
        if(te instanceof TileEntityPipeBaseNT duct){
            FluidType handType = getType(worldIn, pos.getX(), pos.getY(), pos.getZ(), player.getHeldItem(hand));
            if(handType != duct.getType()){
                if (player.isSneaking())
                    spreadType(worldIn, pos, handType, duct.getType(), 256);
                else duct.setType(handType);
            }
            return EnumActionResult.SUCCESS;
        }
        return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
    }

    public static void spreadType(World worldIn, BlockPos pos, FluidType hand, FluidType pipe, int x){
        if(x > 0){
            TileEntity te = worldIn.getTileEntity(pos);
            if(te instanceof TileEntityPipeBaseNT duct){
                if(duct.getType() == pipe){
                    duct.setType(hand);
                    duct.markDirty();
                    spreadType(worldIn, pos.add(1, 0, 0), hand, pipe, x-1);
                    spreadType(worldIn, pos.add(0, 1, 0), hand, pipe, x-1);
                    spreadType(worldIn, pos.add(0, 0, 1), hand, pipe, x-1);
                    spreadType(worldIn, pos.add(-1, 0, 0), hand, pipe, x-1);
                    spreadType(worldIn, pos.add(0, -1, 0), hand, pipe, x-1);
                    spreadType(worldIn, pos.add(0, 0, -1), hand, pipe, x-1);
                }
            }
        }
    }

    @Override
    public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return null;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public GuiScreen provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
        return new GUIScreenFluid(player);
    }
}
