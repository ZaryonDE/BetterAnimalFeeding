package de.zaryon.betteranimalfeeding;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BetterAnimalFeeding implements ModInitializer {
    public static final String MOD_ID = "betteranimalfeeding";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        BetterAnimalFeedingConfig.load();

        LOGGER.info("Better Animal Feeding loaded successfully!");
        LOGGER.info("Feed forwarding radius: {} blocks", BetterAnimalFeedingConfig.INSTANCE.forwardRadius);
    }
}