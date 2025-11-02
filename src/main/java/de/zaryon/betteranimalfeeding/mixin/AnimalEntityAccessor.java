package de.zaryon.betteranimalfeeding.mixin;

import net.minecraft.entity.passive.AnimalEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AnimalEntity.class)
public interface AnimalEntityAccessor {

    @Accessor("loveTicks")
    int getLoveTicks();

    @Accessor("loveTicks")
    void setLoveTicks(int loveTicks);
}