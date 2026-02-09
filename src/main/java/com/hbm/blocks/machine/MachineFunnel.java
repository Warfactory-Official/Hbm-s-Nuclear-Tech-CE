package com.hbm.blocks.machine;

import com.hbm.Tags;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.ModBlocks;
import com.hbm.items.IDynamicModels;
import com.hbm.main.MainRegistry;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.render.model.BlockFunnelBakedModel;
import com.hbm.tileentity.machine.TileEntityMachineFunnel;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Random;

public class MachineFunnel extends BlockContainer implements ITooltipProvider, IDynamicModels {
  private final ResourceLocation objModelLocation =
      new ResourceLocation(Tags.MODID, "models/blocks/funnel.obj");

  @SideOnly(Side.CLIENT)
  public TextureAtlasSprite spriteTop;

  @SideOnly(Side.CLIENT)
  public TextureAtlasSprite spriteSide;

  @SideOnly(Side.CLIENT)
  public TextureAtlasSprite spriteBottom;

  public MachineFunnel(String regName) {
    super(Material.IRON);
    this.setRegistryName(regName);
    this.setTranslationKey(regName);

    ModBlocks.ALL_BLOCKS.add(this);
    IDynamicModels.INSTANCES.add(this);
  }

  @Override
  public boolean isOpaqueCube(IBlockState state) {
    return false;
  }

  @Override
  public boolean isFullCube(IBlockState state) {
    return false;
  }

  @Override
  public EnumBlockRenderType getRenderType(IBlockState state) {
    return EnumBlockRenderType.MODEL;
  }

  @Override
  public TileEntity createNewTileEntity(World world, int meta) {
    return new TileEntityMachineFunnel();
  }

  @Override
  public boolean onBlockActivated(
      World world,
      BlockPos pos,
      IBlockState state,
      EntityPlayer player,
      EnumHand hand,
      EnumFacing facing,
      float hitX,
      float hitY,
      float hitZ) {
    {
      if (world.isRemote) {
        return true;
      } else if (!player.isSneaking()) {
        TileEntity entity = world.getTileEntity(pos);
        if (entity instanceof TileEntityMachineFunnel) {
          FMLNetworkHandler.openGui(
              player, MainRegistry.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
        }
        return true;
      } else {
        return false;
      }
    }
  }

  private final Random rand = new Random();

  @Override
  public void breakBlock(@NotNull World world, @NotNull BlockPos pos, IBlockState state) {
    ISidedInventory tile = (ISidedInventory) world.getTileEntity(pos);
    if (tile != null) {
      for (int i = 0; i < tile.getSizeInventory(); ++i) {
        ItemStack stack = tile.getStackInSlot(i);
        if (stack != ItemStack.EMPTY) {
          float f = this.rand.nextFloat() * 0.8F + 0.1F;
          float f1 = this.rand.nextFloat() * 0.8F + 0.1F;
          float f2 = this.rand.nextFloat() * 0.8F + 0.1F;
          while (stack.getCount() > 0) {
            int j1 = this.rand.nextInt(21) + 10;
            if (j1 > stack.getCount()) j1 = stack.getCount();
            stack.shrink(j1);
            EntityItem entityitem =
                new EntityItem(
                    world,
                    pos.getX() + f,
                    pos.getY() + f1,
                    pos.getZ() + f2,
                    new ItemStack(stack.getItem(), j1, stack.getItemDamage()));
            if (stack.hasTagCompound())
              entityitem.getItem().setTagCompound(stack.getTagCompound().copy());
            float f3 = 0.05F;
            entityitem.motionX = (float) this.rand.nextGaussian() * f3;
            entityitem.motionY = (float) this.rand.nextGaussian() * f3 + 0.2F;
            entityitem.motionZ = (float) this.rand.nextGaussian() * f3;
            world.spawnEntity(entityitem);
          }
        }
      }

      world.notifyNeighborsOfStateChange(pos, state.getBlock(), true);
    }

    super.breakBlock(world, pos, state);
  }

  @Override
  public void addInformation(
      ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
    this.addStandardInfo(tooltip);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void registerModel() {
    Item item = Item.getItemFromBlock(this);
    ModelResourceLocation inv = new ModelResourceLocation(getRegistryName(), "inventory");
    ModelLoader.setCustomModelResourceLocation(item, 0, inv);
  }

  @Override
  @SideOnly(Side.CLIENT)
  public StateMapperBase getStateMapper(ResourceLocation loc) {
    return new StateMapperBase() {
      @Override
      protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
        return new ModelResourceLocation(loc, "normal");
      }
    };
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void registerSprite(TextureMap map) {
    ResourceLocation rl = getRegistryName();
    if (rl != null) {
      this.spriteTop =
          map.registerSprite(new ResourceLocation(rl.getNamespace(), "blocks/machine_funnel_top"));
      this.spriteSide =
          map.registerSprite(new ResourceLocation(rl.getNamespace(), "blocks/machine_funnel_side"));
      this.spriteBottom =
          map.registerSprite(
              new ResourceLocation(rl.getNamespace(), "blocks/machine_funnel_bottom"));
    }
  }

  @Override
  @SideOnly(Side.CLIENT)
  public void bakeModel(ModelBakeEvent event) {
    HFRWavefrontObject wavefront = new HFRWavefrontObject(objModelLocation);
    TextureAtlasSprite[] sprites = new TextureAtlasSprite[] {spriteTop, spriteSide, spriteBottom};

    IBakedModel blockModel = BlockFunnelBakedModel.forBlock(wavefront, sprites);
    IBakedModel itemModel = BlockFunnelBakedModel.forItem(wavefront, sprites);

    ModelResourceLocation mrlBlock = new ModelResourceLocation(getRegistryName(), "normal");
    ModelResourceLocation mrlItem = new ModelResourceLocation(getRegistryName(), "inventory");

    event.getModelRegistry().putObject(mrlBlock, blockModel);
    event.getModelRegistry().putObject(mrlItem, itemModel);
  }
}
