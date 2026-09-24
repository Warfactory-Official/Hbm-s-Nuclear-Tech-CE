package com.hbm.blocks;

import com.google.common.collect.ImmutableMap;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.interfaces.ICopiable;
import com.hbm.items.ClaimedModelLocationRegistry;
import com.hbm.items.IDynamicModels;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.InventoryHelper;
import com.hbm.lib.Library;
import com.hbm.main.MainRegistry;
import com.hbm.main.client.NTMClientRegistry;
import com.hbm.main.client.StaticTesrBakedModels;
import com.hbm.tileentity.IPersistentNBT;
import com.hbm.world.gen.nbt.INBTBlockTransformable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.model.ModelRotation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving.SpawnPlacementType;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.opengl.GL11;

import java.util.*;

public abstract class BlockDummyable extends BlockContainer implements ICustomBlockHighlight, ICopiable, INBTBlockTransformable, IDynamicModels {

    //Drillgon200: I'm far to lazy to figure out what all the meta values should be translated to in properties
    public static final PropertyInteger META = PropertyInteger.create("meta", 0, 15);
    /// BLOCK METADATA ///

    //0-5 		dummy rotation 		(for dummy neighbor checks)
    //6-11 		extra 				(6 rotations with flag, for pipe connectors and the like)
    //12-15 	block rotation 		(for rendering the TE)

