package me.duncanruns.reroller.mixinint;

import me.duncanruns.reroller.RNGManager;
import me.duncanruns.reroller.SpawnerManager;

public interface RerollerServer {
    RNGManager reroller$getRNGManager();

    SpawnerManager reroller$getSpawnerManager();
}
