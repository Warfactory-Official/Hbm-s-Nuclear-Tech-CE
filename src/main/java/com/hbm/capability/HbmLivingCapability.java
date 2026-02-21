package com.hbm.capability;

import com.hbm.capability.HbmLivingProps.ContaminationEffect;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

public class HbmLivingCapability {

    public interface IEntityHbmProps {
        double getRads();

        void setRads(double rads);

        void increaseRads(double rads);

        void decreaseRads(double rads);

        double getNeutrons();

        void setNeutrons(double rads);

        double getRadsEnv();

        void setRadsEnv(double rads);

        double getRadBuf();

        void setRadBuf(double buf);

        double getDigamma();

        void setDigamma(double dig);

        void increaseDigamma(double dig);

        void decreaseDigamma(double dig);

		int getAsbestos();
		void setAsbestos(int asbestos);

		int getBlacklung();
		void setBlacklung(int blacklung);

		int getBombTimer();
		void setBombTimer(int bombTimer);

		int getContagion();
		void setContagion(int cont);

		int getOil();
		void setOil(int time);

		int getPhosphorus();
		void setPhosphorus(int time);

		int getFire();
		void setFire(int time);

		int getBalefire();
		void setBalefire(int time);

		List<HbmLivingProps.ContaminationEffect> getContaminationEffectList();

		void saveNBTData(NBTTagCompound tag);
		void loadNBTData(NBTTagCompound tag);
	}

	public static class EntityHbmProps implements IEntityHbmProps {

		public static final Callable<IEntityHbmProps> FACTORY = EntityHbmProps::new;

        private double rads = 0D;
        private double neutrons = 0D;
        private double envRads = 0D;
        private double radBuf = 0D;
        private double digamma = 0D;
		private int asbestos = 0;
		public static final int maxAsbestos = 60 * 60 * 20;
		private int blacklung;
		public static final int maxBlacklung = 60 * 60 * 20;
		private int bombTimer;
		private int contagion;
		private int oil;
		public int phosphorus;
		public int fire;
		public int balefire;
		private final List<HbmLivingProps.ContaminationEffect> contamination = new ArrayList<>();

        @Override
        public double getRads() {
            return rads;
        }

        @Override
        public void setRads(double rads) {
            this.rads = MathHelper.clamp(rads, 0D, 2500D);
        }

		@Override
        public double getNeutrons() {
            return neutrons;
        }

        @Override
        public void setNeutrons(double neutrons) {
            this.neutrons = Math.max(neutrons, 0D);
        }

        @Override
        public void increaseRads(double rads) {
            this.rads = MathHelper.clamp(this.rads + rads, 0D, 2500D);
        }

        @Override
        public void decreaseRads(double rads) {
            this.rads = MathHelper.clamp(this.rads - rads, 0D, 2500D);
        }

		@Override
        public double getRadsEnv() {
            return envRads;
        }

        @Override
        public void setRadsEnv(double rads) {
            envRads = rads;
        }

		@Override
        public double getRadBuf() {
            return radBuf;
        }

        @Override
        public void setRadBuf(double buf) {
            radBuf = buf;
        }

		@Override
        public double getDigamma() {
            return digamma;
        }

        @Override
        public void setDigamma(double dig) {
            digamma = dig;
        }

        @Override
        public void increaseDigamma(double dig) {
            this.digamma = MathHelper.clamp(this.digamma + dig, 0D, 1000D);
        }

        @Override
        public void decreaseDigamma(double dig) {
            this.digamma = MathHelper.clamp(this.digamma - dig, 0D, 1000D);
        }

		@Override
		public int getAsbestos(){
			return asbestos;
		}

		@Override
		public void setAsbestos(int asbestos){
			this.asbestos = asbestos;
		}

		@Override
		public int getBlacklung(){
			return blacklung;
		}

		@Override
		public void setBlacklung(int blacklung){
			this.blacklung = blacklung;
		}

		@Override
		public int getBombTimer(){
			return bombTimer;
		}

		@Override
		public void setBombTimer(int bombTimer){
			this.bombTimer = bombTimer;
		}

		@Override
		public int getContagion(){
			return contagion;
		}

		@Override
		public void setContagion(int cont){
			contagion = cont;
		}

		@Override
		public int getOil() { return oil; }

		@Override public void setOil(int time) { this.oil = time; }
		@Override
		public int getPhosphorus() { return phosphorus; }

		@Override public void setPhosphorus(int phosphorus) { this.phosphorus = phosphorus; }
		@Override
		public int getFire() { return fire; }

		@Override public void setFire(int time) { this.fire = time; }
		@Override
		public int getBalefire() { return balefire; }

		@Override public void setBalefire(int time) { this.balefire = time; }


		@Override
		public List<HbmLivingProps.ContaminationEffect> getContaminationEffectList(){
			return contamination;
		}

		@Override
        public void saveNBTData(NBTTagCompound tag) {
            // Versioned payload (v1): doubles
            tag.setString("fmt", "v1");
            tag.setDouble("rads", this.rads);
            tag.setDouble("neutrons", this.neutrons);
            tag.setDouble("envRads", this.envRads);
            tag.setDouble("radBuf", this.radBuf);
            tag.setDouble("digamma", this.digamma);
            tag.setInteger("asbestos", getAsbestos());
            tag.setInteger("blacklung", blacklung);
            tag.setInteger("bombtimer", bombTimer);
            tag.setInteger("contagion", contagion);
            tag.setInteger("oil", getOil());
            tag.setInteger("fire", getFire());
            tag.setInteger("phosphorus", getPhosphorus());
            tag.setInteger("balefire", getBalefire());
            tag.setInteger("conteffectsize", contamination.size());
            for (int i = 0; i < contamination.size(); i++) {
                contamination.get(i).save(tag, i);
            }
        }

