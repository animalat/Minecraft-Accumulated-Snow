package io.github.animalat.accumulatedsnow.events;

import io.github.animalat.accumulatedsnow.AccumulatedSnow;
import io.github.animalat.accumulatedsnow.commands.SnowCommand;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.command.ConfigCommand;

@Mod.EventBusSubscriber(modid = AccumulatedSnow.MOD_ID)
public class ModEvents {

    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event) {
        // uncomment below for snow test command
//        new SnowCommand(event.getDispatcher());

        ConfigCommand.register(event.getDispatcher());
    }
}
