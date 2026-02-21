package com.hbm.blocks.network;

import com.hbm.Tags;
import com.hbm.api.conveyor.IConveyorBelt;
import com.hbm.api.conveyor.IConveyorItem;
import com.hbm.api.conveyor.IConveyorPackage;
import com.hbm.api.conveyor.IEnterableBlock;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.ModBlocks;
import com.hbm.entity.item.EntityMovingItem;
import com.hbm.interfaces.AutoRegister;
import com.hbm.inventory.recipes.CrystallizerRecipes;
import com.hbm.items.IDynamicModels;
import com.hbm.main.ResourceManager;
import com.hbm.render.loader.HFRWavefrontObject;
import com.hbm.render.model.CranePartitionerBakedModel;
import com.hbm.tileentity.TileEntityMachineBase;
import com.hbm.util.InventoryUtil;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class CranePartitioner extends BlockContainer implements IConveyorBelt, IEnterableBlock, ITooltipProvider, IDynamicModels {

    public static final PropertyDirection FACING = PropertyDirection.create("facing", EnumFacing.Plane.HORIZONTAL);
    private static final AxisAlignedBB AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.75D, 1.0D);

    private static final ResourceLocation TEX_SIDE = new ResourceLocation(Tags.MODID, "blocks/crane_partitioner_side");
    private static final ResourceLocation TEX_TOP = new ResourceLocation(Tags.MODID, "blocks/crane_top");
    private static final ResourceLocation TEX_BACK = new ResourceLocation(Tags.MODID, "blocks/crane_partitioner_back");
    private static final ResourceLocation TEX_BELT = new ResourceLocation(Tags.MODID, "blocks/crane_splitter_belt");
    private static final ResourceLocation TEX_INNER = new ResourceLocation(Tags.MODID, "blocks/crane_splitter_inner");
    private static final ResourceLocation TEX_INNER_SIDE = new ResourceLocation(Tags.MODID, "blocks/crane_splitter_inner_side");

    private final Random dropRandom = new Random();

    @SideOnly(Side.CLIENT) private ModelResourceLocation blockModelLocation;
    @SideOnly(Side.CLIENT) private ModelResourceLocation itemModelLocation;

    public CranePartitioner(String s) {
        super(Material.IRON);
        this.setTranslationKey(s);
        this.setRegistryName(s);
        setHardness(3.0F);
        setResistance(6.0F);
        setDefaultState(blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
        ModBlocks.ALL_BLOCKS.add(this);
        IDynamicModels.INSTANCES.add(this);
    }

    @Override
    public TileEntity createNewTileEntity(@NotNull World worldIn, int meta) {
        return new TileEntityCranePartitioner();
    }

    @Override
    public @NotNull AxisAlignedBB getBoundingBox(@NotNull IBlockState state, @NotNull IBlockAccess source, @NotNull BlockPos pos) {
        return AABB;
    }

    @Override
    public boolean isOpaqueCube(@NotNull IBlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(@NotNull IBlockState state) {
        return false;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public @NotNull BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.CUTOUT_MIPPED;
    }

    @Override
    public @NotNull EnumBlockRenderType getRenderType(@NotNull IBlockState state) {
        return EnumBlockRenderType.MODEL;
    }

    @Override
    public void onBlockPlacedBy(World worldIn, @NotNull BlockPos pos, IBlockState state, EntityLivingBase placer, @NotNull ItemStack stack) {
        EnumFacing facing = placer.getHorizontalFacing().getOpposite();
        worldIn.setBlockState(pos, state.withProperty(FACING, facing), 2);
    }

    @Override
    public @NotNull IBlockState getStateForPlacement(@NotNull World worldIn, @NotNull BlockPos pos, @NotNull EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, @NotNull EnumHand hand) {
        return getDefaultState().withProperty(FACING, placer.getHorizontalFacing().getOpposite());
    }

    @Override
    public @NotNull IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(FACING, EnumFacing.byHorizontalIndex(meta & 3));
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(FACING).getHorizontalIndex();
    }

    @Override
    protected @NotNull BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, FACING);
    }

    @Override
    public boolean canItemEnter(World world, int x, int y, int z, EnumFacing dir, IConveyorItem entity) {
        return getTravelDirection(world, new BlockPos(x, y, z), null) == dir;
    }

    @Override
    public boolean canPackageEnter(World world, int x, int y, int z, EnumFacing dir, IConveyorPackage entity) {
        return false;
    }

    @Override
    public void onPackageEnter(World world, int x, int y, int z, EnumFacing dir, IConveyorPackage entity) { }

    @Override
    public boolean canItemStay(World world, int x, int y, int z, Vec3d itemPos) {
        return true;
    }

    @Override
    public Vec3d getTravelLocation(World world, int x, int y, int z, @Nullable Vec3d itemPos, double speed) {
        EnumFacing dir = getTravelDirection(world, new BlockPos(x, y, z), itemPos);
        Vec3d currentPos = itemPos != null ? itemPos : new Vec3d(x, y, z).add(0.5D, 0.25D, 0.5D);
        Vec3d snap = getClosestSnappingPosition(world, new BlockPos(x, y, z), currentPos);
        Vec3i dirVec = dir.getDirectionVec();
        Vec3d dest = snap.subtract(dirVec.getX() * speed, dirVec.getY() * speed, dirVec.getZ() * speed);
        Vec3d motion = dest.subtract(currentPos);
        double len = motion.length();
        if (len < 1.0E-4D) {
            return snap;
        }
        Vec3d step = motion.scale(speed / len);
        return new Vec3d(currentPos.x + step.x, currentPos.y + step.y, currentPos.z + step.z);
    }

    @Override
    public Vec3d getClosestSnappingPosition(World world, BlockPos pos, @Nullable Vec3d itemPos) {
        EnumFacing dir = getTravelDirection(world, pos, itemPos);
        Vec3d currentPos = itemPos != null ? itemPos : new Vec3d(pos).add(0.5D, 0.25D, 0.5D);
        double minX = pos.getX();
        double maxX = minX + 1.0D;
        double minZ = pos.getZ();
        double maxZ = minZ + 1.0D;
        double clampedX = MathHelper.clamp(currentPos.x, minX, maxX);
        double clampedZ = MathHelper.clamp(currentPos.z, minZ, maxZ);
        double snapX = pos.getX() + 0.5D;
        double snapZ = pos.getZ() + 0.5D;
        if (dir.getXOffset() != 0) snapX = clampedX;
        if (dir.getZOffset() != 0) snapZ = clampedZ;
        return new Vec3d(snapX, pos.getY() + 0.25D, snapZ);
    }

    public EnumFacing getTravelDirection(World world, BlockPos pos, @Nullable Vec3d itemPos) {
        IBlockState state = world.getBlockState(pos);
        if (state.getBlock() != this) {
            return EnumFacing.NORTH;
        }
        return state.getValue(FACING);
    }

    @Override
    public void onItemEnter(World world, int x, int y, int z, EnumFacing dir, IConveyorItem entity) {
        if (world.isRemote) return;
        TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
        if (!(tile instanceof TileEntityCranePartitioner partitioner)) return;
        ItemStack stack = entity.getItemStack();
        if (stack.isEmpty()) return;

        ItemStack remainder;
        if (CrystallizerRecipes.getAmount(stack) > 0) {
            remainder = InventoryUtil.tryAddItemToInventory(partitioner.inventory, 0, TileEntityCranePartitioner.SLOT_COUNT - 1, stack);
        } else {
            remainder = InventoryUtil.tryAddItemToInventory(partitioner.inventory, TileEntityCranePartitioner.SLOT_COUNT, TileEntityCranePartitioner.SLOT_COUNT * 2 - 1, stack);
        }

        if (remainder != null && !remainder.isEmpty()) {
            EntityItem item = new EntityItem(world, x + 0.5D, y + 0.5D, z + 0.5D, remainder.copy());
            world.spawnEntity(item);
        }
    }

    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World worldIn, @NotNull List<String> tooltip, @NotNull ITooltipFlag flagIn) {
        addStandardInfo(tooltip);
    }

    @Override
    public void breakBlock(World worldIn, @NotNull BlockPos pos, @NotNull IBlockState state) {
        TileEntity tile = worldIn.getTileEntity(pos);
        if (tile instanceof TileEntityCranePartitioner inv) {
            ItemStackHandler inventory = inv.inventory;
            for (int i = 0; i < inventory.getSlots(); ++i) {
                ItemStack stack = inventory.getStackInSlot(i);
                if (stack.isEmpty()) continue;
                inventory.setStackInSlot(i, ItemStack.EMPTY);

                float fx = dropRandom.nextFloat() * 0.8F + 0.1F;
                float fy = dropRandom.nextFloat() * 0.8F + 0.1F;
                float fz = dropRandom.nextFloat() * 0.8F + 0.1F;

                while (!stack.isEmpty()) {
                    int count = dropRandom.nextInt(21) + 10;
                    int dropCount = Math.min(count, stack.getCount());
                    ItemStack dropStack = stack.splitStack(dropCount);
                    NBTTagCompound tag = stack.getTagCompound();
                    if (tag != null) {
                        dropStack.setTagCompound(tag.copy());
                    }
                    EntityItem entityitem = new EntityItem(worldIn, pos.getX() + fx, pos.getY() + fy, pos.getZ() + fz, dropStack);
                    float motion = 0.05F;
                    entityitem.motionX = dropRandom.nextGaussian() * motion;
                    entityitem.motionY = dropRandom.nextGaussian() * motion + 0.2F;
                    entityitem.motionZ = dropRandom.nextGaussian() * motion;
                    worldIn.spawnEntity(entityitem);
                }
            }
            worldIn.updateComparatorOutputLevel(pos, this);
        }
        super.breakBlock(worldIn, pos, state);
    }
    @AutoRegister
    public static class TileEntityCranePartitioner extends TileEntityMachineBase implements ITickable {

        public static final int SLOT_COUNT = 45;
        private static final Comparator<ItemStack> STACK_SIZE_COMPARATOR = Comparator.comparingInt(ItemStack::getCount);

        private int[] access;

        public TileEntityCranePartitioner() {
            super(SLOT_COUNT * 2);
        }

        @Override
        public String getDefaultName() {
            return "container.partitioner";
        }

        @Override
        public void update() {
            if (world == null || world.isRemote) return;

            List<ItemStack> stacks = new ArrayList<>();
            for (int i = 0; i < SLOT_COUNT; i++) {
                ItemStack stack = inventory.getStackInSlot(i);
                if (!stack.isEmpty() && !stack.isEmpty()) {
                    stacks.add(stack);
                }
            }

            stacks.sort(STACK_SIZE_COMPARATOR);
            boolean changed = false;
            BlockPos pos = getPos();

            for (ItemStack stack : stacks) {
                int amount = CrystallizerRecipes.getAmount(stack);
                if (amount == 0) amount = stack.getCount();
                while (!stack.isEmpty() && stack.getCount() >= amount) {
                    ItemStack entityStack = stack.copy();
                    entityStack.setCount(amount);
                    stack.shrink(amount);

                    EntityMovingItem item = new EntityMovingItem(world);
                    item.setItemStack(entityStack);
                    item.setPosition(pos.getX() + 0.5D, pos.getY() + 0.25D, pos.getZ() + 0.5D);
                    world.spawnEntity(item);
                    changed = true;
                }
            }

            for (int i = 0; i < SLOT_COUNT; i++) {
                ItemStack stack = inventory.getStackInSlot(i);
                if (!stack.isEmpty() && stack.getCount() <= 0) {
                    inventory.setStackInSlot(i, ItemStack.EMPTY);
                    changed = true;
                }
            }

            if (changed) {
                markDirty();
            }
        }

        @Override
        public boolean canExtractItem(int slot, ItemStack stack, int side) {
            return slot >= SLOT_COUNT;
        }

        @Override
        public boolean isItemValidForSlot(int index, ItemStack stack) {
            return index <= SLOT_COUNT - 1 && CrystallizerRecipes.getAmount(stack) >= 1;
        }

        @Override
        public int[] getAccessibleSlotsFromSide(EnumFacing side) {
            if (access == null) {
                access = new int[SLOT_COUNT * 2];
                for (int i = 0; i < SLOT_COUNT * 2; i++) {
                    access[i] = i;
                }
            }
            return access;
        }
    }

    @SideOnly(Side.CLIENT)
    private ModelResourceLocation getBlockModelLocation() {
        if (blockModelLocation == null) {
            blockModelLocation = new ModelResourceLocation(java.util.Objects.requireNonNull(getRegistryName()), "normal");
        }
        return blockModelLocation;
    }

    @SideOnly(Side.CLIENT)
    private ModelResourceLocation getItemModelLocation() {
        if (itemModelLocation == null) {
            itemModelLocation = new ModelResourceLocation(java.util.Objects.requireNonNull(getRegistryName()), "inventory");
        }
        return itemModelLocation;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerModel() {
        ModelLoader.setCustomModelResourceLocation(Item.getItemFromBlock(this), 0, getItemModelLocation());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void registerSprite(TextureMap map) {
        map.registerSprite(TEX_SIDE);
        map.registerSprite(TEX_TOP);
        map.registerSprite(TEX_BACK);
        map.registerSprite(TEX_BELT);
        map.registerSprite(TEX_INNER);
        map.registerSprite(TEX_INNER_SIDE);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void bakeModel(ModelBakeEvent event) {
        TextureMap map = Minecraft.getMinecraft().getTextureMapBlocks();
        TextureAtlasSprite side = map.getAtlasSprite(TEX_SIDE.toString());
        TextureAtlasSprite top = map.getAtlasSprite(TEX_TOP.toString());
        TextureAtlasSprite back = map.getAtlasSprite(TEX_BACK.toString());
        TextureAtlasSprite belt = map.getAtlasSprite(TEX_BELT.toString());
        TextureAtlasSprite inner = map.getAtlasSprite(TEX_INNER.toString());
        TextureAtlasSprite innerSide = map.getAtlasSprite(TEX_INNER_SIDE.toString());
        HFRWavefrontObject model = (HFRWavefrontObject) ResourceManager.crane_buffer;

        CranePartitionerBakedModel blockModel = CranePartitionerBakedModel.forBlock(model, side, top, back, belt, inner, innerSide);
        CranePartitionerBakedModel itemModel = CranePartitionerBakedModel.forItem(model, side, top, back, belt, inner, innerSide);

        event.getModelRegistry().putObject(getBlockModelLocation(), blockModel);
        event.getModelRegistry().putObject(getItemModelLocation(), itemModel);
    }

    @SideOnly(Side.CLIENT)
    @Override
    public StateMapperBase getStateMapper(ResourceLocation loc) {
        final ModelResourceLocation mrl = new ModelResourceLocation(loc, "normal");
        return new StateMapperBase() {
            @Override
            protected @NotNull ModelResourceLocation getModelResourceLocation(@NotNull IBlockState state) {
                return mrl;
            }
        };
    }
}