    //meta offset from dummy to TE rotation
    public static final int offset = 10;
    //meta offset from dummy to extra rotation
    public static final int extra = 6;
    private static final long NO_CORE = Long.MIN_VALUE;
    private static final AxisAlignedBB DETAIL_AABB = new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, 0.999F, 1.0F);
    public static boolean safeRem = false;
    public List<AxisAlignedBB> bounding = new ArrayList<>();

    public BlockDummyable(Material materialIn, String s) {
        super(materialIn);
        this.setTranslationKey(s);
        this.setRegistryName(s);
        this.setTickRandomly(true);

        ModBlocks.ALL_BLOCKS.add(this);
        IDynamicModels.INSTANCES.add(this);
    }

    public BlockDummyable(Material materialIn, String s, boolean ignoredDontUseIDynamicModel) {
        super(materialIn);
        this.setTranslationKey(s);
        this.setRegistryName(s);
        this.setTickRandomly(true);

        ModBlocks.ALL_BLOCKS.add(this);
    }

    @Override
    public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
        return BlockFaceShape.UNDEFINED;
    }

    protected int getMaxCoreSearchSteps() {
        return 512;
    }

    protected boolean isSameMultiblock(Block other) {
        return other == this;
    }

    private long findCoreSerialized(IBlockAccess world, BlockPos pos, BlockPos.MutableBlockPos scratch) {
        return findCoreSerialized(world, pos.getX(), pos.getY(), pos.getZ(), scratch);
    }

    private long findCoreSerialized(IBlockAccess world, int x, int y, int z, BlockPos.MutableBlockPos scratch) {
        for (int steps = 0, max = getMaxCoreSearchSteps(); steps < max; steps++) {
            scratch.setPos(x, y, z);
            IBlockState state = world.getBlockState(scratch);
            if (!isSameMultiblock(state.getBlock())) return NO_CORE;
            int meta = state.getValue(META);
            if (meta >= 12) return Library.blockPosToLong(x, y, z);
            if (meta >= extra) meta -= extra;
            ForgeDirection dir = ForgeDirection.getOrientation(meta).getOpposite();
            x += dir.offsetX;
            y += dir.offsetY;
            z += dir.offsetZ;
        }
        return NO_CORE;
    }

    @Nullable
    public BlockPos findCore(IBlockAccess world, BlockPos pos) {
        BlockPos.MutableBlockPos scratch = new BlockPos.MutableBlockPos();
        long core = findCoreSerialized(world, pos, scratch);
        if (core == NO_CORE) return null;
        return BlockPos.fromLong(core);
    }

    @Nullable
    public TileEntity findCoreTE(IBlockAccess world, BlockPos pos) {
        BlockPos.MutableBlockPos scratch = new BlockPos.MutableBlockPos();
        long core = findCoreSerialized(world, pos, scratch);
        if (core == NO_CORE) return null;
        Library.fromLong(scratch, core);
        return world.getTileEntity(scratch);
    }

    @Nullable
    public TileEntity findCoreTE(IBlockAccess world, int x, int y, int z) {
        BlockPos.MutableBlockPos scratch = new BlockPos.MutableBlockPos();
        long core = findCoreSerialized(world, x, y, z, scratch);
        if (core == NO_CORE) return null;
        Library.fromLong(scratch, core);
        return world.getTileEntity(scratch);
    }

    public int @Nullable [] findCore(IBlockAccess world, int x, int y, int z) {
        BlockPos.MutableBlockPos scratch = new BlockPos.MutableBlockPos();
        long core = findCoreSerialized(world, x, y, z, scratch);
        if (core == NO_CORE) return null;
        return new int[]{Library.getBlockPosX(core), Library.getBlockPosY(core), Library.getBlockPosZ(core)};
    }

    @Override
    public void neighborChanged(@NotNull IBlockState state, World world, @NotNull BlockPos pos, @NotNull Block blockIn, @NotNull BlockPos fromPos) {
        if (world.isRemote || safeRem) return;
        cascadeOrphans(world, pos, state);
    }

    @Override
    public void updateTick(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull Random rand) {
        super.updateTick(world, pos, state, rand);
        if (world.isRemote) return;
        cascadeOrphans(world, pos, state);
    }

    private boolean isOrphan(IBlockAccess world, BlockPos pos, IBlockState state) {
        int meta = state.getValue(META);
        if (meta >= 12) return false; // core, not a dummy
        if (meta >= extra) meta -= extra;
        ForgeDirection dir = ForgeDirection.getOrientation(meta).getOpposite();
        BlockPos other = pos.add(dir.offsetX, dir.offsetY, dir.offsetZ);
        return !isSameMultiblock(world.getBlockState(other).getBlock());
    }

    // Iterative orphan cascade. Suppresses re-entry via safeRem so setBlockToAir's
    // neighbor notifications don't recurse into neighborChanged; we manually queue
    // the freshly-orphaned neighbors instead. Without this, destroying a long dummy
    // chain blows the JVM stack (each recursion level burns ~8 frames). idk why it isn't crashing in 1.7
    private void cascadeOrphans(World world, BlockPos start, IBlockState startState) {
        if (!isSameMultiblock(startState.getBlock()) || !isOrphan(world, start, startState)) return;
        safeRem = true;
        try {
            ArrayDeque<BlockPos> queue = new ArrayDeque<>();
            queue.add(start);
            while (!queue.isEmpty()) {
                BlockPos p = queue.poll();
                IBlockState s = world.getBlockState(p);
                if (!isSameMultiblock(s.getBlock())) continue;
                if (!isOrphan(world, p, s)) continue;
                world.setBlockToAir(p);
                for (ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
                    queue.add(p.add(d.offsetX, d.offsetY, d.offsetZ));
                }
            }
        } finally {
            safeRem = false;
        }
    }

    @Override
    public void onBlockPlacedBy(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState state, @NotNull EntityLivingBase player, @NotNull ItemStack itemStack) {
    	if(!(player instanceof EntityPlayer pl))
			return;
		safeRem = true;
    	world.setBlockToAir(pos);
		safeRem = false;

        EnumHand hand = pl.getHeldItemMainhand() == itemStack ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
		
		int i = MathHelper.floor(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
		int o = -getOffset();
		pos = new BlockPos(pos.getX(), pos.getY() + getHeightOffset(), pos.getZ());

		ForgeDirection dir = switch (i) {
            case 0 -> ForgeDirection.getOrientation(2);
            case 1 -> ForgeDirection.getOrientation(5);
            case 2 -> ForgeDirection.getOrientation(3);
            case 3 -> ForgeDirection.getOrientation(4);
            default -> ForgeDirection.NORTH;
        };
		
		dir = getDirModified(dir);
		
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		
		if(!checkRequirement(world, x, y, z, dir, o)) {
			if(!pl.capabilities.isCreativeMode) {
				ItemStack stack = pl.inventory.mainInventory.get(pl.inventory.currentItem);
				Item item = Item.getItemFromBlock(this);
				
				if(stack.isEmpty()) {
					pl.inventory.mainInventory.set(pl.inventory.currentItem, new ItemStack(this));
				} else {
					if(stack.getItem() != item || stack.getCount() == stack.getMaxStackSize()) {
						pl.inventory.addItemStackToInventory(new ItemStack(this));
					} else {
						pl.getHeldItem(hand).grow(1);
					}
				}
			}
			
			return;
		}
		
		if(!world.isRemote){
			BlockPos cur = new BlockPos(x + dir.offsetX * o , y + dir.offsetY * o, z + dir.offsetZ * o);
            int meta = getMetaForCore(world, cur, pl, dir.ordinal() + offset);
			world.setBlockState(cur, this.getDefaultState().withProperty(META, meta), 3);
			IPersistentNBT.onBlockPlacedBy(world, cur, itemStack);
			fillSpace(world, x, y, z, dir, o);
		}
		pos = new BlockPos(pos.getX(), pos.getY() - getHeightOffset(), pos.getZ());
		world.scheduleUpdate(pos, this, 1);
		world.scheduleUpdate(pos, this, 2);

    	super.onBlockPlacedBy(world, pos, state, player, itemStack);
    }

	protected boolean standardOpenBehavior(World world, BlockPos pos, EntityPlayer player, int id){
		return this.standardOpenBehavior(world, pos.getX(), pos.getY(), pos.getZ(), player, id);
	}

    protected boolean standardOpenBehavior(World world, int x, int y, int z, EntityPlayer player, int id) {
		
		if(world.isRemote) {
			return !player.isSneaking();
		} else if(!player.isSneaking()) {
			int[] pos = this.findCore(world, x, y, z);

			if(pos == null)
				return false;

			player.openGui(MainRegistry.instance, id, world, pos[0], pos[1], pos[2]);
			return true;
		} else {
			return false;
		}
	}

    /**
     * A bit more advanced than the dir modifier, but it is important that the resulting direction meta is in the core range.
     * Using the "extra" metas is technically possible but requires a bit of tinkering, e.g. preventing a recursive loop
     * in the core finder and making sure the TE uses the right metas.
     */
    protected int getMetaForCore(World world, BlockPos pos, EntityPlayer player, int original) {
        return original;
    }

    public ForgeDirection getDirModified(ForgeDirection dir) {
		return dir;
	}

	protected final EnumFacing getDirModified(EnumFacing dir) {
        if (dir == null) return null;
        ForgeDirection modified = getDirModified(ForgeDirection.getOrientation(dir));
        EnumFacing facing = modified.toEnumFacing();
		return facing != null ? facing : dir;
	}

    public boolean checkRequirement(World world, int x, int y, int z, ForgeDirection dir, int o) {
		return MultiblockHandlerXR.checkSpace(world, x + dir.offsetX * o , y + dir.offsetY * o, z + dir.offsetZ * o, getDimensions(), x, y, z, dir);
	}
	
	protected void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		MultiblockHandlerXR.fillSpace(world, x + dir.offsetX * o , y + dir.offsetY * o, z + dir.offsetZ * o, getDimensions(), this, dir);
	}
	
	//"upgrades" regular dummy blocks to ones with the extra flag
	public void makeExtra(World world, int x, int y, int z) {
		BlockPos pos = new BlockPos(x, y, z);
		if(world.getBlockState(pos).getBlock() != this)
			return;
		
		int meta = world.getBlockState(pos).getValue(META);
		
		if(meta > 5)
			return;
			
		//world.setBlockMetadataWithNotify(x, y, z, meta + extra, 3);
		safeRem = true;
		world.setBlockState(pos, this.getDefaultState().withProperty(META, meta + extra), 3);
		safeRem = false;
	}
	
	//Drillgon200: Removes the extra. I could have sworn there was already a method for this, but I can't find it.
	public void removeExtra(World world, int x, int y, int z) {
		BlockPos pos = new BlockPos(x, y, z);
		if(world.getBlockState(pos).getBlock() != this)
			return;
		
		int meta = world.getBlockState(pos).getValue(META);
		
		if(meta <= 5 || meta >= 12)
			return;
			
		//world.setBlockMetadataWithNotify(x, y, z, meta + extra, 3);
		safeRem = true;
		world.setBlockState(pos, this.getDefaultState().withProperty(META, meta - extra), 3);
		safeRem = false;
	}
		
	//checks if the dummy metadata is within the extra range
	public boolean hasExtra(int meta) {
		return meta > 5 && meta < 12;
	}
	
	@Override
	public void breakBlock(@NotNull World world, @NotNull BlockPos pos, IBlockState state) {
		int i = state.getValue(META);
		if(i < 12 && !safeRem) {

			if(i >= extra)
				i -= extra;

            ForgeDirection dir = ForgeDirection.getOrientation(i).getOpposite();

            BlockPos.MutableBlockPos scratch = new BlockPos.MutableBlockPos();
            long core = findCoreSerialized(world, pos.getX() + dir.offsetX, pos.getY() + dir.offsetY, pos.getZ() + dir.offsetZ, scratch);

            if (core != NO_CORE) {
                Library.fromLong(scratch, core);
                world.setBlockToAir(scratch);
            }
        }
        InventoryHelper.dropInventoryItems(world, pos, world.getTileEntity(pos));
        super.breakBlock(world, pos, state);
    }

	public boolean useDetailedHitbox() {
		return !bounding.isEmpty();
	}

    @Override
    public void addCollisionBoxToList(@NotNull IBlockState state, @NotNull World worldIn, @NotNull BlockPos pos, @NotNull AxisAlignedBB entityBox,
                                      @NotNull List<AxisAlignedBB> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
        if (!this.useDetailedHitbox()) {
            super.addCollisionBoxToList(state, worldIn, pos, entityBox, collidingBoxes, entityIn, isActualState);
            return;
        }

        BlockPos.MutableBlockPos scratch = new BlockPos.MutableBlockPos();
        long core = findCoreSerialized(worldIn, pos, scratch);
        if (core == NO_CORE) return;

        int coreX = Library.getBlockPosX(core);
        int coreY = Library.getBlockPosY(core);
        int coreZ = Library.getBlockPosZ(core);

        scratch.setPos(coreX, coreY, coreZ);
        IBlockState coreState = worldIn.getBlockState(scratch);

        for (AxisAlignedBB aabb : this.bounding) {
            AxisAlignedBB rotatedBox = getAABBRotationOffset(aabb, coreX + 0.5, coreY, coreZ + 0.5, getRotationFromState(coreState));

            if (entityBox.intersects(rotatedBox)) {
                collidingBoxes.add(rotatedBox);
            }
        }
    }

	private ForgeDirection getRotationFromState(IBlockState state) {
		int meta = state.getValue(META);
		return ForgeDirection.getOrientation(meta - offset).getRotation(ForgeDirection.UP);
	}

	public static AxisAlignedBB getAABBRotationOffset(AxisAlignedBB aabb, double x, double y, double z, ForgeDirection dir) {
		AxisAlignedBB newBox = null;

		if (dir == ForgeDirection.NORTH) {
			newBox = new AxisAlignedBB(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ);
		} else if (dir == ForgeDirection.EAST) {
			newBox = new AxisAlignedBB(-aabb.maxZ, aabb.minY, aabb.minX, -aabb.minZ, aabb.maxY, aabb.maxX);
		} else if (dir == ForgeDirection.SOUTH) {
			newBox = new AxisAlignedBB(-aabb.maxX, aabb.minY, -aabb.maxZ, -aabb.minX, aabb.maxY, -aabb.minZ);
		} else if (dir == ForgeDirection.WEST) {
			newBox = new AxisAlignedBB(aabb.minZ, aabb.minY, -aabb.maxX, aabb.maxZ, aabb.maxY, -aabb.minX);
		}

		if (newBox != null) {
			return newBox.offset(x, y, z);
		}

		return new AxisAlignedBB(aabb.minX, aabb.minY, aabb.minZ, aabb.maxX, aabb.maxY, aabb.maxZ).offset(x + 0.5, y + 0.5, z + 0.5);
	}
	
	@Override
	public boolean isOpaqueCube(@NotNull IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isBlockNormalCube(@NotNull IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isNormalCube(@NotNull IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isNormalCube(@NotNull IBlockState state, @NotNull IBlockAccess world, @NotNull BlockPos pos) {
		return false;
	}
	@Override
	public boolean shouldSideBeRendered(@NotNull IBlockState blockState, @NotNull IBlockAccess blockAccess, @NotNull BlockPos pos, @NotNull EnumFacing side) {
		return false;
	}
	
	@Override
	protected @NotNull BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, META);
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(META);
	}
	
	@Override
	public @NotNull IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(META, meta);
	}
	
	/**
	 * @returns an int array with six fields, describing the amount of dummy blocks in each direction around the core. order is UP, DOWN, FORWARD, BACKWARD, LEFT, RIGHT
	 */
	public abstract int[] getDimensions();

	public abstract int getOffset();
	
	public int getHeightOffset() {
		return 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean shouldDrawHighlight(World world, BlockPos pos) {
		return !this.bounding.isEmpty();
	}

    @Override
    @SideOnly(Side.CLIENT)
    public void drawHighlight(DrawBlockHighlightEvent event, World world, BlockPos pos) {
        BlockPos.MutableBlockPos scratch = new BlockPos.MutableBlockPos();
        long core = findCoreSerialized(world, pos, scratch);
        if (core == NO_CORE) return;

        int coreX = Library.getBlockPosX(core);
        int coreY = Library.getBlockPosY(core);
        int coreZ = Library.getBlockPosZ(core);

        EntityPlayer player = event.getPlayer();
        float interp = event.getPartialTicks();
        double dX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) interp;
        double dY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) interp;
        double dZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) interp;
        float exp = 0.002F;

        scratch.setPos(coreX, coreY, coreZ);
        int meta = world.getBlockState(scratch).getValue(META);

        ICustomBlockHighlight.setup();
        for (AxisAlignedBB aabb : this.bounding)
            RenderGlobal.drawSelectionBoundingBox(getAABBRotationOffset(aabb.expand(exp, exp, exp), 0, 0, 0,
                    ForgeDirection.getOrientation(meta - offset).getRotation(ForgeDirection.UP)).offset(coreX - dX + 0.5, coreY - dY,
                    coreZ - dZ + 0.5), 0, 0, 0, 1.0F);
        ICustomBlockHighlight.cleanup();
    }

	@Override
	public boolean causesSuffocation(IBlockState state) {
		return false;
	}

	/// crappers spawning on large machines pmo
	@Override
	public boolean canCreatureSpawn(IBlockState state,IBlockAccess world,BlockPos pos,SpawnPlacementType type) {
		return false;
	}

	@Override
	public @NotNull AxisAlignedBB getBoundingBox(@NotNull IBlockState state, @NotNull IBlockAccess source, @NotNull BlockPos pos) {
		if (!this.useDetailedHitbox()) {
			return FULL_BLOCK_AABB;
		} else {
			return DETAIL_AABB;
		}
	}

    @Override
    public NBTTagCompound getSettings(World world, int x, int y, int z) {
        BlockPos.MutableBlockPos scratch = new BlockPos.MutableBlockPos();
        long core = findCoreSerialized(world, x, y, z, scratch);
        if (core == NO_CORE) return null;

        Library.fromLong(scratch, core);
        TileEntity tile = world.getTileEntity(scratch);
        if (tile instanceof ICopiable) return ((ICopiable) tile).getSettings(world, scratch.getX(), scratch.getY(), scratch.getZ());
        else return null;
    }

    @Override
    public void pasteSettings(NBTTagCompound nbt, int index, World world, EntityPlayer player, int x, int y, int z) {
        BlockPos.MutableBlockPos scratch = new BlockPos.MutableBlockPos();
        long core = findCoreSerialized(world, x, y, z, scratch);
        if (core == NO_CORE) return;
        Library.fromLong(scratch, core);
        TileEntity tile = world.getTileEntity(scratch);
        if (tile instanceof ICopiable) ((ICopiable) tile).pasteSettings(nbt, index, world, player, scratch.getX(), scratch.getY(), scratch.getZ());
    }

    @Override
    public String[] infoForDisplay(World world, int x, int y, int z) {
        BlockPos.MutableBlockPos scratch = new BlockPos.MutableBlockPos();
        long core = findCoreSerialized(world, x, y, z, scratch);
        if (core == NO_CORE) return null;
        Library.fromLong(scratch, core);
        TileEntity tile = world.getTileEntity(scratch);
        if (tile instanceof ICopiable) return ((ICopiable) tile).infoForDisplay(world, x, y, z);
        return null;
    }

	@Override
	public int transformMeta(int meta, int coordBaseMode) {
		boolean isOffset = meta >= 12; // squishing causes issues
		boolean isExtra = !isOffset && meta >= extra;

		if(isOffset) {
			meta -= offset;
		} else if(isExtra) {
			meta -= extra;
		}

		meta = INBTBlockTransformable.transformMetaDeco(meta, coordBaseMode);

		if(isOffset) {
			meta += offset;
		} else if(isExtra) {
			meta += extra;
		}

		return meta;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void bakeModel(ModelBakeEvent event) {
		try {
			IModel blockBaseModel = ModelLoaderRegistry.getModel(new ResourceLocation("block/cube_all"));
			ImmutableMap<String, String> blockTextures = ImmutableMap.of("all", "hbm:blocks/block_steel");
			IModel blockRetextured = blockBaseModel.retexture(blockTextures);
			IBakedModel blockBaked = blockRetextured.bake(
					ModelRotation.X0_Y0,
					DefaultVertexFormats.BLOCK,
					ModelLoader.defaultTextureGetter()
			);
			ModelResourceLocation worldLocation = new ModelResourceLocation(getRegistryName(), "normal");
			event.getModelRegistry().putObject(worldLocation, blockBaked);
            if (!ClaimedModelLocationRegistry.hasSyntheticTeisrBinding(Item.getItemFromBlock(this))) {
				IModel itemBaseModel = ModelLoaderRegistry.getModel(new ResourceLocation("item/generated"));
				ImmutableMap<String, String> itemTextures = ImmutableMap.of("layer0", "hbm:blocks/" + getRegistryName().getPath());
				IModel itemRetextured = itemBaseModel.retexture(itemTextures);
				IBakedModel itemBaked = itemRetextured.bake(
						ModelRotation.X0_Y0,
						DefaultVertexFormats.ITEM,
						ModelLoader.defaultTextureGetter()
				);
				ModelResourceLocation inventoryLocation = new ModelResourceLocation(getRegistryName(), "inventory");
				event.getModelRegistry().putObject(inventoryLocation, itemBaked);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	@Override
	@SideOnly(Side.CLIENT)
	public void registerModel() {
		Item item = Item.getItemFromBlock(this);
		ModelResourceLocation syntheticLocation = NTMClientRegistry.getSyntheticTeisrModelLocation(item);
		ModelLoader.setCustomModelResourceLocation(item, 0, syntheticLocation != null ? syntheticLocation : new ModelResourceLocation(this.getRegistryName(), "inventory"));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerSprite(TextureMap map) {
	}

	@Override
	@SideOnly(Side.CLIENT)
	public EnumBlockRenderType getRenderType(IBlockState state) {
		if (StaticTesrBakedModels.isManagedBlock(this)) {
			return EnumBlockRenderType.MODEL;
		}
		return super.getRenderType(state);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getRenderLayer() {
		if (StaticTesrBakedModels.isManagedBlock(this)) {
			return BlockRenderLayer.CUTOUT;
		}
		return super.getRenderLayer();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {
		if (StaticTesrBakedModels.isManagedBlock(this)) {
			return layer == BlockRenderLayer.CUTOUT;
		}
		return super.canRenderInLayer(state, layer);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public StateMapperBase getStateMapper(ResourceLocation loc) {
		return new StateMapperBase() {
			@Override
			protected @NotNull ModelResourceLocation getModelResourceLocation(@NotNull IBlockState state) {
				return new ModelResourceLocation(loc, "normal");
			}
		};
	}

    public int[][] getAllDimensions() {
        return new int[][] { getDimensions() };
    }

    public double[][] getAABBExtras() {
        return new double[0][0];
    }

    @SideOnly(Side.CLIENT)
    public void drawPlacementHighlight(EntityPlayer player, float interp) {
        RayTraceResult mop = player.rayTrace(5.0D, interp);

        if(mop != null && mop.typeOfHit == RayTraceResult.Type.BLOCK) {
            double dX = player.lastTickPosX + (player.posX - player.lastTickPosX) * (double) interp;
            double dY = player.lastTickPosY + (player.posY - player.lastTickPosY) * (double) interp;
            double dZ = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * (double) interp;

            int i = MathHelper.floor(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
            int o = -getOffset();
            int pY = mop.getBlockPos().getY() + getHeightOffset();

            // Orientation
            ForgeDirection facing = ForgeDirection.NORTH;
            if(i == 0) facing = ForgeDirection.getOrientation(2);
            if(i == 1) facing = ForgeDirection.getOrientation(5);
            if(i == 2) facing = ForgeDirection.getOrientation(3);
            if(i == 3) facing = ForgeDirection.getOrientation(4);

            ForgeDirection sideHit = ForgeDirection.getOrientation(mop.sideHit.getIndex());
            facing = getDirModified(facing);

            double originX = mop.getBlockPos().getX() + facing.offsetX * o + sideHit.offsetX;
            double originY = pY + sideHit.offsetY;
            double originZ = mop.getBlockPos().getZ() + facing.offsetZ * o + sideHit.offsetZ;

            boolean canPlace = checkRequirement(player.world, mop.getBlockPos().getX() + sideHit.offsetX, pY + sideHit.offsetY, mop.getBlockPos().getZ() + sideHit.offsetZ, facing, o);
            Tessellator tess = Tessellator.getInstance();
            BufferBuilder buffer = tess.getBuffer();

            GlStateManager.pushMatrix();
            GlStateManager.disableLighting();
            GlStateManager.disableTexture2D();
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240F, 240F);
            GlStateManager.glLineWidth(2.0F);
            GlStateManager.depthMask(false);

            buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);

            double timer = (System.currentTimeMillis() % (1000D * Math.PI)) / 250D;
            double sine = Math.sin(timer);
            int color = (int) (255 * (sine * 0.25 + 0.75));

            int r = canPlace ? 0 : color;
            int g = canPlace ? color : 0;
            int b = 0;
            int a = 255;

            List<BlockPos> blocks = new ArrayList<>();
            Set<BlockPos> set = new HashSet<>();

            for(int[] dims : getAllDimensions()) {
                // Some of the multiblocks have offsets for the placements, so
                // this allows for the ones that dont need it to have a bunch of
                // 0s at the end.
                int offFwd = dims.length > 6 ? dims[6] : 0;
                int offUp = dims.length > 7 ? dims[7] : 0;
                int offLat = dims.length > 8 ? dims[8] : 0;
                int worldOffX;
                int worldOffY;
                int worldOffZ;

                worldOffY = offUp;
                worldOffX = facing.offsetX * offFwd + facing.getRotation(ForgeDirection.UP).offsetX * offLat;
                worldOffZ = facing.offsetZ * offFwd + facing.getRotation(ForgeDirection.UP).offsetZ * offLat;

                int[] rot = MultiblockHandlerXR.rotate(dims, facing.toEnumFacing());
                for(int bx = -rot[4] + worldOffX; bx <= rot[5] + worldOffX; bx++) {
                    for(int by = -rot[1] + worldOffY; by <= rot[0] + worldOffY; by++) {
                        for(int bz = -rot[2] + worldOffZ; bz <= rot[3] + worldOffZ; bz++) {
                            BlockPos bp = new BlockPos(MathHelper.floor(originX) + bx, MathHelper.floor(originY) + by, MathHelper.floor(originZ) + bz);
                            blocks.add(bp);
                            set.add(bp);
                        }
                    }
                }
            }
            // This looks for the blocks nearby and draws lines between the
            // vertexes to make different shaped boxes.
            // Most of this was taken from Mellow (Thanks mellow) -Wolf
            for(BlockPos pos : blocks) {
                boolean px = set.contains(pos.add(1, 0, 0));
                boolean nx = set.contains(pos.add(-1, 0, 0));
                boolean ppy = set.contains(pos.add(0, 1, 0));
                boolean ny = set.contains(pos.add(0, -1, 0));
                boolean ppz = set.contains(pos.add(0, 0, 1));
                boolean nz = set.contains(pos.add(0, 0, -1));

                double minX = pos.getX() - dX;
                double maxX = pos.getX() + 1 - dX;
                double minY = pos.getY() - dY;
                double maxY = pos.getY() + 1 - dY;
                double minZ = pos.getZ() - dZ;
                double maxZ = pos.getZ() + 1 - dZ;

                if(!ppy) {
                    if(!nx) {
                        buffer.pos(minX, maxY, minZ).color(r, g, b, a).endVertex();
                        buffer.pos(minX, maxY, maxZ).color(r, g, b, a).endVertex();
                    }
                    if(!ppz) {
                        buffer.pos(minX, maxY, maxZ).color(r, g, b, a).endVertex();
                        buffer.pos(maxX, maxY, maxZ).color(r, g, b, a).endVertex();
                    }
                    if(!px) {
                        buffer.pos(maxX, maxY, maxZ).color(r, g, b, a).endVertex();
                        buffer.pos(maxX, maxY, minZ).color(r, g, b, a).endVertex();
                    }
                    if(!nz) {
                        buffer.pos(maxX, maxY, minZ).color(r, g, b, a).endVertex();
                        buffer.pos(minX, maxY, minZ).color(r, g, b, a).endVertex();
                    }
                }
                if(!ny) {
                    if(!nx) {
                        buffer.pos(minX, minY, minZ).color(r, g, b, a).endVertex();
                        buffer.pos(minX, minY, maxZ).color(r, g, b, a).endVertex();
                    }
                    if(!ppz) {
                        buffer.pos(minX, minY, maxZ).color(r, g, b, a).endVertex();
                        buffer.pos(maxX, minY, maxZ).color(r, g, b, a).endVertex();
                    }
                    if(!px) {
                        buffer.pos(maxX, minY, maxZ).color(r, g, b, a).endVertex();
                        buffer.pos(maxX, minY, minZ).color(r, g, b, a).endVertex();
                    }
                    if(!nz) {
                        buffer.pos(maxX, minY, minZ).color(r, g, b, a).endVertex();
                        buffer.pos(minX, minY, minZ).color(r, g, b, a).endVertex();
                    }
                }
                if(!nz) {
                    if(!nx) {
                        buffer.pos(minX, minY, minZ).color(r, g, b, a).endVertex();
                        buffer.pos(minX, maxY, minZ).color(r, g, b, a).endVertex();
                    }
                    if(!ppy) {
                        buffer.pos(minX, maxY, minZ).color(r, g, b, a).endVertex();
                        buffer.pos(maxX, maxY, minZ).color(r, g, b, a).endVertex();
                    }
                    if(!px) {
                        buffer.pos(maxX, maxY, minZ).color(r, g, b, a).endVertex();
                        buffer.pos(maxX, minY, minZ).color(r, g, b, a).endVertex();
                    }
                    if(!ny) {
                        buffer.pos(maxX, minY, minZ).color(r, g, b, a).endVertex();
                        buffer.pos(minX, minY, minZ).color(r, g, b, a).endVertex();
                    }
                }
                if(!ppz) {
                    if(!nx) {
                        buffer.pos(minX, minY, maxZ).color(r, g, b, a).endVertex();
                        buffer.pos(minX, maxY, maxZ).color(r, g, b, a).endVertex();
                    }
                    if(!ppy) {
                        buffer.pos(minX, maxY, maxZ).color(r, g, b, a).endVertex();
                        buffer.pos(maxX, maxY, maxZ).color(r, g, b, a).endVertex();
                    }
                    if(!px) {
                        buffer.pos(maxX, maxY, maxZ).color(r, g, b, a).endVertex();
                        buffer.pos(maxX, minY, maxZ).color(r, g, b, a).endVertex();
                    }
                    if(!ny) {
                        buffer.pos(maxX, minY, maxZ).color(r, g, b, a).endVertex();
                        buffer.pos(minX, minY, maxZ).color(r, g, b, a).endVertex();
                    }
                }
                if(!nx) {
                    if(!nz) {
                        buffer.pos(minX, minY, minZ).color(r, g, b, a).endVertex();
                        buffer.pos(minX, maxY, minZ).color(r, g, b, a).endVertex();
                    }
                    if(!ppy) {
                        buffer.pos(minX, maxY, minZ).color(r, g, b, a).endVertex();
                        buffer.pos(minX, maxY, maxZ).color(r, g, b, a).endVertex();
                    }
                    if(!ppz) {
                        buffer.pos(minX, maxY, maxZ).color(r, g, b, a).endVertex();
                        buffer.pos(minX, minY, maxZ).color(r, g, b, a).endVertex();
                    }
                    if(!ny) {
                        buffer.pos(minX, minY, maxZ).color(r, g, b, a).endVertex();
                        buffer.pos(minX, minY, minZ).color(r, g, b, a).endVertex();
                    }
                }
                if(!px) {
                    if(!nz) {
                        buffer.pos(maxX, minY, minZ).color(r, g, b, a).endVertex();
                        buffer.pos(maxX, maxY, minZ).color(r, g, b, a).endVertex();
                    }
                    if(!ppy) {
                        buffer.pos(maxX, maxY, minZ).color(r, g, b, a).endVertex();
                        buffer.pos(maxX, maxY, maxZ).color(r, g, b, a).endVertex();
                    }
                    if(!ppz) {
                        buffer.pos(maxX, maxY, maxZ).color(r, g, b, a).endVertex();
                        buffer.pos(maxX, minY, maxZ).color(r, g, b, a).endVertex();
                    }
                    if(!ny) {
                        buffer.pos(maxX, minY, maxZ).color(r, g, b, a).endVertex();
                        buffer.pos(maxX, minY, minZ).color(r, g, b, a).endVertex();
                    }
                }
            }

            b = color;
            r = 0;
            g = 0;

            // boo-yeah
            for(double[] extra : this.getAABBExtras()) {
                ForgeDirection rot = facing.getRotation(ForgeDirection.UP);
                double cX = MathHelper.floor(originX) - dX + 0.5;
                double cY = MathHelper.floor(originY) - dY;
                double cZ = MathHelper.floor(originZ) - dZ + 0.5;

                double upr = extra[0];
                double lwr = extra[1];
                double fwd = extra[2];
                double bwd = extra[3];
                double lft = extra[4];
                double rgt = extra[5];

                double x0 = cX + fwd * facing.offsetX + lft * rot.offsetX;
                double x1 = cX + bwd * facing.offsetX + rgt * rot.offsetX;
                double y0 = cY + lwr;
                double y1 = cY + upr;
                double z0 = cZ + fwd * facing.offsetZ + lft * rot.offsetZ;
                double z1 = cZ + bwd * facing.offsetZ + rgt * rot.offsetZ;

                buffer.pos(x0, y0, z0).color(r, g, b, a).endVertex(); buffer.pos(x0, y0, z1).color(r, g, b, a).endVertex();
                buffer.pos(x1, y0, z0).color(r, g, b, a).endVertex(); buffer.pos(x1, y0, z1).color(r, g, b, a).endVertex();
                buffer.pos(x0, y0, z0).color(r, g, b, a).endVertex(); buffer.pos(x1, y0, z0).color(r, g, b, a).endVertex();
                buffer.pos(x0, y0, z1).color(r, g, b, a).endVertex(); buffer.pos(x1, y0, z1).color(r, g, b, a).endVertex();

                buffer.pos(x0, y1, z0).color(r, g, b, a).endVertex(); buffer.pos(x0, y1, z1).color(r, g, b, a).endVertex();
                buffer.pos(x1, y1, z0).color(r, g, b, a).endVertex(); buffer.pos(x1, y1, z1).color(r, g, b, a).endVertex();
                buffer.pos(x0, y1, z0).color(r, g, b, a).endVertex(); buffer.pos(x1, y1, z0).color(r, g, b, a).endVertex();
                buffer.pos(x0, y1, z1).color(r, g, b, a).endVertex(); buffer.pos(x1, y1, z1).color(r, g, b, a).endVertex();

                buffer.pos(x0, y0, z0).color(r, g, b, a).endVertex(); buffer.pos(x0, y1, z0).color(r, g, b, a).endVertex();
                buffer.pos(x1, y0, z0).color(r, g, b, a).endVertex(); buffer.pos(x1, y1, z0).color(r, g, b, a).endVertex();
                buffer.pos(x0, y0, z1).color(r, g, b, a).endVertex(); buffer.pos(x0, y1, z1).color(r, g, b, a).endVertex();
                buffer.pos(x1, y0, z1).color(r, g, b, a).endVertex(); buffer.pos(x1, y1, z1).color(r, g, b, a).endVertex();
            }

            tess.draw();

            GlStateManager.depthMask(true);
            OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, OpenGlHelper.lastBrightnessX, OpenGlHelper.lastBrightnessY);
            GlStateManager.enableTexture2D();
            GlStateManager.enableLighting();
            GlStateManager.popMatrix();
        }
    }
}
