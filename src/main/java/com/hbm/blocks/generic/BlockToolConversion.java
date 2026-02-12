package com.hbm.blocks.generic;

import com.hbm.api.block.IToolable;
import com.hbm.blocks.BlockBase;
import com.hbm.blocks.ICustomBlockItem;
import com.hbm.blocks.ILookOverlay;
import com.hbm.handler.NTMToolHandler;
import com.hbm.inventory.RecipesCommon;
import com.hbm.inventory.RecipesCommon.AStack;
import com.hbm.inventory.RecipesCommon.MetaBlock;
import com.hbm.items.IModelRegister;
import com.hbm.util.I18nUtil;
import com.hbm.util.InventoryUtil;
import com.hbm.util.Tuple.Pair;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.hbm.handler.NTMToolHandler.conversions;

public class BlockToolConversion extends BlockBase implements IToolable, ILookOverlay, ICustomBlockItem {

    public static final PropertyBool TOOLED = PropertyBool.create("tooled");
    public static HashMap<Object[], Object> bufferedRecipes = new HashMap<>();
    public static HashMap<Object[], Object> bufferedTools = new HashMap<>();
    public ItemBlock itemBlock;
    private boolean showMetaInCreative = true;

    public BlockToolConversion(Material mat) {
        super(mat);
        this.setDefaultState(this.blockState.getBaseState().withProperty(TOOLED, false));
    }


    public BlockToolConversion(Material mat, String s) {
        super(mat, s);
        this.setDefaultState(this.blockState.getBaseState().withProperty(TOOLED, false));
    }

    public static ToolType quickLookup(ItemStack stack) {
        return ToolType.getType(stack);
    }

    public static HashMap<Object[], Object> getRecipes(boolean recipes) {

        if (!bufferedRecipes.isEmpty()) return recipes ? bufferedRecipes : bufferedTools;

        for (Map.Entry<Pair<ToolType, MetaBlock>, Pair<AStack[], MetaBlock>> entry : conversions.entrySet()) {

            List<AStack> list = new ArrayList<>(Arrays.asList(entry.getValue().getKey()));
            list.add(new RecipesCommon.ComparableStack(entry.getKey().getValue().block, 1, entry.getKey().getValue().meta));

            Object[] inputInstance = list.toArray(new AStack[0]); // the instance has to match for the machine lookup to succeed
            bufferedRecipes.put(inputInstance, new ItemStack(entry.getValue().getValue().block, 1, entry.getValue().getValue().meta));
            bufferedTools.put(inputInstance, entry.getKey().getKey().stacksForDisplay.toArray(new ItemStack[0]));
        }

        return recipes ? bufferedRecipes : bufferedTools;
    }

    public void registerItem() {
        itemBlock = new BlockToolConversionItem(this);
        itemBlock.setRegistryName(this.getRegistryName());
        if(showMetaInCreative) itemBlock.setCreativeTab(this.getCreativeTab());
        ForgeRegistries.ITEMS.register(itemBlock);
    }

    @Override
    protected @NotNull BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, TOOLED);
    }

    @Override
    public @NotNull List<ItemStack> getDrops(@NotNull IBlockAccess world, @NotNull BlockPos pos, IBlockState state, int fortune) {
        int meta = state.getValue(TOOLED) ? 1 : 0;
        return Collections.singletonList(new ItemStack(Item.getItemFromBlock(this), 1, meta));
    }

    @Override
    public void onBlockPlacedBy(World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityLivingBase placer, ItemStack stack) {
        int meta = stack.getMetadata();
        world.setBlockState(pos, this.getStateFromMeta(meta), 3);
    }

    public String getTranslationKey(IBlockState state) {
        return super.getTranslationKey() + (state.getValue(TOOLED) ? "_tooled" : "");
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(TOOLED) ? 1 : 0;
    }

    @Override
    public @NotNull IBlockState getStateFromMeta(int meta) {
        return this.getDefaultState().withProperty(TOOLED, meta == 1);
    }

    @Override
    public boolean onScrew(World world, EntityPlayer player, int x, int y, int z,
                           EnumFacing side, float fX, float fY, float fZ, EnumHand hand, ToolType tool) {
        if (world.isRemote) return false;
        BlockPos pos = new BlockPos(x, y, z);

        IBlockState state = world.getBlockState(pos);
        int meta = getMetaFromState(state);

        Pair<AStack[], MetaBlock> result = NTMToolHandler.getConversions().get(new Pair<>(tool, new MetaBlock(this, meta)));

        if (result == null) return false;

        List<AStack> materials = new ArrayList<>(Arrays.asList(result.getKey()));

        if (materials.isEmpty() || InventoryUtil.doesPlayerHaveAStacks(player, materials, true)) {
            world.setBlockState(pos, getStateFromMeta(result.value.meta), 3);
            return true;
        }

        return false;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void printHook(Pre event, World world, BlockPos pos) {
        ItemStack held = Minecraft.getMinecraft().player.getHeldItemMainhand();
        ToolType tool = quickLookup(held);
        if (tool == null) return;

        IBlockState state = world.getBlockState(pos);
        int meta = getMetaFromState(state);

        Pair<AStack[], MetaBlock> result = NTMToolHandler.getConversions().get(new Pair<>(tool, new MetaBlock(this, meta)));

        if (result == null) return;

        List<String> text = new ArrayList<>();
        text.add(TextFormatting.GOLD + "Requires:");

        for (AStack stack : result.getKey()) {
            try {
                ItemStack display = stack.extractForCyclingDisplay(20);
                text.add("- " + display.getDisplayName() + " x" + display.getCount());
            } catch (Exception ex) {
                text.add(TextFormatting.RED + "- ERROR");
            }
        }

        ILookOverlay.printGeneric(event, I18nUtil.resolveKey(state.getBlock().getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
    }

    public static class BlockToolConversionItem extends ItemBlock implements IModelRegister {

        public BlockToolConversionItem(Block block) {
            super(block);
            this.setHasSubtypes(true);
            this.canRepair = false;
        }

        //TODO: may as well make it work with blockmeta, oppose to using custom block states
        @Override
        @SideOnly(Side.CLIENT)
        public void getSubItems(@NotNull CreativeTabs tab, @NotNull NonNullList<ItemStack> list){
            if (this.isInCreativeTab(tab)) {
                for (int i = 0; i <= 1; i++) {
                    list.add(new ItemStack(this, 1, i));
                }
            }
        }



        @Override
        public int getMetadata(int damage) {
            return damage;
        }

        @Override
        public @NotNull String getTranslationKey(ItemStack stack) {
            return super.getTranslationKey() + (stack.getMetadata() == 1 ? "_tooled" : "");
        }

        @Override
        public void registerModels() {
            ModelLoader.setCustomModelResourceLocation(this, 0, new ModelResourceLocation(this.getRegistryName(), "tooled=false"));
            ModelLoader.setCustomModelResourceLocation(this, 1, new ModelResourceLocation(this.getRegistryName() + "_tooled", "tooled=true"));
        }
    }
}


