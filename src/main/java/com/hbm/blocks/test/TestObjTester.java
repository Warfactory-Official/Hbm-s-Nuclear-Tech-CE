package com.hbm.blocks.test;

import com.hbm.blocks.ModBlocks;
import com.hbm.entity.effect.EntityCloudTom;
import com.hbm.interfaces.IBomb;
import com.hbm.tileentity.deco.TileEntityObjTester;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TestObjTester extends BlockContainer implements IBomb {

	public static final PropertyDirection FACING = BlockHorizontal.FACING;
	
	public TestObjTester(Material materialIn, String s) {
		super(materialIn);
		this.setTranslationKey(s);
		this.setRegistryName(s);
		
		ModBlocks.ALL_BLOCKS.add(this);
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityObjTester();
	}
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		worldIn.setBlockState(pos, state.withProperty(FACING, placer.getHorizontalFacing().getOpposite()));
		if(!worldIn.isRemote) {
	    	EntityCloudTom tom = new EntityCloudTom(worldIn, 100);
	    	tom.setPosition(pos.getX() + 0.5, pos.getY() + 2, pos.getZ() + 0.5);
	    	worldIn.spawnEntity(tom);
    	}
	}
	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		if(!world.isRemote) {

    		/*world.setBlockToAir(pos);
    		ExplosionNT ex = new ExplosionNT(world, null, pos.getX() + 0.5, pos.getY() + 2, pos.getZ() + 0.5, 5);
    		ex.addAttrib(ExAttrib.ALLDROP);
    		ex.doExplosionA();
    		ex.doExplosionB(false);*/
    	} else {
    		//Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleBFGRing(world, pos.getX() + 0.5, pos.getY() + 25, pos.getZ() + 0.5));
    		try {
    			//Minecraft.getMinecraft().effectRenderer.addEffect(new PhysicsTestParticle(world, pos.getX()+2.5, pos.getY() + 3, pos.getZ() + 3));
    		}catch (Exception x){
    			x.printStackTrace();
    		}
    	}
		return super.onBlockActivated(world, pos, state, playerIn, hand, facing, hitX, hitY, hitZ);
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[]{FACING});
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		return ((EnumFacing)state.getValue(FACING)).getIndex();
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		EnumFacing enumfacing = EnumFacing.byIndex(meta);

        if (enumfacing.getAxis() == EnumFacing.Axis.Y)
        {
            enumfacing = EnumFacing.NORTH;
        }

        return this.getDefaultState().withProperty(FACING, enumfacing);
	}
	
	
	
	@Override
	public IBlockState withRotation(IBlockState state, Rotation rot) {
		return state.withProperty(FACING, rot.rotate((EnumFacing)state.getValue(FACING)));
	}
	
	@Override
	public IBlockState withMirror(IBlockState state, Mirror mirrorIn)
	{
	   return state.withRotation(mirrorIn.toRotation((EnumFacing)state.getValue(FACING)));
	}

	@Override
	public BombReturnCode explode(World world, BlockPos pos, Entity detonator) {
		if(!world.isRemote) {
    		/*world.setBlockToAir(pos);
    		ExplosionNT ex = new ExplosionNT(world, null, pos.getX() + 0.5, pos.getY() + 2, pos.getZ() + 0.5, 5);
    		ex.addAttrib(ExAttrib.ALLDROP);
    		ex.doExplosionA();
    		ex.doExplosionB(false);*/
    	} else {
    		TileEntity te = world.getTileEntity(pos);
    		if(te instanceof TileEntityObjTester){
    			//((TileEntityObjTester)te).fireAge = 0;
    		}
    		try {
    			//Minecraft.getMinecraft().effectRenderer.addEffect(new PhysicsTestParticle(world, pos.getX()+2.5, pos.getY() + 3, pos.getZ() + 5.55));
    		}catch (Exception x){
    			x.printStackTrace();
    		}
    		//Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleLightning(world, pos.getX(), pos.getY()+40, pos.getZ()));
    		//Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleBFGRing(world, pos.getX() + 0.5, pos.getY() + 25, pos.getZ() + 0.5 - 55));
    		/*EntityCreeper creep = new EntityCreeper(Minecraft.getMinecraft().world);
            creep.setPosition(te.getPos().getX()+0.5F, te.getPos().getY()+7, te.getPos().getZ()+6.5F);
            List<Pair<Matrix4f, ModelRenderer>> boxes = ModelRendererUtil.getBoxesFromMob(creep);
            Vec3d nor = new Vec3d(0, 1, 1).normalize();
            float[] plane = new float[]{(float)nor.x, (float)nor.y, (float)nor.z, -0.5F};
            List<Collider> colliders = new ArrayList<>();
            List<Collider> colliders2 = new ArrayList<>();
            for(Pair<Matrix4f, ModelRenderer> p : boxes){
            	for(ModelBox b : p.getRight().cubeList){
            		VertexData[] dat = ModelRendererUtil.cutAndCapModelBox(b, plane, p.getLeft());
            		if(dat[0].positionIndices != null && dat[0].positionIndices.length > 0){
            			Collider c = new ConvexMeshCollider(dat[0].positionIndices, dat[0].vertexArray(), 1);
                		colliders.add(c);
            		}
            		if(dat[1].positionIndices != null && dat[1].positionIndices.length > 0){
            			Collider c = new ConvexMeshCollider(dat[1].positionIndices, dat[1].vertexArray(), 1);
                		colliders2.add(c);
            		}
            	}
            }
            RigidBody r = new RigidBody(te.getWorld(), creep.posX, creep.posY, creep.posZ);
            r.addColliders(colliders.toArray(new Collider[colliders.size()]));
            r.impulseVelocity(new Vec3(0, 1, 0.4), r.globalCentroid.add(0, -0.02, 0));
            Minecraft.getMinecraft().effectRenderer.addEffect(new PhysicsTestParticle(world, r, creep.posX, creep.posY, creep.posZ));
            r = new RigidBody(te.getWorld(), creep.posX, creep.posY, creep.posZ);
            r.addColliders(colliders2.toArray(new Collider[colliders2.size()]));
            r.impulseVelocity(new Vec3(0, 0, -0.1), r.globalCentroid.add(0, 0.02, 0));
            Minecraft.getMinecraft().effectRenderer.addEffect(new PhysicsTestParticle(world, r, creep.posX, creep.posY, creep.posZ));*/
            
    	}

		return BombReturnCode.UNDEFINED;
	}
	

}
