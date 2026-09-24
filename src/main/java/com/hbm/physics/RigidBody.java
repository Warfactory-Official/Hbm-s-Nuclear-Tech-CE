package com.hbm.physics;

import com.hbm.main.ClientProxy;
import com.hbm.physics.GJK.GJKInfo;
import com.hbm.physics.GJK.Result;
import com.hbm.util.Vec3NT;
import com.hbm.util.BobMathUtil;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.opengl.GL11; import net.minecraft.client.renderer.GlStateManager;

import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix3f;
import javax.vecmath.Quat4f;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class RigidBody {

	public static final Vec3NT[] cardinals = new Vec3NT[]{
			new Vec3NT(1, 0, 0), new Vec3NT(0, 1, 0), new Vec3NT(0, 0, 1),
			new Vec3NT(-1, 0, 0), new Vec3NT(0, -1, 0), new Vec3NT(0, 0, -1)};
	
	public static final RigidBody DUMMY = new RigidBody(null){
		public void solveContacts(float dt) {};
		
		public void impulse(Vec3NT force, Vec3NT position) {};
		public void updateOrientation() {};
		public void updateGlobalCentroidFromPosition() {};
		public void updatePositionFromGlobalCentroid() {};
		public void doTimeStep(float dt) {};
		public void addColliders(Collider... collide) {};
		
		public Vec3NT globalToLocalPos(Vec3NT pos) {return pos;};
		public Vec3NT localToGlobalPos(Vec3NT pos) {return pos;};
		public Vec3NT globalToLocalVec(Vec3NT vec) {return vec;};
		public Vec3NT localToGlobalVec(Vec3NT vec) {return vec;};
		
		public void addLinearVelocity(Vec3NT v) {};
		public void addAngularVelocity(Vec3NT v) {};
		
		public void addContact(Contact c) {};
	};
	
	static {
		DUMMY.inv_rotation = (Matrix3f) DUMMY.rotation.clone();
		DUMMY.localInertiaTensor = new Matrix3f();
		DUMMY.inv_localInertiaTensor = new Matrix3f();
		DUMMY.inv_globalInertiaTensor = new Matrix3f();
		DUMMY.localCentroid = new Vec3NT(0, 0, 0);
		DUMMY.globalCentroid = new Vec3NT(0, 0, 0);
	}
	
	public World world;
	public AxisAlignedBB boundingBox;
	
	public List<Collider> colliders = new ArrayList<>();
	public List<AxisAlignedBB> colliderBoundingBoxes = new ArrayList<>();
	
	public Vec3NT position = new Vec3NT(0, 0, 0);
	public Vec3NT globalCentroid;
	public Matrix3f rotation = new Matrix3f();
	public Matrix3f inv_rotation;
	
	public Vec3NT prevPosition = new Vec3NT(0, 0, 0);
	public Quat4f prevRotation = new Quat4f();
	
	public Vec3NT linearVelocity = new Vec3NT(0, 0, 0);
	public Vec3NT angularVelocity = new Vec3NT(0, 0, 0);
	public Vec3NT force = new Vec3NT(0, 0, 0);
	public Vec3NT torque = new Vec3NT(0, 0, 0);
	
	public float mass;
	public float inv_mass;
	public Matrix3f localInertiaTensor;
	public Matrix3f inv_localInertiaTensor;
	public Matrix3f inv_globalInertiaTensor;
	public float friction = 0.5F;
	public float restitution = 0;
	
	public Vec3NT localCentroid;
	
	public ContactManifold contacts = new ContactManifold();
	
	public RigidBody(World w) {
		this.world = w;
		rotation.setIdentity();
	}
	
	public RigidBody(World w, double x, double y, double z) {
		this(w);
		this.position = new Vec3NT(x, y, z);
	}
	
	public void minecraftTimestep(){
		this.setPrevData();
		int timeStepSubDiv = 8;
		float step = 0.05F/(float)timeStepSubDiv;
		for(int i = 0; i < timeStepSubDiv; i ++){
			doTimeStep(step);
		}
	}
	
	public void doTimeStep(float dt){
		contacts.update();
		//Do collision detection
		GJKInfo bestInfo = null;
		Collider a = null;
		Collider b = null;
		List<AxisAlignedBB> l = world.getCollisionBoxes(null, boundingBox);
		for(AxisAlignedBB box : l){
			for(int i = 0; i < colliders.size(); i ++){
				Collider c = colliders.get(i);
				if(!colliderBoundingBoxes.get(i).intersects(box))
					continue;
				b = new AABBCollider(box);
				GJKInfo info = GJK.colliding(this, null, c, b);
				if(info.result == Result.COLLIDING /*&& (bestInfo == null || bestInfo.depth < info.depth)*/){
					//No need to find whatever is deepest. We can just add every contact and let the manifold sort itself out correctly.
					//bestInfo = info;
					contacts.addContact(new Contact(this, null, c, b, info));
				}
			}
		}
		
		//if(bestInfo != null){
		//	contacts.addContact(new Contact(this, null, a, b, bestInfo));
		//}
		
		solveContacts(dt);
		integrateVelocityAndPosition(dt);
	}
	
	public void integrateVelocityAndPosition(float dt){
		//Integrate velocity
		linearVelocity = linearVelocity.add(force.mult(inv_mass*dt));
		angularVelocity = angularVelocity.add(torque.mult(dt).matTransform(inv_globalInertiaTensor));
				
		force.setComponents(0, 0, 0);
		torque.setComponents(0, 0, 0);
				
		//Integrate position
		globalCentroid = globalCentroid.add(linearVelocity.mult(dt));
		if(angularVelocity.lengthSquared() > 0){
			Vec3NT axis = angularVelocity.normalize();
			double angle = angularVelocity.length()*dt;
			Matrix3f turn = new Matrix3f();
			turn.set(new AxisAngle4f((float)axis.x, (float)axis.y, (float)axis.z, (float)angle));
			turn.mul(rotation);
			rotation = turn;
			updateOrientation();
		}
		updatePositionFromGlobalCentroid();
		updateAABBs();
		addLinearVelocity(new Vec3NT(0, -9.81*dt, 0));
	}
	
	public void setPrevData(){
		this.prevPosition.set(position);
		setFromMat(prevRotation, rotation);
	}
	
	public void addContact(Contact c){
		contacts.addContact(c);
	}
	
	public void solveContacts(float dt){
		for(int j = 0; j < contacts.contactCount; j ++){
			contacts.contacts[j].init(dt);
		}
		int velocityIterations = 4;
		for(int i = 0; i < velocityIterations; i ++){
			for(int j = 0; j < contacts.contactCount; j ++){
				contacts.contacts[j].solve(dt);
			}
		}
	}
	
	/*public void solveContacts(float dt){
		for(int i = 0; i < contacts.contactCount; i ++){
			Contact c = contacts.contacts[i];
			RigidBody bodyA = c.bodyA;
			RigidBody bodyB = c.bodyB;
			Vec3NT contactNormal = c.normal;
			if(bodyA == RigidBody.DUMMY || bodyA != this){
				bodyA = c.bodyB;
				bodyB = c.bodyA;
				contactNormal = contactNormal.negate();
			}
			Vec3NT rA = c.localA.subtract(c.a.localCentroid);
			Vec3NT rB = c.localB.subtract(c.b.localCentroid);
			Vec3NT[] jacobian = new Vec3NT[]{contactNormal.negate(), rA.crossProduct(contactNormal).negate(), contactNormal, rB.crossProduct(contactNormal)};
			double inv_effectiveMass = 
				  bodyA.inv_mass
				+ jacobian[1].dotProduct(jacobian[1].matTransform(bodyA.inv_globalInertiaTensor))
				+ bodyB.inv_mass
				+ jacobian[3].dotProduct(jacobian[3].matTransform(bodyB.inv_globalInertiaTensor));
			
			double jv = 
				  jacobian[0].dotProduct(bodyA.linearVelocity)
				+ jacobian[1].dotProduct(bodyA.angularVelocity)
				+ jacobian[2].dotProduct(bodyB.linearVelocity)
				+ jacobian[3].dotProduct(bodyB.angularVelocity);
			
			float beta = 0.2F;
			float b = -(beta/dt)*c.depth;
			
			float lambda = (float) ((-(jv+b))/inv_effectiveMass);
			//float oldTotalLambda = c.totalLambda;
			//c.totalLambda = Math.max(0, c.totalLambda + lambda);
			//lambda = c.totalLambda - oldTotalLambda;
			
			bodyA.addLinearVelocity(jacobian[0].mult(bodyA.inv_mass*lambda));
			bodyA.addAngularVelocity(jacobian[1].matTransform(bodyA.inv_globalInertiaTensor).mult(lambda));
			bodyB.addLinearVelocity(jacobian[2].mult(bodyB.inv_mass*lambda));
			bodyB.addAngularVelocity(jacobian[3].matTransform(bodyB.inv_globalInertiaTensor).mult(lambda));
		}
		
	}*/
	
	public Vec3NT localToGlobalPos(Vec3NT pos){
		return pos.matTransform(rotation).add(position);
	}
	public Vec3NT globalToLocalPos(Vec3NT pos){
		return pos.subtract(position).matTransform(inv_rotation);
	}
	public Vec3NT localToGlobalVec(Vec3NT vec){
		return vec.matTransform(rotation);
	}
	public Vec3NT globalToLocalVec(Vec3NT vec){
		return vec.matTransform(inv_rotation);
	}
	
	public void addLinearVelocity(Vec3NT v){
		linearVelocity = linearVelocity.add(v);
	}
	public void addAngularVelocity(Vec3NT v){
		angularVelocity = angularVelocity.add(v);
	}
	public void impulse(Vec3NT force, Vec3NT position){
		this.force = this.force.add(force);
		this.torque = this.torque.add(position.subtract(globalCentroid).crossProduct(force));
	}
	
	public void impulseVelocity(Vec3NT force, Vec3NT position){
		linearVelocity = linearVelocity.add(force.mult(inv_mass));
		angularVelocity = angularVelocity.add(position.subtract(globalCentroid).crossProduct(force).matTransform(inv_globalInertiaTensor));
	}
	
	public void impulseVelocityDirect(Vec3NT force, Vec3NT position){
		linearVelocity = linearVelocity.add(force);
		angularVelocity = angularVelocity.add(position.subtract(globalCentroid).crossProduct(force));
	}
	
	public void updateOrientation(){
		Quat4f quat = new Quat4f();
		float epsilon = 0.00001F;
		//quat.set(rotation);
		setFromMat(quat, rotation);
		quat.normalize();
		BobMathUtil.matrixFromQuat(rotation, quat);
		//System.out.println("1");
		inv_rotation = (Matrix3f) rotation.clone();
		inv_rotation.transpose();
		
		/*Matrix3f bruh = new Matrix3f(0.44135904F, -0.0038072586F, 0.89732254F,
				-0.004132689F, -0.9999893F, -0.0022101535F,
				0.8973211F, -0.002732877F, -0.44137013F);
		
		System.out.println(bruh.m00 + bruh.m11 + bruh.m22 + 1.0f);
		quat.set(bruh);
		System.out.println(quat);
		quat.normalize();
		System.out.println(quat);
		BobMathUtil.matrixFromQuat(bruh, quat);
		System.out.println(bruh);*/
		
		inv_globalInertiaTensor.set(inv_rotation);
		inv_globalInertiaTensor.mul(inv_localInertiaTensor);
		inv_globalInertiaTensor.mul(rotation);
	}
	public void updatePositionFromGlobalCentroid(){
		position = globalCentroid.add(localCentroid.mult(-1).matTransform(rotation));
	}
	public void updateGlobalCentroidFromPosition(){
		globalCentroid = localCentroid.matTransform(rotation).add(position);
	}
	public void updateAABBs(){
		colliderBoundingBoxes.clear();
		double tMaxX, tMaxY, tMaxZ, tMinX, tMinY, tMinZ;
		tMaxX = tMaxY = tMaxZ = -Double.MAX_VALUE;
		tMinX = tMinY = tMinZ = Double.MAX_VALUE;
		for(Collider c : colliders){
			double maxX = GJK.localSupport(this, c, cardinals[0]).x;
			double maxY = GJK.localSupport(this, c, cardinals[1]).y;
			double maxZ = GJK.localSupport(this, c, cardinals[2]).z;
			double minX = GJK.localSupport(this, c, cardinals[3]).x;
			double minY = GJK.localSupport(this, c, cardinals[4]).y;
			double minZ = GJK.localSupport(this, c, cardinals[5]).z;
			colliderBoundingBoxes.add(new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ));
			tMaxX = Math.max(tMaxX, maxX);
			tMaxY = Math.max(tMaxY, maxY);
			tMaxZ = Math.max(tMaxZ, maxZ);
			tMinX = Math.min(tMinX, minX);
			tMinY = Math.min(tMinY, minY);
			tMinZ = Math.min(tMinZ, minZ);
		}
		boundingBox = new AxisAlignedBB(tMinX, tMinY, tMinZ, tMaxX, tMaxY, tMaxZ);
	}
	
	public void addColliders(Collider... collide){
		for(Collider c : collide){
			colliders.add(c);
		}
		localCentroid = new Vec3NT(0, 0, 0);
		mass = 0;
		for(Collider c : colliders){
			mass += c.mass;
			localCentroid = localCentroid.add(c.localCentroid.mult(c.mass));
		}
		inv_mass = 1F/mass;
		localCentroid = localCentroid.mult(inv_mass);
		
		localInertiaTensor = new Matrix3f();
		for(Collider c : colliders){
			//https://en.wikipedia.org/wiki/Parallel_axis_theorem
			Vec3NT colliderToLocal = localCentroid.subtract(c.localCentroid);
			double lenSquared = colliderToLocal.dotProduct(colliderToLocal);
			Matrix3f outerProduct = colliderToLocal.outerProduct(colliderToLocal);
			
			Matrix3f colliderToLocalMat = new Matrix3f();
			colliderToLocalMat.setIdentity();
			colliderToLocalMat.mul((float) lenSquared);
			colliderToLocalMat.sub(outerProduct);
			colliderToLocalMat.mul(c.mass);
			Matrix3f cLocalIT = (Matrix3f) c.localInertiaTensor.clone();
			cLocalIT.add(colliderToLocalMat);
			localInertiaTensor.add(cLocalIT);
		}
		inv_localInertiaTensor = new Matrix3f();
		inv_localInertiaTensor.set(localInertiaTensor);
		
		inv_localInertiaTensor.invert();
		inv_globalInertiaTensor = new Matrix3f();
		updateOrientation();
		updateGlobalCentroidFromPosition();
		this.prevPosition = position;
		updateAABBs();
	}
	
	@SideOnly(Side.CLIENT)
	public void doGlTransform(Vec3NT playerPos, float partialTicks){
		FloatBuffer buf = ClientProxy.AUX_GL_BUFFER;
		Quat4f quat = new Quat4f();
		setFromMat(quat, rotation);
		quat.interpolate(prevRotation, 1-partialTicks);
		quat.normalize();
		Matrix3f rotation = new Matrix3f();
		BobMathUtil.matrixFromQuat(rotation, quat);
		
		buf.put(0, rotation.m00);
		buf.put(1, rotation.m10);
		buf.put(2, rotation.m20);
		buf.put(3, 0);
		buf.put(4, rotation.m01);
		buf.put(5, rotation.m11);
		buf.put(6, rotation.m21);
		buf.put(7, 0);
		buf.put(8, rotation.m02);
		buf.put(9, rotation.m12);
		buf.put(10, rotation.m22);
		buf.put(11, 0);
		
		Vec3NT pos = this.prevPosition.add(this.position.subtract(this.prevPosition).mult(partialTicks)).subtract(playerPos);
		
		buf.put(12, (float)pos.x);
		buf.put(13, (float)pos.y);
		buf.put(14, (float)pos.z);
		buf.put(15, 1);
		
		GlStateManager.multMatrix(buf);
		
		buf.rewind();
	}
	
	//vecmath's matrix to quaternion function is garbage and lwjgl's is a chad that actually works correctly when the rotation is aligned along an axis.
	public static void setFromMat(Quat4f q, Matrix3f mat){
		setFromMat(q, mat.m00, mat.m01, mat.m02, mat.m10, mat.m11, mat.m12, mat.m20, mat.m21, mat.m22);
	}
	
	public static void setFromMat(Quat4f q, float m00, float m01, float m02, float m10,
			float m11, float m12, float m20, float m21, float m22) {

		float s;
		float tr = m00 + m11 + m22;
		if (tr >= 0.0) {
			s = (float) Math.sqrt(tr + 1.0);
			q.w = s * 0.5f;
			s = 0.5f / s;
			q.x = (m21 - m12) * s;
			q.y = (m02 - m20) * s;
			q.z = (m10 - m01) * s;
		} else {
			float max = Math.max(Math.max(m00, m11), m22);
			if (max == m00) {
				s = (float) Math.sqrt(m00 - (m11 + m22) + 1.0);
				q.x = s * 0.5f;
				s = 0.5f / s;
				q.y = (m01 + m10) * s;
				q.z = (m20 + m02) * s;
				q.w = (m21 - m12) * s;
			} else if (max == m11) {
				s = (float) Math.sqrt(m11 - (m22 + m00) + 1.0);
				q.y = s * 0.5f;
				s = 0.5f / s;
				q.z = (m12 + m21) * s;
				q.x = (m01 + m10) * s;
				q.w = (m02 - m20) * s;
			} else {
				s = (float) Math.sqrt(m22 - (m00 + m11) + 1.0);
				q.z = s * 0.5f;
				s = 0.5f / s;
				q.x = (m20 + m02) * s;
				q.y = (m12 + m21) * s;
				q.w = (m10 - m01) * s;
			}
		}
	}
	
	public void renderDebugInfo(Vec3NT offset, float partialTicks){
		GlStateManager.pushMatrix();
		BufferBuilder buf = Tessellator.getInstance().getBuffer();
		GlStateManager.disableDepth();
		for(Contact c : contacts.contacts){
			if(c != null){
				buf.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
				Vec3NT normal = c.normal.mult(0.5F);
				Vec3NT globalA = c.globalA.subtract(offset);
				Vec3NT globalB = c.globalB.subtract(offset);
				buf.pos(globalA.x, globalA.y, globalA.z).color(1, 0, 0, 1).endVertex();
				buf.pos(globalA.x-normal.x, globalA.y-normal.y, globalA.z-normal.z).color(1, 0, 0, 1).endVertex();
				
				buf.pos(globalB.x, globalB.y, globalB.z).color(1, 0, 0, 1).endVertex();
				buf.pos(globalB.x+normal.x, globalB.y+normal.y, globalB.z+normal.z).color(1, 0, 0, 1).endVertex();
				Tessellator.getInstance().draw();
				
				GL11.glPointSize(16);
				buf.begin(GL11.GL_POINTS, DefaultVertexFormats.POSITION_COLOR);
				buf.pos(globalA.x, globalA.y, globalA.z).color(1, 0, 0, 1).endVertex();
				buf.pos(globalB.x, globalB.y, globalB.z).color(1, 0, 0, 1).endVertex();
				Tessellator.getInstance().draw();
			}
		}
		GlStateManager.enableDepth();
		doGlTransform(offset, partialTicks);
		for(Collider c : colliders){
			c.debugRender();
		}
		GlStateManager.popMatrix();
	}
}
