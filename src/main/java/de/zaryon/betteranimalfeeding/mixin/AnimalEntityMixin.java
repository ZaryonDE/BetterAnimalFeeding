package de.zaryon.betteranimalfeeding.mixin;

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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(AnimalEntity.class)
public abstract class AnimalEntityMixin extends PassiveEntity implements CooldownAccess {

    @Shadow public abstract boolean isBreedingItem(ItemStack stack);

    protected AnimalEntityMixin(EntityType<? extends PassiveEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "interactMob", at = @At("HEAD"), cancellable = true)
    private void onInteractMob(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack stack = player.getStackInHand(hand);

        if (this.getEntityWorld().isClient()) return;

        AnimalEntity thisAnimal = (AnimalEntity) (Object) this;
        AnimalEntityAccessor accessor = (AnimalEntityAccessor) this;

        boolean cannotFeed = false;

        if (accessor.getLoveTicks() > 0) cannotFeed = true;     // Already fed
        if (this.getBreedingAge() < 0) cannotFeed = true;       // Just bred
        if (this.getBreedingAge() > 0) cannotFeed = true;       // Is baby

        if (cannotFeed && thisAnimal.isBreedingItem(stack)) {
            System.out.println("[BetterAnimalFeeding] Animal cannot be fed, searching for next...");

            ServerWorld world = (ServerWorld) this.getEntityWorld();
            List<AnimalEntity> nearbyAnimals = world.getEntitiesByClass(
                    AnimalEntity.class,
                    this.getBoundingBox().expand(2.0),
                    e -> {
                        if (e == thisAnimal) return false;
                        if (e.getType() != thisAnimal.getType()) return false;
                        if (e.getBreedingAge() != 0) return false;

                        AnimalEntityAccessor eAccessor = (AnimalEntityAccessor) e;
                        return eAccessor.getLoveTicks() == 0;
                    }
            );

            List<AnimalEntity> testCandidates = nearbyAnimals.isEmpty()
                    ? List.of(thisAnimal)
                    : nearbyAnimals;

            boolean isValidFood = false;
            if (!testCandidates.isEmpty()) {
                isValidFood = testCandidates.get(0).isBreedingItem(stack);
            }

            if (!nearbyAnimals.isEmpty() && isValidFood) {
                AnimalEntity targetAnimal = nearbyAnimals.get(0);

                ActionResult result = targetAnimal.interactMob(player, hand);

                System.out.println("[BetterAnimalFeeding] Forwarded food to next animal");
                cir.setReturnValue(result);
                return;
            } else {
                System.out.println("[BetterAnimalFeeding] No unfed animals in range");
            }

            cir.setReturnValue(ActionResult.PASS);
        }
    }
}