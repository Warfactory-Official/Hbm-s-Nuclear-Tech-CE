package com.hbm.api.entity;

import net.minecraft.entity.EntityLivingBase;

// Keep in mind that it MUST NOT rely on Minecraft code to implement it
// They are obfuscated at runtime! that's why I can't use Forge net.minecraftforge.fml.common.registry.IThrowableEntity
public interface IThrowable {
    void setThrower(EntityLivingBase thrower);
}
