package com.hbm.particle.helper;

import java.util.HashMap;

public class ParticleCreators {

    public static HashMap<String, IParticleCreator> particleCreators = new HashMap<>();
    public static final IParticleCreator CASING;
    public static final IParticleCreator FLAME;
    public static final IParticleCreator EXPLOSION_SMALL;
    public static final IParticleCreator EXPLOSION_LARGE;
    public static final IParticleCreator BLACK_POWDER;
    public static final IParticleCreator ASHES;
    public static final IParticleCreator SKELETON;

    static {
        CASING = new CasingCreator();
        FLAME = new FlameCreator();
        EXPLOSION_SMALL = new ExplosionSmallCreator();
        EXPLOSION_LARGE = new ExplosionCreator();
        BLACK_POWDER = new BlackPowderCreator();
        ASHES = new AshesCreator();
        SKELETON = new SkeletonCreator();
        particleCreators.put("casingNT", CASING);
        particleCreators.put("flamethrower", FLAME);
        particleCreators.put("explosionSmall", EXPLOSION_SMALL);
        particleCreators.put("explosionLarge", EXPLOSION_LARGE);
        particleCreators.put("blackPowder", BLACK_POWDER);
        particleCreators.put("ashes", ASHES);
        particleCreators.put("skeleton", SKELETON);
    }
}
