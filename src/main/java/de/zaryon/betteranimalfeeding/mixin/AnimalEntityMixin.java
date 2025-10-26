package de.zaryon.betteranimalfeeding.mixin;

import de.zaryon.betteranimalfeeding.BetterAnimalFeedingConfig;
import de.zaryon.betteranimalfeeding.access.CooldownAccess;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Comparator;
import java.util.List;

@Mixin(AnimalEntity.class)
public abstract class AnimalEntityMixin extends PassiveEntity implements CooldownAccess {

    protected AnimalEntityMixin(EntityType<? extends PassiveEntity> entityType, World world) {
        super(entityType, world);
    }

    @Shadow
    public abstract boolean isBreedingItem(ItemStack stack);

    @Shadow
    private int loveTicks;

    @Override
    public void betterAnimalFeeding$setCooldown(int ticks) {}

    @Override
    public int betterAnimalFeeding$getCooldown() {
        return 0;
    }

    @Inject(method = "interactMob", at = @At("HEAD"), cancellable = true)
    private void onInteractMob(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        if (this.getWorld().isClient) return;

        ItemStack stack = player.getStackInHand(hand);
        AnimalEntity thisAnimal = (AnimalEntity) (Object) this;

        boolean cannotFeed = false;

        if (this.loveTicks > 0) {
            cannotFeed = true;
        }

        if (this.getBreedingAge() < 0) {
            cannotFeed = true;
        }

        if (this.getBreedingAge() > 0) {
            cannotFeed = true;
        }

        if (cannotFeed) {
            ServerWorld world = (ServerWorld) this.getWorld();

            boolean isBreedingFood = false;

            List<AnimalEntity> testAnimals = world.getEntitiesByClass(
                    AnimalEntity.class,
                    this.getBoundingBox().expand(5.0),
                    e -> e.getType() == thisAnimal.getType() && e.getBreedingAge() == 0
            );

            if (!testAnimals.isEmpty()) {
                isBreedingFood = testAnimals.get(0).isBreedingItem(stack);
            } else {
                isBreedingFood = true;
            }

            if (isBreedingFood) {
                double radius = BetterAnimalFeedingConfig.INSTANCE.forwardRadius;

                List<AnimalEntity> candidates = world.getEntitiesByClass(
                        AnimalEntity.class,
                        this.getBoundingBox().expand(radius),
                        e -> {
                            if (e == thisAnimal) return false;
                            if (e.getType() != thisAnimal.getType()) return false;
                            if (e.getBreedingAge() != 0) return false;

                            return ((AnimalEntityAccessor) e).getLoveTicks() == 0;
                        }
                );

                if (!candidates.isEmpty()) {
                    AnimalEntity nearest = candidates.stream()
                            .min(Comparator.comparingDouble(e -> e.squaredDistanceTo(player)))
                            .orElse(null);

                    if (nearest != null) {
                        ActionResult result = nearest.interactMob(player, hand);

                        if (result.isAccepted()) {
                            cir.setReturnValue(result);
                            return;
                        }
                    }
                }

                cir.setReturnValue(ActionResult.PASS);
            }
        }
    }
}