		@Override
        public void loadNBTData(NBTTagCompound tag) {
            final boolean isV1 = tag.hasKey("fmt") && "v1".equals(tag.getString("fmt"));
            if (isV1) {
                this.rads = tag.getDouble("rads");
                this.neutrons = tag.getDouble("neutrons");
                this.envRads = tag.getDouble("envRads");
                this.radBuf = tag.getDouble("radBuf");
                this.digamma = tag.getDouble("digamma");
            } else {
                // Legacy payload (floats)
                this.rads = tag.getFloat("rads");
                this.neutrons = tag.getFloat("neutrons");
                this.envRads = tag.getFloat("envRads");
                this.radBuf = tag.getFloat("radBuf");
                this.digamma = tag.getFloat("digamma");
            }
            setAsbestos(tag.getInteger("asbestos"));
            setBlacklung(tag.getInteger("blacklung"));
            setBombTimer(tag.getInteger("bombtimer"));
            setContagion(tag.getInteger("contagion"));
            setOil(tag.getInteger("oil"));
            setFire(tag.getInteger("fire"));
            setPhosphorus(tag.getInteger("phosphorus"));
            setBalefire(tag.getInteger("balefire"));
            contamination.clear();
            for (int i = 0; i < tag.getInteger("conteffectsize"); i++) {
                contamination.add(HbmLivingProps.ContaminationEffect.load(tag, i));
            }
        }
	}

	public static class EntityHbmPropsStorage implements IStorage<IEntityHbmProps>{

		@Override
		public NBTBase writeNBT(Capability<IEntityHbmProps> capability, IEntityHbmProps instance, EnumFacing side) {
			NBTTagCompound tag = new NBTTagCompound();
			instance.saveNBTData(tag);
			return tag;
		}

		@Override
		public void readNBT(Capability<IEntityHbmProps> capability, IEntityHbmProps instance, EnumFacing side, NBTBase nbt) {
			if(nbt instanceof NBTTagCompound){
				instance.loadNBTData((NBTTagCompound)nbt);
			}
		}

	}

	public static class EntityHbmPropsProvider implements ICapabilitySerializable<NBTBase> {

        public static final IEntityHbmProps DUMMY = new IEntityHbmProps() {
            @Override
            public double getRads() {
                return 0D;
            }

            @Override
            public void setRads(double rads) {
            }

            @Override
            public double getNeutrons() {
                return 0D;
            }

            @Override
            public void setNeutrons(double neutrons) {
            }

            @Override
            public void increaseRads(double rads) {
            }

            @Override
            public void decreaseRads(double rads) {
            }

            @Override
            public double getRadsEnv() {
                return 0D;
            }

            @Override
            public void setRadsEnv(double rads) {
            }

            @Override
            public double getRadBuf() {
                return 0D;
            }

            @Override
            public void setRadBuf(double buf) {
            }

            @Override
            public double getDigamma() {
                return 0D;
            }

            @Override
            public void setDigamma(double dig) {
            }

            @Override
            public void increaseDigamma(double dig) {
            }

            @Override
            public void decreaseDigamma(double dig) {
            }
			@Override
			public int getAsbestos(){
				return 0;
			}
			@Override
			public void setAsbestos(int asbestos){
			}
			@Override
			public void saveNBTData(NBTTagCompound tag){
			}
			@Override
			public void loadNBTData(NBTTagCompound tag){
			}
			@Override
			public List<ContaminationEffect> getContaminationEffectList(){
				return new ArrayList<>(0);
			}
			@Override
			public int getBlacklung(){
				return 0;
			}
			@Override
			public void setBlacklung(int blacklung){
			}
			@Override
			public int getBombTimer(){
				return 0;
			}
			@Override
			public void setBombTimer(int bombTimer){
			}
			@Override
			public int getContagion(){
				return 0;
			}
			@Override
			public void setContagion(int cont){
			}
			@Override
			public int getOil(){ return 0; }
			@Override
			public void setOil(int cont){ }
			@Override
			public int getPhosphorus(){ return 0; }
			@Override
			public void setPhosphorus(int phos){ }
			@Override
			public int getFire(){ return 0; }
			@Override
			public void setFire(int time){ }
			@Override
			public int getBalefire(){ return 0; }
			@Override
			public void setBalefire(int time){ }
		};
		
		@CapabilityInject(IEntityHbmProps.class)
		public static Capability<IEntityHbmProps> ENT_HBM_PROPS_CAP = null;

		private final IEntityHbmProps instance = ENT_HBM_PROPS_CAP.getDefaultInstance();

		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
			return capability == ENT_HBM_PROPS_CAP;
		}

		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
			return capability == ENT_HBM_PROPS_CAP ? ENT_HBM_PROPS_CAP.cast(this.instance) : null;
		}

		@Override
		public NBTBase serializeNBT() {
			return ENT_HBM_PROPS_CAP.getStorage().writeNBT(ENT_HBM_PROPS_CAP, instance, null);
		}

		@Override
		public void deserializeNBT(NBTBase nbt) {
			ENT_HBM_PROPS_CAP.getStorage().readNBT(ENT_HBM_PROPS_CAP, instance, null, nbt);
		}
	}
}
